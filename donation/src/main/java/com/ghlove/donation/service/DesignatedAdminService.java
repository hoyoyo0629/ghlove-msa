package com.ghlove.donation.service;

import com.ghlove.donation.domain.DesignatedPart;
import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.DesignatedNotice;
import com.ghlove.donation.domain.DesignatedNoticeFile;
import com.ghlove.donation.domain.DesignatedNoticeImgExpln;
import com.ghlove.donation.domain.DesignatedProjectImage;
import com.ghlove.donation.domain.DesignatedProjectImgExpln;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.DsgnAprvLog;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.domain.PrjNotice;
import com.ghlove.donation.repository.CommonCodeRepository;
import com.ghlove.donation.repository.DesignatedPartRepository;
import com.ghlove.donation.repository.DesignatedProjectRepository;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.DsgnAprvLogRepository;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.PrjNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 지정기부(designated-donation) 관리자 CRUD (AS-IS opmanager/designated-donation).
 * admin 콘솔이 이 서비스를 cross-service로 호출한다 - donation이 G_DSGN_DNTN_BIZ_MNG 등의
 * 실제 데이터를 소유하기 때문(DB per Service). 갤러리 이미지 관리는 이 프로젝트의 공개
 * 상세화면 자체가 대표이미지 1장+본문 임베드 구조로 이미 스코프 밖 결정이 나 있어
 * (designated-detail.html 주석 참고) 여기서도 다시 만들지 않는다.
 */
@Service
@RequiredArgsConstructor
public class DesignatedAdminService {

    /** 이 프로젝트는 AS-IS의 1/2/9(대기/진행중/종료) 대신 이미 OPEN/CLOSED 2단계로
     *  단순화되어 있었다(DonationService.DSGN_STATUS_OPEN/CLOSED, 공개 목록 조회 쿼리가
     *  이 값을 그대로 사용 중이라 값 자체를 바꿀 수 없음). "담당자 자기승인 금지"
     *  규칙을 재현하려면 대기 상태가 필요해 PENDING을 세 번째 값으로 추가했다 - 컬럼이
     *  VARCHAR(10)이라 제약조건 위반 없이 확장 가능하고, PENDING인 동안은 openProjects()
     *  쿼리(OPEN만 조회)에 안 걸려 공개화면에 자연히 노출되지 않는다. */
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    /** AS-IS 목록의 부서명 기본값({@code IFNULL(..., '부서 미지정')}). */
    private static final String DEPT_UNASSIGNED = "부서 미지정";
    private static final BigInteger MAX_AMOUNT = BigInteger.valueOf(999_999_999_999L);
    private static final long MAX_DURATION_DAYS = 365L * 3;

    private static final String STATUS_COMPLETED = "COMPLETED";

    // 메서드 안에 승인이력 지역변수 log가 있어 @Slf4j 대신 이름을 달리 둔다.
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DesignatedAdminService.class);

    private final DesignatedProjectRepository designatedProjectRepository;
    private final PrjNoticeRepository prjNoticeRepository;
    private final DesignatedPartRepository designatedPartRepository;
    private final DsgnAprvLogRepository dsgnAprvLogRepository;
    private final DonationRepository donationRepository;
    private final LocgovRepository locgovRepository;
    // ★ DonationService를 주입하면 순환참조가 된다(DonationService가 autoCloseIfGoalReached를
    //   쓰려고 이 서비스를 이미 주입하고 있다). 코드표 조회는 저장소를 직접 쓴다.
    private final CommonCodeRepository commonCodeRepository;
    private final FileStorageService fileStorageService;
    private final com.ghlove.donation.repository.DesignatedNoticeRepository designatedNoticeRepository;
    private final com.ghlove.donation.repository.DesignatedNoticeFileRepository designatedNoticeFileRepository;
    private final com.ghlove.donation.repository.DesignatedNoticeImgExplnRepository designatedNoticeImgExplnRepository;
    /** 대민 공지 첨부의 저장 파일명 토큰(AS-IS pCrypto "normal" 자리) - 직인 이미지와 같은 AES-GCM 대체 모듈. */
    private final SealCipher sealCipher;
    private final com.ghlove.donation.repository.DesignatedProjectImageRepository designatedProjectImageRepository;
    private final com.ghlove.donation.repository.DesignatedProjectImgExplnRepository designatedProjectImgExplnRepository;

    public List<DesignatedProject> listAll() {
        return designatedProjectRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getDsgnDntnBizId(), a.getDsgnDntnBizId()))
                .toList();
    }

    public DesignatedProject findOrThrow(Long id) {
        return designatedProjectRepository.findById(id)
                .orElseThrow(() -> new DonationException("지정기부사업을 찾을 수 없습니다."));
    }

    /** 코드유형 → {코드값: 라벨}. {@code DonationService.codesOf}와 같은 조회인데, 그 서비스를
     *  주입하면 순환참조가 되므로 같은 저장소 호출을 여기에 둔다(서비스 간 공유 모듈이 없다). */
    private Map<String, String> codesOf(String codeType) {
        return commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y").stream()
                .collect(Collectors.toMap(
                        com.ghlove.donation.domain.CommonCode::getId,
                        com.ghlove.donation.domain.CommonCode::getLabel,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** true면 ROLE_OPERATOR 등급(AS-IS의 지정기부 담당자)이라 진행중(승인) 상태로 직접
     *  전환할 수 없다 - 반드시 상급자(ROLE_ADMIN)가 승인해야 한다(AS-IS 자기 승인 금지 규칙). */
    @Transactional
    public DesignatedProject create(DesignatedProject form, MultipartFile image, List<MultipartFile> images,
                                    boolean canSelfApprove, Long managerId) {
        return create(form, image, images, null, canSelfApprove, managerId);
    }

    @Transactional
    public DesignatedProject create(DesignatedProject form, MultipartFile image, List<MultipartFile> images,
                                    List<String> imgExplns, boolean canSelfApprove, Long managerId) {
        validate(form);
        if (STATUS_OPEN.equals(form.getDsgnDntnBizSttsCd()) && !canSelfApprove) {
            form.setDsgnDntnBizSttsCd(STATUS_PENDING);
        }
        if (image != null && !image.isEmpty()) {
            form.setImageUrl("/uploads/donation/designated-project/" + fileStorageService.store(image, "designated-project"));
        }
        form.setFrstRgtrId(managerId);
        form.setLastRgtrId(managerId);
        form.setFrstRegDt(LocalDateTime.now());
        form.setLastRegDt(LocalDateTime.now());
        DesignatedProject saved = designatedProjectRepository.save(form);
        storeImages(saved, images, managerId);
        replaceProjectImgExplns(saved.getDsgnDntnBizId(), imgExplns, managerId);
        writeApprovalLog(saved.getDsgnDntnBizId(), null, saved.getDsgnDntnBizSttsCd(), managerId);
        return saved;
    }

    @Transactional
    public DesignatedProject update(Long id, DesignatedProject form, MultipartFile image, List<MultipartFile> images,
                                    boolean canSelfApprove, Long managerId) {
        return update(id, form, image, images, null, canSelfApprove, managerId);
    }

    @Transactional
    public DesignatedProject update(Long id, DesignatedProject form, MultipartFile image, List<MultipartFile> images,
                                    List<String> imgExplns, boolean canSelfApprove, Long managerId) {
        validate(form);
        DesignatedProject entity = findOrThrow(id);
        String beforeStatus = entity.getDsgnDntnBizSttsCd();

        String requestedStatus = form.getDsgnDntnBizSttsCd();
        if (STATUS_OPEN.equals(requestedStatus) && !STATUS_OPEN.equals(beforeStatus) && !canSelfApprove) {
            requestedStatus = STATUS_PENDING;
        }

        entity.setDsgnDntnBizTtl(form.getDsgnDntnBizTtl());
        entity.setDsgnDntnBizCn(form.getDsgnDntnBizCn());
        entity.setDsgnDntnBizBgngYmd(form.getDsgnDntnBizBgngYmd());
        entity.setDsgnDntnBizEndYmd(form.getDsgnDntnBizEndYmd());
        entity.setGoalAmt(form.getGoalAmt());
        entity.setDsgnDntnBizSttsCd(requestedStatus);
        entity.setRlsYn(form.getRlsYn());
        entity.setLclgvCd(form.getLclgvCd());
        entity.setDsgnDntnBizSeCd(form.getDsgnDntnBizSeCd());
        entity.setBsnsSubType(form.getBsnsSubType());
        entity.setContentEtc(form.getContentEtc());
        entity.setDeptId(form.getDeptId());
        entity.setLastRgtrId(managerId);
        entity.setLastRegDt(LocalDateTime.now());
        if (image != null && !image.isEmpty()) {
            entity.setImageUrl("/uploads/donation/designated-project/" + fileStorageService.store(image, "designated-project"));
        }

        DesignatedProject saved = designatedProjectRepository.save(entity);
        storeImages(saved, images, managerId);
        replaceProjectImgExplns(id, imgExplns, managerId);
        if (!beforeStatus.equals(saved.getDsgnDntnBizSttsCd())) {
            writeApprovalLog(id, beforeStatus, saved.getDsgnDntnBizSttsCd(), managerId);
        }
        return saved;
    }

    /** AS-IS form.jsp 의 "이미지 설명" 표(G_DSGN_DNTN_BIZ_CN_IMG_EXPLN).
     *
     *  <p>AS-IS saveDesignatedDonationInfo 와 같은 규칙이다 - 넘어온 목록이 있으면
     *  <b>전부 지우고 다시 넣고</b>, 비어 있으면 <b>전부 지운다</b>. 공백만 든 줄은
     *  AS-IS가 화면에서 미리 걷어내므로(not null 컬럼) 여기서도 건너뛴다. */
    private void replaceProjectImgExplns(Long dsgnDntnBizId, List<String> imgExplns, Long managerId) {
        if (imgExplns == null) {
            return;
        }
        designatedProjectImgExplnRepository.deleteByDsgnDntnBizId(dsgnDntnBizId);
        designatedProjectImgExplnRepository.flush();
        int seq = 0;
        for (String expln : imgExplns) {
            if (expln == null || expln.isBlank()) {
                continue;
            }
            designatedProjectImgExplnRepository.save(
                    new DesignatedProjectImgExpln(dsgnDntnBizId, ++seq, expln, managerId));
        }
    }

    /** 수정 화면이 기존 설명을 다시 그릴 때 쓴다(순번 오름차순). */
    public List<String> imgExplnsOf(Long dsgnDntnBizId) {
        return designatedProjectImgExplnRepository.findByDsgnDntnBizIdOrderByImgSeqAsc(dsgnDntnBizId)
                .stream()
                .map(DesignatedProjectImgExpln::getImgExpln)
                .toList();
    }


    /** 업로드 경로 접두어 - 단일 imageUrl 과 같은 규칙을 쓴다. */
    private static final String IMAGE_URL_PREFIX = "/uploads/donation/designated-project/";

    /**
     * AS-IS {@code prjImageFiles[]} - 이미지를 여러 장 받아 뒤에 이어 붙인다.
     *
     * <p>기존 이미지를 지우지 않는다(AS-IS도 추가 등록이고, 삭제는 썸네일의 x 로 한 장씩 한다).
     * 저장 뒤 대표 이미지({@code imageUrl})를 첫 장으로 맞춰 둔다 - 목록·상세 등 기존 화면이
     * 그 칸만 보기 때문이다.
     */
    private void storeImages(DesignatedProject saved, List<MultipartFile> images, Long managerId) {
        if (images == null || images.isEmpty()) {
            return;
        }
        Long projectId = saved.getDsgnDntnBizId();
        List<DesignatedProjectImage> exist =
                designatedProjectImageRepository.findByDsgnDntnBizIdOrderByImgSeqAsc(projectId);
        int seq = exist.isEmpty() ? 0 : exist.get(exist.size() - 1).getImgSeq();
        boolean added = false;
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String stored = fileStorageService.store(file, "designated-project");
            designatedProjectImageRepository.save(
                    new DesignatedProjectImage(projectId, stored, ++seq, managerId));
            added = true;
        }
        if (added) {
            syncRepresentativeImage(saved);
        }
    }

    /** 대표 이미지 = 순서가 가장 앞선 것. 한 장도 없으면 비운다. */
    private void syncRepresentativeImage(DesignatedProject project) {
        List<DesignatedProjectImage> all =
                designatedProjectImageRepository.findByDsgnDntnBizIdOrderByImgSeqAsc(project.getDsgnDntnBizId());
        project.setImageUrl(all.isEmpty() ? null : IMAGE_URL_PREFIX + all.get(0).getImgNm());
        designatedProjectRepository.save(project);
    }

    /** 사업에 달린 이미지 목록(노출 순서). */
    public List<DesignatedProjectImage> imagesOf(Long projectId) {
        return designatedProjectImageRepository.findByDsgnDntnBizIdOrderByImgSeqAsc(projectId);
    }

    /** AS-IS 썸네일의 x 버튼 - 한 장만 지우고 대표 이미지를 다시 맞춘다. */
    @Transactional
    public void deleteImage(Long imageId) {
        designatedProjectImageRepository.findById(imageId).ifPresent(img -> {
            Long projectId = img.getDsgnDntnBizId();
            designatedProjectImageRepository.delete(img);
            designatedProjectImageRepository.flush();
            designatedProjectRepository.findById(projectId).ifPresent(this::syncRepresentativeImage);
        });
    }
    public List<DsgnAprvLog> approvalLogOf(Long id) {
        return dsgnAprvLogRepository.findByDsgnDntnBizIdOrderByLogIdDesc(id);
    }

    private void writeApprovalLog(Long id, String before, String after, Long managerId) {
        DsgnAprvLog log = new DsgnAprvLog();
        log.setDsgnDntnBizId(id);
        log.setBeforeStatusCode(before);
        log.setAfterStatusCode(after);
        log.setFrstRgtrId(managerId);
        log.setFrstRegistPnttm(LocalDateTime.now());
        dsgnAprvLogRepository.save(log);
    }

    // ---- 자동 상태전환 (AS-IS view_dsgn_dntn_biz_list의 상태자동전환 로직 재현) ----
    // AS-IS는 조회 시점에 뷰가 매번 재계산했지만(DB에 실제로 반영되지 않음), 이 프로젝트는
    // 저장된 DSGN_DNTN_BIZ_STTS_CD를 admin 목록/공개 목록이 그대로 신뢰하므로, 전환 시점에
    // 실제로 CLOSED로 저장하고 기존 승인로그 메커니즘에 남긴다 - 화면마다 재계산할 필요가 없고
    // 감사로그도 자연히 남는다. FRST_RGTR_ID는 NOT NULL이라 시스템 자동전환은 SYSTEM_MANAGER_ID(0)로 남긴다.
    private static final Long SYSTEM_MANAGER_ID = 0L;

    /** 모금액이 목표금액을 초과하면 자동 종료. 기부 완료 처리 직후(DonationService.completeDonation)
     *  호출된다 - 그 시점이 바로 모금액이 바뀌는 유일한 순간이라 이벤트 기반으로 충분하고
     *  배치가 필요 없다. */
    @Transactional
    public void autoCloseIfGoalReached(Long dsgnDntnBizId) {
        if (dsgnDntnBizId == null) {
            return;
        }
        designatedProjectRepository.findById(dsgnDntnBizId).ifPresent(project -> {
            if (!STATUS_OPEN.equals(project.getDsgnDntnBizSttsCd()) || project.getGoalAmt() == null) {
                return;
            }
            BigDecimal raised = sum(donationRepository.findByDsgnDntnBizIdAndCntrSttusCode(dsgnDntnBizId, STATUS_COMPLETED));
            if (raised.compareTo(BigDecimal.valueOf(project.getGoalAmt())) > 0) {
                closeProject(project, "목표금액 초과로 자동 종료됨");
            }
        });
    }

    /** 모금 종료일이 지난 진행중(OPEN) 사업을 매일 자동 종료. 목표금액 초과와 달리 "기간이
     *  그냥 흘러서" 발생하는 전환이라 어떤 이벤트에도 걸리지 않으므로 배치가 필요하다.
     *
     *  <p>정기 실행 트리거는 {@link DesignatedProjectBatchScheduler}로 옮겼다(2026-10-03) -
     *  배치 실행로그(메뉴 7209)를 남기려면 트랜잭션 프록시 밖에서 시작·종료시각을 재고 결과를
     *  보고해야 하는데, 같은 빈 안에서 호출하면 프록시를 타지 않아 @Transactional이 무효가 된다.
     *  이 메서드는 그대로 공개 API({@code DesignatedAdminApiController}의 수동 실행)로도 쓰인다. */
    @Transactional
    public int closeExpiredProjects() {
        String today = YMD.format(LocalDate.now());
        List<DesignatedProject> expired = designatedProjectRepository.findAll().stream()
                .filter(p -> STATUS_OPEN.equals(p.getDsgnDntnBizSttsCd()))
                .filter(p -> p.getDsgnDntnBizEndYmd() != null && p.getDsgnDntnBizEndYmd().compareTo(today) < 0)
                .toList();
        for (DesignatedProject project : expired) {
            closeProject(project, "모금 기간 종료로 자동 종료됨");
        }
        return expired.size();
    }

    private void closeProject(DesignatedProject project, String reasonForLog) {
        String before = project.getDsgnDntnBizSttsCd();
        project.setDsgnDntnBizSttsCd(STATUS_CLOSED);
        project.setLastRegDt(LocalDateTime.now());
        designatedProjectRepository.save(project);
        writeApprovalLog(project.getDsgnDntnBizId(), before, STATUS_CLOSED, SYSTEM_MANAGER_ID);
    }

    private static BigDecimal sum(List<Donation> donations) {
        return donations.stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void validate(DesignatedProject form) {
        if (form.getDsgnDntnBizTtl() == null || form.getDsgnDntnBizTtl().isBlank()) {
            throw new DonationException("사업명을 입력해 주세요.");
        }
        if (form.getLclgvCd() == null || form.getLclgvCd().isBlank()) {
            throw new DonationException("지자체를 선택해 주세요.");
        }
        LocalDate start = parseDate(form.getDsgnDntnBizBgngYmd(), "모금 시작일");
        LocalDate end = parseDate(form.getDsgnDntnBizEndYmd(), "모금 종료일");
        if (!end.isAfter(start) && !end.isEqual(start)) {
            throw new DonationException("모금 종료일은 시작일 이후여야 합니다.");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) > MAX_DURATION_DAYS) {
            throw new DonationException("모금 기간은 최대 3년까지 가능합니다.");
        }
        if (form.getGoalAmt() == null || form.getGoalAmt() < 1
                || BigInteger.valueOf(form.getGoalAmt()).compareTo(MAX_AMOUNT) > 0) {
            throw new DonationException("목표금액은 1원 이상 999,999,999,999원 이하여야 합니다.");
        }
    }

    private static LocalDate parseDate(String ymd, String label) {
        if (ymd == null || ymd.length() != 8) {
            throw new DonationException(label + "을(를) 입력해 주세요.");
        }
        try {
            return LocalDate.parse(ymd, YMD);
        } catch (DateTimeParseException e) {
            throw new DonationException(label + " 형식이 올바르지 않습니다.");
        }
    }

    // ---- 공지사항 (AS-IS G_DSGN_DNTN_BIZ_NTC_MNG) ----
    //
    // 2026-10-08: AS-IS에 없는 이름의 표(G_DSGN_PRJ_NOTICE)를 쓰고 있던 것을 AS-IS 실명 표로
    // 옮겼다. 그 표에는 공개여부(RLS_YN)·첨부·이미지설명이 없어 AS-IS 화면을 만들 수 없었다.
    // [[no-invented-features-ask-first]] / ADMIN_COMMON_CODE 전환과 같은 유형.

    /** AS-IS 허용 확장자 - 등록화면 안내문구에 적힌 그대로다. */
    private static final java.util.Set<String> NOTICE_FILE_EXTS = java.util.Set.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "hwpx", "zip");

    /** 운영자 목록 - 공개/비공개 전부. AS-IS ORDER BY 공지ID DESC. */
    public List<DesignatedNotice> noticesOf(Long dsgnDntnBizId) {
        return designatedNoticeRepository.findByDsgnDntnBizIdOrderByNoticeIdDesc(dsgnDntnBizId);
    }

    /** 대민 목록 - AS-IS 매퍼가 display=='FRONT' 일 때 RLS_YN='Y' 를 건다. */
    public List<DesignatedNotice> publicNoticesOf(Long dsgnDntnBizId) {
        return designatedNoticeRepository.findByDsgnDntnBizIdAndRlsYnOrderByNoticeIdDesc(dsgnDntnBizId, "Y");
    }

    public DesignatedNotice noticeOrThrow(Long noticeId) {
        return designatedNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new DonationException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public DesignatedNotice createNotice(Long dsgnDntnBizId, String subject, String content, String rlsYn,
                                          List<MultipartFile> files, List<String> imgExplns, Long managerId) {
        findOrThrow(dsgnDntnBizId);
        validateNotice(subject);
        DesignatedNotice notice = new DesignatedNotice();
        notice.setDsgnDntnBizId(dsgnDntnBizId);
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setRlsYn(normalizeRlsYn(rlsYn));
        notice.setFrstRgtrId(managerId);
        notice.setLastRgtrId(managerId);
        notice.setFrstRegDt(LocalDateTime.now());
        notice.setLastRegDt(LocalDateTime.now());
        DesignatedNotice saved = designatedNoticeRepository.save(notice);
        storeNoticeFiles(saved.getNoticeId(), files, managerId);
        replaceImgExplns(saved.getNoticeId(), imgExplns, managerId);
        return saved;
    }

    @Transactional
    public DesignatedNotice updateNotice(Long noticeId, String subject, String content, String rlsYn,
                                          List<MultipartFile> files, List<String> imgExplns, Long managerId) {
        DesignatedNotice notice = noticeOrThrow(noticeId);
        validateNotice(subject);
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setRlsYn(normalizeRlsYn(rlsYn));
        notice.setLastRgtrId(managerId);
        notice.setLastRegDt(LocalDateTime.now());
        DesignatedNotice saved = designatedNoticeRepository.save(notice);
        // 첨부는 AS-IS처럼 "덧붙이기"다(기존 파일은 x 로 한 장씩 지운다).
        storeNoticeFiles(noticeId, files, managerId);
        // 이미지 설명은 화면이 보낸 전체로 교체한다(행 추가/삭제를 화면에서 하기 때문).
        replaceImgExplns(noticeId, imgExplns, managerId);
        return saved;
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        designatedNoticeFileRepository.deleteByNoticeId(noticeId);
        designatedNoticeImgExplnRepository.deleteByNoticeId(noticeId);
        designatedNoticeRepository.deleteById(noticeId);
    }

    /** AS-IS 목록 하단의 [공개]/[비공개] 일괄 버튼. @return 바뀐 건수 */
    @Transactional
    public int updateNoticeDisplay(List<Long> noticeIds, String rlsYn, Long managerId) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return 0;
        }
        String value = normalizeRlsYn(rlsYn);
        int changed = 0;
        for (Long id : noticeIds) {
            DesignatedNotice notice = designatedNoticeRepository.findById(id).orElse(null);
            if (notice == null || value.equals(notice.getRlsYn())) {
                continue;
            }
            notice.setRlsYn(value);
            notice.setLastRgtrId(managerId);
            notice.setLastRegDt(LocalDateTime.now());
            designatedNoticeRepository.save(notice);
            changed++;
        }
        return changed;
    }

    public List<DesignatedNoticeFile> noticeFilesOf(Long noticeId) {
        return designatedNoticeFileRepository.findByNoticeIdOrderByFileSeqAsc(noticeId);
    }

    /** 대민 공지 첨부 한 줄 - AS-IS PrjNoticeFile 복사본(getPrjFiles)의 값, fileName은 암호화 토큰. */
    public record PublicNoticeFile(Long prjNoticeId, Integer fileSeq, String fileName, String orgFileName) {
    }

    /**
     * 대민 공지 첨부 목록 - AS-IS {@code DesignatedDonationController.getNoticeList}: 첨부(FILE_SEQ DESC)의 저장
     * 파일명을 {@code pCrypto.Encrypt("normal", ...)}로 바꿔 내려보낸다. 암호화가 실패하면 AS-IS처럼 로그만 남기고
     * 원래 값을 그대로 둔다.
     */
    public List<PublicNoticeFile> publicNoticeFilesOf(Long noticeId) {
        List<PublicNoticeFile> files = new ArrayList<>();
        for (DesignatedNoticeFile f : designatedNoticeFileRepository.findByNoticeIdOrderByFileSeqDesc(noticeId)) {
            String fileName = f.getFileNm();
            try {
                fileName = sealCipher.encryptText(f.getFileNm());
            } catch (RuntimeException e) {
                LOG.error("ERROR: {}", "=========== getNoticeList =============");
            }
            files.add(new PublicNoticeFile(f.getNoticeId(), f.getFileSeq(), fileName, f.getOrgnflNm()));
        }
        return files;
    }

    /**
     * 대민 공지 첨부 내려받기 - AS-IS {@code ProgramFileDownloadController.downloadByProgramData}(PRJ_NOTICE)의 파일
     * 쪽: 토큰을 복호화하고 {@code "../"}·{@code "..\"} 를 지운 뒤(AS-IS 정규식 그대로) 그 공지의 첨부 파일을 읽는다.
     * AS-IS는 공지별 폴더(prjNotice/{공지ID}/)에서 찾았고, TO-BE는 첨부를 한 폴더에 두므로 "그 공지의 첨부인지"를
     * 첨부 표로 확인한다.
     *
     * @return 파일 내용, 토큰이 깨졌거나 그 공지의 첨부가 아니거나 파일이 없으면 null
     */
    public byte[] noticeFileContent(Long noticeId, String fileNameToken) {
        String fileName;
        try {
            fileName = sealCipher.decryptText(fileNameToken);
        } catch (RuntimeException e) {
            return null;
        }
        fileName = fileName.replaceAll("../", "").replaceAll("..\\\\", "");
        DesignatedNoticeFile file = designatedNoticeFileRepository.findFirstByNoticeIdAndFileNm(noticeId, fileName)
                .orElse(null);
        if (file == null) {
            return null;
        }
        java.nio.file.Path path = fileStorageService.resolve(file.getFileNm(), file.getPathNm());
        try {
            return java.nio.file.Files.exists(path) ? java.nio.file.Files.readAllBytes(path) : null;
        } catch (java.io.IOException e) {
            LOG.error("CustomFileService getFileDownloadDataByProgramNameIdFileOrgName error", e);
            return null;
        }
    }

    public DesignatedNoticeFile noticeFileOrThrow(Long noticeId, Integer fileSeq) {
        return designatedNoticeFileRepository.findByNoticeIdAndFileSeq(noticeId, fileSeq)
                .orElseThrow(() -> new DonationException("첨부파일을 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteNoticeFile(Long noticeId, Integer fileSeq) {
        designatedNoticeFileRepository.findByNoticeIdAndFileSeq(noticeId, fileSeq)
                .ifPresent(designatedNoticeFileRepository::delete);
    }

    public List<DesignatedNoticeImgExpln> noticeImgExplnsOf(Long noticeId) {
        return designatedNoticeImgExplnRepository.findByNoticeIdOrderByImgSeqAsc(noticeId);
    }

    private void validateNotice(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new DonationException("제목을 입력해 주세요.");
        }
    }

    /** AS-IS 라디오는 Y/N 뿐이다. 값이 비면 공개(Y)로 본다(AS-IS 신규등록 기본값). */
    private static String normalizeRlsYn(String rlsYn) {
        return "N".equals(rlsYn) ? "N" : "Y";
    }

    private void storeNoticeFiles(Long noticeId, List<MultipartFile> files, Long managerId) {
        if (files == null || files.isEmpty()) {
            return;
        }
        List<DesignatedNoticeFile> exist = designatedNoticeFileRepository.findByNoticeIdOrderByFileSeqAsc(noticeId);
        int seq = exist.isEmpty() ? 0 : exist.get(exist.size() - 1).getFileSeq();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            int dot = original.lastIndexOf('.');
            String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase();
            if (!NOTICE_FILE_EXTS.contains(ext)) {
                throw new DonationException("첨부할 수 없는 확장자입니다: " + (ext.isEmpty() ? original : ext));
            }
            String stored = fileStorageService.store(file, "designated-notice");
            designatedNoticeFileRepository.save(new DesignatedNoticeFile(
                    noticeId, ++seq, stored, "designated-notice", original, managerId));
        }
    }

    private void replaceImgExplns(Long noticeId, List<String> imgExplns, Long managerId) {
        if (imgExplns == null) {
            return;
        }
        designatedNoticeImgExplnRepository.deleteByNoticeId(noticeId);
        designatedNoticeImgExplnRepository.flush();
        int seq = 0;
        for (String expln : imgExplns) {
            if (expln == null || expln.isBlank()) {
                continue;
            }
            designatedNoticeImgExplnRepository.save(
                    new DesignatedNoticeImgExpln(noticeId, ++seq, expln, managerId));
        }
    }

    // ---- 담당부서 ----

    public List<DesignatedPart> departmentsAll() {
        return designatedPartRepository.findAllByOrderByDeptIdDesc();
    }

    public List<DesignatedPart> departmentsOf(String locgovCode) {
        return designatedPartRepository.findByLocgovCodeOrderByDeptIdDesc(locgovCode);
    }

    @Transactional
    public DesignatedPart createDepartment(String deptNm, String locgovCode, Long managerId) {
        if (deptNm == null || deptNm.isBlank()) {
            throw new DonationException("부서명을 입력해 주세요.");
        }
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new DonationException("지자체를 선택해 주세요.");
        }
        DesignatedPart part = new DesignatedPart();
        part.setDeptNm(deptNm);
        part.setLocgovCode(locgovCode);
        part.setUseYn("Y");
        part.setFrstRgtrId(managerId);
        part.setLastRgtrId(managerId);
        part.setFrstRegDt(LocalDateTime.now());
        part.setLastRegDt(LocalDateTime.now());
        return designatedPartRepository.save(part);
    }

    /**
     * AS-IS {@code selectDsgncntrPartMngList} - 사업부서 관리(AS-IS {@code part/list.jsp}) 검색.
     *
     * <p>조건 4종: 지자체(시군구가 있으면 그것, 없으면 광역 하위 전체) · 부서명 부분일치 ·
     * 사용유무 · 등록일 범위({@code yyyyMMdd}). 정렬은 부서ID 내림차순이다.
     */
    public List<DesignatedPart> searchDepartments(String upperLocgovCode, String locgovCode,
                                                  String deptNm, String useYn,
                                                  String searchStDt, String searchEdDt) {
        Map<String, Locgov> locgovsByCode = new HashMap<>();
        locgovRepository.findAll().forEach(l -> locgovsByCode.put(l.getLocgovCode(), l));

        return designatedPartRepository.findAllByOrderByDeptIdDesc().stream()
                .filter(p -> matchesDeptLocgov(p, upperLocgovCode, locgovCode, locgovsByCode))
                .filter(p -> deptNm == null || deptNm.isBlank()
                        || (p.getDeptNm() != null && p.getDeptNm().contains(deptNm.trim())))
                .filter(p -> useYn == null || useYn.isBlank() || useYn.equals(p.getUseYn()))
                .filter(p -> matchesRegDate(p, searchStDt, searchEdDt))
                .toList();
    }

    private static boolean matchesDeptLocgov(DesignatedPart part, String upperLocgovCode, String locgovCode,
                                             Map<String, Locgov> locgovsByCode) {
        if (locgovCode != null && !locgovCode.isBlank()) {
            return locgovCode.equals(part.getLocgovCode());
        }
        if (upperLocgovCode != null && !upperLocgovCode.isBlank()) {
            Locgov locgov = locgovsByCode.get(part.getLocgovCode());
            return locgov != null && upperLocgovCode.equals(locgov.getUpperLocgovCode());
        }
        return true;
    }

    private static boolean matchesRegDate(DesignatedPart part, String from, String to) {
        boolean hasFrom = from != null && !from.isBlank();
        boolean hasTo = to != null && !to.isBlank();
        if (!hasFrom && !hasTo) {
            return true;
        }
        if (part.getFrstRegDt() == null) {
            return false;
        }
        String day = part.getFrstRegDt().toLocalDate().format(YMD);
        if (hasFrom && day.compareTo(from) < 0) {
            return false;
        }
        return !hasTo || day.compareTo(to) <= 0;
    }

    public DesignatedPart departmentOrThrow(Long deptId) {
        return designatedPartRepository.findById(deptId)
                .orElseThrow(() -> new DonationException("사업부서를 찾을 수 없습니다."));
    }

    /**
     * AS-IS {@code updateDsgncntrPartMng} - 부서명·지자체·사용유무를 바꾼다
     * (AS-IS {@code part/form.jsp}의 저장이 등록과 수정을 한 엔드포인트로 처리한다).
     */
    @Transactional
    public DesignatedPart updateDepartment(Long deptId, String deptNm, String locgovCode,
                                           String useYn, Long managerId) {
        if (deptNm == null || deptNm.isBlank()) {
            throw new DonationException("부서명을 입력해 주세요.");
        }
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new DonationException("지자체를 선택해 주세요.");
        }
        DesignatedPart part = departmentOrThrow(deptId);
        part.setDeptNm(deptNm);
        part.setLocgovCode(locgovCode);
        part.setUseYn(useYn == null || useYn.isBlank() ? part.getUseYn() : useYn);
        part.setLastRgtrId(managerId);
        part.setLastRegDt(LocalDateTime.now());
        return designatedPartRepository.save(part);
    }

    @Transactional
    public DesignatedPart toggleDepartment(Long deptId, Long managerId) {
        DesignatedPart part = designatedPartRepository.findById(deptId)
                .orElseThrow(() -> new DonationException("담당부서를 찾을 수 없습니다."));
        part.setUseYn("Y".equals(part.getUseYn()) ? "N" : "Y");
        part.setLastRgtrId(managerId);
        part.setLastRegDt(LocalDateTime.now());
        return designatedPartRepository.save(part);
    }

    // ---- 모금분석 (AS-IS analysis/locgov + analysis/month 통합) ----

    /** 지정기부 전체를 지자체별로 집계 - AS-IS analysis/locgov.jsp. */
    public List<LocgovStat> analysisByLocgov(String year) {
        Map<String, List<Donation>> byLocgov = designatedDonations(year).stream()
                .collect(Collectors.groupingBy(Donation::getCntrLocgovCode));
        List<LocgovStat> rows = new ArrayList<>();
        for (Map.Entry<String, List<Donation>> e : byLocgov.entrySet()) {
            BigDecimal amount = e.getValue().stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
            long donors = e.getValue().stream().map(Donation::getUserId).distinct().count();
            rows.add(new LocgovStat(e.getKey(), amount, e.getValue().size(), donors));
        }
        rows.sort((a, b) -> b.amount().compareTo(a.amount()));
        return rows;
    }

    /** 월별 추이 - AS-IS analysis/month.jsp. */
    public List<MonthStat> analysisByMonth(String year) {
        Map<String, List<Donation>> byMonth = designatedDonations(year).stream()
                .filter(d -> d.getCntrDe() != null && d.getCntrDe().length() >= 6)
                .collect(Collectors.groupingBy(d -> d.getCntrDe().substring(4, 6)));
        List<MonthStat> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String key = String.format("%02d", m);
            List<Donation> list = byMonth.getOrDefault(key, List.of());
            BigDecimal amount = list.stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new MonthStat(key, amount, list.size()));
        }
        return rows;
    }

    public List<String> analysisYears() {
        return donationRepository.findByDsgnDntnBizIdIsNotNullAndCntrSttusCode(STATUS_COMPLETED).stream()
                .filter(d -> d.getCntrDe() != null && d.getCntrDe().length() >= 4)
                .map(d -> d.getCntrDe().substring(0, 4))
                .distinct().sorted(java.util.Comparator.reverseOrder()).toList();
    }

    /**
     * 특정사업 기부만 추린다.
     *
     * <p><b>결함 수정</b>: 전에는 {@code dsgnDntnBizId is not null}만 걸어
     * <b>{@code dsgn_dntn_biz_id}가 0인 일반 기부까지 특정사업 집계에 섞였다</b>
     * (실데이터에 0인 행이 있다). AS-IS는 {@code > 0}이고 {@code DELETE_AT='N'}도 함께 건다 -
     * {@link com.ghlove.donation.repository.DonationRepository#findDesignatedByStatus} 주석 참고.
     * 지자체별·월별 통계(17201·17202)의 숫자도 이 수정으로 같이 바로잡힌다.
     */
    private List<Donation> designatedDonations(String year) {
        return donationRepository.findDesignatedByStatus(STATUS_COMPLETED).stream()
                .filter(d -> year == null || year.isBlank() || (d.getCntrDe() != null && d.getCntrDe().startsWith(year)))
                .toList();
    }

    // ------------------------------------------------------- 관리자 목록 검색 (admin 메뉴 17101)

    /**
     * AS-IS {@code selectDesignatedDonationList} 이식 - 관리자 특정사업 목록.
     *
     * <p><b>AS-IS 규칙 그대로</b>:
     * <ul>
     *   <li>지자체는 시군구가 있으면 그것으로, 없으면 광역 하위 전체로 거른다.</li>
     *   <li>기간은 시작·종료가 <b>둘 다 있으면 사업 모금기간과 겹치는</b> 사업을, 하나만 있으면
     *       그 날짜가 사업 모금기간 안에 드는 사업을 찾는다.</li>
     *   <li><b>모금상태는 저장값을 그대로 쓰지 않고 다시 계산한다</b>: 저장값이 진행('2')인데
     *       모금액이 목표금액을 넘었거나 종료일이 지났으면 종료('9')로 본다. 상태 검색도
     *       <b>계산된 값</b>에 적용된다.</li>
     *   <li>달성율 = {@code floor(모금액 * 10000 / 목표금액) / 100}(소수 2자리), 목표금액 0이면 0.</li>
     *   <li>부서명이 없으면 "부서 미지정".</li>
     *   <li>지자체명은 "광역명 시군구명", 정렬은 사업ID 내림차순.</li>
     *   <li>집계(모금액·기부건수)는 <b>기부일이 오늘까지</b>인 완료 기부만 센다.</li>
     * </ul>
     *
     * <p><b>AS-IS 결함 1건 - 고쳤다</b>: 사업명 검색창은 {@code query}로 전송하는데 목록 SQL은
     * {@code searchKeyword}를 본다(화면이 예전 필드명에서 바뀌었는데 SQL이 안 따라갔다).
     * 그래서 <b>AS-IS에서는 사업명 검색이 전혀 걸리지 않는다</b> - TO-BE는 걸리게 했다.
     */
    public ProjectSearchResult search(ProjectSearchCriteria criteria) {
        String today = LocalDate.now().format(YMD);

        // 집계: 사업별 모금액·기부건수 (기부일이 오늘까지인 완료 기부)
        Map<Long, long[]> aggByProject = new HashMap<>();
        for (Donation d : donationRepository.findDesignatedByStatus(STATUS_COMPLETED)) {
            if (d.getCntrDe() != null && d.getCntrDe().compareTo(today) > 0) {
                continue;
            }
            long[] agg = aggByProject.computeIfAbsent(d.getDsgnDntnBizId(), k -> new long[2]);
            agg[0] += d.getCntrAmt() == null ? 0L : d.getCntrAmt().longValue();
            agg[1] += 1;
        }

        Map<String, Locgov> locgovsByCode = new HashMap<>();
        locgovRepository.findAll().forEach(l -> locgovsByCode.put(l.getLocgovCode(), l));
        Map<Long, String> deptNameById = new HashMap<>();
        designatedPartRepository.findAll().forEach(p -> deptNameById.put(p.getDeptId(), p.getDeptNm()));
        // AS-IS 코드유형은 BUSINESS_TYPE/BUSINESS_SUB_TYPE/PRJ_STATUS지만 TO-BE는 사업구분을
        // DSGN_BSNS_TYPE(100~400)으로 쓰고 상태는 코드표가 아니라 OPEN/CLOSED/PENDING 값이다
        // (회원 상태 9→ACTIVE, 기부 상태 200→COMPLETED와 같은 고정 경계 번역).
        // 상태 라벨은 admin 화면이 붙인다. 사업부문(세부구분)은 TO-BE에 코드표가 없어 코드를 그대로 둔다.
        Map<String, String> bsnsTypes = codesOf("DSGN_BSNS_TYPE");

        List<ProjectRow> rows = new ArrayList<>();
        for (DesignatedProject p : designatedProjectRepository.findAll()) {
            if (!matchesLocgov(p, criteria, locgovsByCode)) {
                continue;
            }
            if (criteria.bsnsType() != null && !criteria.bsnsType().isBlank()
                    && !criteria.bsnsType().equals(p.getDsgnDntnBizSeCd())) {
                continue;
            }
            if (!matchesPeriod(p, criteria.prjStDt(), criteria.prjEdDt())) {
                continue;
            }
            if (criteria.displayFlag() != null && !criteria.displayFlag().isBlank()
                    && !criteria.displayFlag().equals(p.getRlsYn())) {
                continue;
            }
            if (criteria.query() != null && !criteria.query().isBlank()
                    && (p.getDsgnDntnBizTtl() == null
                        || !p.getDsgnDntnBizTtl().contains(criteria.query().trim()))) {
                continue;
            }

            long[] agg = aggByProject.getOrDefault(p.getDsgnDntnBizId(), new long[2]);
            long sumAmt = agg[0];
            long cntrCnt = agg[1];
            long goalAmt = p.getGoalAmt() == null ? 0L : p.getGoalAmt();
            int inDate = inDate(p, today);
            String status = effectiveStatus(p.getDsgnDntnBizSttsCd(), sumAmt, goalAmt, inDate);

            if (criteria.prjStatus() != null && !criteria.prjStatus().isBlank()
                    && !criteria.prjStatus().equals(status)) {
                continue;
            }

            Locgov locgov = locgovsByCode.get(p.getLclgvCd());
            rows.add(new ProjectRow(p.getDsgnDntnBizId(),
                    locgov == null ? p.getLclgvCd() : (locgov.getUpperLocgovNm() + " " + locgov.getLocgovNm()),
                    status, p.getImageUrl(), p.getDsgnDntnBizSeCd(),
                    bsnsTypes.getOrDefault(p.getDsgnDntnBizSeCd(), p.getDsgnDntnBizSeCd()),
                    p.getBsnsSubType(),
                    p.getDsgnDntnBizTtl(), cntrCnt, p.getDsgnDntnBizBgngYmd(), p.getDsgnDntnBizEndYmd(),
                    goalAmt, sumAmt, achievementRate(sumAmt, goalAmt), inDate, p.getRlsYn(),
                    deptNameById.getOrDefault(p.getDeptId(), DEPT_UNASSIGNED)));
        }

        rows.sort(Comparator.comparing(ProjectRow::dsgnDntnBizId).reversed());

        // AS-IS list.jsp 상단 요약표 - 총 목표금액/총 모금액/총 달성율/총 기부건수
        long totGoal = rows.stream().mapToLong(ProjectRow::goalAmt).sum();
        long totAmt = rows.stream().mapToLong(ProjectRow::sumAmt).sum();
        long totCnt = rows.stream().mapToLong(ProjectRow::cntrCnt).sum();
        return new ProjectSearchResult(rows,
                new ProjectSummary(totGoal, totAmt, achievementRate(totAmt, totGoal), totCnt));
    }

    private static boolean matchesLocgov(DesignatedProject p, ProjectSearchCriteria criteria,
                                         Map<String, Locgov> locgovsByCode) {
        if (criteria.locgovCode() != null && !criteria.locgovCode().isBlank()) {
            return criteria.locgovCode().equals(p.getLclgvCd());
        }
        if (criteria.upperLocgovCode() != null && !criteria.upperLocgovCode().isBlank()) {
            Locgov locgov = locgovsByCode.get(p.getLclgvCd());
            return locgov != null && criteria.upperLocgovCode().equals(locgov.getUpperLocgovCode());
        }
        return true;
    }

    /** AS-IS 기간 조건 - 둘 다 있으면 "겹침", 하나만 있으면 "그 날짜가 사업기간 안". */
    private static boolean matchesPeriod(DesignatedProject p, String from, String to) {
        String bgng = p.getDsgnDntnBizBgngYmd();
        String end = p.getDsgnDntnBizEndYmd();
        if (bgng == null || end == null) {
            return from == null || from.isBlank() ? (to == null || to.isBlank()) : false;
        }
        boolean hasFrom = from != null && !from.isBlank();
        boolean hasTo = to != null && !to.isBlank();
        if (hasFrom && hasTo) {
            return between(bgng, from, to) || between(end, from, to)
                    || between(from, bgng, end) || between(to, bgng, end);
        }
        if (hasFrom) {
            return between(from, bgng, end);
        }
        if (hasTo) {
            return between(to, bgng, end);
        }
        return true;
    }

    private static boolean between(String value, String from, String to) {
        return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
    }

    /** AS-IS IN_DATE - 1: 모금기간 내, 2: 시작 전, 3: 종료 후. */
    private static int inDate(DesignatedProject p, String today) {
        String bgng = p.getDsgnDntnBizBgngYmd();
        String end = p.getDsgnDntnBizEndYmd();
        if (end == null || end.compareTo(today) < 0) {
            return 3;
        }
        return bgng != null && bgng.compareTo(today) <= 0 ? 1 : 2;
    }

    /**
     * AS-IS: 진행 상태여도 <b>목표금액을 넘었거나 종료일이 지났으면 종료로 보여준다</b>
     * (저장값을 바꾸지는 않는다 - 보여줄 때만 다시 계산한다).
     * AS-IS 코드값 '2'/'9'는 TO-BE {@code OPEN}/{@code CLOSED}에 대응한다.
     */
    private static String effectiveStatus(String stored, long sumAmt, long goalAmt, int inDate) {
        if (!STATUS_OPEN.equals(stored)) {
            return stored;
        }
        if (goalAmt > 0 && sumAmt > goalAmt) {
            return STATUS_CLOSED;
        }
        return inDate == 3 ? STATUS_CLOSED : stored;
    }

    /** AS-IS RATE_AMT - floor(모금액*10000/목표금액)/100. */
    private static BigDecimal achievementRate(long sumAmt, long goalAmt) {
        if (goalAmt == 0) {
            return BigDecimal.ZERO;
        }
        long scaled = (long) Math.floor(sumAmt * 10000.0 / goalAmt);
        return BigDecimal.valueOf(scaled, 2);
    }

    /**
     * AS-IS 목록 하단 일괄처리 - 모금상태(진행/종료) 또는 공개여부(공개/비공개)를 한꺼번에 바꾼다.
     * AS-IS는 상태값('2'/'9')과 공개값('Y'/'N')을 <b>같은 버튼 묶음</b>에서 같은 파라미터로 보내고
     * 서버가 값으로 구분한다 - 그 모양을 그대로 받는다.
     *
     * @return 실제로 바꾼 건수
     */
    @Transactional
    public int bulkUpdate(List<Long> ids, String value, Long managerId) {
        if (ids == null || ids.isEmpty() || value == null || value.isBlank()) {
            return 0;
        }
        boolean isDisplay = "Y".equals(value) || "N".equals(value);
        int changed = 0;
        for (Long id : ids) {
            DesignatedProject project = designatedProjectRepository.findById(id).orElse(null);
            if (project == null) {
                continue;
            }
            if (isDisplay) {
                project.setRlsYn(value);
            } else {
                project.setDsgnDntnBizSttsCd(value);
            }
            project.setLastRgtrId(managerId);
            designatedProjectRepository.save(project);
            changed++;
        }
        return changed;
    }

    /**
     * 사업 한 건의 모금 현황 - AS-IS 수정화면이 읽기전용으로 보여주는 세 칸
     * (<b>남은 일수 / 모금 된 금액 / 달성률</b>)에 쓴다. 집계 규칙은 목록과 같다
     * ({@link #search} 주석 - 기부일이 오늘까지인 완료 기부만, {@code dsgn_dntn_biz_id > 0}).
     *
     * <p>AS-IS {@code leftDayStr}는 종료일까지 남은 일수다 - <b>종료일이 지났으면 0</b>으로 본다.
     */
    public ProjectStat statOf(Long dsgnDntnBizId) {
        DesignatedProject project = findOrThrow(dsgnDntnBizId);
        String today = LocalDate.now().format(YMD);

        long sumAmt = 0L;
        long cntrCnt = 0L;
        for (Donation d : donationRepository.findDesignatedByStatus(STATUS_COMPLETED)) {
            if (!dsgnDntnBizId.equals(d.getDsgnDntnBizId())) {
                continue;
            }
            if (d.getCntrDe() != null && d.getCntrDe().compareTo(today) > 0) {
                continue;
            }
            sumAmt += d.getCntrAmt() == null ? 0L : d.getCntrAmt().longValue();
            cntrCnt++;
        }

        long goalAmt = project.getGoalAmt() == null ? 0L : project.getGoalAmt();
        return new ProjectStat(sumAmt, cntrCnt, achievementRate(sumAmt, goalAmt),
                leftDays(project.getDsgnDntnBizEndYmd(), today),
                leftDayStr(project.getDsgnDntnBizEndYmd(), today));
    }

    /**
     * AS-IS {@code DesignatedDonation.getLeftDayStr()} 그대로.
     *
     * <p>AS-IS의 {@code leftDay} 는 매퍼의 {@code DATEDIFF(NOW(), 종료일)} 이라
     * <b>오늘 - 종료일</b>(부호 있는 값)이다. 그래서 종료일이 남았으면 음수가 되어
     * {@code "D" + (-87)} = <b>D-87</b> 로 찍히고, 지났으면 {@code D+5} 가 된다.
     */
    static String leftDayStr(String endYmd, String today) {
        if (endYmd == null || endYmd.length() != 8) {
            return "D-Day";
        }
        try {
            long leftDay = LocalDate.parse(today, YMD).toEpochDay()
                    - LocalDate.parse(endYmd, YMD).toEpochDay();
            if (leftDay == 0) {
                return "D-Day";
            }
            return leftDay > 0 ? "D+" + leftDay : "D" + leftDay;
        } catch (DateTimeParseException e) {
            return "D-Day";
        }
    }

    /** AS-IS leftDayStr - 종료일까지 남은 일수(지났으면 0). */
    private static long leftDays(String endYmd, String today) {
        if (endYmd == null || endYmd.length() != 8) {
            return 0L;
        }
        try {
            LocalDate end = LocalDate.parse(endYmd, YMD);
            long days = end.toEpochDay() - LocalDate.parse(today, YMD).toEpochDay();
            return Math.max(days, 0L);
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    /** AS-IS 수정화면의 읽기전용 세 칸 + 기부건수. */
    /**
     * AS-IS 수정화면의 읽기전용 세 칸.
     *
     * @param leftDays   종료일까지 남은 일수(지났으면 0) - 숫자로 쓰는 쪽을 위해 유지한다
     * @param leftDayStr AS-IS {@code DesignatedDonation.getLeftDayStr()} 와 같은 표기
     *                   ({@code D-Day} / {@code D-87} / 지났으면 {@code D+5})
     */
    public record ProjectStat(long sumAmt, long cntrCnt, BigDecimal rateAmt, long leftDays, String leftDayStr) {
    }

    /**
     * 목록 한 행 - AS-IS list.jsp가 쓰는 칸 그대로.
     * {@code prjStatus}는 <b>다시 계산된</b> 상태다({@link #effectiveStatus}).
     */
    public record ProjectRow(Long dsgnDntnBizId, String locgovNm, String prjStatus,
                             String prjImage, String bsnsType, String bsnsTypeDesc, String bsnsSubType,
                             String prjSubject, long cntrCnt, String prjStDt,
                             String prjEdDt, long goalAmt, long sumAmt, BigDecimal rateAmt, int inDate,
                             String displayFlag, String deptNm) {
    }

    /** AS-IS list.jsp 상단 요약표. */
    public record ProjectSummary(long totTargetAmt, long totCntrAmt, BigDecimal totAchvRt, long totCntrCnt) {
    }

    public record ProjectSearchResult(List<ProjectRow> rows, ProjectSummary summary) {
    }

    /** AS-IS DesignatedDonationSearchParam 중 관리자 목록이 쓰는 것만. */
    public record ProjectSearchCriteria(String upperLocgovCode, String locgovCode, String bsnsType,
                                        String prjStDt, String prjEdDt, String query, String prjStatus,
                                        String displayFlag) {
    }

    public record LocgovStat(String locgovCode, BigDecimal amount, long donationCount, long donorCount) {
    }

    public record MonthStat(String month, BigDecimal amount, long donationCount) {
    }
}

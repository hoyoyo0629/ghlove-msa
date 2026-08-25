package com.ghlove.donation.service;

import com.ghlove.donation.domain.DesignatedPart;
import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.DsgnAprvLog;
import com.ghlove.donation.domain.PrjNotice;
import com.ghlove.donation.repository.DesignatedPartRepository;
import com.ghlove.donation.repository.DesignatedProjectRepository;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.DsgnAprvLogRepository;
import com.ghlove.donation.repository.PrjNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final BigInteger MAX_AMOUNT = BigInteger.valueOf(999_999_999_999L);
    private static final long MAX_DURATION_DAYS = 365L * 3;

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final DesignatedProjectRepository designatedProjectRepository;
    private final PrjNoticeRepository prjNoticeRepository;
    private final DesignatedPartRepository designatedPartRepository;
    private final DsgnAprvLogRepository dsgnAprvLogRepository;
    private final DonationRepository donationRepository;
    private final FileStorageService fileStorageService;

    public List<DesignatedProject> listAll() {
        return designatedProjectRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getDsgnDntnBizId(), a.getDsgnDntnBizId()))
                .toList();
    }

    public DesignatedProject findOrThrow(Long id) {
        return designatedProjectRepository.findById(id)
                .orElseThrow(() -> new DonationException("지정기부사업을 찾을 수 없습니다."));
    }

    /** true면 ROLE_OPERATOR 등급(AS-IS의 지정기부 담당자)이라 진행중(승인) 상태로 직접
     *  전환할 수 없다 - 반드시 상급자(ROLE_ADMIN)가 승인해야 한다(AS-IS 자기 승인 금지 규칙). */
    @Transactional
    public DesignatedProject create(DesignatedProject form, MultipartFile image, boolean canSelfApprove, Long managerId) {
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
        writeApprovalLog(saved.getDsgnDntnBizId(), null, saved.getDsgnDntnBizSttsCd(), managerId);
        return saved;
    }

    @Transactional
    public DesignatedProject update(Long id, DesignatedProject form, MultipartFile image, boolean canSelfApprove, Long managerId) {
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
        if (!beforeStatus.equals(saved.getDsgnDntnBizSttsCd())) {
            writeApprovalLog(id, beforeStatus, saved.getDsgnDntnBizSttsCd(), managerId);
        }
        return saved;
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
     *  그냥 흘러서" 발생하는 전환이라 어떤 이벤트에도 걸리지 않으므로 배치가 필요하다. */
    @Scheduled(cron = "0 0 1 * * *")
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

    // ---- 공지사항 ----

    public List<PrjNotice> noticesOf(Long dsgnDntnBizId) {
        return prjNoticeRepository.findByDsgnDntnBizIdOrderByPrjNoticeIdDesc(dsgnDntnBizId);
    }

    public PrjNotice noticeOrThrow(Long noticeId) {
        return prjNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new DonationException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public PrjNotice createNotice(Long dsgnDntnBizId, String subject, String content) {
        findOrThrow(dsgnDntnBizId);
        if (subject == null || subject.isBlank()) {
            throw new DonationException("제목을 입력해 주세요.");
        }
        PrjNotice notice = new PrjNotice();
        notice.setDsgnDntnBizId(dsgnDntnBizId);
        notice.setPrjNoticeSubject(subject);
        notice.setPrjNoticeCn(content);
        notice.setFrstRegistPnttm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        return prjNoticeRepository.save(notice);
    }

    @Transactional
    public PrjNotice updateNotice(Long noticeId, String subject, String content) {
        PrjNotice notice = noticeOrThrow(noticeId);
        if (subject == null || subject.isBlank()) {
            throw new DonationException("제목을 입력해 주세요.");
        }
        notice.setPrjNoticeSubject(subject);
        notice.setPrjNoticeCn(content);
        return prjNoticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        prjNoticeRepository.deleteById(noticeId);
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

    private List<Donation> designatedDonations(String year) {
        return donationRepository.findByDsgnDntnBizIdIsNotNullAndCntrSttusCode(STATUS_COMPLETED).stream()
                .filter(d -> year == null || year.isBlank() || (d.getCntrDe() != null && d.getCntrDe().startsWith(year)))
                .toList();
    }

    public record LocgovStat(String locgovCode, BigDecimal amount, long donationCount, long donorCount) {
    }

    public record MonthStat(String month, BigDecimal amount, long donationCount) {
    }
}

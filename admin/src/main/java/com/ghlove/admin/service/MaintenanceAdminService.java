package com.ghlove.admin.service;

import com.ghlove.admin.domain.GSrMaintenance;
import com.ghlove.admin.domain.GSrMaintenanceFile;
import com.ghlove.admin.repository.GSrMaintenanceFileRepository;
import com.ghlove.admin.repository.GSrMaintenanceRepository;
import com.ghlove.admin.repository.MaintenanceAdminRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 운영관리 SR게시판 (메뉴 5113) - AS-IS {@code MaintenanceController} +
 * {@code MaintenanceServiceImpl} 이식.
 *
 * <p><b>AS-IS 규칙</b>: SR번호는 저장할 때 서버가 {@code GGSR-{yy}-{일련번호}}로 채번한다
 * (화면 입력칸이 없다) · 등록·삭제 모두 <b>소프트 삭제</b>({@code USE_YN='N'})다 ·
 * 첨부는 확장자 18종/50MB이고 저장명이 {@code yyyyMMddHHmmssSSSS_원본명}이다 ·
 * 수정은 기존 첨부를 지우지 않고 더한다(지우려면 첨부 옆 [x]).
 */
@Service
@RequiredArgsConstructor
public class MaintenanceAdminService {

    public static final String SUBDIR = "maintenance";
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** AS-IS {@code AVAILABLE_EXTENSION} 18종 - 자료실(16종)과 달리 hwpx·7z가 있다. */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "hwpx", "zip", "7z");
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private final MaintenanceAdminRepository maintenanceAdminRepository;
    private final GSrMaintenanceRepository maintenanceRepository;
    private final GSrMaintenanceFileRepository fileRepository;
    private final AdminFileStorageService fileStorageService;
    private final EntityManager entityManager;

    public int count(String where, String query, String startDt, String endDt, List<String> processStates) {
        return maintenanceAdminRepository.count(where, query, startDt, endDt, processStates);
    }

    /**
     * 목록의 '내용'은 스마트에디터가 만든 HTML({@code <p>...</p>})이 그대로 들어 있어 표에
     * 태그가 노출된다(AS-IS도 {@code <c:out>}이라 같은 모양이지만 2026-10-12 사용자 지적으로
     * 바로잡는다). 표는 한 줄짜리 칸이므로 태그를 벗기고 줄바꿈도 공백으로 바꿔 보여준다.
     * '답변'(조치결과)도 textarea 줄바꿈이 섞이므로 같이 정리한다. 수정화면은 원본 HTML을 쓴다.
     */
    public List<MaintenanceAdminRepository.Row> list(String where, String query, String startDt, String endDt,
                                                      List<String> processStates, int offset, int limit) {
        return maintenanceAdminRepository.list(where, query, startDt, endDt, processStates, offset, limit)
                .stream()
                .map(r -> new MaintenanceAdminRepository.Row(
                        r.bbsId(), r.srNo(), r.reqManagerNm(), r.reqTypeNm(), r.processTypeNm(),
                        r.bbsTtl(), plainText(r.bbsCn()), r.reqChannelNm(), r.processManagerNm(),
                        plainText(r.processCn()), r.processStateNm(), r.frstCrtDt(), r.lastMdfcnDt(),
                        r.processStartDate(), r.processEndDate(), r.attachedFileCnt()))
                .toList();
    }

    /** HTML 태그를 벗기고 엔티티를 되돌린 뒤 공백을 한 칸으로 정리한다. */
    static String plainText(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        String text = html
                .replaceAll("(?i)<br\\s*/?>", " ")
                .replaceAll("(?i)</(p|div|li|tr|h[1-6])>", " ")
                .replaceAll("<[^>]*>", "");
        text = text.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
        return text.replaceAll("\\s+", " ").trim();
    }

    public GSrMaintenance maintenance(Long bbsId) {
        return maintenanceRepository.findById(bbsId)
                .orElseThrow(() -> new ContentException("게시글을 찾을 수 없습니다."));
    }

    /** AS-IS는 첨부도 소프트 삭제라 살아 있는 것만 보여준다. */
    public List<GSrMaintenanceFile> filesOf(Long bbsId) {
        return fileRepository.findByBbsIdOrderByAtchFileSeq(bbsId).stream()
                .filter(f -> !"N".equals(f.getUseYn()))
                .toList();
    }

    /** AS-IS GET detail - 조회수 증가(목록에서 제목을 누르면 수정화면으로 가면서 올라간다). */
    @Transactional
    public void addInquiryCount(Long bbsId) {
        GSrMaintenance entity = maintenance(bbsId);
        entity.setInqCnt(entity.getInqCnt() == null ? 1L : entity.getInqCnt() + 1);
    }

    @Transactional
    public GSrMaintenance create(GSrMaintenance form, MultipartFile[] files, Long managerId) {
        if (form.getBbsTtl() == null || form.getBbsTtl().isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        form.setBbsId(null);
        form.setSrNo(nextSrNo());
        form.setReqCrtDate(LocalDate.now().format(YMD));
        form.setReqDt(LocalDateTime.now());
        form.setFrstCrtId(managerId);
        form.setFrstCrtDt(LocalDateTime.now());
        form.setUseYn("Y");
        form.setInqCnt(0L);
        if (form.getUrgentYn() == null || form.getUrgentYn().isBlank()) {
            form.setUrgentYn("N");
        }
        GSrMaintenance saved = maintenanceRepository.save(form);
        attachAll(saved.getBbsId(), files, managerId);
        return saved;
    }

    /** AS-IS {@code updateMaintenance} - 화면에서 받은 칸만 고치고 첨부는 더한다. */
    @Transactional
    public GSrMaintenance update(Long bbsId, GSrMaintenance form, MultipartFile[] files, Long managerId) {
        if (form.getBbsTtl() == null || form.getBbsTtl().isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        GSrMaintenance entity = maintenance(bbsId);
        entity.setKiType(form.getKiType());
        entity.setReqChannel(form.getReqChannel());
        entity.setReqType(form.getReqType());
        entity.setProcessType(form.getProcessType());
        entity.setProcessState(form.getProcessState());
        entity.setReqUserPhoneNumber(form.getReqUserPhoneNumber());
        entity.setReqUserInfo(form.getReqUserInfo());
        entity.setBbsTtl(form.getBbsTtl());
        entity.setBbsCn(form.getBbsCn());
        entity.setProcessCn(form.getProcessCn());
        entity.setProcessManagerNm(form.getProcessManagerNm());
        entity.setProcessReceiptDate(form.getProcessReceiptDate());
        entity.setProcessTargetEndDate(form.getProcessTargetEndDate());
        entity.setProcessStartDate(form.getProcessStartDate());
        entity.setProcessEndDate(form.getProcessEndDate());
        entity.setLastMdfcnId(managerId);
        entity.setLastMdfcnDt(LocalDateTime.now());
        maintenanceRepository.save(entity);

        attachAll(bbsId, files, managerId);
        return entity;
    }

    /** AS-IS {@code deleteMaintenance} - 하드 삭제가 아니라 {@code USE_YN='N'}이다. */
    @Transactional
    public void delete(Long bbsId, Long managerId) {
        GSrMaintenance entity = maintenance(bbsId);
        entity.setUseYn("N");
        entity.setLastMdfcnId(managerId);
        entity.setLastMdfcnDt(LocalDateTime.now());
    }

    /** AS-IS {@code deleteMaintenanceFileByFileId} - 파일도 소프트 삭제다(디스크 파일은 남는다). */
    @Transactional
    public void deleteFile(Long fileId, Long managerId) {
        fileRepository.findById(fileId).ifPresent(f -> {
            f.setUseYn("N");
            f.setLastMdfcnId(managerId);
            f.setLastMdfcnDt(LocalDateTime.now());
        });
    }

    public GSrMaintenanceFile file(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ContentException("첨부파일이 존재하지 않습니다."));
    }

    /**
     * AS-IS는 {@code CONCAT('GGSR-', yy, '-', SUBSTRING_INDEX(MAX(sr_no),'-',-1) + 1)}로 채번한다.
     * 그런데 {@code MAX(sr_no)}가 <b>문자열 최대값</b>이라 'GGSR-26-9'가 'GGSR-26-10'보다 커서
     * 10건을 넘기면 번호가 겹친다(AS-IS 결함). 여기서는 일련번호를 숫자로 비교해 채번한다.
     */
    private String nextSrNo() {
        String prefix = "GGSR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yy")) + "-";
        Number max = (Number) entityManager.createNativeQuery("""
                select coalesce(max(nullif(regexp_replace(split_part(sr_no, '-', 3), '[^0-9]', '', 'g'), ''))::int, 0)
                  from admin.g_sr_maintenance
                 where sr_no like :prefix
                """).setParameter("prefix", prefix + "%").getSingleResult();
        return prefix + (max.intValue() + 1);
    }

    private void attachAll(Long bbsId, MultipartFile[] files, Long managerId) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }
            String storedName = fileStorageService.store(file, SUBDIR, ALLOWED_EXTENSIONS, MAX_SIZE, true);

            GSrMaintenanceFile entity = new GSrMaintenanceFile();
            entity.setFileId(maintenanceAdminRepository.nextFileId());
            entity.setBbsId(bbsId);
            entity.setOrgnlAtchFileNm(file.getOriginalFilename());
            entity.setAtchFileNm(storedName);
            entity.setAtchFileExtnNm(extensionOf(file.getOriginalFilename()));
            entity.setAtchFileSz(file.getSize());
            entity.setAtchFileSeq(nextSeq(bbsId));
            entity.setAtchFilePathNm(SUBDIR);
            entity.setUseYn("Y");
            entity.setFrstCrtId(managerId);
            entity.setFrstCrtDt(LocalDateTime.now());
            fileRepository.save(entity);
        }
    }

    private int nextSeq(Long bbsId) {
        return fileRepository.findByBbsIdOrderByAtchFileSeq(bbsId).stream()
                .map(GSrMaintenanceFile::getAtchFileSeq)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private static String extensionOf(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase();
    }
}

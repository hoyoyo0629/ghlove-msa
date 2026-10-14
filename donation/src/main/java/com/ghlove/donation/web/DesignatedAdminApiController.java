package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedPart;
import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.DsgnAprvLog;
import com.ghlove.donation.domain.PrjNotice;
import com.ghlove.donation.service.DesignatedAdminService;
import com.ghlove.donation.service.DesignatedStatService;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 지정기부(designated-donation) 관리자 CRUD - admin 서비스가 호출하는 cross-service
 *  API. CtbnyOpratnApiController와 동일한 이유로 별도 인증 없이 열려있다(admin의
 *  OP_MANAGER 로그인이 실제 게이트). */
@RestController
@RequiredArgsConstructor
public class DesignatedAdminApiController {

    private final DesignatedAdminService designatedAdminService;
    private final DesignatedStatService designatedStatService;
    private final DonationService donationService;

    /** admin의 사업구분(DSGN_BSNS_TYPE) 드롭다운용 - 다른 codesOf() 소비처와 동일하게
     *  donation 자체 OP_COMMON_CODE를 읽는다. */
    @GetMapping("/api/codes/{codeType}")
    public Map<String, String> codes(@PathVariable String codeType) {
        return donationService.codesOf(codeType);
    }

    @GetMapping("/api/designated-projects/admin")
    public List<ProjectDto> list() {
        return designatedAdminService.listAll().stream().map(ProjectDto::of).toList();
    }

    /**
     * admin 특정사업 목록(메뉴 17101)용 - AS-IS {@code selectDesignatedDonationList}의
     * 검색조건·집계·요약을 그대로 돌려준다. 자세한 규칙은
     * {@link DesignatedAdminService#search} 주석.
     */
    @GetMapping("/api/designated-projects/admin/search")
    public DesignatedAdminService.ProjectSearchResult search(
            @RequestParam(required = false) String upperLocgovCode,
            @RequestParam(required = false) String locgovCode,
            @RequestParam(required = false) String bsnsType,
            @RequestParam(required = false) String prjStDt,
            @RequestParam(required = false) String prjEdDt,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String prjStatus,
            @RequestParam(required = false) String displayFlag) {
        return designatedAdminService.search(new DesignatedAdminService.ProjectSearchCriteria(
                upperLocgovCode, locgovCode, bsnsType, prjStDt, prjEdDt, query, prjStatus, displayFlag));
    }

    /**
     * admin 목록 하단 일괄처리 - 모금상태('2' 진행 / '9' 종료) 또는 공개여부('Y' / 'N')를
     * 한꺼번에 바꾼다. AS-IS가 같은 파라미터로 네 값을 보내고 서버가 값으로 구분한다.
     */
    @PostMapping("/api/designated-projects/admin/bulk-update")
    public Map<String, Object> bulkUpdate(@RequestParam List<Long> ids, @RequestParam String value,
                                          @RequestParam Long managerId) {
        return Map.of("changed", designatedAdminService.bulkUpdate(ids, value, managerId));
    }

    @GetMapping("/api/designated-projects/admin/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ProjectDto.of(designatedAdminService.findOrThrow(id)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** AS-IS 등록/수정 화면의 썸네일 목록. 노출 순서대로 내려준다. */
    @GetMapping("/api/designated-projects/admin/{id}/images")
    public List<Map<String, Object>> images(@PathVariable Long id) {
        return designatedAdminService.imagesOf(id).stream()
                .map(i -> Map.<String, Object>of(
                        "imageId", i.getDsgnDntnBizImgId(),
                        "imageUrl", "/uploads/donation/designated-project/" + i.getImgNm(),
                        "seq", i.getImgSeq()))
                .toList();
    }

    /** AS-IS 썸네일의 x 버튼(deleteDsgncntrPrjImageId). 한 장만 지운다. */
    @DeleteMapping("/api/designated-projects/admin/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        designatedAdminService.deleteImage(imageId);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping(value = "/api/designated-projects/admin", consumes = "multipart/form-data")
    public ResponseEntity<?> create(@ModelAttribute ProjectForm form, @RequestParam(required = false) MultipartFile image,
                                     @RequestParam(required = false) java.util.List<MultipartFile> images,
                                     @RequestParam(required = false) java.util.List<String> imgExplns,
                                     @RequestParam boolean canSelfApprove, @RequestParam Long managerId) {
        try {
            DesignatedProject saved = designatedAdminService.create(form.toEntity(), image, images, imgExplns, canSelfApprove, managerId);
            return ResponseEntity.ok(ProjectDto.of(saved));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/designated-projects/admin/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProjectForm form,
                                     @RequestParam(required = false) MultipartFile image,
                                     @RequestParam(required = false) java.util.List<MultipartFile> images,
                                     @RequestParam(required = false) java.util.List<String> imgExplns,
                                     @RequestParam boolean canSelfApprove, @RequestParam Long managerId) {
        try {
            DesignatedProject saved = designatedAdminService.update(id, form.toEntity(), image, images, imgExplns, canSelfApprove, managerId);
            return ResponseEntity.ok(ProjectDto.of(saved));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 상세설명 이미지의 접근성 설명 목록 - admin 수정화면의 "이미지 설명" 표를 다시 그릴 때 쓴다.
     * AS-IS selectImgDescListByPrjId 와 같이 순번 오름차순이다.
     */
    @GetMapping("/api/designated-projects/admin/{id}/img-explns")
    public ResponseEntity<?> imgExplns(@PathVariable Long id) {
        return ResponseEntity.ok(designatedAdminService.imgExplnsOf(id));
    }

    /**
     * 사업 한 건의 모금 현황 - admin 수정화면의 읽기전용 세 칸(남은 일수/모금 된 금액/달성률)용.
     * 집계 규칙은 목록과 같다({@link DesignatedAdminService#statOf}).
     */
    @GetMapping("/api/designated-projects/admin/{id}/stat")
    public ResponseEntity<?> stat(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(designatedAdminService.statOf(id));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/designated-projects/admin/{id}/approval-log")
    public List<ApprovalLogDto> approvalLog(@PathVariable Long id) {
        return designatedAdminService.approvalLogOf(id).stream().map(ApprovalLogDto::of).toList();
    }

    // ---- 공지사항 (AS-IS G_DSGN_DNTN_BIZ_NTC_MNG) ----

    @GetMapping("/api/designated-projects/admin/{id}/notices")
    public List<NoticeDto> notices(@PathVariable Long id) {
        return designatedAdminService.noticesOf(id).stream().map(NoticeDto::of).toList();
    }

    @GetMapping("/api/designated-projects/admin/notices/{noticeId}")
    public ResponseEntity<?> notice(@PathVariable Long noticeId) {
        try {
            return ResponseEntity.ok(NoticeDto.of(designatedAdminService.noticeOrThrow(noticeId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/designated-projects/admin/{id}/notices", consumes = "multipart/form-data")
    public ResponseEntity<?> createNotice(@PathVariable Long id,
                                           @RequestParam String subject,
                                           @RequestParam(required = false) String content,
                                           @RequestParam(required = false) String rlsYn,
                                           @RequestParam(required = false) List<MultipartFile> files,
                                           @RequestParam(required = false) List<String> imgExplns,
                                           @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(NoticeDto.of(
                    designatedAdminService.createNotice(id, subject, content, rlsYn, files, imgExplns, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/designated-projects/admin/notices/{noticeId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateNotice(@PathVariable Long noticeId,
                                           @RequestParam String subject,
                                           @RequestParam(required = false) String content,
                                           @RequestParam(required = false) String rlsYn,
                                           @RequestParam(required = false) List<MultipartFile> files,
                                           @RequestParam(required = false) List<String> imgExplns,
                                           @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(NoticeDto.of(
                    designatedAdminService.updateNotice(noticeId, subject, content, rlsYn, files, imgExplns, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/designated-projects/admin/notices/{noticeId}/delete")
    public void deleteNotice(@PathVariable Long noticeId) {
        designatedAdminService.deleteNotice(noticeId);
    }

    /** AS-IS 목록 하단 [공개]/[비공개] 일괄처리. */
    @PostMapping("/api/designated-projects/admin/notices/display")
    public Map<String, Object> updateNoticeDisplay(@RequestParam List<Long> noticeIds,
                                                    @RequestParam String rlsYn,
                                                    @RequestParam Long managerId) {
        return Map.of("changed", designatedAdminService.updateNoticeDisplay(noticeIds, rlsYn, managerId));
    }

    @GetMapping("/api/designated-projects/admin/notices/{noticeId}/files")
    public List<NoticeFileDto> noticeFiles(@PathVariable Long noticeId) {
        return designatedAdminService.noticeFilesOf(noticeId).stream().map(NoticeFileDto::of).toList();
    }

    /**
     * 대민 공지 첨부 내용 - admin {@code /fileDownload/downloadByProgramData}(AS-IS ProgramFileDownloadController,
     * PRJ_NOTICE)가 대민 화면에서 받은 파일명 토큰을 그대로 넘긴다. 복호화·검증은 키를 가진 이 서비스가 한다.
     */
    @GetMapping("/api/designated-projects/admin/notices/{noticeId}/file-content")
    public ResponseEntity<byte[]> noticeFileContent(@PathVariable Long noticeId, @RequestParam String fileName) {
        byte[] bytes = designatedAdminService.noticeFileContent(noticeId, fileName);
        return bytes == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM).body(bytes);
    }

    @PostMapping("/api/designated-projects/admin/notices/{noticeId}/files/{fileSeq}/delete")
    public void deleteNoticeFile(@PathVariable Long noticeId, @PathVariable Integer fileSeq) {
        designatedAdminService.deleteNoticeFile(noticeId, fileSeq);
    }

    @GetMapping("/api/designated-projects/admin/notices/{noticeId}/img-explns")
    public List<String> noticeImgExplns(@PathVariable Long noticeId) {
        return designatedAdminService.noticeImgExplnsOf(noticeId).stream()
                .map(com.ghlove.donation.domain.DesignatedNoticeImgExpln::getImgExpln).toList();
    }

    // ---- 담당부서 ----

    @GetMapping("/api/designated-projects/admin/departments")
    public List<DepartmentDto> departments(@RequestParam(required = false) String locgovCode) {
        List<DesignatedPart> list = (locgovCode == null || locgovCode.isBlank())
                ? designatedAdminService.departmentsAll()
                : designatedAdminService.departmentsOf(locgovCode);
        return list.stream().map(DepartmentDto::of).toList();
    }

    /**
     * 사업부서 검색 - AS-IS {@code part/list.jsp}의 조건 4종(지자체·부서명·사용유무·등록일 범위).
     * 규칙은 {@link DesignatedAdminService#searchDepartments} 주석.
     */
    @GetMapping("/api/designated-projects/admin/departments/search")
    public List<DepartmentDto> searchDepartments(@RequestParam(required = false) String upperLocgovCode,
                                                 @RequestParam(required = false) String locgovCode,
                                                 @RequestParam(required = false) String deptNm,
                                                 @RequestParam(required = false) String useYn,
                                                 @RequestParam(required = false) String searchStDt,
                                                 @RequestParam(required = false) String searchEdDt) {
        return designatedAdminService
                .searchDepartments(upperLocgovCode, locgovCode, deptNm, useYn, searchStDt, searchEdDt)
                .stream().map(DepartmentDto::of).toList();
    }

    /** 사업부서 단건 - AS-IS {@code part/form/{id}} 수정화면용. */
    @GetMapping("/api/designated-projects/admin/departments/{deptId}")
    public ResponseEntity<?> department(@PathVariable Long deptId) {
        try {
            return ResponseEntity.ok(DepartmentDto.of(designatedAdminService.departmentOrThrow(deptId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 사업부서 수정 - AS-IS {@code updateDsgncntrPartMng}(부서명·지자체·사용유무). */
    @PostMapping("/api/designated-projects/admin/departments/{deptId}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long deptId, @RequestParam String deptNm,
                                              @RequestParam String locgovCode,
                                              @RequestParam(required = false) String useYn,
                                              @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(DepartmentDto.of(
                    designatedAdminService.updateDepartment(deptId, deptNm, locgovCode, useYn, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/designated-projects/admin/departments")
    public ResponseEntity<?> createDepartment(@RequestParam String deptNm, @RequestParam String locgovCode, @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(DepartmentDto.of(designatedAdminService.createDepartment(deptNm, locgovCode, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/designated-projects/admin/departments/{deptId}/toggle")
    public ResponseEntity<?> toggleDepartment(@PathVariable Long deptId, @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(DepartmentDto.of(designatedAdminService.toggleDepartment(deptId, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ---- 자동 상태전환 (기간종료 배치는 매일 새벽 자동 실행되지만, 운영자가 즉시 정리하고
    // 싶을 때 수동 실행도 가능하게 열어둔다 - NhExportBatchController와 동일한 패턴) ----

    @PostMapping("/api/designated-projects/admin/close-expired")
    public Map<String, Object> closeExpired() {
        int closed = designatedAdminService.closeExpiredProjects();
        return Map.of("closedCount", closed);
    }

    // ---- 모금분석 ----

    @GetMapping("/api/designated-projects/admin/analysis/years")
    public List<String> analysisYears() {
        return designatedAdminService.analysisYears();
    }

    @GetMapping("/api/designated-projects/admin/analysis/locgov")
    public List<LocgovStatDto> analysisByLocgov(@RequestParam(required = false) String year) {
        return designatedAdminService.analysisByLocgov(year).stream().map(LocgovStatDto::of).toList();
    }

    @GetMapping("/api/designated-projects/admin/analysis/month")
    public List<MonthStatDto> analysisByMonth(@RequestParam(required = false) String year) {
        return designatedAdminService.analysisByMonth(year).stream().map(MonthStatDto::of).toList();
    }

    // ---- 월별통계(특정사업 월별통계) - AS-IS designated-donation/analysis/month 3종 ----
    // 공통 필터: 시도(shWdr)/시군구(shLocgovCode)/조회년도(selYear)/상태(prjStatus 0전체·2진행·9종료).

    @GetMapping("/api/designated-projects/admin/analysis/month/campaign")
    public List<DesignatedStatService.BsnsStatRow> monthCampaign(
            @RequestParam(required = false) String shWdr, @RequestParam(required = false) String shLocgovCode,
            @RequestParam(required = false) String selYear, @RequestParam(required = false) String prjStatus) {
        return designatedStatService.campaign(shWdr, shLocgovCode, selYear, prjStatus);
    }

    @GetMapping("/api/designated-projects/admin/analysis/month/amountraised")
    public List<DesignatedStatService.BsnsStatRow> monthAmountRaised(
            @RequestParam(required = false) String shWdr, @RequestParam(required = false) String shLocgovCode,
            @RequestParam(required = false) String selYear, @RequestParam(required = false) String prjStatus) {
        return designatedStatService.amountRaised(shWdr, shLocgovCode, selYear, prjStatus);
    }

    @GetMapping("/api/designated-projects/admin/analysis/month/amount")
    public List<DesignatedStatService.MonthStatRow> monthAmount(
            @RequestParam(required = false) String shWdr, @RequestParam(required = false) String shLocgovCode,
            @RequestParam(required = false) String selYear, @RequestParam(required = false) String prjStatus) {
        return designatedStatService.amount(shWdr, shLocgovCode, selYear, prjStatus);
    }

    // ---- 지자체별 통계(AS-IS analysis/locgov) - 요약 + 지자체별 집계 목록 ----

    @GetMapping("/api/designated-projects/admin/analysis/locgov/summary")
    public DesignatedStatService.LocgovSummary locgovSummary(
            @RequestParam(required = false) String shWdr, @RequestParam(required = false) String shLocgovCode,
            @RequestParam(required = false) String bsnsType, @RequestParam(required = false) String frDt,
            @RequestParam(required = false) String toDt, @RequestParam(required = false) String prjStatus) {
        return designatedStatService.locgovSummary(shWdr, shLocgovCode, bsnsType, frDt, toDt, prjStatus);
    }

    @GetMapping("/api/designated-projects/admin/analysis/locgov/list")
    public List<DesignatedStatService.LocgovStatRow> locgovList(
            @RequestParam(required = false) String shWdr, @RequestParam(required = false) String shLocgovCode,
            @RequestParam(required = false) String bsnsType, @RequestParam(required = false) String frDt,
            @RequestParam(required = false) String toDt, @RequestParam(required = false) String prjStatus) {
        return designatedStatService.locgovList(shWdr, shLocgovCode, bsnsType, frDt, toDt, prjStatus);
    }

    public record LocgovStatDto(String locgovCode, java.math.BigDecimal amount, long donationCount, long donorCount) {
        static LocgovStatDto of(DesignatedAdminService.LocgovStat s) {
            return new LocgovStatDto(s.locgovCode(), s.amount(), s.donationCount(), s.donorCount());
        }
    }

    public record MonthStatDto(String month, java.math.BigDecimal amount, long donationCount) {
        static MonthStatDto of(DesignatedAdminService.MonthStat s) {
            return new MonthStatDto(s.month(), s.amount(), s.donationCount());
        }
    }

    public record ProjectForm(String dsgnDntnBizTtl, String dsgnDntnBizCn, String dsgnDntnBizBgngYmd,
                               String dsgnDntnBizEndYmd, Long goalAmt, String dsgnDntnBizSttsCd, String rlsYn,
                               String lclgvCd, String dsgnDntnBizSeCd, String bsnsSubType, String contentEtc,
                               Long deptId) {
        DesignatedProject toEntity() {
            DesignatedProject p = new DesignatedProject();
            p.setDsgnDntnBizTtl(dsgnDntnBizTtl);
            p.setDsgnDntnBizCn(dsgnDntnBizCn);
            p.setDsgnDntnBizBgngYmd(dsgnDntnBizBgngYmd);
            p.setDsgnDntnBizEndYmd(dsgnDntnBizEndYmd);
            p.setGoalAmt(goalAmt);
            p.setDsgnDntnBizSttsCd(dsgnDntnBizSttsCd);
            p.setRlsYn(rlsYn);
            p.setLclgvCd(lclgvCd);
            p.setDsgnDntnBizSeCd(dsgnDntnBizSeCd);
            p.setBsnsSubType(bsnsSubType);
            p.setContentEtc(contentEtc);
            p.setDeptId(deptId);
            return p;
        }
    }

    public record ProjectDto(Long dsgnDntnBizId, String dsgnDntnBizTtl, String dsgnDntnBizCn,
                              String dsgnDntnBizBgngYmd, String dsgnDntnBizEndYmd, Long goalAmt,
                              String dsgnDntnBizSttsCd, String rlsYn, String lclgvCd, String dsgnDntnBizSeCd,
                              String bsnsSubType, String contentEtc, Long deptId, String imageUrl) {
        static ProjectDto of(DesignatedProject p) {
            return new ProjectDto(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl(), p.getDsgnDntnBizCn(),
                    p.getDsgnDntnBizBgngYmd(), p.getDsgnDntnBizEndYmd(), p.getGoalAmt(), p.getDsgnDntnBizSttsCd(),
                    p.getRlsYn(), p.getLclgvCd(), p.getDsgnDntnBizSeCd(), p.getBsnsSubType(), p.getContentEtc(),
                    p.getDeptId(), p.getImageUrl());
        }
    }

    /**
     * 공지 한 건. {@code frstRegistPnttm} 은 화면 표기용 yyyyMMddHHmmss - 옮기기 전 표가
     * 문자열로 들고 있던 포맷을 유지해 admin 쪽 표시 로직을 안 건드린다.
     */
    public record NoticeDto(Long prjNoticeId, Long dsgnDntnBizId, String prjNoticeSubject, String prjNoticeCn,
                             String frstRegistPnttm, String rlsYn) {
        static NoticeDto of(com.ghlove.donation.domain.DesignatedNotice n) {
            return new NoticeDto(n.getNoticeId(), n.getDsgnDntnBizId(), n.getSubject(), n.getContent(),
                    n.getFrstRegDt() == null ? null
                            : n.getFrstRegDt().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                    n.getRlsYn());
        }
    }

    /** 첨부 한 건 - 화면에는 원본 파일명이 보이고 다운로드는 seq 로 건다. */
    public record NoticeFileDto(Long noticeId, Integer fileSeq, String orgnflNm) {
        static NoticeFileDto of(com.ghlove.donation.domain.DesignatedNoticeFile f) {
            return new NoticeFileDto(f.getNoticeId(), f.getFileSeq(), f.getOrgnflNm());
        }
    }

    /** {@code createdDate}는 AS-IS 부서목록의 "등록일시" 칸(yyyy-MM-dd HH:mm)이다. */
    public record DepartmentDto(Long deptId, String deptNm, String locgovCode, String useYn,
                                 String createdDate) {
        private static final java.time.format.DateTimeFormatter REG_DT =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        static DepartmentDto of(DesignatedPart p) {
            return new DepartmentDto(p.getDeptId(), p.getDeptNm(), p.getLocgovCode(), p.getUseYn(),
                    p.getFrstRegDt() == null ? null : p.getFrstRegDt().format(REG_DT));
        }
    }

    public record ApprovalLogDto(Long logId, String beforeStatusCode, String afterStatusCode, Long frstRgtrId,
                                  String frstRegistPnttm) {
        static ApprovalLogDto of(DsgnAprvLog l) {
            return new ApprovalLogDto(l.getLogId(), l.getBeforeStatusCode(), l.getAfterStatusCode(),
                    l.getFrstRgtrId(), l.getFrstRegistPnttm() != null ? l.getFrstRegistPnttm().toString() : null);
        }
    }
}

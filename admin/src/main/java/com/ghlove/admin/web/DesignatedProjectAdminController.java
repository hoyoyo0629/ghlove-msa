package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.DesignatedProjectClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.web.support.DepartmentSearchParam;
import com.ghlove.admin.web.support.DesignatedSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 지정기부(designated-donation) 관리 (AS-IS opmanager/designated-donation). 실제
 *  데이터는 donation 서비스에 있고(DesignatedProjectClient), 여기는 admin 콘솔의
 *  로그인/RBAC과 화면만 담당한다. 갤러리 이미지 관리는 공개 상세화면 자체가 대표이미지
 *  1장 구조로 이미 스코프 밖(designated-detail.html) - 여기서도 다시 만들지 않는다. */
@Controller
@RequiredArgsConstructor
public class DesignatedProjectAdminController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    /** AS-IS 화면값 - '1' 대기, '2' 진행(승인처리), '9' 종료. 저장값은 PENDING/OPEN/CLOSED다. */
    private static final String STATUS_PENDING_VALUE = "1";
    private static final String STATUS_OPEN_VALUE = "2";
    private static final String STATUS_CLOSED_VALUE = "9";

    /** AS-IS: [진행(승인처리)] 버튼은 ROLE_ADMIN_1~5에게만 보인다. */
    private static final java.util.Set<String> CAN_APPROVE = java.util.Set.of(
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5");

    /** AS-IS form.jsp 의 [승인] 버튼 권한 - 목록의 일괄 승인처리(1~5)보다 한 단계 좁다. */
    private static final java.util.Set<String> CAN_APPROVE_ON_FORM = java.util.Set.of(
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");

    /** AS-IS 지정기부 담당자(hasDsgncntrManagerRole) - 자기 승인이 금지된 등급. */
    private static final String ROLE_DSGNCNTR_MANAGER = "ROLE_ADMIN_10";

    /** AS-IS checkDsgncntrAuthPartInfo 가드 문구 그대로. */
    private static final String DEPARTMENT_REQUIRED = "부서정보가 없어서 처리가 불가능합니다.";

    /** AS-IS 가드가 통과시키는 권한 - 최고관리자(1~4)·지자체담당자(5~6). */
    private static final java.util.Set<String> MASTER_OR_LOCGOV = java.util.Set.of(
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
            "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    private static final java.time.format.DateTimeFormatter DAY =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DesignatedProjectClient designatedProjectClient;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final LocgovClient locgovClient;
    private final com.ghlove.admin.service.CommonCodeService commonCodeService;
    private final com.ghlove.admin.repository.ManagerRepository managerRepository;

    /**
     * AS-IS GET·POST {@code designated-donation/list} - 특정사업 목록(메뉴 17101).
     *
     * <p>검색조건 6종(지자체·사업구분·기간·사업명·상태·공개여부)과 상단 요약표(총 목표금액/
     * 총 모금액/총 달성율/총 기부건수)를 donation에서 받아 그린다 - 집계·상태 재계산 규칙은
     * donation {@code DesignatedAdminService.search} 주석.
     *
     * <p><b>지자체담당자는 자기 지자체로 고정</b>된다(AS-IS도 지자체 선택 칸을 ROLE_ADMIN_1~4에게만
     * 보여준다). 출력수는 AS-IS가 <b>10/50/100/200/500</b>이다.
     */
    @GetMapping("/designated-projects")
    public String list(@ModelAttribute("searchParam") DesignatedSearchParam searchParam,
                       HttpSession session, Model model) {
        return fillList(searchParam, session, model);
    }

    @PostMapping("/designated-projects/search")
    public String search(@ModelAttribute("searchParam") DesignatedSearchParam searchParam,
                         HttpSession session, Model model) {
        return fillList(searchParam, session, model);
    }

    private String fillList(DesignatedSearchParam searchParam, HttpSession session, Model model) {
        Manager manager = manager(session);
        searchParam.applyDefaults();
        boolean locgovScoped = MenuService.isLocgovScoped(manager);
        if (locgovScoped) {
            // AS-IS: 지자체담당자에게는 지자체 선택 칸이 없고 자기 지자체만 본다
            searchParam.setUpperLocgovCode(null);
            searchParam.setLocgovCode(manager.getLocgovCode());
        }

        DesignatedProjectClient.SearchResult result = designatedProjectClient.search(
                searchParam.getUpperLocgovCode(), searchParam.getLocgovCode(), searchParam.getBsnsType(),
                searchParam.getPrjStDt(), searchParam.getPrjEdDt(), searchParam.getQuery(),
                // 화면 라디오는 AS-IS 값('1'/'2'/'9')이고 저장값은 PENDING/OPEN/CLOSED다
                toDonationValue(searchParam.getPrjStatus()), searchParam.getDisplayFlag());

        List<DesignatedProjectClient.ProjectRow> all = result.rows();
        int size = searchParam.getItemsPerPage();
        int totalCount = all.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int currentPage = Math.max(1, Math.min(searchParam.getPage(), Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * size, totalCount);
        int to = Math.min(from + size, totalCount);

        model.addAttribute("rows", all.subList(from, to));
        model.addAttribute("summary", result.summary());
        model.addAttribute("projectCount", totalCount);
        model.addAttribute("pagination", Pagination.of(totalCount, currentPage, size));
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("bsnsTypes", bsnsTypes());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovScoped", locgovScoped);
        // AS-IS: 진행(승인처리) 버튼은 ROLE_ADMIN_1~5만 볼 수 있다
        model.addAttribute("canApprove", CAN_APPROVE.contains(
                manager == null ? "" : String.valueOf(manager.getAuthority())));
        String today = java.time.LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", java.time.LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", java.time.LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month3", java.time.LocalDate.now().minusMonths(3).format(DAY));
        model.addAttribute("year1", java.time.LocalDate.now().minusYears(1).format(DAY));
        return "designated/list";
    }

    /**
     * AS-IS 목록 하단 일괄처리 - 모금상태(진행/종료) 또는 공개여부(공개/비공개)를 한꺼번에 바꾼다.
     * AS-IS는 네 버튼이 같은 함수({@code updateListDataLabel})로 값만 달리 보낸다.
     */
    @PostMapping("/designated-projects/bulk-update")
    @ResponseBody
    public Map<String, Object> bulkUpdate(@RequestParam(value = "id", required = false) List<Long> id,
                                          @RequestParam String value, HttpSession session) {
        Manager manager = manager(session);
        if (id == null || id.isEmpty()) {
            return Map.of("isSuccess", false, "errorMessage", "처리할 항목을 선택해 주세요.");
        }
        if (STATUS_OPEN_VALUE.equals(value) && !CAN_APPROVE.contains(String.valueOf(manager.getAuthority()))) {
            return Map.of("isSuccess", false, "errorMessage", "승인 처리 권한이 없습니다.");
        }
        try {
            int changed = designatedProjectClient.bulkUpdate(id, toDonationValue(value), manager.getUserId());
            return Map.of("isSuccess", true, "data", changed);
        } catch (ManagerException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
    }

    /**
     * 화면이 보내는 AS-IS 값('2' 진행 / '9' 종료 / 'Y' 공개 / 'N' 비공개)을 TO-BE 저장값으로 옮긴다
     * (상태는 {@code OPEN}/{@code CLOSED}, 공개여부는 그대로 'Y'/'N').
     */
    private static String toDonationValue(String value) {
        if (STATUS_OPEN_VALUE.equals(value)) {
            return STATUS_OPEN;
        }
        if (STATUS_CLOSED_VALUE.equals(value)) {
            return STATUS_CLOSED;
        }
        if (STATUS_PENDING_VALUE.equals(value)) {
            return STATUS_PENDING;
        }
        return value;
    }

    @GetMapping("/designated-projects/new")
    public String newForm(HttpSession session, Model model) {
        model.addAttribute("project", null);
        commonFormAttrs(model, null, manager(session));
        return "designated/form";
    }

    @GetMapping("/designated-projects/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Manager manager = manager(session);
        DesignatedProjectClient.Project project;
        try {
            project = designatedProjectClient.get(id);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/designated-projects";
        }
        if (MenuService.isLocgovScoped(manager) && !manager.getLocgovCode().equals(project.lclgvCd())) {
            return "redirect:/designated-projects";
        }
        model.addAttribute("project", project);
        commonFormAttrs(model, id, manager);
        return "designated/form";
    }

    private void commonFormAttrs(Model model, Long id, Manager manager) {
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("bsnsTypes", bsnsTypes());
        model.addAttribute("bsnsSubTypes", bsnsSubTypes());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        // 지자체담당자는 자기 지자체 부서만 고르게 한다(AS-IS도 지자체를 못 바꾼다)
        model.addAttribute("departments", designatedProjectClient.departments(
                MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null));
        model.addAttribute("projectId", id);
        // ★ AS-IS form.jsp 의 승인 버튼은 목록의 일괄 승인처리와 권한 범위가 다르다.
        //    목록(list.jsp:338) = ROLE_ADMIN_1~5, 폼(form.jsp 버튼 영역) = ROLE_ADMIN_1~4.
        //    여기서는 폼 기준(1~4)을 쓴다 - 합치지 말 것.
        String authority = manager == null ? "" : String.valueOf(manager.getAuthority());
        model.addAttribute("canApprove", CAN_APPROVE_ON_FORM.contains(authority));
        // AS-IS: 지정기부 담당자(ROLE_ADMIN_10)는 사업부서를 바꿀 수 없고(unableChangePart),
        //        상태를 "진행중(승인)"으로 고르면 "대기 상태로 저장됩니다" 확인을 받는다.
        model.addAttribute("isDsgncntrManager", ROLE_DSGNCNTR_MANAGER.equals(authority));
        // AS-IS 수정화면만 보여주는 읽기전용 세 칸(남은 일수/모금 된 금액/달성률).
        // 저장 실패로 폼을 다시 그릴 때도 필요하므로 여기서 한 번에 넣는다.
        if (id != null) {
            model.addAttribute("stat", designatedProjectClient.statOf(id));
        }
        // AS-IS prjImages - 올라간 이미지 썸네일(순번 + x 삭제). 등록 화면에서는 아직 없다.
        model.addAttribute("projectImages",
                id != null ? designatedProjectClient.imagesOf(id) : java.util.List.of());
        // AS-IS prjImageExplain - 상세설명 이미지의 접근성 설명(수정 모드에서만 기존 값이 있다).
        model.addAttribute("imgExplns",
                id != null ? designatedProjectClient.imgExplnsOf(id) : java.util.List.of());
    }

    /**
     * AS-IS 목록(17101)의 [공지사항] - {@code /opmanager/designated-donation/list/notice/{prjId}}.
     * 팝업이 아니라 <b>페이지 이동</b>이다(AS-IS도 {@code <a href>}).
     *
     * <p>AS-IS 목록 화면은 제목 검색 + 표(체크박스/ID/제목/공개여부) + 일괄 공개·비공개 +
     * 신규등록 + "특정사업에 기부하기 목록" 버튼이다. TO-BE 공지 표에는 <b>공개여부 칸이 없어</b>
     * 그 열과 일괄 버튼은 아직 못 만든다(경위는 템플릿 주석 참고).
     */
    @GetMapping("/designated-projects/{id}/notices")
    public String noticeList(@PathVariable Long id, @RequestParam(required = false) String query,
                              @RequestParam(required = false, defaultValue = "1") int page,
                              @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
                              HttpSession session, Model model) {
        manager(session);
        model.addAttribute("project", designatedProjectClient.get(id));
        model.addAttribute("projectId", id);
        List<DesignatedProjectClient.Notice> notices = designatedProjectClient.noticesOf(id);
        // AS-IS 매퍼: DSGN_DNTN_BIZ_NTC_TTL LIKE '%키워드%'
        if (query != null && !query.isBlank()) {
            String keyword = query.trim();
            notices = notices.stream()
                    .filter(n -> n.prjNoticeSubject() != null && n.prjNoticeSubject().contains(keyword))
                    .toList();
        }
        // AS-IS 출력수는 10/50/100/200/500 이고 목록 위에 "총 N 건"이 붙는다.
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalCount = notices.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * size, totalCount);
        int to = Math.min(from + size, totalCount);
        model.addAttribute("notices", notices.subList(from, to));
        model.addAttribute("noticeCount", totalCount);
        model.addAttribute("pagination", Pagination.of(totalCount, currentPage, size));
        model.addAttribute("itemsPerPage", size);
        model.addAttribute("query", query);
        return "designated/notice-list";
    }

    /** AS-IS {@code /notice/form/{prjId}/{noticeId}} - noticeId 가 0이면 신규등록이다. */
    @GetMapping("/designated-projects/{id}/notices/form")
    public String noticeForm(@PathVariable Long id, @RequestParam(required = false) Long noticeId,
                              HttpSession session, Model model) {
        manager(session);
        model.addAttribute("project", designatedProjectClient.get(id));
        model.addAttribute("projectId", id);
        DesignatedProjectClient.Notice target = null;
        if (noticeId != null && noticeId != 0L) {
            target = designatedProjectClient.noticesOf(id).stream()
                    .filter(n -> noticeId.equals(n.prjNoticeId()))
                    .findFirst().orElse(null);
        }
        model.addAttribute("notice", target);
        // AS-IS 폼의 "파일 첨부" 표와 에디터 아래 "이미지 설명" 표.
        model.addAttribute("noticeFiles",
                target != null ? designatedProjectClient.noticeFilesOf(target.prjNoticeId()) : java.util.List.of());
        model.addAttribute("imgExplns",
                target != null ? designatedProjectClient.noticeImgExplnsOf(target.prjNoticeId()) : java.util.List.of());
        return "designated/notice-form";
    }

    /**
     * AS-IS 목록의 [미리보기] - {@code openPreview(prjId)} 가
     * {@code Common.popup('/opmanager/designated-donation/preview?prjId=', "preview", 1600, 800)} 로 띄운다.
     *
     * <p>"대민에 이렇게 보인다"를 확인하는 화면이라 관리자 API가 아니라 <b>대민 상세 API</b>를
     * 그대로 호출한다 - 공개 규칙(비공개 공지 제외 등)이 저절로 같아진다. 남은 일수(D-N)만
     * 관리자 집계({@code statOf})에서 가져오고, 캐러셀은 멀티 이미지 목록을 쓴다.
     */
    @GetMapping("/designated-projects/{id}/preview")
    public String preview(@PathVariable Long id, HttpSession session, Model model) {
        manager(session);
        DesignatedProjectClient.PublicDetail detail = designatedProjectClient.publicDetail(id);
        if (detail == null) {
            model.addAttribute("errorMessage", "미리보기할 사업을 찾을 수 없습니다.");
        }
        model.addAttribute("detail", detail);
        model.addAttribute("projectId", id);
        model.addAttribute("stat", designatedProjectClient.statOf(id));
        // 대표이미지 1장만 있는 사업도 있으므로, 멀티 이미지가 없으면 대표이미지로 캐러셀을 채운다.
        List<DesignatedProjectClient.ProjectImage> images = designatedProjectClient.imagesOf(id);
        model.addAttribute("images", images);
        // donation 이 서빙하는 업로드 경로라 호스트를 앞에 붙여야 admin 화면에서 보인다.
        model.addAttribute("donationBaseUrl", designatedProjectClient.getDonationBaseUrl());
        return "designated/preview";
    }

    /**
     * AS-IS 목록의 [승인이력] - {@code showLog(prjId)} 가
     * {@code /opmanager/designated-donation/popup-confirm-log/{prjId}} 를 window.open 하는 팝업이다
     * (1510x700). 표는 로그아이디·사업명·승인 전 상태·승인 후 상태·승인자·승인일시.
     *
     * <p>승인자는 AS-IS가 이름({@code registerName})을 찍는데 donation은 관리자 id만 내려주므로,
     * op_manager 를 가진 admin 이 이름을 붙인다(ManagerRequestController 의 updaterLabels 와 같은 방식).
     */
    @GetMapping("/designated-projects/{id}/approval-log")
    public String approvalLogPopup(@PathVariable Long id, HttpSession session, Model model) {
        manager(session);
        model.addAttribute("project", designatedProjectClient.get(id));
        List<DesignatedProjectClient.ApprovalLog> logs = designatedProjectClient.approvalLogOf(id);
        model.addAttribute("logs", logs);
        model.addAttribute("statusLabels", statusLabels());
        java.util.Map<Long, String> registerNames = new java.util.LinkedHashMap<>();
        for (DesignatedProjectClient.ApprovalLog log : logs) {
            Long rgtrId = log.frstRgtrId();
            if (rgtrId == null || registerNames.containsKey(rgtrId)) {
                continue;
            }
            managerRepository.findById(rgtrId)
                    .ifPresent(m -> registerNames.put(rgtrId, m.getUserName()));
        }
        model.addAttribute("registerNames", registerNames);
        return "designated/approval-log-popup";
    }

    /** AS-IS 썸네일의 x(deleteDsgncntrPrjImageId) - 한 장만 지우고 수정 화면으로 돌아온다. */
    @PostMapping("/designated-projects/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long id, @PathVariable Long imageId, HttpSession session) {
        manager(session);
        designatedProjectClient.deleteImage(imageId);
        return "redirect:/designated-projects/" + id;
    }

    @PostMapping("/designated-projects")
    public String create(@ModelAttribute DesignatedProjectClient.ProjectForm form,
                          @RequestParam(required = false) MultipartFile image,
                          @RequestParam(required = false) List<MultipartFile> images,
                          @RequestParam(required = false) List<String> imgExplns,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.create(toYmd(forceOwnLocgov(form, manager)), image, images, imgExplns, canSelfApprove(manager), manager.getUserId());
            return "redirect:/designated-projects";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("project", form);
            commonFormAttrs(model, null, manager);
            return "designated/form";
        }
    }

    @PostMapping("/designated-projects/{id}")
    public String update(@PathVariable Long id, @ModelAttribute DesignatedProjectClient.ProjectForm form,
                          @RequestParam(required = false) MultipartFile image,
                          @RequestParam(required = false) List<MultipartFile> images,
                          @RequestParam(required = false) List<String> imgExplns,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager)) {
            try {
                if (!manager.getLocgovCode().equals(designatedProjectClient.get(id).lclgvCd())) {
                    return "redirect:/designated-projects";
                }
            } catch (ManagerException e) {
                return "redirect:/designated-projects";
            }
        }
        try {
            designatedProjectClient.update(id, toYmd(forceOwnLocgov(form, manager)), image, images, imgExplns, canSelfApprove(manager), manager.getUserId());
            return "redirect:/designated-projects/" + id;
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("project", form);
            commonFormAttrs(model, id, manager);
            return "designated/form";
        }
    }

    /** <input type="date">는 yyyy-MM-dd로 제출되지만 donation은 AS-IS 원본 그대로
     *  yyyyMMdd(8자리)로 저장한다 - give-operation 라운드에서 겪은 것과 동일한 형식차,
     *  여기서 미리 변환해 넘긴다. */
    private static DesignatedProjectClient.ProjectForm toYmd(DesignatedProjectClient.ProjectForm form) {
        return new DesignatedProjectClient.ProjectForm(form.dsgnDntnBizTtl(), form.dsgnDntnBizCn(),
                stripDashes(form.dsgnDntnBizBgngYmd()), stripDashes(form.dsgnDntnBizEndYmd()), form.goalAmt(),
                form.dsgnDntnBizSttsCd(), form.rlsYn(), form.lclgvCd(), form.dsgnDntnBizSeCd(),
                form.bsnsSubType(), form.contentEtc(), form.deptId());
    }

    private static String stripDashes(String ymd) {
        return ymd != null ? ymd.replace("-", "") : null;
    }

    /** 지자체담당자(ROLE_ADMIN_5/6)는 화면에서 어떤 지자체를 골랐든 자기 소속으로 강제한다. */
    private static DesignatedProjectClient.ProjectForm forceOwnLocgov(DesignatedProjectClient.ProjectForm form, Manager manager) {
        if (!MenuService.isLocgovScoped(manager)) {
            return form;
        }
        return new DesignatedProjectClient.ProjectForm(form.dsgnDntnBizTtl(), form.dsgnDntnBizCn(),
                form.dsgnDntnBizBgngYmd(), form.dsgnDntnBizEndYmd(), form.goalAmt(), form.dsgnDntnBizSttsCd(),
                form.rlsYn(), manager.getLocgovCode(), form.dsgnDntnBizSeCd(), form.bsnsSubType(),
                form.contentEtc(), form.deptId());
    }
    /** AS-IS 공지 등록 - 제목·공개여부·첨부·이미지설명을 한 번에 저장한다. */
    @PostMapping("/designated-projects/{id}/notices")
    public String createNotice(@PathVariable Long id, @RequestParam String subject,
                                @RequestParam(required = false) String content,
                                @RequestParam(required = false) String rlsYn,
                                @RequestParam(required = false) List<MultipartFile> files,
                                @RequestParam(required = false) List<String> imgExplns,
                                HttpSession session) {
        Manager manager = manager(session);
        designatedProjectClient.createNotice(id, subject, content, rlsYn, files, imgExplns, manager.getUserId());
        return "redirect:/designated-projects/" + id + "/notices";
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/update")
    public String updateNotice(@PathVariable Long id, @PathVariable Long noticeId,
                                @RequestParam String subject,
                                @RequestParam(required = false) String content,
                                @RequestParam(required = false) String rlsYn,
                                @RequestParam(required = false) List<MultipartFile> files,
                                @RequestParam(required = false) List<String> imgExplns,
                                HttpSession session) {
        Manager manager = manager(session);
        designatedProjectClient.updateNotice(noticeId, subject, content, rlsYn, files, imgExplns, manager.getUserId());
        return "redirect:/designated-projects/" + id + "/notices";
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/delete")
    public String deleteNotice(@PathVariable Long id, @PathVariable Long noticeId) {
        designatedProjectClient.deleteNotice(noticeId);
        return "redirect:/designated-projects/" + id + "/notices";
    }

    /** AS-IS 목록 하단 [공개]/[비공개] - 체크한 공지들의 공개여부를 한 번에 바꾼다. */
    @PostMapping("/designated-projects/{id}/notices/display")
    public String updateNoticeDisplay(@PathVariable Long id,
                                       @RequestParam(required = false) List<Long> noticeIds,
                                       @RequestParam String rlsYn, HttpSession session) {
        Manager manager = manager(session);
        designatedProjectClient.updateNoticeDisplay(noticeIds, rlsYn, manager.getUserId());
        return "redirect:/designated-projects/" + id + "/notices";
    }

    /** AS-IS 첨부 옆 x - 한 개만 지우고 폼으로 돌아온다. */
    @PostMapping("/designated-projects/{id}/notices/{noticeId}/files/{fileSeq}/delete")
    public String deleteNoticeFile(@PathVariable Long id, @PathVariable Long noticeId,
                                    @PathVariable Integer fileSeq) {
        designatedProjectClient.deleteNoticeFile(noticeId, fileSeq);
        return "redirect:/designated-projects/" + id + "/notices/form?noticeId=" + noticeId;
    }

    @GetMapping("/designated-projects/analysis")
    public String analysis(@RequestParam(required = false) String year, Model model) {
        model.addAttribute("years", designatedProjectClient.analysisYears());
        model.addAttribute("year", year);
        model.addAttribute("locgovRows", designatedProjectClient.analysisByLocgov(year));
        model.addAttribute("monthRows", designatedProjectClient.analysisByMonth(year));
        java.util.Map<String, String> locgovNames = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            locgovNames.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm());
        }
        model.addAttribute("locgovNames", locgovNames);
        return "designated/analysis";
    }

    // ---- 지자체별 통계 - AS-IS designated-donation/analysis/locgov 재현(차트 없는 서버렌더 표+요약) ----
    //
    // AS-IS는 같은 URL에 GET/POST 두 핸들러를 두고 동작이 다르다(DesignatedDonationManagerController
    // #locgovStat / #searchLocgovStat). GET(최초 진입·초기화)은 조회를 "하지 않고" count=0,
    // list=EMPTY, summaryData=빈 객체로 화면만 그린다 - 그래서 안내문이 "검색 버튼을 클릭하면
    // 조건에 맞는 검색결과를 확인할 수 있습니다."다. 실제 집계는 POST에서만 돈다.
    // 기간은 두 핸들러 모두 빈값이면 오늘(yyyyMMdd)로 채운다.

    @GetMapping("/designated-projects/analysis/locgov")
    public String analysisLocgovForm(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "") String bsnsType,
            @RequestParam(required = false, defaultValue = "") String frDt,
            @RequestParam(required = false, defaultValue = "") String toDt,
            @RequestParam(required = false, defaultValue = "0") String prjStatus,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            Model model) {
        Manager manager = manager(session);
        boolean scoped = MenuService.isLocgovScoped(manager);
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;

        model.addAttribute("summary", new DesignatedProjectClient.LocgovStatSummary(0, 0, 0f, 0));
        model.addAttribute("rows", List.of());
        model.addAttribute("startNo", 0);
        model.addAttribute("total", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("page", 1);
        model.addAttribute("itemsPerPage", size);
        locgovStatForm(model, scoped, shWdr, shLocgovCode, bsnsType, frDt, toDt, prjStatus);
        return "designated/analysis-locgov";
    }

    @PostMapping("/designated-projects/analysis/locgov")
    public String analysisLocgov(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "") String bsnsType,
            @RequestParam(required = false, defaultValue = "") String frDt,
            @RequestParam(required = false, defaultValue = "") String toDt,
            @RequestParam(required = false, defaultValue = "0") String prjStatus,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {
        Manager manager = manager(session);
        boolean scoped = MenuService.isLocgovScoped(manager);
        String effWdr = scoped ? "" : shWdr;
        String effLocgov = scoped ? manager.getLocgovCode() : shLocgovCode;
        String effFrDt = orToday(frDt);
        String effToDt = orToday(toDt);

        DesignatedProjectClient.LocgovStatSummary summary =
                designatedProjectClient.locgovStatSummary(effWdr, effLocgov, bsnsType, effFrDt, effToDt, prjStatus);
        List<DesignatedProjectClient.LocgovStatRow> all =
                designatedProjectClient.locgovStatList(effWdr, effLocgov, bsnsType, effFrDt, effToDt, prjStatus);

        int total = all.size();
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int cur = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((cur - 1) * size, total);
        int to = Math.min(from + size, total);
        List<DesignatedProjectClient.LocgovStatRow> rows = all.subList(from, to);

        model.addAttribute("summary", summary);
        model.addAttribute("rows", rows);
        model.addAttribute("startNo", total - from); // No 내림차순 시작값(AS-IS itemNumber)
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", cur);
        model.addAttribute("itemsPerPage", size);
        locgovStatForm(model, scoped, shWdr, shLocgovCode, bsnsType, effFrDt, effToDt, prjStatus);
        return "designated/analysis-locgov";
    }

    /** 지자체별통계 검색폼에 필요한 모델(선택목록 + 검색조건 되돌리기). GET/POST 공통. */
    private void locgovStatForm(Model model, boolean scoped, String shWdr, String shLocgovCode,
            String bsnsType, String frDt, String toDt, String prjStatus) {
        model.addAttribute("scoped", scoped);
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("bsnsTypes", bsnsTypes());
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        model.addAttribute("bsnsType", bsnsType);
        model.addAttribute("frDt", orToday(frDt));
        model.addAttribute("toDt", orToday(toDt));
        model.addAttribute("prjStatus", prjStatus);
    }

    /** AS-IS: frDt/toDt가 비어 있으면 오늘(yyyyMMdd)로 채운다. */
    private static String orToday(String yyyyMMdd) {
        if (yyyyMMdd != null && !yyyyMMdd.isBlank()) {
            return yyyyMMdd.trim();
        }
        return java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // ---- 월별통계(특정사업 월별통계) - AS-IS designated-donation/analysis/month 재현 ----
    // 본사(ROLE_ADMIN_1~4, 무제한)만 시도/지자체 필터가 보이고, 지자체 담당자는 자기 지자체로 강제 스코핑된다.

    @GetMapping("/designated-projects/analysis/month")
    public String analysisMonth(HttpSession session, Model model) {
        Manager manager = manager(session);
        boolean scoped = MenuService.isLocgovScoped(manager);
        model.addAttribute("scoped", scoped);
        model.addAttribute("myLocgovCode", scoped ? manager.getLocgovCode() : "");
        model.addAttribute("years", analysisYearOptions());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "designated/analysis-month";
    }

    @RequestMapping(value = "/designated-projects/analysis/month/campaign", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public java.util.Map<String, Object> monthCampaign(@RequestParam(required = false) String shWdr,
            @RequestParam(required = false) String shLocgovCode, @RequestParam(required = false) String selYear,
            @RequestParam(required = false) String prjStatus, HttpSession session) {
        Scope s = scope(session, shWdr, shLocgovCode);
        return success(designatedProjectClient.monthCampaign(s.shWdr(), s.shLocgovCode(), selYear, prjStatus));
    }

    @RequestMapping(value = "/designated-projects/analysis/month/amountraised", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public java.util.Map<String, Object> monthAmountRaised(@RequestParam(required = false) String shWdr,
            @RequestParam(required = false) String shLocgovCode, @RequestParam(required = false) String selYear,
            @RequestParam(required = false) String prjStatus, HttpSession session) {
        Scope s = scope(session, shWdr, shLocgovCode);
        return success(designatedProjectClient.monthAmountRaised(s.shWdr(), s.shLocgovCode(), selYear, prjStatus));
    }

    @RequestMapping(value = "/designated-projects/analysis/month/amount", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public java.util.Map<String, Object> monthAmount(@RequestParam(required = false) String shWdr,
            @RequestParam(required = false) String shLocgovCode, @RequestParam(required = false) String selYear,
            @RequestParam(required = false) String prjStatus, HttpSession session) {
        Scope s = scope(session, shWdr, shLocgovCode);
        return success(designatedProjectClient.monthAmount(s.shWdr(), s.shLocgovCode(), selYear, prjStatus));
    }

    /** AS-IS month.jsp JS가 기대하는 응답 봉투 {isSuccess, data}. */
    private static java.util.Map<String, Object> success(Object data) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("isSuccess", true);
        m.put("data", data);
        return m;
    }

    /** 지자체 담당자는 자기 지자체로 강제, 본사는 넘어온 필터 그대로. */
    private Scope scope(HttpSession session, String shWdr, String shLocgovCode) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager)) {
            return new Scope("", manager.getLocgovCode());
        }
        return new Scope(shWdr == null ? "" : shWdr, shLocgovCode == null ? "" : shLocgovCode);
    }

    private record Scope(String shWdr, String shLocgovCode) {
    }

    /** 조회년도 드롭다운 - 올해부터 과거 5개년(AS-IS는 yyyy 코드 목록을 서버가 내려주나, 여기선 간단히 생성). */
    private java.util.List<String> analysisYearOptions() {
        int y = java.time.LocalDate.now().getYear();
        java.util.List<String> years = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            years.add(String.valueOf(y - i));
        }
        return years;
    }

    /**
     * AS-IS POST {@code designated-donation/partList} - 등록/수정 화면에서 지자체를 바꾸면
     * 그 지자체의 사업부서 목록을 다시 받아 온다(ajax). AS-IS는 첫 항목으로
     * "-부서 미지정-"(값 0)을 넣고 그것을 기본 선택으로 둔다 - 그 모양은 화면 JS가 만든다.
     */
    @GetMapping("/designated-projects/departments/by-locgov")
    @ResponseBody
    public Map<String, Object> departmentsByLocgov(@RequestParam(required = false) String locgovCode) {
        return Map.of("isSuccess", true, "data", designatedProjectClient.departments(locgovCode));
    }

    /**
     * AS-IS GET·POST {@code designated-donation/part/list} - <b>사업부서 관리</b>
     * (AS-IS 화면 제목이 "사업부서 관리"다). 검색 4종(지자체·부서명·사용유무·등록일 범위)과
     * 목록 5칸(ID / [지자체 명] / 부서명 / 사용유무 / 등록일시)이 AS-IS 구성이다.
     *
     * <p><b>AS-IS 비활성 그대로</b>: 체크박스 컬럼과 일괄 사용유무 변경은 JSP에서 <b>전부 주석처리</b>
     * 되어 있다({@code updateListDataLabel} 함수만 남아 있고 누르는 버튼이 없다) - 켜지 않는다.
     * 하단 버튼은 [신규등록] 하나다.
     *
     * <p>모든 입구에 부서정보 가드가 걸려 있다 - {@link #departmentDenyReason} 참고.
     */
    @GetMapping("/designated-projects/departments")
    public String departments(@ModelAttribute("searchParam") DepartmentSearchParam searchParam,
                              HttpSession session, Model model) {
        return fillDepartments(searchParam, session, model);
    }

    @PostMapping("/designated-projects/departments/search")
    public String searchDepartments(@ModelAttribute("searchParam") DepartmentSearchParam searchParam,
                                    HttpSession session, Model model) {
        return fillDepartments(searchParam, session, model);
    }

    private String fillDepartments(DepartmentSearchParam searchParam, HttpSession session, Model model) {
        Manager manager = manager(session);
        String denied = departmentDenyReason(manager);
        if (denied != null) {
            return flashRedirect.to("/designated-projects", denied);
        }
        boolean locgovScoped = MenuService.isLocgovScoped(manager);
        if (locgovScoped) {
            // AS-IS: 지자체담당자에게는 지자체 선택 칸이 없다
            searchParam.setUpperLocgovCode(null);
            searchParam.setLocgovCode(manager.getLocgovCode());
        }

        model.addAttribute("departments", designatedProjectClient.searchDepartments(
                searchParam.getUpperLocgovCode(), searchParam.getLocgovCode(), searchParam.getDeptNm(),
                searchParam.getUseYn(), searchParam.getSearchStDt(), searchParam.getSearchEdDt()));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovScoped", locgovScoped);
        // AS-IS 목록의 "지자체 명" 칸 - 코드가 아니라 "광역명 시군구명"으로 보여준다
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("today", java.time.LocalDate.now().format(DAY));
        model.addAttribute("week", java.time.LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", java.time.LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month3", java.time.LocalDate.now().minusMonths(3).format(DAY));
        model.addAttribute("year1", java.time.LocalDate.now().minusYears(1).format(DAY));
        return "designated/departments";
    }

    /**
     * AS-IS GET {@code part/form/{dsgncntrPartId}} - 등록({@code 0})과 수정을 한 화면이 쓴다.
     * AS-IS는 최고관리자가 아니면 자기 지자체·광역을 미리 넣어 둔다.
     */
    @GetMapping("/designated-projects/departments/form/{deptId}")
    public String departmentForm(@PathVariable Long deptId, HttpSession session, Model model) {
        Manager manager = manager(session);
        String denied = departmentDenyReason(manager);
        if (denied != null) {
            return flashRedirect.to("/designated-projects", denied);
        }
        if (deptId != null && deptId > 0) {
            try {
                DesignatedProjectClient.Department dept = designatedProjectClient.department(deptId);
                if (MenuService.isLocgovScoped(manager)
                        && !manager.getLocgovCode().equals(dept.locgovCode())) {
                    return "redirect:/designated-projects/departments";
                }
                model.addAttribute("department", dept);
            } catch (ManagerException e) {
                return flashRedirect.to("/designated-projects/departments", e.getMessage());
            }
        } else {
            model.addAttribute("department", null);
        }
        model.addAttribute("deptId", deptId == null ? 0L : deptId);
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        model.addAttribute("ownLocgovCode", manager == null ? null : manager.getLocgovCode());
        return "designated/department-form";
    }

    /**
     * AS-IS POST {@code part/save} - 등록·수정을 한 엔드포인트가 처리한다.
     * 문구도 AS-IS대로 등록 M00288 / 수정 M00289 / 실패 "저장에 실패했습니다."
     */
    @PostMapping("/designated-projects/departments/save")
    public String saveDepartment(@RequestParam Long deptId, @RequestParam String deptNm,
                                 @RequestParam String locgovCode,
                                 @RequestParam(required = false) String useYn,
                                 HttpSession session) {
        Manager manager = manager(session);
        String denied = departmentDenyReason(manager);
        if (denied != null) {
            return flashRedirect.to("/designated-projects", denied);
        }
        // 지자체담당자는 자기 지자체로 고정한다(AS-IS도 선택 칸을 주지 않는다)
        String targetLocgov = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : locgovCode;
        try {
            if (deptId != null && deptId > 0) {
                designatedProjectClient.updateDepartment(deptId, deptNm, targetLocgov, useYn, manager.getUserId());
                return flashRedirect.to("/designated-projects/departments", "수정되었습니다.");
            }
            designatedProjectClient.createDepartment(deptNm, targetLocgov, manager.getUserId());
            return flashRedirect.to("/designated-projects/departments", "등록되었습니다.");
        } catch (ManagerException e) {
            return "redirect:/designated-projects/departments/form/" + (deptId == null ? 0L : deptId)
                    + "?errorMessage=" + encode(e.getMessage());
        }
    }

    /**
     * AS-IS {@code checkDsgncntrAuthPartInfo()} 가드 - 특정사업 화면들의 공통 입구 검사.
     * 0이면 "부서정보가 없어서 처리가 불가능합니다."로 막는다.
     *
     * <p>AS-IS 분기: <b>ROLE_ADMIN_10</b>(특정사업 담당자)은 자기 부서 id가 없으면 차단 ·
     * <b>지자체담당자·최고관리자</b>는 통과 · 그 외 차단. ROLE_ADMIN_10의 "자기 부서 id" 조회 API가
     * donation에 없어 그 지자체에 부서가 하나도 없으면 막는 것으로 근사했다
     * (배너 화면의 가드와 같은 근사 - {@code RepresentativeBannerController} 주석 참고).
     *
     * @return 막아야 하면 안내문구, 통과면 {@code null}
     */
    private String departmentDenyReason(Manager manager) {
        String authority = manager == null ? null : manager.getAuthority();
        if (authority == null) {
            return DEPARTMENT_REQUIRED;
        }
        if ("ROLE_ADMIN_10".equals(authority)) {
            return designatedProjectClient.departments(manager.getLocgovCode()).isEmpty()
                    ? DEPARTMENT_REQUIRED : null;
        }
        return MASTER_OR_LOCGOV.contains(authority) ? null : DEPARTMENT_REQUIRED;
    }

    /** 지자체코드 → "광역명 시군구명". AS-IS 목록·폼이 코드가 아니라 이름을 보여준다. */
    private java.util.Map<String, String> locgovNames() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm());
        }
        return map;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/designated-projects/departments")
    public String createDepartment(@RequestParam String deptNm, @RequestParam String locgovCode,
                                    HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.createDepartment(deptNm, locgovCode, manager.getUserId());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", designatedProjectClient.departments(null));
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "designated/departments";
        }
        return "redirect:/designated-projects/departments";
    }

    @PostMapping("/designated-projects/departments/{deptId}/toggle")
    public String toggleDepartment(@PathVariable Long deptId, HttpSession session) {
        Manager manager = manager(session);
        designatedProjectClient.toggleDepartment(deptId, manager.getUserId());
        return "redirect:/designated-projects/departments";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean canSelfApprove(Manager manager) {
        return MenuService.UNRESTRICTED_ROLES.contains(manager.getAuthority());
    }

    private static java.util.Map<String, String> statusLabels() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put(STATUS_PENDING, "대기");
        map.put(STATUS_OPEN, "진행중");
        map.put(STATUS_CLOSED, "종료");
        return map;
    }

    /**
     * 사업구분 대분류 - AS-IS {@code CodeUtils.getCodeList("BUSINESS_TYPE")} 과 같은 목록이다.
     *
     * <p>이전에는 donation 서비스의 {@code /api/codes/DSGN_BSNS_TYPE} 를 불렀는데 <b>그 코드유형은
     * 0건</b>이라 select가 늘 비어 있었고(2026-10-07 사용자 지적), donation이 내려가 있으면
     * 예외를 삼키고 빈 Map을 돌려줘 증상이 겹쳐 보였다. 코드 자체는 admin 스키마
     * {@code op_common_code} 에 있으므로 admin 자기 공통코드에서 직접 읽는다.
     *
     * <p>화면에 보이는 글자는 AS-IS와 같이 {@code detail}("사회적 취약계층의 지원 및 …")이다.
     */
    private java.util.Map<String, String> bsnsTypes() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (com.ghlove.admin.domain.CommonCode c : commonCodeService.listByType("BUSINESS_TYPE")) {
            if (!"Y".equals(c.getUseYn())) {
                continue;
            }
            map.put(c.getId(), c.getDetail() != null && !c.getDetail().isBlank() ? c.getDetail() : c.getLabel());
        }
        return map;
    }

    /**
     * 사업구분 중분류(사업부문) - {@code BUSINESS_SUB_TYPE}. 대분류와는 {@code up_id} 로 묶인다
     * (101~108 → 100, 201~207 → 200 …). AS-IS는 대분류를 고를 때마다
     * {@code /opmanager/designated-donation/bsnsSubType} 로 ajax를 쳐서 다시 그리는데,
     * 24건뿐이라 화면에 전부 내려보내고 {@code data-up} 으로 걸러 쓴다(보이는 결과는 같다).
     * 표시 글자는 AS-IS ajax 응답과 같이 {@code label} 이다.
     */
    private List<com.ghlove.admin.domain.CommonCode> bsnsSubTypes() {
        return commonCodeService.listByType("BUSINESS_SUB_TYPE").stream()
                .filter(c -> "Y".equals(c.getUseYn()))
                .toList();
    }

    private java.util.Map<String, String> provinces() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}

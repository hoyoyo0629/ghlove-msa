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
        model.addAttribute("bsnsTypes", designatedProjectClient.codesOf("DSGN_BSNS_TYPE"));
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
        model.addAttribute("notices", designatedProjectClient.noticesOf(id));
        model.addAttribute("approvalLog", designatedProjectClient.approvalLogOf(id));
        return "designated/form";
    }

    private void commonFormAttrs(Model model, Long id, Manager manager) {
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("bsnsTypes", designatedProjectClient.codesOf("DSGN_BSNS_TYPE"));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        // 지자체담당자는 자기 지자체 부서만 고르게 한다(AS-IS도 지자체를 못 바꾼다)
        model.addAttribute("departments", designatedProjectClient.departments(
                MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null));
        model.addAttribute("projectId", id);
        // AS-IS: 승인 버튼은 ROLE_ADMIN_1~5만 볼 수 있다(목록의 일괄 승인처리와 같은 기준)
        model.addAttribute("canApprove", CAN_APPROVE.contains(
                manager == null ? "" : String.valueOf(manager.getAuthority())));
        // AS-IS 수정화면만 보여주는 읽기전용 세 칸(남은 일수/모금 된 금액/달성률).
        // 저장 실패로 폼을 다시 그릴 때도 필요하므로 여기서 한 번에 넣는다.
        if (id != null) {
            model.addAttribute("stat", designatedProjectClient.statOf(id));
        }
    }

    @PostMapping("/designated-projects")
    public String create(@ModelAttribute DesignatedProjectClient.ProjectForm form,
                          @RequestParam(required = false) MultipartFile image,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.create(toYmd(forceOwnLocgov(form, manager)), image, canSelfApprove(manager), manager.getUserId());
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
            designatedProjectClient.update(id, toYmd(forceOwnLocgov(form, manager)), image, canSelfApprove(manager), manager.getUserId());
            return "redirect:/designated-projects/" + id;
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("project", form);
            commonFormAttrs(model, id, manager);
            model.addAttribute("notices", designatedProjectClient.noticesOf(id));
            model.addAttribute("approvalLog", designatedProjectClient.approvalLogOf(id));
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

    @PostMapping("/designated-projects/{id}/notices")
    public String createNotice(@PathVariable Long id, @RequestParam String subject, @RequestParam String content) {
        designatedProjectClient.createNotice(id, subject, content);
        return "redirect:/designated-projects/" + id;
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/update")
    public String updateNotice(@PathVariable Long id, @PathVariable Long noticeId,
                                @RequestParam String subject, @RequestParam String content) {
        designatedProjectClient.updateNotice(noticeId, subject, content);
        return "redirect:/designated-projects/" + id;
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/delete")
    public String deleteNotice(@PathVariable Long id, @PathVariable Long noticeId) {
        designatedProjectClient.deleteNotice(noticeId);
        return "redirect:/designated-projects/" + id;
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

    @GetMapping("/designated-projects/analysis/locgov")
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

        DesignatedProjectClient.LocgovStatSummary summary =
                designatedProjectClient.locgovStatSummary(effWdr, effLocgov, bsnsType, frDt, toDt, prjStatus);
        List<DesignatedProjectClient.LocgovStatRow> all =
                designatedProjectClient.locgovStatList(effWdr, effLocgov, bsnsType, frDt, toDt, prjStatus);

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
        model.addAttribute("scoped", scoped);
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("bsnsTypes", designatedProjectClient.codesOf("DSGN_BSNS_TYPE"));
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        model.addAttribute("bsnsType", bsnsType);
        model.addAttribute("frDt", frDt);
        model.addAttribute("toDt", toDt);
        model.addAttribute("prjStatus", prjStatus);
        return "designated/analysis-locgov";
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

    private java.util.Map<String, String> provinces() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}

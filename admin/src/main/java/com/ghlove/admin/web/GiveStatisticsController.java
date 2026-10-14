package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.GiveOperateStatService;
import com.ghlove.admin.service.GiveStatClient;
import com.ghlove.admin.service.GiveStateService;
import com.ghlove.admin.service.GiveStatisticsService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 기부 통계 (AS-IS opmanager/give/statistics) - AS-IS의 8개+ 화면(전체/지자체별/기부인원/
 *  기부금액/기부건수/개인별/일자별/운영통계 + 외국인 변형) 중 지자체별·운영통계는 이미
 *  구현된 기능(give-state/give-operation/stats)과 중복이라 제외하고, 나머지를 하나의
 *  통합 화면으로 재구성했다 - 사용자 확인 하에 결정된 스코프(제안요청서 SFR-007/009가
 *  화면 단위가 아닌 분석 내용 단위로 요구사항을 기술하는 점 확인 후). 외국인 변형은 이
 *  시스템에 내/외국인 구분 자체가 없어 제외. */
@Controller
@RequiredArgsConstructor
public class GiveStatisticsController {

    private final GiveStatisticsService giveStatisticsService;
    private final GiveStateService giveStateService;
    private final GiveStatClient giveStatClient;
    private final LocgovClient locgovClient;
    private final com.ghlove.admin.service.GiveOperateStatService giveOperateStatService;
    private final com.ghlove.admin.web.support.ExcelDownloadWriter excelDownloadWriter;
    private final com.ghlove.admin.service.CommonCodeService commonCodeService;

    /**
     * AS-IS 통계 화면들의 년도 select - {@code CodeUtils.getCodeList("YYYY")} 공통코드다.
     *
     * <p>★ TO-BE는 {@code giveStateService.availableYears()}(기부 데이터에 실제로 있는 연도)를
     * 쓰고 있었다. 개발DB에 2026년치만 있어 <b>선택지가 2026 하나뿐</b>이라 다른 해를 볼 수
     * 없었다 - AS-IS는 2022~2031 열 개가 뜬다(op_common_code YYYY 10행, 이미 적재돼 있다).
     */
    private List<String> yearCodes() {
        List<String> years = new java.util.ArrayList<>(commonCodeService.labelsOf("YYYY").keySet());
        if (years.isEmpty()) {
            // 코드가 비어 있으면 화면이 통째로 못 쓰게 되므로 데이터 기준으로 물러선다
            return giveStateService.availableYears();
        }
        // AS-IS는 코드 ordering 순서를 그대로 쓴다(최근 연도가 위로 오게 적재돼 있다)
        return years;
    }

    /** AS-IS GiveStatisticsExcelView 의 파일명 꼬리 - {@code DateUtils.getToday(Const.DATETIME_FORMAT)}. */
    private static final java.time.format.DateTimeFormatter EXCEL_FILE_DATETIME =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @GetMapping("/give-statistics")
    public String view(@RequestParam(required = false) String cntrYear,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        Model model) {
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);

        model.addAttribute("monthRows", giveStatisticsService.monthlyTrend(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("ageRows", giveStatisticsService.ageBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("amountRows", giveStatisticsService.amountBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("personalRows", giveStatisticsService.personalRanking(cntrYear, upperLocgovCode, locgovCode, 20));
        model.addAttribute("levyRows", giveStatisticsService.levyReport(cntrYear, upperLocgovCode, locgovCode));
        return "give/give-statistics";
    }

    // ---- 전체(all) - AS-IS give/statistics/all/detail.jsp 재현(요약 3카드 + Chart.js 차트 5종) ----

    @GetMapping("/give-statistics/all")
    public String all(@RequestParam(required = false) String shCntrYear, Model model) {
        List<String> years = yearCodes();
        // AS-IS: 년도가 비어 오면 올해로 채운다(GET/POST 양쪽 동일)
        String year = (shCntrYear != null && !shCntrYear.isBlank()) ? shCntrYear
                : String.valueOf(java.time.LocalDate.now().getYear());
        model.addAttribute("years", years);
        model.addAttribute("shCntrYear", year);
        model.addAttribute("total", giveStatClient.allSummary(year, ""));
        return "give/give-statistics-all";
    }

    @RequestMapping(value = "/give-statistics/all/{year}/month", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> allMonth(@PathVariable String year) {
        return success(giveStatClient.allByMonth(year, ""));
    }

    @RequestMapping(value = "/give-statistics/all/{date}/hour", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> allHour(@PathVariable String date) {
        return success(giveStatClient.allByHour(date, ""));
    }

    private static Map<String, Object> success(Object data) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", true);
        m.put("data", data);
        return m;
    }

    // ---- 지자체별 - AS-IS give/statistics/locgov/list.jsp 재현(차트 없는 서버렌더 표) ----

    @GetMapping("/give-statistics/locgov")
    public String locgov(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "") String shCntrYear,
            @RequestParam(required = false, defaultValue = "") String itemsOrder,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        boolean scoped = MenuService.isLocgovScoped(manager);
        String effLocgov = scoped ? manager.getLocgovCode() : shLocgovCode;

        List<GiveStatClient.LocgovYearRow> all =
                new java.util.ArrayList<>(giveStatClient.locgovList(shCntrYear, effLocgov));
        // 정렬(AS-IS itemsOrder): 지정 시 해당 컬럼 내림차순(2차 정렬은 서버 기본순 유지).
        // 엑셀도 같은 정렬이어야 하므로 한 곳에서 처리한다.
        sortLocgovRows(all, itemsOrder);

        int total = all.size();
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int cur = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((cur - 1) * size, total);
        int to = Math.min(from + size, total);

        model.addAttribute("rows", all.subList(from, to));
        model.addAttribute("startNo", total - from);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", cur);
        model.addAttribute("itemsPerPage", size);
        model.addAttribute("itemsOrder", itemsOrder);
        model.addAttribute("scoped", scoped);
        // AS-IS locgov/list.jsp 도 같은 YYYY 공통코드를 쓴다
        model.addAttribute("years", yearCodes());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        model.addAttribute("shCntrYear", shCntrYear);
        return "give/give-statistics-locgov";
    }

    /**
     * AS-IS {@code GET /opmanager/give/statistics/locgov/excel} - 지자체별 통계 엑셀.
     *
     * <p>AS-IS는 {@code GiveStatisticsExcelView} 에 {@code type="locgov"} 를 넘겨
     * 제목 <b>"기부금 모금 현황"</b> + No./년도/지자체/기부인원/기부금액/기부건수 6칸으로 쓴다.
     * 목록과 달리 <b>페이징 없이 조건에 맞는 전체</b>가 대상이다
     * ({@code searchParam.setPagination(null)}).
     *
     * <p>AS-IS 화면이 {@code Shop.downloadExcelOrder(..., true)} 로 호출하므로
     * 개인정보 사유 모달을 먼저 거친다 - 다른 엑셀 화면과 같은 흐름이다.
     */
    @GetMapping("/give-statistics/locgov/excel")
    public void locgovExcel(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "") String shCntrYear,
            @RequestParam(required = false, defaultValue = "") String itemsOrder,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        // AS-IS: 지자체 담당자는 조건과 무관하게 자기 지자체로 고정된다(findAdminRoleAndLocgov).
        String effLocgov = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : shLocgovCode;

        List<GiveStatClient.LocgovYearRow> rows =
                new java.util.ArrayList<>(giveStatClient.locgovList(shCntrYear, effLocgov));
        sortLocgovRows(rows, itemsOrder);

        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook =
                new org.apache.poi.xssf.streaming.SXSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("기부금 모금 현황");
        com.ghlove.admin.web.support.ExcelDownloadWriter.CellStyles styles =
                excelDownloadWriter.createStyles(workbook);

        // AS-IS GiveStatisticsExcelView 의 HeaderCell 폭 그대로
        String[] headers = {"No.", "년도", "지자체", "기부인원", "기부금액", "기부건수"};
        int[] widths = {700, 2500, 3500, 3500, 3500, 3500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "기부금 모금 현황", headers, widths);

        // AS-IS는 1부터 증가(목록의 역순 번호와 다르다 - ++index)
        int no = 0;
        for (GiveStatClient.LocgovYearRow row : rows) {
            org.apache.poi.ss.usermodel.Row xlRow = sheet.createRow(rowIndex++);
            xlRow.setHeight((short) 400);
            int col = 0;
            excelDownloadWriter.dataCell(xlRow, col++, styles, String.valueOf(++no));
            excelDownloadWriter.dataCell(xlRow, col++, styles, String.valueOf(row.cntrYear()));
            excelDownloadWriter.dataCell(xlRow, col++, styles, row.upperLocgovNm() + " " + row.locgovNm());
            excelDownloadWriter.dataCell(xlRow, col++, styles, String.format("%,d", row.givePersons()));
            excelDownloadWriter.dataCell(xlRow, col++, styles, String.format("%,d", row.cntrAmt()));
            excelDownloadWriter.dataCell(xlRow, col, styles, String.format("%,d", row.giveCnt()));
        }

        String fileName = "기부금_모금_현황_" + java.time.LocalDateTime.now().format(EXCEL_FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    /** 목록과 엑셀이 같은 정렬을 쓰도록 뽑아 둔다(AS-IS itemsOrder). */
    private static void sortLocgovRows(List<GiveStatClient.LocgovYearRow> rows, String itemsOrder) {
        if ("person".equals(itemsOrder)) {
            rows.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::givePersons).reversed());
        } else if ("amt".equals(itemsOrder)) {
            rows.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::cntrAmt).reversed());
        } else if ("cnt".equals(itemsOrder)) {
            rows.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::giveCnt).reversed());
        }
    }

    // ---- 운영현황 - AS-IS give/statistics/operate (지자체×용도 지출 피벗 + 지자체별 연도 상세) ----

    @GetMapping("/give-statistics/operate")
    public String operateList(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shCntrYear,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        // AS-IS: 지자체 담당자는 목록 없이 자기 지자체 상세로 바로 이동.
        if (MenuService.isLocgovScoped(manager)) {
            return "redirect:/give-statistics/operate/" + manager.getLocgovCode();
        }
        String year = shCntrYear.isBlank() ? String.valueOf(java.time.LocalDate.now().getYear()) : shCntrYear;

        List<GiveOperateStatService.OperateListRow> all = giveOperateStatService.operateList(year, shLocgovCode);
        int total = all.size();
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int cur = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((cur - 1) * size, total);
        int to = Math.min(from + size, total);

        model.addAttribute("useList", giveOperateStatService.codeList());
        model.addAttribute("list", all.subList(from, to));
        model.addAttribute("startNo", total - from);
        model.addAttribute("count", total);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", cur);
        model.addAttribute("itemsPerPage", size);
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("shCntrYear", year);
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        return "give/give-statistics-operate-list";
    }

    @GetMapping("/give-statistics/operate/{locgovCode}")
    public String operateDetail(HttpSession session, @PathVariable String locgovCode, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        boolean scoped = MenuService.isLocgovScoped(manager);
        String eff = scoped ? manager.getLocgovCode() : locgovCode;
        model.addAttribute("codeList", giveOperateStatService.codeList());
        model.addAttribute("shLocgovCode", eff);
        model.addAttribute("scoped", scoped);
        model.addAttribute("locgovFullNm", locgovFullNm(eff));
        return "give/give-statistics-operate-detail";
    }

    @RequestMapping(value = "/give-statistics/operate/{locgovCode}/detail", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> operateDetailData(HttpSession session, @PathVariable String locgovCode) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String eff = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : locgovCode;
        List<GiveOperateStatService.CodeLabel> codes = giveOperateStatService.codeList();
        List<GiveOperateStatService.OperateDetailRow> rows = giveOperateStatService.operateDetailByLocgov(eff);
        // AS-IS detail.jsp JS가 data['amt'+id]/['cnt'+id] 평면키를 읽으므로 그 형태로 직렬화.
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (GiveOperateStatService.OperateDetailRow r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cntrYear", r.cntrYear());
            for (GiveOperateStatService.CodeLabel c : codes) {
                m.put("amt" + c.id(), r.amts().getOrDefault(c.id(), 0L));
                m.put("cnt" + c.id(), r.cnts().getOrDefault(c.id(), 0L));
            }
            m.put("expndtrSum", r.expndtrSum());
            m.put("expndtrCnt", r.expndtrCnt());
            list.add(m);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("codeList", codes);
        data.put("list", list);
        return success(data);
    }

    private String locgovFullNm(String locgovCode) {
        if (locgovCode == null || locgovCode.isBlank()) {
            return "";
        }
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            if (locgovCode.equals(l.locgovCode())) {
                return (l.upperLocgovNm() + " " + l.locgovNm()).trim();
            }
        }
        return "";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}

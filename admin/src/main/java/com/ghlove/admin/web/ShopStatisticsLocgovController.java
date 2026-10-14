package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.ShopStatisticsLocgovService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 지자체별 (AS-IS {@code ShopStatisticsManagerController#salesLocgov} 일가
 * + {@code sales/locgov-mois.jsp}/{@code sales/locgov.jsp}/{@code sales/detail.jsp}).
 *
 * <p>AS-IS는 <b>한 URL</b>({@code /sales/locgov})이 로그인한 관리자 권한(ROLE_ADMIN_1~4=행안부
 * "mois" / ROLE_ADMIN_5~6=지자체담당자 "locgov")과 {@code locgovCode} 파라미터 유무로 서로 다른
 * 화면을 그린다 - mois이고 locgovCode가 없으면 <b>지자체별 목록</b>(locgov-mois), 그 외
 * (locgov 역할이거나 mois인데 locgovCode를 골랐으면) <b>답례품별 목록</b>(locgov)이다.
 * {@link MenuService#isLocgovScoped}가 AS-IS의 역할 분기와 같다([[admin-interest-locgov-port]]
 * 에서 같은 분기를 이미 썼다). ★이전 구현(ShopStatisticsTierBController의 "M. 지자체별
 * 구매현황")은 이 역할 분기·WDR 2단 select·정렬·페이징·엑셀이 전부 없는 발명된 축소판이었다
 * (2026-10-14 전면교체).
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsLocgovController {

    private final ShopStatisticsLocgovService service;
    private final LocgovClient locgovClient;
    private final com.ghlove.admin.service.CommonCodeService commonCodeService;
    private final ExcelDownloadWriter excelDownloadWriter;

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ---- 목록/답례품별 (AS-IS GET/POST sales/locgov) ----

    @GetMapping("/shop-statistics/sales/locgov")
    public String locgov(@ModelAttribute("statisticsParam") LocgovStatsParam param,
                          HttpServletRequest request, HttpSession session, Model model) {
        return render(param, request, session, model);
    }

    @PostMapping("/shop-statistics/sales/locgov")
    public String searchLocgov(@ModelAttribute("statisticsParam") LocgovStatsParam param,
                                HttpServletRequest request, HttpSession session, Model model) {
        return render(param, request, session, model);
    }

    private String render(LocgovStatsParam param, HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        boolean mois = !MenuService.isLocgovScoped(manager);
        model.addAttribute("statisticsParam", param);
        model.addAttribute("years", yearCodes());
        model.addAttribute("role", mois ? "mois" : "locgov");

        if (mois && isBlank(param.getLocgovCode())) {
            // ---- 지자체별 목록 (locgov-mois) ----
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());

            List<ShopStatisticsLocgovService.LocgovRow> all =
                    service.locgovRows(manager, param.startYearMonth(), param.endYearMonth());
            List<ShopStatisticsLocgovService.LocgovRow> filtered = filterLocgov(all, param);
            sortLocgov(filtered, param);

            Pagination pagination = Pagination.of(filtered.size(), param.getPage(), param.getItemsPerPage())
                    .withLinkFrom(request);
            model.addAttribute("dateList", page(filtered, pagination));
            model.addAttribute("pagination", pagination);
            model.addAttribute("count", filtered.size());
            return "shop-statistics/sales-locgov-mois";
        }

        // ---- 답례품별 목록 (locgov) ----
        String locgovCode = mois ? param.getLocgovCode() : manager.getLocgovCode();
        List<ShopStatisticsLocgovService.ItemRow> all =
                service.itemRows(manager, param.startYearMonth(), param.endYearMonth(), locgovCode);
        List<ShopStatisticsLocgovService.ItemRow> filtered = filterItem(all, param);
        sortItem(filtered, param);

        Pagination pagination = Pagination.of(filtered.size(), param.getPage(), param.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("dateList", page(filtered, pagination));
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", filtered.size());
        return "shop-statistics/sales-locgov";
    }

    // ---- 엑셀 (AS-IS mois/download-excel, download-excel) ----

    @GetMapping("/shop-statistics/sales/locgov/mois/download-excel")
    public void downloadExcelMois(@ModelAttribute("statisticsParam") LocgovStatsParam param,
                                   HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        List<ShopStatisticsLocgovService.LocgovRow> rows = filterLocgov(
                service.locgovRows(manager, param.startYearMonth(), param.endYearMonth()), param);
        sortLocgov(rows, param);

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("지자체별 매출");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);
        String[] headers = {"No.", "지자체", "주문 건수", "주문 금액(p)", "반품/환불 건수", "반품/환불 금액(p)",
                "합계 건수", "합계 금액(p)"};
        int[] widths = {700, 3500, 3000, 3500, 3000, 3500, 3000, 3500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "답례품 구매현황(지자체별)", headers, widths);

        // AS-IS ShopStatisticsMoisExcelView: length - (rowIndex-2) - 화면과 같은 내림차순 No.
        int no = rows.size();
        for (ShopStatisticsLocgovService.LocgovRow r : rows) {
            Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(no--));
            excelDownloadWriter.dataCell(xl, c++, styles, r.displayName());
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.saleCount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.saleAmount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.cancelCount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.cancelAmount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.sumCount()));
            excelDownloadWriter.dataCell(xl, c, styles, String.format("%,d", r.sumAmount()));
        }

        String fileName = "지자체별매출_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    @GetMapping("/shop-statistics/sales/locgov/download-excel")
    public void downloadExcelItem(@ModelAttribute("statisticsParam") LocgovStatsParam param,
                                   HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        boolean mois = !MenuService.isLocgovScoped(manager);
        String locgovCode = mois ? param.getLocgovCode() : manager.getLocgovCode();
        List<ShopStatisticsLocgovService.ItemRow> rows = filterItem(
                service.itemRows(manager, param.startYearMonth(), param.endYearMonth(), locgovCode), param);
        sortItem(rows, param);

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("답례품별 매출");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);
        String[] headers = {"No.", "답례품명", "상호명", "주문 건수", "주문 금액(p)", "반품/환불 건수", "반품/환불 금액(p)",
                "합계 건수", "합계 금액(p)"};
        int[] widths = {700, 4000, 3500, 3000, 3500, 3000, 3500, 3000, 3500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "답례품 구매현황(답례품별)", headers, widths);

        // AS-IS ShopStatisticsLocgovExcelView: length - (rowIndex-2) - 화면과 같은 내림차순 No.
        int no = rows.size();
        for (ShopStatisticsLocgovService.ItemRow r : rows) {
            Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(no--));
            excelDownloadWriter.dataCell(xl, c++, styles, r.itemName());
            excelDownloadWriter.dataCell(xl, c++, styles, r.companyName());
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.saleCount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.saleAmount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.cancelCount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.cancelAmount()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.sumCount()));
            excelDownloadWriter.dataCell(xl, c, styles, String.format("%,d", r.sumAmount()));
        }

        String fileName = "답례품별매출_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    // ---- 상세 팝업 (AS-IS sales/locgov/detail) ----

    @GetMapping("/shop-statistics/sales/locgov/detail")
    public String detail(@RequestParam Long itemId,
                          @RequestParam(required = false) String year,
                          @RequestParam(defaultValue = "1") int page,
                          HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        String y = isBlank(year) ? String.valueOf(LocalDate.now().getYear()) : year;
        List<ShopStatisticsLocgovService.UserRow> rows = service.userRows(manager, y + "01", y + "12", itemId);

        // AS-IS 상세는 정렬 UI가 없다 - 등록 순서(= order 그룹 반환 순서) 그대로.
        Pagination pagination = Pagination.of(rows.size(), page, 5).withLinkFrom(request);
        model.addAttribute("dateList", page(rows, pagination));
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", rows.size());
        model.addAttribute("itemName", service.itemNameOf(manager, y + "01", y + "12", itemId));
        return "shop-statistics/sales-locgov-detail";
    }

    // ---- 정렬/필터/페이징 ----

    private static List<ShopStatisticsLocgovService.LocgovRow> filterLocgov(
            List<ShopStatisticsLocgovService.LocgovRow> rows, LocgovStatsParam param) {
        String upper = param.getUpperLocgovCode();
        String exact = param.getLocgovCode();
        if (isBlank(upper) && isBlank(exact)) {
            return new java.util.ArrayList<>(rows);
        }
        List<ShopStatisticsLocgovService.LocgovRow> out = new java.util.ArrayList<>();
        for (ShopStatisticsLocgovService.LocgovRow r : rows) {
            if (!isBlank(exact) && !exact.equals(r.locgovCode())) {
                continue;
            }
            out.add(r);
        }
        return out;
    }

    private static List<ShopStatisticsLocgovService.ItemRow> filterItem(
            List<ShopStatisticsLocgovService.ItemRow> rows, LocgovStatsParam param) {
        String query = param.getQuery();
        if (isBlank(query)) {
            return new java.util.ArrayList<>(rows);
        }
        boolean byCompany = "COMPANY_NAME".equals(param.getWhere());
        List<ShopStatisticsLocgovService.ItemRow> out = new java.util.ArrayList<>();
        for (ShopStatisticsLocgovService.ItemRow r : rows) {
            String target = byCompany ? r.companyName() : r.itemName();
            if (target != null && target.contains(query)) {
                out.add(r);
            }
        }
        return out;
    }

    private static void sortLocgov(List<ShopStatisticsLocgovService.LocgovRow> rows, LocgovStatsParam param) {
        Comparator<ShopStatisticsLocgovService.LocgovRow> cmp = orderBySortKey(param,
                ShopStatisticsLocgovService.LocgovRow::saleCount, ShopStatisticsLocgovService.LocgovRow::saleAmount,
                ShopStatisticsLocgovService.LocgovRow::cancelCount, ShopStatisticsLocgovService.LocgovRow::cancelAmount,
                ShopStatisticsLocgovService.LocgovRow::sumCount, ShopStatisticsLocgovService.LocgovRow::sumAmount);
        if (cmp != null) {
            rows.sort(cmp.thenComparing(ShopStatisticsLocgovService.LocgovRow::locgovCode));
        } else {
            // AS-IS 기본: T.GROUP_OBJECT ASC(지자체 표시명 가나다순)
            rows.sort(Comparator.comparing(ShopStatisticsLocgovService.LocgovRow::displayName));
        }
    }

    private static void sortItem(List<ShopStatisticsLocgovService.ItemRow> rows, LocgovStatsParam param) {
        Comparator<ShopStatisticsLocgovService.ItemRow> cmp = orderBySortKey(param,
                ShopStatisticsLocgovService.ItemRow::saleCount, ShopStatisticsLocgovService.ItemRow::saleAmount,
                ShopStatisticsLocgovService.ItemRow::cancelCount, ShopStatisticsLocgovService.ItemRow::cancelAmount,
                ShopStatisticsLocgovService.ItemRow::sumCount, ShopStatisticsLocgovService.ItemRow::sumAmount);
        if (cmp != null) {
            rows.sort(cmp.thenComparing(r -> r.itemName() == null ? "" : r.itemName()));
        } else {
            // AS-IS 기본: SUM_COUNT DESC(답례품구매현황 답례품별)
            rows.sort(Comparator.comparingLong(ShopStatisticsLocgovService.ItemRow::sumCount).reversed());
        }
    }

    /** AS-IS orderBy 6종(orderCase/orderPoint/claimCase/claimPoint/sumCase/sumPoint) 공통 처리.
     *  orderBy가 없으면 null(호출부가 각 화면의 기본 정렬로 대체한다). */
    private static <T> Comparator<T> orderBySortKey(LocgovStatsParam param,
            java.util.function.ToLongFunction<T> orderCase, java.util.function.ToLongFunction<T> orderPoint,
            java.util.function.ToLongFunction<T> claimCase, java.util.function.ToLongFunction<T> claimPoint,
            java.util.function.ToLongFunction<T> sumCase, java.util.function.ToLongFunction<T> sumPoint) {
        String orderBy = param.getOrderBy();
        if (isBlank(orderBy)) {
            return null;
        }
        java.util.function.ToLongFunction<T> key = switch (orderBy) {
            case "orderCase" -> orderCase;
            case "orderPoint" -> orderPoint;
            case "claimCase" -> claimCase;
            case "claimPoint" -> claimPoint;
            case "sumCase" -> sumCase;
            default -> sumPoint; // sumPoint 및 그 외는 AS-IS otherwise와 같다
        };
        Comparator<T> cmp = Comparator.comparingLong(key::applyAsLong);
        return param.isDesc() ? cmp.reversed() : cmp;
    }

    private static <T> List<T> page(List<T> rows, Pagination pagination) {
        int from = Math.min(pagination.getStartRow(), rows.size());
        int to = Math.min(from + pagination.getItemsPerPage(), rows.size());
        return new java.util.ArrayList<>(rows.subList(from, to));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS 화면의 년도 select - YYYY 공통코드(매출통계 공통 함정,
     *  [[admin-give-statistics-year-select-source]] - 데이터 있는 연도만 뽑으면 안 된다). */
    private List<String> yearCodes() {
        List<String> years = new java.util.ArrayList<>(commonCodeService.labelsOf("YYYY").keySet());
        if (years.isEmpty()) {
            years.add(String.valueOf(LocalDate.now().getYear()));
        }
        return years;
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    /** AS-IS StatisticsParam 중 지자체별 화면이 쓰는 것만. */
    public static class LocgovStatsParam {
        private String year;
        private String locgovSearchMonth;
        private String upperLocgovCode;
        private String locgovCode;
        private String where = "ITEM_NAME";
        private String query;
        private String orderBy;
        private boolean isDesc;
        private int page = 1;
        private int itemsPerPage = 10;

        public String getYear() {
            return isBlank(year) ? String.valueOf(LocalDate.now().getYear()) : year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getLocgovSearchMonth() {
            return locgovSearchMonth == null ? "" : locgovSearchMonth;
        }

        public void setLocgovSearchMonth(String locgovSearchMonth) {
            this.locgovSearchMonth = locgovSearchMonth;
        }

        public String getUpperLocgovCode() {
            return upperLocgovCode;
        }

        public void setUpperLocgovCode(String upperLocgovCode) {
            this.upperLocgovCode = upperLocgovCode;
        }

        public String getLocgovCode() {
            return locgovCode;
        }

        public void setLocgovCode(String locgovCode) {
            this.locgovCode = locgovCode;
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public boolean isDesc() {
            return isDesc;
        }

        public void setDesc(boolean desc) {
            this.isDesc = desc;
        }

        // 화면(th:field)용 - AS-IS form:hidden path="isDesc"와 같은 이유로 getter 이름을 맞춘다.
        public boolean getIsDesc() {
            return isDesc;
        }

        public void setIsDesc(boolean isDesc) {
            this.isDesc = isDesc;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }

        /** AS-IS locgovSearchStartYearMonth - month 미지정시 1월부터. */
        public String startYearMonth() {
            return getYear() + (isBlank(getLocgovSearchMonth()) ? "01" : getLocgovSearchMonth());
        }

        /** AS-IS locgovSearchEndYearMonth - month 미지정시 12월까지. */
        public String endYearMonth() {
            return getYear() + (isBlank(getLocgovSearchMonth()) ? "12" : getLocgovSearchMonth());
        }
    }
}

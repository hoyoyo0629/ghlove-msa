package com.ghlove.admin.web;

import com.ghlove.admin.service.OrderAdminClient;
import com.ghlove.admin.service.ShopStatisticsTierBService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** AS-IS opmanager/shop-statistics의 "판매(sales)/대시보드/선호도" 스위트 재구현
 *  (report 스위트는 {@link ReportStatisticsController}가 별개로 담당). */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsTierBController {

    private final ShopStatisticsTierBService service;
    private final OrderAdminClient orderAdminClient;
    private final ExcelDownloadWriter excelDownloadWriter;
    private final com.ghlove.admin.service.CommonCodeService commonCodeService;

    // ---- A-4. 결제타입별 매출 (AS-IS shop-statistics/sales/payment.jsp) ----

    /**
     * TO-BE 답례품 주문의 유일한 결제수단.
     *
     * <p>AS-IS는 {@code OP_ORDER_ITEM.APPROVAL_TYPE}(ORDER_PAY_TYPE 코드 12종)으로 열을 나누지만
     * TO-BE 주문표에는 결제수단 컬럼 자체가 없다 - 전액 기부포인트 결제이기 때문이다
     * ({@code OrderReconciliationRepository} 주석도 "TO-BE는 결제수단이 포인트뿐"이라고 적고 있다).
     * 그래서 모든 금액이 이 코드 열에 들어가고 나머지 11종은 0원이다
     * (2026-10-13 사용자 결정 - 열 구조는 AS-IS 그대로 둔다).
     */
    static final String PAY_TYPE_GIVE_POINT = "give-point";

    /** AS-IS 표의 기기 열은 PC/모바일/기타 셋인데 TO-BE에 device_type 이 없어 전부 "기타"로 모은다. */
    static final String DEVICE_COL_ETC = "etc";

    @GetMapping("/shop-statistics/sales/payment")
    public String salesPayment(@ModelAttribute("statisticsParam") PaymentStatsParam param,
                                jakarta.servlet.http.HttpSession session, Model model) {
        // AS-IS: GET 에서도 날짜 행 목록은 만든다(값만 비어 있다)
        paymentFormAttrs(model, session, param);
        model.addAttribute("rowsByDate", java.util.Map.of());
        model.addAttribute("hasData", false);
        return "shop-statistics/sales-payment";
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/payment")
    public String searchSalesPayment(@ModelAttribute("statisticsParam") PaymentStatsParam param,
                                      jakarta.servlet.http.HttpSession session, Model model) {
        java.util.Map<String, OrderAdminClient.DateStatRow> byDate = paymentRows(param, manager(session));
        paymentFormAttrs(model, session, param);
        model.addAttribute("rowsByDate", byDate);
        model.addAttribute("hasData", !byDate.isEmpty());
        model.addAttribute("queryString", param.toQueryString());
        return "shop-statistics/sales-payment";
    }

    @GetMapping("/shop-statistics/sales/payment/excel-download")
    public void salesPaymentExcel(@ModelAttribute("statisticsParam") PaymentStatsParam param,
                                   jakarta.servlet.http.HttpSession session,
                                   jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.ghlove.admin.domain.Manager viewer = manager(session);
        java.util.Map<String, OrderAdminClient.DateStatRow> byDate = paymentRows(param, viewer);
        java.util.Map<String, String> payTypes = commonCodeService.labelsOf("ORDER_PAY_TYPE");

        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("결제타입별 매출");
        com.ghlove.admin.web.support.ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);

        java.util.List<String> headers = new java.util.ArrayList<>(java.util.List.of("날짜", "매출총액"));
        for (String label : payTypes.values()) {
            headers.add(label + " PC");
            headers.add(label + " 모바일");
            headers.add(label + " 기타");
        }
        int[] widths = new int[headers.size()];
        java.util.Arrays.fill(widths, 3500);
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "결제타입별 매출",
                headers.toArray(new String[0]), widths);

        for (String day : param.days()) {
            OrderAdminClient.DateStatRow r = byDate.get(day);
            long net = r == null ? 0L : r.totalPayAmount();
            org.apache.poi.ss.usermodel.Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, formatDate(day));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", net));
            for (String code : payTypes.keySet()) {
                boolean mine = PAY_TYPE_GIVE_POINT.equals(code);
                excelDownloadWriter.dataCell(xl, c++, styles, "0");                                  // PC
                excelDownloadWriter.dataCell(xl, c++, styles, "0");                                  // 모바일
                excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", mine ? net : 0L)); // 기타
            }
        }

        String fileName = "결제타입별매출_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, viewer == null ? null : viewer.getLoginId());
    }

    /**
     * AS-IS 는 체크한 결제타입으로 <b>데이터를 거른다</b>(열은 늘 전체를 그린다).
     * TO-BE 금액은 전부 기부포인트라, 기부포인트를 빼고 체크하면 결과가 비는 것이 맞다.
     */
    private java.util.Map<String, OrderAdminClient.DateStatRow> paymentRows(
            PaymentStatsParam param, com.ghlove.admin.domain.Manager manager) {
        if (!param.includesGivePoint()) {
            return java.util.Map.of();
        }
        java.util.Map<String, OrderAdminClient.DateStatRow> byDate = new java.util.LinkedHashMap<>();
        for (OrderAdminClient.DateStatRow r : orderAdminClient.dateStats(
                manager, "day", param.getStartDate(), param.getEndDate())) {
            byDate.put(r.searchDate(), r);
        }
        return byDate;
    }

    private void paymentFormAttrs(Model model, jakarta.servlet.http.HttpSession session, PaymentStatsParam param) {
        model.addAttribute("payTypes", commonCodeService.labelsOf("ORDER_PAY_TYPE"));
        model.addAttribute("days", param.days());
        model.addAttribute("givePointCode", PAY_TYPE_GIVE_POINT);
        model.addAttribute("canExcel", canExcel(manager(session)));
    }

    // ---- A. 기간별 매출 통계 ----

    // ---- A-2. 월별 매출 (AS-IS shop-statistics/sales/month.jsp) ----

    /** AS-IS: GET은 조회하지 않고 빈 표만 준다(일자별과 같다). */
    @GetMapping("/shop-statistics/sales/month")
    public String salesMonth(@ModelAttribute("statisticsParam") MonthStatsParam param,
                              jakarta.servlet.http.HttpSession session, Model model) {
        monthFormAttrs(model, session);
        model.addAttribute("dateList", java.util.List.of());
        return "shop-statistics/sales-month";
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/month")
    public String searchSalesMonth(@ModelAttribute("statisticsParam") MonthStatsParam param,
                                    jakarta.servlet.http.HttpSession session, Model model) {
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                manager(session), "month", param.toStartDate(), param.toEndDate());
        monthFormAttrs(model, session);
        model.addAttribute("dateList", list);
        model.addAttribute("total", DayStatsTotal.of(list));
        model.addAttribute("queryString", param.toQueryString());
        return "shop-statistics/sales-month";
    }

    @GetMapping("/shop-statistics/sales/month/excel-download")
    public void salesMonthExcel(@ModelAttribute("statisticsParam") MonthStatsParam param,
                                 jakarta.servlet.http.HttpSession session,
                                 jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.ghlove.admin.domain.Manager viewer = manager(session);
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                viewer, "month", param.toStartDate(), param.toEndDate());
        writeDateStatsExcel(response, viewer, list, "월별 매출", "월별매출_");
    }

    /** AS-IS month.jsp 의 년/월 select - 년도는 2010~올해, 월은 01~12. */
    private void monthFormAttrs(Model model, jakarta.servlet.http.HttpSession session) {
        int thisYear = java.time.LocalDate.now().getYear();
        java.util.List<String> years = new java.util.ArrayList<>();
        for (int y = 2010; y <= thisYear; y++) {
            years.add(String.valueOf(y));
        }
        java.util.List<String> months = new java.util.ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(String.format("%02d", m));
        }
        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("canExcel", canExcel(manager(session)));
    }

    // ---- A-3. 년도별 매출 (AS-IS shop-statistics/sales/year.jsp) ----

    /** AS-IS: GET은 조회하지 않고 빈 표만 준다(일자별·월별과 같다). */
    @GetMapping("/shop-statistics/sales/year")
    public String salesYear(@ModelAttribute("statisticsParam") YearStatsParam param,
                             jakarta.servlet.http.HttpSession session, Model model) {
        yearFormAttrs(model, session);
        model.addAttribute("dateList", java.util.List.of());
        return "shop-statistics/sales-year";
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/year")
    public String searchSalesYear(@ModelAttribute("statisticsParam") YearStatsParam param,
                                   jakarta.servlet.http.HttpSession session, Model model) {
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                manager(session), "year", param.toStartDate(), param.toEndDate());
        yearFormAttrs(model, session);
        model.addAttribute("dateList", list);
        model.addAttribute("total", DayStatsTotal.of(list));
        model.addAttribute("queryString", param.toQueryString());
        return "shop-statistics/sales-year";
    }

    @GetMapping("/shop-statistics/sales/year/excel-download")
    public void salesYearExcel(@ModelAttribute("statisticsParam") YearStatsParam param,
                                jakarta.servlet.http.HttpSession session,
                                jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.ghlove.admin.domain.Manager viewer = manager(session);
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                viewer, "year", param.toStartDate(), param.toEndDate());
        writeDateStatsExcel(response, viewer, list, "연도별 매출", "연도별매출_");
    }

    /** AS-IS year.jsp 의 년도 select - 2010~올해(월 select 는 없다). */
    private void yearFormAttrs(Model model, jakarta.servlet.http.HttpSession session) {
        int thisYear = java.time.LocalDate.now().getYear();
        java.util.List<String> years = new java.util.ArrayList<>();
        for (int y = 2010; y <= thisYear; y++) {
            years.add(String.valueOf(y));
        }
        model.addAttribute("years", years);
        model.addAttribute("canExcel", canExcel(manager(session)));
    }

    // ---- A-1. 일자별 매출 (AS-IS shop-statistics/sales/day.jsp) ----

    /**
     * AS-IS는 <b>GET이면 조회하지 않고 빈 화면</b>을 주고 POST에서만 검색한다 - 그대로 따른다.
     * (AS-IS {@code ShopStatisticsManagerController#salesDay} 는 모델에 아무것도 담지 않는다.)
     */
    @GetMapping("/shop-statistics/sales/day")
    public String salesDay(@ModelAttribute("statisticsParam") DayStatsParam param,
                            jakarta.servlet.http.HttpSession session, Model model) {
        model.addAttribute("dateList", java.util.List.of());
        model.addAttribute("canExcel", canExcel(manager(session)));
        return "shop-statistics/sales-day";
    }

    /**
     * AS-IS {@code <sec:authorize access="hasRole('ROLE_EXCEL')">} 그대로.
     *
     * <p>★ AS-IS에서 {@code ROLE_EXCEL} 은 <b>판매자 계정에만</b> 붙는다
     * ({@code SellerServiceImpl}/{@code SellerUserServiceImpl} 에서 부여) - 운영자에게 주는
     * 코드가 없고 {@code OP_ROLE} 에 행도 없다({@link com.ghlove.admin.domain.Role} 주석 참고).
     * 즉 <b>AS-IS 운영자 화면에서도 이 엑셀 버튼은 뜨지 않는다.</b> 비활성 상태까지 그대로
     * 옮기는 원칙에 따라 켜지 않았다 - 엔드포인트는 만들어 뒀으므로 권한만 주면 바로 쓸 수 있다.
     */
    private static boolean canExcel(com.ghlove.admin.domain.Manager manager) {
        return manager != null && "ROLE_EXCEL".equals(manager.getAuthority());
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/day")
    public String searchSalesDay(@ModelAttribute("statisticsParam") DayStatsParam param,
                                  jakarta.servlet.http.HttpSession session, Model model) {
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                manager(session), "day", param.getStartDate(), param.getEndDate());
        model.addAttribute("dateList", list);
        model.addAttribute("total", DayStatsTotal.of(list));
        // AS-IS는 검색조건을 쿼리스트링으로 들고 다니다가 엑셀 링크에 붙인다
        model.addAttribute("queryString", param.toQueryString());
        model.addAttribute("canExcel", canExcel(manager(session)));
        return "shop-statistics/sales-day";
    }

    /**
     * AS-IS {@code /sales/day/excel-download} - {@code ROLE_EXCEL} 권한자만 쓸 수 있고,
     * 목록과 같은 조건·같은 집계를 그대로 엑셀로 쓴다(AS-IS {@code DayStatisticsExcelView}).
     */
    @GetMapping("/shop-statistics/sales/day/excel-download")
    public void salesDayExcel(@ModelAttribute("statisticsParam") DayStatsParam param,
                               jakarta.servlet.http.HttpSession session,
                               jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.ghlove.admin.domain.Manager viewer = manager(session);
        java.util.List<OrderAdminClient.DateStatRow> list = orderAdminClient.dateStats(
                viewer, "day", param.getStartDate(), param.getEndDate());
        writeDateStatsExcel(response, viewer, list, "일자별 매출", "일자별매출_");
    }

    /**
     * 일자별·월별이 같은 표를 쓰므로 엑셀 작성도 공유한다
     * (AS-IS도 DayStatisticsExcelView / MonthStatisticsExcelView 가 같은 컬럼이다).
     */
    private void writeDateStatsExcel(jakarta.servlet.http.HttpServletResponse response,
                                      com.ghlove.admin.domain.Manager viewer,
                                      java.util.List<OrderAdminClient.DateStatRow> list,
                                      String title, String fileNamePrefix) throws java.io.IOException {
        DayStatsTotal total = DayStatsTotal.of(list);

        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(title);
        com.ghlove.admin.web.support.ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);

        String[] headers = {"일자", "주문방법",
                "결제 건수", "결제 상품판매가", "결제 할인", "결제 판매액", "결제 배송비", "결제 소계",
                "취소 건수", "취소 상품판매가", "취소 할인", "취소 판매액", "취소 배송비", "취소 소계",
                "합계 판매액", "합계 배송비", "합계"};
        int[] widths = new int[headers.length];
        java.util.Arrays.fill(widths, 3500);
        widths[0] = 3000;
        widths[1] = 2500;
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, title, headers, widths);

        for (OrderAdminClient.DateStatRow r : list) {
            org.apache.poi.ss.usermodel.Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, formatDate(r.searchDate()));
            excelDownloadWriter.dataCell(xl, c++, styles, DEVICE_TYPE_UNKNOWN);
            for (long v : new long[]{r.saleCount(), r.itemPrice(), r.discountAmount(), r.saleAmount(), r.shipping(), r.payAmount(),
                    r.cancelCount(), r.cancelItemPrice(), r.cancelDiscountAmount(), r.cancelAmount(), r.cancelShipping(), r.cancelPayAmount(),
                    r.totalAmount(), r.totalShipping(), r.totalPayAmount()}) {
                excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", v));
            }
        }
        if (!list.isEmpty()) {
            org.apache.poi.ss.usermodel.Row xl = sheet.createRow(rowIndex);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, "합계");
            excelDownloadWriter.dataCell(xl, c++, styles, "");
            for (long v : new long[]{total.saleCount(), total.itemPrice(), total.discountAmount(), total.saleAmount(), total.shipping(), total.payAmount(),
                    total.cancelCount(), total.cancelItemPrice(), total.cancelDiscountAmount(), total.cancelAmount(), total.cancelShipping(), total.cancelPayAmount(),
                    total.totalAmount(), total.totalShipping(), total.totalPayAmount()}) {
                excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", v));
            }
        }

        String fileName = fileNamePrefix + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, viewer == null ? null : viewer.getLoginId());
    }

    /** AS-IS 화면은 DEVICE_TYPE 값을 그대로 찍는다. TO-BE 주문 표에 그 컬럼이 없어 비워 둔다. */
    static final String DEVICE_TYPE_UNKNOWN = "-";

    private static String formatDate(String ymd) {
        if (ymd == null || ymd.length() != 8) {
            return ymd == null ? "" : ymd;
        }
        return ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6);
    }

    private com.ghlove.admin.domain.Manager manager(jakarta.servlet.http.HttpSession session) {
        return (com.ghlove.admin.domain.Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS all.jsp 의 년도 select - YYYY 공통코드(give-statistics/all 과 같은 패턴 ·
     *  [[admin-give-statistics-year-select-source]] 의 함정 - 데이터 있는 연도만 뽑으면 안 된다). */
    private java.util.List<String> yearCodes() {
        java.util.List<String> years = new java.util.ArrayList<>(commonCodeService.labelsOf("YYYY").keySet());
        if (years.isEmpty()) {
            years.add(String.valueOf(java.time.LocalDate.now().getYear()));
        }
        return years;
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 기간 + 소계 노출. */
    public static class DayStatsParam {
        private static final java.time.format.DateTimeFormatter YYYYMM =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMM");
        private static final java.time.format.DateTimeFormatter YYYYMMDD =
                java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

        private String startDate;
        private String endDate;
        /** AS-IS 기본값은 'N'(비노출)이고 라디오로 바꾼다. */
        private String displaySubtotal = "N";

        /**
         * AS-IS {@code StatisticsParam.getStartDate()} - 비어 있으면 <b>이번 달 1일</b>
         * ({@code DateUtils.getToday("yyyyMM") + "01"}). 게터가 기본값을 주기 때문에
         * 화면 입력칸도 비지 않고, 조건 없이 검색해도 이번 달만 집계된다.
         */
        public String getStartDate() {
            if (startDate == null || startDate.isBlank()) {
                return java.time.LocalDate.now().format(YYYYMM) + "01";
            }
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        /** AS-IS {@code StatisticsParam.getEndDate()} - 비어 있으면 <b>오늘</b>(yyyyMMdd). */
        public String getEndDate() {
            if (endDate == null || endDate.isBlank()) {
                return java.time.LocalDate.now().format(YYYYMMDD);
            }
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getDisplaySubtotal() {
            return displaySubtotal;
        }

        public void setDisplaySubtotal(String displaySubtotal) {
            this.displaySubtotal = displaySubtotal;
        }

        /** 엑셀 링크에 붙일 조건. 게터를 쓰므로 비워 둔 경우에도 실제 조회한 기간이 그대로 넘어간다. */
        String toQueryString() {
            return "startDate=" + getStartDate() + "&endDate=" + getEndDate();
        }
    }

    /**
     * AS-IS StatisticsParam 중 월별 화면이 쓰는 것만 - 시작/종료 년·월 + 소계 노출.
     *
     * <p>AS-IS는 네 값 모두 <b>게터가 기본값을 만든다</b>(비었거나 자릿수가 틀리면 올해/이번 달).
     * 그래서 조건 없이 들어가도 select 가 비지 않고, 검색하면 이번 달만 집계된다.
     */
    public static class MonthStatsParam {
        private static final java.time.format.DateTimeFormatter YYYY =
                java.time.format.DateTimeFormatter.ofPattern("yyyy");
        private static final java.time.format.DateTimeFormatter MM =
                java.time.format.DateTimeFormatter.ofPattern("MM");

        private String startYear;
        private String startMonth;
        private String endYear;
        private String endMonth;
        private String displaySubtotal = "N";

        private static String yearOr(String v) {
            return (v == null || v.length() != 4) ? java.time.LocalDate.now().format(YYYY) : v;
        }

        private static String monthOr(String v) {
            return (v == null || v.length() != 2) ? java.time.LocalDate.now().format(MM) : v;
        }

        public String getStartYear() {
            return yearOr(startYear);
        }

        public void setStartYear(String startYear) {
            this.startYear = startYear;
        }

        public String getStartMonth() {
            return monthOr(startMonth);
        }

        public void setStartMonth(String startMonth) {
            this.startMonth = startMonth;
        }

        public String getEndYear() {
            return yearOr(endYear);
        }

        public void setEndYear(String endYear) {
            this.endYear = endYear;
        }

        public String getEndMonth() {
            return monthOr(endMonth);
        }

        public void setEndMonth(String endMonth) {
            this.endMonth = endMonth;
        }

        public String getDisplaySubtotal() {
            return displaySubtotal;
        }

        public void setDisplaySubtotal(String displaySubtotal) {
            this.displaySubtotal = displaySubtotal;
        }

        /** AS-IS getStartDate() 의 month 분기 - {@code startYear + startMonth + "01"}. */
        String toStartDate() {
            return getStartYear() + getStartMonth() + "01";
        }

        /** AS-IS getEndDate() 의 month 분기 - {@code endYear + endMonth + "31"}(말일 보정 없이 31을 쓴다). */
        String toEndDate() {
            return getEndYear() + getEndMonth() + "31";
        }

        String toQueryString() {
            return "startYear=" + getStartYear() + "&startMonth=" + getStartMonth()
                    + "&endYear=" + getEndYear() + "&endMonth=" + getEndMonth();
        }
    }

    /**
     * AS-IS StatisticsParam 중 년도별 화면이 쓰는 것만 - 시작/종료 년도 + 소계 노출.
     * 월 select 가 없고, 기간은 {@code startYear+"0101"} ~ {@code endYear+"1231"} 이 된다.
     */
    public static class YearStatsParam {
        private static final java.time.format.DateTimeFormatter YYYY =
                java.time.format.DateTimeFormatter.ofPattern("yyyy");

        private String startYear;
        private String endYear;
        private String displaySubtotal = "N";

        private static String yearOr(String v) {
            return (v == null || v.length() != 4) ? java.time.LocalDate.now().format(YYYY) : v;
        }

        public String getStartYear() {
            return yearOr(startYear);
        }

        public void setStartYear(String startYear) {
            this.startYear = startYear;
        }

        public String getEndYear() {
            return yearOr(endYear);
        }

        public void setEndYear(String endYear) {
            this.endYear = endYear;
        }

        public String getDisplaySubtotal() {
            return displaySubtotal;
        }

        public void setDisplaySubtotal(String displaySubtotal) {
            this.displaySubtotal = displaySubtotal;
        }

        /** AS-IS getStartDate() 의 year 분기. */
        String toStartDate() {
            return getStartYear() + "0101";
        }

        /** AS-IS getEndDate() 의 year 분기. */
        String toEndDate() {
            return getEndYear() + "1231";
        }

        String toQueryString() {
            return "startYear=" + getStartYear() + "&endYear=" + getEndYear();
        }
    }

    /**
     * AS-IS StatisticsParam 중 결제타입별 화면이 쓰는 것만 - 기간 + 결제타입 체크박스.
     * 기간 기본값은 일자별과 같다(이번 달 1일 ~ 오늘).
     */
    public static class PaymentStatsParam extends DayStatsParam {
        /** AS-IS form:checkboxes path="approvalTypes" - 비어 있으면 전체. */
        private java.util.List<String> approvalTypes = new java.util.ArrayList<>();

        public java.util.List<String> getApprovalTypes() {
            return approvalTypes;
        }

        public void setApprovalTypes(java.util.List<String> approvalTypes) {
            this.approvalTypes = approvalTypes == null ? new java.util.ArrayList<>() : approvalTypes;
        }

        /** 체크가 없으면 전체 조회(AS-IS 와 같다). 체크했다면 기부포인트가 들어 있어야 값이 나온다. */
        boolean includesGivePoint() {
            return approvalTypes.isEmpty() || approvalTypes.contains(PAY_TYPE_GIVE_POINT);
        }

        /** AS-IS 컨트롤러가 endDate 부터 startDate 까지 하루씩 거꾸로 담는 그 목록. */
        java.util.List<String> days() {
            java.util.List<String> days = new java.util.ArrayList<>();
            java.time.LocalDate start = parse(getStartDate());
            java.time.LocalDate end = parse(getEndDate());
            if (start == null || end == null || end.isBefore(start)) {
                return days;
            }
            for (java.time.LocalDate d = end; !d.isBefore(start); d = d.minusDays(1)) {
                days.add(d.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
            }
            return days;
        }

        private static java.time.LocalDate parse(String ymd) {
            try {
                return java.time.LocalDate.parse(ymd, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            } catch (RuntimeException e) {
                return null;
            }
        }

        @Override
        String toQueryString() {
            StringBuilder sb = new StringBuilder(super.toQueryString());
            for (String t : approvalTypes) {
                sb.append("&approvalTypes=").append(t);
            }
            return sb.toString();
        }
    }

    /** AS-IS {@code makeTotalForDate} - 행들을 그대로 합친 합계줄(취소가 음수라 순액이 된다). */
    public record DayStatsTotal(long saleCount, long itemPrice, long discountAmount, long saleAmount, long shipping,
                                 long cancelCount, long cancelItemPrice, long cancelDiscountAmount,
                                 long cancelAmount, long cancelShipping) {

        static DayStatsTotal of(java.util.List<OrderAdminClient.DateStatRow> list) {
            long a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0, j = 0;
            for (OrderAdminClient.DateStatRow r : list) {
                a += r.saleCount();
                b += r.itemPrice();
                c += r.discountAmount();
                d += r.saleAmount();
                e += r.shipping();
                f += r.cancelCount();
                g += r.cancelItemPrice();
                h += r.cancelDiscountAmount();
                i += r.cancelAmount();
                j += r.cancelShipping();
            }
            return new DayStatsTotal(a, b, c, d, e, f, g, h, i, j);
        }

        public long payAmount() {
            return saleAmount + shipping;
        }

        public long cancelPayAmount() {
            return cancelAmount + cancelShipping;
        }

        public long totalAmount() {
            return saleAmount + cancelAmount;
        }

        public long totalShipping() {
            return shipping + cancelShipping;
        }

        public long totalPayAmount() {
            return totalAmount() + totalShipping();
        }

        /** AS-IS 요약줄의 "총 주문건수" - 판매 + 취소(음수). */
        public long totalCount() {
            return saleCount + cancelCount;
        }
    }

    // ---- B. 답례품 구매현황 전체 (AS-IS shop-statistics/sales/all.jsp) ----

    /**
     * AS-IS salesAll - 년도(YYYY 공통코드) select 하나뿐. GET 에서도 카드·월별차트를
     * 바로 그린다(일자별/월별/년도별과 달리 GET 진입에서 조회를 한다 - AS-IS 컨트롤러가
     * GET/POST 양쪽에서 완전히 같은 로직을 돈다).
     *
     * <p>★AS-IS는 device_type 축이 없다(쿼리 자체에 DEVICE_TYPE 이 없음) - 그래서 카드가
     * 한 세트뿐이다(다른 매출통계 화면들과 달리 "주문방법 -" 같은 빈 축을 만들 필요가 없다).
     * ★AS-IS는 취소를 <b>양수</b>로 담고 "합계 = 판매 - 취소"다(다른 매출통계는 취소가 음수라
     * "합계 = 판매 + 취소") - TO-BE {@code DateStatRow} 는 음수로 오므로 화면에 절대값으로 뒤집는다.
     * ★배송비 개념이 없다(상품 결제금액만) - saleAmount 를 쓰고 payAmount(배송비 포함)는 안 쓴다.
     */
    @GetMapping("/shop-statistics/sales/all")
    public String all(@RequestParam(required = false) String year,
                       jakarta.servlet.http.HttpSession session, Model model) {
        return salesAll(year, session, model);
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/all")
    public String searchSalesAll(@RequestParam(required = false) String year,
                                  jakarta.servlet.http.HttpSession session, Model model) {
        return salesAll(year, session, model);
    }

    private String salesAll(String year, jakarta.servlet.http.HttpSession session, Model model) {
        String y = (year == null || year.isBlank()) ? String.valueOf(java.time.LocalDate.now().getYear()) : year;

        java.util.List<OrderAdminClient.DateStatRow> yearRows =
                orderAdminClient.dateStats(manager(session), "year", y + "0101", y + "1231");
        AllSalesCard card = AllSalesCard.of(yearRows.isEmpty() ? null : yearRows.get(0));

        model.addAttribute("years", yearCodes());
        model.addAttribute("year", y);
        model.addAttribute("card", card);
        return "shop-statistics/sales-all";
    }

    /** AS-IS salesAllMonth ajax - 월별 1~12 채워서 {isSuccess,data} 로 돌려준다. */
    @org.springframework.web.bind.annotation.PostMapping("/shop-statistics/sales/all/month")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> allMonth(@RequestParam(required = false) String year,
                                         jakarta.servlet.http.HttpSession session) {
        String y = (year == null || year.isBlank()) ? String.valueOf(java.time.LocalDate.now().getYear()) : year;
        java.util.List<OrderAdminClient.DateStatRow> rows =
                orderAdminClient.dateStats(manager(session), "month", y + "0101", y + "1231");
        java.util.Map<String, OrderAdminClient.DateStatRow> byMonth = new java.util.LinkedHashMap<>();
        for (OrderAdminClient.DateStatRow r : rows) {
            // searchDate 가 yyyyMM(6자리) - 월만 키로 쓴다
            byMonth.put(r.searchDate().substring(4, 6), r);
        }
        java.util.List<java.util.Map<String, Object>> data = new java.util.ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            String mm = String.format("%02d", m);
            OrderAdminClient.DateStatRow r = byMonth.get(mm);
            AllSalesCard c = AllSalesCard.of(r);
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("groupObject", y + mm);
            row.put("subSaleAmount", c.saleAmount());
            row.put("subCancelAmount", c.cancelAmount());
            row.put("subSaleCount", c.saleCount());
            row.put("subCancelCount", c.cancelCount());
            data.add(row);
        }
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("isSuccess", true);
        body.put("data", data);
        return body;
    }

    /**
     * AS-IS BaseStats(Ghlove 버전) 한 칸 - saleCount/saleAmount(주문) · cancelCount/cancelAmount
     * (취소/반품, 둘 다 양수) · sumCount/sumAmount(합계 = 주문 - 취소).
     */
    public record AllSalesCard(long saleCount, long saleAmount, long cancelCount, long cancelAmount,
                                long sumCount, long sumAmount) {
        static AllSalesCard of(OrderAdminClient.DateStatRow r) {
            if (r == null) {
                return new AllSalesCard(0, 0, 0, 0, 0, 0);
            }
            long saleCount = r.saleCount();
            long saleAmount = r.saleAmount();
            long cancelCount = Math.abs(r.cancelCount());
            long cancelAmount = Math.abs(r.cancelAmount());
            return new AllSalesCard(saleCount, saleAmount, cancelCount, cancelAmount,
                    saleCount - cancelCount, saleAmount - cancelAmount);
        }
    }

    // ---- C. 결제타입별 매출 통계 → 위 A-4 로 옮겼다(AS-IS payment.jsp 사양으로 재구현).
    //         옛 메서드(기간 2개만 받아 service.paymentStats 로 5칸 표를 그리던 것)는 제거했다.

    // ---- D. 회원별 매출 통계 ----

    @GetMapping("/shop-statistics/sales/user")
    public String user(Model model) {
        model.addAttribute("stats", service.userStats());
        return "shop-statistics/sales-user";
    }

    @GetMapping({"/shop-statistics/sales/user/user-order", "/shop-statistics/sales/user/order-detail"})
    public String userDetail(@RequestParam Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("orders", service.userOrders(userId));
        return "shop-statistics/sales-user-detail";
    }

    // ---- E. 판매자별 통계 ----

    @GetMapping("/shop-statistics/sales/seller")
    public String seller(Model model) {
        model.addAttribute("stats", service.sellerStats());
        return "shop-statistics/sales-seller";
    }

    // ---- F. 브랜드별 통계 ----

    @GetMapping("/shop-statistics/sales/brand")
    public String brand(Model model) {
        model.addAttribute("stats", service.brandStats());
        return "shop-statistics/sales-brand";
    }

    @GetMapping("/shop-statistics/sales/brand/brand-detail")
    public String brandDetail(@RequestParam(required = false) Integer brandId, Model model) {
        model.addAttribute("brandId", brandId);
        model.addAttribute("orders", service.brandOrders(brandId));
        return "shop-statistics/sales-brand-detail";
    }

    // ---- G. 상품별 판매 통계 ----

    @GetMapping("/shop-statistics/sales/item")
    public String item(Model model) {
        model.addAttribute("stats", service.itemStats());
        return "shop-statistics/sales-item";
    }

    @GetMapping("/shop-statistics/sales/item/day")
    public String itemDay(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, Model model) {
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stats", service.itemStatsByPeriod(startDate, endDate));
        return "shop-statistics/sales-item-day";
    }

    @GetMapping("/shop-statistics/sales/item/{itemId}")
    public String itemDetail(@PathVariable Long itemId, Model model) {
        model.addAttribute("itemId", itemId);
        model.addAttribute("orders", service.itemOrders(itemId));
        return "shop-statistics/sales-item-detail";
    }

    // ---- H. 카테고리별 통계 (AS-IS /sales/category는 미사용 코드주석 - /sales/categories로 통합) ----

    @GetMapping("/shop-statistics/sales/category")
    public String categoryLegacyRedirect() {
        return "redirect:/shop-statistics/sales/categories";
    }

    // ---- I. 지역별 통계 ----

    @GetMapping("/shop-statistics/sales/area")
    public String area(Model model) {
        model.addAttribute("stats", service.areaStats());
        return "shop-statistics/sales-area";
    }

    @GetMapping("/shop-statistics/sales/area/detail")
    public String areaDetail(@RequestParam String locgovCode, Model model) {
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("orders", service.areaOrders(locgovCode));
        return "shop-statistics/sales-area-detail";
    }

    // ---- J. 매출 제로 상품 내역 ----

    @GetMapping("/shop-statistics/sales/no-sales")
    public String noSales(Model model) {
        model.addAttribute("items", service.noSaleItems());
        return "shop-statistics/sales-no-sales";
    }

    // ---- K. 미이용자 목록 ----

    @GetMapping("/shop-statistics/no-user")
    public String noUser(Model model) {
        model.addAttribute("users", service.noUsers());
        return "shop-statistics/no-user";
    }

    // ---- L. 월별 지자체별 답례품 현황 → ShopStatisticsMonthLocgovController로 옮겼다(AS-IS
    //         7칸 카테고리 선정/등록 현황·WDR 2단 select·동률 TOP3·엑셀 전체 재구현, 2026-10-14).
    //         옛 메서드(연/월만 받아 지자체 TOP3 주문금액 순위만 보여주던 축소판)는 제거했다.

    // ---- M. 지자체별 구매현황 → ShopStatisticsLocgovController로 옮겼다(AS-IS mois/locgov 역할
    //         분기·WDR 2단 select·정렬·엑셀 전체 재구현, 2026-10-14). 옛 메서드(연도만 받아
    //         역할분기·정렬·엑셀 없이 locgovStats/locgovUserStats로 그리던 축소판)는 제거했다.


    // ---- O/P. 대시보드 ----

    @GetMapping("/shop-statistics/dashboard/day")
    public String dashboardDay(@RequestParam(required = false) String date, Model model) {
        model.addAttribute("dashboard", service.dashboardDay(date));
        return "shop-statistics/dashboard-day";
    }

    @GetMapping("/shop-statistics/dashboard/month")
    public String dashboardMonth(@RequestParam(required = false) String yearMonth, Model model) {
        model.addAttribute("dashboard", service.dashboardMonth(yearMonth));
        return "shop-statistics/dashboard-month";
    }

    // ---- R. 대시보드 텍스트 리포트 (AS-IS opmanager/report - ReportController, 행안부 일일/주간보고
    // 클립보드복사용 텍스트 생성. 이미 있는 대시보드 데이터를 정형 텍스트로만 포맷팅한다) ----

    @GetMapping("/shop-statistics/dashboard/day/report")
    @org.springframework.web.bind.annotation.ResponseBody
    public String dashboardDayReport(@RequestParam(required = false) String date) {
        return service.dashboardDayReportText(service.dashboardDay(date));
    }

    @GetMapping("/shop-statistics/dashboard/month/report")
    @org.springframework.web.bind.annotation.ResponseBody
    public String dashboardMonthReport(@RequestParam(required = false) String yearMonth) {
        return service.dashboardMonthReportText(service.dashboardMonth(yearMonth));
    }

    // ---- Q. 농협 사업자 답례품 현황 → ShopStatisticsNhItemSalesController로 옮겼다(AS-IS
    //         G_NH_STAT_DATA_TEMP 사업자번호 매칭·검색구분·페이징·엑셀 전체 재구현, 2026-10-14).
    //         옛 메서드(bankName에 "농협" 포함 휴리스틱)는 제거했다.
}

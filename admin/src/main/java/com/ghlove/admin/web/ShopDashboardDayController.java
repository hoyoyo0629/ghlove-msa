package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.DailyReportService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 통계 > 보고서 > 일일 현황보고 (AS-IS {@code ShopStatisticsManagerController#dayStat}/{@code SearchDayStat} +
 * {@code dashboard/day.jsp} + {@code ReportController} + {@code daily.report.js} + {@code WeekExcelView}).
 *
 * <p>이전 구현(TierB "O.")은 결제건수·결제포인트·취소건수·신규가입자를 보여주는 발명된 화면이었다. AS-IS 화면은 날짜
 * 조회 폼 + 표 3개(총괄현황/기부현황/답례품현황 - AS-IS도 값이 비어 있는 틀뿐이다) + 보고서 문안 도구
 * (행안부 일일보고·주간보고·복사)이고, 문안은 {@code /report/daily/..} JSON을 JS가 조립한다.
 */
@Controller
@RequiredArgsConstructor
public class ShopDashboardDayController {

    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final DailyReportService reportService;
    private final ExcelDownloadWriter excelDownloadWriter;

    @GetMapping("/shop-statistics/dashboard/day")
    public String dayStat(@ModelAttribute("statisticsParam") DayParam param, Model model) {
        return "shop-statistics/dashboard-day";
    }

    @PostMapping("/shop-statistics/dashboard/day")
    public String searchDayStat(@ModelAttribute("statisticsParam") DayParam param, Model model) {
        return "shop-statistics/dashboard-day";
    }

    /** AS-IS ReportController GET /daily/{searchDay} - 일일보고 JSON. */
    @GetMapping("/shop-statistics/dashboard/day/report/daily/{searchDay}")
    @ResponseBody
    public Map<String, Object> dailyReport(@PathVariable String searchDay) {
        return reportService.daily(searchDay);
    }

    /** AS-IS ReportController GET /daily/{startDay}/{endDay} - 주간보고 JSON. */
    @GetMapping("/shop-statistics/dashboard/day/report/daily/{startDay}/{endDay}")
    @ResponseBody
    public Map<String, Object> weekReport(@PathVariable String startDay, @PathVariable String endDay) {
        return reportService.week(startDay, endDay);
    }

    /**
     * AS-IS GET dashboard/day/list-excel - JS가 {@code Shop.downloadExcelOrder(url, startDate, false)}로 날짜를
     * <b>키 없이</b> 쿼리스트링으로 붙여 보낸다(?20261014). 그 문자열이 엑셀 제목·헤더에 그대로 찍힌다.
     * AS-IS 엑셀은 표 틀만 있는 일일보고 양식이고 값 칸은 "신청건수/판매액" 둘뿐(그것도 항상 0)이다.
     */
    @GetMapping("/shop-statistics/dashboard/day/list-excel")
    public void listExcel(HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws IOException {
        String raw = request.getQueryString();
        String param = raw == null ? "" : raw.replaceAll("[^0-9]", "");
        if (param.length() < 8) {
            param = LocalDate.now().withDayOfMonth(1).format(YMD);
        }
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        writeExcel(response, manager, param.substring(0, 8));
    }

    private void writeExcel(HttpServletResponse response, Manager manager, String param) throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        String title = "고향사랑기부제 기부 현황 일일보고(" + param + "기준)";
        Sheet sheet = wb.createSheet(title);

        CellStyle titleStyle = wb.createCellStyle();
        Font tf = wb.createFont();
        tf.setBold(true);
        tf.setFontHeightInPoints((short) 14);
        titleStyle.setFont(tf);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle header = wb.createCellStyle();
        Font hf = wb.createFont();
        hf.setBold(true);
        header.setFont(hf);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle data = wb.createCellStyle();
        data.setAlignment(HorizontalAlignment.CENTER);
        data.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle left = wb.createCellStyle();
        left.setAlignment(HorizontalAlignment.LEFT);
        left.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle right = wb.createCellStyle();
        right.setAlignment(HorizontalAlignment.RIGHT);
        right.setVerticalAlignment(VerticalAlignment.CENTER);

        int[] widths = {3500, 3500, 3500, 3500, 5000, 3500};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
        }

        Row t = sheet.createRow(0);
        t.setHeight((short) 800);
        put(t, 0, title, titleStyle);
        fillBlank(t, titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        row(sheet, 1, left, "□총괄 현황");
        headerRow(sheet, 2, header, "구분", "", param, "전일 대비", "전년 동안 대비(누적)", "비고");
        row(sheet, 3, data, "회원 수");
        row(sheet, 4, data, "기부", "기부건수");
        row(sheet, 5, data, "", "기부금액");
        row(sheet, 6, data, "답례품", "등록건수");
        Row r7 = row(sheet, 7, data, "", "신청건수");
        put(r7, 2, "0", right);
        Row r8 = row(sheet, 8, data, "", "판매액");
        put(r8, 2, "0", right);

        row(sheet, 9, left, "□기부현황");
        headerRow(sheet, 10, header, "구분", "2024", "", "2023", "", "비고");
        headerRow(sheet, 11, header, "", param, "누계", param, "누계", "");
        row(sheet, 12, data, "금액 계");
        row(sheet, 13, data, "온라인");
        row(sheet, 14, data, "오프라인");
        row(sheet, 15, data, "건수 계");
        row(sheet, 16, data, "온라인");
        row(sheet, 17, data, "오프라인");
        row(sheet, 18, left, "※누계는 해당연도 1.1. 부터 작성기준 날짜까지의 총합을 의미");
        // AS-IS 그대로 두 번째 블록 제목도 "□기부현황"이다(답례품 현황 표인데 복붙 흔적)
        row(sheet, 19, left, "□기부현황");
        headerRow(sheet, 20, header, "구분", "2024", "", "2023", "", "비고");
        headerRow(sheet, 21, header, "", param, "누계", param, "누계", "");
        row(sheet, 22, data, "등록 건수");
        row(sheet, 23, data, "신청 건수");
        row(sheet, 24, data, "총 판매액");

        int[][] merges = {{1, 1, 0, 5}, {2, 2, 0, 1}, {3, 3, 0, 1}, {4, 5, 0, 0}, {6, 8, 0, 0},
                {9, 9, 0, 5}, {10, 10, 1, 2}, {10, 10, 3, 4}, {10, 11, 0, 0}, {10, 11, 5, 5},
                {18, 18, 0, 5}, {19, 19, 0, 5}, {20, 20, 1, 2}, {20, 20, 3, 4}, {20, 21, 0, 0}, {20, 21, 5, 5}};
        for (int[] m : merges) {
            sheet.addMergedRegion(new CellRangeAddress(m[0], m[1], m[2], m[3]));
        }

        String fileName = "주별 통계_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, wb, fileName, manager == null ? null : manager.getLoginId());
    }

    private static Row row(Sheet sheet, int idx, CellStyle style, String... values) {
        Row row = sheet.createRow(idx);
        row.setHeight((short) 400);
        for (int i = 0; i < 6; i++) {
            put(row, i, i < values.length ? values[i] : "", style);
        }
        return row;
    }

    private static void headerRow(Sheet sheet, int idx, CellStyle style, String... values) {
        Row row = sheet.createRow(idx);
        for (int i = 0; i < values.length; i++) {
            put(row, i, values[i], style);
        }
    }

    private static void put(Row row, int col, String value, CellStyle style) {
        Cell c = row.getCell(col) != null ? row.getCell(col) : row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private static void fillBlank(Row row, CellStyle style) {
        for (int i = 1; i < 6; i++) {
            put(row, i, "", style);
        }
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 기간 시작(startDate)·종료(endDate) 기본값. */
    public static class DayParam {
        private String startDate;
        private String endDate;
        private String displaySubtotal = "N";

        public String getStartDate() {
            return (startDate == null || startDate.isBlank()) ? LocalDate.now().withDayOfMonth(1).format(YMD) : startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return (endDate == null || endDate.isBlank()) ? LocalDate.now().format(YMD) : endDate;
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
    }
}

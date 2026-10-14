package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ShopStatisticsMonthLocgovService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
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
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 월별 지자체별 답례품 현황
 * (AS-IS {@code ShopStatisticsManagerController#monthLocgovStat} + {@code sales/month-locgov.jsp}
 * + {@code getMonthLocgovItemStat} 매퍼 + {@code MonthLocgovStatExcel}).
 *
 * <p>이전 구현({@code ShopStatisticsTierBController} "L. 월별 지자체별 답례품 현황")은 연/월만
 * 받아 지자체 TOP3 주문금액 순위만 보여주는 축소판(7칸 카테고리 현황·WDR select·엑셀 전부 없음)
 * 이라 전면 교체했다(2026-10-14).
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsMonthLocgovController {

    private final ShopStatisticsMonthLocgovService service;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final ExcelDownloadWriter excelDownloadWriter;

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** AS-IS GET - 조회조건만 채우고 집계는 하지 않는다(다른 매출통계 화면들과 같은 패턴). */
    @GetMapping("/shop-statistics/sales/month-locgov")
    public String monthLocgov(@ModelAttribute("statisticsParam") MonthLocgovParam param, Model model) {
        formAttrs(model, param);
        model.addAttribute("result", emptyResult());
        return "shop-statistics/sales-month-locgov";
    }

    @PostMapping("/shop-statistics/sales/month-locgov")
    public String searchMonthLocgov(@ModelAttribute("statisticsParam") MonthLocgovParam param,
                                     HttpSession session, Model model) {
        formAttrs(model, param);
        model.addAttribute("result", service.stats(manager(session), param.getSearchYear(), param.getSearchMonth(),
                param.getUpperLocgovCode(), param.getLocgovCode()));
        return "shop-statistics/sales-month-locgov";
    }

    @GetMapping("/shop-statistics/sales/month-locgov/excel-download")
    public void excelDownload(@ModelAttribute("statisticsParam") MonthLocgovParam param,
                               HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        ShopStatisticsMonthLocgovService.Result result = service.stats(manager, param.getSearchYear(),
                param.getSearchMonth(), param.getUpperLocgovCode(), param.getLocgovCode());
        writeExcel(response, manager, result);
    }

    private void formAttrs(Model model, MonthLocgovParam param) {
        model.addAttribute("years", yearCodes());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
    }

    private static ShopStatisticsMonthLocgovService.Result emptyResult() {
        ShopStatisticsMonthLocgovService.CategoryCounts zero =
                new ShopStatisticsMonthLocgovService.CategoryCounts(0, 0, 0, 0, 0, 0, 0);
        List<ShopStatisticsMonthLocgovService.TopItem> blankTop3 = List.of(
                new ShopStatisticsMonthLocgovService.TopItem("", 0),
                new ShopStatisticsMonthLocgovService.TopItem("", 0),
                new ShopStatisticsMonthLocgovService.TopItem("", 0));
        ShopStatisticsMonthLocgovService.LocgovRow grandTotal =
                new ShopStatisticsMonthLocgovService.LocgovRow(null, "합계(전체)", "", zero, 0, 0, blankTop3);
        return new ShopStatisticsMonthLocgovService.Result(grandTotal, List.of());
    }

    // ---- 엑셀 (AS-IS MonthLocgovStatExcel - 타이틀행 + 2단 헤더 + 합계(전체) 1행 + 지자체별 1행씩) ----

    private void writeExcel(HttpServletResponse response, Manager manager,
                             ShopStatisticsMonthLocgovService.Result result) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("월별 지자체별 답례품 현황");

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setWrapText(true);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        String[] headers2 = {"지역", "지자체명", "합계", "관광서비스", "농축산물", "수산물", "가공식품", "생활용품", "지역상품권",
                "답례품 선택건수", "답례품 선택금액", "가장많이 선택한 답례품명1", "가장많이 선택한\n답례품 건수1",
                "가장많이 선택한 답례품명2", "가장많이 선택한\n답례품 건수2", "가장많이 선택한 답례품명3", "가장많이 선택한\n답례품 건수3"};
        int[] widths = {3000, 3000, 2500, 2500, 2500, 2500, 2500, 2500, 2500, 3500, 3500, 7000, 3000, 7000, 3000, 7000, 3000};

        Row titleRow = sheet.createRow(0);
        titleRow.setHeight((short) 800);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("월별 지자체별 답례품 현황");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers2.length - 1));

        Row group = sheet.createRow(1);
        group.setHeight((short) 512);
        Row sub = sheet.createRow(2);
        sub.setHeight((short) 512);
        for (int i = 0; i < headers2.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
            Cell g = group.createCell(i);
            g.setCellStyle(headerStyle);
            Cell s = sub.createCell(i);
            s.setCellValue(headers2[i]);
            s.setCellStyle(headerStyle);
        }
        group.getCell(0).setCellValue("지역");
        group.getCell(1).setCellValue("지자체명");
        group.getCell(2).setCellValue("지자체별 답례품 선정/등록 현황");
        group.getCell(9).setCellValue("지자체별 답례품 구매 현황");
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 8));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 9, 16));

        int rowIndex = 3;
        rowIndex = writeDataRow(sheet, rowIndex, dataStyle, "합계(전체)", "", result.grandTotal());
        for (ShopStatisticsMonthLocgovService.LocgovRow r : result.rows()) {
            rowIndex = writeDataRow(sheet, rowIndex, dataStyle, r.upperLocgovNm(), r.locgovNm(), r);
        }

        String fileName = "월별_지자체별_답례품_현황_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    private int writeDataRow(Sheet sheet, int rowIndex, CellStyle dataStyle, String region, String locgovNm,
                              ShopStatisticsMonthLocgovService.LocgovRow r) {
        Row row = sheet.createRow(rowIndex);
        row.setHeight((short) 400);
        ShopStatisticsMonthLocgovService.CategoryCounts c = r.categoryCounts();
        List<ShopStatisticsMonthLocgovService.TopItem> top3 = r.top3();
        Object[] values = {region, locgovNm,
                fmt(c.total()), fmt(c.tour()), fmt(c.agri()), fmt(c.seafood()), fmt(c.processed()), fmt(c.living()), fmt(c.voucher()),
                fmt(r.orderCount()), fmt(r.saleAmount()),
                top3.get(0).name(), fmt(top3.get(0).count()),
                top3.get(1).name(), fmt(top3.get(1).count()),
                top3.get(2).name(), fmt(top3.get(2).count())};
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(String.valueOf(values[i]));
            cell.setCellStyle(dataStyle);
        }
        return rowIndex + 1;
    }

    private static String fmt(long v) {
        return String.format("%,d", v);
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

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

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 지자체 2단 + 년/월. */
    public static class MonthLocgovParam {
        private String upperLocgovCode;
        private String locgovCode;
        private String searchYear;
        private String searchMonth;

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

        /** AS-IS StatisticsParam.searchYear 기본값 - 비었으면 올해. */
        public String getSearchYear() {
            return (searchYear == null || searchYear.isBlank())
                    ? String.valueOf(LocalDate.now().getYear()) : searchYear;
        }

        public void setSearchYear(String searchYear) {
            this.searchYear = searchYear;
        }

        /** AS-IS StatisticsParam.getSearchMonth() - 2자리로 0패딩(select는 1~12 숫자만 쓰지만
         *  서버가 "03"처럼 맞춰준다). 비었으면 이번 달. */
        public String getSearchMonth() {
            String m = (searchMonth == null || searchMonth.isBlank())
                    ? LocalDate.now().format(DateTimeFormatter.ofPattern("MM")) : searchMonth;
            m = "0" + m;
            return m.substring(m.length() - 2);
        }

        public void setSearchMonth(String searchMonth) {
            this.searchMonth = searchMonth;
        }
    }
}

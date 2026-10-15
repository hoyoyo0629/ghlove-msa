package com.ghlove.admin.web.support;

import org.apache.poi.ss.usermodel.BorderStyle;
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
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.ghlove.admin.service.ShopStatisticsReportGeneralService.numberFormat;

/**
 * AS-IS {@code saleson.shop.statistics.support.ShopStatisticsStoredPastYearExcelView} 재현 - 통계 > 보고서 > 연간통계 현황의
 * "엑셀" 버튼({@code report/lclgv/excel-pastYearStatsStored}). 한 지자체의 연간 집계를 시트 8장(총괄·기부방법별·기부금액별·
 * 기부연령별·기부자주소지 광역/전체·기부월별·답례품 인기순)으로 내려준다. 암호화 없음(AbstractSXSSFExcelView 계열).
 */
public class ShopStatisticsStoredPastYearExcelView {

    private final AsisSxssfExcelView view = new AsisSxssfExcelView();
    private final SXSSFWorkbook workbook = view.workbook();

    private CellStyle mergedTitle;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle bodyStyle;

    public AsisSxssfExcelView view() {
        return view;
    }

    public void build(List<Map<String, Object>> sum, List<Map<String, Object>> path, List<Map<String, Object>> price,
                      List<Map<String, Object>> ages, List<Map<String, Object>> psint, List<Map<String, Object>> locgov,
                      List<Map<String, Object>> month, List<Map<String, Object>> goods) {
        setStyleBase();
        sumSheet(sum, "기부현황");
        pathSheet(path, "기부현황");
        priceSheet(price, "기부현황");
        agesSheet(ages, "기부현황");
        psintSheet(psint, "기부현황");
        locgovSheet(locgov, "기부현황");
        monthSheet(month, "기부현황");
        goodsSheet(goods, "답례품현황");
    }

    // ---- 1. 총괄 ----

    private void sumSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"총괄", ""}, new int[]{700, 700}, "1. " + title);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        row = sheet.createRow(2);
        row.setHeight((short) 400);
        AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
        if (list.size() >= 1) {
            view.setText(sheet, 2, c, "건수");
            view.setTextRight(sheet, 2, c, numberFormat(String.valueOf(list.get(0).get("sum_tnocs"))));
        }
        row = sheet.createRow(3);
        row.setHeight((short) 400);
        AsisSxssfExcelView.CellIndex a = new AsisSxssfExcelView.CellIndex(-1);
        if (list.size() >= 1) {
            view.setText(sheet, 3, a, "금액");
            view.setTextRight(sheet, 3, a, numberFormat(String.valueOf(list.get(0).get("sum_amt"))));
        }
    }

    // ---- 2. 기부방법별 ----

    private void pathSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title + "_기부방법별");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        Row row = sheet.createRow(0);
        row.setHeight((short) 800);
        Cell titleCell = row.createCell(0);
        titleCell.setCellStyle(mergedTitle);
        titleCell.setCellValue("2. 기부방법별");

        Row row1 = sheet.createRow(1);
        row1.setHeight((short) 500);
        for (int i = 0; i < 6; i++) {
            row1.createCell(i).setCellStyle(headerStyle);
        }
        row1.getCell(0).setCellValue("구분");
        row1.getCell(2).setCellValue("기부금액");
        row1.getCell(3).setCellValue("기부건수");
        row1.getCell(4).setCellValue("기부금액(%)");
        row1.getCell(5).setCellValue("기부건수(%)");

        int rowIndex = 2;
        if (list.size() >= 1) {
            for (Map<String, Object> obj : list) {
                Row rowContent = sheet.createRow(rowIndex);
                rowContent.setHeight((short) 400);
                for (int i = 0; i < 6; i++) {
                    rowContent.createCell(i).setCellStyle(bodyStyle);
                }
                Object path = obj.get("dntn_path");
                if (rowIndex >= 3 && rowIndex <= (list.size() - 1)) {
                    rowContent.getCell(1).setCellValue(path == null ? "" : String.valueOf(path));
                    rowContent.getCell(0).setCellValue("민간플랫폼");
                } else {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
                    rowContent.getCell(0).setCellValue(path == null ? "" : String.valueOf(path));
                }
                rowContent.getCell(2).setCellValue(numberFormat(String.valueOf(obj.get("gramt"))));
                rowContent.getCell(3).setCellValue(numberFormat(String.valueOf(obj.get("tnocs"))));
                rowContent.getCell(4).setCellValue(obj.get("gramtrt") + "%");
                rowContent.getCell(5).setCellValue(obj.get("rt") + "%");
                rowIndex++;
            }
        }
        if (list.size() > 4) {
            sheet.addMergedRegion(new CellRangeAddress(3, (list.size() - 1), 0, 0));
        }
        autoSize(sheet, 700);
    }

    // ---- 3. 기부금액별 ----

    private void priceSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title + "_기부금액별");
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"구분", "건수", "금액", "(백만원환산)"}, new int[]{1600, 900, 900, 900}, "3. 기부금액별");
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            sheet.createRow(rowIndex).setHeight((short) 400);
            AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("gramt")));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("tnocs"))));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("atm"))));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("atmhund"))));
            rowIndex++;
        }
        autoSize(sheet, 700);
    }

    // ---- 4. 기부연령별 ----

    private void agesSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title + "_기부연령별");
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"구분", "건수", "비율", "금액"}, new int[]{1300, 900, 900, 900}, "4. 기부연령별");
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            sheet.createRow(rowIndex).setHeight((short) 400);
            AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("ages")));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("tnocs"))));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("rt")) + "%"));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("asum"))));
            rowIndex++;
        }
        autoSize(sheet, 700);
    }

    // ---- 5. 기부자 주소지별 광역 / 6. 전체 ----

    private void psintSheet(List<Map<String, Object>> list, String title) {
        addressSheet(list, title + "_기부자주소지_광역", "5. 기부자주소지별_광역");
    }

    private void locgovSheet(List<Map<String, Object>> list, String title) {
        addressSheet(list, title + "_기부자주소지_전체", "6. 기부자주소지별_전체");
    }

    private void addressSheet(List<Map<String, Object>> list, String sheetName, String heading) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"구분", "건수", "비율", "금액", "(백만원환산)", "비중"},
                new int[]{1300, 900, 900, 900, 900, 900}, heading);
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            sheet.createRow(rowIndex).setHeight((short) 400);
            AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("gramt")));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("tnocs"))));
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("tnocsrt")) + "%");
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("asum"))));
            view.setText(sheet, rowIndex, c, exchangHundFlot(obj.get("asum")));
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("asumrt")) + "%");
            rowIndex++;
        }
        autoSize(sheet, 700);
    }

    // ---- 7. 기부월별 ----

    private void monthSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title + "_기부월별");
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"구분", "건수", "비중", "금액", "비중", "고유아이디"},
                new int[]{900, 900, 900, 900, 900, 900}, "7. 기부월별");
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            sheet.createRow(rowIndex).setHeight((short) 400);
            AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("cate_month")));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("tnocs"))));
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("tnocs_rt")) + "%");
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("asum"))));
            view.setText(sheet, rowIndex, c, String.valueOf(obj.get("asum_rt")) + "%");
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("uniq_id"))));
            rowIndex++;
        }
        autoSize(sheet, 700);
    }

    // ---- 답례품 인기순 ----

    private void goodsSheet(List<Map<String, Object>> list, String title) {
        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, new String[]{"행레이블", "판매가", "합계:판매가", "개수:수량"},
                new int[]{900, 900, 900, 900}, "1. 답례품 인기순");
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            sheet.createRow(rowIndex).setHeight((short) 400);
            AsisSxssfExcelView.CellIndex c = new AsisSxssfExcelView.CellIndex(-1);
            view.setTextLeft(sheet, rowIndex, c, String.valueOf(obj.get("item_name")));
            if ("총합계".equals(String.valueOf(obj.get("item_name")))) {
                view.setText(sheet, rowIndex, c, "");
            } else {
                view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("price"))));
            }
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("sum_price"))));
            view.setText(sheet, rowIndex, c, numberFormat(String.valueOf(obj.get("sum_quantity"))));
            rowIndex++;
        }
        autoSize(sheet, 200);
    }

    /** AS-IS 끝에서 0~5열을 autoSize + 여유폭(시트마다 700 또는 200). */
    private static void autoSize(Sheet sheet, int extra) {
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + extra);
        }
    }

    /** AS-IS exchangHundFlot - 백만원 단위 반올림 후 콤마(백만원 미만은 소수 처리). */
    static String exchangHundFlot(Object oVal) {
        DecimalFormat formatter = new DecimalFormat("###,###");
        BigDecimal bd = new BigDecimal(Double.parseDouble(String.valueOf(oVal)));
        String bdSrt = bd.setScale(-6, RoundingMode.HALF_UP).toPlainString();
        int strLength = bdSrt.length();
        String tmpExHund = bdSrt;
        if (strLength > 6) {
            tmpExHund = bdSrt.substring(0, strLength - 6);
            tmpExHund = formatter.format(Double.parseDouble(tmpExHund));
        } else {
            tmpExHund = String.format("%.0f", Integer.parseInt(tmpExHund) * 0.000001);
        }
        return tmpExHund;
    }

    /** AS-IS setStyleBase - 제목(맑은 고딕 14 굵게)·헤더(연노랑 9pt 굵게 테두리)·본문(9pt 테두리). */
    private void setStyleBase() {
        Font titleFont = workbook.createFont();
        titleFont.setFontName("맑은 고딕");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);
        mergedTitle = workbook.createCellStyle();
        mergedTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        mergedTitle.setFont(titleFont);

        Font subFont = workbook.createFont();
        subFont.setFontName("맑은 고딕");
        subFont.setFontHeightInPoints((short) 9);
        subFont.setBold(true);
        subFont.setColor(IndexedColors.BLACK.getIndex());
        headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 153), null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        thinBorders(headerStyle);
        headerStyle.setFont(subFont);

        Font bodyFont = workbook.createFont();
        bodyFont.setFontName("맑은 고딕");
        bodyFont.setColor(IndexedColors.BLACK.getIndex());
        bodyFont.setFontHeightInPoints((short) 9);
        bodyFont.setBold(false);
        bodyStyle = (XSSFCellStyle) workbook.createCellStyle();
        bodyStyle.setAlignment(HorizontalAlignment.CENTER);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        thinBorders(bodyStyle);
        bodyStyle.setFont(bodyFont);
    }

    private static void thinBorders(XSSFCellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }
}

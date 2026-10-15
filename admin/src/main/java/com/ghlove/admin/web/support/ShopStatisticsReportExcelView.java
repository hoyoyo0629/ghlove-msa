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

import java.util.List;
import java.util.Map;

import static com.ghlove.admin.service.ShopStatisticsReportGeneralService.numberFormat;
import static com.ghlove.admin.service.ShopStatisticsReportGeneralService.numOf;
import static com.ghlove.admin.service.ShopStatisticsReportGeneralService.rateInExcel;
import static com.ghlove.admin.service.ShopStatisticsReportGeneralService.strOf;

/**
 * AS-IS {@code saleson.shop.statistics.support.ShopStatisticsReportExcelView} 재현 - 통계 > 보고서 > 총괄 현황의
 * "엑셀" 버튼(실제 요청은 {@code report/lclgv/excel-download}, 폼의 기준년·월을 그대로 싣는다).
 * 시트 구성은 운영자 롤에 따라 다르다: 지자체 담당자(ROLE_ADMIN_5/6)는 지자체 시트 3장, 그 외는 14장.
 *
 * <p>AS-IS 서브클래스의 buildExcelDocument와 같은 방식 - {@link AsisSxssfExcelView}에 시트를 만들고
 * createSheetHeader → 행마다 setText. 월 컬럼은 검색월(shMonth)까지만 나간다.
 */
public class ShopStatisticsReportExcelView {

    private static final String[] MON = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    private final AsisSxssfExcelView view = new AsisSxssfExcelView();
    private final SXSSFWorkbook workbook = view.workbook();
    private final int cellMonth;

    public ShopStatisticsReportExcelView(int shMonth) {
        this.cellMonth = shMonth;
    }

    public AsisSxssfExcelView view() {
        return view;
    }

    /** 지자체 담당자(ROLE_ADMIN_5/6) - 시트 3장. */
    public void buildForLocgov(List<Map<String, Object>> lclgvAod, List<Map<String, Object>> lclgvAodMaxAmt,
                                List<Map<String, Object>> lclgvGds) {
        lclgvSheet(lclgvAod, "1.지자체_기부금현황");
        lclgvSheet(lclgvAodMaxAmt, "2.지자체_기부금현황_2000만원");
        lclgvSheet(lclgvGds, "3.지자체_답례품현황");
    }

    /** 그 외 운영자 - 시트 14장. */
    public void buildForAll(List<Map<String, Object>> mbr, List<Map<String, Object>> dntn,
                             List<Map<String, Object>> dntnMaxAmt, List<Map<String, Object>> gds,
                             List<Map<String, Object>> mctpvDntn, List<Map<String, Object>> mctpvDntnMaxAmt,
                             List<Map<String, Object>> lclgvAod, List<Map<String, Object>> lclgvAodMaxAmt,
                             List<Map<String, Object>> mctpvGds, List<Map<String, Object>> lclgvGds,
                             List<Map<String, Object>> path, List<Map<String, Object>> amt,
                             List<Map<String, Object>> age, List<Map<String, Object>> hab) {
        mbrSheet(mbr, "1.총괄현황_회원수");
        mctpvSheet(dntn, "2.총괄현황_기부건수");
        mctpvSheet(dntnMaxAmt, "3.총괄현황_기부건수(2000만원)");
        mctpvSheet(gds, "4.총괄현황_답례품건수");
        mctpvMSheet(mctpvDntn, "5.지자체별_기부금_현황(누계)");
        mctpvMSheet(mctpvDntnMaxAmt, "6.지자체별_기부금_현황_2000만원(누계)");
        lclgvSheet(lclgvAod, "7.지자체_기부금현황");
        lclgvSheet(lclgvAodMaxAmt, "8.지자체_기부금현황_2000만원");
        mctpvMSheet(mctpvGds, "9.지자체별_답례품제공현황(누계)");
        lclgvSheet(lclgvGds, "10.지자체_답례품현황");
        pathSheet(path, "11.기부방법별_기부건수");
        amtSheet(amt, "12.기부금액별_기부건수");
        ageSheet(age, "13.기부연령별_기부건수");
        habSheet(hab, "14.거주지역->기부지역별_기부건수");
    }

    // ---- 1. 회원수 ----

    private void mbrSheet(List<Map<String, Object>> list, String title) {
        String[] titles = new String[cellMonth + 1];
        int[] widths = new int[cellMonth + 1];
        for (int i = 0; i < cellMonth; i++) {
            titles[i] = (i + 1) + "월";
            widths[i] = 700;
        }
        titles[cellMonth] = "총";
        widths[cellMonth] = 700;

        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title + " (단위: 명)");

        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            // AS-IS는 1월은 무조건, 2월부터는 검색월 초과면 생략
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("mbr_cnt_jan")))));
            for (int m = 2; m <= 12; m++) {
                if (cellMonth > m - 1) {
                    view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("mbr_cnt_" + MON[m - 1])))));
                }
            }
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("mbr_tnocs")))));
            rowIndex++;
        }
    }

    // ---- 2~4. 총괄(시/도 건수) ----

    private void mctpvSheet(List<Map<String, Object>> list, String title) {
        String[] titles = new String[cellMonth * 3 + 3];
        int[] widths = new int[cellMonth * 3 + 3];
        titles[0] = "시/도";
        titles[1] = "올해 총계";
        titles[2] = "전년도";
        monthTriples(titles, widths, 3);
        widths[0] = widths[1] = widths[2] = 700;

        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title + " (단위: 건, %)");

        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(strOf(obj.get("mctpv_nm")))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("now_yr")))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("prvyr")))));
            monthTripleCells(sheet, rowIndex, cellIndex, obj, 1);
            rowIndex++;
        }
    }

    // ---- 5·6·9. 지자체별 누계(백만원) ----

    private void mctpvMSheet(List<Map<String, Object>> list, String title) {
        String[] titles = new String[cellMonth * 3 + 1];
        int[] widths = new int[cellMonth * 3 + 1];
        titles[0] = "시/도";
        monthTriples(titles, widths, 1);
        widths[0] = 700;

        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title + " (단위: 백만원, %)");

        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(strOf(obj.get("mctpv_nm")))));
            monthTripleCells(sheet, rowIndex, cellIndex, obj, 1_000_000L);
            rowIndex++;
        }
    }

    // ---- 1~3·7·8·10. 지자체(천원) ----

    private void lclgvSheet(List<Map<String, Object>> list, String title) {
        String[] titles = new String[cellMonth * 3 + 4];
        int[] widths = new int[cellMonth * 3 + 4];
        titles[0] = "시/도";
        titles[1] = "지자체";
        titles[2] = "올해 총계";
        titles[3] = "전년도";
        monthTriples(titles, widths, 4);
        widths[0] = widths[1] = widths[2] = widths[3] = 700;

        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title + " (단위: 천원, %)");

        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(strOf(obj.get("mctpv_nm")))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(strOf(obj.get("lclgv_nm")))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(round(numOf(obj.get("now_yr")), 1000L))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(round(numOf(obj.get("prvyr")), 1000L))));
            monthTripleCells(sheet, rowIndex, cellIndex, obj, 1000L);
            rowIndex++;
        }
    }

    /** 헤더의 "N월 / 전년도N월 / 전년대비" 3칸 묶음(검색월 수만큼) - AS-IS 루프와 같은 위치·너비(700). */
    private void monthTriples(String[] titles, int[] widths, int start) {
        int j = 1;
        for (int i = start; i < cellMonth * 3 + start; i += 3) {
            titles[i] = j + "월";
            titles[i + 1] = "전년도" + j + "월";
            titles[i + 2] = "전년대비";
            widths[i] = widths[i + 1] = widths[i + 2] = 700;
            j++;
        }
    }

    /**
     * 월 3칸 묶음 값 - 1월은 무조건, 2월부터는 검색월 초과면 생략(AS-IS 그대로).
     * @param unit 1이면 원값, 1,000이면 천원(반올림), 1,000,000이면 백만원(반올림) - StatisticsReport의 getNowYrKJan/getNowYrMJan.
     */
    private void monthTripleCells(Sheet sheet, int rowIndex, AsisSxssfExcelView.CellIndex cellIndex,
                                  Map<String, Object> obj, long unit) {
        for (int m = 1; m <= 12; m++) {
            if (m > 1 && cellMonth <= m - 1) {
                break;
            }
            String s = MON[m - 1];
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(round(numOf(obj.get("now_yr_" + s)), unit))));
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(round(numOf(obj.get("prvyr_" + s)), unit))));
            view.setText(sheet, rowIndex, cellIndex, rateInExcel(strOf(obj.get("prvyr_now_yr_rt_" + s))));
        }
    }

    /** (int) Math.round((double) value / unit) - StatisticsReport의 K/M 환산 getter. */
    private static long round(long value, long unit) {
        if (unit == 1L) {
            return value;
        }
        return (int) Math.round((double) value / unit);
    }

    // ---- 11. 기부방법별 ----

    private void pathSheet(List<Map<String, Object>> list, String title) {
        Font titleFont = workbook.createFont();
        titleFont.setFontName("맑은 고딕");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);
        CellStyle mergedTitle = workbook.createCellStyle();
        mergedTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        mergedTitle.setFont(titleFont);

        Sheet sheet = workbook.createSheet(title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        Row row = sheet.createRow(0);
        row.setHeight((short) 800);
        Cell titleCell = row.createCell(0);
        titleCell.setCellStyle(mergedTitle);
        titleCell.setCellValue(title);

        Row row1 = sheet.createRow(1);
        row1.setHeight((short) 600);
        Font subFont = workbook.createFont();
        subFont.setFontName("맑은 고딕");
        subFont.setFontHeightInPoints((short) 10);
        subFont.setBold(true);
        subFont.setColor(IndexedColors.BLACK.getIndex());
        XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 153), null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        thinBorders(headerStyle);
        headerStyle.setFont(subFont);
        for (int i = 0; i < 6; i++) {
            row1.createCell(i).setCellStyle(headerStyle);
        }
        row1.getCell(0).setCellValue("구분");
        row1.getCell(2).setCellValue("기부금액");
        row1.getCell(3).setCellValue("기부건수");
        row1.getCell(4).setCellValue("기부금액(%)");
        row1.getCell(5).setCellValue("기부건수(%)");

        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            XSSFCellStyle bodyStyle = (XSSFCellStyle) workbook.createCellStyle();
            bodyStyle.setAlignment(HorizontalAlignment.CENTER);
            bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            thinBorders(bodyStyle);
            Font bodyFont = workbook.createFont();
            bodyFont.setFontName("맑은 고딕");
            bodyFont.setColor(IndexedColors.BLACK.getIndex());
            bodyFont.setFontHeightInPoints((short) 10);
            bodyFont.setBold(false);
            bodyStyle.setFont(bodyFont);

            Row rowContent = sheet.createRow(rowIndex);
            rowContent.setHeight((short) 400);
            for (int i = 0; i < 6; i++) {
                rowContent.createCell(i).setCellStyle(bodyStyle);
            }
            String path = strOf(obj.get("dntn_path"));
            if (rowIndex >= 3 && rowIndex <= (list.size() - 1)) {
                rowContent.getCell(1).setCellValue(path == null || path.isEmpty() ? "" : path);
                rowContent.getCell(0).setCellValue("민간플랫폼");
            } else {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
                rowContent.getCell(0).setCellValue(path == null || path.isEmpty() ? "" : path);
            }
            rowContent.getCell(2).setCellValue(numberFormat(String.valueOf(numOf(obj.get("gramt")))));
            rowContent.getCell(3).setCellValue(numberFormat(String.valueOf(numOf(obj.get("tnocs")))));
            rowContent.getCell(4).setCellValue(strOf(obj.get("gramtrt")) + "%");
            rowContent.getCell(5).setCellValue(strOf(obj.get("rt")) + "%");
            rowIndex++;
        }
        if (list.size() > 3) {
            sheet.addMergedRegion(new CellRangeAddress(3, (list.size() - 1), 0, 0));
        }
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 700);
        }
    }

    private static void thinBorders(XSSFCellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    // ---- 12. 기부금액별 ----

    private void amtSheet(List<Map<String, Object>> list, String title) {
        String[] titles = {"구분", "합계", "10만원 미만", "10만원", "10만원~100만원 미만", "100만원~500만원 미만",
                "500만원", "500~2000만원 미만", "2000만원"};
        int[] widths = {700, 700, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title);

        AsisSxssfExcelView.CellIndex[] bands = bandRows(sheet, list, "dntn_amt_cd", "00");
        if (list.size() < 8) {
            // AS-IS: 구간이 덜 적재됐으면 끝 두 칸을 "-"로 채운다(건수행·비율행 둘 다)
            AsisSxssfExcelView.CellIndex c1 = bands[0];
            view.setText(sheet, 2, c1, "-");
            view.setText(sheet, 2, c1, "-");
            AsisSxssfExcelView.CellIndex c2 = bands[1];
            view.setText(sheet, 3, c2, "-");
            view.setText(sheet, 3, c2, "-");
        }
    }

    // ---- 13. 기부연령별 ----

    private void ageSheet(List<Map<String, Object>> list, String title) {
        String[] titles = {"구분", "합계", "10대", "20대", "30대", "40대", "50대", "60대", "70대", "80대 이상", "생년월일 없음"};
        int[] widths = new int[11];
        java.util.Arrays.fill(widths, 700);
        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title);

        bandRows(sheet, list, "dntn_age_cd", "00");
    }

    /** 건수행(2행)·비율행(3행) - 첫 코드("00")에는 "건수"/"비율" 라벨을 먼저 찍고, 이어서 목록 순서대로 값. */
    private AsisSxssfExcelView.CellIndex[] bandRows(Sheet sheet, List<Map<String, Object>> list, String codeKey, String firstCode) {
        int rowIndex = 2;
        Row row = sheet.createRow(rowIndex);
        row.setHeight((short) 400);
        AsisSxssfExcelView.CellIndex cellIndex1 = new AsisSxssfExcelView.CellIndex(-1);
        for (Map<String, Object> obj : list) {
            if (firstCode.equals(strOf(obj.get(codeKey)))) {
                view.setText(sheet, rowIndex, cellIndex1, "건수");
            }
            view.setText(sheet, rowIndex, cellIndex1, numberFormat(String.valueOf(numOf(obj.get("tnocs")))));
        }

        rowIndex = 3;
        row = sheet.createRow(rowIndex);
        row.setHeight((short) 400);
        AsisSxssfExcelView.CellIndex cellIndex2 = new AsisSxssfExcelView.CellIndex(-1);
        for (Map<String, Object> obj : list) {
            if (firstCode.equals(strOf(obj.get(codeKey)))) {
                view.setText(sheet, rowIndex, cellIndex2, "비율");
            }
            view.setText(sheet, rowIndex, cellIndex2, strOf(obj.get("rt")) + "%");
        }
        return new AsisSxssfExcelView.CellIndex[]{cellIndex1, cellIndex2};
    }

    // ---- 14. 거주지역 -> 기부지역 ----

    private void habSheet(List<Map<String, Object>> list, String title) {
        String[] titles = {"거주지역>기부지역", "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시",
                "울산광역시", "세종특별자치시", "경기도", "충청북도", "충청남도", "전라남도", "경상북도", "경상남도",
                "제주특별자치도", "강원특별자치도", "전북특별자치도"};
        int[] widths = new int[18];
        java.util.Arrays.fill(widths, 1100);
        Sheet sheet = workbook.createSheet(title);
        Row row = sheet.createRow(0);
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        view.createSheetHeader(sheet, row, titles, widths, title);

        String[] cols = {"seoul", "busan", "daegu", "incheon", "gwangju", "daejeon", "ulsan", "sejong", "gyeonggi",
                "chungbuk", "chungnam", "jeonnam", "gyeongbuk", "gyeongnam", "jeju", "gangwon", "jeonbuk"};
        int rowIndex = 2;
        for (Map<String, Object> obj : list) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(strOf(obj.get("hab_mctpv_nm")))));
            for (String c : cols) {
                view.setText(sheet, rowIndex, cellIndex, numberFormat(String.valueOf(numOf(obj.get("dntn_cnt_" + c)))));
            }
            rowIndex++;
        }
    }
}

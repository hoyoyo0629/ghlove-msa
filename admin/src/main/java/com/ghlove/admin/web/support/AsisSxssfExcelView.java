package com.ghlove.admin.web.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * AS-IS opframework {@code AbstractSXSSFExcelView}(+ {@code OpAbstractView.responseHeader})를
 * libs/opframework-3.15.0.jar 바이트코드 그대로 옮긴 것. {@link ExcelDownloadWriter}("새 패턴",
 * ExcelCellStyleUtils + 암호화)와는 <b>다른 계열</b>이다 - 이 계열은 암호화하지 않고 바로 내려주며
 * 헤더가 연노랑(LIGHT_YELLOW)·전체 셀 얇은 테두리·맑은 고딕이다.
 *
 * <p>사용법(AS-IS 서브클래스의 buildExcelDocument와 같은 순서): {@link #workbook()}에 시트를 만들고
 * {@link #createSheetHeader} → 행마다 {@code sheet.createRow(i)} + {@link #setText}류 → {@link #write}.
 */
public class AsisSxssfExcelView {

    private static final String FONT_NAME = "맑은 고딕";

    private final SXSSFWorkbook workbook = new SXSSFWorkbook(100);
    private final CellStyle titleStyle;
    private final CellStyle headerStyle;
    private final CellStyle alignCenterStyle;
    private final CellStyle alignLeftStyle;
    private final CellStyle alignRightStyle;

    public AsisSxssfExcelView() {
        titleStyle = titleCellStyle();
        headerStyle = headerCellStyle(IndexedColors.LIGHT_YELLOW.getIndex());
        alignCenterStyle = defaultCellStyle();
        alignLeftStyle = defaultCellStyle();
        alignLeftStyle.setAlignment(HorizontalAlignment.LEFT);
        alignRightStyle = defaultCellStyle();
        alignRightStyle.setAlignment(HorizontalAlignment.RIGHT);
    }

    public SXSSFWorkbook workbook() {
        return workbook;
    }

    /** AS-IS CellIndex - setText류가 먼저 증가시킨 뒤 그 칸에 쓴다(시작값 -1). */
    public static final class CellIndex {
        private int index;

        public CellIndex(int index) {
            this.index = index;
        }

        void increase() {
            index++;
        }

        int get() {
            return index;
        }
    }

    /**
     * AS-IS createSheetHeader(Sheet, Row, HeaderCell[], String): 0행 높이 800·제목(전체 병합),
     * 1행 높이 512·헤더. 열 너비는 빈 열을 autoSize한 너비 + HeaderCell 너비.
     */
    public void createSheetHeader(Sheet sheet, Row titleRow, String[] titles, int[] widths, String title) {
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        titleRow.setHeight((short) 800);
        int count = titles.length;
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + widths[i]);
        }
        for (int i = 0; i < count; i++) {
            titleRow.createCell(i).setCellStyle(titleStyle);
        }
        titleRow.getCell(0).setCellValue(title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, count - 1));

        Row headerRow = sheet.createRow(1);
        headerRow.setHeight((short) 512);
        for (int i = 0; i < count; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(titles[i]);
        }
    }

    public void setText(Sheet sheet, int rowIndex, CellIndex cellIndex, String value) {
        put(sheet, rowIndex, cellIndex, value, alignCenterStyle);
    }

    public void setTextLeft(Sheet sheet, int rowIndex, CellIndex cellIndex, String value) {
        put(sheet, rowIndex, cellIndex, value, alignLeftStyle);
    }

    public void setTextRight(Sheet sheet, int rowIndex, CellIndex cellIndex, String value) {
        put(sheet, rowIndex, cellIndex, value, alignRightStyle);
    }

    private void put(Sheet sheet, int rowIndex, CellIndex cellIndex, String value, CellStyle style) {
        cellIndex.increase();
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(cellIndex.get());
        if (cell == null) {
            cell = row.createCell(cellIndex.get());
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    /**
     * AS-IS renderMergedOutputModel + responseHeader: 암호화 없이 그대로 쓴다. 파일명 인코딩은
     * AS-IS getDisposition의 브라우저 분기(MSIE/Firefox/Opera/Chrome) 그대로.
     */
    public void write(HttpServletRequest request, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        // Spring AbstractView가 다운로드 뷰(generatesDownloadContent=true)에 붙이던 헤더
        response.setHeader("Pragma", "private");
        response.setHeader("Cache-Control", "private, must-revalidate");
        try {
            response.setHeader("Content-Disposition", disposition(fileName, browser(request)));
        } catch (UnsupportedEncodingException | RuntimeException e) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        }
        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
            os.flush();
        } finally {
            workbook.dispose();
        }
    }

    private static String browser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Firefox";
        }
        if (userAgent.contains("MSIE")) {
            return "MSIE";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Opera")) {
            return "Opera";
        }
        return "Firefox";
    }

    private static String disposition(String fileName, String browser) throws UnsupportedEncodingException {
        String encoded;
        switch (browser) {
            case "MSIE" -> encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            case "Firefox", "Opera" -> encoded = "\"" + new String(fileName.getBytes(StandardCharsets.UTF_8),
                    "8859_1") + "\"";
            case "Chrome" -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fileName.length(); i++) {
                    char c = fileName.charAt(i);
                    if (c > '~') {
                        sb.append(URLEncoder.encode(String.valueOf(c), StandardCharsets.UTF_8));
                    } else {
                        sb.append(c);
                    }
                }
                encoded = sb.toString();
            }
            default -> throw new RuntimeException("Not supported browser");
        }
        return "attachment;filename=" + encoded;
    }

    private CellStyle titleCellStyle() {
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        return style;
    }

    private CellStyle headerCellStyle(short foregroundColor) {
        CellStyle style = workbook.createCellStyle();
        thinBlackBorders(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
        style.setFillForegroundColor(foregroundColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle defaultCellStyle() {
        CellStyle style = workbook.createCellStyle();
        thinBlackBorders(style);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 10);
        font.setBold(false);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("text"));
        return style;
    }

    private static void thinBlackBorders(CellStyle style) {
        short black = IndexedColors.BLACK.getIndex();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(black);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(black);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(black);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(black);
    }
}

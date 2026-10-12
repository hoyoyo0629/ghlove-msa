package com.ghlove.admin.web.support;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 진짜 엑셀(.xlsx) 다운로드 - AS-IS {@code saleson.common.file.ExcelDownloadView} 재현.
 *
 * <p>AS-IS는 다운로드 사유 모달(fragments/privacy-access.html)에 적혀 있는 대로
 * <b>엑셀 파일을 암호화하고, 비밀번호는 다운받는 운영자 본인의 로그인ID</b>다(변환 없이 그대로).
 * Apache POI의 Office 2010+ 표준 방식(agile)으로 .xlsx(OOXML) 패키지 자체를 암호화한다 -
 * 비밀번호를 몰라도 파일은 내려받지만 열리지 않는다.
 *
 * <p>TO-BE가 지금까지 여러 화면에서 CSV로 대체했던 걸 이걸로 교체한다(2026-10-08) - 각 컨트롤러는
 * AS-IS {@code *ExcelView}와 같은 제목·헤더·컬럼으로 {@link SXSSFWorkbook}을 만들어 이 메서드만
 * 호출하면 된다(워크북 작성 자체는 화면마다 다르므로 그대로 둔다).
 */
@Component
public class ExcelDownloadWriter {

    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * AS-IS {@code saleson.common.file.ExcelCellStyleUtils} verbatim 재현 - title(굵게 14pt,
     * 수직중앙)/header(굵게+회색25% 배경+수평수직중앙)/data(수평수직중앙) 세 스타일. 운영유지관리·
     * 오프라인기부접수·오프라인담당자 등 "새 패턴"(AbstractSXSSFExcelView가 아니라 서비스가 직접
     * SXSSFWorkbook을 만드는 화면) 전부가 이 유틸 하나를 공유해서 썼다 - 그래서 TO-BE도 하나로
     * 공유한다.
     */
    public CellStyles createStyles(Workbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return new CellStyles(titleStyle, headerStyle, dataStyle);
    }

    public record CellStyles(CellStyle title, CellStyle header, CellStyle data) {
    }

    /**
     * 제목행(0번, 전체 컬럼 병합, 높이 800)·헤더행(1번, 높이 512)·컬럼폭을 만들고 데이터를 쓸
     * 다음 행 번호(2)를 돌려준다. 행 높이·병합 방식까지 AS-IS {@code streamChargerData} 등과 동일.
     */
    public int writeTitleAndHeaderRow(Sheet sheet, CellStyles styles, String title, String[] headers, int[] widths) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeight((short) 800);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(styles.title());

        Row headerRow = sheet.createRow(1);
        headerRow.setHeight((short) 512);
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header());
        }
        if (headers.length > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        }
        return 2;
    }

    /** data() 스타일(수평수직중앙)이 적용된 셀 하나 - AS-IS {@code ExcelCellStyleUtils.data()}. */
    public Cell dataCell(Row row, int col, CellStyles styles, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(styles.data());
        return cell;
    }

    /**
     * @param workbook 호출부가 이미 시트·행·셀을 다 채운 워크북(이 메서드가 write 후 dispose한다)
     * @param fileName 다운로드 파일명(확장자 .xlsx 포함)
     * @param password 암호화 비밀번호 - AS-IS와 동일하게 호출부가 로그인한 관리자의 loginId를 넘긴다
     */
    public void write(HttpServletResponse response, SXSSFWorkbook workbook, String fileName, String password)
            throws IOException {
        File plainFile = null;
        File encryptedFile = null;
        try {
            plainFile = File.createTempFile("excel_plain_", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(plainFile)) {
                workbook.write(fos);
            } finally {
                workbook.dispose();
            }

            encryptedFile = encrypt(plainFile, password);
            stream(encryptedFile, response, fileName);
        } finally {
            if (plainFile != null) {
                plainFile.delete();
            }
            if (encryptedFile != null) {
                encryptedFile.delete();
            }
        }
    }

    private File encrypt(File plainFile, String password) throws IOException {
        File encryptedFile = File.createTempFile("excel_encrypted_", ".xlsx");
        try {
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);

            try (OPCPackage opc = OPCPackage.open(plainFile, PackageAccess.READ_WRITE);
                 OutputStream encOut = encryptor.getDataStream(fs)) {
                opc.save(encOut);
            }
            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fs.writeFilesystem(fos);
                fs.close();
            }
            return encryptedFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("엑셀 암호화에 실패했습니다.", e);
        }
    }

    private void stream(File encryptedFile, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType(CONTENT_TYPE);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (InputStream in = new BufferedInputStream(new FileInputStream(encryptedFile));
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }
}

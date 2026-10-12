package com.ghlove.admin.web.support;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 진단용 - "암호를 넣어도 계속 틀리다고 나온다"는 사용자 보고를 재현/검증한다.
 * ExcelDownloadWriter가 실제로 올바른 비밀번호로 열리는 파일을 만드는지 POI로 직접 왕복 확인.
 */
class ExcelDownloadWriterPasswordTest {

    @Test
    void encryptedFileOpensWithCorrectPasswordButNotWithWrongOne() throws Exception {
        ExcelDownloadWriter writer = new ExcelDownloadWriter();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.createSheet("test").createRow(0).createCell(0).setCellValue("hello");

        File encrypted = encryptToFile(writer, workbook, "admin01");

        // 올바른 비밀번호로는 열려야 한다
        try (InputStream in = new java.io.FileInputStream(encrypted)) {
            Workbook opened = WorkbookFactory.create(in, "admin01");
            assertThat(opened.getSheet("test")).isNotNull();
            opened.close();
        }

        // 틀린 비밀번호로는 못 열려야 한다(대조군)
        assertThrows(Exception.class, () -> {
            try (InputStream in = new java.io.FileInputStream(encrypted)) {
                WorkbookFactory.create(in, "wrong-password").close();
            }
        });
    }

    /** ExcelDownloadWriter.write()와 동일한 암호화 로직을 거쳐 평문이 아닌 암호화된 파일 자체를 돌려준다
     *  (write()는 바로 HttpServletResponse로 스트리밍해버려서 테스트에서 재사용하기 어려워 복제). */
    private File encryptToFile(ExcelDownloadWriter writer, SXSSFWorkbook workbook, String password) throws Exception {
        File plain = File.createTempFile("test_plain_", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(plain)) {
            workbook.write(fos);
        }
        workbook.dispose();

        java.lang.reflect.Method encrypt = ExcelDownloadWriter.class.getDeclaredMethod(
                "encrypt", File.class, String.class);
        encrypt.setAccessible(true);
        return (File) encrypt.invoke(writer, plain, password);
    }
}

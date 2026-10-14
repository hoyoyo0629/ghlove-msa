package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** 자료실 첨부파일을 로컬 디스크에 저장한다 (QnaFileStorageService와 동일 패턴).
 * DataBoardController#download가 uploadDir/data-board/{fileName}에서 읽으므로
 * 저장 위치를 반드시 맞춘다. */
@Service
@Slf4j
public class DataBoardFileStorageService {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** AS-IS DataboardServiceImpl의 AVAILABLE_EXTENSION <b>16종</b> 그대로 - 운영유지관리·제공자
     *  공지(18종)와 달리 hwpx·7z가 없다(매뉴얼과 같은 목록이다). */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "zip");

    private static final long MAX_SIZE = 50L * 1024 * 1024; // AS-IS 50MB

    /** AS-IS {@code Const.DATENANO_FORMAT} - 초 뒤 4자리까지 붙인다. */
    private static final java.time.format.DateTimeFormatter DATENANO =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS");

    /**
     * @return 저장된 파일명. AS-IS와 같은 {@code yyyyMMddHHmmssSSSS_원본명}이다
     *         (예전엔 UUID였는데 AS-IS 규칙으로 되돌렸다 - 운영에서 디스크 파일만 봐도
     *         언제 올린 무슨 파일인지 알 수 있어야 한다).
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        // AS-IS 순서: 확장자 검사 → 용량 검사. 문구도 AS-IS verbatim.
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("유효하지 않은 파일입니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("업로드 가능한 최대 용량 : 50MB 입니다");
        }
        // 경로조작 방지 - 원본명에서 디렉터리 구분자를 떼고 파일명만 쓴다(AS-IS엔 없는 보강).
        String baseName = original == null ? "" : java.nio.file.Paths.get(original).getFileName().toString();
        String storedName = java.time.LocalDateTime.now().format(DATENANO) + "_" + baseName;

        try {
            Path dir = Paths.get(uploadDir, "data-board");
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store data-board file", e);
            throw new IllegalStateException("파일 저장에 실패했습니다.");
        }
        return storedName;
    }

    public void delete(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir, "data-board", fileName));
        } catch (IOException e) {
            log.warn("Failed to delete data-board file {}", fileName, e);
        }
    }

    /** 운영자 화면의 첨부 다운로드가 읽을 실제 경로. */
    public Path resolve(String fileName) {
        return Paths.get(uploadDir, "data-board", fileName);
    }
}

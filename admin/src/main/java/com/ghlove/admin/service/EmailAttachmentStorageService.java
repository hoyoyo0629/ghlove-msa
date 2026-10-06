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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 이메일 발송 첨부파일 저장 (MainBannerImageStorageService와 같은 패턴).
 *
 * AS-IS {@code EmailServiceImpl}은 {@code locgovService.saveFile(files, email.getUploadPath(),
 * AVAILABLE_EXTENSION, 10, false)}로 저장하고 경로는 {@code <업로드루트>/email}이다 -
 * 확장자 12종과 10MB 제한도 AS-IS 상수를 그대로 옮겼다(화면 JS의 validationFile과 동일).
 */
@Service
@Slf4j
public class EmailAttachmentStorageService {

    /** AS-IS EmailServiceImpl.AVAILABLE_EXTENSION 그대로. */
    private static final List<String> AVAILABLE_EXTENSION = List.of(
            "jpg", "jpeg", "gif", "bmp", "png", "hwp", "doc", "docx", "pdf", "zip", "ppt", "pptx");

    /** AS-IS saveFile(..., 10, ...) - 10MB. */
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    /** AS-IS Email.getUploadPath() - <업로드루트>/email. */
    private static final String SUB_DIR = "email";

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** @return 디스크에 저장한 파일명(OP_EMAIL_FILE.FILE_NAME에 들어가는 값). 파일이 없으면 null. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ContentException("이미지 파일은 개당 10MB 이하로 등록 가능합니다.");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (!AVAILABLE_EXTENSION.contains(ext)) {
            throw new ContentException("jpg|jpeg|png|gif|bmp|hwp|doc|docx|pdf|zip|ppt|pptx 파일만 등록 가능합니다.");
        }
        String storedName = UUID.randomUUID() + "." + ext;
        try {
            Path dir = Paths.get(uploadDir, SUB_DIR);
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store email attachment", e);
            throw new ContentException("첨부파일 저장에 실패했습니다.");
        }
        return storedName;
    }

    /** AS-IS FileUtils.getExtension - 확장자(소문자). */
    public String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /** 저장된 첨부파일을 읽는다 - 없으면 null(AS-IS는 404를 돌려준다). */
    public byte[] read(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return null;
        }
        Path base = Paths.get(uploadDir, SUB_DIR).normalize();
        Path path = base.resolve(storedName).normalize();
        // 경로 조작 방지 - 저장 디렉터리 밖은 읽지 않는다
        if (!path.startsWith(base)) {
            return null;
        }
        try {
            return Files.exists(path) ? Files.readAllBytes(path) : null;
        } catch (IOException e) {
            log.warn("Failed to read email attachment: {}", storedName, e);
            return null;
        }
    }

    /**
     * AS-IS 다운로드는 {@code Files.probeContentType}으로 Content-Type을 정하고 못 알아내면
     * application/octet-stream을 쓴다 - 같게 맞췄다.
     */
    public String contentTypeOf(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return "application/octet-stream";
        }
        try {
            String probed = Files.probeContentType(Paths.get(storedName));
            return (probed == null || probed.isEmpty()) ? "application/octet-stream" : probed;
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

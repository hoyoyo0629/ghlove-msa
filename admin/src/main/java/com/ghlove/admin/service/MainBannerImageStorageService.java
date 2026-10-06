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
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 메인 배너 PC/모바일 이미지 저장 (PopupImageStorageService와 같은 패턴).
 * AS-IS는 저장 파일명(PC_FILE_NAME/M_FILE_NAME)과 원본 파일명(PC_ORG_FILE_NAME/M_ORG_FILE_NAME)을
 * 따로 두고, 화면에서는 {@code /mainBanner/pc/{bannerId}} 엔드포인트로 스트리밍해 보여준다 -
 * 그래서 여기서는 URL이 아니라 저장 파일명을 돌려주고 읽기도 함께 담당한다.
 * 확장자·용량 제한은 AS-IS 화면 JS와 같게 jpg/jpeg/gif/png, 20MB다.
 */
@Service
@Slf4j
public class MainBannerImageStorageService {

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "gif", "png");
    private static final String SUB_DIR = "main-banner";

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** @return 디스크에 저장한 파일명(AS-IS PC_FILE_NAME/M_FILE_NAME에 들어가는 값). 파일이 없으면 null. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ContentException("파일크기는 20MB 이내로 등록 가능합니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED.contains(ext)) {
            throw new ContentException("이미지파일만 등록 가능합니다.");
        }
        String storedName = UUID.randomUUID() + "." + ext;
        try {
            Path dir = Paths.get(uploadDir, SUB_DIR);
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store main banner image", e);
            throw new ContentException("이미지 저장에 실패했습니다.");
        }
        return storedName;
    }

    /** 저장된 이미지를 읽는다 - 없으면 null(화면은 이미지 영역을 비워 둔다). */
    public byte[] read(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return null;
        }
        Path path = Paths.get(uploadDir, SUB_DIR, storedName).normalize();
        // 경로 조작 방지 - 저장 디렉터리 밖은 읽지 않는다
        if (!path.startsWith(Paths.get(uploadDir, SUB_DIR).normalize())) {
            return null;
        }
        try {
            return Files.exists(path) ? Files.readAllBytes(path) : null;
        } catch (IOException e) {
            log.warn("Failed to read main banner image: {}", storedName, e);
            return null;
        }
    }

    /** 확장자로 Content-Type을 돌려준다(스트리밍 응답용). */
    public String contentTypeOf(String storedName) {
        if (storedName == null) {
            return "application/octet-stream";
        }
        String lower = storedName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}

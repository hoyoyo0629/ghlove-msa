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
import java.util.UUID;

/** 신규 admin 관리화면(운영유지관리/매뉴얼/판매자공지 등)의 첨부파일을 로컬 디스크에 저장하는
 *  공용 서비스 - QnaFileStorageService/DataBoardFileStorageService와 동일 패턴이지만
 *  subdir을 파라미터로 받아 화면마다 클래스를 새로 만들지 않는다(신규 화면이 다수라 이번
 *  라운드부터 이 방식으로 통합). */
@Service
@Slf4j
public class AdminFileStorageService {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** AS-IS MaintenanceServiceImpl·SysNoticeSellerServiceImpl의 AVAILABLE_EXTENSION 18종 그대로
     *  (이 서비스를 쓰는 두 화면 - 운영유지관리·제공자 공지 - 목록이 동일하다). 커뮤니티와 같은
     *  목록이고 자료실(16종)만 hwpx·7z가 없다. */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "hwpx", "zip", "7z");

    private static final long MAX_SIZE = 50L * 1024 * 1024; // AS-IS 50MB

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file, String subdir) {
        return store(file, subdir, ALLOWED_EXTENSIONS, MAX_SIZE);
    }

    /**
     * 화면마다 AS-IS 허용 목록·용량이 다를 때 쓰는 형태다. 예: 스마트에디터 사진은 4종/5MB,
     * CTP는 html·ctp·css를 포함한 7종/5MB여서 이 서비스의 기본 18종으로는 저장 자체가 거부된다
     * (2026-10-06: 기본 목록을 그대로 써서 CTP 업로드가 "유효하지 않은 파일입니다."로 터졌다).
     */
    public String store(MultipartFile file, String subdir,
                        java.util.Set<String> allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        // AS-IS 순서: 확장자 검사 → 용량 검사. 문구도 AS-IS verbatim.
        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("유효하지 않은 파일입니다.");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("업로드 가능한 최대 용량 : " + (maxSize / (1024 * 1024)) + "MB 입니다");
        }
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        try {
            Path dir = Paths.get(uploadDir, subdir);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store {} file", subdir, e);
            throw new IllegalStateException("파일 저장에 실패했습니다.");
        }
        return storedName;
    }

    public void delete(String fileName, String subdir) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(uploadDir, subdir, fileName));
        } catch (IOException e) {
            log.warn("Failed to delete {} file {}", subdir, fileName, e);
        }
    }

    public Path resolve(String fileName, String subdir) {
        return Paths.get(uploadDir, subdir, fileName);
    }
}

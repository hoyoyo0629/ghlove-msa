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

    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
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

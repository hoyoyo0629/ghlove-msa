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

    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file) {
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
        String storedName = java.util.UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

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
}

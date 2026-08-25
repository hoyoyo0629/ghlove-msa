package com.ghlove.gift.service;

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

/** 답례품/리뷰 이미지를 로컬 디스크에 저장한다. 저장 경로는 정적 리소스 핸들러(/uploads/**)로 노출된다. */
@Service
@Slf4j
public class FileStorageService {

    @Value("${gift.upload.dir}")
    private String uploadDir;

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GiftException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new GiftException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store uploaded file", e);
            throw new GiftException("파일 저장에 실패했습니다.");
        }
        return storedName;
    }
}

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
import java.util.UUID;

/** 1:1 문의 첨부파일을 로컬 디스크에 저장한다 (gift의 FileStorageService와 동일한 패턴). */
@Service
@Slf4j
public class QnaFileStorageService {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB (AS-IS 1:1문의 첨부 제한)
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "gif", "png", "hwp", "doc", "docx", "ppt", "pptx", "pdf");

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new QnaException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new QnaException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new QnaException("jpg, gif, png, hwp, doc, ppt, pdf 등 이미지/문서파일만 등록 가능합니다.");
        }
        String storedName = UUID.randomUUID() + "." + ext;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store uploaded file", e);
            throw new QnaException("파일 저장에 실패했습니다.");
        }
        return storedName;
    }
}

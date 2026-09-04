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

/** 팝업 이미지(POPUP_STYLE=이미지 타입)를 로컬 디스크에 저장한다 (DataBoardFileStorageService와 동일 패턴). */
@Service
@Slf4j
public class PopupImageStorageService {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    /** @return 웹에서 접근 가능한 상대 URL (/uploads/admin/popup/{파일명}) */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ContentException("이미지 크기는 5MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        try {
            Path dir = Paths.get(uploadDir, "popup");
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store popup image", e);
            throw new ContentException("이미지 저장에 실패했습니다.");
        }
        return "/uploads/popup/" + storedName;
    }
}

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
    /** AS-IS QnaServiceImple의 AVAILABLE_EXTENSION 12종 그대로.
     *  (2026-10-06 교정: xls·xlsx가 빠져 있어 엑셀 첨부가 거부되고 있었다) */
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "gif", "png", "hwp", "doc", "docx",
                    "xls", "xlsx", "ppt", "pptx", "pdf");

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

    /** 저장된 파일명으로 실제 경로를 만든다 - Q&A 관리(5112) 첨부 다운로드용. */
    public Path resolve(String storedName) {
        return Paths.get(uploadDir, storedName);
    }

    /**
     * 저장된 파일을 디스크에서 지운다 - Q&A 관리(5112) 첨부 삭제용.
     * AS-IS {@code deleteItemImageByItemId}도 DB 행을 지우기 전에 {@code fileStorage.delete}로
     * 실제 파일을 지운다(그래서 화면 확인문구가 "파일이 실제로 삭제됩니다"다).
     * 파일이 없어도 삭제는 계속 진행한다.
     */
    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("첨부파일 삭제 실패: {}", storedName, e);
        }
    }
}

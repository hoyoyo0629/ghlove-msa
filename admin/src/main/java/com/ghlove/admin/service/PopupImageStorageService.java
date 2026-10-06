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

    /** AS-IS PopupServiceImpl.saveImage의 availableExtensions 그대로. */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS =
            java.util.Set.of("jpg", "gif", "bmp", "png", "jpeg");

    /** @return 웹에서 접근 가능한 상대 URL (/uploads/admin/popup/{파일명}) */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        // AS-IS는 확장자를 먼저 보고 그다음 용량을 본다 - 문구도 AS-IS verbatim.
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ContentException("유효하지 않은 파일입니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ContentException("업로드 가능한 최대 용량 : 5MB 입니다");
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

    /**
     * AS-IS PopupServiceImpl이 이미지 삭제·교체 때 부르는 {@code fileStorage.delete}에 해당한다.
     * 인자는 store()가 돌려준 웹 경로(/uploads/popup/{파일명})다. 파일명만 떼어내 업로드
     * 디렉터리에서 지우므로 경로 조작(../)으로 다른 파일을 지울 수 없다.
     * 파일이 이미 없어도 조용히 넘어간다(컬럼만 남은 과거 데이터).
     */
    public void delete(String webPath) {
        if (webPath == null || webPath.isBlank()) {
            return;
        }
        String fileName = Paths.get(webPath).getFileName().toString();
        if (fileName.isBlank() || ".".equals(fileName) || "..".equals(fileName)) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(uploadDir, "popup").resolve(fileName));
        } catch (IOException e) {
            // AS-IS도 삭제 실패로 트랜잭션을 되돌리지 않는다 - 기록만 남긴다.
            log.warn("Failed to delete popup image: {}", fileName, e);
        }
    }
}

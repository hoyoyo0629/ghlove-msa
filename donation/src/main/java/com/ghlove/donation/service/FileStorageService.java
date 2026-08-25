package com.ghlove.donation.service;

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

/** 파일을 로컬 디스크에 저장한다 (gift의 FileStorageService와 동일 패턴). 용도별로
 *  하위폴더를 나눈다(ctbny-opratn=지출증빙, designated-project=지정기부 대표이미지). */
@Service
@Slf4j
public class FileStorageService {

    @Value("${donation.upload.dir}")
    private String uploadDir;

    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB (AS-IS 증빙서류 첨부 제한과 동일)

    /** @return 저장된 파일명 (UUID 기반, DB에는 이 값만 기록) */
    public String store(MultipartFile file) {
        return store(file, "ctbny-opratn");
    }

    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new DonationException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new DonationException("파일 크기는 20MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store uploaded file", e);
            throw new DonationException("파일 저장에 실패했습니다.");
        }
        return storedName;
    }

    public Path resolve(String storedName) {
        return resolve(storedName, "ctbny-opratn");
    }

    public Path resolve(String storedName, String subDir) {
        return Paths.get(uploadDir, subDir, storedName);
    }
}

package com.ghlove.donation.service;

import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.LocgovRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * 지자체별 직인(관인) 이미지를 암호화 저장/조회한다. AS-IS의
 * opmanager/user/locgov/edit.jsp "직인이미지" 업로드 + sealView 복호화-조회 흐름과
 * 동등하게 재현하되, 상용 pCrypto 모듈 대신 {@link SealCipher}(AES-256-GCM)를 쓴다.
 *
 * 저장 위치는 donation.upload.dir(정적 리소스로 공개 서빙되는 /uploads/donation/**)과
 * 완전히 분리된 별도 디렉터리다 - 암호문이라도 공개 URL 아래 두지 않는다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocgovSealService {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    private final LocgovRepository locgovRepository;
    private final SealCipher sealCipher;

    @Value("${donation.seal.storage-dir}")
    private String storageDir;

    public void uploadSeal(String locgovCode, String offcsNm, MultipartFile file) {
        Locgov locgov = locgovRepository.findById(locgovCode)
                .orElseThrow(() -> new DonationException("존재하지 않는 지자체입니다: " + locgovCode));
        if (file == null || file.isEmpty()) {
            throw new DonationException("직인 이미지 파일을 선택해 주세요.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new DonationException("직인 이미지는 5MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
        String storedName = UUID.randomUUID() + ext + ".enc";

        try {
            byte[] plain = file.getBytes();
            byte[] encrypted = sealCipher.encrypt(plain);
            Path dir = Paths.get(storageDir);
            Files.createDirectories(dir);
            Files.write(dir.resolve(storedName), encrypted);
        } catch (IOException e) {
            log.error("Failed to store encrypted seal image for {}", locgovCode, e);
            throw new DonationException("직인 이미지 저장에 실패했습니다.");
        }

        // 기존 파일이 있었다면 교체이므로 이전 암호화 파일은 정리한다.
        if (locgov.getOffcsFileNm() != null && !locgov.getOffcsFileNm().isBlank()) {
            deleteQuietly(locgov.getOffcsFileNm());
        }

        locgov.setOffcsNm(offcsNm);
        locgov.setOffcsFileNm(storedName);
        locgov.setOrginlFileNm(original);
        locgovRepository.save(locgov);
    }

    /** 복호화된 원본 이미지 바이트. 직인이 등록되지 않은 지자체면 empty. */
    public Optional<byte[]> decryptedSealBytes(String locgovCode) {
        if (locgovCode == null) {
            return Optional.empty();
        }
        return locgovRepository.findById(locgovCode)
                .filter(l -> l.getOffcsFileNm() != null && !l.getOffcsFileNm().isBlank())
                .flatMap(l -> {
                    try {
                        byte[] encrypted = Files.readAllBytes(Paths.get(storageDir, l.getOffcsFileNm()));
                        return Optional.of(sealCipher.decrypt(encrypted));
                    } catch (IOException e) {
                        log.error("Failed to read encrypted seal image for {}", locgovCode, e);
                        return Optional.empty();
                    }
                });
    }

    /** 인쇄/화면 템플릿에 <img src="..."> 로 바로 꽂아 쓸 수 있는 base64 data URI. */
    public Optional<String> sealDataUri(String locgovCode) {
        return locgovRepository.findById(locgovCode)
                .filter(l -> l.getOffcsFileNm() != null && !l.getOffcsFileNm().isBlank())
                .flatMap(l -> decryptedSealBytes(locgovCode).map(bytes -> {
                    String mime = URLConnection.guessContentTypeFromName(l.getOrginlFileNm());
                    if (mime == null) {
                        mime = "image/png";
                    }
                    return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
                }));
    }

    private void deleteQuietly(String storedName) {
        try {
            Files.deleteIfExists(Paths.get(storageDir, storedName));
        } catch (IOException e) {
            log.warn("Failed to delete old encrypted seal file {}", storedName, e);
        }
    }
}

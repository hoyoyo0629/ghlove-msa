package com.ghlove.admin.service;

import com.ghlove.admin.domain.RepresentativeBanner;
import com.ghlove.admin.repository.RepresentativeBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 메인 대표배너 관리 (AS-IS opmanager/representative-banner - RepresentativeBannerManagerController,
 * PROCESS_TYPE='ITEM'). RepresentativeBannerService(지정기부, PROCESS_TYPE='DESIGNATED_DONATION')와
 * 같은 G_REPRST_BANNER 테이블·같은 15슬롯 규칙을 쓰지만 PROCESS_TYPE만 다른 별개 화면이다
 * (AS-IS도 두 컨트롤러가 URL만 다를 뿐 같은 서비스/JSP를 공유하는 구조).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MainBannerRepBannerService {

    private static final String PROCESS_TYPE = "ITEM";
    private static final int MAX_SLOTS = 15;
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepresentativeBannerRepository repository;

    @Value("${admin.upload.dir}")
    private String uploadDir;

    public List<RepresentativeBanner> list() {
        return repository.findByProcessTypeOrderByDisplayOrderAsc(PROCESS_TYPE);
    }

    public RepresentativeBanner get(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RepresentativeBannerException("배너를 찾을 수 없습니다."));
    }

    @Transactional
    public RepresentativeBanner create(String title, String linkUrl, String bannerContent, Integer displayOrder,
                                        MultipartFile pcImage, MultipartFile mobileImage, Long managerId) {
        validate(title, displayOrder, null);
        if (pcImage == null || pcImage.isEmpty() || mobileImage == null || mobileImage.isEmpty()) {
            throw new RepresentativeBannerException("PC/모바일 이미지를 모두 첨부해 주세요.");
        }
        RepresentativeBanner banner = new RepresentativeBanner();
        banner.setTitle(title);
        banner.setLinkUrl(linkUrl);
        banner.setBannerContent(bannerContent);
        banner.setDisplayOrder(displayOrder);
        banner.setUseYn("Y");
        banner.setProcessType(PROCESS_TYPE);
        banner.setFileNamePc(store(pcImage));
        banner.setFileNameMobile(store(mobileImage));
        banner.setFrstRegisterId(managerId);
        banner.setFrstRegistPnttm(LocalDateTime.now());
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    @Transactional
    public RepresentativeBanner update(Integer id, String title, String linkUrl, String bannerContent,
                                        Integer displayOrder, MultipartFile pcImage, MultipartFile mobileImage,
                                        Long managerId) {
        validate(title, displayOrder, id);
        RepresentativeBanner banner = get(id);
        banner.setTitle(title);
        banner.setLinkUrl(linkUrl);
        banner.setBannerContent(bannerContent);
        banner.setDisplayOrder(displayOrder);
        if (pcImage != null && !pcImage.isEmpty()) {
            banner.setFileNamePc(store(pcImage));
        }
        if (mobileImage != null && !mobileImage.isEmpty()) {
            banner.setFileNameMobile(store(mobileImage));
        }
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    @Transactional
    public RepresentativeBanner toggle(Integer id, Long managerId) {
        RepresentativeBanner banner = get(id);
        banner.setUseYn("Y".equals(banner.getUseYn()) ? "N" : "Y");
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    private void validate(String title, Integer displayOrder, Integer editingId) {
        if (title == null || title.isBlank()) {
            throw new RepresentativeBannerException("제목을 입력해 주세요.");
        }
        if (displayOrder == null || displayOrder < 1 || displayOrder > MAX_SLOTS) {
            throw new RepresentativeBannerException("노출순서는 1~" + MAX_SLOTS + " 사이여야 합니다.");
        }
        boolean duplicate = list().stream()
                .anyMatch(b -> displayOrder.equals(b.getDisplayOrder()) && !b.getReprstBannerId().equals(editingId));
        if (duplicate) {
            throw new RepresentativeBannerException("이미 사용 중인 노출순서입니다.");
        }
    }

    private String store(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new RepresentativeBannerException("이미지 크기는 5MB를 초과할 수 없습니다.");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + ext;
        try {
            Path dir = Paths.get(uploadDir, "main-banner");
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store banner image", e);
            throw new RepresentativeBannerException("이미지 저장에 실패했습니다.");
        }
        return "/uploads/main-banner/" + storedName;
    }
}

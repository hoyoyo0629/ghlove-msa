package com.ghlove.admin.web;

import com.ghlove.admin.domain.Banner;
import com.ghlove.admin.service.OperationContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only JSON API for other services (member 메인 화면 배너 캐러셀) - not part of any write path. */
@RestController
@RequiredArgsConstructor
public class BannerApiController {

    private final OperationContentService operationContentService;

    @GetMapping("/api/banners")
    public List<BannerDto> activeBanners() {
        return operationContentService.activeBanners().stream()
                .map(this::toDto)
                .toList();
    }

    private BannerDto toDto(Banner b) {
        return new BannerDto(b.getBannerId(), b.getTitle(), b.getContents(), b.getLinkUrl(), b.getImageUrl());
    }
}

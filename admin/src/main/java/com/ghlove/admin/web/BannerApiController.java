package com.ghlove.admin.web;

import com.ghlove.admin.domain.Banner;
import com.ghlove.admin.service.OperationContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only JSON API for other services (member 메인 화면 배너 캐러셀, 로그인화면 배너) -
 *  not part of any write path. {@code type} 생략 시 기존과 동일하게 메인(MAIN) 배너를 돌려준다 -
 *  하위호환을 위해 기본값을 바꾸지 않는다. */
@RestController
@RequiredArgsConstructor
public class BannerApiController {

    private final OperationContentService operationContentService;

    @GetMapping("/api/banners")
    public List<BannerDto> activeBanners(@RequestParam(required = false) String type) {
        return operationContentService.activeBanners(type == null || type.isBlank() ? "MAIN" : type).stream()
                .map(this::toDto)
                .toList();
    }

    private BannerDto toDto(Banner b) {
        return new BannerDto(b.getBannerId(), b.getTitle(), b.getContents(), b.getLinkUrl(), b.getImageUrl());
    }
}

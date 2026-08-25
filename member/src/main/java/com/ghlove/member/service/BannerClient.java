package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 메인 화면 상단 배너 캐러셀에 노출할 배너를 admin 서비스에서 읽어오는 조회 - donation의
 * DonationClient/order의 GiftClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시
 * 메인 화면 자체가 깨지면 안 되므로 빈 목록으로 조용히 대체한다.
 */
@Component
@Slf4j
public class BannerClient {

    private final RestClient restClient;

    public BannerClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
    }

    public List<BannerInfo> activeBanners() {
        try {
            List<BannerInfo> banners = restClient.get()
                    .uri("/api/banners")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<BannerInfo>>() {
                    });
            return banners != null ? banners : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch active banners from admin service - showing empty list", e);
            return List.of();
        }
    }
}

package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** admin이 gift 서비스의 메인 진열상품 관리 API를 호출하는 클라이언트. */
@org.springframework.stereotype.Service
@Slf4j
public class MainDisplayAdminClient {

    private final RestClient restClient;

    public MainDisplayAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record ItemRow(Long itemId, String itemName, Integer displayOrder) {
    }

    public List<ItemRow> get(String templateId) {
        try {
            return restClient.get().uri("/api/admin/main-display/{templateId}", templateId).retrieve()
                    .body(new ParameterizedTypeReference<List<ItemRow>>() {
                    });
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public void replace(String templateId, List<Long> itemIds) {
        restClient.put().uri("/api/admin/main-display/{templateId}", templateId)
                .body(Map.of("itemIds", itemIds)).retrieve().toBodilessEntity();
    }
}

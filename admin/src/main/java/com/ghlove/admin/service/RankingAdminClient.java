package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 답례품 판매 랭킹 관리 (AS-IS opmanager/ranking - RankingManagerController, B9) -
 *  gift 서비스의 OP_RANKING 카테고리(그룹)별 수동 큐레이션 순서를 cross-service CRUD한다. */
@Component
public class RankingAdminClient {

    private final RestClient restClient;

    public RankingAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<String> groups() {
        try {
            List<String> groups = restClient.get().uri("/api/admin/ranking/groups")
                    .retrieve().body(new ParameterizedTypeReference<List<String>>() {
                    });
            return groups != null ? groups : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<RankingRow> list(String categoryUrl) {
        if (categoryUrl == null || categoryUrl.isBlank()) {
            return List.of();
        }
        try {
            List<RankingRow> rows = restClient.get()
                    .uri("/api/admin/ranking?categoryUrl={categoryUrl}", categoryUrl)
                    .retrieve().body(new ParameterizedTypeReference<List<RankingRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<ItemOption> itemSearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        try {
            List<ItemOption> items = restClient.get()
                    .uri("/api/admin/ranking/item-search?keyword={keyword}", keyword)
                    .retrieve().body(new ParameterizedTypeReference<List<ItemOption>>() {
                    });
            return items != null ? items : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void add(String categoryUrl, Long itemId) {
        try {
            restClient.post().uri("/api/admin/ranking")
                    .body(Map.of("categoryUrl", categoryUrl, "itemId", itemId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "랭킹 등록에 실패했습니다."));
        }
    }

    public void delete(Integer rankingId) {
        restClient.post().uri("/api/admin/ranking/{id}/delete", rankingId).retrieve().toBodilessEntity();
    }

    public void move(Integer rankingId, String direction) {
        restClient.post().uri("/api/admin/ranking/{id}/move?direction={d}", rankingId, direction)
                .retrieve().toBodilessEntity();
    }

    private static String extractMessage(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record ItemOption(Long itemId, String itemName, String locgovCode, Integer salePrice) {
    }

    public record RankingRow(Integer rankingId, String categoryUrl, Long itemId, Integer ordering,
                              String itemName, Integer salePrice, String locgovCode, String thumbnailUrl) {
    }
}

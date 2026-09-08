package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 답례품 리뷰 관리자 대응 (AS-IS opmanager/item review 승인/추천/CSV 다운로드 - 답례품
 *  상품관리 2단계 #4) - gift 서비스의 OP_ITEM_REVIEW cross-service API 래퍼. */
@Component
public class ReviewAdminClient {

    private final RestClient restClient;

    public ReviewAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<ReviewDto> search(Long itemId, String keyword, String recommendFlag, String displayFlag) {
        try {
            List<ReviewDto> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/gift-reviews")
                            .queryParamIfPresent("itemId", java.util.Optional.ofNullable(itemId))
                            .queryParamIfPresent("keyword", java.util.Optional.ofNullable(blankToNull(keyword)))
                            .queryParamIfPresent("recommendFlag", java.util.Optional.ofNullable(blankToNull(recommendFlag)))
                            .queryParamIfPresent("displayFlag", java.util.Optional.ofNullable(blankToNull(displayFlag)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<ReviewDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public Map<Long, String> itemNames(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<?, ?> raw = restClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/api/admin/gift-reviews/item-names");
                        itemIds.forEach(id -> b.queryParam("itemIds", id));
                        return b.build();
                    })
                    .retrieve().body(Map.class);
            if (raw == null) {
                return Map.of();
            }
            java.util.Map<Long, String> result = new java.util.LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(Long.valueOf(String.valueOf(k)), String.valueOf(v)));
            return result;
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /** 신고 건수 (SFR-005 재검토 라운드) - 목록 화면에서 리뷰별 신고 건수 표시용. */
    public Map<Long, Long> reportCounts(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<?, ?> raw = restClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/api/admin/gift-reviews/report-counts");
                        reviewIds.forEach(id -> b.queryParam("reviewIds", id));
                        return b.build();
                    })
                    .retrieve().body(Map.class);
            if (raw == null) {
                return Map.of();
            }
            java.util.Map<Long, Long> result = new java.util.LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(Long.valueOf(String.valueOf(k)), Long.valueOf(String.valueOf(v))));
            return result;
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    public void setRecommend(Long id, boolean recommend) {
        try {
            restClient.post().uri("/api/admin/gift-reviews/{id}/recommend?recommend={r}", id, recommend)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "리뷰 추천 처리에 실패했습니다."));
        }
    }

    public void setDisplay(Long id, boolean display) {
        try {
            restClient.post().uri("/api/admin/gift-reviews/{id}/display?display={d}", id, display)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "리뷰 노출 처리에 실패했습니다."));
        }
    }

    public byte[] export(Long itemId, String keyword, String recommendFlag, String displayFlag) {
        ResponseEntity<byte[]> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/admin/gift-reviews/export")
                        .queryParamIfPresent("itemId", java.util.Optional.ofNullable(itemId))
                        .queryParamIfPresent("keyword", java.util.Optional.ofNullable(blankToNull(keyword)))
                        .queryParamIfPresent("recommendFlag", java.util.Optional.ofNullable(blankToNull(recommendFlag)))
                        .queryParamIfPresent("displayFlag", java.util.Optional.ofNullable(blankToNull(displayFlag)))
                        .build())
                .retrieve().toEntity(byte[].class);
        return response.getBody();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
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

    public record ReviewDto(Long itemReviewId, Long itemId, String orderCode, String subject, String content,
                             Integer score, String recommendFlag, Long userId, String userName, Long sellerId,
                             String displayFlag, LocalDateTime createdDate) {
    }
}

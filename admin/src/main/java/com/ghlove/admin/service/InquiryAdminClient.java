package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 답례품 상품문의(Q&A) 관리자 대응 (SFR-005 재검토 라운드 - 원래 admin 콘솔에 노출되지
 *  않던 gap) - gift 서비스의 G_ITEM_INQUIRY cross-service API 래퍼. */
@Component
public class InquiryAdminClient {

    private final RestClient restClient;

    public InquiryAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<InquiryDto> search(Long itemId, String keyword, String status, String displayFlag) {
        try {
            List<InquiryDto> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/gift-inquiries")
                            .queryParamIfPresent("itemId", java.util.Optional.ofNullable(itemId))
                            .queryParamIfPresent("keyword", java.util.Optional.ofNullable(blankToNull(keyword)))
                            .queryParamIfPresent("status", java.util.Optional.ofNullable(blankToNull(status)))
                            .queryParamIfPresent("displayFlag", java.util.Optional.ofNullable(blankToNull(displayFlag)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<InquiryDto>>() {
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
                        var b = uriBuilder.path("/api/admin/gift-inquiries/item-names");
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

    public Map<Long, Long> reportCounts(List<Long> inquiryIds) {
        if (inquiryIds == null || inquiryIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<?, ?> raw = restClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/api/admin/gift-inquiries/report-counts");
                        inquiryIds.forEach(id -> b.queryParam("inquiryIds", id));
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

    public void answer(Long id, String answer) {
        try {
            restClient.post().uri("/api/admin/gift-inquiries/{id}/answer?answer={a}", id, answer)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "답변 등록에 실패했습니다."));
        }
    }

    public void setDisplay(Long id, boolean display) {
        try {
            restClient.post().uri("/api/admin/gift-inquiries/{id}/display?display={d}", id, display)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "노출 처리에 실패했습니다."));
        }
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

    public record InquiryDto(Long inquiryId, String status, Long itemId, Long userId, String question,
                              String secretYn, String answer, LocalDateTime answeredDate, String displayFlag,
                              LocalDateTime createdDate) {
    }
}

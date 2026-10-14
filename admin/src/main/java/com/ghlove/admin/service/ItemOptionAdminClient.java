package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 답례품 옵션 관리 (SFR-005 재검토 라운드) - gift 서비스의 OP_ITEM_OPTION cross-service API 래퍼. */
@Component
public class ItemOptionAdminClient {

    private final RestClient restClient;

    public ItemOptionAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<OptionDto> list(Long itemId) {
        try {
            List<OptionDto> list = restClient.get().uri("/api/admin/gift-items/{itemId}/options", itemId)
                    .retrieve().body(new ParameterizedTypeReference<List<OptionDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void register(Long itemId, String optionTitle, String optionName, Integer optionPrice, boolean stockTracked,
                          Integer stockQuantity) {
        try {
            restClient.post().uri("/api/admin/gift-items/{itemId}/options", itemId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form(optionTitle, optionName, optionPrice, stockTracked, stockQuantity))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "옵션 등록에 실패했습니다."));
        }
    }

    public void edit(Long itemId, Long optionId, String optionTitle, String optionName, Integer optionPrice,
                      boolean stockTracked, Integer stockQuantity) {
        try {
            restClient.put().uri("/api/admin/gift-items/{itemId}/options/{optionId}", itemId, optionId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form(optionTitle, optionName, optionPrice, stockTracked, stockQuantity))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "옵션 수정에 실패했습니다."));
        }
    }

    public void delete(Long itemId, Long optionId) {
        try {
            restClient.post().uri("/api/admin/gift-items/{itemId}/options/{optionId}/delete", itemId, optionId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "옵션 삭제에 실패했습니다."));
        }
    }

    public void setDisplay(Long itemId, Long optionId, boolean display) {
        try {
            restClient.post().uri("/api/admin/gift-items/{itemId}/options/{optionId}/display?display={d}",
                            itemId, optionId, display)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "옵션 노출 처리에 실패했습니다."));
        }
    }

    private static MultiValueMap<String, String> form(String optionTitle, String optionName, Integer optionPrice,
                                                        boolean stockTracked, Integer stockQuantity) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("optionTitle", optionTitle);
        body.add("optionName", optionName);
        body.add("optionPrice", String.valueOf(optionPrice != null ? optionPrice : 0));
        body.add("stockTracked", String.valueOf(stockTracked));
        body.add("stockQuantity", String.valueOf(stockQuantity != null ? stockQuantity : 0));
        return body;
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

    /** 선택형(S)은 AS-IS 저장규칙대로 optionName1=옵션 제목, optionName2=옵션 내용. */
    public record OptionDto(Long itemOptionId, Long itemId, String optionType, String optionName1, String optionName2,
                             Integer optionPrice, String optionStockFlag, Integer optionStockQuantity,
                             String optionSoldOutFlag, String optionDisplayFlag, Long createdUserId,
                             String createdDate) {
    }
}

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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 답례품 상품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1단계 대응) -
 *  gift 서비스의 OP_ITEM(Gift) cross-service CRUD. 셀러 self-service만 있던 답례품 등록/수정을
 *  관리자가 셀러를 대신 지정해 직접 수행하고, 카테고리 일괄 배정/상품 복사를 지원한다. */
@Component
public class ItemAdminClient {

    private final RestClient restClient;

    public ItemAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public List<ItemDto> search(String itemName, String categoryCode, Long sellerId, String dataStatusCode) {
        try {
            List<ItemDto> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/gift-items")
                            .queryParamIfPresent("itemName", java.util.Optional.ofNullable(blankToNull(itemName)))
                            .queryParamIfPresent("categoryCode", java.util.Optional.ofNullable(blankToNull(categoryCode)))
                            .queryParamIfPresent("sellerId", java.util.Optional.ofNullable(sellerId))
                            .queryParamIfPresent("dataStatusCode", java.util.Optional.ofNullable(blankToNull(dataStatusCode)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<ItemDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public ItemDto get(Long id) {
        try {
            return restClient.get().uri("/api/admin/gift-items/{id}", id).retrieve().body(ItemDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "답례품을 찾을 수 없습니다."));
        }
    }

    public ItemDto create(Long sellerId, String itemName, String itemSummary, String detailContent,
                           String categoryCode, String locgovCode, Integer salePrice, Integer stockQuantity,
                           String displayType, String displayStartDate, String displayEndDate,
                           Integer minDonationAmount, Integer brandId, List<MultipartFile> images) {
        try {
            var multipart = new org.springframework.util.LinkedMultiValueMap<String, Object>();
            addIfPresent(multipart, "sellerId", sellerId);
            multipart.add("itemName", itemName);
            addIfPresent(multipart, "itemSummary", itemSummary);
            addIfPresent(multipart, "detailContent", detailContent);
            multipart.add("categoryCode", categoryCode);
            addIfPresent(multipart, "locgovCode", locgovCode);
            addIfPresent(multipart, "salePrice", salePrice);
            addIfPresent(multipart, "stockQuantity", stockQuantity);
            addIfPresent(multipart, "displayType", displayType);
            addIfPresent(multipart, "displayStartDate", displayStartDate);
            addIfPresent(multipart, "displayEndDate", displayEndDate);
            addIfPresent(multipart, "minDonationAmount", minDonationAmount);
            addIfPresent(multipart, "brandId", brandId);
            if (images != null) {
                for (MultipartFile f : images) {
                    if (f != null && !f.isEmpty()) {
                        multipart.add("images", f.getResource());
                    }
                }
            }
            return restClient.post().uri("/api/admin/gift-items")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipart)
                    .retrieve().body(ItemDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "답례품 등록에 실패했습니다."));
        }
    }

    public ItemDto update(Long id, Long sellerId, String itemName, String itemSummary, String detailContent,
                           String categoryCode, Integer salePrice, Integer stockQuantity, String displayType,
                           String displayStartDate, String displayEndDate, Integer minDonationAmount, Integer brandId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("sellerId", String.valueOf(sellerId));
        body.add("itemName", itemName);
        addIfPresent(body, "itemSummary", itemSummary);
        addIfPresent(body, "detailContent", detailContent);
        body.add("categoryCode", categoryCode);
        body.add("salePrice", String.valueOf(salePrice));
        body.add("stockQuantity", String.valueOf(stockQuantity));
        addIfPresent(body, "displayType", displayType);
        addIfPresent(body, "displayStartDate", displayStartDate);
        addIfPresent(body, "displayEndDate", displayEndDate);
        if (minDonationAmount != null) {
            body.add("minDonationAmount", String.valueOf(minDonationAmount));
        }
        if (brandId != null) {
            body.add("brandId", String.valueOf(brandId));
        }
        try {
            return restClient.put().uri("/api/admin/gift-items/{id}", id)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().body(ItemDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "답례품 수정에 실패했습니다."));
        }
    }

    public int bulkAssignCategory(List<Long> itemIds, String categoryCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(id -> body.add("itemIds", String.valueOf(id)));
        body.add("categoryCode", categoryCode);
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-category")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().body(Map.class);
            Object updated = result != null ? result.get("updated") : null;
            return updated != null ? ((Number) updated).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "카테고리 일괄 배정에 실패했습니다."));
        }
    }

    public ItemDto copy(Long id) {
        try {
            return restClient.post().uri("/api/admin/gift-items/{id}/copy", id).retrieve().body(ItemDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "상품 복사에 실패했습니다."));
        }
    }

    private static void addIfPresent(MultiValueMap<String, Object> map, String key, Object value) {
        if (value != null && !(value instanceof String s && s.isBlank())) {
            map.add(key, value);
        }
    }

    private static void addIfPresent(MultiValueMap<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.add(key, value);
        }
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

    public record ItemDto(Long itemId, Long sellerId, String itemName, String itemSummary, String detailContent,
                           String categoryCode, String locgovCode, Integer salePrice, Integer stockQuantity,
                           String soldOut, String displayFlag, String dataStatusCode, String createdDate,
                           String displayType, String displayStartDate, String displayEndDate,
                           Integer minDonationAmount, String representativeItemYn, Integer brandId) {
    }
}

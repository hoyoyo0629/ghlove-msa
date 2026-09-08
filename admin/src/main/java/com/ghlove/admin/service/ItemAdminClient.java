package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public ItemAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
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

    // ---------------------------------------------------------------- 지자체 승인/반려, 대표상품관리
    // (원래 gift 자체 무인증 임시화면이었던 것을 admin 콘솔 전용 API로 이관)

    public List<ItemDto> pending() {
        try {
            List<ItemDto> list = restClient.get().uri("/api/admin/gift-items/pending").retrieve()
                    .body(new ParameterizedTypeReference<List<ItemDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void approve(Long id) {
        try {
            restClient.post().uri("/api/admin/gift-items/{id}/approve", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "승인 처리에 실패했습니다."));
        }
    }

    public void reject(Long id) {
        try {
            restClient.post().uri("/api/admin/gift-items/{id}/reject", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "반려 처리에 실패했습니다."));
        }
    }

    public List<ItemDto> representativeItems() {
        try {
            List<ItemDto> list = restClient.get().uri("/api/admin/gift-items/representative").retrieve()
                    .body(new ParameterizedTypeReference<List<ItemDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void registerRepresentative(Long id) {
        try {
            restClient.post().uri("/api/admin/gift-items/{id}/representative/register", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "대표상품 등록에 실패했습니다."));
        }
    }

    public void unregisterRepresentative(Long id) {
        try {
            restClient.post().uri("/api/admin/gift-items/{id}/representative/delete", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "대표상품 해제에 실패했습니다."));
        }
    }

    // ---------------------------------------------------------------- 2단계: 엑셀 대량처리/일괄작업

    public BulkUploadResult bulkUpload(MultipartFile file) {
        try {
            var multipart = new LinkedMultiValueMap<String, Object>();
            multipart.add("file", file.getResource());
            return restClient.post().uri("/api/admin/gift-items/bulk-upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipart)
                    .retrieve().body(BulkUploadResult.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "엑셀 대량등록에 실패했습니다."));
        }
    }

    public byte[] export(String itemName, String categoryCode, Long sellerId, String dataStatusCode) {
        ResponseEntity<byte[]> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/admin/gift-items/export")
                        .queryParamIfPresent("itemName", java.util.Optional.ofNullable(blankToNull(itemName)))
                        .queryParamIfPresent("categoryCode", java.util.Optional.ofNullable(blankToNull(categoryCode)))
                        .queryParamIfPresent("sellerId", java.util.Optional.ofNullable(sellerId))
                        .queryParamIfPresent("dataStatusCode", java.util.Optional.ofNullable(blankToNull(dataStatusCode)))
                        .build())
                .retrieve().toEntity(byte[].class);
        return response.getBody();
    }

    public int bulkDisplay(List<Long> itemIds, String displayFlag) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(id -> body.add("itemIds", String.valueOf(id)));
        body.add("displayFlag", displayFlag);
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-display")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(Map.class);
            return result != null && result.get("updated") != null ? ((Number) result.get("updated")).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "노출 일괄변경에 실패했습니다."));
        }
    }

    public int bulkLabel(List<Long> itemIds, String itemLabel) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(id -> body.add("itemIds", String.valueOf(id)));
        body.add("itemLabel", itemLabel);
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-label")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(Map.class);
            return result != null && result.get("updated") != null ? ((Number) result.get("updated")).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "라벨 일괄변경에 실패했습니다."));
        }
    }

    public int bulkDelete(List<Long> itemIds) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(id -> body.add("itemIds", String.valueOf(id)));
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-delete")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(Map.class);
            return result != null && result.get("updated") != null ? ((Number) result.get("updated")).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "일괄 삭제에 실패했습니다."));
        }
    }

    public int bulkOrdering(List<Long> itemIds, List<Integer> orderings) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(id -> body.add("itemIds", String.valueOf(id)));
        orderings.forEach(o -> body.add("orderings", String.valueOf(o)));
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-ordering")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(Map.class);
            return result != null && result.get("updated") != null ? ((Number) result.get("updated")).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "순서 변경에 실패했습니다."));
        }
    }

    public int bulkSales(List<SalesUpdate> updates) {
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/gift-items/bulk-sales")
                    .contentType(MediaType.APPLICATION_JSON).body(updates).retrieve().body(Map.class);
            return result != null && result.get("updated") != null ? ((Number) result.get("updated")).intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "판매정보 일괄수정에 실패했습니다."));
        }
    }

    public record BulkUploadResult(int created, List<String> errors) {
    }

    public record SalesUpdate(Long itemId, Integer salePrice, Integer stockQuantity) {
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
                           Integer minDonationAmount, String representativeItemYn, Integer brandId,
                           String itemLabel, Integer adminOrdering, String deliveryCompanyName, String shippingType,
                           Integer shipping, Integer shippingFreeAmount, Integer shippingExtraCharge1,
                           Integer shippingExtraCharge2, String itemReturnFlag) {
    }

    /** 배송비/택배사 설정 (SFR-005 재검토 라운드). */
    public void updateShipping(Long itemId, String deliveryCompanyName, String shippingType, Integer shipping,
                                Integer shippingFreeAmount, Integer shippingExtraCharge1, Integer shippingExtraCharge2,
                                boolean itemReturnAllowed) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            addIfPresent(body, "deliveryCompanyName", deliveryCompanyName);
            body.add("shippingType", shippingType);
            if (shipping != null) {
                body.add("shipping", String.valueOf(shipping));
            }
            if (shippingFreeAmount != null) {
                body.add("shippingFreeAmount", String.valueOf(shippingFreeAmount));
            }
            if (shippingExtraCharge1 != null) {
                body.add("shippingExtraCharge1", String.valueOf(shippingExtraCharge1));
            }
            if (shippingExtraCharge2 != null) {
                body.add("shippingExtraCharge2", String.valueOf(shippingExtraCharge2));
            }
            body.add("itemReturnAllowed", String.valueOf(itemReturnAllowed));
            restClient.post().uri("/api/admin/gift-items/{id}/shipping", itemId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "배송비/택배사 설정에 실패했습니다."));
        }
    }
}

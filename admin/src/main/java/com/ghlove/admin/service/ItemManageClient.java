package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** 답례품관리(AS-IS opmanager/item/list) - gift 서비스의 답례품 목록·일괄처리·로그·대표 답례품 cross-service API 래퍼. */
@Component
public class ItemManageClient {

    private static final String BASE = "/api/admin/item-manage";

    private final RestClient restClient;

    public ItemManageClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record Search(String locgov, String shLocgovCode, String where, String query, String srchDateType,
                         String searchStartDate, String searchEndDate, Integer categoryGroupId, String categoryClass,
                         Integer categoryId, String saleStatus, String displayFlag, Long sellerId, String orderBy, String sort) {
    }

    public record ItemRow(Long itemId, String itemCode, String itemUserCode, String itemName, String itemSellerCode,
                          Integer salePrice, String itemPrice, String displayFlag, String dataStatusCode,
                          Integer orderMinQuantity, Integer orderMaxQuantity, String itemOptionFlag, String itemTextOptionFlag,
                          String stockFlag, Integer stockQuantity, String soldOut, String itemSoldOutFlag, String itemType,
                          String itemLabel, Long sellerId, String sellerName, String companyName, String sellerLocgovCode,
                          Integer hits, String brand, String createdDate, String updatedDate, String displayCommissionRate,
                          Integer minusSpotDiscount, Integer exceptSpotDiscount, Integer presentPrice, String thumbnail) {
    }

    public record LogRow(Long itemLogId, Long itemId, String itemUserCode, String itemName, String displayFlag, String soldOut,
                         Integer salePrice, String processPage, Long createdManagerId, String createdDate, String actionType,
                         String sellerLoginId, String sellerName) {
    }

    public record RprsRow(Long itemId, String itemUserCode, String dataStatusCode, String displayFlag, String itemCode, String itemName,
                          Integer salePrice, String itemSoldOutFlag, String thumbnail) {
    }

    public record OffRprsRow(Long itemId, String itemUserCode, String dataStatusCode, String displayFlag, String itemCode, String itemName,
                             Integer salePrice, String itemOptionFlag, Integer stockQuantity, String options, String itemSoldOutFlag,
                             String itemOptionId, List<String> optionStockQuantity, String changedOption, String thumbnail) {
    }

    public record OffRprsInput(Long itemId, String option) {
    }

    public record CategoryOption(String categoryCode, String categoryLevel, Integer categoryId, String categoryName) {
    }

    private record ListRequest(Search search, int offset, int limit) {
    }

    private record IdsRequest(List<Long> ids, String flag, long managerId) {
    }

    private record RprsRequest(String locgov, List<Long> itemIds, long userId) {
    }

    private record OffRprsRequest(String locgov, List<OffRprsInput> items, long userId) {
    }

    public int count(Search s) {
        Map<String, Integer> body = restClient.post().uri(BASE + "/count").body(s).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Integer>>() {
                });
        return body != null && body.get("count") != null ? body.get("count") : 0;
    }

    public List<ItemRow> list(Search s, int offset, int limit) {
        List<ItemRow> rows = restClient.post().uri(BASE + "/list").body(new ListRequest(s, offset, limit)).retrieve()
                .body(new ParameterizedTypeReference<List<ItemRow>>() {
                });
        return rows != null ? rows : List.of();
    }

    public void updateDisplay(List<Long> ids, String flag, long managerId) {
        restClient.post().uri(BASE + "/update-display").body(new IdsRequest(ids, flag, managerId)).retrieve().toBodilessEntity();
    }

    public void updateLabel(List<Long> ids, String flag, long managerId) {
        restClient.post().uri(BASE + "/update-label").body(new IdsRequest(ids, flag, managerId)).retrieve().toBodilessEntity();
    }

    public void delete(List<Long> ids, long managerId) {
        restClient.post().uri(BASE + "/delete").body(new IdsRequest(ids, null, managerId)).retrieve().toBodilessEntity();
    }

    public int logCount(long itemId) {
        Map<String, Integer> body = restClient.get().uri(BASE + "/{id}/log/count", itemId).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Integer>>() {
                });
        return body != null && body.get("count") != null ? body.get("count") : 0;
    }

    public List<LogRow> logList(long itemId, int offset, int limit) {
        List<LogRow> rows = restClient.get().uri(BASE + "/{id}/log?offset={o}&limit={l}", itemId, offset, limit).retrieve()
                .body(new ParameterizedTypeReference<List<LogRow>>() {
                });
        return rows != null ? rows : List.of();
    }

    public List<RprsRow> rprs(String locgov) {
        List<RprsRow> rows = restClient.get().uri(BASE + "/rprs?locgov={l}", locgov).retrieve()
                .body(new ParameterizedTypeReference<List<RprsRow>>() {
                });
        return rows != null ? rows : List.of();
    }

    public void saveRprs(String locgov, List<Long> itemIds, long userId) {
        restClient.post().uri(BASE + "/rprs").body(new RprsRequest(locgov, itemIds, userId)).retrieve().toBodilessEntity();
    }

    public List<OffRprsRow> offRprs(String locgov) {
        List<OffRprsRow> rows = restClient.get().uri(BASE + "/off-rprs?locgov={l}", locgov).retrieve()
                .body(new ParameterizedTypeReference<List<OffRprsRow>>() {
                });
        return rows != null ? rows : List.of();
    }

    public void saveOffRprs(String locgov, List<OffRprsInput> items, long userId) {
        restClient.post().uri(BASE + "/off-rprs").body(new OffRprsRequest(locgov, items, userId)).retrieve().toBodilessEntity();
    }

    /** 답례품 옵션 - 옵션이 없으면 null. 화면 JS가 읽는 모양 그대로(itemOptionType, itemOptionTitle1~3, itemOptionGroups[0].itemOptions[]). */
    public List<Map<String, Object>> optionList(long itemId) {
        return restClient.get().uri(BASE + "/option-list?itemId={id}", itemId).retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
    }

    public List<Map<String, String>> comboOptionList(Map<String, Object> optionMap) {
        List<Map<String, String>> rows = restClient.post().uri(BASE + "/combo-option-list").body(optionMap).retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, String>>>() {
                });
        return rows != null ? rows : List.of();
    }

    /** 오프라인 대표 답례품으로 저장해 둔 옵션 문자열 - 없으면 null. */
    public String selectedOption(long itemId) {
        Map<String, String> body = restClient.get().uri(BASE + "/selected-option?itemId={id}", itemId).retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });
        return body != null ? body.get("option") : null;
    }

    public List<CategoryOption> categoryOptions(int groupId) {
        List<CategoryOption> rows = restClient.get().uri(BASE + "/category-options?groupId={g}", groupId).retrieve()
                .body(new ParameterizedTypeReference<List<CategoryOption>>() {
                });
        return rows != null ? rows : List.of();
    }

    public List<CategoryOption> categoryChildren(String parent) {
        List<CategoryOption> rows = restClient.get().uri(BASE + "/category-children?parent={p}", parent).retrieve()
                .body(new ParameterizedTypeReference<List<CategoryOption>>() {
                });
        return rows != null ? rows : List.of();
    }
}

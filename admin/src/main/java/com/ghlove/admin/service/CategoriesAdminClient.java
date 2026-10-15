package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 카테고리관리(AS-IS opmanager/categories) - gift 서비스의 OP_CATEGORY 1~4차 트리 cross-service API 래퍼. */
@Component
public class CategoriesAdminClient {

    private final RestClient restClient;

    public CategoriesAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** AS-IS getCategoiesTreeListByLevel1 - 1차 카테고리(그룹 지정 시 그 그룹만). */
    public List<Map<String, Object>> treeLevel1(String groupId) {
        try {
            List<Map<String, Object>> list = restClient.get()
                    .uri(b -> b.path("/api/admin/categories/tree")
                            .queryParamIfPresent("groupId", java.util.Optional.ofNullable(
                                    groupId != null && !groupId.isBlank() ? groupId : null))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** AS-IS getCategoiesTreeListByLevels - 노드 id("코드-레벨-순서-하위최대레벨-그룹")의 하위. */
    public List<Map<String, Object>> treeChildren(String nodeId) {
        try {
            List<Map<String, Object>> list = restClient.get().uri("/api/admin/categories/tree/{id}", nodeId)
                    .retrieve().body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 없으면 null. */
    public Category byCode(String categoryCode) {
        try {
            return restClient.get().uri("/api/admin/categories/by-code/{c}", categoryCode)
                    .retrieve().body(Category.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    public long count(String categoryUrl, String categoryCode) {
        Map<String, Object> body = restClient.get()
                .uri(b -> b.path("/api/admin/categories/count")
                        .queryParamIfPresent("categoryUrl", java.util.Optional.ofNullable(categoryUrl))
                        .queryParamIfPresent("categoryCode", java.util.Optional.ofNullable(categoryCode))
                        .build())
                .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        Object c = body != null ? body.get("count") : null;
        return c instanceof Number n ? n.longValue() : 0L;
    }

    public void create(Form form) {
        restClient.post().uri("/api/admin/categories").body(form).retrieve().toBodilessEntity();
    }

    public void createSub(Form form) {
        restClient.post().uri("/api/admin/categories/sub").body(form).retrieve().toBodilessEntity();
    }

    public void update(Form form) {
        restClient.put().uri("/api/admin/categories").body(form).retrieve().toBodilessEntity();
    }

    public void move(Move move) {
        restClient.post().uri("/api/admin/categories/move").body(move).retrieve().toBodilessEntity();
    }

    public void delete(String categoryCode, String categoryLevel) {
        restClient.post()
                .uri("/api/admin/categories/delete?categoryCode={c}&categoryLevel={l}", categoryCode, categoryLevel)
                .retrieve().toBodilessEntity();
    }

    public record Seo(String title, String keywords, String description, String headerContents1,
                      String headerContents2, String headerContents3, String themawordTitle,
                      String themawordDescription) {
    }

    public record Category(Integer categoryId, String categoryCode, Integer categoryGroupId, String categoryUrl,
                           String categoryName, String categoryType, String categoryClass1, String categoryClass2,
                           String categoryClass3, String categoryClass4, String categoryLevel, Integer ordering,
                           String categoryFlag, String accessType, Seo categoriesSeo) {
    }

    public record Form(Integer categoryId, String categoryCode, Integer categoryGroupId, String categoryUrl,
                       String categoryName, String categoryLevel, String categoryFlag, String accessType,
                       String categoryClass1, String categoryClass2, String categoryClass3, String categoryClass4,
                       Seo categoriesSeo) {
    }

    public record Move(String categoryGroupId, String parentCode, String parentLevel, String parentGroupId,
                       String currentCode, String currentLevel, String previousCode, String previousLevel,
                       String previousOrdering) {
    }
}

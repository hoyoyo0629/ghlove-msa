package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 답례품 카테고리 관리 (AS-IS opmanager/categories - CategoriesManagerController) -
 *  gift 서비스의 GIFT_CATEGORY(대분류 공통코드)/GIFT_SUBCATEGORY(중분류)/GIFT_SUBCATEGORY_ITEM
 *  (개별품목) cross-service CRUD. 답례품몰 GNB "전체 카테고리" 메가메뉴가 실제로 읽는 구조라
 *  여기서 바꾸면 스토어프론트에 즉시 반영된다. */
@Component
public class CategoryAdminClient {

    private final RestClient restClient;

    public CategoryAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<MajorDto> tree() {
        try {
            List<MajorDto> list = restClient.get().uri("/api/admin/gift-categories/tree")
                    .retrieve().body(new ParameterizedTypeReference<List<MajorDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<MajorDto> majors() {
        try {
            List<MajorDto> list = restClient.get().uri("/api/admin/gift-categories/majors")
                    .retrieve().body(new ParameterizedTypeReference<List<MajorDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public MajorDto major(String id) {
        return restClient.get().uri("/api/admin/gift-categories/majors/{id}", id).retrieve().body(MajorDto.class);
    }

    public boolean majorExists(String id) {
        Map<?, ?> result = restClient.get().uri("/api/admin/gift-categories/majors/exists?id={id}", id)
                .retrieve().body(Map.class);
        return result != null && Boolean.TRUE.equals(result.get("exists"));
    }

    public void createMajor(MajorForm form) {
        restClient.post().uri("/api/admin/gift-categories/majors").body(form).retrieve().toBodilessEntity();
    }

    public void updateMajor(String id, MajorForm form) {
        restClient.put().uri("/api/admin/gift-categories/majors/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteMajor(String id) {
        try {
            restClient.post().uri("/api/admin/gift-categories/majors/{id}/delete", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "대분류 삭제에 실패했습니다."));
        }
    }

    public void moveMajor(String id, String direction) {
        restClient.post().uri("/api/admin/gift-categories/majors/{id}/move?direction={d}", id, direction)
                .retrieve().toBodilessEntity();
    }

    public SubcategoryDto subcategory(Long id) {
        return restClient.get().uri("/api/admin/gift-categories/subcategories/{id}", id).retrieve().body(SubcategoryDto.class);
    }

    public void createSubcategory(SubcategoryForm form) {
        restClient.post().uri("/api/admin/gift-categories/subcategories").body(form).retrieve().toBodilessEntity();
    }

    public void updateSubcategory(Long id, SubcategoryForm form) {
        restClient.put().uri("/api/admin/gift-categories/subcategories/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteSubcategory(Long id) {
        restClient.post().uri("/api/admin/gift-categories/subcategories/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void moveSubcategory(Long id, String direction) {
        restClient.post().uri("/api/admin/gift-categories/subcategories/{id}/move?direction={d}", id, direction)
                .retrieve().toBodilessEntity();
    }

    public ItemDto item(Long id) {
        return restClient.get().uri("/api/admin/gift-categories/items/{id}", id).retrieve().body(ItemDto.class);
    }

    public void createItem(ItemForm form) {
        restClient.post().uri("/api/admin/gift-categories/items").body(form).retrieve().toBodilessEntity();
    }

    public void updateItem(Long id, ItemForm form) {
        restClient.put().uri("/api/admin/gift-categories/items/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteItem(Long id) {
        restClient.post().uri("/api/admin/gift-categories/items/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void moveItem(Long id, String direction) {
        restClient.post().uri("/api/admin/gift-categories/items/{id}/move?direction={d}", id, direction)
                .retrieve().toBodilessEntity();
    }

    public record MajorDto(String id, String label, Integer ordering, String useYn, List<SubcategoryDto> subcategories) {
    }

    public record MajorForm(String id, String label, Integer ordering, String useYn) {
    }

    public record SubcategoryDto(Long subcategoryId, String categoryCode, String name, Integer ordering,
                                  String metaTitle, String metaKeywords, String metaDescription, List<ItemDto> items) {
    }

    public record SubcategoryForm(String categoryCode, String name, String metaTitle, String metaKeywords,
                                   String metaDescription) {
    }

    public record ItemDto(Long subcategoryItemId, Long subcategoryId, String name, Integer ordering) {
    }

    public record ItemForm(Long subcategoryId, String name) {
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
}

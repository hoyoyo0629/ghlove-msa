package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** admin이 gift 서비스의 스타일북 관리 API를 호출하는 클라이언트. */
@org.springframework.stereotype.Service
@Slf4j
public class StyleBookAdminClient {

    private final RestClient restClient;

    public StyleBookAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public record StyleBookRow(Long id, String title, String content, String image, Integer ordering) {
    }

    public record StyleBookItemRow(Long id, Long itemId, String itemName, Integer ordering) {
    }

    public List<StyleBookRow> list() {
        try {
            return restClient.get().uri("/api/admin/style-books").retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<StyleBookRow>>() {
                    });
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public StyleBookRow get(Long id) {
        return restClient.get().uri("/api/admin/style-books/{id}", id).retrieve().body(StyleBookRow.class);
    }

    public List<StyleBookItemRow> items(Long id) {
        try {
            return restClient.get().uri("/api/admin/style-books/{id}/items", id).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<StyleBookItemRow>>() {
                    });
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public void create(String title, String content, String image, Integer ordering) {
        restClient.post().uri("/api/admin/style-books")
                .body(Map.of("title", title, "content", content == null ? "" : content, "image", image == null ? "" : image, "ordering", ordering == null ? 0 : ordering))
                .retrieve().toBodilessEntity();
    }

    public void update(Long id, String title, String content, String image, Integer ordering) {
        restClient.put().uri("/api/admin/style-books/{id}", id)
                .body(Map.of("title", title, "content", content == null ? "" : content, "image", image == null ? "" : image, "ordering", ordering == null ? 0 : ordering))
                .retrieve().toBodilessEntity();
    }

    public void delete(Long id) {
        restClient.delete().uri("/api/admin/style-books/{id}", id).retrieve().toBodilessEntity();
    }

    public void addItem(Long styleBookId, Long itemId) {
        restClient.post().uri("/api/admin/style-books/{id}/items", styleBookId)
                .body(Map.of("itemId", itemId)).retrieve().toBodilessEntity();
    }

    public void removeItem(Long styleBookId, Long itemId) {
        restClient.delete().uri("/api/admin/style-books/{id}/items/{itemId}", styleBookId, itemId).retrieve().toBodilessEntity();
    }
}

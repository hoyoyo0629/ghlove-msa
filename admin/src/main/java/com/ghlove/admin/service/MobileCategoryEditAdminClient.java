package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** admin이 gift 서비스의 모바일 카테고리 편집 API(MobileCategoryEditAdminApiController)를
 *  호출하는 클라이언트. */
@org.springframework.stereotype.Service
public class MobileCategoryEditAdminClient {

    private final RestClient restClient;
    private final String giftServiceBaseUrl;

    public MobileCategoryEditAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
        this.giftServiceBaseUrl = giftServiceBaseUrl;
    }

    public record EditRow(Integer categoryEditId, String code, String editKind, String editPosition,
                           String editContent, String editImage, String editUrl,
                           String createdDate, String updatedDate) {
    }

    public String giftServiceBaseUrl() {
        return giftServiceBaseUrl;
    }

    public List<EditRow> list(String code) {
        try {
            List<EditRow> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/mobile-category-edit")
                            .queryParamIfPresent("code", java.util.Optional.ofNullable(blankToNull(code)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<EditRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public EditRow get(Integer id) {
        try {
            return restClient.get().uri("/api/admin/mobile-category-edit/{id}", id).retrieve().body(EditRow.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "편집 항목을 찾을 수 없습니다."));
        }
    }

    public void create(String code, String editKind, String editPosition, String editContent, String editUrl, MultipartFile editImage) {
        try {
            restClient.post().uri("/api/admin/mobile-category-edit")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(code, editKind, editPosition, editContent, editUrl, editImage))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "등록에 실패했습니다."));
        }
    }

    public void update(Integer id, String code, String editKind, String editPosition, String editContent, String editUrl, MultipartFile editImage) {
        try {
            restClient.put().uri("/api/admin/mobile-category-edit/{id}", id)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(code, editKind, editPosition, editContent, editUrl, editImage))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "수정에 실패했습니다."));
        }
    }

    public void delete(Integer id) {
        try {
            restClient.delete().uri("/api/admin/mobile-category-edit/{id}", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "삭제에 실패했습니다."));
        }
    }

    private static LinkedMultiValueMap<String, Object> toMultipart(String code, String editKind, String editPosition,
                                                                     String editContent, String editUrl, MultipartFile editImage) {
        var multipart = new LinkedMultiValueMap<String, Object>();
        multipart.add("code", code);
        multipart.add("editKind", editKind);
        multipart.add("editPosition", editPosition);
        if (editContent != null && !editContent.isBlank()) {
            multipart.add("editContent", editContent);
        }
        if (editUrl != null && !editUrl.isBlank()) {
            multipart.add("editUrl", editUrl);
        }
        if (editImage != null && !editImage.isEmpty()) {
            multipart.add("editImage", editImage.getResource());
        }
        return multipart;
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
}

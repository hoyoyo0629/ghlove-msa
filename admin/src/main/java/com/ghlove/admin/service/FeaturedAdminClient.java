package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** admin이 gift 서비스의 기획전/이벤트 관리 API(FeaturedAdminApiController)를 호출하는 클라이언트. */
@org.springframework.stereotype.Service
public class FeaturedAdminClient {

    private final RestClient restClient;
    private final String giftServiceBaseUrl;

    public FeaturedAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
        this.giftServiceBaseUrl = giftServiceBaseUrl;
    }

    public record FeaturedDto(Integer featuredId, Integer featuredClass, String featuredType, String featuredUrl,
                               String featuredCode, String featuredName, String featuredSimpleContent,
                               String featuredContent, String featuredImage, String thumbnailImage,
                               String featuredFlag, String link, String displayListFlag, Integer ordering,
                               String createdDate, String startDate, String endDate, String locgovCode) {
        public String imageUrl(String baseUrl) {
            return featuredImage != null ? baseUrl + "/uploads/" + featuredImage : null;
        }
    }

    public record FeaturedItemRow(Integer itemId, String itemName, Integer displayOrder) {
    }

    public String giftServiceBaseUrl() {
        return giftServiceBaseUrl;
    }

    public List<FeaturedDto> list(String featuredType, String featuredName) {
        try {
            List<FeaturedDto> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/featured")
                            .queryParam("featuredType", featuredType)
                            .queryParamIfPresent("featuredName", java.util.Optional.ofNullable(blankToNull(featuredName)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<FeaturedDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(Integer id) {
        try {
            return restClient.get().uri("/api/admin/featured/{id}", id).retrieve().body(Map.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기획전을 찾을 수 없습니다."));
        }
    }

    public void create(Map<String, Object> fields, MultipartFile image, MultipartFile thumbnailImage) {
        try {
            restClient.post().uri("/api/admin/featured")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(fields, image, thumbnailImage))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기획전 등록에 실패했습니다."));
        }
    }

    public void update(Integer id, Map<String, Object> fields, MultipartFile image, MultipartFile thumbnailImage) {
        try {
            restClient.put().uri("/api/admin/featured/{id}", id)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(fields, image, thumbnailImage))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기획전 수정에 실패했습니다."));
        }
    }

    public void delete(Integer id) {
        try {
            restClient.delete().uri("/api/admin/featured/{id}", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기획전 삭제에 실패했습니다."));
        }
    }

    public void replaceItems(Integer id, List<Long> itemIds) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        itemIds.forEach(i -> body.add("itemIds", String.valueOf(i)));
        try {
            restClient.put().uri("/api/admin/featured/{id}/items", id)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기획전 답례품 구성 저장에 실패했습니다."));
        }
    }

    private static MultiValueMap<String, Object> toMultipart(Map<String, Object> fields, MultipartFile image, MultipartFile thumbnailImage) {
        var multipart = new LinkedMultiValueMap<String, Object>();
        fields.forEach((k, v) -> {
            if (v != null && !(v instanceof String s && s.isBlank())) {
                multipart.add(k, v);
            }
        });
        if (image != null && !image.isEmpty()) {
            multipart.add("image", image.getResource());
        }
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            multipart.add("thumbnailImage", thumbnailImage.getResource());
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

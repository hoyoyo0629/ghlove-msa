package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 관리자 > 답례품관리 > 이벤트 관리(AS-IS opmanager/featured) - gift 서비스 FeaturedAdminApiController 호출. */
@org.springframework.stereotype.Service
public class FeaturedAdminClient {

    private static final String BASE = "/api/admin/featured";

    private final RestClient restClient;

    public FeaturedAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
                               @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** AS-IS FeaturedParam 중 목록이 쓰는 것. locgovCodes는 시도만 골랐을 때 그 시도에 속한 지자체 코드. */
    public record Search(String featuredType, String where, String query, String progression, String displayListFlag,
                         String locgovCode, String upperLocgovCode, List<String> locgovCodes) {
    }

    public record Row(Integer featuredId, String featuredName, String progression, String displayListFlag, String startDate,
                      String endDate, String createdDate, String locgovCode) {
    }

    public record Item(Long itemId, String itemUserCode, String itemName, Integer salePrice, String thumbnail) {
    }

    public record Detail(Integer featuredId, Integer featuredClass, String featuredType, String featuredUrl, String featuredName,
                         String featuredContent, String featuredImage, String featuredImageMobile, String featuredListImage,
                         String featuredFlag, String displayListFlag, String link, String prodState, String startDate,
                         String startTime, String endDate, String endTime, String featuredHost, String featuredPhoneNo1,
                         String featuredPhoneNo2, String featuredPhoneNo3, String locgovCode, String createdDate, List<Item> items,
                         List<String> userGroups) {
    }

    private record ListRequest(Search search, int offset, int limit) {
    }

    private record IdsRequest(List<Integer> ids) {
    }

    public int count(Search search) {
        Integer n = restClient.post().uri(BASE + "/count").contentType(MediaType.APPLICATION_JSON).body(search)
                .retrieve().body(Integer.class);
        return n == null ? 0 : n;
    }

    public List<Row> list(Search search, int offset, int limit) {
        List<Row> rows = restClient.post().uri(BASE + "/list").contentType(MediaType.APPLICATION_JSON)
                .body(new ListRequest(search, offset, limit)).retrieve().body(new ParameterizedTypeReference<List<Row>>() {
                });
        return rows == null ? List.of() : rows;
    }

    /** @return 없으면 null */
    public Detail detail(int featuredId) {
        try {
            return restClient.get().uri(BASE + "/{id}", featuredId).retrieve().body(Detail.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    /** 폼 칸 그대로 + 첨부 3개. 확장자 오류 등 업무 오류는 ManagerException(문구)으로 올린다. */
    public int create(Map<String, String> params, String locgovCode, MultipartFile featuredFile, MultipartFile featuredFileMobile,
                      MultipartFile featuredListFile) {
        MultiValueMap<String, Object> body = multipart(params, featuredFile, featuredFileMobile, featuredListFile);
        body.add("locgovCode", locgovCode);
        try {
            Map<String, Object> res = restClient.post().uri(BASE).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return res != null && res.get("featuredId") instanceof Number n ? n.intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(message(e));
        }
    }

    public void update(int featuredId, Map<String, String> params, MultipartFile featuredFile, MultipartFile featuredFileMobile,
                       MultipartFile featuredListFile) {
        try {
            restClient.post().uri(BASE + "/{id}", featuredId).contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipart(params, featuredFile, featuredFileMobile, featuredListFile)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(message(e));
        }
    }

    public boolean deleteImage(int featuredId, int fileType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("fileType", String.valueOf(fileType));
        Map<String, Object> res = restClient.post().uri(BASE + "/{id}/delete-image", featuredId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return res != null && Boolean.TRUE.equals(res.get("isSuccess"));
    }

    public void delete(List<Integer> featuredIds) {
        restClient.post().uri(BASE + "/delete").contentType(MediaType.APPLICATION_JSON).body(new IdsRequest(featuredIds))
                .retrieve().toBodilessEntity();
    }

    private static MultiValueMap<String, Object> multipart(Map<String, String> params, MultipartFile... files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach((k, v) -> {
            if (v != null) {
                body.add(k, v);
            }
        });
        String[] names = {"featuredFile", "featuredFileMobile", "featuredListFile"};
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null && files[i].getSize() > 0) {
                body.add(names[i], files[i].getResource());
            }
        }
        return body;
    }

    private static String message(RestClientResponseException e) {
        try {
            Map<?, ?> body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : "실패했습니다.";
        } catch (RuntimeException ex) {
            return "실패했습니다.";
        }
    }
}

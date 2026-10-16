package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 답례품제공자 관리(AS-IS opmanager/seller) - gift 서비스의 판매자(OP_SELLER)+계정(OP_SELLER_USER) cross-service API 래퍼. */
@Component
public class SellerManageClient {

    private static final String BASE = "/api/admin/seller-manage";

    private final RestClient restClient;

    public SellerManageClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record Search(String statusCode, String where, String query, String startDate, String endDate,
                         String itemApprovalType, String locgov) {
    }

    public record ListRow(Long sellerId, String loginId, String companyName, String representativeName, String phoneNumber,
                          String statusCode, String itemApprovalType, String createdDate, String locgovCode,
                          String mberDn, String mberFinDn, String mberCi) {
    }

    public int count(Search s) {
        Map<String, Object> vars = new java.util.LinkedHashMap<>();
        Map<String, Integer> body = restClient.get().uri(b -> {
            var u = b.path(BASE + "/count");
            searchParams(u, vars, s);
            return u.build(vars);
        }).retrieve().body(new ParameterizedTypeReference<Map<String, Integer>>() {
        });
        return body != null && body.get("count") != null ? body.get("count") : 0;
    }

    public List<ListRow> list(Search s, int offset, int limit) {
        Map<String, Object> vars = new java.util.LinkedHashMap<>();
        vars.put("offset", offset);
        vars.put("limit", limit);
        List<ListRow> rows = restClient.get().uri(b -> {
            var u = b.path(BASE + "/list").queryParam("offset", "{offset}").queryParam("limit", "{limit}");
            searchParams(u, vars, s);
            return u.build(vars);
        }).retrieve().body(new ParameterizedTypeReference<List<ListRow>>() {
        });
        return rows != null ? rows : List.of();
    }

    private static void searchParams(org.springframework.web.util.UriBuilder u, Map<String, Object> vars, Search s) {
        param(u, vars, "statusCode", s.statusCode());
        param(u, vars, "where", s.where());
        param(u, vars, "query", s.query());
        param(u, vars, "startDate", s.startDate());
        param(u, vars, "endDate", s.endDate());
        param(u, vars, "itemApprovalType", s.itemApprovalType());
        param(u, vars, "locgov", s.locgov());
    }

    /** 값은 URI 변수로 넘겨 &, +, 한글이 정확히 인코딩되게 한다. */
    private static void param(org.springframework.web.util.UriBuilder u, Map<String, Object> vars, String name, String value) {
        if (value != null && !value.isBlank()) {
            u.queryParam(name, "{" + name + "}");
            vars.put(name, value);
        }
    }

    /** 없으면 null. */
    public Map<String, Object> detail(long sellerId) {
        try {
            return restClient.get().uri(BASE + "/{id}", sellerId).retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    public String makeSellerId(String locgovCode) {
        Map<String, String> body = restClient.get().uri(b -> {
            var u = b.path(BASE + "/make-id");
            if (locgovCode != null && !locgovCode.isBlank()) {
                u.queryParam("locgovCode", locgovCode);
            }
            return u.build();
        }).retrieve().body(new ParameterizedTypeReference<Map<String, String>>() {
        });
        return body != null ? body.get("loginId") : null;
    }

    /** 폼 입력칸 이름 그대로 + 업로드 3개를 multipart로 보낸다(AS-IS 컨트롤러가 폼을 그대로 바인딩하는 것과 같다). */
    public long create(Map<String, String> params, MultipartFile[] files, Long createdUserId, String locgovCode, String passwordExpiredDate) {
        MultiValueMap<String, Object> body = multipart(params, files);
        body.add("createdUserId", String.valueOf(createdUserId));
        if (locgovCode != null) {
            body.add("locgovCode", locgovCode);
        }
        if (passwordExpiredDate != null) {
            body.add("passwordExpiredDate", passwordExpiredDate);
        }
        Map<String, Long> res = restClient.post().uri(BASE).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Long>>() {
                });
        return res != null && res.get("sellerId") != null ? res.get("sellerId") : 0L;
    }

    public void update(long sellerId, Map<String, String> params, MultipartFile[] files, Long updatedUserId) {
        MultiValueMap<String, Object> body = multipart(params, files);
        body.add("updatedUserId", String.valueOf(updatedUserId));
        restClient.post().uri(BASE + "/{id}", sellerId).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().toBodilessEntity();
    }

    public void initPassword(long sellerId, String password, Long updatedUserId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("password", password);
        form.add("updatedUserId", String.valueOf(updatedUserId));
        restClient.post().uri(BASE + "/{id}/init-password", sellerId).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().toBodilessEntity();
    }

    public void deleteFile(long sellerId, String fileType, Long updatedUserId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("fileType", fileType);
        form.add("updatedUserId", String.valueOf(updatedUserId));
        restClient.post().uri(BASE + "/{id}/delete-file", sellerId).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().toBodilessEntity();
    }

    private static MultiValueMap<String, Object> multipart(Map<String, String> params, MultipartFile[] files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach((k, v) -> body.add(k, v == null ? "" : v));
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i] != null && files[i].getSize() > 0) {
                body.add("uploadFile" + (i + 1), files[i].getResource());
            }
        }
        return body;
    }
}

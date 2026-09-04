package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * D11 회원등급 관리 (docs/as-is-admin-gap-deep-audit-part2.md 배치D D11) - member 서비스의
 * /api/admin/user-levels를 호출한다(MemberAdminClient와 동일한 패턴). 아이콘 이미지는 admin
 * 콘솔에서 받은 MultipartFile을 그대로 member 서비스로 멀티파트 전달한다.
 */
@Component
public class UserLevelAdminClient {

    private final RestClient restClient;

    public UserLevelAdminClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl) {
        this.restClient = RestClient.create(memberServiceBaseUrl);
    }

    public List<UserLevelDto> list() {
        try {
            List<UserLevelDto> result = restClient.get().uri("/api/admin/user-levels")
                    .retrieve().body(new ParameterizedTypeReference<List<UserLevelDto>>() {
                    });
            return result != null ? result : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public UserLevelDto get(Integer levelId) {
        try {
            return restClient.get().uri("/api/admin/user-levels/{id}", levelId).retrieve().body(UserLevelDto.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public void create(String levelName, Integer depth, Integer priceStart, Integer priceEnd, Double discountRate,
                        Double pointRate, Integer shippingCouponCount, Integer retentionPeriod,
                        Integer referencePeriod, Integer exceptReferencePeriod, MultipartFile icon) {
        try {
            restClient.post().uri("/api/admin/user-levels")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipartBody(levelName, depth, priceStart, priceEnd, discountRate, pointRate,
                            shippingCouponCount, retentionPeriod, referencePeriod, exceptReferencePeriod, icon))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "회원등급 등록에 실패했습니다."));
        }
    }

    public void update(Integer levelId, String levelName, Integer depth, Integer priceStart, Integer priceEnd,
                        Double discountRate, Double pointRate, Integer shippingCouponCount, Integer retentionPeriod,
                        Integer referencePeriod, Integer exceptReferencePeriod, MultipartFile icon) {
        try {
            restClient.post().uri("/api/admin/user-levels/{id}", levelId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipartBody(levelName, depth, priceStart, priceEnd, discountRate, pointRate,
                            shippingCouponCount, retentionPeriod, referencePeriod, exceptReferencePeriod, icon))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "회원등급 수정에 실패했습니다."));
        }
    }

    public void delete(Integer levelId) {
        try {
            restClient.delete().uri("/api/admin/user-levels/{id}", levelId).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "회원등급 삭제에 실패했습니다."));
        }
    }

    private MultiValueMap<String, Object> toMultipartBody(String levelName, Integer depth, Integer priceStart,
                                                            Integer priceEnd, Double discountRate, Double pointRate,
                                                            Integer shippingCouponCount, Integer retentionPeriod,
                                                            Integer referencePeriod, Integer exceptReferencePeriod,
                                                            MultipartFile icon) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("levelName", levelName);
        addIfPresent(body, "depth", depth);
        addIfPresent(body, "priceStart", priceStart);
        addIfPresent(body, "priceEnd", priceEnd);
        addIfPresent(body, "discountRate", discountRate);
        addIfPresent(body, "pointRate", pointRate);
        addIfPresent(body, "shippingCouponCount", shippingCouponCount);
        addIfPresent(body, "retentionPeriod", retentionPeriod);
        addIfPresent(body, "referencePeriod", referencePeriod);
        addIfPresent(body, "exceptReferencePeriod", exceptReferencePeriod);
        if (icon != null && !icon.isEmpty()) {
            try {
                ByteArrayResource resource = new ByteArrayResource(icon.getBytes()) {
                    @Override
                    public String getFilename() {
                        return icon.getOriginalFilename();
                    }
                };
                body.add("icon", resource);
            } catch (IOException e) {
                throw new ManagerException("아이콘 파일을 읽는 데 실패했습니다.");
            }
        }
        return body;
    }

    private static void addIfPresent(MultiValueMap<String, Object> body, String key, Object value) {
        if (value != null) {
            body.add(key, String.valueOf(value));
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

    public record UserLevelDto(Integer levelId, String groupCode, Integer depth, String levelName, String fileName,
                                Integer priceStart, Integer priceEnd, Double discountRate, Double pointRate,
                                Integer shippingCouponCount, Integer retentionPeriod, Integer referencePeriod,
                                Integer exceptReferencePeriod, String createdDate) {
    }
}

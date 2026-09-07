package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** admin이 donation 서비스의 행정복지센터 관리 API를 호출하는 클라이언트. */
@org.springframework.stereotype.Service
@Slf4j
public class WelfareCenterAdminClient {

    private final RestClient restClient;

    public WelfareCenterAdminClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public record Row(Long id, String lclgvCd, String name, String code, String useYn, String frstRegDt) {
    }

    public List<Row> list(String lclgvCd) {
        return restClient.get().uri(uriBuilder -> uriBuilder.path("/api/welfare-centers-admin")
                        .queryParamIfPresent("lclgvCd", java.util.Optional.ofNullable(lclgvCd != null && !lclgvCd.isBlank() ? lclgvCd : null))
                        .build())
                .retrieve().body(new ParameterizedTypeReference<List<Row>>() {
                });
    }

    public void create(String lclgvCd, String name, String code) {
        restClient.post().uri("/api/welfare-centers-admin")
                .body(Map.of("lclgvCd", lclgvCd == null ? "" : lclgvCd, "name", name, "code", code == null ? "" : code))
                .retrieve().toBodilessEntity();
    }

    public void update(Long id, String lclgvCd, String name, String code, String useYn) {
        restClient.put().uri("/api/welfare-centers-admin/{id}", id)
                .body(Map.of("lclgvCd", lclgvCd == null ? "" : lclgvCd, "name", name, "code", code == null ? "" : code, "useYn", useYn == null ? "Y" : useYn))
                .retrieve().toBodilessEntity();
    }

    public void delete(Long id) {
        restClient.delete().uri("/api/welfare-centers-admin/{id}", id).retrieve().toBodilessEntity();
    }
}

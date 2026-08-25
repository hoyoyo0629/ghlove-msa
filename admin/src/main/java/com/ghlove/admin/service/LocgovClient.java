package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 관리자권한요청 폼의 "광역지자체" 선택용 - donation 서비스의 point/order와 동일한 패턴
 *  (읽기 전용, 실패 시 조용히 빈 목록으로 대체). */
@Component
public class LocgovClient {

    private final RestClient restClient;

    public LocgovClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public List<LocgovInfo> allLocgovs() {
        try {
            List<LocgovInfo> locgovs = restClient.get()
                    .uri("/api/locgovs")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LocgovInfo>>() {
                    });
            return locgovs != null ? locgovs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record LocgovInfo(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }
}

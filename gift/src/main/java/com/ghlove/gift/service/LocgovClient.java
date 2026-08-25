package com.ghlove.gift.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 마이페이지 "관심답례품" 목록의 지자체명 표시용 - gift 서비스는 LOCGOV_CODE만 갖고 있고
 * 이름은 donation 서비스(G_LOCGOV)에만 있다. point/order의 LocgovClient와 동일한 패턴
 * (읽기 전용, 실패 시 조용히 빈 목록으로 대체).
 */
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

    /** locgovCode -> "상위지자체명 지자체명" 맵. 실패/미매칭 시 그 항목은 그냥 빠진다(호출부가 코드로 폴백). */
    public Map<String, String> namesByCode() {
        return allLocgovs().stream()
                .collect(Collectors.toMap(LocgovInfo::locgovCode,
                        l -> (l.upperLocgovNm() != null ? l.upperLocgovNm() + " " : "") + l.locgovNm(),
                        (a, b) -> a));
    }

    public record LocgovInfo(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }
}

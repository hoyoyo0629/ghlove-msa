package com.ghlove.point.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * 마이페이지 "기부포인트 조회" 지자체별 집계 화면의 시/도·시/군/구 이름 표시용 - point
 * 서비스는 LOCGOV_CODE만 갖고 있고 이름은 donation 서비스(G_LOCGOV)에만 있다. order의
 * LocgovClient와 동일한 패턴(읽기 전용, 실패 시 조용히 빈 목록으로 대체).
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

    /**
     * "기부포인트 조회" 화면 "기부처" 열 - 지자체별 연계기관 표시문구
     * (AS-IS `mypage-mapper.xml`의 `DETAIL`). 연계기관을 거치지 않은 지자체는 결과에 없고,
     * 화면은 그때 `고향사랑e음`을 쓴다.
     *
     * <p>표시용 부가정보라 donation 조회가 실패해도 화면을 죽이지 않고 빈 맵을 돌려준다 -
     * 그러면 모든 행이 `고향사랑e음`으로 보인다(연계 데이터가 없는 지금과 같은 모습).
     */
    public Map<String, String> donationSourcesOf(Long userId) {
        try {
            Map<String, String> sources = restClient.get()
                    .uri("/api/donation-sources?userId={userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            return sources != null ? sources : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    public record LocgovInfo(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }
}

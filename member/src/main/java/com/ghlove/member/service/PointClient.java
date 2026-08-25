package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 마이페이지 "기부포인트 조회" 카드에 보유 포인트를 보여주기 위한 읽기 전용 조회 -
 * donation의 DonationClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시
 * 마이페이지 자체가 깨지면 안 되므로 0으로 조용히 대체한다.
 */
@Component
@Slf4j
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl) {
        this.restClient = RestClient.create(pointServiceBaseUrl);
    }

    public long balanceOf(Long userId) {
        try {
            PointSummary summary = restClient.get()
                    .uri("/api/balance?userId={userId}", userId)
                    .retrieve()
                    .body(PointSummary.class);
            return summary != null ? summary.balance() : 0L;
        } catch (RestClientException e) {
            log.warn("Failed to fetch point balance from point service - showing 0", e);
            return 0L;
        }
    }

    /** 마이페이지 "회원탈퇴" 화면의 "잔여포인트" 표(지자체별, 잔여 있는 곳만) - AS-IS
     *  users/secede.html의 pointList. */
    public List<LocgovPointSummary> locgovSummaryOf(Long userId) {
        try {
            List<LocgovPointSummary> summary = restClient.get()
                    .uri("/api/locgov-point-summary?userId={userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LocgovPointSummary>>() {
                    });
            return summary != null ? summary : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch locgov point summary from point service - showing empty list", e);
            return List.of();
        }
    }

    private record PointSummary(long balance) {
    }

    public record LocgovPointSummary(String locgovCode, String upperLocgovNm, String locgovNm, long remaining) {
    }
}

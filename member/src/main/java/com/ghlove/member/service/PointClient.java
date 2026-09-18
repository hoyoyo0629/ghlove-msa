package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * 마이페이지 "기부포인트 조회" 카드에 보유 포인트를 보여주기 위한 읽기 전용 조회 -
 * donation의 DonationClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시
 * 마이페이지 자체가 깨지면 안 되므로 0으로 조용히 대체한다.
 */
@Component
@Slf4j
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl,
                       @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // point의 /api/admin/* (탈퇴 시 포인트 소멸)은 내부 전용이라 공유 시크릿을 실어야 통과한다.
        this.restClient = RestClient.builder()
                .baseUrl(pointServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret)
                .build();
    }

    /**
     * 탈퇴 시 잔여 기부포인트 전량 소멸 (AS-IS `GeneralCustomerServiceImpl:286~296` -
     * 탈퇴 처리 안에서 만료 배치와 같은 소멸을 돈다).
     *
     * <p>조회 실패를 0으로 삼키는 다른 메서드들과 달리 <b>여기서는 예외를 그대로 올린다</b> -
     * 소멸에 실패했는데 탈퇴만 진행되면 탈퇴 회원의 포인트가 원장에 살아남는다.
     *
     * @return 소멸된 총 포인트
     */
    public long expireAllOnWithdrawal(Long userId) {
        try {
            Map<String, Object> body = restClient.post()
                    .uri("/api/admin/expire-on-withdrawal?userId={userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            Object expired = body == null ? null : body.get("expiredPoints");
            return expired instanceof Number n ? n.longValue() : 0L;
        } catch (RestClientException e) {
            throw new MemberException("잔여 포인트 소멸 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
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

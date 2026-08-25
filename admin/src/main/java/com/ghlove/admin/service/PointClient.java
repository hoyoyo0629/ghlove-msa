package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/** 기부포인트 현황 (AS-IS opmanager/give/give-point) - point 서비스의 원장을 CNTR_SN 단위로
 *  조회한다(읽기 전용). 다른 client와 동일하게 실패 시 조용히 빈 값으로 대체. */
@Component
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl) {
        this.restClient = RestClient.create(pointServiceBaseUrl);
    }

    public List<EarnLot> earnedLots(List<String> cntrSns) {
        if (cntrSns == null || cntrSns.isEmpty()) {
            return List.of();
        }
        try {
            List<EarnLot> lots = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/ledger/earned-lots")
                            .queryParam("cntrSns", cntrSns).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EarnLot>>() {
                    });
            return lots != null ? lots : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record EarnLot(String cntrSn, Long earnedAmount, Long remainingAmount) {
    }

    /** 기부포인트 적립 백필용 - point의 기존 멱등 엔드포인트(REF_KEY+EARN 중복체크)를 그대로
     *  호출한다. 이미 정상 적립된 기부건은 자동으로 no-op(false 반환). 다른 client 메서드와
     *  달리 실패를 조용히 삼키지 않고 그대로 던진다 - 잔액에 영향을 주는 쓰기 작업이라
     *  "이미 적립됨"과 "호출 실패"를 호출부(ResyncService)가 반드시 구분해야 한다. */
    public boolean creditForDonation(String cntrSn, Long userId, String locgovCode, java.math.BigDecimal cntrAmt, String cntrDe) {
        Map<String, Object> result = restClient.post()
                .uri("/api/admin/credit-for-donation")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(new CreditRequest(cntrSn, userId, locgovCode, cntrAmt, cntrDe))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return result != null && Boolean.TRUE.equals(result.get("credited"));
    }

    public record CreditRequest(String cntrSn, Long userId, String locgovCode, java.math.BigDecimal cntrAmt, String cntrDe) {
    }

    /** StatsService ReadModel 재동기화용 - 포인트 원장 전체 스냅샷. */
    public List<LedgerSnapshot> allForResync() {
        try {
            List<LedgerSnapshot> ledger = restClient.get()
                    .uri("/api/admin/ledger/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LedgerSnapshot>>() {
                    });
            return ledger != null ? ledger : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record LedgerSnapshot(Long ledgerId, Long userId, String locgovCode, String txnType,
                                  Long pointAmount, java.time.LocalDateTime createdDate) {
    }

    /** "주문금액-포인트사용 대사"용 - 주문ID당 실제 차감된 포인트 절대값. */
    public Map<String, Long> usedByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<String, Long> used = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/ledger/used")
                            .queryParam("orderIds", orderIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Long>>() {
                    });
            return used != null ? used : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }
}

package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 기부포인트 현황 (AS-IS opmanager/give/give-point) - point 서비스의 원장을 CNTR_SN 단위로
 *  조회한다(읽기 전용). 다른 client와 동일하게 실패 시 조용히 빈 값으로 대체. */
@Component
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(pointServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
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

    /** 지자체관리 화면 "포인트 지급률 변경이력" 팝업(AS-IS locgov-point-list.jsp) - 연도별
     *  행 자체가 이력이다. */
    public List<LocgovPointRate> locgovPointRateHistory(String locgovCode) {
        try {
            List<LocgovPointRate> list = restClient.get()
                    .uri("/api/admin/locgov-point-rate?locgovCode={locgovCode}", locgovCode)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LocgovPointRate>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /**
     * 지자체관리(메뉴 4401) <b>목록</b>의 "포인트 지급률" 컬럼 - 해당 연도 지급률을 한 번에 받는다.
     * AS-IS는 목록 쿼리의 스칼라 서브쿼리({@code g_ctbny_setup})였는데 TO-BE는 지급률이
     * point 소유라 일괄 조회한다(행마다 호출하면 N+1).
     */
    public Map<String, java.math.BigDecimal> currentLocgovPointRates(String stdrYear, List<String> locgovCodes) {
        if (locgovCodes == null || locgovCodes.isEmpty()) {
            return Map.of();
        }
        try {
            Map<String, java.math.BigDecimal> rates = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/locgov-point-rates/current")
                            .queryParam("stdrYear", stdrYear)
                            .queryParam("locgovCodes", locgovCodes).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, java.math.BigDecimal>>() {
                    });
            return rates != null ? rates : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /** 지자체관리 등록/수정 화면 "포인트 지급률" 등록(AS-IS user/locgov/edit.jsp) - 연도+지자체
     *  단위 upsert. */
    public void upsertLocgovPointRate(String stdrYear, String locgovCode, java.math.BigDecimal pointRate,
                                       String managerName) {
        try {
            restClient.post().uri("/api/admin/locgov-point-rate")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of("stdrYear", stdrYear, "locgovCode", locgovCode, "pointRate", pointRate,
                            "managerName", managerName == null ? "" : managerName))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "포인트 지급률 저장에 실패했습니다."));
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

    public record LocgovPointRate(String stdrYear, String locgovCode, java.math.BigDecimal pointRate,
                                   java.time.LocalDateTime lastUpdtPnttm, String lastUpdusrNm) {
    }

    /** "주문금액-포인트사용 대사"용 - 주문ID당 실제 차감된 포인트 절대값. */
    /**
     * 포인트사용 정합성검증(메뉴 7210)의 "사용완료 포인트" - AS-IS pointcheck 쿼리의
     * G_CNTR_USE_POINT 파트를 point 서비스가 집계해 내려준다(조회 전용, 사용자 승인 후 추가
     * 2026-10-03). 키는 주문번호(멀티아이템의 {@code #항목번호} 접미는 point가 떼어 준다).
     */
    public Map<String, Long> cntrUsedPointsByOrderCode(String query) {
        try {
            List<UsedPointRow> rows = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/reconciliation/cntr-used-points")
                            .queryParamIfPresent("query",
                                    java.util.Optional.ofNullable(query == null || query.isBlank() ? null : query))
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UsedPointRow>>() {
                    });
            if (rows == null) {
                return Map.of();
            }
            Map<String, Long> result = new java.util.LinkedHashMap<>();
            rows.forEach(r -> result.put(r.orderCode(), r.usedPoint()));
            return result;
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    public record UsedPointRow(String orderCode, long usedPoint) {
    }

    /**
     * 회원별 포인트 잔액 일괄 조회 - 휴면회원관리(메뉴 4107)의 "포인트잔액" 컬럼용.
     * 목록의 userId를 한 번에 보낸다(행마다 호출하면 N+1).
     */
    public Map<Long, Long> balancesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<Long, Long> balances = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/point-balances")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<Long, Long>>() {
                    });
            return balances != null ? balances : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /* ---------- 일반회원관리(메뉴 4101)가 쓰는 회원별 포인트 사용 조회 ---------- */

    /**
     * 회원 한 명의 포인트 사용 행 전체 - 상세화면 포인트 내역의 USE 쪽.
     * donation의 적립(OCC) 행과 합쳐 등록시각 역순으로 세운다(AS-IS UNION ALL 재현).
     */
    public List<MemberPointUseRow> memberPointUseRows(Long userId) {
        try {
            List<MemberPointUseRow> rows = restClient.get()
                    .uri("/api/admin/member-points/{userId}/use-rows", userId)
                    .retrieve().body(new ParameterizedTypeReference<List<MemberPointUseRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 회원별 사용포인트(USE_SE_CODE='1') 합계 - 목록의 포인트잔액 = 발생 - 사용. */
    public Map<Long, Long> memberUsedPointTotals(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<Long, Long> totals = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/member-points/used-totals")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<Long, Long>>() {
                    });
            return totals != null ? totals : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /** 지자체명은 donation의 G_LOCGOV에 있어 코드만 온다 - admin이 LocgovClient로 붙인다. */
    public record MemberPointUseRow(Integer useSn, String pointUseDe, String cntrLocgovCode,
                                     Long cntrUsePoint, String orderCode, String useSeCode,
                                     String frstRegistPnttm) {
    }

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

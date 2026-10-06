package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** StatsService ReadModel 재동기화 전용 - donation 서비스의 기부 전체 현재 상태 스냅샷을
 *  읽기 전용으로 조회한다(SFR-009 "원장 복구·재동기화 절차"). */
@Component
public class DonationClient {

    private final RestClient restClient;

    public DonationClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /**
     * 회원별 기부누적액 일괄 조회 - 휴면회원관리(메뉴 4107)의 "기부누적액" 컬럼용.
     * 목록의 userId를 한 번에 보낸다(행마다 호출하면 N+1). 완료(COMPLETED) 기부만 합산된다.
     */
    public java.util.Map<Long, java.math.BigDecimal> donationTotalsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Map.of();
        }
        try {
            java.util.Map<Long, java.math.BigDecimal> totals = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/donation-totals")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<java.util.Map<Long, java.math.BigDecimal>>() {
                    });
            return totals != null ? totals : java.util.Map.of();
        } catch (RestClientException e) {
            return java.util.Map.of();
        }
    }

    /* ---------- 일반회원관리(메뉴 4101)가 쓰는 회원별 기부 조회 ---------- */

    /** 상세화면 누적합계의 기부금액·발생포인트. */
    public MemberCumulativeTotal memberCumulativeTotal(Long userId) {
        try {
            MemberCumulativeTotal total = restClient.get()
                    .uri("/api/admin/member-donations/{userId}/cumulative-total", userId)
                    .retrieve().body(MemberCumulativeTotal.class);
            return total != null ? total : new MemberCumulativeTotal(BigDecimal.ZERO, BigDecimal.ZERO);
        } catch (RestClientException e) {
            return new MemberCumulativeTotal(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    /** 상세화면 기부내역 목록(페이징). */
    public MemberCntrList memberCntrList(Long userId, int startRow, int size) {
        try {
            MemberCntrList result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/member-donations/{userId}/cntr-list")
                            .queryParam("startRow", startRow).queryParam("size", size).build(userId))
                    .retrieve().body(MemberCntrList.class);
            return result != null ? result : new MemberCntrList(0, List.of());
        } catch (RestClientException e) {
            return new MemberCntrList(0, List.of());
        }
    }

    /** 포인트 내역의 적립(OCC) 행 - point의 사용(USE) 행과 합쳐 쓴다. */
    public List<MemberPointOccRow> memberPointOccRows(Long userId) {
        try {
            List<MemberPointOccRow> rows = restClient.get()
                    .uri("/api/admin/member-donations/{userId}/point-occ-rows", userId)
                    .retrieve().body(new ParameterizedTypeReference<List<MemberPointOccRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 목록화면의 기부누적액·발생포인트 일괄 조회. */
    public java.util.Map<Long, MemberCumulativeTotal> memberDonationTotals(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Map.of();
        }
        try {
            java.util.Map<Long, MemberCumulativeTotal> totals = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/member-donations/totals")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<java.util.Map<Long, MemberCumulativeTotal>>() {
                    });
            return totals != null ? totals : java.util.Map.of();
        } catch (RestClientException e) {
            return java.util.Map.of();
        }
    }

    public record MemberCumulativeTotal(BigDecimal totalCntrAmt, BigDecimal totalCntrPoint) {
    }

    /** 기부형태·민간연계기관은 코드로 내려온다 - 라벨은 admin의 OP_COMMON_CODE에서 붙인다. */
    public record MemberCntrRow(String cntrDe, String cntrUpperLocgovNm, String cntrLocgovNm,
                                 String cntrPathCode, String linkInsttCd, BigDecimal cntrAmt,
                                 BigDecimal cntrPoint, String elctrnPayNo, String sttemntPayDe) {
    }

    public record MemberCntrList(int count, List<MemberCntrRow> content) {
    }

    public record MemberPointOccRow(String cntrDe, String cntrLocgovCode, String cntrUpperLocgovNm,
                                     String cntrLocgovNm, BigDecimal cntrAmt, BigDecimal cntrPoint,
                                     String frstRegistPnttm) {
    }

    public List<DonationSnapshot> allForResync() {
        try {
            List<DonationSnapshot> donations = restClient.get()
                    .uri("/api/admin/donations/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<DonationSnapshot>>() {
                    });
            return donations != null ? donations : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record DonationSnapshot(String cntrSn, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                                    String cntrDe, String cntrSttusCode, String cntrPathCode, String psitnLocgovCode) {
    }

    /** "연계 로그 관리"(AS-IS opmanager/log gif-stnd/gif-seoul buga·sunap 재구현)용. */
    public List<LevyLog> levyLogs() {
        try {
            List<LevyLog> logs = restClient.get()
                    .uri("/api/admin/donation-levy")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LevyLog>>() {
                    });
            return logs != null ? logs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record LevyLog(String cntrSn, String bugaNo, LocalDateTime bugaDate, String sunapYn,
                           LocalDateTime sunapDate, String ntsStatus, String ntsReceiptNo,
                           LocalDateTime ntsRegisteredDate, String ntsErrorMessage) {
    }

    /** completeDonation()에 부과/수납 연계가 추가되기 전 COMPLETED된 기부 건의
     *  DONATION_LEVY 누락 이력을 채운다 - 실제 잔액/상태를 바꾸지 않는 조회이력 보정이라
     *  실패해도 예외를 그대로 올려 호출부(컨트롤러)가 사용자에게 실패를 보여줄 수 있게 한다. */
    public BackfillResult backfillLevy() {
        return restClient.post()
                .uri("/api/admin/donation-levy/backfill")
                .retrieve()
                .body(BackfillResult.class);
    }

    public record BackfillResult(int filled, int alreadyOk, int failed) {
    }

    /** "연계 로그 관리" 화면의 시도 이력(성공/실패 모두, G_RELAY_LOG) 조회. */
    public List<RelayLog> relayLogs() {
        try {
            List<RelayLog> logs = restClient.get()
                    .uri("/api/admin/relay-log")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RelayLog>>() {
                    });
            return logs != null ? logs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record RelayLog(Integer relayLogId, Long userId, String relayType, String cntrLocgovCode,
                            String cntrSn, String relayResultCode, LocalDateTime frstRegistPnttm) {
    }
}

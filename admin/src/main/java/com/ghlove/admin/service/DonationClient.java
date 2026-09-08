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

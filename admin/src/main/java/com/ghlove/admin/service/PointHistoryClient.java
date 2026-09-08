package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 포인트 사용내역/일별 현황 화면(AS-IS opmanager/point - PointManagerController, B2)용 -
 *  point 서비스의 PT_POINT_LEDGER 전체 트랜잭션을 cross-locgov로 읽어온다(읽기 전용, 실패 시
 *  조용히 빈 값으로 대체 - 다른 Client와 동일 패턴). 건별(CNTR_SN) 발생-사용-잔액을 다루는
 *  기존 {@link PointClient}(GivePointController가 씀)와는 목적이 달라 별도 파일로 분리했다. */
@Component
public class PointHistoryClient {

    private final RestClient restClient;

    public PointHistoryClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(pointServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<HistoryRow> history(Long userId, String locgovCode, String txnType, String fromDate, String toDate) {
        try {
            List<HistoryRow> rows = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/point-history")
                            .queryParamIfPresent("userId", Optional.ofNullable(userId))
                            .queryParamIfPresent("locgovCode", Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("txnType", Optional.ofNullable(blankToNull(txnType)))
                            .queryParamIfPresent("fromDate", Optional.ofNullable(blankToNull(fromDate)))
                            .queryParamIfPresent("toDate", Optional.ofNullable(blankToNull(toDate)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<HistoryRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<DailyStat> dailyStats(String fromDate, String toDate, String locgovCode) {
        try {
            List<DailyStat> rows = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/point-daily-stats")
                            .queryParamIfPresent("fromDate", Optional.ofNullable(blankToNull(fromDate)))
                            .queryParamIfPresent("toDate", Optional.ofNullable(blankToNull(toDate)))
                            .queryParamIfPresent("locgovCode", Optional.ofNullable(blankToNull(locgovCode)))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<DailyStat>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public record HistoryRow(Long ledgerId, Long userId, String locgovCode, String txnType, Long pointAmount,
                              String reason, String refKey, LocalDateTime createdDate,
                              Long remainingAmount, String expirationDate) {
    }

    public record DailyStat(String date, long earnCount, long earnAmount,
                             long useCount, long useAmount, long expireCount, long expireAmount) {
    }
}

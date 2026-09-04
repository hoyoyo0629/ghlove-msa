package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 관심 지자체 통계 화면(AS-IS opmanager/statistics - StatisticsLocgovManagerController)용 -
 *  donation 서비스의 G_INTRST_LOCGOV 원본 등록행을 월별/지자체별로 집계한 결과를 읽어온다
 *  (읽기 전용, 실패 시 조용히 빈 값으로 대체 - 다른 Client와 동일 패턴). */
@Component
public class StatisticsLocgovClient {

    private final RestClient restClient;

    public StatisticsLocgovClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public List<String> years() {
        try {
            List<String> years = restClient.get().uri("/api/admin/interest-locgov-stats/years")
                    .retrieve().body(List.class);
            return years != null ? years : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public ListResult search(String year, String locgovNm) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/interest-locgov-stats")
                            .queryParamIfPresent("year", java.util.Optional.ofNullable(blankToNull(year)))
                            .queryParamIfPresent("locgovNm", java.util.Optional.ofNullable(blankToNull(locgovNm)))
                            .build())
                    .retrieve().body(ListResult.class);
        } catch (RestClientException e) {
            return new ListResult(0, List.of(), List.of());
        }
    }

    public DetailResult detail(String locgovCode, String year) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/interest-locgov-stats/{locgovCode}")
                            .queryParamIfPresent("year", java.util.Optional.ofNullable(blankToNull(year)))
                            .build(locgovCode))
                    .retrieve().body(DetailResult.class);
        } catch (RestClientException e) {
            return new DetailResult(locgovCode, locgovCode, null, 0, List.of());
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public record LocgovRow(String locgovCode, String locgovNm, String upperLocgovNm, long count) {
    }

    public record MonthlyStat(String yearMonth, long count) {
    }

    public record ListResult(long total, List<LocgovRow> byLocgov, List<MonthlyStat> monthly) {
    }

    public record DetailResult(String locgovCode, String locgovNm, String upperLocgovNm, long total,
                                List<MonthlyStat> monthly) {
    }
}

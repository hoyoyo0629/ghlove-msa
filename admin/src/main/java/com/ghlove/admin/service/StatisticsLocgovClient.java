package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 관심 지자체 통계 화면(AS-IS opmanager/statistics/locgov - StatisticsLocgovManagerController /
 * statistics-locgov-mapper.xml)용 - donation 서비스의 G_INTRST_LOCGOV 원본 등록행을
 * 지자체별/월별로 집계한 결과를 읽어온다(읽기 전용, 실패 시 조용히 빈 값으로 대체 -
 * 다른 Client와 동일 패턴).
 */
@Component
public class StatisticsLocgovClient {

    private final RestClient restClient;

    public StatisticsLocgovClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** AS-IS getLocgovLikeList + getLocgovLikeListTotal. */
    public ListResult search(String shLocgovCode, int offset, int limit) {
        try {
            ListResult r = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/interest-locgov-stats")
                            .queryParamIfPresent("shLocgovCode", java.util.Optional.ofNullable(blankToNull(shLocgovCode)))
                            .queryParam("offset", offset)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve().body(ListResult.class);
            return r != null ? r : new ListResult(0, List.of());
        } catch (RestClientException e) {
            return new ListResult(0, List.of());
        }
    }

    /** AS-IS getLikeCntByLocgov. */
    public DetailTotal totalOf(String locgovCode, String year) {
        try {
            DetailTotal r = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/interest-locgov-stats/{locgovCode}/total")
                            .queryParamIfPresent("year", java.util.Optional.ofNullable(blankToNull(year)))
                            .build(locgovCode))
                    .retrieve().body(DetailTotal.class);
            return r != null ? r : DetailTotal.empty(locgovCode);
        } catch (RestClientException e) {
            return DetailTotal.empty(locgovCode);
        }
    }

    /** AS-IS statisticsLocgovLikeByMonth(월 1~12 모두 채워서 온다). */
    public List<MonthRow> monthlyOf(String locgovCode, String year) {
        try {
            List<MonthRow> r = restClient.get()
                    .uri("/api/admin/interest-locgov-stats/{locgovCode}/{year}/month", locgovCode, year)
                    .retrieve().body(new org.springframework.core.ParameterizedTypeReference<List<MonthRow>>() {
                    });
            return r != null ? r : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public record LocgovRow(String locgovCode, String locgovNm, String upperLocgovNm, long count) {
    }

    public record ListResult(int total, List<LocgovRow> rows) {
    }

    /** JSON {"total":{"total":n,"yearTotal":n}, "locgovNm":.., "upperLocgovNm":..} 모양 그대로 받는다. */
    public record DetailTotal(Total total, String locgovNm, String upperLocgovNm) {
        public record Total(long total, long yearTotal) {
        }

        static DetailTotal empty(String locgovCode) {
            return new DetailTotal(new Total(0, 0), locgovCode, null);
        }
    }

    public record MonthRow(int month, long count) {
    }
}

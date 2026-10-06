package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 기부 통계(AS-IS opmanager/give/statistics) - 실제 데이터는 donation(G_CNTR)에 있고 이 클라이언트가
 *  cross-service API를 호출한다(전체 집계). CtbnyOpratnClient와 동일한 조회대행 패턴. */
@Component
public class GiveStatClient {

    private final RestClient restClient;

    public GiveStatClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public AllSummary allSummary(String year, String locgovCode) {
        try {
            AllSummary s = restClient.get()
                    .uri(uri -> uri.path("/api/give-statistics/admin/all/summary")
                            .queryParam("year", nz(year)).queryParam("locgovCode", nz(locgovCode)).build())
                    .retrieve().body(AllSummary.class);
            return s != null ? s : new AllSummary(0, 0, 0);
        } catch (RestClientException e) {
            return new AllSummary(0, 0, 0);
        }
    }

    public List<MonthRow> allByMonth(String year, String locgovCode) {
        try {
            List<MonthRow> list = restClient.get()
                    .uri(uri -> uri.path("/api/give-statistics/admin/all/{year}/month")
                            .queryParam("locgovCode", nz(locgovCode)).build(nz(year)))
                    .retrieve().body(new ParameterizedTypeReference<List<MonthRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<HourRow> allByHour(String date, String locgovCode) {
        try {
            List<HourRow> list = restClient.get()
                    .uri(uri -> uri.path("/api/give-statistics/admin/all/{date}/hour")
                            .queryParam("locgovCode", nz(locgovCode)).build(nz(date)))
                    .retrieve().body(new ParameterizedTypeReference<List<HourRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<LocgovYearRow> locgovList(String year, String locgovCode) {
        try {
            List<LocgovYearRow> list = restClient.get()
                    .uri(uri -> uri.path("/api/give-statistics/admin/locgov/list")
                            .queryParam("year", nz(year)).queryParam("locgovCode", nz(locgovCode)).build())
                    .retrieve().body(new ParameterizedTypeReference<List<LocgovYearRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public record AllSummary(long cntrCnt, long cntrPerson, long cntrAmt) {
    }

    public record LocgovYearRow(int cntrYear, String upperLocgovCode, String upperLocgovNm, String locgovCode,
                                String locgovNm, long cntrAmt, long givePersons, long giveCnt) {
    }

    public record MonthRow(int cntrMonth, long cntrCnt, long cntrPerson, long cntrAmt, long cntrOnline, long cntrOffline) {
    }

    public record HourRow(int cntrHour, long cntrAmt) {
    }
}

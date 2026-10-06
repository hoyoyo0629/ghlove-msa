package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * 연계 로그 조회 (메뉴 1413~1416) - donation 소유 표를 조회 전용으로 읽는다.
 * AS-IS는 운영관리에서 직접 쿼리했지만 MSA에서는 donation의
 * {@code /api/admin/levy-logs/*}를 거친다. 실패 시 다른 client와 동일하게 빈 목록.
 */
@Component
public class LevyLogClient {

    private final RestClient restClient;

    public LevyLogClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
                         @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** 1413 서울세외 부과연계 로그 - AS-IS 화면 11컬럼. */
    public record SeoulBugaRow(String enapbuNo, String siguCd, String semokCd, String taxYm,
                               String taxGubun, String sidoCd, String napGubun, Long taxAmt,
                               String errorCd, String errorMsg, String ifStDt) {
    }

    public List<SeoulBugaRow> seoulBuga(String srchTxt, String srchErrorCd,
                                        String startDate, String endDate) {
        return get("/api/admin/levy-logs/seoul-buga", srchTxt, srchErrorCd, startDate, endDate,
                new ParameterizedTypeReference<List<SeoulBugaRow>>() {
                });
    }

    /** 1414 서울 수납연계 로그 - AS-IS 화면 9컬럼. */
    public record SeoulSunapRow(String epayNo, String comReqMeche, String orgC, String sunapYn,
                                Long sunapAmt, String sunapDt, String rstCd, String rstMsg,
                                String ifStDt) {
    }

    public List<SeoulSunapRow> seoulSunap(String srchTxt, String srchRstCd,
                                          String startDate, String endDate) {
        return get("/api/admin/levy-logs/seoul-sunap", srchTxt, srchRstCd, startDate, endDate,
                new ParameterizedTypeReference<List<SeoulSunapRow>>() {
                });
    }

    /** 1415 지방세외 부과연계 로그 - AS-IS 화면 34컬럼. */
    public record StndBugaRow(String upperLocgovNm, String locgovNm, String linkMngKey, String sgbCd,
                              String linkTrgtCd, String dptCd, String spclFisBizCd, String fyr,
                              String actSeCd, String rprsTxmCd, String operItemCd, String lvyYmd,
                              String frstPctAmt, String frstPidYmd, String pyrSeCd, String pyrNo,
                              String pyrNm, String rprsPyrNo, String rprsPyrNm, String pyrSttCd,
                              String lotnoRoadAddrSeCd, String zip, String roadNmCd, String bmno,
                              String bsno, String stdgCd, String dongCd, String roadNmDaddr,
                              String glNm, String mngItemCn1, String bugaStatusCd, String linkRstCd,
                              String linkRstMsg, String frstRegistPnttm) {
    }

    public List<StndBugaRow> stndBuga(String startDate, String endDate, String bugaStatusCd,
                                      String linkRstCd, String epayNo, String srchlinkRstYn,
                                      String srchpyrNm) {
        try {
            List<StndBugaRow> rows = restClient.get()
                    .uri(b -> b.path("/api/admin/levy-logs/stnd-buga")
                            .queryParamIfPresent("startDate", opt(startDate))
                            .queryParamIfPresent("endDate", opt(endDate))
                            .queryParamIfPresent("bugaStatusCd", opt(bugaStatusCd))
                            .queryParamIfPresent("linkRstCd", opt(linkRstCd))
                            .queryParamIfPresent("epayNo", opt(epayNo))
                            .queryParamIfPresent("srchlinkRstYn", opt(srchlinkRstYn))
                            .queryParamIfPresent("srchpyrNm", opt(srchpyrNm))
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StndBugaRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 1416 지방세외 수납연계 로그 - AS-IS 화면 37컬럼. */
    public record StndSunapRow(String linkMngKey, String sgbCd, String sgbNm, String taxnNo,
                               String untyTaxnNo, String dptCd, String dptNm, String spclFisBizCd,
                               String spclFisBizNm, String fyr, String actSeCd, String actSeNm,
                               String rprsTxmCd, String rprsTxmNm, String operItemCd, String operItemNm,
                               String lvyNo, String itmNo, String epayNo, String rcvmtNo,
                               String rcvmtSeCd, String rcvmtSeNm, String rcvmtYmd, String actYmd,
                               String tsfYmd, String rcvmtPctAmt, String rcvmtAdtnAmt,
                               String rcvmtIntrAmt, String bankNm, String rcvmtTyCd, String rcvmtTy,
                               String rsveItem1, String rsveItem2, String rsveItem3, String rsveItem4,
                               String rsveItem5, String frstRegistPnttm) {
    }

    public List<StndSunapRow> stndSunap(String startDate, String endDate, String epayNo) {
        try {
            List<StndSunapRow> rows = restClient.get()
                    .uri(b -> b.path("/api/admin/levy-logs/stnd-sunap")
                            .queryParamIfPresent("startDate", opt(startDate))
                            .queryParamIfPresent("endDate", opt(endDate))
                            .queryParamIfPresent("epayNo", opt(epayNo))
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StndSunapRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 서울 두 화면은 파라미터 모양이 같아(검색어 + 결과여부 + 등록일 범위) 한 곳에 모았다. */
    private <T> List<T> get(String path, String srchTxt, String resultCode,
                            String startDate, String endDate, ParameterizedTypeReference<List<T>> type) {
        try {
            List<T> rows = restClient.get()
                    .uri(b -> b.path(path)
                            .queryParamIfPresent("srchTxt", opt(srchTxt))
                            .queryParamIfPresent(path.endsWith("seoul-buga") ? "srchErrorCd" : "srchRstCd",
                                    opt(resultCode))
                            .queryParamIfPresent("startDate", opt(startDate))
                            .queryParamIfPresent("endDate", opt(endDate))
                            .build())
                    .retrieve()
                    .body(type);
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static Optional<String> opt(String value) {
        return Optional.ofNullable(value == null || value.isBlank() ? null : value);
    }
}

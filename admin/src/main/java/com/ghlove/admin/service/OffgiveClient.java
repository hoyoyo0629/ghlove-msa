package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 오프라인기부 접수 관리 (AS-IS opmanager/offgive) - 실제 데이터/도메인효과는 donation
 *  서비스에 있고(OffgiveClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만 담당한다. */
@Component
public class OffgiveClient {

    private final RestClient restClient;

    public OffgiveClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<Donation> list() {
        try {
            List<Donation> list = restClient.get().uri("/api/offgive").retrieve()
                    .body(new ParameterizedTypeReference<List<Donation>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** AS-IS offgive/list.jsp 검색조건(지자체/접수일자 범위) 재현. */
    /**
     * 기부금 접수관리 목록(메뉴 15101) - AS-IS 검색조건 전체. 이름은 회원(member) 소유라
     * USER_ID만 오고 admin이 채운다.
     */
    public List<AdminRow> adminSearch(String shKeyword, String shText, String startDate, String endDate,
                                       java.math.BigDecimal amountFrom, java.math.BigDecimal amountTo,
                                       String cntrSttusCode, String rceptBankCode, String rceptBankNm) {
        try {
            List<AdminRow> rows = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/admin/offgive/search");
                        addIfPresent(uriBuilder, "shKeyword", shKeyword);
                        addIfPresent(uriBuilder, "shText", shText);
                        addIfPresent(uriBuilder, "startDate", startDate);
                        addIfPresent(uriBuilder, "endDate", endDate);
                        if (amountFrom != null) {
                            uriBuilder.queryParam("amountFrom", amountFrom);
                        }
                        if (amountTo != null) {
                            uriBuilder.queryParam("amountTo", amountTo);
                        }
                        addIfPresent(uriBuilder, "cntrSttusCode", cntrSttusCode);
                        addIfPresent(uriBuilder, "rceptBankCode", rceptBankCode);
                        addIfPresent(uriBuilder, "rceptBankNm", rceptBankNm);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<AdminRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (org.springframework.web.client.RestClientException e) {
            return List.of();
        }
    }

    private static void addIfPresent(org.springframework.web.util.UriBuilder uriBuilder, String name,
                                      String value) {
        if (value != null && !value.isBlank()) {
            uriBuilder.queryParam(name, value);
        }
    }

    /** AS-IS 목록 11컬럼의 원본 - 기부상태는 TO-BE 낱말, 이름은 USER_ID로만 온다. */
    public record AdminRow(String cntrSn, String cntrSttusCode, String elctrnPayNo, String sttemntPayDe,
                            String cntrLocgovCode, String upperLocgovNm, String locgovNm, Long userId,
                            BigDecimal cntrAmt, String rceptBankCode, String rceptBankNm,
                            String frstRegistPnttm, String prjSubject, Long cntrPoint,
                            String rtnpsntReqstCode) {
    }

    public List<Donation> search(String locgovCode, String startDate, String endDate) {
        try {
            List<Donation> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/offgive/search")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Donation>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public Donation get(String cntrSn) {
        try {
            return restClient.get().uri("/api/offgive/{cntrSn}", cntrSn).retrieve().body(Donation.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부내역을 찾을 수 없습니다."));
        }
    }

    public RegisterResult register(Long userId, String walkInName, String walkInPhone, String walkInBirthday,
                                    String walkInAddress, String locgovCode, BigDecimal amount,
                                    String rceptBankCode, String rceptBankNm, String signatureImage) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (userId != null) {
            body.add("userId", String.valueOf(userId));
        }
        if (walkInName != null) {
            body.add("walkInName", walkInName);
        }
        if (walkInPhone != null) {
            body.add("walkInPhone", walkInPhone);
        }
        if (walkInBirthday != null) {
            body.add("walkInBirthday", walkInBirthday);
        }
        if (walkInAddress != null) {
            body.add("walkInAddress", walkInAddress);
        }
        body.add("locgovCode", locgovCode);
        body.add("amount", amount.toPlainString());
        if (rceptBankCode != null) {
            body.add("rceptBankCode", rceptBankCode);
        }
        if (rceptBankNm != null) {
            body.add("rceptBankNm", rceptBankNm);
        }
        if (signatureImage != null && !signatureImage.isBlank()) {
            body.add("signatureImage", signatureImage);
        }
        try {
            return restClient.post().uri("/api/offgive")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().body(RegisterResult.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "오프라인 기부 등록에 실패했습니다."));
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

    public record Donation(String cntrSn, String cntrDe, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                            String cntrSttusCode, String cntrPathCode, String rceptBankCode, String rceptBankNm,
                            String frstRegistPnttm, String signatureFileNm) {
    }

    public record WalkIn(Long userId, String loginId, String tempPassword) {
    }

    public record RegisterResult(Donation donation, WalkIn walkIn) {
    }
}

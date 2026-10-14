package com.ghlove.admin.service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 납부(세외수입) 부과요청 연계 proxy - 기탁서 등록(15102) 화면이 최종 등록 <b>전에</b>
 * 먼저 호출해 전자납부번호를 받아오는 자리(AS-IS {@code nextBugaRequest}/
 * {@code getSdonationCharge}, form.jsp {@code goContry2()}/{@code goSeoul()}). 브라우저는
 * donation 서비스를 직접 호출할 수 없어 admin이 donation의 이미 만들어진
 * {@code LevyRelayAdminApiController}(/api/admin/levy/*)로 중계한다.
 */
@Component
public class LevyRelayClient {

    private final RestClient restClient;

    public LevyRelayClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
                            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record BugaResponse(String epayNo, String linkRstCd, String linkRstMsg) {
    }

    /**
     * 지방세외 차세대 부과요청 - 화면 값(AS-IS NextBugaRequestDto 바인딩분)을 그대로 넘기고, donation이 돌려준
     * 연계 응답(키 전부 + epayNo)을 그대로 돌려준다. 특정사업 검증 실패는 {@code errorMessage}로 담는다
     * (AS-IS 컨트롤러가 UserException을 resultMap.put("errorMessage", ...)로 받는 자리).
     */
    public Map<String, Object> nextBuga(Map<String, String> form) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        form.forEach((k, v) -> {
            if (v != null) {
                body.add(k, v);
            }
        });
        body.set("cntrPathCode", "200");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post().uri("/api/admin/levy/next-buga")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body).retrieve().body(Map.class);
            return response != null ? response : new java.util.HashMap<>();
        } catch (RestClientResponseException e) {
            Map<String, Object> result = new java.util.HashMap<>();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> err = e.getResponseBodyAs(Map.class);
                if (err != null && err.get("message") != null) {
                    result.put("errorMessage", err.get("message").toString());
                }
            } catch (RuntimeException ignore) {
                // 응답 본문을 못 읽으면 빈 결과 - 화면은 "지방세외 시스템 연결에 실패하였습니다."
            }
            return result;
        }
    }

    public BugaResponse seoulBuga(String locgovCode, String napId, String napNm, BigDecimal amount,
                                   String systemCd, String semokCd, String taxGubun, String sidoCd,
                                   String napGubun, String resideStatus, String mulGubun, String mulNm,
                                   String sysGubun) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("locgovCode", locgovCode);
        addIfPresent(body, "napId", napId);
        addIfPresent(body, "napNm", napNm);
        body.add("amount", amount.toPlainString());
        addIfPresent(body, "systemCd", systemCd);
        addIfPresent(body, "semokCd", semokCd);
        addIfPresent(body, "taxGubun", taxGubun);
        addIfPresent(body, "sidoCd", sidoCd);
        addIfPresent(body, "napGubun", napGubun);
        addIfPresent(body, "resideStatus", resideStatus);
        addIfPresent(body, "mulGubun", mulGubun);
        addIfPresent(body, "mulNm", mulNm);
        addIfPresent(body, "sysGubun", sysGubun);
        return post("/api/admin/levy/seoul-buga", body);
    }

    private BugaResponse post(String uri, MultiValueMap<String, String> body) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post().uri(uri)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body).retrieve().body(Map.class);
            return new BugaResponse(str(response, "epayNo"), str(response, "linkRstCd"), str(response, "linkRstMsg"));
        } catch (RestClientResponseException e) {
            return new BugaResponse(null, null, "지방세외/서울세외 연계 호출에 실패했습니다.");
        }
    }

    private static String str(Map<String, Object> response, String key) {
        Object v = response != null ? response.get(key) : null;
        return v != null ? v.toString() : null;
    }

    private static void addIfPresent(MultiValueMap<String, String> body, String key, String value) {
        if (value != null && !value.isBlank()) {
            body.add(key, value);
        }
    }
}

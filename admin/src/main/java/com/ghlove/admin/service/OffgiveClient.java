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

    public OffgiveClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
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

    public Donation get(String cntrSn) {
        try {
            return restClient.get().uri("/api/offgive/{cntrSn}", cntrSn).retrieve().body(Donation.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부내역을 찾을 수 없습니다."));
        }
    }

    public RegisterResult register(Long userId, String walkInName, String walkInPhone, String walkInBirthday,
                                    String walkInAddress, String locgovCode, BigDecimal amount,
                                    String rceptBankCode, String rceptBankNm) {
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
                            String frstRegistPnttm) {
    }

    public record WalkIn(Long userId, String loginId, String tempPassword) {
    }

    public record RegisterResult(Donation donation, WalkIn walkIn) {
    }
}

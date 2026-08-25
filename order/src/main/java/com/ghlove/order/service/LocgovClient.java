package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 장바구니 화면의 지자체명 표시용 - point/gift 서비스는 LOCGOV_CODE만 갖고 있고 이름은
 * donation 서비스(G_LOCGOV)에만 있다. 읽기 전용 조회이므로 fault-isolated sync 호출로
 * 처리하고, 실패 시 코드 자체를 이름으로 대신 보여준다 (member의 DonationClient 등과 동일 패턴).
 */
@Component
public class LocgovClient {

    private final RestClient restClient;

    public LocgovClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public String nameOf(String locgovCode) {
        try {
            LocgovNameInfo info = restClient.get()
                    .uri("/api/locgovs/{code}", locgovCode)
                    .retrieve()
                    .body(LocgovNameInfo.class);
            return info != null ? info.displayName() : locgovCode;
        } catch (RestClientException e) {
            return locgovCode;
        }
    }

    /** "지자체몰 선택하기" 지도 팝업의 시/도별 시/군/구 그리드용 - 실패 시 빈 목록. */
    public List<LocgovInfo> allLocgovs() {
        try {
            List<LocgovInfo> locgovs = restClient.get()
                    .uri("/api/locgovs")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LocgovInfo>>() {
                    });
            return locgovs != null ? locgovs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }
}

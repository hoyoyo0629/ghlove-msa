package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

/** admin이 donation 서비스의 기부금영수증 국세청연계 로그 API를 호출하는 클라이언트. */
@org.springframework.stereotype.Service
@Slf4j
public class NtsReceiptLogClient {

    private final RestClient restClient;

    public NtsReceiptLogClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record Row(Long logSn, String cntrSn, String logType, String requestDate,
                       String responseCode, String responseMessage, String processStatus) {
    }

    public List<Row> list(String logType) {
        return restClient.get().uri(uriBuilder -> uriBuilder.path("/api/nts-receipt-logs")
                        .queryParam("logType", logType).build())
                .retrieve().body(new ParameterizedTypeReference<List<Row>>() {
                });
    }
}

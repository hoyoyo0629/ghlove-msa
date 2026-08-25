package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 스마트택배(sweettracker) 배송조회 연계 (AS-IS SmartDeliveryServiceImpl,
 * SmartDeliveryBatchServiceImpl). SFR-006이 "택배사 시스템과의 연계는 운영관리
 * 서비스가 담당"이라 명시하므로 order가 아니라 여기(admin)에 둔다 - order는
 * 배송 상태값만 갖고, 실시간 조회는 운영자가 이 화면에서 확인한다.
 * enabled=false(방화벽 미개방)면 모크 추적정보를 돌려준다.
 */
@Component
@Slf4j
public class SmartDeliveryClient {

    private final boolean enabled;
    private final RestClient restClient;
    private final String apiKey;

    public SmartDeliveryClient(
            @Value("${ghlove.integrations.smart-delivery.enabled}") boolean enabled,
            @Value("${ghlove.integrations.smart-delivery.api-url}") String apiUrl,
            @Value("${ghlove.integrations.smart-delivery.api-key}") String apiKey) {
        this.enabled = enabled;
        this.restClient = RestClient.create(apiUrl);
        this.apiKey = apiKey;
    }

    public DeliveryTrackingResult track(String carrierCode, String invoiceNo) {
        if (!enabled) {
            log.info("[smart-delivery] disabled - mock 배송조회 carrier={} invoice={}", carrierCode, invoiceNo);
            return DeliveryTrackingResult.mock(carrierCode, invoiceNo);
        }
        try {
            return restClient.get()
                    .uri("/api/v1/trackingInfo?t_key={key}&t_code={carrier}&t_invoice={invoice}",
                            apiKey, carrierCode, invoiceNo)
                    .retrieve().body(DeliveryTrackingResult.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("배송 조회에 실패했습니다: " + e.getMessage());
        }
    }
}

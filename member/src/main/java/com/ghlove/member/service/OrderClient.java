package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 마이페이지 "주문조회"/"취소반품교환" 카드에 건수를 보여주기 위한 읽기 전용 조회 -
 * donation의 DonationClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시
 * 마이페이지 자체가 깨지면 안 되므로 0건으로 조용히 대체한다.
 */
@Component
@Slf4j
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl) {
        this.restClient = RestClient.create(orderServiceBaseUrl);
    }

    public OrderSummaryInfo mySummary(Long userId) {
        try {
            OrderSummaryInfo summary = restClient.get()
                    .uri("/api/my-summary?userId={userId}", userId)
                    .retrieve()
                    .body(OrderSummaryInfo.class);
            return summary != null ? summary : new OrderSummaryInfo(0, 0, 0);
        } catch (RestClientException e) {
            log.warn("Failed to fetch order summary from order service - showing zeros", e);
            return new OrderSummaryInfo(0, 0, 0);
        }
    }
}

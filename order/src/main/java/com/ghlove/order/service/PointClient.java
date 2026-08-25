package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 장바구니 화면의 "지자체별 잔여 포인트"/주문가능여부 표시용 - 실제 차감은 SAGA(order.saga)를
 * 통해 비동기로 이뤄지므로 여기서는 화면 표시/사전 안내 목적의 읽기 전용 조회만 한다.
 */
@Component
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl) {
        this.restClient = RestClient.create(pointServiceBaseUrl);
    }

    public long balanceByLocgov(Long userId, String locgovCode) {
        try {
            PointBalanceInfo info = restClient.get()
                    .uri("/api/balance-by-locgov?userId={userId}&locgovCode={locgovCode}", userId, locgovCode)
                    .retrieve()
                    .body(PointBalanceInfo.class);
            return info != null ? info.balance() : 0L;
        } catch (RestClientException e) {
            return 0L;
        }
    }
}

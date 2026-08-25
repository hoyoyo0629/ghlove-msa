package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Read-only lookup of a gift's current price/stock, used only to enrich the
 * order-creation request with accurate data - NOT part of the SAGA write
 * path (reserving stock happens asynchronously via order.saga events, see
 * OrderSagaListener/OrderSagaPublisher). A synchronous GET here is a
 * deliberate, minimal exception to "avoid sync inter-service calls".
 */
@Component
public class GiftClient {

    private final RestClient restClient;

    public GiftClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public GiftItemInfo fetch(Long itemId) {
        try {
            return restClient.get()
                    .uri("/api/gifts/{id}", itemId)
                    .retrieve()
                    .body(GiftItemInfo.class);
        } catch (RestClientException e) {
            throw new OrderException("답례품 정보를 조회할 수 없습니다. (itemId=" + itemId + ")");
        }
    }
}

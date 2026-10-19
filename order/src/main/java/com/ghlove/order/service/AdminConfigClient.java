package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** 배치 기준일(OP_CONFIG, admin 소유) 조회 - AS-IS 배치의 {@code configMapper.getShopConfig(SHOP_CONFIG_ID)} 대응. */
@Component
public class AdminConfigClient {

    private final RestClient restClient;

    public AdminConfigClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl,
                             @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(adminServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** 사이트 설정 > 배송 화면 값(일 수, 문자열 그대로). */
    public record BatchDays(String shippingCompleteDate, String confirmPurchaseDate, String smartDeliveryCriteriaDate) {
    }

    public BatchDays batchDays() {
        return restClient.get().uri("/api/admin/shop-config/batch-days").retrieve().body(BatchDays.class);
    }
}

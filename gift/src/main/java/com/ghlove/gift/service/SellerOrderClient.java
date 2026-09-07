package com.ghlove.gift.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * 판매자 셀프포털(SellerPortalController)의 "주문현황" 섹션이 order 서비스의 관리자 전용 API
 * (/api/admin/orders?searchType=SELLER_ID)를 호출한다 - admin 서비스의 OrderAdminClient와
 * 동일한 패턴(공유시크릿 X-Internal-Secret 헤더)이다. 이 호출은 판매자 자신의 sellerId로만
 * 필터링하므로 관리자 신원 헤더(X-Manager-*)는 싣지 않는다 - order 쪽 검색 API는 그 헤더
 * 없이도 동작한다(지자체 스코프 검증이 필요한 처리성 API에서만 그 헤더를 쓴다).
 */
@Component
public class SellerOrderClient {

    private static final String HEADER_SECRET = "X-Internal-Secret";

    private final RestClient restClient;
    private final String adminSecret;

    public SellerOrderClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl,
                              @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.create(orderServiceBaseUrl);
        this.adminSecret = adminSecret;
    }

    /** 이 판매자의 최근 주문 목록 (최신순 20건 - 대시보드 요약용이라 페이징은 필요치 않다). */
    public List<OrderSummary> ordersOf(Long sellerId) {
        try {
            PageResponse<OrderSummary> result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/orders")
                            .queryParam("searchType", "SELLER_ID")
                            .queryParam("keyword", sellerId)
                            .queryParam("page", 0)
                            .queryParam("size", 20)
                            .build())
                    .header(HEADER_SECRET, adminSecret)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<OrderSummary>>() {
                    });
            return result != null ? result.content() : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public record OrderSummary(String orderId, Long userId, Long itemId, String itemName, Integer quantity,
                                Long pointAmount, String orderStatus, String deliveryStatus, String createdDate) {
    }

    private record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
    }
}

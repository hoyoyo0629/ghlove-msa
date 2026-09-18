package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 마이페이지 주문 목록 화면(주문조회/취소반품교환)이 필요로 하는 표시용 값을 한 번에 모아준다.
 * 답례품 썸네일은 gift, 지자체명은 donation에 있어서 화면마다 따로 호출하면 N+1이 되므로
 * itemId/locgovCode를 모아 한 번씩만 조회한 뒤 행에 붙인다(CartService가 장바구니에서 쓰는
 * 것과 같은 방식).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRowSupport {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final GiftClient giftClient;
    private final LocgovClient locgovClient;

    @Value("${ghlove.gift-service.base-url}")
    private String giftServiceBaseUrl;

    public List<MyOrderRow> rowsOf(List<Order> orders, Map<String, String> statusLabels) {
        return rowsOf(orders, statusLabels, Map.of(), Map.of(), Map.of());
    }

    /** 취소반품교환 목록용 - 주문 행에 클레임 유형/상태를 얹는다. */
    public List<MyOrderRow> rowsOf(List<Order> orders, Map<String, String> statusLabels,
                                    Map<String, String> claimTypeByOrder, Map<String, String> claimStatusByOrder,
                                    Map<String, Map<String, String>> claimLabels) {
        Map<String, String> locgovNames = locgovNames();
        Map<String, String> thumbnails = thumbnailsOf(orders);
        Map<String, String> typeLabels = claimLabels.getOrDefault("type", Map.of());
        Map<String, String> statusLabelsForClaim = claimLabels.getOrDefault("status", Map.of());

        return orders.stream().map(o -> {
            String claimType = claimTypeByOrder.get(o.getOrderId());
            String claimStatus = claimStatusByOrder.get(o.getOrderId());
            return new MyOrderRow(
                    o.getOrderId(),
                    o.getCreatedDate() != null ? o.getCreatedDate().format(DATE) : "",
                    o.getItemName(),
                    thumbnails.get(o.getOrderId()),
                    o.getLocgovCode(),
                    locgovNames.getOrDefault(o.getLocgovCode(), o.getLocgovCode()),
                    o.getQuantity() != null ? o.getQuantity() : 0,
                    o.getPointAmount() != null ? o.getPointAmount() : 0L,
                    o.getOrderStatus(),
                    statusLabels.getOrDefault(o.getOrderStatus(), o.getOrderStatus()),
                    o.getCarrierCode(),
                    o.getInvoiceNo(),
                    o.getCancelReason(),
                    claimType, claimType != null ? typeLabels.getOrDefault(claimType, claimType) : null,
                    claimStatus, claimStatus != null ? statusLabelsForClaim.getOrDefault(claimStatus, claimStatus) : null);
        }).toList();
    }

    /** 답례품 썸네일 - 같은 itemId를 여러 주문이 공유할 수 있어 itemId 단위로 한 번만 조회한다. */
    private Map<String, String> thumbnailsOf(List<Order> orders) {
        Map<Long, String> byItemId = new LinkedHashMap<>();
        orders.stream().map(Order::getItemId).filter(java.util.Objects::nonNull).distinct().forEach(itemId -> {
            try {
                GiftItemInfo gift = giftClient.fetch(itemId);
                if (gift != null && gift.thumbnailPath() != null) {
                    byItemId.put(itemId, giftServiceBaseUrl + gift.thumbnailPath());
                }
            } catch (RuntimeException e) {
                // 답례품이 폐지됐거나 gift 서비스가 내려가 있어도 목록 자체는 보여야 한다.
                log.debug("Thumbnail lookup failed for item {}: {}", itemId, e.getMessage());
            }
        });
        Map<String, String> byOrderId = new LinkedHashMap<>();
        orders.forEach(o -> {
            String url = o.getItemId() != null ? byItemId.get(o.getItemId()) : null;
            if (url != null) {
                byOrderId.put(o.getOrderId(), url);
            }
        });
        return byOrderId;
    }

    public Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        try {
            locgovClient.allLocgovs().forEach(l -> names.put(l.locgovCode(), l.locgovNm()));
        } catch (RuntimeException e) {
            log.debug("Locgov list lookup failed: {}", e.getMessage());
        }
        return names;
    }

    public List<LocgovInfo> allLocgovs() {
        try {
            return locgovClient.allLocgovs();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    /** 시·도 선택 목록 (AS-IS wdrList). */
    public Map<String, String> provinces() {
        Map<String, String> provinces = new LinkedHashMap<>();
        allLocgovs().forEach(l -> provinces.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm()));
        return provinces;
    }
}

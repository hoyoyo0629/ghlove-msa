package com.ghlove.gift.event;

import java.util.List;

/**
 * order 서비스 {@code SHIPMENT_CREATED} 계약의 gift 로컬 사본 - gift가 필요한 필드만 담는다
 * (Spring Boot 기본 Jackson은 unknown 프로퍼티를 무시하므로 subset으로 충분).
 * order/src/main/java/com/ghlove/order/event/ShipmentCreatedEvent.java 와 구조적으로 동기화 유지.
 */
public record ShipmentCreatedEvent(
        Long shipmentId,
        String orderId,
        List<Line> lines
) {
    public record Line(Long orderItemId, Long itemId, Long itemOptionId, Integer quantity) {
    }
}

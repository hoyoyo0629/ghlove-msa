package com.ghlove.gift.event;

import java.time.LocalDateTime;

/**
 * Local copy of order service's event contract (SFR-008 이벤트 스키마).
 * Must be kept structurally in sync with
 * order/src/main/java/com/ghlove/order/event/OrderCreatedEvent.java.
 */
public record OrderCreatedEvent(
        String orderId,
        Long userId,
        Long itemId,
        Long sellerId,
        Integer quantity,
        Integer unitPrice,
        Long pointAmount,
        String locgovCode,
        LocalDateTime occurredAt
) {
}

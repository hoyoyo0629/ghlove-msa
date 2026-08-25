package com.ghlove.admin.event;

import java.time.LocalDateTime;

/** Local copy of order service's event contract - must stay in sync with order/src/.../event/OrderCreatedEvent.java. */
public record OrderCreatedEvent(
        String orderId, Long userId, Long itemId, Long sellerId, Integer quantity,
        Integer unitPrice, Long pointAmount, LocalDateTime occurredAt
) {
}

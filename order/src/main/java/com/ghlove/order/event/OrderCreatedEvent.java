package com.ghlove.order.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        String orderId,
        Long userId,
        Long itemId,
        Long sellerId,
        Integer quantity,
        Integer unitPrice,
        Long pointAmount,
        String locgovCode,
        String itemName,
        String receiverName,
        LocalDateTime occurredAt
) {
}

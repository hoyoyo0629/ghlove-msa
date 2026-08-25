package com.ghlove.order.event;

import java.time.LocalDateTime;

public record StockReserveFailedEvent(String orderId, Long itemId, String reason, LocalDateTime occurredAt) {
}

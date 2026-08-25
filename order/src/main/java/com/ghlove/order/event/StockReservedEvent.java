package com.ghlove.order.event;

import java.time.LocalDateTime;

public record StockReservedEvent(String orderId, Long itemId, Integer quantity, LocalDateTime occurredAt) {
}

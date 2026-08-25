package com.ghlove.gift.event;

import java.time.LocalDateTime;

public record StockReserveFailedEvent(String orderId, Long itemId, String reason, LocalDateTime occurredAt) {
}

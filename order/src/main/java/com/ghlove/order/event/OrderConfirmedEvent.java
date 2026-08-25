package com.ghlove.order.event;

import java.time.LocalDateTime;

public record OrderConfirmedEvent(String orderId, LocalDateTime occurredAt) {
}

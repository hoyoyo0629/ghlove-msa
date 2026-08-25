package com.ghlove.admin.event;

import java.time.LocalDateTime;

public record OrderConfirmedEvent(String orderId, LocalDateTime occurredAt) {
}

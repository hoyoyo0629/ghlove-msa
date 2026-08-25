package com.ghlove.admin.event;

import java.time.LocalDateTime;

public record OrderCancelledEvent(String orderId, String reason, LocalDateTime occurredAt) {
}

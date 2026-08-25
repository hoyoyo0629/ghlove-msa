package com.ghlove.point.event;

import java.time.LocalDateTime;

public record PointDeductFailedEvent(String orderId, Long userId, String reason, LocalDateTime occurredAt) {
}

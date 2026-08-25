package com.ghlove.point.event;

import java.time.LocalDateTime;

public record PointDeductedEvent(String orderId, Long userId, Long pointAmount, LocalDateTime occurredAt) {
}

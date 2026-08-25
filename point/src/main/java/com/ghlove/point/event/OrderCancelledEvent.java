package com.ghlove.point.event;

import java.time.LocalDateTime;

/** Local copy of order service's event contract - see OrderCreatedEvent's note. */
public record OrderCancelledEvent(String orderId, String reason, LocalDateTime occurredAt) {
}

package com.ghlove.admin.event;

import java.time.LocalDateTime;

/** Local copy of order service's event contract - must stay in sync with order/src/.../event/OrderDeliveryUpdatedEvent.java. */
public record OrderDeliveryUpdatedEvent(String orderId, String deliveryStatus, LocalDateTime shippedDate,
                                          LocalDateTime deliveredDate, LocalDateTime confirmedDate,
                                          LocalDateTime occurredAt) {
}

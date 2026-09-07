package com.ghlove.admin.event;

import java.time.LocalDateTime;

/** Local copy of order service's event contract - see order/.../event/OrderClaimUpdatedEvent.java. */
public record OrderClaimUpdatedEvent(
        Long claimId, String orderId, String claimType, String reason, String status,
        LocalDateTime createdDate, LocalDateTime processedDate, String itemName, String locgovCode,
        Long userId, String receiverName
) {
}

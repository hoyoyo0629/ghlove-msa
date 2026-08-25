package com.ghlove.point.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Local copy of donation service's event contract (SFR-008 이벤트 스키마).
 * Must be kept structurally in sync with
 * donation/src/main/java/com/ghlove/donation/event/DonationCancelledEvent.java.
 */
public record DonationCancelledEvent(
        String cntrSn,
        Long userId,
        String cntrLocgovCode,
        BigDecimal cntrAmt,
        LocalDateTime occurredAt
) {
}

package com.ghlove.point.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Local copy of donation service's event contract (SFR-008 이벤트 스키마).
 * Services don't share Java classes across the deployment boundary - this
 * must be kept structurally in sync with
 * donation/src/main/java/com/ghlove/donation/event/DonationCompletedEvent.java.
 */
public record DonationCompletedEvent(
        String cntrSn,
        Long userId,
        String cntrLocgovCode,
        BigDecimal cntrAmt,
        String cntrDe,
        LocalDateTime occurredAt
) {
}

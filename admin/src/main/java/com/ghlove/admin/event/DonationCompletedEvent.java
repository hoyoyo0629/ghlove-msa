package com.ghlove.admin.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Local copy of donation service's event contract - must stay in sync with donation/src/.../event/DonationCompletedEvent.java. */
public record DonationCompletedEvent(
        String cntrSn, Long userId, String cntrLocgovCode, BigDecimal cntrAmt, String cntrDe, LocalDateTime occurredAt,
        String cntrPathCode, String psitnLocgovCode
) {
}

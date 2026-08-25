package com.ghlove.admin.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Local copy of donation service's event contract - must stay in sync with donation/src/.../event/DonationCancelledEvent.java. */
public record DonationCancelledEvent(String cntrSn, Long userId, String cntrLocgovCode, BigDecimal cntrAmt, LocalDateTime occurredAt) {
}

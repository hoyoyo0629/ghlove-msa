package com.ghlove.donation.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published when a previously-COMPLETED donation is cancelled/refunded, so the
 * point service can run its compensating transaction (포인트 복원/조정, SAGA 보상
 * 트랜잭션 - SFR-004/SFR-008). Not published when cancelling a donation that was
 * still REQUESTED, since no completion event (and therefore no point accrual)
 * was ever sent for it.
 */
public record DonationCancelledEvent(
        String cntrSn,
        Long userId,
        String cntrLocgovCode,
        BigDecimal cntrAmt,
        LocalDateTime occurredAt
) {
}

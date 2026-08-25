package com.ghlove.donation.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published to Kafka when a donation is completed (SFR-003 이벤트 기반 연계).
 * The point service will eventually subscribe to this to trigger 기부포인트 적립
 * (SFR-004: 기부금액의 30%를 기준으로 지자체별 적립 규칙 적용) - no consumer exists
 * yet since the point service hasn't been built, so this is publish-only for now.
 */
public record DonationCompletedEvent(
        String cntrSn,
        Long userId,
        String cntrLocgovCode,
        BigDecimal cntrAmt,
        String cntrDe,
        LocalDateTime occurredAt,
        String cntrPathCode,
        String psitnLocgovCode
) {
}

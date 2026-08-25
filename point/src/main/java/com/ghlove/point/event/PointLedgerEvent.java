package com.ghlove.point.event;

import java.time.LocalDateTime;

/** 원장(PT_POINT_LEDGER) 행이 생길 때마다 발행 - admin의 포인트 통계 ReadModel용 (SFR-007/009). */
public record PointLedgerEvent(Long ledgerId, Long userId, String locgovCode, String txnType,
                                Long pointAmount, LocalDateTime createdDate) {
}

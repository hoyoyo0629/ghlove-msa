package com.ghlove.admin.event;

import java.time.LocalDateTime;

/** Local copy of point service's event contract - must stay in sync with point/src/.../event/PointLedgerEvent.java. */
public record PointLedgerEvent(Long ledgerId, Long userId, String locgovCode, String txnType,
                                Long pointAmount, LocalDateTime createdDate) {
}

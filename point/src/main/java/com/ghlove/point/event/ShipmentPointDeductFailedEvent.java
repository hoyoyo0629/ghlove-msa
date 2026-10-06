package com.ghlove.point.event;

import java.time.LocalDateTime;

/** point→order: 출고(지자체) 단위 포인트 차감 실패(포인트 부족). order 동명 레코드와 구조 동기화. */
public record ShipmentPointDeductFailedEvent(Long shipmentId, String orderId, String reason, LocalDateTime occurredAt) {
}

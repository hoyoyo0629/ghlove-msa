package com.ghlove.gift.event;

import java.time.LocalDateTime;

/** gift→order: 출고 내 품목 재고 예약 실패(하나라도 부족). order의 동명 레코드와 구조 동기화. */
public record ShipmentStockReserveFailedEvent(Long shipmentId, String orderId, String reason, LocalDateTime occurredAt) {
}

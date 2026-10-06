package com.ghlove.gift.event;

import java.time.LocalDateTime;

/** gift→order: 출고 내 모든 품목 재고 예약 성공. order의 동명 레코드와 구조 동기화. */
public record ShipmentStockReservedEvent(Long shipmentId, String orderId, LocalDateTime occurredAt) {
}

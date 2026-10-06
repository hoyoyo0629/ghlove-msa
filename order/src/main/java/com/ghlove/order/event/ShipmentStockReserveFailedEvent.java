package com.ghlove.order.event;

import java.time.LocalDateTime;

/** gift가 출고 내 품목 재고 예약에 실패(하나라도 부족)했음을 알린다. key=orderId. */
public record ShipmentStockReserveFailedEvent(Long shipmentId, String orderId, String reason, LocalDateTime occurredAt) {
}

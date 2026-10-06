package com.ghlove.order.event;

import java.time.LocalDateTime;

/** gift가 출고 내 모든 품목 재고 예약에 성공했음을 알린다(출고 단위 SAGA). key=orderId. */
public record ShipmentStockReservedEvent(Long shipmentId, String orderId, LocalDateTime occurredAt) {
}

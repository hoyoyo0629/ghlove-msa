package com.ghlove.order.event;

import java.time.LocalDateTime;

/** point가 출고(지자체) 단위 포인트 차감에 성공했음을 알린다. key=orderId. */
public record ShipmentPointDeductedEvent(Long shipmentId, String orderId, LocalDateTime occurredAt) {
}

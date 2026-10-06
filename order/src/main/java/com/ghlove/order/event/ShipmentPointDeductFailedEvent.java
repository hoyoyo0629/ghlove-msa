package com.ghlove.order.event;

import java.time.LocalDateTime;

/** point가 출고(지자체) 단위 포인트 차감에 실패(포인트 부족)했음을 알린다. key=orderId. */
public record ShipmentPointDeductFailedEvent(Long shipmentId, String orderId, String reason, LocalDateTime occurredAt) {
}

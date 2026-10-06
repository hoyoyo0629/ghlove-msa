package com.ghlove.gift.event;

/**
 * order 서비스 {@code SHIPMENT_CANCELLED} 계약의 gift 로컬 사본(subset). gift는 shipmentId만으로
 * 그 출고의 예약 재고를 복원한다(orderItemIds 등 나머지 필드는 무시).
 */
public record ShipmentCancelledEvent(Long shipmentId, String orderId) {
}

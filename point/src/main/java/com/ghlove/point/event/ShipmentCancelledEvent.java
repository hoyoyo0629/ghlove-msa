package com.ghlove.point.event;

import java.util.List;

/** order 서비스 {@code SHIPMENT_CANCELLED} 계약의 point 로컬 사본(subset). 그 출고의 품목
 *  차감분을 모두 복원하기 위해 orderItemIds를 함께 받는다. */
public record ShipmentCancelledEvent(Long shipmentId, String orderId, List<Long> orderItemIds) {
}

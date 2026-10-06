package com.ghlove.point.event;

/** order 서비스 {@code ITEM_CANCELLED}(품목 단위 부분취소/반품)의 point 로컬 사본(subset).
 *  (orderId, orderItemId)로 그 품목 차감분만 복원한다. */
public record ItemCancelledEvent(Long orderItemId, Long shipmentId, String orderId) {
}

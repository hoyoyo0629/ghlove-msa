package com.ghlove.gift.event;

/** order 서비스 {@code ITEM_CANCELLED}(품목 단위 부분취소/반품)의 gift 로컬 사본(subset).
 *  orderItemId로 그 품목 예약 재고만 복원한다. */
public record ItemCancelledEvent(Long orderItemId, Long shipmentId, String orderId) {
}

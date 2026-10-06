package com.ghlove.order.event;

import java.time.LocalDateTime;

/**
 * 품목 단위 취소/보상 이벤트. 사용자의 품목 부분취소 또는 반품/교환 처리완료로 <b>한 품목만</b>
 * 되돌릴 때 발행해 gift(그 품목 재고 복원)·point(그 품목 차감분 복원)가 보상한다. key=orderId.
 */
public record ItemCancelledEvent(
        Long orderItemId,
        Long shipmentId,
        String orderId,
        Long itemId,
        String reason,
        LocalDateTime occurredAt
) {
}

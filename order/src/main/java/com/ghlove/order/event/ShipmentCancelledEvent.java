package com.ghlove.order.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 출고 취소/보상 이벤트. 출고 단위 SAGA가 실패(재고부족·포인트부족)하거나 사용자가 취소할 때
 * 발행해 gift(재고 복원)·point(포인트 복원)가 <b>그 출고분만</b> 되돌린다. key=orderId.
 *
 * <p>재고 복원은 품목 단위라 복원 대상 orderItemId 목록을 함께 싣는다(부분취소 대비).
 */
public record ShipmentCancelledEvent(
        Long shipmentId,
        String orderId,
        String reason,
        List<Long> orderItemIds,
        LocalDateTime occurredAt
) {
}

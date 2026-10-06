package com.ghlove.order.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 출고 생성 이벤트 (멀티아이템 재설계, 선택지 A). 주문 1건이 지자체별 출고 N개로 나뉘고,
 * 각 출고가 <b>독립 SAGA 인스턴스</b>가 된다. order.saga 토픽에 <b>orderId를 키</b>로 발행해
 * 한 주문의 모든 출고 이벤트가 같은 파티션에서 순서대로 처리되게 하고, 상관ID(shipmentId)는
 * 페이로드로 싣는다.
 *
 * <p>gift는 {@code lines}의 품목별 재고를 <b>전부</b> 예약해야 출고 성공(하나라도 부족하면
 * 출고 전체 실패)이고, point는 {@code groupPointAmount}(그 지자체 소계)를 출고 단위로 차감한다.
 */
public record ShipmentCreatedEvent(
        Long shipmentId,
        String orderId,
        Long userId,
        String locgovCode,
        long groupPointAmount,
        List<Line> lines,
        LocalDateTime occurredAt
) {
    /** 출고 내 품목 한 줄 - 재고 예약 단위. itemOptionId(옵션행)가 있으면 gift가 옵션단위 재고를 본다. */
    public record Line(Long orderItemId, Long itemId, Long itemOptionId, Integer quantity, long pointAmount) {
    }
}

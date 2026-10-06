package com.ghlove.point.event;

import java.util.List;

/**
 * order 서비스 {@code SHIPMENT_CREATED} 계약의 point 로컬 사본(subset). 출고(지자체) 단위
 * 포인트 차감에 필요한 필드 + 품목별 차감액(부분취소/반품 시 품목 단위 복원을 위해)을 담는다.
 * order 쪽 레코드와 구조 동기화.
 */
public record ShipmentCreatedEvent(
        Long shipmentId,
        String orderId,
        Long userId,
        String locgovCode,
        Long groupPointAmount,
        List<Line> lines
) {
    public record Line(Long orderItemId, long pointAmount) {
    }
}

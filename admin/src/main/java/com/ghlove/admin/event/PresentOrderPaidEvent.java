package com.ghlove.admin.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * order가 답례품 주문의 결제완료를 알린다(온라인 주문 = AS-IS saveGiveOrderTempAndPay, 기탁서 등록 답례품 =
 * AS-IS procOffOrder) - 국민비서 답례품 주문 문자 재료.
 */
public record PresentOrderPaidEvent(String orderId, Long userId, List<Line> items, LocalDateTime occurredAt) {

    public record Line(Long itemId, Long sellerId, String itemName, String optionName) {
    }
}

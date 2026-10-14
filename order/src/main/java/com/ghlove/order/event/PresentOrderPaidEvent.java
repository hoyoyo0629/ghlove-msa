package com.ghlove.order.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 답례품 주문 결제완료 - AS-IS가 결제 직후 보내던 국민비서 "답례품 주문시"(PRESENT_ORDER) 문자의 재료.
 * admin이 받아 구매자·답례품 제공자 문자를 적재한다. 보내던 곳은 두 군데다:
 * 온라인 주문 {@code OrderServiceImpl.saveGiveOrderTempAndPay}(주문 즉시)와
 * 기탁서 등록 답례품 {@code NgDonationBatchServiceImpl.procOffOrder}(수납완료 뒤 결제처리).
 * items는 AS-IS buyItems/selectBuyItemList처럼 주문의 품목 전부(품목 순서대로, 모바일 분할 복제분 포함)다.
 */
public record PresentOrderPaidEvent(String orderId, Long userId, List<Line> items, LocalDateTime occurredAt) {

    /** AS-IS BuyItem 중 문자에 쓰는 값 - optionName은 AS-IS OP_ORDER_ITEM.OPTIONS(옵션 표시문자열). */
    public record Line(Long itemId, Long sellerId, String itemName, String optionName) {
    }
}

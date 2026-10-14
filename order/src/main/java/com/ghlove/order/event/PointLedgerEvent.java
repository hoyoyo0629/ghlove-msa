package com.ghlove.order.event;

/**
 * point.ledger 원장행 이벤트 중 order가 쓰는 필드만 - 기부 적립(EARN)의 refKey가 기부일련번호다.
 * 나머지 필드(ledgerId·locgovCode·pointAmount·createdDate)는 무시한다.
 */
public record PointLedgerEvent(Long userId, String txnType, String refKey) {
}

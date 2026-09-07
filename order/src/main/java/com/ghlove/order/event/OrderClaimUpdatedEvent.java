package com.ghlove.order.event;

import java.time.LocalDateTime;

/**
 * SFR-006 재검토 라운드 - ISP p.161 "클레임사건/상태뷰" ReadModel gap fill. 클레임
 * request/approve/reject/complete 전환마다 발행해 admin이 자체 사본(ClaimLedger)을
 * 채운다 - order.saga 토픽을 그대로 재사용한다(같은 orderId 키로 주문 자체의 라이프사이클
 * 이벤트와 순서가 보장돼야 하므로, 별도 토픽으로 분리하지 않는다).
 */
public record OrderClaimUpdatedEvent(
        Long claimId,
        String orderId,
        String claimType,
        String reason,
        String status,
        LocalDateTime createdDate,
        LocalDateTime processedDate,
        String itemName,
        String locgovCode,
        Long userId,
        String receiverName
) {
}

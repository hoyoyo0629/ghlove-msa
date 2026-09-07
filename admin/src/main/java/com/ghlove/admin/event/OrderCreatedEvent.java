package com.ghlove.admin.event;

import java.time.LocalDateTime;

/** Local copy of order service's event contract - must stay in sync with order/src/.../event/OrderCreatedEvent.java. */
// SFR-006 SLA 라운드에서 발견한 실제 버그: 이 레코드가 order 서비스의 실제 계약(2026-08-14
// 장바구니 라운드에서 locgovCode 필드가 추가됨)과 오랫동안 어긋나 있었다 - Jackson이
// FAIL_ON_UNKNOWN_PROPERTIES 없이 이름 매칭으로 역직렬화하다 보니 예외 없이 조용히
// locgovCode를 버려왔다("Local copy... must stay in sync"라는 경고 주석이 있었는데도
// 실제로 어긋난 걸 아무도 못 알아챈 사례). 이번에 locgovCode를 추가해 실제 계약과 맞췄다.
public record OrderCreatedEvent(
        String orderId, Long userId, Long itemId, Long sellerId, Integer quantity,
        Integer unitPrice, Long pointAmount, String locgovCode, String itemName, String receiverName,
        LocalDateTime occurredAt
) {
}

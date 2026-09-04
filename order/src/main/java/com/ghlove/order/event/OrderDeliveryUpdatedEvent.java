package com.ghlove.order.event;

import java.time.LocalDateTime;

/** SFR-006 "통계 및 SLA 관리를 위한 ReadModel 제공(제공자·지자체별 SLA 지표)" - 배송상태가
 *  바뀔 때마다(송장등록/배송중·배송완료/구매확정) admin의 SLA ReadModel에 실어보낸다.
 *  sellerId/locgovCode는 이미 ORDER_CREATED 이벤트로 admin에 전달돼 있어 여기선 반복하지 않는다. */
public record OrderDeliveryUpdatedEvent(String orderId, String deliveryStatus, LocalDateTime shippedDate,
                                          LocalDateTime deliveredDate, LocalDateTime confirmedDate,
                                          LocalDateTime occurredAt) {
}

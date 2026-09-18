package com.ghlove.point.event;

import java.time.LocalDateTime;

/**
 * 포인트 예약 상태 변화 - ISP 이벤트스토밍(원본 p.415/427)의 도메인 이벤트
 * "포인트예약됨 / 포인트해제됨"에 해당한다. 적립·사용·환불·소멸은 원장 행이 생기면서
 * {@link PointLedgerEvent}로 나가지만, 예약(hold)과 해제는 원장을 건드리지 않아
 * 그 경로로는 아무것도 발행되지 않았다 - 그래서 별도 이벤트로 낸다.
 *
 * <p>예약 확정(CONFIRMED)은 그 시점에 USE 원장행이 생기므로 {@link PointLedgerEvent}가
 * 이미 "사용됨"을 알린다 - 여기서 중복 발행하지 않는다.
 */
public record PointReservationEvent(Long reservationId, Long userId, String locgovCode,
                                     Long amount, String status, String refKey,
                                     LocalDateTime occurredAt) {
}

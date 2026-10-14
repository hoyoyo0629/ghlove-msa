package com.ghlove.point.event;

import java.time.LocalDateTime;

/**
 * 원장(PT_POINT_LEDGER) 행이 생길 때마다 발행 - admin의 포인트 통계 ReadModel용 (SFR-007/009).
 * refKey는 원장행의 REF_KEY(기부 적립이면 기부일련번호) - order가 기탁서 등록 답례품(입금대기)을
 * 그 기부의 포인트 적립 뒤에 결제처리하는 데 쓴다(AS-IS procOffOrder).
 */
public record PointLedgerEvent(Long ledgerId, Long userId, String locgovCode, String txnType,
                                Long pointAmount, LocalDateTime createdDate, String refKey) {
}

package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 포인트 예약(hold) - SFR-004에서 명시하는 "예약/예약해제" 기능. 원장(PT_POINT_LEDGER)과
 * 잔액(PT_POINT_BALANCE)은 예약 시점엔 건드리지 않고, 예약 행만 쌓아 가용잔액을
 * (balance - 활성 예약 합계)로 별도 계산한다 - 예약 해제가 보상 트랜잭션 없이 즉시 가능.
 * confirm()에서만 실제 USE 원장행 + 잔액차감이 발생한다.
 */
@Entity
@Table(name = "PT_POINT_RESERVATION")
@Getter
@Setter
@NoArgsConstructor
public class PointReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_ID")
    private Long reservationId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "REF_KEY")
    private String refKey;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "RESOLVED_DATE")
    private LocalDateTime resolvedDate;
}

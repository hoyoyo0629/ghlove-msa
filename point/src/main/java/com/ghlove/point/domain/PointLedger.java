package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 포인트 거래 원장 (append-only). POINT_AMOUNT는 부호 있는 값
 * (+적립/-사용/-취소회수). 잔액 테이블과 분리 관리 (SFR-004 명시 요구사항).
 *
 * EARN/RESTORE(적립성) 행은 동시에 FIFO "lot"이기도 하다: REMAINING_AMOUNT는
 * 생성 시 POINT_AMOUNT와 같게 시작해서 이후 USE/REVERSE(차감성) 거래가 소비할
 * 때마다 줄어든다 - 소멸 배치가 실제로 안 쓰인 포인트만 정확히 소멸시키기 위함.
 */
@Entity
@Table(name = "PT_POINT_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class PointLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEDGER_ID")
    private Long ledgerId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "TXN_TYPE")
    private String txnType;

    @Column(name = "POINT_AMOUNT")
    private Long pointAmount;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "REF_KEY")
    private String refKey;

    @Column(name = "EXPIRATION_DATE")
    private String expirationDate;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "REMAINING_AMOUNT")
    private Long remainingAmount;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 포인트 통계용 ReadModel - point.ledger 이벤트를 구독해 쌓는 로컬 사본 (append-only). */
@Entity
@Table(name = "STAT_POINT_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class PointLedgerStat {

    @Id
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

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}

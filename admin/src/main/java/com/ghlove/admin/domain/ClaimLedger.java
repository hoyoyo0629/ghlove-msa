package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SFR-006 재검토 라운드 - ISP p.161 "클레임사건/상태뷰" ReadModel gap fill. order.saga의
 * ORDER_CLAIM_UPDATED 이벤트를 구독해 쌓는 로컬 사본(admin은 order의 DB를 직접 읽을 수
 * 없음, DB per Service) - OrderLedger와 같은 패턴.
 */
@Entity
@Table(name = "STAT_CLAIM_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class ClaimLedger {

    @Id
    @Column(name = "CLAIM_ID")
    private Long claimId;

    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "CLAIM_TYPE")
    private String claimType;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "PROCESSED_DATE")
    private LocalDateTime processedDate;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "RECEIVER_NAME")
    private String receiverName;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 통계용 ReadModel - order.saga 이벤트를 구독해 쌓는 로컬 사본
 * (admin은 order 서비스의 DB를 직접 읽을 수 없음, DB per Service).
 */
@Entity
@Table(name = "STAT_ORDER_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class OrderLedger {

    @Id
    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "POINT_AMOUNT")
    private Long pointAmount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "SETTLEMENT_ID")
    private Long settlementId;
}

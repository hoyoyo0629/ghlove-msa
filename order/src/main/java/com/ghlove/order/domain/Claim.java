package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 반품/교환 클레임. 승인되면 원 주문을 취소 처리(재고/포인트 보상)한다. */
@Entity
@Table(name = "OD_CLAIM")
@Getter
@Setter
@NoArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLAIM_ID")
    private Long claimId;

    @Column(name = "ORDER_ID")
    private String orderId;

    /** 클레임 대상 품목 (멀티아이템 재설계 - 품목 단위 부분취소/반품). 레거시 클레임은 null. */
    @Column(name = "ORDER_ITEM_ID")
    private Long orderItemId;

    /** 대상 품목이 속한 출고. */
    @Column(name = "SHIPMENT_ID")
    private Long shipmentId;

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
}

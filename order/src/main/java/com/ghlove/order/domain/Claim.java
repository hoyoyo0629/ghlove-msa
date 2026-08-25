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

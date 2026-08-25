package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 정산 (제공자별, 포인트 기준). 생성→세금계산서발행→입금확인→마감 4단계로 진행. */
@Entity
@Table(name = "SETTLEMENT")
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SETTLEMENT_ID")
    private Long settlementId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "ORDER_COUNT")
    private Integer orderCount;

    @Column(name = "TOTAL_POINT_AMOUNT")
    private Long totalPointAmount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "INVOICE_NO")
    private String invoiceNo;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}

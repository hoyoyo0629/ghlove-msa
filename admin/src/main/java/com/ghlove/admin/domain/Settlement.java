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

    /** SFR-007 재검토 라운드 "정산 데이터 생성·조정" gap fill - 정산 생성 이후 그 정산에 속한
     *  주문이 클레임(반품/교환)으로 취소되는 경우의 사후 처리. GENERATED/INVOICED 상태는
     *  자동으로 금액/건수를 재계산하지만, DEPOSITED/CLOSED는 이미 입금·마감된 확정치라
     *  자동으로 건드리지 않고 이 플래그만 세워 운영자가 수동으로 검토하게 한다. */
    @Column(name = "PENDING_ADJUSTMENT_YN")
    private String pendingAdjustmentYn = "N";

    @Column(name = "ADJUSTMENT_NOTE", columnDefinition = "TEXT")
    private String adjustmentNote;
}

package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 출고 = AS-IS ORDER_SEQUENCE. 한 주문({@link Order}) 안의 <b>지자체(+판매자) 그룹</b>이며,
 * 포인트 차감이 지자체 단위라 <b>출고가 SAGA·정산의 기본 단위</b>다(설계안
 * docs/order-multiitem-redesign-plan.md §2·§3, 선택지 A). 하위에 여러 {@link OrderItem}(품목)을
 * 갖는다.
 *
 * <p>Phase 1(가산적 신설) 단계에서는 테이블만 만들어 두고 아직 어떤 경로도 이 엔티티에
 * 쓰지 않는다 - 단일품목 경로({@link Order} 직접 생성)가 그대로 병행 동작한다. 체크아웃/SAGA가
 * 이 모델로 전환되는 Phase 2 이후 실제로 채워진다.
 */
@Entity
@Table(name = "OD_SHIPMENT")
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipmentIdSeq")
    @SequenceGenerator(name = "shipmentIdSeq", sequenceName = "od_shipment_shipment_id_seq", allocationSize = 1)
    @Column(name = "SHIPMENT_ID")
    private Long shipmentId;

    /** 소속 주문 헤더 (FK 없이 참조). */
    @Column(name = "ORDER_ID")
    private String orderId;

    /** 이 출고의 지자체 - 포인트/정산 경계. */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    /** 이 출고(지자체)의 결제포인트 소계 = SAGA 포인트차감 단위. */
    @Column(name = "POINT_AMOUNT")
    private Long pointAmount = 0L;

    /** 출고 단위 배송비. */
    @Column(name = "DELIVERY_FEE")
    private Long deliveryFee = 0L;

    /** 출고 상태 - 하위 품목 상태들의 집계 파생. */
    @Column(name = "SHIPMENT_STATUS")
    private String shipmentStatus;

    /** 출고 단위 SAGA 재고예약 결과(출고 내 모든 품목이 성공해야 성공). */
    @Column(name = "STOCK_OUTCOME")
    private String stockOutcome;

    /** 출고 단위 SAGA 포인트차감 결과. */
    @Column(name = "POINT_OUTCOME")
    private String pointOutcome;

    @Column(name = "CANCEL_REASON")
    private String cancelReason;

    /** 배송관리(SFR-006) - 출고 단위 택배사/송장. */
    @Column(name = "CARRIER_CODE")
    private String carrierCode;

    @Column(name = "INVOICE_NO")
    private String invoiceNo;

    @Column(name = "DELIVERY_STATUS")
    private String deliveryStatus;

    @Column(name = "SHIPPED_DATE")
    private LocalDateTime shippedDate;

    @Column(name = "DELIVERED_DATE")
    private LocalDateTime deliveredDate;

    @Column(name = "CONFIRMED_DATE")
    private LocalDateTime confirmedDate;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}

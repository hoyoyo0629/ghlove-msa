package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 주문 (choreography SAGA로 gift/point 서비스와 비동기 연계). STOCK_OUTCOME과
 * POINT_OUTCOME은 각각 gift/point가 발행한 결과 이벤트를 반영하는 필드로, 둘 다
 * 채워지면 CONFIRMED 또는 CANCELLED로 전이한다. MVP 범위는 단일 품목 주문
 * (장바구니/복수 품목 없음).
 */
@Entity
@Table(name = "OD_ORDER")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "ITEM_NAME")
    private String itemName;

    /** 이 답례품의 지자체 - point 서비스가 "이 주문에 쓸 수 있는 지자체별 포인트"를
     * 판정하는 데 쓴다 (기부 포인트는 기부한 지자체 답례품에만 쓸 수 있음). */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "UNIT_PRICE")
    private Integer unitPrice;

    @Column(name = "POINT_AMOUNT")
    private Long pointAmount;

    @Column(name = "ORDER_STATUS")
    private String orderStatus;

    @Column(name = "STOCK_OUTCOME")
    private String stockOutcome;

    @Column(name = "POINT_OUTCOME")
    private String pointOutcome;

    @Column(name = "CANCEL_REASON")
    private String cancelReason;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    /** 배송관리 (SFR-006). 택배사 실연계는 admin 서비스가 담당(역할분리) - 여기는
     * 주문에 귀속된 배송 상태값만 갖는다. DELIVERY_STATUS가 NULL이면 아직 미발송. */
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

    @Column(name = "DELIVERY_ADDRESS")
    private String deliveryAddress;

    @Column(name = "DELIVERY_ADDRESS_DETAIL")
    private String deliveryAddressDetail;

    @Column(name = "RECEIVER_NAME")
    private String receiverName;

    @Column(name = "RECEIVER_PHONE")
    private String receiverPhone;

    @Column(name = "REQUEST_NOTE")
    private String requestNote;

    /** 이 주문에 적용된 쿠폰 발급건 (OP_COUPON_USER.COUPON_USER_ID) - 없으면 미적용. */
    @Column(name = "COUPON_ISSUE_ID")
    private Integer couponIssueId;

    /** 쿠폰으로 할인된 포인트 - POINT_AMOUNT는 이미 이 금액만큼 차감된 실결제액이다. */
    @Column(name = "DISCOUNT_AMOUNT")
    private Long discountAmount = 0L;

    /** admin 주문관리 콘솔 전용 - 운영자가 남기는 처리 메모(고객에게 노출되지 않음). */
    @Column(name = "ADMIN_MEMO")
    private String adminMemo;
}

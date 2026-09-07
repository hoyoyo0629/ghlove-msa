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

    /** SFR-006 재검토 라운드 "주문상세/이력뷰" ReadModel gap fill - admin 주문목록 검색이
     *  더 이상 order 서비스에 매 요청 REST 호출을 하지 않고 이 사본으로 조회할 수 있도록
     *  검색/표시에 필요한 최소 필드만 추가했다(상세보기·쓰기는 여전히 OrderAdminClient). */
    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "RECEIVER_NAME")
    private String receiverName;

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

    /** SFR-006 "제공자·지자체별 SLA 지표" gap fill - order.saga의 ORDER_CREATED/
     *  ORDER_DELIVERY_UPDATED 이벤트로 채워지는 배송 리드타임 원본 데이터. */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    /** 주문 확정(결제/재고 확보 완료) 시각 - 발송까지 걸린 시간의 기준점. */
    @Column(name = "ORDER_CONFIRMED_AT")
    private LocalDateTime orderConfirmedAt;

    @Column(name = "DELIVERY_STATUS")
    private String deliveryStatus;

    @Column(name = "SHIPPED_DATE")
    private LocalDateTime shippedDate;

    @Column(name = "DELIVERED_DATE")
    private LocalDateTime deliveredDate;

    /** 구매자 수취확인(구매확정) 시각. */
    @Column(name = "DELIVERY_CONFIRMED_DATE")
    private LocalDateTime deliveryConfirmedDate;
}

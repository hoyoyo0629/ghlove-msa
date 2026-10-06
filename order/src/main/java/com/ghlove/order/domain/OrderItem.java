package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 품목 = AS-IS ITEM_SEQUENCE. 한 출고({@link Shipment}) 안의 답례품 한 줄이며,
 * <b>부분취소/부분반품/교환의 단위(정본)</b>다(설계안 docs/design-decisions.md §5
 * §2-2). 옵션 스냅샷(optionName/optionPrice)이 여기로 귀속된다 - 단일품목 모델의
 * {@link Order#getOptionName()} 등이 재설계 시 이 엔티티로 이동한다.
 *
 * <p>Phase 1(가산적 신설) 단계에서는 아직 어떤 경로도 이 엔티티에 쓰지 않는다.
 */
@Entity
@Table(name = "OD_ORDER_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderItemIdSeq")
    @SequenceGenerator(name = "orderItemIdSeq", sequenceName = "od_order_item_order_item_id_seq", allocationSize = 1)
    @Column(name = "ORDER_ITEM_ID")
    private Long orderItemId;

    /** 소속 출고. */
    @Column(name = "SHIPMENT_ID")
    private Long shipmentId;

    /** 소속 주문 헤더 (비정규화 - 주문 단위 조회 편의). */
    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    /** 선택한 옵션행(OP_ITEM_OPTION.ITEM_OPTION_ID). 옵션 미선택이면 0. 옵션단위 재고 SAGA가
     *  option_stock_flag='Y'인 옵션의 재고를 이 값으로 차감/복원한다(Phase 5). */
    @Column(name = "ITEM_OPTION_ID")
    private Long itemOptionId = 0L;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    /** 주문 시점 상품명 스냅샷. */
    @Column(name = "ITEM_NAME")
    private String itemName;

    /** 주문 시 선택한 옵션명 스냅샷(AS-IS 주문상세 "옵션 [ ... ]"). */
    @Column(name = "OPTION_NAME")
    private String optionName;

    /** 옵션 추가금액(단가에 가산). */
    @Column(name = "OPTION_PRICE")
    private Integer optionPrice = 0;

    /** 각인(필수 추가정보) 구매자 입력값 - 제목별 값을 '||'로 연결(AS-IS textOption). */
    @Column(name = "TEXT_OPTION")
    private String textOption;

    @Column(name = "QUANTITY")
    private Integer quantity;

    /** 기본 판매가(옵션가 제외). */
    @Column(name = "UNIT_PRICE")
    private Integer unitPrice;

    /** 품목 소계 = (unitPrice + optionPrice) * quantity - discountAmount. */
    @Column(name = "POINT_AMOUNT")
    private Long pointAmount = 0L;

    /** 품목 단위 상태 - 부분취소/반품/교환의 정본. */
    @Column(name = "ITEM_STATUS")
    private String itemStatus;

    /** 이 품목에 적용된 쿠폰 발급건 (OP_COUPON_USER.COUPON_USER_ID). */
    @Column(name = "COUPON_ISSUE_ID")
    private Integer couponIssueId;

    @Column(name = "DISCOUNT_AMOUNT")
    private Long discountAmount = 0L;

    @Column(name = "CANCEL_REASON")
    private String cancelReason;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}

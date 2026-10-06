package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 출고 단위 재고예약 추적 (멀티아이템 재설계). 레거시 {@link GiftOrderStock}(주문=1품목)의
 * 출고·다품목 버전으로, 한 출고(shipment)의 <b>품목(order_item)마다</b> 한 행이다. order.saga의
 * {@code SHIPMENT_CREATED}에 반응해 예약할 때 기록해 (1) Kafka 재전달 중복예약을 막고
 * (2) {@code SHIPMENT_CANCELLED}/{@code ITEM_CANCELLED} 보상 시 실제 예약분만 복원되게 한다.
 *
 * <p>PK는 {@code orderItemId}다 - 같은 답례품의 다른 옵션이 한 출고에 2줄로 담길 수 있어
 * (shipmentId,itemId)로는 충돌하기 때문. orderItemId는 전역 유일하다.
 */
@Entity
@Table(name = "GIFT_SHIPMENT_STOCK")
@Getter
@Setter
@NoArgsConstructor
public class GiftShipmentStock {

    @Id
    @Column(name = "ORDER_ITEM_ID")
    private Long orderItemId;

    @Column(name = "SHIPMENT_ID")
    private Long shipmentId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    /** 옵션단위 재고를 차감한 경우 그 옵션행 ID(option_stock_flag='Y'). 아이템단위면 0/NULL.
     *  복원 시 이 값이 있으면 옵션 재고를, 없으면 아이템 재고를 되돌린다(Phase 5). */
    @Column(name = "ITEM_OPTION_ID")
    private Long itemOptionId;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 추가구성(추가상품) - AS-IS OP_ITEM_ADDITION. 본품(itemId)에 함께 담을 수 있는 별도
 * 부가 답례품(additionItemId)을 편성한다. 구매 시 본품과 독립된 주문 라인(OrderItem)으로
 * 담긴다(docs/design-decisions.md §4 §2). AS-IS 판매자 등록폼의 편성 UI 노출 여부는
 * Phase 1 말에 form.jsp 확인 후 Phase 2에 반영한다.
 */
@Entity
@Table(name = "OP_ITEM_ADDITION")
@IdClass(GiftAdditionId.class)
@Getter
@Setter
@NoArgsConstructor
public class GiftAddition {

    /** 본품 답례품 ID. */
    @Id
    @Column(name = "ITEM_ID")
    private Long itemId;

    /** 함께 담는 추가구성 답례품 ID. */
    @Id
    @Column(name = "ADDITION_ITEM_ID")
    private Long additionItemId;
}

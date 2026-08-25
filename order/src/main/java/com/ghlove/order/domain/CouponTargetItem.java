package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 쿠폰의 특정상품 타겟팅 (AS-IS OP_COUPON_TARGET_ITEM) - Coupon.targetItemType=2일 때만 의미있다. */
@Entity
@Table(name = "OP_COUPON_TARGET_ITEM")
@IdClass(CouponTargetItemId.class)
@Getter
@Setter
@NoArgsConstructor
public class CouponTargetItem {

    @Id
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Id
    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 쿠폰의 선택회원 발급대상 (AS-IS OP_COUPON_TARGET_USER) - Coupon.targetUserType=2일 때만 의미있다. */
@Entity
@Table(name = "OP_COUPON_TARGET_USER")
@IdClass(CouponTargetUserId.class)
@Getter
@Setter
@NoArgsConstructor
public class CouponTargetUser {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

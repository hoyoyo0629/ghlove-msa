package com.ghlove.order.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CouponTargetUserId implements Serializable {
    private Long userId;
    private Integer couponId;
}

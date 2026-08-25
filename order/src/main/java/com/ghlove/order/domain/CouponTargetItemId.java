package com.ghlove.order.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CouponTargetItemId implements Serializable {
    private Long itemId;
    private Integer couponId;
}

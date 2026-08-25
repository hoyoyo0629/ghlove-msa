package com.ghlove.order.service;

import com.ghlove.order.domain.CouponIssue;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AS-IS ShopUtils.getCouponDiscountPriceForItemCoupon()을 그대로 재현한 순수 계산 로직.
 * lineTotal(단가*수량)을 기준으로 할인액을 계산하며, 계산된 할인액이 lineTotal을 넘으면
 * AS-IS와 동일하게 0(적용불가)으로 무효화한다(라인 총액으로 캡하지 않음 - AS-IS 원본 동작).
 */
public final class CouponDiscountCalculator {

    private static final String PAY_TYPE_FIXED = "1";
    private static final String CONCURRENTLY_PER_QUANTITY = "2";

    private CouponDiscountCalculator() {
    }

    public static long discount(CouponIssue issue, long lineTotal, int quantity) {
        int payRestriction = issue.getPayRestriction() != null ? issue.getPayRestriction() : -1;
        if (payRestriction > 0 && lineTotal < payRestriction) {
            return 0;
        }

        long discount;
        if (PAY_TYPE_FIXED.equals(issue.getPayType())) {
            long pay = issue.getPay() != null ? issue.getPay() : 0;
            discount = CONCURRENTLY_PER_QUANTITY.equals(issue.getConcurrently()) ? pay * Math.max(quantity, 1) : pay;
        } else {
            long pay = issue.getPay() != null ? issue.getPay() : 0;
            discount = BigDecimal.valueOf(lineTotal)
                    .multiply(BigDecimal.valueOf(pay))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                    .longValue();
            Integer limit = issue.getDiscountLimitPrice();
            if (limit != null && limit > 0 && discount > limit) {
                discount = limit;
            }
        }

        if (discount > lineTotal) {
            discount = 0;
        }
        return discount;
    }
}

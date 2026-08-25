package com.ghlove.member.service;

import java.math.BigDecimal;

public record GiveStateInfo(BigDecimal nowYearTotalAmt, BigDecimal prevYearTotalAmt, long dDay, int nowDayPercent) {

    public static GiveStateInfo empty() {
        return new GiveStateInfo(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
    }

    /** AS-IS total_give_state.vue: Math.floor(now / prev * 100). prev가 0이면 표시하지 않는다. */
    public Integer yoyPercent() {
        if (prevYearTotalAmt == null || prevYearTotalAmt.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return nowYearTotalAmt.multiply(BigDecimal.valueOf(100))
                .divide(prevYearTotalAmt, 0, java.math.RoundingMode.FLOOR)
                .intValue();
    }
}

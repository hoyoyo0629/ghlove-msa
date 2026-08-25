package com.ghlove.donation.service;

import java.math.BigDecimal;

/**
 * 메인화면 "총 기부금" 위젯 (AS-IS total_give_state.vue가 호출하는
 * GET /api/common/getGiveState 응답 필드와 동일한 이름을 그대로 썼다).
 */
public record GiveState(BigDecimal nowYearTotalAmt, BigDecimal prevYearTotalAmt, long dDay, int nowDayPercent) {
}

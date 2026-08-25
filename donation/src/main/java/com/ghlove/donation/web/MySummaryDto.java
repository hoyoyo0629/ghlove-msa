package com.ghlove.donation.web;

import java.math.BigDecimal;

/** member 마이페이지 "기부내역 조회" 카드 + 메인화면 로그인 계정 박스용. */
public record MySummaryDto(BigDecimal totalAmt, BigDecimal thisYearAmt) {
}

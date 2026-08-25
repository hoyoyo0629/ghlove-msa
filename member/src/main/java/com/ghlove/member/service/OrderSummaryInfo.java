package com.ghlove.member.service;

/** order 서비스 GET /api/my-summary 응답 매핑 (마이페이지 "주문조회"/"취소반품교환"/"쿠폰함"). */
public record OrderSummaryInfo(int orderCount, int claimCount, int usableCouponCount) {
}

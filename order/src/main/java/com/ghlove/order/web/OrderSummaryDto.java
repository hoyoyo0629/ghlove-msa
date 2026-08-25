package com.ghlove.order.web;

/** member 마이페이지 "주문조회"/"취소반품교환"/"쿠폰함" 카드용. */
public record OrderSummaryDto(int orderCount, int claimCount, int usableCouponCount) {
}

package com.ghlove.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 쿠폰 자동발급 배치 (AS-IS는 별도 배치서버가 담당하던 회원가입/생일/정기발행 쿠폰발급을
 *  이 서비스의 @Scheduled로 재현). 매일 새벽 1시(다른 야간배치와 겹치지 않는 시각)에 실행. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponBatchScheduler {

    private final CouponService couponService;

    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyCouponBatch() {
        log.info("쿠폰 자동발급 배치 시작");
        couponService.issueSignupCoupons();
        couponService.issueBirthdayCoupons();
        couponService.reissueRegularCoupons();
        log.info("쿠폰 자동발급 배치 종료");
    }
}

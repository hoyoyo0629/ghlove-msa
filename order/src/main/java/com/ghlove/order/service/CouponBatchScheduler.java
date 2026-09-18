package com.ghlove.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 자동발급 배치 (AS-IS는 별도 배치서버가 담당하던 회원가입/생일/정기발행 쿠폰발급을
 * 이 서비스의 @Scheduled로 재현). 매일 새벽 1시(다른 야간배치와 겹치지 않는 시각)에 실행.
 *
 * <p>2026-09-09부터 기본 OFF. 쿠폰 기능이 AS-IS에서도 현재 사용되지 않아 화면 진입점을
 * 감추기로 했는데, 배치는 화면과 무관하게 계속 돌아 "회원이 볼 수 없는 쿠폰"이 매일
 * 쌓이기 때문이다. 코드를 지우지 않고 설정으로만 끄는 이유는 쿠폰 기능을 나중에 다시
 * 켤 수 있어야 해서다 - application.yml의 ghlove.coupon.enabled를 true로
 * 되돌리면 이 빈이 다시 등록되고 배치가 그대로 동작한다.
 *
 * <p>주문 확정 시점의 구매 트리거 발급(CouponService.issueAfterItemPurchase /
 * issueFirstPurchaseCoupons)은 SAGA 흐름 안이라 이 스위치가 아니라
 * OrderService.issuePurchaseTriggeredCoupons의 코드 주석으로 막혀 있다.
 */
@Component
@ConditionalOnProperty(name = "ghlove.coupon.enabled", havingValue = "true")
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

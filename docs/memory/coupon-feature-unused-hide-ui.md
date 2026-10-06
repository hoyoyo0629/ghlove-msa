---
name: coupon-feature-unused-hide-ui
description: 쿠폰 기능은 AS-IS에서도 미사용 - 화면 진입점만 숨기고 서비스 로직/DB/API는 그대로 둔다
metadata: 
  node_type: memory
  type: project
  originSessionId: a3be5a2a-5b3f-4f39-918b-27f2fd49d02b
  modified: 2026-09-09T06:01:40.454Z
---

2026-09-09 사용자 확인: AS-IS에서도 쿠폰 관련 기능은 현재 사용하지 않는다. 나중에 **화면에서 안 보이게만** 하고 **서비스 로직은 그대로 둔다**(삭제 아님).

**Why:** 나중에 다시 켤 수 있어야 하고, 쿠폰 로직은 주문 금액 계산에 얽혀 있어 걷어내면 회귀 위험이 크다.

**2026-09-09 조치 완료.** 자동발급 중단 + 회원 API 404 + 화면 진입점 전부 숨김. 코드는 하나도 지우지 않았다. 백엔드는 `order/application.yml`의 `ghlove.coupon.enabled: false` **마스터 스위치 하나**로 좌우된다(배치·회원API·Thymeleaf 쿠폰화면·체크아웃 쿠폰적용).

**How to apply (되살릴 때):** ① `ghlove.coupon.enabled: true` → 백엔드 전부 복구 ② `OrderService.issuePurchaseTriggeredCoupons()` 본문 주석 해제(구매트리거만 스위치 밖) ③ `storefront/src/router/index.js` 쿠폰 라우트 3줄 주석 해제 + `/mypage` redirect 3줄 삭제 ④ `storefront MyPageView.vue`와 `member/templates/mypage.html`의 "쿠폰함" 타일 주석 해제 ⑤ `CheckoutView.vue`의 `COUPON_ENABLED = true`.

**의도적으로 안 막은 것:** admin 관리 API `/api/admin/coupons**`(`CouponApiController`). admin `CouponAdminController`가 `OrderClient`로 실제 호출해서 막으면 admin 쿠폰 화면이 500이 된다. 그 화면들은 `admin-nav.html`에 링크가 없어 원래부터 메뉴 도달 불가라 그대로 뒀다. 상세 표는 `docs/cart-review-2026-09-09.md` §4-5.

관련: [[cart-review-remaining-tasks]]

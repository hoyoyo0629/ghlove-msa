---
name: cart-review-remaining-tasks
description: "장바구니 심각결함 2건 수정 완료, 잔여과제와 미완 재검증은 docs/cart-review-2026-09-09.md §4에 정리"
metadata: 
  node_type: memory
  type: project
  originSessionId: a3be5a2a-5b3f-4f39-918b-27f2fd49d02b
  modified: 2026-09-09T05:48:03.216Z
---

2026-09-09 장바구니 전면 분석 후 심각결함 2건(체크아웃 부분실패 유령 SAGA 이벤트, 화면금액≠실제차감액)과 중간 4건을 수정했다. **잔여과제·미완 검증은 `docs/cart-review-2026-09-09.md` §4에 전부 정리돼 있으니 장바구니/주문결제를 다시 손대기 전에 그 문서부터 읽는다.**

가장 중요한 미완 항목 둘:
- `OrderSagaPublisher`를 커밋 이후 발행(afterCommit)으로 바꾼 것은 장바구니뿐 아니라 **모든 order.saga 이벤트에 적용**된다. 6개 서비스 실기동 후 gift/point 소비자 타이밍 재검증이 아직 안 됐다.
- 배송비 경로가 실데이터로 검증되지 않았다 — 개발 DB 답례품이 대부분 무료배송(GIFT_SHIPPING_TYPE=1)이라 조건부무료/개당과금 경로를 실호출로 못 봤다.

관련: [[coupon-feature-unused-hide-ui]], [[production-domain-migration-plan]], [[thymeleaf-duplicate-cleanup-deferred]]

---
name: gift-seller-portal-unauthenticated
description: gift 판매자 화면의 sellerId 무인증 신뢰 문제 - 2026-09-10 해소 완료 (로그인 기반 + 서비스 계층 소유권 검사)
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-10T09:47:41.322Z
---

**2026-09-10 해소됨.** 이전 상태와 무엇을 고쳤는지 기록.

**있었던 문제:** gift의 제공자 화면이 `sellerId`를 쿼리파라미터·hidden input·화면 입력칸("제공자 ID")으로 받아 그대로 신뢰했다. 값만 바꾸면 **남의 답례품을 등록·수정·판매중지·폐지·재고조정**하고 **남의 문의에 답변**할 수 있었다. `GiftService.stop(itemId)`와 `adjustStock(itemId, qty)`, `InquiryService.answer(inquiryId, answer)`는 소유권 검사가 **아예 없었다** - 컨트롤러를 우회하면 그대로 통했다.

**고친 방식:**
- `GiftController.currentSellerId(request)` - GH_AUTH JWT의 userId → `OP_SELLER.MEMBER_USER_ID` → sellerId. **서버가 신원을 결정**하고 요청의 주장은 무시한다. `SellerPortalController`(`/seller/dashboard`)가 이미 쓰던 방식을 그대로 가져왔다.
- 엔드포인트 9개 전환: `GET/POST /register`, `GET/POST /gifts/{id}/edit`, `POST /gifts/{id}/discontinue`, `/stop`, `/stock`, `GET /my`, `GET /my/inquiries`, `POST /inquiries/{id}/answer`.
- `sellerGate()` - 로그인 없으면 member 로그인으로, 로그인은 했으나 연결된 판매자가 없으면 안내 화면(구분하지 않으면 로그인 무한루프).
- **서비스 계층에도 소유권 검사 신설**: `GiftService.stop(itemId, sellerId)`, `adjustStock(itemId, sellerId, qty)`, `InquiryService.answer(inquiryId, sellerId, answer)`. 운영자 대리답변은 `answerAsOperator()`로 분리(운영관리 경로는 내부 시크릿으로 이미 보호).
- 화면에서 신원 주장 제거: `register.html`의 "제공자 ID" 입력칸, `my.html`·`inquiries.html`·`edit.html`의 sellerId hidden/링크 파라미터 삭제.

**검증(실측):** 비로그인 `/my` → 로그인 리다이렉트 / 판매자 아닌 회원 → 안내 화면 / **판매자 아닌 회원이 `sellerId=9001`을 실어 재고 9999 조작 시도 → 차단(재고 46 유지)** / **판매자 9001이 판매자 9002 답례품에 재고·판매중지·폐지 시도 → 3건 모두 차단(재고 30·APPROVED 유지)** / 본인 답례품 정상 조정 성공.

**How to apply:** 판매자 화면을 새로 추가할 때는 반드시 `currentSellerId(request)`로 sellerId를 얻고, 서비스 메서드에도 `sellerId`를 넘겨 소유권을 검사한다. **`@RequestParam Long sellerId`를 다시 만들지 않는다.** 판매자↔회원 연결(`OP_SELLER.MEMBER_USER_ID`)을 admin 화면에서 맺어주는 UI는 아직 없다(DB 시딩만) - [[provider-portal-split-deferred]]와 함께 볼 것.

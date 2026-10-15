---
name: gift-seller-status-asis-codes
description: 판매자(OP_SELLER) 상태코드를 AS-IS 2=사용/3=중지/5=승인대기로 전환(2026-10-15) - TO-BE 발명 0/1/2반려/3이 AS-IS '2'와 충돌했었다
metadata:
  type: project
---

AS-IS `OP_SELLER.STATUS_CODE`는 **2=사용 / 3=중지 / 5=승인대기** 셋뿐이다(seller/list.jsp 라벨, form.jsp 라디오 기본값 2).
AS-IS 카탈로그·발송 쿼리는 전부 `S.STATUS_CODE = '2'`로 살아있는 업체를 고른다.

TO-BE는 0대기/1승인/2반려/3중지를 새로 만들어 써서 **같은 '2'가 AS-IS 사용 ↔ TO-BE 반려**로 충돌했다
(gift `SellerRepository.findEmailSendTargets`가 AS-IS '2'를 그대로 써 반려 업체를 고르게 돼 있었다).
2026-10-15 관리자 메인 이식 중 발견 → 사용자 승인으로 전환:
- 코드: `Seller.STATUS_ACTIVE/STOPPED/WAITING`, 등록 기본값 5, admin `seller/list.html` 라벨·승인 버튼(→2), 메인 집계(gift AdminDashboardService) '2'.
- 데이터: 18곳 1→2(`database/ddl/migration-gift-seller-status-asis-codes.sql`, gift DB), seed `service-gift.sql`도 '2'로.
- 반려 상태는 AS-IS에 없어 없앴다(전환 시점 0건).

**남은 것**: TO-BE 판매자 관리 화면(목록·등록) 자체가 AS-IS seller/list.jsp·form.jsp와 다른 발명 화면이다(상태 라디오 없음,
등록 후 승인 버튼 흐름). AS-IS는 등록 폼에서 상태를 고르고 기본값이 '2'(사용) - 화면 재이식은 서비스 로직 전수조사 때.
관련 [[admin-main-dashboard-asis-port]], [[service-logic-full-audit-planned]].

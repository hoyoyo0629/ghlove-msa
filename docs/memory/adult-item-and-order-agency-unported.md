---
name: adult-item-and-order-agency-unported
description: 2026-10-14 발견한 미이식 2건 - 성인(19세) 답례품 제한은 같은 날 이식 완료, 주문대행(상담원 대리주문) 주문 생성 흐름은 조사 끝·이식 대기
metadata:
  type: project
---

온라인 주문 PRESENT_ORDER 문자·모바일 분할을 이식하다(AS-IS `OrderServiceImpl.saveGiveOrderTempAndPay`) 같은 블록에서 발견. 사용자 "미이식 2건도 구현하자"(2026-10-14).

**1. 성인 답례품 제한 - 이식 완료(2026-10-14, 재기동 대기)**
- AS-IS 실동작만 이식: 매퍼의 `isAdult`/MINOR_ALLOW_FLAG 필터는 setIsAdult 호출자가 없는 죽은 코드라 제외. 목록 화면들은 "19세 답례품 모두
  보여주는 걸로 변경"으로 adultYn을 'Y'로 덮어써 19세 뱃지·상세이동 차단이 실제로는 안 걸린다(goods/index·searchGoods 담기만 buyFlag로 막음).
- 성인 판정 = 올해 - 출생연도 ≥ 19 (`MemberService.isAdult`, `/api/auth/me` adultYn, `/api/admin/members/{id}/adult`).
- 서버 차단: `CartService.checkoutMultiItem` "19세 이상 로그인 사용자만 주문 가능합니다." / 상세 담기·바로선택 alert / 체크아웃 paymentYn(결제버튼 숨김+빨간 안내) /
  DonateGiftSelectView 담기 alert.
- 설정: gift `Seller.adultItemYn`(성인답례품 등록권한), `Gift.adultItemYn`(구매 제한 성인, 판매자 권한 없으면 'N'로 저장 `applyAdultItemYn`).
  admin 입점업체 폼·답례품 폼에 체크박스 - 두 폼 모두 AS-IS와 구성이 달라(업태/업종 행 없음, 답례품 분류 행 없음) **임시 위치**. 폼 재이식은 전수조사 때.
- 렌더 미검증: offtest01(오프라인 권한)로는 두 관리자 폼이 access-denied라 직접 못 봄.

**2. 주문대행 주문 생성 - 조사 끝, 이식 대기**
- AS-IS: 관리자 목록 `opmanager/order-agency/order/list.jsp`(검색 + "주문대행 로그인 페이지 이동" + 행별 "인쇄페이지") → 프론트(3000)
  `/admin/order-agency/{login,cntr-list,goods-list,item-details,step1,step2,order-agency-detail}.html`(약 6,700줄) →
  api `/api/order-agency/*` 11개(`ghlove-api OrderAgencyController`) + `OrderAgencyServiceImpl`(RSA 키 30분·로그인 120분, 관리자 status 9·pCrypto 비번,
  기부자 전화번호+본인인증 CI로 회원 확정, ROLE_AGENCY JWT) + `orderAgencySaveGiveOrderTempAndPay`(OrderServiceImpl:9269~, 약 600줄) +
  테이블 G_AGENCY_LOGIN_CONFIRM_INFO·G_ORDER_AGENCY_ORDER_LOG·(JPA)AgencyPrivateKeyInfo·TempData.
- TO-BE `OrderAgencyAdminController`(/admin/order-agency)는 AS-IS 목록이 아니라 발명된 "주문대행 조회"(전화번호 검색 추가 등) - 재이식 대상.

**Why:** 사용자 원칙 - MSA 전환사업이라 미이식 0이 기본선 ([[port-everything-no-omissions-then-rfp-isp]]).

**How to apply:** 주문대행 착수 시 [[asis-screen-port-procedure]] 3종 대조부터, 프론트 7화면은 AS-IS처럼 대민(storefront) 쪽 라우트로.
관련 [[service-logic-full-audit-planned]].

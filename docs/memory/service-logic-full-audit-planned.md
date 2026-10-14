---
name: service-logic-full-audit-planned
description: 서비스 로직 누락이 계속 나와서, 오늘 작업 기준(서비스 본문 블록 단위 대조)으로 운영관리 → 답례품관리 → 대민 순서의 전수조사를 나중에 하기로 함(2026-10-14 사용자 지시)
metadata:
  type: project
---

2026-10-14 사용자 지시: "운영관리 > 답례품관리 > 대민 순서대로 나중에 지금 작업하는 기준으로 전수조사 하자."

계기: 화면·엔드포인트는 이식됐는데 AS-IS 서비스 메서드 **본문 안의 블록**이 빠진 것이 계속 나왔다.
- 같은 날 발견: 온라인 주문의 국민비서 문자·모바일 분할·배송지 검사 5종·우편번호 저장, 기탁서 수납의 포인트 부분 차감,
  성인 답례품 제한 전체, 주문대행 주문 생성([[adult-item-and-order-agency-unported]]).
- 앞서 있던 것: 수납일·삭제여부 미기록([[donation-sttemnt-pay-de-populated]]), 기탁서 답례품 주문([[offgive-gift-order-asis-flow]]).

**Why:** 화면 기준 대조로는 서비스 본문 누락이 안 잡힌다. 매번 사용자 테스트에서 하나씩 드러나는 걸 끝내려는 것.

**How to apply ("지금 작업하는 기준")**:
- 순서: ① 운영관리(admin) ② 답례품관리(gift·order 관리자/판매자) ③ 대민(storefront 사용자 흐름).
- 단위: AS-IS 컨트롤러 → 서비스 구현 메서드 본문을 **블록(주석 단위)마다** 목록화하고, TO-BE에서 그 블록이 어디서 실행되는지
  (동기 호출·SAGA 이벤트·배치)를 하나씩 대조한다. 매퍼 SQL 조건(예: STTEMNT_PAY_DE IS NOT NULL)까지.
- 주석처리·죽은 코드(호출 없는 setter, 주석된 검증)는 AS-IS 실동작 기준으로 판정 - 죽은 건 이식 안 함, 기록만.
- 산출물: 영역별 갭 원장(docs/)을 먼저 만들고 보고 → 사용자 확인 후 수정. [[as-is-parity-exhaustive-audit-method]], [[asis-screen-port-procedure]]
- 상태: **미착수(예정)**. 현재 진행 작업 끝난 뒤 사용자가 시작 지시.

**이미 발견해 둔 항목(전수조사 때 원장에 넣을 것, 2026-10-14)**:
- 답례품관리: admin 답례품 폼(gift-items/form)·입점업체 폼(seller/form)이 AS-IS item/form.jsp·seller/form.jsp와 구성이 다름(AS-IS 항목 누락 +
  "최소 기부금액"·"노출유형" 등 AS-IS에 없는 항목). ★재고연동(OP_ITEM.STOCK_FLAG): AS-IS는 'Y'일 때만 재고 검증·차감(0 하한, -1 무제한)인데
  TO-BE는 STOCK_FLAG를 안 보고 항상 재고를 본다(TO-BE 데이터 26건 전부 'N'). 판매자 포털 정보수정에 통신판매업번호·"(성인 답례품 판매가능)" 없음.
- 대민: 답례품 목록 카드의 장바구니 아이콘(AS-IS $goodsItem.addToCart, isShowCartIcon) 없음.
- 고쳐 둔 것: order MemberClient가 member /api/admin에 내부인증 헤더 없이 호출해 401→빈 목록(가입·생일 쿠폰 자동발급 대상 0건) - 2026-10-14 헤더 추가.

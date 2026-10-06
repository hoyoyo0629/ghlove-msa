---
name: order-single-item-vs-multiitem-decision
description: "MSA 주문이 1주문=1답례품(단일품목)인 것은 RFP/ISP 요건이 아니라 구현 단순화 선택. AS-IS는 멀티아이템(주문→출고→품목). 멀티아이템 재설계로 방향 결정, 설계안 문서부터 검토 후 착수"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-21T14:03:40.492Z
---

**결정(2026-09-21)**: MSA order 도메인을 AS-IS처럼 **멀티아이템 주문(1주문=여러 답례품)**으로 재설계하기로 함. 단, 대공사라 **설계안 문서 → 리뷰 → 단계별 구현** 순서. 설계안은 `docs/order-multiitem-redesign-plan.md`.

**근거(소스 대조)**:
- MSA 현행은 **1주문=1답례품**: `CartService.checkout`이 장바구니 행마다 `OrderService.createOrder`를 호출해 단일품목 주문을 만든다(주석 "단일품목 주문 SAGA"). 한 번 결제해도 품목 수만큼 주문번호 생성.
- AS-IS는 **1주문(ORDER_CODE)→출고(ORDER_SEQUENCE, 지자체/판매자)→품목(ITEM_SEQUENCE)** 3단 계층. orderDetail.html이 result.item을 지자체별로 그룹핑.
- **RFP·ISP 어디에도 단일품목 요건 없음.** RFP SFR-006은 "장바구니·결제·주문상태"만 명시(SAGA는 포인트 차감·복원에 적용하라는 것). ISP 이벤트스토밍(p.415/427)은 오히려 "주문생성 / **출고생성**"을 분리해 AS-IS 계층에 가깝다. 즉 단일품목은 구현자의 SAGA 단순화 선택.

**핵심 설계 방향(문서 요약)**: Order(헤더)→Shipment(출고=지자체 그룹)→OrderItem(품목). **SAGA는 출고(지자체) 단위**가 권장(포인트는 지자체 전용이라 차감 단위가 지자체=출고와 일치). 부분취소/반품/교환은 품목 단위. 옵션(optionName/optionPrice)은 OrderItem 속성 — 2026-09-21 단일품목 모델에 얹은 옵션을 재설계 시 품목으로 이관.

**구현 진행(2026-09-21) — Phase 1~4 + 체크아웃 flip 코드 완료, 전 서비스 컴파일·storefront 빌드 OK. 재기동+E2E 미실행.**
- **선택지 A(출고=지자체 단위 SAGA) 확정.** 부분실패=부분성공(그 출고만 취소, 나머지 확정, 헤더 PARTIALLY_CONFIRMED).
- **Phase 1**: `od_shipment`/`od_order_item` DDL + 엔티티/리포지토리. **Phase 2**: `CartService.checkoutMultiItem`(주문1+출고N+품목M), `od_order` item/qty/unit_price NOT NULL 해제, SHIPMENT_* 이벤트, `ShipmentSagaService`. **Phase 3**: gift `GIFT_SHIPMENT_STOCK`+reserve/restoreStockForShipment, point deduct/restoreForShipment(refKey=`orderId#shipmentId`). **Phase 4**: 읽기 API/프런트(주문목록·상세·완료) 신 모델 + **레거시 폴백**(출고행 없으면 헤더로 표시→기존주문 백필불요). **flip**: `CheckoutApiController.complete`→checkoutMultiItem(레거시 checkout은 롤백용 존치).
- **재기동 시 활성.** DDL 3개 이미 적용됨: `database/ddl/migration-order-multiitem-phase1.sql`, `-phase2.sql`, `migration-gift-shipment-stock.sql`.
- **Phase 5 완료(2026-09-21, 재기동·E2E 미실행)**: (5a) gift/point 보상을 **품목(orderItemId) 단위**로 — GIFT_SHIPMENT_STOCK PK를 order_item_id로 재작성(★같은 답례품 다른옵션 2줄 PK충돌 버그 수정), point 차감원장 품목단위 분할(refKey=`orderId#orderItemId`). (5b) 품목 단위 부분취소/반품/교환 — `ShipmentSagaService.cancelItem`+`ITEM_CANCELLED` 이벤트, `OrderService.cancelItem`(발송전 즉시), `ClaimService` 품목단위 재작성, `Claim`에 order_item_id/shipment_id, API `/items/{orderItemId}/cancel|claim`, 프런트 품목별 버튼. **5c 배송(출고 단위 송장/배송상태/구매확정)은 seller/admin 의존이라 보류** → return/exchange는 출고 DELIVERED여야 신청되므로 5c 후 완전검증.
- **Phase 6 완료(2026-09-21)**: point 기부포인트상세 주문번호 표시=refKey `#` 앞부분(`displayOrderCode`); admin 주문목록용 헤더 요약 채움(`checkoutMultiItem`이 itemName="대표 외 N건"·총수량·대표 itemId/sellerId, 권위는 OrderItem·헤더는 표시캐시); 프런트 레거시 주문 분기(orderItemId 유무). **★백필은 안 함**(기존주문 예약은 레거시 추적에 있어 백필 시 신 per-item 보상이 복원 실패 → 기존주문은 레거시 읽기폴백+주문단위 취소로 처리).
- **여전히 보류**: 5c 출고 단위 배송(송장/배송상태/구매확정, seller/admin 의존); admin 통계(stat_order_ledger, 헤더집계가 ORDER_CONFIRMED 미발행→신주문 미반영); 멀티셀러 admin 판매자필터(헤더 대표 sellerId 부정확→OrderItem 기준 필요). 셋 다 admin/seller 재개 시.
- 관련 [[as-is-logic-is-the-spec]], [[api-vs-kafka-decision-criteria]], [[point-use-tracking-g-cntr-use-point]].

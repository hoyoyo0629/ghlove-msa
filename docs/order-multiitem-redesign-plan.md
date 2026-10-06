# 주문(order) 도메인 멀티아이템 재설계 설계안

> **문서 목적 / 독자**: 이 문서는 order 도메인을 "1주문=1답례품"에서 **"1주문=여러 답례품(AS-IS 계층)"**으로 재설계하기 위한 **검토용 설계안**이다. order 서비스를 구현·리뷰할 개발자와 아키텍처 리뷰어가 대상이다. 착수 전 이 문서를 리뷰해 범위·순서·리스크에 합의한 뒤 단계별로 구현한다.
>
> 작성 2026-09-21. 배경 결정 근거는 [as-is-logic-is-the-spec], [api-vs-kafka-decision-criteria], [point-use-tracking-g-cntr-use-point] 메모 및 `docs/cross-service-transaction-patterns.md` 참고.

---

## 1. 배경과 결정

### 1-1. 현행(AS-IS 대비 이탈)
- **MSA 현행**: 1주문 = 1답례품. 체크아웃(`CartService.checkout`)이 장바구니의 **행마다 `OrderService.createOrder`를 호출해 별도 단일품목 주문**을 만든다. 한 번 결제해도 품목 수만큼 주문번호가 생긴다.
- **AS-IS**: 1주문(`ORDER_CODE`) → **출고(`ORDER_SEQUENCE`, 지자체/판매자 그룹)** → **품목(`ITEM_SEQUENCE`)** 3단 계층. 한 번 결제 = 주문 1건, 그 안에 여러 지자체·여러 품목.

### 1-2. 왜 바꾸나 (근거)
- **RFP·ISP 어디에도 "단일품목 주문" 요건이 없다.** RFP SFR-006은 "장바구니·결제·주문상태 관리"만 명시. SAGA는 "포인트 차감·복원"에 적용하라는 것이지 주문 분할을 강제하지 않는다.
- **ISP는 오히려 멀티아이템에 가깝다.** 이벤트스토밍(원본 p.415/427)에 주문관리 명령이 "주문생성 / **출고생성** / 운송장등록 / 배송완료 / 반품·교환…"으로 **주문과 출고를 분리** 설계 → AS-IS의 `ORDER_CODE→ORDER_SEQUENCE(출고)` 계층과 동일 방향.
- 단일품목은 **구현자의 SAGA 단순화 선택**이었을 뿐 정본(AS-IS)·요건과 어긋난다. 옵션·부분취소·부분반품 등 후속 기능이 전부 AS-IS와 달라지는 근본 원인이다.

### 1-3. 결정
**AS-IS처럼 멀티아이템 주문 계층으로 재설계한다(옵션 2).** 단, 규모가 커서 이 설계안 리뷰 후 단계별로 착수한다.

---

## 2. 목표 도메인 모델

### 2-1. 계층 (AS-IS 대응)
```
Order (주문 헤더)                      = AS-IS ORDER_CODE
 └─ Shipment (출고 = 지자체+판매자 그룹) = AS-IS ORDER_SEQUENCE
     └─ OrderItem (품목)               = AS-IS ITEM_SEQUENCE
```

- **Order (주문 헤더, `OD_ORDER` 재정의)**: orderId, userId, createdDate, 주문 전체 상태(요약), 배송지 정보(받는이/주소/전화/요구사항 — 주문 단위 공통), 총 주문포인트/총 취소포인트/총 결제포인트, 총 배송비.
- **Shipment (출고, 신규 `OD_SHIPMENT`)**: shipmentId, orderId, **locgovCode(+sellerId)**, 배송상태(deliveryStatus), 택배사(carrierCode)/송장(invoiceNo), 출고 단위 배송비. — **포인트 차감이 지자체 단위이므로 출고=지자체 그룹이 SAGA·정산의 기본 단위**가 된다.
- **OrderItem (품목, 신규 `OD_ORDER_ITEM`)**: orderItemId, shipmentId(또는 orderId+locgov), itemId, itemName(스냅샷), **optionName/optionPrice(스냅샷)**, quantity, unitPrice, pointAmount(품목 소계), **품목별 상태**(주문/취소/반품/교환 - 부분처리용), couponIssueId/discount.

> **옵션은 OrderItem 속성**이다. 방금(2026-09-21) 단일품목 모델에 옵션을 얹었는데, 재설계 시 옵션 스냅샷 필드(optionName/optionPrice)는 그대로 OrderItem으로 이동한다.

### 2-2. 상태 모델
- **품목 단위 상태**가 정본이다(AS-IS도 ITEM_SEQUENCE 단위 상태). 부분취소/부분반품이 여기서 나온다: 10 결제대기 / 20 결제완료 / 30 배송중 / 35 배송완료 / 40 구매확정 / 취소·반품·교환 계열.
- **출고 상태** = 그 출고 품목들의 배송상태(송장·배송중·완료).
- **주문 헤더 상태** = 품목 상태들의 요약(전부 확정=확정, 일부 취소=부분취소 등) — 표시용 파생.

---

## 3. 이벤트 / SAGA 재설계

### 3-1. 현행 SAGA (단일품목)
`order.saga` 토픽. `ORDER_CREATED` → gift `STOCK_RESERVED`/`STOCK_RESERVE_FAILED` + point `POINT_DEDUCTED`/`POINT_DEDUCT_FAILED` → order `resolveIfReady`(재고예약 && 포인트차감 → `ORDER_CONFIRMED`, 아니면 `ORDER_CANCELLED`). 보상은 주문(=품목) 1건 단위.

### 3-2. 목표 SAGA (멀티아이템)
핵심 난점은 **한 주문이 여러 지자체를 포함**한다는 것 — 포인트는 지자체별로만 쓸 수 있어 **차감은 출고(지자체) 단위**, 재고 예약은 **품목 단위**다.

**선택지 A — 출고(지자체) 단위 SAGA (권장)**
- 주문 1건을 **출고 N개(지자체별)** 로 나눠 각 출고를 독립 SAGA 인스턴스로 처리. 상관ID = shipmentId.
- `SHIPMENT_CREATED(shipmentId, orderId, locgov, items[])` → gift가 **품목별 재고예약**(출고 내 모든 품목 성공해야 출고 성공) + point가 **출고 단위 포인트차감**(그 지자체 소계) → order가 출고 확정/취소.
- 출고 단위로 부분 성공 가능(강남구 출고는 성공, 상주 출고는 재고부족으로 취소). 주문 헤더는 출고 결과들을 집계.
- **장점**: 포인트(지자체)·정산(지자체) 경계와 일치, 부분처리 자연스러움. **AS-IS ORDER_SEQUENCE=출고 단위와 정확히 대응**.

**선택지 B — 주문 단위 원자 SAGA**
- 주문 전체가 all-or-nothing. 한 품목이라도 실패하면 주문 전체 취소. 구현은 단순하나 부분처리·부분결제 불가 → AS-IS와 다름. **비권장**.

→ **선택지 A(출고 단위 SAGA)** 를 채택. 보상 트랜잭션도 출고/품목 단위로 정의(부분취소=해당 품목만 재고복원+포인트복원).

### 3-3. 이벤트 스키마 변화
- 기존 `ORDER_CREATED(orderId, itemId, quantity, pointAmount, locgov)` → `SHIPMENT_CREATED(shipmentId, orderId, locgovCode, userId, lines[{orderItemId, itemId, optionId, quantity, pointAmount}], groupPointAmount)`.
- `STOCK_RESERVED/FAILED`, `POINT_DEDUCTED/FAILED`, `CONFIRMED/CANCELLED`를 **shipmentId 상관ID**로. 재고 결과는 품목 리스트 단위.
- 부분 클레임: `ITEM_CANCEL_REQUESTED / ITEM_RETURN_REQUESTED / ITEM_EXCHANGE_REQUESTED(orderItemId)` → 승인 시 `ITEM_CANCELLED` → gift 재고복원 + point 포인트복원(그 품목분).
- admin 통계 리스너(`stat_order_ledger`)도 출고/품목 단위 반영으로 수정.

---

## 4. 영향 범위 (변경 대상)

| 영역 | 변경 |
|---|---|
| **DB (ord 스키마)** | `OD_ORDER` 헤더로 축소 + `OD_SHIPMENT`·`OD_ORDER_ITEM` 신설. FK 없이(서비스 내부도 미사용 원칙) orderId/shipmentId 참조 |
| **체크아웃** | `CartService.checkout`: 행마다 주문 → **장바구니 전체를 주문 1건**, 지자체별 출고, 행별 품목. 옵션 스냅샷은 품목으로 |
| **SAGA** | 출고 단위 인스턴스로 재작성. gift `reserveStockForOrder`→품목 리스트, point `deductForOrder`→출고(지자체) 단위 |
| **주문상세** | 방금 만든 단일 레이아웃 → **지자체(출고) 그룹 + 품목 목록** 반복(AS-IS orderDetail.html 원형 그대로). 이미 CSS·구조는 재현돼 있어 데이터만 다차원화 |
| **주문목록** | 주문 1건에 대표품목+외 N건 표기 |
| **클레임** | `ClaimService`: 주문 단위 → **품목 단위** 부분취소/반품/교환 |
| **포인트** | 출고(지자체)별 포인트 차감/복원 — 이미 지자체별 lot 소진 로직 있음([point-use-tracking-g-cntr-use-point]) |
| **admin** | 주문/정산/통계 조회모델을 출고·품목 단위로 |
| **프론트** | 장바구니(이미 그룹 있음)·체크아웃·주문상세·주문목록·클레임 화면 |

---

## 5. 마이그레이션 계획

기존 단일품목 주문(`od_order` 각 행)을 **주문1+출고1+품목1**로 승격한다.
1. 신규 테이블 생성(`od_shipment`, `od_order_item`).
2. 백필: 각 기존 `od_order` 행 → `od_order`(헤더, 배송지/포인트 합계 유지) 유지 + `od_shipment`(그 주문의 locgov 1개) 1건 + `od_order_item`(itemId/option/qty/point) 1건 생성. 기존 컬럼(itemId/optionName 등)은 품목으로 이관 후 헤더에서 제거(또는 뷰 유지).
3. 클레임(`od_claim`)의 order 참조 → orderItem 참조로 매핑(기존은 1:1이라 자연 승계).
4. 이벤트/SAGA는 **신규 주문부터** 신 스키마로. 진행 중 주문이 없도록 배포 타이밍 조율(로컬은 무관).

---

## 6. 단계별 구현 순서 (제안)

1. **Phase 1 — 스키마·엔티티** ✅**완료(2026-09-21)**: `OD_SHIPMENT`/`OD_ORDER_ITEM` DDL(`database/ddl/migration-order-multiitem-phase1.sql`, ord 스키마 적용) + 엔티티(`Shipment`/`OrderItem`)·리포지토리(`ShipmentRepository`/`OrderItemRepository`). **가산적**: 기존 od_order 컬럼 미변경, 단일품목 경로 그대로 병행. ddl-auto=none이라 기동검증 무영향, 아직 호출부 없음.
2. **Phase 2 — 체크아웃 (order-side)** 🔶**진행중(2026-09-21)**: 아래 2-1 참조. **order 서비스 신규 경로 완성**(레거시와 병행, 아직 미배선). gift/point 소비자 전환은 Phase 3.
3. **Phase 3 — SAGA 재작성** 🔶**진행중(2026-09-21)**: gift/point 소비자 완성(아래 6-3). SAGA 루프 전체 배선·컴파일. 미배선(체크아웃 컨트롤러 전환 전이라 실트래픽 없음).
4. **Phase 4 — 주문상세/목록/완료 화면** ✅**완료(2026-09-21, 아래 6-4)** + **체크아웃 flip 반영**(재기동 시 활성).
5. **Phase 5 — 클레임/배송**: 5a 완료(per-item 재고·포인트), 5b 완료(품목 단위 부분취소/반품/교환), **5c 배송(출고 단위 송장·배송상태·구매확정)은 seller/admin 의존이라 보류**. 아래 6-5.
6. **Phase 6 — admin/통계·마이그레이션·point조회모델**: 조회모델·백필·refKey 표시.

각 Phase 끝에 E2E 검증(다지자체 장바구니 결제 → 주문 1건 → 부분취소).

### 6-1. SAGA 구조 결정 (2026-09-21 확정)
- **선택지 A(출고=지자체 단위 SAGA)** 채택 — AS-IS `ORDER_SEQUENCE`(출고)와 정확히 대응, 포인트·정산 경계 일치. 상관ID=shipmentId.
- **부분실패 UX(§7)**는 Phase 3(SAGA)에서만 영향 → 그 시점에 재확인.

### 6-2. Phase 2 order-side 산출물 (2026-09-21)
> ⚠ **핵심 제약**: 체크아웃(쓰기)·SAGA·읽기 화면은 **원자적 전환**만 가능(중간 배포 시 신규 주문이 확정 불가/화면 깨짐). 그래서 **레거시 단일품목 경로를 기본값으로 유지한 채 신규 출고 단위 경로를 병행 구축**하고, gift/point 소비자·읽기까지 준비되면 마지막에 컨트롤러를 한 번에 전환한다.

- **스키마**: `migration-order-multiitem-phase2.sql` — `od_order`의 item_id/quantity/unit_price NOT NULL 해제(헤더는 품목컬럼 없이 저장, 레거시는 계속 채움 → 하위호환).
- **이벤트(신규, order.saga 토픽·key=orderId)**: `SHIPMENT_CREATED`(order→gift/point, 품목 lines[] 포함), `SHIPMENT_STOCK_RESERVED/FAILED`(gift→order), `SHIPMENT_POINT_DEDUCTED/FAILED`(point→order), `SHIPMENT_CANCELLED`(order→gift/point 보상).
- **체크아웃**: `CartService.checkoutMultiItem` — 검증(priceSelected)·포인트 부족판정은 레거시와 동일. 주문 헤더1 + 지자체별 출고N + 품목M 저장, 쿠폰 확정소진, 출고마다 `SHIPMENT_CREATED` 발행. 반환=orderId 1건. (레거시 `checkout`은 그대로 존치)
- **출고 SAGA 해소**: `ShipmentSagaService` — 출고별 stock&&point 결과 집계→CONFIRMED/CANCELLED(취소 시 `SHIPMENT_CANCELLED`로 반대편 보상), 주문 헤더는 출고들 종결 후 CONFIRMED/CANCELLED/**PARTIALLY_CONFIRMED**(부분성공) 집계. `OrderSagaListener`에 SHIPMENT_* 케이스 배선.
- **아직 안 함(다음)**: 읽기 화면(done/목록/상세), 컨트롤러 전환, 기존 주문 백필.

### 6-3. Phase 3 gift/point 소비자 산출물 (2026-09-21)
- **gift**: `migration-gift-shipment-stock.sql`(GIFT_SHIPMENT_STOCK, PK=(shipment_id,item_id)) + `GiftShipmentStock` 엔티티/리포지토리. `GiftService.reserveStockForShipment`(출고 품목 전량 선검증 후 차감, all-or-nothing, 멱등)·`restoreStockForShipment`(예약분만 복원). 리스너에 `SHIPMENT_CREATED`→예약→`SHIPMENT_STOCK_RESERVED/FAILED`, `SHIPMENT_CANCELLED`→복원 배선.
- **point**: `PointService.deductForShipment`(출고=지자체 단위 차감, refKey=`orderId#shipmentId`로 출고별 멱등·독립복원)·`restoreForShipment`. 리스너에 `SHIPMENT_CREATED`→차감→`SHIPMENT_POINT_DEDUCTED/FAILED`, `SHIPMENT_CANCELLED`→복원 배선. `consumeLots`/`gCntrUsePoint`도 refKey 단위.
  - ⚠ **Phase 6 유의**: point 조회모델(기부포인트 상세)의 '답례품 주문번호' 표시는 refKey의 `#` 앞부분(orderId)을 써야 한다(현재는 refKey 원문). 신 모델 flip 전에 처리 필요.
- **이벤트 계약 로컬 사본**: gift/point에 `ShipmentCreatedEvent`(subset)·`ShipmentCancelledEvent`(subset) + 각자 발행하는 결과 이벤트 레코드. order 원본과 구조 동기화 유지.

### 6-4. Phase 4 읽기 화면 + flip 산출물 (2026-09-21)
- **읽기 API(신 모델, 레거시 폴백 포함)**: `OrderMyApiController` 주문목록(대표품목 "외 N건"+itemCount)·주문상세(헤더+출고그룹 shipments[]→품목 items[], 결제=상품소계+배송비-취소분). `CheckoutApiController.done` 주문완료(출고 그룹+품목). **출고행이 없으면(백필 전 레거시 주문) 헤더를 단일 출고·품목으로 어댑팅** → 기존 주문도 백필 없이 정상 표시.
- **프런트**: `OrderDetailView`(출고→품목 2중 루프, 품목별 상태/후기, 출고별 배송상태), `OrderCompleteView`(그룹 items[]+헤더 배송지/배송비), `MyOrdersView`(대표품목·itemCount, 다품목은 목록 후기버튼 숨김).
- **flip**: `CheckoutApiController.complete` → `checkoutMultiItem`(주문 1건 반환). 레거시 `CartService.checkout`은 롤백용 존치. **재기동+빌드 시 활성**.
- **flip 후 알려진 미커버(Phase 5/6)**: ① 주문취소/구매확정/반품·교환이 아직 헤더/주문 단위(출고·품목 단위 아님) ② admin 주문목록의 답례품명 등 헤더 품목컬럼은 신 주문에서 NULL(조회모델 Phase 6) ③ point 기부포인트 상세 '주문번호'=refKey 원문 ④ 기존 주문 백필 미실행(폴백으로 표시).
- **E2E 검증 필요(재기동 후)**: 다지자체 장바구니 결제 → 주문 1건(주문번호 1개) → 지자체별 출고 확정 → 주문상세 그룹/품목 표시 → 포인트 지자체별 차감 확인.

---

## 7. 리스크 / 검토 포인트

- **포인트 차감 단위**: 출고(지자체) 단위가 맞다(포인트는 지자체 전용). 주문 단위로 묶으면 안 됨.
- **부분 실패 UX**: 다지자체 주문에서 한 지자체만 재고부족 시 — 그 출고만 취소하고 나머지는 확정할지(선택지 A), 주문 전체 실패로 볼지(선택지 B). **A 권장**이나 제품 결정 필요.
- **주문번호 체계**: 현재 품목마다 orderId. 재설계 후 주문 1개 = orderId 1개, 출고/품목은 하위 시퀀스. 기존 데이터·외부 참조(리뷰의 orderCode 등) 영향 점검.
- **정산(admin)**: 지자체·판매자별 정산이 출고 단위와 정합. 현 통계 리스너 재작성 필요.
- **범위 확정 전 착수 금지**: 이 문서 리뷰에서 선택지 A/B와 Phase 범위를 확정한 뒤 Phase 1 시작.

### 6-5. Phase 5 산출물 (2026-09-21)
- **★ Phase 3 버그 수정**: `GIFT_SHIPMENT_STOCK` PK를 (shipment_id,item_id)→**order_item_id**로 변경. 같은 답례품의 서로 다른 옵션이 한 출고에 2줄로 담기면 (shipment,item)으로는 PK 충돌했다. 재고 선검증도 답례품별 합산 수요로 수정.
- **5a per-item 보상 토대**:
  - gift: `reserveStockForShipment`/`restoreStockForShipment`가 orderItemId 단위 기록, `restoreStockForItem(orderItemId)` 추가. 이벤트 `SHIPMENT_CREATED.lines[]`에 orderItemId 추가.
  - point: 차감 원장을 **품목(orderItemId) 단위로 분할**(refKey=`orderId#orderItemId`), 잔액검증은 출고 총액 1회. `restoreForShipment(orderItemIds)`=품목별 복원 루프, `restoreForItem(orderId,orderItemId)` 추가. `SHIPMENT_CREATED.lines[{orderItemId,pointAmount}]`.
- **5b 품목 단위 부분취소/반품/교환**:
  - order: `ShipmentSagaService.cancelItem`(품목→CANCELLED, `ITEM_CANCELLED` 발행, 출고 전량취소 시 출고 CANCELLED, 헤더 재집계=품목 기준 CONFIRMED/CANCELLED/PARTIALLY_CONFIRMED). `ITEM_CANCELLED` 이벤트(gift/point가 그 품목분만 복원).
  - `OrderService.cancelItem`(발송 전 즉시 부분취소), `ClaimService` 품목 단위 재작성(request/approve/reject/complete, complete→cancelItem). `Claim`에 order_item_id/shipment_id 컬럼.
  - API: `POST /api/orders/{id}/items/{orderItemId}/cancel`, `.../claim`. 프런트 주문상세 품목별 버튼(주문취소/교환·반품/후기) + 선택품목 클레임 폼.
- **5c 보류(seller/admin 의존)**: 출고 단위 송장등록/배송상태/배송완료/구매확정. 현재 delivery 필드는 Shipment에 있으나 세팅 경로(seller/admin)가 신 모델 미대응 → return/exchange는 출고가 DELIVERED여야 신청 가능하므로 5c 이후 완전 검증됨.
- **DDL 적용됨**: `migration-gift-shipment-stock.sql`(재작성, order_item_id PK), `od_claim` order_item_id/shipment_id 컬럼(ALTER).

### 6-6. Phase 6 산출물 (2026-09-21)
- **★ 백필 미실행 결정**: 기존 단일품목 주문을 출고/품목행으로 백필하지 **않는다**. 이유: 기존 주문의 재고·포인트 예약은 레거시 추적(GIFT_ORDER_STOCK PK=orderId, pt_point_ledger refKey=orderId)에 있는데, 백필로 품목행만 만들면 신 per-item 보상 경로(refKey=`orderId#orderItemId`, GIFT_SHIPMENT_STOCK)가 그 예약을 못 찾아 복원이 실패한다. 대신 **기존 주문은 레거시 경로로 처리**: 읽기는 폴백(헤더→단일 출고·품목), 취소는 주문 단위(`/api/orders/{id}/cancel`→ORDER_CANCELLED→레거시 복원). 프런트가 `orderItemId` 유무로 분기(있으면 품목 단위, 없으면 주문 단위, 레거시는 클레임 버튼 숨김).
- **point 조회모델 표시**: 기부포인트 상세 '답례품 주문번호'가 refKey 원문 대신 `#` 앞부분(주문번호)만 표시(`PointService.displayOrderCode`). 멀티아이템 주문은 품목마다 USE 행이 생겨 AS-IS의 품목별 사용내역 표시와도 더 부합.
- **admin 주문목록 헤더 요약**: `checkoutMultiItem`이 헤더의 표시용 컬럼(itemName="대표품목 외 N건", quantity=총수량, itemId/sellerId=대표, locgovCode=단일지자체만)을 채운다 — 권위 데이터는 OrderItem, 헤더는 admin 주문목록·검색·마이주문 목록이 읽는 **요약 캐시**. admin 콘솔이 신 주문에서 빈칸 없이 표시된다.
- **여전히 보류(admin 재개 시)**: ① admin 통계(`stat_order_ledger`)는 헤더 집계가 ORDER_CONFIRMED를 발행하지 않아 신 주문 미반영 → 출고/품목 단위 이벤트로 재작성 필요 ② 멀티셀러 주문의 판매자별 admin 필터는 헤더 대표 sellerId로는 부정확 → OrderItem 기준 조회로 재작성 필요 ③ 5c 출고 단위 배송 라이프사이클.

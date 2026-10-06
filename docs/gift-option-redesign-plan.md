# gift 옵션 다형 체계 재설계 계획 (AS-IS 동일 재현)

> 근거: [[gift-option-parity-audit]] 갭목록 + 사용자 결정(2026-09-22).
> 방침: **기능 자체(서비스+화면)는 AS-IS와 동일하게 풀 구현**하되, 판매자 등록화면에서
> AS-IS처럼 **S2(2조합형)·T(텍스트형 옵션)는 CSS 숨김**(마크업 존재·비노출)으로 처리한다.
> 판매자(답례품관리자) / 운영자(admin) 분리도 AS-IS 동일. 옵션 재고는 **옵션단위**(AS-IS parity).
>
> **기준점([[as-is-parity-includes-disabled-state]])**: AS-IS에 기능이 있으면 다 만들되, AS-IS 소스가
> 숨김(CSS)/주석/비활성이면 MSA도 동일하게 숨김/주석/비활성으로 남긴다(빼지도 켜지도 않음).

## 0. 전제 — DDL 불요 (스키마 이미 존재)

로컬 gift DB에 AS-IS 이관 스키마가 그대로 있다. **추가 DDL 없이 애플리케이션 계층만 구현**한다.

- `op_item_option`: item_option_id(PK), item_id, **option_type**, option_display_type, option_hide_flag,
  **option_name1/2/3**, option_price, option_cost_price, option_price_nonmember, **option_stock_flag/quantity/code**,
  option_stock_schedule_date/text, **option_sold_out_flag**, option_display_flag, created_user_id/date.
- `op_item`: **item_option_flag, item_option_type, item_text_option_flag, item_text_option_title1/2/3**.
- `op_item_addition`: (item_id, addition_item_id) — 추가구성 조인.
- `op_item_option_soldout`: 아이템 단위 품절 요약(목록/상세 조인).

> 유일한 예외: 주문측(OrderItem)에 각인값(textOption)·itemOptionId 저장 컬럼이 없으면 그때만 최소 ALTER.
> Phase 4에서 판정.

## 1. AS-IS 동일 구현 대상 (전 옵션형태)

| 옵션형태 | 판매자 등록화면 | 서비스/데이터 | 구매자 화면 |
|---|---|---|---|
| S 선택형 | **노출** | 구현 | 단일 드롭다운 |
| S3 3조합형 | **노출** | 구현 | name1→name2→name3 종속 드롭다운 |
| S2 2조합형 | **숨김(hidden)** | 구현(비노출) | (편성 시) 2단 드롭다운 |
| T 텍스트형 옵션 | **숨김(hidden)** | 구현(비노출) | (편성 시) 옵션별 텍스트 |
| 각인=필수 추가정보(itemTextOptionFlag/Title1~3) | **노출**(별도 섹션) | 구현 | 필수 추가정보 입력칸 |
| 추가구성(itemAdditionFlag) | **노출**(form.jsp:1686~ "추가구성상품") | 구현(별도 item_data_type=2 + op_item_addition) | 별도 부가품 선택 |

핵심: **기능은 다 만들되 S2·T는 판매자 화면에서만 숨김.** 데이터·서비스·구매흐름·조회는 전부 동작하게.

## 2. 주문 연동 설계 (order 멀티아이템 위 배선)

- 이미 OrderItem에 `optionName`·`optionPrice`(단일) 존재 → 확장 배선.
- **선택형/조합형**: 장바구니 담기 시 `itemOptionId` 전달 → checkout에서 옵션 검증·optionName 조립
  (S: `제목: 값`, S2/S3: `제목1:값1 | 제목2:값2 [| 제목3:값3]`)·추가금액(option_price)을 결제금액·OrderItem에 반영.
- **각인(필수 추가정보)**: 구매자 입력값을 `textOption`(`||` 구분)으로 OrderItem에 저장 → 주문/마이페이지 표시.
- **추가구성**: AS-IS와 동일하게 **별도 주문 라인(OrderItem)** 으로 담김(본품과 독립 수량·가격).
- **옵션단위 재고(AS-IS parity)**: gift 재고예약 SAGA를 `option_stock_flag='Y'`인 옵션에 대해
  **itemOptionId 단위**로 차감/복원. option_stock_flag='N'이면 재고 무관.

## 3. 페이즈

### Phase 1 — gift 모델/서비스 (기능 풀 구현, 화면 무관) ✅ 완료(2026-09-22, gift 컴파일 OK)
- ✅ `GiftOption` 확장: name2/3, display_type, hide_flag, cost_price, price_nonmember, stock_code, schedule_* 필드.
- ✅ `Gift`에 itemOptionFlag/Type, itemTextOptionFlag, itemTextOptionTitle1/2/3, itemDataType, itemAdditionFlag 매핑.
- ✅ `GiftAddition`/`GiftAdditionId`/`GiftAdditionRepository`(op_item_addition) 신설.
- ✅ `GiftOptionService`: `saveOptions`(S/S2/S3/T 전체교체), `saveTextOptions`(각인, 30자·`:|<>` 검증),
  `saveAdditions`(추가구성 교체), `additionsOf`. 기존 단일 register/edit 호환.
- ✅ buyer 상세(GiftPublicApiController): OptionDto에 optionType·name2/3, AdditionDto 신설, DetailResponse에
  additions 추가(옵션형태·각인은 gift 필드로 노출).
- ✅ API: PUT `/api/admin/gift-items/{itemId}/options/bulk|text|additions`.

### Phase 2 — 판매자 등록화면 (S2·T 숨김) ✅ 완료(2026-09-22, gift 컴파일 OK)
- **배치 확정(A안)**: 현행 gift 판매자 포털(Thymeleaf, edit.html)에 API-first로 얹음.
- ✅ edit.html에 옵션 에디터 추가: 옵션 사용여부 Y/N, 옵션형태 라디오 S/S2/S3/T(**S2·T는
  `style="display:none"` 숨김 = AS-IS 동일**), 형태별 조합 그리드(name1/2/3·추가금액·재고연동·수량·숨김),
  필수 추가정보 섹션(제목1~3·30자·`: | < >` 금지 안내), 추가구성(ID 목록) 편성.
- ✅ **판매자 스코프 엔드포인트**(GiftController, currentSellerId 소유권): GET/POST `/gifts/{id}/options`,
  POST `/gifts/{id}/text-options`, POST `/gifts/{id}/additions`. (/api/admin/** 옵션 API는
  InternalApiAuthInterceptor로 내부전용이라 브라우저 미사용 → 판매자 경로 신설.)

### Phase 3 — 구매자 storefront 상세(GiftDetailView) ✅ 완료(2026-09-22, storefront build OK)
- ✅ itemOptionType 분기: S 단일 드롭다운(기존), S2·S3 종속 드롭다운(sel1→sel2→sel3 → chosenOption 해석),
  재고 N개/[품절]/추가금액(+) 표시.
- ✅ 필수 추가정보 입력칸(itemTextOptionTitle1~3 → textOption `||`), 추가구성 체크박스(additions).
- ✅ 검증: V1 옵션 미선택 차단, V2 각인 미입력 차단, V3 품절 차단.
- ⚠ textOption·additionItemIds는 payload에 실리나 order 장바구니가 아직 미소비 → **Phase 4에서 배선**
  (Jackson이 미지원 필드 무시하므로 현재 무해).

### Phase 4 — 장바구니/체크아웃 배선 ✅ 완료(2026-09-22, gift·order 컴파일·테스트·build OK)
- ✅ **최소 ALTER**: od_cart_item·od_order_item에 text_option 컬럼 추가. od_cart_item 유니크키를
  (user,item,option)→(user,item,option,**text_option**)로 교체(각인 다르면 별도 라인, ''로 정규화).
  `database/ddl/migration-order-option-textoption.sql` (적용 완료).
- ✅ CartItem/OrderItem에 textOption 필드. AddRequest에 textOption·additionItemIds.
- ✅ CartService.add(…,textOption,additionItemIds): 각인 스냅샷·4튜플 dedup, 추가구성은 **별도 라인**으로 추가.
  checkoutMultiItem이 OrderItem.textOption 복사. gift 단건 옵션 API는 조합명(name1/2/3) 결합.
- ✅ CartLine·CheckoutLine에 textOption 노출, CartView·CheckoutView에 "각인 : …" 표시.
- ✅ (부수) 이전 세션 단일옵션 작업으로 깨져 있던 CartServiceTest 5건(3-arg finder·7-arg createOrder) 정정.

### Phase 5 — 옵션단위 재고 SAGA ✅ 완료(2026-09-22, gift·order 컴파일·테스트 OK)
- ✅ **최소 ALTER**: od_order_item·GIFT_SHIPMENT_STOCK에 item_option_id 추가
  (`database/ddl/migration-order-option-stock.sql`, 적용 완료).
- ✅ OrderItem에 itemOptionId(checkout에서 CartItem→OrderItem 복사), ShipmentCreatedEvent.Line(order·gift)에 itemOptionId.
- ✅ gift reserveStockForShipment: 옵션이 option_stock_flag='Y'면 **옵션단위**(op_item_option.option_stock_quantity)
  로 선검증·차감·품절갱신(option_sold_out_flag), 아니면 기존 답례품단위. 예약행(GiftShipmentStock)에 itemOptionId 기록.
- ✅ restoreReservation: itemOptionId 있으면 옵션 재고 복원, 없으면 답례품 재고 복원(SHIPMENT_CANCELLED·ITEM_CANCELLED 공용).
- ⚠ op_item_option_soldout 요약테이블은 미갱신(옵션행 option_sold_out_flag로 직접 표기) - AS-IS 요약 재현은 후순위.

### Phase 6 — 조회/표시/포인트 ✅ 완료(2026-09-22, order 컴파일·테스트·storefront build OK)
- ✅ 주문상세(OrderMyApiController.OrderItemDto)·주문완료(CheckoutApiController.DoneItemDto)에 textOption 노출.
- ✅ storefront OrderDetailView·OrderCompleteView에 "각인 [ … ]" 표시(장바구니·체크아웃은 Phase 4).
- ✅ point: 옵션 추가금액은 이미 OrderItem.pointAmount=(salePrice+optionPrice)*qty에 포함돼 출고 포인트차감/기부포인트에
  그대로 반영(checkoutMultiItem). 추가 코드 불요 확인.

## 완료 요약
**Phase 1~6 코드 완료(2026-09-22). 전 서비스 컴파일·테스트·storefront build OK. 재기동 시 활성.**
적용 DDL: `migration-order-option-textoption.sql`, `migration-order-option-stock.sql`(+ gift DB gift_shipment_stock ALTER).

### 후속 보강(2026-09-22, 사용자 지적 반영)
- ✅ **추가구성 자식 답례품 생성 구현**(AS-IS 동일): `GiftService.createAdditionChild`가 추가상품명/가격/재고로
  item_data_type=2 자식 OP_ITEM을 생성(본품 seller/locgov/category 상속, displayFlag='N'로 목록 숨김,
  dataStatusCode='APPROVED'로 주문가능) → op_item_addition 연결. `GiftOptionService.saveAdditions`가
  교체 시 기존 자식까지 삭제. edit.html 추가구성 UI를 (상품명·가격·재고) 그리드로 교체 + 조회 GET.
  (기존 "ID 링크" 단순화 폐기.)
- ✅ **옵션 전부 품절 → 목록 품절뱃지**(AS-IS op_item_option_soldout 요약 대체): MSA는 그 배치 파생캐시를
  안 쓰고, `GiftOptionService.optionSoldOutItemIds`가 조회시점에 "표시옵션 전부 품절" 아이템을 계산해
  목록 카드 soldOut에 OR. 시드(item 1026: 재고50·옵션 전부품절)로 판정 검증 완료(실화면은 재기동 후).
- 남은 후순위: S2·T 실데이터 E2E(판매자 화면 숨김이라 시드 필요).

## 4. 미결/확인
- Phase 2 판매자 UI 스택(Vue storefront vs gift 서빙) — 착수 시 확인.
- ~~추가구성 편성 UI 존재 여부~~ → **확인완료**: form.jsp:1686~ "추가구성상품"(itemAdditionFlag) 노출.
  각 추가구성은 item_data_type=2 별도 OP_ITEM + op_item_addition 연결. Gift에 itemDataType·itemAdditionFlag 매핑 필요.
- OrderItem 각인/optionId 저장 컬럼 유무 → Phase 4에서 최소 ALTER 판정.
- **옵션단위 재고 이해 확인**: "AS-IS 동일"에 따라 option_stock_flag='Y' 옵션은 옵션단위 차감. (사용자 확인 요청)

# 배송비 정책 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] (프론트이벤트+검증로직+매퍼 전수 → 갭목록 →
실데이터로 실사용 확인 → 사용자와 범위 결정). 정본은 AS-IS 소스.

## 1. AS-IS 배송정책 모델 (정본)

`Item`(op_item) 컬럼 — `saleson.shop.item.domain.Item`:
- `shippingType` : **1 무료 / 2 판매자조건부 / 3 출고지조건부 / 4 상품조건부 / 5 개당배송비(BOX당) / 6 고정배송비**
- `shipping` 기본배송비, `shippingFreeAmount` 조건부무료 기준금액, `shippingItemCount` 개당배송비 BOX 기준수량
- `shippingExtraCharge1` 제주(JEJU) 추가, `shippingExtraCharge2` 도서산간(ISLAND) 추가, `shippingReturn` 반품/교환 배송비
- `shippingGroupCode`/`shipmentGroupCode` 묶음배송·물류통합 그룹, `deliveryType` 1 본사 / 2 업체

계산 본체 — `saleson.shop.order.domain.Shipping#getShippingGroups()` (Shipping.java:195~419):
1. 묶음배송 그룹핑: type 1·6은 개별(single), 그 외는 `shippingGroupCode` 같은 것끼리 한 묶음.
2. islandType(JEJU/ISLAND)로 `addDeliveryCharge` = extraCharge1/2.
3. type별 realShipping:
   - **1 무료**: addDeliveryCharge만
   - **2/3 조건부**: 그룹 합계(`baseAmountForShipping`) ≥ freeAmount → 무료, 아니면 shipping+add. type3+shipmentGroupCode면 물류통합 상품 전체 합산.
   - **4 상품조건부**: 그룹/단일 합계 기준 동일
   - **5 개당배송비**: `boxCount = ceil(총수량 / shippingItemCount)`; fee = (shipping+add) × boxCount
   - **6 고정**: shipping + add
4. 착불(`shippingPaymentType`=2) → payShipping=0.

프론트 표기 — `ghlove-frontend/items/details-main.html`(구매자 상세):
- 화면 마크업은 **"배송비 : 무료" 하드코딩**(L223, L835), "모든 상품은 답례품 제공업체에서 배송"(L839).
- `shippingTypeText()`(L3248~3269)에 type별 문구 계산 함수가 있으나 **템플릿에 바인딩 안 됨 = 죽은 함수**.

등록 화면 표기 — 답례품 등록/수정 폼 `opmanager/item/form.jsp`(판매자 `seller/item/form.jsp`가 이 파일을 `<jsp:include>`, 즉 판매자·운영자 공유):
- 배송정보 영역(`#deliveryDiv`)에서 **택배사·배송구분(본사/업체)·출고지 주소·반품/교환 신청 가능 여부·반품/교환 주소는 노출**.
- **"■ 배송비 설정"(shippingType 1~6·기본배송비·조건부무료 기준·제주/도서산간 추가)은 `<tr class="hidden">`로 숨김**(form.jsp:1800). 반품/교환 구분·반품/교환 배송비(shippingReturn)도 `<tr class="hidden">`.
- 즉 판매자/운영자는 **화면에서 배송비를 설정할 수 없다**(입력 UI 숨김). 서버 계산 로직만 살아있음. DB의 type3 8건은 이 숨겨진 경로/마이그레이션 유입.

## 2. 실사용 판정 (gift DB `public.op_item` 실데이터 26건)

| 요소 | 실데이터 | 판정 |
|---|---|---|
| shippingType=3 출고지조건부(10만↑무료/미만 3천) | **8건** | ✅ 실사용 |
| shippingType 미설정(NULL=무료취급) | 18건 | ✅ 무료 |
| 제주/도서산간 추가배송비(extra1/2) | **0건** | 미사용 |
| 묶음배송 그룹(shipping_group_code) | **0건** | 미사용 |
| 물류통합(shipment_group_code) | **0건** | 미사용 |
| 개당배송비 기준수량(shipping_item_count>1) | **0건** | 미사용 |
| 반품배송비(shipping_return>0) | **0건** | 미사용 |
| 업체배송(delivery_type=2) | **0건** | 미사용 |
| 화면 배송비 표기 | 전 상품 "무료" 하드코딩 | shippingTypeText 죽은코드 |

## 3. TO-BE(MSA) 현재 상태 대조

- **화면**: `storefront/.../GiftDetailView.vue` L424/L602 = "배송비 : 무료" 하드코딩 → **AS-IS와 동일(parity ✅)**.
- **서버 계산**: `order OrderService#deliveryFeeOf()` 이미 존재 —
  - type1 무료 ✅ / type6 고정 ✅ / type2·3·4 조건부무료(lineTotal 기준) ✅ / type5 base×quantity(부분) / 제주·도서산간 주소문자열 판정(부분).
  - cart(`CartService`)·checkout preview·shipment까지 배선 완료.

## 4. 갭목록 (AS-IS엔 있으나 MSA 미/부분 구현) — 전부 실데이터 0건

| # | 갭 | AS-IS | MSA | 실데이터 |
|---|---|---|---|---|
| G1 | 묶음배송 그룹핑 | shippingGroupCode 묶어 그룹당 1배송비 | 라인별 개별 계산 | 0건 |
| G2 | type2/3 구분·물류통합 합산 | shipmentGroupCode 전체 합산 | lineTotal 단독 | 0건 |
| G3 | 개당배송비 BOX수량 | ceil(수량/itemCount)×fee | base×quantity (itemCount 무시) | 0건 |
| G4 | 제주/도서산간 정식 판정 | Island 판정표(IslandRepository) | 주소 문자열 키워드 5개 | 0건 |
| G5 | 착불 payShipping=0 | shippingPaymentType=2 | 없음 | 0건 |
| G6 | 반품/교환 배송비 | shippingReturn (클레임) | 없음 | 0건 |
| G7 | 본사/업체 배송 정산배분 | deliveryType로 sellerId 배정 | 정산 미구현 영역 | 0건 |

## 5-1. 구현 결과 (2026-09-22, 사용자 결정: G1~G7 전부 구현)

배송비 계산 전체를 AS-IS `Shipping.getShippingGroups()` verbatim으로 이식했다.

- **신규**: `order DeliveryFeeCalculator`(묶음배송 그룹핑 + type1~6 + 제주/도서산간 + 착불 분기 전부).
- **G1 묶음배송**: shippingGroupCode 같은 것끼리 그룹당 1배송비(type1·6은 개별). 그룹 배송비는 그룹 첫 라인에 배분(합계는 AS-IS와 동일). ✅
- **G2 type2/3 + 물류통합**: type3+shipmentGroupCode면 물류그룹 전체 합계로 조건부무료 판정. ✅
- **G3 개당배송비**: `boxCount = ceil(총수량/shippingItemCount)` × (shipping+추가). ✅
- **G4 제주/도서산간 정식판정**: AS-IS OP_ISLAND를 `ord.op_island`로 포팅(엔티티 `Island`+`IslandRepository`, 우편번호 REPLACE 매칭 ORDER BY id DESC LIMIT 1). 체크아웃이 우편번호(form.post)를 preview/complete로 전달. 초기 데이터 0건 → 추가배송비 미발생(AS-IS와 동일). 기존 주소문자열 휴리스틱은 제거. ✅
- **G5 착불**: shippingPaymentType=2 → payShipping 0 분기 이식. 답례품은 포인트 선결제라 데이터 원천 없음(코드 parity, 데이터로 비활성). ✅
- **필드 배선**: gift `Gift`(shippingItemCount/shippingGroupCode/shipmentGroupCode/shippingReturn/deliveryType) → `GiftItemInfoDto` → order `GiftItemInfo` 전 구간 추가. op_item에 컬럼 기존재라 DDL 불요(island 테이블만 신규 `migration-order-island.sql`).
- 배선: `CartService.buildGroups`(장바구니, islandType "")·`priceSelected`(체크아웃, 우편번호 판정) 모두 라인별→지자체 그룹별 계산으로 전환. 활성 경로 `checkoutMultiItem`가 PricedLine.deliveryFee 사용하므로 자동 반영. 컴파일·CartServiceTest·storefront build OK.

**화면 parity 조치(2026-09-22)**: MSA admin `gift-items/form.html`이 이전 세션("SFR-005 재검토")에 배송비 설정을 **드롭다운으로 노출**하고 있었으나, AS-IS는 `<tr class="hidden">`로 숨김. 기준([[as-is-parity-includes-disabled-state]])대로 **배송비 설정 5행(구분·기본배송비·조건부무료·제주·도서산간)을 `display:none`으로 숨김**(input은 남겨 저장 시 값 보존). 택배사·반품가능여부는 AS-IS와 동일하게 노출 유지. 구매자 상세는 이미 "무료" 하드코딩으로 일치.

**G6/G7 (다른 서브시스템 downstream)**: 필드는 order까지 전달 완료. 단, 실제 효과는 배송비 *계산*이 아니라 다른 서브시스템에 있다 —
- **G6 반품배송비**: AS-IS `OrderClaimApplyServiceImpl:2513 apply.setCollectionShippingAmount(orderItem.getShippingReturn())` — 반품 클레임 시 고객이 부담하는 회수배송비. MSA `ClaimService`는 반품 시 포인트 전액복원(회수비 미차감)하는 단순화 모델. 재현하려면 클레임 환불 SAGA(order→point)를 회수비 차감으로 재구성해야 함. 실데이터 shipping_return 0건.
- **G7 본사/업체 정산**: AS-IS `Shipping`가 deliveryType=1(본사배송)이면 배송비 정산 대상을 운영자(DEFAULT_OPMANAGER_SELLER_ID)로 잡음 — *배송비 remittance* 레코드용(품목 정산과 별개). MSA엔 판매자별 배송비 정산 레코드가 없어 올바른 귀속처가 없음. OrderItem.sellerId에 얹으면 품목정산이 오귀속됨(부정확). 실데이터 delivery_type 전건 NULL.

## 5. 판정 결론 / 권고

- **실사용 범위(type3 조건부무료 + 무료)는 이미 MSA가 서버·화면 모두 AS-IS와 동등하게 재현 완료.**
- **화면 표기도 AS-IS와 동일**(무료 하드코딩, shippingTypeText 죽은코드까지 parity).
- 갭 G1~G7은 **AS-IS 코드엔 살아있으나 답례품 실데이터에 0건** → [[as-is-parity-includes-disabled-state]]의 "기능 있으면 다 만들되"에 해당하나, 실운영 데이터가 없어 검증 불가능한 순수 코드 parity.

권고: **배송비 정책은 실사용분 재현 완료로 마감**하고, 나머지 항목(카테고리 3단 트리 / 검색어 관리 / 판매자 셀프서비스)로 이동. G1~G7 순수코드 parity는 운영데이터 확보 후 재검토(보류). — 최종 결정은 사용자.

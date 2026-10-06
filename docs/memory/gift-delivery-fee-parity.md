---
name: gift-delivery-fee-parity
description: "배송비 정책 AS-IS 전수대조·구현 상태. G1~G5(배송비 계산 전체) 완료, G6(반품배송비)·G7(정산 라우팅)은 다른 서브시스템이라 결정 대기."
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T05:28:13.856Z
---

**배송비 정책** 전수대조+구현 (2026-09-22). 갭목록/판정: `docs/gift-delivery-fee-parity-audit.md`.

**실사용 판정**: 답례품 실데이터(public.op_item 26건)는 type3 출고지조건부 8건 + 무료 18건뿐. 제주/도서산간·묶음배송·개당배송비·반품배송비·업체배송은 전부 0건.
- 구매자 상세(details-main.html): "배송비 : 무료" 하드코딩(shippingTypeText 죽은 함수).
- **등록 화면(opmanager/item/form.jsp = 판매자 seller/item/form.jsp가 include)**: "배송비 설정"(shippingType·기본배송비·조건부무료·제주도서산간)은 `<tr class="hidden">`로 **숨김**. 택배사·배송구분·출고지·반품가능여부는 노출. 즉 배송비는 화면에서 설정 불가 = **AS-IS에서 비활성 기능**. 서버 계산 로직만 살아있음.

**사용자 결정**: "G1~G7 전부 구현".

**완료(G1~G5)**: AS-IS `Shipping.getShippingGroups()`를 `order DeliveryFeeCalculator`로 verbatim 이식.
- 묶음배송 그룹핑(shippingGroupCode, 그룹당 1배송비/첫 라인 배분), type2·3+물류통합(shipmentGroupCode 합계), 개당배송비 ceil(수량/shippingItemCount), 착불(shippingPaymentType=2→0) 분기.
- G4 제주/도서산간: AS-IS OP_ISLAND를 `ord.op_island`로 포팅(`Island`+`IslandRepository`, 우편번호 REPLACE 매칭). `migration-order-island.sql` 적용 완료(orderdb GRANT). 초기 0건→추가배송비 없음. 체크아웃이 우편번호(form.post) preview/complete로 전달. 기존 주소문자열 휴리스틱 제거.
- 필드 배선: gift `Gift`+5필드(shippingItemCount/shippingGroupCode/shipmentGroupCode/shippingReturn/deliveryType) → `GiftApiController.GiftItemInfoDto` → order `GiftItemInfo`. op_item 컬럼 기존재라 DDL 불요.
- CartService buildGroups(장바구니 islandType "")·priceSelected(우편번호 판정) 라인별→지자체그룹별 전환. 활성 경로 checkoutMultiItem 자동반영. 컴파일·CartServiceTest·storefront build OK. **재기동 시 활성**.

**화면 parity 조치**: MSA admin gift-items/form.html이 이전 세션(SFR-005)에 배송비 설정을 드롭다운 노출 중이었음 → AS-IS는 `<tr class="hidden">` 숨김이라, 배송비 설정 5행(구분·기본배송비·조건부무료·제주·도서산간)을 display:none 처리(input 남겨 저장 시 값 보존). 택배사·반품가능여부는 노출 유지. admin은 template 변경이라 재기동/새로고침 시 반영.

**결정 대기(G6/G7)**: 필드는 order까지 전달했으나 downstream이 배송비 계산이 아닌 다른 서브시스템.
- G6 반품배송비: AS-IS는 반품 클레임 회수비(collectionShippingAmount=shippingReturn) 차감. MSA ClaimService는 전액복원 단순모델 → 재현 시 클레임 환불 SAGA 재구성 필요.
- G7 본사/업체 정산: AS-IS deliveryType=1이면 배송비 remittance를 운영자로 귀속(품목정산과 별개). MSA엔 판매자별 배송비 정산 레코드 없음 → OrderItem.sellerId에 얹으면 오귀속(부정확). 둘 다 실데이터 0건.

관련 [[as-is-parity-includes-disabled-state]] [[gift-option-multiform-redesign]] [[order-single-item-vs-multiitem-decision]].

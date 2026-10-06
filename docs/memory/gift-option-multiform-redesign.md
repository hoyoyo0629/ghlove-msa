---
name: gift-option-multiform-redesign
description: "gift 답례품 옵션 다형체계(S/S2/S3/T+각인 필수추가정보+추가구성)를 AS-IS 동일 재현. 판매자화면 S2·T 숨김, 옵션단위 재고, DDL 불요(스키마 이관됨). 계획 docs/gift-option-redesign-plan.md, 갭목록 docs/gift-option-parity-audit.md"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T04:03:53.592Z
---

**결정(2026-09-22)**: gift 옵션을 AS-IS와 동일하게 재현. order 멀티아이템 재설계([[order-single-item-vs-multiitem-decision]]) 완료 위에 배선.

**전수대조 결과([[gift-option-parity-audit]])**: AS-IS 판매자 등록화면(seller/item/create → opmanager/item/form.jsp) 실측 —
- 옵션형태 라디오 S(선택형)·S3(3조합형) **노출**, S2(2조합형)·T(텍스트형옵션) **CSS hidden**.
- 각인의 실체 = 별도 "필수 추가정보"(itemTextOptionFlag, itemTextOptionTitle1~3) 섹션, **노출·현행 유지보수**.
- 추가구성(op_item_addition) 데이터 0행.
- 인벤토리 gift.tsv "옵션 미사용"은 매퍼 고아 한정이지 기능 전체 사장 아님(읽기+구매흐름 살아있음).

**방침([[as-is-parity-includes-disabled-state]])**: 기능은 S/S2/S3/T+각인+추가구성 **전부 풀구현**, 단 판매자 화면에서 S2·T는 AS-IS처럼 숨김. 판매자/운영자(admin) 분리 유지. 옵션 재고는 **옵션단위**(AS-IS parity).

**DDL 불요**: gift DB에 스키마 이관 완료 — op_item_option(name1/2/3·price·cost·nonmember·stock_flag/quantity/code·soldout·hide·display), op_item(item_option_type·item_text_option_flag·title1/2/3), op_item_addition, op_item_option_soldout. 예외: OrderItem 각인/optionId 컬럼 부족 시 Phase4 최소 ALTER.

**페이즈(docs/gift-option-redesign-plan.md)**: P1 gift 모델/서비스 · P2 판매자 등록화면(S2·T 숨김) · P3 구매자 storefront 상세 · P4 장바구니/체크아웃 배선 · P5 옵션단위 재고 SAGA · P6 조회/표시/포인트.

**채널 결정(2026-09-22, A안)**: ISP는 프론트 3종(대민/운영/제공자)이나 현 MSA는 온전한 독립 프론트 2개(storefront=대민, admin=운영)뿐. 제공자웹 분리는 [[provider-portal-split-deferred]] 계속 보류. 옵션 판매자 등록화면은 **현행 gift 판매자 포털(Thymeleaf, SellerPortalController)에 API-first로** 얹고, 훗날 제공자웹 신설 시 백엔드 재사용·뷰만 이식.

**진행상태: P1(gift 모델/서비스/구매자DTO) 완료 — gift 컴파일 OK.**
- 도메인: Gift에 itemOptionFlag/Type·itemTextOptionFlag·Title1~3·itemDataType·itemAdditionFlag; GiftOption 전 컬럼(name2/3·display_type·hide_flag·cost·nonmember·stock_code·schedule); GiftAddition+Id+Repository 신설.
- 서비스 GiftOptionService: `saveOptions(itemId,optionType,rows)`(S/S2/S3/T 전체 교체), `saveTextOptions`(각인 필수추가정보, 30자·`:|<>`금지 검증), `saveAdditions`(추가구성 op_item_addition 교체), `additionsOf`. 기존 단일 register/edit 호환 유지.
- 구매자 상세(GiftPublicApiController.detail): OptionDto에 optionType·name2/3 추가, AdditionDto 신설, DetailResponse에 additions 추가(옵션형태·각인은 gift 필드로 노출).
- API: PUT `/api/admin/gift-items/{itemId}/options/bulk|text|additions`.
**P2 완료(2026-09-22, gift 컴파일 OK)**: edit.html 옵션 에디터(형태 S/S2/S3/T, S2·T `display:none` 숨김, 조합 그리드, 각인, 추가구성) + 판매자 스코프 엔드포인트 GiftController GET/POST `/gifts/{id}/options`·POST `/text-options`·`/additions`(currentSellerId 소유권). /api/admin/** 옵션 API는 InternalApiAuthInterceptor 내부전용이라 판매자경로 신설.
**P3 완료(2026-09-22, storefront build OK)**: GiftDetailView 옵션형태 분기(S 단일·S2/S3 종속드롭다운 sel1→2→3→chosenOption), 각인 입력칸(textOption `||`), 추가구성 체크박스, 검증 V1/V2/V3. textOption·additionItemIds는 payload에 실리나 order 미소비→P4 배선(Jackson 무시라 무해).
**P4 완료(2026-09-22, gift·order 컴파일·테스트·storefront build OK)**: 최소 ALTER(od_cart_item·od_order_item text_option 컬럼, od_cart_item 유니크키에 text_option 포함=각인 다르면 별도라인, `database/ddl/migration-order-option-textoption.sql` 적용). CartItem/OrderItem textOption 필드, AddRequest textOption·additionItemIds, CartService.add(각인 스냅샷·4튜플 dedup·추가구성 별도라인), checkoutMultiItem OrderItem.textOption 복사, gift 단건옵션API 조합명(name1/2/3) 결합, CartLine·CheckoutLine·CartView·CheckoutView 각인표시. 부수로 이전세션 단일옵션 작업발 CartServiceTest 5건 정정(3-arg finder·7-arg createOrder).
**P5 완료(2026-09-22)**: 옵션단위 재고. ALTER od_order_item·GIFT_SHIPMENT_STOCK item_option_id(`migration-order-option-stock.sql`). OrderItem.itemOptionId(checkout 복사), ShipmentCreatedEvent.Line(order·gift) itemOptionId. gift reserveStockForShipment: option_stock_flag='Y'면 옵션재고(op_item_option.option_stock_quantity) 선검증·차감·품절갱신, 예약행에 itemOptionId; restoreReservation은 itemOptionId 있으면 옵션재고 복원. (op_item_option_soldout 요약은 미갱신, 옵션행 플래그로 직접 표기.)
**P6 완료(2026-09-22)**: OrderItemDto·DoneItemDto에 textOption, storefront OrderDetailView·OrderCompleteView "각인 [ ]" 표시. 포인트는 이미 OrderItem.pointAmount=(salePrice+optionPrice)*qty 포함이라 반영됨(코드 불요).

**★ Phase 1~6 코드 완료(2026-09-22). 전 서비스 컴파일·테스트·storefront build OK. 재기동 시 활성.**

**후속 보강(2026-09-22, 사용자 지적)**:
- 추가구성 = **AS-IS처럼 자식 답례품 생성**으로 제대로 구현(초기 "ID 링크" 단순화는 기준위반이었음). `GiftService.createAdditionChild`(item_data_type=2, 본품 seller/locgov/category 상속, displayFlag='N' 목록숨김·dataStatusCode='APPROVED' 주문가능) + `GiftOptionService.saveAdditions`(교체 시 자식 삭제) + edit.html 추가구성 (상품명·가격·재고) 그리드. GiftOptionService가 GiftService 주입(비순환).
- op_item_option_soldout은 **재현 불필요**(AS-IS 배치 파생캐시, MSA 미참조). 대신 목록 "옵션 전부 품절→품절뱃지"만 `GiftOptionService.optionSoldOutItemIds`로 조회시점 계산해 GiftPublicApiController.list soldOut에 OR. 시드 item 1026(재고50·옵션전부품절)로 판정 검증(실화면 재기동 후). 정본은 OP_ITEM_OPTION.OPTION_SOLD_OUT_FLAG(MSA 직접유지).
- **검증 시드**: gift DB item 1026=옵션전부품절(목록뱃지용), item 1027=S3+각인+추가구성(자식 1028 '선물포장 상자'), item 1025=단일옵션(플레인/딸기맛/초코맛품절).

**E2E 1차 피드백·수정(2026-09-22, order 컴파일·테스트 OK)**:
- **[버그] 품절 옵션이 장바구니/주문에 안 걸러짐** → AS-IS는 (a)장바구니 품절 자동제거(옵션재고까지 `isChangeSoldoutItem`), (b)주문단계 차단(OrderServiceImpl:8940/8961 throw→/cart). MSA 반영: `CartService.buildGroups`에 옵션품절 라인 자동제거(`isOptionSoldOut`), `priceSelected`에 옵션품절 주문차단 throw. (AS-IS는 품절 '뱃지'가 아니라 자동제거임.)
- **[오해아님] 옵션 재고 미차감**: 초코맛(품절)이 같은 출고에 섞여 출고 전체가 재고예약 실패(all-or-nothing)→전 품목 CANCELLED→딸기맛도 미차감. 정상 원자성. 근본원인=위 버그. 클린 주문이면 옵션재고 차감됨(딸기맛 opt2 stock_flag=Y·20).
- **목록 품절뱃지**: storefront GiftListView가 `g.soldOut`이면 sold-out.png 렌더(정상). optionSoldOutItemIds 뱃지코드는 E2E 이후 추가돼 **gift 재기동 필요**.
- **재기동 필요**: gift(옵션품절뱃지·추가구성자식·최신) + order(장바구니/주문 품절차단). 그 후 재E2E.
- 남은 후순위: S2·T 실데이터 E2E(판매자 숨김이라 시드 필요).

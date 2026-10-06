# gift 옵션(다형 옵션 체계) AS-IS 전수대조 갭목록

> 방법: [[as-is-parity-exhaustive-audit-method]] — 프론트 이벤트 + 검증로직 + 매퍼를 전수로 훑어
> AS-IS 능력을 먼저 확정하고, 실사용 여부와 MSA 갭을 판정한다. ISP/RFP 신규·변경 판단은
> 사용자 확인 후 착수. 작성 2026-09-22.

## 0. 결론 요약

- **AS-IS 옵션 능력은 코드 레벨에서 완전히 살아있다.** 선택형 단일(S)·조합형 2단(S2)·조합형
  3단(S3)·텍스트형(T, "각인")·추가구성(add option)까지 프론트 렌더링 → 장바구니/주문 전달 →
  마이페이지 표시가 end-to-end로 연결돼 있고, 옵션은 `insertItemOption`으로 저장된다.
- **인벤토리(2026-09-10 gift.tsv) "상품옵션/추가상품 미사용" 판정은 매퍼 일부(관리자 CRUD·엑셀·
  오픈마켓 잔재)에 대한 것**이고, 읽기경로(`item-front-mapper` 옵션 collection)와 프론트 구매흐름은
  살아있다. 즉 "기능 미사용"이 아니라 "특정 매퍼 statement 고아"가 정확한 판정이다. → **인벤토리
  판정과 코드 증거가 상충** (본 문서 §5에서 정정).
- **실사용(운영에서 실제로 쓰이는가)은 로컬로 확정 불가.** 로컬 gift DB의 `op_item_option`은
  3행 전부 S이고 단일 데모아이템(item_id=1025, 플레인/딸기맛/초코맛)에만 붙어 있으며, 실아이템
  23개는 `item_option_flag`가 전부 비어 있다. `op_item_addition`(추가구성)은 0행. → 이 데이터는
  이번 세션 "옵션 재고 표시" 테스트 시드로 보이며 **AS-IS 운영 실사용 판정은 운영 데이터가 필요**하다.
- **RFP 근거는 존재**한다: MSA `GiftOption`이 인용한 **SFR-005 "카탈로그 관리: 카테고리, 옵션,
  규격/구성"** — 즉 옵션 관리 자체는 요건 범위. 다만 S2/S3/T/추가구성까지 요건인지는 미확정.
- **MSA 현행은 의도적 축소**: `GiftOption`이 S(단일)만, 그나마 **구매에 미반영**(장바구니/주문에
  옵션 안 실림). 방금 끝낸 order 멀티아이템 재설계로 OrderItem에 `optionName/optionPrice`(단일)만
  탑재된 상태.

**핵심 미결 질문(사용자 확인 필요)** → §8.

---

## 0-1. 판매자 등록화면 실측 (결정적, 2026-09-22 추가)

사용자가 `http://localhost:8080/seller/item/create`를 확인하고 "옵션 사용 가능·3조합형까지 가능,
단 T(각인) 설정부분은 없어 보인다"고 판단. 그 화면 소스(`SellerItemController` →
`seller/i18n/item/create.jsp` → include `opmanager/i18n/item/form.jsp`)를 실측한 결과:

- **상품옵션 형태(itemOptionType) 라디오 4개 중 2개가 CSS `hidden`으로 미노출**:
  - `S` 선택형 → **노출** (form.jsp:1361)
  - `S2` 2조합형 → **숨김** `class="input-form hidden"` (form.jsp:1363~1365)
  - `S3` 3조합형 → **노출** (form.jsp:1367)
  - `T` 텍스트형 → **숨김** `class="input-form hidden"` (form.jsp:1369~1372)
  - JS에 `removeClass('hidden')` 없음 → S2·T는 런타임에도 계속 숨김.
  - → **판매자가 실제로 만들 수 있는 옵션형태는 선택형(S)·3조합형(S3) 뿐.** 사용자 관찰과 일치.
- **각인의 실체 = 별도 "필수 추가정보 등록" 섹션(`itemTextOptionFlag`)**, itemOptionType=T와 다른 기능:
  - 등록폼에 **노출**되는 독립 섹션(form.jsp:1571~ "■ 필수 추가정보 등록", 사용여부 Y/N 라디오 :1588~).
  - `itemTextOptionTitle1/2/3` 저장(최대 3개, 쉼표구분, `: | < >` 금지). 최근 유지보수 흔적
    "20260325 필수 추가정보"(form.jsp:2483) → **현행 운영 기능**.
  - 구매자 상세가 렌더: `v-show="item.itemTextOptionFlag === 'Y'"` + itemTextOptionTitle1/2/3 입력칸
    → `itemTextOptionValue1/2/3` (details-main.html:238~250).
  - 주문/장바구니로 `textOption`(`||` 구분)으로 흐름 (cart/index.html:149,441; order/step1.html).
- **즉 "각인"은 AS-IS에 실재하지만, itemOptionType='T'(숨김)가 아니라 `itemTextOptionFlag`(노출)로
  구현돼 있다.** 사용자가 T를 못 찾은 건 T 라디오가 숨겨져 있어서가 맞고, 각인 자체는 "필수 추가정보"
  섹션에 있다.

**정정된 실사용 결론**:
| 옵션형태 | 등록화면 노출 | 실사용 판정 |
|---|---|---|
| 선택형(S) | O | **사용** |
| 3조합형(S3) | O | **사용 가능**(운영데이터로 실제 편성량 확인 권장) |
| 2조합형(S2) | X(hidden) | **비활성**(마크업만 존재) |
| 텍스트형 옵션(itemOptionType=T) | X(hidden) | **비활성**(마크업만 존재) |
| 각인=필수 추가정보(itemTextOptionFlag) | O(별도 섹션) | **사용**(현행 유지보수) |
| 추가구성(itemAdditionFlag) | **O**(별도 섹션 "추가구성상품", form.jsp:1686~) | **활성**(데이터 0행이나 UI 노출→재현 대상) |

**추가구성 모델링(form.jsp 실측)**: 추가상품명(최대40자)·추가상품가격을 직접 입력하며, 각 추가구성은
`item_data_type=2`(추가구성상품)인 **별도 OP_ITEM**으로 생성되어 본품과 `op_item_addition`(item_id,
addition_item_id)로 연결된다. 쿠폰 등 고객혜택 미적용(옵션과 구분). → Gift 도메인에 `itemDataType`,
`itemAdditionFlag` 매핑 필요.

---

## 1. AS-IS 옵션 데이터 모델 (전수)

| 테이블/도메인 | 역할 | 근거 |
|---|---|---|
| `OP_ITEM` (ItemBase) | `ITEM_OPTION_FLAG`(옵션 사용 Y/N), `ITEM_OPTION_TYPE`(S/S2/S3/T) | ItemBase.java:82,287~311 |
| `OP_ITEM_OPTION` (ItemOption) | 옵션행: `OPTION_TYPE`, `OPTION_NAME1/2/3`, `OPTION_PRICE`, `OPTION_PRICE_NONMEMBER`, `OPTION_COST_PRICE`, `OPTION_STOCK_FLAG`, `OPTION_STOCK_CODE`, `OPTION_STOCK_QUANTITY`, `OPTION_SOLD_OUT_FLAG`, `OPTION_DISPLAY_FLAG` | ItemOption.java |
| `OP_ITEM_OPTION_GROUP` (ItemOptionGroup) | 옵션 그룹: `OPTION_TYPE`, `OPTION_TITLE`, `OPTION_DISPLAY_TYPE`, `OPTION_HIDE_FLAG` | item-front-mapper.xml:170~186 |
| `OP_ITEM_OPTION_SOLDOUT` | 아이템 단위 품절 요약(목록/상세 조인 대상) | item-front-mapper.xml 전 목록쿼리 LEFT JOIN |
| `OP_ITEM_OPTION_IMAGE` (ItemOptionImage) | 옵션별 이미지 | 도메인 존재 |
| `OP_ITEM_ADDITION` (ItemAddOption) | 추가구성(본품과 별도로 함께 담는 유료 부가품목) | item-addition-mapper.xml |
| 옵션 제목 | `ITEM_OPTION_TITLE1/2/3` (조합형 각 단계 라벨) | ItemDetailInfo.java:154~156 |

**옵션 타입(`itemOptionType`) 값**: `S`(단일 선택), `S2`(2단 조합), `S3`(3단 조합), `T`(텍스트=각인).
프론트 JS가 이 4값을 모두 분기 처리 (item_tab-ali.vue:922 `T`, 933 `S`, 950 `S2`, 3단 조합 로직).

---

## 2. 프론트 이벤트 전수 (item_tab-ali.vue = 답례품 상세 탭, details-main.html)

| # | 이벤트/렌더 | 동작 | 근거(item_tab-ali.vue) |
|---|---|---|---|
| F1 | `itemOptionFlag==='N'` | 옵션 없는 상품: 수량만 | :230, :942 |
| F2 | `itemOptionType==='S'` | 단일 드롭다운(optionName2 목록), `writeOptionName(1,...)` | :319~343, :979~1003 |
| F3 | `itemOptionType==='S2'` | 2단: 상위(optionName1)→하위(optionName2) 종속 드롭다운, 하위에 추가금액/[품절]/재고N개 표시 | :344~403, :1012~ |
| F4 | `itemOptionType==='S3'` | 3단: name1→name2→name3, 3번째 드롭다운 추가 | :404~440 |
| F5 | `itemOptionType==='T'` | 텍스트 입력(각인) — `textOptionValues[]` 배열, 옵션별 입력칸 | :660, :922~923 |
| F6 | 추가구성(addOption) | `addOptionList[index]` 별도 선택·수량 | :545, :574 |
| F7 | 옵션 조합 재고/품절/가격 산출 | `itemOptionInfo`에 optionPrices/optionSoldOuts/optionStockFlags/optionStockQuantity 배열 구성 | :967~1018 |
| F8 | 장바구니/주문 optionName 조립 | S: `optionName1 + ": " + selectOptionName1`; S2/S3: `title1:name1 \| title2:name2 [\| title3:name3]` | :1211~1216 |
| F9 | 세트답례품(itemType=3) | 모든 옵션 선택 시 세트 구성 추가 | :706 |

**구매흐름 전달 확인**: `order/step1.html`·`step2.html`·`cart/index.html`·`mypage/orderDetail.html`이
`textOption`(‖ `||` 구분, `formatTextOption`)과 optionType을 참조 → **옵션이 주문·마이페이지까지 흐른다**
(order/step1.html:134,962). 즉 읽기·구매·조회 전 경로에서 옵션이 살아있다.

---

## 3. 검증로직 전수

| # | 검증 | 메시지/동작 | 근거 |
|---|---|---|---|
| V1 | 필수옵션 미선택 차단 | "답례품 필수옵션을 선택하세요." (장바구니/즉시구매 진입 시) | item_tab-ali.vue:1081,1122 |
| V2 | 텍스트옵션(각인) 미입력 차단 | "{optionName1}의 옵션을 입력해주세요." | :747 |
| V3 | 옵션 품절 차단 | `optionSoldOuts[index]` → `:disabled`, [품절] 표기 | :386,393,413,420 |
| V4 | 옵션 재고연동 표기 | `optionStockFlag==='Y' && optionStockQuantity>0` → "재고 N개" | :396,423 |
| V5 | 옵션 추가금액 합산 | `optionPrices[index]>0` → "+금액" 표기 및 결제금액 반영 | :390,417 |
| V6 | 옵션 중복 조합 제거 | `checkDuplication`으로 상위 값 중복 축약 | :974~992 |

---

## 4. 매퍼 전수 (읽기 vs 쓰기 구분이 핵심)

**읽기경로 (살아있음)** — `item-front-mapper.xml`:
- `ItemOptionGroupResult`(:170) / `ItemOptionResult`(:188) resultMap + `<collection itemOptions>`(:176),
  `<collection itemOptionGroups>`(:158). 단 목록/상세 SELECT는 `OP_ITEM_OPTION_SOLDOUT`(품절요약)만
  LEFT JOIN하고, 옵션 상세행은 상세 서비스가 별도 로드.
- `ItemServiceImpl`: `getItemOptionList(itemId)`(:576, 프론트 상세), `getItemOptionListForManager`(:509,
  관리자). → **읽기 호출 존재**.

**쓰기경로 (관리자 상품등록/수정 시 저장)**:
- `ItemServiceImpl.insertItemOption`(:1231, :1493), `deleteItemOptionByItemId`(:1160,1164),
  `updateItemOption`(:703 mapper), `updateItemOptionStockQuantity...`, soldout 배치(`insert/deleteItemOptionSoldout`).
  → **옵션 저장/삭제/재고갱신 호출 존재**.

**고아/미사용 (인벤토리가 "미사용"으로 잡은 부분)**:
- `item-mapper.xml` ItemOption 관련 13건 중 일부(엑셀 일괄등록 `insertItemOptionListForExcel`,
  오픈마켓 연동 등) 호출 0 → 고아.
- `item-addition-mapper.xml` 6건 중 5건 사장(추가구성) → 인벤토리 "추가상품 기능 미사용".

> **정정**: 인벤토리 line 48/56의 "상품옵션/추가상품 기능 미사용"은 **매퍼 statement 고아** 수준이지
> "옵션 기능 전체 사장"이 아니다. 읽기+구매흐름은 살아있으므로 갭 판정에서 이를 분리해야 한다.

---

## 5. MSA 현행 대응

| 요소 | MSA 상태 | 근거 |
|---|---|---|
| 옵션 도메인 | `GiftOption`(OP_ITEM_OPTION 매핑) — S(단일)만, name1/price/stock/soldout/display | gift/domain/GiftOption.java |
| 옵션 관리 API | `GiftOptionAdminApiController` + `GiftOptionService` (등록/수정/삭제, 재고표시) | gift/web/GiftOptionAdminApiController.java |
| 프론트 옵션 | GiftDetailView: 단일 `<option>` 드롭다운 + "[품절]"/"재고 N개" (이번 세션 추가) | storefront GiftDetailView.vue |
| 구매 반영 | **없음** — GiftOption 주석: "장바구니/주문에 옵션이 반영되지 않음"(의도적 축소) | GiftOption.java:11~14 |
| 주문 옵션 | order 멀티아이템 재설계로 OrderItem에 `optionName/optionPrice`(단일) 탑재됨 | order/domain/OrderItem.java |
| S2/S3/T | 없음 | GiftOption.java:31 "조합형/텍스트형은 스코프 밖" |
| 추가구성 | 없음 | — |

---

## 6. 갭목록 (AS-IS ↔ MSA)

| # | 항목 | AS-IS | MSA 현행 | 실사용 근거(로컬) | 갭 판정 |
|---|---|---|---|---|---|
| G1 | 단일옵션(S) 관리/표시 | O | O(구매 미반영) | 데모 3행(S) | **부분** — 관리·표시는 됨, 구매 반영 필요 |
| G2 | 옵션 구매 반영(장바구니→주문) | O(optionName/textOption 전달) | X | 데모 | **미구현** — order 멀티아이템 위에서 배선 필요 |
| G3 | 3단 조합옵션(S3) | O(등록화면 **노출**·프론트/읽기 완비) | X | 운영데이터 편성량 확인권장 | **미구현**(실사용 확정 → 재현 대상) |
| G4 | 2단 조합옵션(S2) | 마크업 O / 등록화면 **숨김** | X | 비활성 | **재현 불요 추정**(사용자 확인) |
| G5 | 텍스트형 옵션(itemOptionType=T) | 마크업 O / 등록화면 **숨김** | X | 비활성 | **재현 불요 추정**(사용자 확인) |
| G5b | **각인=필수 추가정보**(itemTextOptionFlag, Title1~3) | O(등록화면 **노출**·현행 유지보수·구매흐름 완비) | X | **사용** | **미구현**(재현 대상, 실사용 확정) |
| G6 | 추가구성(itemAdditionFlag) | O(등록폼 노출·별도 item_data_type=2 + op_item_addition) | X | 데이터 0행(UI 활성) | **미구현**(재현 대상) |
| G7 | 옵션 필수선택 검증(V1) | O | 해당없음(단일뿐) | — | G2 종속 |
| G8 | 옵션 품절/재고 표시(V3/V4) | O | O(이번 세션) | 데모 | **대응** |
| G9 | 옵션 추가금액 합산(V5) | O | 표시만, 결제 미반영 | 데모(초코맛 +500) | **부분** — G2 종속 |
| G10 | 옵션 제목(title1/2/3) | O(조합형 라벨) | X | — | G3/G4 종속 |
| G11 | 옵션 이미지(OP_ITEM_OPTION_IMAGE) | O(도메인) | X | 미확인 | 실사용 확인 필요 |
| G12 | 옵션 재고 SAGA(주문 시 옵션단위 차감) | 옵션 재고차감 존재 | gift는 아이템단위 재고예약(멀티아이템)만 | — | **미구현** — 옵션단위 재고까지 갈지 결정 필요 |

---

## 7. 실사용 판정 (핵심)

- **코드**: S/S2/S3/T/추가구성 전부 end-to-end 살아있음 (§2·§3·§4 읽기·구매흐름).
- **인벤토리**: "미사용"은 매퍼 고아 한정, 옵션 기능 전체 사장 아님 (§4 정정).
- **로컬 데이터**: 옵션 3행 전부 S·단일 데모아이템, 실아이템 23개 옵션플래그 공백, 추가구성 0행
  → **테스트 시드로 판단, 운영 실사용 근거로 쓸 수 없음**.
- **RFP**: SFR-005가 "옵션" 카탈로그 관리를 명시 → 옵션 관리 자체는 요건.

→ **판정: 옵션 관리·단일옵션 구매반영(G1/G2)은 요건으로 확정 진행 가능. S2/S3/T/추가구성/옵션단위
재고(G3~G6,G11,G12)는 "AS-IS 운영에서 실제 사용됐는지"를 운영 데이터로 확인해야 신규/축소를 결정**한다.

---

## 8. 사용자 확인 필요 (착수 전 결정)

등록화면 실측(§0-1)으로 범위가 상당히 좁혀졌다. 확정 대상은 **S(단일)·S3(3조합형)·각인(필수 추가정보)**,
비활성 추정은 **S2·T옵션·추가구성**. 남은 확인:

1. **비활성 3종(S2·itemOptionType=T·추가구성)을 재현 대상에서 제외 확정?** 등록화면에서 숨김/데이터
   0행이라 미사용으로 보나, 최종 확인 요청.
2. **S3(3조합형) 실제 편성량**: 운영 DB `SELECT item_option_type, count(*) FROM op_item GROUP BY 1`로
   S3 답례품이 유의미하게 있는지. 소수/0이면 S(단일)만 우선하고 S3는 후순위.
3. **옵션 재고를 옵션단위로 관리(G12)할 것인가, 아이템단위로 둘 것인가?** 현재 멀티아이템 SAGA는
   아이템단위 예약. 옵션단위면 gift 재고예약·point 흐름 재설계 필요.
4. **최소 확정 범위 제안**: (a) G2 단일옵션 구매반영 + G9 추가금액 결제반영 + V1 필수선택검증,
   (b) G5b 각인(필수 추가정보) 구매반영. 둘 다 order 멀티아이템 위에 배선(OrderItem에 optionName/
   optionPrice 기탑재). 이후 2번 답에 따라 S3 확장.

---

## 9. 다음 액션 (승인 후)

- [ ] 사용자 답변(§8) 수령
- [ ] G2/G9 배선: GiftDetailView 옵션선택 → 장바구니 optionId → checkout OrderItem optionName/optionPrice/추가금액 반영 + V1 필수선택 검증 이식
- [ ] (요건 확정 시) S2/S3/T/추가구성 순차 확장 — 각 단계 프론트(F3~F6)·검증(V2)·데이터모델 이식

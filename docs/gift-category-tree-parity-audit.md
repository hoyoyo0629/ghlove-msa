# 카테고리 3단 트리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]]. 신규 기준 적용: [[defer-saleson-dependent-unused-features]]
(SalesOn 종속·미사용은 기록만 하고 보류, 단 이미 구현된 건 유지).

## 1. AS-IS 카테고리 계통 (두 갈래)

**A. 고향사랑 답례품 카테고리 (실사용)**
- 대분류: `GIFT_CATEGORY` 공통코드 — AGRI/SEAFOOD/LIVING/PROCESSED/VOUCHER.
- 답례품 헤더 `ghlove-frontend/components/layouts/header_g.vue`: 대분류(groups) > 중분류(topCategory) > 3단(childCategories) 3단 메가메뉴.
  - 대분류 클릭 `categoryGroupLink(url)` → `/goods/searchGoods.html?group=<url>&type=C`.
  - 중분류·3단 클릭 `categoryLink(url)` → `/goods/searchGoods.html?category=<url>&type=C`.

**B. SalesOn 원제품 카테고리 트리 (원제품 잔재)**
- `saleson.shop.categories`(Category 재귀 parent/child) + `categoriesedit` CRUD, 테이블 `OP_CATEGORY`(category_class1~4 + category_level = 최대 4단), 매핑 `OP_ITEM_CATEGORY`.
- 관리자 CRUD: `opmanager/categories/{create,edit,list}.jsp`, `categories-team-group`.

## 2. 실사용 판정 (MSA DB)

| 대상 | 데이터 | 판정 |
|---|---|---|
| 대분류 GIFT_CATEGORY | 26품목(AGRI 15·SEAFOOD 5·LIVING 2·PROCESSED 2·VOUCHER 2) | ✅ 실사용 |
| 중분류 gift_subcategory | 51건 | ✅ 실사용 |
| 3단 gift_subcategory_item | 40건(이름만, item 링크 없음) | ✅ 실사용(표시용 라벨) |
| **SalesOn op_category (4단)** | **0건** | SalesOn 종속·미사용 |
| **op_item_category (품목↔카테고리)** | **0건** | SalesOn 종속·미사용 |
| op_category_team_item | 2건 | (팀그룹, 별도) |

- AS-IS header_g.vue의 중분류/3단 `?category=` 필터는 `OP_ITEM_CATEGORY`(품목↔카테고리) 의존인데 MSA DB 0건 → **SalesOn 종속·현재 미사용**.

## 3. TO-BE(MSA) 현재 상태 — 이미 구현됨

- **모델**: `GiftSubcategory`(gift_subcategory) + `GiftSubcategoryItem`(gift_subcategory_item), 대분류=`Gift.categoryCode`(GIFT_CATEGORY).
- **서비스**: `GiftService.categoryTree()` — 대분류 > 중분류 > 3단 트리 조립.
- **스토어프론트**: `AppHeaderShopping.vue` 3단 메가메뉴(top/middle/detail_category). 대분류 클릭 → `categoryCode` 필터, 3단 품목명 클릭 → 검색어(q). (`GiftListView`도 categoryCode·q 지원)
- **관리자 CRUD**: gift `CategoryAdminApiController`(subcategory GET/POST/PUT/delete) + admin `CategoryAdminController`·`gift-categories/subcategory-form.html`, `CategoryTeamAdminApiController`·category-teams.

→ **대분류+중분류+3단 트리, 렌더링, 관리자 CRUD가 모두 이미 구현되어 있다.**

## 4. 갭목록

| # | 갭 | AS-IS | MSA | 판정 |
|---|---|---|---|---|
| C1 | 중분류/3단 단위 필터 | `?category=<url>` 필터(OP_ITEM_CATEGORY 의존) | 대분류=필터, 3단=검색어 | **SalesOn 종속·미사용(op_item_category 0건) → 보류+기록** |
| C2 | SalesOn 4단 트리(category_class1~4) + CRUD | opmanager/categories | op_category 0건, MSA 미구현 | **SalesOn 종속·미사용 → 보류+기록** |
| C3 | 품목↔중분류/3단 매핑 | OP_ITEM_CATEGORY | 없음(대분류만) | **SalesOn 종속·미사용 → 보류+기록** |

## 5. 결론 / 권고

- **live 답례품 카테고리 3단 트리(대분류 GIFT_CATEGORY + 중분류/3단 gift_subcategory 메가메뉴 + 관리자 CRUD)는 MSA에 이미 구현 완료** → [[defer-saleson-dependent-unused-features]]대로 그대로 둔다.
- **C1~C3(SalesOn op_category 4단 트리 · op_item_category 필터 · 품목↔중분류 매핑)은 SalesOn 종속·현재 미사용(DB 0건)** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "카테고리 3단 트리"는 **추가 구현 없이 마감**하고 다음 항목(검색어 관리)로 이동 권고. — 최종 결정은 사용자.

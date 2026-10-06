# 검색어 관리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] + [[defer-saleson-dependent-unused-features]].

## 1. AS-IS 검색 계통 (여러 갈래)

- **추천검색어**: `OP_SEARCH` + `SearchManagerController`(opmanager/search). 검색어·링크·노출기간 관리.
- **인기검색어/자동완성/일별집계**: `keyword-mapper.xml`(getBestKeyword·getAutoComplete·setDailyKeyword·clearDailyKeyword), `saleson.shop.keyword`.
- **금지어**: `banword-mapper.xml`.
- **통합검색**: `saleson.shop.integrationsearch`(외부 SearchEngine 연동, SearchApiResponse 등).
- **최근검색어**: `totalsearch-myrecent-mapper.xml`(클라이언트 로컬).

프론트 표기 — 답례품 헤더 `ghlove-frontend/components/layouts/header_g.vue`:
- 검색은 **단순 텍스트 입력** → `goSearchGoods({type:'T', keyword})`. 인기/추천/자동완성 UI 없음.
- `recommendSearch: {}`·`searchWord: ''` 데이터가 선언돼 있으나 **템플릿·메서드 어디에도 바인딩 안 됨 = 죽은 데이터** → 추천검색어 미노출.

## 2. 실사용 판정 (MSA DB / 소스)

| 대상 | 상태 | 판정 |
|---|---|---|
| 답례품 검색(키워드 q) | storefront GiftListView q → gift publicGifts(keyword) | ✅ 실사용 |
| 추천검색어 OP_SEARCH | admin.op_search **0건**, 프론트 미노출(AS-IS도 죽은 데이터) | 관리 기능만 존재 |
| 인기검색어/자동완성/일별집계 | SalesOn keyword system, 프론트 미노출 | SalesOn 종속·미사용 |
| 금지어 banword | SalesOn | SalesOn 종속·미사용 |
| 통합검색 integrationsearch | 외부 SearchEngine 연동 | SalesOn/외부인프라 종속·미사용 |

## 3. TO-BE(MSA) 현재 상태

- **답례품 검색**: storefront `AppHeaderShopping.vue`·`HomeView.vue` 검색박스 → `/gifts?q=`. gift `publicGifts(categoryCode, keyword)` 필터. ✅ 이미 동작.
- **추천검색어 관리**: `admin SearchKeyword`(OP_SEARCH) + `SearchKeywordAdminController`(list/new/edit/create/update/delete) + `search-admin/{list,form}.html`. **관리자 CRUD 이미 구현**. 프론트 미노출(= AS-IS와 동일).
- **인기검색어/자동완성/금지어/통합검색**: MSA 미구현.

## 4. 갭목록

| # | 갭 | 판정 |
|---|---|---|
| S1 | 추천검색어 프론트 노출 | AS-IS도 미노출(recommendSearch 죽은 데이터) → **노출 안 하는 게 parity**. admin CRUD는 이미 구현·유지 |
| S2 | 인기검색어/자동완성/일별집계 | SalesOn 종속·프론트 미노출·MSA 미구현 → **보류+기록** |
| S3 | 금지어(banword) | SalesOn 종속·MSA 미구현 → **보류+기록** |
| S4 | 통합검색(외부 SearchEngine) | SalesOn/외부 인프라 종속·MSA 미구현 → **보류+기록** |

## 5. 결론 / 권고

- **답례품 키워드 검색(q 필터)과 추천검색어 관리자 CRUD(OP_SEARCH)는 MSA에 이미 구현 완료** → 유지([[defer-saleson-dependent-unused-features]]: 이미 구현된 건 그대로 둠). 추천검색어 프론트 미노출도 AS-IS와 일치.
- **S2~S4(인기검색어·자동완성·일별집계·금지어·통합검색)는 SalesOn/외부 인프라 종속·현재 미사용** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "검색어 관리"는 **추가 구현 없이 마감** 권고. — 최종 결정은 사용자.

# 답례품(gift) 서비스 상세분석 — 2026-09-09

분석 기준: **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본:
- AS-IS: `C:\workspace\ghlove`
  - 화면 `ghlove-frontend/goods/index-main.html`(답례품 몰), `goods/searchGoods-main.html`(통합검색결과), `items/details-main.html`(답례품 상세), `totalsearch/index.html`(통합검색)
- RFP: `docs/requirements.md` SFR-005
- ISP: `docs/isp-detailed-design-summary.md` (p.253/445 엔티티 명세 — `op_item`/`op_item_option`/`op_seller`/**`rwd_item_view`**, p.217 Scale-out 강조, p.148 Core 서브도메인)

> 화면 대조는 라우트·파일명이 아니라 **제목·본문 마크업 기준**으로 했다(donation 분석에서 파일명만 보고 오판한 전례가 있어서다).

---

## 1. 요구사항 대비 충족 현황

| RFP SFR-005 항목 | 상태 | 근거 |
|---|---|---|
| 답례품 카탈로그 관리(카테고리/옵션/규격/가격/노출상태, 등록·수정·중지·폐지) | 충족 | `Gift`/`GiftOption`/`ItemImage`, 카테고리 3계층(`CategoryGroup`/`CategoryTeam`/`GiftSubcategory`) |
| 지자체 정책 관리(승인·반려, 노출기간, 한시/상시, 기부금액 대비 선택 기준) | 충족 | `dataStatusCode`(승인상태), `displayFlag`, `displayType`(한시/상시), `displayStartDate`·`displayEndDate`, **`minDonationAmount`** |
| 주문 연계(재고/판매수량, 품절, 배송비·택배사) — 이벤트 기반 | 충족 | `OrderSagaListener` / `StockReservedEvent`·`StockReserveFailedEvent`, `GiftOrderStock`, `shippingType`·`shippingFreeAmount` |
| 후기/문의/답변/신고 | 충족 | `Review`/`ReviewReport`/`Inquiry`/`InquiryReport` |
| 관리 화면 API 재설계(조회/검색/필터링) | 충족 | `GiftPublicApiController`/`GiftApiController` + 도메인별 Admin API 다수 |
| **비동기 이미지 처리(썸네일 소/중/대 3종)** | 충족 | `ThumbnailService`가 `@Async`로 small/medium/large 생성 |
| **대규모 트래픽 대응: 조회 전용 ReadModel 분리, 캐시·인덱스** | **미충족** | §3-2 |
| **행안부 표준 카테고리 체계 적용** | **미충족** | §3-3 |
| **중복 등록 방지(실시간 유사도 검사)** | **미충족** | §3-4 |
| **통합검색 고도화(검색엔진 활용)** | **미충족** | §3-5 |

ISP 대조:
- 엔티티 `op_item`/`op_item_option`/`op_seller` 대응 ✅ (`Gift`/`GiftOption`/`Seller`)
- **`rwd_item_view`(답례품조회 ReadModel — 조회전용 Search Index 계층 분리) 없음**(§3-2)
- ISP가 답례품관리를 **Scale-out 강조 서비스**(오토스케일링 Min/Max 최대 10)로 지목한 만큼 조회 최적화 계층이 전제돼 있다

---

## 2. 화면 대응표

| AS-IS | MSA | 비고 |
|---|---|---|
| `goods/index-main.html`(답례품 몰) | `list.html` | 대응. 키워드(`q`)·카테고리·지자체 필터 있음 |
| `items/details-main.html`(답례품 상세) | `detail.html` | 대응 |
| `goods/searchGoods-main.html`·`totalsearch/index.html`(통합검색) | (없음) | §3-5. 답례품 내 검색은 `list.html`에 있으나 **탭 기반 교차 도메인 통합검색은 없음** |
| (업무Web 제공자 화면) | `register`·`edit`·`my`·`inquiries`·`seller-dashboard` | §3-1 — **무인증 상태** |
| — | `my-reviews`·`my-qna`·`wishlist` | 마이페이지 답례품 관련 |

---

## 3. 기능 갭

### 3-1. 판매자 화면이 무인증 — `sellerId` 쿼리파라미터를 그대로 신뢰 (보안, 높음)
`SellerPortalController`·`GiftController`의 판매자 화면(`/my` 내 답례품 관리, `/register` 등록, `/edit` 수정, 문의답변·재고조정)이 **`sellerId`를 URL 쿼리파라미터로 받아 무인증으로 신뢰**한다. 로그인도, JWT 검증도, 소유권 확인도 없다.

**실측(2026-09-09)**: 쿠키·토큰 없이
- `GET /my?sellerId=9001` → 200, "내 답례품 관리" 화면 렌더
- `GET /my?sellerId=9999` → 200 (존재하지 않는 판매자도 통과)
- `GET /register?sellerId=9001` → 200

즉 **아무나 임의 판매자를 사칭해 답례품을 등록·수정하고 문의에 답변할 수 있다.** 조회가 아니라 **쓰기 경로**라는 점이 핵심이다. 코드 주석에도 "무인증 스푸핑 가능 임시툴"로 적혀 있어 인지된 상태이지만, 다른 서비스가 SFR-010으로 IDOR을 정리한 것과 달리 여기만 남아 있다.

관련 보류 건: 제공자 포털 분리(`provider-portal-split-deferred`). 다만 **포털을 어디에 두느냐와 무관하게 인증은 지금 필요하다** — 분리 결정을 기다릴 사안이 아니다.

### 3-2. 조회 전용 ReadModel·캐시 없음 (요구사항 미충족, 중)
RFP가 **"대규모 트래픽(연말 성수기 12월) 대응: 조회 전용 ReadModel 분리, 캐시·인덱스 최적화, 오토스케일링"**을 명시하고, ISP는 엔티티 명세에 **`rwd_item_view`(조회전용 Search Index 계층 분리)**를 못박는다. 현재 gift에는 조회 전용 모델도, 캐시(`@Cacheable`/Redis)도 없다. admin 통계용 이벤트(`GiftLifecycleEvent`)는 있으나 gift 자신의 조회 계층이 아니다. point·member·donation과 같은 유형의 갭이지만, **ISP가 이 서비스를 Scale-out 대상으로 특정했다는 점에서 우선순위가 더 높다.**

### 3-3. 행안부 표준 카테고리 체계 미적용 (요구사항 미충족, 중)
RFP "행안부 표준 카테고리 체계 적용(매핑, 메타데이터 강화, 추가/변경 용이한 확장 설계)". 현재 카테고리는 자체 3계층(`CategoryGroup`/`CategoryTeam`/`GiftSubcategory`)이고 표준 코드 매핑 필드나 메타데이터가 없다.

### 3-4. 중복 등록 방지(실시간 유사도 검사) 미구현 (요구사항 미충족, 소~중)
RFP "중복 등록 방지(등록 시 실시간 유사도 검사)". 등록 시 유사 답례품 검사 로직이 없다.

### 3-5. 통합검색 미구현 (재현 누락 + 요구사항 미충족, 중)
AS-IS는 `totalsearch/index.html`(탭 기반 교차 도메인 통합검색)과 `goods/searchGoods-main.html`(답례품 검색결과)을 갖는다. MSA는 `list.html`의 키워드 필터만 있고 **통합검색 화면 자체가 없다**. RFP도 "답례품 통합검색 기능 고도화(**검색엔진 활용**)"를 요구하는데 OpenSearch/Elasticsearch 의존성·설정이 없다(ISP는 백킹서비스로 OpenSearch 채택, p.469).

---

## 4. 레이아웃 (AS-IS 기준)

**CSS 링크 누락 문제 없음.** AS-IS가 링크하는 `event.css`/`header-footer_ali.css`/`joind-agf.css`/`popup.css`/`order-modal.css`/`lclgv_chc/map.css`가 gift에 없지만, **클래스 단위 대조 결과 gift 템플릿이 실제로 쓰는 것은 링크된 CSS로 전부 커버**된다.

- `.modal*` 계열이 미정의로 잡혔으나 `modal-dialog` 등은 링크된 `new.css`/`output.css`에 있다 — donation과 동일한 오탐.

→ 이 결함 유형의 실사례는 여전히 order의 `mypage-order.css` 하나뿐이다(member·point·donation·gift 모두 정상).

---

## 5. 조치 / 잔여

이번 분석에서는 조치를 진행하지 않았다.

| # | 항목 | 규모 / 필요한 결정 |
|---|---|---|
| **3-1** | **판매자 화면 무인증** | **보안 건이라 우선.** 다만 "판매자 인증을 어떻게 할지"(member의 PROVIDER 계정으로 로그인시킬지, 별도 자격증명을 둘지)가 제공자 포털 분리 결정과 얽힌다 — 결정 필요 |
| 3-2 | 조회 ReadModel·캐시 | point ReadModel이 DB 설계 대기 중이라 같은 시점에 판단. ISP Scale-out 대상이라 우선순위 높음 |
| 3-3 | 행안부 표준 카테고리 | 표준 코드표 확보가 선행 |
| 3-4 | 중복 등록 유사도 검사 | 판정 기준(상품명·이미지·제공자 조합) 결정 필요 |
| 3-5 | 통합검색 | 검색엔진 도입 여부(ISP는 OpenSearch)와 검색 대상 도메인 범위 결정 필요 |

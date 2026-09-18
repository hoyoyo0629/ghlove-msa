# 주문·배송·클레임(order) 서비스 상세분석 — 2026-09-10

분석 기준: **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본:
- AS-IS: `C:\workspace\ghlove`
  - 화면 `ghlove-frontend/cart/index.html`(장바구니), `order/step1.html`(주문결제), `order/step2.html`(주문완료), `mypage/orderDetail.html`(주문상세), `mypage/orderList.html`·`orderCancel.html`
- RFP: `docs/requirements.md` SFR-006
- ISP: `docs/isp-detailed-design-summary.md` (p.161 이벤트스토밍 — 주문관리 명령/이벤트/조회모델)

> 화면 대조는 라우트·파일명이 아니라 **제목·본문 마크업 기준**으로 했다.
> 2026-09-09에 이미 조치한 건(주문조회·취소반품교환 화면 AS-IS 재구축, `mypage-order.css` 도입, `CLAIM_TYPE`에 `CANCEL` 추가)은 이 문서의 갭 목록에서 제외했다.

---

## 1. 요구사항 대비 충족 현황

| RFP SFR-006 항목 | 상태 | 근거 |
|---|---|---|
| 주문 생성 및 상태관리(장바구니·결제·주문상태) | 충족 | `CartService`/`CheckoutController`/`OrderService`, `cart.html`·`checkout.html`·`order-complete.html` |
| **포인트 차감·복원 SAGA** | 충족 | `OrderSagaPublisher`/`OrderSagaListener`, `POINT_DEDUCTED`·`POINT_DEDUCT_FAILED`·`STOCK_RESERVED`·`STOCK_RESERVE_FAILED` 보상 흐름 |
| 배송관리(송장등록·상태추적·배송완료·수취확인·배송지변경) | 충족 | `carrierCode`/`invoiceNo`/`deliveryStatus`/`shippedDate`, `confirmReceipt()`·배송지 변경(둘 다 **본인 확인** 포함) |
| **택배사 연계는 admin 담당(역할 분리)** | 충족 | 택배사 조회는 admin의 `DeliveryTrackingResult`(모크). order에는 연계 코드 없음 — RFP의 역할 분리를 지킴 |
| 클레임(주문취소·반품/교환·승인/거절·회수/재발송·포인트복원·재고복원) | 충족 | `ClaimService` 상태흐름 `REQUESTED → APPROVED → COMPLETED`(회수 확인 시점에 주문취소 + 재고·포인트 보상 트리거) |
| 이벤트 기반 비동기 연계 | 충족 | `ORDER_CREATED`/`CONFIRMED`/`CANCELLED`/`DELIVERY_UPDATED`/`CLAIM_UPDATED` 5종 발행 |
| **통계·SLA ReadModel 제공** | 충족 | order가 발행하고 **admin이 실제로 구독해 `OrderLedger`·`ClaimLedger`로 보유**. RFP가 "운영관리 대시보드에서 활용"이라 했으므로 소비처까지 성립 |
| **배송 자동화·구매확정 프로세스 개선**(배송완료 자동처리, 구매확정 기간 단축) | **미충족** | §3-1 |

ISP 대조(p.161):
- 명령/이벤트(주문생성·취소·확정, 운송장등록, 배송완료처리, 반품·교환 접수/승인/반려/종료) 대응 ✅
- 조회모델 "주문상세/이력/정제상태뷰, 배송추적/SLA뷰, 클레임사건/상태뷰" — **admin 쪽 ReadModel로 충족** ✅ (order 자체 조회모델은 없으나 ISP·RFP 모두 소비처를 운영관리로 지정)
- "택배사어댑터API, 웹훅" — 택배사 연계가 admin 소관이고 현재 모크

**다른 서비스 대비 특징**: point·member·donation·gift는 ReadModel이 통째로 없는 반면, **order는 SLA·클레임 ReadModel을 이벤트로 admin에 공급하는 구조가 실제로 동작한다.** 6개 서비스 중 CQRS 요구를 가장 충실히 만족한 서비스다.

---

## 2. 화면 대응표

| AS-IS | MSA | 비고 |
|---|---|---|
| `cart/index.html`(장바구니) | `cart.html` | 대응 |
| `order/step1.html`(주문결제) | `checkout.html` | 대응 |
| `order/step2.html`(주문완료) | `order-complete.html` | 대응 |
| `mypage/orderList.html` | `my.html` | 2026-09-09 AS-IS 6열 표로 재구축 완료 |
| `mypage/orderCancel.html` | `claims/my.html` | 2026-09-09 AS-IS 3열 표로 재구축 완료 |
| **`mypage/orderDetail.html`(주문상세)** | `detail.html` | **디자인 미재현**(§3-2) |
| (운영자 화면) | `claims/queue.html` | 내부 골격 화면. AS-IS 대응은 admin 소관 |
| — | `coupon/claimable`·`coupon/my`·`coupon/offline-claim` | 쿠폰 기능 미사용으로 진입점 숨김 상태(별건) |

---

## 3. 기능·레이아웃 갭

### 3-1. 배송완료 자동처리·구매확정 기간 단축 미구현 (요구사항 미충족, 중)
RFP가 **"배송 자동화 및 구매확정 프로세스 개선(배송완료 자동처리, 구매확정 기간 단축, 클레임 처리 간소화)"**을 요구한다. 현재 order의 `@Scheduled`는 **쿠폰 배치 하나뿐**이고, 배송완료 자동 전환이나 일정 기간 경과 시 자동 구매확정 배치가 없다. 수취확인(`confirmReceipt`)은 **구매자가 직접 눌러야만** 일어난다.

### 3-2. 주문상세 화면이 AS-IS 디자인으로 재현되지 않음 (레이아웃, 중)
`order/detail.html`은 **`style.css` 하나만 링크**하고 사이트 헤더·푸터 프래그먼트도 쓰지 않는 **내부 골격 화면**이다(107줄, `<h1>주문 상세<span class="badge">order</span></h1>` 형태). AS-IS 대응 화면 `mypage/orderDetail.html`은 1,111줄의 정식 마이페이지 화면으로 `mypage-order-details.css`를 쓴다(이 CSS는 order 모듈에 없다).

**회원이 실제로 쓰는 화면**이라는 점이 중요하다 — 2026-09-09에 재구축한 주문조회 목록의 "주문상세" 링크가 이 화면으로 들어온다. 즉 목록은 AS-IS 디자인인데 상세로 넘어가면 내부 툴 화면이 나온다.

> `claims/queue.html`도 같은 상태이나 이쪽은 운영자용이고 AS-IS 대응이 admin 소관이라 성격이 다르다.

### 3-3. 나머지 화면 CSS 링크는 정상
`cart.html`·`checkout.html`·`order-complete.html`은 AS-IS가 링크하는 `popup.css`/`header-footer_ali.css`/`joind-agf.css`/`lclgv_chc/map.css`가 없지만, 클래스 단위 대조 결과 링크된 CSS로 커버된다.

→ 이 결함 유형(클래스는 쓰는데 CSS를 안 들여옴)의 실사례는 **order 모듈에 집중**돼 있다 — `mypage-order.css`(조치 완료)와 이번 `detail.html`(미조치). member·point·donation·gift는 모두 정상이었다.

---

## 4. 조치 내역 (2026-09-10)

### 3-2 주문상세 AS-IS 디자인 재현 (구현)
- `mypage-order-details.css`를 order 모듈에 도입(`/static/` 경로 정정, 참조 이미지 전량 존재 확인). 누락 아이콘 `donation-warning.png` 복사.
- `detail.html`을 AS-IS `mypage/orderDetail.html` 구성으로 재작성: 빵부스러기 + "주문 상세" 타이틀 → **주문번호/주문일자 카드**(`row_con flex order_case`) → **지자체 그룹별 답례품정보(70%)/주문·배송상태(25%) 표** → **배송지 정보 + 결제정보 2단**. 사이트 헤더·푸터 프래그먼트도 붙였다(이전에는 없었다).
- 답례품 썸네일·지자체명은 주문 모델에 없는 값이라 목록 화면과 **같은 조립기(`OrderRowSupport`)를 재사용**해 한 건짜리 행으로 만들어 넘긴다.
- **기존 기능 전량 보존**: 주문취소 / 반품·교환 신청 / 배송지 변경 / 송장 등록 / 배송상태 변경 / 수취확인(구매확정). 렌더된 폼 `action`으로 확인했다.

**작업 중 발견한 CSS 충돌**: 처음에 목록용 `mypage-order.css`를 같이 링크했더니 그쪽 `.row_con { display:flex }`가 상세용 블록 레이아웃을 덮어써서 상태 텍스트가 세로로 쪼개지고 표가 반쪽 폭이 됐다. **AS-IS orderDetail도 `mypage-order.css`를 링크하지 않는다** — 링크를 빼서 해결했고 템플릿에 사유를 주석으로 남겼다.

검증: 렌더 확인(깨진 이미지 0, 문서 폭 1268), 목록·상세·취소반품교환 3개 경로 200, 폼 action 6종 조건별 정상 노출.

### 의도적 축소 (기록)
- AS-IS는 한 주문에 여러 답례품이 담겨 지자체별로 그룹을 나누지만 이 MSA는 **주문 1건 = 답례품 1건** 모델이라 그룹이 항상 하나다.
- 옵션(`options`/`textOption`)은 주문 모델에 없는 필드라 표시하지 않는다.
- 송장등록·배송상태 변경은 원래 제공자/운영자 화면 기능인데 이 MSA에 그 화면이 없어 상세 화면에 "운영자용"으로 남겼다(기존 동작 유지).

## 5. 잔여 (미조치)

| # | 항목 | 규모 / 필요한 결정 |
|---|---|---|
| 3-1 | 배송완료 자동처리·자동 구매확정 | **정책 결정 선행** — 배송완료 후 며칠에 자동 구매확정할지, 배송완료 자동 전환을 택배사 연계(admin, 현재 모크)에 의존시킬지 |
| — | `claims/queue.html` | 운영자용 내부 골격 화면. AS-IS 대응이 admin 소관이라 admin 분석 시 함께 판단 |
| — | 쿠폰 화면 | 쿠폰 기능 미사용 결정에 따른 숨김 상태(별건, 기존 결정 유지) |

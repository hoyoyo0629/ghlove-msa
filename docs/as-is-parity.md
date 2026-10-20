# AS-IS ↔ TO-BE 대조 원장

> 2026-10-06에 docs/ 최상위 .md 46개를 6개로 통합했다. 이 파일은 **AS-IS 대조·갭·커버리지 문서 21개**를 합친 것이고,
> 각 절 머리의 `통합 전 파일`이 원래 파일명이다(내용은 그대로 옮기고 헤딩만 한 단계 내렸다 - git 이력으로 원본 추적 가능).
> 구성 순서: 공통 커버리지 → admin → member → donation → gift → order → point.
> 진척 상태는 이 문서가 아니라 메모리 원장(`admin-*-area-port-progress` 등)이 정본이다.

## 목차

- [1. 전체 커버리지 맵 (어느 화면이 어느 인벤토리에)](#1-전체-커버리지-맵-어느-화면이-어느-인벤토리에) — `as-is-coverage-map.md`
- [2. 전수 커버리지 원장 (파일 단위, 누락 0 목표)](#2-전수-커버리지-원장-파일-단위-누락-0-목표) — `coverage-ledger.md`
- [3. 대민 SPA 충실도 감사](#3-대민-spa-충실도-감사) — `as-is-fidelity-audit-2026-09-17.md`
- [4. admin 기능 대조](#4-admin-기능-대조) — `as-is-feature-audit-admin.md`
- [5. admin parity 감사](#5-admin-parity-감사) — `admin-parity-audit.md`
- [6. admin 메뉴트리 parity](#6-admin-메뉴트리-parity) — `admin-menu-tree-parity.md`
- [7. admin 갭 심층감사 part1](#7-admin-갭-심층감사-part1) — `as-is-admin-gap-deep-audit-part1.md`
- [8. admin 갭 심층감사 part2 (배치D)](#8-admin-갭-심층감사-part2-배치d) — `as-is-admin-gap-deep-audit-part2.md`
- [9. 업로드 파일 규칙 parity (확장자·용량·파일삭제)](#9-업로드-파일-규칙-parity-확장자용량파일삭제) — `upload-file-parity-audit.md`
- [10. member 기능 대조](#10-member-기능-대조) — `as-is-feature-audit-member.md`
- [11. member 소셜 연동해지 parity](#11-member-소셜-연동해지-parity) — `member-social-unlink-parity-audit.md`
- [12. donation 기능 대조](#12-donation-기능-대조) — `as-is-feature-audit-donation.md`
- [13. donation parity 감사](#13-donation-parity-감사) — `donation-parity-audit.md`
- [14. gift 기능 대조](#14-gift-기능-대조) — `as-is-feature-audit-gift.md`
- [15. gift 옵션 체계 parity](#15-gift-옵션-체계-parity) — `gift-option-parity-audit.md`
- [16. gift 카테고리 3단 트리 parity](#16-gift-카테고리-3단-트리-parity) — `gift-category-tree-parity-audit.md`
- [17. gift 검색어 관리 parity](#17-gift-검색어-관리-parity) — `gift-search-keyword-parity-audit.md`
- [18. gift 배송비 정책 parity](#18-gift-배송비-정책-parity) — `gift-delivery-fee-parity-audit.md`
- [19. gift 판매자 셀프서비스 parity](#19-gift-판매자-셀프서비스-parity) — `gift-seller-selfservice-parity-audit.md`
- [20. order 기능 대조](#20-order-기능-대조) — `as-is-feature-audit-order.md`
- [21. point 기능 대조](#21-point-기능-대조) — `as-is-feature-audit-point.md`
- [22. admin 영역별 이식 원장](#22-admin-영역별-이식-원장) — 메모리 영역 원장 23개(2026-10-20 이동)
- [23. 배치 job 이식 원장](#23-배치-job-이식-원장) — `batch-job-port-ledger.md`
- [24. admin 발명 링크 정리 원장](#24-admin-발명-링크-정리-원장) — `admin-invented-backlink-cleanup-ledger.md`

---

## 1. 전체 커버리지 맵 (어느 화면이 어느 인벤토리에)

> 통합 전 파일: `docs/as-is-coverage-map.md`

## AS-IS 전체 커버리지 맵 — 어느 화면이 어느 인벤토리에 들어가는가

> 6개 MSA 서비스(member/donation/point/gift/order/admin)에 걸리지 않는 영역이 상당하다.
> 누락 없이 다루기 위해 AS-IS 프론트 **최상위 디렉터리 전부**를 도메인에 배정하고,
> 어디에도 안 걸리는 것은 **7번째 시트 `common`** 으로 모은다.
> 작성 2026-09-10. 생사 판정 기준은 [[asis-sources-and-data]].

`live` = 백업/날짜본/`old/` 제외 후 남는 .html 수 (도달 가능 여부는 도메인별 인벤토리에서 개별 판정)

### 배정표

| AS-IS 디렉터리 | live/total | 배정 | 비고 |
|---|---|---|---|
| `users/` | 13/27 | **member** | 완료 |
| `mypage/` (회원분) | 21/36 | **member** | 배송지·회원정보 |
| `mypage/` (cntrList, receipt*, honorList*, intrstLocGov) | ↑ 포함 | **donation** | 기부내역·기부확인증·명예의전당·관심지자체 |
| `mypage/` (cntrPoint, cntrPointDetail) | ↑ 포함 | **point** | |
| `mypage/` (orderList, orderDetail, orderCancel, deliveryInfo) | ↑ 포함 | **order** | |
| `mypage/` (review, writeReview, favorItem, inquiryItem) | ↑ 포함 | **gift** | |
| `donation/` | 30/44 | **donation** | guide1~6 안내화면 포함 |
| `designated-donation/` | 3/3 | **donation** | 지정기부 |
| `featured/` | 5/5 | **donation** | 지역이벤트(기부하기 하위) |
| `goods/` | 4/4 | **gift** | 답례품 |
| `items/` | 2/3 | **gift** | 답례품 상세 |
| `catalog/` | 3/3 | **gift** | |
| `category/` | 1/1 | **gift** | |
| `community-business/` | 1/1 | **gift** | 답례품 > 마을기업 |
| `cart/` | 1/2 | **order** | |
| `order/` | 2/5 | **order** | |
| `admin/` | 7/7 | **admin** | 주문대행 등 |
| **`notice/`** | 2/3 | **common** | 공지사항 |
| **`faq/`** | 1/2 | **common** | FAQ |
| **`qna/`** | 4/4 | **common** | 1:1 문의 |
| **`data-board/`** | 2/2 | **common** | 고객센터 > 자료실 |
| **`qustnr/`** | 2/2 | **common** | 온라인 설문 |
| **`event/`** | 8/8 | **common** | 이벤트 |
| **`totalsearch/`** | 2/2 | **common** | 통합검색 (RFP 신규 대상 확인 필요) |
| **`policy/`** | 21/21 | **common** | 약관·개인정보처리방침 원문 (policy-content-db-migration-deferred(보관: docs/archive/memory-fix-log-2026-10.md)) |
| **`guide/`** | 1/2 | **common** | 사이트맵 |
| **`error/`** | 1/1 | **common** | 오류 화면 |
| **`popup/`** | 1/1 | **common** | 팝업 |
| **`newsletter/`** | 77/77 | **common** | 소식지 — 발행분 정적 아카이브. 애플리케이션 기능인지 콘텐츠인지 판정 필요 |
| **`components/layouts/`** | (vue) | **common** | **GNB/LNB/헤더/푸터/플로팅** — 전 화면 공통 |
| **루트 `*.html`** | 11개 | **common** | `main.html`(메인), `index.html`, `intro.html`, `check.html`, `healthcheck.html` + 백업 6 |
| `pkiContent/` | 13/13 | 제외 | 공동인증서 SDK 정적자산(외부 제공) |
| `webDrm/` | 31/31 | 제외 | 문서보안 SDK 정적자산(외부 제공) |
| `modules/`, `static/`, `common/`, `public/`, `components/`(레이아웃 외) | - | 제외 | 스크립트·이미지 등 자산 |

### `common` 시트가 다룰 항목

1. **레이아웃/내비게이션** — GNB(대메뉴 구조·드롭다운), LNB(도메인별 좌측메뉴), 헤더(로그인상태·검색·장바구니), 푸터, 플로팅 버튼, 브레드크럼
   - 살아있는 레이아웃 컴포넌트 판별이 선행돼야 한다(`header_ali.vue` 계열이 live, `header.vue`/`header_ali_as-is.vue`/`header_ali_20240529.vue` 등은 사장 후보 — member 작업에서 `donation-lnb_ali.vue`가 **활성 등록 0**으로 확인된 전례가 있다).
2. **게시판류** — 공지사항, FAQ, 1:1문의(QnA), 자료실, 설문, 이벤트. 각각 목록·상세·검색·페이징·첨부·권한.
3. **안내/정책 화면** — 약관 21종, 사이트맵, 오류화면, 팝업.
4. **통합검색** — 전 도메인 교차검색. RFP 신규 요건 여부 확인 필요.
5. **메인화면** — 배너 캐러셀, 추천 답례품, 기부현황 위젯 등 여러 도메인을 끌어 쓰는 화면.
6. **소식지(newsletter)** 77건의 성격 판정.

### 서버측 미배정 영역 (별도 확인 대상)

프론트에 대응 화면이 없는 백엔드 기능도 `common` 또는 admin으로 배정해야 한다:
- `ghlove-batch` (배치 1개 컨트롤러 + 배치 잡)
- `ghlove-common`의 공통 인프라 — 코드관리, 파일업로드, 메시지, 시퀀스, 토큰, 캐시, 이력(change-log), 엑셀다운로드 로그, 금칙어(banword), 접속통계(access)
- 외부연계 모듈 `onepass/`, `payment/`, `simpleauth/`, `magicline/`

---

### 판정 결과 (2026-09-10, [[as-is-inventory-common]] 작성 시점)

작성 당시 열어뒀던 질문들의 답:

| 미결 항목 | 판정 |
|---|---|
| 살아있는 레이아웃 컴포넌트 판별 | 41개 중 **live 14 / 사장 27**. `header_ali`·`footer_ali`가 주 레이아웃. `customer-lnb_ali`(12곳 전부 주석)·`donation-lnb_ali`(6곳 전부 주석)·`totalSearch`(등록 주석)는 사장. `subsearch_ali.vue`는 6화면이 등록하나 **파일 자체가 없음** |
| 통합검색이 RFP 신규 대상인가 | **아니다 — AS-IS 기존 기능**이다. 다만 헤더 진입은 주석 처리돼 사장이고, 살아있는 진입점은 **메인화면 검색창 하나**. 자동완성·최근검색어·인기검색어는 재현 대상 아님 |
| 소식지 77건의 성격 | **콘텐츠(정적 아카이브)**. 컨트롤러·매퍼 없음. 진입점은 플로팅 버튼 1곳. 코드 이관 대상 아님 |
| 외부연계 모듈 `onepass/`·`payment/`·`simpleauth/`·`magicline/` | **자바 코드가 0개** — 전부 인증서·키·설정 자산 디렉터리다. 실제 코드는 `saleson/api/{payment,magicline}`, `saleson/common/sns` 에 있고 각각 donation(PG)·member(전자서명·SNS) 도메인 소관 |
| `ghlove-common` 공통 인프라 | common 시트 §8에 전수 배정 |
| `ghlove-batch` | common 시트 §10 — 잡 78개 분류 완료 |

추가로 확인된 것:

- `com.onlinepowers.board` **범용 게시판 프레임워크는 통째로 미사용** (24 엔드포인트/47 쿼리).
  live 프론트에서 `/board/` 링크 0건 → 이관 대상에서 제외. `DemoController`(샘플) 5종도 동일.
- `com.onlinepowers.framework.*` 매퍼 52쿼리는 인터페이스가 `libs/opframework-3.15.0.jar`
  내부라 **소스 grep으로 사장 판정이 불가능** → 판정보류로 분리.
- AS-IS **메인화면의 섹션 6종(MD추천·신규·인기 답례품, 스타일북, 이벤트, 프로모션)은
  메서드만 있고 호출·바인딩이 없다** → 재현 불필요.

---

## 2. 전수 커버리지 원장 (파일 단위, 누락 0 목표)

> 통합 전 파일: `docs/coverage-ledger.md`

## AS-IS → TO-BE 전수 커버리지 원장 (누락 0 목표)

작성 2026-10-01. 목적: MSA 전환에서 **front/back/static 어느 것도 누락 없이** 이식됐는지 **파일 단위**로 증명.
기능/화면 레벨 대조(docs/inventory/*.tsv, docs/*-parity-audit.md)는 이미 있으나, 이 원장은 그 위에
**① back 엔드포인트 ② front 뷰파일 ③ static 에셋(css/js/img/font)** 3축을 각 버킷마다 파일 단위로 present/missing/gap 판정한다.
원칙: [[asis-porting-principles]] — AS-IS 100% 이식이 기본선, RFP/ISP 변경은 별도 확인.

- AS-IS 소스: `C:\workspace\ghlove` (ghlove-web / ghlove-common / ghlove-api / ghlove-batch / ghlove-frontend).
- AS-IS 코드데이터: [[asis-sources-and-data]].
- 버킷 배정 기준: docs/as-is-parity.md §1 (프론트 최상위 디렉터리 전부를 6서비스+common에 배정).
- 범례: ✅present(이식됨) · ❌missing(갭, 이식대상) · 🟡partial/통합 · ⏸보류(SalesOn종속·미사용 등 결정된 보류)

### 버킷별 진행 상태
| 버킷 | front(화면) | back(엔드포인트) | static(에셋) | 상태 |
|---|---|---|---|---|
| member | ✅ 12/12 | ✅ 누락0 | ✅ CSS14/14 | **완료(파일럿)** |
| donation | ✅ 핵심 / ⏸ 납부연계 | ✅ 핵심 / ⏸ 납부연계 | ✅ 핵심 | **완료** |
| point | ✅ 2/2 | ✅ 누락0 | ✅ | **완료** |
| gift | ✅ 대부분 / ❌ 특산·소식지 | ✅ 대부분 / ❌ 특산·소식지 | 🟡 | **완료(갭2)** |
| order | ✅ 누락0 | ✅ (PG=도메인상 N/A) | ✅ 5/5 | **완료** |
| admin | tsv + 메뉴트리 | 진행중(103컨트롤러) | 부분 | 메뉴트리 라운드 진행중 |
| common | ✅ 대부분 / ❌ 통합검색 | ✅ 대부분 / ❌ 통합검색 | ✅ | **완료(갭1)** |

### ★ 전체 재현갭 요약 (이식대상, 2026-10-01 점검 결과)
6개 스토어프론트 버킷 전수 결과 — 대부분 이식 완료, **진짜 누락은 소수**. 보류/도메인상 N/A는 갭 아님.

**이식대상(❌ 재현갭):**
1. **donation 납부 게이트웨이 연계** (NTS·지방세·서울 etax·지로, NgDonation/RegionTax/SeoulTax + external-* 프론트) — **이식 결정됨**([[donation-payment-gateway-port-decision]], 설정스왑 어댑터). 최대 작업량.
2. **common 통합검색(totalsearch)** — [2026-10-01 정정] **AS-IS에서도 비활성/미완성**: `/api/totalsearch/*` 서버 비활성, 진입 url '#' 차단(main.html:474 ASIS-UNFINISHED), 메인 검색창은 답례품검색(goods)으로 감. → 활성갭 아님. 이식하되 **AS-IS처럼 비활성 상태로 재현**(또는 보류) — 사용자 결정 대기.
3. **gift/admin 특산물관(speciality)·소식지(catalog·카드뉴스)** — [2026-10-01 결정] front+back+admin **전부 이식하되 AS-IS와 동일하게 TO-BE도 숨김상태로 재현**([[asis-porting-principles]]): 스토어프론트 GNB 미노출, 소식지 admin메뉴 display='N', 특산 권한 ROLE_ADMIN_3·4 한정. (AS-IS 근거: 프론트 진입점 전무, 소식지 admin숨김·데이터0, 특산 행안부권한만 — 비활성 feature). ※ 제철(seasonal)은 TO-BE 구현완료(SeasonalView), 별개.
4. **admin**: 메뉴트리 라운드의 ❌ 갭들(특정사업 통계/배너, 외국인기부, 만족도조사, 기부혜택증 열람현황, 방문자접속경로, 배송업체·온라인입금·카드혜택, 기금사업소개, 답례품Q&A, 배치로그·공통메시지) — 각 AS-IS 활성/숨김 상태대로 이식.

**결정된 보류/도메인 N/A (갭 아님):**
- order PG결제(easypay/nicepay/naverpay): 답례품=기부포인트 단일결제라 미사용.
- point 예약: TO-BE 전용·미사용 결정.
- policy 원문 DB이관: 운영데이터 대기.

**저위험 교차확인 TODO:** member 전자서명(signRegister), donation featured(지역이벤트) 뷰, gift community-business/store back.

### ★ B5 소급교정: admin 통계 화면 차트화 (2026-10-01)
[[asis-porting-principles]] — AS-IS는 admin 통계 화면 다수가 Chart.js 차트인데, TO-BE admin엔 Chart.js가
아예 없어(2026-10-01 만족도조사 때 `content/modules/chart.min.js`+`op.chart.js`를 AS-IS에서 복사해 들여옴)
그 화면들이 **표로 대체돼 있던 상태 = 규칙 위반**. AS-IS와 동일하게 차트로 교정 대상:
- 특정사업 월별통계(designated-donation/analysis/month), give statistics all/locgov/operate/detail,
  shop-statistics dashboard day·month / report general·generalTotal·lclgv·mctpv / sales/all, 관심지자체(statistics/locgov/detail).
- 로그인 메인(main/index·dashboard)은 별도(메인 대시보드 재현 단계).
- 방식: 각 AS-IS JSP의 ChartCommon.drawChart 설정(type·datasets·색상·stacked 등)을 verbatim 복제, TO-BE 데이터 주입.
- **[2026-10-01 중대 확인] 통계 화면은 "차트 빠진 AS-IS"가 아니라 간이 자체화면(re-invention)이다.** 예: 지정기부 월별통계 — 현재 TO-BE analysis.html은 지자체별(모금액/건수/참여인원)+월별(모금액/건수) 단순표. AS-IS month.jsp는 **사업구분별(100취약계층/200문화예술/300자원봉사/400복리증진) 건수·모금액 비율 pie 2개 + 월별 목표/모금액 막대(ChartCommon.drawChartMultiYaxis) + 표3 + 필터(시도/지자체/년도/상태0전체·2진행·9종료)**. 전혀 다름. → B5는 대시보드 통째 재구축.
  - 데이터 토대 OK: donation `DesignatedProject`에 dsgnDntnBizSeCd(사업구분100~400)·goalAmt(목표금액)·dsgnDntnBizSttsCd(상태)·lclgvCd·기간ymd 존재. 모금액은 g_cntr 집계.
  - 필요 백엔드(donation): ①/analysis/month/campaign(사업구분별 건수+%), ②/amountraised(사업구분별 모금액+%), ③/amount(월별 목표/모금 m01~m12). admin 프록시+차트UI(AS-IS JS verbatim).
  - **규모**: 이런 대시보드 재구축이 ~10화면(give statistics all/locgov/operate, shop-statistics report/dashboard/sales, 관심지자체, 특정사업 월별 등). 각 화면이 feature급.

### ★티어 주의 (2026-10-01 보정)
AS-IS는 **레거시 서버렌더(ghlove-web/shop JSP + opmanager)** 와 **라이브 SPA(ghlove-frontend + `ghlove-api` REST 53개)** 두 티어 공존([[saleson-original-product-leftovers]]).
라이브 고향사랑(ilovegohyang)은 **SPA 티어**다. 따라서:
- **back축 정본 = `ghlove-api`** (ghlove-web/shop은 대부분 SalesOn 레거시·死코드). opmanager만 admin 버킷의 back 정본.
- **front축 정본 = `ghlove-frontend`** (SPA html) ↔ TO-BE storefront Vue.

### 방법 (버킷마다 반복)
1. **back**: AS-IS 해당 도메인 컨트롤러의 모든 `@RequestMapping/@GetMapping/@PostMapping` 엔드포인트 enumerate → TO-BE 대응 엔드포인트 매칭. (present/missing/gap)
2. **front**: AS-IS 뷰파일(JSP 또는 프론트 html) 전부 enumerate(백업·old·날짜본 제외) → TO-BE 템플릿/Vue 뷰 매칭.
3. **static**: 각 live 화면이 참조하는 css/js/img/font를 추출 → TO-BE에 실제 파일이 있는지 확인. (static-asset-scoping-2026-09-18(보관: docs/archive/memory-fix-log-2026-10.md) 누락복사 전례)
4. 각 missing은 "AS-IS 이식" vs "RFP/ISP 변경(별도확인)"으로 분류.

---

### member (파일럿) — 진행중

#### back축 — AS-IS 컨트롤러 인벤토리 (TO-BE 서비스 배정)
AS-IS member 도메인 컨트롤러는 **스토어프론트(회원용) → TO-BE member** 와 **opmanager(운영자용) → TO-BE admin** 으로 갈린다.
엔드포인트 단위 present/missing은 아래 각 컨트롤러를 열어 채운다(다음 단계).

**스토어프론트(회원용) → TO-BE `member` 서비스**
| AS-IS 컨트롤러 | 대략 역할 | TO-BE 대응(추정) | 판정 |
|---|---|---|---|
| user/UserController | 로그인/가입/정보수정/탈퇴 등 | member 웹/ API | (엔드포인트 대조 예정) |
| user/UserMobileController | 모바일 회원 | (반응형 통합?) | |
| mypage/MypageController | 마이페이지(회원분) | member mypage | |
| mypage/MypageMobileController | 모바일 마이페이지 | | |
| userdelivery/UserDeliveryController | 배송지 관리 | member 배송지 | |
| userdelivery/UserDeliveryMobileController | 모바일 배송지 | | |
| snsuser/SnsUserController · usersns/UserSnsController | SNS 간편로그인/연동 | member 외부로그인 | |
| seller/user/SellerUserController | 판매자 사용자 | gift 판매자? (버킷 재배정 검토) | |

**opmanager(운영자용) → TO-BE `admin` 서비스** (admin 메뉴트리 라운드와 연계)
| AS-IS 컨트롤러 | 역할 | TO-BE 대응 | 판정 |
|---|---|---|---|
| user/UserManagerController | 일반회원관리 | /admin/members | |
| user/GeneralCustomerManagerController | 일반회원 | /admin/members | |
| user/SecedeUserManagerController | 탈퇴회원 | /admin/secede-users | |
| user/SleepUserManagerController | 휴면회원 | /admin/sleep-users | |
| user/LocgovManagerController | 지자체관리 | /admin/locgovs | |
| user/LocgovPersonInChargeManagerController | 지자체담당자 | /admin/person-in-charge | |
| user/OperPersonInChargeManagerController | 운영담당자 | /admin/person-in-charge | |
| user/OffPersonInChargeManagerController | 오프라인담당자 | /admin/off-person-in-charge | |
| user/ManagerRequestController | 관리자 권한요청 | /admin/manager-requests | |
| usergroup/UserGroupController | 회원그룹 | (admin 저우선 갭) | |
| userlevel/UserLevelManagerController | 회원등급 | /admin/user-levels | |
| lclgvHnrUser/LclgvHnrUserManagerController | 기부혜택증 | /admin/honor-users (donation도 관여) | |
| loginbanner/UserLoginBannerManagerController | 로그인배너 | Banner.bannerType 흡수 | |

#### back축 — 엔드포인트 대조 결과 (스토어프론트 회원분 → TO-BE member 서비스)
> ★티어보정: 아래 표는 처음 ghlove-web/shop(레거시)로 대조했으나, 정본 back은 **ghlove-api**(auth/AuthController·user/UserController·user/JoinController·mypage/MypageController)다.
> ghlove-api 회원 엔드포인트(login/me/secede/find-id/find-password-step1·2/change-password/join/sns-join/disconnect-sns/onepass-*/mobile-auth/signRegister(전자서명)/modifyUser/getUserInfo 등)와 TO-BE member API를 재대조한 결과 **기능 대응 동일(누락 없음)**.
> 단 **전자서명(signRegister/signRemove/checkSign, user/UserController)** 은 TO-BE 미구현일 수 있어 point/member deferred와 교차확인 필요(저위험·[[deferred-features]] 전자서명 보류건과 동일 가능성).
AS-IS는 서버렌더 MVC, TO-BE는 REST(Vue SPA 소비)라 기능 단위 대조. **모바일 `/m/*` 컨트롤러는 TO-BE 반응형 SPA로 통합(🟡, 별도 아님)**.
mypage의 order/point/coupon/review/wishlist/inquiry 엔드포인트는 각 버킷(order/point/gift) 소관이라 여기서 제외.

| AS-IS 기능(컨트롤러) | TO-BE member API | 판정 |
|---|---|---|
| 로그인/팝업로그인/게스트(UserController login·popup-login·guestLogin) | AuthApiController `/auth/login`·`/auth/me`, UserApiController `/api/users/walk-in` | ✅ (팝업=Vue모달 🟡) |
| 가입 흐름(join-us·sns-join·agreement·entryForm·join·join-complete·confirm) | AuthApiController `/auth/signup` + ExternalLogin `/signup/*` | ✅ (다단계는 Vue 🟡) |
| ID/중복확인(user-availability-check(-join)) | UserApiController `/api/check-login-id` | ✅ |
| 아이디/비번찾기(find-user·check-account·find-id·find-password·find-password-change) | AuthController `/find-idpw/*` | ✅ |
| 비번변경(change-password·delay-change-password) | AuthApiController `/auth/change-password`·`/auth/delay-change-password` | ✅ |
| 본인인증(user-auth·user-auth-success·authCheck) | `/auth/login/mfa-verify`, ProfileApiController `/api/profile/mfa` | ✅🟡 |
| 정보수정(modify·editMode·modify-action) | ProfileApiController `/api/profile`(GET/PUT)·`/api/password/verify` | ✅ |
| 탈퇴(secede) | ProfileApiController `/api/withdraw`·`/api/withdraw-info` | ✅ |
| 휴면/해제(sleep-user·wakeup-user·dormancy) | LoginView 로그인흐름 내 처리 + AdminMemberApi 배치 | ✅🟡 (전용페이지 아닌 로그인흐름) |
| 배송지(/delivery list·write·edit·list-action) | DeliveryApiController `/api/delivery` CRUD+default | ✅ |
| SNS(naver-callback·search·setup-sns·disconnect-sns·loginId-check·sns-joined-check·redirect) | ExternalLoginController(kakao/naver), ExternalAuthApiController, AccountUnlinkApiController | ✅🟡 |
| 마이페이지 요약 | MyPageApiController `/api/mypage/summary` | ✅ |

→ **back 누락 없음.** (TO-BE 쪽 AdminMemberApi/UserLevelAdminApi/RoleRequest 등은 admin 버킷 소관.)

#### front축 — AS-IS `ghlove-frontend/users/` live 12종 → TO-BE storefront
(login-backup·login_test·modify-backup 제외)

| AS-IS 화면 | TO-BE 뷰 | 판정 |
|---|---|---|
| login.html | LoginView.vue | ✅ |
| join.html | SignupView.vue | ✅ |
| find-idpw.html | FindIdPwView.vue | ✅ |
| modify.html | mypage/ProfileView.vue | ✅ |
| secede.html | mypage/WithdrawView.vue | ✅ |
| secede-kakao.html | mypage/KakaoSecedeView.vue | ✅ |
| onepass_secede.html | mypage/OnepassSecedeView.vue | ✅ |
| onepass-join.html | SignupView(onepass 흐름) | ✅🟡 |
| onepass-result.html | 로그인/가입 콜백 처리(LoginView/SignupView) | 🟡 콜백흐름 |
| mobile-auth-result.html | `/signup/mobile-auth` 콜백 | 🟡 콜백흐름 |
| sign-certificate.html | LoginView/SignupView(finance-cert·simple-auth) | ✅🟡 |
| jusoPopup.html | 주소검색(DeliveryFormView·ProfileView 내 컴포넌트) | ✅ |

→ **front 누락 없음.** 🟡는 "별도 페이지가 아니라 Vue 뷰/콜백에 통합"된 것으로, 기능 상실 아님.

#### static축 — member 라우트 참조 CSS 존재검증
pageStyles.js의 login/signup/find-idpw/mypage 세트 CSS 14종(authentication-modal·change-pw·login-total·join-inf2·joind-ag·joind-agf·login-idse·main·event·mypage-status·favo_info·change-info·m_main·notice-box) **전부 `storefront/public/css/`에 존재**. → **static 누락 없음.**

#### member 결론
**3축 전부 파일 단위 누락 0.** 🟡 항목은 모두 "AS-IS의 별도 페이지/모바일/다단계 UI가 TO-BE에선 반응형 SPA·콜백·모달로 통합"된 것으로 기능 상실 아님(이식 완료). RFP/ISP 변경분은 이 범위에서 추가 식별된 것 없음.
남은 확인(저위험): 휴면해제 전용 안내페이지 유무, onepass-result/mobile-auth-result 콜백 라우트 명시 여부, **전자서명(signRegister/checkSign) TO-BE 유무** — 실동작엔 영향 없으나 추후 교차확인.

---

### donation — 완료
back 정본 = ghlove-api(designateddonation/DesignatedDonationController, donation/NgDonationController·RegionTaxController·SeoulTaxController, mypage/MypageController 기부분). front 정본 = ghlove-frontend/{donation,designated-donation,featured}.

#### back축
| AS-IS(ghlove-api) | TO-BE donation | 판정 |
|---|---|---|
| DesignatedDonation getList·getDetail·getBsnsTypes·getDesignatedCntrList·getFaqList·saveCheerMsg·getNoticeList | DonationPublicApiController `/api/designated-donation/projects(/{id})`·`saveCheerMsg`·`donate/designated`, DesignatedAdminApiController | ✅ |
| 일반기부 접수·거주지확인·포인트율·중복확인(NgDonation userCntrInfo·locGovInfo·sidoList·sigunguList 등 조회분) | DonationPublicApiController `/api/donate/form`·`verify-residence`·`point-rate`·`today-duplicate`·`/api/donate`, DonationApiController `/api/locgovs`·`/api/donation-sources` | ✅ |
| 관심지자체(getIntrstLocgov·setIntrstLocgov) | InterestLocgovController, DonationMyApiController `/api/my/interest-locgovs` | ✅ |
| 마이 기부내역·영수증·명예(MypageController getCntrList·honorList·receipt-print·saveHonorViewHist) | DonationMyApiController `/api/my/donations`·`/api/my/receipts`·`honor/certificates`, OfficialReceiptController | ✅ |
| 기부금 운용/모금현황/기금사업 | DonationApiController `/api/give-state`, CtbnyOpratnApiController, FundProjectApiController | ✅ |
| 오프라인 기탁 | OffgiveApiController, DonationMyApiController `/api/my/donations/offline` | ✅ |
| **납부 게이트웨이 연계**: 부과/수납(sntrBugaInsert·etaxSunapInfo·sunapSuccess·contryBugaInsert·local-sunap-confirm), giroPay, 공개키, 지방세(RegionTax bugaRequest·sunapProcess), 서울세(SeoulTax etax) | — 없음 | ⏸ **보류** |
→ 핵심 기부접수·지정기부·관심·영수증·마이 = ✅. **납부 게이트웨이(NTS/지방세/서울 etax·지로) ~50 엔드포인트 = ⏸ 보류**([[deferred-features]] PG/납부연계, 외부 세정·PG 연동·DA 대기). 결정된 보류지 silent 누락 아님.

#### front축 (ghlove-frontend/{donation,designated-donation,featured})
| AS-IS 화면 | TO-BE 뷰 | 판정 |
|---|---|---|
| donation-main·donation·complete·intro·quick-donation | DonateView(+단계/complete) | ✅🟡 |
| list-select | ListSelectView | ✅ |
| map-select·mPop | MapSelectPopup 컴포넌트 | ✅ |
| guide1·2·3·5·6 | GuideDonationView·GuideCautionView·GuideOnlineMethodView·GuideOfflineMethodView·TaxCreditGuideView | ✅ |
| designated-donation index(-main)·details | DesignatedListView·DesignatedDetailView | ✅ |
| featured eventList·eventDetail·detail (지역이벤트) | storefront featured 뷰 **확인필요** | 🟡 재확인 |
| **external-etax(-2)·external-giro(-2)·giro-success/fail·wetaxInfo(_m)·ngdonation·donationNext(-main)·process·donation-tax·popup-success/fail** | — 없음 | ⏸ 납부연계(보류) |
→ 핵심·가이드·지정기부 = ✅. 납부연계 프론트 = ⏸(back과 동일 보류). featured(지역이벤트) 뷰 1건 재확인 TODO.

#### static축
donate/designated-list/donate-gift-select/list-select/mypage 라우트 CSS(donation_doak·donation_liemt·donation_selmt·designation·event·joind-ag(f)·main·notice-box·research-box 등) pageStyles 등록 → member와 동일 기준 존재(개별 재확인 저위험). 납부연계 페이지 미이식이라 해당 에셋만 부재(보류와 일관).

#### donation 결론
핵심 기부 기능(접수·지정기부·관심지자체·영수증·마이·가이드·기금) **이식 완료**. 유일한 큰 영역 = **납부 게이트웨이 연계(NTS·지방세·서울 etax·지로)** 로, 외부 세정/PG 연동이라 **결정된 보류(⏸)**. 재현갭(silent 누락) 아님. featured 뷰·전자서명 저위험 TODO.

---

### point — 완료
- **front**: AS-IS mypage/cntrPoint.html·cntrPointDetail.html → TO-BE MyPointsView·MyPointDetailView ✅.
- **back**: AS-IS ghlove-api mypage getCntrPoint/getCntrPointDetail → TO-BE PointMyApiController `/api/my/points`·`/api/my/points/detail` ✅. 잔액/적립율/원장/소멸배치/지자체적립율 = PointApiController·PointController·PointHistoryAdminApiController(admin·inter-service) ✅.
- **static**: mypage 공통 CSS 세트 공유(member에서 존재확인됨).
- **TO-BE 전용**: 포인트 예약(PointReservationsView·`/api/my/reservations`)은 AS-IS에 없는 TO-BE 추가분, **미사용 결정됨**([[deferred-features]]) — 갭 아님.
→ **3축 누락 0.**

---

### gift — 완료 (재현갭 2건)
back 정본 = ghlove-api(item/ItemController, catalog/CatalogController, category/CategoriesController·CategoryController, seasonfood/SeasonFoodController, speciality/SpecialityController, communityBusiness/CommunityBusinessController, store/StoreController). front = ghlove-frontend/{goods,items,catalog,category,community-business}.

#### back축
| AS-IS(ghlove-api) | TO-BE gift | 판정 |
|---|---|---|
| ItemController(목록·상세·reviews·review·wishlist·qna·restock·coupons·relation·getUserCntrPoint) | GiftPublicApiController(`/api/gifts`·`/{id}/detail`·reviews·inquiries), GiftMyApiController(wishlist·my/reviews·my/qna), GiftController(wishlist toggle·review like·restock-notice) | ✅ |
| CategoriesController·CategoryController(searchResult·best·filter·price-areas·category-path) | GiftApiController `/api/categories`, GiftPublicApiController `/api/gifts`(필터) | ✅🟡 |
| SeasonFoodController(제철 search) | GiftPublicApiController `/api/season-food` | ✅ |
| CommunityBusinessController(마을기업) | CommunityView 경유(gifts 필터) | ✅🟡 back 재확인 |
| **SpecialityController(특산물 search·search-item-list)** | — 없음 | ❌ **갭(이식대상)** |
| **CatalogController(소식지: getCatalogNewItem·getDsgnDonationItem·getLocgovFavItem·getCatalogSeasonalItem·getCatalogMainInfo·카드뉴스)** | — 없음 | ❌ **갭(이식대상)** |
| StoreController(판매자몰 조회) | seller 포털 경유 | 🟡 |

#### front축 (ghlove-frontend)
| AS-IS 화면 | TO-BE 뷰 | 판정 |
|---|---|---|
| goods/index(-main)·searchGoods(-main) | gift/GiftListView(검색포함) | ✅ |
| items/details(-main) | gift/GiftDetailView | ✅ |
| category/index | GiftListView 카테고리 필터 | ✅🟡 (전용 CategoryView 없음) |
| community-business/communityList-main | gift/CommunityView | ✅ |
| (제철) | gift/SeasonalView | ✅ |
| **catalog/catalog-main·catalog-faq (소식지)** | — 없음 | ❌ **갭(이식대상)** |
| **(특산물 전시)** | — 없음 | ❌ **갭(이식대상)** |
| mypage review·wishlist·qna·inquiry-item | mypage/GiftReviewsView·WishlistView·GiftQnaView·QnaView | ✅ |

#### static축
gift-list/gift-detail/gift-seasonal/gift-community-business 라우트 CSS(item·goods_card·evt_card·event·joind-agf·lclgv-map·order-modal) pageStyles 등록·존재. 특산물/소식지 페이지 미이식이라 해당 에셋만 부재(갭과 일관).

### order — 완료
back 정본 = ghlove-api(cart/CartController, order/OrderController, payment/PaymentController, shipping/ShippingController, orderagency/OrderAgencyController). front = ghlove-frontend/{cart,order,mypage order분}.

#### back축
| AS-IS(ghlove-api) | TO-BE order | 판정 |
|---|---|---|
| CartController(add·delete·update-quantity·shipping-payment-type) | CartApiController | ✅ |
| OrderController(detail·save·buy·payment-step·confirm-purchase·coupons) | CheckoutApiController(review·preview·complete·done), OrderMyApiController(orders·cancel·confirm-receipt), CouponMyApiController | ✅ |
| OrderController 클레임(return-apply·exchange-apply·cancel-apply·refund-amount) | ClaimController·ClaimMyApiController·OrderMyApi`/items/{id}/claim` | ✅ |
| ShippingController(배송지·base-shipping) | member DeliveryApi + OrderMyApi `/delivery-address` | ✅ |
| **OrderController PG결제(easypay·nicepay-vacct·naverpay·redirect-pay·giveGoodsSavePay), PaymentController(pay-log)** | — 없음 | **N/A(의도적)** 답례품=기부포인트 단일결제, SalesOn 신용카드·가상계좌 PG 미사용([[order-single-item-vs-multiitem-decision]] 플랫폼 PG 미연동 명시) |
| OrderAgencyController(주문대행 상담원) | OrderAdminApiController `/agency-search`·`/admin/order-agency` | 🟡 admin 버킷 |

#### front축
| AS-IS 화면 | TO-BE 뷰 | 판정 |
|---|---|---|
| cart/index | order/CartView | ✅ |
| order/step1·step2 | order/CheckoutView + OrderCompleteView | ✅ |
| mypage/orderList | order/MyOrdersView | ✅ |
| mypage/orderDetail | order/OrderDetailView | ✅ |
| mypage/orderCancel | order/MyClaimsView | ✅ |
| mypage/deliveryInfo | mypage/DeliveryListView·DeliveryFormView | ✅ |

#### static축
order_ali·mypage-order·mypage-order-details·order-modal·favo_info **5종 전부 public/css/에 존재**(pageStyles의 "미복사" 주석은 낡음, 실제 복사됨 — 원장이 잡아낸 뒤 확인). → **누락 0**.

#### order 결론
장바구니·체크아웃(포인트결제)·주문·클레임(취소/반품/교환/환불)·쿠폰·배송지 **3축 누락 0**. **PG 결제연계는 "답례품=기부포인트 단일결제"라 의도적 미이식(도메인)** — silent 누락 아님. 멀티아이템 재설계는 진행중 개선과제([[order-single-item-vs-multiitem-decision]]), 누락과 무관.

---

### common — 완료 (재현갭 1건)
back 정본 = ghlove-api(notice·faq·qna·databoard·qustnr·event·totalsearch·policy·help·popup·banner·Main·search). front = ghlove-frontend/{notice,faq,qna,data-board,qustnr,event,totalsearch,policy,guide,error}.

| AS-IS 영역 | TO-BE | 판정 |
|---|---|---|
| notice(2) | customer/NoticeListView·NoticeDetailView | ✅ |
| faq(1) | customer/FaqListView | ✅ |
| qna(4) 1:1문의 | customer/QnaBoardListView·QnaBoardDetailView + mypage/QnaView | ✅ |
| data-board(2) | customer/DataBoardListView·DataBoardDetailView | ✅ |
| qustnr(2) 설문 | SurveyView | ✅ |
| event(8) = 제철/특산/시즌 전시 | 제철→gift/SeasonalView ✅ / 특산→❌(gift갭) / 기획전→customer/EventListView·EventDetailView | 🟡 |
| policy(20 약관원문) | donation/PolicyView(slug별) | ✅ (원문 DB이관 policy-content-db-migration-deferred(보관: docs/archive/memory-fix-log-2026-10.md)) |
| guide(사이트맵) | AppFooter 사이트맵 | ✅ |
| error | ErrorView | ✅ |
| main | HomeView | ✅ |
| **totalsearch(통합검색, index.html + TotalSearchController)** | — 없음(검색뷰·라우트 전무) | ❌ **갭(이식대상)** |

static: notices/faq/data-board 라우트 CSS(ct_nov·research-box·main·event) 등 pageStyles 등록·존재. 통합검색 페이지 미이식이라 해당 에셋만 부재.

#### common 결론
게시판(공지·FAQ·1:1·자료실)·설문·이벤트·정책·가이드·오류·메인 **이식 완료**. **재현갭 1건: 통합검색(totalsearch)** — AS-IS live 기능인데 TO-BE 전무(검색뷰·API 없음). 사용자 원칙상 **이식대상**(단 통합검색 범위가 RFP 변경대상인지 교차확인 필요 — coverage-map 주석).

---

#### gift 결론
답례품 핵심(목록·검색·상세·위시·리뷰·문의·제철·마을기업·카테고리) **이식 완료**. **특산물관(speciality)·소식지/카탈로그(catalog·카드뉴스)** 는 AS-IS에서 **비활성/미사용**(프론트 진입점 전무·소식지 admin숨김·데이터0·특산 행안부권한만) → [2026-10-01 결정] **front+back+admin 전부 이식하되 TO-BE도 AS-IS처럼 숨김상태로 재현**([[asis-porting-principles]]). community-business/store back 저위험 재확인 TODO.


---

## 3. 대민 SPA 충실도 감사

> 통합 전 파일: `docs/as-is-fidelity-audit-2026-09-17.md`

## AS-IS 충실도 감사 (대민 SPA 전환분)

- 작성: 2026-09-17
- 목적: 이미 storefront Vue3 SPA로 전환된 **대민 화면**을 AS-IS 소스와 대조해, 휴면해제·영수증출력에서 드러난 것과 같은 **흐름·문구·에러처리 divergence를 선제 발굴·목록화**한다. 쓰다가 발견하는 반복을 줄이는 게 목적.
- 방법: 화면 단위로 (1) AS-IS 프론트 흐름(`ghlove-frontend/**`, 특히 API 허브 `modules/op.saleson.js`) + (2) AS-IS 서버(`ghlove-web`/`ghlove-api`/`ghlove-common`) + (3) MSA 구현(백엔드 + storefront)을 대조. 라우트·파일명이 아니라 **동작·문구·분기**로 확인([[asis-porting-principles]]). `old/`·주석처리 코드는 비활성이므로 라이브 경로만.
- 판정: **버그 / 재현누락(미배선) / 의도적 축소(외부연계·상용SW 대체 등) / 확인필요** 로 구분. 이 프로젝트엔 의도적 축소가 많으므로 함부로 "누락"으로 몰지 않는다.
- 상태: **진행 중(v1).** AUTH(로그인) 우선 완료. 나머지 영역은 하단 커버리지 체크리스트.

---

### 수정 반영 (2026-09-17, 컴파일·빌드 통과 · 런타임 검증 대기 → pending-verify-dormancy-receipt(보관: docs/archive/memory-fix-log-2026-10.md))

- **A1 수정**: 로그인에 비번만료/임시비번 배선. `MemberService.passwordChangeCode()`/`changePasswordOnLogin()`/`postponePasswordChangeAndLogin()` 추가, `loginWithMfaCheck`가 휴면 다음에 이를 검사해 `PASSWORD_EXPIRED`/`PASSWORD_TEMP` 신호. `AuthApiController`에 `/api/auth/change-password`·`/api/auth/delay-change-password`. `LoginView`: PASSWORD_TEMP→"임시 비밀번호 사용자 입니다."+find-idpw, PASSWORD_EXPIRED→변경 모달("변경하고 로그인"/"나중에 변경").
- **D1 수정**: 누락 5건 완료 알림 복원(AS-IS 문구 그대로) — 문의등록·장바구니삭제(OrderDetailView·CartView·GiftDetailView).
- **D2 수정**: 반품/교환 신청 전 확인 프롬프트 + 유형별 완료문구.
- **E 수정**: 답례품 상세 재입고 알림·상품평 좋아요 배선. 백엔드 `requestRestockNotice`를 JSON화, `ReviewService.likedBy()`+DTO에 `likeCount`/`likedByMe`/`restockRequested` 추가, `GiftDetailView`에 버튼 2종.
- **A3 수정**: 세션만료 401 전역처리. `api/http.js`에 `setUnauthorizedHandler` 훅(3개 요청함수 모두 401 감지), `main.js`가 "로그인 후 이용이 가능합니다." 안내 + `/login?target=` 이동 + 로그인 스토어 초기화(중복 리다이렉트 가드). **프론트 전용이라 서비스 재기동 불필요**(storefront만).
- **A2 종결(해당없음)**: OBUJE_FAIL은 서버가 반환하지 않는 죽은 프론트 분기 → MSA 미재현이 충실(아래 A2 참조).
- **G 수정**: 체크아웃 결제 전 검증 추가 - `CheckoutView.submit`에 받는사람/연락처/우편번호/배송지주소/상세주소/포인트부족/동의 검증(AS-IS step1.html 문구). 프론트 전용.
- 저위험 tail 감사 종료: 포인트(H)·특정사업(I) 충실, 체크아웃(G) 수정. **대민 전 영역 감사 완료.**

### 요약 (심각도순)

| # | 영역 | 판정 | 심각도 | 한줄 |
|---|---|---|---|---|
| A1 | 로그인 비번만료/임시비번 | 재현누락(미배선) | **높음** | `passwordChangeRequired` 등 로직은 포팅했으나 로그인이 호출 안 함 → 만료·임시비번 강제변경이 아예 안 걸림 |
| A2 | 로그인 OBUJE_FAIL(생년월일 추가입력) | 해당없음 | - | 서버가 반환 안 하는 죽은 프론트 분기 → MSA 미재현이 충실 |
| A3 | 세션만료 401 전역처리 | 수정완료 | 중 | http.js 훅+main.js에서 401시 안내+`/login?target=` 이동(프론트 전용) |
| D1 | 액션 완료 알림 | 재현누락 | 중 | 대다수 충실, **특정 5건**(문의등록·장바구니삭제·주문취소/구매확정/클레임)만 완료문구 누락 |
| E | 답례품 상세 미배선 | 재현누락 | 중 | 재입고 알림·상품평 좋아요 = 백엔드 있는데 SPA 버튼 없음 |
| G | 체크아웃 필드검증 | 수정완료 | 중 | 결제 전 받는사람/주소/우편번호/상세주소/포인트부족/동의 검증 추가(프론트 전용) |
| A0 | 로그인 휴면(SLEEP_USER) | 수정완료 | - | 2026-09-17 AS-IS와 동일화(pending-verify-dormancy-receipt(보관: docs/archive/memory-fix-log-2026-10.md)) |

---

### A. 인증(로그인) — `LoginView.vue` / `AuthApiController` / `MemberService`

AS-IS 로그인 응답 핸들러(`op.saleson.js:1157~1224`)는 `/api/auth/token` 응답의 `code`로 분기한다. 코드 전량:

| AS-IS code | AS-IS 동작(op.saleson.js) | MSA 처리 | 판정 |
|---|---|---|---|
| (정상) | 로그인 완료 | `status:"OK"` | ✅ |
| `SLEEP_USER` | :1169 confirm→`/api/auth/recovery` | 2026-09-17 동일 구현 | ✅ (검증대기) |
| `PASSWORD_EXPIRED` | :1182 비번변경 모달 / 로그인 리다이렉트 | **없음** | ❌ A1 |
| `PASSWORD_TEMP` | :1201 `alert("임시 비밀번호 사용자 입니다.")`→find-idpw | **없음** | ❌ A1 |
| `OBUJE_FAIL` | :1163 생년월일 입력창(#temp-birthday) 노출, 토큰제거 | 없음(불필요) | ✅ A2(죽은 분기) |
| `ONEPASS_USER` | 주석처리(비활성) | 없음 | ✅ (AS-IS도 비활성) |

#### A1. 비밀번호 만료/임시비밀번호가 로그인에 미배선 — 재현누락(높음)

- **AS-IS**: 만료(`PASSWORD_EXPIRED`)면 비밀번호 변경 모달을 띄우고("나중에 변경" 가능), 임시비번(`PASSWORD_TEMP`, passwordType 'T')이면 `alert("임시 비밀번호 사용자 입니다.")` 후 비밀번호찾기로 보내 **반드시 변경**시킨다.
- **MSA 현황**: 관련 로직은 이미 있다 — `User.passwordExpiredDate`/`passwordType('N'/'T'/'P')`, `MemberService.passwordChangeRequired()`([MemberService.java:953](../member/src/main/java/com/ghlove/member/service/MemberService.java#L953)), `postponePasswordChange()`(:973, "나중에 변경"), 만료일 계산(:924). **그런데 `passwordChangeRequired`/`postponePasswordChange`의 호출부가 0곳** — 로그인(`loginWithMfaCheck`/`AuthApiController.login`)이 이걸 전혀 부르지 않아 만료·임시비번 회원이 그냥 `OK`로 로그인된다. 임시비번('T') 회원은 담당자 방문접수(`walkInRegister`, :210)로 실제 생성되므로 도달 가능한 경로다.
- **필요 조치**: 로그인 성공 지점에서 `passwordChangeRequired(user)` 검사 → 응답 코드 추가(`PASSWORD_EXPIRED`/`PASSWORD_TEMP`) → SPA가 비밀번호 변경 안내(만료는 "나중에 변경" 허용=`postponePasswordChange` 노출, 임시는 강제). find-idpw의 직접 재설정+자동로그인은 의도적 재아키텍처라 그대로 두되, 임시비번 표출만 로그인에 붙이면 된다.

#### A2. OBUJE_FAIL(생년월일 추가입력) — 해당없음(죽은 프론트 분기, 재현 불필요)

- **AS-IS 프론트**: 로그인 응답이 `OBUJE_FAIL`이면 `vm.birthday`를 채우고 생년월일 입력 UI(`#temp-birthday`)를 노출한 뒤 토큰을 제거한다(op.saleson.js:1163).
- **트리거 확인 결과(2026-09-17)**: AS-IS **서버는 이 코드를 반환하지 않는다.** 로그인 토큰 발급부(`ghlove-api/saleson/api/auth/AuthController.java`)가 설정하는 `code`는 `PASSWORD_EXPIRED`(:1233)·`PASSWORD_TEMP`(:1236)·`SLEEP_USER`(:1239) 셋뿐이고, 저장소 전체 grep에서 `OBUJE`/`OBUJE_FAIL`은 **op.saleson.js(프론트)에만** 존재한다. 즉 주석처리된 `ONEPASS_USER`처럼 **더 이상 트리거되지 않는 레거시 프론트 핸들러**다.
- **판정**: MSA가 재현하지 않는 게 **라이브 AS-IS 동작과 일치(충실)**. 조치 불필요.
- **부수 확인**: 서버 실사용 코드 3종이 A1·휴면에서 MSA에 심은 코드(PASSWORD_EXPIRED/PASSWORD_TEMP/SLEEP_USER)와 정확히 일치함을 교차검증.

#### A3. 에러 표출·401 처리 방식(전역) — 확인필요(중)

- **AS-IS**(`op.saleson.js:4454 handleApiExeption`): API 에러 시 (a) `redirect` 필드 있으면 이동, (b) **401이면 `alert("로그인 후 이용이 가능합니다.")` + `/login?target=` 자동이동**, (c) 그 외 서버 메시지를 **모달 `$s.alert`** 로 표시.
- **MSA**(`api/http.js`): 실패를 `throw new Error(message)` → 각 뷰가 `catch`해 **화면 내 텍스트**(errorMessage)로 표시. 401 전역 자동이동은 라우터 가드(requiresAuth)로 커버되나, **이미 로그인한 뒤 세션 만료로 API가 401을 주는 경우**의 일괄 처리는 없다(뷰마다 제각각).
- **판정**: 모달 vs 인라인은 UX 선택차라 반드시 틀린 건 아니나, **세션만료(401) 일괄 처리 부재**는 실사용 중 로그인이 풀렸을 때 화면이 조용히 깨질 수 있어 점검 필요.
- **증거(임시분기 산재)**: 401 전역처리가 없어 뷰마다 제각각이다 — 예: `DonateView.verifyResidence`는 `if (e.message.includes('로그인')) router.push('/login…')`로 문자열 매칭 우회, `OfficialReceiptPrintView`는 alert+close, 대부분 뷰는 그냥 인라인 텍스트. requiresAuth 라우터 가드는 **진입 시점**만 커버하고 **이미 열린 화면에서 API가 401을 주는 경우**는 못 잡는다.
- **권고(수정안, 미적용)**: `api/http.js`의 공용 `request()`에서 `res.status === 401`을 잡아 (a) 로그인 스토어 초기화 + (b) `/login?target=<현재경로>`로 이동(가능하면 "로그인 후 이용이 가능합니다." 안내). 라우터/스토어를 http 모듈에 직접 물리기 어려우면 커스텀 이벤트나 콜백 주입으로 App 레벨에서 처리. AS-IS `handleApiExeption`의 401 분기와 1:1 대응.

---

### B. 아이디/비밀번호 찾기 — `FindIdPwView.vue` / `AuthController`(find-idpw AJAX)

AS-IS `users/find-idpw.html` + `op.saleson.js`(`findId`/`findPasswordStep1`/`findPasswordStep2`) 대조. **대체로 충실 — 숨은 결함 없음.**

- **비번찾기 = 새 비밀번호 직접 입력(임시비번 발급 아님).** AS-IS step2 폼이 "새 비밀번호 입력/확인"(find-idpw.html:218,225)으로 사용자가 직접 정한다. MSA도 동일(직접 재설정). → **A1의 "find-idpw 직접재설정=재아키텍처"라던 초기 서술은 오류. 사실 AS-IS와 동일하다.** (임시비번 'T'는 find-idpw가 아니라 담당자 방문접수/관리자 발급 경로에서만 생긴다.)
- **비밀번호 복잡도 규칙 일치.** AS-IS(find-idpw.html:630~660 `checkPwd`): 9~20자·3개이상 반복/연속문자 금지·loginId 미포함. MSA `validatePasswordComplexity`([MemberService.java:557](../member/src/main/java/com/ghlove/member/service/MemberService.java#L557))가 동일 규칙을 **서버측에서 재검증**(AS-IS는 클라이언트만) → 오히려 더 견고.
- **본인인증은 전자서명(magicline)/휴대폰 = 의도적 축소.** AS-IS는 `/api/magicline/signedFormRGhlove` 등 외부 인증망으로 본인확인(find-idpw.html:802). MSA는 이 외부연계가 없어 dev bypass로 대체([[deferred-features]]). → 재현누락 아님, 알려진 보류.
- ⚠ **확인필요(경미):** 비번 재설정 성공 후 이동 — MSA `resetPwSubmit`은 자동로그인 후 `/mypage`. AS-IS가 자동로그인인지 로그인화면 이동인지는 step2 최종 콜백에서 재확인 필요(현재 MSA 주석은 "AS-IS처럼 자동로그인"이라 주장).

### C. 회원가입 — `SignupView.vue` / `AuthApiController`(signup)

이미 인벤토리 전수조사에서 대조·판정된 영역이라([[asis-sources-and-data]], [[continue-vs-rebuild-msa-decision]]) 여기서는 재도출하지 않고 요지만 옮긴다.

- **구현 분기는 AS-IS 일치**로 확인됨. 단 인벤토리가 재현누락으로 찍은 **① 본인인증(`isAuth`) ② 법정대리인(만14세 미만, `OP_USER_PARENT`)** 은 실제로 미구현 = 인벤토리가 정직한 갭. 둘 다 외부연계·정책 게이트라 보류군.
- 추가 정밀대조가 필요하면 이 항목을 별도 라운드로 승격(약관 필수/선택 동의 매핑, 아이디 중복확인 문구, 주소검색 팝업 동작 등).

### D. 마이페이지 주문 상세/클레임 — `OrderDetailView.vue`

AS-IS `mypage/orderDetail.html` + 클레임 모달(`components/ui/modal-return.vue`·`modal-exchange.vue`·`modal-order_cancle.vue`) 대조.

#### D1. 액션 "완료" 알림 누락 — 재현누락(중, 전역 가능성)

- **AS-IS**: 주요 액션 성공 시 모달 `$s.alert("...되었습니다.")`로 완료를 알린다 — 구매확정 "구매확정이 완료되었습니다."(orderDetail.html:976), 취소 "취소신청 되었습니다."(modal-order_cancle.vue:360), 반품 "반품신청 되었습니다."(modal-return.vue:301), 교환 "교환신청 되었습니다."(modal-exchange.vue:286).
- **MSA**: `OrderDetailView`의 `submitClaim`/`cancelOrder`/`confirmReceipt`는 성공 후 **조용히 `load()`만** 하고 완료 문구를 안 띄운다. 사용자가 처리 여부를 확신하기 어렵다.
- **확산 실측 결과: 전역 아님, 특정 5건.** storefront 전 화면을 훑으니 대다수는 AS-IS 완료문구를 (일부는 AS-IS 파일·라인 주석까지 달아) **충실히 보존**했다 — 예: 답례품Q&A/후기 삭제 "삭제되었습니다."(GiftQnaView:36·GiftReviewsView:35), 관심지자체 등록(ListSelectView:90), 포인트 사용(MyPointsView:24), 기탁서 접수(OfflineDonationView:118), 장바구니 담기(GiftDetailView:60). **누락된 것만 추리면 다음 5건:**

  | 액션 | 빠진 AS-IS 문구 | 위치 |
  |---|---|---|
  | 답례품 문의 등록 | "답례품Q&A가 등록되었습니다." (items/details-main.html:2612) | `GiftDetailView.submitInquiry` |
  | 장바구니 선택삭제 | "해당 답례품이 장바구니에서 삭제되었습니다." (cart/index.html:552) | `CartView.deleteSelected` |
  | 주문 취소 | "취소신청 되었습니다." (modal-order_cancle.vue:360) | `OrderDetailView.cancelOrder` |
  | 구매확정 | "구매확정이 완료되었습니다." (orderDetail.html:976) | `OrderDetailView.confirmReceipt` |
  | 반품/교환 신청 | "반품신청/교환신청 되었습니다." (modal-return/exchange.vue) | `OrderDetailView.submitClaim` |

  나머지(후기작성=AS-IS도 완료alert 없음, 위시토글=아이콘만)는 **갭 아님**. → 권장: 이 5곳에만 완료 알림 추가(문구는 위 AS-IS 그대로).

#### D2. 클레임 신청에 확인 프롬프트 없음 — 경미

- AS-IS는 반품/교환/취소를 **유형별 모달**로 확인받는다. MSA는 `cancelOrder`/`confirmReceipt`엔 `confirm()`이 있으나 **반품/교환(`submitClaim`)엔 확인 없이 바로 신청**된다. 최소한 신청 전 confirm + 완료 알림(D1)을 붙이는 게 일관적.

> 주문 목록(`MyOrdersView`)의 상태별 건수 요약·기간/상태/지자체 필터는 AS-IS `orderList.html`(my_order_status)와 구조 일치 — 별도 결함 없음(코드 대조).

### E. 답례품 상세 — SPA 미배선 기능 — 재현누락(중) — `GiftDetailView.vue`

백엔드에 엔드포인트는 있는데 SPA가 호출하지 않아 화면에 없는 기능들. **휴면해제와 같은 "능력 있음 + 흐름 미배선" 패턴.**

- **재입고 알림**: AS-IS는 품절 답례품 상세에 "재입고 알림" 버튼(ItemController:879, `POST /api/item/restock`). MSA 백엔드에 `GiftController.requestRestockNotice`(`POST /gifts/{itemId}/restock-notice`)가 있으나 `GiftDetailView`는 `soldOut` 표시만 하고 신청 버튼/호출이 없다([GiftDetailView.vue:245](../storefront/src/views/gift/GiftDetailView.vue#L245) 경고문구만). → 품절 시 재입고 알림 불가.
- **상품평 좋아요**: AS-IS는 리뷰에 좋아요(비로그인 IP 허용, ItemController:899, `add-like`). MSA 백엔드에 `GiftController.likeReview`(`POST /reviews/{id}/like`)가 있으나 SPA는 신고(report)만 호출하고 좋아요는 없다.
- **조치**: 두 버튼을 GiftDetailView에 배선(재입고=품절 조건부, 좋아요=리뷰행). 백엔드는 이미 준비돼 있어 프론트 추가만으로 복원 가능.

### F. 기부하기 — `DonateView.vue` — 대체로 충실

AS-IS `donation/donation-main.html` 대조. 제출 검증(지자체 미선택→거주지 미확인→고용관계(checkA)→기부자확인(checkB)→최소 100원→100원 단위)·법정 동의문구·적립예상·특정사업/응원메시지 흐름이 AS-IS와 일치. **새 결함 없음.**

- AS-IS에만 있는 검증들은 전부 **알려진 의도적 축소**(외부연계 미개방)로 확인:
  - "본인인증 미완료상태입니다…"(donation-main.html:995) → MSA는 본인인증(isAuth) 미구현([[deferred-features]], 인벤토리 갭과 일치).
  - "주민번호를 입력해주세요/뒷자리 오류/주민등록번호 오류"(1110·1117·1244) → MSA는 등록주소 기반이라 주민번호 실검증 없음. **단, juminNo2 입력칸(비밀번호형)은 화면에 있으나 `verifyResidence`가 locgovCode만 보내 값이 안 쓰인다** — 행정정보 연계가 붙기 전까지의 의도적 dead input(코드 주석에 명시). 정보성으로만 남김.
  - 외국인 거소·체류만료(1170~1182) → 외국인 기부 흐름 미구현(축소).

### G. 체크아웃(결제) — `CheckoutView.vue` — 재현누락(중)

AS-IS `order/step1.html` 대조. 서버 quote 재사용(preview=complete 동일계산)·쿠폰 숨김([[domain-code-facts]], 문서화)·동의 필수는 충실. 그러나 **결제 전 필드 검증이 AS-IS보다 얕다.**

- **AS-IS**(step1.html)는 결제 직전 각 항목을 alert로 막는다: "주문자 이름이 없습니다."(:1627)·"주문자 주소를 입력해주세요."(:1637)·"주문자 우편번호를 입력해주세요."(:1647/1657)·"주문자 상세주소를 입력해주세요."(:1667)·지자체별 "…포인트가 부족합니다."(:1425)·"구매에 동의해주시기 바랍니다."(:1578).
- **MSA**(`submit`)는 **`form.agree`만 검사**한다. 받으시는분·주소·우편번호 입력칸에 `required`가 붙어 있으나, 결제 버튼이 `<button type="button" @click="submit">`(폼 submit 아님)이라 **HTML5 required가 발동하지 않는다** → 빈 값으로도 결제 시도가 나간다. 포인트 부족도 `insufficient()`는 있으나 `submit`에서 안 쓴다(장바구니→체크아웃 진입 시 `CartView.goCheckout`에서만 검사).
- **심각도**: 서버(`OrderService.checkout`)가 배송지 필수면 서버 에러(errorMessage)로 드러나 조용히 깨지진 않으나, AS-IS의 구체적 필드 안내 대비 UX 저하 + 배송지 없이 주문 생성 가능성(서버 허용 시)이 있어 **확인·보강 권장**.
- **조치**: `submit`에 받는사람/주소/우편번호/상세주소/포인트부족 검증을 AS-IS 문구로 추가(작은 수정).

### H. 마이페이지 포인트 — `MyPointsView.vue` — 충실

AS-IS `mypage/cntrPoint.html` 대조. 연도·지자체별 집계·기부처 표시가 AS-IS와 일치. "포인트 사용" 폼은 AS-IS엔 없는 개발용 수동차감 기능(백엔드 `manual-point-use` 스위치로 gate, 기본 off)이라 정상. 완료문구("포인트가 사용되었습니다.") 있음. **새 결함 없음.**

### I. 특정사업 목록/상세 — `DesignatedList/DetailView.vue` — 충실(지도 위젯만 범위밖)

AS-IS `designated-donation/index-main.html`·`details.html` 대조. 상태/정렬(최근·참여금액·모금율·종료임박)/사업구분 필터·페이지네이션·모금현황·응원메시지·기간표기 재현. **"지자체별 검색" 지도 팝업**(`fragments/dsg-search-header.html`)만 미포팅 = gift 답례품몰·order 장바구니가 각자 헤더의 같은 지도 위젯을 남긴 것과 **동일한 의도적 경계**(locgovCode 쿼리·키워드검색 q는 지원). [[deferred-features]]의 지도선택 보류와 일치 → 재현누락 아님.

### 커버리지 체크리스트 (다음 감사 대상)

- [x] 인증 - 로그인 응답 코드 분기 (A1~A3)
- [x] 인증 - 아이디/비번찾기(FindIdPwView): **대체로 충실**(B), 본인인증만 의도적 축소
- [x] 인증 - 회원가입(SignupView): 인벤토리 기존판정 요지 반영(C), 정밀대조는 필요시 승격
- [x] 마이페이지 - 주문조회/상세(MyOrders/OrderDetail): D1(완료알림 누락, 전역 가능성)·D2(클레임 확인)
- [x] 답례품 - 목록/상세(GiftList/Detail): D1 확인(문의등록 완료문구 누락 1건), 후기=충실. 재입고 알림은 SPA 미배선(별도 갭, gift 감사 참조)
- [x] 장바구니(Cart): D1 확인(선택삭제 완료문구 누락 1건), 담기·검증문구 충실. Checkout 쿠폰·동의는 미대조(남김)
- [x] 기부(Donate): **충실**(F), 갭은 전부 알려진 축소(본인인증·주민번호·외국인)
- [x] 전역 - 401 처리(A3): 증거·권고안 정리(미적용). 성공알림(D1)은 수정 완료
- [x] 마이페이지 - 포인트(MyPoints): **충실**(H)
- [x] 장바구니→주문(Checkout): **G 발견**(결제 전 필드검증 얕음, 중)
- [x] 기부 특정사업(DesignatedList/Detail): **충실**(I), 지도 위젯만 의도적 범위밖
- [ ] (필요시) 회원가입 정밀대조 승격(C) — 나머지 대민 대상은 모두 감사 완료
- [ ] (선택) G 보강, A3와 함께 세션·검증 UX 마감

> 각 항목은 AS-IS `op.saleson.js`의 해당 API 호출 + 화면 html + 서버 컨트롤러를 함께 대조해 채운다.

---

## 4. admin 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-admin.md`

## AS-IS 기능 감사 - admin(운영관리) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(`ghlove-web` saleson 백엔드, JSP `WEB-INF/views/opmanager/i18n/**`) → `ghlove-msa/admin`

### 0. 조사 방법

이 감사의 목적은 `admin_console_real_asis_markup_round` 메모의 "AS-IS opmanager 123개 화면 전부 완료" 주장을 실측으로 검증하는 것이다. AS-IS `ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` 하위를 전수 `find`로 나열하고(총 165개 리프 디렉토리, 531개 `.jsp` 파일), 각 디렉토리를 아래 기준으로 분류했다.

- **admin 소관**: 이 감사의 범위. 공통코드/공지/배너/팝업/통계/정산/외부연계/RBAC/시스템로그/배치/설정/게시판/고객센터 등 크로스커팅·운영전용 기능.
- **타 서비스 소관**(member/donation/point/gift/order): 다른 담당 에이전트가 확인 중이므로 화면수만 집계하고 상세 조사 생략.
- 각 admin 소관 항목은 실제 `ghlove-web` Java 컨트롤러(`@RequestMapping`)로 살아있는 경로인지 먼저 확인한 뒤, `ghlove-msa/admin`의 컨트롤러+템플릿 존재 여부와 로직 동등성을 코드 레벨로 대조했다.

### 1. AS-IS opmanager 카테고리별 화면 현황 (집계표)

전체: **165개 디렉토리, 531개 JSP 파일** (list.jsp 보유 디렉토리만 120개 — 이는 "123개 화면" 주장과 근접한 수치이며, 상세는 5절 참고)

| 카테고리(디렉토리) | AS-IS JSP 수 | 소관 | TO-BE 상태 | 비고 |
|---|---|---|---|---|
| user (+manager/customer/locgov/pki/popup 등) | 74 | member (일부 admin) | 혼재 | manager/manager-request/manager-role/pki(관리자계정·RBAC, ~10개)는 admin 소관이며 `admin_rbac_6tier_role_round`로 이미 커버됨. customer/locgov/secede-user 등(~64개)은 member 담당 에이전트가 확인 중 |
| order (+list/detail/popup/process/include) | 54 | order | 타서비스담당 | order 담당 에이전트 확인 중 |
| give (+give-notice/operation/point/reqmng/state/statistics) | 39 | donation | 타서비스담당 | 이미 알려진 give-statistics 등 포함, donation 담당 확인 중 |
| shop-statistics (+dashboard/report/sales/wish) | 38 | gift/point | 커버됨 | `report_statistics_suite`, `shop_statistics_tier_b_round` |
| item (+popup/representative-item/review/seller) | 25 | gift | 타서비스담당 | gift 담당 에이전트 확인 중 |
| community (+databoard/faqBbs/freeboard/locgfaq/offSrBbs/oldFreeBoard/srBbs) | 24 | **admin** | 커버됨(1개 죽은코드) | `CmntyBoardController`(bbs/sr-bbs/off-sr-bbs/faq-bbs 4종 통합), `DataBoardController`, `LocgFaqAdminController`. `oldFreeBoard`(1개)는 AS-IS Java 소스에 컨트롤러 매핑 자체가 없어 죽은 코드로 확인 |
| offgive (+prj) | 19 | donation | 타서비스담당 | `offgive_and_remittance_rounds`로 이미 커버 확인됨 |
| designated-donation (+analysis/banner/notice/part/request) | 17 | donation | 타서비스담당 | `designated_donation_admin_round`로 이미 커버 확인됨 |
| config (+deny/google-analytics/pg/policy/popup) | 17 | **admin** | **부분GAP** | site-config/policy/isms만 커버, 나머지는 4절 참고 |
| log (+popup/user) | 15 | **admin** | 커버됨 | `system_infra_log_round` |
| magicline (+include) | 12 | **admin** | 커버됨 | `external_integrations_architecture` |
| proposal (+donation) | 10 | donation | 타서비스담당 | |
| remittance (+confirm/expected/finishing) | 9 | donation | 타서비스담당 | `offgive_and_remittance_rounds`로 이미 커버 확인됨 |
| catalog (+cardNews/catalogMng/content/locgovFavItem) | 8 | gift | 타서비스담당 | |
| statistics (+locgov) | 6 | **admin** | 커버됨 | `give_statistics_scope_and_levy_assumption`, `report_statistics_suite` |
| qna-open | 6 | **admin** | 커버됨 | `QnaController`/`QnaApiController` 존재 확인 |
| qna | 6 | **admin** | 커버됨 | 상동 |
| proposal-statistics | 6 | donation | 타서비스담당 | |
| main (+popup) | 6 | **admin** | 커버됨 | `main_banner_carousel_bug` 메모(메인배너) |
| welfareCenter (+popup) | 5 | donation | 타서비스담당 | |
| temp-process (+order/remittance) | 5 | donation/order | 타서비스담당 | |
| stats (+visit) | 4 | **admin** | **부분GAP** | `/stats/sla`만 커버(`sfr_gap_fill_round2`), 접속경로/방문통계(`stats/referer.jsp`,`stats/visit/index.jsp`)는 미구현 |
| seller | 4 | gift | 타서비스담당 | |
| qna-item | 4 | gift | 타서비스담당 | |
| **newsletter** (send/template) | 4 | **admin** | **GAP(신규발견)** | |
| **manual** (manager/user) | 4 | **admin** | **GAP(신규발견)** | |
| featured | 4 | gift | 타서비스담당 | |
| **categories-team-group** (team/group) | 4 | **admin** | **GAP(신규발견)** | 관리자 팀별 그룹 관리 |
| banner (+main) | 4 | **admin** | 커버됨 | `BannerApiController`, `OperationContentService` |
| user-group | 3 | member | 타서비스담당 | |
| shipment-return | 3 | order | 타서비스담당 | |
| shipment | 3 | order | 타서비스담당 | |
| seasonal-food | 3 | gift | 타서비스담당 | |
| **qustnr** | 3 | **admin** | **GAP(신규발견)** | 설문조사 관리 |
| popup | 3 | **admin** | 커버됨 | `content/popup-list.html` 등 |
| menu | 3 | **admin** | 커버됨 | 메뉴 RBAC |
| **maintenance** | 3 | **admin** | **GAP(신규발견)** | 시스템 점검 공지 |
| mail-config | 3 | **admin** | 커버됨 | `MailConfigController` |
| mail | 3 | **admin** | 커버됨 | `OpEmailController`(AS-IS `/opmanager/email/**` → view `mail/list` 매핑 확인) |
| **locgov-notice** | 3 | **admin** | **GAP(신규발견)** | 지자체 담당자 대상 공지(일반 공지사항과 별개) |
| lclgvHnrUser (+viewHist) | 3 | donation | 타서비스담당 | |
| delivery | 3 | order | 타서비스담당 | |
| categories | 3 | gift | 타서비스담당 | |
| brand | 3 | gift | 타서비스담당 | |
| user-level | 2 | member | 타서비스담당 | |
| speciality-item | 2 | gift | 타서비스담당 | |
| sellerNotice | 2 | gift | 타서비스담당 | |
| representative-banner | 2 | gift | 타서비스담당 | |
| qna-admin | 2 | **admin** | 커버됨 | `QnaAdminController` |
| pay-info (+cancel-fail-info) | 2 | order | 타서비스담당 | |
| notice | 2 | **admin** | 커버됨 | `NoticeApiController` |
| **message** | 2 | **admin** | **GAP(신규발견)** | 관리자↔회원 메시지 발송 관리 |
| inquiry | 2 | member | 타서비스담당 | |
| group-banner | 2 | **admin** | **GAP(신규발견)** | 그룹별 배너(현재 Banner 엔티티는 단일 타입) |
| group | 2 | gift(추정) | 타서비스담당 | |
| faq | 2 | **admin** | 커버됨 | `FaqController`/`FaqApiController` |
| delivery-company | 2 | gift/order | 타서비스담당 | |
| data-board | 2 | **admin** | 커버됨 | `DataBoardController`/`DataBoardApiController` |
| code | 2 | **admin** | 커버됨 | `CommonCodeController` |
| **cntnts-stsfdg** | 2 | **admin** | **GAP(신규발견)** | 콘텐츠(페이지)별 만족도 조사 통계 |
| **board-cfg** | 2 | **admin** | **GAP(신규발견, 우선순위 낮음)** | 게시판 마스터/스킨 설정 — TO-BE는 게시판 4종이 고정 스키마라 실효성 낮음 |
| batch-job | 2 | **admin** | 커버됨 | `BatchJobController` |
| access | 2 | **admin** | 커버됨 | `AccessController` |
| **user-login-banner** | 1 | **admin** | **GAP(신규발견)** | 로그인 페이지 전용 배너 |
| sms-log | 1 | **admin** | 커버됨(추정) | `log` 라운드 계열로 추정, 별도 재확인 권장 |
| sellerconfirmOrder | 1 | gift/order | 타서비스담당 | |
| sellerconfirm | 1 | gift/order | 타서비스담당 | |
| point-check | 1 | **admin** | 커버됨 | `ReconciliationController`(`reconciliation/order-point.html`), AS-IS `PointCheckManagerController`와 동일 view 재구현이라고 코드 주석에 명시 |
| oz | 1 | **admin** | 보류확인됨 | OZReport, `asis_ozreport_gap` 메모 |
| order-agency | 1 | order | 타서비스담당 | |
| **juso** | 1 | **admin** | **GAP(신규발견, 우선순위 낮음)** | 주소검색 팝업(Daum API) — 공용 UI 위젯 성격 |
| isms | 1 | **admin** | 커버됨 | `ConfigIsmsController` |
| **file** | 1 | **admin** | **GAP(신규발견, 우선순위 낮음)** | 파일첨부 유틸(에디터용) — SmartEditor2 보류와 연계된 항목 |
| claim-memo | 1 | order | 타서비스담당 | |
| chatbot | 1 | **admin** | 죽은코드 확인됨(스킵) | AS-IS Java 소스 전체에 `chatbot` 컨트롤러가 없음(템플릿만 존재) |
| batch-log | 1 | **admin** | 커버됨 | `system_infra_log_round` |

### 2. 이미 알려진 것 재확인만(상세 생략)

지시받은 대로 아래 항목은 이미 완료 기록이 있어 코드 존재만 재확인하고 깊이 파지 않음: 공통코드(`CommonCodeController`), 공지/배너/팝업 CRUD(`NoticeApiController`/`BannerApiController`/`content/popup-*`), 통계(`StatsController`/`GiveStatisticsController`/`ReportStatisticsController`/`ShopStatisticsTierBController`), 정산(`SettlementController`/`ReconciliationController`), 외부연계(`CertLoginController`/`NhExportBatchController`), 배송조회(`DeliveryTrackingController`), RBAC(`ManagerAuthController`/`ManagerRequestController`/`AccessController`), 2차인증+메뉴RBAC(`ManagerAuthAdvice`/`HeaderAuthAdvice`), 지정기부(`DesignatedProjectAdminController` 등), offgive+remittance, Kong(`OpenApiController`), MFA+SLA(`/stats/sla`). 전부 파일 존재 확인됨.

### 3. admin 고유 카테고리 gap 상세

#### 우선순위: 높음

**1. 관리자 팀별 그룹 관리 (`categories-team-group`, 4화면)**

AS-IS `saleson.shop.categoriesteamgroup.CategoriesTeamGroupManagerController`(`/opmanager/categories-team-group`)는 관리자(매니저)를 팀/그룹 단위로 조직화하는 CRUD를 제공한다(팀 등록/수정/삭제, 그룹 등록/수정/삭제, 순서변경). `admin_rbac_6tier_role_round`에서 구현한 6단계 역할(ROLE_ADMIN_1~6)과는 별개 축으로, 관리자를 부서/팀으로 묶는 조직 구조 관리 기능이다. TO-BE `admin` 소스 전체를 grep해도 `team`/`Team`/`그룹` 관련 관리자 조직 개념이 전혀 없다.

**2. 그룹별 배너 관리 (`group-banner`, 2화면)**

AS-IS `GroupBannerManagerController`(`/opmanager/group-banner`, 클래스 주석 "그룹별 베너 관리")는 위 팀/그룹 단위로 스코프되는 별도 배너 타입이다. TO-BE `Banner` 엔티티(`OperationContentService`)는 `bannerType` 같은 구분 필드 없이 단일 타입(메인배너)만 다루므로 그룹 스코프 배너는 없음.

**3. 지자체 공지사항 (`locgov-notice`, 3화면)**

AS-IS `LocgovNoticeManagerController`(`/opmanager/locgov-notice/**`, 클래스 주석 "지자체 공지사항")는 일반 공지사항(`notice`, 커버됨)과 별개로 지자체 담당자만을 대상으로 하는 공지 채널이다. TO-BE `NoticeApiController`/`content/notice-*.html`은 대상 구분(전체공지 vs 지자체전용) 필드나 화면이 없어 이 세분화가 없음.

**4. 관리자↔회원 메시지 관리 (`message`, 2화면)**

AS-IS `MessageManagerController`(`/opmanager/message/**`, 메뉴명 "메세지 관리")는 아이디/메시지 내용으로 검색하는 쪽지(메시지) 발송·관리 화면이다. TO-BE `admin` 소스에 `/message` 경로나 관련 컨트롤러가 전혀 없음.

#### 우선순위: 중간

**5. Config 세부 설정 잔여분 (`config` 하위, admin 소관분)**

AS-IS `ConfigManagerController`(`/opmanager/config/**`)는 하나의 컨트롤러가 site-config/conversion-tag/popup(전환추적팝업)/pg/google-analytics/policy/deny 등 12개 이상의 세부 설정을 제공한다. TO-BE는 `ShopConfigController`(`/site-config` 1종만), `PolicyController`(policy), `ConfigIsmsController`(isms)만 구현되어 있고, **전환추적 태그/팝업(conversion-tag, popup/conversion-popup), Google Analytics 설정(google-analytics), PG 설정(pg), 회원등록 불가능ID(deny)**는 미구현. 이 중 `deny`(회원 아이디 블록리스트)는 member 도메인과 겹칠 수 있어 교차 확인 권장. 나머지(전환추적/GA/PG)는 admin(운영/마케팅) 소관으로 판단.

**6. 설문조사 관리 (`qustnr`, 3화면)**

AS-IS `QustnrManagerController`(`/opmanager/qustnr/**`, "설문 관리")는 설문 등록/응답결과 조회 기능. TO-BE에 대응 없음.

**7. 뉴스레터 발송/템플릿 관리 (`newsletter`, 4화면)**

AS-IS `NewsletterManagerController`(`/opmanager/newsletter`, "메일 매거진 리스트")는 뉴스레터(메일매거진) 발송 이력 및 템플릿 관리. `mail-config`(발송 템플릿 설정, 커버됨)나 `email`(단건 발송, 커버됨)과는 별개로 대량 뉴스레터 캠페인 관리 기능이라 TO-BE에 없음.

**8. 매뉴얼 관리 (`manual`, 4화면)**

AS-IS `ManualManagerController`(`/opmanager/manual/**`, "메뉴얼 관리")는 관리자용/회원용 매뉴얼 문서를 등록·관리하는 화면. TO-BE에 대응 없음.

**9. 콘텐츠 만족도조사 통계 (`cntnts-stsfdg`, 2화면)**

AS-IS `CntntsStsfdgManagerController`(`/opmanager/cntnts-stsfdg`, "만족도 조사")는 페이지(URL)별 만족도 응답 집계를 연도별로 보여주는 통계 화면. TO-BE `StatsController`/통계 계열에 대응 없음.

**10. 사이트 방문/접속경로 통계 (`stats`/`stats/visit`, 4화면)**

AS-IS `stats/referer.jsp`("접속경로 통계"), `stats/visit/index.jsp`("접속통계")는 TO-BE `StatsController`가 커버하는 SLA 통계(`/stats/sla`)와는 다른, 사이트 방문자/유입경로 분석 화면이다. 미구현.

**11. 시스템 점검 공지 (`maintenance`, 3화면)**

AS-IS `MaintenanceController`(`/opmanager/maintenance/`)는 점검 시간대에 서비스 화면에 점검 안내를 노출하는 기능. TO-BE에 대응 없음.

**12. 로그인 배너 (`user-login-banner`, 1화면)**

AS-IS `UserLoginBannerManagerController`(`/opmanager/user-login-banner`, "PC 로그인 배너")는 로그인 페이지 전용 배너. TO-BE `Banner`는 단일 타입이라 미구현.

#### 우선순위: 낮음

**13. 게시판 마스터 설정 (`board-cfg`, 2화면)** — `com.onlinepowers.board.BoardCfgController`(`/opmanager/board-cfg`)는 신규 게시판 타입을 동적으로 생성/설정하는 범용 CMS 게시판 엔진 설정 화면이다. TO-BE는 게시판을 4종 고정 스키마(`CmntyBoardController`)로 재구현했으므로 신규 게시판 타입을 동적으로 만들 필요가 실질적으로 없다. 스킵해도 기능상 지장 없을 가능성이 높으나, 명시적 보류 결정은 아니므로 표에는 gap으로 남김.

**14. 파일첨부 유틸리티 (`file`, 1화면)** — 위지윅 에디터(SmartEditor2, 이미 보류 확인됨)에 종속된 첨부파일 브라우저. SmartEditor2 도입 여부가 정해지면 함께 처리 권장.

**15. 주소검색 팝업 (`juso`, 1화면)** — Daum 우편번호 API 연동 팝업. 화면이라기보다 공용 UI 위젯이며, 주소 입력 필드가 있는 각 도메인(회원가입/배송지 등)에서 이미 별도 방식(관련 서비스 자체 주소 입력)으로 처리됐을 가능성이 있음. 낮은 우선순위.

#### 추가로 발견한 프로젝트 전역 이슈 (admin 소관은 아니나 기록)

**엑셀 다운로드 기능 전무**: `admin/src/main/java` 전체를 grep한 결과 `excel`/`Excel`/`엑셀` 매치가 0건이었다. AS-IS opmanager는 로그/통계/give-point/order 등 거의 모든 목록 화면에 엑셀 다운로드 버튼이 있었다(`point` 서비스 감사에서도 동일하게 지적됨 — `as-is-feature-audit-point.md` #2). admin 하나만의 문제가 아니라 프로젝트 전역 공통 컴포넌트 부재로 보이며, 여러 서비스 감사 결과를 취합해 "엑셀 다운로드 공통 모듈 도입"으로 한 번에 처리하는 것을 권장.

### 4. "AS-IS opmanager 123개 화면 전부 완료" 주장 검증 결론

**실측 결과**: AS-IS `opmanager/i18n/` 하위 실제 화면 수는 관점에 따라 다음과 같다.

- JSP 파일 총수: **531개** (list/form/edit/detail/popup 등 한 논리 화면이 여러 파일로 나뉘는 경우 다수 포함, 예: `order/list` 15개, `shop-statistics/sales` 28개)
- `list.jsp`(목록/진입 화면) 보유 디렉토리 수: **120개**
- JSP를 가진 리프 디렉토리(팝업/프래그먼트 포함) 총수: **165개**

"123개"라는 수치는 531(전체 JSP 파일)과는 거리가 멀지만, **120(list.jsp 기준 대표 화면 수)과는 매우 근접**하며, `maintenance`/`main`/`log`/`magicline`/`statistics` 등 list.jsp 없이 단일 폼·인덱스로 동작하는 정상 화면 몇 개를 더하면 123 안팎에 수렴하는 합리적인 수치로 보인다. 즉 **"123개"는 팝업/폼/상세 등 보조 파일을 제외한 "메뉴 진입 가능한 대표 화면" 기준으로는 대체로 타당한 카운트였다고 판단된다.**

다만 **완료율 자체는 재검증이 필요하다**. 이번 감사에서 admin 고유 카테고리 165개 리프 디렉토리 중 새로 확인한 순수 gap은 아래와 같다.

- 신규 발견 미구현: **12개 카테고리, 약 34개 JSP 상당** (categories-team-group 4, group-banner 2, locgov-notice 3, message 2, qustnr 3, newsletter 4, manual 4, cntnts-stsfdg 2, stats/visit 관련 잔여 2~4, maintenance 3, user-login-banner 1, board-cfg 2)
- 부분 GAP: config 하위 세부 설정 다수(전환추적/GA/PG/deny 등, 약 7~8개 JSP 상당)
- 저우선순위: file 1, juso 1
- 죽은코드(정당한 스킵): chatbot 1, community/oldFreeBoard 1
- 기존 보류 확인됨: oz(OZReport) 1

따라서 "123개 화면 전부 완료"는 **화면 수 집계(120~123) 자체는 대체로 맞았으나, "전부 완료"라는 완료율 주장은 과장되었다** — 실측 결과 admin 소관만으로도 최소 12개 카테고리(신규 미구현)가 이번 감사에서 처음 드러났다. 이전 라운드들이 대체로 "타 서비스 도메인과 겹치지 않는 admin 소관 대형 카테고리"(로그/통계/정산/연계/RBAC/공지/배너 등)를 잘 완료한 것은 사실이나, `stats`/`user`/`config` 하위처럼 하나의 상위 디렉토리 안에 여러 개의 이질적인 세부 화면이 섞여 있는 경우 일부만 구현하고 나머지를 놓친 패턴이 반복 확인된다.

---

## 5. admin parity 감사

> 통합 전 파일: `docs/admin-parity-audit.md`

## admin 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS opmanager(운영관리)를 "서비스로직도 화면단도 똑같이" 재현하기 위한
> **전수 갭 목록**. 방식 `[[asis-porting-principles]]` — "발견되면그때"가 아니라 이 목록
> 기준으로 구현한다. NEW/CHANGED(ISP·RFP) 요건은 표기 + 착수 전 사용자 확인.
>
> 상태 범례: **O** 재현됨 / **X** 누락 / **부분** 일부만 / **확인** 추가 대조(심도검증) 필요.
> 작성 2026-09-23 (5개 서비스 완료 후 admin 라운드).

---

### 0. 이 문서의 위치 — 기존 심층감사 재조정(reconcile)

admin은 이미 두 라운드의 심층감사가 있다:
- [as-is-admin-gap-deep-audit-part1.md](as-is-admin-gap-deep-audit-part1.md) — AS-IS 67개 컨트롤러
- [as-is-admin-gap-deep-audit-part2.md](as-is-admin-gap-deep-audit-part2.md) — AS-IS 나머지 66개 (배치 A/B/C/D)
- [as-is-feature-audit-admin.md](as-is-feature-audit-admin.md) · [as-is-inventory-admin.md](as-is-inventory-admin.md)

**단 이 감사들은 2026-09-03 기준이고, 그 뒤 admin이 대폭 구축됐다(컨트롤러 51→103개).**
당시 "진짜미구현"이던 갭 대부분에 이제 TO-BE 컨트롤러가 존재한다. 이 문서는 **09-03 갭목록을
현재 admin 실물(103 컨트롤러 / ~65 템플릿 폴더)로 재대조**해, (a) 이미 닫힌 갭, (b) 여전히
남은 갭(X), (c) 컨트롤러는 생겼으나 기능단 parity 심도검증이 필요한 것(확인)으로 정리한다.

> ⚠ **판정 원칙**: "TO-BE 컨트롤러 존재 = gross(존재) 갭 해소"일 뿐, **AS-IS와 화면·검증분기·
> 매퍼까지 동일한가(feature parity)는 영역별 심도검증이 남는다.** 아래 "O"는 별도 표기가 없으면
> "컨트롤러/화면 존재, 심도 parity는 해당 영역 라운드에서 확정" 수준으로 읽는다.

---

### 1. 09-03 "진짜미구현" 갭 → 현재 상태 재대조

#### 1-A. 이미 닫힌 갭 (09-03 미구현 → 현재 TO-BE 컨트롤러 존재)

| 09-03 갭 | 현재 TO-BE 컨트롤러 | 상태 |
|---|---|---|
| 3b 관리자 대시보드(홈) | `AdminHomeController` | O(존재) |
| 20 답례품 카테고리 CRUD | `CategoryAdminController` | O(존재) |
| 23 카테고리 팀/그룹 | `CategoryTeamAdminController` | O(존재) |
| 38 자료실 관리자 CRUD | `DataBoardAdminController` | O(존재) |
| 39 택배사 마스터 | `DeliveryCompanyAdminController` | O(존재, inventory §10 조치완료) |
| 46 기획전(featured) | `FeaturedAdminController` | O(존재) |
| 58 1:1상담(구, OP_SHOP_INQUIRY) | `ShopInquiryAdminController` | O(존재) |
| 60 답례품 통합관리(부분) | `ItemAdminController` + `ReviewAdminController` | 부분(심도확인) |
| 62 지자체 명예직원 | `LclgvHnrUserAdminController` | O(존재) |
| 63 엑셀다운로드로그 / 국세청연계로그 | `ExcelDownloadLogAdminController` / `NtsReceiptLogAdminController` | O(존재) |
| A1 메인진열아이템 | `MainDisplayAdminController` | O(존재) |
| A2 점검(maintenance) | `MaintenanceAdminController` | O(존재) |
| A3 매뉴얼 | `ManualAdminController` | O(존재) |
| A4 메뉴 CRUD | `MenuAdminController` | O(존재) |
| A6 모바일카테고리편집 | `MobileCategoryEditAdminController` | O(존재) |
| A10 판매자 시스템공지 | `SysNoticeSellerAdminController` | O(존재) |
| A13 대표배너 | `RepresentativeBannerController` | O(존재) |
| A14 검색어 관리 | `SearchKeywordAdminController` | O(존재, gift-search-keyword-parity-audit) |
| A15 SEO | `SeoAdminController` | O(존재) |
| A16 스타일북 | `StyleBookAdminController` | O(존재) |
| B2 포인트 관리 | `PointHistoryAdminController` | 부분(심도확인) |
| B8 온라인설문 | `QustnrAdminController` | O(존재) |
| B9 랭킹 | `RankingAdminController` | O(존재) |
| B10/B13 리포트/방문통계 | `ReportStatisticsController` / `StatsController` | 확인(방문통계 로깅) |
| B11 지자체통계 | `StatisticsLocgovController` | O(존재) |
| B14/B15 메일·SMS 발송로그 | `SendLogAdminController` | O(존재) |
| B19 UMS 통합알림 | `UmsAdminController` | O(존재) |
| 27/28 커뮤니티 댓글 | `CmntyBoardController`(+`CmntyCommentRepository`) | 부분(댓글 저장·표시 존재, CRUD 심도확인) |
| C2 주문관리 콘솔 | `OrderAdminController` | O(존재, 최우선갭 해소) |
| C3 주문대행 | `OrderAgencyAdminController` | O(존재) |
| C4 결제정보 | `PayInfoAdminController` | O(존재) |
| C13/C14 반품(shipment-return) | `ShipmentReturnAdminController` | O(존재) |
| C18 보류주문 처리 | `HoldOrderAdminController` | O(존재) |
| C19 복지관 | `WelfareCenterAdminController` | O(존재) |
| D1 지자체 마스터관리 | `LocgovAdminController` | O(존재, LocgovSeal 무인증화면 대체) |
| D2 매니저계정+회원360 | `ManagerAdminController` + `MemberAdminController` | O(존재) |
| D3 일반회원 검색/상세 | `MemberAdminController` | O(존재) |
| D4 탈퇴회원 | `SecedeUserAdminController` | O(존재) |
| D5 휴면회원 | `SleepUserAdminController` | O(존재) |
| D6/D9 지자체·운영 담당자 | `PersonInChargeAdminController` | O(존재) |
| D8 오프라인 담당자 | `OffPersonInChargeAdminController` | O(존재) |
| D10 역할·메뉴권한 | `RoleAdminController` | O(존재) |
| D11 회원등급 | `UserLevelAdminController` | O(존재) |

#### 1-B. ★ 여전히 남은 갭 (현재도 TO-BE 컨트롤러/화면 없음 = X)

| # | AS-IS 기능 | 분류 | 성격 / 근거 | 우선순위 |
|---|---|---|---|---|
| ~~G1~~ | ~~FAQ 관리자 CRUD(#45)~~ | **오탐→O (기능parity 충족)** | **[2026-09-23 정정+A조사 완결] 이미 구현됨.** 스토어프론트 `FaqController`(`/faqs`)와 관리자 CRUD `LocgFaqAdminController`(`/community/locv-faq`)가 동일 `LocgovFaqRepository`(`OP_COMMUNITY_LOCGOVFAQ`, ≈63건)를 공유(list/new/edit/create/update/toggle/delete + 지자체담당자 조회전용 RBAC). **A조사 결론**: ①**per-지자체(locgovCode) FAQ는 AS-IS에도 없음** — AS-IS `locgFaq-mapper.xml`도 `op_community_locgovfaq`에 locgovCode 컬럼 없음("LocgFaq"는 AS-IS부터 misnomer). ②테이블 반전 존재: **AS-IS 고객FAQ(`/api/faq`)=`OP_FAQ`(FaqType enum)**, `op_community_locgovfaq`는 고객노출 0(admin편집만)이었는데, **TO-BE는 고객FAQ를 `op_community_locgovfaq`로 서빙하고 `OP_FAQ`는 0행 고아**. 콘텐츠를 반대 테이블에 담았을 뿐 **고객 노출+관리자 CRUD = 기능parity 충족**. ⚠비기능 debt만: (a) 고아 `OP_FAQ` 정리, (b) 일반FAQ가 `locv-faq`/`LocgovFaq` 이름으로 서빙되는 명명혼선(SalesOn 잔재) | - |
| G2 | 카탈로그 관리(#19: 연간사업/즐겨찾기상품/카드뉴스/콘텐츠) | X | `Catalog*` 컨트롤러 없음. DB(`G_CATALOG_*`)도 schema.sql 원본에만 존재, 서비스DDL 미이관. gift/admin 소관 결정 선행 | 中 |
| G3 | 공통메시지 관리(A5 MessageManager, `OP_COMMON_MESSAGE`) | X | 컨트롤러 없음. 아키텍처 불일치 주의(09-03) | 低 |
| G4 | SMS 설정(B17 SmsConfig) | X | 컨트롤러 없음. UMS(`UmsAdminController`)로 흡수 권장 | 低 |
| G5 | 제철식품관 관리(C6 SeasonalFood) | X | 컨트롤러 없음. storefront 제철식품관과 연동 | 低 |
| G6 | 명품/특산품관(C15 SpecialityItem) | X | 컨트롤러 없음 | 低 |
| G7 | 콘텐츠 만족도조사(#25 CntntsStsfdg) | X | 컨트롤러 없음. 실사용 빈도 낮음 | 低 |
| G8 | 전자서명 캡처(#65 MagicLine) | X(외부연계) | 매직라인 상용연계. [[external-integrations-architecture]] mock 게이트 패턴 미적용. 법적필수 아니면 mock UI만 | 低 |
| G9 | 배치 실행이력 로그(#13 BatchLog) | X(설계선결) | 동적 스케줄러 엔진 부재 → 로그 소스 자체가 없음. 각 서비스 `@Scheduled`에 이력기록 추가 vs 스코프아웃 결정 선행 | 低 |

#### 1-C. 심도검증 결과 (V1~V8 전건 완료 2026-09-23) — 컨트롤러 존재 → 기능parity 확정

| # | 항목 | 확인 포인트 |
|---|---|---|
| V1 | 33 쇼핑몰설정 허브 잔여 서브화면(~10종) | **[2026-09-23 판정: O 거의 완비]** `SiteConfigExtController`가 **가입거부(IP/이메일도메인)·결제수단·배송기본+희망배송일·PG·전환추적스크립트·GA·주문임시저장** 전부 커버(클래스 주석 명시, OP_CONFIG/OP_CONFIG_PG/OP_CONFIG_GOOGLE_ANALYTICS 단일행). 랭킹=`RankingAdminController`(/admin/ranking), site-config/shop-config/가입불가ID/금지어=`ShopConfigController`, policy=`PolicyController`. 09-03 "~10종 미구현" 판정 obsolete. **유일 잠재 미커버: 전역 "포인트 적립/사용 정책" 1화면** — 고향사랑은 지자체별 지급률(`LocgovAdminController`)이 실질 정책이라 전역 shop 포인트정책은 N/A 가능성. 低(확인만) |
| V2 | 24 클레임메모(ClaimMemo) | **[2026-09-23 판정: 부분 O]** TO-BE `order/ClaimMemo`(`OD_CLAIM_MEMO`) + `OrderAdminController` 클레임큐(`/admin/claims`, 클레임별 메모)·승인/거절/완료 메모·`addClaimMemo`·주문상세 관리자메모까지 RBAC/지자체 스코프로 완비. AS-IS `ClaimMemoManagerController`는 조회전용 **통합 메모목록(기간필터 today/week/1·2개월)** 단일화면뿐 → 실체는 대체됨. **미제공: memo-centric 통합 목록/기간검색 뷰**(claim queue는 claim-centric·status필터). 低우선 |
| V3 | 56/57 카테고리그룹/그룹배너 | **[2026-09-23 판정: 신 O / 구 오분류→잠정 X]** 신버전(CategoriesTeamGroup·GroupBanner)은 TO-BE `CategoryTeamAdminController`(+gift `CategoryTeam/Group/GroupBanner`, OP_CATEGORY_TEAM/GROUP/GROUP_BANNER)로 CRUD 커버 O. **단 09-03 감사가 #56 `GroupManagerController`(saleson.shop.group, `OP_GROUP`)를 "카테고리그룹"으로 오분류** — 실제로는 **회원그룹(고객 세그먼트)** 관리다(`coupon-mapper.xml`이 `OP_GROUP ON UD.GROUP_CODE`로 조인해 쿠폰 대상·`display-mapper` 기획전 SpotApplyGroup 타겟팅에 사용, JSP `opmanager/group/{list,form}.jsp` 생존). TO-BE엔 **회원그룹(OP_GROUP) 관리 없음**. **[후속 확정]** TO-BE 쿠폰은 **회원등급(`targetUserLevel`)+선택회원+특정상품**으로 타겟팅 → 회원그룹 없이 성립. AS-IS OP_GROUP(회원 세그먼트)은 구버전 쿠폰/기획전 타겟팅용인데 TO-BE가 등급+선택회원으로 **의도적 축소**. → **최종: 회원그룹 X(legacy 축소·[[asis-porting-principles]], 실사용 확인 시 재검토)**. 低 |
| V4 | 64 로그인화면 전용 배너 | **[2026-09-23 판정: O 흡수완료]** AS-IS는 로그인배너를 `CategoriesEdit`(code=loginWeb/loginMobile)로 관리(진입점 `/index-old`, legacy). TO-BE는 이를 **배너관리(`OperationContentController`/`Banner`)의 노출위치 필드 `bannerType`(MAIN/LOGIN_WEB/LOGIN_MOBILE)로 흡수 통합** — `OperationContentService.activeBanners(bannerType)`가 위치별 서빙, 코드 주석에도 명시. 별도 화면 불필요. ⚠경미: 스토어프론트 로그인화면이 `LOGIN_WEB` 배너를 실제 렌더하는지는 미확인(AS-IS 고객 로그인화면도 표시근거 약함·legacy) → 표시 연동만 후속 |
| V5 | B4/B7 상품문의·내부문의(QnaItem/QnaAdminManager) | **[2026-09-23 판정: 상품문의 O / 내부문의 X]** ①**상품문의(B4)=O**: TO-BE `InquiryAdminController`(`/admin/gift-inquiries`)가 gift `G_ITEM_INQUIRY`를 admin에서 답변/블라인드(SFR-005 라운드). ②**내부문의(B7, 지자체담당자↔본사)=X 진짜 갭**: 테이블 `G_QNA_ADMIN`(+ANSWER/ANSWER_FILE/FILE) 4종이 admin DB에 이관됐으나 **참조 Java 0·컨트롤러/화면 미구현**. TO-BE `QnaAdminController`(`/qna-admin`)는 **일반 1:1문의(OP_QNA)** 로 별개(경로만 우연히 동일). AS-IS `QnaAdminManagerController`는 locgov 스코프 + 문의유형(extentionCode 'locgov') + 시도코드 필터. **DB 준비됨 → 컨트롤러+화면만 추가**. 우선순위 中(지자체 담당자 운영지원) |
| V6 | C7/C8/C10~C12 판매자 셀프포털·출고·정산확인 | **[2026-09-23 판정: 별도 audit로 위임 — 기능 O(admin전용) / 셀프이관 보류]** `gift-seller-selfservice-parity-audit.md`에 확정: 출고/송장=order `/api/admin/orders/{id}/invoice`, 정산=admin `settlements`, 반품출고=order `DeliveryReturnAdminApiController` — 전부 **admin(운영관리) 전용으로 구현됨**. 판매자 공지(C8/P4)=`SellerISysNoticeController`(/seller/sys-notice) 2026-09-22 구현완료. **판매자 셀프 이관(P1~P3·P5)만 [[deferred-features]] 결정 대기로 보류**(P6~P8 SalesOn 보류). admin-parity C-series 중복은 그 문서 소관 |
| V7 | B13 방문통계 | **[2026-09-23 판정: X 미구현]** AS-IS `StatsManagerController`(`/opmanager/stats/visit`)는 **접속/방문 통계**(방문자·일별 접속, `getVisitSummary`, VisitExeclView). TO-BE `StatsController`(`/stats`)는 **SFR 기부/주문/포인트/gift 비즈니스 통계**로 성격 다름 → 방문통계 아님. TO-BE에 방문자 집계용 **접속 로깅 인프라 없음**(`ManagerActionLog`=관리자 액션감사 전용). **로깅 인프라(스토어프론트 접속 계측+서버 적재)부터 선결 필요**. 中 |
| V8 | A8 지자체공지 RBAC 스코핑 | **[2026-09-23 판정: O 해소됨]** TO-BE `Notice.locgovCode`(전역=NULL/지자체=코드) + `OperationContentController`가 스코핑 완비: `adminNotices`(list)는 `isLocgovScoped`면 `viewer.locgovCode==n.locgovCode` 필터, `createNotice`는 `effectiveLocgovCode(viewer,·)`로 자기 지자체 강제, `editNoticeForm`은 `requireOwnLocgovOrThrow`로 타 지자체 공지 수정 차단. 09-03 "스코핑 빠진 권한버그" fixed |

---

### 2. AS-IS 죽은코드(재현 불필요) — 재확인만

09-03 감사에서 **죽은코드로 판정된 것(렌더 대상 JSP 물리적 부재)**: BoardCfg/Board(범용CMS),
AccountNumber, Attendance, BanWord, Calendar/CalendarManager, CampaignManager, CardBenefits,
CategoriesEdit/Filter/FeaturedBanner, Condition, Customer(고객사), Display, EmptyPage,
EventStatistics, GiftIGroup, GiftItem(사은품, 전체 주석), GoogleAnalytics(중복), Island,
Newsletter(빈스텁), OzViewTest, Transfer(주석), Store(소스에 "미사용" 명시) 등. → **재현 대상 아님**
([[asis-porting-principles]]: AS-IS에서 죽어있으면 우리도 만들지 않음).

### 3. 범위밖(다른 도메인 소관)

Common(고객메인 AJAX)·Main(쇼핑몰 메인)·Calendar(고객)·Juso(주소검색 위젯) 등은 storefront(Vue3
SPA)/gift 소관. onepass/payment/simpleauth/magicline 자산 디렉터리는 코드 0(도메인 분산).

---

### 4. ★ admin 최종 갭 목록 (확정 — 구현 단계에서 이 목록대로)

| 갭 | 상태 | 성격 | 우선순위 |
|---|---|---|---|
| ~~FAQ 관리자 CRUD (G1)~~ | ~~X~~ **오탐→O** | 이미 `LocgFaqAdminController`로 구현됨(2026-09-23 정정). 잔여는 OP_FAQ 고아정리·명명debt·지자체FAQ 확인뿐 | - |
| ~~내부문의(지자체담당자↔본사, V5-B7)~~ | **구현완료(재기동 대기)** | **[2026-09-23 구현]** `InternalInquiryAdminController`(/admin/internal-inquiry) + QnaAdmin/QnaAdminAnswer 엔티티·서비스·템플릿(list/form). 지자체담당자=자기지자체 문의작성, 본사=전체조회+답변, RBAC(isLocgovScoped). 시퀀스·메뉴(OP_MENU 1903+RIGHT 5/6) 시드. **첨부파일·발송알림(SMS/메일)은 추후 라운드**. 컴파일 OK | - |
| **방문통계(접속통계, V7)** | **X** | 접속 로깅 인프라(스토어프론트 계측+서버 적재) 선결 필요 | **中** |
| 카탈로그 관리 (G2) | X | 순수재현, DB 이관·소관결정 선행 | 中 |
| 회원그룹(OP_GROUP, V3) | X | legacy 축소(쿠폰=등급+선택회원으로 대체). 실사용 확인 시 재검토 | 低 |
| 클레임메모 통합목록 뷰(V2) | 부분 | 실체 완비, memo-centric 기간검색 뷰만 | 低 |
| 공통메시지·SmsConfig·제철·특산·만족도 (G3~G7) | X | 저빈도/흡수권장 | 低 |
| 전자서명(G8)·배치로그(G9) | X | 외부연계/설계선결 | 低 |
| 전역 포인트정책 1화면 (V1) | 확인 | 지자체별 지급률이 실질 정책이라 N/A 가능성 | 低 |
| ~~FAQ(G1)·설정허브(V1)·판매자포털(V6)·클레임메모실체·로그인배너(V4)·상품문의(V5)·지자체공지RBAC(V8)~~ | **O** | 심도검증서 이미 구현/흡수 확인 — 갭 아님 | - |

> **admin audit 완료 (심도검증 V1~V8 전건 2026-09-23).** 09-03 심층감사 대비 **대부분의 gross 갭이 이미 닫힘**,
> 심도검증에서 다수가 O로 확정(설정허브·로그인배너·상품문의·지자체공지RBAC·판매자포털)되고 FAQ는 오탐 정정.
> **최종 순수재현 갭(구현 대상)**: **내부문의(中)·방문통계(中)·카탈로그(中)**, 그리고 저우선(회원그룹·클레임메모뷰·공통메시지류·전자서명·배치로그).
> **다음 단계**: 남은 X 갭 중 **내부문의(DB 준비됨·즉시가능)** 또는 카탈로그부터 구현. 방문통계는 로깅 인프라 선결.
> ★ 6개 서비스(donation·member·point·gift·order·admin) 전수 parity 대조 + admin 심도검증 완료.

---

## 6. admin 메뉴트리 parity

> 통합 전 파일: `docs/admin-menu-tree-parity.md`

## admin 메뉴 트리 parity 대조 (AS-IS OP_MENU ↔ TO-BE)

작성 2026-10-01. 근거: AS-IS live 덤프(`OP_MENU_202610010945.sql` 355행 / `OP_MENU_RIGHT_202610010946.sql` 503행, CUBRID)를
`asis_dump` 스키마에 적재해 추출. TO-BE는 `admin.op_menu`(현재 122행, 7그룹 2단).

### 0. 핵심 사실
- AS-IS·TO-BE 둘 다 상단 GNB + 좌측 LNB를 **100% OP_MENU DB로 렌더링**. 즉 "상단 11개 vs 7개"는 **데이터 차이**.
- AS-IS는 **root(0) 아래 상단 21개 중 display='Y' 11개**가 노출(나머지 10개는 SalesOn 전자상거래 잔재라 숨김). TO-BE는 임의 7그룹으로 재편돼 있음.
- **깊이 차이**: AS-IS = 3단(상단GNB → LNB 섹션그룹 → 링크). TO-BE `navTreeFor`/`admin-nav.html` = 2단. → 렌더링 코드도 3단화 필요.
- **URL 체계 불일치**: AS-IS는 전부 `/opmanager/...`, TO-BE는 `/admin/*`·`/codes`·`/community/*`·`/shop-statistics/*` 등. 철자도 다름(`code/list`↔`codes`, `community/srBbs/list`↔`community/sr-bbs`). → 덤프 그대로 재시드하면 전부 404. **구조·명칭·순서·숨김·권한은 AS-IS에서, menu_url은 TO-BE 컨트롤러로 remap**.

### 1. AS-IS 상단 11개(노출, seq순) + 숨김 10개
노출: 시스템관리(1000) · 회원관리(4000) · 기부금관리(14000) · 오프라인기부금접수(15000) · 답례품관리(16000) ·
고객센터(5000) · 특정사업 기부 관리(17000) · 커뮤니티(11000) · 통계(6000) · 기부혜택증관리(19000) · 기부현황 대시보드(20000).

숨김(display='N', SalesOn 잔재 → 숨김 그대로 유지): 디자인/전시관리(7000) · 이벤트(12000) · 주문관리(3000) ·
마켓 관리(10000) · 입점업체관리(8000) · 정산관리(9000) · UMS 캠페인(13000) · 소식지관리(18000) · 상품정보(2000) · 페이지관리(9999).

- 노출 11개 상단 아래 **가시(display=Y) 행 188 / 링크 140개**.

### 2. 매핑/갭 (가시 링크 140개 분류)
범례: ✅ TO-BE 동일화면 존재 · 🟡 유사/통합/흡수(재확인) · ❌ TO-BE 없음(갭)

#### 시스템관리 > 시스템 관리(1400)
| AS-IS | AS-IS url | TO-BE | 상태 |
|---|---|---|---|
| 팝업관리 설정 | popup/list | /popups | ✅ |
| 사용자 권한 관리 | user-group/role/list | /admin/roles | 🟡 역할+메뉴권한 통합 |
| 사용자 권한그룹 관리 | user-group/list | /admin/roles | 🟡 동일화면 통합 |
| 관리자 권한 승인관리 | manager-request/list | /admin/manager-requests | ✅ |
| ISMS관리 | isms/isms-config | /isms-config | ✅ |
| 메인 배너 관리 | user-login-banner/index | (Banner.bannerType) | 🟡 로그인배너 흡수, 전용화면 없음 |
| 공통코드 관리 | code/list | /codes | ✅ |
| 사용자로그관리 | log/user/login-log | /log/user-login | ✅ |
| 관리자로그관리 | log/login-log | /log/login | ✅ |
| 이메일 설정 | mail-config/list | /mail-config | ✅ |
| 약관관리 | config/policy/list | /policy | ✅ |
| Batch Job | batch-job/list | /batch-job | ✅ |
| 메세지 관리 | message/list | — | ❌ 공통메시지(보류) |
| 배송업체 관리 | delivery-company/list | — | ❌ 갭 |
| 메뉴관리 | menu/list | /admin/menus | ✅ |
| 설문관리 | qustnr/list | /admin/surveys | ✅ |
| 엑셀다운로드사유관리 | log/exceldownload-log | /admin/excel-download-logs | ✅ |
| 서울부과/수납·지방부과/수납 로그 (4) | log/gif-* | /log/levy | 🟡 연계로그로 통합(4→1) |
| 문자전송이력 | sms-log/list | /admin/send-sms-logs | ✅ |
| 배치 실행로그 조회 | batch-log/list | — | ❌ 배치로그(보류) |
| 포인트사용 정합성검증 | point-check/list | /reconciliation/order-point | 🟡 유사 |
| 이메일 발송 | email/list | /email | ✅ |
| 결제정보 설정 | config/payment-config | /site-config | 🟡 SiteConfigExt |
| 온라인 입금 계좌 | account-number/list | — | ❌ 갭 |
| 카드 혜택 관리 | card-benefits/list | — | ❌ 갭 |
| PG 설정 | config/pg | /site-config | 🟡 SiteConfigExt |

#### 회원관리(4000)
| 일반회원관리 | user/customer/list | /admin/members | ✅ |
| 탈퇴회원리스트 | user/secede-user/list | /admin/secede-users | ✅ |
| 휴면회원관리 | user/sleep-user/list | /admin/sleep-users | ✅ |
| 지자체관리 | user/locgov/list | /admin/locgovs | ✅ |
| 지자체담당자관리 | user/locgov-charger/list | /admin/person-in-charge | 🟡 재확인 |
| 운영관리자 | user/oper-charger/list | /admin/managers | 🟡 재확인 |
| 오프라인담당자 | user/off-charger/list | /admin/off-person-in-charge | ✅ |
| 답례품 관리자 | user/rtnpsnt/list | /seller | 🟡 제공자=판매자 |

#### 기부금관리(14000)
| 기부금모금현황 | give/give-state/list | /give-state | ✅ |
| 기부포인트현황 | give/give-point/list | /give-point | ✅ |
| 기부금 운용정보 | give/give-operation/list | /give-operation | ✅ |
| 기부금·포인트 변경 | give/give-state/reqmng-list | /give-reqmng | ✅ |
| 기부금전체현황 | give/give-state/detail_list | /give-state | 🟡 상세목록(동일컨트롤러?) |
| 기금사업 등록/관리 | give/give-notice/list | — | 🟡/❌ 기금사업소개 재확인 |

#### 오프라인기부금접수(15000)
| 기탁서 등록 | offgive/create | /offgive | 🟡 (/offgive 내 등록) |
| 기부금 접수관리 | offgive/list/ | /offgive | ✅ |

#### 답례품관리(16000)
| 카테고리관리 | categories/list | /admin/gift-categories | ✅ |
| 카테고리 그룹 관리 | categories-team-group/list | /admin/category-teams | ✅ |
| 답례품제공자 관리 | seller/list | /seller | ✅ |
| 대표배너관리 | representative-banner/list | /admin/main-banners | 🟡 재확인 |
| 답례품관리 | item/list | /admin/gift-items | ✅ |
| 답례품승인관리 | item/seller/list | /admin/gift-items | 🟡 승인필터 재확인 |
| 전체주문관리/신규주문/배송준비중/배송중/배송완료/구매확정/오프라인주문 (7) | order/* | /admin/orders | 🟡 상태필터 통합 |
| 신규주문(모바일)/발송준비중(모바일) (2) | order/*-mobile | — | ❌ 모바일 전용(보류) |
| 주문취소/반품/교환/환불 관리 (4) | order/{cancel,return,exchange,refund}/list | /admin/claims | 🟡 클레임큐 통합 |
| 정산예정/확정/마감 내역 (3) | remittance/* | /settlements(/pending) | 🟡 통합 |
| 답례품후기 관리 | item/review/list | /admin/gift-reviews | ✅ |
| 답례품 Q&A | qna-item/list | — | 🟡/❌ (gift 판매자Q&A?) |
| 이벤트 관리 | featured/list | /admin/featured | ✅ |
| 관리자 문의 | qna-admin/list | /qna-admin | ✅ |
| 제철식품관 관리 | seasonal-food/list | — | 🟡/❌ (gift측 구현, admin 없음) |
| 특산물관 관리 | speciality-item/list | — | 🟡/❌ |
| 미완료 주문/정산 목록 (2) | temp-process/* | — | ❌ 갭 |

#### 고객센터(5000)
| 지자체공지사항 | locgov-notice/list | /admin/notices | 🟡 재확인 |
| 공지사항 | notice/list | /admin/notices | ✅ |
| 자료실 | data-board/list | /admin/data-board | ✅ |
| 1:1 문의 | qna/list | /admin/shop-inquiries | 🟡 구버전 |
| Q&A | qna-open/list | — | 🟡/❌ |
| FAQ | faq/list | /community/faq-bbs | 🟡 |
| 운영관리 SR게시판 | maintenance/list | /admin/maintenance | ✅ |
| 답례품제공자 공지사항 | sellerNotice/list | /admin/seller-notices | ✅ |
| 관리자매뉴얼 | manual/manager/list | /admin/manuals | 🟡 |
| 사용자매뉴얼 | manual/list | /admin/manuals | 🟡 |

#### 특정사업 기부 관리(17000)
| 특정사업 기부 목록 | designated-donation/list | /designated-projects | ✅ |
| 특정사업 기부 등록 | designated-donation/form | /designated-projects | 🟡 |
| 지자체별통계/월별통계 (2) | designated-donation/analysis/* | — | ❌ 갭 |
| 특정사업 기부 배너관리 | designated-donation/banner/list | — | ❌ 갭 |
| 특정사업 기부 권한승인 | designated-donation/request/list | — | ❌ 갭 |

#### 커뮤니티(11000)
| SR 게시판 | community/srBbs/list | /community/sr-bbs | ✅ |
| 소통방 | community/bbs/list | /community/bbs | ✅ |
| 자료실 | community/databoard/list | /community/databoard | ✅ |
| 담당자용 FAQ | community/faqBbs/list | /community/faq-bbs | ✅ |
| 오프라인 담당자 SR 게시판 | community/offSrBbs/list | /community/off-sr-bbs | ✅ |
| (지자체FAQ 11403은 AS-IS에서 display='N' 숨김) | community/locv-faq | /community/locv-faq | ✅ 숨김유지 |

#### 통계(6000)
| 기부금 모금현황 전체/지자체별 (2) | give/statistics/{all,locgov} | /give-statistics | 🟡 |
| 매출통계 일/월/년/결제타입 (4) | shop-statistics/sales/{day,month,year,payment} | 동일 | ✅ |
| 기부금 운영현황 | give/statistics/operate | — | 🟡/❌ |
| 판매자별/상품별/카테고리별/지역별 통계 (4) | sales/{seller,item,category,area} | 동일(category→categories) | ✅/🟡 |
| 관심지자체 | statistics/locgov/like | /statistics/interest-locgov | ✅ |
| 만족도 조사 | cntnts-stsfdg/list | — | ❌ 갭 |
| 답례품 구매현황 전체/지자체별/월지자체/농협/카테고리 (5) | sales/* | 동일 | ✅ |
| 이벤트/상품이벤트 통계 (2) | event-statistics/* | — | ❌ 갭 |
| 답례품 선호도 | shop-statistics/wish/list | /shop-statistics/wish/list | ✅ |
| GA 사용자/페이지/세션/유입/캠페인 통계 (5) | google-analytics/* | — | ❌ GA(보류) |
| 접속통계 | stats/visit/index | /stats | 🟡 방문통계 갭 |
| 방문자접속경로 | stats/referer | — | ❌ 갭 |
| 보고서 일일/총괄/누계/지자체누계/지자체/연간 (6) | shop-statistics/{dashboard/day,report/*} | 동일 | ✅ |
| 외국인 기부 전체/지자체별 (2) | give/statistics/foreigner/* | — | ❌ 갭 |

#### 기부혜택증관리(19000)
| 기부혜택증 설정 관리/설정 (2) | lclgvHnrUser/.../ {list,form} | /admin/honor-users | 🟡 |
| 기부혜택증 열람현황 | .../lclgvHnrUserViewHist/list | — | ❌ 갭 |

#### 기부현황 대시보드(20000)
| 기부현황 대시보드 | bix5-access | — | ❌ BIX5 외부BI(보류) |

### 3. 갭 요약 (❌ + 보류성 🟡)
- **보류(SalesOn/외부/모바일/GA/BIX5)**: 모바일 주문 2, GA 통계 5, BIX5 대시보드, 배치로그, 공통메시지. → 숨김 처리 + 기록.
- **진짜 재현갭(고향사랑 기능인데 TO-BE 없음)**: 특정사업 통계 2·배너·권한승인(4), 외국인기부 2, 만족도조사, 이벤트통계 2, 기부혜택증 열람현황, 방문자접속경로, 접속통계(방문통계-기존식별), 배송업체·온라인입금계좌·카드혜택 관리, 기금사업소개(재확인). → 메뉴 구조엔 넣되 화면은 후속 라운드.

### 4. 반영 방식 결정 (사용자 확인 필요 → §5)
- **D1 menu_id 체계**: AS-IS 원본 id(1000/1400/1401…) 그대로 재사용 권장(향후 덤프 재대조·verbatim 유리). 현 TO-BE id는 폐기·전면 교체.
- **D2 AS-IS 노출인데 TO-BE 화면 없는 링크**: 이번 패스는 **숨김(display='N')+갭목록 기록**(죽은 링크 노출 금지), 화면 생기면 노출 복원. (AS-IS 노출상태와의 의도적·추적되는 일시 divergence)
- 공통: 숨김 상단 10개·숨김 하위는 AS-IS display 그대로 복제(링크 404 무관, 안 보임).

### 5. 다음 단계(결정 후)
1. 재시드 SQL: `admin.op_menu` 전면 교체(AS-IS 구조+TO-BE url remap) + `op_menu_right`(AS-IS 권한, ROLE_ADMIN_1~6만; 7/8/10/11은 미구현 역할이라 제외).
2. `MenuService.navTreeFor` + `fragments/admin-nav.html` 3단화(GNB→섹션→링크), `inc_header.jsp` 구조 기준.
3. 변경영향: 현 7그룹 전제 화면·`ManagerAuthAdvice`(activeTopMenuId 계산)·site-config-nav 등 확인.
4. 컴파일까지. 재기동은 사용자.
</content>
</invoke>

---

## 7. admin 갭 심층감사 part1

> 통합 전 파일: `docs/as-is-admin-gap-deep-audit-part1.md`

## AS-IS opmanager → TO-BE admin 심층 갭 감사 (Part 1, 67개 컨트롤러)

조사 대상: `saleson.shop.**` 및 `com.onlinepowers.board` 하위 67개 AS-IS 컨트롤러.
방법: 각 컨트롤러의 실제 `@RequestMapping`/메서드, 반환 뷰 이름(`ViewUtils.getView(...)`, `"view:..."` 관례)을 확인하고,
`WEB-INF/views/opmanager/i18n/<폴더>`에 대응 JSP가 실제로 존재하는지 대조했다. TO-BE는 `admin/src/main/java/com/ghlove/admin/web/*`
51개 컨트롤러의 실제 매핑을 전수 확인했고, 필요한 경우 `gift` 서비스(답례품 상품/카테고리 API)도 함께 대조했다.
DB 존재여부는 `database/ddl/schema.sql`(원본 550테이블 통짜 덤프, 아직 어느 서비스에도 배분 안 된 테이블 포함)과
`database/ddl/service-admin.sql`(admin 서비스에 실제 마이그레이션된 테이블)을 각각 grep해 구분했다.

**dead-code 판정 기준**: AS-IS 컨트롤러가 반환하는 뷰 이름에 해당하는 JSP 파일이 `WEB-INF/views/opmanager/i18n/` 전체 트리(뿐 아니라 webapp 전체)에 단 하나도 없으면,
그 화면은 AS-IS 운영 환경에서도 렌더링 시 오류가 나는 고아 코드로 판단했다("이름이 낯설다"가 아니라 "렌더링 대상 파일이 물리적으로 없다"는 코드 근거).
단, 로컬 AS-IS 소스가 실제 운영 배포본과 100% 동일하다는 보장은 없으므로(SETUP.md 삭제 이력 등), 이 판정은 "로컬 소스 기준" 임을 전제로 한다.

### 요약

67개 컨트롤러 기준(단, #3 CommonController는 성격이 다른 기능 2개가 한 클래스에 섞여있어 범위밖/진짜미구현 양쪽에 중복 집계됨 - 그래서 합계가 68):

- 이미 커버됨: 26건
- AS-IS 죽은코드: 18건
- 진짜미구현: 21건
- 범위밖: 3건

### 분류표

| # | AS-IS 컨트롤러 | 실제 기능 요약 | 분류 | 근거 / 커버하는 TO-BE 파일 | DB 존재여부 | 우선순위 |
|---|---|---|---|---|---|---|
| 1 | BoardCfgController | 게시판 종류(코드) 등록/설정 | 이미 커버됨 | TO-BE는 게시판마다 전용 컨트롤러(OperationContentController=공지, CmntyBoardController=커뮤니티4종, FaqController=FAQ, DataBoardController=자료실)를 고정 설계해 "게시판 종류를 동적으로 추가"하는 범용 CMS 개념 자체가 불필요해짐 | - | - |
| 2 | BoardController | 범용 게시판 CRUD/댓글/파일첨부 렌더링 엔진(`/board/{code}`) | 이미 커버됨 | 위와 동일 사유로 목적별 컨트롤러가 대체 | - | - |
| 3 | CommonController | 혼합: (a)고객 메인페이지 AJAX(장바구니/위시리스트/쿠폰적용/방문로그/GNB) (b)관리자 메인 대시보드 위젯(주문수·회원수·송금수·배송지연수·스토어수 카운트, 매출/건수 차트, 공지, 통합검색, 답례품검색) | 범위밖(a) + 진짜미구현(b) | (a)는 storefront Vue3 SPA 자체 API로 대체(범위밖). (b)에 해당하는 "관리자 로그인 후 첫 진입 대시보드"는 TO-BE 어디에도 없음(StatsController는 SFR 통계 대시보드로 성격이 다름) | b는 StatsService/각 서비스 원장 재사용 가능, 신규 스키마 불필요 | 中 |
| 4 | FileUploadController | 콘텐츠 에디터 공용 파일 업로드/다운로드(`/opmanager/file/**`) | 이미 커버됨 | 각 TO-BE 화면(공지/이벤트/자료실 등)이 자체 업로드 처리 로직을 갖고 있어 범용 업로더가 불필요 | - | - |
| 5 | ProgramFileDownloadController | 프로그램자료 단건 다운로드 | 이미 커버됨 | `DataBoardController#/data-board/file-download/{fileId}`가 동일 역할 | - | - |
| 6 | AccessManagerController | 관리자 IP 접근허용목록 CRUD | 이미 커버됨 | `AccessController`(`/access`) 1:1 대응 | - | - |
| 7 | AccountNumberManagerController | 계좌번호 관리 화면 | AS-IS 죽은코드 | 반환 뷰 컨벤션(`ViewUtils.view()`)이 가리키는 `/account-number/*` JSP가 AS-IS 소스 전체에 존재하지 않음(`account-number` 폴더 자체가 없음) | OP_ACCOUNT_NUMBER는 이미 service-admin.sql에 존재(다른 기능 재사용 추정) | - |
| 8 | AttendanceManagerController | 출석체크 이벤트 관리자 설정 | AS-IS 죽은코드 | `/attendance/list`, `/attendance/form` JSP가 webapp 전체에 없음 | - | - |
| 9 | AuthController | SMS/이메일/관리자SMS 인증요청 범용 유틸(`/auth/*`) | 이미 커버됨(대체) | `ManagerAuthController`의 이메일 2차인증 + 인증서 로그인이 관리자 인증 목적을 이미 대체 | - | - |
| 10 | MainBannerManagerController | 메인배너 등록/수정/노출순서 | 이미 커버됨 | `BannerApiController` + memory(`main-banner-carousel-bug`)로 실제 배너 등록·노출 확인됨 | - | - |
| 11 | BanWordController | 금칙어 관리 | AS-IS 죽은코드 | `list` 뷰가 가리키는 `ban-word` JSP 폴더가 없음(고객용 `/ban-word` 매핑도 동일 컨트롤러라 공유) | OP_BAN_WORD는 service-admin.sql에 존재하나 화면 자체가 고아 | - |
| 12 | BatchJobManagerController | 배치작업 등록/실행주기 관리 | 이미 커버됨 | `BatchJobController` 1:1 대응(`detail`, `batchForNh` 세부 기능은 없지만 NH는 `NhExportBatchController`가 별도 커버) | - | - |
| 13 | BatchlogManagerController | 배치 실행이력 로그 조회 | 진짜미구현 | 아래 상세 참고 | 신규 필요(단, 아키텍처 재검토 선행 필요) | 低 |
| 14 | BrandManagerController | 브랜드(제공업체) CRUD | 이미 커버됨 | `BrandAdminController` 1:1 대응 | - | - |
| 15 | CalendarController | 고객용 캘린더(휴무일 등) 표시 | 범위밖 | storefront/gift 담당 영역. 짝을 이루는 관리자 편집화면(#16)이 AS-IS에서도 죽은 코드라 실질적으로 유명무실한 기능 | - | - |
| 16 | CalendarManagerController | 달력(공휴일/휴무일) 관리자 등록 | AS-IS 죽은코드 | `/calendar` 관련 JSP 폴더가 없음(폴더 목록에 "calendar" 없음, "chatbot"만 존재) | - | - |
| 17 | CampaignManagerController | SMS/알림톡 캠페인 발송 + 정기캠페인(대규모) | AS-IS 죽은코드 | `view:/campaign/list` 등 12개 뷰 경로가 가리키는 `campaign` JSP 폴더가 AS-IS 소스 전체에 없음 | OP_CAMPAIGN 등은 service-admin.sql에 존재(다른 목적 또는 미래 대비로 이관됐을 가능성) | - |
| 18 | CardBenefitsManagerController | 카드사 혜택 안내 관리 | AS-IS 죽은코드 | `/card-benefits/*` JSP 폴더 없음 | OP_CARD_BENEFITS는 service-admin.sql에 존재하나 화면은 고아 | - |
| 19 | CatalogManagerController | 카탈로그(연간사업) / 지자체즐겨찾기상품 / 카드뉴스 / 콘텐츠 관리(4개 서브기능) | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`G_CATALOG_MNG`, `G_CATALOG_CARD_NEWS_MNG`, `G_CATALOG_CONTENT_MNG` 등), 서비스 DDL 이관 필요 | 中 |
| 20 | CategoriesManagerController | 답례품 카테고리 CRUD/트리/SEO | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_CATEGORY`, `OP_ITEM_CATEGORY`), 서비스 DDL 이관 필요 | 高 |
| 21 | CategoriesEditManagerController | 카테고리별 화면(상단배너/레이아웃/팝업) 편집 | AS-IS 죽은코드 | `categories-edit` JSP 폴더 없음(하위 `top-banner` 등 전부 미존재) | - | - |
| 22 | CategoriesFilterManagerController | 카테고리 속성 필터 관리 | AS-IS 죽은코드 | `categories-filter` JSP 폴더 없음 | - | - |
| 23 | CategoriesTeamGroupManagerController | 카테고리 팀/그룹(프로모션 그룹) 관리 | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_CATEGORY_TEAM`, `OP_CATEGORY_GROUP`) | 中 |
| 24 | ClaimMemoManagerController | 고객 클레임 메모(주문 관련 상담이력) | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_CLAIM_MEMO`) | 中 |
| 25 | CntntsStsfdgManagerController | 콘텐츠 만족도 조사(통계 포함) | 진짜미구현 | 아래 상세 참고 | 미확인(테이블명 패턴 불일치, 재확인 필요) | 低 |
| 26 | CodeManagerController | 공통코드 관리 | 이미 커버됨 | `CommonCodeController`(`/codes`) 1:1 대응 | - | - |
| 27 | CommentManagerController | 커뮤니티 게시판 전역 댓글 CRUD(AJAX 전용) | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql 확인 필요(커뮤니티 4테이블에 댓글 컬럼/서브테이블 존재 여부 재확인) | 中 |
| 28 | CommunityManagerController | 커뮤니티 게시판 4종(자유/SR/FAQ/오프라인SR) CRUD+댓글 | 이미 커버됨(댓글 제외) | `CmntyBoardController`(`/community/{boardType}`)가 게시글 CRUD를 1:1 대응. 단 AS-IS의 `cmnt/*`, `srBbs/cmnt/*` 등 댓글 기능은 TO-BE에 전혀 없음(#27과 동일 gap) | - | - |
| 29 | LocgFaqManagerController | 지자체 FAQ 관리 | 이미 커버됨 | `LocgFaqAdminController`(`/community/locv-faq`) - AS-IS 경로(`/opmanager/community/locv-faq`)와 슬러그까지 동일 | - | - |
| 30 | LocGovDataBoardManagerController | 지자체 자료실 관리 | 이미 커버됨 | `CmntyRpstrController`(`/community/databoard`) | - | - |
| 31 | ConditionManagerController | 카테고리별 검색조건(필터) 관리 | AS-IS 죽은코드 | `condition` JSP 폴더 없음 | - | - |
| 32 | ConfigIsmsManagerController | ISMS(정보보호관리체계) 설정 | 이미 커버됨 | `ConfigIsmsController`(`/isms-config`) 1:1 대응 | - | - |
| 33 | ConfigManagerController | 쇼핑몰 설정 허브(가입거부/전환태그/샵설정/결제설정/포인트정책/배송정책·희망일/랭킹설정/정책CRUD/PG설정/GA설정/주문임시설정 등 15개 서브화면) | 진짜미구현(부분) | `site-config`→`ShopConfigController`, `policy/*`→`PolicyController`가 이미 커버. 나머지 약 10개 서브화면은 TO-BE 어디에도 없음 | `OP_CONFIG`/`OP_CONFIG_PG`/`OP_CONFIG_GOOGLE_ANALYTICS`는 이미 service-admin.sql에 존재(라이브) - 화면/API만 만들면 됨 | 高 |
| 34 | CouponManagerController | 쿠폰 CRUD/대상상품·회원 지정/오프라인코드 | 이미 커버됨 | `CouponAdminController`(`/coupon`) | - | - |
| 35 | CouponRegularManagerController | 정기(자동발급) 쿠폰 관리 | 이미 커버됨 | `CouponAdminController`(`/coupon-regular`) | - | - |
| 36 | CouponUseManagerController | 쿠폰 사용내역/대상 미리보기 | 이미 커버됨 | `CouponAdminController`(`/coupon/{id}/usage`) | - | - |
| 37 | CustomerManagerController | 고객사(기관) 계정 관리 + 엑셀 업/다운로드 | AS-IS 죽은코드 | `ViewUtils.view()` 컨벤션이 가리키는 `customer` JSP 폴더가 없음(`user/customer` 폴더는 범위 밖 컨트롤러인 `GeneralCustomerManagerController` 소관) | - | - |
| 38 | DataboardManagerController | 고객센터 자료실 관리자 CRUD(등록/수정/삭제/파일첨부) | 진짜미구현 | 아래 상세 참고 | 신규 필요 없음 - `DataBoardController`가 이미 쓰는 테이블 재사용 가능 | 高 |
| 39 | DeliveryCompanyManagerController | 택배사 마스터 데이터 CRUD | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_DELIVERY_COMPANY`) | 中 |
| 40 | DesignatedDonationManagerController | 지정기부사업 전체 관리(목록/폼/분석/배너/공지/신청승인/부서) | 이미 커버됨 | `DesignatedProjectAdminController` - memory(`designated-donation-admin-round`)로 완료 확인 | - | - |
| 41 | DisplayManagerController | 메인 진열(카테고리 아이템)/기획전(스팟)/SNS링크/템플릿 관리 | AS-IS 죽은코드 | 반환 뷰(`/display/item/form`, `/display/spot/list` 등 10여개)가 가리키는 JSP가 webapp 전체에 하나도 없음 | - | - |
| 42 | EmailManagerController | 이메일 발송/이력/검색/첨부다운로드 | 이미 커버됨(부분) | `OpEmailController`가 목록/작성/발송/상세를 커버. 검색, 기존 초안 재수정, 첨부파일 다운로드는 없음(경미한 gap) | - | - |
| 43 | EmptyPageController | 빈 페이지(iframe 래퍼) | AS-IS 죽은코드 | `/emptypage/form` JSP 없음 | - | - |
| 44 | EventStatisticsManagerController | 이벤트 응모/조회 통계 | AS-IS 죽은코드 | `event-statistics` JSP 폴더 없음 | - | - |
| 45 | FaqManagerController | FAQ 관리자 CRUD(등록/수정/삭제) | 진짜미구현 | 아래 상세 참고 | `OP_FAQ`는 이미 service-admin.sql에 존재(라이브) - 화면/API만 만들면 됨 | 高 |
| 46 | FeaturedManagerController | 기획전(featured) CRUD | 진짜미구현 | 아래 상세 참고(AS-IS 자체에서도 배너생성/URL검색/이벤트답글관리 서브기능은 이미 주석처리되어 죽어있음) | 원본 schema.sql에만 존재(`OP_FEATURED`, `OP_FEATURED_ITEM`) | 中 |
| 47 | FeaturedBannerManagerController | 기획전배너(카테고리edit 하위 소기능) | AS-IS 죽은코드 | 상위 경로(`categories-edit`) JSP 자체가 없어 렌더 불가(#21과 동일 원인) | - | - |
| 48 | GiftIGroupManagerController | 답례품 그룹 관리 | AS-IS 죽은코드 | `gift-group` JSP 폴더 없음 | - | - |
| 49 | GiftItemManagerController | "사은품(GiftItem)" 관리(ItemManagerController의 "답례품"과는 다른 별개 엔티티) | AS-IS 죽은코드 | 클래스 본문 전체(모든 `@GetMapping`/`@PostMapping`)가 주석처리되어 실제 매핑 0개, JSP도 없음 - 가장 명백한 죽은코드 | - | - |
| 50 | GiveNoticeManagerController | 기부금 접수기관(사업자) 대상 공지 관리 | 진짜미구현 | 아래 상세 참고 | 원본 테이블명 미확인(재확인 필요) | 中 |
| 51 | GiveOperationManagerController | 기부금 운용현황 등록 | 이미 커버됨 | `GiveOperationController` 1:1 대응 | - | - |
| 52 | GivePointManagerController | 기부포인트 지급현황 조회 | 이미 커버됨 | `GivePointController` 1:1 대응 | - | - |
| 53 | GiveStateManagerController | 기부현황 조회 | 이미 커버됨 | `GiveStateController` 1:1 대응 | - | - |
| 54 | GiveStatisticsController(AS-IS) | 기부통계(8개 이상 화면 통합) | 이미 커버됨 | TO-BE `GiveStatisticsController` - 경로(`/opmanager/give/statistics` ↔ `/give-statistics`)까지 사실상 동일, memory(`give-statistics-scope-and-levy-assumption`)로 통합 확인 | - | - |
| 55 | GoogleAnalyticsManagerController | GA 추적스크립트 설정 | AS-IS 죽은코드 | `/google-analytics/*` JSP 없음(`ConfigManagerController` 내 중복 엔드포인트도 동일하게 화면 없음) | - | - |
| 56 | GroupManagerController | 카테고리 그룹(구버전) 관리 | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_GROUP`) | 中 |
| 57 | GroupBannerManagerController | 그룹배너 관리(#56과 연동) | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_CATEGORY_GROUP_BANNER`) | 中 |
| 58 | InquiryManagerController | 1:1 상담 문의(구버전, `OP_SHOP_INQUIRY`) | 진짜미구현 | 아래 상세 참고 | 원본 schema.sql에만 존재(`OP_SHOP_INQUIRY`) | 中 |
| 59 | IslandManagerController | 도서(섬) 배송지역 관리 | AS-IS 죽은코드 | `island` JSP 폴더 없음 | - | - |
| 60 | ItemManagerController | 답례품 상품 통합관리(목록/생성/수정/승인/엑셀대량등록·다운로드/카테고리일괄배정/색상옵션/판매정보수정 등 대규모) | 진짜미구현(부분 커버) | 아래 상세 참고 | 원본 schema.sql에만 존재하는 세부 테이블 다수, gift 서비스 기본 아이템 테이블은 이미 존재 | 高 |
| 61 | JusoController | 주소검색 팝업(다음/행안부 API 연동 유틸) | 이미 커버됨(추정) | 신규 화면들은 클라이언트사이드 주소검색 위젯을 직접 쓰는 것으로 보임(각 폼에서 개별 확인은 못함, 확신도 낮음) | - | - |
| 62 | LclgvHnrUserManagerController | 지자체 명예직원 등록/관리 + 열람이력(엑셀) | 진짜미구현 | 아래 상세 참고 | 메인 테이블명 미확인(서브테이블 `G_LCLGV_HNR_USER_RWRD_IMG_EXPLN`만 원본 덤프에서 발견) | 中 |
| 63 | LogManagerController | 관리자로그인로그/회원로그인로그/엑셀다운로드로그/연계(relay)로그/기부금영수증 국세청연계로그(4종) | 이미 커버됨(부분) | `AuditLogController`가 로그인로그·액션로그 4화면을 커버(memory: `system-infra-log-round`). 엑셀다운로드로그, 기부금영수증 국세청연계로그(표준양식·서울시 부가/승인 4종)는 미커버 | 진짜미구현 부분은 신규 감사로그 테이블 필요 | 中 |
| 64 | UserLoginBannerManagerController | 로그인화면(웹/모바일) 전용 배너 | 진짜미구현 | 아래 상세 참고 | `OperationContentController`의 범용 배너 테이블 재사용 가능 여부 재확인 필요 | 低 |
| 65 | MagicLineController | 전자서명 캡처(매직라인 연계, 오프라인가입/본인인증 서명패드) | 진짜미구현 | 아래 상세 참고(offgive 어디서도 참조하지 않음, mock 미처리 상태) | 없음(외부연계 mock 게이트로 대체 가능) | 低 |
| 66 | MailConfigManagerController | 메일 발송 템플릿 관리 | 이미 커버됨 | `MailConfigController`(`/mail-config`) 1:1 대응 | - | - |
| 67 | MainController | 쇼핑몰 메인페이지(고객용) 전체 | 범위밖 | storefront(gift)/Vue3 SPA 담당 영역. 단, 내장된 `/opmanager/popup/insertCall` 소형 로깅 엔드포인트 1개(팝업 노출 카운트)는 관리자 기능이나 미커버 - 경미해 별도 gap으로는 기재하지 않음 | - | - |

### 진짜미구현 상세 (다음 라운드 구현 지시서)

#### 3(b). 관리자 메인 대시보드(홈 화면)
AS-IS `CommonController`의 `opmanager/main-info`, `main-boardInfo`, `main-amountChart`, `main-countChart`, `main-chartInfo`, `main-search`, `call-search`,
`gift-search`, `gift-amount`, `get-user`, `main-notification-agree-member`, `order-count`, `user-count`, `remittance-count`, `shipping-delay-count`, `store-count`가
관리자 로그인 직후 첫 화면(대시보드)을 구성하는 AJAX 위젯 세트다. 구성:
- 상단 카운트 위젯: 금일 주문수, 회원가입수, 송금대기수, 배송지연수, 입점몰수
- 매출/건수 차트(일별/월별)
- 최근 공지사항 목록
- 통합검색(주문번호/회원/전화번호로 조회) 및 답례품 검색/금액 조회
- 회원 알림동의 현황

TO-BE에는 로그인 후 랜딩되는 대시보드 화면 자체가 없다(로그인하면 특정 메뉴로 바로 이동하는 구조로 추정). `StatsController`(`/stats`)가 SFR 통계 대시보드로
유사하지만 목적(기부/주문/포인트 통계)이 달라 대체 불가.
**필요 작업**: `AdminHomeController` 신설, 상기 6종 위젯 API + 화면. DB는 각 서비스 원장(order/member/point 등)을 admin이 REST로 재조회하는 기존 패턴 재사용 가능, 신규 스키마 불필요.

#### 13. BatchlogManagerController - 배치 실행이력 로그
AS-IS는 `OP_BATCH_JOB`에 등록된 배치가 실제 실행될 때마다 이력을 남기고 `/opmanager/batch-log`에서 조회했다. 그러나 TO-BE `BatchJobController`의 코드 주석에
명시된 대로 "실제 동적 스케줄러 엔진은 없다(각 서비스의 `@Scheduled`로 이미 개별 구현된 것과 별개)" - 즉 `OP_BATCH_JOB` 등록은 메타데이터 표시용일 뿐 실행 엔진이 없어
원래 의미의 "배치 실행이력"을 낼 소스 자체가 없다. **구현 전에 먼저 결정할 사항**: (1) 각 서비스의 실제 `@Scheduled` 메서드에 실행이력 기록을 추가해 배치로그를 재구성할지,
(2) 이 기능 자체를 스코프 아웃할지. 후순위로 유지.

#### 19. CatalogManagerController - 카탈로그(연간사업/즐겨찾기상품/카드뉴스/콘텐츠) 관리
4개 서브탭:
- `catalogMng`: 연도별 카탈로그(연간 캠페인 묶음) 등록/수정, 카탈로그명·연도·기간 관리
- `locgovFavItem`: 지자체별 "즐겨찾기 상품"(지자체 담당자가 추천 답례품을 지정) - 연도/지자체 조합 키로 상품 리스트 등록
- `cardNews`: 카드뉴스(이미지 슬라이드형 콘텐츠) 등록/수정, 라벨 노출 토글
- `content`: 일반 콘텐츠(서브타입 존재) 등록/수정

**필요 DB**: `G_CATALOG_MNG`, `G_CATALOG_CARD_NEWS_MNG`, `G_CATALOG_CARD_NEWS_IMG_DESC`, `G_CATALOG_CONTENT_MNG`, `G_CATALOG_CONTENT_IMG_DESC`가 `database/ddl/schema.sql`
원본 덤프에 이미 정의되어 있으나 `service-gift.sql`/`service-admin.sql` 어디에도 마이그레이션되지 않았다 - 어느 서비스 소관인지(gift vs admin) 먼저 결정 후 DDL 이관 필요.

#### 20. CategoriesManagerController - 답례품 카테고리 관리
답례품 상품 분류체계(카테고리) CRUD: 목록/생성/수정/삭제, 트리 구조 조회(`tree-list`), 상위-하위 이동(`move`), 노출순서 변경, SEO 메타 편집, 카테고리코드 중복확인.
gift 서비스의 `GiftApiController#/api/categories`는 **읽기 전용**(스토어프론트 카테고리 네비게이션용)이라 이 카테고리 체계를 실제로 만들고 편집하는 화면이 어디에도 없다 -
즉 지금 스토어프론트에 뜨는 카테고리 목록은 초기 시드 데이터 이후 한 번도 관리자가 수정할 수 없는 상태다.
**필요 DB**: `OP_CATEGORY`, `OP_ITEM_CATEGORY`가 원본 schema.sql에만 존재 - gift 서비스 스키마로 이관(현재 gift가 참조하는 카테고리 테이블과의 관계 재정리 필요) 후
admin 서비스에서 REST로 CRUD하거나 gift 서비스에 직접 관리자 API를 추가하는 두 방식 중 택1.

#### 23. CategoriesTeamGroupManagerController - 카테고리 팀/그룹 관리
"팀"(대분류 상위 그룹)과 "그룹"(팀 하위, 프로모션성 묶음) 2계층 CRUD + 노출순서 변경 + 그룹→카테고리 전환 기능. `#56/#57`(GroupManagerController/GroupBannerManagerController)과
개념이 유사하나 별도 테이블(`OP_CATEGORY_TEAM`, `OP_CATEGORY_TEAM_ITEM`, `OP_CATEGORY_GROUP`, `OP_CATEGORY_GROUP_BANNER`)을 쓰는 별개 기능이다. 카테고리 관리(#20)와 함께
"카테고리/진열 관리" 화면군으로 묶어서 구현하는 것을 권장.

#### 24. ClaimMemoManagerController - 고객 클레임 메모
주문/배송 관련 고객 클레임(불만/문의) 처리 이력을 자유 텍스트로 남기는 간단한 메모 게시판(`list`/`list` POST 검색만 존재, 단일 화면). order 서비스의 클레임/취소반품 처리와
연동되는 상담이력 관리 기능으로 추정. **필요 DB**: `OP_CLAIM_MEMO`(원본 schema.sql에만 존재) - order 서비스 주문ID를 FK로 참조하는 구조라 admin이 order 서비스 REST로
주문 정보를 조회하며 메모만 admin 자체 테이블에 쌓는 방식을 권장.

#### 25. CntntsStsfdgManagerController - 콘텐츠 만족도 조사
콘텐츠(공지/이벤트 등) 하단에 "이 페이지가 도움이 되었나요?" 형태의 만족도 조사를 삽입하고, 관리자가 `list`(응답목록)/`detail`(상세)/`statistics`(집계)로 확인하는 기능.
JSP는 살아있으나(`cntnts-stsfdg/form.jsp`, `list.jsp`) 실제 사용 빈도는 낮을 것으로 추정 - 낮은 우선순위.

#### 27. CommentManagerController + 28(커뮤니티 댓글 하위기능)
AS-IS 커뮤니티 게시판(자유게시판/SR게시판/FAQ게시판/오프라인SR게시판) 각각에 댓글 CRUD(`cmnt/add`, `cmnt/update`, `cmnt/delete`, 첨부파일 업로드/다운로드)가 있고,
별도로 `CommentManagerController`가 전역 댓글 카운트/목록/CRUD API를 제공한다. TO-BE `CmntyBoardController`는 게시글 CRUD만 구현했고 댓글 관련 엔드포인트가 전혀 없다.
**필요 작업**: `CmntyBoard` 계열 엔티티에 댓글 서브엔티티(또는 별도 `CmntyComment` 테이블) 추가 + `CmntyBoardController`에 댓글 CRUD 서브리소스 확장.

#### 33. ConfigManagerController - 쇼핑몰 설정 허브(잔여 ~10개 서브화면)
`site-config`(→ShopConfigController)와 `policy/*`(→PolicyController)를 제외한 나머지:
- `deny/edit`: 회원가입 거부 IP/이메일도메인 등 블랙리스트
- `conversion-tag`: 전환추적 스크립트(광고 픽셀 등) 삽입
- `shop-config`: 쇼핑몰 기본정보(별도 site-config와 필드가 다름 - 확인 필요)
- `payment-config`: 결제수단 활성화 설정
- `point`: 포인트 적립/사용 정책(적립률, 최소사용액 등 - domain-point-rate-policy memory와 연관 가능성 있음, 지자체별이 아닌 전역 기본값일 가능성)
- `delivery` / `delivery-hope`: 배송 기본정책 / 희망배송일 등록·수정·삭제
- `ranking-config`: 인기상품 랭킹 산정 기준 설정
- `pg`: PG사(결제대행사) 연동 설정
- `google-analytics`: GA 추적ID 설정(#55와 중복 엔드포인트, #55는 죽은코드이므로 이쪽이 유일한 생존 경로일 수 있음 - 재확인 필요)
- `order-temp-config`: 주문 임시저장 관련 설정

**DB는 이미 준비됨**: `OP_CONFIG`, `OP_CONFIG_PG`, `OP_CONFIG_GOOGLE_ANALYTICS`가 `service-admin.sql`에 이미 존재(라이브 스키마) - 화면과 컨트롤러 로직만 추가하면 된다.
운영에 필수적인 설정이 다수 섞여 있어 우선순위 高로 책정.

#### 38. DataboardManagerController - 고객센터 자료실 관리자 CRUD
TO-BE `DataBoardController`는 코드 주석에 명시된 대로 "로그인 불필요, 공개 화면"인 **조회 전용**이다(`/data-board`, `/data-board/{id}`, 파일다운로드만 존재).
AS-IS `DataboardManagerController`(`/opmanager/data-board`)는 이 자료실 게시물을 실제로 등록/수정/삭제하는 관리자 화면이며 JSP도 살아있다(`data-board/list.jsp`, `form.jsp`).
즉 지금 TO-BE는 "자료실을 보여줄 수는 있지만 아무도 새 자료를 올릴 수 없는" 상태다. **필요 작업**: `DataBoardController`가 참조하는 것과 동일한 `DataBoard`/`DataBoardFile`
엔티티를 대상으로 관리자 CRUD 컨트롤러 추가(list 검색/create/edit/delete/파일첨부, AS-IS는 검색조건 `search-date`, 이미지 삭제 `delete-item-image` 포함). 신규 스키마 불필요, 우선순위 高.

#### 39. DeliveryCompanyManagerController - 택배사 마스터 관리
택배사 코드/이름/사이트URL 등록·수정·삭제(목록 조회는 POST 검색 포함). `DeliveryTrackingController`(TO-BE)는 이미 등록된 택배사 코드로 배송조회를 "사용"하는 화면이지
택배사 코드 자체를 등록/관리하는 화면이 아니다(`commonCodeService.labelsOf("DELIVERY_CARRIER")`로 공통코드를 참조). 공통코드(`CommonCodeController`, `/codes`)로
`DELIVERY_CARRIER` 타입 코드를 관리하면 최소 기능은 대체 가능하나, AS-IS는 사이트URL 등 확장 필드를 갖고 있어 완전 대체는 아니다.
**권장**: 별도 화면을 새로 만들기보다 `CommonCodeController`의 코드 확장 필드로 흡수하는 방안을 우선 검토.

#### 45. FaqManagerController - FAQ 관리자 CRUD
TO-BE `FaqController`(Thymeleaf)와 `FaqApiController`(JSON)는 둘 다 클래스 주석에 "로그인 불필요, 공개 화면"이라고 명시된 스토어프론트 전용 컨트롤러다.
관리자가 FAQ를 등록/수정/삭제하는 화면이 어디에도 없다 - 즉 현재 FAQ 데이터는 최초 시드 이후 절대 바뀔 수 없다.
AS-IS `FaqManagerController`(`/opmanager/faq`)는 목록(검색조건 포함)/생성/수정/삭제를 제공한다.
**필요 DB**: `OP_FAQ`가 이미 `service-admin.sql`에 존재(라이브, `FaqService`가 이미 사용 중) - `FaqAdminController` 신설 + CRUD 로직만 추가하면 즉시 동작 가능. 우선순위 高(구현 난이도 낮고 가치 확실).

#### 46. FeaturedManagerController - 기획전(featured) 관리
상품을 묶어 "기획전" 형태로 노출하는 목록/생성/수정/삭제(+이미지 삭제) 기능. AS-IS 소스 내에서도 배너생성(`banner-create`), URL검색, 이벤트답글관리(`manage-event-reply`)
서브기능은 이미 주석처리되어 죽어있어 실제 살아있는 범위는 CRUD 핵심 4개뿐이다. `DisplayManagerController`의 "spot"(특가/기획전)과 개념이 겹치나 `#41`은 완전 죽은코드이므로
`FeaturedManagerController`가 사실상 유일한 생존 "기획전" 기능이다. **필요 DB**: `OP_FEATURED`, `OP_FEATURED_ITEM`(원본 schema.sql에만 존재).

#### 50. GiveNoticeManagerController - 기부접수기관 대상 공지 관리
기부금 접수기관(사업자/판매자)에게 노출되는 공지사항을 관리자가 등록/수정/삭제(+검색조건, 사업자별 삭제)하는 화면. `give-operation`/`give-point`/`give-state`/`give-statistics`
4종 TO-BE 컨트롤러 중 공지 기능을 가진 것은 없다. JSP는 살아있다(`give/give-notice/list.jsp`, `form.jsp`, `edit.jsp`). **선결 조사사항**: 원본 schema.sql에서
정확한 테이블명을 확인하지 못했다(패턴 매칭 실패) - 구현 착수 전 AS-IS MyBatis 매퍼(give-mapper.xml류)에서 실제 테이블명을 재조사 필요.

#### 56+57. GroupManagerController / GroupBannerManagerController - 카테고리그룹 + 그룹배너
`GroupManagerController`는 카테고리 그룹(구버전, `OP_GROUP`) CRUD, `GroupBannerManagerController`는 그룹별 배너 등록(`OP_CATEGORY_GROUP_BANNER`)이다.
`#23`(CategoriesTeamGroupManagerController)의 "그룹" 개념과 유사해 보이나 별도 테이블을 쓰는 병행 기능으로 보인다 - 구현 전 두 기능의 실제 차이(신버전/구버전 여부,
AS-IS 내 실사용 화면 대조)를 좀 더 조사해 통합 여부를 결정하는 것을 권장. 두 화면 모두 JSP는 살아있다.

#### 58. InquiryManagerController - 1:1 상담 문의(구버전)
`OP_SHOP_INQUIRY` 테이블 기반의 목록/상세/답변/첨부파일다운로드. TO-BE `QnaAdminController`는 클래스 주석에 명시된 대로 `OP_QNA`/`OP_QNA_ANSWER`를 쓰는 **별개 시스템**이라
이 기능을 커버하지 못한다. AS-IS에 `InquiryManagerController`(구, `OP_SHOP_INQUIRY`)와 `QnaManagerController`(신, `OP_QNA` - 이번 조사대상 67개에는 미포함, 이미 이전 라운드에서
QnaAdminController로 이전 완료된 것으로 추정)가 공존하는 것으로 보아, AS-IS 자체가 신/구 시스템 과도기다. **결정 필요**: 구버전 데이터가 실제 운영에 남아있다면 조회 전용
화면만이라도 이식하는 것을 권장(신규 문의 접수는 이미 QNA로 통합됐을 가능성이 높음).

#### 62. LclgvHnrUserManagerController - 지자체 명예직원 관리
지자체 "명예사용자/명예직원"(대표 답례품 홍보 등을 위촉받은 사람으로 추정) 등록/수정/삭제 + 열람이력 조회(엑셀다운로드 포함) 2탭 구성. JSP가 풍부하게 존재해 실사용
가능성이 높다. **선결 조사사항**: 원본 schema.sql에서 메인 테이블명을 찾지 못했다(서브테이블 `G_LCLGV_HNR_USER_RWRD_IMG_EXPLN`만 확인) - AS-IS 매퍼 XML에서
정확한 테이블명 재조사 필요.

#### 63(부분). LogManagerController - 엑셀다운로드로그 + 기부금영수증 국세청연계로그
`AuditLogController`가 커버하지 못하는 두 그룹:
1. **엑셀다운로드 로그**(`exceldownload-log`, `exceldownload-reason`, 팝업 상세/이력): 관리자가 회원/주문/통계 등에서 엑셀 다운로드를 실행할 때마다 사유를 입력하고
   그 이력을 조회하는 감사로그. `admin-pii-display-no-masking`/`as-is-feature-audit-round` memory에서 이미 "admin에 엑셀다운로드 0건" 패턴이 지적된 바 있는데,
   이 로그 화면 자체도 미구현 상태임이 이번에 재확인됨.
2. **기부금영수증 국세청연계 로그**(`gif-stnd-buga`/`gif-stnd-sunap`/`gif-seoul-buga`/`gif-seoul-sunap`): 표준양식/서울시 특별양식 기부금영수증의 국세청 "부가"(발급)/"승인"
   전송 이력 조회 4종. 국세청 연계 자체가 mock 처리 대상이더라도(외부연계 아키텍처 원칙상), 이력을 조회하는 관리 화면은 필요.

#### 64. UserLoginBannerManagerController - 로그인화면 전용 배너
AS-IS는 웹/모바일 로그인 페이지에 별도 배너 이미지를 노출하는 기능(`loginWeb`, `loginMobile` POST, 이미지 삭제)을 갖고 있다. `OperationContentController`의 범용
배너 CRUD(`/banners`)가 이미 있으나, "로그인 페이지"라는 특정 노출 위치/타입을 지원하는지는 코드상 확인하지 못했다 - 지원하지 않는다면 배너 타입 코드 하나 추가하는
수준의 경미한 작업이므로 낮은 우선순위.

#### 65. MagicLineController - 전자서명 캡처(매직라인 연계)
오프라인 가입/본인인증 시 태블릿 서명패드로 서명을 캡처해 저장하는 기능(`signedFormR`, `vidClientIDNR`). offgive(오프라인 기부접수) 라운드에서 이 컨트롤러를 참조한
흔적이 없어(grep 0건), `external-integrations-architecture` memory의 "enabled=false mock-gated" 패턴이 적용되지 않은 채 그냥 빠진 것으로 보인다.
전자서명 자체가 법적으로 필수가 아니라면(단순 UX 요소), mock 서명패드 UI만 추가하는 낮은 우선순위 작업으로 충분해 보인다.

#### 60. ItemManagerController - 답례품 상품 통합관리 (부분 커버, 잔여 대규모)
gift 서비스 `GiftController`에 이미 존재하는 것:
- `GET /admin`: 승인대기 상품 목록
- `POST /gifts/{itemId}/approve`, `/reject`: 셀러가 등록한 상품 승인/반려
- `GET /admin/representative-items`, `POST .../representative/register`, `.../representative/delete`: 대표상품 관리(이미 완료 - view-reimplementation-round memory)

AS-IS `ItemManagerController`(`/opmanager/item`)가 갖고 있지만 gift 서비스에 없는 것(관리자가 **직접** 수행하는 대량/전문 관리 기능):
- 관리자 직접 상품 생성/수정(`create`, `edit/{itemUserCode}`) - 현재는 셀러 self-service(`/register`, `/gifts/{id}/edit`)만 존재, 관리자가 셀러를 대신해 등록/수정 불가
- 카테고리 일괄 배정(`add-items-to-category`), 상품 복사(`copy/{itemId}`)
- 엑셀 대량등록/다운로드(`upload-excel`, `download-excel`, `upload-csv`), CSV 상태 조회
- 색상/옵션 관리(`color`, `getOptionList`, `getComboOptionList`)
- 노출/라벨 일괄 변경(`update-display`, `update-label`), 순서변경, 일괄삭제
- 판매정보 일괄수정(`sale-edit/*`), 재입고알림 메시지 발송(`restock-notice/message`)
- 상품평(리뷰) 관리자 대응(`review/*` - 별도 `review` 서브패키지, gift에는 리뷰 자체는 있으나 관리자용 승인/추천/엑셀다운로드 화면 없음)

우선순위 高(운영자가 매일 쓰는 핵심 업무 화면). 다만 범위가 매우 크므로 다음 라운드에서 "1) 관리자 직접 CRUD+카테고리배정" → "2) 엑셀 대량처리" → "3) 리뷰/재입고알림"
순으로 단계적 구현을 권장.

---

## 8. admin 갭 심층감사 part2 (배치D)

> 통합 전 파일: `docs/as-is-admin-gap-deep-audit-part2.md`

## AS-IS opmanager → TO-BE admin 심층 갭 감사 (Part 2, 66개 컨트롤러)

Part1(67개)과 동일 방법론으로 나머지 66개 컨트롤러를 4개 배치(A:콘텐츠/CMS 16개, B:커뮤니티·통계·QnA 20개, C:주문·배송·판매자 19개, D:회원·지자체관리 12개)로 나눠 조사.

### 배치 A: 콘텐츠/CMS (16개)

핵심 발견: MenuManagerController는 1차 감사에서 "커버됨"이었으나 재검증 결과 메뉴 CRUD UI가 전혀 없어 SQL 마이그레이션으로만 메뉴를 바꿀 수 있는 상태로 판정 정정(高 우선순위). NewsletterManagerController는 AS-IS 코드 자체가 서비스/도메인 연결이 전혀 없는 빈 스텁으로 죽은코드 확정. LocgovNoticeManagerController는 화면은 있지만 지자체담당자 RBAC 스코핑이 빠진 실제 권한 버그.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| A1 | MainDisplayItemManagerController | 진짜미구현 | 있음(gift: OP_MAIN_DISPLAY_ITEM) | 中 |
| A2 | MaintenanceController | 진짜미구현 | 있음(admin: G_SR_MAINTENANCE) | 中 |
| A3 | ManualManagerController | 진짜미구현 | 없음(설계 필요) | 中 |
| A4 | MenuManagerController | 진짜미구현(판정정정) | 있음(admin: OP_MENU) | **高** |
| A5 | MessageManagerController | 진짜미구현(아키텍처 불일치 주의) | 있음(admin: OP_COMMON_MESSAGE) | 低 |
| A6 | MobileCategoriesEditManagerController | 진짜미구현 | 있음(gift: OP_CATEGORY_EDIT) | 中 |
| A7 | NewsletterManagerController | AS-IS 죽은코드 | - | - |
| A8 | LocgovNoticeManagerController | 이미 커버됨(RBAC 스코핑 gap) | - | 中(버그수정) |
| A9 | NoticeManagerController | 이미 커버됨 | - | - |
| A10 | SysNoticeSellerController | 진짜미구현 | 있음(admin: OP_SYS_NOTICE_SELLER) | 中 |
| A11 | OpmanagerController | 이미 커버됨(비밀번호정책/계정잠금 gap) | - | - |
| A12 | PopupManagerController | 이미 커버됨(CRUD 완성도 gap 큼: 수정/삭제/이미지업로드 없음) | - | 中(완성도 보완) |
| A13 | RepresentativeBannerManagerController | 진짜미구현 | 있음(admin: G_REPRST_BANNER) | 低 |
| A14 | SearchManagerController | 진짜미구현 | 있음(admin: OP_SEARCH) | 中 |
| A15 | SeoManagerController | 진짜미구현 | 있음(admin: OP_SEO) | 低(storefront 연동 선행 필요) |
| A16 | StyleBookManagerController | 진짜미구현 | 있음(gift: OP_STYLE_BOOK) | 低 |

### 배치 B: 커뮤니티/통계/QnA (20개)

핵심 발견: SendMailLog/SendSmsLog/UMS/알림톡 전부 완전 미구현(1차 감사의 "sms-log 커버됨(추정)" 정정). QnaAdminManagerController는 이름이 QnaAdminController(TO-BE)와 겹쳐 보이지만 실제로는 전혀 다른 도메인(지자체담당자↔본사 내부문의).

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| B1 | OzViewTestController | AS-IS 죽은코드 | - | - |
| B2 | PointManagerController | 진짜미구현(부분) | 있음(point) | 中 |
| B3 | PointCheckManagerController | 이미 커버됨(ReconciliationController) | - | - |
| B4 | QnaItemManagerController | 진짜미구현(부분: 관리자 감독+엑셀만) | - | 低 |
| B5 | QnaManagerController | 이미 커버됨 | - | - |
| B6 | QnaOpenManagerController | 이미 커버됨 | - | - |
| B7 | QnaAdminManagerController(내부문의) | 진짜미구현 | 있음(admin: QNA_ADMIN) | 低 |
| B8 | QustnrManagerController | 진짜미구현 | 없음(설계 필요) | 中 |
| B9 | RankingManagerController | 진짜미구현 | 있음(gift: OP_RANKING) | 中(공개페이지도 없어 함께 필요) |
| B10 | ReportController | 진짜미구현(텍스트리포트 생성) | 기존 대시보드 데이터 재사용 | 低 |
| B11 | StatisticsLocgovManagerController | 진짜미구현 | 있음(donation: G_INTRST_LOCGOV) | 中 |
| B12 | ShopStatisticsManagerController | 이미 커버됨 | - | - |
| B13 | StatsManagerController(방문통계) | 진짜미구현 | 없음(로깅 인프라부터 필요) | 中 |
| B14 | SendMailLogManagerController | 진짜미구현 | 있음(admin: OP_SEND_MAIL_LOG) | 中 |
| B15 | SendSmsLogManagerController | 진짜미구현 | 있음(admin: OP_SEND_SMS_LOG) | 中 |
| B16 | SmsController(인증문자로그) | 진짜미구현 | 있음(admin: TIF_IPS_*) | 低(mock연계라 실효 낮음) |
| B17 | SmsConfigManagerController | 진짜미구현(UMS로 흡수 권장) | 미확인 | 低 |
| B18 | UmsAlimTalkManagerController | 진짜미구현 | 있음(admin: OP_ALIM_TALK_*) | 低 |
| B19 | UmsManagerController | 진짜미구현(통합알림 상위개념) | 있음(admin: OP_UMS_*) | 中 |
| B20 | TransferController | AS-IS 죽은코드(어노테이션 전부 주석처리) | - | - |

### 배치 C: 주문/배송/판매자 (19개)

핵심 발견: **OrderManagerController(주문관리 콘솔) 완전 부재 확정, 우선순위 최고**. order 서비스는 관리자 인증 모델 자체가 없음(코드 주석 "아직 이 MSA에 운영자 로그인 모델이 없다"). 클레임 승인큐 무인증도 이 gap의 일부.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| C1 | OffgiveManagerController | 이미 커버됨(엑셀다운로드만 미커버) | - | - |
| C2 | **OrderManagerController** | **진짜미구현** | 있음(OD_ORDER/OD_CLAIM, 확장 필요) | **高(최우선)** |
| C3 | OrderAgencyController | 진짜미구현(C2 파생) | - | 中 |
| C4 | PayInfoManagerController | 진짜미구현 | 없음(PG모델 재설계 필요) | 中 |
| C5 | RemittanceManagerController | 이미 커버됨(SettlementController로 단순화) | - | - |
| C6 | SeasonalFoodManagerController | 진짜미구현 | 없음 | 低 |
| C7 | SellerController(판매자 셀프포털) | 진짜미구현(의도적 스코프컷, 재검토 필요) | - | 中 |
| C8 | SellerISysNoticeController | 진짜미구현(C7 종속) | - | 低 |
| C9 | SellerManagerController | 이미 커버됨(SellerAdminController, 부가기능만 미커버) | - | - |
| C10 | SellerShipmentController | 진짜미구현 | 없음 | 低 |
| C11 | SellerconfirmManagerController | 진짜미구현(C2/C5 서브셋) | - | 中 |
| C12 | ShipmentManagerController | 진짜미구현(C10 미러) | 없음 | 低 |
| C13 | ShipmentReturnController | 진짜미구현 | 없음 | 低 |
| C14 | ShipmentReturnManagerController | 진짜미구현 | 없음 | 低 |
| C15 | SpecialityItemManagerController | 진짜미구현 | 없음 | 低 |
| C16 | StoreManagerController | **AS-IS 죽은코드**(소스에 "사용 안하는 테이블" 명시) | - | - |
| C17 | StoreInquiryManagerController | 진짜미구현 | 있음(STORE_INQUIRY) | 低 |
| C18 | TempProcessManagerController | 진짜미구현(1차감사 오분류 정정, order/remittance 소관) | 없음 | 中 |
| C19 | WelfareCenterManagerController | 진짜미구현 | 있음(admin) | 中 |

#### C2 OrderManagerController 구현 지시서 요약
- 관리자 전체 주문 목록/검색(지자체 스코프)+상태별 처리탭(일괄 상태변경)+상세화면(관리자메모)+클레임 처리큐+엑셀 다운로드/업로드
- 선행 필요: admin↔order 관리자 인증 연동 설계(HeaderAuthAdvice 패턴 재사용), 지자체 스코핑(Order.locgovCode)
- DB 확장: OD_ORDER.ADMIN_MEMO, OD_CLAIM_MEMO(신규), OD_EXCEL_DOWNLOAD_LOG(신규), Claim에 반송비 필드
- 이 gap의 파생: C3(주문대행조회), C11(정산확인큐), C18(보류주문처리)도 함께 해결 가능

### 배치 D: 회원/지자체관리 (12개)

핵심 발견: **지자체 마스터관리, 회원관리 콘솔 전체가 admin에 없다는 기존 gap이 컨트롤러 단위로 확정**. `LocgovSealAdminController`(donation, 무인증 임시화면)는 정식 지자체관리 화면으로 흡수 후 폐기 대상.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| D1 | **LocgovManagerController**(지자체 마스터관리) | **진짜미구현** | 있음(donation: G_LOCGOV, 미매핑 컬럼 다수) | **高** |
| D2 | **UserManagerController**(매니저계정+회원360도뷰) | **진짜미구현** | 있음(OP_USER 등) | **高** |
| D3 | **GeneralCustomerManagerController**(일반회원 검색/상세) | **진짜미구현** | 있음(OP_USER/OP_USER_DETAIL) | **高** |
| D4 | **SecedeUserManagerController**(탈퇴회원 조회) | **진짜미구현** | 있음(추정, 재확인) | **高** |
| D5 | **SleepUserManagerController**(휴면회원 조회/해제) | **진짜미구현** | 있음(OP_USER) | **高** |
| D6 | LocgovPersonInChargeManagerController | 진짜미구현 | 있음(OP_MANAGER) | 中 |
| D7 | ManagerRequestController | 이미 커버됨 | - | - |
| D8 | OffPersonInChargeManagerController | 진짜미구현(신규 권한티어 ROLE_ADMIN_7/8 필요) | 있음(OP_MANAGER 확장컬럼) | 中 |
| D9 | OperPersonInChargeManagerController | 진짜미구현(D6과 통합 권장) | - | 中 |
| D10 | UserGroupController(역할·메뉴권한 CRUD) | 진짜미구현 | 있음(OP_ROLE/OP_MENU_RIGHT) | 中 |
| D11 | UserLevelManagerController(회원등급) | 진짜미구현 | 있음(OP_USER_LEVEL, order 쿠폰과 이미 연결된 스키마) | 中 |
| D12 | WelfareCenterManagerController | (C19과 중복 배정, C19 참고) | - | - |

#### D1/D2~D5 구현 지시서 요약
- **D1 지자체관리**: donation에 `LocgovAdminApiController`(CRUD+포인트지급률+부서이력) 신설, `Locgov.java` 미매핑 컬럼 전부 추가, admin에 `LocgovAdminController`(RBAC: SYS=전체, LOC=자기지자체만) + `LocgovAdminClient`. 기존 `LocgovSealAdminController`(무인증)는 흡수 후 폐기.
- **D2~D5 회원관리**: member에 관리자 전용 검색/상세/상태변경 API 일괄 추가(`/api/admin/members/**`, `/api/admin/secede-users/**`, `/api/admin/sleep-users/**`), admin에 `MemberAdminController`+`MemberAdminClient` 통합 구현(매니저 계정 CRUD는 admin 자체 도메인). DB 신규 테이블 불필요, 기존 OP_USER 계열 재사용.

---

## 9. 업로드 파일 규칙 parity (확장자·용량·파일삭제)

> 통합 전 파일: `docs/upload-file-parity-audit.md`

## 업로드·파일처리 AS-IS 전수 대조 (2026-10-06)

### 왜 이 문서가 생겼나

팝업관리(1311) 점검 중 사용자가 지적: **"as-is 서비스로직을 메서드 단위까지 상세하게 확인하고
이식하기로 한거 아니었어? 왜 자꾸 이런 문제가 생기는거야?"**

원인은 내 절차 위반이다. 나는 화면 이식을 **JSP + 매퍼 SQL** 기준으로 맞추고
`*ServiceImpl` **메서드 본문을 끝까지 읽지 않았다**. 업로드 검증·조건부 정리·파일 삭제는
**매퍼에 흔적이 전혀 없고 서비스 계층에만 있다**. 그래서 화면마다 같은 누락이 반복됐다.

확정된 기준(사용자 지시 2026-10-06): **JSP + 매퍼 SQL + 서비스 로직 3종 전부.**

이 문서는 그 기준으로 **AS-IS 업로드 지점 전수**를 뽑아 TO-BE와 대조한 결과다.
(AS-IS 탐색 근거: `grep -rn "AVAILABLE_EXTENSION *=\|availableExtensions *=" --include=*.java`
→ 30개 지점 + 화이트리스트가 없는 지점은 서비스 본문 개별 확인)

---

### AS-IS 업로드 규칙 원본 표

| AS-IS 서비스(메서드) | 확장자 | 용량 | 저장 파일명 | 기존파일 삭제 |
|---|---|---|---|---|
| `PopupServiceImpl.saveImage` | **5종** jpg gif bmp png jpeg | 5MB | `getNewFileName` | 교체·삭제 시 **O** |
| `QnaServiceImple` | **12종** jpg jpeg gif png hwp doc docx xls xlsx ppt pptx pdf | 5MB | `getNewFileName` | **O**(7곳) |
| `CmntyServiceImpl` | **18종**(16종+hwpx,7z) | 50MB | `yyyyMMddHHmmssSSS_원본명` | - |
| `ManualServiceImpl` | **16종** | 50MB | 동일 | - |
| `DataboardServiceImpl` | **16종** | 50MB | 동일 | O(1곳) |
| `LocgovDataBoardServiceImple` | **16종** | 50MB | 동일 | - |
| `MaintenanceServiceImpl` | **18종** | 50MB | 동일 | X |
| `SysNoticeSellerServiceImpl` | **18종** | 50MB | 동일 | X |
| `EmailServiceImpl` | **12종** jpg jpeg gif bmp png hwp doc docx pdf zip ppt pptx | 10MB | `CustomFileService` | - |
| `GiveOperationServiceImpl` | 12종(동일) | 10MB | `CustomFileService` | - |
| `RemittanceServiceImpl` | 16종 | - | `CustomFileService` | - |
| `SellerconfirmServiceimpl` | 16종 | - | `CustomFileService` | - |
| `CustomFileService`(공용) | **17종**(16종+hwpx) | - | 프로그램별 경로 | O |
| `LocgovServiceImpl` | 5종 jpg jpeg gif bmp png | - | - | - |
| `CategoriesFilterServiceImpl`·`StyleBookServiceImpl` | 3종 jpg jpeg png | - | - | - |
| `RepresentativeBannerServiceImpl.uploadFile` | **없음**(JSP `accept`만) | **없음** | `yyyyMMddHHmmss_pc.ext` | X (`// TODO :: 기존 파일 삭제 추가 필요`) |
| `DesignatedDonationServiceImpl.saveDesignatedDonationInfo` | **없음**(`ImageIO.read` 실패로 거름) | 없음 | `yyyyMMddHHmmssSSS_{사이즈}.ext` **사이즈별 다건** | 삭제 시 변형 3개 **O** |

---

### A. 순수 누락 — AS-IS에 있는데 TO-BE에 없었다 (2026-10-06 수정 완료)

| TO-BE | 누락 내용 | 조치 |
|---|---|---|
| `PopupImageStorageService` | 확장자 5종 검사 **전무** | 추가(AS-IS 순서: 확장자→용량, 문구 verbatim) |
| `OperationContentService.createPopup` | 이미지 저장 시 `setContent("")` 없음 | 추가 |
| `OperationContentService.createPopup` | 형태 '3' + 파일없음 → 이미지링크·배경색 `""` 없음 | 추가 |
| `OperationContentService.updatePopup` | 형태 '3'→텍스트 전환 시 **이미지·링크·배경색이 그대로 남음** | AS-IS 분기 3갈래 그대로 이식 |
| `OperationContentService.updatePopup` | 이미지 교체 시 **옛 파일이 디스크에 쌓임** | 교체 전 `delete` 추가 |
| `OperationContentService.deletePopupImage` | 컬럼만 null, **디스크 파일 잔존** | AS-IS 순서(①파일 삭제 ②컬럼 null)로 수정 |
| `OperationContentController` | 형태≠'3'인데 파일을 보내면 TO-BE는 저장했다 | AS-IS처럼 형태 '3'일 때만 저장 |
| `QnaFileStorageService` | 확장자 10종 — **xls·xlsx 빠져 엑셀 첨부가 거부** | 12종으로 교정 |
| `DataBoardFileStorageService` | 확장자 검사 전무 + 20MB(AS-IS 50MB) | 16종 + 50MB |
| `AdminFileStorageService`(운영유지관리·제공자공지) | 확장자 검사 전무 + 20MB(AS-IS 50MB) | 18종 + 50MB |

이미 맞던 것: `CmntyFileStorageService`(18종) · `ManualFileStorageService`(16종/50MB) ·
`EmailAttachmentStorageService`(12종/10MB) · `MainBannerImageStorageService`(AS-IS는 화면 JS만
검사하므로 그 목록과 동일하게 둔 것 — 주석에 사유 있음).

### B. TO-BE가 발명한 차이 — **사용자 판단 필요**

1. **대표배너 수정 시 이미지 유지 vs 비움.** AS-IS `editRepresentativeBanner`는 수정에서도
   파일을 다시 안 올리면 `setFileNamePc("")`로 **컬럼을 비운다**(제목만 고쳐도 이미지가 사라진다).
   디스크 파일은 `// TODO :: 기존 파일 삭제 추가 필요` 주석과 함께 남긴다.
   TO-BE는 "새 파일 있을 때만 교체"다. → **AS-IS 미완성 코드로 보여 현행 유지를 권한다.**
2. **대표배너 등록 시 PC·모바일 이미지 필수.** TO-BE가 만든 검증("PC/모바일 이미지를 모두
   첨부해 주세요.")이고 AS-IS는 **둘 다 없어도 통과**(빈 문자열 저장)한다.
3. **AS-IS에 확장자 검사가 없는 지점**(대표배너·특정사업 이미지·donation `FileStorageService`)에
   화이트리스트를 넣을지. 보안상 넣는 게 맞지만 AS-IS보다 엄격해지는 **의도적 편차**라 기록·승인
   대상이다(기존 승인 편차: 업로드 파일명 경로성분 제거, 사용자 입력 HTML 이스케이프).
4. **TO-BE 임의 용량 제한 20MB**(donation `FileStorageService`) — AS-IS엔 제한이 없다.

### C. 아직 구현 안 된 기능 — 특정사업(17000) 영역 잔여

내가 ①(대표배너)·③(등록/수정 폼)을 "완료"로 적었지만 **파일처리 기준으로는 미완**이다.

1. **사업 이미지가 다건 + 썸네일 사이즈별 저장이다.** AS-IS는 `prjImageFiles` 배열을 받아
   `ShopUtils.getThumbnailType()`의 사이즈마다 리사이즈해 `yyyyMMddHHmmssSSS_{사이즈}.ext`로
   저장하고 `G_DSGNCNTR_PRJ_IMG`에 **ordering과 함께 여러 행**을 넣는다. 첫 이미지를
   `PRJ_IMAGE`(목록 노출용)로 올린다. 삭제는 변형 파일 3개를 지우고 행을 삭제한다
   (`deletePrjImageById`).
   TO-BE는 **단일 이미지 1장**을 프로젝트 행의 `imageUrl`에 저장하고, TO-BE DB에 이미 있는
   **`donation.g_dsgncntr_prj_img`를 쓰지 않는다** → "AS-IS 표가 TO-BE 스키마에 이미 있는데
   안 쓰고 다른 데 저장" 안티패턴 **7번째 사례**.
2. **사업 공지사항 첨부파일 미이식.** AS-IS는 `prjNoticeFiles`를 `CustomFileService`로 저장하고
   `deletePrjNoticeFile`·다운로드까지 있다. TO-BE 공지 API는 `subject`·`content`만 받는다.
   TO-BE DB에 `g_dsgncntr_prj_notice_file`·`g_dsgncntr_prj_notice_img_desc`가 비어 있다.
3. **저장 파일명 규칙.** AS-IS는 `yyyyMMddHHmmssSSS_원본명`(자료실·운영유지관리·제공자공지·
   커뮤니티·매뉴얼)과 `yyyyMMddHHmmss_pc.ext`(대표배너)다. TO-BE는 `UUID.ext`를 쓰는 곳이 많다
   (매뉴얼만 AS-IS 규칙). 원본 파일명을 그대로 쓰면 경로성분 제거가 전제다.

### D. 다음 화면부터 쓰는 체크리스트

AS-IS 서비스 메서드(저장·수정·삭제 각각)를 읽을 때 반드시 확인한다:

1. **입력 검증** — 확장자 화이트리스트 / 용량 / 형식. 검사 **순서와 문구**까지(보통 확장자 먼저).
   AS-IS는 확장자 검사를 목록 + 파일명 `endsWith` **두 번** 하는 곳이 있다(CWE-434 주석).
2. **조건부 분기** — 타입·형태 값에 따라 **다른 컬럼을 비우는지**(팝업 형태 전환이 그 사례).
3. **파일 I/O** — 저장 경로, 파일명 규칙, **파생 파일**(썸네일 사이즈별), **기존 파일 삭제 선행**.
4. **부수효과** — 다른 표 insert/delete, 문자·메일 발송, 이력 적재, 시퀀스 채번.
5. **다건 여부** — 단건으로 보이는 화면이 실제로는 배열(`MultipartFile[]`)인지.

관련 메모리: `asis-screen-port-procedure` · `as-is-logic-is-the-spec` ·
`as-is-parity-exhaustive-audit-method` · `copy-as-is-verbatim-never-invent`

---

### E. 2026-10-06 2차 - 컨트롤러 단위 누락과 업로드 상한 (사용자 지적으로 추가)

사용자 지시로 기준이 **"JSP + 매퍼 SQL + 서비스 로직 + 컨트롤러"** 4종, 각각 **메서드 단위**가 됐다.

#### E-1. 컨트롤러가 통째로 빠져 있었다 - 스마트에디터 팝업 3종
스마트에디터 툴바의 **사진 / 동영상 / CTP** 버튼은 AS-IS
`saleson/common/module/smarteditor/SmartEditorController`(엔드포인트 5개)를 부른다.
TO-BE에 **이 컨트롤러가 아예 없어서** 세 버튼이 모두 오류였다. 메뉴 트리에 없는 공통 모듈이라
화면 단위 이식 목록에 잡히지 않았다 → 영역 이식 전에 **AS-IS 컨트롤러 메서드 전수 체크표**를
만들어야 한다(공통/모듈 컨트롤러 포함: 에디터·주소검색·파일다운로드·팝업결과).

이식한 AS-IS 규칙(컨트롤러 본문 기준):
- 이미지: `file[]` 다건, **jpg·jpeg·png·gif만**(아닌 건 조용히 버림), **건당 5MB** 초과는
  파일명을 모아 "… 파일은 용량 제한을 넘어 업로드에 실패했습니다."
- 결과 문서는 HTML을 **이스케이프**해 심고 JS가 DOMParser로 되돌려 에디터에 넣는다(왕복 재현 필수)
- 동영상: **업로드가 아니다**. youtu.be → youtube.com/embed 치환 + 반응형 iframe(서버 저장 없음)
- CTP: html·ctp·css·jpg·jpeg·png·gif / 5MB / **html(ctp) 1개 이상 필수**,
  `<body>` 안쪽만 추출 + 상대경로 이미지 치환, 오류는 ERROR_01/02/03

검증(여분 포트 8099 + 실제 업로드): 이미지 정상·비이미지·5MB초과·다건혼합 4종,
CTP 정상(body추출+경로치환)·ERROR_01·02·03 4종 **전부 AS-IS대로 확인**, 파일 디스크 저장 확인.

#### E-2. ★업로드 상한이 설정되지 않아 1MB 넘는 모든 업로드가 413이었다
`spring.servlet.multipart`가 **admin·donation·member·order·point에 아예 없었다** → 스프링 기본
**파일 1MB / 요청 10MB**. 그래서 서비스 로직의 50MB·10MB·5MB 규칙에 **도달 자체가 불가능**했다
(매뉴얼·자료실·커뮤니티 50MB, 이메일 10MB, 팝업·Q&A·배너·에디터 5MB 전부). gift만 10MB/30MB로
근거 없는 값이 들어 있었다.
→ AS-IS `application-production.yml`과 같게 **50MB / 50MB**로 맞췄다(AS-IS 개발 프로필은 20MB지만
그 값으로는 50MB 규칙에 못 닿는다). admin·donation·member·gift 적용, order·point는 업로드가 없다.

#### E-3. 공용 저장 서비스의 화이트리스트를 그대로 쓰면 안 되는 경우
`AdminFileStorageService`의 기본 18종에는 **html·ctp·css가 없어** CTP 업로드가
"유효하지 않은 파일입니다."로 터졌다 → 호출부가 허용목록·용량을 넘기는 형태
(`store(file, subdir, allowedExtensions, maxSize)`)를 추가했다. **화면별 AS-IS 목록이 다르다**는
것을 전제로 쓸 것.

---

## 10. member 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-member.md`

## AS-IS 기능 감사 — 회원(member) 서비스

작성일: 2026-09-03
범위: `ghlove-web`(saleson 백엔드 + opmanager 관리자콘솔) / `ghlove-frontend`(Vue2 고객화면)의 회원(User) 도메인 전체를
`ghlove-msa`의 `member`/`admin`/`storefront`와 대조. admin 서비스의 6단계 RBAC(ROLE_ADMIN_1~6) 자체와 인증서(PKI)
로그인은 이미 별도 라운드에서 구현 완료로 확인되어 이번 감사 범위에서 제외했다(`admin_rbac_6tier_role_round`,
`admin_console_auth` 메모 참고).

### 1. AS-IS 회원 도메인 기능 전수 목록

#### 1-A. 고객용(회원 본인) 기능 — storefront/member 서비스

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 회원가입/로그인/로그아웃 | `ghlove-web/.../saleson/shop/user/UserController.java`(join/login), `ghlove-frontend/users/join.html,login.html` | 완료 | member `AuthController`/`AuthApiController` + storefront |
| 아이디/비밀번호 찾기·비밀번호변경 | `UserController.java`(find-id/find-password/change-password), `users/find-idpw.html` | 완료 | round `login_findidpw_and_mypage_redirect_round` |
| 회원정보수정 | `UserController.java`(/modify), `users/modify.html` | 완료 | member `ProfileController`/storefront ProfileView |
| 회원탈퇴 | `UserController.java`(/secede), `users/secede.html` | 완료 | member 탈퇴 플로우 |
| 휴면전환(자동배치)/본인 해제(로그인시 재활성화) | `UserController.java`(sleep-user/wakeup-user) | 완료 | member `DormancyController`(`/batch/dormancy`,`/reactivate`) |
| 관심 지자체 등록/해제 | `mypage/intrstLocGov.html` | 완료 | storefront ProfileView (round9에서 form-urlencoded 계약버그 수정) |
| 배송지 관리 | `saleson/shop/userdelivery/*` | 완료 | member `DeliveryController`/`DeliveryApiController` |
| SNS(카카오/네이버) 로그인 연동, 디지털원패스 본인인증 | `saleson/shop/usersns`,`snsuser`, `users/onepass-*.html` | 완료(mock-gated) | `external_integrations_architecture` 메모, `OnePassClient`/`OAuth2LoginClient` |
| 관리자권한/역할 신청+승인대기 | `saleson/shop/user/ManagerRequestController.java`(비로그인 신청폼) | 완료 | member `RoleRequestController`+admin 승인큐 |
| 마이페이지 상세 7종(1:1문의/관심지자체/회원정보수정/관심답례품/기부혜택증/기부포인트/기부내역) | `mypage/*.html` | 완료 | `mypage_detail_screens_round` |
| 개인정보처리방침 등 정책 동의 | 회원가입 약관동의 | 완료 | `vue3_storefront_migration_round13_policy` |
| 개인정보 파기절차(탈퇴 후 유예기간 파기) | — | 완료 | `sfr_gap_fill_round2_mfa_destruction_sla`, member `UserDataDestructionLog`/`DataDestructionController` |
| SNS 계정 연동/해제(로그인 아닌 마이페이지 자기관리) | `saleson/shop/usersns/UserSnsController.java`(setup-sns/disconnect-sns) | 확인 필요(낮음) | 카카오/네이버 OAuth 로그인 자체는 대체됐으나, "이미 가입된 계정에 SNS를 나중에 연결/해제"하는 자기관리 UI가 별도로 있는지는 미검증. 아래 gap 참고 |

#### 1-B. 관리자용(opmanager) "회원관리" 기능 — admin 서비스

이 카테고리가 이번 감사의 핵심 발견이다. **admin 서비스에는 회원(고객) 통합 관리 화면이 전혀 없다.**
`admin/src/main/java/com/ghlove/admin/service/MemberClient.java`를 확인한 결과 admin↔member 연동은
다음 3가지 읽기전용 호출뿐이다: ①관리자권한신청서 자동입력용 `GET /api/users/{id}`, ②통계 재동기화용
`GET /api/admin/members/all`, ③로그인/액션 로그 화면용 `GET /api/admin/login-log`,`/api/admin/user-action-log`.
회원을 검색/조회/수정/정지/등급변경/포인트지급/강제탈퇴 하는 화면, 관리자(운영자) 계정을 CRUD하는 화면,
지자체 담당자 계정을 관리하는 화면이 admin 템플릿 어디에도 없다.

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 회원(고객) 통합 목록/등록/수정/삭제, 관리자role·메뉴권한 관리, 상세팝업(정보/수정/탈퇴/비번변경/쿠폰/포인트/주문내역) | `saleson/shop/user/UserManagerController.java`(`/opmanager/user/*`, 2000+줄) | **미구현** | §2-1 |
| 일반회원관리(PII 마스킹+비밀번호 재확인 열람, 기부내역/포인트내역 팝업, 탈퇴사유+누계 표시) | `saleson/shop/user/GeneralCustomerManagerController.java`(`/opmanager/user/customer/*`) | **미구현** | §2-1, UserManagerController를 대체하는 최신 버전으로 보임 |
| 지자체 계정/정보 관리(직인이미지, 지자체별 포인트정책, 부서, SYS/LOC 권한분기) | `saleson/shop/user/LocgovManagerController.java`(`/opmanager/user/locgov/*`) | **미구현** | §2-2 |
| 지자체 담당자(정/부, ROLE_ADMIN_5·6) 계정관리 | `saleson/shop/user/LocgovPersonInChargeManagerController.java`(`/opmanager/user/locgov-charger/*`) | **미구현** | §2-2 |
| 오프라인 접수 담당자 계정관리(생성/수정/삭제/엑셀/상태변경/정담당-부담당 교체) | `saleson/shop/user/OffPersonInChargeManagerController.java`(`/opmanager/user/off-charger/*`) | **미구현** | §2-2 |
| 운영자 담당자 계정관리 | `saleson/shop/user/OperPersonInChargeManagerController.java`(`/opmanager/user/oper-charger/*`) | **미구현** | §2-2 |
| 탈퇴회원 목록/탈퇴사유 상세조회(읽기전용) | `saleson/shop/user/SecedeUserManagerController.java`(`/opmanager/user/secede-user/*`) | **미구현** | §2-3 |
| 휴면회원 목록/검색 + 관리자가 개별 회원 수동 해제(wakeup) | `saleson/shop/user/SleepUserManagerController.java`(`/opmanager/user/sleep-user/*`) | **부분구현** | member `DormancyController`는 배치실행(건수만 표시)+본인셀프해제뿐, 목록조회·검색·관리자의 개별 wakeup 없음. §2-3 |
| 회원등급(포인트등급/할인등급) 마스터 관리 | `saleson/shop/userlevel/UserLevelManagerController.java`(`/opmanager/user-level/*`) | **미구현** | §2-4, member `UserDetail.levelId` 필드는 존재하나 값을 채우는 관리화면이 없어 죽은 FK 상태 |
| 지자체 명예사용자(시도민증 이미지, 등급기준) 관리 + 열람이력 통계 | `saleson/shop/lclgvHnrUser/LclgvHnrUserManagerController.java`(`/opmanager/lclgvHnrUser/*`) | **미구현** | §2-5, 고객화면(명예기부자 등급 표시)은 `storefront/.../HonorCertificatesView.vue`로 간이 구현되어 있으나 그 등급/이미지를 지자체별로 세팅하는 관리자 화면은 없음 |
| 사용자권한그룹 관리 | `saleson/shop/usergroup/UserGroupController.java`(`/opmanager/user-group/*`) | 의도적 제외(중복 확인됨) | §2-6, 내부적으로 `roleService.insertRole()`을 그대로 호출 — admin의 `/admin/access/*`(RBAC role 관리)와 동일 엔티티를 다루는 AS-IS의 또다른 메뉴 진입점일 뿐. 신규 구현 불필요 |
| 관리자 권한요청(PKI 인증서 신청폼 포함) | `saleson/shop/user/ManagerRequestController.java`(`/opmanager/manager-request/*`) | 완료(대응 확인됨) | admin `ManagerRequestController`(`/admin/manager-requests/*`) + `CertLoginController`(`/admin/my-cert/*`)로 대응. PKI 신청폼 세부 문항 1:1 대조는 안 함 |
| 로그인 실패횟수 기반 계정 잠금 | AS-IS grep 결과 없음(`saleson/shop/user`,`saleson/shop/auth`, `ghlove-common` 전체에 lockout/failCount 로직 부재) | 해당없음 | AS-IS 자체에 없는 기능으로 확인됨 — 스킵 근거 있음 |
| 다중 디바이스/동시세션 제한 | AS-IS 소스에서 명시적 세션제한 로직 미확인 | 확인 필요(낮음) | 프레임워크 레벨(Spring Security concurrent-session) 설정일 가능성이 높아 애플리케이션 기능 감사 범위 밖으로 판단, 배포설정 검토 시 별도 확인 권장 |

### 2. Gap 상세

#### 2-1. [높음] 관리자 콘솔 "회원(고객) 관리" 화면 전체 부재

**왜 필요한가**: 운영자가 민원 대응, 결제/포인트 오류 정정, 부정가입 의심 계정 탈퇴처리 등을 하려면 회원을
검색해서 상세를 보고 직접 조작할 수 있는 화면이 반드시 필요하다. 현재 admin은 회원 데이터를 **전혀 조회할 수
없다** — member 서비스에 로그인해서 DB를 직접 봐야 하는 상태다. 이 프로젝트의 admin 콘솔이 "123개 화면
전부 완료"로 기록되어 있지만(`admin_console_real_asis_markup_round`), 그 목록에 회원관리 카테고리가
빠져 있었다는 것이 이번 감사의 핵심 발견이다.

**AS-IS 동작 방식** (두 컨트롤러가 공존, 후자가 최신):
- `UserManagerController`(`/opmanager/user/*`): `customer/list-old`(구버전 목록), `customer/create`(회원 직접등록),
  `customer/point`·`customer/point-by-excel`(전체/엑셀 일괄 포인트지급), `manager/list`·`create`·`edit`·`delete`
  (ROLE_OPMANAGER 관리자 계정 CRUD), `manager-role/list`·`create`·`edit`(Role/메뉴권한 CRUD — 이건 admin에
  `/admin/access/*`로 이미 있음, 중복 불필요), 팝업 시리즈: `popup/details`(회원상세), `popup/edit`(정보수정),
  `popup/sns-user`(SNS연동상태), `popup/delete`(관리자에 의한 강제탈퇴, 탈퇴사유 기록), `popup/password`
  (비밀번호 강제변경/SMS·이메일 발송), `popup/coupon`(보유쿠폰 내역), `popup/point/{pointType}`(포인트내역,
  기본포인트/배송비쿠폰 등 pointType별), `popup/point-create`(개별 포인트 지급·차감), `popup/order`(주문내역).
- `GeneralCustomerManagerController`(`/opmanager/user/customer/*`, 신버전): 목록/상세는 **기본적으로 PII를
  마스킹**해서 보여주고, `popup/password/{userId}`(관리자 자신의 비밀번호 재확인) → `popup/access/{userId}`
  (통과 시에만 마스킹 해제된 상세 표시)라는 2단계 열람 절차를 거친다. 상세에는 회원누계
  (`getGeneralCustomerCumulativeTotal`), 기부내역(`details/{userId}/cntr-list`), 포인트내역
  (`details/{userId}/point-list`), 배송지(`delivery/{userId}`)가 탭/팝업으로 딸려 있고, 탈퇴는
  `popup/secede`에서 사유를 받아 처리하며 처리자 본인이 탈퇴 대상이면 로그아웃까지 트리거한다.

  **주의**: 기존 메모 `admin_pii_display_no_masking`은 "AS-IS opmanager 화면은 마스킹 없이 노출"이라고
  기록했는데, 이는 구버전 `UserManagerController.popup/details`(마스킹 없음)만 확인한 결과였다.
  신버전 `GeneralCustomerManagerController`는 **명시적으로 마스킹+비밀번호 재확인 열람** 패턴을 쓴다.
  회원관리 화면을 새로 만들 때는 신버전(GeneralCustomerManagerController) 쪽 PII 마스킹 정책을 따라야
  기존 메모와 상충하지 않게 재정리된다 — 즉 "opmanager는 마스킹 안 함" 규칙은 회원관리 화면에는
  적용하지 말 것.

**TO-BE에서 뭘 만들어야 하는가**:
- admin 신규 컨트롤러 `MemberAdminController`(가칭) + 템플릿 `member/*` (또는 `user/*`): 목록(검색조건:
  로그인ID/이름/이메일/전화번호/가입일/상태), 상세(마스킹 기본, 비밀번호 재확인 후 언마스킹), 정보수정,
  강제탈퇴(사유입력), 비밀번호 강제변경, 포인트내역+개별지급/차감(point 서비스 API 필요),
  쿠폰내역(order/coupon 서비스), 주문내역(order 서비스), 기부내역(donation 서비스), 배송지(member 서비스).
- member 서비스에 admin 전용 API 신설 필요: 목록/검색(`GET /api/admin/members?...`), 상세(마스킹 여부
  파라미터 또는 별도 unmask 엔드포인트), 수정, 강제탈퇴, 비밀번호 강제변경. 현재 `UserApiController`는
  `/api/users/{userId}`(단건, MemberInfo record — 마스킹 없음, 프리필 전용), `/api/admin/members/all`
  (통계용 스냅샷), `/api/admin/login-log`,`/api/admin/user-action-log`만 있고 admin이 CUD를 걸 수 있는
  API가 전무하다.
- 회원 직접 등록(`customer/create`, 신규가입쿠폰 자동발급 포함)은 우선순위 낮음(관리자가 대신 가입시키는
  기능은 실사용 빈도 낮음) — 필요시 후순위.

#### 2-2. [높음] 지자체·오프라인·운영자 "담당자 계정" 관리 화면 부재

**왜 필요한가**: 현재 admin은 관리자권한신청(`/admin/manager-requests/*`) 승인 시 **계정이 생성되는 것**까지는
커버하지만, 승인 후 이미 존재하는 담당자 계정을 목록으로 보거나, 담당 지자체를 바꾸거나, 정담당/부담당을
교체하거나(`auth-swap`), 비밀번호를 초기화하거나, 계정을 삭제하는 사후관리 화면이 전혀 없다. 신청→승인
플로우만으로는 "이 지자체 담당자가 퇴사해서 계정을 지워야 한다" 같은 운영 시나리오를 처리할 수 없다.

**AS-IS 동작 방식**:
- `LocgovManagerController`(`/opmanager/user/locgov/*`): 로그인한 관리자의 역할(SYS=시스템/행안부 전체목록,
  LOC=지자체담당자 본인 지자체로 자동 리다이렉트)에 따라 분기. 지자체 정보(직인이미지 `sealView`, 연도별
  포인트정책 `point/{stdrYear}/{locgovCode}`, 부서 `dept/popup`) CRUD.
- `LocgovPersonInChargeManagerController`(`/opmanager/user/locgov-charger/*`): 지자체 담당자 계정
  목록/수정/삭제, 상위지자체 기준 드릴다운(`locgov/{upperLocgovCode}/list`).
- `OffPersonInChargeManagerController`(`/opmanager/user/off-charger/*`): 오프라인 담당자 계정
  목록/수정/삭제/엑셀다운로드/상태변경(`status/edit`)/**정담당↔부담당 교체(`auth-swap`)**/비밀번호
  초기화(`popup/password-init`)/생성(`create`,`createProcess`).
- `OperPersonInChargeManagerController`(`/opmanager/user/oper-charger/*`): 운영자 담당자 계정
  목록/수정/삭제(단순 CRUD, 위 두 개보다 기능 적음).

**TO-BE에서 뭘 만들어야 하는가**: admin에 "담당자 계정 관리" 화면 신설 — 이미 있는 6단계 RBAC(ROLE_ADMIN_1~6)
계정을 role/지자체 소속별로 목록조회+검색하고, 소속 지자체 변경, 계정 삭제, 비밀번호 초기화(이메일/SMS
재발송 또는 강제 재설정), 정/부 담당자 교체 기능을 추가. 지자체 정보(직인이미지/포인트정책) CRUD는
`designated_donation_admin_round`에서 만든 부서관리와 겹치지 않는지 먼저 확인 후, 없는 부분(직인이미지,
연도별 포인트정책)만 보강.

#### 2-3. [중간] 탈퇴회원 조회 화면 + 휴면회원 관리자용 목록/개별해제

**왜 필요한가**: 탈퇴 처리 자체(2-1에서 다룸)와는 별개로, "이미 탈퇴한 회원이 몇 명이고 왜 탈퇴했는지"를
집계·조회하는 화면, 그리고 "현재 휴면 상태인 회원이 누구인지 목록으로 보고 특정 회원만 관리자가 수동으로
깨워주는" 화면이 AS-IS에는 있지만 TO-BE엔 없다.

**AS-IS 동작 방식**:
- `SecedeUserManagerController`(`/opmanager/user/secede-user/*`): `list`(탈퇴일 검색조건), `popup/reason-details/{userId}`
  (탈퇴사유 상세) — 읽기전용.
- `SleepUserManagerController`(`/opmanager/user/sleep-user/*`): `list`(최종로그인일 기준 검색), `wakeup`(POST,
  관리자가 특정 회원 1명을 휴면 해제).

**TO-BE 현황**: member `DormancyController`는 `/batch/dormancy`(전체 일괄 휴면전환 실행, 처리건수만 표시,
대상자 목록 없음)와 `/reactivate`(회원 본인이 로그인ID+비번으로 셀프 해제)만 있다. 관리자가 목록에서 검색해
개별 회원을 골라 깨워주는 기능, 탈퇴회원 통계/사유 조회 기능이 없다.

**TO-BE에서 뭘 만들어야 하는가**: admin에 "탈퇴회원 관리"(읽기전용 목록+사유 상세), "휴면회원 관리"(검색
가능한 목록 + 개별 wakeup 버튼) 2개 화면 신설. member 서비스에 상태값(휴면/탈퇴) 기준 목록 조회 API와
관리자용 개별 wakeup API(`POST /api/admin/members/{userId}/wakeup`) 추가 필요.

#### 2-4. [중간] 회원등급(포인트등급) 마스터 관리 화면 부재

**왜 필요한가**: `UserDetail.levelId`가 이미 member 도메인에 존재하고, admin `CouponAdminController`에도
`targetUserLevel`로 등급별 쿠폰 타겟팅 필드가 있다 — 즉 "등급"이라는 개념은 이미 코드 여러 곳에서 쓰이고
있는데, 정작 그 등급 자체(이름, 그룹, 등급별 혜택/할인율)를 정의·관리하는 화면이 없다. 현재는 값을 채울
방법이 없는 죽은 FK/파라미터 상태다.

**AS-IS 동작 방식**: `UserLevelManagerController`(`/opmanager/user-level/*`) — 그룹코드별 등급 목록
(`list/{groupCode}`), 등급 생성/수정/삭제(`create/{groupCode}`,`edit/{levelId}`,`delete/{levelId}`),
등급별 첨부파일(뱃지 이미지 등) 삭제.

**TO-BE에서 뭘 만들어야 하는가**: admin에 "회원등급 관리" 화면 신설(등급 그룹/등급 CRUD), member 서비스에
등급 마스터 테이블+API 추가. 우선순위는 중간 — 등급 개념이 실제 할인/혜택 로직에서 얼마나 쓰이는지
(현재는 쿠폰 타겟팅 필드 정도만 확인됨) 재확인 후 실제 활용도에 따라 조정 가능.

#### 2-5. [중간] 지자체 명예사용자(시도민증) 관리 화면 부재

**왜 필요한가**: 고향사랑기부 특유의 "명예기부자" 등급(골드/실버/브론즈 등, 지자체별 기준금액 다름) 표시는
storefront `HonorCertificatesView.vue`에 **연도·지자체별 등급 카드 목록**으로 간단히 구현되어 있다(주석에
"AS-IS mypage/honorList.html 재현"이라고 명시됨). 그러나 AS-IS의 "시도민증" 이미지 스와이퍼 UI가 참조하는
지자체별 커스텀 이미지(`/upload/lclgvHnrUserMng/{locgovCd}/{rprsImgNm}`)와 등급 기준을 **지자체 담당자가
직접 설정하는 관리자 화면**은 도입 자체가 안 됐다. 즉 지금 storefront가 보여주는 등급은 하드코딩된 로직으로
산정될 뿐, 지자체별로 다르게 설정할 방법이 없다.

**AS-IS 동작 방식**: `LclgvHnrUserManagerController`(`/opmanager/lclgvHnrUser/*`) — 역할에 따라 분기(SYS/MOIS
관리자는 전체 지자체 목록, 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체 설정폼으로 즉시 리다이렉트),
`lclgvHnrUserMng/form/{lclgvCd}`(시도민증 이미지 업로드+등급기준 설정), `lclgvHnrUserViewHist/list`(+엑셀)
(회원들이 명예사용자 화면을 열람한 이력 통계).

**TO-BE에서 뭘 만들어야 하는가**: admin에 "지자체 명예사용자 관리" 화면 신설(이미지 업로드, 등급기준 입력),
donation 또는 member 서비스에 지자체별 설정 테이블+API. storefront `HonorCertificatesView.vue`는 이 설정값을
읽어와 이미지/기준을 반영하도록 보강. 열람이력 통계는 우선순위를 더 낮게 잡아도 무방(부가 기능).

#### 2-6. [정보] 사용자권한그룹 관리 — 신규 구현 불필요(중복 확인됨)

`UserGroupController`(`/opmanager/user-group/*`)의 `create`/`edit` 액션은 내부적으로 `Role role` 파라미터를
받아 `roleService.insertRole(role)`/`updateRole(role)`을 그대로 호출한다 — `UserManagerController`의
`manager-role/*`(현재 admin `/admin/access/*`로 대응 완료)와 완전히 같은 `Role` 엔티티를 다루는 AS-IS의
또 다른 메뉴 진입점일 뿐이다. 별도 구현 불필요.

#### 2-7. [낮음/확인필요] SNS 계정 자기관리(연동/해제) 화면

`UserSnsController`/`SnsUserController`(`/sns-user`,`/sns-user-delete`)의 `setup-sns`/`disconnect-sns`가
"이미 로그인된 기존 계정에 나중에 SNS 계정을 연결하거나 해제하는" 마이페이지 자기관리 기능인지, 아니면
로그인 자체의 일부(현재 `ExternalLoginController`로 대체 완료)인지 코드 레벨로 100% 확정하지 못했다.
컨트롤러 타이틀이 "특집페이지"(구 이벤트성 캠페인)로 되어 있어 레거시 캠페인 잔재일 가능성이 있다.
우선순위 낮음 — 실제 착수 전 AS-IS 라이브사이트(마이페이지 내 SNS 연동 메뉴 존재 여부)를 재확인 권장.

### 3. 완전히 구현 확인됨 (요약)

회원가입/로그인/로그아웃, 아이디·비밀번호 찾기/변경, 회원정보수정, 회원탈퇴, 휴면전환 배치+본인 셀프해제,
관심지자체, 배송지 관리, SNS/디지털원패스 로그인 연동(mock-gated), 역할신청+승인큐, 마이페이지 상세 7종,
정책 동의, 개인정보 파기절차, 관리자권한요청(PKI 포함) — 위 §1-A, §1-B 표에 상세 및 근거 라운드/파일 기재.

---

## 11. member 소셜 연동해지 parity

> 통합 전 파일: `docs/member-social-unlink-parity-audit.md`

## member 연동해지(디지털원패스/카카오) parity audit

작성 2026-09-22. AS-IS 전수대조 후 구현. 근거는 모두 AS-IS 소스(file:line).

### 1. AS-IS 전수 (확인 완료)

#### 진입점 — 회원정보수정 `ghlove-frontend/users/modify.html`
회원구분 필드 옆(96~107) 버튼 2개 + 하단 회원탈퇴 버튼(431):
- `디지털원패스\n연동 해지` — `v-if="param.userKeyYN=='Y'"` → `onepassCancel()`
- `카카오\n연동 해지` — `v-if="param.kakaoUserKeyYN=='Y'"` → `kakaoLinkClear()`
- `회원탈퇴` — `v-if="param.userKeyYN=='N'"` → `goToSecede()` = `/users/secede.html`(일반, 비번 필요)

**onepassCancel()** (956):
- userKey/intfToken 쿠키 없으면 alert `"연동해지를 위해서는 디지털 원패스로 다시 로그인 해주세요."`
- confirm `"디지털원패스 회원 연동해지를 하시겠습니까?"`
  - `loginPathCode=='300'`(원패스가입) → `/users/onepass_secede.html` (연동해지+탈퇴)
  - else(일반가입+원패스 부가연동) → POST `/api/auth/onepass-unlink` (연동만 해제, reload)

**kakaoLinkClear()** (1408): POST `/api/kakao-link/kakao-link-clear`
- `result=='SUCCESS'` → alert `"카카오 계정 연동해제가 완료되었습니다."` reload (연동만 해제)
- `result=='CHECK_SECEDE'` → confirm `"카카오 계정 연동해제시 탈퇴가 같이 진행됩니다. 진행하시겠습니까?"` → `/users/secede-kakao.html`

#### 연동해지 탈퇴화면
`users/onepass_secede.html` (제목 "연동해지"):
- init POST `/api/user/getSecedeInfo` → {loginId,userName,leaveCodeList}
- 탈퇴사유(leaveCode) 필수, 불편사항(leaveReason)
- submit POST `/api/auth/onepass-cancel` {userKey,intfToken,leaveCode,leaveReason}
  → `info.value=='00'`이면 alert(info.message) 후 logout

`users/secede-kakao.html` (제목 "회원탈퇴"):
- init POST `/api/user/getSecedeInfo` → +pointList(지자체별 잔여포인트 표)
- 탈퇴사유 필수 → confirm `"회원 탈퇴 시 회원 서비스를 모두 사용할 수 없습니다.\n정말 탈퇴하시겠습니까?"`
- submit POST `/api/kakao-link/kakao-link-secede` {loginId,password,leaveCode,leaveReason}
  → `result=='SUCCESS'`이면 alert `"탈퇴처리가 정상처리 되었습니다."` 후 logout / `errMsg`면 alert

#### AS-IS 백엔드
- `AuthController#onepassCancel` (1830): `ApiSendHandler.InterLockRelease(userKey,intfToken)`(원패스 외부 연계해지 API) 성공 시 `userService.updateUserKeyStatusCode(userId,leaveCode,leaveReason)`(=일반 탈퇴처리 동일 계열: 당해 기부액 스냅샷 → OP_USER/DETAIL 개인정보 NULL → 관심지자체/답례품/권한 삭제).
- `KakaoLinkController#kakaoLinkSecede` (106): `kakaoLinkService.kakaoLinkSecedeByUserId(userId,leaveCode,leaveReason)`(카카오 unlink + 탈퇴처리).
- `KakaoLinkServiceImpl#kakaoLinkClearByUserId` (161): `loginPathCode` 100/300 → 카카오 유저키 삭제+톡키트 unlink → SUCCESS / `500` → CHECK_SECEDE.
- `getSecedeInfo` (UserController:129): {loginId,userName,leaveCodeList=LEAVE_CODE,pointList,loginPathCode}.

### 2. MSA 매핑 결정 (기록 — 임의발명 아님)

| AS-IS | MSA 대응 | 근거 |
|---|---|---|
| `userKeyYN=='Y'` (원패스 연동) | `loginPathCode=='300'` | MSA 원패스=CI기반 가입, 별도 userKey 컬럼 없음. loginPathCode 300=ONEPASS([[ExternalLoginService]] LOGIN_PATH_BY_PROVIDER) |
| `kakaoUserKeyYN=='Y'` | `loginPathCode=='500'` 또는 `kakaoUserKey!=null` | 카카오 가입경로(500) 또는 부가연동 키 보유. mock 카카오가입은 kakaoUserKey를 안 넣으므로(프로필 신원) 500도 포함해야 버튼이 뜬다 |
| InterLockRelease / 카카오 톡키트 unlink | `OnePassClient.releaseInterlock()` / `KakaoCertClient.unlink()` | 로그인과 동일하게 실연계 off면 통과(mock), on이면 실제 API. 현재 off |
| userKey/intfToken 쿠키 가드 | (재현 안 함) | MSA 인증은 GH_AUTH JWT+세션. 원패스 세션쿠키 자체가 없어, 재현 시 항상 실패→기능검증 불가. 가드 의도(원패스 인증세션 필요)를 loginPathCode로 대체 |
| `getSecedeInfo` | 기존 `GET /api/withdraw-info` 재사용 | {loginId,userName,leaveCodeList,pointSummary} 동일 필드 |

### 3. 구현 (2026-09-22)
- member `ProfileResponse` += loginPathCode/userKeyYN/kakaoUserKeyYN.
- member `MemberService`: `withdraw`(비번확인)에서 공통본문 `applyWithdrawal` 추출, `secedeExternal`(비번 없이 탈퇴) + `clearKakaoLink`(500→CHECK_SECEDE, 그 외→키삭제 SUCCESS) 추가.
- member `OnePassClient.releaseInterlock()` / `KakaoCertClient.unlink()` 추가(off면 통과).
- member `AccountUnlinkApiController`(신규): `POST /api/auth/onepass-cancel`, `/api/kakao-link/kakao-link-clear`, `/api/kakao-link/kakao-link-secede`. 성공 시 session invalidate + 쿠키 clear.
- storefront `ProfileView.vue`: 회원구분 옆 연동해지 버튼 2개(조건부) + 회원탈퇴 버튼 `userKeyYN=='N'` 조건 + onepassCancel/kakaoLinkClear.
- storefront `OnepassSecedeView.vue`(/mypage/onepass-secede), `KakaoSecedeView.vue`(/mypage/secede-kakao) 신규 + 라우트.

### 4. 보류(기록) — DA/외부연계 개방 대기
- **`/api/auth/onepass-unlink`(원패스 연동만 해제, 탈퇴X)**: MSA는 원패스가 가입경로(300)여서 "탈퇴 없는 원패스 부가연동 해제" 상태가 성립하지 않음(onepassCancel은 300→항상 secede). 부가연동 도입/DA 설계 시. → [[deferred-features]]
- **실 연계해지 API 본체**(InterLockRelease/카카오 톡키트 unlink): 방화벽·실 IdP 개방 시 client 내부만 교체. 현재 off 통과.
- 카카오 mock 가입이 kakaoUserKey를 채우지 않는 점은 mock 한계(실연계 on이면 채워짐).

---

## 12. donation 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-donation.md`

## AS-IS 기능감사 - donation(기부) 서비스

조사일: 2026-09-03. 대상: `ghlove`(legacy 모놀리스) 고향사랑기부(도네이션) 도메인 전체 vs `ghlove-msa`의 `donation`/`storefront`/(참고)`admin` 서비스.

### 조사 방법 메모

AS-IS 기부 도메인의 실제 소스는 처음 추정한 것과 달리 `ghlove-web`(saleson opmanager 관리자콘솔)이 아니라 아래 3곳에 걸쳐 있다.

- `ghlove-frontend/donation`, `mypage/cntr*.html`, `modules/op.donation*.js` — 고객용 Vue2 화면 + 클라이언트 로직
- `ghlove-api/src/main/java/saleson/api/donation` — 고객용 REST 진입점(`NgDonationController`, `RegionTaxController`, `SeoulTaxController`)
- `ghlove-common/src/main/java/saleson/shop/donation` — 실제 비즈니스 로직(`DonationService`, `NgDonationServiceImpl`, `NgDonationRelayServiceImpl`, `DonationVerification`, `NgDonationBatchServiceImpl` 등, 총 60+ 파일)

`ghlove-web`의 `saleson/shop/give*`, `designateddonation`, `offgive` 패키지는 전부 **admin(운영자콘솔)** 쪽 컨트롤러이며, 이미 `designated_donation_admin_round`/`give_statistics_scope_and_levy_assumption`/`offgive_and_remittance_rounds` 메모에서 admin 서비스 감사로 완료 확인됨 — 이번 donation 감사에서는 참고만 하고 재검토하지 않았다.

### AS-IS 기능 전수 목록

| 기능 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 지자체 목록/기부하기 폼(일반+지정) | `ghlove-frontend/donation/donation-main.html`, `ngDonationService.getUserCntrInfo/getSidoList/getLocGovInfo` | ✅ 완료 | `donation/donate.html`, `DonationService` |
| 지정기부(목록/상세/응원메시지) | `ghlove-frontend/designated-donation/*` | ✅ 완료 | storefront `/donate/designated*` (round6 메모) |
| 기부 상태전이(REQUESTED→COMPLETED/CANCELLED) | `DonationService.sunapProcess/paymentCancellation` | ✅ 완료 | `DonationService.completeDonation/cancelDonation` |
| 연간 개인 기부한도 검증(동적 금액) | `DonationVerification.isDonationNormalAmount` (연도별 CODE 테이블) | ✅ 완료 | `DonationService.validateAnnualLimit` — `SYSTEM_CONFIG` + `G_CTBNY_SETUP` 지자체별 한도까지 반영, 하드코딩 아님 |
| 세외수입 부과/수납 연계(서울 vs 그외 지자체) | `SeoulTaxService`/`RegionTaxService`, `NgDonationRelayServiceImpl.sntrBugaInsert/contryBugaInsert` | ✅ 완료(mock) | `LocalTaxClient` — `external_integrations_architecture` 메모의 기존 스코프 결정 |
| 국세청 홈택스 전자기부금영수증 연계 | `NgDonationRelayServiceImpl.sendNtsEreceipt` | ✅ 완료(mock) | `NtsClient`, `DONATION_LEVY` 테이블 |
| GIRO(지로) 계좌이체 결제 팝업 | `op.donation.js` giroPay/giroPolling/giroSunapConfirm 등(1300줄+) | ✅ 완료(mock, 팝업 UI 없이 백엔드 단순화) | 세외수입 연계와 동일한 mock 패턴의 일부로 흡수됨. 실제 지로 팝업창 UX는 재현 안 함 |
| 기부확인증(마이페이지, 카드뒤집기, 출력) | `mypage/receiptList.html`, `modal-receipt_view.vue` | ✅ 완료 | `donation_receipt_as_is_parity` 메모, `receipts.html`/`certificate.html`/`certificate-print.html` |
| 세액공제 계산(2026 개정 3단계 요율) | AS-IS 실제코드는 구법 유지(안내문구와 불일치, AS-IS 자체버그) | ✅ 완료(TO-BE가 개정법 기준으로 의도적 보정) | `donation_receipt_as_is_parity` 메모 |
| 기부혜택(지자체별 혜택 텍스트) | `G_HONOR_BENEFIT` | ✅ 완료 | `HonorBenefit`/`HonorBenefitController` |
| **명예기부자(누적기부액 기준 등급 등록/변경/삭제)** | `NgDonationServiceImpl.insertHonorCntrbtr`, `mypage/honorList.html` | ✅ 완료 | `HonorCntrbtr`/`HonorCntrbtrRepository`, `DonationService`(467~478행), `honor-certificates.html` — 처음엔 미확인 기능으로 의심했으나 실제로는 이미 구현되어 있음을 확인 |
| 오프라인 기부(기탁서) 등록 | AS-IS `procOffOrder` 등 | ✅ 완료 | `vue3_storefront_migration_round7_offline` 메모 |
| 관심 지자체 등록/조회 | `NgDonationServiceImpl.setIntrstLocgov/getIntrstLocgovInfo` | ✅ 완료 | `interest-locgovs.html`, `InterestLocgovController` |
| 기부 취소 사유 기록(admin 처리) | AS-IS opmanager give-reqmng | ✅ 완료 | `CntrReqmngService`(사유 10자 이상 검증), admin `give-reqmng-list/form.html` |
| 과오납 처리 / 수납취소 상태관리 | `DonationService.overPayment/paymentCancellation`(AS-IS) | ✅ 완료 | admin `CntrReqmng` 워크플로우로 흡수 |
| 일반기부(비지정) admin 관리 화면 | opmanager give-reqmng | ✅ 완료 | `CntrReqmngService`는 지정기부 여부와 무관하게 전체 `Donation` 대상 검색/승인/취소 지원 확인 |
| 안내사항(guide1/2/5/6) | `ghlove-frontend/donation/guide1,2,5,6.html` | ✅ 완료 | `vue3_storefront_migration_round9_guides` 메모 |
| 연말정산 세액공제 안내(guide3) | `ghlove-frontend/donation/guide3.html` | ✅ 완료 | storefront `TaxCreditGuideView.vue`(라우트 `/honor`) — Thymeleaf가 아닌 Vue3로 이전되어 있어 처음엔 누락으로 의심했으나 실제로는 존재 확인 |
| 고향사랑기부 주의사항(guide4, AS-IS 죽은코드) | `donation_info-lnb_ali.vue`에서 링크 자체가 주석처리 | — 스킵 정당 | AS-IS 자체가 죽은 코드(내비게이션에 노출 안 됨) — `feedback_scope_default_full_parity`의 유일한 스킵 근거 충족 |
| 정책(개인정보처리방침/저작권정책/이용약관) | AS-IS `policy/*` | ✅ 완료 | `vue3_storefront_migration_round13_policy` 메모 |
| 행공센 연계 주소조회(내국인, `rsgstadres`) | `DonationService.rsgstadres`, `NgDonationRelayServiceImpl.rsgstadresinfo` | ✅ 완료(mock) | `LocalTaxClient` 계열로 흡수. 마이페이지 등록주소 기준으로 지자체 판정(코드 주석에 명시) |
| **외국인/재외국민/외국국적동포 기부(거소신고 조회 연계)** | `DonationService.rsgstadresForeigner`, `donation-main.html`(외국인등록번호 입력 UI) | ❌ 미구현 | `donate.html` 29~32행 코드주석에 "외국인(거소신고) 별도 플로우... 이번 스코프에서는 제외한다"고 명시돼 있으나, 이 결정을 뒷받침하는 메모리/사용자 승인 기록이 없음. AS-IS는 죽은 코드가 아니라 실동작 기능 |
| **지자체별 기부제한 스케줄(CntrLmtt, 강원도 등 일시적 기부금지)** | `DonationService.locGovLmtt`, `G_CNTR_LMTT` | ❌ 미구현 | 위와 동일한 `donate.html` 코드주석에서 함께 제외 명시 |
| **기부 완료/명예기부자 등급변경 SMS·알림톡 통지** | `NgDonationServiceImpl.sunapSuccess`(기부 감사 인사 SMS), `insertHonorCntrbtr`(국민비서 SMS) | ❌ 미구현 | `DonationService.completeDonation()`/명예기부자 갱신 로직에 `NotificationClient` 연동 없음. member/admin 서비스엔 이미 자체 `NotificationClient` 사본이 있어 동일 패턴 이식 가능 |
| 오늘 같은 지자체 중복기부 시도 경고 다이얼로그 | `donation-main.html` 893행 confirm() | ❌ 미구현(경미) | 순수 UX 넛지, 데이터 무결성엔 영향 없음 |
| "빠른기부하기(예시)" 페이지 | `donation/quick-donation.html` (제목에 "(예시)" 명시) | — 검토불요 | 프로덕션 내비게이션에 연결 안 된 데모/샘플 페이지로 판단 |

### Gap 상세

#### 우선순위 높음

없음 — 나머지 미구현 항목은 모두 외부기관 연계(mock 패턴으로 이미 흡수) 또는 알림/UX 수준이라 상위 우선순위로 분류할 항목이 없음.

#### 우선순위 중간

**1. 외국인/재외국민/외국국적동포 기부 플로우 미구현**
- 위치: 신규 필요 — `donation/service/DonationService`(또는 신규 `ForeignDonorService`), `donation/templates/donate.html`
- AS-IS 근거: `ghlove-common/.../DonationService.rsgstadresForeigner()`(등록외국인/재외국민/외국국적동포 3분류, 체류만료일 검증, "자기 지자체 기부불가" 검증), `donation-main.html`의 외국인등록번호 입력 UI
- 착수 방법: 이미 확립된 mock-gated 외부연계 패턴(`LocalTaxClient` 참고)을 그대로 적용 — `ForeignResidentClient`(가칭)를 만들어 `enabled=false`일 때 그럴듯한 mock 응답(체류상태코드 1/2/3, 만료일, 주소)을 반환하게 하고, `donate.html`에 "외국인/재외국민" 체크박스+외국인등록번호 입력란을 추가해 국내 흐름과 동일하게 `validateAnnualLimit` 이전 단계에서 "자기 거주 지자체엔 기부 불가" 검증을 추가한다.
- 참고: 신규 DB 컬럼 `FOREIGN_STATUS_CODE`(OP_USER 등)가 필요할 수 있음 — AS-IS `ForeignStatusParam.updateForeignStatusCode` 참고.

**2. 지자체별 기부제한 스케줄(CntrLmtt) 미구현**
- 위치: 신규 필요 — `donation/domain/CntrLmtt`(엔티티), `LocgovRepository`, `DonationService.validateAnnualLimit()` 앞단
- AS-IS 근거: `DonationService.locGovLmtt()` — `G_CNTR_LMTT` 테이블에서 `LMTT_BGN_DE`~`LMTT_END_DE` 사이 오늘 날짜가 걸리면 해당 지자체 기부 자체를 차단(위반사유 `VIOLT_RESN_CN` 노출)
- 착수 방법: 이미 존재하는 `G_CTBNY_SETUP`(지자체별 설정) 테이블 옆에 `G_CNTR_LMTT` DDL 추가, 기부하기 폼 진입 시 지자체 선택 즉시 이 제한을 조회해 안내 문구로 노출 + 실제 기부 신청 시 서버단에서도 재검증. admin 쪽에 이 제한기간을 등록/해제하는 CRUD 화면도 함께 필요(현재 admin에도 없음 — admin 재감사 시 같이 반영 권장).

#### 우선순위 낮음

**3. 기부 완료/명예기부자 등급변경 알림 미발송**
- 위치: `donation/src/main/java/com/ghlove/donation/service/DonationService.java` — `completeDonation()`(316행 부근)과 명예기부자 갱신 블록(467~478행)
- AS-IS 근거: `NgDonationServiceImpl.sunapSuccess()`가 수납 성공 시 `smsIpsService.giveSendSms(..., SmsType.DONATION)`으로 "기부 감사 인사" SMS 발송, `insertHonorCntrbtr()`가 등급 신규등록/변경 시 국민비서(Gov24) SMS 발송
- 착수 방법: member/admin 서비스에 이미 있는 `service/integration/NotificationClient.java`와 동일한 모양(서비스별 사본, `enabled=false` mock)을 donation에도 만들어 `completeDonation()`과 명예기부자 갱신 지점에 best-effort 호출만 추가하면 됨. 실패해도 기부 완료 자체를 막지 않도록 `NtsClient`처럼 예외를 삼키는 패턴을 그대로 따를 것.

**4. 동일 지자체 당일 중복기부 시도 경고**
- 위치: `donation/templates/donate.html`(폼 제출 전 confirm 단계)
- AS-IS 근거: `donation-main.html` 893행 — "선택하신 지자체에 오늘 기부를 시도하신 내역이 확인됩니다" confirm 다이얼로그
- 착수 방법: 폼 제출 직전 오늘 날짜+동일 사용자+동일 지자체 `REQUESTED/COMPLETED` 기부 존재 여부만 조회해 JS confirm 창 하나 추가하면 됨. 데이터 무결성엔 영향 없는 순수 UX 항목이라 후순위.

### 완전히 구현 확인됨 (요약)

일반기부·지정기부 신청/승인/취소 전체 플로우, 연간 기부한도(동적 금액+지자체별 한도), 세외수입 부과·수납 및 국세청 전자영수증 연계(mock), 기부확인증(카드뒤집기+인쇄), 세액공제 계산(2026 개정 요율로 AS-IS 버그까지 의도적으로 보정), 지자체별 기부혜택 텍스트, **명예기부자 등급 등록/변경/삭제**, 오프라인 기부(기탁서), 관심 지자체, 기부 취소 사유 기록 및 과오납/수납취소 상태관리(admin), 안내사항 guide1/2/3/5/6 전체(guide3는 Vue3로 이전되어 위치만 다름), 정책 3종, 행공센 주소조회 연계(mock) — AS-IS와 동등 수준으로 확인됨.

---

## 13. donation parity 감사

> 통합 전 파일: `docs/donation-parity-audit.md`

## donation 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS를 "서비스로직도 화면단도 똑같이" 재현하기 위한 **전수 갭 목록**. `[[asis-porting-principles]]` 방식대로 프론트 이벤트 + 서비스 검증 분기 + 매퍼를 대조한다. "발견되면 그때"가 아니라 **이 목록 기준으로** 구현한다. NEW/CHANGED(ISP·RFP) 요건은 표에 표시하고 **착수 전 사용자 확인**.
>
> 상태 범례: **O** 재현됨 / **X** 누락 / **부분** 일부만 / **확인** 추가 대조 필요. 작성 2026-09-21 (1차: 기부하기 플로우).

---

### 1. 기부하기 플로우 — 지자체 선택(@change) 시 검증

AS-IS `donation-main.html` `getLocgovInfo(locgovCode)` (지자체 드롭다운 선택 시 호출) + `/api/ngdonation/locGovInfo`.

| # | AS-IS 동작 | 트리거 | MSA 재현 | 상태 | 비고 |
|---|---|---|---|---|---|
| 1-1 | **본인 주민등록주소지 지자체 차단** — alert "자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다" | 지자체 선택 즉시(프론트) | `DonationService:290`에서 기부 신청 시 차단. 단 **선택 즉시 프론트 경고는 없음**(신청 눌러야 막힘) | **부분** | AS-IS는 선택 순간 막고 리셋. MSA는 제출 시점 차단 → UX 다름 |
| 1-2 | **하루 중복기부 확인** — `resultStatus=="dupl"`이면 confirm "선택하신 지자체에 오늘 기부를 시도하신 내역이 확인됩니다. 중복 기부를 방지하기 위해… 납부확인은 최대 3일…" (취소=뒤로가기) | 지자체 선택 | **없음** | **X** | **정본 조건**: `getTodayCntrListInfo(userId, locgovCode)` = 오늘 + 같은 지자체 + **부가정보(전자납부번호) 생성된 기부건** 존재. (25.08.01 KLID: 금일 생성 부가정보 리스트로만 판단) → MSA는 `donation.g_cntr` 중 `cntr_de=오늘·cntr_locgov_code=선택·user_id=본인`이고 `DonationLevy(전자납부번호)` 있는 건 존재 여부로 판정 |
| 1-3 | **기부 불가 기간(violtResn) 차단** — `lmttBgnDe~lmttEndDe` 사이면 alert "선택하신 지자체는 {violtResnCn}으로 {시작}부터 {종료}일까지 기부가 불가능합니다" + 리셋 | 지자체 선택 | **없음** | **X** | g_locgov의 violtResnCn/lmttBgnDe/lmttEndDe. 인천 영종(28155)·제물포(28125)·검단(28290)은 "18시~10시" 특수 문구 |
| 1-4 | 수납결과 확인 오류 시 alert "수납결과 확인중 오류가 발생했습니다" | locGovInfo ERROR | 없음 | **X** | 1-2 구현에 딸림 |
| 1-5 | 선택 지자체 포인트 지급률 표시 | 지자체 선택 | `point-rate` 조회 | **O** | |

### 2. 기부하기 플로우 — 주소확인/본인인증/금액

| # | AS-IS 동작 | MSA 재현 | 상태 | 비고 |
|---|---|---|---|---|
| 2-1 | **주소확인하기**(rsgstadresinfo) → 거주지 확인, "귀하는 {지역}에 납부가 가능합니다" | DonateView `verifyResidence` → `/api/donate/verify-residence`, "귀하는 …에 납부가 가능합니다" | **O** | |
| 2-2 | **본인인증 미완료 차단** — `userCntrInfo`에서 미인증 시 alert "본인인증 미완료상태입니다. 회원정보수정에서 본인인증을…" | 확인 필요(로컬은 `identity-verification-bypass:true`) | **확인** | MSA 본인인증 우회 설정과의 관계 정리 필요 |
| 2-3 | **거소지(거소지 지자체) 차단** — "자신의 거소지 지자체에는 기부를 하실 수 없습니다" (거소지 코드 36 시작 등) | 주소지만 차단, **거소지 차단 확인 필요** | **확인** | |
| 2-4 | 주민번호 입력·검증 (이름/주민번호 앞·뒤) | MSA는 CI 미수집 구조라 주민번호 입력 자체가 없음 | **확인** | 본인인증 방식 차이 — 외부연계 축소인지 확인 |
| 2-5 | 금액: 최소 100원, 100원 단위, 한도 초과 | DonateView:170-174 최소 100·100원단위, 서비스 연간한도 | **O** | 한도는 신청+완료 이중검증(`DonationService`) |
| 2-6 | 답례품 제공 여부(presentType) 선택 | DonateView 재현 | **O** | |

### 3. 연간 한도 / 신청·완료

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 3-1 | 연간 기부한도 검증(개인 연 2천만원 등) | 신청 시 + **완료 시점 재검증**(한도우회 방지) | **O** |
| 3-2 | 기부 신청 → 부가정보(전자납부번호) 생성 → 결제 | MSA는 REQUESTED 생성(PG 미개방으로 "결제완료 처리" 대체) | **부분(외부차단)** |

---

### 4. 이번 대조로 확정된 착수 대상 (기부하기 플로우)

전부 **순수 AS-IS 재현**(신규·변경 요건 아님 → 사용자 확인 불요, 제1원칙대로 동일 구현):

1. **[X→구현] 1-2 하루 중복기부 확인** — 지자체 선택 시 오늘·같은지자체·전자납부번호생성 건 존재하면 confirm 경고. MSA에 `GET /api/donate/today-duplicate?locgovCode=` 류 추가 + `DonateView.onLocgovChanged`에서 confirm.
2. **[X→구현] 1-3 기부불가기간(violtResn) 차단** — g_locgov의 violt/lmtt 필드 기반 차단 alert.
3. **[부분→보완] 1-1 주소지 차단을 선택 즉시 프론트 경고**로 (AS-IS와 동일 UX).
4. **[확인] 2-2 본인인증 / 2-3 거소지 / 2-4 주민번호** — 백엔드·설정 대조 후 재현 여부 판정.

> 데이터 의존(1-3 violtResn, 1-2 dupl)은 g_locgov·g_cntr 실데이터가 있어야 화면 검증 가능.

---

### 5. 지정기부(designated) 신청 검증

AS-IS `designated-donation/details.html` + MSA `DesignatedDetailView.vue` / `DonationService.donateToDesignatedProject`.

| # | AS-IS 동작 | MSA 재현 | 상태 |
|---|---|---|---|
| 5-1 | "기부가능한 상태가 아닙니다" / "진행 중 상태만 기부 가능합니다" | `DonationService:310` "현재 기부를 받고 있지 않은 사업입니다" | **O(문구차)** |
| 5-2 | "시작일 이전에 기부가 불가능합니다" / "종료일 이후에 기부가 불가능합니다" | `DonationService:313-315` 사업기간(bgngYmd~endYmd) 검증 | **O(문구차)** |
| 5-3 | **"목표 금액이 달성되어 기부가 불가능합니다"** — 목표액 도달 시 차단 | **확인** (MSA에 목표금액 달성 차단 있는지 대조 필요) | **확인** |
| 5-4 | 응원메시지 30자 제한 "30자 까지 입력가능합니다" | 확인 | **확인** |
| 5-5 | **지정기부 완료 시 취소 불가** | 확인 (MSA cancelDonation이 지정기부 구분하는지) | **확인** |

### 6. 기탁/오프라인 기부(offgive) 등록 검증

AS-IS `OffgiveServiceImpl` + MSA `OfflineDonationView.vue` / `DonationService.registerOfflineDonation`.

| # | AS-IS 동작 | MSA 재현 | 상태 |
|---|---|---|---|
| 6-1 | **"기 신고한 기부정보(기부한도 사용)가 있어 추가 등록할 수 없습니다. 신고일 익일 자정에 초기화"** — 기탁 중복 신고 차단 | **확인** (MSA registerOfflineDonation에 기신고 차단 있는지) | **확인** |
| 6-2 | "기부 금액 단위는 100원 단위입니다" | 확인 | **확인** |
| 6-3 | 사용자 기부 한도 체크 | 연간한도 검증 재사용(주석상 O) | **O** |

### 7. 아직 전수 안 한 donation 흐름 (계속 목록화 예정)

- 영수증/기부확인증 발급·재발급, 국세청(NtsClient) 연계 검증
- 명예기부자/등급 산정, 기부혜택증
- 마이페이지 기부내역/기부포인트/기부확인증(→ 별도 `point`/마이페이지 대조와 겹침)
- 빠른기부/논현금기부(quick/ng) — AS-IS 존재하나 MSA 미대응(의도적 축소 여부 확인)

### 8. 영수증/기부확인증 · 명예기부자

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 8-1 | 기부확인증: "선택한 기부내역이 없습니다" (선택 후 출력) | ReceiptListView 선택검증 | **확인(경미)** |
| 8-2 | 영수증출력: 로그인 필요 / "영수증 정보가 없습니다" | requiresAuth + 404 | **O(부분)** |
| 8-3 | 명예기부자/혜택증: 서버 errMsg 표시 | HonorCertificatesView | **확인(경미)** |

---

### 9. ★ donation 최종 갭 목록 (확정 — 구현 단계에서 이 목록대로)

MSA 백엔드 대조로 확정. **전부 순수 AS-IS 재현**(신규·변경 요건 아님 → 사용자 확인 불요).

| 우선 | 갭 | 상태 | 구현 요지 |
|---|---|---|---|
| **높음** | ~~하루 중복기부 확인(1-2)~~ | **✅완료(2026-09-21)** | `GET /api/donate/today-duplicate` + `DonationService.hasTodayDonation`(오늘 cntrDe·같은지자체·비취소건) + `DonateView.onLocgovChanged` confirm(취소=선택리셋). AS-IS는 차단 아닌 경고(확인 시 진행) |
| **높음** | ~~기부불가기간(violtResn) 차단(1-3)~~ | **✅완료(2026-09-21)** | g_locgov에 `violt_resn_cn/lmtt_bgn_de/lmtt_end_de` 컬럼+엔티티 추가, `donationRestrictionMessage`(오늘이 제한기간이면 안내, 인천3구 특수문구), 엔드포인트+프론트 alert+리셋, createGeneralDonation 서버가드. **로컬 데이터 없어 미발동(재현 배치 완료)** |
| 중 | ~~주소지 차단 선택 즉시 경고(1-1)~~ | **✅완료(2026-09-21)** | `knownResidenceLocgov` 유지 → 확인된 거주지 (재)선택 시 즉시 alert+리셋. 제출·주소확인 시점 차단은 기존 유지 |
| 중 | ~~지정기부 목표금액 달성 차단(5-3)~~ | **✅완료(2026-09-21)** | `createDesignatedDonation`에 `raisedAmount≥goalAmt` 차단 "목표 금액이 달성되어 기부가 불가능합니다" |
| 중 | **지정기부 목표금액 달성 차단**(5-3) | **X(순수재현)** | AS-IS: prjStatus=2여도 달성률 rateAmt(누적기부액/목표액×100)≥100%면 "목표 금액이 달성되어 기부가 불가능합니다". MSA `createDesignatedDonation`은 상태·기간만 검증 → 달성률 계산+차단 추가 |
| ~~중~~ | ~~거소지 지자체 차단(2-3)~~ | **재분류→축소** | **주소지 차단의 외국인 버전.** 외국인 거소신고 주소를 `rsgstadresinfoForeigner`(외국인 거소정보 **외부조회**)로 받아 같은 지자체면 차단(체류만료도). **외부연계 축소** — 개방 시 재검토(순수재현 아님) |
| ~~중~~ | ~~기탁 기신고 중복 차단(6-1)~~ | **✅완료(2026-09-21) — 한도검증 대기포함** | AS-IS `getGCntrSumCntrAmt`는 상태무관(삭제제외)으로 **올해 대기+완료 전체**를 한도사용액으로 합산(신청만 해도 즉시 잡힘). MSA는 완료건만 셌음 → **`validateAnnualLimit`을 취소아닌(REQUESTED+COMPLETED) 합산으로 수정**, 완료 시점 재검증은 `excludeCntrSn`으로 자기건 제외(이중집계 방지). 일반/지정/기탁 모든 신청 경로에 적용. "익일 자정 초기화" 문구는 실제 연간계산과 무관한 레거시 안내 |
| 낮음 | ~~지정기부 응원메시지 30자 제한(5-4)~~ | **✅완료(2026-09-22)** | MSA가 100자로 지어놨던 것(verbatim 위반)을 AS-IS 30자로 정합: DonateView maxlength 100→30·placeholder "30자"·제출검증 alert "30자 까지 입력가능합니다."(details.html:736), 서버 truncate 100→30. (AS-IS는 기부내역 탭 인라인편집 saveCheerMsg, MSA는 제출시점 수집 — flow는 문서화된 적응) |
| — | ~~지정기부 완료취소 불가(5-5)~~ | **갭 아님(2026-09-22)** | AS-IS 원문이 `"...취소 안되용~ 테스트 문구"`(details.html:206) — 명백한 테스트 잔재. MSA가 완료취소 허용이 맞음 |
| — | quick 빠른기부(donation/quick-donation.html) | **미사용 시안 → 보류+기록** | AS-IS 소스 주석(2026-09-14): "빠른기부하기(예시) 시안, 메뉴·헤더·푸터·배너·팝업 어디에도 미연결, 동작 안 함, 기능수정·신규구현 대상 아님. 실제 기부는 donation-main.html". MSA 미구현이 맞음 |
| — | 본인인증 미완료 차단(2-2)/주민번호(2-4) | **축소** | MSA는 CI 미수집·`identity-verification-bypass`(외부연계 축소). **외부연계 개방 시** 재검토 — 물리차단이라 사용자 확인 대상 |

**재현 확인된 것(O)**: 주소지 차단(제출시점), 사업상태·기간, 100원단위·금액검증, 연간한도(신청+완료 이중), 포인트율, 영수증출력, 답례품제공여부.

> **donation audit 완료(1차).** 구현은 "전 서비스 목록화 완료 후" 시작(방식 [[asis-porting-principles]]). 다음: member 전수조사.

---

## 14. gift 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-gift.md`

## AS-IS 기능감사 — gift(답례품) 서비스

조사범위: `ghlove-web/src/main/java/saleson/shop/*`(opmanager 백엔드, saleson 커스터마이징), `ghlove-api/src/main/java/saleson/api/*`(공개 API 계층), `ghlove-frontend/*`(고객 화면), 대비 `ghlove-msa/gift`, `admin`, `order`, `storefront`.

saleson은 상용 쇼핑몰 엔진(`libs/saleson-license-*.jar`)이고 실제 커스터마이징 소스는 `ghlove-web`(opmanager 관리자 콘솔 + 판매자 콘솔)과 `ghlove-api`(공개 REST API)에 있다. 도메인 모델 상당수는 `ghlove-common`에 공유되어 있다.

### AS-IS 기능 전수 목록

| 기능 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 답례품(Item) 등록/승인/반려/노출/재고 | `ghlove-web/.../shop/item/ItemManagerController.java`, `ghlove-api/.../item/ItemController.java` | **완료** | `gift/GiftController`, `GiftService` |
| 답례품 상세/목록/카탈로그 조회 API | `ghlove-api/.../item/ItemController.java`, `ItemDataSupport.java` | **완료** | `GiftApiController`, `GiftPublicApiController` |
| 답례품 재고 0 자동품절/판매중지/재고조정 | `ItemManagerController`, `SellerItemController` | **완료** | `GiftService`(stop/stock), `GiftOrderStock` SAGA 참여 |
| 리뷰(작성/이미지/평점) | `shop/review/ReviewController.java` | **완료** | `Review`, `ReviewImage`, `ReviewService` |
| 문의(1:1, 상품별) | `shop/qna/QnaController.java`(`/qna`,`/inquiry`), `QnaItemManagerController` | **완료** | `Inquiry`, `InquiryService` |
| 찜(위시리스트) | (saleson 표준기능, mypage 연계) | **완료** | `Wishlist`, `WishlistService` |
| 썸네일 자동생성(소/중/대) | `saleson.common.thumbnail` | **완료** | `ThumbnailService` |
| 대표상품관리(`REPRESENTATIVE_ITEM_YN`) | (view_reimplementation_round에서 확인) | **완료** | `GiftController` `/admin/representative-items` |
| 브랜드 관리(지자체별 답례품 브랜드) | `shop/brand/BrandManagerController.java` | **부분구현** | 관리자 CRUD(`BrandApiController`)만 존재, **고객용 공개 브랜드 페이지 없음** |
| 제철식품관(공개 노출) | `shop/seasonalfood/SeasonalFoodManagerController.java` | **부분구현** | `SeasonFoodItem`으로 공개 목록(`/seasonal`)은 됨. **관리자가 월별/키워드로 직접 지정하는 CRUD 없음**(TO-BE는 배치가 채운다고 가정, AS-IS는 수동 큐레이션 화면) |
| 특산물관(`G_SPCL_ITEM*`) | `shop/specialityitem/SpecialityItemManagerController.java` | **의도적 제외 확인됨** | Controller는 있으나 대응 Service 클래스 자체가 소스에 없음(껍데기). 기존 메모(`dormant-saleson-boilerplate-tables`)에서 saleson 미사용 보일러플레이트로 이미 확인됨. 재확인 완료 |
| 사은품(GiftItem)/사은품그룹(GiftGroup) — 주문시 증정품 | `shop/giftitem/*`, `shop/giftgroup/*` | **의도적 제외 확인됨** | `GiftItemManagerController`/`GiftItemController`/`GiftItemMobileController` 전체 메서드가 주석처리(죽은 코드). 유일하게 살아있던 `GiftGroup` CRUD도 참조하는 쪽(`OrderManagerController` 등)에서 전부 주석처리되어 실사용 0건. AS-IS 자체 죽은 코드로 재확인 |
| **상품 옵션(단일/2단/3단 선택형, 텍스트옵션(각인), 옵션별 가격/재고, 추가구성상품)** | `ghlove-frontend/items/details-main.html`(itemOptionType S/S2/S3, itemTextOptionFlag, addOptionList) | **미구현** | Gift 도메인에 옵션 개념 자체가 없음. `storefront/.../GiftDetailView.vue` 주석에 "스코프 밖"이라 적혀있으나 AS-IS에서 활발히 쓰이는 살아있는 기능이라 스킵 근거가 되지 않음(표준규칙 위반) |
| **배송비 정책(출고지/기본배송비/무료배송기준/도서산간 추가배송비)** | `shop/shipment/Shipment*Controller.java`, `saleson.shop.shipment.domain.Shipment`(ghlove-common) | **미구현** | `order` 서비스 Java 소스 전체에 shipping 관련 코드 0건. cart/checkout 템플릿엔 "무료배송" 정적 텍스트만 있고 실제 계산로직 없음 |
| **도서산간 우편번호 마스터(배송비 자동판정)** | `shop/island/Island*Controller.java` | **미구현** | 배송비 정책과 연동되는 우편번호→도서산간 판정 테이블/화면 전체 없음 |
| 반품/교환 배송비 정책 | `shop/shipmentreturn/ShipmentReturn*Controller.java` | **미구현** | 위 배송비 정책과 동일 계열, 없음 |
| **판매자(제공자) 자체 로그인/셀프서비스 콘솔** | `shop/seller/SellerController.java`(자체 로그인/세션/비번변경/PKI인증서), `shop/seller/mall/MallController.java`(`/mall/{sellerLoginId}` 미니몰+qna+review 탭) | **미구현** | TO-BE는 provider가 member 서비스 JWT(PROVIDER 롤)로 로그인 — 이 자체는 합리적 단순화. 그러나 미니몰 공개페이지, 판매자 공지사항(`SellerISysNoticeController`), PKI 인증서 로그인은 대응 기능 없음 |
| 판매자 하위 직원 계정관리 | `shop/seller/user/SellerUserController.java` | **미구현** | 제공사 조직 내 담당자 여러 명을 등록/관리하는 기능. member 서비스에 대응 개념 없음(계정=1인 1롤) |
| **상품 수정신청 승인 워크플로우** | `shop/seller/item/SellerItemController.java`(`sale-edit` — 수정요청 큐, 지자체/본사 승인 후 반영) | **미구현** | `GiftController.edit()`는 즉시 반영(승인절차 없음). AS-IS는 최초 등록만 승인필요, 이후 가격/내용 변경은 별도 재승인 큐를 거침 |
| 판매자→지자체 담당자 문의 | `shop/qnaadmin/QnaLocgovManagerController.java`(`/seller/qna-locgov`), `QnaAdminManagerController.java`(`/opmanager/qna-admin`) | **미구현** | `admin`의 `QnaAdminController`(`/qna-admin`)는 이름이 같지만 실은 별개 기능(일반 회원 1:1문의 관리자 답변 화면, `OP_QNA` 기반). AS-IS `QNA_ADMIN` 테이블 기반의 "제공자↔지자체 문의"는 대응 없음 |
| 재입고 알림 신청 | `shop/restocknotice/RestockNoticeController.java` | **미구현** | 품절 상품에 대해 회원이 SMS 재입고알림을 신청하는 기능, 대응 없음 |
| **입점문의(신규 판매처 지원서)** | `shop/storeinquiry/StoreInquiryController.java`(`/store-inquiry/inquiry`, 파일첨부) | **미구현** | 신규 제공자가 되고 싶은 업체가 공개 폼으로 지원하는 페이지, 대응 없음 |
| 판매처(오프라인 매장) 정보 관리 | `shop/store/StoreManagerController.java` | **미구현(저우선)** | `Store`(이름/주소/영업시간/storeType) 마스터 관리, 실사용 범위 불명확 — 후속 확인 필요 |
| 랭킹관리(관리자 큐레이션 TOP상품) | `shop/ranking/Ranking*Controller.java`(공개 `/ranking`, 관리 `/opmanager/ranking`) | **미구현** | 판매량 기반이 아닌 관리자가 직접 지정하는 카테고리별 랭킹 페이지 |
| 카테고리 관리(3단 트리)/카테고리 필터(속성 검색)/모바일 카테고리 편집/카테고리 팀그룹 | `shop/categories/CategoriesManagerController.java`, `categoriesfilter/*`, `mobilecategoriesedit/*`, `categoriesteamgroup/*` | **미구현** | `gift`는 `GIFT_CATEGORY` 공통코드 + `GiftSubcategory`(GNB 메뉴용, 필터링 미사용)로 단순화되어 있음. AS-IS의 계층형 카테고리 트리 관리, 카테고리별 속성 필터 관리, 모바일 전용 카테고리 편집 화면은 전부 없음 |
| 검색어 관리(자동완성/인기검색어/금칙어) | `shop/keyword/KeywordController.java`, `shop/search/SearchManagerController.java` | **미구현** | 답례품 카탈로그 자체 검색의 자동완성/인기검색어/금칙어 관리. (참고: round11에서 스킵한 "통합검색"은 이것과 다른 사이트 전체검색 기능이며 AS-IS에서도 죽은코드였음 — 이 항목은 그것과 무관한 별개 기능이며 살아있음) |
| 상품 복제등록 | `shop/item/ItemManagerController.java`(`copy/{itemId}`) | **미구현(저우선)** | 기존 상품을 복사해서 새 상품 초안을 만드는 편의기능 |
| 다중 이미지 순서변경(드래그) | `ItemImage`에 `ordering` 컬럼은 있음 | **미구현(저우선)** | 값은 있으나 등록/수정 화면에 순서 재배치 UI 없음(업로드 순서 고정) |
| 리뷰/QnA 신고·숨김(관리자 모더레이션) | (AS-IS에 opmanager 화면 있음) | **의도적 제외 확인됨** | 기존 메모에 명시된 스킵결정, 재확인만(이번 라운드에서 별도 조사 안함) |
| 쿠폰 | `shop/coupon/*` | **완료(order 서비스 소관)** | `coupon_subsystem_round` 메모 |
| 이벤트/기획전(공지성 GNB)/팝업 배너 | `shop/event/*`, `shop/popup/*` | **완료(admin 콘솔 소관)** | `gnb_expansion_round`, `admin_console_real_asis_markup_round`에서 처리 확인(`admin/templates/content/popup-*.html` 등) |

### Gap 상세

#### 높음(HIGH) — 핵심 커머스 로직, 다수 화면/데이터에 영향

1. **상품 옵션 시스템 부재**: 단일/2단/3단 선택형 옵션(옵션별 가격·재고), 텍스트옵션(각인문구 등 최대 3개), 추가구성상품(add-on) 전체가 Gift 도메인에 없다. `Gift` 엔티티가 단일 SKU/단일 가격/단일 재고만 가정하고 있어, 옵션을 추가하려면 `Gift`, 장바구니(`order`), 주문라인, 재고관리(`GiftOrderStock`), 등록/수정 화면(`register.html`/`edit.html`), 상세화면(`GiftDetailView.vue`)까지 전부 손대야 한다. `storefront/src/views/gift/GiftDetailView.vue` 상단 주석에 "스코프 밖"이라 명시돼 있지만 이는 AS-IS 죽은코드가 아니라 실사용 기능이므로 표준 스킵기준(AS-IS 자체 죽은코드 확인)을 충족하지 못한다.
2. **배송비 계산 로직 전체 부재**: `order` 서비스 Java 소스에 shipping 관련 코드가 0건이다. AS-IS는 판매자별 출고지/기본배송비/무료배송기준(`Shipment`), 반품배송비 정책(`ShipmentReturn`), 도서산간 우편번호 판정(`Island`)을 조합해 배송비를 계산한다. TO-BE는 cart/checkout 템플릿에 "무료배송" 정적 텍스트만 있다. 관리자가 배송비 정책을 설정할 화면도, 체크아웃 시 실제 배송비를 더하는 로직도 없다.
3. **판매자 자체 셀프서비스 콘솔 부재**: AS-IS는 판매자 전용 로그인/세션(`SellerController`), 미니몰 공개페이지(`/mall/{sellerLoginId}` — 자체 QnA/리뷰 탭 포함), 하위직원 계정관리(`SellerUserController`), 판매자 공지사항(`SellerISysNoticeController`), 상품 수정신청 승인워크플로우(`SellerItemController` sale-edit)를 갖춘 별도 콘솔이다. TO-BE는 member 서비스의 PROVIDER 롤 하나로 이를 대체했는데(합리적 단순화), 그 결과 미니몰 공개페이지·하위직원 관리·수정재승인 워크플로우가 통째로 빠졌다. 특히 수정재승인 워크플로우 부재는 실제 운영상 리스크(제공자가 승인 없이 가격을 즉시 바꿀 수 있음)로 이어질 수 있다.

#### 중간(MEDIUM)

4. **입점문의(신규 판매처 지원서) 부재**: `/store-inquiry/inquiry` 공개 폼(파일첨부 포함)이 대응 없이 완전히 빠져 있다.
5. **판매자→지자체 담당자 문의 부재**: `QNA_ADMIN` 기반의 제공자-지자체 소통채널(`/seller/qna-locgov`, `/opmanager/qna-admin`)이 없다. admin의 `/qna-admin`은 이름만 같은 별개 기능(일반회원 1:1문의)이라 혼동 주의.
6. **제철식품관 관리자 수동 큐레이션 부재**: 공개 노출(`SeasonFoodItem`)은 되지만, AS-IS처럼 관리자가 월별로 상품을 검색해 지정하는 CRUD 화면이 없다(TO-BE는 배치가 채운다고 가정하나 실제 AS-IS는 수동 큐레이션).
7. **랭킹관리(관리자 큐레이션 TOP상품) 부재**: 카테고리별 "랭킹" 공개 페이지 및 관리자 CRUD가 없다.
8. **고급 카테고리 관리 부재**: 계층형 카테고리 트리 관리, 카테고리별 속성 필터 관리, 모바일 전용 카테고리 편집이 없다(TO-BE는 공통코드 기반 단순 대분류만).
9. **검색어 관리(자동완성/인기검색어/금칙어) 부재**: 답례품 카탈로그 자체 검색용 자동완성 API, 인기검색어, 금칙어 관리가 없다.
10. **재입고 알림 신청 부재**: 품절상품 SMS 알림 신청 기능이 없다.
11. **브랜드 공개페이지 부재**: `Brand`는 관리자 CRUD만 있고 고객이 브랜드별로 상품을 모아볼 수 있는 공개 페이지가 없다.

#### 낮음(LOW)

12. 상품 복제등록(기존 상품 복사해서 신규 초안 생성) 편의기능 없음.
13. 다중 이미지 순서변경 UI 없음(`ordering` 컬럼은 존재).
14. 판매처(오프라인 매장) 정보 관리(`Store`) 대응 없음 — 실사용 범위 불명확, 후속 확인 필요.

### 완전히 구현 확인됨

답례품 등록·승인·반려·노출·재고관리, 재고 0 자동품절, 리뷰(이미지 포함), 상품별 1:1문의, 찜(위시리스트), 썸네일 자동생성, 대표상품관리, 브랜드 관리자 CRUD, 판매자 관리자 CRUD(기본), 쿠폰(order), 이벤트/기획전/팝업배너(admin), 제철식품관 공개노출, order SAGA 참여(재고예약/복원), 답례품몰 Vue3 프론트(목록/상세/리뷰/QnA/찜/마이페이지). 사은품(GiftItem/GiftGroup), 특산물관(G_SPCL_ITEM)은 AS-IS 자체 죽은 코드로 재확인되어 스킵이 타당함.

---

## 15. gift 옵션 체계 parity

> 통합 전 파일: `docs/gift-option-parity-audit.md`

## gift 옵션(다형 옵션 체계) AS-IS 전수대조 갭목록

> 방법: [[asis-porting-principles]] — 프론트 이벤트 + 검증로직 + 매퍼를 전수로 훑어
> AS-IS 능력을 먼저 확정하고, 실사용 여부와 MSA 갭을 판정한다. ISP/RFP 신규·변경 판단은
> 사용자 확인 후 착수. 작성 2026-09-22.

### 0. 결론 요약

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

### 0-1. 판매자 등록화면 실측 (결정적, 2026-09-22 추가)

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

### 1. AS-IS 옵션 데이터 모델 (전수)

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

### 2. 프론트 이벤트 전수 (item_tab-ali.vue = 답례품 상세 탭, details-main.html)

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

### 3. 검증로직 전수

| # | 검증 | 메시지/동작 | 근거 |
|---|---|---|---|
| V1 | 필수옵션 미선택 차단 | "답례품 필수옵션을 선택하세요." (장바구니/즉시구매 진입 시) | item_tab-ali.vue:1081,1122 |
| V2 | 텍스트옵션(각인) 미입력 차단 | "{optionName1}의 옵션을 입력해주세요." | :747 |
| V3 | 옵션 품절 차단 | `optionSoldOuts[index]` → `:disabled`, [품절] 표기 | :386,393,413,420 |
| V4 | 옵션 재고연동 표기 | `optionStockFlag==='Y' && optionStockQuantity>0` → "재고 N개" | :396,423 |
| V5 | 옵션 추가금액 합산 | `optionPrices[index]>0` → "+금액" 표기 및 결제금액 반영 | :390,417 |
| V6 | 옵션 중복 조합 제거 | `checkDuplication`으로 상위 값 중복 축약 | :974~992 |

---

### 4. 매퍼 전수 (읽기 vs 쓰기 구분이 핵심)

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

### 5. MSA 현행 대응

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

### 6. 갭목록 (AS-IS ↔ MSA)

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

### 7. 실사용 판정 (핵심)

- **코드**: S/S2/S3/T/추가구성 전부 end-to-end 살아있음 (§2·§3·§4 읽기·구매흐름).
- **인벤토리**: "미사용"은 매퍼 고아 한정, 옵션 기능 전체 사장 아님 (§4 정정).
- **로컬 데이터**: 옵션 3행 전부 S·단일 데모아이템, 실아이템 23개 옵션플래그 공백, 추가구성 0행
  → **테스트 시드로 판단, 운영 실사용 근거로 쓸 수 없음**.
- **RFP**: SFR-005가 "옵션" 카탈로그 관리를 명시 → 옵션 관리 자체는 요건.

→ **판정: 옵션 관리·단일옵션 구매반영(G1/G2)은 요건으로 확정 진행 가능. S2/S3/T/추가구성/옵션단위
재고(G3~G6,G11,G12)는 "AS-IS 운영에서 실제 사용됐는지"를 운영 데이터로 확인해야 신규/축소를 결정**한다.

---

### 8. 사용자 확인 필요 (착수 전 결정)

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

### 9. 다음 액션 (승인 후)

- [ ] 사용자 답변(§8) 수령
- [ ] G2/G9 배선: GiftDetailView 옵션선택 → 장바구니 optionId → checkout OrderItem optionName/optionPrice/추가금액 반영 + V1 필수선택 검증 이식
- [ ] (요건 확정 시) S2/S3/T/추가구성 순차 확장 — 각 단계 프론트(F3~F6)·검증(V2)·데이터모델 이식

---

## 16. gift 카테고리 3단 트리 parity

> 통합 전 파일: `docs/gift-category-tree-parity-audit.md`

## 카테고리 3단 트리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[asis-porting-principles]]. 신규 기준 적용: [[asis-porting-principles]]
(SalesOn 종속·미사용은 기록만 하고 보류, 단 이미 구현된 건 유지).

### 1. AS-IS 카테고리 계통 (두 갈래)

**A. 고향사랑 답례품 카테고리 (실사용)**
- 대분류: `GIFT_CATEGORY` 공통코드 — AGRI/SEAFOOD/LIVING/PROCESSED/VOUCHER.
- 답례품 헤더 `ghlove-frontend/components/layouts/header_g.vue`: 대분류(groups) > 중분류(topCategory) > 3단(childCategories) 3단 메가메뉴.
  - 대분류 클릭 `categoryGroupLink(url)` → `/goods/searchGoods.html?group=<url>&type=C`.
  - 중분류·3단 클릭 `categoryLink(url)` → `/goods/searchGoods.html?category=<url>&type=C`.

**B. SalesOn 원제품 카테고리 트리 (원제품 잔재)**
- `saleson.shop.categories`(Category 재귀 parent/child) + `categoriesedit` CRUD, 테이블 `OP_CATEGORY`(category_class1~4 + category_level = 최대 4단), 매핑 `OP_ITEM_CATEGORY`.
- 관리자 CRUD: `opmanager/categories/{create,edit,list}.jsp`, `categories-team-group`.

### 2. 실사용 판정 (MSA DB)

| 대상 | 데이터 | 판정 |
|---|---|---|
| 대분류 GIFT_CATEGORY | 26품목(AGRI 15·SEAFOOD 5·LIVING 2·PROCESSED 2·VOUCHER 2) | ✅ 실사용 |
| 중분류 gift_subcategory | 51건 | ✅ 실사용 |
| 3단 gift_subcategory_item | 40건(이름만, item 링크 없음) | ✅ 실사용(표시용 라벨) |
| **SalesOn op_category (4단)** | **0건** | SalesOn 종속·미사용 |
| **op_item_category (품목↔카테고리)** | **0건** | SalesOn 종속·미사용 |
| op_category_team_item | 2건 | (팀그룹, 별도) |

- AS-IS header_g.vue의 중분류/3단 `?category=` 필터는 `OP_ITEM_CATEGORY`(품목↔카테고리) 의존인데 MSA DB 0건 → **SalesOn 종속·현재 미사용**.

### 3. TO-BE(MSA) 현재 상태 — 이미 구현됨

- **모델**: `GiftSubcategory`(gift_subcategory) + `GiftSubcategoryItem`(gift_subcategory_item), 대분류=`Gift.categoryCode`(GIFT_CATEGORY).
- **서비스**: `GiftService.categoryTree()` — 대분류 > 중분류 > 3단 트리 조립.
- **스토어프론트**: `AppHeaderShopping.vue` 3단 메가메뉴(top/middle/detail_category). 대분류 클릭 → `categoryCode` 필터, 3단 품목명 클릭 → 검색어(q). (`GiftListView`도 categoryCode·q 지원)
- **관리자 CRUD**: gift `CategoryAdminApiController`(subcategory GET/POST/PUT/delete) + admin `CategoryAdminController`·`gift-categories/subcategory-form.html`, `CategoryTeamAdminApiController`·category-teams.

→ **대분류+중분류+3단 트리, 렌더링, 관리자 CRUD가 모두 이미 구현되어 있다.**

### 4. 갭목록

| # | 갭 | AS-IS | MSA | 판정 |
|---|---|---|---|---|
| C1 | 중분류/3단 단위 필터 | `?category=<url>` 필터(OP_ITEM_CATEGORY 의존) | 대분류=필터, 3단=검색어 | **SalesOn 종속·미사용(op_item_category 0건) → 보류+기록** |
| C2 | SalesOn 4단 트리(category_class1~4) + CRUD | opmanager/categories | op_category 0건, MSA 미구현 | **SalesOn 종속·미사용 → 보류+기록** |
| C3 | 품목↔중분류/3단 매핑 | OP_ITEM_CATEGORY | 없음(대분류만) | **SalesOn 종속·미사용 → 보류+기록** |

### 5. 결론 / 권고

- **live 답례품 카테고리 3단 트리(대분류 GIFT_CATEGORY + 중분류/3단 gift_subcategory 메가메뉴 + 관리자 CRUD)는 MSA에 이미 구현 완료** → [[asis-porting-principles]]대로 그대로 둔다.
- **C1~C3(SalesOn op_category 4단 트리 · op_item_category 필터 · 품목↔중분류 매핑)은 SalesOn 종속·현재 미사용(DB 0건)** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "카테고리 3단 트리"는 **추가 구현 없이 마감**하고 다음 항목(검색어 관리)로 이동 권고. — 최종 결정은 사용자.

---

## 17. gift 검색어 관리 parity

> 통합 전 파일: `docs/gift-search-keyword-parity-audit.md`

## 검색어 관리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[asis-porting-principles]] + [[asis-porting-principles]].

### 1. AS-IS 검색 계통 (여러 갈래)

- **추천검색어**: `OP_SEARCH` + `SearchManagerController`(opmanager/search). 검색어·링크·노출기간 관리.
- **인기검색어/자동완성/일별집계**: `keyword-mapper.xml`(getBestKeyword·getAutoComplete·setDailyKeyword·clearDailyKeyword), `saleson.shop.keyword`.
- **금지어**: `banword-mapper.xml`.
- **통합검색**: `saleson.shop.integrationsearch`(외부 SearchEngine 연동, SearchApiResponse 등).
- **최근검색어**: `totalsearch-myrecent-mapper.xml`(클라이언트 로컬).

프론트 표기 — 답례품 헤더 `ghlove-frontend/components/layouts/header_g.vue`:
- 검색은 **단순 텍스트 입력** → `goSearchGoods({type:'T', keyword})`. 인기/추천/자동완성 UI 없음.
- `recommendSearch: {}`·`searchWord: ''` 데이터가 선언돼 있으나 **템플릿·메서드 어디에도 바인딩 안 됨 = 죽은 데이터** → 추천검색어 미노출.

### 2. 실사용 판정 (MSA DB / 소스)

| 대상 | 상태 | 판정 |
|---|---|---|
| 답례품 검색(키워드 q) | storefront GiftListView q → gift publicGifts(keyword) | ✅ 실사용 |
| 추천검색어 OP_SEARCH | admin.op_search **0건**, 프론트 미노출(AS-IS도 죽은 데이터) | 관리 기능만 존재 |
| 인기검색어/자동완성/일별집계 | SalesOn keyword system, 프론트 미노출 | SalesOn 종속·미사용 |
| 금지어 banword | SalesOn | SalesOn 종속·미사용 |
| 통합검색 integrationsearch | 외부 SearchEngine 연동 | SalesOn/외부인프라 종속·미사용 |

### 3. TO-BE(MSA) 현재 상태

- **답례품 검색**: storefront `AppHeaderShopping.vue`·`HomeView.vue` 검색박스 → `/gifts?q=`. gift `publicGifts(categoryCode, keyword)` 필터. ✅ 이미 동작.
- **추천검색어 관리**: `admin SearchKeyword`(OP_SEARCH) + `SearchKeywordAdminController`(list/new/edit/create/update/delete) + `search-admin/{list,form}.html`. **관리자 CRUD 이미 구현**. 프론트 미노출(= AS-IS와 동일).
- **인기검색어/자동완성/금지어/통합검색**: MSA 미구현.

### 4. 갭목록

| # | 갭 | 판정 |
|---|---|---|
| S1 | 추천검색어 프론트 노출 | AS-IS도 미노출(recommendSearch 죽은 데이터) → **노출 안 하는 게 parity**. admin CRUD는 이미 구현·유지 |
| S2 | 인기검색어/자동완성/일별집계 | SalesOn 종속·프론트 미노출·MSA 미구현 → **보류+기록** |
| S3 | 금지어(banword) | SalesOn 종속·MSA 미구현 → **보류+기록** |
| S4 | 통합검색(외부 SearchEngine) | SalesOn/외부 인프라 종속·MSA 미구현 → **보류+기록** |

### 5. 결론 / 권고

- **답례품 키워드 검색(q 필터)과 추천검색어 관리자 CRUD(OP_SEARCH)는 MSA에 이미 구현 완료** → 유지([[asis-porting-principles]]: 이미 구현된 건 그대로 둠). 추천검색어 프론트 미노출도 AS-IS와 일치.
- **S2~S4(인기검색어·자동완성·일별집계·금지어·통합검색)는 SalesOn/외부 인프라 종속·현재 미사용** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "검색어 관리"는 **추가 구현 없이 마감** 권고. — 최종 결정은 사용자.

---

## 18. gift 배송비 정책 parity

> 통합 전 파일: `docs/gift-delivery-fee-parity-audit.md`

## 배송비 정책 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[asis-porting-principles]] (프론트이벤트+검증로직+매퍼 전수 → 갭목록 →
실데이터로 실사용 확인 → 사용자와 범위 결정). 정본은 AS-IS 소스.

### 1. AS-IS 배송정책 모델 (정본)

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

### 2. 실사용 판정 (gift DB `public.op_item` 실데이터 26건)

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

### 3. TO-BE(MSA) 현재 상태 대조

- **화면**: `storefront/.../GiftDetailView.vue` L424/L602 = "배송비 : 무료" 하드코딩 → **AS-IS와 동일(parity ✅)**.
- **서버 계산**: `order OrderService#deliveryFeeOf()` 이미 존재 —
  - type1 무료 ✅ / type6 고정 ✅ / type2·3·4 조건부무료(lineTotal 기준) ✅ / type5 base×quantity(부분) / 제주·도서산간 주소문자열 판정(부분).
  - cart(`CartService`)·checkout preview·shipment까지 배선 완료.

### 4. 갭목록 (AS-IS엔 있으나 MSA 미/부분 구현) — 전부 실데이터 0건

| # | 갭 | AS-IS | MSA | 실데이터 |
|---|---|---|---|---|
| G1 | 묶음배송 그룹핑 | shippingGroupCode 묶어 그룹당 1배송비 | 라인별 개별 계산 | 0건 |
| G2 | type2/3 구분·물류통합 합산 | shipmentGroupCode 전체 합산 | lineTotal 단독 | 0건 |
| G3 | 개당배송비 BOX수량 | ceil(수량/itemCount)×fee | base×quantity (itemCount 무시) | 0건 |
| G4 | 제주/도서산간 정식 판정 | Island 판정표(IslandRepository) | 주소 문자열 키워드 5개 | 0건 |
| G5 | 착불 payShipping=0 | shippingPaymentType=2 | 없음 | 0건 |
| G6 | 반품/교환 배송비 | shippingReturn (클레임) | 없음 | 0건 |
| G7 | 본사/업체 배송 정산배분 | deliveryType로 sellerId 배정 | 정산 미구현 영역 | 0건 |

### 5-1. 구현 결과 (2026-09-22, 사용자 결정: G1~G7 전부 구현)

배송비 계산 전체를 AS-IS `Shipping.getShippingGroups()` verbatim으로 이식했다.

- **신규**: `order DeliveryFeeCalculator`(묶음배송 그룹핑 + type1~6 + 제주/도서산간 + 착불 분기 전부).
- **G1 묶음배송**: shippingGroupCode 같은 것끼리 그룹당 1배송비(type1·6은 개별). 그룹 배송비는 그룹 첫 라인에 배분(합계는 AS-IS와 동일). ✅
- **G2 type2/3 + 물류통합**: type3+shipmentGroupCode면 물류그룹 전체 합계로 조건부무료 판정. ✅
- **G3 개당배송비**: `boxCount = ceil(총수량/shippingItemCount)` × (shipping+추가). ✅
- **G4 제주/도서산간 정식판정**: AS-IS OP_ISLAND를 `ord.op_island`로 포팅(엔티티 `Island`+`IslandRepository`, 우편번호 REPLACE 매칭 ORDER BY id DESC LIMIT 1). 체크아웃이 우편번호(form.post)를 preview/complete로 전달. 초기 데이터 0건 → 추가배송비 미발생(AS-IS와 동일). 기존 주소문자열 휴리스틱은 제거. ✅
- **G5 착불**: shippingPaymentType=2 → payShipping 0 분기 이식. 답례품은 포인트 선결제라 데이터 원천 없음(코드 parity, 데이터로 비활성). ✅
- **필드 배선**: gift `Gift`(shippingItemCount/shippingGroupCode/shipmentGroupCode/shippingReturn/deliveryType) → `GiftItemInfoDto` → order `GiftItemInfo` 전 구간 추가. op_item에 컬럼 기존재라 DDL 불요(island 테이블만 신규 `migration-order-island.sql`).
- 배선: `CartService.buildGroups`(장바구니, islandType "")·`priceSelected`(체크아웃, 우편번호 판정) 모두 라인별→지자체 그룹별 계산으로 전환. 활성 경로 `checkoutMultiItem`가 PricedLine.deliveryFee 사용하므로 자동 반영. 컴파일·CartServiceTest·storefront build OK.

**화면 parity 조치(2026-09-22)**: MSA admin `gift-items/form.html`이 이전 세션("SFR-005 재검토")에 배송비 설정을 **드롭다운으로 노출**하고 있었으나, AS-IS는 `<tr class="hidden">`로 숨김. 기준([[asis-porting-principles]])대로 **배송비 설정 5행(구분·기본배송비·조건부무료·제주·도서산간)을 `display:none`으로 숨김**(input은 남겨 저장 시 값 보존). 택배사·반품가능여부는 AS-IS와 동일하게 노출 유지. 구매자 상세는 이미 "무료" 하드코딩으로 일치.

**G6/G7 (다른 서브시스템 downstream)**: 필드는 order까지 전달 완료. 단, 실제 효과는 배송비 *계산*이 아니라 다른 서브시스템에 있다 —
- **G6 반품배송비**: AS-IS `OrderClaimApplyServiceImpl:2513 apply.setCollectionShippingAmount(orderItem.getShippingReturn())` — 반품 클레임 시 고객이 부담하는 회수배송비. MSA `ClaimService`는 반품 시 포인트 전액복원(회수비 미차감)하는 단순화 모델. 재현하려면 클레임 환불 SAGA(order→point)를 회수비 차감으로 재구성해야 함. 실데이터 shipping_return 0건.
- **G7 본사/업체 정산**: AS-IS `Shipping`가 deliveryType=1(본사배송)이면 배송비 정산 대상을 운영자(DEFAULT_OPMANAGER_SELLER_ID)로 잡음 — *배송비 remittance* 레코드용(품목 정산과 별개). MSA엔 판매자별 배송비 정산 레코드가 없어 올바른 귀속처가 없음. OrderItem.sellerId에 얹으면 품목정산이 오귀속됨(부정확). 실데이터 delivery_type 전건 NULL.

### 5. 판정 결론 / 권고

- **실사용 범위(type3 조건부무료 + 무료)는 이미 MSA가 서버·화면 모두 AS-IS와 동등하게 재현 완료.**
- **화면 표기도 AS-IS와 동일**(무료 하드코딩, shippingTypeText 죽은코드까지 parity).
- 갭 G1~G7은 **AS-IS 코드엔 살아있으나 답례품 실데이터에 0건** → [[asis-porting-principles]]의 "기능 있으면 다 만들되"에 해당하나, 실운영 데이터가 없어 검증 불가능한 순수 코드 parity.

권고: **배송비 정책은 실사용분 재현 완료로 마감**하고, 나머지 항목(카테고리 3단 트리 / 검색어 관리 / 판매자 셀프서비스)로 이동. G1~G7 순수코드 parity는 운영데이터 확보 후 재검토(보류). — 최종 결정은 사용자.

---

## 19. gift 판매자 셀프서비스 parity

> 통합 전 파일: `docs/gift-seller-selfservice-parity-audit.md`

## 판매자 셀프서비스 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[asis-porting-principles]] + [[asis-porting-principles]].
연관 결정: [[deferred-features]](제공자웹 분리는 현행 gift 내 /seller/* 유지·보류), gift-seller-portal-unauthenticated(보관: docs/archive/memory-fix-log-2026-10.md).

### 1. AS-IS 답례품제공자(seller) 기능 전수 (`seller/i18n/*`, `*SellerController`)

| AS-IS 메뉴 | 컨트롤러 | 성격 |
|---|---|---|
| gift-item 답례품 등록/관리 | GiftItemSellerController | 답례품 live |
| item 상품관리 | SellerItemController | SalesOn 원제품 |
| order 주문관리 | SellerOrderController | 답례품 live |
| shipment 출고/송장 | SellerShipmentController | 답례품 live |
| shipment-return 반품출고 | SellerShipmentReturnController | 답례품 live |
| remittance 정산 | SellerRemittanceController | 답례품 live |
| qna/qna-item/qna-locgov | SellerQnaController 등 | 답례품 live |
| notice/sellerNotice 공지 | SellerNoticeController 등 | 답례품 live |
| mall 판매자몰 | MallController | SalesOn(판매자 개별몰) |
| user 계정 | SellerUserController | member 영역 |
| magicline 전자서명 | MagicLineSellerController | SalesOn/외부 |
| sale-edit, temp-process | - | SalesOn/임시 |
| categoriesfilter | CategoriesFilterSellerController | SalesOn |

### 2. TO-BE(MSA) 현재 커버리지

**이미 구현(인증 기반, gift 내 /seller·/my)** — GiftController + SellerPortalController + SellerApiController:
- 답례품 **등록/수정**(/register, /gifts/{id}/edit), **옵션·텍스트옵션·추가구성 편집**(이번 라운드), **재고조정**(/stock), **판매중지**(/stop·/discontinue)
- **본인 답례품 목록 + 주문현황 대시보드**(/seller/dashboard, ROLE_PROVIDER JWT 인증)
- **Q&A 답변**(/my/inquiries, /inquiries/{id}/answer)
- 입점업체(Seller) admin CRUD(SellerApiController)

**현재 admin(운영관리) 전용 — 판매자 셀프 아님**:
- **송장/출고 등록**: order `/api/admin/orders/{id}/invoice`. OrderMyApiController:307~311 주석 "송장등록·배송상태변경은 admin으로만 수행" 명시.
- **정산(remittance)**: admin `settlements` + SettlementService + Settlement/OrderLedger.
- **반품출고**: order DeliveryReturnAdminApiController(admin).

### 3. 갭목록

| # | 갭 | AS-IS | MSA | 판정 |
|---|---|---|---|---|
| P1 | 판매자 출고/송장 등록 | SellerShipmentController(판매자가 송장입력) | admin 전용 | **live 갭 — 판매자 이관 여부 결정 필요** |
| P2 | 판매자 정산 조회 | SellerRemittanceController | admin 전용 | **live 갭 — 결정 필요** |
| P3 | 판매자 반품출고 처리 | SellerShipmentReturnController | admin 전용 | **live 갭 — 결정 필요** |
| P4 | 판매자 공지 조회 | SellerISysNoticeController(/seller/sys-notice: 목록/상세/첨부다운로드) | **구현 완료(2026-09-22)** | ✅ |
| P5 | 판매자 주문관리 | SellerOrderController **extends OrderManagerController**(=운영자 주문관리 전체: 송장/배송상태/취소 포함) | 대시보드 조회만 | **정정: P1과 동일 영역(전체 주문관리 상속) → 경량 아님, P1~P3와 함께 보류** |
| P6 | mall 판매자몰 | MallController | 없음 | **SalesOn 종속 → 보류+기록** |
| P7 | magicline 전자서명 | MagicLineSellerController | 없음 | **SalesOn/외부 → 보류+기록** |
| P8 | item 상품관리·sale-edit·temp-process·categoriesfilter | - | 없음 | **SalesOn 종속 → 보류+기록** |
| P9 | user 계정관리 | SellerUserController | member 영역 | member 서비스 소관(별도) |

### 4. 결론 / 권고 (결정 필요)

- **핵심 판매자 기능(등록/수정/옵션/재고/판매중지/Q&A/주문현황)은 이미 인증 기반으로 구현**되어 있다 → 유지.
- **P6~P8(mall·magicline·item·sale-edit 등 SalesOn 종속)은 현재 미사용 → 보류+기록**(DA 설계 대기).
- **P1~P3(출고/송장·정산·반품출고)은 답례품 live 기능이나 현재 MSA는 admin(운영관리) 전용**이다. 이걸 판매자 셀프로 이관/추가하는 것은 **[[deferred-features]](제공자웹 분리 보류) 결정**에 걸린다 → 보류.
  - ⚠ **정정(2026-09-22)**: 이전 판에 "ISP상 판매자가 직접 출고/정산하는가 범위"라고 적었으나, **원본 ISP 문서는 리포지토리에 없고(요약본 `docs/requirements.md` 후반부 ISP 요약만 존재), 특정 페이지로 뒷받침되지 않는다.** 오히려 요약본상 **정산(op_settlement)은 "운영관리(admin) MSA" 소관**으로 설계됨(원본 p.259/373/379). 출고/운송장등록은 "주문관리 MSA" 기능으로 나열되나 수행 주체(판매자 vs 운영자)는 요약본에 명시 없음. 판매자 출고/정산 주체 근거는 AS-IS 소스(Seller*Controller)와 접근권한 매트릭스(답례품제공자 롤·UI_S* 화면ID)뿐.
- **사용자 결정(2026-09-22)**: P4만 지금 구현(P5는 위 정정으로 P1과 동일영역 판명 → 보류), P1~P3·P5는 제공자웹 분리 결정과 함께 보류, P6~P8(SalesOn)은 기록+보류.

### 5. P4 구현 (2026-09-22, 완료)

AS-IS `SellerISysNoticeController`(/seller/sys-notice) 그대로 재현. 판매자 공지 데이터(admin op_sys_notice_seller)는 admin 소유라 gift 셀프포털이 admin 내부 API를 호출한다.
- **admin** `SellerNoticeInternalApiController`(/api/seller-notices): 목록(노출필터 useYn/displayFlag≠'N', 제목·기간 검색)/상세(조회수 증가)/첨부다운로드. X-Internal-Secret 가드.
- **gift** `SellerNoticeClient`(gift→admin, X-Internal-Secret) + `SellerNoticeController`(/seller/sys-notice/list·/detail/{id}·/file-download/{fileId}, JWT ROLE_PROVIDER 인증) + 템플릿 `seller-notice-list.html`(공지사항, No./제목/조회수/등록일시, 공지 라벨·첨부표시, 총 N건, 제목·기간 검색)·`seller-notice-detail.html`(제목/등록일시/조회/내용/첨부자료). 대시보드에 "공지사항" 링크 추가. gift config `ghlove.admin-service.base-url` 추가.
- admin·gift 컴파일 OK. 검증 시드: admin.op_sys_notice_seller 2건(1002 공지·1003 일반). **재기동 시 활성**.

---

## 20. order 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-order.md`

## AS-IS 기능 감사 - order(주문) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(saleson 백엔드 `ghlove-web` + Vue2 `ghlove-frontend`) → `ghlove-msa` order/admin/storefront

### 조사 방법 메모

`ghlove-web`의 고객용 주문/결제 컨트롤러(`saleson.shop.order.OrderController`, `OrderMobileController`)는 **본문 전체가 주석처리된 죽은 코드**임을 확인했다(`private final OrderService orderService;` 필드부터 `step1`/`save`/`pay`/각종 PG(LG Dacom, PAYCO, CJ, KCP, 이니시스, KSPay, easypay) 콜백까지 전부 `//` 처리, `grep -nE "^\t@(Get|Post)Mapping"` 결과 0건). 즉 AS-IS 자체에서 현금 PG 기반 결제 플로우는 실사용되지 않는다 — `sfr_gap_fill_round`/`domain_point_rate_policy` 메모대로 실제 구매수단은 기부포인트이므로 이는 당연한 결과다. 이 감사는 이 죽은 코드 자체의 이관 여부는 묻지 않고(스킵 근거 충족), 살아있는 코드(`OrderManagerController`(admin), `OrderClaimApplyController`(고객 클레임 신청), `CartController`, 쿠폰/정산 컨트롤러 등)와 실제 화면(JSP/Vue2 html)을 기준으로 감사했다.

### AS-IS 기능 전수 목록

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 고객 - 장바구니(지자체별 그룹핑, 포인트잔액 표시) | `ghlove-frontend/cart/index.html`, `saleson.shop.cart.CartController` | **구현됨** | `order/.../web/CartController.java`, `storefront/.../CartView.vue` (`cart_locgov_point_architecture` 메모) |
| 고객 - 주문서작성/결제(PG 연동) | `saleson.shop.order.OrderController`(**전체 주석처리, 죽은 코드**), `ghlove-frontend/order/step1.html` | **의도적 제외 확인됨** | AS-IS 자체가 죽은 코드 - 실구매는 포인트 결제(`CheckoutController`)로 구현됨 |
| 고객 - 네이버페이 간편구매(오픈마켓 연동) | `CartController`(`configPgService.isDisplayNaverPayFlag()` 게이트), `components/ui/naver/naver-pay-button.vue`, `POST /api/open-market/checkOutReturn` | **미구현** | 아래 gap 상세 참고(낮음) |
| 고객 - 주문완료 | `ghlove-frontend/order/step2.html` | **구현됨** | `storefront/.../OrderCompleteView.vue` (`vue3_storefront_migration_round3_order`) |
| 고객 - 주문내역 조회(마이페이지, 기간/답례품명 검색) | `mypage/orderList.html`, `MypageController#order` | **구현됨** | `order/.../web/OrderController.java` `/my`, `storefront/.../MyOrdersView.vue` |
| 고객 - 주문상세(배송정보/운송장/영수확인) | `mypage/orderDetail.html`, `MypageController#order-detail` | **구현됨** | `OrderController#detail`, `OrderDetailView.vue` |
| 고객 - 구매확정(수령확인) | `mypage/orderDetail.html` | **구현됨** | `OrderController#confirmReceipt` |
| 고객 - 배송지 변경 | `mypage/orderDetail.html` | **구현됨** | `OrderController#changeDeliveryAddress` |
| 고객 - 배송지 관리(등록/수정/목록, 기본배송지) | `saleson.shop.userdelivery.UserDeliveryController`(`/delivery/list,write,edit`), `mypage/deliveryInfo.html` | **구현됨** | `storefront` `/mypage/delivery`, `/mypage/delivery/new`, `/mypage/delivery/:id/edit` (`vue3_storefront_migration_round2_mypage`) |
| 고객 - 취소/반품/교환 신청(사유입력) | `saleson.shop.order.claimapply.OrderClaimApplyController`(`/order-claim-apply/{cancel,return,exchange}`) | **구현됨** | `ClaimController#request`, `MyClaimsView.vue` |
| 고객 - 취소/반품/교환 내역 조회 | `mypage/orderCancel.html` | **구현됨** | `ClaimController#myClaims` (`/claims/my`) |
| 클레임 처리방식 - 교환(원 상품↔새 상품 실제 맞교환) | `OrderManagerController` `claim/exchange/process`, `order-item-exchange.jsp` | **의도적 제외 확인됨(코드에 명시)** | `ClaimService` 주석: "교환은 원 주문을 취소 처리한 뒤 사용자가 새로 주문하는 방식(반품+재주문)으로 처리" - 아이템 실물 교환이 아니라 단순화된 반품+재구매 흐름. 의도적 설계로 코드에 이미 문서화되어 있음 |
| 고객 - 쿠폰함(보유쿠폰/사용) | `saleson.shop.coupon.CouponController`, `mypage/old/coupon-list.html` | **구현됨** | `CouponMyController`, `MyCouponsView.vue` (`coupon_subsystem_round`) |
| 고객 - 오프라인쿠폰 등록(쿠폰코드 입력) | `CouponController` | **구현됨** | `OfflineCouponClaimView.vue` |
| 고객 - 체크아웃 쿠폰 할인 적용 | `OrderController`(죽은코드), `CouponController` | **구현됨** | `CheckoutApiController` (실제 포인트차감까지 E2E검증, `coupon_subsystem_round`) |
| **관리자 - 주문 전체 목록(상태별 탭: 신규주문/입금대기/배송준비/배송중/배송완료/구매확정/취소/반품/교환, 기간·회원·지자체 검색)** | `OrderManagerController`(`/opmanager/order/list`,`offList`), JSP 11종(`order/list/all,new-order,shipping-ready,shipping,finish,confirm,cancel,return,exchange,refund,waiting-deposit`) | **미구현** | 아래 gap 상세 참고(높음) |
| **관리자 - 주문 상세(주문정보 변경, 관리자메모)** | `OrderManagerController#detail`,`orderInfoChange`,`changeAdminMemo` | **미구현** | 아래 gap 상세 참고(높음) |
| **관리자 - 클레임 승인/거절/처리큐(반품·교환·취소)** | `OrderManagerController#claimReturnProcess,claimExchangeProcess,claimCancelProcess` (`/opmanager/order/{pageType}/claim/*`) | **부분구현(인증 없음)** | 아래 gap 상세 참고(높음) - `order` 서비스에 `/claims`로 존재하나 운영자 로그인/RBAC 전혀 없음(코드 주석에 명시된 알려진 미비) |
| 관리자 - 클레임메모(반품/교환 건별 상담 메모) | `saleson.shop.claimmemo.ClaimMemoManagerController`(`/opmanager/claim-memo`), `OrderManagerController` `claim-memo/create,list,update` | **미구현** | 위 클레임 처리큐 gap에 포함 |
| 관리자 - 운송장번호 등록/배송상태 변경 화면 | `OrderManagerController` `shipping/change-shipping-number` | **부분구현(인증 없음)** | `order` 서비스에 `POST /orders/{orderId}/invoice`,`/delivery-status` API는 존재하나 코드 주석에 "판매자·운영자가 쓰는 경로라 본인확인 범위 밖(아직 이 MSA에 운영자 로그인 모델 없음)"이라고 명시 - 화면도 RBAC도 없음 |
| 관리자 - 결제수단 변경(무통장↔카드 등) | `OrderManagerController` `{pageType}/change-pay` | **의도적 제외 확인됨(추정)** | 실구매가 포인트 결제라 현금 PG 결제수단 변경 자체가 무의미(원조사 방법론 참고) |
| 관리자 - 입금대기(무통장입금) 목록/취소 | `OrderManagerController` `waiting-deposit` | **의도적 제외 확인됨(추정)** | 상동 - 실결제가 포인트이므로 무통장입금 대기 상태가 발생하지 않음 |
| 관리자 - 전화주문 대리등록(신규주문/모바일신규주문) | `OrderManagerController` `new-order`,`new-order-mobile`, `call-find-user.jsp` | **미구현** | 위 주문관리 gap에 포함(중간 우선순위) |
| 관리자 - 주문대행(order-agency, 위탁업체 조회) | `saleson.shop.orderagency.OrderAgencyController`(`/opmanager/order-agency`) | **미구현** | 위 주문관리 gap과 통합 검토 권장(낮음) |
| 관리자 - 택배사(배송업체) 마스터 관리 | `saleson.shop.deliverycompany.DeliveryCompanyManagerController`(`/opmanager/delivery-company`) | **미구현** | `order`는 `DELIVERY_CARRIER` CommonCode만 사용, 전용 CRUD 화면 없음(낮음, CommonCode로 충분할 가능성) |
| 관리자 - 주문/클레임 관련 엑셀 다운로드(전 상태 탭) | `OrderManagerController` 각 탭별 `*-excel` 매핑 10종 이상 | **미구현** | admin 콘솔 전반에 엑셀 다운로드 자체가 없음(point 서비스 감사에서도 동일 지적, 프로젝트 공통 gap으로 보임) |
| 관리자 - 주문건수 대시보드 위젯 | `CommonController` `opmanager/order-count`,`seller/order-count` | **미구현** | 주문관리 화면이 없으니 위젯도 없음 |
| 판매자(셀러) 콘솔 - 주문관리 | `saleson.shop.order.SellerOrderController extends OrderManagerController` | **미구현** | AS-IS 자체가 admin(OrderManagerController)과 화면/로직 100% 공유(상속) - admin 주문관리 신설 시 자연히 커버 범위 판단 필요, 별도 판매자 인증체계는 프로젝트에 없음 |
| 지자체 송금/정산(답례품 제공자 계좌입금) | `saleson.shop.remittance.RemittanceManagerController` | **구현됨** | `admin/.../SettlementController`(`GENERATED→INVOICED→DEPOSITED→CLOSED`) - AS-IS "지자체송금"과 동일 프로세스로 이미 확인됨(`offgive_and_remittance_rounds` 메모) |
| 관리자 - 쿠폰 CRUD/발행/타겟설정 | `saleson.shop.coupon.CouponManagerController`(`/opmanager/coupon`) | **구현됨** | `admin/.../CouponAdminController.java` (list/new/edit/copy/delete/publish/target-items/target-users/offline-codes/usage - 1:1 대응) |
| 관리자 - 정기쿠폰(회원가입 등 조건부 자동발급) | `saleson.shop.couponregular.CouponRegularManagerController` | **구현됨** | `CouponAdminController` `/coupon-regular/*` |
| 관리자 - 쿠폰 사용내역 조회 | `saleson.shop.couponuse.CouponUseManagerController` | **구현됨** | `admin/.../templates/coupon/usage.html` |
| 관리자 - 택배사 배송추적(SmartDelivery 연계) | 명세상 admin 소관(스코프 분리) | **구현됨** | `admin/.../DeliveryTrackingController`, `SmartDeliveryClient` - 기존 감사 완료 항목(배경 메모 참고) |
| 고객 - 후기작성 유도(주문내역에서 "후기작성" 버튼) | `mypage/orderList.html`(343행,495행,1317행), `orderDetail.html`(215행,856행) - `saleson.shop.item.ItemController#create-review` 링크 | **미구현** | 아래 gap 상세 참고(낮음) |
| 다품목 주문의 품목별 부분취소 | AS-IS `OD_ORDER`가 다품목 주문 지원(`order-item-cancel.jsp` 등 품목단위 처리 UI 존재) | **의도적 제외 확인됨** | `cart_locgov_point_architecture` 메모: 이 프로젝트는 원래부터 단일품목 주문 아키텍처(체크아웃이 N개 독립 단일품목 SAGA로 팬아웃) - 재설계 범위 밖으로 이미 결정됨 |
| 배송비 계산 로직(상품별 shippingType 1~6) | `OpenMarketController`(NaverPay 연동 payload 전용), `step1.html`(죽은 페이지 내 3건) | **의도적 제외 확인됨(신규 확인)** | `cart/index.html`·`orderDetail.html` 등 실사용 페이지에는 배송비 언급이 사실상 없음(0~1건) - 죽은 PG 결제플로우에 종속된 잔재로 판단, 답례품은 사실상 전부 무료배송으로 운영되는 것으로 보임 |
| 주문서 임시저장 | 고객 화면(`step1.html`,`cart/index.html`,`orderList.html`) 어디에도 "임시저장" 문구/버튼 없음 | **해당없음(AS-IS 자체 미존재)** | `CartController#save-order-item-temp`/`ConfigManagerController#order-temp-config`는 죽은 PG 플로우의 세션 연속성용 백엔드 배관일 뿐, 고객 노출 기능이 아님 |
| 재구매 버튼 | 고객 화면 어디에도 "재구매" 문구 없음 | **해당없음(AS-IS 자체 미존재)** | grep 결과 0건 |

### Gap 상세

#### 우선순위: 높음

**1. 관리자 - 주문관리 콘솔 전체 부재 (목록/상세/상태변경/클레임 처리큐)**

AS-IS `OrderManagerController`(`ghlove-web/src/main/java/saleson/shop/order/OrderManagerController.java`, 3000줄 이상)는 `/opmanager/order/*` 아래 다음을 제공했다:
- 상태별 탭 11종 목록: 전체(`list`), 오프라인(`offList`), 입금대기(`waiting-deposit`), 신규주문(`new-order`), 배송준비(`shipping-ready`), 배송중(`shipping`), 배송완료(`finish`), 구매확정(`confirm`), 취소(`cancel`), 반품(`return`), 교환(`exchange`)
- 주문 상세: 주문정보 변경(`order-info/change`), 관리자메모(`admin-memo/change`), 품목상세(`item-detail`)
- 클레임 처리: 반품/교환/취소 각각의 처리(`claim/{return,exchange,cancel}/process`), 목록(`claim/{return,exchange,cancel}/list`), 로그(`claim/return-log`,`exchange-log`)
- 클레임메모: `saleson.shop.claimmemo.ClaimMemoManagerController`(별도 컨트롤러, 반품/교환 상담이력)
- 운송장번호 변경(`shipping/change-shipping-number`)
- 전화주문 대리등록: `new-order`,`new-order-mobile` + 회원검색 팝업(`call-find-user.jsp`)
- 각 탭별 엑셀 다운로드 10종 이상

TO-BE `admin/src/main/java/com/ghlove/admin/web/` 디렉토리를 전체 확인한 결과 `Order*Controller` 자체가 존재하지 않는다. `OrderClient`(`admin/.../service/OrderClient.java`)와 `OrderLedger`(`admin/.../domain/OrderLedger.java`)는 통계(`StatsService`)·정산(`SettlementService`)·재동기화(`/stats/resync`) 용도로만 order 서비스를 조회하며, 주문을 검색/열람/상태변경하는 화면은 admin에 전혀 없다.

한편 `order` 서비스 자체에 `ClaimController`(`order/.../web/ClaimController.java`)가 `/claims`(승인큐), `/claims/{id}/approve,reject,complete`를 제공하지만, 코드 주석에 명시된 대로 **완전히 비인증** 상태다:
```
/** /claims (운영자 승인 큐)는 판매자/운영자용 경로라 이 라운드의 "본인 확인" 범위 밖 -
 *  아직 이 MSA에 운영자 로그인 모델이 없다. */
```
`OrderController`의 운송장 등록(`/orders/{orderId}/invoice`)·배송상태 변경(`/delivery-status`)도 동일하게 비인증이다. 이 주석은 `admin_rbac_6tier_role_round`(ROLE_ADMIN_1~6 전면 구현) 이전에 작성된 것으로 보이며, 현재는 admin에 완비된 RBAC 체계가 있으므로 더 이상 "아직 운영자 로그인 모델이 없어서"라는 전제가 성립하지 않는다 - 즉 이 gap은 admin RBAC 완성 후 방치된 실제 결함이다.

**필요 작업**:
1. admin에 `주문관리` 메뉴 신설: 목록(상태별 탭 - 최소 신규주문/배송준비/배송중/배송완료/구매확정/취소·반품·교환은 필요, 입금대기·결제수단변경은 포인트결제 모델상 불필요 판단) + 상세(관리자메모 포함)
2. 클레임 처리큐(`/claims`, `/claims/{id}/approve,reject,complete`)를 order에서 admin으로 이관하거나, 최소한 admin RBAC 미들웨어로 감싸 운영자만 접근 가능하도록 게이트 - 현재 인터넷에 노출된 URL을 아는 누구나 임의 주문의 반품/교환/취소를 승인·거절할 수 있는 상태(보안 결함에 가까움)
3. 운송장번호 등록/배송상태 변경 화면 신설 + 동일하게 RBAC 게이트
4. 클레임메모(상담이력) 엔티티 신규 추가
5. 우선순위는 낮지만 함께 검토: 전화주문 대리등록(신규주문), order-agency, 택배사 마스터관리, 엑셀 다운로드(admin 공통 컴포넌트로 다른 서비스 감사와 통합 권장 - point 서비스 감사에서도 동일 지적됨)

#### 우선순위: 중간

**2. 관리자 - 전화주문 대리등록(신규주문)**

AS-IS `OrderManagerController#newOrder`(`new-order`,`new-order-mobile`)는 상담원이 전화로 접수한 주문을 회원 검색(`call-find-user.jsp`) 후 대신 등록하는 기능이다. TO-BE에는 이 경로 자체가 없다. 위 gap #1(주문관리 콘솔)의 하위 기능으로 함께 구현 권장.

**3. 판매자(셀러) 콘솔 주문관리 범위 판단**

AS-IS `SellerOrderController extends OrderManagerController`로 판매자와 운영자가 완전히 동일한 화면/로직을 `ShopUtils.isSellerPage()` 분기로만 나눠 쓴다(셀러는 본인 물품 주문만 필터링). 이 프로젝트에는 판매자 전용 로그인/인증 체계가 없으므로(`donation_receipt_as_is_parity` 등 기존 결정과 일관되게 seller 도메인은 admin 하위 CRUD로만 존재 - `seller` 템플릿 디렉토리 확인됨), gap #1 해결 시 "판매자용 주문조회는 셀러 인증이 별도로 필요한 더 큰 프로젝트"로 분리해 스코프 아웃할지 결정 필요.

#### 우선순위: 낮음

**4. 클레임 처리큐 관련 세부기능(클레임메모, 처리로그, 재배송비 계산)**

AS-IS는 반품/교환 처리 시 상담메모(`ClaimMemoManagerController`), 처리이력 로그(`return-log`,`exchange-log` 팝업), 교환 시 재배송비 계산(`re-shipping-amount`)까지 제공했다. TO-BE `Claim` 엔티티(`order/.../domain/Claim.java`)는 `claimId,orderId,claimType,reason,status,createdDate,processedDate`만 가진 매우 단순한 모델이다. gap #1 해결 규모에 따라 함께 검토.

**5. 네이버페이 간편구매(오픈마켓 연동)**

`CartController`가 `configPgService.isDisplayNaverPayFlag()`로 게이트하는 네이버페이 퀵바이 버튼(`naver-pay-button.vue` → `POST /api/open-market/checkOutReturn`)이 AS-IS에 존재한다. `external_integrations_architecture` 메모의 "enabled=false mock-gated" 패턴이 적용될 법한 연계이지만 order 서비스에는 대응 코드가 전혀 없다. 죽은 PG 플로우와 달리 이 버튼은 `cart/index.html`(실사용 페이지)에도 노출되므로 완전한 죽은 코드로 단정하기는 어려우나, 실제 운영 사이트(ilovegohyang.go.kr)에서 노출 플래그가 꺼져 있을 가능성이 높다(교차확인 권장). 확정 전까지 낮은 우선순위로 보류 권장.

**6. 주문내역 화면의 "후기작성" 유도 버튼**

AS-IS `mypage/orderList.html`/`orderDetail.html`은 배송완료/구매확정된 주문 옆에 "후기작성" 버튼을 노출해 `ItemController#create-review{openerReload}/{orderCode}/{itemUserCode}`로 연결한다. TO-BE `storefront/.../MyOrdersView.vue`,`OrderDetailView.vue`에는 이 CTA가 없다(gift 서비스의 답례품후기 작성 화면 자체는 `vue3_storefront_migration_round4_gift`/`round10`에서 이미 구현됨 - 진입 동선만 빠져있음). 낮은 공수로 닫을 수 있는 gap.

**7. 택배사 마스터관리, 주문대행(order-agency)**

`DeliveryCompanyManagerController`(택배사 코드/추적URL 관리로 추정, AS-IS 도메인 필드까지는 미확인)와 `OrderAgencyController`(위탁업체가 지자체 주문건을 조회하는 읽기전용 화면으로 보임)는 실사용 빈도가 낮고 CommonCode로 상당부분 대체 가능해 보여 낮은 우선순위로 분류. gap #1 규모 확정 시 함께 검토.

### 완전히 구현 확인됨 (요약)

- 장바구니(지자체별 그룹핑, 포인트잔액) - `CartView.vue`
- 주문서 작성~완료~조회~상세~구매확정~배송지변경(포인트결제 기반, PG 아님) - `order/.../web/OrderController.java`, `MyOrdersView.vue`, `OrderDetailView.vue`, `OrderCompleteView.vue`
- 배송지(주소록) 관리 - storefront `/mypage/delivery`
- 취소/반품/교환 신청 및 본인 내역 조회 - `ClaimController#request,myClaims`, `MyClaimsView.vue` (교환=반품+재주문 단순화는 코드에 이미 문서화된 의도적 설계)
- 쿠폰함/오프라인쿠폰 등록/체크아웃 할인적용 - `coupon_subsystem_round`에서 이미 E2E 검증
- 관리자 쿠폰 CRUD/정기쿠폰/사용내역 - `CouponAdminController` (AS-IS 3개 컨트롤러와 1:1 대응)
- 지자체송금/정산(답례품 제공자 계좌입금) - `SettlementController` (`offgive_and_remittance_rounds`에서 이미 확인)
- 택배사 배송추적(SmartDelivery) - `DeliveryTrackingController` (배경 메모의 명시적 역할분리)
- 단일품목 주문 아키텍처, 배송비 미적용 - AS-IS 자체 죽은코드/비활성 확인됨(이번 감사에서 신규 확인)

---

## 21. point 기능 대조

> 통합 전 파일: `docs/as-is-feature-audit-point.md`

## AS-IS 기능 감사 - point(포인트) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(saleson 백엔드 `ghlove-web` + Vue2 `ghlove-frontend`) → `ghlove-msa` point/admin/storefront

### AS-IS 기능 전수 목록

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 고객 마이페이지 - 기부포인트 조회(지자체별 발생/사용/잔액) | `ghlove-frontend/mypage/cntrPoint.html`, `cntrPointDetail.html` / `saleson.shop.mypage.MypageController#pointSaveList,pointUsedList` | **구현됨** | `storefront/src/views/mypage/MyPointsView.vue` (`locgovSummary`) |
| 소멸 임박 포인트 안내 | `MypageController` `getNextMonthExpirationPointAmountByParam` | **구현됨** | `MyPointsView.vue` `upcomingExpirations` / `PointService.upcomingExpirations` |
| 포인트 자동 적립(기부 발생시) | `GivePointService`, `PT_LOCGOV_POINT_RATE` 사용처 | **구현됨** | `DonationEventListener`, `LocgovPointRate` (지자체별 요율, `domain_point_rate_policy` 메모) |
| 포인트 사용(답례품 결제) | `saleson.shop.order.OrderManagerController`(admin, PointService 연동), 실제 차감 로직은 프레임워크 jar 내부 | **구현됨** | `PointService.deductForOrder` / order SAGA |
| 포인트 예약/예약해제(SFR-004) | 별도 AS-IS 화면 없음(TO-BE 신규 설계, SFR 스펙 기반) | **구현됨** | `PointReservation`, `PointController#reservations`, storefront `PointReservationsView.vue` (round15) |
| 포인트 소멸 배치(FIFO lot) | 배치 잡(소스 미포함, 프레임워크 내부) | **구현됨** | `PointService.runExpirationBatch` / `/batch/expire` |
| 관리자 - 기부 포인트현황(행안부 전체/지자체별 목록+상세) | `saleson.shop.give.givepoint.GivePointManagerController` (`/opmanager/give/give-point/list`,`/detail`) | **구현됨** | `admin/.../web/GivePointController.java`, `templates/give/give-point-list.html`, `give-point-detail.html` (mois/locgov 역할분기까지 일치) |
| 관리자 - 포인트 정합성 검사(주문금액 vs 포인트사용 대사) | `saleson.shop.pointcheck.PointCheckManagerController` (`/opmanager/point-check/list`, view `view_rc_order_amt_vs_point_use_check` 기반) | **구현됨** | `admin/.../templates/reconciliation/order-point.html` (동일 view 재구현이라고 코드 주석에 명시) |
| 관리자 - 전체 포인트 발행내역/사용내역/일별 발생·사용현황 + 엑셀다운로드 | `saleson.shop.point.PointManagerController` (`/opmanager/point/list`, `total-history/list`, `day-group/list` + 각 엑셀다운로드) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 회원 개별 포인트 수동 지급/차감 | `saleson.shop.user.UserManagerController` (`popup/point-create/{pointType}/{userId}`, `PointService.earnPoint`/`deductedPoint`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 전체회원 대상 포인트 일괄 지급 | `UserManagerController` (`customer/point`, `insertPointAllUser`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 선택회원 대상 포인트 일괄 지급 | `UserManagerController` (`customer/point-pay` → `point-setting`, `insertPointPay`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 엑셀 업로드 포인트 일괄 지급 | `UserManagerController` (`customer/point-by-excel`, `insertPointByExcel`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 회원별 포인트 내역 조회 팝업(단건 회원, 페이징) | `UserManagerController` (`popup/point/{pointType}/{userId}`) | **부분구현** | `give-point-detail.html`이 지자체×연도 단위로 유사 데이터(건별 발생/사용/잔액)를 제공하지만, 진입 경로가 "회원관리→회원선택"이 아니라 "지자체 선택"이며, admin에 회원(고객) 관리 화면 자체가 없어 "특정 회원 1명의 전체 포인트 내역"을 바로 조회하는 기능은 없음 |
| 관리자 - 포인트 정책 설정(기본포인트%, 가입포인트, 리뷰포인트, 기간별포인트, 사용최소/최대, 만료월, 배송쿠폰만료월) | `saleson.shop.config.ConfigManagerController` (`/opmanager/config/point`) | **의도적 제외 확인됨(신규 확인)** | 아래 근거 참고 |
| 배송비쿠폰(포인트와 별도의 두 번째 포인트 타입) | `PointUtils.SHIPPING_COUPON_CODE`, 여러 opmanager 컨트롤러에서 `avilablePoint2`로 병행 표시 | **의도적 제외 확인됨(신규 확인)** | 아래 근거 참고 |
| 관리자 - 환불처리 화면에서 회원 가용포인트 표시 | `OrderManagerController`(admin) refund 처리 시 `pointService.getAvailablePointByUserId` | **구현됨(간접)** | `admin/.../templates/settlements/detail.html`, `list.html`에서 포인트 관련 표시 확인 |

### Gap 상세

#### 우선순위: 높음

**1. 관리자 - 회원 포인트 수동 지급/차감 기능 전체 (개별/전체/선택/엑셀)**

AS-IS `UserManagerController`(`ghlove-web/src/main/java/saleson/shop/user/UserManagerController.java`)는 아래 4가지 방식으로 관리자가 회원에게 포인트를 수동으로 지급하거나 차감할 수 있었다.
- 개별회원: `popup/point-create/{pointType}/{userId}` — `mode`가 지급/차감이면 각각 `pointService.earnPoint("admin", point)` / `pointService.deductedPoint(pointUsed, userId, pointType)` 호출, 사유(`reason`) 필수 입력
- 전체회원 일괄: `customer/point` (`point.jsp`) — "전체회원에게 포인트를 지급하시겠습니까?" 확인 후 `insertPointAllUser`
- 선택회원 일괄: `customer/point-pay` (`point-pay.jsp`) — 회원 목록에서 체크박스로 선택한 회원들에게만 지급, `insertPointPay`
- 엑셀 일괄: `customer/point-by-excel` (`point-by-excel.jsp`) — 엑셀 업로드로 다수 회원 포인트 지급, `insertPointByExcel`

TO-BE `point/src/main/java/com/ghlove/point/service/PointService.java`를 전체 확인한 결과, 주문결제 차감(`deductForOrder`)/복원(`restoreForOrder`)/기부 적립/예약/소멸배치 외에 **관리자가 임의 사유로 포인트를 지급(양수 적립)하는 메서드 자체가 존재하지 않는다.** `usePoints`(사용/차감 테스트용)만 있고 반대 방향(지급)이 없다. 또한 admin 서비스에는 회원(고객) 관리 화면 자체가 없어(templates 디렉토리에 user/member/customer CRUD 화면 부재, `fragments/customer-lnb.html`은 고객센터 게시판 내비게이션일 뿐 회원관리와 무관) 이 기능을 걸 UI 진입점도 없다.

**필요 작업**: (1) point 서비스에 `PointService.grantByAdmin(userId, amount, reason, locgovCode)` 같은 관리자 발급 메서드 추가(원장에 `TXN_TYPE=ADMIN_GRANT` 등으로 남기고 사유 필수), 반대의 `deductByAdmin`도 필요. (2) admin에 회원(고객) 목록/상세 화면이 없다면 최소한 "포인트 지급/차감" 단독 화면(회원ID 또는 검색으로 대상 지정 + 사유 입력)을 신설. (3) 전체회원/선택회원 일괄 지급, 엑셀 업로드 지급은 운영 빈도를 고려해 우선순위 조정 가능하나, 개별회원 수동 지급/차감은 CS 대응에 필수적이라 우선 구현 권장.

#### 우선순위: 중간

**2. 관리자 - 포인트 발행/사용 전체 내역 + 일별 통계 + 엑셀다운로드**

AS-IS `saleson.shop.point.PointManagerController`(`/opmanager/point/*`)는 기부포인트현황(GivePoint, 지자체 단위 집계)과는 별개로, 전체 포인트 원장을 기간 검색으로 조회하는 3개 화면을 제공했다.
- `list` — 포인트 발행 내역(원시 트랜잭션 목록)
- `total-history/list` — 포인트 사용 내역
- `day-group/list` — 일별 발생/사용 현황(집계)
- 위 3개 모두 `ROLE_EXCEL`/`ROLE_OPMANAGER` 권한 체크 후 엑셀 다운로드 제공

TO-BE `admin/.../web/GivePointController.java`(`give-point-list.html`)는 지자체×연도 단위로 집계된 뷰만 제공하고, 원시 트랜잭션 단위 조회·일별 집계·엑셀 다운로드는 없다. `admin/src/main/java` 전체를 grep해도 excel 관련 코드가 전혀 없음을 확인했다(`grep -rl "excel|Excel|엑셀"` 결과 0건) — 이는 point 서비스만의 문제가 아니라 admin 콘솔 전반에서 엑셀 다운로드 기능이 프로젝트 차원에서 아직 이관되지 않은 것으로 보인다.

**필요 작업**: point 서비스 또는 admin에 원장 원시 목록 조회 화면(검색조건: 기간, 회원, 지자체) + 일별 집계 화면 추가. 엑셀 다운로드는 point 서비스 하나만의 문제가 아니라 admin 전체의 공통 gap일 가능성이 높으므로, 다른 서비스 감사 결과와 합쳐 "admin 엑셀 다운로드 공통 컴포넌트 도입"으로 한 번에 처리하는 것을 권장.

**3. 관리자 - 회원별 포인트 내역 단건 조회 팝업**

AS-IS `popup/point/{pointType}/{userId}`는 회원관리 화면에서 특정 회원 1명을 클릭하면 그 회원의 포인트(또는 배송쿠폰) 내역을 페이지네이션과 함께 팝업으로 보여줬다(`point-list.jsp` 프래그먼트 재사용). TO-BE `give-point-detail.html`은 유사한 데이터(건별 발생/사용/잔액)를 지자체×연도 단위로 제공하지만, "회원 이름으로 검색"만 가능하고 회원 ID로 직접 진입하는 경로가 없다. 회원관리 화면 자체가 없다는 동일한 근본 원인(gap #1)에서 파생된 문제이므로, #1 해결 시 함께 정리 권장.

#### 우선순위: 낮음 (확인 필요, 확정 아님)

**4. 포인트 정책 설정 화면(`/opmanager/config/point`) 일부 항목의 실사용 여부**

이 화면은 기본포인트%, 회원가입시 포인트, 리뷰채택시 포인트, 기간별 포인트, 포인트 사용 최소/최대 금액, 포인트 만료월, 배송쿠폰 만료월 등을 설정하는 saleson 범용 쇼핑몰 프레임워크의 표준 설정 화면이다. 다음 근거로 ghlove의 실제 기부-포인트 비즈니스에는 적용되지 않는 saleson 템플릿 잔재일 가능성이 높다고 판단했다(단, `dormant-saleson-boilerplate-tables` 사례처럼 100% 확정하려면 운영 DB의 `OP_CONFIG` 실제 값 확인이 필요):
- 고객용 포인트 페이지(`ghlove-frontend/mypage/cntrPoint.html`, `cntrPointDetail.html`)에는 "최대 3일 지연 반영" 안내만 있을 뿐, 유효기간/최소·최대 사용한도/가입포인트/리뷰포인트 등 이 설정과 관련된 정책 안내 문구가 전혀 없음
- ghlove의 실제 적립은 "기부 시 지자체별 요율"(`PT_LOCGOV_POINT_RATE`, `domain_point_rate_policy` 메모로 이미 확인됨)로 이루어지는데, 이 설정화면은 "회원가입시/리뷰채택시/방문시" 등 일반 쇼핑몰형 적립 시나리오를 다룸 — 기부 기반 모델과 맞지 않음
- JSP 내부에 이미 "CJH 2016.10.20 미구현 기능" 주석으로 일부 항목이 죽어있다고 스스로 명시된 부분도 존재

다만 "포인트 만료월" 개념 자체는 TO-BE에도 실존하며(FIFO lot expire), 현재는 `CommonCode`(`SYSTEM_CONFIG`/`POINT_EXPIRY_NOTICE_DAYS` 등)로 관리 가능해 실질적으로 흡수된 것으로 판단했다. 전용 "포인트 설정" 화면 신설은 필요성이 확인되기 전까지 보류 권장.

### 완전히 구현 확인됨 (요약)

- 고객 마이페이지 포인트 조회(지자체별 집계, 소멸임박 안내) — `MyPointsView.vue`
- 포인트 자동 적립(기부 이벤트 기반, 지자체별 요율) — `DonationEventListener`, `LocgovPointRate`
- 포인트 사용(주문 결제, SAGA 연동) — `PointService.deductForOrder/restoreForOrder`
- 포인트 예약/예약해제(SFR-004) — `PointReservation`, storefront `PointReservationsView.vue`
- 포인트 소멸 배치(FIFO lot) — `PointService.runExpirationBatch`
- 관리자 기부포인트현황(행안부 전체/지자체별, 역할분기 포함) — `GivePointController`, `give-point-list/detail.html`
- 관리자 포인트 정합성 검사(주문금액-포인트사용 대사) — `reconciliation/order-point.html`
- 배송비쿠폰: AS-IS 자체에서 `mypage/old/`, `order/old/` 폴더에만 남아있는 죽은 saleson 템플릿 기능으로 확인되어 스킵 확정

## 22. admin 영역별 이식 원장

2026-10-20 메모리 통폐합 때 `docs/memory/`의 영역·화면별 이식 원장 23개를 원문 그대로 옮겼다(제목 = 원래 메모리 파일명). 영역별 현재 상태 한 줄 요약은 메모리 `admin-port-progress`에 있다.

### admin-system-area-port-progress

> 시스템관리(1000>1400) 영역 25화면 AS-IS 이식 원장과 진척 - 팝업관리·공통코드 완료, AS-IS JSP 경로 매핑표, 발견된 구조 갭(연계로그 4화면→1화면)


[[asis-screen-port-procedure]] 절차로 **메뉴 영역별** 진행 중. 첫 영역 = 시스템관리(1000 > 1400), 리프 25개.

**AS-IS JSP 경로 원장** (`ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` 기준):

| id | 메뉴 | AS-IS JSP | TO-BE url | 상태 |
|---|---|---|---|---|
| 1311 | 팝업관리 설정 | `popup/{list,form}.jsp` | /popups | **완료** |
| 1401 | 공통코드 관리 | `code/{list,form}.jsp` | /codes | **완료** |
| 1404 | 사용자 권한 관리 | `user-group/role/list.jsp` | /admin/roles/matrix | **완료** |
| 1405 | 사용자 권한그룹 관리 | `user-group/{list,form}.jsp` | /admin/roles | **완료** |
| 1406 | 관리자 권한 승인관리 | `user/manager-request/list.jsp` + `user/popup/manager-request-{details,history}.jsp` | /admin/manager-requests | **완료** |
| 1105 | ISMS관리 | `isms/isms-config.jsp` | /isms-config | **완료** |
| 7203 | 메인 배너 관리 | **`banner/main/{list,form}.jsp`**(user-login-banner/form.jsp 아님) | /banners | **완료** |
| 1408 | 사용자로그관리 | `log/user/{login-log-list,login-log-details}.jsp` | /log/user-login | **완료**(상세는 미연결 - 아래 참고) |
| 6401 | 관리자로그관리 | `log/{login-log-list,login-log-details,action-log-list}.jsp` | /log/login | **완료** |
| 1312 | 이메일 설정 | `mail-config/{list,form,change-code}.jsp` | /mail-config | **완료**(AS-IS는 목록 미사용→리다이렉트) |
| 1308 | 약관관리 | `config/policy/{list,form,create}.jsp` | /policy | **완료**(create.jsp는 policy/edit/{type} 전용, 1308 아님) |
| 1403 | Batch Job | `batch-job/{list,form}.jsp` | /batch-job | **완료** |
| 1305 | 배송업체 관리 | `delivery-company/{list,form}.jsp` | /admin/delivery-companies | **완료** |
| 1409 | 메뉴관리 | `menu/{list,form,page}.jsp` | /admin/menus | **완료**(page.jsp는 목록의 호출부가 주석처리돼 진입점 없음 - 미이식) |
| 7207 | 설문관리 | `qustnr/{list,form,result}.jsp` | /admin/surveys | **완료**(데이터모델까지 AS-IS로 교체, 아래 ★ 참고) |
| 1411 | 엑셀다운로드사유 | `log/exceldownload-log-list.jsp` | /admin/excel-download-logs | **완료**(표를 AS-IS 개인정보접근로그로 교체) |
| 1413 | 서울세외 부과연계 로그 | `log/gif-seoul-buga-list.jsp` (11컬럼) | /log/levy/seoul-buga | **완료** |
| 1414 | 서울 수납연계 로그 | `log/gif-seoul-sunap-list.jsp` (9컬럼) | /log/levy/seoul-sunap | **완료** |
| 1415 | 지방세외 부과연계 로그 | `log/gif-stnd-buga-list.jsp` (34컬럼) | /log/levy/stnd-buga | **완료** |
| 1416 | 지방세외 수납연계 로그 | `log/gif-stnd-sunap-list.jsp` (37컬럼) | /log/levy/stnd-sunap | **완료** |
| 7208 | 문자전송이력 | `sms-log/list.jsp` | /admin/send-sms-logs | **완료**(원천=TIF_IPS_SNDNG_M) |
| 7210 | 포인트사용 정합성검증 | `point-check/list.jsp` | /reconciliation/order-point | **완료**(order·point에 조회API 신규, 아래 ★) |
| 1410 | 이메일 발송 | `mail/{list,form,detail}.jsp` | /email | **완료**(EMS 리포트는 미연동 - 아래 ★) |
| 1402 | 메세지 관리 | `message/{list,form}.jsp` | /message | **완료**(신규, menu_url 넣음) |
| 7209 | 배치 실행로그 조회 | `batch-log/list.jsp` | /batch-log | **완료**(기록 지점까지, 아래 ★) |

**진척 25 / 25 - 시스템관리 영역 완료(2026-10-03)**. 실제 메뉴 트리에서 `menu_type=3 AND path LIKE '시스템%'`로 뽑은 리프가 정확히 25개이고 전부 menu_url이 채워졌다(이 쿼리로 재확인 가능).

**★구조 갭 1413~1416 해소(2026-10-03) - 내 초기 판단이 틀렸던 건**: AS-IS는 **별개 화면 4개**(컬럼 11/9/34/37, 제목도 각각)인데 TO-BE는 `/log/levy` 한 화면("연계 로그 관리 (국세청 부과·수납)")으로 합쳐 놨었다. 원장에 "원천이 납부게이트웨이 연계라 그 라운드로 보류"로 적어뒀지만 **네 원천표가 이미 TO-BE donation 스키마에 전부 있었다**(전부 0행). 보류가 필요한 건 **쓰기(실제 연계)뿐**이고 조회 화면은 지금 분리 구현할 수 있었다. 사용자가 "시드데이터 생성해서 4개 따로 구현 가능?"이라고 물어 바로 진행했다.
- 원천: 1413 `donation.gif_seoul`(24col) / 1414 `donation.gif_etax_sunap`(15col) / 1415 `donation.g_next_buga_request`(32col) + `g_locgov` LEFT JOIN(`sgb_cd = administ_instt_code`) / 1416 `donation.g_next_sunap_response`(37col). **AS-IS 화면 컬럼과 1:1로 일치**한다.
- donation에 조회 전용 API 4개 신규(`/api/admin/levy-logs/{seoul-buga,seoul-sunap,stnd-buga,stnd-sunap}`, `LevyLinkLogRepository` 네이티브 쿼리 - 표에 PK가 없어 엔티티 없음). admin은 `LevyLogClient` + `LevyLinkLogController`(4라우트, GET+POST) + 템플릿 4개.
- `migration-admin-menu-1413-1416-levy-split.sql`로 menu_url 4개 분리. 기존 `/log/levy`(TO-BE가 만든 DONATION_LEVY 통합조회 + 백필)는 AS-IS에 없는 운영도구라 메뉴에서만 빠지고 경로는 남겼다.
- **검증용 시드데이터** `seed-donation-levy-link-logs.sql`(각 표 3행, 성공/실패 섞음, 재실행 안전 - 'SEED%' 키만 삭제). 날짜는 **오늘 기준**으로 만든다(네 화면 모두 날짜 기본값이 오늘이라 고정 날짜면 아무것도 안 보인다). 개발DB에 `administ_instt_code`가 있는 지자체는 '1234567'(서울특별시 종로구) 한 곳뿐이라 1415 첫 행만 지자체명이 차고 나머지는 빈칸이다 - LEFT JOIN 실패 모습까지 확인 가능. 컬럼 길이 제약 주의: `gif_seoul.error_cd(3)`, `gif_etax_sunap.rst_cd(3)`·`sunap_dt`는 **NOT NULL**(미수납도 빈 문자열), `g_next_sunap_response.lvy_no(6)`·`rcvmt_no(2)`.
- AS-IS verbatim 보존: 1414 **초기화 버튼이 서울'부과' 화면으로 이동**(복사 흔적), 1416 검색폼·목록의 summary/caption이 "지방세외 **부과** 로그"(수납 화면인데), 데이터없음 colspan이 1415는 14·1416은 12(컬럼 34/37), 1415·1416은 결과가 있을 때만 페이지네이션(pagination-wrap 중첩), 결과여부 select title이 "아이디". 날짜 필수 검증 문구도 화면별로 다르다("등록일자/부과일자/수납일자는 필수입니다").

**공통코드 데이터 대조 결과(2026-10-02):** 앱이 읽는 테이블은 `admin.admin_common_code`(컬럼 `code_language`)이고, AS-IS 테이블명 그대로인 `admin.op_common_code`(컬럼 `language`)는 내가 AS-IS export를 적재해 둔 **대조용 사본**이다. AS-IS 71개 code_type/1091행이 이미 전부 이식돼 있었고 행 차이는 `ORDER_STATUS` 98/99(주문취소완료) 2건뿐이라 보충했다(`migration-admin-common-code-asis-gap.sql`). TO-BE 전용 7개 code_type(BSNS_PURPS_CODE/DELIVERY_CARRIER/MANAGER_STATUS/NOTICE_CATEGORY/POLICY_TYPE/POPUP_TYPE/SETTLEMENT_STATUS)은 의도적 추가라 유지. `POP_ADMIN`의 period/period2는 운영 중 바뀌는 팝업기간 값이라 동기화 제외.

**AS-IS 자체 결함 - 고치지 않고 보존했으니 운영 확인 필요(발견 2026-10-02):**
0. **[수정함 - AS-IS와 의도적으로 다름]** 약관관리 목록 제목이 AS-IS에서는 "이용후기(리뷰)관리"로 뜬다. `config/policy/list.jsp`가 `${op:message('M00756')}`(=이용후기(리뷰)관리), summary/caption에 `M00459`(=이용후기(리뷰))를 쓰기 때문(리뷰 화면 복사 흔적). **사용자가 "고치는 게 맞다"고 확인해서** 같은 기능의 상세화면 `form.jsp` 방식 - 제목은 리터럴 "약관관리", caption은 `M00206`(=약관관리) - 에 맞췄다. AS-IS verbatim 원칙의 예외이므로 템플릿에 사유를 주석으로 남겼다.
0-2. **`policy/form.jsp`의 `Link.list()`가 동작하지 않는다** - `op.link.js`가 AS-IS 어디에서도 로드되지 않는다(inc_head.jsp의 JS 목록에 없음). `Link.list`의 폴백이 `location.href = url`이라 그 동작으로 대체했다.
0-3. `Policy.getPolicyTypeLabel()`에 `'6'` 분기가 없어 "개인정보 수집·이용 동의"는 목록 정책구분이 빈칸이다(검색/등록 라디오에는 있음). `'1'`도 라디오는 "개인정보처리방침", 라벨 메서드는 "개인정보취급방침"으로 다르다. 둘 다 AS-IS 그대로 보존.

**AS-IS 자체 결함 2건(권한그룹):**
1. 권한그룹 **생성**: `user-group/form.jsp`의 authority hidden에 name이 없어 폼은 authority를 안 보낸다. **[2026-10-06 정정]** 이걸 처음엔 "생성 불가 결함"으로 오판했으나, 사용자가 AS-IS에서 "test" 그룹을 실제로 생성해 보여줬다(authority=`ROLE_ADMIN_104`). AS-IS `RoleServiceImpl.insertRole`(바이트코드 확인)이 **`"ROLE_ADMIN_" + SequenceService.getId("ROLE_ADMIN")`으로 authority를 서버에서 채번**한다 - 폼이 안 보내도 서버가 만든다. TO-BE도 동일하게 서버 채번(기존 `ROLE_ADMIN_<숫자>` 최대값+1)으로 **생성이 되게 고쳤다**(`RoleAdminService.create(roleName, roleDesc)`). 교훈: 바이트코드/시퀀스까지 안 보고 JSP만 보고 "불가"로 단정한 오류.
2. 권한그룹 **삭제** 버튼은 JSP에서 `<%-- --%>` 주석처리돼 노출되지 않는다 → TO-BE도 숨김 유지(엔드포인트만 보존). [[asis-porting-principles]]
또한 1404/1405가 둘 다 `/admin/roles`를 가리키고 있어 1404를 `/admin/roles/matrix`로 바로잡았다(`migration-admin-menu-1404-roles-matrix.sql`).

**★op_menu 트리 구조를 AS-IS와 동일하게 교정(2026-10-02, `migration-admin-menu-tree-asis-shape.sql`)** - 메뉴관리 화면이 이 구조에 의존해서다:
- AS-IS는 `menu_id=0`('root') 행이 있고 최상위 메뉴의 `menu_parent_id=0`이다. TO-BE는 root 행 없이 최상위가 NULL이었다(전수 대조의 "트리 불일치 21건"이 이것 - 표현차가 아니라 실제 차이였다). AS-IS 계층 쿼리가 `START WITH MENU_ID='0'`이라 root 없으면 트리가 전개되지 않는다.
- `menu_type`(1=상단 2=LNB섹션 3=링크)이 183행 전부 NULL이었다 → 깊이대로 1/21건, 2/41건, 3/121건 채움. 이게 없으면 ID 채번(레벨별 MAX+1000/+100/+1)과 목록 들여쓰기가 깨진다.
- 코드: 최상위 판정을 `Menu.isTopLevel()`(menuId≠0 AND parent가 null이거나 0)로 통일해 4곳 교체(MenuService.navTreeFor, MenuAdminService×2, RoleAdminService.matrixFor). `MenuService.topMenuIdOf`도 root까지 올라가 0을 반환하지 않도록 top에서 멈추게 수정.
- `Menu.menuId`는 `@GeneratedValue` 시퀀스를 **제거**하고 직접 배정으로 바꿨다(AS-IS 채번 규칙을 써야 트리 위치가 ID에 드러난다).
- PostgreSQL엔 CONNECT BY가 없어 `MenuTreeRepository`의 재귀 CTE로 대체(경로·자식수·ORDER SIBLINGS 포함). 183행 정상 확인.

**★`MENU_<id>` 문구코드는 신뢰하지 말고 확인 후 쓸 것** - `MENU_1409`가 문구표에 "엑셀 다운로드 사유관리"로 잘못 등록돼 있다(1409는 메뉴관리). AS-IS menu/list.jsp도 거기에 `MENU_1415`(=지방부과로그관리)를 써서 틀렸다. 검증된 것: MENU_1401/1404/1405는 정확. 쓰기 전에 `select message from admin.op_common_message where id='MENU_<id>'`로 확인하고, 틀리면 AS-IS h3의 한글 리터럴을 쓴다.

**★설문관리(7207)는 데이터모델 자체를 AS-IS로 교체했다(2026-10-02)** - 화면만 고칠 수 없는 케이스였다:
- AS-IS 4개 표: `G_QESTNAR`(설문: 제목/목적/기간/대상 srvy_trgt U=대민 M=관리자 S=답례품제공자) + `G_QUSTNR_QESITM`(문항: qestn_ty_code **rtype=객관식 stype=주관식**, **parent_sn=연계질문**, answer_choise_co) + `G_QUSTNR_IEM`(선택지) + `G_QUSTNR_RSPNS_RESULT`(응답).
- TO-BE는 문항을 자유서술형으로 **단순화한 `op_qustnr`/`op_qustnr_qesitm`/`op_qustnr_rspns`를 따로 만들어** 썼다 → 선택지·주관식 구분·연계질문·설문대상이 전부 없어 AS-IS 화면을 만들 수 없었다. **AS-IS 4개 표는 이미 TO-BE DB에 존재**했고(내가 처음 `%qustnr%`로만 찾아 `g_qestnar`를 놓쳤다) 6개 표 전부 0행이어서 그대로 갈아탔다.
- `QestnarRepository`(네이티브 SQL, 메서드명을 AS-IS 쿼리 id와 동일하게) + `QestnarAdminService`(설문 upsert → 미제출 문항 제거 → 문항 upsert → 미제출 선택지 제거 → 선택지 upsert) + 컨트롤러(JSON 본문 POST, `{isSuccess}` 응답). 채번도 AS-IS대로(설문 MAX+1, QESTN_SN 설문내 MAX+1, IEM 문항내 MAX+1).
- **storefront 참여 API(`SurveyApiController`)도 같은 표로 옮겼다** - 안 옮기면 관리자가 등록한 설문이 이용자 화면에 안 보이는 split-brain이 된다. 문항에 선택지가 생겼으므로 DTO에 `choices`가 추가되고 응답은 `qustnrIemSn`+`respondAnswerCn`을 받는다. **storefront Vue SPA가 이 DTO를 쓰므로 그쪽 수정이 남아 있다(미확인).**
- 쓰이지 않게 된 `op_qustnr*` 전용 엔티티/리포지토리 6개는 삭제했다(이름이 `Qestnar`와 혼동됨). **표 자체는 DROP하지 않았다**(0행, 파괴적 작업이라 사용자 판단 필요).
- AS-IS 오타 보존: `form.jsp`의 취소 버튼이 `class="btn btn-defualt btn-small"`(default 오타)라 AS-IS에서도 스타일이 안 먹는다 - verbatim 유지.

**★이메일 설정(1312) - AS-IS는 목록 화면을 쓰지 않는다(2026-10-02)**: `/mail-config/list`가 첫 템플릿의 등록/수정 화면으로 **리다이렉트**한다("cjh - 2014.05.27 리스트 페이지 삭제" 주석과 함께 본문 전체가 주석처리). 템플릿 전환은 폼 상단 `ul.mail_list`로 한다. 예전 TO-BE는 목록을 두고 "AS-IS가 우연히 지운 것"이라 적어뒀는데 근거 없는 판단이라 AS-IS대로 되돌리고 `mail-config/list.html`을 삭제했다.
- **템플릿 목록도 발명이었다**: TO-BE가 공통코드 `ORDER_STATUS`로 목록을 만들어 써서 회원가입·임시비밀번호·문의답변·휴면안내·관리자 권한 승인/거절 템플릿이 아예 없었다. AS-IS는 `MailTemplate.getTemplateCodes()`의 **고정 10종**(order_deposit_wait/order_cready_payment/order_delivering/expiration_point/member_join/pwsearch/qna_complete/member_sleep/manager_request_approval/manager_request_reject) → `MailTemplateCodes`로 순서까지 옮겼다.
- **표 컬럼 13개 누락**: AS-IS `OP_MAIL_CONFIG`는 23컬럼인데 TO-BE는 10컬럼이어서 화면의 "발송여부"(buyer_send_flag/admin_send_flag)조차 저장 불가였다 → `migration-admin-mail-config-asis-columns.sql`로 보충(23컬럼 확인).
- 메일 대체코드 팝업은 AS-IS 9개 `*Mail.getMap()`을 `MailTemplateCodes.changeCodes()`로 옮겼고, `{under_score}` 변환규칙(AS-IS `StringUtils.convertToUnderScore`)까지 재현했다.
- AS-IS 결함 보존: 삭제 엔드포인트가 `@PostMapping`인데 (지금은 죽은) 목록 JS가 `location.href` GET으로 불러 405가 난다 - TO-BE는 양쪽 메서드를 받아 둔다.

**배송업체 관리(1305)**: 표는 order 서비스 `ord.op_delivery_company`(컬럼은 AS-IS와 동일)이고 admin은 `DeliveryReturnClient`로 cross-service CRUD. order API가 전체목록 조회 + 단건 삭제만 주므로 검색·화면출력(10~1000)·페이징은 admin 메모리에서, 선택삭제는 id별 반복 호출로 구현(order 쪽 API는 수정하지 않았다). AS-IS 폼의 전송방법(sendFlag 라디오)·송장번호 파라미터 행은 주석처리 상태라 hidden으로만 넘긴다.

**★메인 배너 관리(7203) - 원장이 틀렸던 건(2026-10-02)**: 7203의 실제 뷰는 `user-login-banner/form.jsp`가 아니라 **`banner/main/{list,form}.jsp`**다(`MainBannerManagerController./index`가 `ViewUtils.getView("/banner/main/list")`를 리턴). `user-login-banner/form.jsp`는 별도 컨트롤러(`UserLoginBannerManagerController` loginWeb/loginMobile)의 PC/모바일 로그인 배너 화면이고 메뉴 7203과 무관하다.
- **TO-BE가 AS-IS의 이미지 업로드를 발명한 텍스트 입력으로 대체**해 뒀다: `op_main_banner`의 `pc_file_name`/`m_file_name`/`pc_org_file_name`/`m_org_file_name` 4컬럼을 엔티티가 매핑하지 않고 `imageUrl` 입력칸을 쓰고 있었다 → 4컬럼 매핑 + `MainBannerImageStorageService`(저장/스트리밍, 20MB·jpg/jpeg/gif/png) 추가, AS-IS대로 `/banners/mainBanner/pc|m/{bannerId}`로 이미지를 내려준다.
- 라우트도 AS-IS 모양으로: 목록 `/banners`, 등록 `/banners/create`, 수정 `/banners/edit/{id}`, 저장은 FormData ajax(`/banners/create`·`/banners/edit`), 순서 저장 `/banners/change-display-order`("bannerId|순서" 배열). AS-IS에 없던 `POST /banners/{id}`(imageUrl 기반)는 제거.
- 목록은 **등록분 뒤를 10행까지 빈 행으로 채우고 그 자리에 등록 버튼**을 두는 AS-IS 구조이며, AS-IS가 등록 버튼을 3번째 td(사용여부 칸)에 둔 것까지 그대로다.

**Batch Job(1403)**: 표 컬럼은 AS-IS와 동일. 검색(메서드명/작업명·트리거종류·배치상태)·11컬럼·선택삭제·1250x310 등록/수정 팝업(ajax) 복원. 목록의 트리거종류/배치상태/적용여부는 AS-IS 쿼리의 CASE 라벨(심플·크론 / 실행중·정지 / 적용전·적용완료)을 엔티티 `@Transient` getter로 옮겼다. AS-IS에 없던 배치상태 토글 버튼은 제거. **"농협배치테스트" 버튼이 호출하는 `ngDonationBatchService.getNoBugaLocgovList()`(농협 미부과 지자체 조회)는 TO-BE에 미이식** - 성공으로 꾸미지 않고 사유를 알리는 응답으로 두었다(버튼은 AS-IS대로 노출). AS-IS 폼 JS가 `input[name=triggerType]`으로 select를 읽어 그 검증이 항상 통과하는 것도 verbatim 유지.

**ISMS관리(1105)**: AS-IS는 구분(공통/관리자/회원)별 rowspan 묶음 표 하나이고 **모든 입력칸을 JSON 배열로 모아 한 번에** 저장한다. 예전 TO-BE는 행 단위 폼 저장 + 화면에 없는 사용여부 체크박스를 받았는데 AS-IS 방식으로 되돌렸다(rowspan 계산은 컨트롤러가 각 구분 첫 행에만 typeName/typeCount를 채워 내려준다).

**★로그 계열 3화면(2026-10-02)**
- **6401 관리자로그관리**: 표(`op_login_log`)는 AS-IS 컬럼과 동일. 검색(로그인ID/접속IP·접속일 범위·성공여부·권한그룹)·화면출력·7컬럼·페이징 복원 + **상세화면**(로그인ID/권한그룹/접속IP) + 상세 안의 **메뉴사용이력 조각**(ajax, `op_manager_action_log`). AS-IS 핵심 규칙: 상세는 그 로그인 시각 ~ **같은 계정의 다음 로그인 시각**까지를 본다(`LoginLogRepository.findNextLoginDate`). 권한그룹명은 로그인ID→매니저→authority→`op_role.role_name`으로 얻는다(AS-IS는 OP_USER_ROLE 조인). 예전 TO-BE는 최근 200건만 뿌리고 액션이력을 AS-IS에 없는 독립 화면으로 분리해 뒀다(그 화면들은 대응 메뉴가 없어 그대로 남겨둠).
- **1408 사용자로그관리**: 같은 구조, 권한그룹 컬럼 없는 6컬럼. 사용자 로그인 로그는 member 서비스 소관이라 `MemberClient.loginLogs()`로 전체를 받아 admin에서 걸러 페이징한다(member API에 검색 파라미터가 없어서 - member는 수정하지 않았다). **상세(회원 메뉴사용이력)는 미연결** - 로그인 세션 단위 조회 API가 member에 없어 "관리" 칸을 '-'로 두고 템플릿에 사유를 주석으로 남겼다.
- **1411 엑셀다운로드사유 관리**: AS-IS는 **개인정보 접근로그**(`OP_PRIVACY_ACCESS_LOG` + `_HIST`)를 읽는다(AS-IS 컨트롤러가 `PrivacyLogParam`을 쓴다). 두 표가 TO-BE DB에 AS-IS 컬럼 그대로 있어 엔티티·리포지토리를 새로 만들고 화면을 AS-IS로 되돌렸다 - 8컬럼, 사유 50자 절단 + 전문 팝업, 변경이력 2건 이상이면 이력보기 팝업, 등록일 기본 오늘, 기본 20건, **ROLE_ADMIN_1~4가 아니면 본인 것만**.
  - **쓰기 쪽 2026-10-03 해결** - 사용자가 "쓰기 쪽을 op_privacy_access_log로 옮기기"를 택해 AS-IS 2단계 흐름(사유 모달 → 접근로그 → 다운로드)으로 이전했다. 아래 ★엑셀다운로드 기록 지점 항목 참고.

**★이메일 발송(1410) - 2026-10-03**: 예전 TO-BE는 "관리자에게 즉시 메일 한 통"으로 축소돼 있었고 **발송대상 코드를 지어냈다**(A:전체관리자/D:ROLE_ADMIN/O:ROLE_OPERATOR) → AS-IS 코드 **A:권한별 S:답례품 E:개별 L:로그인인증**으로 되돌렸다. 발송시점(즉시/지정+시·분)·첨부파일(12종 10MB)·스마트에디터(기본 메일틀 `EmailDefaultContent.HTML`에 verbatim)·개별수신자 검색·목록 검색/페이징·상세(발송결과·발송인원)·첨부 다운로드 전부 신규. 표 3개(`op_email`/`op_email_detail`/`op_email_file`)는 AS-IS 컬럼과 동일해 DDL 불요.
- 발송은 AS-IS대로 2단계: `POST /email/form`(등록, STATUS='R') → 응답 emailId로 숨은 iframe에 `POST /email/send`. 수신자는 30,000명 단위로 끊어 EMS에 요청(`EmsMailClient`, AS-IS sendEmailData의 JSON 본문·필드명 그대로, `ghlove.integrations.ems.*`, enabled=false 모크).
- **EMS 리포트(EMS_REPORT/EMS_REPORT_DETAIL)는 TO-BE에 없다** - AS-IS는 별도 datasource(CUBRID `ems`, emsDS)로 붙어 발송결과 집계와 발송인원 명단을 읽고 그걸로 상태를 C(전송완료)/P(전송실패)/T(일부성공)로 갱신한다. 그 DB가 없어 집계는 비어 있고 상태는 R/S/F에 머문다 → 상세화면의 발송결과·발송인원 영역이 뜨지 않는다(AS-IS에서 리포트 없을 때와 동일). 갱신 규칙은 `OpEmailService.updateEmailStatus`에 AS-IS 그대로 보존.
- 대상 조회 경로: 권한별=`op_manager.authority`(AS-IS는 OP_USER_ROLE 조인, TO-BE는 매니저가 authority 직접 보유. 정상 상태코드는 AS-IS '9'↔TO-BE 'ACTIVE'), 개별=`MemberAdminClient.search(srchKey=USER_NAME)` 후 **이름 완전일치**만(AS-IS는 암호화 이름 완전일치), 답례품=**gift에 조회 전용 API 신규** `GET /api/admin/sellers/email-send-targets`(AS-IS sendSellerUserList 조건 `STATUS_CODE=2 AND ITEM_APPROVAL_TYPE=1`). `ITEM_APPROVAL_TYPE`은 일부러 gift `Seller` 엔티티에 매핑하지 않았다 - 컬럼 DEFAULT '1'을 admin 등록폼이 NULL로 덮으므로 네이티브 쿼리로만 읽는다.
- **AS-IS 결함 3건(이 화면에 몰려 있다) - 셋 다 사용자 확인 후 고쳤다**: ① **검색어가 동작하지 않았다** - 쿼리는 `searchType`이 'S'/'C'일 때만 제목 LIKE를 거는데 화면 select는 SUBJECT/CONTENT를 보낸다(게다가 'C'=내용 분기마저 SUBJECT를 LIKE한다). → 화면 값대로 제목은 SUBJECT, 내용은 CONTENT를 보도록 **고쳤다**(2026-10-03). ② 검색 버튼이 `search()`를 부르는데 그 함수가 어디에도 없어 AS-IS에선 눌러도 에러만 난다(= 동작하는 등록일 범위 검색조차 못 쓴다) → 폼 submit으로 **살렸다**. ③ 화면출력 select에 change 핸들러가 없어 AS-IS에선 무효 → 다른 운영관리 목록화면 방식대로 **살렸다**. 또 list.jsp의 `<h3><span></span></h3>`가 비어 제목이 안 보여 form/detail과 같은 "이메일 발송"으로 채웠다. No. 칸에 행번호가 아니라 EMAIL_ID를 찍는 것, 취소 버튼의 `btn-defualt` 오타는 verbatim 유지.

**★메세지 관리(1402) - 신규 구현(2026-10-03)**: AS-IS `/opmanager/message`는 **OP_COMMON_MESSAGE 자체를 관리하는 화면**이다(ID 하나당 한국어/일본어 한 행으로 피벗). TO-BE엔 화면도 menu_url도 없었다 → `migration-admin-menu-1402-message.sql`로 `/message` 설정. 검색구분(아이디/메세지)·출력수·5컬럼(체크박스/순번/아이디/한국어/일본어)·선택삭제·등록·수정. **등록은 ID 입력 여부로 갈린다** - 비우면 `M`+5자리 자동채번(AS-IS `CONCAT('M', SUBSTR(CONCAT('00000', SUBSTR(MAX(ID),2,5)+1), -5))`, MENU_ 제외. 현재 다음값 M01705), 적으면 그 ID로 ko/ja 2행 INSERT(MENU_xxxx 직접 등록용). 저장·삭제 후 `CommonMessageService.reload()`로 문구 캐시를 비운다(AS-IS도 매번 reload). **AS-IS 결함 보존**: 수정은 언어별 UPDATE라 ja 행이 없는 ID(2,034개 중 1,936개)는 일본어 입력이 조용히 버려진다 - INSERT로 보충하면 AS-IS에 없던 ja 행이 생기므로 그대로 뒀다. 피벗 쿼리는 네이티브(self left join), `[[asis-sources-and-data]]`의 사전과 같은 표다.

**★배치 실행로그 조회(7209) - 신규 구현(2026-10-03)**: AS-IS `/opmanager/batch-log/list`. `migration-admin-menu-7209-batch-log.sql`로 `/batch-log` 설정. 작업명 검색 + 실행날짜 범위(둘 다 비면 **오늘**) + 출력수(10/50/100/200/500) + 7컬럼. 원천은 `admin.op_batch_execution`(배치구분/실행날짜/시작·종료시간/결과/메시지, **PK 없음** → 엔티티 없이 네이티브 쿼리 전용 `BatchLogRepository`)이고 작업명은 `op_batch_job`과 **LIKE 조인**(`BE.BATCH_TYPE LIKE '%'||BJ.JOB_METHOD||'%'`, 외래키 아님 - AS-IS 그대로). AS-IS verbatim: h3가 메뉴명이 아니라 "배치 로그", 작업검색의 "메서드명" option·트리거종류 칸·선택삭제/신규등록 버튼은 JSP에서 주석처리돼 화면에 없음, 날짜칸 title이 "주문일자 시작일/종료일"(복사 흔적, 안 보임).
- **★기록 지점까지 구현(2026-10-03)**: admin에 내부 전용 `POST /api/admin/batch-executions`(`BatchExecutionInternalApiController`, 공유시크릿 X-Internal-Secret, 같은 키면 UPDATE·없으면 INSERT = AS-IS MERGE 대응)를 두고, @Scheduled 배치가 있는 **donation·point·order 세 서비스에 `BatchExecutionReporter`**를 넣어 보고한다(서비스별 독립 Gradle 프로젝트라 공유모듈이 없어 같은 클래스 3개). 보고 실패는 배치를 죽이지 않고 로그만 남긴다. point·order에는 `ghlove.admin-service.base-url`을 새로 추가했다.
- **트랜잭션 함정**: `@Scheduled`가 `@Transactional` 메서드에 같이 달려 있으면 같은 빈 안에서 감싸 호출할 수 없다(self-invocation → 프록시 미경유 → 트랜잭션 무효). 그래서 트리거를 **별도 빈**으로 분리했다(`DesignatedProjectBatchScheduler`, `PointExpirationBatchScheduler`; order는 이미 `CouponBatchScheduler`로 분리돼 있었다). 원래 서비스 메서드는 수동 실행 API용으로 공개 유지.
- **`op_batch_job` 등록이 없으면 7209에 안 보인다** - 화면이 `BATCH_TYPE LIKE '%'||JOB_METHOD||'%'`로 작업명을 조인하기 때문이다. 실제로 도는 배치는 3개인데 1건만 등록돼 있어 나머지를 등록했다(`seed-admin-batch-job-registrations.sql` - 1001 `PointService.runExpirationBatch`, 1002 `CouponBatchScheduler.runDailyCouponBatch`). **보고하는 batchType 문자열은 job_method와 같게 유지해야 한다.** 쿠폰 배치는 `ghlove.coupon.enabled=false`라 빈이 등록되지 않아 현재 돌지 않는다(등록만 해 둠).
- 실측: 세 batch_type으로 행을 넣고 화면 쿼리를 돌려 작업명·정상/오류 라벨이 전부 정상 조인됨을 확인(검증 행은 삭제).

**★포인트사용 정합성검증(7210) - 2026-10-03, 사용자 승인 후 order·point에 조회전용 API 추가**: AS-IS는 주문번호 하나에 세 곳의 금액을 모아 **결제금액 − 입금대기금액 − 취소금액 == 사용완료 포인트**면 금액일치로 판정하고, 목록 맨 위에 합계 행(정합여부 칸에 차액)을 둔다. 소유 서비스가 달라 한 쿼리 조인이 불가해 **order `GET /api/admin/reconciliation/order-amounts`** + **point `GET /api/admin/reconciliation/cntr-used-points`**를 새로 만들고 admin(`PointCheckService`)이 주문번호로 합친다.
- **TO-BE 주문 모델이 달라 금액 대응을 바꿨다**: `ord.op_order_item`은 **7개 컬럼만 있는 미완성 잔재(0행)**로 `sale_price`/`etc_amt`/`created_date`/`locgov_code`가 없어 AS-IS 쿼리를 돌릴 수 없다(반면 `op_order_item_hold`는 118컬럼 완전체, 0행). 실제 주문은 `ord.od_order`다 → 결제금액=`point_amount`, 취소금액=상태 `CANCELLED`, 입금대기금액=0(TO-BE에 해당 상태 없음), 정산금액(`op_remittance_detail`)·미정주문건수(`op_order_item_hold`)는 쌓이는 게 없어 0. 주문상태는 TO-BE 이름(CONFIRMED 등)을 AS-IS 라벨로 매핑.
- **사용완료 포인트는 `point.g_cntr_use_point`(use_se_code='1')** - AS-IS와 같은 표다. 멀티아이템 주문은 ORDER_CODE가 `주문번호#항목번호`라 `split_part(order_code,'#',1)`로 묶는다(AS-IS는 주문번호 그대로였다).
- 예전 TO-BE는 AS-IS에 없는 대사(`od_order.point_amount` ↔ **포인트 원장** `PT_POINT_LEDGER`)를 검색·컬럼 없이 보여줬다. `ReconciliationService`는 그 점검이라 화면 연결만 끊고 남겨 뒀다(SAGA 드리프트 탐지용).
- 실측: 개발DB 8건 중 3건이 금액불일치로 잡힌다(PARTIALLY_CONFIRMED 주문 2건 + 포인트사용 기록 없는 1건) - 화면이 실제로 작동한다는 뜻.

**★엑셀다운로드 기록 지점을 AS-IS와 같게 옮김(2026-10-03, 사용자 결정)**: AS-IS 흐름은 **2단계**다 - ① 다운로드 버튼 → 사유 모달(`include/modal/privacy-access.jsp`)이 `POST /common/opmanager/privacy-access-log`로 사유를 보내 `OP_PRIVACY_ACCESS_LOG` + `_HIST` 1건을 남기고 ② 성공하면 그때 다운로드 URL로 이동. 즉 **기록은 다운로드를 수행하는 서비스가 아니라 admin이** 한다. order의 CSV export가 `ord.OD_EXCEL_DOWNLOAD_LOG`에 쓰던 것을 제거하고(사유 파라미터도 더 이상 안 보냄) admin으로 옮겼다.
- 신규: `PrivacyTask`·`PrivacyAccess`(AS-IS enum **verbatim** 100행 + 이식된 화면에만 `tobePattern` 추가), `PrivacyAccessLogService`, `PrivacyAccessLogApiController`(`privacy-access-log`, `privacy-access-log-update`), `fragments/privacy-access.html`(모달). 사유 5글자 이상 + 동의 체크 필수, 수정은 **등록 본인만**.
- 모달을 붙인 곳: 주문관리(`/admin/orders/export`), 답례품 후기(`/admin/gift-reviews/export`), 운영유지관리 SR(`/admin/maintenance/export`). AS-IS `PrivacyAccess`에 항목이 없는 다운로드(답례품 목록·명예등급 열람이력)는 AS-IS도 로그를 남기지 않으므로 그대로 뒀다.
- **1411 화면 자체의 버그 3건도 이때 고쳤다**: ① `PrivacyAccessLog`/`Hist` 엔티티가 `manager_id`/`user_id`(bigint)를 String으로, `created_at`(timestamp)을 String으로 매핑하고 있었다(표가 비어 있어 안 드러남) ② AS-IS 목록 쿼리의 **`WHERE TASK='엑셀 다운로드'` 필터가 빠져** 있었다 ③ 본인것만 보기 필터를 `loginId`로 비교했는데 AS-IS는 `MANAGER_ID`(숫자)다. 관리자ID 칸은 MANAGER_ID로 찾은 `op_manager.login_id`이고 검색구분 관리자ID는 **완전일치**, 사유구분은 `EXCELDOWNLOAD_REASON_TYPE` 라벨이다(AS-IS 모달에 사유구분 select가 없어 실제로는 빈칸 - verbatim).
- `admin.op_privacy_access_log.id`에 시퀀스가 없어 INSERT 자체가 불가했다 → `migration-admin-privacy-access-log-seq.sql`로 생성(AS-IS는 `OP_PRIVACY_ACCESS_LOG_SEQ`, initialValue 2152877은 운영 누적값이라 TO-BE는 1부터).
- `admin.ExcelDownloadLog`(+Repository)는 AS-IS에 없는 표라 `@Deprecated` 표시만 하고 남겼다(과거 행 보존, DROP은 사용자 판단).
- 주문관리 목록의 `downloadExcel()`이 `th:inline="none"`이어서 **검색조건이 항상 빈 값으로 나가 현재 검색조건이 무시**되던 것도 함께 고쳤다.

**★[2026-10-06 3차] 1404·1405·1406을 AS-IS 개발DB 실데이터와 대조해 교정**
([[asis-sources-and-data]]으로 직접 조회. 사용자 지적: "권한 그룹이 as-is 9개인데 to-be 8개,
답례품 관리자가 빠져있다 / 처음 접속하면 답례품관리자가 선택돼 있어야 한다").
- **AS-IS `OP_ROLE` 은 ROLE_ADMIN_1~10 열 행**이다(9=답례품관리자, 10=지정기부사업자).
  TO-BE엔 1~8만 있었고 `created_date` 가 전부 비어 있었다. 1404 JSP가 **지정기부사업자만**
  `<c:if>` 로 빼기 때문에 AS-IS 화면엔 9개가 보이고, `ORDER BY CREATED_DATE DESC` 의 1위가
  걸러져 **첫 행 = 답례품관리자**가 되어 기본 선택된다. `migration-admin-op-role-asis-sync.sql`
  로 2행 추가 + created_date 10건 + `role_desc` 를 AS-IS verbatim(지어낸 설명문이었다)으로.
  정렬·집합 10행 전부 AS-IS와 일치 확인.
- 코드 결함 2건: `firstAdminAuthority()` 가 **걸러내기 전** 첫 행을 집어 안 보이는
  지정기부사업자를 선택하게 돼 있었고, 템플릿은 `th:each`+`th:if` 라 **건너뛴 행도 `i.count`
  에 포함**돼 번호가 2부터 시작했다(AS-IS는 그려진 행만 세는 rowNum). → 걸러낸 목록을
  서버에서 내려준다(`RoleAdminService.matrixRoleRows`). **1405에는 이 필터가 없다**(10개 전부).
- **★오른쪽 메뉴권한 트리를 AS-IS 쿼리로 교체.** AS-IS `MenuMapper.getAllMenuList` 는 3단
  **전부 INNER JOIN + `STATUS_CODE='1'` + `MENU_TYPE` 1/2/3 고정**이다 → ① **미사용 메뉴는
  아예 안 나온다**(사용자 관찰이 정확했다) ② **자식 없는 1·2단 묶음도 안 나온다**.
  TO-BE는 status/type을 안 보고 트리를 직접 걸어 미사용 1단 9개·3단 7개를 노출했다.
  `MenuRepository.findAllMenuListForRightMatrix()`(네이티브, AS-IS SQL verbatim) + 중첩
  `MatrixNode` 로 교체. **데이터·순서는 원래 AS-IS와 같았다**(차이는 아래 5120 하나뿐).
- **AS-IS에 없던 메뉴 5120 "내부문의 관리" 제거**(`migration-admin-menu-5120-admin-inquiry-asis-shape.sql`).
  AS-IS에는 이 화면이 **답례품관리 > 관리자 문의 > 관리자 문의(16651, `/opmanager/qna-admin/list`)**
  로 있는데, TO-BE가 16651의 menu_url을 `/qna-admin`(=5112 공개Q&A와 중복)으로 잘못 넣고
  실제 화면(`/admin/internal-inquiry`)에는 새 메뉴 5120을 만들어 고객센터 아래 달아놨다.
  16651을 실제 경로로 돌리고 5120+권한 6건 제거 → **권한트리 119건이 AS-IS와 순서까지 완전 일치.**
- **1404 저장이 OP_ROLE을 갱신하지 않고 있었다.** AS-IS `RoleServiceImpl.updateRole`(바이트코드)은
  ①OP_ROLE의 ROLE_NAME·ROLE_DESC·UPDATED_DATE UPDATE ②권한 전체 DELETE ③체크분 INSERT 순이다
  (화면이 roleName/roleDesc를 hidden으로 보내는 이유). ①을 추가했고, 저장 후 응답도 AS-IS대로
  **M00406 flash + userAuthority 없는 리다이렉트**(= 선택이 첫 행으로 되돌아간다)로 맞췄다.
  ②가 무조건 전체 삭제라 **안 보이는 미사용 메뉴의 권한도 저장 시 사라진다 - AS-IS 동일, 유지.**
- **1406 시드 20건**(`seed-admin-manager-request.sql`): AS-IS는 8,910행인데 TO-BE가 1행이라
  검색·상태필터·페이징·이력 팝업을 눌러볼 수 없었다. 상태 100승인/200대기/300거절
  (AS-IS 코드표 `CFM_STATUS` 확인 - TO-BE 라벨은 맞았다), 신청구분 4종, 1인 다건 4명, 거절사유 포함.
  **`REQST_SE_CODE` 는 AS-IS가 6개**(ROLE_ADMIN_6/8/4/2 + 10·11은 use_yn='N')인데 TO-BE에
  지어낸 `LOCALGOV`·`PROVIDER`·`OPERATOR` 3개가 더 있어 `use_yn='N'` 으로 내렸다(행 삭제는 안 함).
  기존 1행의 `reqst_se_code='1'`(AS-IS에 없는 값)도 ROLE_ADMIN_6으로 교정.
  **AS-IS는 LOGIN_ID·OFCPS_NM·CTTPC를 암호화 저장**하는데 TO-BE는 평문이다 - 별도 과제.
- **미확인(사용자 클릭 필요)**: 1404 저장 실제 동작, 1406 검색·페이징(로그인 세션이 필요해
  런타임 확인 못 함). 1405 인원수는 AS-IS가 OP_USER_ROLE, TO-BE가 매니저 수라 값이 다르다(로직 차이 아님).
- **`op_menu_right` 전수 대조 = AS-IS와 완전 일치(503쌍).** 권한마다 +1 많았던 것(ROLE_ADMIN_1
  115 vs 114 등)의 **원인은 5120이었다** - 5120에 ROLE_ADMIN_1~6 권한이 하나씩 붙어 있었으므로
  5120을 지우자 1~6이 각각 -1 되어 AS-IS와 같아졌다(114/87/95/85/53/51, 7·8·10은 각 6).
  `(authority, menu_id)` **짝 단위로도 503쌍 전부 동일**하고, 고아 권한행 0건,
  미사용 메뉴를 가리키는 권한행 0건이다. (처음엔 "원인 미확인"으로 적었는데 5120 제거 **이전**
  수치를 비교한 착오였다 - 대조는 조치 **후** 수치로 할 것.)
  AS-IS 활성 3단 메뉴는 207개인데 TO-BE는 120개인데도 권한 집합이 일치한다 = AS-IS 권한이
  걸려 있는 메뉴는 전부 TO-BE에도 있다는 뜻이다.

**★[2026-10-06 4차] 1406 관리자 권한 승인관리 - 상태/이력 팝업을 AS-IS대로 재이식.**
사용자가 "팝업 이름이 AS-IS와 다르고 폼도 다르다"고 지적. 4축(JSP+매퍼+컨트롤러+ServiceImpl) 대조 결과
TO-BE가 **발명**한 것들을 제거했다([[asis-porting-principles]]):
- **상세(상태) 팝업**: 제목 `관리자 권한 요청 (승인)/(거절)`(TO-BE는 "관리자 권한 승인관리"였음),
  생년월일 추가, 부서+직위 한 행. **핵심**: AS-IS 승인은 관리자가 권한을 고르지 않는다 -
  신청구분(reqstSeCode)이 그대로 부여 권한(AS-IS `updateManagerRequestApproval`이
  `ROLE_OPMANAGER + reqstSeCode` 부여). TO-BE가 넣었던 **'부여 권한' 드롭다운 + '소속 지자체'
  입력은 발명 → 제거**. AS-IS대로 라디오(승인/거절) 1개 + 거절 시 거절사유 textarea,
  **AJAX 단일 `confirm` 엔드포인트**(POST `/admin/manager-requests/confirm`, confmSttusCode
  100/300, JSON `{isSuccess,data:{code}}`)로 교체. 기존 `/approve`·`/reject` 폼 2개 삭제.
- **이력 팝업**: 제목 `권한 상태 변경이력`(TO-BE "관리자 권한 신청이력"이었음), board_list 래퍼,
  빈 메시지 `데이터가 없습니다.`, **닫기(확인) 버튼** 추가. 작성자 칸은 AS-IS대로 변경 관리자
  **권한명+(로그인아이디)** - 컨트롤러가 `LAST_UPDUSR_ID`로 op_manager 조회해 `updaterLabels` 구성.
- **팝업 창 크기/이름 AS-IS로**: 상세 600×650, 이력 **800×650**(TO-BE 700×500이 틀렸었다).
- **승인/거절 통지(사용자 결정: 메일로 AS-IS 최대한 맞춤)**: AS-IS `sendMail` 이식 -
  op_mail_config 템플릿(`manager_request_approval`/`reject`) 있고 buyerSendFlag='Y'면
  토큰 치환(`{loginId}{adminRole}{rejectResn}`) 후 EmsMailClient로 발송(enabled=false면 로그만).
  `adminRole`은 AS-IS `getAdminRoleText`(신청구분→"구분 등급") 이식. **발명 배너(목록에 임시비번
  표시) 제거**. op_manager가 member와 분리돼 자격증명이 필요해 임시비번을 승인 메일에 실어 보낸다
  (분리 인증구조의 최소 추가, 사용자 승인). **미이식**: AS-IS의 per-user 수신동의(receiveEmail=='0')
  검사는 TO-BE MemberInfo에 그 플래그가 없어 생략(문서화).
- 빌드·기동(JPA EMF 정상, BeanCreationException 0)·가드테스트 통과. **런타임 클릭은 세션 필요로
  미확인** - 사용자가 재기동 후 상태 링크→승인/거절, 이력 링크 확인 필요.

**caption/summary는 화면에 안 보인다**(`opmanager.css:41 caption {display:none}`) - AS-IS가 caption에 엉뚱한 문구코드를 쓴 곳(배송업체 폼의 `M00055`=전체주문 내역 등)은 사용자에게 안 보이므로 verbatim 유지한다. 눈에 보이는 건 `<h3>`뿐이다.

**추가 공통부품:** `templates/common/popup-result.html` = AS-IS `ViewUtils.redirect(url, message, javascript)`의 팝업 응답(`alert(message)` → `opener.fnSearch()` → `self.close()`). AS-IS 운영관리 팝업 화면 전부가 이 패턴이라 재사용한다.

**★[2026-10-06] 이 영역 검색 쿼리 5건이 "검색어 비움" 경로에서 500이었다.** 아침에 사용자가
**팝업관리(1311) 목록**에서 실제로 맞았다 - `ERROR: operator does not exist: character varying ~~ bytea`.
`(:query is null or ... like concat('%', :query, '%'))`에서 Hibernate가 파라미터 타입을 못 정해
null을 bytea로 바인딩한 것이다. `cast(:x as String)`을 양쪽 occurrence에 넣어 고쳤고, 같은 패턴이던
`PopupRepository`(1311) · `PolicyRepository`(1308) · `OpEmailRepository`(1410) ·
`CommonCodeRepository`(1401) · `IpsSendingMasterRepository`(문자발송) **5개를 함께** 처리했다.
`= :x`·`>= :x` 조건은 타입이 잡히므로 손대지 않았다. 네이티브 쿼리와 SQL 조립 방식은 해당 없다.
상세·판정근거는 [[backend-db-pitfalls]]. **25화면 "완료" 판정은 화면 렌더만 본 것이고
"검색어 비움" 경로를 호출해 본 적이 없었다** - 이 영역 재점검 시 그 경로를 먼저 볼 것.

**★[2026-10-06 2차] 팝업관리(1311) 실사용 점검에서 치명 결함 3건.** "25/25 완료"는 화면 렌더만
본 판정이었고, **검색·페이징·등록화면이 실제로는 동작하지 않았다.**
1. **검색·페이징이 전부 500** - 저장을 목록 URL에 얹어(`POST /popups`가 목록검색+등록저장 양쪽)
   `IllegalStateException: Ambiguous handler methods`. AS-IS는 **저장 URL = 폼 URL**이고
   (`POST write` / `POST edit/{popupId}`) 목록 검색만 `POST list`다 → TO-BE도
   `POST /popups/new` · `POST /popups/{id}/edit`로 분리했다. 스캐너로 admin·5개 서비스 전수
   재확인 = 다른 충돌 없음(`scratchpad/find-ambiguous2.pl`).
2. **등록/수정 화면이 렌더링 중단** - 스마트에디터 조각의 AS-IS 주석 때문. 에디터와 달력이
   동시에 사라진 원인이고 **그 조각을 쓰는 16개 화면이 같이 깨져 있었다**.
   상세·가드테스트: [[admin-screen-pitfalls]]
3. **목록 건수 퍼블리싱** - `.count_title h5 span{display:block}`이라 숫자가 줄바꿈됐다.
   AS-IS는 h5에 평문만 두고 `<span>`은 h5 **밖**(출력수 select, `h5 + span`)에 둔다.
   `th:remove="tag"`로 **44개 화면 / span 62개** 일괄 교정.
또 업로드 규칙 누락(확장자·용량·형태전환 정리·파일삭제)이 이 영역 4개 화면에 있었다 -
`docs/upload-file-parity-audit.md`. **교훈: 이 영역 "완료" 판정은 전부 렌더만 본 것이다.
검색·페이징·저장 경로를 실제로 눌러봐야 한다.**

### admin-member-area-port-progress

> 회원관리 영역 8화면 AS-IS 이식 **완료**(7화면 이식 + 4701 이식대상없음) - AS-IS JSP/컨트롤러 매핑표, 구조갭(4402·4501) 해소, 담당자 사용여부 9/2↔ACTIVE/LOCKED 변환 규칙


[[asis-screen-port-procedure]] 절차로 진행한 **두 번째 메뉴 영역 = 회원관리**(리프 8개) - **2026-10-04 완료**(7화면 이식 + 4701은 AS-IS에서도 죽은 메뉴라 이식대상 없음). 첫 영역 시스템관리는 25/25 완료([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-system-area-port-progress)).

영역 목록은 실측으로 뽑았다 - `menu_type=3 AND top='회원관리'`인 리프 8개, 전부 menu_url 있음.

**AS-IS 경로 원장** (`ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` + `saleson.shop.user` 컨트롤러):

| id | 메뉴 | AS-IS JSP | AS-IS 컨트롤러(엔드포인트수) | TO-BE url | 상태 |
|---|---|---|---|---|---|
| 4101 | 일반회원관리 | `user/customer/{list,details,cntr-list,point-list}.jsp` + `user/popup/{customer-password-confirm,customer-info-access,customer-secede-write,delivery}.jsp` | `GeneralCustomerManagerController` `/opmanager/user/customer` (10) | /admin/members | **완료**(아래 ★) |
| 4105 | 탈퇴회원리스트 | `user/secede-user/list.jsp` + `user/popup/secede-reason-details.jsp` | `SecedeUserManagerController` (4) | /admin/secede-users | **완료**(아래 ★) |
| 4107 | 휴면회원관리 | `user/sleep-user/list.jsp` | `SleepUserManagerController` (4) | /admin/sleep-users | **완료**(아래 ★) |
| 4401 | 지자체관리 | `user/locgov/{list,form,edit,popup}.jsp` + `user/popup/locgov-point-list.jsp` + `juso/juso-popup.jsp` | `LocgovManagerController` (14) | /admin/locgovs | **완료**(아래 ★) |
| 4402 | 지자체담당자관리 | `user/locgov-charger/{list,edit}.jsp` | `LocgovPersonInChargeManagerController` `/opmanager/user/locgov-charger` (7) | /admin/person-in-charge/**locgov** | **완료**(분리, 아래 ★) |
| 4501 | 운영관리자 | `user/oper-charger/{list,edit}.jsp` | `OperPersonInChargeManagerController` `/opmanager/user/oper-charger` (6) | /admin/person-in-charge/**oper** | **완료**(분리, 아래 ★) |
| 4601 | 오프라인담당자 | `user/off-charger/{list,form,edit}.jsp` + `user/popup/charger-password-init.jsp` | `OffPersonInChargeManagerController` (13) | /admin/off-person-in-charge | **완료**(아래 ★) |
| 4701 | 답례품 관리자 | **없음**(AS-IS url `/opmanager/user/rtnpsnt/list`인데 컨트롤러·JSP가 존재하지 않는다) | - | /seller | **이식대상 없음**(아래 ★) |

**★구조 갭 4402·4501 완전 해소(2026-10-04)** - 아래 경과 참고. 두 메뉴 모두 자기 화면을 가리킨다(`/admin/person-in-charge/locgov`, `/admin/person-in-charge/oper`). 기존 통합화면 `/admin/person-in-charge`는 **어느 메뉴도 가리키지 않는 TO-BE 자체 화면**이라 지우지 않고 남겼다(시스템관리의 `/log/levy`·액션로그 화면과 같은 처리).

**★지자체담당자관리(4402) 완료 - 2026-10-04**: 검색구분·등록일 범위·사용여부·컬럼(권한에 따라 9 또는 10)·상세수정(ajax)·시도/시군구 연동 select 복원.
- **AS-IS 권한 분기(`adminRoleType`)를 그대로 재현**: SYS(ROLE_ADMIN_1~4)는 전체 지자체 + 지자체명 검색·컬럼이 있고 **지자체명이 링크**(아이디·이름은 평문), 지자체를 시도·시군구로 바꿀 수 있다. LOC(5·6)는 **자기 지자체만** 보이고 지자체명 컬럼이 없으며 아이디·이름이 링크다. 등급까지 갈린다 - **부관리자(6)는 회원구분·사용여부 라디오 disabled**, **주관리자(5)가 자기 자신을 볼 때는 사용여부만 disabled**. LOC가 다른 지자체 담당자를 열면 목록으로 되돌린다.
- **AS-IS 비활성 상태 보존**: 삭제 버튼은 권한 조건을 통과해도 `style="display:none;"`이라 **목록·상세 모두에서 안 보인다**. 엔드포인트는 남기고 버튼만 숨긴 채 옮겼다([[asis-porting-principles]]).
- 상위 지자체 목록은 AS-IS가 공통코드 **WDR**에서 가져오는데 TO-BE 공통코드엔 WDR이 없어 **지자체 목록에서 시도를 추렸다**(값 집합은 동일). 시군구는 AS-IS대로 ajax(`/locgov/{upperLocgovCode}/list`)로 채운다.
- `PersonInChargeAdminService.update`에 `locgovCode` 오버로드 추가. **주담당자 정원(2명) 검사를 "옮겨갈 지자체" 기준으로** 했다 - 다른 지자체로 이동하며 주담당자가 되는 경우 기존 지자체 기준으로 검사하면 정원을 우회할 수 있다.

**(경과) 구조 갭 4402·4501 (2026-10-04)**: 두 메뉴가 같은 menu_url(`/admin/person-in-charge`)이었다 - TO-BE가 두 화면을 `scope` 탭 하나로 **의도적으로 통합**해 뒀다(`docs/as-is-admin-gap-deep-audit-part2.md` D6/D9 권장사항). AS-IS는 **컨트롤러·JSP·검색조건·컬럼이 모두 다른 별개 화면**이라 분리 중이다(시스템관리 1404·1405와 같은 유형).
- **4501 분리 완료** → `/admin/person-in-charge/oper`(`migration-admin-menu-4501-oper-charger.sql`, `OperPersonInChargeAdminController` + `person-in-charge/oper-{list,edit}.html`).
- **4402는 아직 통합화면을 가리킨다** - 통합화면의 기본 scope가 `locgov`라 4402를 누르면 지자체담당자 내용이 나오므로 중간상태로도 앞뒤는 맞다. 4402 전용 화면을 이식할 때 `/admin/person-in-charge/locgov`로 바꾼다.
- 두 화면 차이(이식 시 참고): 대상권한 5·6 vs 1~4 / 삭제허용 1~5 vs **1·3만** / 주담당자 2명 제한 ROLE_ADMIN_5 vs ROLE_ADMIN_3 / 지자체 스코프·지자체명 검색·상위지자체(WDR)·하위지자체 ajax·"주관리자+중지→부관리자 강등"은 **locgov에만** 있음.

**★운영관리자(4501) 완료 - 2026-10-04**: 검색구분(아이디/이름/휴대폰)·등록일 범위(기본 오늘, 전체 포함)·회원구분 체크박스(전체/시스템 주·부/행안부 주·부)·사용여부(전체/사용/중지)·10컬럼·상세수정(ajax)·삭제(ajax) 복원. 4105·4107과 같이 **진입(GET)에서는 조회하지 않는다**.
- `Manager`에 **`phoneNumber`·`denyDate` 매핑 추가**(컬럼은 원래 있었고 매핑만 없었다 - 목록의 휴대폰·중지일자 컬럼용). `PersonInChargeAdminService.update`에 소속부서·직위 오버로드 추가(기존 호출부 영향 없음).
- 권한 게이팅: 수정/삭제 버튼과 소속부서·직위 입력칸은 **ROLE_ADMIN_1·3에만** 노출(AS-IS c:if 그대로). 응답 코드도 AS-IS대로 `SUCC`/`ERR_MAIN_CNT`/`ERR_NOT_ALLOW`/`FAIL`이고 본인 삭제 시 `isLogout=Y`.
- AS-IS verbatim: h3가 `MENU_4501`(운영관리자)이 아니라 **`MENU_1101`="운영자관리"**, colgroup col 2개(컬럼 10개), 아이디·이름·휴대폰은 필수표시(*)인데 **읽기전용**, 목록 버튼 class `btn-defualt` 오타, 중지일자는 '중지'일 때만 표시.
- **AS-IS 결함 1건 고침**: oper 목록의 삭제가 `locgov-charger/delete`로 POST한다(복사 흔적). 버튼이 ROLE_ADMIN_1·3에만 보이고 그 권한은 양쪽에서 허용되므로 관측되는 동작은 동일 - 화면이 분리된 TO-BE에서는 앞뒤가 안 맞아 자기 엔드포인트로 바로잡았다.

**★오프라인담당자(4601) 완료 - 2026-10-04**: AS-IS 13개 엔드포인트 전부(목록 GET/POST·엑셀·사용여부 일괄변경·등록 폼/처리·아이디중복확인·상세·수정·권한이관·삭제·비밀번호초기화 팝업) + 템플릿 4개(`off-person-in-charge/{list,form,edit,password-init}.html`). 이 영역은 소속이 지자체가 아니라 **지점(BANK_CODE + PSITN_NM)**이다(농협 창구 오프라인 기부접수 담당자).
- **권한분기가 이 화면의 본체다**: 목록은 ROLE_ADMIN_1~4·7만. **부담당자(ROLE_ADMIN_8)는 목록 대신 자기 상세로 리다이렉트**되고 그 외 권한은 홈으로. 상세도 8이 남의 것을 열면 자기 것으로 되돌린다. 오프라인담당자(7·8)가 조회하면 목록이 **자기 지점으로 스코프**된다. 상세는 SYS/주담당자에게만 수정·삭제·목록 버튼·구분 라디오·사용여부 행·비밀번호초기화 버튼이 보이고, 부담당자에게는 **'저장' 버튼 하나**뿐이다(사용여부 행 자체가 없다).
- **주담당자 승격 3분기**(AS-IS `getOffPersonInChargeMainCount` = 같은 **BANK_CODE** + STATUS_CODE=9 + 본인 제외): 요청자가 주담당자면 2명 초과 `ERR_MAIN_CNT` / 정확히 2명 + 대상이 남 → **`NEED_AUTH_SWAP`** / 그 외 저장. 요청자가 시스템·행안부 관리자면 **2명부터 바로 차단**(강등 대상을 지정할 방법이 없어서 - AS-IS 주석).
- **권한이관(`/auth-swap`)**: 대상을 주담당자로 올리면서 **요청자 본인을 부담당자 + 중지(DENY_DATE 기록)**로 내린다 → `AUTH_CHANGED_LOGOUT` → 즉시 로그아웃.
- **AS-IS 결함 1건 고침**: 등록(`insertOffPersonInCharge`)이 `String.valueOf(personInCharge.getStatusCode())`로 상태를 넣는데 등록 폼에 사용여부 입력칸이 없어 **statusCode(Long)가 항상 null → 문자열 `"null"`이 STATUS_CODE에 INSERT**된다. 목록·상세가 `STATUS_CODE IN (9,2)`로 걸러지고 로그인은 `='9'`를 요구하므로 **방금 등록한 담당자가 목록에도 안 보이고 로그인도 못 한다**(등록 기능이 사실상 불구). TO-BE는 '사용'(ACTIVE)으로 넣는다.
- AS-IS verbatim 보존: h3가 목록만 `MENU_4601`이고 **등록·상세는 `MENU_1101`="운영자관리"**(복사 흔적) / 등록 폼의 구분 라디오는 **둘 다 disabled**이고 서비스도 권한을 ROLE_ADMIN_8로 고정 → **등록되는 계정은 항상 부담당자** / 비밀번호·이메일도메인 칸 disabled(미입력 시 기본 비밀번호 `nacf1234`, 도메인 `@nonghyup.com` 강제) / **직책명(OFCPS_NM)에 이메일을 그대로 중복 기록**(AS-IS 매퍼 `EMAIL=#{email}, OFCPS_NM=#{email}` - 2025-10-29에 '직책' 칸을 '이메일'로 바꾼 흔적) / 목록 버튼 class `btn-defualt` 오타 / 상세의 사용여부 라벨엔 required_mark 없음 / '담당자 연락처' 행은 AS-IS 주석상태 그대로 / 아이디중복확인 응답은 **점유됐을 때 `SUCC`**(의미가 거꾸로) / 비밀번호 팝업 table의 summary·caption이 "메인 배너관리" / 팝업을 **여는 것만으로 비밀번호가 초기화**된다(GET이 발급까지 수행) / 목록 하단의 숨은 "날짜 셋팅 영역"도 유지(이 화면엔 날짜 입력칸이 없어 읽히지 않는 비활성 마크업).
- `Manager`에 **`infoUpdtDe` 매핑 추가**(컬럼은 원래 있었다). 등록 시 오늘로, 수정 시 **비어 있을 때만** 오늘로 채운다(AS-IS CASE WHEN 그대로) - 부담당자의 "지점정보 미입력" 판정 플래그.
- 검색 일치방식도 AS-IS 그대로: **지점명만 LIKE**이고 아이디·이름·개인번호는 `=`(완전일치). 그래서 `PersonInChargeSearchParam`(등록일을 오늘로 강제)을 쓰지 않고 `OffPersonInChargeSearchParam`을 따로 뒀다.
- 엑셀다운로드는 `PrivacyAccess.OFF_CHARGER`에 **tobePattern 추가**(`/admin/off-person-in-charge/list/download-excel`)해서 기존 사유 모달을 그대로 재사용한다. AS-IS xlsx → TO-BE CSV(컬럼 8개·파일명 어간 동일).
- **검증용 시드**: `database/ddl/seed-admin-off-person-in-charge.sql`(적용 완료) - 개발DB에 ROLE_ADMIN_7/8 계정이 0건이었다. 지점 011에 주담당자 2명(정원 꽉 찬 상태) + 부담당자 2명(1명은 중지), 012에 주 1·부 1, 035에 부 1(infoUpdtDe 빈 상태). 이 구성으로 `ERR_MAIN_CNT`·`NEED_AUTH_SWAP`·지점 스코프를 바로 재현할 수 있다. 목록/정원/스코프/검색 쿼리는 SQL로 대조 검증했다.
- **곁들여 고친 것**: 4402·4501 템플릿 4개가 `location.href="/admin/logout"`(GET)으로 로그아웃하는데 TO-BE `/admin/logout`은 **POST 전용**이라 405였다 → 숨은 `#logoutForm` submit으로 교체.

**★4701 답례품 관리자 = 이식대상 없음 (2026-10-04 실측 확정)**: AS-IS export(`OP_MENU_202610010945.sql`) 28행이
`(4701,4700,'답례품 관리자','/opmanager/user/rtnpsnt/list',1,'Y','2')`다 - **status_code='2'(중지)로 AS-IS에서도 노출되지 않고**,
AS-IS 소스에 `rtnpsnt` 컨트롤러·JSP가 **아예 없다**(`RtnpsntReqstCode`·`RtnpsntInfo` 도메인 클래스 2개만 존재). 즉 죽은 메뉴 행이다.
노출되는 쪽은 **16151 답례품제공자 관리 → `/opmanager/seller/list`(status '1')**로 별개 메뉴이고 TO-BE `/seller`가 그쪽에 대응한다.
[[asis-porting-principles]]대로 비활성 그대로 두었다 - TO-BE op_menu의 4701 menu_url(`/seller`)은 과거 추정값이고
숨은 메뉴라 도달하지 않는다(건드리지 않았다. 혹시 status를 1로 켜면 16151 화면이 열리는 점만 유의).

**★일반회원관리(4101) 완료 - 2026-10-04**: AS-IS 10개 엔드포인트 전부(목록 GET/POST·상세·기부내역 조각·포인트내역 조각·
비밀번호확인 팝업·개인정보열람 팝업·회원탈퇴 팝업·탈퇴처리·배송지 팝업) + 템플릿 8개
(`member-admin/{list,details,cntr-list,point-list,password-confirm,info-access,secede-write,delivery}.html`).
구버전 `member-admin/detail.html`은 대체돼 삭제했다. 4105·4107과 같이 **진입(GET)에서는 조회하지 않는다**.
- **데이터가 3개 서비스에 걸쳐 있어 admin이 조립한다**: 회원·배송지·탈퇴=member / 기부내역·기부누적액·발생포인트=donation(`g_cntr`) /
  사용포인트=point(`g_cntr_use_point`) / 가입경로·기부형태·민간연계기관 **라벨**=admin `OP_COMMON_CODE`(SBSCRB_SE·CNTR_PATH·LINK_INSTT_CD).
  지자체명은 donation의 `g_locgov`(admin은 `LocgovClient.allLocgovs()`로 코드→이름).
- **★포인트 내역은 AS-IS가 UNION ALL + 러닝잔액이다**: `G_CNTR`(적립 OCC) ∪ `G_CNTR_USE_POINT`(사용)을 등록시각 역순(같으면 USE_SN 역순)으로
  세우고, 각 행의 잔액 = **같은 지자체에서 그 행의 등록시각까지 적립합 − 사용합**이다. 두 표가 다른 서비스에 있어 각자 전량 받아
  `GeneralCustomerAdminController.buildPointRows()`에서 같은 공식으로 계산한다. 적립합은 납부완료 건만, **사용합은 use_se_code를 가리지 않는다**
  (AS-IS 서브쿼리 그대로. 누적합계의 '사용포인트'만 `use_se_code='1'`).
  타임스탬프 타입이 서로 다르다 - donation `g_cntr.frst_regist_pnttm`은 VARCHAR(14), point는 TIMESTAMP → **point API가 yyyyMMddHHmmss 문자열로 내려준다**(비교 가능하게).
- **★AS-IS 조건의 TO-BE 번역 2건(중요)**: ① AS-IS `CNTR_STTUS_CODE='200'` → TO-BE는 낱말(`COMPLETED`/`REQUESTED`/`CANCELLED`).
  ② AS-IS `STTEMNT_PAY_DE IS NOT NULL`(납부일) → **TO-BE는 `g_cntr.sttemnt_pay_de`를 어디서도 채우지 않는다**(납부 게이트웨이 이식이 별도 라운드).
  둘을 AS-IS 문자열 그대로 쓰면 기부내역·누적합계·포인트내역이 **전부 0건**이 된다 → 납부완료 판정은 `cntr_sttus_code='COMPLETED' AND delete_at='N'`으로 옮겼다
  (donation의 기존 `DonationTotalAdminApiController`와 같은 기준). 다만 **기부내역의 '납부일' 칸은 AS-IS 표시로직대로 '미결제', '발생포인트'는 '-'로 보인다**(데이터 공백, 로직은 AS-IS 그대로).
- **member 결함 1건 고침**: `AdminMemberService.search`에 **상태 필터가 없어** 탈퇴·휴면 회원까지 일반회원관리 목록에 나왔다(AS-IS는 `WHERE U.STATUS_CODE = 9`).
  실측 38 ACTIVE / 11 WITHDRAWN - 4101·4105·4107 세 화면의 모집단이 겹쳐 있던 셈이다. `ACTIVE`만 남기도록 고쳤다.
- **member 결함 1건 더 고침**: `adminWithdraw`가 **`LEAVE_USER_ID`(탈퇴처리자)를 남기지 않아** 관리자가 처리한 탈퇴가 4105에서 '회원탈퇴'로 보였다
  (4105의 탈퇴구분·담당자 컬럼이 그 값으로 갈린다). `adminWithdraw(userId, reason, leaveUserId)` 오버로드 추가.
- **AS-IS 결과코드 분기 복원**: 탈퇴는 `SUCC`/`ERR_ALR_SECEDE`/`ERR_ONE_PASS`다. **디지털원패스 회원(`op_user.user_key` 보유)은 관리자가 탈퇴시킬 수 없다** -
  `User.userKey` 매핑 추가(컬럼은 원래 있었다) + `OnePassMemberException` + Detail DTO에 `onePassUser` boolean(원패스 키 자체는 내려주지 않는다).
- **개인정보 열람 이력 복원**: AS-IS는 비마스킹 상세를 보여주기 직전 `G_INDVDLINFO_READNG_HIST`에 (열람자, 대상, 시각)을 남긴다.
  `member.g_indvdlinfo_readng_hist` 테이블은 있었지만 READNG_SN 시퀀스가 없어 `migration-member-indvdlinfo-readng-hist-seq.sql`로 만들고 6개 롤에 GRANT(적용 완료).
  `readng_dt`는 다른 AS-IS 날짜컬럼과 달리 **실제 TIMESTAMP**다. 엑셀 사유로그(`OP_PRIVACY_ACCESS_LOG`, 메뉴 1411)와는 다른 테이블이다.
  기존 TO-BE는 세션에 한 번 비밀번호를 받고 재방문 시 묻지 않는 방식이었는데, AS-IS 방식(**열람할 때마다 팝업 + 이력 적재**)으로 교체했다.
- **TO-BE 전용 기능 보존**: 계정잠금 해제·RBAC 권한 회수(SFR-002)는 AS-IS 4101에 없다 → 엔드포인트는 `MemberAdminController`에 남기고
  상세화면 맨 아래 **"운영 기능 (AS-IS 외)" 블록**으로 분리 노출했다(AS-IS 영역과 섞지 않음). 사용자 확인 필요 사항으로 보고했다.
- 신규 API: donation `/api/admin/member-donations/{cumulative-total,cntr-list,point-occ-rows,totals}`(네이티브 쿼리 `MemberDonationAdminRepository` - 엔티티 `Donation`이 cntr_point·sttemnt_pay_de 등을 매핑하지 않아 조회전용 네이티브로 읽는다),
  point `/api/admin/member-points/{userId}/use-rows`·`/used-totals`, member `/api/admin/members/{userId}/deliveries`·`/pii-access`.
- AS-IS verbatim: h3가 목록·상세 모두 **M00210**(메뉴명 '일반회원관리'가 아니다) / 목록 colgroup col 1개(컬럼 9개) / **엑셀 다운로드 버튼 없음**(구버전 list-old.jsp에만 있었다) /
  아이디·이름 두 칸 모두 상세 링크 / 가입일이 비면 "가입일을 입력 하십시오" / 상세 네비 끝에 "수정"을 덧붙인다(상세인데) / 상세는 "국민비서·SMS 수신동의", 열람팝업은 "SMS 수신동의" /
  열람팝업의 '성별' 행은 AS-IS 주석상태 그대로 / 탈퇴팝업 취소 버튼만 `btn-normal` / 배송지 팝업은 제목만 '관리'이고 **조회 전용**(빈 문구도 "데이터가 없습니다.") /
  포인트내역의 외부 SalesOn 상점 팝업 코드도 주석 그대로.
- **검증**: 모집단(38/11)·누적합계(user 1000 → 기부 13,765,000 / 발생 924,000 / 사용 136,000 / 잔액 788,000)·기부내역 지자체 INNER JOIN·
  포인트내역 OCC/USE 교차정렬을 SQL로 대조했다. 4개 서비스(member·donation·point·admin) bootJar 전부 EXIT=0.

**★지자체관리(4401) 완료 - 2026-10-04. 이로써 회원관리 영역 8/8(4701은 이식대상 없음) 종료.**
AS-IS 14개 엔드포인트 전부(목록 GET/POST·등록 폼/처리·수정 폼/처리·포인트율 ajax조회/적용·포인트율 이력팝업·부서코드 이력팝업·직인뷰어·직인삭제·배경이미지 삭제/조회·지자체삭제) + 템플릿 5개(`locgov-admin/{list,form,edit,dept-popup,point-history-popup}.html`) + **공통 주소검색 팝업 신규**.
- **이 화면은 TO-BE 기능이 이미 거의 다 있었다**(직인 암호화 저장·복호화 뷰어, 배경이미지 PC/MB, 포인트율 이력, 모금제한 g_cntr_lmtt, 부서이력, 기부혜택문구). **갭은 AS-IS 화면 형태·라우트·분기**였다 - 그래서 컨트롤러를 AS-IS 라우트로 재작성하고 템플릿을 AS-IS 구조로 다시 썼다.
- **권한분기(AS-IS getLoginUserAdminRoleCheck)**: SYS(1~4)는 목록+등록+전체수정. **LOC(5·6)는 목록이 없다** - 자기 지자체가 등록돼 있으면 수정화면, 없으면 등록화면으로 바로 보낸다(남의 지자체를 열면 자기 것으로 되돌림). 그 외 권한은 홈. 수정화면 버튼도 SYS는 상단 수정/목록, LOC는 하단 가운데 '저장' 하나다.
- **회계구분(fisSp)은 공통코드가 아니다**: AS-IS가 **지자체코드 3~5자리가 '000'(광역)이면 31/51, 아니면 41/61**을 자바에서 직접 만들어 내려준다(등록화면은 시군구를 고르는 순간 JS가 select를 다시 만든다). 그대로 옮겼다. 실측 지자체는 전부 기초(41/61)이고 fis_sp는 아직 전부 NULL.
- **포인트 지급률은 point 소유**: AS-IS는 `G_CTBNY_SETUP`에 쓰지만 TO-BE에서 실제 적립 계산(`PointService#currentPointRateOf`)이 읽는 표는 `PT_LOCGOV_POINT_RATE`라 그쪽에 쓴다(기존 결정 유지). 이력 팝업도 같은 표를 읽는다 - 연도별 행 자체가 이력이고 작성자명까지 있어 AS-IS 팝업 5컬럼과 1:1로 맞는다. **다만 그 표에 frst_regist_pnttm이 없어** AS-IS의 `IFNULL(last_updt_pnttm, frst_regist_pnttm)` 폴백은 불가 - 변경일이 비면 빈칸이다(실측 시드 일부가 그렇다).
- 목록의 포인트율 컬럼은 AS-IS가 스칼라 서브쿼리였는데 지급률이 다른 서비스라 **일괄 조회 API 신규**: point `GET /api/admin/locgov-point-rates/current?stdrYear&locgovCodes`(+ `LocgovPointRateRepository.findByStdrYearAndLocgovCodeIn`). 행마다 호출하면 N+1.
- **donation 신규 2건**: 직인 삭제(`POST /api/locgov-admin/{code}/seal/delete` → 파일 삭제 + OFFCS_NM·OFFCS_FILE_NM·ORGINL_FILE_NM NULL. AS-IS `updateLocgovOffcsInfo`가 **직인명까지 비운다**), 부서이력 DTO에 `lastUpdusrId` 추가(이력 팝업의 "등록자 이름(아이디)"는 admin이 자기 OP_MANAGER에서 찾는다 - 관리자 계정은 admin 소유).
- **기부금 모금제한은 AS-IS가 지자체 수정 저장 안에서 처리**한다(기간·사유 있으면 insert/update, 비면 delete). TO-BE는 등록/삭제 API가 따로 있어 `applyLmtt()`로 묶었다. 화면의 '사용' 체크박스는 AS-IS대로 `onclick="return false;"`라 직접 켤 수 없고 기간이 유효할 때 스크립트가 자동 체크한다.
- **★공통 주소검색 팝업 신규 이식**: AS-IS `/opmanager/juso-popup`(JusoController + 689줄 JSP)이 지자체관리·오프라인기부접수 등 여러 화면이 공유하는 컴포넌트인데 TO-BE admin에는 **아예 없었다**(모든 주소칸이 맨 텍스트 입력). 행정안전부 주소검색 API를 **브라우저에서 JSONP로 직접** 호출하는 클라이언트 전용 위젯이라 서버 로직이 없다 → `/admin/juso-popup` + `templates/juso/juso-popup.html`로 옮겼다(스크립트로 기계변환, 스크립트릿 0개 잔류). 정적자산(`content/modules/juso/addrlink.js`, `content/opmanager/css/juso/addrlink.css`, 이미지)은 AS-IS 통째복사 때 **이미 들어와 있었다**. AS-IS가 JSP에 박아 둔 승인키·도메인·결과형식(4)만 `ghlove.juso.*` 설정으로 뺐다(기본값 = AS-IS 값). WebConfig 게이트에도 추가.
- AS-IS verbatim: **등록·수정 화면의 h3가 빈 칸**(`<h3><span></span></h3>`)이다 / 목록 colgroup col 1개인데 빈목록 colspan은 9(컬럼 8개 - AS-IS 불일치) / 등록일 범위에 **기본값이 없고 날짜버튼에 '전체'도 없다** / 지자체 칸만 링크(담당자명·연락처는 평문) / **삭제 버튼은 2022.11.18 주석처리** - 엔드포인트만 남기고 버튼은 주석 상태로 / 체크박스 컬럼이 없는데 `checkedEventSet()`이 남아 있음(죽은 코드도 유지) / **명예회원선정기준 블록은 AS-IS에서 전체 주석처리** → 주석 그대로(그래서 honor-amt 검증·전송도 없다. [[domain-code-facts]]와 일치) / 사업자번호 3-2-5·연락처 3분할 입력 / 부서코드 11자리·행정표준기관코드 7자리 / 연도 select는 **올해 이후만** / 직인 파일이 있으면 file input을 숨기고 파일명+삭제(x)만.
- **AS-IS 죽은 코드 2건 미이식(기록)**: `POST /test`(본문이 `System.out.println("test")`뿐인 디버그 스텁), 주석처리된 `GET /imageView/{locgovCode}`.
- TO-BE 전용 "지자체별 기부혜택 안내문구"(SFR-003)는 AS-IS 4401에 없어 **수정화면 맨 아래 "운영 기능(AS-IS 외)" 블록**으로 분리 노출(4101과 같은 처리).
- **검증**: 목록 ordering 정렬 + 올해 지급률 LEFT JOIN(순천시 25.00 / 미설정은 빈칸), 회계구분 분기, 지급률 이력 정렬(변경일 desc→년도 desc), 부서이력 1건·모금제한 0건·직인 2건을 SQL로 대조. admin·donation·point bootJar 전부 EXIT=0.

**TO-BE 기존 자산**: 컨트롤러는 `MemberAdminController`·`MemberBatchAdminController`·`SecedeUserAdminController`·`SleepUserAdminController`·`LocgovAdminController`·`PersonInChargeAdminController`·`OffPersonInChargeAdminController`·`SellerAdminController`가 이미 있고, 템플릿은 `member-admin/{list,detail,batch}.html`·`locgov-admin/{list,form}.html` 등이 있다. 시스템관리 영역의 경험상 **기존 화면은 AS-IS와 상당히 다를 가능성이 높으니 그대로 신뢰하지 말고 AS-IS와 1:1 대조부터 할 것**.

**★회원탈퇴관리(4105) 완료 - 2026-10-03**: 검색구분(아이디/탈퇴사유)·탈퇴일 범위(기본 오늘, 날짜버튼에 **전체(all-1)**가 하나 더 있다)·탈퇴구분(전체/회원탈퇴/관리자탈퇴)·화면출력·6컬럼·탈퇴사유 팝업(600x350) 복원.
- **AS-IS는 진입(GET)에서 조회하지 않는다** - 컨트롤러가 `Collections.EMPTY_LIST`/count 0을 내려주고 검색(POST)해야 조회된다(화면 안내문구 "검색 버튼을 클릭하면 조건에 맞는 검색결과를 확인할 수 있습니다."가 그 설명). 예전 TO-BE는 GET에서 전체를 조회하고 탈퇴구분·검색구분이 없었다. GET/POST를 분리해 재현.
- 6컬럼을 서비스 경계로 나눠 채운다: 탈퇴일·아이디·사유·탈퇴코드·**탈퇴처리자ID**는 member, 사유 라벨은 admin의 `LEAVE_CODE` 공통코드(5종), 담당자(권한그룹+이름)는 admin의 `op_manager`/`op_role`에서 탈퇴처리자ID로 찾는다. AS-IS는 OP_MANAGER→OP_USER 폴백이 있는데 탈퇴 처리자는 관리자이므로 OP_MANAGER 경로만 이식(폴백 미이식).
- **member에 필드 1개 추가(읽기 전용·additive)**: `UserDetail.leaveUserId`(컬럼 `LEAVE_USER_ID`는 원래 있었고 **매핑만 없었다**) + `SecedeRowDto.leaveUserId`. 이게 없으면 탈퇴구분·담당자 2컬럼을 영구히 채울 수 없다(admin이 member.op_user_detail을 직접 못 봄). 탈퇴구분 필터는 member API에 없어 admin 메모리에서 적용.
- AS-IS verbatim: h3가 메뉴명(탈퇴회원리스트)이 아니라 `MENU_4105`="**회원탈퇴관리**", 4번째 컬럼 헤더가 **"탙퇴사유" 오타**, colgroup에 col이 1개뿐(컬럼 6개), 팝업 제목이 "회원탈퇴"(탈퇴사유가 아님), 빈 목록도 표 안 한 행으로.

**★휴면회원관리(4107) 완료 - 2026-10-03**: 검색구분(아이디/이름)·최종 방문일 범위(기본 오늘, 전체(all-1) 포함)·화면출력·8컬럼·휴면회원 해제(ajax) 복원. 4105와 똑같이 **진입(GET)에서는 조회하지 않고 검색(POST)해야 조회된다**. 검색은 화면 안내("※ "이름, 아이디"는 정확하게 입력해야 합니다.")대로 **완전일치** - member API가 부분일치라 admin에서 한 번 더 좁힌다.
- 8컬럼이 세 서비스에 걸쳐 있다: 최종방문일·아이디·이름·**주소**=member, **기부누적액**=donation, **포인트잔액**=point. 예전 TO-BE는 뒤 3컬럼이 아예 없었다.
- **신규 조회전용 API 2개 + member 필드 2개(전부 additive)**: point `GET /api/admin/point-balances?userIds=`(PT_POINT_BALANCE), donation `GET /api/admin/donation-totals?userIds=`(**완료(COMPLETED) 기부만** 합산), member `SleepRowDto.address/addressDetail`. 둘 다 **현재 페이지 userId를 한 번에** 보내는 일괄 조회다(행마다 호출하면 N+1).
- 휴면 해제는 AS-IS대로 ajax `POST /wakeup`(파라미터명 `userIdList`)이고 응답 `{isSuccess, data}`에서 화면은 **data=="SUCC"만 성공**으로 본다(그 외는 "오류가 발생했습니다.").
- AS-IS verbatim: 표 summary/caption이 **"휴먼회원관리" 오타**(휴면→휴먼), colgroup col 2개(컬럼 8개), 금액 `#,###.##`·없으면 '-', 빈 목록 행에만 background 스타일 없음, "휴면회원 해제" 버튼은 목록이 비어도 항상 노출.

**★★담당자 화면의 사용여부는 반드시 경계에서 변환할 것(2026-10-04에 터진 버그)**: AS-IS `OP_MANAGER.STATUS_CODE`는 **'9'(사용)/'2'(중지)** 숫자코드인데 이 프로젝트는 같은 컬럼에 **`ACTIVE`/`LOCKED`**를 쓴다(`ManagerAdminService.STATUS_ACTIVE/STATUS_LOCKED`, 개발DB 실측도 전부 `ACTIVE`). 4402·4501을 처음 이식할 때 AS-IS 값 9/2를 그대로 비교·저장해서 **① 수정화면에서 사용여부 라디오가 아무것도 체크되지 않고 ② 저장하면 '9'/'2'가 들어가 두 체계가 섞이는** 버그가 있었다. 조치:
- `Manager.getAsIsStatusCode()`(@Transient, LOCKED→'2' 그 외 '9') - **템플릿은 이걸 비교**한다(`item.asIsStatusCode == '2'`).
- `PersonInChargeAdminService.toStatusCode('9'|'2')` → ACTIVE/LOCKED - **컨트롤러 저장 직전에 변환**한다.
- `PersonInChargeSearchParam.matchesStatus`도 AS-IS 표기로 바꿔 비교한다.
- **마크업의 라디오 value는 AS-IS대로 9/2 유지**(verbatim). 4601(오프라인담당자)도 사용여부 라디오가 9/2이므로 같은 규칙을 쓸 것.

**주의**: 회원 데이터는 member 서비스 소유다(`member.op_user` 등). admin은 `MemberAdminClient`로 cross-service 조회한다 - member의 검색 API에 파라미터가 부족하면 시스템관리 1408처럼 admin 메모리 필터링으로 우회하거나, 7210처럼 **사용자 승인을 받고** member에 조회 API를 추가한다. member 서비스 쪽 검색 키는 현재 `USER_NAME`/`ADDRESS`/`LOGIN_ID` 3종뿐이고 **page가 0부터**다.

### admin-customer-center-area-port-progress

> 고객센터 영역(11화면) 이식 원장 - AS-IS 대조표·구조갭 6건·TO-BE 축소 실측


[[asis-screen-port-procedure]] 절차로 진행하는 **네 번째 영역 = 고객센터 11화면**(2026-10-04 착수).
앞선 영역: 시스템관리 25/25([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-system-area-port-progress)) · 회원관리 8/8([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-member-area-port-progress)) ·
소규모 묶음([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-small-areas-port-progress) - 기부혜택증3·대시보드1·커뮤니티6 완료).

**남은 영역 실측**(op_menu status_code='1', menu_type=3 기준): 기부금관리 6 · 특정사업 5 · **고객센터 11** · 통계 25 · 답례품관리 30.

#### AS-IS ↔ TO-BE 대조표 (2026-10-04 실측)

| id | 메뉴 | AS-IS url · 컨트롤러(줄) | TO-BE url · 컨트롤러(줄) | 상태 |
|---|---|---|---|---|
| 5110 | 지자체공지사항 | `/opmanager/locgov-notice/**` · LocgovNoticeManagerController **259** | `/admin/notices` · OperationContentController 477(공유) | **★URL 충돌** |
| 5111 | 공지사항 | `/opmanager/notice/**` · NoticeManagerController **223** | `/admin/notices` (같음) | **★URL 충돌** |
| 5107 | 자료실 | `/opmanager/data-board/**` · DataboardManagerController **294** | `/admin/data-board` · DataBoardAdminController 124 | 축소 |
| 5102 | 1:1 문의 | `/opmanager/qna` · QnaManagerController **539** | `/admin/shop-inquiries` · ShopInquiryAdminController **36** | **대폭 축소** |
| 5112 | Q&A | `/opmanager/qna-open` · QnaOpenManagerController **575** | **menu_url 비어 있음** | **★미구현** |
| 5104 | FAQ | `/opmanager/faq` · FaqManagerController **247** | `/community/faq-bbs` | **★URL 충돌(11405와) + 미구현** |
| 5113 | 운영관리 SR게시판 | `/opmanager/maintenance/` · MaintenanceController **443** | `/admin/maintenance` · MaintenanceAdminController 236 | 축소 |
| 5114 | 답례품제공자 공지사항 | `/opmanager/sellerNotice` · SysNoticeSellerController **290** | `/admin/seller-notices` · SysNoticeSellerAdminController 203 | 축소 |
| 5120 | 내부문의 관리 | **AS-IS op_menu에 없음**(실측 0건) | `/admin/internal-inquiry` · 134 | **★TO-BE 추가 - 근거 확인 필요** |
| 5201 | 관리자매뉴얼 | `/opmanager/manual/manager/list` · ManualManagerController **410**(5202와 공유) | `/admin/manuals` · ManualAdminController 111 | **★URL 충돌** |
| 5202 | 사용자매뉴얼 | `/opmanager/manual/list` (같은 컨트롤러) | `/admin/manuals` (같음) | **★URL 충돌** |

AS-IS 컨트롤러 합계 **약 3,280줄** vs TO-BE 약 **900줄** → 라인 기준 약 27%.
(5108 만족도 조사는 parent가 **6100**이라 고객센터 소속이 아니다 - AS-IS `/opmanager/cntnts-stsfdg` 204줄 vs TO-BE 38줄.)

#### 구조갭 6건 (먼저 정리할 것)
1. **5110 vs 5111** 둘 다 `/admin/notices` - AS-IS는 지자체공지(locgov-notice)와 공지(notice)가 **별도 컨트롤러·별도 표**다.
2. **5201 vs 5202** 둘 다 `/admin/manuals` - AS-IS는 같은 컨트롤러의 `manager/list`와 `list`로 갈린다.
3. **5104 FAQ** → `/community/faq-bbs`가 **11405 담당자용 FAQ와 충돌**. 자기 URL이 필요하다.
4. **5112 Q&A** → menu_url이 빈 값이라 클릭 자체가 안 된다(AS-IS는 `/opmanager/qna-open/list`).
5. **AS-IS 중지 메뉴 5건이 TO-BE에 아예 없다** - 5101 상품평 관리 · 5103 상품문의 · 5105 민원신고 ·
   5106 행사이벤트 · 5109 매뉴얼관리(구). 전부 `display_flag='N', status_code='2'`다.
   [[asis-porting-principles]]대로 **중지 상태로 등록**해야 한다(11403 선례와 동일).
6. **5120 내부문의 관리는 AS-IS 메뉴에 없다** - TO-BE 추가물이다. [[asis-porting-principles]]
   ("AS-IS 로직 + RFP/ISP 신규기능만, 근거 없는 추가 금지")에 걸리므로 RFP/ISP 근거를 확인해야 한다.
   삭제는 사용자 판단(이미 134줄 구현돼 있다).

#### 이미 찾아 둔 live 결함 (커뮤니티 라운드에서 발견)
- **`FAQ_TYPE` 공통코드 11행이 DB에 없다**: `database/ddl/service-admin.sql`(4962행~)이
  `JOIN`·`DONATE`·`POINT`·`OFFLINE`·`DESIGNATED`·`TAX`·`GIFT`·`ORDER`·`PRIVATE`·`SYSTEM`·`ETC`를
  넣으려는데 **`ADMIN_COMMON_CODE`라는 없는 표 이름**으로 INSERT한다 → 실제 `admin.op_common_code`의
  `FAQ_TYPE`에는 쇼핑몰용 1~6만 있다. 그래서 `op_community_locgovfaq` **63행의 질문유형이
  공개 FAQ(`/faqs`)·관리화면 양쪽에서 빈 값으로 보인다**. 그 11행을 올바른 표에 넣으면 해결.
  (담당자용 FAQ(11405)는 `CMNTY_FAQ_TYPE`을 쓰므로 무관 - [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-small-areas-port-progress))
- **11403 지자체FAQ는 종결됨**: AS-IS 중지 메뉴라 메뉴 등록만 하고 화면은 손대지 않기로 결정(사용자 ②).
  그 화면(`LocgFaqAdminController`, `/community/locv-faq`)이 **공개 FAQ 63행의 관리화면**이다 - 다시 열지 말 것.

#### ① 구조갭 정리 - 진행 결과 (2026-10-05)

**(a) AS-IS 중지 메뉴 5건 등록 완료** (`migration-admin-menu-5100-stopped-menus.sql`, 적용):
5101 상품평 관리 · 5106 행사이벤트 · 5103 상품문의 · 5105 민원신고 · 5109 매뉴얼관리(구).
전부 `display_flag='N', status_code='2'`라 nav·breadcrumb에 영향 없고 화면 이식 대상도 아니다(11403 선례와 동일).
op_menu_right은 AS-IS에도 없어 추가하지 않았다.

**(b) ★live 결함 해결 - 공개 FAQ 질문유형이 전부 빈 값이던 것** (`migration-admin-faq-type-codes-fix.sql`, 적용):
- `FAQ_TYPE` 11행 추가(id는 `op_community_locgovfaq.faq_type`에 실제 저장된 짧은 코드 JOIN/DONATE/…,
  라벨은 AS-IS enum `FaqType` 그대로) + **SalesOn 잔재 1~6은 use_yn='N'으로 숨김**(삭제 아님).
  `labelsOf()`가 use_yn='Y'만 추리므로 화면엔 11종만 보인다. FAQ_TYPE 소비처는 `/faqs`·`/api/faqs`·
  `/community/locv-faq` 세 곳뿐이고 모두 이 11종이 맞는 화면이다(실측).
- 데이터 1행 보정: id=1000 '로그인이 안 돼요'가 faq_type='1'(잔재 코드)이어서 'JOIN'으로 맞췄다.
- **검증: 라벨 못 찾는 행 63 → 0**.

**(c) 공지 2화면 분리 완료** (`migration-admin-menu-5110-locgov-notice-split.sql`, 적용 + 코드):
- **AS-IS 분리 규칙 확정**(매퍼까지 실측): 표는 하나(`op_notice`)이고 조회범위만 다르다.
  5111 공지사항 = 검색조건 지자체코드를 **'00000' 고정** → 전체공지만 /
  5110 지자체공지사항 = **LOC 담당자일 때만** 자기 지자체로 스코프, **시스템·행안부는 조건 없이 전부**
  (전체공지 포함 - AS-IS `getLocgovNoticeList`의 조건부 WHERE 그대로). 목록에 지자체명 표시.
- TO-BE: `5110 → /admin/locgov-notices`로 분리. `OperationContentController`에
  `adminLocgovNotices()` 추가 + `fillNoticeList(category, wholeNoticeOnly, …)` 공유.
  템플릿은 하나를 공유하고 `screenTitle`·`listUrl`만 갈린다. 등록·수정·삭제·토글은
  `/admin/notices/...` 한 경로 공유(AS-IS도 같은 표의 같은 자원).
- **★경계 번역**: AS-IS는 전체공지를 `locgov_code='00000'`으로 쓰는데 **TO-BE 데이터는 비워 두었다**
  (실측 빈값 14행 / 지자체코드 16행) → `isWholeNotice()`가 NULL·빈값·'00000'을 모두 전체공지로 본다.
  데이터를 고치지 않은 이유: 공개 공지 화면(`/notices`)이 NULL을 전체로 보고 살아 있어서 건드리면 위험하다.
- WebConfig에 `/admin/locgov-notices` 게이트 추가.

**(d) 매뉴얼 5201/5202 URL 분리는 ②로 넘김** - 이유 2가지:
- **표가 다르다**: AS-IS 매뉴얼은 **`g_mnl`**(mnl_sn·menu_se_code·menu_url·menu_nm·file_nm·file_ty·
  inqire_co·menu_sj·menu_cn)인데 TO-BE는 **자체 설계한 `op_manual`**(title·content·menu_url_code·file_src, 0행)을 쓴다.
  **`g_mnl`은 TO-BE admin 스키마에 이미 있다(0행)** - 커뮤니티 게시판과 똑같은 "AS-IS 표 있는데 골격이 발명" 상황이라
  ②에서 `g_mnl`로 옮겨야 한다. `menu_se_code`는 `op_common_code(code_type='MENU_URL')`와 조인하는
  **대상 메뉴 코드**이고(TO-BE에 33행 있음) 관리자/사용자 구분자가 아니다.
- **분리 규칙이 아직 불명확**: 5201 관리자매뉴얼은 `getAllMenuList()`로 **메뉴 트리(2차→3차) 표**를 그리고,
  5202 사용자매뉴얼은 `getManualList()`로 **검색 목록**을 그린다. 같은 g_mnl 행을 다른 뷰로 보는 것으로 보이는데
  구분 조건을 아직 확정하지 못했다 → 규칙을 확정한 뒤 URL을 나누는 것이 맞다(먼저 나누면 없는 구분을 발명하게 된다).
- 현재 TO-BE 화면(`/admin/manuals`, 평면 목록)은 5202 쪽에 가깝다.

**(e) 5104 FAQ의 URL 변경은 ②(구현)와 함께** - 지금 URL만 옮기면 404가 된다.
- 5104 FAQ: AS-IS 표는 **`OP_FAQ`**(entity `saleson.model.Faq`, faqType=enum FaqType). TO-BE `op_faq` **0행**.
  11403 지자체FAQ(`op_community_locgovfaq`, 63행)와 **다른 표**다 - 혼동 금지.

**(f) 5112 Q&A의 menu_url 빈 값 해소 완료** (`migration-admin-menu-5112-qna-url.sql`, 적용):
`5112 → /qna-admin`. **표 DDL은 필요 없었다**(아래 정정 참고).

#### ★ 앞선 추정 정정 2건 (2026-10-05 실측)
1. **"5112 Q&A의 표가 TO-BE에 없다"는 틀렸다.** Q&A는 1:1 문의와 **같은 표를 공유**한다 -
   `op_qna`/`op_qna_answer`/`op_qna_file`/`op_qna_answer_file`(AS-IS 두 컨트롤러가 같은 `QnaService`를 쓴다).
   TO-BE에 네 표 모두 있고 `op_qna` 9행·`op_qna_answer` 4행이 들어 있다. **DDL 신설 불필요.**
   - 구분자는 **`qna_type`**: 1:1문의='0'(INDIVIDUAL) / 상품문의='1'(ITEM) / Q&A='2'(QNA).
     **5112는 AS-IS가 그 필터를 주석 처리**해 두어 사실상 전체를 본다 → [[asis-porting-principles]]대로
     주석 상태를 유지한다(필터를 켜지 말 것).
   - ★**TO-BE 9행은 qna_type이 전부 NULL**이고 `qna_group`에 3·6·7·8·9·10이 들어 있다 →
     5102가 `qna_type='0'`으로 거르면 **0건**이 된다. 공지 '00000'과 같은 경계 번역이 필요하다(②에서 처리).
   - TO-BE에서 이 표를 쓰는 것: 공개 Q&A 게시판(`QnaController`·`QnaApiController`) + 관리화면 `/qna-admin`(60줄).
     **1:1문의 화면(`/admin/shop-inquiries`)은 `op_qna`가 아니라 TO-BE가 만든 `OP_SHOP_INQUIRY`를 쓴다**
     → ②에서 `op_qna`로 옮겨야 한다(커뮤니티·매뉴얼과 같은 "AS-IS 표 있는데 골격이 발명" 패턴, 세 번째 사례).
2. **"5120 내부문의 관리는 근거 없는 TO-BE 추가"도 틀렸다.** AS-IS에 **`/opmanager/qna-admin`
   (`QnaAdminManagerController`)** 화면이 실재하고 표도 **`g_qna_admin`/`_answer`/`_file`**로 AS-IS 덤프에 있다
   (TO-BE `InternalInquiryAdminController`가 이미 그 표를 쓰고 주석에도 AS-IS 출처를 적어 두었다).
   **AS-IS op_menu export에 행이 없을 뿐**이고 화면 자체는 AS-IS다 → 근거 없는 추가가 아니다.
   TO-BE가 메뉴 행을 만들어 접근 경로를 준 것은 타당하다. **gap 6 해소.**
   - ★**이름 충돌 주의**: TO-BE `/qna-admin` = **공개 Q&A(5112)** 관리화면,
     AS-IS `/opmanager/qna-admin` = **내부문의(5120)**. TO-BE 내부문의는 `/admin/internal-inquiry`다.

#### ② 구현 순서 (사용자 승인)
**5112 Q&A → 5104 FAQ → 매뉴얼 2화면(g_mnl 이전+분리) → 1:1문의 보강**

**5112 Q&A 범위 실측**: AS-IS `QnaOpenManagerController` 575줄 / **엔드포인트 16개**
(list GET·POST / delete 일괄 / view/{id} / answer/{id} GET·POST / **download-excel GET·POST** /
**upload-excel GET·POST** / edit/{id} GET·POST / delete/{id} / delete/{id}/answer/{answerId} /
delete-item-image / file-download/{fileId}/{type}) + JSP 6종 **1,164줄**
(list 378 · form 239 · view 215 · answer 183 · download-excel 84 · upload-excel 65).
TO-BE `/qna-admin`은 **3개**(list·detail·answer)뿐 → 13개 + 엑셀 업·다운로드 + 첨부파일이 추가 대상.

##### 5112 Q&A 진행 (2026-10-05) - 목록·답변화면·일괄삭제 완료, admin·member bootJar EXIT=0
**신규/수정**
- admin: `repository/QnaOpenAdminRepository`(AS-IS 목록 SQL 이식), `service/QnaOpenAdminService`,
  `web/support/QnaOpenSearchParam`, `web/QnaAdminController` 확장(GET/POST 목록 · `POST /qna-admin/delete` ·
  `GET /qna-admin/answer/{qnaId}`), 템플릿 `qna-admin/{list,answer-form}.html` 신규.
- admin `domain/Qna`에 **누락 컬럼 3개 매핑 추가**: `qna_type`·`data_status_code`·`use_yn`
  (기존 엔티티가 부분 매핑이라 Q&A 조회·삭제에 필요한 컬럼이 없었다).
- member: `AdminMemberService.userLoginIds()`·`userIdsByLoginIdLike()` +
  `GET /api/admin/members/login-ids`·`/api/admin/members/ids-by-login`,
  `UserRepository.findByLoginIdContainingAndStatusCode`. admin `MemberAdminClient`에 대응 메서드 2개.
  → AS-IS가 `OP_USER`를 조인해 `LOGIN_ID`를 뽑는 자리를 member 조회로 대체(회원은 member 소유).
  **검색구분 '아이디'는 member에서 회원ID를 먼저 받아 `user_id IN (...)`으로 거른다.**

**AS-IS 고정조건/동작 그대로**: `qna_type='2'` · `data_status_code='0'` · `display_flag='Y'` · `use_yn='Y'` /
진입(GET) 빈 목록·검색(POST)에서만 조회 / 정렬은 컨트롤러가 `CREATED_DATE`,`DESC`로 고정하는데
**매퍼 CASE가 CREATED_DATE를 QNA_ID로 매핑**해 실제로는 qna_id 기준이다(그대로 둠) /
작성자명 마스킹 `첫글자+"*"+세번째부터`(두 글자면 "김*") / 답변여부는 answer_count로 판정 /
날짜조건은 varchar(14)에 `000000`·`235959`를 붙여 문자열 비교 / 이 화면만 날짜 기준값이 **month3**(다른 화면은 month2).

**AS-IS 비활성 그대로 유지**: 답변을 행으로 펼치는 **UNION ALL 주석처리**(그래서 '구분' 컬럼은 항상 'Q',
제목 앞 답변 아이콘은 절대 안 나옴) / 검색구분 '전체(ALL)'·'이름(USER_NAME)' 주석 / 문의유형 select·answerCount 라디오 주석 /
**엑셀 다운로드는 함수만 있고 호출 버튼이 없다**(게다가 submit 대상 `#listForm`의 action이 1:1문의 목록을 가리키는 복사 흔적) → 버튼 만들지 않음 /
`qnaTypes` 모델값은 항상 빈 목록(`QNA_GROUPS.code_value`가 비어 존재하지 않는 코드유형을 찾는다 - SalesOn 2단분류 잔재).

**AS-IS 규칙(중요)**: 일괄삭제는 **답변이 없는 건만** 지우고 답변 있는 건은 **조용히 건너뛴다**.
삭제는 `DATA_STATUS_CODE='1'` 소프트 삭제다. 답변 제목은 화면에 없고 **"답변입니다."가 hidden 고정값**이다.

**AS-IS 결함 1건 고침**: 마스킹이 `substring(2)`를 무조건 호출해 **한 글자 이름이면 목록 전체가 500**이 된다 → 두 글자 이상만 마스킹.

**데이터 보정 1건**(`migration-admin-qna-type-backfill.sql`, 적용): 시드 9행의 `qna_type`이 전부 NULL이라
AS-IS 조건(`qna_type='2'`)으로는 0건이었다. 내용·용도상 전부 공개 Q&A이고 공개게시판(`/qna`)이 읽는 행이라
**SQL을 느슨하게 바꾸는 대신 데이터에 '2'를 채웠다**.

**내가 만든 버그 1건 - 즉시 수정**: `op_qna_answer.answer_date`가 **varchar**인데 `LocalDateTime`으로
캐스팅해 런타임에 터질 코드였다(빌드는 통과). String으로 바꾸고 화면에서 포맷한다.
`QnaAnswer`에는 `answerUserName`이 없어 `userId`로 운영자명을 조회해 모델에 싣는다. `QnaFile`은 `orgFileName`이다.

**검증(SQL 대조)**: 목록 9건·문의유형 라벨 매핑·답변건수·답변일 / 답변완료 4·미답변 5 /
제목 LIKE '배송' 2 / 문의유형 LIKE '답례품' 2 / 등록일 20260715~ 4건 - 전부 일치.

##### 5112 2차 (2026-10-05) - 삭제·첨부 엔드포인트 4개 추가, admin bootJar EXIT=0
- `GET /qna-admin/delete/{qnaId}` (문의글 삭제) · `GET /qna-admin/delete/{qnaId}/answer/{qnaAnswerId}` (답변 삭제) ·
  `POST /qna-admin/delete-item-image` (첨부 삭제) · `GET /qna-admin/file-download/{qnaFileId}/{qnaDetailType}` (첨부 다운로드).
  서비스에 `deleteQna`·`deleteQnaAnswer`·`deleteFile`·`file` 추가. `QnaFileStorageService.resolve()` 신규.
- **★AS-IS 삭제 규칙이 두 군데가 서로 다르다**(둘 다 그대로 옮겼다):
  목록 일괄삭제는 **답변 있으면 건너뛰고**, 답변화면의 [문의글 삭제]는 **답변이 있어도 지운다**(AS-IS가 여기선 검사하지 않음).
  답변 삭제는 소프트가 아니라 **행을 지운다**(매퍼에 소프트삭제 UPDATE가 주석으로 남아 있고 DELETE가 실행된다).
- **AS-IS 결함 1건 보정**: 답변을 지워도 `answer_count`를 갱신하지 않아 목록이 계속 '답변완료'로 보이고
  미답변 검색에서도 빠진다 → 실제 답변 수로 다시 계산한다.
- **내가 만든 오류 1건 수정**: 답변화면의 삭제 버튼을 `location.href`로 짰는데 AS-IS는 **ajax `$.get` +
  `Common.confirm` + `Common.responseHandler`**이고 서버도 JSON을 돌려준다(JsonView). AS-IS 방식으로 바로잡았다.
  첨부 다운로드 링크도 AS-IS대로 **`/file-download/{id}/{qnaDetailType}` 두 조각**으로 고쳤다(화면은 항상 `qna-open`을 보낸다).
- **검증(SQL 트랜잭션 후 롤백)**: 소프트삭제 시 목록 9→8 / 답변 하드삭제 후 `answer_count` 1→0 /
  미답변 검색 건수 재계산 일치(5 = 기존5 − 소프트삭제1 + 답변삭제1).

##### 5112 3차 (2026-10-05) - 답변 저장 POST + 국민비서 문자 + 답변화면 parity 보정, admin·member bootJar EXIT=0

**추가한 엔드포인트** (AS-IS 경로 모양 그대로):
`POST /qna-admin/answer/{qnaId}` (답변 등록·수정) · `GET /qna-admin/edit/{qnaId}` · `POST /qna-admin/edit/{qnaId}`.
**발명이던 TO-BE 스켈레톤을 제거**: `GET /qna-admin/{qnaId}` + `templates/qna-admin/detail.html` + `POST /qna-admin/{qnaId}/answer` 삭제.

**★AS-IS 답변 POST의 부가동작 실측 - 실제로 나가는 알림은 하나뿐이다**:
- `sendMailFlag`/`sendSmsFlag`를 "값 없으면 N"으로 정하는데 **form.jsp에 그 체크박스가 아예 없어 항상 'N'**이다 →
  `QnaCompleteMail` 메일 분기와 `UnifiedMessagingService`(UMS) 분기는 **AS-IS에서 한 번도 실행되지 않는다**(SalesOn 잔재).
  같은 이유로 첨부 `detailImageFiles[]`도 **화면에 input이 없고**, 받더라도 insert/update 매퍼가 파일을 저장하지 않는다.
  → [[asis-porting-principles]]대로 **옮기지 않고 기록만** 했다.
- **실제 알림 = `sendSmsQnaAnswer(qnaId)` 하나**(무조건 호출, 답변 **수정 때도 다시 나간다**).
  국민비서(IPS) 연계라 "발송"은 `TIF_IPS_SNDNG_M`에 한 행 적재하는 것이고 ESB가 집어간다.

**국민비서 발송 경로 신규 이식**(TO-BE에 발송부가 아예 없었다 - 7208 조회화면만 있었음):
- `admin/service/SmsIpsService.java` (AS-IS `SmsIpsServiceImpl.giveSendSms`/`insertTifIpsSndngM`/`initTifIpsSndngM`)
  - 수신동의 **`RECEIVE_SMS='0'`만** 발송 · 수신식별값은 **회원 CI**(`PRVC_IDNTFC_INFO=MBER_CI`) ·
    발송내용 `이름|전화번호`(QNA/가입/탈퇴/비번변경/관리자로그인 공통 2칸) ·
    공통값 `ESB_STATUS_CD='N'`, `ESB_WORK_GBN='I'`, `SVC_ID='812-A022'` · `LIST_SN`=`INSTT_CRT_SN`(같은 채번값)
  - 설정 `ghlove.integrations.sms-ips.{enabled,svc-grp-id,prvc-idntfc-se-cd,esb-if-id}` (AS-IS 전 프로파일 동일값 `812`/`C0090001`/`IF_P01_MIS_IPS_ALM08`), **개발환경 enabled=false**(AS-IS 소스에도 같은 이유의 로컬 가드가 있다)
- `database/ddl/migration-admin-tif-ips-sequence.sql` **적용** - AS-IS CUBRID serial `TIF_IPS_SNDNG_M_LIST_SN` 대체 시퀀스
- member 신규: `AdminMemberService.smsReceiver(userId)`(AS-IS `getQnaUserInfo`의 OP_USER·OP_USER_DETAIL INNER JOIN 대체) +
  `GET /api/admin/members/{userId}/sms-receiver`(없으면 204) + admin `MemberAdminClient.smsReceiver()`
- **AS-IS 결함 보정**: 비회원·탈퇴회원 문의는 `getQnaUserInfo`가 null → `Arrays.asList(null)` → **NPE로 500**
  (답변은 저장된 뒤 화면만 깨진다). TO-BE는 조용히 건너뛴다.

**답변화면(form.jsp) parity 보정 - 내가 1차에서 놓친 것들**:
- 상단 [문의글 삭제][목록]은 **답변이 없을 때만** 나온다(`answerCount == 0`) · 하단 버튼은 `btn_right`
- **답변 작성자 칸은 "역할명 (관리자 로그인ID)"**다(사람 이름이 아니다). AS-IS `getQnaByQnaId`가
  `OP_ROLE.ROLE_NAME`(authority LIKE 'ROLE_ADMIN%') + `OP_MANAGER.LOGIN_ID`를 뽑아 `roleNm`/`answerLoginId`로 내려준다
  → TO-BE는 `op_manager.authority`에서 찾는다(고정 경계 번역)
- 문의 작성자 칸은 `이름 (로그인ID)` + 회원상세 링크(`/admin/members/details/{userId}`), 없으면 이름, 그것도 없으면 `-`
- 작성일·답변일은 **`op:date`=날짜까지만**(목록은 `op:datetime`) ← 1차에서 작성일을 시각까지 찍어 둔 것 수정
- 첨부 칸에 **파일별 삭제 [x]** 버튼(`file_camera_{index}` div + `deleteItemImage`) 추가, 확인문구
  `'파일이 실제로 삭제됩니다.\n삭제하시겠습니까?'`는 `Common.confirm`이 아니라 **브라우저 confirm**
- 첨부 다운로드의 `qnaDetailType`은 **`'Q'`**다(1차에 `qna-open`으로 잘못 보냈다) -
  `'Q'`면 `OP_QNA_FILE`, 그 외면 `OP_QNA_ANSWER_FILE`을 본다(`getFrontQnaOpenFileDetail`)
- 답변 없을 때 textarea 기본값 `"안녕하세요. 고향사랑e음 상담센터입니다.\n\n감사합니다."` (AS-IS 문구 verbatim)
- `$('#qnaAnswer').validator()` · `Common.checkedMaxStringLength('textarea[name=answer]', null, 500)` ·
  검색조건 보존 hidden 폼(`#qnaParam`) + `qnaList()`
- **AS-IS 결함 보정**: `deleteItemImageByItemId`는 디스크 파일도 지운다(`fileStorage.delete`) →
  2차 주석에 "디스크 파일은 AS-IS도 남긴다"고 적은 건 **틀렸다**. `QnaFileStorageService.delete()` 추가해 바로잡음.
- **보안 편차**(기존 방침 적용): 문의 제목·본문은 AS-IS가 `op:nl2br`로 **이스케이프 없이** 렌더 → 저장형 XSS.
  `QnaOpenAdminService.escapeNl2br()`로 이스케이프 후 줄바꿈만 `<br/>`. 커뮤니티 본문(에디터 HTML)과 구분.
- `findFirstByQnaIdOrderByAnswerDateAsc` 추가 - AS-IS `ORDER BY ANSWER_DATE LIMIT 1` 그대로.
  `OP_QNA_ANSWER`에 QNA_ID 유일제약이 없어 2행 이상이면 `findByQnaId`(Optional)가 예외로 터진다.

**★AS-IS `view.jsp`·`edit` 진입점 없음 확인**: `/view/{qnaId}`·`/edit/{qnaId}`로 가는 링크가 JSP·JS 어디에도 없다.
게다가 `qna-open/view.jsp`는 **1:1문의(`/opmanager/qna`) 화면을 잘못 복사해 둔 것**이다 - 제목이 "1:1 문의",
모든 URL이 `/opmanager/qna/...`, 조건이 `${qnaAnswer.answer > '1'}`(본문 문자열을 '1'과 비교하는 무의미한 식).
→ **5112에 /view 화면은 만들지 않았다.** 1:1문의 보강 라운드에서 그 메뉴의 화면으로 다룰 것.
`/edit/{qnaId}` GET은 AS-IS처럼 답변화면과 같은 화면을 **첨부만 비워** 보여주고, POST는 AS-IS 결함(답변만 저장)대로 옮겼다.
답변자 `USER_ID`는 AS-IS가 hidden으로 받지만 값이 결국 로그인 매니저라 **세션에서 직접** 쓴다(결과 동일·위조 불가).

**검증**: admin·member `compileJava`·`bootJar` EXIT=0. IPS 적재 INSERT 모양을 트랜잭션에서 실행 후 롤백
(시퀀스·NOT NULL·`812-A022` 확인, 시퀀스 되돌림).

**엑셀 2건 = AS-IS 비활성 확정 → 5112 종결**:
AS-IS 목록 하단 버튼은 **[삭제] 하나뿐**이고 `downloadExcel()`은 **함수만 있고 호출하는 버튼이 없다**.
엑셀 업로드는 컨트롤러 POST가 **주석 처리**되어 있고 `upload-excel.jsp`는 제목이 일본어("Excel ダウンロード")인
복사본이다. → [[asis-porting-principles]]대로 **구현하지 않고 비활성 유지**(목록 템플릿 주석에 기록).
**5112 Q&A는 이것으로 완료.**

##### 5104 FAQ 완료 (2026-10-05) - 신규 화면 + 공개 FAQ 표·코드 교정, admin bootJar EXIT=0

**★ 두 가지가 어긋나 있었고 둘 다 바로잡았다**:
1. **메뉴 5104 'FAQ'가 `/community/faq-bbs`(커뮤니티 담당자FAQ 11405)를 가리켰다** - 고객센터 FAQ를
   누르면 다른 메뉴의 화면이 열렸다. → `/faq-admin`
2. **공개 FAQ가 엉뚱한 표를 읽고 있었다.** AS-IS 정본은 **`op_faq`**다 - 운영자 5104(`/opmanager/faq`),
   공개 페이지(`/faq/list.html`), 공개 API(`/api/faq`), 그리고 통합검색 뷰 **`view_search_faq`**
   (`op_faq`를 보고 `/faq/list.html`로 링크)까지 전부 같은 표다. 그런데 TO-BE 초기 시드는 FAQ 63건을
   **`op_community_locgovfaq`**(AS-IS에서 **중지된** 메뉴 11403 지자체FAQ의 표)에 넣고 질문유형 코드도
   AS-IS enum이 아닌 **자체 코드**(`JOIN`·`DONATE`…)로 바꿔 두었다. `op_faq`는 **0행**이었다.
   → 라벨 11건이 AS-IS enum과 **글자까지 같아** 코드만 1:1 되돌려 `op_faq`로 이관(63건, 원본은 안 지웠다).

**★질문유형은 공통코드가 아니라 enum이 정본이다**: AS-IS는 세 화면 모두
`enumMapper.get("FaqType")`를 쓴다 → `admin/service/FaqType.java`로 verbatim 이식.
코드 대응(이관 번역표): `JOIN→F_LOGIN · DONATE→F_CNTR_SYSTEM · POINT→F_CNTR_POINT ·
OFFLINE→F_OFF_CNTR · DESIGNATED→F_CNTR_DESIGNATED · TAX→F_API_PLATFORM · GIFT→F_PRESENT_PURC ·
ORDER→F_ORDER · PRIVATE→F_OPEN · SYSTEM→F_SYSTEM · ETC→F_ETC`.
`op_common_code`의 `FAQ_TYPE` 11건(①에서 넣은 것)은 **중지된 11403 화면이 계속 쓰므로 그대로 둔다**
(그 표의 행은 여전히 `JOIN`… 코드다). `FAQ_TYPE` 1~6은 SalesOn 쇼핑몰 잔재로 이미 `use_yn='N'`.

**신규/수정**
- admin 신규: `domain/Faq`(OP_FAQ) · `service/FaqType`(AS-IS enum verbatim) · `repository/FaqRepository` ·
  `service/FaqAdminService` · `service/FaqException` · `web/FaqAdminController`(`/faq-admin`) ·
  템플릿 `faq-admin/{list,form}.html`
- admin 수정: `service/FaqService`(공개 조회를 `op_faq`로) · `web/FaqController`·`web/FaqApiController`
  (질문유형을 enum에서, id를 Long으로) · `WebConfig`(게이트 `/faq-admin`)
- storefront 수정: `FaqListView.vue` 답변 본문 `{{ }}`→`v-html`
- DDL **적용**: `database/ddl/migration-admin-faq-5104.sql`(시퀀스 `admin.op_faq_seq` start 630000 +
  63건 이관 + 메뉴 URL 교정)

**AS-IS 동작 그대로**
- 목록 **진입(GET)은 빈 목록**(`new PageImpl<>(Collections.emptyList())`), 검색(POST)에서만 조회
- 정렬 **`id DESC` 고정**(화면에 정렬 수단 없음) · 검색은 **제목만**(`where=title` hidden 고정,
  검색구분 select는 주석처리 / 조건식의 `all`·`content` 분기는 옮기고 화면은 주석 유지)
- 운영자 목록은 **미사용 건도 보인다**(화면이 useYn을 안 보냄). 공개화면만 `use_yn='Y'`로 거른다
- 저장은 등록·수정 모두 **`use_yn='Y'`로 다시 써 넣는다** = 이 화면에 사용여부를 끄는 수단이 없다
- 삭제는 **행 삭제**(소프트 아님) · **수정 저장 후 목록이 아니라 수정화면으로 되돌아온다**
- 등록폼은 질문유형 select에 **빈 항목이 없고**(전체 옵션 주석처리) 제목에만 required가 붙는다
- 내용은 **스마트에디터 HTML** → 공개화면은 AS-IS처럼 그대로 렌더(`v-html`/`th:utext`)
- 내용 textarea의 title 속성이 M00661("배송/반품/환불/교환안내")인 복사 흔적까지 그대로 뒀다

**★부수 수정: `op.link.js`가 TO-BE에서 죽어 있었다**
AS-IS는 `layouts/common/inc_common.jsp`에서 `RequestContext = {currentUrl, prevPageUrl}`를 내려주는데
TO-BE에는 그 정의가 없어 `Link.view`/`Link.list`를 쓰는 화면에서 **ReferenceError**가 났다
(`policy/form.html`도 해당). `ManagerAuthAdvice`에 `@ModelAttribute("currentUrl")`를 넣고
`fragments/opmanager-head.html`에서 전역 객체를 내려주도록 고쳤다 - Thymeleaf 3.1에서 `#request`가
제거되어 템플릿에서 직접 못 읽는다. `prevPageUrl`은 AS-IS 프레임워크 추적값이라 비워 둔다
(`Link.list`가 인자 URL로 가는 폴백이 돈다).

**검증**: 63건 이관 후 `op_faq` 유형 분포가 원본과 동일(11/10/7/6/5/5/5/4/4/3/3),
`use_yn='Y'` 63건·`created` 결측 0건, 메뉴 5104=`/faq-admin`·11405=`/community/faq-bbs` 분리 확인.
admin `bootJar` EXIT=0.

##### 매뉴얼 2화면 완료 (2026-10-05) - 5201/5202 분리 + g_mnl로 교정, admin bootJar EXIT=0

**★구조갭 해소**: AS-IS는 **한 컨트롤러**(`ManualManagerController`, `/opmanager/manual/**`)가 두 화면을 담당한다.
TO-BE는 **5201·5202·중지된 5109 셋 다 `/admin/manuals`**를 가리켜 구분되지 않았다. 이번에 나눴다:
- **5202 사용자매뉴얼 → `/admin/manuals`** (AS-IS `manual/user/*`)
- **5201 관리자매뉴얼 → `/admin/manuals/manager`** (AS-IS `manual/manager/*`)
- 5109 '매뉴얼관리'(구)는 AS-IS 중지 메뉴라 그대로 중지 유지

**★두 화면의 성격이 완전히 다르다(분리 규칙의 근거)**
- **사용자매뉴얼(5202)** = 표 `g_mnl`. 한 행이 "공개 화면 한 페이지에 대한 설명"이다.
  어느 화면인지는 `menu_se_code`가 공통코드 **`MENU_URL`**(33건)의 id를 가리켜 정하고,
  **코드의 label이 URL, detail이 화면명**이라 등록 시 그 두 값을 베껴 넣는다. 첨부 1건.
- **관리자매뉴얼(5201)** = **별도 표가 없다**. 운영자가 볼 수 있는 메뉴를 2단으로 묶어 카드로 늘어놓고
  3단 메뉴마다 **`op_menu.file_nm`/`orginl_file_nm`에 파일을 직접 붙인다**
  (그래서 "등록"이 `op_menu` UPDATE 한 번이다. `op_menu`에 그 두 컬럼이 왜 있는지가 이걸로 설명된다).

**★같은 유형의 중복 5번째 발견**: TO-BE가 사용자매뉴얼용으로 **`op_manual`을 새로 만들어** 두었는데
AS-IS 표 **`g_mnl`이 TO-BE 스키마에 이미 있었다**(AS-IS 덤프에도 있다). 기존 주석에는
"AS-IS 원본 DB에는 이 테이블이 없었다(신규 설계)"고 적혀 있었는데 **사실과 다르다**.
두 표 모두 0행이라 옮길 데이터는 없었다. `op_manual` 표는 남겨 두었다(표 정리는 사용자 판단).

**신규/수정**
- admin 신규: `domain/Mnl`(G_MNL) · `repository/{MnlRepository,MnlAdminRepository,ManagerManualRepository}` ·
  `service/{ManualAdminService,ManualFileStorageService,ManualException}` · `web/support/ManualSearchParam` ·
  템플릿 `manual-admin/{list,form,manager-list,manager-form}.html`
- admin 수정: `web/ManualAdminController` 전면 재작성(두 화면) · `domain/Menu`에 `fileNm`·`orginlFileNm` 추가
- **삭제**: `domain/Manual.java`, `repository/ManualRepository.java`(발명분)
- DDL **적용**: `database/ddl/migration-admin-manual-5201-5202-split.sql`(메뉴 URL 분리 + `admin.g_mnl_seq`)

**AS-IS 동작 그대로**
- 사용자매뉴얼 목록 **진입(GET)은 빈 목록**, 등록일 두 칸은 비어 있으면 **오늘로 채운다**(진입에서도)
- 목록은 `g_mnl`과 `MENU_URL` 코드를 **내부조인**하고 `use_yn='Y'`를 걸어서
  **안 쓰는 코드로 등록된 매뉴얼은 목록에서 사라진다**
- 검색구분 PAGE/SUBJECT/CONTENT 셋('전체'는 주석처리) · 날짜버튼 **오늘·1주일·한달·3달·1년 5개** ·
  출력수 **10/20/50/100**(다른 화면은 100이 없다) · 정렬 `frst_regist_pnttm DESC` 고정
- 같은 화면 코드로 **두 번 등록 불가** → "이미 등록 되어 있습니다."
- 등록폼의 스마트에디터는 **주석처리** 상태 → 평범한 textarea 유지
- 첨부 규칙: **50MB·확장자 16종**(커뮤니티 18종과 다름 - hwpx·7z 없음) ·
  저장명 `yyyyMMddHHmmssSSSS_원본명`(`DATENANO_FORMAT`, 4자리 S) · 폴더 **`help`**
- 삭제는 **행 삭제**, 첨부 삭제는 파일 컬럼만 비움(행 유지)
- 일괄삭제는 응답 본문 결과코드(`SUCC`/`FAIL`)로 성공을 알린다(화면이 그걸 보고 문구 선택)
- 관리자매뉴얼은 **ROLE_ADMIN_1~4(`role=='SYSTEM'`)만 메뉴명이 등록 팝업 링크**가 되고
  그 외 권한은 글자만(다운로드는 누구나). 등록은 500x300 팝업이고 저장 후 **부모창 새로고침+팝업 닫기**
- 관리자매뉴얼의 전체선택·[선택 다운로드]·행별 체크박스는 **전부 주석처리** 상태 → 켜지 않음
  (`downloadCheckManual` 함수만 그대로 둠). 카드 4열 배치는 `pinterest_grid.js`

**AS-IS 결함 3건 보정**
1. 등록·수정이 파일 루프 안에서 UPDATE를 **두 번** 실행한다("파일이 있으면" 뒤에 "파일이 없으면"
   주석을 달고 또 한 번) → 한 번만.
2. 첨부 삭제 매퍼(`deleteManualFile`)가 파라미터를 `mnlSn` 하나만 받는데 SQL에서
   `last_updusr_id = #{lastUpdusrId}`를 쓴다 - MyBatis가 단일 파라미터를 이름 무관하게 꽂아 주므로
   **최종수정자에 매뉴얼 번호가 들어간다** → 로그인 운영자 id.
3. 수정 시 `menu_url`/`menu_nm`을 다시 쓰지 않아 **화면 구분을 바꾸면 저장값이 예전 것으로 남는다**
   (목록은 코드를 조인해 보여주므로 화면에는 안 드러난다) → 코드 값으로 다시 채움.

**PostgreSQL 적응 1건**: 관리자매뉴얼 2단 SQL이 `group by menu_parent_id` 상태에서
`order by m.menu_seq, m.menu_id`를 쓴다(CUBRID 허용, PostgreSQL 오류) → `min()` 집계로.
정렬 1순위가 `menu_parent_id`라 결과 순서는 같다.

**검증**: 사용자매뉴얼 목록 SQL을 트랜잭션에서 행 하나 넣고 실행(페이지구분·작성자·URL/화면명 베끼기
확인) 후 롤백. 관리자매뉴얼 2단/3단 SQL 실측(ROLE_ADMIN_1 → 2단 8묶음, 5100 묶음 아래 3단 9건).
메뉴 URL 분리 확인(5201 `/admin/manuals/manager` · 5202 `/admin/manuals` · 5109 중지 유지).

##### 5102 1:1문의 완료 (2026-10-05) - 다른 화면이 올라가 있던 것 교체, admin bootJar EXIT=0

**★메뉴 5102에 엉뚱한 화면이 올라가 있었다**: TO-BE `/admin/shop-inquiries`(36줄, 조회 전용)는
AS-IS **입점문의**(`InquiryManagerController`, `/opmanager/inquiry`)를 옮겨 놓은 것이었고,
표도 AS-IS의 `OP_STORE_INQUIRY`가 아닌 **`OP_SHOP_INQUIRY`를 새로 만들어**(0행) 쓰고 있었다.
1:1문의의 AS-IS 정본은 **`QnaManagerController`(539줄, 엔드포인트 18개)**이고 표는 **`OP_QNA`** -
Q&A(5112)와 **같은 표**다.
→ 화면을 새로 만들고 URL을 **`/admin/inquiries`**로 옮겼다. 발명분(`ShopInquiry` 엔티티·리포지토리·
컨트롤러·템플릿 2종)은 삭제. `op_shop_inquiry`·`op_store_inquiry` 표는 남겨 둠(둘 다 0행).
AS-IS 입점문의는 **op_menu에 행이 없어 메뉴로 접근 불가**한 SalesOn 쇼핑몰 기능이라 별도 판단 대상.

**★QNA_TYPE이 세 화면을 가른다**(AS-IS `Qna` 상수):
`'0'`=**1:1문의(5102)** · `'1'`=상품문의(5103, **중지**) · `'2'`=**공개 Q&A(5112)**.

**★공개 쪽에 그 구분이 아예 빠져 있었다 - 3건 수정(그중 1건은 개인정보 노출)**
1. `QnaService.ask()`가 `qna_type`을 **넣지 않았다** → 마이페이지에서 등록한 1:1문의가
   운영자 5102 화면에 **영원히 안 보일** 상태였다. `'0'`을 넣게 했다.
2. `myInquiries()`에 타입 필터가 없어 **내가 쓴 공개 Q&A 글까지 마이페이지 1:1문의 내역에** 나왔다 → `'0'` 필터.
3. **`publicBoard()`·`publicBoardDetail()`에 타입 필터가 없었다** → 마이페이지 **1:1문의의 제목·작성자가
   공개 Q&A 게시판 목록에 그대로 노출**되고, 비밀글이 아니면 id만 바꿔 **본문까지 열람** 가능했다 → `'2'` 필터.

**신규/수정**
- admin 신규: `repository/QnaIndividualAdminRepository`(AS-IS `getQnaListByParam` 이식) ·
  `service/QnaIndividualAdminService` · `web/support/QnaIndividualSearchParam` ·
  `web/QnaIndividualAdminController`(12 엔드포인트) · 템플릿 `inquiry-admin/{list,answer-form}.html`
- admin 수정: `service/QnaService`(위 3건) · `WebConfig`(게이트 `/admin/inquiries`)
- **삭제**: `domain/ShopInquiry`, `repository/ShopInquiryRepository`, `web/ShopInquiryAdminController`,
  `templates/shop-inquiry-admin/`
- DDL **적용**: `database/ddl/migration-admin-qna-5102-individual.sql`
  (메뉴 URL + 1:1문의 시드 5건·답변 2건 `qna_type='0'` + 두 시퀀스 재설정)

**AS-IS 동작 그대로 / 5112와 다른 점**
- 진입(GET) 빈 목록 · 정렬 `created_date DESC` 고정 · 검색구분 GROUP/LOGIN_ID/SUBJECT/QUESTION
  ('전체'·'이름'은 주석처리) · 상태 라디오 `qnaAnswerCode` ""/0/1
- 컬럼 **7개**(체크박스/No./문의유형/제목/작성자(아이디)/상태/등록일) - 5112는 11개
- **작성자명을 마스킹하지 않는다**(5112 목록은 마스킹한다)
- 목록 상태 문구가 **M00464 '답변대기'** - 검색 라디오의 '답변미완료'와 다르다
- 날짜버튼에 **'전체'(clear)가 있다**(5112에는 없다)
- 답변화면: **답변제목 입력칸이 화면에 있다**(기본값 "문의에 대한 답변입니다.", 5112는 hidden
  "답변입니다.") · 첨부 행은 **첨부가 있을 때만** · 답변자 칸 라벨이 "답변자" · `qnaDetailType` hidden 없음
- **국민비서 문자를 보내지 않는다**(5112는 답변 저장마다 무조건 보낸다). 메일·UMS 분기는
  5112와 같이 화면에 체크박스가 없어 닿지 않는 코드라 옮기지 않았다
- 삭제·첨부는 AS-IS가 두 화면에서 **같은 서비스 메서드**를 부르므로 `QnaOpenAdminService`를 그대로 쓴다
  (같은 규칙·같은 결함 보정 적용)
- `edit/{qnaId}` POST는 5112와 **똑같은 결함**(답변만 저장, 문의 본문 미저장)이고 전송하는 화면도 없다
- 옮기지 않은 조건: `answerCount` 1/2(같은 판정 중복), `itemId`/`sellerId`,
  바깥 쿼리의 `ITEM_NAME`/`ITEM_CODE`/`COMPANY_NAME`/`sido`/`sigungu` -
  전부 **상품문의(5103, 중지)**용이고 `OP_ITEM`/`OP_SELLER`는 TO-BE에서 gift 소유라 조인 불가
- `/view/{qnaId}`는 5112와 같이 **진입점이 없다**(list.jsp의 링크가 주석처리). `qna/view.jsp`가
  5112 쪽으로 잘못 복사된 그 원본이고 `${qnaAnswer.answer > '1'}` 같은 깨진 조건이 있다 → 만들지 않음
- 엑셀 다운로드·업로드도 5112와 같이 **비활성**(호출 버튼 없음 / POST 주석처리). 숨은 iframe만 유지

**남은 갭(이 라운드 밖)**: TO-BE에 **공개 Q&A 글쓰기 경로가 없다**(AS-IS `api/qna` line 541에
`QNA_TYPE_QNA`로 등록하는 엔드포인트가 있다). 공개 게시판이 조회 전용이라 `'2'` 글은 시드뿐이다.

**검증**: 메뉴 URL 5102=`/admin/inquiries`, 타입 분포 `'0'`=5 / `'2'`=9.
목록 SQL 실측(전체 5건 최신순 · 답변완료 2건 · 문의유형 '결제' 검색 1건),
화면별 분리 확인(공개 Q&A 9 / 마이페이지 1:1 회원별 2·2 / 5102 5 / 5112 9).

##### 5110 지자체공지사항 완료 (2026-10-12) - 구조갭 1 해소, 전용 컨트롤러·템플릿으로 분리

**기존 상태가 전면 재작성 대상이었다**: `/admin/locgov-notices`가 `OperationContentController`의
`fillNoticeList`를 5111과 공유하며 발명 클래스(`small-btn`·`pill`·`ghost`·`inline-form`·
`category-nav`·`empty`)로 그려져 있었고, AS-IS에 없는 "분류(NOTICE_CATEGORY_CODE)" 필터까지
붙어 있었다. AS-IS는 `LocgovNoticeManagerController`(259줄)·`list.jsp`·`form.jsp`·`edit.jsp` 3종이
전용이라(5111의 `NoticeManagerController`와 컨트롤러·화면 다 별도, 표만 `OP_NOTICE` 공유)
**완전히 새 컨트롤러·화면으로 분리**했다.

**신규**: `web/LocgovNoticeAdminController`(`/admin/locgov-notices`) ·
`service/LocgovNoticeAdminService` · `repository/LocgovNoticeAdminRepository`(AS-IS
`getLocgovNoticeList`/`getLocgovNoticeCount` 매퍼 SQL 이식, `locgov_code<>'00000'`+
`board_code<>'DONATION'` 고정조건) · `web/support/LocgovNoticeSearchParam` ·
템플릿 `locgov-notice/{list,form,edit}.html`(AS-IS 3개 JSP 마크업 그대로, 실제 opmanager.css
클래스만 사용). `OperationContentController.fillNoticeList`는 5111 전용으로 단순화(불린 파라미터
제거), `adminLocgovNotices` 삭제. `WebConfig` 게이트에 `/admin/locgov-notices/**` 와일드카드 추가
(기존엔 정확히 그 경로 하나만 게이트되어 `/create`·`/edit/**`가 빠져 있었다).

**AS-IS 동작 그대로**: GET 진입은 빈 목록(POST 검색에서만 조회) / 등록일 검색조건이 **GET
진입에도 오늘로 채워짐**(화면에 바로 보임, "전체" 버튼으로 비워도 재검색하면 서버가 다시
오늘로 채우는 AS-IS 한계까지 유지) / 검색구분 제목·내용뿐 / 작성자 칸은 사람이름이 아니라
**지자체명**(상위+하위, g_locgov 조인) / 등록·선택삭제 버튼은 **role=='LOC'일 때만** 노출
(시스템·행안부는 지자체 조건 없이 전체를 봄) / **edit.jsp는 role을 전혀 검사하지 않는다**(상단
버튼바도 없음, 저장버튼 라벨이 "등록"(M00088) 그대로) / form.jsp의 `noticeId!=0` 상단버튼바·
판매자선택 블록은 이 컨트롤러 흐름에서 **도달 불가능한 죽은 코드**라 옮기지 않음.

**추가한 보호 1건**: edit/delete에 5111에서 먼저 발견한 것과 같은 지자체 교차수정 가드
(`requireOwnLocgovOrThrow`)를 적용 - AS-IS edit.jsp/컨트롤러엔 없는 보호지만 같은 표에 대해
이미 한 번 권한버그로 확인·수정한 전례를 따른 것.

**검증**: admin `compileJava`/`bootJar` EXIT=0, bootJar 재기동 후 Playwright로 실사용자 플로우
end-to-end 실행(로그인→목록→등록→검색→수정화면 바인딩→게시여부 토글→목록 반영→선택삭제→
SYSTEM 권한 버튼 숨김·교차조회 24건 중 23건 PASS, 1건은 체크박스 padding 기반 간격이라
gapPx 메트릭이 안 맞는 거짓양성(스크린샷으로 육안 확인 완료)). 발명 클래스 0건
(`btn-defualt`류 AS-IS 오타 클래스는 기존 35곳에서 이미 쓰이는 verbatim 패턴이라 유지).
[[tooling-and-local-env]] [[tooling-and-local-env]]

##### 5111 공지사항 완료 (2026-10-12) - 구조갭 1 완전 해소, 5110 회귀 1건 동반 수정

5110과 같은 날 이어서 처리. 기존 `/admin/notices`도 발명 마크업(`category-nav`·`small-btn`·
`pill`·`ghost`·`inline-form`·`empty`)에 AS-IS에 없는 "분류(NOTICE_CATEGORY_CODE)" 필터·
"지자체 선택" 콤보·`/toggle` 엔드포인트까지 얹혀 있어 **전면 재작성**했다.

**신규**: `web/NoticeAdminController`(`/admin/notices`) · `service/NoticeAdminService` ·
`repository/NoticeAdminRepository`(AS-IS `getNoticeList`/`getNoticeCount` 이식) ·
`web/support/NoticeSearchParam` · 템플릿 `notice-admin/{list,form}.html`.
`domain/Notice`에 **`USER_NAME` 매핑 추가**(목록 "작성자" 칸). `OperationContentController`에서
5111 관리 메서드 전부 제거(공개 `/notices`·`/notices/{id}`·`/api/notices`만 남김),
`OperationContentService`의 `createNotice`/`updateNotice`/`deleteNotice`/`toggleNotice` 삭제,
발명 템플릿 `content/{admin-notice-list,notice-form}.html` 삭제.

**★5110과 다른 점(전부 AS-IS 그대로 - 두 화면을 한 템플릿으로 합치면 안 되는 이유)**:
① AS-IS `NoticeManagerController`는 **등록일을 오늘로 채우지 않는다**(5110만 채운다) → 날짜칸이
빈 채로 뜨고 비워 두면 전체 기간 조회 ② 작성자 칸이 **사람 이름(USER_NAME)**(5110은 지자체명)
③ **역할 분기 없음** - 등록·삭제 버튼 항상 노출(5110은 role=='LOC') ④ 등록·수정이 **같은
화면**(form.jsp, noticeId 유무로 상단 [수정][삭제][목록] / 하단 [등록][취소] 분기. 5110만
edit.jsp 별도) ⑤ 게시여부 라디오를 **`.input-form`으로 감싼다**(5110은 안 감싼다)
⑥ 내용 textarea 클래스가 `editor-content` 단독(5110은 `w90` 추가) ⑦ 삭제 버튼이 `btn-default`
(5110은 `btn-dark-gray`) ⑧ 목록 표가 `.board_write` 안(5110은 `.board_list`)
⑨ `search()`가 날짜를 문자열로만 비교(5110은 Date + `Common.searchDateMonth`)
⑩ 일괄삭제 파라미터명이 `noticeList`(5110은 `locgovNoticeList`).

**경계 번역 승계**: AS-IS는 전체공지를 `locgov_code='00000'`으로 쓰는데 TO-BE 시드는 NULL(14행)
→ 조회는 `(null or '' or '00000')`을 전체공지로 보고, **이 화면에서 새로 등록하는 행은 AS-IS대로
'00000'**을 넣는다.

**옮기지 않은 AS-IS 결함 1건**: `updateNotice` 매퍼가 화면에 입력칸이 없는 `URL`·`TARGET_OPTION`·
`REL_OPTION`·`VISIBLE_TYPE`·`SELLER_SELECT_FLAG`까지 UPDATE해 **수정할 때마다 NULL로 덮어쓴다**
(VISIBLE_TYPE이 NULL이 되면 AS-IS 공개 조회조건에서 빠진다). 엔티티에 그 컬럼들을 두지 않아
자연히 건드리지 않는다.

**★5110 회귀 1건 동반 수정**: 지난 라운드에 만든 `locgov-notice/form.html`·`edit.html`의 게시여부
라디오를 `.input-form`으로 감쌌는데 AS-IS 5110엔 그 래퍼가 없다(5111 패턴을 잘못 적용). 실측
gapPx 0→7로 되돌렸다. 자세한 규칙: [[admin-screen-pitfalls]]

**검증**: `compileJava`/`bootJar` EXIT=0, Thymeleaf 가드 테스트 2종 통과(th:onclick을 쓰려다
`th:attr="onclick=..."`로 교정 - [[admin-screen-pitfalls]]), 재기동 후 Playwright
**31/31 PASS**(목록·검색·등록·수정·상단버튼바·게시여부·선택삭제·5110 회귀 포함) +
공개화면 회귀 **7/7 PASS**(`/notices`·상세·`/api/notices/list`·donation 연계 API·대시보드·
수정화면 ajax 삭제).

**남은 데이터 이슈(화면 아님, 사용자 판단)**: `op_notice` 31행 전부 `USER_NAME`이 NULL이라
기존 공지의 작성자 칸이 비어 보인다(새로 등록하는 글부터는 로그인 운영자명이 들어간다).
또 시드 제목 일부에 `&#39;` 같은 HTML 엔티티가 그대로 저장돼 있어 화면에 문자 그대로 노출된다 -
AS-IS도 `<c:out>` 이스케이프라 같은 모양이므로 parity는 맞고, 데이터 정제 여부만 판단하면 된다.

##### 5107 자료실 완료 (2026-10-12) - 축소구현 보강 + ★첨부 업로드 불능 결함 수정

AS-IS `DataboardManagerController`(294줄) + `list.jsp`/`form.jsp` + `DataboardServiceImpl` +
`databoard-mapper.xml` 4종 대조. 기존 TO-BE(124줄)는 **검색·페이징·건수·출력수·선택삭제가
전부 없고** 첨부도 한 건만 받는 축소 구현이었다.

**★구조적 결함 1건(이번에 발견·수정)**: `OP_DATA_BOARD_FILE.DATA_FILE_ID`는 **integer**인데
엔티티가 `String`으로 매핑돼 있었고 `UUID.randomUUID().toString()`을 넣고 있었다 →
**첨부 업로드는 실행되면 반드시 실패하는 코드**였다(아무도 안 써봐서 드러나지 않음).
엔티티·리포지토리·공개 컨트롤러/DTO를 `Integer`로 바로잡고, AS-IS의 CUBRID serial을 대신할
시퀀스를 만들어 채번한다(`database/ddl/migration-admin-databoard-file-seq.sql`, **적용 완료**,
start 1006 - 시드 6행이 1000~1005).

**신규**: `web/DataBoardAdminController`(재작성) · `service/DataBoardAdminService` ·
`repository/DataBoardAdminRepository` · `web/support/DataBoardSearchParam` ·
템플릿 `data-board-admin/{list,form}.html`(재작성).
**수정**: `DataBoardFileStorageService` 저장명을 UUID → **AS-IS `yyyyMMddHHmmssSSSS_원본명`**
(+`resolve()` 추가, 원본명에서 디렉터리 구분자 제거는 AS-IS에 없는 보강).

**AS-IS 그대로**: GET 진입 빈 목록 · 등록일을 오늘로 채움(5110과 같고 5111과 다름) ·
검색구분 제목/내용 · 역할 분기 없음 · 등록과 수정이 같은 화면(dataId 유무로 버튼만) ·
**첨부 다중 업로드**(`detailImageFiles[]`) · 수정은 기존 첨부를 지우지 않고 추가 ·
확장자 16종/50MB/문구 verbatim · ORDERING은 글당 MAX+1 · 첨부 [x]는 디스크 파일까지 삭제 ·
일괄삭제가 불일치해도 예외 없이 "FAIL"(5110·5111은 예외를 던져 빈 문자열이 된다).

**5110/5111과 다른 화면 디테일(전부 AS-IS)**: 배지 문구 **"추천"**(공지는 "공지") ·
검색구분 라벨 M00011 · 검색어칸 half · 초기화 버튼 btn-dark-gray · 조회수 콤마 포맷 ·
목록 래퍼 `.board_list` · 라벨 "상단게시" · **상단게시 체크박스 checked 하드코딩**(수정화면에서
DB값이 N이어도 체크돼 보이는 AS-IS 결함) · 게시여부 라디오 기본 선택 없음 ·
내용 textarea가 `.smart_editor2-wrap` 안.

**AS-IS 결함 중 옮기지 않은 것**: `deleteDataboard`가 본문 행만 지워 첨부 행·디스크 파일이
고아로 남는다 → 첨부까지 정리한다. `updateDataboard`가 화면에 없는 컬럼(URL·VISIBLE_TYPE 등)을
NULL로 덮어쓰는 것도 옮기지 않았다(엔티티에 없음).

**검증**: `compileJava`/`test`(가드 포함)/`bootJar` EXIT=0, 재기동 후 Playwright **32/32 PASS** -
첨부 2건 동시 업로드→수정화면 노출→원본파일명 다운로드→[x] 개별삭제→금지확장자(.exe) 거부
("유효하지 않은 파일입니다.")→선택삭제 정리, 공개 `/data-board`·`/api/data-board/{id}` 회귀 포함.
h3 제목 관련 2건은 기대값 오류였다 → [[admin-screen-pitfalls]]

##### 5112 Q&A 재검수 (2026-10-12) - 목록 마크업 갭 7건 보정 + 기간버튼 스타일 통일

2026-10-05에 "완료" 처리했던 화면인데 `qna-open/list.jsp`와 다시 전수 대조하니 **목록 마크업에
갭이 7건** 남아 있었다(로직은 이상 없음). 전부 AS-IS대로 되돌렸다:
① 날짜 **[전체](clear) 버튼 누락** ② 안내문구(`btn_all btn_left`) 누락 ③ 목록 표
`summary`/`caption`(M00273) 누락 ④ **colgroup 누락**(AS-IS는 8개인데 컬럼은 11개인 결함 -
그대로 재현) ⑤ 목록 래퍼가 `.board_list`였다(AS-IS는 `.board_write`) ⑥ 상태 행 라벨이
"답변여부"였다(AS-IS "상태") + 라디오가 `span.radio`였다(AS-IS `.flex_box.gap-12 > .input-form`,
[[admin-screen-pitfalls]]) ⑦ 화면출력이 별도 `displayCount`+hidden이었다(AS-IS는
select 자체가 검색폼의 `itemsPerPage` 필드) + `iframe#downloadFrame`·`where` select title 누락 +
등록일 영역에 AS-IS엔 없는 `.search-date` 래퍼.

**★이 화면만의 특징**: `h3`에 **span이 없어** op.manager.js의 메뉴명 덮어쓰기가 먹지 않는다 →
"Q&A"가 그대로 보인다([[admin-screen-pitfalls]]). 다른 화면처럼 span을 넣으면
"Q&A"가 메뉴명으로 바뀌어 버리니 넣지 말 것.

**사용자 지시 1건(AS-IS와 의도적 차이)**: 기간 지정 버튼을 TO-BE 자료실과 같은 `.btn_date`로
통일(AS-IS는 `.table_btn`). 같은 날 1:1문의(5102)도 동일하게 바꿨다 - 이로써 운영관리 33개
화면의 날짜버튼이 전부 `.btn_date`가 됐다. 동작 클래스(today·week-1·month-1·month-3·clear)와
`Common.DateButtonEvent` 셀렉터도 함께 맞춰야 한다(셀렉터를 안 바꾸면 버튼이 먹통).

**검증**: `test`/`bootJar` EXIT=0, 재기동 후 Playwright **35/35 PASS**(버튼 스타일 7종 실측 일치,
마크업 갭 7건, 라디오 간격 gapPx 0, [한달]/[전체] 동작, 검색 9건, 화면출력 변경 재검색,
제목→답변화면 이동). 목록 화면 404는 0건이고 답변화면의 `/ban-word/list-all` 404는 전 화면 공통
기존 갭이다.

**남은 판단거리**: colgroup을 AS-IS대로 8개만 두니 뒤 3개 컬럼(답변여부·답변자·답변일)이 좁아
"답변미완료"가 줄바꿈된다. AS-IS 결함 재현이라 그대로 뒀고, 보기 싫으면 11개로 보정할지 결정 필요.

##### 5113 운영관리 SR게시판 완료 (2026-10-12) - 축소구현(236→AS-IS 443줄) 전면 보강

AS-IS `MaintenanceController`(443줄) + `list/form/edit.jsp` + `MaintenanceServiceImpl` +
`maintenance-mapper.xml` 4종 대조. 기존 TO-BE는 **페이징·건수·출력수·완료구분 다중선택·
SR번호 채번이 전부 없고** 첨부도 한 건만, 검색은 제목만 되는 축소 구현이었다.

**신규**: `repository/MaintenanceAdminRepository`(AS-IS 목록 SQL 이식 - op_manager + 공통코드 4종
LEFT JOIN, 첨부개수 서브쿼리) · `service/MaintenanceAdminService` · `web/support/MaintenanceSearchParam` ·
템플릿 `maintenance-admin/{list,form,edit}.html`(form/edit 신규 분리).
**수정**: `web/MaintenanceAdminController` 전면 재작성(AS-IS URL 체계로:
`/form`·`/edit/{bbsId}`·`/delete-item-image`·`/deleteMaintenance/{bbsId}`·`/file-download/{fileId}`·
`/list/mainten-excel-download`) · `AdminFileStorageService`에 **AS-IS 저장명 오버로드**
(`asIsFileName=true` → `yyyyMMddHHmmssSSSS_원본명`, 기존 호출부 영향 0).

**★이 화면만의 AS-IS 특징(다른 고객센터 화면과 다름)**:
① 목록이 **GET 진입에서도 조회된다**(공지·자료실·Q&A는 검색해야 나온다) ② 정렬이 **긴급 우선**
(`urgent_yn DESC, frst_crt_dt DESC`) ③ 완료구분이 **체크박스 다중선택**(`shArrProcessState`,
진입 시 전체 체크) ④ 날짜버튼 span 클래스가 둘(`day_btns day_btn1`)이라 JS 셀렉터도 `.day_btn1`
⑤ 컬럼 15개에 체크박스/선택삭제가 없다 ⑥ **SR번호를 서버가 채번**(`GGSR-{yy}-{n}`, 화면 입력칸 없음)
⑦ 삭제가 **소프트 삭제**(게시글·첨부 모두 `USE_YN='N'`) ⑧ 제목 클릭이 상세가 아니라 **수정화면**
(AS-IS가 detail 호출을 주석처리했고 detail.jsp 자체가 없다) ⑨ 등록/수정 표 전체가 role 체크로
감싸져 **ROLE_ADMIN_1·2에게만 보이고**, 담당자·조치결과·날짜는 **ROLE_ADMIN_1만** 입력 가능
⑩ 확장자 **18종**(hwpx·7z 포함 - 자료실 16종과 다름) ⑪ `community.css`를 따로 불러온다.

**AS-IS 결함 중 보정한 것 1건**: SR번호 채번이 `SUBSTRING_INDEX(MAX(sr_no),'-',-1)`인데
`MAX`가 **문자열 최대값**이라 10건을 넘기면 'GGSR-26-9' > 'GGSR-26-10'으로 번호가 겹친다 →
일련번호를 숫자로 비교해 채번한다.

**AS-IS와 의도적 차이 2건**: ① 엑셀이 AS-IS는 POI로 .xlsx인데 **admin 모듈에 POI 의존성이 없어
CSV로 내려준다**(파일명·컬럼 구성은 동일) - 필요하면 POI 추가 후 전환 ② 내용(bbsCn)을
`encodeURIComponent`로 감싸 보내고 서버가 디코드하는 AS-IS 왕복은 옮기지 않았다(그대로 받는다).

**검증**: `compileJava`/`test`(가드 포함)/`bootJar` EXIT=0, 재기동 후 Playwright **42/42 PASS** -
GET 진입 조회·완료구분 다중선택 필터·CSR번호 검색·SR번호 채번(GGSR-26-n)·첨부(hwpx) 업로드→
수정화면 노출→원본명 다운로드·금지확장자 2중 방어(클라이언트 addFile alert + 서버 거부)·
엑셀 파일명·권한 분기(ROLE_ADMIN_6은 access-denied)·소프트 삭제 정리까지.

**부수 확인**: `.file_camera` 배경으로 쓰이는 `/content/images/btn/file_camera.gif`는 **AS-IS에도
없는 파일**이라 원본에서도 404다(아이콘만 안 보이고 기능 영향 없음). 첨부 쓰는 화면 공통.

**후속 수정(2026-10-12, 사용자 지적)**: 목록 '내용' 칸에 스마트에디터 HTML이 그대로 노출됐다
(`<p>xcvxcv...</p>`). AS-IS도 `<c:out>`이라 같은 모양이지만 표에 태그가 보이는 건 명백한 문제라
`MaintenanceAdminService.plainText()`로 **목록·엑셀에서만** 태그를 벗기고 줄바꿈을 공백으로
정리한다(`<br>`·블록 끝은 공백, HTML 엔티티 디코드). '답변'(조치결과)도 textarea 줄바꿈이
섞이므로 같이 정리한다. **수정화면은 원본 HTML을 그대로 보존**한다(에디터 입력값이라 건드리면 안 됨).
회귀 방지로 `MaintenanceAdminServicePlainTextTest` 5케이스 추가.

##### 5114 답례품제공자 공지사항 완료 (2026-10-12) - 고객센터 11화면 전부 종료

AS-IS `SysNoticeSellerController`(290줄) + `list/form.jsp` + `SysNoticeSellerServiceImpl` +
`sys-notice-seller-mapper.xml` 대조. 기존 TO-BE(203줄)는 페이징·건수·출력수·날짜검색·
검색구분이 없고 첨부도 한 건만 받는 축소 구현이었다.

**신규**: `repository/SysNoticeSellerAdminRepository`(AS-IS 목록 SQL - `BOARD_CODE IS NULL` +
`USE_YN='Y'` 고정, 첨부개수 서브쿼리) · `service/SysNoticeSellerAdminService` ·
`web/support/SellerNoticeSearchParam`. **재작성**: `web/SysNoticeSellerAdminController`
(AS-IS URL로: `/create`·`/edit/{noticeId}`·`/delete/{noticeId}`·`/deleteNotice`·
`/delete-item-image`·`/file-download/{fileId}`) · 템플릿 `seller-notice-admin/{list,form}.html`.

**AS-IS 그대로**: 목록은 GET 진입에서도 조회 · **등록일을 오늘로 채우지 않는다**(5111과 같음) ·
컬럼 7개(체크박스/No./제목/조회수/등록일시/수정일시/게시여부) · **게시여부 문구가 '게시'/'중지'**
(공지사항은 '-'/'중지') · 정렬 상단공지 우선 + 최신순 · 삭제는 소프트 삭제(글·첨부 모두) ·
등록과 수정이 같은 화면 · 첨부 1건, 확장자 18종/50MB · 등록 confirm "공지를 등록하시겠습니까?".
**권한**: 목록은 AS-IS가 권한 검사를 주석처리해 두어 제한이 없고, **등록·수정·삭제만
ROLE_ADMIN_1·2**로 막는다(문구도 AS-IS verbatim).

**AS-IS 결함 2건 보정**: ① 선택삭제가 `/sellerNotice/deleteNotice`로 보내는데 **그 엔드포인트가
컨트롤러에 없어 404**였다(버튼이 아무 일도 못 함) → 같은 주소로 구현 ② `search()`가 존재하지 않는
id(`#startCreateDate`)를 읽어 날짜 검증이 늘 통과했다 → 실제 id(`startDt`/`endDt`)를 보도록 교정.

**★구현 중 500 하나를 밟고 고쳤다**: `NOTICE_FLAG`·`DISPLAY_FLAG`가 `varchar(1)`이라 네이티브
쿼리가 **Character로** 돌려주는데 `(String)`으로 캐스팅해 목록이 즉시 500이었다 →
[[backend-db-pitfalls]]

**검증**: `test`/`bootJar` EXIT=0, 재기동 후 Playwright **34/34 PASS**(GET 진입 조회·검색·
상단공지 우선 정렬·첨부 업로드/다운로드/소프트삭제·금지확장자 2중 방어·선택삭제 동작·
게시여부 '게시'/'중지'·권한 분기·정리까지).

#### 보류 (2026-10-05 사용자 지시 "나중에 처리할 사항으로 남겨두고 다른 메뉴 구현")
**★★고객센터 11화면 전부 완료(2026-10-12)**. 승인순서 4건(5112·5104·매뉴얼2·5102)에 이어
같은 날 5110·5111·5107·5113·5114를 끝냈고 5112는 재검수로 마크업 갭 7건을 보정했다.
구조갭 1(5110 vs 5111 URL 충돌)도 해소. 아래는 **다음 기회에** 할 것:
2. **공개 Q&A 글쓰기 경로 신규** - AS-IS `api/qna`에 `QNA_TYPE_QNA`로 등록하는 엔드포인트가
   있는데 TO-BE에는 없다(공개 게시판이 조회 전용). [[domain-code-facts]]
3. 5104 FAQ 라운드에서 남긴 표 정리 판단(`op_community_locgovfaq`), 매뉴얼 라운드의 `op_manual`,
   1:1문의 라운드의 `op_shop_inquiry`·`op_store_inquiry` - 전부 0행 또는 중지화면 전용.

**★TO-BE 결함 발견(5112 밖, member 영역) - `RECEIVE_SMS` 인코딩이 두 갈래다**:
AS-IS 정본은 **`0`=수신, `1`=비수신**(`GeneralCustomer` 주석). 그런데 TO-BE는
`MemberService:652`가 **`Y`/`N`**으로 쓰고 `ExternalLoginService:214`는 **`0`**으로 쓴다.
읽는 쪽도 갈린다: `ProfileApiController:62`는 `"Y".equals`, admin `member-admin/details.html`·`info-access.html`은 `== '0'`.
→ 지금 일반가입 회원(`Y`)은 **운영자 상세화면에서 "비동의"로 표시**되고 Q&A 답변 문자 대상에서도 빠진다.
고치려면 member 쓰기/읽기 + 기존 행 데이터 변환(`Y`→`0`, `N`→`1`)이 필요하고, 같은 의심이
`receive_email`·`receive_pbanc`·`receive_kakao`에도 있다. **데이터 변환은 사용자 판단 대상**이라 보류·기록.

**★AS-IS 공통 패턴 재확인**: 공지(5110·5111)·사용자매뉴얼(5202) 모두 **GET 진입은 `count=0`+`EMPTY_LIST`,
POST 검색에서만 실제 조회**한다. 이 프로젝트에서 반복 확인된 패턴이라 이식 시 그대로 유지할 것.
(5201 관리자매뉴얼은 GET·POST 모두 매뉴얼 행을 싣지 않고 메뉴 트리만 싣는다 - JSP가 menuList로 그린다.)

#### 재사용할 공용 자산 (커뮤니티 라운드 산출물)
`service/CmntyFileStorageService`(AS-IS 50MB·확장자 18종·`yyyyMMddHHmmssSSS_원본명`·크기표기) ·
`service/CmntyText`(decode·nl2br) · `service/CmntyException` ·
**`fragments/file-upload-agree.html`(자료 업로드 전 보안 점검 모달 - AS-IS는 전역 포함이니 첨부 있는 화면엔 반드시 붙일 것)** ·
`web/support/Pagination` · `fragments/pagination :: manager`.

### admin-small-areas-port-progress

> 소규모 메뉴영역 묶음 이식 원장(기부혜택증3·오프라인기부금접수2·대시보드1·커뮤니티5) - 사용자가 정한 순서, 구조갭·AS-IS 매핑


[[asis-screen-port-procedure]] 절차로 진행하는 **세 번째 묶음 = 소규모 영역 4개**. 사용자가 정한 순서:
**기부혜택증관리 3 → 오프라인기부금접수 2 → 기부현황 대시보드 1 → 커뮤니티 5**(2026-10-04).
앞선 영역: 시스템관리 25/25([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-system-area-port-progress)), 회원관리 8/8([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-member-area-port-progress)).

**AS-IS url 실측**(`OP_MENU_202610010945.sql`) - TO-BE menu_url과 대조해 구조갭을 먼저 찾았다:

| 영역 | id | 메뉴 | AS-IS url | TO-BE url | 상태 |
|---|---|---|---|---|---|
| 기부혜택증 | 19101 | 기부혜택증 설정 관리 | `/opmanager/lclgvHnrUser/lclgvHnrUserMng/list` | /admin/honor-users | **완료** |
| 기부혜택증 | 19102 | 기부혜택증 설정 | `/opmanager/lclgvHnrUser/lclgvHnruserMng/form` | /admin/honor-users/**form** | **완료**(분리) |
| 기부혜택증 | 19103 | 기부혜택증 열람현황 | `/opmanager/lclgvHnrUser/lclgvHnrUserViewHist/list` | /admin/honor-users/view-history | **완료** |
| 오프라인기부금접수 | 15101 | 기부금 접수관리 | `/opmanager/offgive/list/` | /offgive/**list** | **완료**(분리) |
| 오프라인기부금접수 | 15102 | 기탁서 등록 | `/opmanager/offgive/create` | /offgive/**new** | **완료**(2026-10-13 verbatim 재이식, 데이터 갭은 [[offgive-gift-order-asis-flow]]) |
| 대시보드 | 20101 | 기부현황 대시보드 | `/opmanager/bix5-access` | /admin/bix5-access | |
| 커뮤니티 | 11401 | 소통방 | `/opmanager/community/bbs/list` | /community/bbs | |
| 커뮤니티 | 11402 | 자료실 | `/opmanager/community/databoard/list` | /community/databoard | |
| 커뮤니티 | 11404 | SR 게시판 | `/opmanager/community/srBbs/list` | /community/sr-bbs | |
| 커뮤니티 | 11405 | 담당자용 FAQ | `/opmanager/community/faqBbs/list` | /community/faq-bbs | |
| 커뮤니티 | 11406 | 오프라인 담당자 SR 게시판 | `/opmanager/community/offSrBbs/list` | /community/off-sr-bbs | |

**★기부혜택증관리 3화면 완료 - 2026-10-04**: AS-IS `LclgvHnrUserManagerController` 8개 엔드포인트 전부
(목록 GET/POST·설정폼 GET·저장·대표이미지삭제·열람현황 GET/POST·엑셀) + 템플릿 3개 재작성
(`honor-user-admin/{list,form,view-history}.html`). AS-IS JSP 1,295줄(form 748 + list 264 + viewHist 283) 대비 기존 TO-BE는 331줄이었다.
- **구조갭 해소**: 19101(목록)·19102(설정 폼)가 같은 URL이었다 → 19102를 `/admin/honor-users/form`으로 분리
  (`migration-admin-menu-19102-honor-user-form.sql`, 적용 완료). 4402·4501과 같은 유형.
- **★소유 서비스를 donation으로 옮겼다**: 기부혜택증 설정(`g_lclgv_hnr_user_stng_mng`)·이미지설명
  (`g_lclgv_hnr_user_rwrd_img_expln`)·열람이력(`op_honor_view_hist`)이 전부 donation 스키마에 있고 형제 표
  (`g_honor_benefit`·`g_honor_cntrbtr`·`g_honor_cntrbtr_stdr`)도 그렇다. admin 스키마에도 **같은 이름의 빈 표**가
  있었는데(예전 TO-BE 구현이 만든 것) 실데이터는 donation에 쌓인다.
- **★발견한 결함 1건 고침**: 예전 TO-BE 열람현황 화면이 **admin 스키마의 빈 `op_honor_view_hist`를 읽어 항상 0건**이었다.
  실제 열람이력은 storefront 열람 시 donation이 쌓는다(실측 5건). donation API로 바꿨다.
  admin 쪽 죽은 엔티티·레포 4개(`LclgvHnrUserStngMng`/`OpHonorViewHist` + 레포)는 **삭제**했고, 빈 표 자체는 남겼다
  (DROP은 파괴적이라 사용자 판단 - op_qustnr* 선례와 동일).
- **열람현황은 집계 화면이다**: AS-IS는 `(열람일자 yyyy-MM-dd, USER_ID, LCLGV_CD)`로 GROUP BY해 `COUNT(*)`를 열람횟수로 쓰고
  OP_USER·G_LOCGOV를 **INNER JOIN**한다. TO-BE는 회원이 member 소유라 donation이 지자체만 조인해 묶음을 주고
  **사용자명은 member 일괄조회**로 채운다 - AS-IS가 INNER JOIN이므로 **회원을 못 찾은 행은 admin이 버린다**. 정렬도 AS-IS대로 지자체코드→사용자명.
- 신규 API: donation `/api/admin/honor-users`(목록·상세·저장·대표이미지·삭제·`/view-hist`), member `GET /api/admin/members/names?userIds=`(회원 이름 일괄조회 - 첫 사용처가 이 화면이다).
- AS-IS verbatim: h3가 메뉴명이 아니라 **"기부혜택증 기준목록"/"기부혜택증 열람현황목록"/"{지자체명} 기부혜택증 기준 등록"**(수정인데 '등록') /
  출력개수 select가 다른 화면과 달리 **10·50·100·200·500** / **골드·실버 등급 금액은 입력칸·컬럼 모두 주석처리**(발급기준금액=브론즈 하나만 씀) /
  목록의 **등록 버튼도 주석처리** / 열람현황 날짜버튼에 **15일**이 있다 / 열람현황 표 caption이 "지자체 기부혜택증 기준 목록"(복사 흔적) /
  설정폼의 발급기준 라디오 옆에 공통코드 **detail 설명문**을 그대로 찍고 물음표 아이콘이 안내이미지(honor_rule4.png) 팝업을 띄운다 /
  이미지 설명표 안내문구와 순번 readonly / 메인이미지 옆 '기본 이미지'(default_honor_img.png) 동시 노출 / 미리보기는 swiper 카드 모달.
- 공통코드 `HNR_USER_SLCTN_SE_CD` 3종 실측: BF_YR_1YR(기준1 전년도) / NOW_CRTR_1YR_WTHN(기준2 최근1년) / JEJU_REQ_CRTR(기준3 회계연도) - label+detail 모두 있어 AS-IS 화면과 그대로 맞는다.
- 권한분기: 시스템·행안부(1~4)는 목록, **지자체 담당자(5·6)는 목록 없이 자기 지자체 설정폼으로 리다이렉트**(남의 것을 열면 자기 것으로 되돌림).
  열람현황도 지자체 담당자는 자기 지자체로 강제 스코프되고 **지자체 검색칸 자체가 숨는다**.
- 설정 저장은 AS-IS가 UPDATE 한 방이라 행이 없으면 아무 일도 없었는데(실측 0건), **없으면 만들고 있으면 수정**으로 바꿨다.
  대표이미지는 AS-IS처럼 새 파일이 올라왔을 때만 갱신하고, 이미지 설명은 지자체 단위로 전부 지우고 다시 넣는다(AS-IS 동일).
- **검증**: 열람현황 집계 5건(날짜·회원·지자체 묶음)·회원 INNER JOIN 판정(user 1000=테스트유저2)을 SQL로 대조. admin·donation·member bootJar 전부 EXIT=0.
- **주의**: AS-IS가 기간 기본값을 오늘로 채우므로, 실측 열람이력이 2026-09-11자라 **기본 검색은 0건이 정상**이다(기간을 넓혀야 보인다).

**★기부금 접수관리(15101) 완료 - 2026-10-04**: AS-IS `GET/POST /opmanager/offgive/list` + `/list/download-excel` 이식
(`OffgiveReceiptAdminController` + `offgive/list.html` 재작성, donation `GET /api/admin/offgive/search` 신규).
- **구조갭 해소**: 15101·15102가 같은 URL(`/offgive`)이었다 → 15101=`/offgive/list`, 15102=`/offgive/new`로 분리
  (`migration-admin-menu-15101-15102-offgive-split.sql`, 적용 완료). 예전 `/offgive` 링크는 `/offgive/list`로 리다이렉트.
- 예전 TO-BE 목록은 검색조건 4개(지자체·기간·이름)뿐이었다 → **AS-IS 검색조건 전체**(검색구분 4종·신청일·금액범위+바로가기버튼·기부상태·소속지점)와 **11컬럼** 복원.
- **★AS-IS 기부상태 숫자코드 번역**: AS-IS는 `100 신고 / 200 수납 / 300 과오납`인데 TO-BE `g_cntr.cntr_sttus_code`는 낱말
  (`REQUESTED`/`COMPLETED`/`CANCELLED`)이다. **화면 라디오 값은 AS-IS대로 100/200/300 유지, 변환은 donation 레포 경계에서** 한다
  (담당자 사용여부 9/2↔ACTIVE/LOCKED와 같은 방식). 오프라인분만 보이게 `cntr_path_code='200'`으로 한정.
- **권한별 스코프(AS-IS role)**: 1~4=SYSTEM(전체 + **소속지점 라디오 검색칸 노출**) / 7=OFF_MAIN(자기 **지점코드**만 고정 - 같은 은행의 모든 지점을 본다) /
  8=OFF_SUB(**지점코드+지점명** 고정 = 자기 지점만) / 그 외=LOC(지점조건 없음). role을 못 정하면 "권한이 없습니다."
- AS-IS verbatim: 이름 **마스킹**(`첫글자 + ' * ' + 세번째글자부터` - 두 글자면 뒤가 빈다) / 지점·센터명 = 은행명+공백+지점명 /
  신고일은 신청시각 앞 19자 / 빈 목록은 표 밖 `no_content` div(M00473) / '출력' 버튼·체크박스 칸은 주석처리 /
  **기탁서 등록 버튼은 SYSTEM이 아닐 때만** 노출 / 날짜버튼 모델 키가 `year`(다른 화면은 year1) / 안내문구 "기탁서 등록 후 정상 처리까지 최대 1시간 가량 소요될 수 있습니다."
- **검증**: 오프라인 접수분 6건이 11컬럼으로 조회됨(SQL 대조). 다만 실데이터의 `rcept_bank_code`가 전부 빈 값이어서
  지점 스코프는 데이터로 걸러지지 않는다(기존 TO-BE 접수 폼이 지점코드를 안 넣었다 - 로직이 아니라 데이터 문제).
  `rcept_bank_nm` 2건은 **한글 인코딩이 깨져 있다**(과거 테스트데이터 적재 흔적, 이번 작업과 무관).

**★★기탁서 등록(15102) 보류 - 결정 필요 (2026-10-04)**: "2화면 영역"으로 보이지만 실측은
**AS-IS 컨트롤러 1,191줄 / 28개 엔드포인트 / form.jsp 1,995줄**이고 다음에 묶여 있다:
1. **본인인증(SCI) 외부연계** - `sci-call`/`sci-result`/`user-check`/`getPublicKey`/`getUserAddressInfo`(내국인)·`getUserAddressInfoForeign`(외국인).
   기존 TO-BE는 이걸 **"담당자가 현장에서 신분증 직접 확인"으로 대체**해 두었고, 그 판단은 `OffgiveController` 주석에
   **"사용자 확인 없이 진행된 판단"**이라고 스스로 적어 놓았다 → 이식할지/대체를 유지할지 사용자 결정이 필요하다.
2. **납부 게이트웨이** - `nextBugaRequest`(지방세외 부과요청)·`getSdonationCharge`(서울 기부금 조회). [[donation-payment-gateway-port-decision]] 라운드와 함께 가야 한다.
3. **ROLE_ADMIN_11(OFF_CENTER, 오프라인 센터)** - AS-IS 권한분기에 있는데 TO-BE OP_ROLE에는 **없다**(실측 1~8만).
   15101에는 분기 자리만 남겨 뒀다. 권한을 새로 만드는 일이라 결정 필요.
4. 기탁서 서식 팝업 3종(`popup/form-policy`·`form-apply/{cntrSn}`·`form-list`)·기부한도 확인(`maxCheck`)·지정기부 선택(`getDesignatedDonationList`)·
   취소(`offgiveCancel`/`offgiveCancelReq` - 기존 TO-BE는 give-reqmng으로 대체)·`offRprs`·`updatePhoneNumber`.
→ 독립 세션 + 위 1·3 결정이 선행돼야 한다. 그래서 15101만 완결하고 15102는 기존 TO-BE 접수 폼(`/offgive/new`)을 가리키게 두었다.

**★기부현황 대시보드(20101) = 추가 작업 없음 (2026-10-04 재검증)**: 이전 라운드가 이미 올바르게 처리해 뒀고
이번에 독립 검증했다.
- AS-IS `ghlove-web`에 `/opmanager/bix5-access` **핸들러도 JSP도 없다**(bix5 grep 0건) → 클릭하면 404
  (`public/error/404.html`, 120줄). 즉 AS-IS에서 "메뉴는 있으나 눌러보면 페이지 오류"다.
- TO-BE `Bix5AccessController`가 그 404 페이지를 verbatim 복사해 **404 상태로** 돌려준다
  ([[asis-porting-principles]] 그대로). 핵심문구("페이지 오류"/"페이지가 존재하지 않습니다") 대조 일치.
- **BIX5 통계 배치**(`Bix5Service` 20개 메서드 + `bix5-mapper.xml`)는 ghlove-common에 있지만 **AS-IS 웹에서 호출부가 0건**이다
  (별도 배치 배포나 외부 BI 도구가 테이블을 읽는 구조). TO-BE DB에도 bix5 테이블이 없다 → 지금 붙일 대상이 없다.
  외부 BI 연계는 운영 인프라 사안이라 기록만 한다.

**★커뮤니티 5화면 실측 - 소규모가 아니다(2026-10-04). 다음 세션 과제**:
- AS-IS 컨트롤러가 3개로 나뉘어 있다: `community/freeboard/CommunityManagerController`(**1,872줄**, 엔드포인트 40+ -
  bbs·srBbs·faqBbs·offSrBbs를 한 클래스에서 다룬다), `community/locgFaq/LocgFaqManagerController`,
  `community/locgovDataBoard/LocGovDataBoardManagerController`.
- JSP는 게시판마다 list/form/edit/detail 4종씩: freeboard 791 / databoard 904 / srBbs 1,202 / faqBbs 1,049 / offSrBbs 1,143 = **합계 ~5,100줄**.
  (그 외 `oldFreeBoard`·`locgfaq` 폴더도 있다 - live 여부 확인 필요.)
- 게시판마다 **댓글(cmnt) CRUD + 첨부파일 업로드/삭제/다운로드**가 붙어 있다(srBbs·faqBbs·offSrBbs는 댓글에도 파일이 붙는다).
- TO-BE 템플릿 디렉터리는 `community/{board,databoard,locv-faq}` 3개뿐 - 5종 게시판과 1:1이 아니다(대조 필요).
→ 한 세션에 한 게시판씩 잡는 것이 맞다. 시작 전 TO-BE 기존 자산과 1:1 대조부터([[asis-screen-port-procedure]]).

**★커뮤니티 1:1 대조 결과 - "0/5"의 정확한 뜻 (2026-10-04, 사용자가 순서 일임: "나머지도 니가 순서 정해서 해")**
- **TO-BE에 골격이 있었다**(기능 기준 0/5는 맞지만 백지가 아니다): `CmntyBoardController`(226줄, `/community/{boardType}`로
  bbs·sr-bbs·off-sr-bbs·faq-bbs 4종을 한 컨트롤러에서) + `CmntyRpstrController`(70) + `LocgFaqAdminController`(101) = **397줄**,
  템플릿 6개 **400줄**, 도메인 7개·리포지토리 6개. AS-IS는 컨트롤러 **2,493줄** + JSP **5,638줄** + 엔드포인트 **56개**
  (bbs 11 / srBbs 15 / faqBbs 15 / offSrBbs 15)다. 골격에는 **detail·edit 템플릿이 아예 없고** 검색·페이징·조회수·비밀글·
  첨부파일·댓글파일이 전부 없다 → 라인 기준 약 7%.
- **골격의 댓글 설계는 AS-IS에 없는 발명이다**: `G_CMNTY_COMMENT`(boardType 판별컬럼 단일표). AS-IS는 게시판마다 전용 표
  (`g_cmnty_cmnt` / `g_cmnty_{sr,faq,off_sr}_bbs_cmnt` + `_file`)를 쓴다. **AS-IS 표 16개가 TO-BE admin 스키마에 이미 다 있다**
  (전부 0건). 그래서 이식하는 게시판은 AS-IS 표로 갈아탄다. 발명 표는 남은 3개 게시판이 아직 쓰므로 그때까지 두고,
  다 끝나면 죽은 표가 된다(DROP은 파괴적이라 사용자 판단 - op_qustnr*·honor 선례와 동일).
- **★숨겨진 6번째 게시판**: AS-IS 메뉴 `11403 지자체FAQ`(`/opmanager/community/locv-faq/list`)가 **display_flag='N',
  status_code='2'(중지)**다. JSP 4종 538줄 + 컨트롤러 343줄이 실재한다. [[asis-porting-principles]]대로
  코드는 이식하고 메뉴는 중지 유지. **TO-BE op_menu에는 11403 행이 아예 없어서 중지 상태로 추가해야 한다.**
- **★URL 충돌 1건(구조갭 5번째)**: TO-BE `5104 FAQ`와 `11405 담당자용 FAQ`가 **둘 다 `/community/faq-bbs`**다.
  AS-IS에선 전자가 `/opmanager/faq/list`(고객센터 FAQ), 후자가 `/opmanager/community/faqBbs/list`(지자체 담당자용 운영 FAQ)로
  **서로 다른 화면**이다. 5104는 고객센터(11화면) 영역 소속이니 그 라운드에서 자기 URL을 받아야 한다.
  (5107 자료실·5113 운영관리SR은 TO-BE URL이 달라서 충돌 아님 - 확인했다.)
- **내가 정한 순서**(의존 패턴 기준): ① 11401 소통방(댓글만, 첨부 없음 - 댓글 패턴 확정) → ② 11404 SR(본문+**댓글** 첨부 =
  최대 복잡도, 파일 패턴 확정) → ③ 11406 오프라인SR → ④ 11405 담당자FAQ(②와 구조 거의 동일) → ⑤ 11402 자료실 →
  ⑥ 11403 지자체FAQ(중지 유지).
- 정적자산은 이미 복사돼 있다: `static/content/modules/cmnty/{bbs,cmnt,srBbs,srBbsCmnt,faqBbsCmnt,offSrBbsCmnt}.js` +
  `content/css/community.css`. **AS-IS 원본(`ghlove-web/.../content/modules/cmnty/`)은 지금 비어 있으니 TO-BE 복사본이 정본 사본이다.**
  `op.common.js`에 `Common.responseHandler`·`DateButtonEvent`·`searchDateMonth`·`getEditorContent`가, `fragments/smarteditor.html`에
  스마트에디터2가 이미 있다. JSON 계약: 성공 `{isSuccess:true, data:"<문구>"}` / 실패 `{isSuccess:false, errorMessage:"<문구>"}`.

**★①소통방(11401) 완료 - 2026-10-04**: AS-IS `CommunityManagerController`의 bbs/cmnt **11개 엔드포인트 전부** +
템플릿 4종 신규(`community/freeboard/{list,detail,form,edit}.html` - AS-IS JSP 791줄 대응). admin bootJar EXIT=0.
- 신규: `domain/CmntyCmnt`(AS-IS `G_CMNTY_CMNT`), `repository/{CmntyCmntRepository,CmntyBbsAdminRepository}`,
  `service/{CmntyBbsAdminService,CmntyException}`, `web/CmntyBbsAdminController`(`/community` 기준 - AS-IS 경로 모양대로
  게시글은 `bbs/*`, 댓글은 `cmnt/*`), `web/support/CmntyBbsSearchParam`.
- `CmntyBoardController`에서 **bbs만 떼어냈다**(TITLES·switch 5곳). 남은 3개는 이식될 때까지 그대로 - 게시판 이식할 때마다 떼어내면 된다.
- **★AS-IS 조건의 TO-BE 번역 2건**: ① 작성자 권한 - AS-IS는 `(select min(authority) from op_user_role where user_id=...)`인데
  **TO-BE에 `op_user_role` 표가 없다**(`op_manager.authority` 한 컬럼) → 그 값을 그대로 쓴다. 소속 검색(searchRole)도 같은 치환.
  ② 작성자 소속 지자체명 - AS-IS는 `g_locgov` 조인인데 TO-BE에선 donation 소유 → 레포는 `locgov_code`만 주고
  서비스가 `LocgovClient.allLocgovs()`로 이름을 채운다(19101과 같은 방식).
- **★AS-IS와 다른 점 - 소통방은 진입(GET)에서도 조회한다**(`list`와 `searchList`가 같은 코드). 다른 운영화면의
  "검색해야 조회" 규칙과 반대라 착각하기 쉽다.
- AS-IS verbatim: 상단 빨간 안내문구("타인의 개인정보를 작성하지 않도록 각별히 유의 해주시기 바랍니다." - 그 위
  "행안부와 지자체를 위한…"은 **주석처리** 상태 유지) / **'작성자' 컬럼과 검색구분 'USERNAME' 옵션 모두 주석처리** /
  소속 표기 3분기(1·2 "시스템 관리자"는 **이름 안 찍음**, 3·4 "행정안전부"+이름, 그 외 상위지자체명+지자체명+이름) /
  공지 라벨 + 댓글 빨간 `[N]` / 상단공지 체크박스는 **1~4에만** 노출 / 체크박스 id는 `noticeYn1`·`isSecret1`
  (bbs.js가 그 id로 읽어서 **바꾸면 등록이 깨진다**) / 제목 maxlength가 등록 255·수정 1000으로 **서로 다른데 그대로 뒀다**(컬럼은 varchar(300)) /
  빈 목록은 표 밖 `.no_content`(M00170) / 정렬 `notice_yn desc, frst_crt_dt desc` / 삭제는 전부 `use_yn='N'` 소프트삭제.
- **권한규칙(AS-IS 그대로)**: 글·댓글 수정/삭제는 **작성자 본인만** - 시스템 관리자도 예외 없다("본인 게시글만 삭제 가능합니다." 등 4문구).
  비밀글은 작성자 + **ROLE_ADMIN_1·2만** 열람(3·4·5·6은 남의 비밀글 못 봄). `role`은 1~6만 값이 들어가므로 ROLE_ADMIN_10은 빈 값 취급.
  op_menu_right 실측 11401=1,2,3,4,5,6,10 → AS-IS export와 **일치**(수정 불필요).
- **★조회수는 비밀글 차단보다 먼저 올라간다**(AS-IS 순서) + **수정화면 진입에서도 올라간다**(상세와 같은 메서드를 쓴다).
- **AS-IS 결함 1건 안전하게 고침**: `detailBbs`가 글을 못 찾아도 `detail.getIsSecret()`을 먼저 호출해 **NPE**가 난다 →
  컨트롤러의 "해당 글은 존재하지 않습니다." 리다이렉트가 **도달 불가 코드**였다(삭제된 글 링크 = 500). null 반환으로 바꿔 본래 의도대로 안내.
- **의도적 차이 1건(보안)**: AS-IS는 댓글 본문도 `escapeXml="false"`로 raw 렌더링해 **저장형 XSS**가 가능하다.
  댓글칸은 일반 textarea라 HTML이 필요없고 `<pre>`가 줄바꿈을 살리므로 이스케이프했다(정상 글은 보이는 모습 동일).
  **게시글 본문은 에디터 HTML이라 AS-IS대로 raw + nl2br 유지.**
- 체크박스 초기상태도 방식만 다르다(결과 동일): AS-IS는 form:checkbox value에 현재값을 넣어 **항상 체크**로 그린 뒤
  인라인 JS가 'N'이면 푸는 식 → 서버에서 'Y'일 때만 체크해 내려주고 그 보정 스크립트를 뺐다.
- JS 자산은 경로 접두어만 고쳤다(`/opmanager/community/...` → `/community/...`). AS-IS `cmnt/add/`의 **끝 슬래시**는
  Boot 3에서 404라 뗐다. 파일 머리에 "바꾼 것은 경로뿐" 주석을 달았다.
- **검증**(`seed-admin-cmnty-bbs.sql`, 적용 완료 - 글 6건·댓글 3건): 공지 상단고정 / 소프트삭제 글·댓글 제외(6→5, 댓글 3→2) /
  댓글수 `[2]` / 소속검색 지자체 3건·시스템 1건 / 시군구 26350 1건 / 제목 LIKE 1건 / 등록일 범위 3건 - 전부 SQL 대조 일치.
  admindb 롤의 `g_cmnty_cmnt` 권한·시퀀스 2개 USAGE도 확인.

**★②SR 게시판(11404) 완료 - 2026-10-04**: AS-IS {@code srBbs/*} **15개 엔드포인트 전부** + 템플릿 4종
(`community/srBbs/{list,detail,form,edit}.html` - AS-IS JSP 1,202줄 대응). admin bootJar EXIT=0.
- 신규: `domain/{CmntySrBbsFile,CmntySrBbsCmnt,CmntySrBbsCmntFile}`,
  `repository/{CmntySrBbsFileRepository,CmntySrBbsCmntRepository,CmntySrBbsCmntFileRepository,CmntySrBbsAdminRepository}`,
  `service/{CmntySrBbsAdminService,CmntyFileStorageService,CmntyText}`, `web/CmntySrBbsAdminController`.
  라우트는 기존 op_menu 값(`/community/sr-bbs`)에 맞췄다 - **DB 변경 없음**. 하위경로는 AS-IS 그대로
  (`/list`·`/form`·`/detail/{id}`·`/edit/{id}`·`/deleteSrBbs/{id}`·`/delete-item-image`·`/cmnt/*`·`/file-download/{id}`).
- `CmntyBoardController`에서 **sr-bbs도 떼어냈다**(남은 것은 off-sr-bbs·faq-bbs 2종).
- **★★AS-IS 전역 모달 1개가 TO-BE에 아예 없었다 - 이식**: `include/modal/file-upload-agree.jsp`
  = **"자료 업로드 전 보안 점검"** 모달(개인정보 포함 여부·암호화·공유범위 3문항, 전부 '예'여야
  파일창이 열린다). AS-IS는 `layouts/common/inc_common.jsp`에서 **모든 운영관리 화면에 전역 포함**한다.
  → `fragments/file-upload-agree.html`로 옮기고 **첨부가 있는 화면에서만** 포함했다(숨은 모달이라 동작 동일,
  admin-nav 전역조각을 건드리지 않아 210페이지에 영향 없음). `fileUploadPopupOpen(fileInputId)`가 진입점.
  **남은 첨부 화면(오프라인SR·담당자FAQ·자료실)도 이 조각을 쓸 것.** bootstrap.css/js는 opmanager-head가 이미 로드.
- **`CmntyFileStorageService` 신규 - AS-IS 파일규칙 전용**: 50MB / 확장자 18종 화이트리스트 /
  저장명 `yyyyMMddHHmmssSSS_원본파일명` / `ATCH_FILE_SEQ = max+1`. 기존 `AdminFileStorageService`
  (20MB·UUID명·화이트리스트 없음)를 쓰지 않은 이유는 **화면 안내문구에 "1개(50MB 이하) / jp(e)g, png, ppt(x)…"가
  그대로 적혀 있어서**다. 크기표기 공식(B/KB/MB)도 AS-IS 경계조건 그대로 `formatSize`에 옮겼다.
  ★보안상 조인 것 1건: AS-IS는 원본 파일명을 저장명에 그대로 이어 붙여 `../` 경로이탈이 가능 → 경로부분을 뗀다.
- **★AS-IS 결함 5건 고쳤다**(상세는 `CmntySrBbsAdminService` 클래스 주석):
  ① **수정에 작성자 확인이 없다** - 같은 클래스의 `deleteSrBbs`와 소통방 `updateBbs`에는 있다.
     화면은 작성자에게만 수정 버튼을 주지만 **URL 직접호출로 남의 글을 고칠 수 있었다**(인가 취약점).
     댓글 첨부 등록·삭제, 본문 첨부 삭제도 같은 이유로 권한을 맞췄다(댓글 첨부 삭제는 화면 규칙대로 **등록자 또는 1·2**).
  ② `getSrBbsDetail`에 **use_yn 조건이 없어 삭제된 글이 URL로 열린다**(소통방은 있다 - 두 화면이 서로 달랐다).
  ③ **첨부 2개 이상이면 상세화면이 500**: 파일 크기를 스칼라 서브쿼리 하나로 뽑아 모든 첨부에 같은 값을 찍는데,
     행이 2건 이상이면 SQL 자체가 실패한다 → 파일마다 자기 크기를 쓴다(현재 첨부 1개 제한이라 보이는 결과는 같다).
  ④ **댓글 첨부 크기가 항상 빈 값**(매퍼가 `fileSize`를 안 채워 괄호만 나왔다) → 같은 공식으로 채웠다.
  ⑤ 댓글 수정 시 `LAST_MDFCN_ID`가 null로 들어간다(입력 DTO를 그대로 넘겨서) → 수정자 ID를 넣는다.
- AS-IS verbatim: 상단 안내문구 "개선하고 싶은 기능에 대한 건의 및 시스템 오류 사항에 대해 작성 부탁드립니다." /
  제목 옆 **첨부 아이콘(cli-icon_file-list.png)·비밀글 아이콘(icon_lock.png)** / 등록 버튼은 M00088이 아니라 "등록" 하드코딩 /
  **댓글 입력칸이 목록 위**(소통방은 아래)이고 **댓글 정렬이 오름차순**(소통방은 내림차순) /
  등록·수정은 ajax가 아니라 **multipart 폼 전송** 후 목록 리다이렉트("등록되었습니다."/"수정에 성공하였습니다.") /
  댓글 첨부만 **숨은 multipart 폼** → 상세 리다이렉트("파일이 등록되었습니다.") /
  파일이 디스크에 없으면 **alert+history.back() HTML 조각**을 돌려준다 /
  삭제 확인문구 "등록된 게시글을 삭제합니다. 글 삭제 시 댓글도 함께 삭제됩니다." /
  비밀글 차단 문구가 상세 "비밀글입니다."·수정 "비밀글은 본인만 수정 가능 합니다."(소통방과 다름) /
  **댓글 첨부 클라이언트 검사는 30MB인데 서버는 50MB**(AS-IS 불일치 - 그대로 둠) /
  제목 maxlength 등록 255·수정 1000(AS-IS 불일치 - 그대로 둠) / 등록화면 오타 "이미 등록된 파일이이 있습니다."(수정화면은 정상) /
  **수정화면은 조회수를 올리지 않는다**(소통방은 올린다 - 상세와 같은 메서드를 쓰기 때문).
- AS-IS **죽은 코드는 옮기지 않았다**: srBbs/form.jsp의 `downloadItemImage`/`deleteItemImage`/`deleteDataboard` 세 함수는
  **자료실 URL을 가리키는 복사 흔적**이고 등록화면엔 호출할 요소가 없다. validator의 `$("#databoardParam")`도 없는 요소다.
- **검증**(`seed-admin-cmnty-sr-bbs.sql`, 적용 완료 - 글 5·본문첨부 2·댓글 3·댓글첨부 2): 공지 상단고정 /
  소프트삭제 글·댓글·첨부 전부 제외 / 비밀글+첨부 행이 아이콘 2개 조건 충족 / 댓글 오름차순 /
  파일크기 3구간(512B·36KB·2MB) 모두 확인. 첨부 행만 넣고 디스크 파일은 안 만들어
  **"파일이 존재하지 않습니다…" 경로까지** 확인 가능하게 뒀다.
**★③오프라인 담당자 SR(11406) 완료 - 2026-10-04**: AS-IS {@code offSrBbs/*} **15개 엔드포인트 전부** +
템플릿 4종(`community/offSrBbs/{list,detail,form,edit}.html` - AS-IS JSP 1,143줄 대응). admin bootJar EXIT=0.
- 신규: `domain/{CmntyOffSrBbsFile,CmntyOffSrBbsCmnt,CmntyOffSrBbsCmntFile}`, 리포지토리 4개
  (3 JPA + `CmntyOffSrBbsAdminRepository`), `service/CmntyOffSrBbsAdminService`,
  `web/CmntyOffSrBbsAdminController`(`/community/off-sr-bbs` - 기존 op_menu 값, **DB 변경 없음**),
  `web/support/CmntyOffSrBbsSearchParam`. `CmntyFileStorageService`·`CmntyText`·`fragments/file-upload-agree`는 ②의 것을 재사용.
- **11404와 다른 점은 "소속"뿐이다**: 작성자 소속이 지자체가 아니라 **`[은행명] 지점명`**이고,
  은행명은 공통코드 **OFF_BANK_LIST** label(AS-IS도 서브쿼리로 바로 가져온다), 지점명은 `op_manager.psitn_nm`.
  검색도 지자체(시도/시군구) 대신 **은행 select(shBank)**, 검색구분이 제목 / **소속지점(PSITN, LIKE)**,
  소속 구분이 **오프라인 담당자(ROLE_ADMIN_7 → 7·8을 함께)**다. 소속 컬럼 폭 250px(11404는 150px).
  삭제 경로만 AS-IS가 `deleteOffSrBbs/{bbsId}`로 다르다.
- **★댓글에는 지자체 담당자 분기가 있다**: 메뉴 권한 실측이 **1·2·7·8**(지자체 5·6은 들어올 수 없음)인데
  AS-IS 상세화면 댓글에는 **5·6(지자체) 분기와 7·8(지점) 분기가 따로** 있고 otherwise도 지자체명이다
  (과거 데이터 대비). 그대로 옮겼고 시드로 그 경로까지 확인했다.
- AS-IS 결함 5건은 ②와 **같은 내용·같은 방식으로** 고쳤다(수정 권한 없음 / 삭제된 글 URL 열림 /
  첨부 2개 이상 상세 500 / 댓글 첨부 크기 빈 값 / 댓글 수정자 ID null).
- **제목(h3)은 `activeMenu.menuName`을 쓴다**: `MENU_11406`이 op_common_message에 **없어서**(실측)
  `msg.get()`이 코드 문자열("MENU_11406")을 그대로 뱉는다. AS-IS JSP의 h3는 원래 비어 있고 레이아웃이 채우므로,
  **op_menu.menu_name(AS-IS 원문 '오프라인 담당자 SR 게시판')**을 쓰는 것이 맞다. **11405도 같은 상황이니 같게 처리할 것.**
- **검증**(`seed-admin-cmnty-off-sr-bbs.sql`, 적용 완료 - 글 6·본문첨부 1·댓글 4·댓글첨부 1):
  소속 표기 3종(시스템/행안부/[농협은행] 서울중앙지점·[농축협] 수원농축협·[제주은행] 제주본점) /
  공지 상단고정 / 비밀글 / 소프트삭제 제외 / 댓글 3건 오름차순(시스템·**지자체(5)**·오프라인부담당자(8)) /
  은행검색 012=1건 · 소속지점 LIKE '지점'=1건 · 소속 7·8=3건 - 전부 SQL 대조 일치.
  작성자는 4601 시드(`seed-admin-off-person-in-charge.sql`)의 담당자 9701·9705·9707·9704를 재사용했다.
- `CmntyBoardController`는 이제 **faq-bbs 하나만** 남았다(`listOf`도 제거). ④가 끝나면 이 클래스와
  `community/board/*` 템플릿·`G_CMNTY_COMMENT` 표가 **전부 죽은 코드**가 된다.

**★④담당자용 FAQ(11405) 완료 - 2026-10-04**: AS-IS {@code faqBbs/*} **15개 엔드포인트 전부** + 템플릿 4종
(`community/faqBbs/{list,detail,form,edit}.html`). admin bootJar EXIT=0. 라우트 `/community/faq-bbs`(기존 op_menu, DB변경 없음).
- **★AS-IS 질문유형은 Java enum이었다**: `enumMapper.get("FaqType")` → `saleson.common.enumeration.FaqType` 11종
  (F_LOGIN 회원가입/로그인 · F_CNTR_SYSTEM 기부하기 · F_CNTR_POINT 기부포인트 · F_OFF_CNTR 오프라인기부 ·
  F_CNTR_DESIGNATED 특정사업기부 · F_API_PLATFORM 세액공제 · F_PRESENT_PURC 답례품 · F_ORDER 주문/배송 ·
  F_OPEN 민간플랫폼 · F_SYSTEM 시스템 · F_ETC 기타). **저장값은 enum 이름**(getCode()=name()).
  TO-BE 관례대로 공통코드 **`CMNTY_FAQ_TYPE`**으로 적재(`migration-admin-cmnty-faq-type-codes.sql`, 적용 완료).
  ★예전 TO-BE 골격은 **쇼핑몰 FAQ 코드(`FAQ_TYPE` 1~6 회원정보/주문결제/…)**를 쓰고 있었다 - 코드셋이 전혀 달라 잘못된 연결이었다.
- **★AS-IS가 노출하지 않는 기능은 그대로 비활성 유지**: FAQ 상세화면에는 SR·오프라인SR과 달리
  **댓글 '파일등록' 버튼과 댓글 첨부 목록이 없다**. 숨은 업로드 폼·스크립트·서버 엔드포인트는 AS-IS에 그대로 있어
  같이 옮기고 버튼·목록만 렌더링하지 않았다 → **이 게시판은 본문 첨부만 실제로 쓰인다**.
- AS-IS verbatim(11404와 다른 점): 상단 빨간 안내문구가 **없다** / 소속 컬럼 대신 **질문유형** 컬럼 /
  질문유형이 select가 아니라 **탭**(한 줄 6개, '전체'가 첫 탭, 누르면 바로 검색) /
  등록·수정 화면의 **상단공지 체크박스에 권한조건이 없다**(SR은 1~4만) /
  첨부 파일칸이 **보안 점검 모달 없이 그냥 file input** / 수정 확인문구가 "게시글을 수정하시겠습니까?"(SR은 복사 흔적으로 '등록') /
  삭제 경로가 `deleteFaqBbs/{bbsId}`.
- **Thymeleaf 제약 1건 처리**: AS-IS는 `forEach` 안에서 `</ul><ul>`로 탭 줄을 바꾸는데 Thymeleaf는 태그를 그렇게 쪼갤 수 없다
  → `CmntyFaqBbsAdminService.faqTypeTabRows()`가 **6칸씩 묶어** 내려준다(AS-IS 줄바꿈·빈칸 패딩 규칙 그대로, 보이는 결과 동일).
- `faqBbsCmnt.js` **AS-IS 오타 1건 고침**: 삭제 URL이 `deletefaqBbs`(소문자 f)로 컨트롤러와 안 맞았다.
  상세화면 버튼에 `.delete` 클래스가 없어 바인딩되지 않는 죽은 코드지만 잘못된 경로를 남기지 않았다.
- **★임시 골격 제거 완료**: `CmntyBoardController`·발명 엔티티 `CmntyComment`+리포지토리·`community/board/*` 템플릿 **삭제**.
  4개 게시판이 모두 AS-IS 표로 옮겨가 완전히 죽은 코드가 됐다. 빈 표 `G_CMNTY_COMMENT`는 남겨 뒀다(DROP은 사용자 판단).
  `CmntyBoard` 매핑 슈퍼클래스는 4개 엔티티가 실제로 공유하므로 유지.
- **검증**(`seed-admin-cmnty-faq-bbs.sql`, 적용 완료 - 글 6·첨부 1·댓글 3): 질문유형 라벨 매핑 / 공지 상단고정 /
  비밀글 / 소프트삭제 제외 / 질문유형 탭 필터(F_OFF_CNTR=1건). **골격이 남긴 `faq_type='1'` 행은 질문유형이 빈 값으로 나온다** -
  AS-IS도 코드를 못 찾으면 빈 값이라 동작 일치(확인용으로 남겨 뒀다).

**★⑤자료실(11402) 완료 - 2026-10-04**: AS-IS `LocGovDataBoardManagerController` 이식 + 템플릿 3종
(`community/databoard/{list,detail,form}.html`). admin bootJar EXIT=0. 라우트 `/community/databoard`(기존 op_menu).
- 신규: `domain/CmntyFile`(AS-IS `G_CMNTY_FILE` - 다른 게시판과 달리 **RPSTR_ID**를 갖는다),
  `repository/{CmntyFileRepository,CmntyRpstrAdminRepository}`, `service/CmntyRpstrAdminService`,
  `web/support/CmntyRpstrSearchParam`. 기존 `CmntyRpstrController`(70줄 골격)를 전면 교체.
- **다른 게시판과 다른 점**: **댓글도 비밀글도 없다**(표에 컬럼 자체가 없다) / 등록·수정이 **템플릿 하나를 공유**한다
  (AS-IS edit도 form.jsp를 돌려준다 - 그래서 edit.jsp가 없다) / 첨부 파라미터가 **`detailImageFiles[]`**(대괄호!) /
  등록화면 role 판정에 **ROLE_ADMIN_10이 포함**(SR·FAQ는 1~6) / 컬럼 5개(No./소속/제목/조회수/**등록일 yyyy-MM-dd**) /
  검색구분 '전체'의 값이 **빈 문자열**(다른 게시판은 'ALL') / 수정 완료문구가 M00289("수정되었습니다.") /
  삭제 ajax 응답에 **메시지가 없다** / 파일 없을 때 다운로드가 **alert 스크립트가 아니라 오류 응답**(AS-IS ApiResponseEntity.error).
- **게시글 삭제 시 첨부 행은 건드리지 않는다**(AS-IS deleteRpstr이 게시글만 UPDATE) - 그대로 따랐다.
- **★AS-IS 죽은 엔드포인트 2개는 이식하지 않았다**(기록만):
  ① `POST /deleteDataboard`(체크박스 일괄삭제) - 목록 표에 **체크박스 컬럼이 아예 없어** 호출 불가인 죽은 UI이고,
     게다가 이 엔드포인트는 `LocgovDataBoardService`를 거쳐 **전혀 다른 표 `op_community_databoard`**를 지운다
     (화면이 보여주는 `g_cmnty_rpstr`와 무관 - 눌렀더라도 아무 행도 안 지워지고 오류만 났을 것이다).
  ② `POST search-date` - 날짜만 계산해 모델에 담고 JSON에는 아무 값도 싣지 않으며, 호출하는 곳도 없다.
  같은 이유로 list.jsp의 `deleteNotice`·`deleteCheckDataboard`·`checkedEventSet`, detail.jsp의 `formatBytes`(인자 없이 호출돼 NaN),
  `deleteItemImage`(**1:1문의 URL `/opmanager/qna/delete-item-image`를 가리키는 복사 흔적**)도 옮기지 않았다.
- **AS-IS 결함 고침**: 목록의 첨부 유무를 `(select orgnl_atch_file_nm ...)` 스칼라 서브쿼리로 뽑아 **첨부 2건 이상이면 SQL 실패** →
  개수를 세어 아이콘 판정. 상세 첨부 크기도 같은 유형(파일별 자기 크기로). 수정·삭제에 **작성자 확인 추가**(다른 게시판과 동일한 인가 누락).
- **검증**(`seed-admin-cmnty-rpstr.sql`, 적용 완료 - 글 5·첨부 2): 소속 3종·공지 상단고정·첨부 아이콘·소프트삭제 제외·등록일 포맷 확인.
- **★부수 수정(5개 게시판 전부)**: 목록 정렬이 `order by notice_yn desc`인데 PostgreSQL은 **DESC에서 NULL을 먼저** 놓는다.
  옛 골격이 남긴 `notice_yn IS NULL` 행이 **공지보다 위로 올라오는** 것을 실측 → 5개 리포지토리 모두
  `coalesce(a.notice_yn,'N') desc`로 바꿨다(정상 데이터에는 영향 없음, 컬럼 기본값은 'N').

**★★⑥지자체FAQ(11403) - 메뉴 등록만으로 종결. 결정 완료 - 2026-10-04**
사용자 결정: **"② AS-IS가 꺼 둔 화면이니 메뉴 등록만으로 종결"**(내가 권한 방안). **다시 열지 말 것.**
결정 근거와 하지 않은 일을 `LocgFaqAdminController` 클래스 주석에도 박아 뒀다(다음 세션이 "미이식 화면"으로 오인하지 않게).
- AS-IS op_menu 실측: `(11403,11400,'지자체FAQ','/opmanager/community/locv-faq/list',3,'N','2')` →
  **display_flag='N', status_code='2'(중지)**. TO-BE에는 행이 **아예 없었다** →
  `migration-admin-menu-11403-locgov-faq-stopped.sql`로 **중지 상태 그대로** 등록(적용 완료).
  status_code='2'라 `MenuService.visibleMenus()`(='1'만)에 안 걸려 nav·breadcrumb에 영향 없다.
  AS-IS op_menu_right에도 11403 행이 없어(메뉴가 꺼져 권한 미부여) 추가하지 않았다.
- **★화면을 덮지 않은 이유**: AS-IS `LocgFaqManagerController`가 다루는 표 `op_community_locgovfaq`는
  **TO-BE에서 고객센터 공개 FAQ(`/faqs`·`/api/faqs` → `FaqService`)의 실데이터 63행**이고,
  같은 URL(`/community/locv-faq`)에 그걸 관리하는 TO-BE 화면(`LocgFaqAdminController` 101줄)이 **살아서 쓰이고 있다**.
  **AS-IS가 꺼 둔 화면을 살아있는 공개 FAQ 관리 화면 위에 덮으면 운영 중 기능이 깨질 수 있다** → 사용자 결정 사항으로 올림.
- **★덤으로 찾은 live 결함(고객센터 영역, 이번에 손대지 않음)**: `database/ddl/service-admin.sql`(4962행~)이
  `FAQ_TYPE`에 **JOIN/DONATE/POINT/OFFLINE/DESIGNATED/TAX/GIFT/ORDER/PRIVATE/SYSTEM/ETC 11행**(라벨은 AS-IS FaqType과 동일)을
  넣으려 하는데, 그 INSERT가 **`ADMIN_COMMON_CODE`라는 다른 표 이름**을 쓴다 → 실제 `admin.op_common_code`에는
  들어가지 않았고 `FAQ_TYPE`에는 쇼핑몰 코드 1~6만 있다. 그래서 `op_community_locgovfaq`의 63행
  (faq_type=JOIN/DONATE/…)은 **질문유형 라벨을 못 찾아 빈 값으로 보인다**(공개 FAQ·관리화면 양쪽).
  고객센터(11화면) 라운드에서 그 11행을 올바른 표에 넣으면 해결된다. 담당자용 FAQ(11405)는 `CMNTY_FAQ_TYPE`을 쓰므로 무관.
- AS-IS 자산(이식 시 참고): 컨트롤러 343줄(list GET/POST·create GET/POST·**list/{id}가 상세**·edit/{id} GET/POST·
  delete/{id}·deleteDataboard) + JSP 4종 538줄. 질문유형은 `FaqType.values()`(enum 전체)를 쓰는데
  **실데이터 코드셋과 애초에 맞지 않는다**(enum은 F_* 이고 데이터는 JOIN/DONATE/…) - 메뉴가 중지된 배경으로 보인다.

- **커뮤니티 영역 종료**: 게시판 **5개 이식 완료**(11401 소통방 · 11404 SR · 11406 오프라인SR · 11405 담당자FAQ · 11402 자료실)
  **+ 11403 지자체FAQ는 메뉴 등록만으로 종결**(AS-IS 중지 화면, 사용자 결정 ②). 추가 작업 없음.
- 이 영역에서 만든 공용 자산(다음 게시판·첨부 화면에서 재사용): `service/CmntyFileStorageService`(AS-IS 50MB·확장자 18종·
  `yyyyMMddHHmmssSSS_원본명`·크기표기 공식), `service/CmntyText`(decode·nl2br), `service/CmntyException`,
  `fragments/file-upload-agree.html`(자료 업로드 전 보안 점검 모달 - **AS-IS는 전역 포함이니 첨부 있는 신규 화면은 반드시 붙일 것**).
  - ④는 **②·③과 구조가 거의 같다**(본문+댓글 첨부, 표 `g_cmnty_faq_bbs*`). 추가되는 것은
    **FAQ 구분(FAQ_TYPE 공통코드)** 하나이고 AS-IS 매퍼·서비스 메서드 이름만 `*FaqBbs*`로 바뀐다.
    **②의 파일 7개를 그대로 복제해 이름·표·FAQ_TYPE만 바꾸면 된다**(③을 그렇게 했다).
    `faqBbsCmnt.js`도 이미 복사돼 있으니 경로 접두어만 고치면 된다. 제목은 `activeMenu.menuName`.
  - ⑤ 자료실(11402)은 **별도 컨트롤러**(`LocGovDataBoardManagerController` 278줄 + databoard JSP 904줄)이고
    TO-BE에 `CmntyRpstrController`(70줄) + `CmntyRpstr` 엔티티 골격이 있다. 표는 `g_cmnty_rpstr` + `g_cmnty_file`.
    **주의: TO-BE `/admin/data-board`(메뉴 5107, 고객센터 자료실)와는 다른 화면이다** - 혼동 금지.
  - ⑥ 지자체FAQ(11403)는 AS-IS에서 **메뉴 중지(display_flag='N', status_code='2')**다.
    코드는 이식하고 **TO-BE op_menu에 11403 행을 중지 상태로 추가**해야 한다(현재 행 자체가 없다).
    AS-IS 컨트롤러 `LocgFaqManagerController` 343줄 + JSP 538줄, TO-BE `LocgFaqAdminController` 101줄 골격 있음.

**★납부 게이트웨이 부과요청 2건 구현 완료 - 2026-10-04 (사용자 승인: "시드데이터 생성해서 구현하고 기능확인 가능하면 동일하게")**
15102 보류사유 중 **2번(납부 연계)만 분리해 완결**했다. 화면(15102)은 1·3 보류라 못 열지만 연계 경로는 독립 검증된다.
- **AS-IS가 이미 목 분기를 갖고 있다**: `nextBugaRequest`는 `ServiceType.LOCAL`이면 전자납부번호를
  `"99" + yyyyMMddHHmmssSSS`로 만들고 `linkRstCd="000"`, `linkRstMsg="LOCAL FAKE : {번호}"`로 응답한다.
  그 분기를 그대로 옮겼으므로 **목 구현도 AS-IS verbatim**이다(내 발명이 아니다).
- 신규: `service/integration/NextBugaRequest`(차세대 부과요청 전문 - g_next_buga_request 컬럼과 1:1),
  `SeoulBugaRequest`(서울 세외 **~90키** payload를 키 순서·빈 값·0까지 verbatim),
  `LocalTaxClient.sendNextBuga/sendSeoulBuga`(기존 5필드 자리표시자 payload를 AS-IS 실제 전문으로 교체),
  `LevyRelayService`(AS-IS 고정값·파생값 조립), `LevyLinkLogWriteRepository`, `POST /api/admin/levy/{next-buga,seoul-buga}`.
- **AS-IS 파생값 공식 그대로**: `sgbCd`=지자체 행정기관코드 / `dptCd`=처리부서코드에서 **뒤 4자리 뗀 값** /
  `spclFisBizCd`=회계구분 51·61이면 **7092**, 그 외 0000(AS-IS 주석 "2024.03.04 개발원 요청") /
  `fyr`=올해 · `actSeCd`=회계구분 · `rprsTxmCd`=224102 · `operItemCd`=000 · `pyrSeCd`=01 · `pyrSttCd`=10 ·
  `lotnoRoadAddrSeCd`=02 · `mngItemCn1`="고향사랑기부금" / 회계구분이 비면 지자체코드 3~5자리 '000'이면 31, 아니면 41
  (AS-IS `getLocgovFisSp` - 4401 회계구분 분기와 같은 규칙) / 서울 `siguCd`=행정기관코드(AS-IS `getSiguCdSeoul`).
- **채번 시퀀스 2개 신규**(`migration-donation-levy-relay-seq.sql`, 적용 완료):
  `g_cntr_link_mng_key_seq`(연계관리키 = yyyyMMddHHmmss+seq), `gif_seoul_book_no_seq`(서울 대장번호).
  AS-IS는 linkMngKey에 기부번호 시퀀스(g_cntr_cntr_sn)를 쓰는데 TO-BE는 기부번호를 앱에서 만들어 그 시퀀스가 없어 전용으로 만들었다.
- **★연계로그 적재는 "목일 때만" 한다**: AS-IS 애플리케이션에는 `g_next_buga_request`·`gif_seoul`에 **INSERT가 없다**
  (실측: 매퍼에 SELECT만 - 연계서버/상대 시스템이 적재한다). 그래서 `enabled=false`일 때만 적재해
  개발환경에서 admin **1413 서울세외 부과연계 로그**·**1415 지방세외 부과연계 로그** 화면으로 흐름을 확인하게 했다.
  실연계(`enabled=true`)에서는 적재하지 않는다(상대 시스템 표에 끼어들면 안 된다).
- **AS-IS 결함 1건 안전하게 고침**: 전자납부번호를 `linkRstMsg.split(":")[1]`로 꺼내는데 ':'이 없으면
  `ArrayIndexOutOfBoundsException`이 나고 바깥 catch가 삼켜 **요청이 조용히 실패**한다 → ':' 없으면 빈 값으로 두고 결과코드만 돌려준다.
- 설정: `ghlove.integrations.local-tax.link-trgt-cd`(AS-IS 상수 LINK_TRGT_CD) 추가, `enabled: false` 기본 유지.
- **검증**: 목 INSERT를 SQL로 재현해 1413·1415 화면 질의에 잡히는 것까지 확인
  (1415: link_mng_key=202610040853491·dpt_cd=처리부서코드-4·link_rst_cd=000·"LOCAL FAKE : 99…" /
   1413: enapbu_no="99"+ts·error_cd=0). 기존 시드의 정상/오류 행과 함께 보인다. donation bootJar EXIT=0.
- **남은 연계(미착수)**: `localSunapConfirm`(지방세외 수납확인)·`etaxSunapInfo`(서울 수납확인)는 1414·1416 수납연계
  로그와 짝인데 AS-IS에서 **배치**(`NgDonationBatchServiceImpl`)가 호출한다 - 배치 쪽 라운드에서 같이 보는 게 맞다.

### admin-designated-donation-area-port-progress

> 특정사업 기부 관리(17000) 영역 이식 원장 - AS-IS 대조표·갭 실측·구현순서. 기부금관리/통계를 뒤로 미룬 이유 포함


### 특정사업 기부 관리(17000) 이식 원장

2026-10-05 착수. 고객센터 영역(승인순서 4건) 완료 후 사용자 지시
"나중에 처리할 사항으로 남겨두고 다른 메뉴들 구현작업하자"에 따라 고른 영역.

#### 왜 이 영역을 먼저 골랐나 (기부금관리·통계를 미룬 이유)

**기부금관리(14000)·통계(6000)는 보류 중인 설계 결정에 정면으로 걸린다.**
TO-BE `GiveStateService` 주석이 그대로 말한다 - "admin은 donation/point 서비스의 DB를 직접 읽을 수
없어(DB per Service) 이미 있는 통계 ReadModel(`STAT_DONATION_LEDGER`/`STAT_POINT_LEDGER`)을 재사용한다".
그런데 [[da-design-pending]]에 따라 **조회모델 작업 자체가 DA 설계안까지 보류**다.
그 위에 화면을 더 쌓는 것도, 걷어내고 교차조회/API로 바꾸는 것도 보류 결정을 건드린다.
→ **사용자 결정 필요**: 기부금 현황 화면들을 (a) ReadModel 유지 (b) donation API 호출
(c) 교차스키마 직접조회 중 무엇으로 갈 것인가. 결정 전에는 착수하지 않는다.

**특정사업(17000)은 그 경계가 없다** - TO-BE가 이미 `DesignatedProjectClient`로
**donation 서비스 API를 호출**하는 구조이고(깔끔한 MSA 패턴), 배너는 admin 자체 표
(`op_representative_banner`)다. 그래서 바로 이어서 할 수 있다.

#### 메뉴 구조 (실측)

| 메뉴 | 이름 | TO-BE menu_url | 상태 |
|---|---|---|---|
| 17100 | 특정사업 기부 정보 | (그룹) | |
| 17101 | 특정사업 기부 목록 | `/designated-projects` | ✅ 있음 |
| 17102 | 특정사업 기부 등록 | `/designated-projects` | ⚠ **17101과 중복** (등록은 `/new`) |
| 17200 | 특정사업 기부 통계 | (그룹) | |
| 17201 | 지자체별통계 | `/designated-projects/analysis/locgov` | ✅ |
| 17202 | 월별통계 | `/designated-projects/analysis/month` | ✅ |
| 17300 | 특정사업 기부 배너관리 | (그룹) | |
| 17301 | 특정사업 기부 배너관리 | `/designated-projects/banners` | ✅ |

#### AS-IS vs TO-BE 실측 (2026-10-05)

AS-IS: `DesignatedDonationManagerController` **1,262줄** + JSP **17개 7,275줄**
(`opmanager/i18n/designated-donation/`). TO-BE: `DesignatedProjectAdminController` 359줄 +
`RepresentativeBannerController` + 템플릿 8개.

| 화면 | AS-IS JSP | TO-BE 템플릿 | 갭 |
|---|---|---|---|
| 목록 17101 | list.jsp **518** | list.html 75 | **14%** ← 최대 갭 |
| 등록/수정 17102 | form.jsp **928** | form.html 243 | **26%** |
| 지자체별통계 17201 | analysis/locgov.jsp 409 | analysis-locgov.html 286 | 70% |
| 월별통계 17202 | analysis/month.jsp 720 | analysis-month.html 612 | 85% |
| 배너 17301 | banner/list 145 + form 298 | banner-list 56 + banner-form 83 | **31%** |
| 참여기관(part) | part/list 350 + form 188 | — | **0%** |
| 권한요청(request) | request/list 322 + form 347 + details 414 + history 91 | — | **0%** |
| 미리보기(preview) | preview.jsp **632** | — | **0%** |
| 공지(notice) | notice/list 268 + form 323 | form.html 안에 일부 | 부분 |
| 기타 | analysis/month_temp2025.jsp 715 · popup-confirm-log 107 | — | temp2025는 1회성 가능성 |

#### 확인된 사실 (조사 결과 - 다시 조사하지 말 것)

- **배너는 같은 표를 `PROCESS_TYPE`으로 갈라 쓴다**: AS-IS가 `listParam.setProcessType("DESIGNATED_DONATION")`.
  TO-BE `RepresentativeBannerService`도 **이미 그 상수로 필터**한다(`PROCESS_TYPE` 상수 + 전용 쿼리) -
  대표배너(16251 `/admin/main-banners`)와 섞이지 않는다. op_qna/QNA_TYPE과 같은 패턴이지만 이쪽은 정상.
- AS-IS 배너 엔드포인트는 `/banner/list` GET · `/banner/change-ordering` POST ·
  `/banner/form/{id}` GET+POST · `/banner/delete` POST.
  **TO-BE에는 순서변경·삭제가 없고** 대신 `/{id}/toggle`이 있다(사용여부 전환).
- AS-IS 배너 목록 진입에 가드가 있다: `checkDsgncntrAuthPartInfo() == 0` →
  "부서정보가 없어서 처리가 불가능합니다." 로 리다이렉트. TO-BE에 없다.
- `DesignatedProjectClient`에 이미 있는 API: 목록/단건/등록/수정/공지CRUD/부서/승인로그/
  연도목록/지자체별·월별 통계/월별 캠페인·모금액·금액/지자체통계 요약·목록.
  **없는 것**: 참여기관(part), 권한요청(request), 미리보기(preview), 배너 순서변경·삭제.

#### 구현 순서 (내가 정함)

1. **17102 등록 URL 분리** - `/designated-projects/new`로 (현재 17101과 중복)
2. **목록 17101 보강**(518→75, 최대 갭) - 검색조건·컬럼·권한분기 전수 대조
3. **등록/수정 17102 보강**(928→243) - 이미지/기간/부서/승인 흐름
4. **배너 17301 보강** - 순서변경·삭제·부서정보 가드
5. 참여기관(part) 신규 - 538줄
6. 권한요청(request) 신규 - 1,174줄 (승인 흐름이라 권한 결정 필요할 수 있음)
7. 미리보기(preview) 신규 - 632줄
8. 통계 2화면 잔여 보강(70%/85%)

#### 진행 기록

##### 1 · 4 완료 (2026-10-05) - 등록 URL 분리 + 배너 보강, admin bootJar EXIT=0

**① 17102 등록 URL 분리**: `/designated-projects` → **`/designated-projects/new`**
(17101 목록과 같은 URL이라 메뉴 둘이 같은 화면을 열었다). DB 직접 UPDATE로 적용.

**④ 배너 17301 보강** - AS-IS에 있고 TO-BE에 없던 엔드포인트 2개 + 가드:
- `POST /designated-projects/banners/change-ordering` - 노출순서 **일괄 변경**(ajax, JSON).
  AS-IS는 `id[]`와 `ordering[]`을 **같은 인덱스로 짝지어** 돌며 UPDATE한다(길이가 다르면
  AS-IS는 예외로 터지므로 짝이 맞는 만큼만 처리). 목록에 순서 입력칸 + [순서변경] 버튼 추가.
- `POST /designated-projects/banners/delete` - **★이름은 "삭제"지만 배너 행을 지우지 않는다**.
  AS-IS 매퍼가 `deleteFlag`에 따라 **이미지 파일명만 비우는 UPDATE**다
  (`pc` → `FILE_NAME_PC=''`, `mobile` → `FILE_NAME_MOBILE=''`). 배너를 내리는 수단은 사용여부 전환.
  등록/수정 화면의 이미지 옆에 [x] 링크로 붙였다. 디스크 파일은 AS-IS도 남긴다.
- **부서정보 가드**(AS-IS `checkDsgncntrAuthPartInfo()`) - 문구 "부서정보가 없어서 처리가
  불가능합니다." 그대로. AS-IS 분기: **ROLE_ADMIN_10**(특정사업 담당자)은 자기 부서 id가 없으면 차단 ·
  **ROLE_ADMIN_1~6**(최고관리자·지자체담당자)은 통과(-1) · 그 외 차단.
  **근사 1건**: ROLE_ADMIN_10의 "자기 부서 id" 조회 API가 donation에 없어(있는 건 지자체별 부서
  **목록**) 그 지자체에 부서가 하나도 없으면 막는다 - API가 생기면 그 값으로 교체할 것.
- 사용여부 전환을 행마다 `<form>`을 두던 방식에서 폼 하나 재사용 + JS로 바꿨다(표 안의 중첩 폼 제거).

**검증**(트랜잭션 후 롤백): 목록 범위가 `DESIGNATED_DONATION`만(대표배너 `MAIN`과 안 섞임, 3건 중 2건) ·
순서변경 1→5 / 2→3 반영 · 이미지삭제 후 **행은 남고 pc만 비워지고 모바일·사용여부는 그대로**.

##### 2 완료 (2026-10-05) - 목록 17101 보강, admin·donation bootJar EXIT=0

**donation 서비스 API 확장이 선행이었다**(목록이 요구하는 검색·집계가 없었다):
- `GET /api/designated-projects/admin/search` - 검색 8개 파라미터 + 행별 집계 + 요약집계
- `POST /api/designated-projects/admin/bulk-update` - 모금상태/공개여부 일괄 변경
- `DesignatedAdminService.search(...)`에 AS-IS `selectDesignatedDonationList` 규칙 전부 이식
- `Donation` 엔티티에 `DELETE_AT`·`STTEMNT_PAY_DE` 매핑 추가(컬럼은 있었으나 미매핑이라
  AS-IS 조건을 걸 수 없었다)

**★AS-IS 규칙 중 중요한 것들**
- **모금상태는 저장값을 그대로 쓰지 않고 다시 계산한다**: 진행이어도 ① 모금액 > 목표금액
  ② 종료일 경과 중 하나면 **종료**로 보여준다(저장값은 안 바꾼다). **상태 검색도 계산된 값에 적용**된다.
- 달성율 = `floor(모금액 * 10000 / 목표금액) / 100`(소수 2자리), 목표금액 0이면 0
- 기간 검색: 시작·종료 **둘 다 있으면 사업 모금기간과 겹치는** 사업(4가지 OR), 하나만 있으면
  그 날짜가 사업기간 안에 드는 사업
- 지자체 검색: 시군구가 있으면 그것, 없으면 광역 하위 전체
- 부서명 기본값 "부서 미지정" · 지자체명은 "광역명 시군구명" · 정렬 사업ID 내림차순
- 집계는 **기부일이 오늘까지**인 완료 기부만
- IN_DATE(1 기간내 / 2 시작전 / 3 종료후)도 같이 내려준다

**★★집계 결함 1건 수정 - 숫자가 2배 가까이 틀려 있었다**
기존 `designatedDonations()`가 `dsgn_dntn_biz_id **is not null**`만 걸어
**`dsgn_dntn_biz_id = 0`인 일반 기부가 특정사업 집계에 섞여 있었다**(실데이터에 0인 행 18건).
AS-IS는 `> 0`이고 `DELETE_AT='N'`도 함께 건다 → `findDesignatedByStatus`로 교체.
**실측: 23건/29,432,000원 → 5건/16,662,000원**. 지자체별(17201)·월별(17202) 통계 숫자도 같이 바로잡혔다.

**AS-IS 결함 1건 보정**: 사업명 입력칸은 `query`로 전송하는데 목록 SQL은 `searchKeyword`를 본다
(화면이 예전 필드명에서 바뀌었는데 SQL이 따라가지 않았다) → **AS-IS에서는 사업명 검색이 전혀
걸리지 않는다**. TO-BE는 걸리게 했다.

**경계 번역 2건**(이 프로젝트 관례대로 TO-BE 어휘 유지):
- 모금상태: AS-IS `'1'/'2'/'9'` ↔ TO-BE `PENDING/OPEN/CLOSED`. 화면 라디오·일괄처리 버튼은
  AS-IS 값을 그대로 쓰고 컨트롤러가 옮긴다.
- 사업구분: AS-IS 코드유형 `BUSINESS_TYPE` ↔ TO-BE **`DSGN_BSNS_TYPE`**(100~400, 4건).
  AS-IS의 `BUSINESS_SUB_TYPE`·`PRJ_STATUS` 코드표는 TO-BE에 없다(상태 라벨은 화면이 붙인다).

**화면**: 검색 6종(지자체는 **지자체담당자에게 숨김**·자기 지자체 고정) · 요약표 4칸 ·
**컬럼 16개** · 출력수 **10/50/100/200/500** · 하단 일괄처리 4종(**진행(승인처리)은 ROLE_ADMIN_1~5만**) ·
광역→시군구 캐스케이딩(`wdrChange`) · 날짜버튼 5종.
공지사항·승인이력 버튼은 기존 상세화면의 해당 영역으로 보낸다(AS-IS는 별도 화면/팝업).
**엑셀·미리보기는 아직 없어 안내만 띄운다**(없는 화면으로 보내 404를 만들지 않는다).

**검증**: 집계 전환 실측(위) · 사업별 집계·달성율(1000: 1건 10,000원 0.03% / 1106: 4건
16,652,000원 40.00%) · 상태 재계산(두 건 모두 목표 미달·기간 내라 OPEN 유지, OPEN인데 종료일
지난 사업은 현재 0건) · admin·donation `bootJar` EXIT=0.

##### 3 완료 (2026-10-05) - 등록/수정 폼 보강, admin·donation bootJar EXIT=0

**추가한 것**
- **읽기전용 3칸(수정 모드에서만)**: 남은 일수 / 모금 된 금액 / 달성률.
  AS-IS도 `mode == 'edit'`일 때만 보여준다. donation에 `GET /api/designated-projects/admin/{id}/stat`
  + `DesignatedAdminService.statOf(id)` 신규(집계 규칙은 목록과 동일).
  **AS-IS `leftDayStr`는 종료일이 지나면 0**으로 본다 - 그대로.
- **상태·공개여부를 select → 라디오**로(AS-IS 형태). 문구도 AS-IS 그대로:
  "진행중(승인)" / "종료" / "대기(승인 전)" · "공개" / "비공개".
- **사업부서 지자체별 재로드**: 지자체를 바꾸면 그 지자체 부서로 다시 채운다
  (AS-IS `getDsgncntrPartList`). admin에 ajax `GET /designated-projects/departments/by-locgov` 신규.
  첫 항목은 AS-IS대로 **"-부서 미지정-"(값 0)**. 라벨도 AS-IS대로 "사업부서"(기존 "담당부서").
  지자체담당자는 처음부터 자기 지자체 부서만 받는다.
- **승인 버튼**(AS-IS `confirmProcess`) - 수정 모드 + ROLE_ADMIN_1~5에게만. 목록의 일괄 승인처리와
  같은 API를 쓴다. 미등록 상태면 "등록 후 승인처리가 가능합니다."(AS-IS 문구).
- **상세 설명·기타 사항을 표 밖 별도 섹션(h3)**으로 분리(AS-IS 구조). 상세 설명 textarea에
  `editor-content` 클래스 부여, 이미지 라벨에 AS-IS 안내 "(600px * 600px)" 추가.
- 목표금액 라벨을 AS-IS대로 "목표금액(원)"으로.

**남긴 갭 2건(데이터·결정 필요)**
1. **사업부문(세부구분)**: AS-IS는 `BUSINESS_SUB_TYPE` 코드의 **필수 캐스케이딩 select**
   (`bsnsTypeChange` → `/bsnsSubType`)다. TO-BE에는 그 코드표가 **없고**(`up_id`를 가진 코드가 0건)
   114개 사업 전부 `dsgn_dntn_biz_se_dtl_cd`가 **빈 값**이다. 그래서 기존 **텍스트 입력 그대로 뒀다** -
   빈 select를 필수로 걸면 저장 자체가 막힌다. 코드 시딩은 데이터 결정 사항(AS-IS 덤프는 DDL만).
2. **사업부서 데이터가 0행**(`g_dsgn_dntn_biz_dept_mng`) - 화면은 동작하지만 "-부서 미지정-"만 보인다.
   배너 화면의 부서정보 가드도 같은 이유로 ROLE_ADMIN_10에게는 실질적으로 막힌다.

**기간 입력 형태**: AS-IS는 8자리 datepicker, TO-BE는 `type="date"`(+컨트롤러 `toYmd` 변환)로
이미 동작하므로 바꾸지 않았다 - 저장 포맷은 같다(yyyyMMdd).

**검증**: 읽기전용 3칸 실측 - 1106(목표 41,630,000 / 모금 16,652,000 / 4건 / **40.00%** / 남은 **87일**) ·
1000(0.03% / 87일) · **204(종료일 20250831 지남 → 남은일수 0**, AS-IS 규칙 확인).
admin·donation `bootJar` EXIT=0.

##### 5 완료 (2026-10-05) - 사업부서 관리 보강, admin·donation bootJar EXIT=0

**★원장 수정**: 위 갭표에 "참여기관(part) 0%"로 적었던 것은 **틀렸다**.
AS-IS `part/list.jsp`의 화면 제목이 **"사업부서 관리"**다 - `DesignatedPart`(=`G_DSGN_DNTN_BIZ_DEPT_MNG`)는
참여기관이 아니라 **사업부서**이고, TO-BE에 이미 `departments.html`(94줄)이 있었다.
즉 신규 구현이 아니라 **보강 건**이었다(AS-IS 538줄 vs TO-BE 94줄).

**donation 신규**
- `GET /api/designated-projects/admin/departments/search` - AS-IS `selectDsgncntrPartMngList`
  조건 4종(지자체·부서명·사용유무·등록일 범위), 정렬 부서ID 내림차순
- `GET /api/designated-projects/admin/departments/{deptId}` - 단건(수정화면용)
- `POST /api/designated-projects/admin/departments/{deptId}` - **수정**(AS-IS `updateDsgncntrPartMng`)
- `DepartmentDto`에 `createdDate` 추가(AS-IS 목록의 "등록일시" 칸, `yyyy-MM-dd HH:mm`)

**admin 신규/수정**
- `web/support/DepartmentSearchParam` 신규
- 컨트롤러: `GET/POST /designated-projects/departments(/search)` · `GET /departments/form/{deptId}` ·
  `POST /departments/save`(등록·수정 한 엔드포인트, AS-IS `/part/save`와 같은 모양)
- 템플릿: `designated/departments.html` **전면 재작성**, `designated/department-form.html` **신규**
- `DesignatedProjectClient`: `searchDepartments`·`department`·`updateDepartment` 추가

**AS-IS 그대로**
- 제목 "사업부서 관리" · 검색 4종(**지자체 칸은 ROLE_ADMIN_1~4에게만**) · 날짜버튼 5종
- 컬럼 5개: ID / [지자체 명] / 부서명(수정 링크) / 사용유무 / 등록일시.
  지자체 칸은 코드가 아니라 **"광역명 시군구명"**
- 하단 버튼은 **[신규등록] 하나**(`form/0`)
- 폼: 지자체는 **최고관리자만** 변경 가능(그 외는 자기 지자체 고정) · 저장 문구 등록/수정/실패 그대로
- **부서정보 가드**(`checkDsgncntrAuthPartInfo`)를 목록·폼·저장 전부에 적용(배너와 같은 근사)
- **AS-IS 비활성 유지**: 체크박스 컬럼과 일괄 사용유무 변경은 JSP에서 전부 주석처리 상태
  (`updateListDataLabel` 함수만 남아 있고 버튼이 없다) → 켜지 않았다.
  그래서 **사용유무는 수정화면에서 바꾼다**(TO-BE에 있던 목록 toggle 버튼은 AS-IS에 없어 뺐다).

**시드 추가**: `database/ddl/seed-donation-designated-dept.sql` **적용** - 부서 5건
(강남구 2 / 서천군 1 / 해운대구 1 / 순천시 1(미사용)). 표가 0행이라 화면 확인이 불가했고,
사업 등록폼의 "사업부서" select도 "-부서 미지정-"만 보였다. 이것으로 **③에서 남긴 갭 2번이 해소**된다.

**검증**: 검색조건 4종 실측(지자체 11680→2 · 광역 11000→2 · 부서명 '청소년'→1 · 사용유무 N→1 ·
등록일 최근7일→1) · 수정 경로 트랜잭션 검증 후 롤백(부서명·사용유무·최종수정자 반영, **등록일시 불변**).
admin·donation `bootJar` EXIT=0.

##### 2026-10-06 아침 - 기동 실패 2건 수습(내 결함)

사용자가 아침에 재기동하자 **donation이 기동 실패**했다. 원인은 ③에서 내가 넣은 순환참조다:
`DesignatedAdminService`가 사업구분 라벨을 얻으려 `DonationService`를 주입했는데, `DonationService`는
이미 `autoCloseIfGoalReached` 때문에 `DesignatedAdminService`를 주입하고 있었다.
`bootJar`는 EXIT=0이라 못 잡았다 → **코드표 조회는 `CommonCodeRepository`를 직접 주입**해 끊었다
(`DesignatedAdminService.codesOf` private 메서드). [[build-is-mine-restart-is-users]]에 검증 절차 추가.

같은 아침 로그에서 **팝업관리 목록 500**도 발견했다(특정사업과 무관, 시스템관리 영역).
`(:query is null or ... like concat('%', :query, '%'))`의 파라미터가 bytea로 바인딩돼 PostgreSQL이
거절한 것 - admin 5개 리포지터리에 `cast(:x as String)` 적용. 상세는 [[backend-db-pitfalls]].

**검증(여분 포트 기동 + 내부 API 직접 호출)**: donation 8092 / admin 8099 모두 `Started ...Application`.
`GET /search` 요약 = **5건 / 16,662,000원**(②의 교정치와 일치) · `bsnsType=100` 필터 정상 ·
부서 검색 전체/지자체/사용유무/부서명(UTF-8) 정상 · `{id}/stat`·`departments/{id}` 정상.
admin·donation `bootJar` EXIT=0, 임시 인스턴스는 모두 내렸다.

##### ③ 재검수 (2026-10-13) - 등록/수정 폼 AS-IS 전수대조, 큰 누락 2건

사용자가 "ASIS와 동일하게, css도 버튼도 틀어지지 않게" 재지시. **③에서 완료로 적었지만
AS-IS form.jsp 928줄을 다시 전수 대조하니 통째로 빠진 블록이 2개 있었다** - 그때는 표(정보
입력칸) 위주로만 맞췄고 그 아래 영역을 못 봤다.

**누락 ① 스마트에디터 자체가 없었다** - AS-IS `<module:smarteditor id="prjCn"/>` 미이식이라
상세 설명이 맨 textarea 로 떠 있었다. `fragments/smarteditor :: init/editor('dsgnDntnBizCn')` 추가.

**누락 ② "이미지 설명"(접근성) 블록 전체** - AS-IS `item-option-table2`(순번/이미지 설명/삭제
+ [+ 추가])가 화면·백엔드 **양쪽 다** 없었다. 표 `G_DSGN_DNTN_BIZ_CN_IMG_EXPLN` 은 DDL에
이미 있었는데(개발DB에도 실재) 엔티티·리포지토리·API가 없었다. 공지쪽
`DesignatedNoticeImgExpln` 과 달리 **복합키가 아니라 자체 PK(시퀀스)** 다.
- donation: `DesignatedProjectImgExpln` + 리포지토리 신규, `DesignatedAdminService`
  create/update에 `imgExplns` 오버로드 + `replaceProjectImgExplns`(AS-IS처럼 전삭제 후 재삽입)
  + `imgExplnsOf`, API `GET .../{id}/img-explns` 신규
- admin: 클라이언트 multipart에 `imgExplns` 반복 필드(공지와 같은 방식), 컨트롤러 통과

**그 외 AS-IS와 어긋나 고친 것**
- **버튼 영역**: `btn_all btn_center` → AS-IS `id="buttons" btn_all btn_right`,
  **[목록] 버튼 누락** 추가, `scrollButtons()`(바닥 170px 밖이면 `fixed_button`) 이식
- **★승인 버튼 조건이 둘 다 틀렸다**: AS-IS는 `prjId>0 && prjStatus=='1'(대기) && ROLE_ADMIN_1~4`.
  TO-BE는 상태 조건이 아예 없었고 권한도 1~5였다. **목록(list.jsp:338)의 일괄 승인처리가
  1~5라서 그걸 그대로 가져다 쓴 것** - 두 화면은 권한 범위가 다르다, 합치지 말 것.
  `CAN_APPROVE_ON_FORM`(1~4) 상수 분리.
- **AS-IS 유효성 검사 전부 이식**(그동안 HTML5 required 뿐이었다): 사업구분·사업부문·기간 입력·
  종료<시작·`Common.searchDateMonth`·날짜 정합성(윤년)·3년 제한·목표금액·상태·공개여부·
  이미지 최소1/최대10·확장자(jpg,gif,png,pdf,jpeg)·**파일당 500KB**·이미지 설명 공백행 제거·
  **에디터 이미지 수 == 설명 수**(다르면 저장 차단, alt 주입)·본문 빈값. 문구 전부 verbatim.
- 사업부문 select 에 `required` 클래스 누락분 추가, 목표금액은 콤마 제거 후 판정
- **지자체 2단 select 잠금**(AS-IS는 값이 있으면 mousedown 차단) · **ROLE_ADMIN_10 사업부서
  잠금**(`unableChangePart`) · ROLE_ADMIN_10이 "진행중" 고르면 "대기 상태로 저장됩니다" 확인
- 이미지 삭제: "최소 1개" 가드 + 확인 문구 AS-IS 그대로
- AS-IS에 있는 hidden 블록도 그대로 복원(`copy_button hidden`, `add_detail_image_file`
  + `#multiple_files`) - [[asis-porting-principles]]
- 기타 사항 textarea 의 인라인 `width:99%` 제거(AS-IS는 없다, opmanager.css 가 100% 준다)
- **★발명된 링크 제거**: `<div class="location">지정기부 관리</div>` 가 breadcrumb 조각과
  **같은 자리에 겹쳐** 있었다(목록 화면은 조각 하나뿐). [[admin-invented-backlink-per-screen-cleanup]]

**일부러 안 옮긴 것**: AS-IS가 prjCn 을 `encodeURIComponent` 로 싸서 보내고 서버가 푸는 것
(AS-IS 쪽 인코딩 회피용, TO-BE donation 은 그대로 받는다).

**검증**(여분 포트 donation 8093 / admin 8099, Playwright):
등록폼 col 4칸·라벨 8개 / 수정폼 col 6칸·라벨 11개(남은 일수·모금 된 금액·달성률) ·
버튼 `btn_all btn_right` [등록][목록] / 수정은 [승인][수정][목록](**PENDING 일 때만 승인 노출** 확인) ·
에디터 iframe 생성 · 이미지 설명 추가/삭제 시 순번 재계산(1,2 → 삭제 후 1) ·
**등록 왕복 실측**: 설명 2건이 `img_seq` 1,2 로 저장되고 수정화면에 다시 그려짐 ·
콘솔 에러 0 · 검증용 사업 2건(1107,1108)은 관련 4개 표에서 삭제 완료.
admin·donation `test`+`bootJar` EXIT=0.

**★주의(내가 겪은 함정)**: admin 의 donation 주소 프로퍼티는 `ghlove.donation-service.base-url`
이다(`services.*` 아님). 틀린 키로 override 하면 **조용히 기존 8082 를 호출**해서
"코드는 맞는데 저장이 안 되는" 것처럼 보인다 - 1차 검증이 그래서 0건으로 나왔다.

**남은 것**: 목록 화면 썸네일이 `/uploads/donation/...` 를 admin 에서 찾아 404 (donation 이
가진 파일이라 admin 정적 경로엔 없다). 이번 폼 작업과 무관한 **기존 문제**이고 목록·상세
전반에 걸리므로 별도 처리 필요.

##### 공지 등록폼(notice) 제출 검증 이식 (2026-10-13, 사용자 지적)

사용자가 AS-IS 화면 캡처와 함께 "내용에 사진을 2개 이상 올리면 그 갯수만큼 설명을 입력해야
등록된다"고 지적. **TO-BE `designated/notice-form.html` 에는 제출 검증이 통째로 없었다** -
이미지 설명 표는 있는데 검사하는 쪽이 없어 사진만 넣고 설명 없이 등록이 됐다.
AS-IS `notice/form.jsp` validator 를 그대로 이식:
공개여부 미선택 · 첨부 10개 초과 · 공백 설명행 제거 · `optionSize==0 && imageSize>0` ·
**`optionSize != imageSize`** · alt 주입 · 에디터 빈 본문. 문구 verbatim.
`scrollButtons()` 도 같이 넣었다(AS-IS에 있는데 빠져 있었다).
**AS-IS가 주석처리한 500KB 검사는 켜지 않았다**([[asis-porting-principles]]).

**★함정 - 제출을 막으면 로딩막이 화면을 덮는다**: `op.common.js:1683` 이
`$('form').not('.without-loading').on("submit", Common.loading.show)` 라서, submit 이벤트에서
`preventDefault()` 로 막으면 **반투명 막(`#loading-dimmed`)이 남아 클릭이 전부 먹힌다**.
AS-IS는 jQuery `.validator()` 를 쓰고 따로 `Common.loading.hide()` 를 부르는 자리도 있다.
→ **검증 실패 분기마다 `Common.loading.hide()` 를 반드시 부른다**. 사업 폼·공지 폼 둘 다 적용.
Playwright 에서 "element intercepts pointer events" 로 먼저 드러났다.

**검증**: 사진 2 + 설명 0 → "시각 장애인을 위한 이미지 설명을 입력 부탁드립니다." ·
사진 2 + 설명 1 → "…이미지 설명 갯수: 1 , …이미지 갯수: 2"(사용자 캡처와 동일) · 둘 다 URL 유지 ·
사진 2 + 설명 2 → 저장되고 **본문에 alt 2개 주입 확인**, `img_seq` 1·2 저장 확인.
검증용 공지 1건 삭제 완료. admin `test`+`bootJar` EXIT=0.

**남긴 차이 1건**: TO-BE 입력칸은 HTML5 `required` 속성을 쓰고 AS-IS는 jQuery `.validator()`
의 `.required` 클래스를 쓴다. 비어 있을 때 **브라우저 기본 툴팁 vs AS-IS alert** 으로 안내
모양이 다르다(막는 동작 자체는 같다). `.validator()` 전환은
[[tooling-and-local-env]] 위험이 있어 건드리지 않았다.

##### 수정화면 남은 일수 D-Day 표기 (2026-10-13, 사용자 지적)

목록에서 사업명을 누르면 가는 곳은 **수정 화면**(`/designated-donation/form/{prjId}`)이다.
거기 "남은 일수"가 TO-BE는 숫자(`87 일`)였는데 **AS-IS는 D-Day 표기**다.

**AS-IS `DesignatedDonation.getLeftDayStr()`**: `leftDay == 0 → "D-Day"` ·
`> 0 → "D+" + n` · `< 0 → "D" + n`. 여기서 `leftDay` 는 매퍼의
**`DATEDIFF(NOW(), DSGN_DNTN_BIZ_END_YMD)` = 오늘 − 종료일**(부호 있음)이라,
종료일이 남아 있으면 음수 → `"D" + (-79)` = **D-79** 로 찍힌다. 지났으면 **D+408**.
화면 출력은 `${leftDayStr} 일` 이라 **"D-79 일"** 이 된다(뒤의 "일"도 AS-IS 그대로).

- donation `ProjectStat` 에 `leftDayStr` 추가(`leftDays` 숫자는 쓰는 곳이 있어 유지)
- 세 분기 + 깨진 날짜까지 단위테스트로 고정: `DesignatedAdminServiceLeftDayStrTest`
- **실측**: 1106(종료 20261231) → `D-79 일` · 204(종료 20250831, 지남) → `D+408 일`

**★AS-IS 결함(옮기지 않음)**: `preview.jsp` 는 `D-${leftDay}` 로 **접두사를 직접 붙인다**.
leftDay 가 음수라 AS-IS 미리보기는 **`D--79`** 로 찍힌다. TO-BE `preview.html` 은
`'D-' + stat.leftDays`(0으로 clamp된 양수)라 우연히 정상으로 보인다 - 사용자가 지적한 건
수정화면이고 미리보기는 지금이 더 맞게 나오므로 **건드리지 않았다**.

**"상세설명이 아래에 미리보기로 보인다"는 지적**: form.jsp 에 별도 미리보기 블록은 없다(검색 0건).
**스마트에디터가 저장된 본문을 렌더링해 보여주는 것**이고, TO-BE는 그 에디터 자체가 빠져 있어
원문 HTML이 맨 textarea 로 보였던 것 - 위 ③ 재검수에서 이미 해소됐다(에디터 iframe 안에
본문·이미지가 그려지는 것까지 실측 확인).

##### 다음: ⑥ 권한요청(request, 1,174줄) → ⑦ 미리보기(632줄) → ⑧ 통계 2화면 잔여
**재기동 대기**: donation은 2026-10-06 09시 현재 **내려가 있다**(위 기동 실패 후). admin은 떠 있지만
팝업 수정분이 안 들어간 옛 jar다. 사용자가 재기동한 뒤 눈으로 볼 목록 -
5112 답변폼 / 5104 FAQ / 5201·5202 매뉴얼 / 5102 1:1문의 / 17101 목록·등록폼·사업부서 관리 /
**팝업관리 목록(검색어 비운 상태)**.
목록의 **엑셀 다운로드·미리보기**도 아직 안내만 띄우는 상태다(원장 ② 참고).
**③에서 남긴 갭 1번(사업부문 코드표 없음)은 여전히 데이터 결정 대기**다.

##### 참고: AS-IS 목록(17101) 요구사항 명세 - ②에서 **전부 반영 완료**
(아래는 ② 착수 전에 뜬 요구사항표다. "donation API 확장이 선행된다"고 적어 두었던 것은
②에서 `GET .../admin/search`·`POST .../admin/bulk-update`·`GET .../admin/{id}/stat`을 만들어
해소되었다 - 다시 선행과제로 읽지 말 것. 남은 것은 **엑셀·미리보기**뿐이다.)
- 검색 6종: 지자체(광역+시군구, ROLE_ADMIN_1~4만 노출) · 사업구분(`DSGN_BSNS_TYPE`, 라벨은 `detail`) ·
  기간(prjStDt~prjEdDt + 날짜버튼 오늘/1주일/한달/3개월/1년) · 사업명 · 상태 라디오(전체/2 진행/9 종료/1 대기) ·
  공개여부 라디오(전체/Y/N)
- 요약 집계표 4칸: 총 목표금액 / 총 모금액 / 총 모금 달성율 / 총 기부건수
- 컬럼 16개: 체크박스/ID/[지자체명]/모금상태/사업구분/사업명(이미지+링크)/기부건수/시작일/종료일/
  목표금액/모금액/달성율/공개여부/사업부서/공지사항(버튼)/승인이력(버튼)/미리보기(버튼)
- 출력수 **10/50/100/200/500**(다른 화면과 다르다)
- 하단 일괄처리 4종: 진행(승인처리)='2'[ROLE_ADMIN_1~5만] / 종료='9' / 공개='Y' / 비공개='N'
- 엑셀 다운로드(`/list/excel-download`)
- 모금상태 표기: '1'→승인 대기, '2'→진행, '9'→종료

### admin-give-state-area-port-progress

> 기부금관리(14000) 영역 AS-IS vs TO-BE 4축(JSP+매퍼+컨트롤러+ServiceImpl) 대조 분석 완료, 구현은 아직 착수 전(2026-10-12) - 재개 시 참고용 상세 스펙


### 기부금관리(14000) 영역 - 분석만 완료, 구현 미착수

2026-10-12 사용자가 `/give-state`(14101 기부금모금현황) 화면이 AS-IS와 많이 다르다고 보고 →
4축(JSP+매퍼SQL+컨트롤러+ServiceImpl) 대조 상세 분석 진행. **이번 세션은 분석만 하고
구현은 보류** - "정리해놨다가 나중에 작업하기 전에 참고하자"는 사용자 지시로 마무리.

#### 메뉴 구조

```
14000 기부금관리(top)
 └14100 기부금모금현황(section)
    ├14101 기부금모금현황      /give-state         ✅ 있음(이번에 분석한 화면)
    ├14102 기부포인트현황      /give-point         있음(미대조)
    ├14103 기부금전체현황      /give-state         ⚠ 14101과 URL 중복 - AS-IS는 별도 화면
    ├14104 기부금 운용정보     /give-operation     있음(미대조)
    └14105 기부금·포인트 변경  /give-reqmng        있음(미대조)
 └14200 기금사업소개(section)
    └14201 기금사업 등록/관리  (menu_url 빈값)     ❌ LNB에서 섹션 전체가 사라짐(아래 참고)
```

#### "기금사업소개" LNB 누락 원인 (확정, RBAC 아님)

`MenuService.navTreeFor()`가 섹션의 leaf 중 `menuUrl`이 있는 것만 모아 0개면 섹션째
건너뛴다. 14201의 `menu_url`이 빈 값이라 14200 섹션 전체가 LNB에서 사라진 것.
status_code/display_flag/권한행은 전부 정상.

AS-IS 원본에서 14201의 실제 URL은 `/opmanager/give/give-notice/list`
(`GiveNoticeManagerController`, `@RequestProperty(title="기금사업소개")`) -
`boardCode="DONATION"`인 공지글을 관리하는 CRUD 화면(스토어프론트 "기금사업소개" 페이지에
뜨는 지자체 공지 콘텐츠를 관리자가 작성). **TO-BE에 이 화면 자체가 없다** - 과거 리시드 때
"TO-BE 화면 미구현"이라 url을 비워둔 채 방치된 상태. 즉 **신규 구현 대상**(기존 화면 보강이
아니라 admin에 없는 CRUD 한 벌을 새로 만들어야 함).

#### 14101 "기부금 모금현황" - AS-IS 실제 동작 (전부 확인됨, 재조사 불필요)

##### 컨트롤러 구조 (`GiveStateManagerController`, AS-IS)
- **역할 분기**: `ROLE_ADMIN_1~4`(시스템/행안부) → `list.jsp`(전체 지자체 조회 가능).
  `ROLE_ADMIN_5~6`(지자체) → `list_locgov.jsp`(자기 지자체로 고정, 완전히 다른 레이아웃).
- **GET은 절대 조회 안 함** - mois 역할은 `isSubmit`이 true일 때만 sum/list/count를
  모델에 채운다(GET으로는 `isSubmit`이 설정될 수 없어 항상 빈 화면). locgov 역할은 GET에서
  `isSubmit` 체크조차 없이 **항상** `sum=new GiveState()`(전부 null)·`list=EMPTY_LIST`·
  `count=0`을 깔아놓는다 - 두 역할 다 결과적으로 GET=빈 화면, POST(검색버튼)=실조회.
  이 프로젝트에서 반복 확인된 "검색화면 GET은 조회 안 함" 관례와 동일 - 버그 아님.
- **년도 코드**: `CodeUtils.getCodeList("YYYY")`에서 `id=="2022"`와 빈 id를 명시적으로
  제외. TO-BE `admin.op_common_code`에 YYYY 코드가 이미 있고 2022/2027~2031은
  `use_yn='N'`으로 로드돼 있어(`migration-admin-common-code-asis-load.sql:1199~1209`),
  **`use_yn='Y'`로만 걸러도 결과적으로 AS-IS와 동일**(2023~2026) - 추가 변환 로직 불필요,
  그냥 `CommonCodeRepository`에서 YYYY/Y만 읽으면 됨.

##### 집계 쿼리 (`slave-give-state-mapper.xml`, 확인됨)
- **getGiveStateSum**(합계 카드 4개): `G_CNTR`에서
  `DELETE_AT='N' AND cntr_sttus_code='200' AND sttemnt_pay_de IS NOT NULL` + 선택적
  연도/지자체 필터. `CNTR_AMT`(기부모금액 SUM) / `GIVE_PERSONS`(DISTINCT user_id) /
  `CNTR_POINT`(**GC.CNTR_POINT 컬럼 자체의 SUM** - 발생포인트) /
  `CNTR_USE_POINT`(G_CNTR_USE_POINT 서브쿼리 SUM, use_se_code='1'). **4개 카드뿐, 기부건수
  카드는 없다.**
- **getGiveStateList/Count**(목록): 필터가 **`cntr_sttus_code='200'`만**
  (DELETE_AT·sttemnt_pay_de 조건 없음) - **합계와 목록의 상태필터가 AS-IS 자체에서 불일치**.
  연도×지자체로 GROUP BY, `give_cnt`(COUNT(cntr_sn), 목록 컬럼으로만 존재, 카드 아님) 포함.
  답례품사용금액은 행마다 상관 서브쿼리로 `a.cntr_locgov_code = gc.cntr_locgov_code`만
  필수 조건이고 `year(point_use_de)` 필터는 **검색조건에 연도가 있을 때만** 걸린다 -
  연도 미지정이면 그 지자체의 전체기간 합계가 해당 지자체의 모든 연도 행에 동일하게 반복
  표시됨. **단, 컨트롤러가 GET/POST 둘 다에서 `shCntrYear`가 비어 있으면 당해년도로
  강제 채우기 때문에(`StringUtils.defaultIfEmpty(.., today)`) 사용자가 연도를
  명시적으로 지우지 않는 한 이 반복 증상은 잘 드러나지 않는다.**

##### 사용자 결정 (2026-10-12): 위 2가지 버그성 동작(상태필터 불일치, 답례품사용금액 반복)은
**AS-IS 그대로 재현** - 더 "합리적인" 쪽으로 통일하지 않는다.

##### 화면 (list.jsp / list_locgov.jsp, 확인됨)
| | mois(list.jsp) | locgov(list_locgov.jsp) |
|---|---|---|
| 검색조건 | 년도(전체 옵션 있음) + 시도→시군구 캐스케이딩(`wdrChange` ajax) | 소속지자체(고정표시) + 년도(전체 옵션 **없음**, 필수) |
| 합계카드 | 기부모금액/기부인원/발생포인트/답례품금액 (4개) | 동일 |
| 목록컬럼 | No./년도/광역지자체/기부지자체/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (9, 지자체명 클릭→detail) | No./년도/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (7, 년도 클릭→detail) |
| 출력수 select | 있음(10/20/50/100) | **없음**(itemsPerPage select 자체가 없음) |
| 엑셀 | `list/download-excel` | `list/locgov/download-excel`(별도 URL, 서버가 `getLocgovCode`로 지자체 강제) |
| 엑셀 헤더 | No./년도/광역지자체/기부지자체/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (9) | No./년도/기부모금액/기부인원/발생포인트/답례품금액 (**6, 기부건수 없음** - 화면 테이블엔 있는데 엑셀만 뺀 AS-IS 자체 불일치) |
| 초기화 버튼 | `/opmanager/give/give-state/list`로 이동(검색조건 리셋) | 동일 |

##### TO-BE 현재 상태 (수정 전)
- `GiveStateController.list()`(`/give-state`)는 역할분기·GET빈화면 없이 **항상** 즉시
  전체집계 표시, 카드 **5개**(기부건수 카드 추가 - AS-IS에 없는 발명), No.컬럼·페이징·엑셀
  없음, 지자체 역할 전용 레이아웃 없음.
- `GiveStateService.search()`가 `DonationLedger`(STAT_DONATION_LEDGER)와
  `PointLedgerStat`(STAT_POINT_LEDGER) 두 ReadModel을 조인해서 집계 - **발생포인트를
  포인트 원장 자신의 `createdDate` 연도로 그룹핑**하는데, AS-IS는 **기부(G_CNTR) 자신의
  연도**로 묶는다(포인트 적립 시각이 아니라 기부일 기준) - 그룹 키가 다르면 숫자가 달라질 수
  있음. **답례품 사용금액**은 AS-IS의 `G_CNTR_USE_POINT`(지자체×사용일 기준) 개념 자체가
  admin에 없어서 포인트 원장의 TXN_TYPE=USE를 대체로 쓰고 있다 - 의미가 다른 데이터.

##### 근본원인 조사 결과 (2026-10-12, 구현 안 함 - 아래 보류 사유 때문)
point 서비스의 실제 원장 `PointLedger`(`PT_POINT_LEDGER`)에는 이미
**`stdrYear`**(기부연도 - 주석에 "AS-IS는 year(G_CNTR.CNTR_DE)로 요약을 묶는다"라고 명시),
**`refKey`**(기부건번호), **`cntrAmt`**(기부금액)가 **있다** - AS-IS parity를 염두에 두고
만들어 둔 필드인데, admin으로 가는 `PointLedgerEvent`/`PointLedgerStat` 미러에는 이 필드들이
빠져 있어 묻혀 있었다. "답례품 사용금액"은 더 심해서 admin 쪽에 대응 개념 자체가 없다
(point의 `g_cntr_use_point` 테이블에서 직접 집계해야 함, point-use-tracking 관련
[[point-use-tracking-g-cntr-use-point]] 참고).

**donation.g_cntr.cntr_point 컬럼은 죽은 컬럼이다** - AS-IS와 같은 컬럼명으로 테이블엔
존재하고 `MemberDonationAdminRepository`/`OffgiveAdminRepository`가 **읽기는** 하지만,
실제 기부 완료 시 **아무도 이 컬럼에 값을 쓰지 않는다**(point 서비스의
`pointService.creditForDonation()`은 point 자신의 원장에만 적립하고 donation 스키마로
역기록하지 않음). 즉 일반회원관리 상세화면 등 **다른 화면에서 이 컬럼을 읽는 누적
발생포인트 표시도 라이브 데이터로는 항상 0에 가까울 수 있다** - 이번에 발견했지만
14101 범위 밖이라 별도 기록만 해둠(추후 점검 필요, [[asis-porting-principles]]
유형).

#### 보류 사유 - RFP vs 기존 동결 결정 충돌 (2026-10-12 확정)

위 발생포인트/답례품사용금액을 AS-IS와 정확히 일치시키려면 ① `STAT_POINT_LEDGER`에
stdrYear/refKey/cntrAmt 컬럼 추가(기존 ReadModel 스키마 확장) ② 답례품사용금액용 신규
ReadModel/이벤트 파이프라인 신설이 필요하다. RFP `SFR-009`는 "대민/관리자/제공자 서비스
조회 기능(**기부내역/포인트**/주문배송/정산/통계 등)을 **ReadModel로 통일된 방식 제공**"을
명시하므로 방향 자체는 맞다. 하지만 **2026-09-22 사용자 결정**
([[da-design-pending]]) - "모든 조회모델(ReadModel) 신규/확장 작업은
DA(데이터아키텍트) 설계안이 나올 때까지 전부 보류, 이미 만들어진 건 그대로 둔다" - 가
정확히 이 작업을 막는다. 사용자에게 예외 허용 여부를 물어봤고, **"기존 결정대로 DA
설계안 나올 때까지 보류"로 확정**. 즉 14101의 발생포인트/답례품사용금액 집계방식 교정은
**DA 설계안이 나올 때까지 손대지 않는다.**

#### 재개 시 체크리스트 (DA 설계안 나오기 전에도 할 수 있는 것 - ReadModel과 무관)

1. **mois/locgov 역할분기 + GET빈화면/POST조회** 구조로 컨트롤러 재작성
2. **카드 4개로 축소**(기부건수 카드 제거 - 발명이었음), 목록에 No.컬럼·기부건수 컬럼 추가
3. **지자체 역할용 별도 템플릿**(locgov 전용, 출력수 select 없음, 지자체 고정표시)
4. **년도 select**: `CommonCodeRepository`에서 `code_type='YYYY' AND use_yn='Y'` 조회
   (추가 필터링 불필요, 이미 2022/2027~2031이 N으로 로드돼 있음)
5. **시도→시군구 캐스케이딩** ajax 엔드포인트(`LocgovClient` 이미 있음, wdrChange 패턴 포팅)
6. **엑셀 다운로드 2종**(mois 9컬럼 vs locgov 6컬럼 - **엑셀만 기부건수 빠지는 AS-IS 자체
   불일치도 그대로 재현**) - 기존 `ExcelDownloadWriter` 공통유틸 재사용 가능
   ([[admin-excel-download-real-xlsx-progress]] 참고)
7. **14103 "기부금전체현황"**: 현재 14101과 URL이 겹쳐 있음(AS-IS `detail_list`/
   `cntr_status.jsp`는 별도 화면) - 분리 신규구현 필요(이번엔 미대조, AS-IS 컨트롤러
   `GiveStateManagerController.viewTest/overallStatusOfDonations` 참고)
8. **14201 기금사업소개**: `GiveNoticeManagerController` 기준 신규 admin CRUD 구현
   (boardCode=DONATION), 완료 후 `op_menu`의 menu_url 채우기
9. 발생포인트/답례품사용금액은 **위 보류 사유가 풀릴 때(DA 설계안 확정) 다시 논의**

관련: [[da-design-pending]] [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-designated-donation-area-port-progress)
(같은 유형의 충돌을 먼저 겪은 영역) [[api-vs-kafka-decision-criteria]]
[[point-use-tracking-g-cntr-use-point]] [[admin-excel-download-real-xlsx-progress]]
[[asis-porting-principles]] (donation.g_cntr.cntr_point 죽은 컬럼 건)

### admin-sales-day-statistics-port

> 통계>일자별 매출(shop-statistics/sales/day) AS-IS 이식 - 17칸 2단헤더·취소 음수 규칙·주문방법 축은 TO-BE에 데이터가 없어 "-


### 통계 > 일자별 매출 (AS-IS `/opmanager/shop-statistics/sales/day`)

2026-10-13 이식. TO-BE에 있던 화면은 **발명된 축소판**이었다 - 5칸짜리 표
(기간/결제건수/결제포인트/취소건수/취소포인트)에 "전액 포인트결제 기준. 미지정 시 최근 30일."
이라는 **AS-IS에 없는 안내문구**까지 달려 있었다. AS-IS 사양으로 다시 만들었다.

#### AS-IS 사양 (day.jsp 128줄 + include/date-list.jsp 130줄)

- **GET 진입에서는 조회하지 않는다**(빈 표). POST에서만 집계한다 - 컨트롤러가 GET에서 모델에
  아무것도 안 담는다.
- 검색조건은 **기간 + "소계 노출" 라디오** 둘뿐. 판매자 select 는 AS-IS가 통째로 주석처리
  상태라 켜지 않았다([[asis-porting-principles]]).
- **날짜버튼이 6종**: 오늘/1주일/**15일**/한달/3개월/1년. 다른 화면의 5종과 다르다.
- 표는 **2단 헤더 17칸**: 일자(rowspan2) · 주문방법(rowspan2) ·
  결제 6(건수/상품판매가/할인/판매액/배송비/소계) · 취소 6(동일) · 합계 3(판매액/배송비/합계).
- **★취소는 전부 음수로 담는다**(건수까지 `-1`). 그래서 "합계 = 판매 + 취소"가 그대로 순액이다.
  요약줄의 "취소 : -10건" 도 AS-IS가 음수를 그대로 찍는 것이다 - 버그 아님.
- 파생식(AS-IS `BaseStats`): 소계 = 판매액 + 배송비 · 합계판매액 = 판매액 + 취소판매액 ·
  합계 = 합계판매액 + 합계배송비 · 총주문건수 = 판매건수 + 취소건수.
- 소계 노출 'N'일 때만 표에 `odd-even` 줄무늬 클래스가 붙는다.
- 엑셀 버튼은 `<sec:authorize access="hasRole('ROLE_EXCEL')">` 안에 있다.

#### ★ROLE_EXCEL은 운영자에게 없다 - 엑셀 버튼은 AS-IS에서도 안 보인다

`ROLE_EXCEL` 은 AS-IS에서 **판매자 계정에만** 붙는다(`SellerServiceImpl`·`SellerUserServiceImpl`).
운영자에게 주는 코드가 없고 `OP_ROLE` 에 행도 없다. 즉 **AS-IS 운영자 화면에서도 이 버튼은
뜨지 않는다.** 비활성 상태 그대로 옮겼다 - 엔드포인트
(`/shop-statistics/sales/day/excel-download`)는 만들어 뒀으므로 권한만 주면 바로 쓸 수 있다.

#### ★주문방법(DEVICE_TYPE) 축 - TO-BE에 데이터가 없다

AS-IS는 `OP_ORDER_ITEM.DEVICE_TYPE`(PC/MOBILE/IOS/ANDROID, enum `DeviceType`)으로 **한 일자를
여러 행으로 쪼개고** 일자 셀에 rowspan을 건다. TO-BE 실제 주문표 `ord.od_order`(37컬럼, 44행)에
**그 컬럼이 없다**(실측 확인). 나머지 16칸은 전부 계산 가능하다:
`unit_price`·`option_price`·`quantity`·`discount_amount`·`delivery_fee`.

**사용자 결정(2026-10-13)**: 컬럼 구조는 AS-IS 그대로 두고 **값만 "-"** 로 표시한다.
→ 일자당 한 행이 된다. 나중에 수집을 켜면 그대로 채워진다.
(대안이던 "DEVICE_TYPE 컬럼 신설 + UA 수집"은 기존 44건을 소급할 수 없어 보류.)

#### 금액 매핑 (AS-IS 컬럼 → TO-BE)

| AS-IS | TO-BE |
|---|---|
| 상품판매가 `(PURCHASE_PRICE + OPTION_PRICE) * QUANTITY` | `(unit_price + option_price) * quantity` |
| 할인 (쿠폰·판매자·스팟·등급·세트 **5종 합**) | `discount_amount` **하나** (TO-BE는 분리 보관 안 함) |
| 판매액 `SALE_PRICE * QUANTITY` | 상품판매가 − 할인 |
| 배송비 | `delivery_fee` |
| 취소 판정 `SALES_CANCEL_DATE` | `order_status = 'CANCELLED'` (나머지는 판매분) |

#### 구현

- **order** `OrderDateStatsService` 신규(네이티브 집계, day/month/year 공용) +
  `GET /api/admin/orders/stats/date`
- **admin** `OrderAdminClient.dateStats()` + `DateStatRow`(파생식은 AS-IS BaseStats 그대로 record 메서드로)
- **admin** `ShopStatisticsTierBController` - GET/POST/엑셀 3개. 기존 `period()` 는
  month/year 만 맡도록 분리(그 둘은 아직 옛 축소판 `sales-period.html` 이다 - 같은 방식으로
  보강하면 된다. AS-IS도 `date-list.jsp` 를 셋이 공유한다)
- **템플릿** `shop-statistics/sales-day.html` 신규

**검증**: GET 진입 빈 표 · 헤더 1행 `일자/주문방법/결제(6)/취소(6)/합계(3)` · 2행 15칸 ·
날짜버튼 6종 · 소계 라디오 N기본 · 엑셀버튼 숨김 · 검색 후 12일자 ·
합계 `34 / 1,876,000 / 0 / 1,876,000 / 6,000 / 1,882,000 / -10 / -638,000 / 0 / -638,000 /
-14,000 / -652,000 / 1,238,000 / -8,000 / 1,230,000`(파생식 전부 일치) ·
소계 노출 ON → 소계행 12 + `odd-even` 제거 · 콘솔/HTTP 오류 0.
order·admin `test`+`bootJar` EXIT=0.

#### 월별 매출(sales/month) 이식 (2026-10-13)

일자별과 **표가 같아서** AS-IS처럼 조각 하나를 공유하게 바꿨다
(`fragments/stats-date-list.html` 의 `table(...)`·`summary(...)` - AS-IS `include/date-list.jsp` 대응).
일자별 화면도 이 조각을 쓰도록 전환했고, 엑셀 작성도 `writeDateStatsExcel()` 로 합쳤다
(AS-IS Day/Month ExcelView 가 같은 컬럼이다).

**일자별과 다른 점만**
- 기간이 datepicker 가 아니라 **년/월 select 4개**(startYear/startMonth/endYear/endMonth).
  년도는 AS-IS 그대로 **2010~올해**, 월은 01~12 두 자리.
- **날짜 버튼이 없다.**
- 기간 → 날짜 변환은 AS-IS `getStartDate()/getEndDate()` 의 month 분기 그대로:
  `startYear+startMonth+"01"` ~ `endYear+endMonth+**"31"**`(말일 보정 없이 31을 쓴다).
- 네 값 모두 **게터가 기본값(올해/이번 달)을 만든다** - 일자별과 같은 패턴.
- 표의 일자 칸이 **그 달의 일자별 화면으로 드릴다운**한다.
  일자별 화면은 AS-IS가 onclick 을 주석처리해 아무 동작도 안 한다.

##### 결제타입별 매출(sales/payment) 이식 (2026-10-13)

AS-IS payment.jsp 262줄. 앞의 세 화면과 달리 **표가 둘**이고 구조가 꽤 다르다.

- 검색: 기간(datepicker + 날짜버튼 6종, 일자별과 동일) + **결제타입 체크박스**(ORDER_PAY_TYPE 코드).
  ★체크박스는 **데이터를 거르고 열은 늘 전체를 그린다**(AS-IS 매퍼가 approvalTypes 로 IN 필터).
- 표① 결제타입별 총 매출금액 요약(열 = 전체 코드), 표② **날짜 × 결제타입 × 기기(PC/모바일/기타)** 피벗.
- 표②는 **결과가 있을 때만** 그린다. 날짜 행은 조회 결과가 아니라 **기간 전체를 하루씩**
  (endDate → startDate 역순) 만든다 - 값 없는 날도 0원 행이 나온다.
- 금액 색: 매출총액 칸은 0 회색 / 음수 빨강 굵게 / 양수 **파랑** 굵게, 타입별 칸은 0 회색 / 그 외 굵게.
- `.board_list` 에 `overflow-x:auto`, 꼬리 `<style> .board_list_table td{border:1px solid #ECECE1}`.
- ★h3·location 문구가 **"일자별 매출"** 로 되어 있다(AS-IS 복붙 흔적) - 어차피 op.manager.js 가
  메뉴명으로 덮어쓰므로 화면에는 "결제타입별 매출"이 보인다. 그대로 뒀다.
- ★엑셀만 `Shop.downloadExcelOrder(..., **false**)` 라 **개인정보 사유 모달을 거치지 않는다**
  (다른 엑셀 화면과 다르다).

**★데이터가 없는 축이 둘 - 사용자 결정(2026-10-13)으로 구조는 AS-IS 유지**
1. **결제타입**: TO-BE 주문표에 결제수단 컬럼이 없다. 전액 기부포인트 결제이기 때문
   (`OrderReconciliationRepository` 주석 "TO-BE는 결제수단이 포인트뿐"). → 코드 12종 열을 그대로
   두고 **`give-point`(기부포인트) 열에만 금액**, 나머지 11종은 0원.
2. **기기**: device_type 부재(주문방법 때와 같은 사정). → PC/모바일/기타 3칸을 그대로 두고
   **금액은 전부 "기타"** 에 넣는다(PC·모바일 0원).
   → 12×3 = **36칸 중 1칸에만 값**이 들어간다. 수집이 생기면 그대로 갈라진다.

**집계는 기존 것을 재사용**했다 - `dateStats("day", ...)` 의 `totalPayAmount()`(= 판매 + 취소,
취소가 음수라 순액)가 AS-IS의 `payAmount - cancelAmount` 와 같다. 새 API를 만들지 않았다.

**★발명 코드 제거 + 매핑 충돌**: 기존에 같은 URL(`/shop-statistics/sales/payment`)을 쓰는
축소판 메서드가 이미 있어서 **`Ambiguous mapping` 으로 기동 실패**했다(테스트 3건이 그래서 깨짐 -
DB 문제처럼 보였지만 아니었다). 옛 메서드를 제거했다. 서비스의 `paymentStats()` 는 호출부가
사라졌지만 남겨 뒀다(다른 축소판 화면들과 함께 정리할 때 같이 볼 것).

##### ★결제타입 순서가 체크박스↔표마다 달라 보이던 것 - 2차 정렬 키 누락 (2026-10-14 사용자 지적)

`CommonCodeRepository.findByCodeTypeOrderByOrdering`(= `commonCodeService.labelsOf` 가 쓰는 쿼리)에
**`ordering` 뒤 2차 정렬 키가 없었다**. `ORDER_PAY_TYPE` 12종은 전부 `ordering IS NULL`(동률)이라
`ORDER BY ordering nulls first` 만으로는 **동률 사이 순서를 SQL이 보장하지 않는다** -
PostgreSQL이 그 순간의 실행계획대로 아무 순서나 돌려줘도 표준 위반이 아니다.
체크박스(GET 요청)와 표 헤더(POST 요청, 검색 후)는 **서로 다른 SQL 실행**이라 각기 다른 순서로
나올 수 있었다 - 사용자가 실제로 겪은 증상이다.

`c.id` 를 2차 키로 추가해 매 호출이 항상 같은 순서를 내도록 고쳤다
(`order by c.ordering nulls first, c.id`). 같은 문제가 있던
`findAllByOrderByCodeTypeAscOrderingAsc`(공통코드 관리 화면)도 같이 고쳤다 -
새로고침마다 동률 구간 순서가 바뀌어 보이는 것을 막는다. **`labelsOf` 는 다른 여러 화면도
공유해서 쓰므로**(알림·팝업 등) 이 수정이 그 화면들의 코드 순서도 같이 안정시킨다.

**검증**: 같은 GET 요청을 5회 반복해 체크박스 순서가 **매번 동일**함을 확인(수정 전에는
이론상 매 요청 달라질 수 있었다) · GET 체크박스 순서 == POST 체크박스 순서 == POST 표 헤더
순서 **완전히 일치**(수정 후 실측 동일 문자열) · admin `test`+`bootJar` EXIT=0.

##### 체크박스 필터는 설계대로 동작한다 (같은 날 질문)

"결제타입 체크여부와 상관없이 다 조회되는게 맞아?" 질문에 대한 확인:
**체크 안 함(기본) → 전체 조회**가 AS-IS 규칙이다(매퍼가 `approvalTypes.length > 0` 일 때만
IN 필터를 건다 - 빈 배열이면 필터 자체가 안 걸린다). TO-BE도 `PaymentStatsParam.includesGivePoint()`
로 같은 규칙을 재현했다.

다만 **TO-BE 주문은 전액 기부포인트 결제뿐**이라(위 "데이터가 없는 축이 둘" 참고), 기부포인트를
포함해 체크하거나 아무것도 체크하지 않으면 늘 같은 결과가 나오고, 기부포인트를 **빼고** 체크하면
0건이 된다. 그래서 겉보기엔 "체크해도 안 해도 똑같다"로 보이기 쉽다 - 실제로는 필터가 걸리고
있고, 단지 테스트 데이터의 결제수단이 한 종류뿐이라 체感이 안 되는 것뿐이다.

**실측(2026-10-14)**: 미체크 → 1,230,000원(전체) · **신용카드+은행입금만 체크**(기부포인트
제외) → "등록된 데이터가 없습니다." / 0원(필터가 실제로 걸림 확인) · 기부포인트만 체크 →
1,230,000원(전체와 동일, 다른 결제수단 데이터가 없으므로).

**검증**: GET 진입 기간 `20261001~20261013`(이번 달 1일~오늘)·체크박스 12개·안내문구 ·
검색 후 요약표 13칸(총액 955,000원 + 12종, **기부포인트 열만 955,000원**) ·
피벗 헤더1 14칸(날짜·매출총액 + 12종 colspan=3)·헤더2 **36칸** · 날짜 **13행**(값 없는 날 포함) ·
10/07 행 매출총액 322,000원 / 기부포인트 PC·모바일 0원, **기타 322,000원** ·
`overflow-x:auto` 적용 · 콘솔/HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

#### ★★기간 조건은 timestamp 비교로 - AS-IS 문자열 방식을 물려받지 않는다 (2026-10-13 사용자 지시)

**AS-IS가 `99`를 쓸 수 있었던 이유**: AS-IS 날짜 컬럼이 전부 `VARCHAR(yyyyMMddHHmmss)` 라
조건이 `SALES_DATE BETWEEN CONCAT(#{startDate},'000000') AND CONCAT(#{endDate},'595959')`
**문자열 비교**다. 그래서 ① 없는 날짜 `20261099` 도 에러 없이 "상한"으로 동작하고
② `'595959'`(59시59분59초) 같은 값으로 하루 전체를 덮는 것도 가능했다.
**JSP/EL 에서 말일(28/29/30/31)을 계산할 방법이 마땅치 않아 생긴 편법**이지, 의도된 설계가 아니다.

TO-BE `ord.od_order.created_date` 는 **진짜 timestamp** 라 그 사고방식을 가져올 이유가 없다.
사용자 지시("99 붙이는 건 말도 안 된다, 쿼리로 잡아야지")로 `OrderDateStatsService` 의 WHERE 를
바꿨다:
```sql
-- before: to_char(o.created_date,'YYYYMMDD') >= :startDate  (컬럼에 함수 → 인덱스 못 탐)
-- after
and o.created_date >= :startTs
and o.created_date <  :endTsExclusive   -- 종료일 +1일, "미만" 비교
```
- 종료일을 **다음날 00:00 미만**으로 잡아 그 날 23:59:59 까지 빠짐없이 들어간다.
- 옛 링크·북마크가 `YYYYMM99` 를 들고 들어와도 깨지지 않게 `parseYmd()` 가 **일자를 말일로 당긴다**.
- GROUP BY 키는 `to_char(...)` 그대로다(필터가 아니라 키 생성이라 인덱스와 무관).

**검증**: DB에서 옛 문자열 방식 vs 새 timestamp 방식을 4개 구간으로 직접 대조 -
`20260101~20261231`(44건/2,644,500) · `20261001~20261031`(20/1,211,000) ·
`20260901~20260930`(24/1,433,500) · `20261007` 하루(6/398,000) **전부 일치**.
종료일 경계도 확인(10/07 마지막 주문 20:05 포함). 화면도 전후 동일하고,
**옛 `20261099` 를 넣어도 10월 결과가 그대로** 나온다(방어 동작 확인).

**남은 권고**: `ord.od_order` 에 인덱스가 **PK 하나뿐**이다(`od_order_pkey`). 지금은 44건이라
무관하지만 운영 데이터가 들어오면 `created_date` 인덱스가 필요하다 - DDL 변경이라 보류.

##### ★AS-IS에서 의도적으로 벗어난 것 1건 - 드릴다운 endDate

AS-IS는 `?startDate=YYYYMM01&endDate=YYYYMM**99**` 를 넘긴다. 문자열 비교 상한으로는 동작하지만
**받는 일자별 화면의 기간 입력칸에 `20261099` 라는 없는 날짜가 그대로 찍힌다**
(AS-IS가 `getEndDate()` 에서 값을 그대로 돌려주기 때문). 처음엔 verbatim 으로 옮겼는데
사용자가 "잘못된 일자로 셋팅된다. 99" 로 지적해서 **실제 말일**을 넘기도록 바꿨다
(`@opDate.monthEnd()` 신설, 202610 → 20261031). **조회 범위는 둘이 완전히 같다** -
바뀌는 건 화면에 보이는 기간뿐이다. AS-IS와 똑같이 되돌리려면 이 한 곳만 `99` 로 돌리면 된다.
실측: 2026-10 → `20261001~20261031`, 2026-09 → `20260901~20260930`, 그 상태로 검색하면 10월 3일자만.
- 일자 칸 표기는 6자리라 `@opDate.**ym**()` 을 새로 만들어 썼다(yyyyMM → yyyy-MM).

**검증**: 기본값 2026/10~2026/10 · 년도 17개(2010~2026)·월 12개 · 날짜버튼 0 · GET 진입 빈 표 ·
2026/01~12 검색 시 2026-10/2026-09 두 달, 합계가 일자별과 완전히 일치
(`34 / 1,876,000 / … / 1,230,000`) · 드릴다운 클릭 → 일자별 화면에 `20261001~20261099` 전달 ·
일자별 회귀(소계 노출 12행, odd-even 해제) 정상 · 콘솔/HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

#### 년도별 매출(sales/year) 이식 (2026-10-13) - 세 화면 완료

월별과 같은 모양이고 **년도 select 2개만**(월 select 없음, 2010~올해) 다르다.
기간은 `startYear+"0101"` ~ `endYear+"1231"`(AS-IS getStartDate/getEndDate 의 year 분기).
표의 일자 칸은 **그 해의 월별 화면으로 드릴다운**한다
(`?startYear=YYYY&startMonth=01&endYear=YYYY&endMonth=12` - AS-IS 그대로).

이로써 **일자별·월별·년도별 세 화면이 모두 AS-IS 사양**이고 조각 하나
(`fragments/stats-date-list`)를 공유한다 - AS-IS가 `include/date-list.jsp` 를 공유하는 것과 같다.
`viewType` 분기도 AS-IS와 같이 year→월별 / month→일자별 / day→동작없음 세 가지다.

**발명 코드 제거**: 옛 축소판 `shop-statistics/sales-period.html`(5칸 표 + "전액 포인트결제 기준"
같은 AS-IS에 없는 문구)은 참조가 사라져 **삭제**했다. 서비스의 `periodStats()` 는 다른 화면
(답례품 구매현황 전체·결제타입별 등)이 아직 쓰고 있어 남겼다.

**검증**: 년도별 GET 진입 - 년도 select 2개·월 select 0개·년도 17개(2010~2026)·소계 N·빈 표 ·
2025~2026 검색 → 2026 한 행 · 드릴다운 클릭 → 월별 화면에 2026/01~2026/12 전달 확인 ·
**세 화면 합계가 모두 1,230,000 으로 일치**(일자별 12행 / 월별 2행 / 년도별 1행) ·
콘솔·HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

#### ★기간 기본값은 "이번 달 1일 ~ 오늘" - 게터가 만든다 (2026-10-13 2차 지적)

사용자가 "겉보기엔 다 된 것 같지만 동일하게" 재지시해서 다시 대조하니 **기간 기본값**이 달랐다.

AS-IS `StatisticsParam` 은 필드에 기본값이 없고 **게터가 만든다**:
```java
getStartDate() → 비면 DateUtils.getToday("yyyyMM") + "01"   // 이번 달 1일
getEndDate()   → 비면 DateUtils.getToday("yyyyMMdd")         // 오늘
```
JSP가 `path="startDate"` 로 바인딩하니 **입력칸도 비어 보이지 않고**, 조건 없이 검색해도
**이번 달만** 집계된다. TO-BE는 필드를 그대로 돌려줘서 입력칸이 비고 **전체 기간**이 조회됐다
(겉보기엔 멀쩡해서 놓치기 쉽다).

→ `DayStatsParam` 게터에 같은 기본값을 넣었다. 엑셀 링크의 쿼리스트링도 게터를 쓰게 바꿔
목록과 엑셀이 항상 같은 기간을 본다.

**실측**: GET 진입에 `20261001 ~ 20261013` 이 채워지고, 조건 없이 검색하면 이번 달 3일자 /
14건 / 955,000원만 나온다(이전에는 전체 기간이 나왔다).

**How to apply:** AS-IS 검색 파라미터 클래스는 **필드가 아니라 게터에 기본값을 숨겨 두는** 경우가
있다. 화면이 비어 보이지 않는데 TO-BE만 비어 있으면 게터부터 열어볼 것.

**함정**: Playwright에서 **검색 버튼을 실제로 클릭하면 `/admin/login` 으로 튕긴다** -
[[tooling-and-local-env]] 와 같은 증상이고 화면 버그가 아니다.
검증은 `document.getElementById('statisticsParam').submit()` 으로 우회한다.

#### 답례품 구매현황 전체(sales/all) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 581~675줄 + `all.jsp`(416줄) + 매퍼
`getDateStatsListGhloveQuery`. 앞의 일/월/년/결제타입 네 화면과는 **다른 쿼리/다른 부호규칙**을
쓰는 별도 화면이라 그대로 재사용할 수 없었다.

- 검색조건은 **년도(YYYY 공통코드) 하나뿐**, GET/POST 둘 다 조회한다(일자별과 달리 GET에서도
  집계한다).
- `getDateStatsListGhloveQuery` 는 **`DEVICE_TYPE` 컬럼이 없다** - 카드가 한 세트뿐인 이유.
  ★취소를 **양수**로 담아 "합계 = 주문 − 취소"다(일/월/년 화면은 취소가 음수라
  "합계 = 판매 + 취소") - 여기서만 부호 규칙이 반대다. `AllSalesCard.of()` 에서
  `Math.abs()`로 변환해 맞췄다.
- 카드 3세트(주문/취소·반품/합계) × 2(건수/금액) + 월별 바차트 2개(금액/건수, 주문·취소반품
  2계열) - `ChartCommon.drawChart` 재사용(AS-IS 로컬 `drawChart` 함수 중복 안 함).
- 기존 축소판 `ShopStatisticsTierBService.allSales()`/`AllSalesSummary` 는 제거.

**검증(여분 포트, DB 직접대조)**: 2026년 조회 → 카드 "주문 38건/1,876,000원", "취소·반품
10건/638,000원", "합계 28건/1,238,000원" · 차트 라벨 1~12, 데이터는 9월(793,000/17건,
취소 510,000/7건)·10월(1,083,000/21건, 취소 128,000/3건)에만 정상 분포 · 콘솔/HTTP 오류 0.

★`od_order` 를 직접 쿼리해 재확인(2026년 비취소 38 / 취소 10 / 전체 48)했고 화면 숫자와
**완전 일치** - 이전에 일/월/년 화면에서 검증했던 "34건"과 다른 이유는 버그가 아니라 그 사이
개발DB에 테스트 주문이 더 쌓였기 때문(최신 주문 생성시각이 검증 당일 오전이었다). 같은 화면을
시차를 두고 재검증할 때는 항상 "그때는 맞았는데 지금 다르다"가 데이터 변화인지 버그인지
먼저 DB count로 가른다.

admin `compileJava`+`test`+`bootJar` EXIT=0.

#### 지자체별 구매현황(sales/locgov) 이식 (2026-10-14) - mois/locgov 역할분기 3단 화면

AS-IS `ShopStatisticsManagerController` 675~924줄 + `sales/locgov-mois.jsp`(지자체 목록, 306줄)
+ `sales/locgov.jsp`(답례품 목록, 283줄) + `sales/detail.jsp`(회원별 상세 팝업, 88줄) + 매퍼
`getDateStatsListGhloveQuery`의 `extra="LOCGOV"/"ITEM"/"USER"` 분기. 이전 구현
(`ShopStatisticsTierBController` "M. 지자체별 구매현황")은 연도만 받는 발명된 축소판
(역할분기·WDR select·정렬·페이징·엑셀 전부 없음)이라 전면 교체했다. **별도 컨트롤러**
(`ShopStatisticsLocgovController`)로 분리했다(TierB와 매핑 경로는 그대로 `/shop-statistics/sales/locgov`
하나지만, 로직량이 커서 파일을 나눴다).

##### AS-IS 사양 - 한 URL이 역할+파라미터로 세 화면을 낸다

- `GET/POST /sales/locgov`: **mois**(ROLE_ADMIN_1~4) 이고 `locgovCode` 없음 → **지자체별 목록**
  (locgov-mois.jsp). 그 외(**locgov**(ROLE_ADMIN_5~6)이거나, mois인데 locgovCode를 골랐음) →
  **답례품별 목록**(locgov.jsp). `MenuService.isLocgovScoped(manager)`가 AS-IS 역할분기와 같다
  ([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-interest-locgov-port)에서 쓴 것 재사용).
- 지자체별 목록: WDR→LOCGOV_CODE 2단 select(캐스케이딩, 관심지자체와 같은 client-side
  `ALL_LOCGOVS` 필터 방식) + 년/월 select, **정렬가능 6칸**(주문/반품환불/합계 × 건수/금액,
  `orderBySelect`/`sort_arrow` AS-IS JS 그대로) + 페이징(10/20/50/100) + 좌측 엑셀 버튼.
  지자체명 클릭 → 같은 URL에 locgovCode를 꽂아 POST(`detail()` JS가 동적 form 생성).
- 답례품별 목록: 년/월 + 검색구분(답례품명/상호명)+검색어, 정렬 6칸 동일, 답례품명 클릭 →
  회원별 상세 **팝업**(`window.open`, 700x580). 엑셀 버튼 + **mois만** [목록] 버튼(scoped는
  목록 자체가 없다). AS-IS 주석 그대로 "개인정보 없어서 팝업 없이" - 엑셀 둘 다 사유모달 없음
  (`Shop.downloadExcelOrder(..., false)`).
- 상세 팝업: 회원별 주문/반품환불만(합계 칸 없음), 페이지당 **5건**, 정렬 UI 없음, 엑셀 없음.

##### ★취소 양수, AS-IS 쿼리가 LOCGOV/ITEM/USER 세 분기를 공유

`getDateStatsListGhloveQuery`는 sales/all과 같은 "Ghlove" 계열 쿼리라 **취소를 양수**로 담고
"합계 = 주문 − 취소"다(일/월/년 화면과 부호가 반대). 세 분기 모두 `S.COMPANY_NAME`·`I.ITEM_NAME`·
`I.ITEM_USER_CODE`·`OI.LOCGOV_CODE`를 **공통으로 항상 선택**한다(GROUP_OBJECT만 분기별로 다름) -
처음에 USER 분기 SQL에 item_name을 null로 빼놓았다가 상세팝업 제목이 비는 버그를 냈다(아래 참고).

##### ★데이터가 없는 필드 - "답례품 코드"(ITEM_USER_CODE)

TO-BE `gift.Gift` 엔티티에 AS-IS `OP_ITEM.ITEM_USER_CODE`(답례품 코드, PK인 ITEM_ID와 별개인
업무코드) 대응 컬럼이 **아예 없다**(이식 안 됨, DB 검색으로 확인). 주문방법(DEVICE_TYPE)·
결제타입과 같은 사정이라 같은 원칙으로 열 구조는 두고 값만 `"-"`로 비웠다.

##### 구현

- **order** `OrderLocgovStatsService` 신규 - `byLocgov`/`byItem`/`byUser` 세 메서드, 전부
  `ord.od_order`(locgov_code·item_id·item_name·seller_id·user_id가 이미 비정규화돼 있어
  order 혼자 집계 가능) 네이티브 쿼리. **지자체명(donation G_LOCGOV)·상호명(gift OP_SELLER)·
  회원명(member)은 다른 서비스 소유**라 order는 원시 키(`GroupRow`)만 돌려주고 admin이
  `LocgovClient`/`GiftClient.sellers()`/`MemberClient.fetchOrNull`로 각각 이름을 붙인다
  (관심지자체와 같은 cross-service enrichment 패턴, N+1 허용 - 데이터가 소량). 신규 엔드포인트
  `GET /api/admin/orders/stats/locgov-group`(extra/startYearMonth/endYearMonth/locgovCode/itemId).
  ★기간은 [[order-date-timestamp-comparison]]과 같은 원칙으로 timestamp 비교(월 범위
  `>= 시작월1일 and < 종료월+1개월1일`) - AS-IS `locgovSearchStartYearMonth+'00000000'~
  locgovSearchEndYearMonth+'99999999'` 문자열 상한을 물려받지 않는다.
- **admin** `OrderAdminClient.locgovGroupStats()` + `GroupRow` 레코드(order와 같은 모양).
  `ShopStatisticsLocgovService` 신규 - `locgovRows`/`itemRows`/`userRows`/`itemNameOf` 네
  메서드로 enrichment 담당. `ShopStatisticsLocgovController` 신규 - GET/POST 목록(역할분기),
  엑셀 2종, 상세 팝업. **정렬·필터·페이징은 메모리에서** 처리(관심지자체와 같은 패턴 - 그룹
  건수가 소량이라 서버사이드 페이징 불필요). `ShopStatisticsTierBController`/`Service`의 옛
  "M. 지자체별 구매현황"(`locgov`/`locgovDetail`/`locgovStats`/`locgovUserStats`/`LocgovStat`)은
  제거(매핑 충돌 방지 - [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-sales-day-statistics-port) 결제타입별 때 겪은 Ambiguous
  mapping과 같은 함정이라 미리 제거).
- **템플릷** `shop-statistics/sales-locgov-mois.html`(신규) · `sales-locgov.html`(전면 재작성) ·
  `sales-locgov-detail.html`(전면 재작성, AS-IS detail.jsp 그대로 팝업 레이아웃).

##### ★★CSS 함정 발견 - `<span>`으로 건수를 감싸면 "총 N건"이 세 줄로 쪼개진다

`opmanager.css:1123` `.count_title h5 span{display:block;...}`가 **h5 안의 span을 블록으로
바꾼다** - AS-IS JSP는 `<c:out>`(태그 없음)이라 문제가 없는데, Thymeleaf로 옮기며
`<span th:text="${count}">0</span>`처럼 감싸면 "총" "12" "건"이 세 줄로 쪼개져 보인다.
기존 코드 40여 화면 대부분이 `th:remove="tag"`를 붙여 렌더링 결과에서 span 자체를 지우는
식으로 이미 피해가고 있었는데(`member-admin/list.html` 등), **이 패턴을 안 쓴 화면 3곳이
이미 쇼핑통계 작업 이전부터 같은 버그 상태였다**: `statistics-locgov/list.html`(관심지자체,
2026-10-14 같은 세션 작업분)·`designated/approval-log-popup.html`·`featured-admin/list.html`.
이번에 발견해 **5곳 전부**(위 3곳 + 신규 2개) 수정했다 - 스크린샷으로 수정 전/후 비교해
"총 12건"이 한 줄로 돌아오는 것 확인.

**How to apply**: `<h5>총 <span th:text="...">...</span>건</h5>` 패턴을 새로 쓸 때마다
`th:remove="tag"`를 span에 붙일 것. `count_title` 쓰는 화면을 포팅할 때 체크리스트에 추가.

##### ★테스트데이터 함정 - 상호명 "-" 는 버그가 아니라 seller_id 불일치

검증 중 답례품별 목록의 "상호명"이 전부 `-`로 나와 처음엔 버그로 의심했다. DB 직접조회로
확인한 결과 `ord.od_order.seller_id`(501~508)와 `gift.op_seller.seller_id`(9001~9015,
9502/9504/9509)가 **전혀 겹치지 않는다** - 관리자 수기등록 주문 샘플데이터([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-order-manual-registration-port-progress))의
seller_id가 가짜값이라 실제 판매자 테이블과 매칭되지 않는 것뿐이다. 코드의 `getOrDefault(sellerId, "-")`
폴백이 의도대로 동작한 것 - 실데이터가 들어오면 정상적으로 상호명이 채워진다.

##### 검증 (Playwright, 여분 포트, 두 계정)

- **mois**(pwtest_sys): GET 목록(조회 안 함) - 지자체 2단 select·년/월 select·안내문구 확인 ·
  검색(조건없음) → 총 12건, 10+2 페이지 분할 ·  **정렬**(건수·금액 6키 × asc/desc) 실측
  일치(orderPoint desc → 360,000/320,000/225,000 내림차순 확인, 기본정렬은 지자체명 가나다순
  확인) · 지자체 클릭 drill-down → 답례품별 목록(품목 1건, [목록] 버튼 보임) · 답례품명 클릭 →
  상세 팝업(700x580, 제목 "한우 선물세트", 회원 3명, 합계 칸 없음) · 검색구분(상호명/답례품명)
  필터 정상(없는 상호명 → 0건, "한우" → 1건 정확히 매칭) · itemsPerPage 변경 정상 · 엑셀 2종
  (지자체별·답례품별) 200 응답 + 올바른 xlsx content-type.
- **locgov**(pwtest_loc, 11230): GET 진입 즉시 답례품별 목록(자기 지자체로 강제) · [목록] 버튼
  없음 확인 · 다른 지자체(26350) locgovCode를 쿼리로 직접 넣어도 **차단되어 자기 지자체 유지**
  (관심지자체와 같은 보호).
- 콘솔/HTTP 오류 0건. 스크린샷으로 세 화면(지자체목록/답례품목록/상세팝업) 전부 레이아웃·버튼
  정상 확인(위 CSS 수정 반영 후).
- order·admin `test`+`bootJar` EXIT=0.

#### 월별 지자체별 답례품 현황(sales/month-locgov) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 2332~2489줄 + `sales/month-locgov.jsp`(409줄) + 매퍼
`getMonthLocgovItemStat`(DENSE_RANK·GROUP_CONCAT 범벅의 196줄 CUBRID SQL) + `MonthLocgovStatExcel`.
이전 구현(`ShopStatisticsTierBController` "L. 월별 지자체별 답례품 현황")은 연/월만 받아 지자체
TOP3 주문금액 순위만 보여주는 축소판(7칸 카테고리 현황·WDR select·엑셀 전부 없음)이라 전면
교체했다.

##### ★처음엔 "SalesOn 미사용"으로 오판할 뻔했다 - 실은 TO-BE에 1:1 대응 체계가 이미 있었다

표 왼쪽 절반("지자체별 답례품 선정/등록 현황")은 AS-IS가 `OP_CATEGORY_GROUP.CODE`
(tour/farm/aquatic/process/daily/ticket 6종)로 품목을 분류한다. TO-BE gift 서비스의
`op_category_group`/`op_category_team`은 전혀 다른 용도(기획전/특가전)로 재사용되고 있어서
([[gift-category-tree-parity-audit]]가 말하는 "SalesOn op_category 4단은 0건·보류"와 같은
테이블명이라 처음엔 [[asis-porting-principles]] 대상으로 오판할 뻔했다.

**실제로는** `gift.Gift.categoryCode` 필드(TOUR/AGRI/SEAFOOD/PROCESSED/LIVING/VOUCHER)가
AS-IS 6종과 **1:1 대응**하고(gift 자체 `OP_COMMON_CODE` "GIFT_CATEGORY" 라벨까지
관광서비스/농축산물/수산물/가공식품/생활용품/지역상품권으로 정확히 일치, AS-IS CUBRID
`OP_CATEGORY_GROUP`과 라벨·순서 동일 확인) `GiftPublicApiController.CATEGORY_ICONS`에 이미
스토어프론트 아이콘 매핑으로 박혀 있었다 - storefront 카테고리 메가메뉴가 쓰는 **살아있는
분류체계**였다. AS-IS가 OP_ITEM_CATEGORY(품목-카테고리 다대다, 51,027건 실데이터 확인)로
하던 걸 TO-BE는 품목에 직접 category_code 하나(다대일, 더 단순)로 재설계해 둔 것뿐이었다.

**교훈**: 테이블명이 같다고 같은 기능이 아니다 - 실데이터·실제 소비 코드(`GiftPublicApiController`
같은)까지 확인해야 "미사용"과 "이미 포팅됨, 다른 구조"를 구분할 수 있다.

##### AS-IS 사양 - 표 왼쪽은 월과 무관한 카탈로그 스냅샷

- 왼쪽 7칸(합계+6카테고리)은 **"현재 승인·노출중"인 답례품 종류 수** - 선택한 년/월과
  **무관**하다(SQL을 재확인: 카테고리 카운트가 월별 주문 서브쿼리 LEFT JOIN **바깥**에서
  카탈로그 자체를 센다). 오른쪽 4칸(주문건수/금액/TOP3)만 월 필터가 걸린다. 칼럼명
  "선정/등록 현황" vs "구매 현황"의 의미 그대로였다.
- TOP1/2/3(최다판매 품목)은 **동률 허용**(AS-IS DENSE_RANK + GROUP_CONCAT) - 동률이면 이름을
  줄바꿈으로 묶고 건수는 하나만 보여준다(동률이니 값이 같다). "합계(전체)" 요약 3행 블록은
  **검색조건(지자체)으로 좁혀진 결과 안에서의 합계**이고 항상 표 맨 위에 고정으로 나온다
  (AS-IS `list[0].*TotalAll` 하드코딩 블록 재현).
- 취소 주문은 집계에서 아예 뺀다(별도 취소 열이 없다 - AS-IS `ORDER_STATUS IN (...)` 활성
  상태만 포함).
- 페이지네이션 없음(AS-IS도 itemsPerPage select가 주석처리돼 꺼져 있다).

##### 구현

- **gift** `GiftApiController.allForResync()`/`ItemSnapshotDto`에 `displayFlag` 추가(기존엔
  dataStatusCode만 있어 "승인+노출중" 전체를 못 걸렀다) - 다른 화면이 이미 쓰던 공유 DTO라
  필드만 얹었다(하위호환, 기존 호출부는 그대로).
- **order** `OrderLocgovStatsService.byItemAllLocgovs()` 신규 - [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-sales-day-statistics-port)
  지자체별(sales/locgov) 작업 때 만든 `byItem()`의 "전체 지자체 버전"(locgovCode 필터 없이
  전체를 한 번에 품목 단위로 그룹). 기존 `/api/admin/orders/stats/locgov-group` 엔드포인트에
  `extra=ITEM_ALL` 분기만 추가해 재사용했다(새 엔드포인트 안 만듦).
- **admin** `ShopStatisticsMonthLocgovService` 신규 - 카탈로그 스냅샷(`giftClient.allForResync()`
  APPROVED+Y 필터, locgov×categoryCode로 집계) + 월별 주문 통계(order API) + 동률 TOP3(Java
  Comparator 기반 dense rank, SQL 윈도우함수 대신 메모리 계산 - 데이터량이 작아 충분)를 조합.
  `ShopStatisticsMonthLocgovController` 신규 - GET/POST + 엑셀(2행 헤더 병합 + "합계(전체)"
  고정행, `ExcelDownloadWriter`의 단일헤더 헬퍼로는 부족해 POI를 직접 써서 AS-IS
  `MonthLocgovStatExcel`의 병합 구조를 그대로 재현).
  `ShopStatisticsTierBController`/`Service`의 옛 "L." 섹션(`monthLocgov`/`monthLocgovTop3`/
  `LocgovTop3Row`)과, 그것만 쓰던 `currentYear()`/`currentMonth()`(다른 호출부 없음 확인 후
  죽은코드로 같이 제거)를 삭제했다.
- **템플릿** `shop-statistics/sales-month-locgov.html` 전면 재작성 - 2행 헤더(rowspan2 지역·
  지자체명 + colspan7/colspan4 그룹) + 지자체마다 3행(rowspan) TOP1/2/3 구조.
  `th:remove="tag"`를 처음부터 넣어 [[admin-screen-pitfalls]]을 재발 없이
  피했다.

##### 검증 (Playwright, 여분 포트 3개 - admin:8099/order:8095/gift:8096)

- GET 진입: 전부 0·"데이터가 없습니다"(AS-IS GET이 `Collections.EMPTY_LIST`를 하드코딩하는 것과
  동일) · 지자체 2단 select·년(4)/월(12) select 정상.
- 검색(2026-10, 전체): 총 246건(활성 G_LOCGOV 전체 행수) · 합계(전체) 카테고리
  `23(=14+3+2+2+2+0)` - **DB 직접조회로 실측 일치**(카탈로그 AGRI 14/SEAFOOD 3/PROCESSED 2/
  LIVING 2/VOUCHER 2/TOUR 0) · 주문건수 21·금액 1,083,000 - **sales/all 10월 수치와 교차
  일치**(같은 데이터 소스이므로 당연하지만 두 독립 구현이 같은 답을 낸 것으로 교차검증) ·
  TOP1 "곡물 선물세트\n전통주 세트\n지역 특산 꿀 2병"(동률 3건, DB 확인) 정상 표시.
  서울특별시 중구 행: 주문1건/32,000원/TOP1 "지역 특산 사과 5kg"(1건) - DB와 일치.
- 지자체 필터(강남구=11230)로 좁히기: 합계(전체)가 그 지자체 수치로 그대로 수축(카테고리
  8/0/8/0/0/0/0=AGRI 8건, DB와 일치) · 표 1행으로 줄어듦.
- 엑셀 200 응답 + 올바른 xlsx content-type.
- 콘솔/HTTP 오류 0건. 스크린샷으로 레이아웃·버튼·2행 헤더 병합·3행 rowspan 전부 정상 확인.

★테스트데이터 함정: 강남구의 10월 주문 2건이 금액 0원으로 나왔는데, DB 확인 결과
`unit_price IS NULL`인 테스트 행(오늘 방금 생성된 것으로 보임)이라 `coalesce(unit_price,0)`가
의도대로 0을 돌려준 것 - 집계 로직 버그 아님.

gift·order·admin `test`+`bootJar` EXIT=0.

#### 농협 사업자 답례품 현황(sales/nh-item-sales) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 3270~3409줄 + `sales/nh-item-sales.jsp`(273줄) + 매퍼
`getNhItemSalesStats`(슬레이브 전용 `SlaveShopStatisticsMapper`, 196줄). 이전 구현
(`ShopStatisticsTierBController` "Q. 농협 사업자 답례품 현황")은 **완전히 잘못 짚었다** -
판매자 `bankName`에 "농협"이 포함되는지로 걸렀는데, AS-IS는 그게 아니다.

##### ★★"농협 사업자"의 진짜 정의 - bankName 아니라 사업자번호 매칭

AS-IS는 `G_NH_STAT_DATA_TEMP`(외부에서 받은 농협 소속 사업자번호 목록, AS-IS CUBRID 실측
**6,779건**)와 `OP_SELLER.BUSINESS_NUMBER`를 매칭해 농협 판매자를 가린다. "은행 계좌가
농협인지"와 "그 사업체가 농협 소속인지"는 전혀 다른 질문이다 - 이전 구현은 질문 자체를
잘못 세웠다.

TO-BE에 **DDL은 이미 있었다**(`database/ddl/service-admin.sql`의 `G_NH_STAT_DATA_TEMP`,
admin 스키마)인데 **테이블이 비어 있었다**(0건). AS-IS CUBRID에서 6,779건을 그대로 export해
TO-BE admin 스키마에 적재했다(엔티티 `NhStatDataTemp`/리포지토리 신규 - DDL만 있고 JPA
연결은 안 돼 있었다). 검증용으로 기존 판매자(seller_id=9002, 실제 주문 이력 있는 판매자) 한
곳의 business_number를 적재한 번호 중 하나(`101-82-13600`)로 세팅해 종단간 검증했다.

##### 품목 목록은 카탈로그 전체 - 승인·노출 필터가 없다

AS-IS 베이스 서브쿼리(`OP_ITEM LEFT JOIN OP_ITEM_CATEGORY`)에는 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-sales-day-statistics-port)
월별 지자체별 답례품 현황의 카탈로그 스냅샷과 달리 `DATA_STATUS_CODE`/`DISPLAY_FLAG` 필터가
**아예 없다** - 농협 판매자의 모든 품목(승인대기·중지·비노출 포함)이 전부 나온다. 실제로
`DISCONTINUED`/`display_flag='N'` 품목도 화면에 뜨는 게 맞다(검증에서 실측 확인).

##### ★AS-IS `ORDER_STATUS = '40'` 단일코드 - 다른 화면들의 "취소 아닌 전부"보다 좁다

일/월/년 화면군은 `ORDER_STATUS <> CANCELLED`(취소 아니면 전부)를 쓰는데, 이 화면은
`ORDER_STATUS = '40'` **딱 하나**로 좁힌다. TO-BE `order_status` 값 집합
(WAITING_DEPOSIT → CONFIRMED → CANCELLED)에서 '40'에 대응하는 값은 결제가 확인된
`CONFIRMED` 하나뿐이라고 보고 그렇게 좁혔다(입금대기는 AS-IS도 포함 안 하는 것으로 해석).

##### ★"초기화" 버튼의 AS-IS 링크는 명백한 복붙 버그 - 자기 자신으로 되돌렸다

AS-IS JSP의 초기화 버튼은 `/remittance/expected/list`(정산예정목록, 전혀 다른 화면)로
이동한다 - 다른 화면(정산/이체) 템플릿을 복붙하며 안 고친 흔적이다. 이 시리즈의 다른 모든
화면이 예외 없이 "초기화 → 자기 자신"이고, 이 링크를 그대로 두면 사용자가 엉뚱한 화면으로
튕기는 실질적 불편이라 판단해 자기 자신으로 되돌아가게 고쳤다(AS-IS 문구·레이아웃·집계
로직은 전부 verbatim, 이 한 군데만 명백한 오류로 보고 교정).

##### "하위카테고리"·"상품코드" - 데이터 없음

AS-IS `CATEGORY_DETAIL_NMS`(OP_CATEGORY 세부분류)·`ITEM_USER_CODE`는 TO-BE 답례품 모델에
대응 필드가 없다(지자체별 화면의 "답례품 코드"와 같은 사정) - "-"로 비웠다. "카테고리"
(상위, GIFT_CATEGORY 6종)는 월별 지자체별 답례품 현황과 같은 라벨 매핑을 재사용했다.

##### 구현

- **DDL** 변경 없음(테이블은 이미 있었다, 데이터만 적재).
- **admin** `NhStatDataTemp` 엔티티 + `NhStatDataTempRepository` 신규.
  `ShopStatisticsNhItemSalesService` 신규 - NH 사업자번호 집합(admin 자체 DB, 빠름) +
  `giftClient.sellers()`(businessNumber 매칭으로 NH 판매자 추출) + `giftClient.allForResync()`
  (그 판매자들의 전체 품목, 상태 필터 없음) + 주문 통계(order 신규 API)를 조합. 검색구분
  (사업자번호/아이디/상호명/답례품명)·WDR 지자체 필터·페이징(10/50/100/200/500, 다른 화면과
  다른 증가폭)은 메모리에서 처리(관심지자체·지자체별과 같은 패턴).
  `ShopStatisticsNhItemSalesController` 신규 - GET/POST + 엑셀(사유모달 없음, AS-IS
  `Shop.downloadExcelOrder(..., false)` 그대로).
  `ShopStatisticsTierBController`/`Service`의 옛 "Q." 섹션(`nhItemSales()`, bankName 휴리스틱)을
  제거했다.
- **order** `OrderLocgovStatsService.nhItemSaleStats()` 신규 - `ORDER_STATUS='CONFIRMED'`
  단일조건으로 품목별 주문건수/판매수량(SUM(QUANTITY))/판매액을 집계. 새 엔드포인트
  `GET /api/admin/orders/stats/nh-item`.
- **템플릿** `shop-statistics/sales-nh-item-sales.html` 전면 재작성. `th:remove="tag"`를
  처음부터 넣어 [[admin-screen-pitfalls]]을 재발 없이 피했다.

##### 검증 (Playwright, 여분 포트 3개 - admin:8099/order:8095/gift:8096)

- GET 진입: 전부 0·"데이터가 없습니다" · 지자체 2단 select·검색구분 5종·년(4)/월(12)·
  출력수(10/50/100/200/500) 전부 정상.
- 검색(2026-09, 전체): 총 5건 - seller_id=9002(NH 매칭) 소속 품목 5개 전부 노출(DISCONTINUED·
  display=N 포함 - 상태필터 없음 확인) · 카테고리 라벨(AGRI→농축산물, SEAFOOD→수산물) 정상 ·
  "해운대 건어물 세트" 주문1/판매1/35,000원, "선물포장 상자" 주문1/판매4/0원 - **DB 직접조회와
  정확히 일치**(두 품목만 실제 9월 CONFIRMED 주문 있음, 나머지 3품목은 0으로 올바르게 채워짐).
- 검색구분(상호명) 없는 값 → 0건 · 사업자번호(101-82-13600) 정확매칭 → 동일 5건(필터 정상
  동작, 데이터가 단일 판매자뿐이라 체感은 약함).
- itemsPerPage 변경 정상 · 엑셀 200 응답 + 올바른 xlsx content-type.
- 콘솔/HTTP 오류 0건. 스크린샷으로 레이아웃·버튼 정상 확인(13칸 표, 페이지네이션 조각 정상).

gift·order·admin `test`+`bootJar` EXIT=0.

#### ★엑셀 No 컬럼 방향 버그 수정 (2026-10-14, 사용자 지적)

농협 사업자 답례품 현황 엑셀을 사용자가 스크린샷으로 화면과 대조해 **No 컬럼이 반대 방향**
(화면은 5→1 내림차순, 엑셀은 1→5 오름차순)임을 지적했다. 확인해보니 지자체별(sales/locgov)
엑셀 2개도 같은 실수였다(`int no = 0; ... ++no`로 짜서 오름차순이 되어 있었다) - 3곳 모두
AS-IS 엑셀 View 소스(`ShopStatisticsMoisExcelView`/`ShopStatisticsLocgovExcelView`는
`length-(rowIndex-2)` 내림차순, 농협은 컨트롤러의 `idx--` 내림차순)를 다시 대조해 전부
`no--`(내림차순)로 고쳤다. **단 관심지자체 엑셀(`StatisticsLocgovExcelView`)은 그대로 뒀다** -
AS-IS 자체가 `++index`(오름차순)라 화면(내림차순)과 원래 다르다. 자세한 내용과 재발 방지
절차는 [[admin-screen-pitfalls]] 참고.

POI로 복호화해 수정 후 셀 값 직접 대조(지자체별 목록 12→11→10→9→8, 농협 5→4→3→2→1) -
전부 화면 번호와 일치 확인. admin `test`+`bootJar` EXIT=0.

#### 카테고리별(sales/categories) 이식 (2026-10-14)

AS-IS `salesCategories`(1599~1662줄) + `categories.jsp` + 슬레이브 매퍼 `getCategoriesStatsList`.
이전 구현(TierB "H.")은 카테고리코드별 건수/금액 한 줄짜리 축소판이라 교체. `/sales/category`
(AS-IS 미사용 엑셀 전용 별개 화면)의 redirect만 남겼다.

- 사양: 기간(startDate~endDate, 기본 이번달1일~오늘, 날짜버튼 + "전체"는 ROLE_ADMIN_1/5만) +
  지자체 select(adminRole=SYS만, 지자체담당자는 숨김+자기 지자체 고정). 표는 지자체 × 6카테고리
  (수산물/농축산물/지역상품권/생활용품/가공식품/관광서비스) 건수·금액 + 총계. **엑셀·페이지네이션
  없음**(AS-IS도 없다). GET은 조회 안 함. 숫자는 c:out 그대로라 천단위 콤마 없음. No.는 오름차순.
- AS-IS `ORDER_STATUS='40'`→`CONFIRMED`, 카테고리는 `gift.Gift.categoryCode`(AS-IS 6종과 1:1).
  카테고리 없는/지자체 null/gift 카탈로그에 없는 품목은 AS-IS 내부조인처럼 제외. 정렬은 지자체명.
- 구현: order `OrderLocgovStatsService.locgovItemQuantityStats`(일자 범위·지자체×품목) +
  `GET /api/admin/orders/stats/category-item`, admin `ShopStatisticsCategoriesService/Controller`
  신규, 템플릿 재작성(`th:remove="tag"` 적용).
- 검증: 2026-09-01~10-31 → 4행(강남구 농축산 2/160000, 제주 도청 상품권 2/60000+생활용품
  5/25000, 부여군 농축산 1/28000, 해운대 수산물 1/35000) - DB 직접 집계와 일치. 지자체 필터
  정상. 콘솔/HTTP 오류 0. ★지자체담당자 화면은 메뉴권한표상 ROLE_ADMIN_5만 허용인데 테스트계정
  pwtest_loc는 ROLE_ADMIN_6이라 접근거부(AS-IS 권한표대로) - **ROLE_ADMIN_5 계정이 없어
  LOC 분기(select 숨김·자기지자체 고정)는 미검증**.

#### 답례품 선호도(shop-statistics/wish/list) 이식 (2026-10-14)

AS-IS `wishList` GET/POST + `wish/list.jsp` + `getOpWishlistList` + `ShopWishMoisExcelView`/`ShopWishLocgovExcelView`.
이전 구현(TierB "N.")은 상품ID·찜건수 한 표짜리 축소판이라 교체. order 연동 없이 admin이 gift(찜·품목·판매자)+
donation(지자체)를 조합(`ShopStatisticsWishService/Controller` 신규).

- 사양: 찜 행을 품목별로 COUNT → "등록수" 내림차순. 지자체 필터(locgovCode 우선, 없으면 upperLocgovCode)·답례품명
  부분일치·페이징(10/20/50/100, 메모리). mois(ROLE_ADMIN_1~4)는 지자체 2단 select + "지자체" 열, 지자체담당자는
  자기 지자체명(상위명+시군구명을 **공백 없이** 붙임) 고정 + 지자체 열 없음. GET은 조회 안 함. OP_ITEM·OP_SELLER·
  G_LOCGOV 내부조인이라 셋 중 하나라도 없는 품목은 제외(TO-BE는 품목 자신의 locgovCode 사용).
- ★엑셀 둘이 **서로 다르다**(AS-IS 그대로): mois = No. 오름차순·6열(지자체 포함)·시트 제목행 문구 "답례품 구매현황"
  (복붙 흔적)·파일명 "답례품 선호도_…" / locgov = No. 내림차순·5열·제목 "답례품 선호도"·파일명 "답례품선호도_…".
  금액은 숫자셀(#,##0). 둘 다 사유모달 없음. [[admin-screen-pitfalls]] 재확인.
- "상품코드(ITEM_CODE)" 없음 → 답례품명 링크는 AS-IS 프론트 `/items/details.html?code=` 대신 storefront
  `http://localhost:5173/gifts/{itemId}` 새창(설문 미리보기와 같은 방식).
- 검증: 찜 9품목(강진 머그컵 2건 선두 등) DB와 일치, 이름/지자체 필터·선택값 유지·페이지수 변경 정상, 두 엑셀 POI
  복호화로 방향·열·제목 확인, 콘솔/HTTP 오류 0, 스크린샷 정상. ★지자체담당자(ROLE_ADMIN_5/6) 분기는 테스트
  계정 한계로 미검증(이 메뉴 권한을 가진 5번 계정 없음).

### admin-report-general-port

> 통계>보고서>총괄 현황(shop-statistics/report/general) AS-IS 이식 - 사전집계 통계표 14종을 AS-IS 이름 그대로 신설·적재, 탭4개 화면+14시트 엑셀. 통계 배치는 TO-BE에 없음


2026-10-15 이식. 이전 구현(연도 입력 하나·탭 없음·엑셀 없음, 회원/기부/답례품 원장 즉석집계)은 AS-IS에 없는 발명이라 통째로 교체
(`ReportStatisticsController.general`·`ReportStatisticsService`의 총괄 전용 메서드 삭제. 총괄누계/지자체별/연간 화면은 아직 옛 구현 그대로).

- ★AS-IS는 즉석 집계가 아니라 **통계 배치가 미리 적재한 표를 SELECT만** 한다. 화면은 MBR_TNOCS_STATS·DNTN_TNOCS_STATS·
  DNTN_TNOCS_MAX_AMT_STATS·GDS_TNOCS_STATS 4종, 엑셀은 14종(+MCTPV_*·LCLGV_*·DNTN_PATH/AMT/AGE_TNOCS·HAB_DNTN_MCTPV).
  14종 전부 admin 스키마에 AS-IS 이름·컬럼 그대로 만들고(database/ddl/service-admin.sql) AS-IS CUBRID 2,066행을 적재
  (database/ddl/migration-admin-report-stats-asis-load.sql, 개발DB 적용 완료). 다른 보고서 화면(총괄누계·지자체별·연간)도 같은 표를 쓰니 이식할 때 재사용.
- ★이 표들을 채우는 AS-IS 배치(`setStatisticsReport`, 연령배치 birthdayBatch)는 TO-BE에 없다 - 2026-06 이후 데이터는 안 늘어난다
  ([[batch-job-port-decisions]]). AS-IS 화면의 연령배치·통계배치·연통계 버튼은 주석처리라 안 켰다.
- 화면: 탭 4개(회원 수/기부건수/기부건수(2000만원)/답례품건수)를 한 페이지에 다 그리고 JS가 전환. **GET은 표 비움, POST에서 채움**.
  ★GET 직후 년도 select가 빈칸인 건 AS-IS JS 그대로(탭 클릭 때 monthChange(undefined)가 select를 비움) - 버그 아님. 탭을 누른 뒤 년도를 골라야 한다.
  월 select는 display:none(직전월까지만 조회). 검색 onclick `search()`는 AS-IS도 미정의 함수라 옮기지 않음.
- ★"엑셀" 버튼은 `report/lclgv/excel-download`(지자체별 화면과 같은 주소)를 부른다. 암호화 없음(AbstractSXSSFExcelView 계열 =
  AsisSxssfExcelView). 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체로 고정+시트 3장, 그 외 14장. 전년대비 칸은 화면(op:numberFormat, 천단위·"%")과
  엑셀(`%.2f`+"%" 문자열, 콤마 없음)이 서로 다르게 찍힌다 - AS-IS 그대로.
- 구현: `ShopStatisticsReportGeneralService`(native Tuple→Map, 월 컬럼 접미사 jan~dec)·`ShopStatisticsReportGeneralController`·
  `web/support/ShopStatisticsReportExcelView`·`templates/shop-statistics/report-general.html`(JS는 data 속성으로 값 전달).
- 검증: Playwright로 GET/탭4/검색/초기화/엑셀, 2025 서울 130.66%·회원 27,539 등 DB와 일치, 엑셀 14시트 POI로 확인,
  지자체담당자 3시트 확인(pwtest_loc 11230은 통계표에 없는 지자체라 0행, 메뉴 권한은 직접 URL로 우회해 확인).
- admin 재기동 필요(+DB에 DDL·적재 반영).

#### 총괄 누계 현황(report/general-total) 이식 - 2026-10-15
같은 14종 통계표를 재사용(DNTN_PATH/AMT/AGE_TNOCS_STATS·HAB_DNTN_MCTPV_TNOCS_STATS). 이전 원장 즉석집계 4표 구현 교체
(`ReportStatisticsController.generalTotal`·`ReportStatisticsService` 누계 전용 메서드 삭제). 구현: `ShopStatisticsReportGeneralTotalController`,
`ShopStatisticsReportGeneralService.generalTotal()`, `report-general-total.html`. 메뉴 URL은 TO-BE가 kebab(`general-total`, AS-IS는 generalTotal) - 그대로 둠.
- 총괄 현황과 같은 틀(탭4·GET 빈 표·POST 채움·"엑셀"=lclgv/excel-download 14시트)이되 **월 select가 화면에 보이고**, 끝에 monthChange 호출이 없다.
- ★AS-IS JS 그대로의 비대칭: 3번 탭(연령별)은 단위 캡션을 "기부방법별" div로 켠다(연령별 캡션 div는 어떤 탭에서도 안 켜짐, 4번 탭은 캡션 없음).
- ★거주지역->기부지역 표만 값에 콤마 없음(c:out). 금액별/연령별은 JSP의 코드별 c:if 사슬이라 정의 코드만 칸이 나온다('00'은 라벨+값 2칸).
- ★개발 데이터에 연령 코드 '50'이 40대·50대 둘 다 들어 있어(AS-IS 데이터 결함) 정렬이 모호 -> `order by dntn_age_cd, dntn_age`로 고정(화면·엑셀 모두).
- 데이터가 2026-05까지라 2026/6~9를 검색하면 빈 표(그리고 년도 select가 빈칸으로 돌아감 - crtrYr undefined, AS-IS 동작).
- admin 재기동 필요.

#### 지자체별 누계 현황(report/mctpv) 이식 - 2026-10-15
MCTPV_DNTN_STATS·MCTPV_DNTN_MAX_AMT_STATS·MCTPV_GDS_STATS 3표 재사용. 이전 구현(연도 입력·원장 즉석집계) 교체
(`ReportStatisticsController.mctpv` 삭제 - 같은 컨트롤러의 `lclgv`와 `report-locgov.html`은 아직 옛 구현, 지자체 현황 이식 때 교체).
구현: `ShopStatisticsReportMctpvController`, `ShopStatisticsReportGeneralService.mctpvRows()`, `report-mctpv.html`.
- 총괄 누계와 같은 틀(탭 3개, 기본 탭 3번, 월 select 보임, GET 빈 표, 엑셀=lclgv/excel-download). crtrYr은 세 탭 모두 "기부금 누계 표 마지막 행 기준년".
- 값은 원→백만원 반올림(`(int)Math.round(원/1e6)`, StatisticsReport.getNowYrMJan). 세 표 모두 캡션 "(단위: 백만원, %)"(답례품 표도).
- ★2000만원 탭(MAX_AMT)만 값 0과 전년대비 '0'을 "-"로 찍는다(JSP에 `== 0` 분기가 24+12곳). 다른 두 탭은 0 그대로. 월 구간 정합은 awk로 전수 확인.
- admin 재기동 필요.

#### 지자체 현황(report/lclgv) 이식 - 2026-10-15
LCLGV_AOD_STATS·LCLGV_AOD_MAX_AMT_STATS·LCLGV_GDS_STATS 3표 재사용. 옛 구현(연도 입력·원장 즉석집계)과 공유 템플릿 `report-locgov.html` 삭제,
`ReportStatisticsController`는 이제 연간통계(`year`)만 남음(다음 이식 대상). 구현: `ShopStatisticsReportLclgvController`(GET/POST + `options-by-lclgv` JSON),
`ShopStatisticsReportGeneralService.lclgvRows/mctpvOptions/lclgvOptions/upperLocgovCode`, `report-lclgv.html`.
- 지자체별 누계와 같은 틀 + **시/도 → 시/군/구 2단 select**(시/도 선택 시 `POST /shop-statistics/report/lclgv/options-by-lclgv`, 3번 탭은 답례품 표·그 외 기부 표 기준). 월 select는 숨김.
- 표는 높이 700px 스크롤 박스에 시/도·지자체 두 열 sticky. 단위 천원(`(int)Math.round(원/1000)`). 기부금 두 탭만 0→"-"(값·전년대비), 답례품 탭은 0 그대로.
- ★탭 JS의 "마지막 행" 출처가 탭마다 다르다(1·3번=기부금 표, 2번=답례품 표). 1·2번 탭은 AS-IS 오타(shLclgv가 비면 #shMctpv를 비움, 아니면 #shLclgv에 시/도 코드를 넣음)가 있지만 바로 뒤 searchSigungu가 복구해 결과 동일 - 그대로 옮김.
- ★지자체 담당자(ROLE_ADMIN_5/6)는 select 대신 소속 이름 텍스트, 조회·엑셀 범위가 소속으로 고정(광역이면 시/도만, 기초면 시/도+지자체). 엑셀은 이제 화면에서 고른 시/도·지자체 조건을 시트 5~10(+기부/답례품 지자체 시트)에 그대로 적용(`ReportParam.shMctpv/shLclgv`).
- ★**코드 불일치 주의**: 통계표(AS-IS 개발DB 적재)의 지자체 코드는 옛 체계(인천 중구 28110, 동구 28140 …)인데 TO-BE donation 지자체 목록은 개편 코드(인천 28125·28155 …)라 겹치는 게 거의 없다(순천시 46150 정도). 그래서 지자체 담당자 계정의 locgov_code가 통계표에 없으면 빈 표가 나온다(TO-BE 통계 배치가 생기면 TO-BE 코드로 쌓여 해소). 검증은 pwtest_loc의 locgov_code를 46150으로 잠시 바꿔 했고 11230으로 되돌림.
- admin 재기동 필요.

#### 연간통계 현황(report/year) 이식 - 2026-10-15 (보고서 5화면 전부 완료)
옛 구현(최근 5년 연도별 건수·금액 표)과 `ReportStatisticsController/Service`·`report-year.html` 전부 삭제(보고서 스위트의 옛 구현 파일 제로).
구현: `ShopStatisticsReportYearController`(GET 화면 + `lclgv/excel-pastYearStatsStored`), `ShopStatisticsReportYearService`(CUBRID→PG 변환 쿼리),
`web/support/ShopStatisticsStoredPastYearExcelView`, `report-year.html`.
- ★화면에는 **표시되는 데이터가 없다**: 년도(YYYY 공통코드 중 올해 이전·2022/2023 제외)+시/도+시/군/구 select와 "엑셀"·"초기화"뿐이다. 엑셀이 그 지자체의 연간 집계 시트 8장(총괄·기부방법별·기부금액별·기부연령별·주소지 광역/전체·월별·답례품 인기순).
- 표 8종 신설·적재(AS-IS 이름 그대로): YEAR_SUM/PATH/PRICE/AGE/PSINT/MONTH/GOODS_AMT_TNOCS + STAT_LOCGOV(통계용 지자체 243건). 1,507행, `migration-admin-report-year-stats-asis-load.sql`. ★AS-IS 개발DB에서도 연간표는 표마다 **정확히 200행**(일부 지자체만)만 들어 있어, 지자체에 따라 시트가 비어 나온다(예: 2025 순천시는 총괄·방법별·연령별만 값, 주소지·월별·답례품은 빈 시트). 이 표를 채우는 연통계 배치(statisticsYearBatch)는 TO-BE에 없음.
- 엑셀 버튼: 시/도·시/군/구가 비면 alert(상위 지자체를 선택해 주세요 / 지자체을 선택해 주세요) 후 중단. 시/도를 고른 뒤 시/군/구 select는 AS-IS가 tabId를 안 넘겨 기부(AOD) 통계표 기준 목록이 채워진다. 지자체 담당자는 select 없이 소속 이름만 보이고 서버가 소속으로 덮어쓴다(광역이면 시/도=지자체=광역 코드).
- CUBRID→PG 변환: decode→CASE, nvl→coalesce, 문자열 컬럼 SUM→`sum(cast(.. as numeric))`, 정렬 불명 쿼리는 `order by ctid`(등록일시 순 쿼리는 `frst_reg_dt, ctid`)로 적재 순서 고정. 주소지 광역 비율은 `trunc(cast(.. as numeric(5,1)),1)`.
- 지자체 칸 이름 규칙은 `ShopStatisticsReportGeneralService.scope()`로 공유(지자체 현황·연간통계). `search()`처럼 AS-IS 미정의 함수는 이 화면에 없다(monthChange는 #shMonth가 없어 AS-IS도 no-op이라 verbatim 유지, 연통계집계-적재 버튼은 주석처리라 안 켬).
- admin 재기동 필요 + DDL·적재 SQL 적용 필요(개발DB는 적용 완료).

#### ★정정(2026-10-15 사용자 지적) - 첫 진입 기본값 4화면(총괄·총괄누계·지자체별누계·지자체)
위 본문의 "기본 탭은 4번/3번"·"GET 직후 년도 select가 빈칸인 건 AS-IS JS 그대로"는 **틀린 서술**이다. 원인 두 가지:
1. AS-IS `StatisticsParam.getTabId()`는 **비어 있으면 "1"을 돌려준다**(필드는 null이지만 getter 기본값). 컨트롤러·JSP 모두 getter를 쓰므로 첫 진입 탭은 **항상 1번**(회원 수 / 기부방법별 / 지자체별 기부금 누계 / 지자체별 기부금). 필드(null)만 보고 "else → 마지막 탭"으로 읽었다. → `ReportParam.getTabId()`에 같은 기본값. ★JSP/JS가 쓰는 값은 필드가 아니라 getter 반환값인지 항상 확인할 것(StatisticsParam처럼 getter에 기본값 로직이 있는 DTO 주의).
2. 첫 진입에는 조회된 행이 없어 탭 JS의 `crtrYr`(마지막 행의 기준년)이 undefined → `monthChange(undefined, …)`가 년도 select를 비우고 월을 12로 만든다. AS-IS 컨트롤러가 직전월(shYear/shMonth)을 계산해 모델에 담는 의도와 맞지 않는 결과라, 행이 없으면 서버가 정한 년도(`data-search-year`)로 대체하게 했다(템플릿 4곳의 crtrYr 헬퍼). 결과: 첫 진입 = 1번 탭 + 직전월(오늘 2026-10 → 2026년 9월), 데이터 없는 달을 검색해도 년도가 유지됨. 숨김 월 select도 9라 엑셀이 12개월 시트로 나오던 것도 해소.

### admin-dashboard-day-port

> 통계>보고서>일일 현황보고(shop-statistics/dashboard/day) AS-IS 이식 - 표 3개는 AS-IS도 빈 틀, 실기능은 행안부 일일/주간보고 JSON→문안·엑셀·복사 버튼


2026-10-14 이식. 이전 TierB 구현(결제건수·포인트·취소·신규가입 표)은 AS-IS에 없는 발명이라 통째로 교체
(`dashboardDay`·`dashboardDayReport` 제거, 월별 `dashboard/month`는 유지).

- AS-IS 화면 자체가 거의 빈 틀: 표 3개는 모델에 없는 `${total}`을 읽어 값 칸이 전부 공백, "2024년/2023년" 하드코딩,
  차트 canvas는 그리지도 않음, 검색 onclick `fnSearch()`는 정의 없음(옮기지 않음). 그대로 비워 둠.
- 실제 기능은 버튼: 초기화 / 검색(POST) / 엑셀(`Shop.downloadExcelOrder(url, startDate, false)` → 키 없는 `?20261014`
  쿼리스트링, 양식 틀+신청건수·판매액 "0"만) / 행안부 일일보고·주간보고(`daily.report.js`가 `/report/daily/{date}`,
  `/{start}/{end}` JSON을 받아 문안 조립 → textarea) / 복사.
- ★종료일 input은 `id="endDate"` 필수 - daily.report.js 생성자가 전역 `endDate`를 읽음(AS-IS는 form:input이 id 자동부여).
- ★daily.report.js의 fetch URL `/opmanager/shop-statistics/...` → `/shop-statistics/...`로 TO-BE 사본만 패치(admin에 /opmanager 매핑 없음).
- 구현: admin `ShopDashboardDayController`(화면·JSON·엑셀)·`DailyReportService`(조립), donation/point/member 각
  `ReportDailyAdminApiController`(`/api/admin/report/daily-*` 집계), admin `DonationClient/PointClient/MemberClient` 메서드 추가.
- 검증(2026-09-30 기준): 누계 31건/22,482,000원, 일반(온라인)21건·지정5건·오프라인5건/110,000, 답례품 신청 13건/461,000P,
  회원 39(일일)/45(주간 전체) 모두 DB 직접조회와 일치. 엑셀 POI 복호화로 양식 확인.
- 근사/공백: `itemRegCount`=지자체 있는 gift 품목 수(AS-IS giftA+B 근사), `admin.op_call` 비어 있어 콜수 0, 민간플랫폼(LINK_INSTT_CD) 데이터 없어 0.
- admin+donation+point+member 재기동 필요. 커밋 안 함(요청 시에만).

### admin-foreigner-give-stat-all-port

> 통계>외국인 기부 현황>전체(6711) 이식 - 기부금 모금현황 전체 화면에 foreignerFlag 분기, 메뉴 URL 누락이 원인, AS-IS 검색(POST) 405 결함 해소


2026-10-15 이식("누락된 메뉴"). 6711 메뉴 행은 TO-BE에 있었지만 `menu_url`이 NULL이라 눌러도 아무 화면도 안 열렸다
(6706 일반 전체는 `/give-statistics/all`). 6712(지자체별)도 같은 상태이고 **아직 미이식**(URL 그대로 NULL, LNB에서도 안 보임).

- AS-IS는 별도 화면이 아니라 `give/statistics/all/detail.jsp`를 `searchParam.foreignerFlag == 'Y'`로 재사용한다. 다른 점: 카드 3개·월별 차트 4개 제목에
  "외국인 " 접두어, 시간대별 차트 블록은 숨김 빈 div로 대체, 페이지 제목은 JS가 "외국인 기부 현황 (전체)"로 덮어씀, ajax URL이 `foreigner/all/`,
  집계 쿼리에 `GC.FOREIGN_STATUS_CODE != '0'`. 구현: `GiveStatisticsController.foreignerAll/…`(+`allView` 공통화), `give-statistics-all.html`의
  `foreigner` 분기(JS는 폼의 hidden foreignerFlag를 읽음), donation `GiveStatRepository/Service/GiveStatAdminApiController`에 `foreigner` 인자,
  `GiveStatClient` 3메서드에 인자 추가. 메뉴는 `migration-admin-menu-6711-foreigner-all.sql`(개발DB 적용 완료).
- ★AS-IS 결함 둘(그대로 안 옮김): ① `foreigner/all`에는 **POST 매핑이 없어** 검색 버튼이 405 - 요약 카드는 POST(`total`)에서만 채워지므로 AS-IS 외국인 화면은 카드가 영영 안 채워진다. TO-BE는 일반 화면처럼 GET(년도 파라미터)에서 요약을 계산한다. ② 외국인 나이대 ajax(`foreigner/all/{year}/age`)는 locgovCode 자리에 년도를 넣는 오버로드 실수가 있는데 UI에서 호출하지 않는 죽은 코드(차트 주석처리).
  또 `init()`이 `#giveDate` 없이도 `statisticsAllByTime()`을 불러 `foreigner/all/undefined/hour`를 POST한다 - AS-IS 동작이라 그대로(빈 24행 응답, 그리지 않음).
- ★데이터: TO-BE `donation.g_cntr.foreign_status_code` 컬럼은 있고(엔티티 미매핑, 기본 '0') 개발DB 47건 전부 '0'이라 외국인 화면은 0으로 나온다.
  AS-IS 실데이터는 코드 '1'(1,761건)·'3'(1,175건)가 외국인이다. 값을 세팅하는 쪽(실명인증 결과 반영)은 TO-BE에 없다. 검증은 기부 3건을 잠시 '1'/'3'으로 바꿔
  (1,510,000원·3명·3건) 확인하고 전부 '0'으로 되돌림.
- 관련: [[admin-screen-pitfalls]](같은 화면의 년도 select·0 채움 규칙 공유).
- admin+donation 재기동 필요.

#### 외국인 기부 현황 > 지자체별(6712) + 지자체 상세 이식 - 2026-10-15
6712도 `menu_url` NULL("누락된 메뉴")이라 `migration-admin-menu-6712-foreigner-locgov.sql`로 `/give-statistics/foreigner/locgov` 설정(개발DB 적용). 권한은 AS-IS와 같이 ROLE_ADMIN_1~6.
- 일반 지자체별 목록(6707)과 같은 템플릿·핸들러를 `foreigner` 플래그로 공유(`GiveStatisticsController.locgovList(foreigner,…)`, `give-statistics-locgov.html`): 제목 "외국인 기부 현황 (지자체)"(AS-IS는 일반도 JS로 "기부금 모금현황"을 덮어써서 TO-BE 일반 화면 제목도 그에 맞춤),
  초기화/검색 URL, hidden foreignerFlag(엑셀 URL에도 실림 - `locgov/excel`이 `foreignerFlag`를 받음), 집계 `GC.FOREIGN_STATUS_CODE != '0'`(donation `locgovList`에 `foreigner` 인자).
- ★**지자체 상세 신설**(`give-statistics-locgov-detail.html`, `/give-statistics/[foreigner/]locgov/{code}` + `/{year}/month`): 지자체명 링크의 도착지로 AS-IS `locgov/detail.jsp`(요약 3카드+월별 4차트, 시간대별 없음, "목록" 버튼). 일반 지자체별에도 쓰인다 - TO-BE 일반 목록에서 "보류"였던 드릴다운이 이로써 해소.
  링크 규칙은 AS-IS 그대로: 일반=그 행의 년도, 외국인=선택된 년도(GET `?shCntrYear=`). AS-IS 상세는 GET에서 카드 미계산이지만 TO-BE는 계산(전체 화면과 같은 이유).
- ★지자체 담당자(ROLE_ADMIN_5/6): AS-IS처럼 **목록 없이 자기 지자체 상세로 redirect**(일반 목록도 같은 규칙으로 바뀜 - 예전 TO-BE는 목록을 자기 지자체로 필터해 보여줬다). 다른 지자체 코드로 접근하면 "등록된 지자체만 이용할 수 있습니다." alert 후 자기 상세, 소속이 없으면 "지자체 등록 후 이용할 수 있습니다." 후 홈. 상세에서는 "목록" 버튼이 숨는다.
- 검증: 개발데이터 외국인 4건을 잠시 '1'/'3'으로 바꿔(순천 2건 25,000·곡성·해운대) 목록 3행·정렬(금액)·링크→상세 카드 25,000/2/2·년도 검색·목록 버튼·엑셀(암호화, No 오름차순, 금액순) 확인, pwtest_loc의 locgov_code를 46150으로 잠시 바꿔 redirect·alert 확인 후 전부 원복.
- admin+donation 재기동 필요.

### admin-visit-stats-port

> 통계>접속통계(stats/visit/index) 신규 이식 - OP_VISIT_COUNT 1,096행 적재, 메뉴 URL을 AS-IS 값으로 교정, 접속 수집 쪽은 아직 없음


2026-10-14 이식. 화면이 아예 없었다(메뉴는 있는데 라우트 없음). AS-IS `StatsManagerController#visit/visitPost/visitDay` +
`stats/visit/index.jsp`/`day.jsp` + `stats-mapper.xml`(getVisitSummary·getMonthStatsList·getDayStatsList).

- 사양: 요약 6칸(전체/오늘/어제/월별최대/일별최대/일별최소) · 검색기간(시작년~종료년, 2013~올해) · 월별 가로막대(월 이름
  클릭 → `/stats/visit/day` ajax로 그 달 일별 막대만 교체) · 이번 달 일별 세로막대(기록 없는 날 0으로 채움, 퍼센트는 소수
  2자리). **GET은 막대를 비우고 POST에서 채운다**(AS-IS 빈 목록 하드코딩). 구현: `VisitStatsService`(native 집계)·
  `VisitStatsController`·`templates/stats/visit.html`·`visit-day.html`(조각).
- ★메뉴 URL 데이터 오류 교정: TO-BE `op_menu` 6101이 `/stats`였는데 AS-IS는 `/opmanager/stats/visit/index`다 - 접두어
  매칭 기준이 달라 라우트와 안 맞았다. 개발DB에서 `/stats/visit/index`로 UPDATE(다른 환경은 같은 교정 필요).
- ★데이터 적재: TO-BE `admin.op_visit_count`가 0건이라 AS-IS CUBRID 1,096행(2023-01-01~2026-06-09, 합계 10,285,133)을
  그대로 적재. 접속 상세 `OP_VISIT`(1,028만건·IP 포함)는 이 화면이 안 써서 적재 안 함.
- ★**접속을 이 표에 쌓는 쪽이 TO-BE에 없다**(AS-IS `saveVisitData` 인터셉터+쿠키 IS_VISITED) - 오늘/어제는 계속 0이고
  2026-06-09 이후 데이터는 안 늘어난다. storefront 접속 수집은 별도 과제(결정 필요).
- AS-IS 월별 막대에 `</c:choose>ß` 오타(화면에 "ß" 한 글자가 찍힘)가 있어 옮기지 않았다. 엑셀(`visit/download-excel`)은
  ROLE_EXCEL(판매자 전용) 버튼이라 운영자에겐 안 보여 만들지 않았다. `/stats/referer`(방문자접속경로, 6102)는 TO-BE 메뉴에
  없어 별도(미요청).
- `line_chart.png` 404는 AS-IS 저장소에도 없는 이미지라 AS-IS와 같다.
- 검증: 요약값·2025~2026 월별 18행(비율 합 100)·6월 일별(6/9=3건=42.86%) 확인, 콘솔 오류는 위 404뿐, 레이아웃 정상.

### admin-interest-locgov-port

> 통계>관심지자체(statistics/interest-locgov) AS-IS 전면 재구현 - 이전 구현은 텍스트검색·"전체연도"·발명된 막대그래프 등 구조가 전혀 달랐다


### 통계 > 관심 지자체 (AS-IS `/opmanager/statistics/locgov/like`)

2026-10-14 이식. TO-BE에 있던 구현은 **발명된 축소판**이었다 - 목록·상세를 하나의
`ListResult`(지자체별+월별 합침)로 합치고, 텍스트검색 "지자체명 검색"·"전체 연도" 옵션·
div 막대그래프(`background:#eee; width:200px`) 같은 AS-IS에 전혀 없는 UI를 썼다.
AS-IS 소스(컨트롤러 219줄 + list.jsp 177줄 + detail.jsp 130줄 + ServiceImpl + 매퍼 XML)를
전수 대조해 다시 만들었다.

#### AS-IS 사양

**목록(`like`)**
- 지자체 2단 select(WDR→LOCGOV_CODE 캐스케이딩, 텍스트검색 아님) · 초기화/검색 버튼
- `count_title`(총 N건) + `itemsPerPage` select(10/20/50/100, AS-IS 공용 패턴)
- 표 3칸: No./지자체("광역명 시군구명")/등록수, **정렬은 등록수 DESC**
- 지자체명 클릭 → 그 지자체 상세로 이동(연도 무관, AS-IS는 ajax 아니라 `location.href`)
- 좌측 엑셀 버튼(`Shop.downloadExcelOrder(..., true)` = 개인정보 사유 모달 경유)
- 표준 페이지네이션 조각
- ★목록 쿼리(`getLocgovLikeList`)에는 **연도 조건이 없다** - 전체 기간 누적. 연도 select는
  상세 화면에만 있다.
- ★**지자체담당자(scoped)는 목록 자체를 못 본다** - GET/POST 진입 즉시 자기 지자체 상세로
  리다이렉트(`findAdminRoleAndLocgov(null,null)` → `ac.getIsLoc() && ac.isPass()`).

**상세(`like/{locgovCode}`)**
- [목록] 버튼은 **지자체담당자에게는 안 보인다**(`adminRole != 'LOC'`)
- 년도 select(YYYY 공통코드, 매출통계와 같은 함정) + 검색
- 요약카드 2개: **총 등록수(현재기준, 연도 무관 전체)** / **년도별 등록수**(선택 연도만) -
  이름이 비슷해 헷갈리기 쉽지만 분모가 다른 두 숫자다
- Chart.js bar 1개: 월별 등록건수, ajax `POST like/{locgovCode}/{year}`
- ★**다른 지자체 상세를 직접 URL로 접근하면 막힌다**(지자체담당자가 자기 지자체가 아닌
  곳을 보려 하면 차단+리다이렉트)
- ★월별 집계는 AS-IS 서비스가 **1~12월을 전부 채워서** 돌려준다(매출통계와 같은 함정 -
  없으면 차트 x축이 데이터 있는 달만 찍힌다)

**엑셀**: 시트명 `locgov_like`, 제목 "관심 지자체", 헤더 No./지자체/등록수,
파일명 `관심_지자체_yyyyMMddHHmmss.xlsx`, 페이징 없이 전체.

#### 구현

- **donation** `InterestLocgovStatsService` 전면 재작성 - `search(shLocgovCode, offset, limit)`
  (목록, COUNT DESC 정렬 후 메모리 페이징) · `totalOf(locgovCode, year)`(총/연도별 분리) ·
  `monthlyOf(locgovCode, year)`(1~12 채움). `InterestLocgovRepository.findByLocgovCode` 신규.
  API 컨트롤러 전면 재작성(`/api/admin/interest-locgov-stats`, `/{code}/total`, `/{code}/{year}/month`).
  기존 `availableYears()`(목록의 "전체 연도" 셀렉트용)는 상세가 YYYY 공통코드를 쓰게 되며
  더 이상 필요 없어져 제거.
- **admin** `StatisticsLocgovClient` 전면 재작성(목록/총계/월별 3개 메서드로 분리).
  `StatisticsLocgovController` 전면 재작성 - GET/POST 목록·상세, ajax 월별, 엑셀.
  `MenuService.isLocgovScoped` 로 지자체담당자 리다이렉트/차단 재현(AS-IS `LocManagerCheck` 대응).
  `PrivacyAccess.GIVE_STATISTICS_LIKE` 에 TO-BE 경로(`/statistics/interest-locgov/excel`) 추가
  (엑셀이 개인정보 사유 모달을 거치므로 등록 안 하면 사유 저장이 거부된다 - 매출통계 때와
  같은 함정, [[admin-excel-download-real-xlsx-progress]] 패턴 참고).
- **템플릿** `statistics-locgov/{list,detail}.html` 전면 재작성.

#### 검증 (Playwright, 여분 포트)

- GET 목록: 조회 안 함(0건), 시도 select 18개, 표3칸, 안내문구, 출력수 4종, 엑셀버튼,
  페이지네이션 조각 - 전부 확인
- 검색(조건없음): 총 11건, 1페이지 10행 + 2페이지 1행(itemsPerPage 분할 정상), **등록수
  내림차순** 확인
- WDR 캐스케이딩: 전남광주통합특별시 선택 → 시군구 29개 정상 로드
- 상세(11230, 기본 2026년): 카드1="1"(총) 카드2="0"(2026년 등록 없음) - DB `regist_de=20250115`
  와 일치. **2025년으로 재검색** → 카드2="1", 월별차트 1월=1·나머지 0으로 정확히 찍힘(1~12
  채움 로직 확인)
- 지자체담당자(pwtest_loc, 11230) 목록 접근 → **자동으로 `/11230` 상세로 리다이렉트**,
  [목록] 버튼 숨김 확인 · **다른 지자체(26350) 상세 직접 URL 접근 → 차단**되어 다시
  `/11230`으로 돌아옴
- 엑셀: 사유모달 정상 경유 → 다운로드 `관심_지자체_....xlsx`(9,216바이트) → **POI로 복호화**
  (비밀번호=로그인ID) → 시트 `locgov_like`, 제목 "관심 지자체", 헤더 `No. | 지자체 | 등록수`,
  11건 전부 담김(마지막행 12 = 제목+헤더+11데이터) 확인
- 콘솔/HTTP 오류 0건(엑셀 다운로드 중 뜨는 "다운로드가 진행 중입니다" 안내는 기존 공용
  UX, 오류 아님)
- donation·admin `test`+`bootJar` EXIT=0

### admin-categories-port

> 답례품관리>카테고리관리(16101) AS-IS 이식 - OP_CATEGORY 1~4차 트리(jsTree 드래그 이동)를 gift.public.op_category에 올림, 메뉴 URL을 /categories로 교체, 알려진 한계(GNB는 gift_subcategory를 읽음)


2026-10-15 이식. AS-IS `opmanager/categories/list`는 **OP_CATEGORY(1~4차, 코드 12자리 `CCC000000000` 구조)** 를 jsTree로 보여주고
드래그앤드롭으로 이동한다. TO-BE 메뉴 16101은 원래 `/admin/gift-categories`(GIFT_SUBCATEGORY를 고치는 자체 화면)를 가리켰다 - 다른 데이터라
화면을 새로 이식하고 메뉴 URL을 `/categories`로 바꿨다(옛 화면은 삭제하지 않고 메뉴 연결만 끊음).

**구성**: gift `CategoriesAdminService`(네이티브 SQL, `public.op_category`)·`CategoriesAdminApiController`(`/api/admin/categories`) ↔ admin
`CategoriesAdminClient`·`CategoriesManagerController`(`/categories/**`)·템플릿 `categories/list|create|edit.html`(create/edit은 ajax 조각).
메뉴 prefix 매칭(`menuUrl + "/"`) 때문에 menu_url을 `/categories`로 둬야 하위 경로 전부가 16101로 권한·브레드크럼 매칭된다.
`database/ddl/migration-admin-menu-16101-categories.sql`(적용 필요), `migration-gift-category-asis-load.sql`(데이터: 팀1·그룹6·카테고리91행, 개발DB 적용 완료).

**AS-IS 특징(그대로)**
- 등록·수정·삭제는 JS가 `adminRole != 'SYS'`면 alert로 막는다. 그런데 16101 메뉴 권한이 ROLE_ADMIN_1~4뿐이라 지자체(5,6) 등은 아예 `/admin/access-denied` - 권한 alert은 사실상 도달 불가(verbatim 유지).
- 1차 트리는 `ORDER BY CATEGORY_NAME`인데 **CUBRID는 코드포인트 순, PG en_US는 한글을 그렇게 안 정렬** → `collate "C"`로 맞춤(AS-IS 실DB 조회로 확인).
- SEO 영역(#seo)은 AS-IS도 항상 `display:none`이라 화면에선 못 치고 값 채우기/저장만 동작(Playwright에선 evaluate로 값 주입해 검증).
- 수정폼의 "하위 카테고리 등록" 블록 조건이 `level != '2'`라 4차에도 뜨는 AS-IS 이상 동작 그대로(3차 위 추가하면 SQL 에러 - AS-IS도 동일).
- 미리보기 링크는 AS-IS 운영 설정(`view.type=api`)대로 `{frontend}/goods/searchGoods.html?category=…&type=C` 한 개만(프론트 주소는 `ghlove.storefront.url`, 기본 localhost:5173 - 설문 미리보기와 같은 관례). TO-BE storefront엔 그 경로가 없다.
- 안 옮긴 것: AS-IS `/change`, `/change-ordering-category1`(화면이 안 부르는 죽은 엔드포인트), 필터 UI(edit.jsp 마크업에 원래 없음), `updateCategoryUpdatedDate`(쇼핑몰 캐시 갱신 - TO-BE 대응 없음), 시퀀스 서비스(MAX+1 사용).

**함정**: OP_ITEM_FILTER엔 `category_id` 칸이 없다 - AS-IS는 `ITEM_ID IN (SELECT ITEM_ID FROM OP_ITEM_CATEGORY WHERE CATEGORY_ID IN …)`로 지운다(처음 `category_id`로 짜서 등록이 500).
수정 시 필터 초기화는 상위(브레드크럼) 카테고리가 쓰는 필터 그룹은 남긴다(`resetFiltersKeepingAncestors`).
**spare 기동은 각 모듈 디렉터리에서**(`cd gift && java -jar build/libs/…`) - 루트에서 띄우면 `config/application.yml`(개발DB 10.0.110.37)을 못 읽어 localhost:15432로 가서 실패.

**알려진 한계(사용자에게 알림)**: 스토어프론트 GNB 메가메뉴는 여전히 `gift_subcategory`를 읽는다 → 이 화면의 편집이 GNB에 반영되지 않는다(데이터 모델 통합은 별도 결정).
`op_category_team` 1001·`op_category_group` 1000(이전 테스트 시드)이 그룹 select에 "여름 특가 기획전/농산물 특가"로 남아 있다(지우지 않음).

검증: Playwright로 트리 로드(전체 51)·그룹 재조회·노드→수정폼+SEO·검증 alert 7종·등록/수정/하위등록/이동(jsTree move_node, 취소 포함)/삭제(하위·1차 경고문구) 후 DB 91행 복원 확인.
admin·gift 재기동 필요. 관련: [[asis-porting-principles]], [[asis-porting-principles]].

### admin-categories-team-group-port

> 답례품관리>카테고리 그룹 관리(16102) AS-IS 이식 - 대그룹+1차 카테고리 그룹 목록/등록/수정/삭제/정렬순서변경, 메뉴 URL→/categories-team-group, 안 옮긴 AS-IS 도달불가 기능, 알려진 한계


2026-10-15 이식. AS-IS `opmanager/categories-team-group/list`(메뉴 16102, AS-IS 실DB status 1 활성 확인). TO-BE 메뉴 16102는 원래
`/admin/category-teams`(대그룹/그룹/**배너/추천상품** 자체 화면 - AS-IS 목록에 없는 발명)를 가리켰다 → AS-IS 화면을 새로 이식하고 메뉴 URL을
`/categories-team-group`으로 교체(옛 화면은 삭제 안 하고 연결만 끊음, [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-categories-port)와 같은 처리).

**구성**: gift `CategoriesTeamGroupAdminService`(네이티브 SQL, op_category_team/group)·`CategoriesTeamGroupAdminApiController`
(`/api/admin/categories-team-group`) ↔ admin `CategoriesTeamGroupAdminClient`·`CategoriesTeamGroupManagerController`(`/categories-team-group/**`)·
템플릿 `categories-team-group/list|group-form.html`. `database/ddl/migration-admin-menu-16102-categories-team-group.sql`(적용 필요).
코드 중복확인은 카테고리관리의 `/categories/code-check`를 그대로 쓴다(AS-IS도 OP_CATEGORY 기준).

**AS-IS 특징(그대로)**
- 상단 구분 select(전체/대그룹)는 display:none 폼 안 → 화면에선 안 보이고 `?categoryType=team|group`으로만 바뀜. 기본(전체)은 대그룹 행 + 하위 그룹 행(tbody별 jQuery UI sortable).
- 정렬순서변경은 **tbody마다 1부터** 번호를 다시 매겨 전부 저장 - 손대지 않은 대그룹의 그룹 순서도 같이 덮어쓴다(검증 중 시드 그룹 0→1로 바뀜, 원복함).
- 대그룹 수정 버튼은 AS-IS가 주석처리 → 대그룹은 보기만. 접속권한 "성인만" 라디오는 hidden 클래스.
- **수정은 SEO 칼럼을 전부 NULL로 덮는다**(폼에 없는 값, DESCRIPTION만 주석처리라 보존) + 숨은 영역(`.email_tb` display:none)의 `categoryCodes`가 같이 전송돼
  REMOVE(목록에 없는 소속 카테고리의 그룹을 0으로; 목록 없으면 전부) → SAVE(목록 카테고리를 이 그룹으로). 공개 1차 카테고리만 목록에 실리므로 **비공개 1차 카테고리는 수정할 때마다 그룹이 0이 된다**(AS-IS 결함 그대로).
- 삭제는 그룹 행만 지운다(소속 카테고리의 그룹 ID는 그대로 남음).
- 등록·수정·순서변경은 JS가 adminRole != SYS면 alert로 막는다. 16102 메뉴 권한이 ROLE_ADMIN_1~4뿐이라 지자체 등은 `/admin/access-denied`로 막혀 alert 도달 불가.

**안 옮긴 것(AS-IS도 화면에서 닿는 길이 없음)**: 대그룹 등록/수정/삭제 화면(`/team/**`, 상품 검색 팝업은 SalesOn 종속), "1차 카테고리로 변경" 팝업(그룹 폼 버튼 주석처리),
`ordering-edit/{type}`(대응 입력칸 없는 죽은 핸들러), GET 불가한 `/group/delete/{id}`, 캐시 갱신, 시퀀스(MAX+1).

**알려진 것**: ①`/ban-word/list-all` 404(`_filter` 클래스 폼 공통, [[tooling-and-local-env]]에도 기록된 전 화면 공통 갭 - AS-IS는 ShopConfig.banWord 콤마목록 반환, 미구현) ②sortable 자리표시 배경이미지 404는 AS-IS도 경로가 틀려 같음 ③
시드 대그룹 1001·그룹 1000(이전 테스트 데이터)이 목록에 "여름 특가 기획전/농산물 특가"로 남아 있고, 그룹 신규 ID가 MAX+1이라 1001부터 시작.
**테스트 계정 로그인이 바뀜**: 새 로그인은 `#op_username/#op_password` + `idLogin()` → devCode 자동 입력(기존 `#loginId` 스크립트는 hidden이라 실패).

검증: Playwright - 전체/team/group 3모드 목록, 등록(검증 alert 4종·코드중복·확인창·flash 안내), 수정(코드 readonly), 마우스 드래그 정렬+저장, 숨은 영역 추가/삭제 JS, REMOVE 효과(DB), 삭제. DB 원복 확인.
admin·gift 재기동 필요.

### admin-seller-manage-port

> 답례품관리>답례품제공자 관리(16151) AS-IS 이식 - 목록/등록/수정 + 계정(OP_SELLER_USER) 같이 저장, 사용자 결정 2건(계정 AS-IS처럼·실데이터 9,229건 보류), 안 옮긴 것(shadow 로그인 등), 알려진 한계


2026-10-15 이식. AS-IS `opmanager/seller/list`(메뉴 16151, 권한 ROLE_ADMIN_1~6). 기존 TO-BE 화면은 상태변경 버튼이 있는 축소 구현(목록+짧은 폼)이라
AS-IS `SellerManagerController`(list/create/edit/makeSellerId/init-seller-password/deleteFile)에 맞춰 교체했다(경로 `/seller`, `/seller/list`, `/seller/create`,
`/seller/edit/{id}`; 메뉴 URL은 `/seller` 그대로라 migration 불필요).

**사용자 결정(2026-10-15, AskUserQuestion)**: ①계정은 AS-IS처럼 **OP_SELLER + OP_SELLER_USER 같이 저장**(design-decisions §4의 "OP_SELLER만"과 다르게 - 목록의 인증서·본인인증
등록여부 3칸과 SMS수신동의가 OP_SELLER_USER에만 있어서). 사용자번호는 gift 자체 채번(MAX+1), 역할(ROLE_SELLER*)은 gift에 역할표가 없어 안 만듦, 비번은 salt+hash.5 대신 **BCrypt**
(gift에 spring-security-crypto 의존성 추가), 개인정보는 평문(TO-BE 전체 방침). ②AS-IS 실데이터 **9,229건은 보류**(아이디·이름·연락처·계좌가 암호화돼 있어 복호화 키·개인정보 규모 때문, op_manager와 같은 이유).

**구성**: gift `SellerManageService`·`SellerManageApiController`(`/api/admin/seller-manage`, multipart 폼 필드명 그대로) ↔ admin `SellerManageClient`·`SellerAdminController`·`templates/seller/list|form.html`·
`web/support/AdminRole`. 옛 `/api/admin/sellers`(이메일 발송대상·문자수신정보·답례품 판매자 select가 씀)는 그대로 둠 - 그 create/update/status는 이제 admin이 안 쓰는 죽은 엔드포인트.
`database/ddl/migration-gift-seller-user-asis-columns.sql`(OP_SELLER_USER에 pwd_chg_seller_yn·mber_dn·mber_fin_dn 추가 + 계정행 없던 기존 판매자 18건 보강 - **적용 필요**).

**AS-IS 특징(그대로)**: GET 진입은 빈 목록(POST 검색에서만, 등록일 기본=오늘, 페이지 링크는 op.manager.js가 폼 POST) · 목록은 OP_SELLER inner join OP_SELLER_USER(계정행 없으면 안 보임),
건수는 join 없이 OP_SELLER만 셈 · 지자체 칸/시도·시군구 검색은 SYS만 · 등록버튼·비밀번호 초기화는 SYS면 alert로 막힘 · 폼 저장은 ROLE_ADMIN_5/6만(그 밖은 form id=impossible-form + alert) ·
담당자정보/주문담당자/배송비/관리항목/수수료·정산주기 영역은 hidden이지만 값은 전송돼 등록 시 팩스·담당자 전화가 "010--"처럼 이어 붙고, 정산주기는 월정산(4)/8일로 고정 · 상태→계정상태 매핑(2→9,3→2,4→3,5→1) ·
첨부 3종 5MB·확장자 검사, 새 첨부 올리면 옛 파일 삭제(폴더 `gift/uploads/gift/seller/{id}/원본파일명`) · 비번 초기화=1111 + 계정 PWD_CHG_SELLER_YN='Y'.
지자체 담당자의 목록·등록은 서버가 자기 지자체로 강제(AS-IS는 파라미터로 덮어쓸 수 있는 구멍이 있었음).

**안 옮긴 것**: 목록의 "로그인"(shadow-login - 제공자 포털 로그인 세션이 아직 없음, design-decisions §5 #1·#2 이후), 삭제/일괄삭제(AS-IS도 버튼 주석처리), find/find-popup/find-md 팝업(다른 화면용·MD 영역 hidden),
비밀번호 변경 팝업(호출 주석처리), 변경이력 팝업(본문 주석처리), id-duplicate-Check(화면이 안 부름), 담당MD·배송비 상품 일괄수정(hidden 영역 기능), 국민비서 등록 문자(AS-IS도 CI 폼값이 없어 안 나감).

**알려진 것**: 개발 판매자 18건은 locgov_code가 없어 지자체 담당자(pwtest_loc, 11230)에겐 0건 - 검증은 pwtest_loc로 새로 등록해서 함(끝나고 삭제·시퀀스 10001 복원). `/ban-word/list-all` 404는 `_filter` 폼 공통 갭.
AS-IS CSS의 `file_camera.gif` 경로(`/images/btn/`)는 AS-IS에도 404(파일은 `/images/icon/`에만) - 임의로 복사하지 않음.
테스트 로그인은 `#op_username`+`idLogin()` 방식([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-categories-team-group-port) 참고). admin+gift 재기동 필요. 관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-categories-port), [[domain-code-facts]].

### admin-representative-banner-port

> 답례품관리>대표배너관리(16251) AS-IS 이식 - G_REPRST_BANNER PROCESS_TYPE=ITEM, 지정기부 배너와 서비스 공유, 옛 /admin/main-banners 삭제, 이미지 크기 맞춤(ThumbUtils) 추가


2026-10-16 이식. AS-IS `opmanager/representative-banner/list`(메뉴 16251, 권한 ROLE_ADMIN_1~4). 같은 테이블 `G_REPRST_BANNER`를 **PROCESS_TYPE**으로 나눠 쓰는 두 화면 중 하나다:
ITEM(이 화면, AS-IS 4건) / DESIGNATED_DONATION(특정사업 기부 배너관리 17301, `/designated-projects/banners`, AS-IS 5건). AS-IS JSP 두 벌은 URL만 다르다(diff 확인).

**구성**: admin `ItemRepresentativeBannerController`(`/representative-banner`: list·form/{id}(0=등록)·change-ordering·delete) + `templates/representative-banner/list|form.html`(지정기부 템플릿을 URL만 바꿔 복사, 등록모드 판단은 AS-IS 그대로
`location.href.includes("/0")`) + **기존 `RepresentativeBannerService`를 PROCESS_TYPE 파라미터로 일반화**(`list(String)`, `create(processType,…)`, 상수 `PROCESS_TYPE_ITEM`). 메뉴 URL `/representative-banner`
(`database/ddl/migration-admin-menu-16251-representative-banner.sql` - **적용 필요**). 지정기부 컨트롤러·템플릿은 그대로.

**삭제한 것**: 같은 화면의 TO-BE 축소 구현(`MainBannerAdminController`, `MainBannerRepBannerService`, `templates/main-banner-admin/*` - 노출순서 입력칸·토글/삭제 버튼 등 AS-IS에 없던 것, 5MB 한도). 아무도 안 썼다.
(`MainBannerImageStorageService`는 운영콘텐츠 쪽이 써서 그대로.)

**AS-IS 특징(그대로)**: 목록 항상 15행(빈 행엔 [등록] → form/0), 순서는 select(1~15)+저장 시 필수·중복 검사, 사용여부 "사용/중지"; 등록 순서는 `MAX(DISPLAY_ORDER)+1`을 **PROCESS_TYPE 무관 테이블 전체**로 센다(그래서 ITEM 첫 배너가 2번부터 시작);
"배너 설명"은 별표가 없지만 textarea에 required 클래스가 붙어 validator가 필수로 막는다; 이미지 X는 배너가 아니라 파일명만 비움; 수정은 새 파일을 안 올리면 기존 유지; 저장은 시스템 관리자(SYS)만(JS alert), 서버 검사 없음.

**이번에 추가한 것(지정기부에도 적용됨)**: AS-IS `ThumbUtils.create`는 업로드 이미지를 PC 1280×400 / 모바일 768×280으로 **비율 무시하고 맞춰** 저장한다 - TO-BE 서비스엔 그게 없었다. `fitToSize`로 추가(RGB로 저장, 투명 PNG는 검게, 읽을 수 없는 pdf 등은 원본 유지).
저장 파일명은 TO-BE 관례(UUID, DB엔 `/uploads/rep-banner/…` 경로) 그대로 - AS-IS의 `yyyyMMddHHmmss_pc.ext`·`/upload/representativeBanner/{id}/`와 다르다.

**안 한 것/알려진 것**: AS-IS ITEM 실데이터 4건(제철식품관·특산물관·지역이벤트·원산지 표기)은 적재하지 않았다 - 이미지 원본이 없어 미리보기가 깨지고 지정기부 5건도 같은 이유로 안 올렸다(결정 필요).
`file_camera.gif` 404·`/ban-word/list-all` 404는 AS-IS/공통 갭. 등록·수정 완료 안내는 TO-BE 공통 모달(`fragments/alert`)이라 확인 버튼을 눌러야 화면이 풀린다(Playwright에서 `.gh-alert-ok` 클릭 필요).
검증 중 `admin/uploads/admin/rep-banner/*`를 통째로 지웠다가 추적 중이던 파일 2개는 git으로 복구, 추적 안 되던 테스트 이미지 2개(1004번 행 참조)는 같은 이름으로 대체 이미지를 다시 넣었다 - **업로드 폴더는 내가 만든 파일만 지울 것**.
admin 재기동 필요. 관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-designated-donation-area-port-progress), [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-categories-port).

### admin-item-manage-port

> 답례품관리>답례품관리(16301) 목록 AS-IS 이식 - 목록/일괄처리/삭제신청/변경사항팝업/지자체 대표·오프라인 대표 답례품. 등록·수정 폼과 엑셀은 미이식, 상태코드 숫자 전환 포함


2026-10-16 이식·2026-10-19 실화면 검증. AS-IS `opmanager/item/list`(메뉴 16301). 구성: admin `ItemManageController`(`/item`) + `ItemManageClient` +
`templates/item/list|log-popup.html`, gift `ItemManageApiController`(`/api/admin/item-manage/*`) + `ItemManageService`. 메뉴 URL `/item`, 16351(답례품승인관리)은 옛 `/admin/gift-items` 그대로.

**검증한 것(2026-10-19, 스페어 포트 + Playwright)**: 시스템 관리자 목록 23건·지자체 컬럼("시도 시군구")·페이징, 지자체 담당자(pwtest_loc 11230)는 자기 지자체 8건 + 대표 답례품(≤4)/오프라인 대표(≤2) 영역,
일괄 공개/비공개/품절/품절해제(품절만 처리 직전 상태를 `op_item_log`에 남김 - AS-IS 동일), 삭제=삭제신청(상태 40+로그), 변경사항 팝업, 대표 답례품 저장·전체삭제,
오프라인 대표 저장 시 AS-IS 검증 문구("30,000P와 15,000P인 상품 1건씩만"). 날짜 범위 검사(`Common.searchDateMonth`)가 AS-IS 그대로라 "전체"로 넓게 잡으면 alert로 막힌다 - 버그 아님.

**이번에 고친 것**: 상품코드 검색이 0건 - TO-BE 답례품은 `item_user_code`가 26건 전부 비어 있고(엔티티에 매핑조차 없음) 화면은 `G2`+9자리로 계산해 보여주는데 검색은 저장값만 봤다.
`USER_CODE_EXPR`(coalesce(nullif(item_user_code,''),'G2'||lpad(item_id,9,'0')))로 화면 표시와 같은 식으로 검색(상품코드·상품명 두 분기).

**상태코드 숫자 전환(같은 날, 마이그레이션 적용 완료)**: `op_item.data_status_code`를 AS-IS 숫자(1 정상/20 등록신청/21 반려/90 판매종료/99 삭제…)로 통일, GIFT_STATUS 공통코드도 재키잉.
gift/order/storefront/admin 코드가 같이 바뀌었으므로 이 서비스들은 함께 재기동해야 한다. 개발 판매자 3/18은 지자체코드 비어 있음(상품 없는 판매자 - 정상).

**★미이식(남은 것)**: ①엑셀 다운로드 팝업 `/item/download-excel`(목록 "엑셀" 버튼이 여는데 컨트롤러 매핑 없음 = 죽은 버튼) ②업로드 `/item/upload-excel` ③등록 `item/create`·수정 `item/edit/{코드}` 폼 -
지금은 옛 TO-BE 폼(`/admin/gift-items/new`, `/{id}/edit`)으로 리다이렉트만 한다(코드 `G2`+번호에서 번호 추출). 컨트롤러 javadoc에도 같은 내용.

**함정**: 검증 중 `item_id=1028`은 목록에 안 나온다 - `item_data_type=2`(추가구성 자식)라서 `item_data_type='1'` 조건에 걸리는 게 맞다. 테스트 변경은 SQL로 원복하되 Kafka 이벤트가 안 나가므로 `update-display`를 한 번 더 쳐서 현재 상태를 재발행한 뒤 `updated_*`만 되돌렸다.
관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-categories-port), [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-seller-manage-port), [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-representative-banner-port), [[domain-code-facts]], [[tooling-and-local-env]].

### admin-featured-event-port-progress

> 이벤트 관리(featured, 메뉴 16601 /admin/featured) AS-IS 이식 진척 - OP_FEATURED=지자체별 이벤트(class0/type1). 목록 완료, 등록/수정 폼 잔여


**featured = 지자체별 이벤트 관리**(AS-IS 메뉴 "이벤트 관리" MENU_12000, opmanager/featured). 처음엔
"기획전/미사용"으로 오판했으나 사용자가 AS-IS OP_FEATURED 1,063건(전부 `FEATURED_CLASS=0,
FEATURED_TYPE=1`, 이름 "이벤트-NNNN", 지자체별, 기간 있음)을 확인해줌. 실사용 기능. featured-unused-skip(보관: docs/archive/memory-fix-log-2026-10.md)
데이터 export: `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\OP_FEATURED_202610061741.sql`.

**범위(사용자 승인 2026-10-06): 관리화면(목록+등록/수정+삭제), PC·이벤트(type=1) 기준.** 스토어프론트
노출은 보류.

**★정정: OP_FEATURED 테이블은 미배포가 아니다.** 처음 `ghlove_core.gift.op_featured`로 조회해
"미배포"로 오판했는데, **gift는 별도 DB**(`jdbc:postgresql://localhost:15432/gift`)다. gift DB의
`op_featured`는 `service-gift.sql`에 **AS-IS 46컬럼 전부** 정의돼 있고 실재한다(0행). `op_featured_item`도
있다. 따라서 **DDL 작업 불필요**(사용자가 덤프 DDL 주기로 했으나 안 줘도 됨 - AS-IS DDL은
`...db-dump\테이블dump(ddl만...)\op_featured.sql`에도 있음). gift DB 조회는
`docker exec -i ghlove-postgres psql -U postgres -d gift`.

**AS-IS 4축 요지(FeaturedManagerController + featured/list.jsp + featured-mapper):**
- `/opmanager/featured`(PC type1) · `/opmanager/featured-mobile`(type2, 데이터 없음). GET=빈 목록, POST=검색.
- 지자체 스코핑: ROLE_ADMIN_5~8 자기 지자체만, 1~4(시스템/행안부) 전체 + 시도/시군구 필터. 그 외 "00000".
- 검색: 이벤트명(FEATURED_NAME LIKE)·진행상태(1미진행/2진행중/3진행완료, START/END_DATE+TIME vs NOW yyyyMMddHH)·공개유무(DISPLAY_LIST_FLAG Y/N).
- 목록 컬럼: (5/6만)체크박스·No·(1~4만)지자체(locgovName)·이벤트명(수정링크)·진행상태·공개여부·진행기간(99999999 센티넬→상시게시/개방형)·등록일.
- 등록/삭제 버튼 ROLE_ADMIN_5/6만. 선택삭제=checked-delete. 등록/수정은 ROLE_ADMIN_5~8.
- 채번: insertFeatured. 상세 featured/form.jsp(이벤트 내용 에디터·이미지·기간/상시·아이템).

**TO-BE 진척:**
- **목록 완료(2026-10-06)**: `FeaturedAdminController.list`를 GET(빈)/POST(검색)으로, 지자체 스코핑·
  진행상태·공개유무·이벤트명 검색·지자체명·페이징·시도필터·선택삭제 구현. **gift 변경 없이** admin이
  `client.list("1",null)` 전체를 받아 메모리에서 필터(1406·배송업체 패턴). 진행상태는 날짜정밀(gift
  FeaturedDto에 START/END_TIME 없음 - AS-IS는 시정밀, 표시상 차이 미미). locgovName·시도·시군구는
  LocgovClient(donation)로 해소, 시군구는 `GET /admin/featured/locgov-children` ajax(AS-IS
  /common/getLocgovCode 대응). 진행기간은 `@opDate.ymd`+99999999 로직([[admin-screen-pitfalls]]).
  **URL 충돌 해소**: 검색 POST가 `/admin/featured`라 기존 create(POST 베이스)와 충돌 → create를
  `POST /admin/featured/create`로 이동(form.html action도 수정).
- **폼 진행 중(2026-10-06)**: AS-IS `featured/form.jsp`(907줄) 활성 필드 = 이벤트명·진행기간(시작/종료
  날짜+시각 select hours)·대표연락처(phone1/2/3)·주최주관(host)·홈페이지(link)·목록이미지(type5,
  5MB,300x300)·상단PC이미지(type2,20MB,1120)·상단모바일이미지(type3,20MB,350)·상세내용(스마트에디터)·
  상품편성(prodString→OP_FEATURED_ITEM). 저장은 ROLE_ADMIN_5~8, 이미지 삭제 ajax.
  - **[완료] gift Featured 엔티티 확장**: startTime/endTime/featuredHost/featuredPhoneNo1~3/
    featuredImageMobile/featuredListImage/thumbnailImageMobile/thumbnailListImage/prodState 매핑 추가.
    gift 빌드·기동 확인(컬럼은 테이블에 이미 있음).
  - **[잔여] gift create/update API**: 위 필드 수신 + 이미지 3슬롯(목록/PC/모바일) 업로드·삭제 + 아이템.
  - **[잔여] admin form.html 전면 재작성**(현재 발명형 "기획전" 간이폼) + FeaturedAdminController/Client 확장.
  - 그 다음 스토어프론트(보류).
- 미검증: 런타임 클릭(세션 필요). 빌드·기동·가드테스트 통과.

### admin-main-dashboard-asis-port

> 관리자 메인(/admin) AS-IS main/index.jsp 재이식(2026-10-15) - 발명 위젯 제거, 권한별 위젯·5개 서비스 집계 API·콜 등록 팝업·로그인 직후 오프라인 담당자 안내. REPORT_DASHBOARD는 배치 미이식이라 0


2026-10-15 사용자 지시 "관리자 메인화면(http://localhost:8086/admin) 이식". 이전 TO-BE 화면(오늘 주문 건수·오늘 신규 회원·
최근 공지사항)은 **AS-IS에 없는 발명**이었다(docs/as-is-parity.md 3(b)의 위젯 목록도 SalesOn 원제품 기준 오판 - 실제 화면과 다름).

**AS-IS 정본**: `OpmanagerController.index` + `main/index.jsp`(1,065줄) + `CommonController /common/opmanager/*` 12개 +
`MainController /opmanager/popup/insertCall` + 매퍼 `slave-main-mapper.xml`(main-info·boardInfo·차트·chartInfo·gift-search·
get-user·알림동의) / `main-mapper.xml`(main-search·call-search·gift-amount·콜등록 - 같은 id라도 두 파일 SQL이 다르니 서비스가
어느 매퍼를 부르는지 확인할 것).
- 레이아웃: `main == 'main'`이면 LNB·.contents 래퍼 없음 → `.mainArea`가 `#container` 바로 아래. 브레드크럼도 없다.
- **"메인화면 데이터 조회" 버튼을 눌러야 조회**(AS-IS가 서버 부하 때문에 바꾼 것). 하단 표는 기부일자 입력 후 검색.
- 화면 권한(JSP c:choose): 7·8·11 OFF(오프라인 접수현황만) / 5·6·10 LOC(기부·답례품 카드+차트) / 1~4 SYS(전부) / 그 외 아무것도 없음.
  쿼리 권한(getLoginUserAdminRoleCheck)은 별개: 5·6 LOC, 7·8 OFF, 11 WCM, **10 포함 나머지 SYS** - 10은 LOC 화면에 전체 데이터.
- AS-IS 라벨 엇갈림 그대로: "국민비서 동의 회원"에 sms_count, "SMS 동의 회원"에 pbanc_count.

**구현**: 서비스별 `AdminDashboardService`+`AdminDashboardApiController`(member `/api/admin/members/dashboard/*`, donation·point
`/api/admin/dashboard/*`, order `/api/admin/orders/stats/dashboard-present`, gift `/api/admin/gifts/dashboard/*`) →
admin `AdminMainClient`/`AdminMainService`/`AdminHomeController`(데이터 주소는 AS-IS `/common/opmanager/*` 그대로, 응답 `{isSuccess,data}`).
admin 소유 표(G_MNGR_REQST·OP_QNA·OP_CALL·REPORT_DASHBOARD)는 admin이 직접. 콜 등록 팝업 `/admin/popup/insertCall`.
로그인 성공(ID/PW·인증서) 시 세션 `loginNow` → 메인 첫 진입에 7·8에게 "{은행} {지점}지점 {이름}님으로 로그인…" alert.
설문조사·엑셀암호화 팝업은 POP_ADMIN period/period2 기간판정(현재 둘 다 만료라 안 뜸).

**코드 치환**: G_CNTR '200'→COMPLETED / OP_USER '9'→ACTIVE / 판매자 '2'(AS-IS 값 그대로, 10-15 전환) /
답례품 DATA_STATUS '1'→APPROVED / OP_ORDER_ITEM 65·75 제외 → 품목행(od_order_item, item_status≠CANCELLED) +
품목행 없는 단품주문(od_order, order_status≠CANCELLED). 판매자 지자체코드가 TO-BE에 없어 답례품의 locgov_code 사용.
수신동의는 AS-IS대로 '0'만 센다(10-15 member 저장값 통일로 이제 정확).

**남은 것**: ①"기부현황 복사" 영역(get-user)은 REPORT_DASHBOARD를 읽는데 그걸 채우는 AS-IS 배치(`confirmDonationForReportBatch`,
JobServiceImpl)가 미이식이라 늘 0 → [[batch-job-port-decisions]](사용자: 메인 끝나면 누락 배치 전부 찾아 이식).
②`/opmanager/dashboard`(main/dashboard.jsp, index 옛 사본, 링크 0건)는 사용자 지시로 같은 날 `/admin/dashboard`로 이식 -
진입 즉시 조회·알림동의 없음·카드값이 백만원인데 "(원)" 라벨·mainGift 상대경로 404까지 AS-IS 그대로.
③판매자 상태코드는 같은 날 AS-IS 값(2·3·5)으로 전환 완료 → [[domain-code-facts]]. 메인 집계도 '2'.
④수신동의도 같은 날 AS-IS 0/1로 통일 → [[domain-code-facts]] - 메인 알림동의 수가 이제 실제 동의 회원을 센다.
관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-login-asis-parity-2026-10-14), [[service-logic-full-audit-planned]].

### admin-login-asis-parity-2026-10-14

> 관리자 로그인(/admin/login) AS-IS login_main.jsp 재이식 - 결과코드 응답·로컬 인증번호 자동입력·권한요청 진입·잠김화면·비밀번호 변경 팝업·관리자 공지 팝업·로그인 국민비서 문자. 남은 판단(EMPTY_CI) 포함


2026-10-14 사용자 지시 "관리자 로그인부터 AS-IS랑 비교분석해서 수정". AS-IS `/opmanager/login`은 NetFunnel 중간 페이지, 실제 화면은
`/opmanager/login-main`(login_main.jsp).

**바뀐 응답 계약(테스트용 curl 절차도 바뀜)**: `POST /admin/login/check`(loginId,password) → `{data:{code}}`(AS-IS 결과코드
SUCC/REQ/REQ_NOT/ERR_ROCK/ERR_MNG_PASS/ERR_USER_PASS/ERR_USER_NOT/ERR/PASS_CHANGE_*) → `POST /admin/login/email` →
`{data:"SUCC"+이메일, devCode}`(이메일 연계 꺼진 로컬만 devCode) → `POST /admin/login/verify`(code) → `{data:"SUCC",redirect}`.
화면은 devCode가 오면 자동 입력해 바로 로그인(AS-IS IS_LOCAL_DEV 250324 우회와 같은 방식).

**이식한 것**: 화면 verbatim(국민비서 안내·안내문구·alert 문구·error 파라미터 알림·아이디저장 포커스), 발명 요소 제거("권한 요청하기"
링크·빨간 오류줄·개발모드 안내/버튼). REQ_NOT → `POST /admin/manager-requests/form`(아이디·비번으로 회원 확인, 세션에 신청자 저장 -
대민 로그인 없이 신청). `/admin/login-lock`(계정 잠김 해지 안내). 비밀번호 변경 팝업 `/admin/login/popup/temp-password-change` +
저장 `/admin/login/temp-password-change`(Manager에 PASSWORD_TYPE·PASSWORD_EXPIRED_DATE 매핑, 만료 = 오늘+ISMS LIFE_TIME_PASSWORD).
관리자 공지 팝업 API `/common/manager/notice/list`(AS-IS '%Y%m%d%H%m%s' 분자리=월 오타까지 그대로) + AS-IS 15건 적재
(`migration-admin-op-manager-notice-asis-load.sql`, 전부 기간 만료라 지금은 안 뜸 - 15102 공지 팝업도 이 API). 로그인 성공 시
국민비서 MANAGER_LOGIN 문자(같은 USER_ID 회원, 수신동의 무시 - AS-IS).

**남은 것/구조 문제**:
- ★EMPTY_CI 미이식: AS-IS는 관리자=회원(같은 USER_ID·같은 계정)이라 권한 7·8이 아니면 회원 CI 없을 때 차단. TO-BE op_manager는 회원과
  별개 계정이라 시스템관리자 테스트계정 전부 CI 없음 → 그대로 넣으면 다 막힘. 사용자 판단 대기. 같은 이유로 MANAGER_LOGIN 문자는
  우연히 같은 USER_ID인 회원(admin01=1000 ↔ testuser01)에게 갈 수 있다.
- 비밀번호 변경 팝업은 TO-BE가 PASSWORD_TYPE 'T'·만료일을 아무 데서도 채우지 않아 실제로는 안 뜬다(관리자 등록·초기화·오프라인담당자 생성 쪽, 전수조사).
- 잠김 해지: AS-IS 안내는 "비밀번호 찾기"(회원 비번=관리자 비번)인데 TO-BE 관리자 비번은 별도라 그 경로로 안 풀린다.
- 인증서 등록/삭제 버튼은 로그인 필요 화면(/admin/my-cert)으로 감 - AS-IS 로그인 전 등록화면(/opmanager/user/pki/form) 미이식.
  AS-IS는 회원 정보(생년월일+휴대폰+CI, 오프라인은 OP_MANAGER.emp_id)로 본인 확인 후 DN을 **OP_USER**에 저장 → 계정 연결 결정에 묶임.
- 권한 요청 폼은 문구 차이가 아니라 구조 차이: 소속구분 라디오+구분별 칸(은행·지점·지점연락처/지정기부부서/복지센터), 생년월일 행,
  이메일 안내문. TO-BE ManagerRequest에 psitnCode·dsgncntrPartId 등 없음, 승인은 AS-IS(회원에 권한 부여)와 달리 새 관리자계정+임시비번 발급.
- ★위 구조 문제(EMPTY_CI·로그인문자·잠김해지·비번팝업·인증서등록·권한요청폼)는 같은 날 A안으로 전부 해소 → [[admin-manager-member-account-link]].
- 2026-10-14 사용자: 관리자↔회원 계정 연결은 RFP SFR-002·ISP 검토 후 상의하기로. 기존 결정은 docs/design-decisions.md §1(2026-09-09
  개인/업무계정 분리). 인증서 등록·권한 요청 폼은 이 결정 뒤에 이식.
- AS-IS 정확한 구조: OP_MANAGER는 별도 테이블이지만 승인 시 회원 정보(USER_ID·LOGIN_ID·PASSWORD 해시·이름·이메일·휴대폰)를
  복사(insertManagerByUserSync)하고, 회원 비번 변경/찾기 시 "관리자동기화"로 OP_MANAGER 비번도 갱신. 승인 메뉴 1406(1·3·4 전체, 5는 자기 지자체 부담당자만).
- ISP 원문 확인(pdftotext로 텍스트 있는 쪽만): Ⅲ-12(PDF p.204) 목표 서비스 구성에서 **회원관리 서비스**에 운영자 관리·지자체 담당자 관리·
  오프라인 담당자 관리·답례품제공자 회원관리·인증서등록/해지·아이디/비번 찾기가 들어 있고, Ⅲ-217 회원 애그리거트에 op_manager_login 포함.
  → TO-BE는 관리자 계정이 admin 소유라 "서비스 소유" 측면은 ISP와 다름(A/B 선택과 별개 쟁점, PM 확인 대상).
관련 [[service-logic-full-audit-planned]], [[asis-porting-principles]].

### admin-order-manual-registration-port-progress

> AS-IS '관리자 주문 수기/엑셀 등록'(OrderAdminServiceImpl) 기능을 TO-BE에 포팅, 7210 정합성검증이 보는 AB코드 주문 생성경로 확보(2026-10-08)


[[domain-code-facts]]에서 발견한 "AB" 접두사 주문코드의 출처를 추적한 결과물.
AS-IS에는 **두 개의 별개 주문코드 생성기**가 있다:
- `OrderServiceImpl.getNewOrderCode(OrderCodePrefix.FRONT)` = **"K"**+10자리(일반 고객 체크아웃)
- `OrderAdminServiceImpl.getNewOrderCode("A")` = **"A"**+프레임워크 시퀀스(실데이터는 "AB"+yyMMdd+
  8자리) - **관리자가 전화주문·오프라인거래를 직접 등록**하는 별개 기능(`/opmanager/order/admin/
  {list,upload-excel}`, 엑셀 업로드→스테이징(OP_ORDER_ADMIN/_DETAIL)→목록검수→확정의 2단계
  워크플로). 포인트사용 정합성검증(7210, `pointcheck-mapper`)은 네 쿼리 전부
  `ORDER_CODE LIKE 'AB%'`로 **이 관리자-등록 주문만** 본다.

TO-BE에는 이 기능 자체가 없어 7210이 엉뚱하게 일반 고객주문 전체를 보여주고 있었다
(구조적 결함, [[asis-porting-principles]]).

**포팅 범위(이번 라운드)**: AS-IS 엑셀 업로드+스테이징 워크플로(옵션그룹 UI·Daum 주소검색·
동일주문 묶음판정 포함, 500줄+ JSP)는 전체를 옮기면 별도 다일 과제라, **핵심(답례품 1건을
"AB" 코드로 직접 등록 - 7210이 보는 바로 그 데이터)만 단건 입력 폼**으로 먼저 구현했다.
Excel 업로드·스테이징 검수는 미포팅(필요하면 다음 라운드).

**order 서비스**:
- `OrderRepository.nextAdminOrderSeqForDate(datePrefix)` - `coalesce(max(substring(...)),0)+1`
  패턴(QestnarRepository 등과 동일), 날짜별로 1부터 다시 시작.
- `OrderAdminService.createManualOrder(ManualOrderRequest)` - AS-IS `insertOrderAdmin`과 같이
  **SAGA(PENDING→비동기 확정)를 타지 않고 바로 CONFIRMED**로 Order+Shipment+OrderItem을 만든다
  (이미 성립된 거래의 사후 기록이라서). **재고 차감 안 함**(AS-IS도 이 경로엔 재고갱신 호출이
  없다 - 관찰된 사실, 보존). 비회원은 `userId=0`(AS-IS와 동일한 게스트 관례) + `adminMemo`에
  구매자 이름/연락처 기록(TO-BE `Order`엔 비회원 구매자명 컬럼이 아예 없어서의 타협).
  admin readmodel(`stat_order_ledger`) 동기화를 위해 `publishCreated`+`publishConfirmed`를
  순서대로 발행한다(CONFIRMED만 보내면 선행 CREATED가 없어 readmodel에 반영 안 됨).
- `POST /api/admin/orders/manual` (`OrderAdminApiController`) 신규.
- `OrderReconciliationRepository.orderAmounts()`에 `WHERE o.order_id LIKE 'AB%'` 필터 추가
  (AS-IS `pointcheck-mapper` 네 군데와 동일 스코프) - **이게 진짜 버그 수정**이다.

**admin 서비스**: `OrderAdminClient.createManualOrder()` + `OrderAdminController`
`GET/POST /admin/orders/manual` + `order-admin/manual-form.html`(신규 화면, AS-IS verbatim
아님 - 단건 입력으로 축소했다고 템플릿 주석에 명시).

**★2026-10-08 정정 - 메뉴 위치를 확인 안 하고 2가지를 임의로 했다가 사용자 지적으로 되돌림**:
① 범위를 혼자 판단해서 단건입력으로 줄였다(위 "포팅 범위" 문단) ② 주문목록 화면에 "관리자
주문 등록" 링크를 **발명**해서 추가했었다. `asis_dump.op_menu`(AS-IS 원본, TO-BE가 remap하지
않은 값) 직접 조회로 진짜 위치를 찾았다: **3000(주문관리,status_code='2' 사용안함) >
3700(대량주문 관리,display_flag='N') > 3701(작업 목록,display_flag='N' →
`/opmanager/order/admin/list`)**. 최상위부터 전부 AS-IS 자체 비활성 - 운영자도 메뉴로는
못 들어간다. "오프라인 주문관리"(16409, `/opmanager/order/offList`)는 이름만 비슷한
**완전히 다른 기능**(결제수단이 무통장입금/ARS인 주문의 읽기전용 조회화면, 엑셀업로드 없음) -
처음에 이걸로 착각했다.
[[asis-porting-principles]] 원칙대로 TO-BE도 이 화면을 메뉴/링크 어디에도
노출하지 않기로 되돌렸다(링크 제거) - 사용자 결정: "미사용 기능도 일단 동일하게 구현하기로
약속했잖아"(기능/코드는 유지, 노출 상태만 AS-IS처럼 숨김). `admin.op_menu`에 3700/3701을
AS-IS와 같은 display_flag='N'으로 자리만 심었다(`migration-admin-menu-order-admin-hidden.sql`,
2026-10-02 전수동기화가 "가시 트리만" 뽑을 때 이 가지 전체가 빠졌던 것을 보충). URL은
직접 입력해야 들어온다.

**검증**:
- `OrderRepositoryAdminCodeTest`(`@SpringBootTest` + `@Transactional` 롤백) - 실제 DB로
  "AB"+날짜+8자리 채번이 행 생성 후 이어지는지 확인.
- `OrderAdminServiceManualOrderTest`(Mockito) - CONFIRMED 즉시확정, 금액계산, CREATED→CONFIRMED
  발행 순서 확인.
- **검증용 샘플 데이터 25건**(`seed-order-admin-reconciliation-sample.sql` +
  `seed-point-reconciliation-sample.sql`, **실제 AS-IS 값이 아닌 현실적인 가짜 데이터** -
  사용자 결정, 2.7GB 단일행 AS-IS 덤프에서 샘플 추출이 비효율적이라 판단) - 18건 정상일치 +
  3건 취소(0=0으로 일치) + 4건 의도적 불일치(포인트 사용기록 누락/부분누락). 직접 SQL로 재계산해
  21일치/4불일치 정확히 확인함(화면 코드와 동일한 식).

**상태**: order·admin 모두 compileJava+test+bootJar 통과, 시드 적용 완료. **재기동 필요: order, admin.**

### admin-isms-config-port-progress

> 시스템관리 > ISMS관리(/isms-config) AS-IS 동기화 완료(2026-10-07) - 발명 시드 제거, 실데이터 12건+정렬 버그 수정


**AS-IS**: `saleson.shop.config.ConfigIsmsManagerController`(`/opmanager/isms/isms-config`) +
`isms-config.jsp` + `config-isms-mapper.xml`(`OP_CONFIG_ISMS`, `ORDER BY ISMS_TYPE, ORDERING`).
구분(0=공통/1=관리자/2=회원)별 rowspan 묶음 표, 값만 수정 가능(키·설명·정렬은 편집 불가),
저장은 전체 입력칸을 JSON 배열로 모아 한 번에 POST.

**발견된 문제(2026-10-07, 사용자 지적)**:
1. **발명 데이터**: 기존 시드(`service-admin.sql`)가 `AD_SEND_START_HOUR`/`AD_SEND_END_HOUR`/
   `AD_SEND_RESTRICT`(광고성 메일/문자 발송시간) 3건을 "배치 스캔 당시 0건이라 실제 키를
   몰라 합리적인 기본값으로" **지어내서** 시드해 둔 상태였다 - AS-IS에 존재하지 않는 키.
   [[asis-porting-principles]] 위반 사례. 사용자가 CUBRID 개발DB에서 직접 export한
   `OP_CONFIG_ISMS_202610071026.sql`(12행)로 확인·교체했다(`migration-admin-isms-config-asis-sync.sql`).
   실제 12키: 관리자5(LIFE_TIME_MANAGER_ACTION_LOG·CHANGE_LOG, LIFE_TIME_LOGIN_LOG,
   SESSION_TIMEOUT_MANAGER, UNUSED_MANAGER) / 회원4(LIFE_TIME_USER_CHANGE_LOG·LOGIN_LOG,
   SESSION_TIMEOUT_USER, LOCK_TIME_PASSWORD) / 공통3(FAIL_PASSWORD_COUNT, LIFE_TIME_PASSWORD,
   LIFE_TIME_QUERY_LOG). SESSION_TIMEOUT_MANAGER는 이미 있었으나 ISMS_TYPE이 '0'(공통)으로
   잘못 들어가 있었다 - AS-IS는 '1'(관리자).
2. **정렬 버그**: `ConfigIsmsRepository.findAllByOrderByOrdering()`이 ORDERING만으로 정렬해
   AS-IS `ORDER BY ISMS_TYPE, ORDERING`과 달랐다. 실데이터는 ORDERING이 타입을 가로질러
   섞여 있어서(회원 LOCK_TIME_PASSWORD=13이 공통 LIFE_TIME_PASSWORD=11과
   LIFE_TIME_QUERY_LOG=14 사이에 낌) ORDERING 단독 정렬 시 구분 rowspan 묶음이 깨진다 -
   `findAllByOrderByIsmsTypeAscOrderingAsc()`로 수정.

**기능 커버리지(기록만, 범위 밖)**: 12키 중 `SESSION_TIMEOUT_MANAGER`만 실제로 소비되고
([[admin-screen-pitfalls]]와 같은 축, `ManagerAuthAdvice.managerTimeout()`) 나머지
11개(패스워드 만료 180일, 로그인 실패 5회 잠금, 잠금시간, 관리자 계정 미사용 35일, 각종
로그 보관기간)는 **값만 있고 그 값을 읽어 동작하는 로직이 TO-BE에 없다**. 화면/데이터 자체는
AS-IS와 일치하나, 이 키들을 실제로 적용하는 기능(로그인 실패 카운트/잠금, 비번 만료 강제,
계정 휴면 처리, 로그 보관주기 배치삭제)은 별도 과제로 남아 있다 - 구현 여부는 사용자 확인 필요.

**상태**: 빌드·테스트 통과, admin bootJar 완료. 재기동 대기.

### as-is-parity-audit-progress

> AS-IS↔TO-BE 전수 parity 대조 진척: 6개 서비스(donation·member·point·gift·order·admin) 모두 완료. 남은 것은 common 버킷 + 영역별 심도검증 + 갭 구현


**서비스별 전수 parity 대조(방식 [[asis-porting-principles]]) 진척.**

- **완료 6/6 서비스**: `docs/{donation,member,point,gift,order,admin}-parity-audit.md`.
  - donation·member·point·gift·order = 2026-09-21~22.
  - **admin = 2026-09-23** (`docs/admin-parity-audit.md`). 09-03 심층감사(part1/2, 133 컨트롤러)를 현재 admin 실물(컨트롤러 51→**103개**)로 재대조 — 당시 "진짜미구현" 대부분이 이미 구축돼 gross 갭은 대부분 닫힘.
- **admin 잔여 순수재현 갭**: 카탈로그 관리(中), 설정허브 잔여 서브화면 ~10종(中~高), 판매자포털 경계정리. 저우선: 공통메시지·SmsConfig·제철·특산·만족도조사·전자서명(MagicLine)·배치로그.
- **[2026-09-23 정정] FAQ 관리자 CRUD는 오탐**: 이미 `LocgFaqAdminController`(`/community/locv-faq`)가 스토어프론트와 같은 `OP_COMMUNITY_LOCGOVFAQ`(일반FAQ, ≈63건)에 CRUD. `OP_FAQ`는 0행·무참조 고아. 잔여 debt: 고아 OP_FAQ 정리·명명혼선·지자체별(locgovCode) FAQ 별개기능 여부 확인.
- **admin 심도검증 V1~V8 전건 완료(2026-09-23)**: V1 설정허브=**O**(SiteConfigExtController가 가입거부·결제·배송희망·PG·전환태그·GA·주문임시 전부, 랭킹=RankingAdmin; 전역 포인트정책만 확인·低), V2 클레임메모=부분O(통합목록뷰만·低), V3 카테고리그룹 신O/구 회원그룹=X(legacy축소·低), V4 로그인배너=**O 흡수**(Banner.bannerType), V5 상품문의=O/내부문의=**X 진짜갭**(中), V6 판매자포털=별도 seller audit 위임(admin전용 구현·셀프이관 보류), V7 방문통계=**X**(접속로깅 인프라 선결·中), V8 지자체공지 RBAC=**O 해소**(isLocgovScoped 필터+effectiveLocgov+requireOwnLocgov).
- **admin 최종 순수재현 갭(구현대상)**: ~~내부문의~~(**2026-09-23 구현완료·재기동 대기**), **방문통계(中·로깅인프라 선결)**, **카탈로그(中·DB이관 선결)**, 저우선(회원그룹·클레임메모뷰·공통메시지류·전자서명·배치로그). FAQ(G1)는 오탐→O.
- **내부문의 구현(2026-09-23)**: `InternalInquiryAdminController`(/admin/internal-inquiry) + QnaAdmin/QnaAdminAnswer 엔티티·InternalInquiryService·템플릿 internal-inquiry/{list,form}. G_QNA_ADMIN/ANSWER 시퀀스 생성, OP_MENU 1903(부모100)+RIGHT(5/6) 시드, 테스트문의 1건(qna_admin_id=1, locgov 11230). 첨부·발송알림은 미구현(추후). 컴파일 OK, **admin 재기동 후 활성**. 검증: 본사계정 /admin/internal-inquiry 목록→답변, 지자체담당자계정 문의작성.
- **admin 셸(메뉴트리+메인대시보드) parity 착수(2026-10-01)**: AS-IS opmanager는 상단 GNB+좌측 LNB를 100% OP_MENU DB로 렌더(3단: GNB→섹션→링크), 상단 노출 11개(+숨김 10개 SalesOn잔재). TO-BE는 임의 7그룹·2단이라 전면 재편 필요. 사용자 결정: 갭화면 **이번에 신규구현**, 통합화면 **AS-IS대로 분리노출**(최대충실도).
  - **1라운드 백본 완료(컴파일OK, 재시드 미적용·재기동 대기)**: ① AS-IS 덤프(`OP_MENU_*`/`OP_MENU_RIGHT_*`, 355행)를 `asis_dump` 스키마 적재→조상인지 가시트리 **172행** 추출. ② 매핑/갭 전수 = `docs/admin-menu-tree-parity.md`(99개 url매핑). ③ 재시드 SQL `database/ddl/migration-admin-menu-asis-reseed.sql`(182메뉴+485권한+내부문의5120+op_menu_id_seq→30000), BEGIN/ROLLBACK 검증통과. ④ `MenuService.navTreeFor` 3단화+`topMenuIdOf`, `ManagerAuthAdvice.activeTopMenuId`=조부모, `fragments/admin-nav.html` GNB(topFirstUrl)+LNB(섹션→리프) 3단. menu_id는 AS-IS 원본 verbatim.
  - **적용법(사용자)**: `migration-admin-menu-asis-reseed.sql`을 psql로 실행 + admin 재빌드·재기동(둘이 짝. 적용만 하고 구 jar면 2단 템플릿이 3단 데이터를 잘못 그림). 내부문의는 1903→5120(고객센터>문의관리)로 이동.
  - **2라운드+ 대기**: ❌ 진짜 재현갭 화면 신규구현(특정사업 통계/배너, 외국인기부, 만족도, 기부혜택증 열람현황, 방문자경로, 배송업체/온라인입금/카드혜택, 기금사업소개, 답례품Q&A/제철/특산/미완료, 배치로그/공통메시지) + 통합화면(주문/클레임/정산)에 상태필터 딥링크. TO-BE 전용 orphan화면(open-api·sla·point-history 등) 메뉴 배치 결정. 메인 대시보드(main/index.jsp) 재현 별도.
  - **코드데이터 원본**: [[asis-sources-and-data]].
- **전수 커버리지 원장(파일단위 back/front/static) 완료(2026-10-01)**: `docs/coverage-ledger.md`. 6개 스토어프론트 버킷(member·donation·point·gift·order·common) 3축 점검 끝. ★정본 back=ghlove-api(53개), front=ghlove-frontend(둘 다 SPA티어; ghlove-web/shop은 SalesOn레거시). admin은 메뉴트리 라운드로 별도.
  - **재현갭(이식대상)**: ①donation 납부게이트웨이([[donation-payment-gateway-port-decision]], 이식결정) ②common 통합검색(totalsearch) ③gift/admin **특산물·소식지/카탈로그·카드뉴스** = AS-IS 비활성(프론트진입점0·소식지admin숨김·데이터0·특산행안부권한만)이라 **이식+TO-BE도 숨김재현**([[asis-porting-principles]], 2026-10-01 사용자결정) ④admin 메뉴트리 ❌갭들(특정사업통계/배너·외국인기부·만족도·열람현황·방문자경로·배송업체/온라인입금/카드혜택·기금사업·답례품Q&A·배치로그/공통메시지).
  - **보류/도메인N/A(갭아님)**: order PG(포인트단일결제)·point예약·policy원문DB이관.
  - **저위험 TODO**: member 전자서명, donation featured뷰, gift community-business/store back.
- **A라운드(admin 메뉴트리 2라운드) 진행중**: item-4 admin갭 재확인 결과 상당수가 **오탐(화면은 구현됨·메뉴만 미배선)**.
  - **B1 완료(2026-10-01, DB배선만·재기동불요)**: `migration-admin-menu-B1-wire.sql` — 답례품Q&A(16552→/admin/gift-inquiries)·배송업체(1305→/admin/delivery-companies)·특정사업 지자체별/월별통계(17201·17202→/designated-projects/analysis)·특정사업 배너관리(17301→/designated-projects/banners)·기부혜택증 열람현황(19103→/admin/honor-users/view-history) display='Y'로 노출. 권한·컨트롤러 기존.
  - **B2 신규구현**: 만족도조사(5108 cntnts-stsfdg)=**완료**(ContentSatisfaction* + AS-IS 동일 Chart.js 스택막대, 재기동대기). 잔여: 메세지관리(1402 공통메시지)·기금사업 등록/관리(14201 give-notice, 재확인).
  - **B5 통계화면 AS-IS차트 재현(2026-10-01 착수)**: TO-BE 통계화면들이 AS-IS 차트를 표/단순화로 대체해둔 걸 AS-IS대로 교정하는 라운드([[asis-porting-principles]]). **지정기부 월별통계=완료**: donation `DesignatedStatRepository`(네이티브 3종 campaign/amountraised/amount, CUBRID→PG 포팅·라이브검증 통과)+`DesignatedStatService`+API 3종; admin `DesignatedProjectClient` 3메서드+컨트롤러 `/designated-projects/analysis/month`+데이터3종({isSuccess,data})+템플릿 `designated/analysis-month.html`(AS-IS month.jsp verbatim: pie2+stacked bar1+표3, jquery+op.common+chart.min+op.chart 로드). 메뉴 17202→/analysis/month 재배선(적용됨). 스키마 치환: 확정기부=COMPLETED(STTEMNT_PAY_DE 미사용), 사업상태 OPEN/CLOSED→쿼리내 1/2/9 환산, 조인키 DSGN_DNTN_BIZ_ID. 상세 `docs/b5-designated-month-stats-spec.md`. **월별통계는 재기동 후 정상 확인됨(2026-10-01)**. 그 과정에서 두 함정 해결: spin.min.js 미로드(Spinner 예외)·CSRF meta 누락([[admin-screen-pitfalls]]).
    - **지자체별통계(17201)=완료(2026-10-01)**: AS-IS analysis/locgov.jsp(차트 없는 서버렌더 표+요약) 이식. donation `DesignatedStatRepository.selectLocgovSummary/selectLocgovList`(요약 4종+지자체별 집계, 라이브검증: 총목표 26.9억·총모금 16,662,000·달성율 0.61%·기부건수 5)+`DesignatedStatService.locgovSummary/locgovList`+API 2종(/analysis/locgov/summary·/list). admin `DesignatedProjectClient.locgovStatSummary/locgovStatList`+컨트롤러 `/designated-projects/analysis/locgov`(메모리 페이징·지자체담당자 자기locgov 강제)+템플릿 `designated/analysis-locgov.html`(필터 지자체/사업구분/기간+퀵버튼/상태, 요약표, 지자체별 집계표, 페이징, 행클릭 딥링크; jquery+op.common+CSRF meta). 메뉴 17201→/analysis/locgov 재배선(적용됨). 양쪽 bootJar 빌드OK, **재기동 대기**.
    - **기부통계 '전체'(6706)=완료(2026-10-01)**: AS-IS give/statistics/all/detail.jsp(요약3카드+Chart.js 5종: 월별 건수 line·online/offline stacked bar·인원 line·금액 line·시간대별 line) 이식. donation `GiveStatRepository`(allSummary/allByMonth/allByHour)+`GiveStatService`+`GiveStatAdminApiController`(/api/give-statistics/admin/all/{summary,{year}/month,{date}/hour}, 내부시크릿보호 경로 추가). admin `GiveStatClient`+`GiveStatisticsController`에 `/give-statistics/all` 페이지(요약 서버렌더)+월/시간 ajax({isSuccess,data})+템플릿 `give/give-statistics-all.html`(op.chart+jquery+op.common+spin+CSRF meta). 라이브검증: 2026 요약 31건/9명/22,482,000원. 메뉴 6706→/give-statistics/all 재배선(적용). bootJar OK, **재기동 대기**. 스키마치환: 연=substr(cntr_de,1,4)(STTEMNT_PAY_DE NULL이라), 확정=COMPLETED, 시=substr(frst_regist_pnttm,9,2), online=path '100'/offline '200','300'.
    - **기부통계 '전체' 차트 미표시 미해결(2026-10-01 저녁)**: 재기동 후 요약은 떠도 Chart 5종 안 뜸. 서버측 전부 정상 검증(donation API 200, 스크립트 5종 200, 페이지 렌더). 월/시간 ajax가 서버 미도달(감사로그 0)=브라우저 $.post 전 JS 중단 추정. **사용자가 다음날 F12 Console 에러+Network month 요청 status 확인 예정**. (designated month은 th:inline CDATA였고 이 화면은 plain script인 차이가 단서 후보.)
    - **기부통계 '지자체별'(6707)=완료(2026-10-01)**: AS-IS give/statistics/locgov/list.jsp(차트 없는 서버렌더 표) 이식. donation `GiveStatRepository.locgovList`(연도+지자체 그룹, getGivePersonStatisticsList 포팅)+`GiveStatService.locgovList`+API /api/give-statistics/admin/locgov/list. admin `GiveStatClient.locgovList`+`GiveStatisticsController` `/give-statistics/locgov`(정렬 person/amt/cnt·메모리페이징·지자체담당자 스코프)+템플릿 `give/give-statistics-locgov.html`(필터 지자체/년도, 표 No/년도/지자체/인원/금액/건수, 페이징; 로컬 캐스케이딩, **ajax 없음**이라 전체 화면 버그와 무관). 라이브검증 OK. 메뉴 6707→/give-statistics/locgov 재배선(적용). bootJar OK, 재기동 대기. **보류**: 지자체명 드릴다운(월별 detail), 엑셀.
    - 잔여: 기부통계 **운영현황(6901)**(현재 통합 /give-statistics 유지), shop-statistics report/dashboard/sales, 관심지자체(6801). 기존 `/give-statistics` 통합화면은 6901 분리 완료 전까지 유지.
  - **B3 로깅 선결**: 접속통계(6101)·방문자접속경로(6102).
  - **⏸ 외국인기부통계(6711/6712)**: TO-BE가 "시스템에 내/외국인 구분 자체가 없어 제외" 명시 → 데이터 부재. 사용자 판단 대기.
  - **B4 숨김재현**: 온라인입금계좌(1202)·카드혜택(1203)[결제설정 숨김], 특산물·소식지·카드뉴스[front+back+admin, 숨김].
  - 통합검색(totalsearch)=보류(AS-IS도 서버 비활성·미완성).
- 통합 원장 docs/coverage-ledger.md.

## 23. 배치 job 이식 원장

(원래 `docs/as-is-parity.md 23절(배치 job 이식 원장)`, 2026-10-20 흡수)

## 배치 job 이식 원장 (2026-10-15)

기준: AS-IS `OP_BATCH_JOB` 실데이터 76행(`Desktop/.../1. DB/_op_batch_job__202610071247.sql`) + `ghlove-batch` `JobServiceImpl`(1,097줄)·`JobPsintServiceImpl`.
AS-IS는 `SchedulerServiceImpl.invokeJobChecker`가 15초마다 OP_BATCH_JOB을 읽어 Quartz에 등록/재등록/중지한다
(`BATCH_STATUS` 1=실행/2=중지, `TRIGGER_TYPE` 1=심플(초)/2=크론, 바뀌면 `BATCH_APPLY_FLAG=0`) - **관리 화면(1403) 설정이 곧 스케줄**이다.

TO-BE 현황: `admin.op_batch_job` 5행. 실제로 도는 것은 각 서비스에 cron을 박아 둔 `@Scheduled` 5개이고 화면 설정은 스케줄에 영향이 없다.
실행 결과는 `BatchExecutionReporter`(donation·point·order) → admin `op_batch_execution`(7209 화면).

### A. AS-IS 사용 중(BATCH_STATUS=1) - 41행

| id | job_method | 내용 | 소유 서비스 | 외부연계 | TO-BE |
|---|---|---|---|---|---|
| 26 | deleteCntrDataBatch | 서울 미결제분 삭제 00:03 | donation | - | ✅ 이식됨 |
| 29 | deleteNotSunapStndBatch | 지방세외 미납 삭제 00:05 | donation | - | ✅ 이식됨 |
| 2000113 | allDayOffItemUpdateBatch | 오프라인 미수납 답례품 상태 03:30 | order | - | ✅ 이식됨 |
| 3000000 | dsgnBizStatusUpdateBatch | 특정기부사업 모금완료·기간만료 종료 30분마다 | donation | - | △ `DesignatedAdminService.closeExpiredProjects`(1000, 매일 01:00) - 주기·job_method 다름, 로직 대조 필요 |
| 2000109 | cntrSunapBatch | 차세대 수납결과 → 수납완료·기탁서 답례품 주문 5분마다 | donation(+order) | 차세대 세외 | ❌ (임시 "수납처리" 버튼으로 대체 중) |
| 34 | fiveMinuteSunapCheckBatch | 세외 수납결과 실시간 반영 5분마다 | donation | 세외 | ❌ |
| 35 | dayCompleteSunapCheckBatch | 세외 수납결과 일정산 23:40 | donation | 세외 | ❌ |
| 2000105 | getNoBugaLocgovListBatch | 3일 기부내역 없는 지자체 월·목 09:00 | donation | (알림 발송 확인 필요) | ❌ |
| 2000114 | dailyGramtInsertBatch | 매일 총 기부금(G_DY_GRAMT) 03:00 | donation | - | ❌ |
| 2000103 | confirmdonationForReportBatch | 보고용 대시보드(REPORT_DASHBOARD) 18:00 | admin(+donation·point·gift·member) | - | ❌ (메인 "기부현황 복사" 0의 원인) |
| 2000111 | statisticsReportBatch | 월간 통계 현황보고 매월 1일 07:00 | ? | - | ❌ 통계표 TO-BE에 없음 |
| 2000001/2000106/2000107 | userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch | 농협 연계 04:25/04:30/04:35 | admin? | 농협 | ❌ (`NhDataClient` 있음) |
| 2000120 | sendNuri2PresentOrder | 답례품 주문내역 알림톡 09:00 | order→admin(nuri2) | 누리2 알림톡 | ❌ (nuri2 표는 admin에 있음) |
| 2000119 | updateWaitUserCacheBatch | 넷퍼넬 대기자 캐시 5분마다 | ? | NetFunnel | ❌ |
| 36 | itemDisplayContorlBatch | 마감일자 공개여부 02:10 | donation? | - | ❌ |
| 10 | autoConfirmPurchaseBatch | 구매확정(배송완료 7일) 01:20 | order(+point) | - | ❌ |
| 1000001 | autoShippingCompleteBatch | 배송완료(배송시작 7일) 01:10 | order | - | ❌ |
| 1000005 | autoSmartDeliveryShippingCompleteBatch | 스마트택배 배송완료 23:30 | order | 스마트택배 | ❌ (`SmartDeliveryClient` 있음) |
| 1000002 | deleteItemNewBatch | 상품 NEW 삭제 00:10 | gift | - | ❌ |
| 1000003 | deleteCartBatch | 장바구니 30일 삭제 00:10 | order | - | ❌ |
| 1000004 | sendRemittanceExpectedMsgBatch | 정산예정 문자 매월 1일 09:00 | order/admin | 문자 | ❌ |
| 3000001~3000020 | bix5* 20개 | BIX5 통계 적재 | - | (외부 BI) | ❌ BIX5 표 TO-BE에 없음 |

### B. AS-IS 중지(BATCH_STATUS=2) - 35행

- ghlove 고유 로직: statisticsYearReport·AgeMonth·Psint·Goods(연간통계 4), opUserBirthdayStatBatch, cntrTaxTempBatch, confirmSunapForDeletedListBatch,
  updateLockForManagerBatch, sendNtsEreceipt(On/Off/Old/One/전체 5), expirationPointBatch·expirationPointSendMessageBatch(포인트 만료 - TO-BE는 1001이 매일 돌고 있음 ★),
  processInactiveUserBatch·sendMailToInactiveUserBatch(휴면), cntrBugaTestBatch(테스트)
- SalesOn 원제품: 캠페인 3, 카카오 알림톡 업데이트, UMS 2, 쿠폰 2, 랭킹 3, 회원등급, 자동완성, 품절옵션, 입금지연 취소, 판매자 주문 SMS, 포인트 만료 메시지
- 다수가 JobServiceImpl에서 본문이 비어 있다("미사용 주석처리").

### 확인된 구조 차이

1. 스케줄 주체: AS-IS = OP_BATCH_JOB(화면) / TO-BE = 코드 고정 cron. 화면에서 중지·cron 변경이 반영되지 않는다.
2. TO-BE 등록 job_method가 AS-IS와 다른 것: 1000 `DesignatedAdminService.closeExpiredProjects`(AS-IS dsgnBizStatusUpdateBatch), 1001 `PointService.runExpirationBatch`(AS-IS는 포인트 만료 배치가 **중지** 상태).

### 결정 (2026-10-15 사용자)

1. **실행 방식 = AS-IS처럼 화면(op_batch_job) 설정으로.** 각 서비스가 admin의 op_batch_job을 주기적으로 읽어 자기 job_method의 사용/중지·cron·심플주기를 반영한다.
2. **중지 job = 76행 전부 AS-IS 상태로 등록.** 고향사랑 고유 로직(연간통계·국세청 영수증·관리자 잠금 등)은 구현하되 중지 유지, SalesOn 원제품 job은 행만 등록·기록.
3. **BIX5 20개 = 행만 등록, 적재 로직은 보류**(외부 BI·DA 설계 후).
4. **포인트 소멸 = AS-IS처럼 중지.** TO-BE 1001(PointService.runExpirationBatch, 매일 실행)을 AS-IS job_method·중지 상태로 바꾼다.

### 진행

#### 1단계 - 실행 구조 전환 (2026-10-15 완료)
- admin `BatchJobInternalApiController`: `GET /api/admin/batch-jobs`(설정 목록), `POST /api/admin/batch-jobs/{id}/applied`(적용여부 '1'). 화면 수정 저장 시 적용여부 '0'(AS-IS updateBatchJob).
- 서비스별 `BatchJobScheduler` + `BatchJobSource`(donation·point·order, 공유모듈이 없어 같은 클래스 복제): 15초마다 설정 조회 →
  자기 job_method만 등록/재등록/해제(1=실행·2=중지, 트리거 1=심플(값은 밀리초, 고정주기)·2=크론), 반영 후 적용여부 '1'.
  실행로그는 AS-IS Aspect처럼 이름이 Batch로 끝나는 job만, 배치구분 `JobServiceImpl.메서드()`.
- 수동 실행 `POST /api/admin/batch/run/{jobMethod}`(AS-IS ManualBatchController) - 각 서비스.
- 기존 고정 @Scheduled 4개 제거 → `DonationBatchJobs`(deleteCntrDataBatch·deleteNotSunapStndBatch·dsgnBizStatusUpdateBatch),
  `PointBatchJobs`(expirationPointBatch, 중지), `OrderBatchJobs`(allDayOffItemUpdateBatch).
- dsgnBizStatusUpdateBatch는 AS-IS 쿼리로 새로 구현(목표금액 달성 + 기간만료, 공개분만, 이력 없음) - 예전 closeExpiredProjects는 기간만료만 봤다.
- 데이터: `migration-admin-op-batch-job-asis-load.sql`로 76행 적재(적용여부 0으로 시작).
- 검증: 기동 15초 내 소유 5행 적용여부 1 / 화면에서 cron을 매분으로 바꾸자 재등록·13:23 실행·7209에
  `JobServiceImpl.dsgnBizStatusUpdateBatch()` 정상 "종료 0건" 기록 → 원복.

#### 2단계 - job 이식 1차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| confirmdonationForReportBatch | admin `AdminMainService.reportDashboardBatch` | 메인 하단표·등록답례품·콜·전체회원 → REPORT_DASHBOARD(오늘 지우고 넣기). 메인 "기부현황 복사" 0 해소 |
| dailyGramtInsertBatch | donation `DonationBatchService.insertDailyGramt` | G_DY_GRAMT 적재(AS-IS 화면은 더 이상 안 읽음) |
| itemDisplayContorlBatch | gift `GiftBatchService.closeDisplayOfEndedItems` | 마감일 어제인 공개 답례품 비공개 |
| deleteItemNewBatch | gift `GiftBatchService.clearNewLabel` | NEW 라벨 15일 경과 해제 |
| deleteCartBatch | order `OrderBatchService.deleteOldCartItems` | od_cart_item 30일 경과 삭제 |
| dsgnBizStatusUpdateBatch | donation `DesignatedAdminService.updateDesignatedDonationStatus` | 1단계에서 AS-IS 쿼리로 재구현 |

admin·gift에 스케줄러 추가(@EnableScheduling 신규, admin은 리포지토리 직접). 시퀀스 `op_batch_job_batch_job_id_seq`를 max로 보정.
검증: 여분 포트에서 수동 실행 5건 정상·7209 기록·적용여부 1, get-user 응답에 값(기부 34·회원 65·답례품 23·콜 126).

#### 남은 사용 중 job (다음)
- 세외 수납: cntrSunapBatch(procOffOrder) · fiveMinuteSunapCheckBatch · dayCompleteSunapCheckBatch - 이식 시 기탁서 임시 "수납처리" 버튼 제거
- 주문: autoConfirmPurchaseBatch · autoShippingCompleteBatch · autoSmartDeliveryShippingCompleteBatch
- 연계: userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch(농협) · sendNuri2PresentOrder(누리2 알림톡) · updateWaitUserCacheBatch(넷퍼넬)
- 기타: sendRemittanceExpectedMsgBatch · statisticsReportBatch(통계표 없음) · getNoBugaLocgovListBatch(★수신자 결정 필요)
- 중지 상태 고향사랑 로직(연간통계 4·국세청 영수증 5·관리자 잠금 등)

#### 2단계 - job 이식 2차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| getNoBugaLocgovListBatch | admin `NoBugaLocgovMailService` (+ donation `GET /api/admin/batch/no-buga-locgovs`) | AS-IS 수신자 8명 그대로(사용자 결정), 로컬은 `ghlove.batch.no-buga-mail.test-mode=true` → `test-recipients`(비면 등록만). 관리 화면 "농협배치테스트" 버튼도 이 본문(AS-IS batchForNh) |
| fiveMinuteSunapCheckBatch · dayCompleteSunapCheckBatch | donation `DonationBatchJobs.sunapCheck` + `DonationBatchService` | 대상 SQL·서울 이택스/차세대 수납확인 전문(`LocalTaxClient.sendEtaxSunapInfo`·`sendLocalSunapConfirm`, local 프로파일은 AS-IS LOCAL 가짜 "납부됨")·성공 시 변경신청 300 등록+승인·G_NEXT_SUNAP_RESPONSE "시스템 수납처리"·수납일=기부일자·SUNAP_PROCSS_YN='Y' |
| cntrSunapBatch | order `OrderBatchJobs.processPaidOffgiveOrders` | AS-IS procOffOrder = TO-BE 포인트 적립 이벤트 경로와 같은 처리를 5분마다 폴링(적립 존재 = 수납완료). 이벤트가 놓친 건 보정 |

- 기탁서 상세의 임시 "수납처리" 버튼·안내문·`POST /offgive/{sn}/confirm`·OffgiveClient.confirm·donation `/api/offgive/{sn}/confirm`·OffgiveService.confirm 제거.
- ★로컬(local 프로파일)에서는 AS-IS처럼 5분 안에 신고 기부가 자동 수납처리된다(AS-IS LOCAL 분기가 "납부됨"으로 응답).

#### 2단계 - job 이식 3차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| autoShippingCompleteBatch | order `OrderBatchService.autoShippingComplete` | 배송완료 기준일(OP_CONFIG, admin `GET /api/admin/shop-config/batch-days`) 지난 SHIPPED·IN_TRANSIT(AS-IS 30) → DELIVERED + 배송상태 이벤트 |
| autoConfirmPurchaseBatch | order `OrderBatchService.autoConfirmPurchase` | 구매확정 기준일 지난 DELIVERED → CONFIRMED. AS-IS 정산예정일·SalesOn 구매포인트는 TO-BE 대응 없음 |
| autoSmartDeliveryShippingCompleteBatch | admin `SmartDeliveryBatchService` + order `/api/admin/orders/batch/*` | 스마트택배 기준일·택배사→SMART_DELIVERY_COMPANY(MAPPING_CODE) 코드·level 6이면 배송완료. `SmartDeliveryClient.trackingInfo`(AS-IS sweettracker.url 내부 연계서버, disabled면 빈 응답 = 처리 없음) |

- OP_CONFIG 기준일이 TO-BE에서 비어 있던 것을 AS-IS 값으로(배송완료 7·스마트택배 2, `migration-admin-op-config-batch-days-asis.sql`).
- ★구조 문제: TO-BE 송장 등록은 택배사를 자유 문자열("CJ")로 저장하는데 AS-IS는 배송업체 ID(OP_DELIVERY_COMPANY)다 →
  스마트택배 코드 매핑이 안 돼 현재 데이터로는 스마트택배 배치가 늘 건너뛴다(송장 등록 화면·API 쪽 정리 필요).
- AS-IS 교환배송중(55)→교환배송완료(58)는 TO-BE 교환이 클레임 흐름이라 주문 배송상태에 대응 값이 없다.

#### 2단계 - job 이식 4차: 외부 연계 (2026-10-19)
| job | 위치 | 비고 |
|---|---|---|
| userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch | admin `NhBatchService` + `NhDataClient`(재작성) | AS-IS analysis DB(운영 CUBRID ghlove_analysis) `stat_cntr_mber`·`stat_cntr_mber_loc`·`stat_locgov` 전부 지우고 다시 넣기. 원천 donation `/api/admin/batch/nh/*` + member `/api/admin/members/batch/ci-map`(CI 있는 회원만 = AS-IS INNER JOIN). disabled면 건수만. ★운영에서 켜려면 CUBRID JDBC 드라이버 필요 |
| sendNuri2PresentOrder | admin `Nuri2PresentOrderService` + order `/api/admin/orders/batch/present-order-counts` | 어제 0시 이후 결제완료(배송 전) 품목을 판매자별로 세어 사용(2) 판매자 휴대폰으로 NURI2_NRMSG_DATA에 KR002 알림톡 등록. sender-key는 배포 설정(저장소에 두지 않음) |

- **제거한 TO-BE 발명**: NH 반출 화면 `/batch/nh-export`(메뉴 없음)·NhExportBatchService·NhExportLog·NhExportRow(기부 건별 NH_USER_CNTR) - AS-IS에 없는 구조. 빈 표 `admin.nh_export_log`는 남김(DROP은 사용자 판단).
- **보류(기록만, 행은 등록·적용여부 0)**
  - updateWaitUserCacheBatch(넷퍼넬): AS-IS도 배치 서버 JVM 메모리 캐시만 갱신하고, 읽는 API(getRealTimeWaitUser)는 다른 JVM이며 프론트 호출부도 0 - 효과가 없는 job. TO-BE에 NetFunnel 없음.
  - sendRemittanceExpectedMsgBatch(정산예정 문자): 품목의 정산예정일·정산ID에 의존 - 정산 영역(판매자 셀프서비스 P1~P3 보류) 이식 때 함께.
  - statisticsReportBatch(월간 통계) + 중지 상태 statisticsYearReport* 4: AS-IS 분석 DB에 통계를 쌓고 현황보고 메뉴(6223~6227)가 그걸 읽는다. TO-BE 현황보고는 admin 조회모델(DonationLedger)로 구현돼 있어 "분석 DB vs 조회모델" 결정 필요 → DA 설계([[da-design-pending]])와 함께(BIX5와 같은 성격).

#### 송장 택배사 = 배송업체 ID 전환 (2026-10-19, 사용자 지시)
- order `OrderService.registerInvoice`: 택배사를 사용 중(USE_FLAG='Y') 배송업체 ID로만 받는다. '-선택-'(0)·옛 코드("CJ" 등)는
  AS-IS 문구 "배송업체를 선택해 주세요"로 거절. `DeliveryCompanyService.active()` 정렬을 AS-IS getActiveDeliveryCompanyListAll(ID 내림차순)로.
- admin 주문 상세 송장 등록 select: 하드코딩 4종(CJ/POST/HANJIN/LOTTE) → "-선택-" + 사용 중 배송업체(AS-IS shipping-ready.jsp와 같은 값).
  등록된 택배사는 업체명으로 표시.
- 데이터: `migration-order-carrier-code-delivery-company-id.sql`(CJ→2, POST→3, HANJIN→4, LOTTE→5) - od_order 4건.
  이제 SMART_DELIVERY_COMPANY 매핑(2→04, 3→01)이 맞아 스마트택배 배치가 조회까지 간다.
- 판매자 출고 화면(AS-IS seller 송장등록)은 판매자 셀프서비스 P1(출고) 보류 범위라 TO-BE에 아직 없다.

#### 2단계 - job 이식 5차: AS-IS 중지 상태 고향사랑 고유 job (2026-10-19)
전부 **구현만 하고 중지 유지**(AS-IS BATCH_STATUS=2 그대로). 화면에서 사용으로 바꾸거나 수동 실행(`POST /api/admin/batch/run/{jobMethod}`)하면 동작한다.

| job | 위치 | 비고 |
|---|---|---|
| sendNtsEreceiptOnBatch · OffBatch · BatchOld | donation `DonationBatchJobs` → `DonationBatchService.ntsTargets/recordNts` + `NtsClient.relayAccessToken/sendEreceipt` | AS-IS NgDonationBatchServiceImpl 중계서버 전문(RSA(CI)·RSA(사업자번호)). 주소·IP는 AS-IS 값, 키·ID·비밀번호는 배포 설정(빈 값) |
| cntrTaxTempBatch | donation `DonationBatchService.taxTemp*`·`insertTaxLog/updateTaxLog` | conbCd 43/44, 2024.02 추가 항목 elcrAplcd·elcrAdmNo |
| confirmSunapForDeletedListBatch | donation `deletedSunapTargets/completeSunapForDeleted/markSunapRecheckedUnpaid` | ★미납 재확인 표시가 AS-IS 리터럴 '900' - TO-BE 상태코드 체계에 대응 값이 없어 그대로 둠(지뢰) |
| updateLockForManagerBatch | admin `ManagerLockBatchService` | ISMS UNUSED_MANAGER 일수, 실패횟수 10000으로 잠금 |
| opUserBirthdayStatBatch | member `MemberBatchJobs`(member에 스케줄러 신설·@EnableScheduling) | OP_USER_BIRTHDAY 적재(통계 DB 부분은 AS-IS도 주석). 실측: 1회 34건·재실행 0건(테스트 후 표 비움) |
| expirationPointSendMessageBatch | admin `PointExpirationMailService` + point `GET /api/admin/batch/expiring-points` + member `GET /api/admin/members/batch/mail-targets` | 만료 90일 전 잔여 포인트(TO-BE 적립 원장 = AS-IS G_CNTR 잔여·만료일)를 회원·만료일별로 합쳐 `expiration_point` 템플릿으로 EMS. 템플릿 buyer_send_flag가 AS-IS 실데이터 'N'이라 실제로는 발송 안 됨(AS-IS 동일). 정상·휴면만. TO-BE 휴면은 OP_USER에 그대로 있어 복호화 단계 없음 |

- **행만(AS-IS에 메서드 없음)**: sendNtsEreceiptBatch · sendNtsEreceiptOneBatch · cntrBugaTestBatch - JobServiceImpl에 구현이 없어 레지스트리에 넣지 않음.
- **보류 - 휴면(processInactiveUserBatch · sendMailToInactiveUserBatch)**: AS-IS `setSleepUser`는 개인정보를 OP_USER_SLEEP로 암호화 이관 후 OP_USER/OP_USER_DETAIL을 비우는데,
  ① AS-IS SQL 자체가 깨져 있다(user-mapper.xml `updateUserDetailToSleep` 1603행 `RECEIVE_EMAIL = '1'` 뒤 쉼표 누락 → 실행 시 오류),
  ② TO-BE 휴면은 OP_USER에 정보를 둔 채 상태만 DORMANT라, 그대로 이식하면 휴면 해제(wakeup)가 OP_USER_SLEEP에서 복원하도록 함께 바뀌어야 한다.
  → 별도 과제로 사용자 결정 대기.

### 남은 사항 - 결정 후 처리 (2026-10-19 배치 작업 일단 종료, 사용자 지시)
| # | 항목 | 필요한 결정/조치 |
|---|---|---|
| 1 | 휴면 전환·안내메일(processInactiveUserBatch·sendMailToInactiveUserBatch) | AS-IS SQL 쉼표 누락 + OP_USER_SLEEP 이관 시 wakeup 복원 변경 필요. A 별도 과제(추천)/B 지금 이식 |
| 2 | confirmSunapForDeletedListBatch의 '900' 리터럴 | TO-BE 상태코드 대응값 결정 전엔 켜지 말 것 |
| 3 | 3일 기부내역 없는 지자체 메일 테스트 수신자 | 사용자가 주소 알려주면 `ghlove.batch.no-buga-mail.test-recipients`에 입력 |
| 4 | 농협 연계 3종 운영 활성화 | CUBRID JDBC 드라이버 추가 필요 |
| 5 | 국세청 영수증·누리2·EMS 등 연계 비밀값 | 배포 설정에 주입(저장소에 두지 않음) |
| 6 | 통계 분석DB(statisticsReportBatch·statisticsYearReport* 4)·BIX5 20 | DA 설계(분석DB vs 조회모델) 후 |
| 7 | sendRemittanceExpectedMsgBatch(정산예정 문자) | 정산 영역(판매자 셀프서비스 P1~P3) 이식 때 |
| 8 | updateWaitUserCacheBatch(넷퍼넬) | AS-IS에서도 효과 없음 - 행만 유지, 추가 조치 여부 결정 |
| 9 | 판매자 출고(송장 등록) 화면 | 판매자 셀프서비스 P1 보류 범위 |
| 10 | 빈 표 admin.nh_export_log | DROP 여부 사용자 판단 |

## 24. admin 발명 링크 정리 원장

(원래 `docs/as-is-parity.md 24절(admin 발명 링크 정리 원장)`, 2026-10-20 흡수)

## admin 상단 "← …" 되돌아가기 링크 정리 원장 (화면별)

작성 2026-10-08. 전수 조사는 끝났고, **정리는 화면별로** 한다(다른 개발자와 동시 수정 충돌을
피하려는 사용자 결정). 각 화면을 담당 영역 작업할 때 이 표의 "조치"만 수행하면 된다.

### 무엇이 문제인가

TO-BE admin 템플릿 21곳이 제목 위에 `<p><a>&larr; …</a></p>` 형태의 되돌아가기 링크를 갖고 있다.
**AS-IS opmanager view 트리 전체에 이 패턴은 0건이다.**

```
$ grep -rn '&larr;\|←' ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n --include=*.jsp | wc -l
0
```

AS-IS가 상세화면에서 목록으로 돌아가는 방법은 **화면 하단 `.btn_all` 영역의 `목록` 버튼**이다
(예: `give/give-operation/detail.jsp:237`, `give/give-point/form.jsp:214`,
`give/give-state/form.jsp:466`). 즉 조치는 "링크 삭제"가 아니라 경우에 따라
**"링크 삭제 + AS-IS 하단 목록 버튼 추가"** 다. 링크만 지우면 화면 안에서 목록으로 갈 길이 없어진다
(LNB는 남지만 AS-IS와 다른 상태가 된다).

[[asis-porting-principles]] / [[asis-porting-principles]] 위반 사례다.

### 이미 처리한 화면 (2026-10-08)

| 화면 | 메뉴 | 조치 | 비고 |
|---|---|---|---|
| `designated/analysis-locgov.html` | 17201 지자체별통계 | 링크 삭제 | AS-IS `.location`만. 하단 목록 버튼 AS-IS에도 없음(통계 목록 화면이라 불필요) |
| `designated/analysis-month.html` | 17202 월별통계 | 링크 삭제 | 동일 |

### 남은 화면 (19곳)

"하단목록" = TO-BE 템플릿에 `목록` 버튼이 이미 있는지. "AS-IS 목록버튼" = AS-IS 짝 JSP에 있는지.

#### A. 링크만 삭제하면 끝 (가장 안전)

| 화면 | 메뉴 | AS-IS 짝 | 하단목록 | 조치 |
|---|---|---|---|---|
| `give/give-operation-detail.html` | 14104 기부금 운용정보 | `give/give-operation/detail.jsp` | 있음 | 상단 링크 삭제 |

#### B. 링크 삭제 + AS-IS 하단 `목록` 버튼 추가

| 화면 | 메뉴 | AS-IS 짝 | AS-IS 목록버튼 | 조치 |
|---|---|---|---|---|
| `give/give-point-detail.html` | 14102 기부포인트현황 | `give/give-point/form.jsp` | `:214` | 링크 삭제 + 하단 목록 버튼 |
| `give/give-state-detail.html` | 14101/14103 기부금모금현황 | `give/give-state/form.jsp` | `:466` | 링크 삭제 + 하단 목록 버튼 |
| `order-admin/detail.html` | 16401~16409 주문관리 | `order/detail/order-detail.jsp` | 있음 | 링크 삭제 + 하단 목록 버튼 |
| `statistics-locgov/detail.html` | 6801 관심지자체 | `statistics/locgov/detail.jsp` | 있음 | 링크 삭제 + 하단 목록 버튼 |

#### C. AS-IS 실화면 확인이 먼저 필요 (AS-IS 짝에 목록 버튼이 없음)

AS-IS가 팝업으로 띄우는지, 브라우저 뒤로가기에 의존하는지 실화면을 봐야 판정된다.

| 화면 | 메뉴 | AS-IS 짝 | AS-IS 목록버튼 |
|---|---|---|---|
| `shop-statistics/sales-area-detail.html` | 메뉴 없음 | `shop-statistics/sales/area-detail.jsp` | 없음 |
| `shop-statistics/sales-brand-detail.html` | 메뉴 없음 | `shop-statistics/sales/brand-detail.jsp` | 없음 |
| `shop-statistics/sales-item-detail.html` | 메뉴 없음 | `shop-statistics/sales/item-detail.jsp` | 없음 |
| `shop-statistics/sales-user-detail.html` | 메뉴 없음 | `shop-statistics/sales/user-order.jsp` | 없음 |
| `shop-statistics/sales-locgov-detail.html` | 6502 지자체별 | `shop-statistics/sales/locgov.jsp` | 있음 |
| `order-admin/claim-queue.html` | 16451~16454 (`/admin/claims`) | `order/detail/cancel-*`·`return-*` 계열 | 미확정 |
| `settlements/detail.html` | 16502/16503 (`/settlements`) | AS-IS에 `settlement` 디렉터리 없음(`sellerconfirm*` 후보) | 미확정 |
| `settlements/pending.html` | 16501 정산예정 내역 | 동일 | 미확정 |

#### D. 접근 경로가 없어 링크 자체가 사문 — 영역 작업 때 함께 판정

| 화면 | 상태 |
|---|---|
| `coupon/offline-codes.html` | `op_menu`에 `/coupon` 없음. [[domain-code-facts]] 로 진입점을 숨긴 상태 |
| `coupon/regular-list.html` | 동일 |
| `coupon/target-items.html` | 동일 |
| `coupon/target-users.html` | 동일 |
| `coupon/usage.html` | 동일 |

#### E. 별건 — TO-BE 전용 발명 화면 (링크 문제가 아님)

| 화면 | 사실 |
|---|---|
| `designated/analysis.html` (`/designated-projects/analysis`, 제목 "지정기부 모금분석") | `op_menu`에 없고 AS-IS `designated-donation/analysis/` 에는 `locgov.jsp`·`month.jsp` 둘뿐이다. 즉 **AS-IS에 없는 화면**이다. 링크만 지울 게 아니라 화면 존치 여부를 사용자에게 확인해야 한다 |

### 재확인 명령

```bash
## 남은 곳 세기
grep -rn '&larr;' admin/src/main/resources/templates/ | wc -l

## AS-IS에 이 패턴이 없다는 근거
grep -rn '&larr;\|←' <AS-IS>/ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n --include=*.jsp | wc -l   # 0

## 메뉴 등록 여부
SELECT menu_id, menu_name, menu_url, status_code FROM admin.op_menu WHERE menu_url LIKE '%<경로>%';
```

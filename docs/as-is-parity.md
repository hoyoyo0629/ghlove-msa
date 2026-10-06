# AS-IS â TO-BE ëì¡° ìì¥

> 2026-10-06ì docs/ ìµìì .md 46ê°ë¥¼ 6ê°ë¡ íµí©íë¤. ì´ íì¼ì **AS-IS ëì¡°Â·ê°­Â·ì»¤ë²ë¦¬ì§ ë¬¸ì 21ê°**ë¥¼ í©ì¹ ê²ì´ê³ ,
> ê° ì  ë¨¸ë¦¬ì `íµí© ì  íì¼`ì´ ìë íì¼ëªì´ë¤(ë´ì©ì ê·¸ëë¡ ì®ê¸°ê³  í¤ë©ë§ í ë¨ê³ ë´ë ¸ë¤ - git ì´ë ¥ì¼ë¡ ìë³¸ ì¶ì  ê°ë¥).
> êµ¬ì± ìì: ê³µíµ ì»¤ë²ë¦¬ì§ â admin â member â donation â gift â order â point.
> ì§ì² ìíë ì´ ë¬¸ìê° ìëë¼ ë©ëª¨ë¦¬ ìì¥(`admin-*-area-port-progress` ë±)ì´ ì ë³¸ì´ë¤.

## ëª©ì°¨

- [1. ì ì²´ ì»¤ë²ë¦¬ì§ ë§µ (ì´ë íë©´ì´ ì´ë ì¸ë²¤í ë¦¬ì)](#1-ì ì²´-ì»¤ë²ë¦¬ì§-ë§µ-ì´ë-íë©´ì´-ì´ë-ì¸ë²¤í ë¦¬ì) â `as-is-coverage-map.md`
- [2. ì ì ì»¤ë²ë¦¬ì§ ìì¥ (íì¼ ë¨ì, ëë½ 0 ëª©í)](#2-ì ì-ì»¤ë²ë¦¬ì§-ìì¥-íì¼-ë¨ì-ëë½-0-ëª©í) â `coverage-ledger.md`
- [3. ëë¯¼ SPA ì¶©ì¤ë ê°ì¬](#3-ëë¯¼-spa-ì¶©ì¤ë-ê°ì¬) â `as-is-fidelity-audit-2026-09-17.md`
- [4. admin ê¸°ë¥ ëì¡°](#4-admin-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-admin.md`
- [5. admin parity ê°ì¬](#5-admin-parity-ê°ì¬) â `admin-parity-audit.md`
- [6. admin ë©ë´í¸ë¦¬ parity](#6-admin-ë©ë´í¸ë¦¬-parity) â `admin-menu-tree-parity.md`
- [7. admin ê°­ ì¬ì¸µê°ì¬ part1](#7-admin-ê°­-ì¬ì¸µê°ì¬-part1) â `as-is-admin-gap-deep-audit-part1.md`
- [8. admin ê°­ ì¬ì¸µê°ì¬ part2 (ë°°ì¹D)](#8-admin-ê°­-ì¬ì¸µê°ì¬-part2-ë°°ì¹d) â `as-is-admin-gap-deep-audit-part2.md`
- [9. ìë¡ë íì¼ ê·ì¹ parity (íì¥ìÂ·ì©ëÂ·íì¼ì­ì )](#9-ìë¡ë-íì¼-ê·ì¹-parity-íì¥ìÂ·ì©ëÂ·íì¼ì­ì ) â `upload-file-parity-audit.md`
- [10. member ê¸°ë¥ ëì¡°](#10-member-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-member.md`
- [11. member ìì ì°ëí´ì§ parity](#11-member-ìì-ì°ëí´ì§-parity) â `member-social-unlink-parity-audit.md`
- [12. donation ê¸°ë¥ ëì¡°](#12-donation-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-donation.md`
- [13. donation parity ê°ì¬](#13-donation-parity-ê°ì¬) â `donation-parity-audit.md`
- [14. gift ê¸°ë¥ ëì¡°](#14-gift-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-gift.md`
- [15. gift ìµì ì²´ê³ parity](#15-gift-ìµì-ì²´ê³-parity) â `gift-option-parity-audit.md`
- [16. gift ì¹´íê³ ë¦¬ 3ë¨ í¸ë¦¬ parity](#16-gift-ì¹´íê³ ë¦¬-3ë¨-í¸ë¦¬-parity) â `gift-category-tree-parity-audit.md`
- [17. gift ê²ìì´ ê´ë¦¬ parity](#17-gift-ê²ìì´-ê´ë¦¬-parity) â `gift-search-keyword-parity-audit.md`
- [18. gift ë°°ì¡ë¹ ì ì± parity](#18-gift-ë°°ì¡ë¹-ì ì±-parity) â `gift-delivery-fee-parity-audit.md`
- [19. gift íë§¤ì ìíìë¹ì¤ parity](#19-gift-íë§¤ì-ìíìë¹ì¤-parity) â `gift-seller-selfservice-parity-audit.md`
- [20. order ê¸°ë¥ ëì¡°](#20-order-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-order.md`
- [21. point ê¸°ë¥ ëì¡°](#21-point-ê¸°ë¥-ëì¡°) â `as-is-feature-audit-point.md`

---

## 1. ì ì²´ ì»¤ë²ë¦¬ì§ ë§µ (ì´ë íë©´ì´ ì´ë ì¸ë²¤í ë¦¬ì)

> íµí© ì  íì¼: `docs/as-is-parity.md §1`

## AS-IS 전체 커버리지 맵 — 어느 화면이 어느 인벤토리에 들어가는가

> 6개 MSA 서비스(member/donation/point/gift/order/admin)에 걸리지 않는 영역이 상당하다.
> 누락 없이 다루기 위해 AS-IS 프론트 **최상위 디렉터리 전부**를 도메인에 배정하고,
> 어디에도 안 걸리는 것은 **7번째 시트 `common`** 으로 모은다.
> 작성 2026-09-10. 생사 판정 기준은 [[as-is-inventory-procedure]].

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
| **`policy/`** | 21/21 | **common** | 약관·개인정보처리방침 원문 ([[policy-content-db-migration-deferred]]) |
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

## 2. ì ì ì»¤ë²ë¦¬ì§ ìì¥ (íì¼ ë¨ì, ëë½ 0 ëª©í)

> íµí© ì  íì¼: `docs/coverage-ledger.md`

## AS-IS → TO-BE 전수 커버리지 원장 (누락 0 목표)

작성 2026-10-01. 목적: MSA 전환에서 **front/back/static 어느 것도 누락 없이** 이식됐는지 **파일 단위**로 증명.
기능/화면 레벨 대조(docs/inventory/*.tsv, docs/*-parity-audit.md)는 이미 있으나, 이 원장은 그 위에
**① back 엔드포인트 ② front 뷰파일 ③ static 에셋(css/js/img/font)** 3축을 각 버킷마다 파일 단위로 present/missing/gap 판정한다.
원칙: [[port-everything-no-omissions-then-rfp-isp]] — AS-IS 100% 이식이 기본선, RFP/ISP 변경은 별도 확인.

- AS-IS 소스: `C:\workspace\ghlove` (ghlove-web / ghlove-common / ghlove-api / ghlove-batch / ghlove-frontend).
- AS-IS 코드데이터: [[asis-table-dump-path]].
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
3. **gift/admin 특산물관(speciality)·소식지(catalog·카드뉴스)** — [2026-10-01 결정] front+back+admin **전부 이식하되 AS-IS와 동일하게 TO-BE도 숨김상태로 재현**([[as-is-parity-includes-disabled-state]]): 스토어프론트 GNB 미노출, 소식지 admin메뉴 display='N', 특산 권한 ROLE_ADMIN_3·4 한정. (AS-IS 근거: 프론트 진입점 전무, 소식지 admin숨김·데이터0, 특산 행안부권한만 — 비활성 feature). ※ 제철(seasonal)은 TO-BE 구현완료(SeasonalView), 별개.
4. **admin**: 메뉴트리 라운드의 ❌ 갭들(특정사업 통계/배너, 외국인기부, 만족도조사, 기부혜택증 열람현황, 방문자접속경로, 배송업체·온라인입금·카드혜택, 기금사업소개, 답례품Q&A, 배치로그·공통메시지) — 각 AS-IS 활성/숨김 상태대로 이식.

**결정된 보류/도메인 N/A (갭 아님):**
- order PG결제(easypay/nicepay/naverpay): 답례품=기부포인트 단일결제라 미사용.
- point 예약: TO-BE 전용·미사용 결정.
- policy 원문 DB이관: 운영데이터 대기.

**저위험 교차확인 TODO:** member 전자서명(signRegister), donation featured(지역이벤트) 뷰, gift community-business/store back.

### ★ B5 소급교정: admin 통계 화면 차트화 (2026-10-01)
[[copy-asis-css-js-assets-verbatim]] — AS-IS는 admin 통계 화면 다수가 Chart.js 차트인데, TO-BE admin엔 Chart.js가
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
3. **static**: 각 live 화면이 참조하는 css/js/img/font를 추출 → TO-BE에 실제 파일이 있는지 확인. ([[static-asset-scoping-2026-09-18]] 누락복사 전례)
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
> 단 **전자서명(signRegister/signRemove/checkSign, user/UserController)** 은 TO-BE 미구현일 수 있어 point/member deferred와 교차확인 필요(저위험·[[member-service-deferred-items]] 전자서명 보류건과 동일 가능성).
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
→ 핵심 기부접수·지정기부·관심·영수증·마이 = ✅. **납부 게이트웨이(NTS/지방세/서울 etax·지로) ~50 엔드포인트 = ⏸ 보류**([[donation-service-deferred-items]] PG/납부연계, 외부 세정·PG 연동·DA 대기). 결정된 보류지 silent 누락 아님.

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
- **TO-BE 전용**: 포인트 예약(PointReservationsView·`/api/my/reservations`)은 AS-IS에 없는 TO-BE 추가분, **미사용 결정됨**([[point-reservation-unused-decision-deferred]]) — 갭 아님.
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
| policy(20 약관원문) | donation/PolicyView(slug별) | ✅ (원문 DB이관 [[policy-content-db-migration-deferred]]) |
| guide(사이트맵) | AppFooter 사이트맵 | ✅ |
| error | ErrorView | ✅ |
| main | HomeView | ✅ |
| **totalsearch(통합검색, index.html + TotalSearchController)** | — 없음(검색뷰·라우트 전무) | ❌ **갭(이식대상)** |

static: notices/faq/data-board 라우트 CSS(ct_nov·research-box·main·event) 등 pageStyles 등록·존재. 통합검색 페이지 미이식이라 해당 에셋만 부재.

#### common 결론
게시판(공지·FAQ·1:1·자료실)·설문·이벤트·정책·가이드·오류·메인 **이식 완료**. **재현갭 1건: 통합검색(totalsearch)** — AS-IS live 기능인데 TO-BE 전무(검색뷰·API 없음). 사용자 원칙상 **이식대상**(단 통합검색 범위가 RFP 변경대상인지 교차확인 필요 — coverage-map 주석).

---

#### gift 결론
답례품 핵심(목록·검색·상세·위시·리뷰·문의·제철·마을기업·카테고리) **이식 완료**. **특산물관(speciality)·소식지/카탈로그(catalog·카드뉴스)** 는 AS-IS에서 **비활성/미사용**(프론트 진입점 전무·소식지 admin숨김·데이터0·특산 행안부권한만) → [2026-10-01 결정] **front+back+admin 전부 이식하되 TO-BE도 AS-IS처럼 숨김상태로 재현**([[as-is-parity-includes-disabled-state]]). community-business/store back 저위험 재확인 TODO.


---

## 3. ëë¯¼ SPA ì¶©ì¤ë ê°ì¬

> íµí© ì  íì¼: `docs/as-is-fidelity-audit-2026-09-17.md`

## AS-IS 충실도 감사 (대민 SPA 전환분)

- 작성: 2026-09-17
- 목적: 이미 storefront Vue3 SPA로 전환된 **대민 화면**을 AS-IS 소스와 대조해, 휴면해제·영수증출력에서 드러난 것과 같은 **흐름·문구·에러처리 divergence를 선제 발굴·목록화**한다. 쓰다가 발견하는 반복을 줄이는 게 목적.
- 방법: 화면 단위로 (1) AS-IS 프론트 흐름(`ghlove-frontend/**`, 특히 API 허브 `modules/op.saleson.js`) + (2) AS-IS 서버(`ghlove-web`/`ghlove-api`/`ghlove-common`) + (3) MSA 구현(백엔드 + storefront)을 대조. 라우트·파일명이 아니라 **동작·문구·분기**로 확인([[verify-screen-by-content-not-route]]). `old/`·주석처리 코드는 비활성이므로 라이브 경로만.
- 판정: **버그 / 재현누락(미배선) / 의도적 축소(외부연계·상용SW 대체 등) / 확인필요** 로 구분. 이 프로젝트엔 의도적 축소가 많으므로 함부로 "누락"으로 몰지 않는다.
- 상태: **진행 중(v1).** AUTH(로그인) 우선 완료. 나머지 영역은 하단 커버리지 체크리스트.

---

### 수정 반영 (2026-09-17, 컴파일·빌드 통과 · 런타임 검증 대기 → [[pending-verify-dormancy-receipt]])

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
| A0 | 로그인 휴면(SLEEP_USER) | 수정완료 | - | 2026-09-17 AS-IS와 동일화([[pending-verify-dormancy-receipt]]) |

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
- **본인인증은 전자서명(magicline)/휴대폰 = 의도적 축소.** AS-IS는 `/api/magicline/signedFormRGhlove` 등 외부 인증망으로 본인확인(find-idpw.html:802). MSA는 이 외부연계가 없어 dev bypass로 대체([[member-service-deferred-items]]). → 재현누락 아님, 알려진 보류.
- ⚠ **확인필요(경미):** 비번 재설정 성공 후 이동 — MSA `resetPwSubmit`은 자동로그인 후 `/mypage`. AS-IS가 자동로그인인지 로그인화면 이동인지는 step2 최종 콜백에서 재확인 필요(현재 MSA 주석은 "AS-IS처럼 자동로그인"이라 주장).

### C. 회원가입 — `SignupView.vue` / `AuthApiController`(signup)

이미 인벤토리 전수조사에서 대조·판정된 영역이라([[as-is-inventory-procedure]], [[continue-vs-rebuild-msa-decision]]) 여기서는 재도출하지 않고 요지만 옮긴다.

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
  - "본인인증 미완료상태입니다…"(donation-main.html:995) → MSA는 본인인증(isAuth) 미구현([[member-service-deferred-items]], 인벤토리 갭과 일치).
  - "주민번호를 입력해주세요/뒷자리 오류/주민등록번호 오류"(1110·1117·1244) → MSA는 등록주소 기반이라 주민번호 실검증 없음. **단, juminNo2 입력칸(비밀번호형)은 화면에 있으나 `verifyResidence`가 locgovCode만 보내 값이 안 쓰인다** — 행정정보 연계가 붙기 전까지의 의도적 dead input(코드 주석에 명시). 정보성으로만 남김.
  - 외국인 거소·체류만료(1170~1182) → 외국인 기부 흐름 미구현(축소).

### G. 체크아웃(결제) — `CheckoutView.vue` — 재현누락(중)

AS-IS `order/step1.html` 대조. 서버 quote 재사용(preview=complete 동일계산)·쿠폰 숨김([[coupon-feature-unused-hide-ui]], 문서화)·동의 필수는 충실. 그러나 **결제 전 필드 검증이 AS-IS보다 얕다.**

- **AS-IS**(step1.html)는 결제 직전 각 항목을 alert로 막는다: "주문자 이름이 없습니다."(:1627)·"주문자 주소를 입력해주세요."(:1637)·"주문자 우편번호를 입력해주세요."(:1647/1657)·"주문자 상세주소를 입력해주세요."(:1667)·지자체별 "…포인트가 부족합니다."(:1425)·"구매에 동의해주시기 바랍니다."(:1578).
- **MSA**(`submit`)는 **`form.agree`만 검사**한다. 받으시는분·주소·우편번호 입력칸에 `required`가 붙어 있으나, 결제 버튼이 `<button type="button" @click="submit">`(폼 submit 아님)이라 **HTML5 required가 발동하지 않는다** → 빈 값으로도 결제 시도가 나간다. 포인트 부족도 `insufficient()`는 있으나 `submit`에서 안 쓴다(장바구니→체크아웃 진입 시 `CartView.goCheckout`에서만 검사).
- **심각도**: 서버(`OrderService.checkout`)가 배송지 필수면 서버 에러(errorMessage)로 드러나 조용히 깨지진 않으나, AS-IS의 구체적 필드 안내 대비 UX 저하 + 배송지 없이 주문 생성 가능성(서버 허용 시)이 있어 **확인·보강 권장**.
- **조치**: `submit`에 받는사람/주소/우편번호/상세주소/포인트부족 검증을 AS-IS 문구로 추가(작은 수정).

### H. 마이페이지 포인트 — `MyPointsView.vue` — 충실

AS-IS `mypage/cntrPoint.html` 대조. 연도·지자체별 집계·기부처 표시가 AS-IS와 일치. "포인트 사용" 폼은 AS-IS엔 없는 개발용 수동차감 기능(백엔드 `manual-point-use` 스위치로 gate, 기본 off)이라 정상. 완료문구("포인트가 사용되었습니다.") 있음. **새 결함 없음.**

### I. 특정사업 목록/상세 — `DesignatedList/DetailView.vue` — 충실(지도 위젯만 범위밖)

AS-IS `designated-donation/index-main.html`·`details.html` 대조. 상태/정렬(최근·참여금액·모금율·종료임박)/사업구분 필터·페이지네이션·모금현황·응원메시지·기간표기 재현. **"지자체별 검색" 지도 팝업**(`fragments/dsg-search-header.html`)만 미포팅 = gift 답례품몰·order 장바구니가 각자 헤더의 같은 지도 위젯을 남긴 것과 **동일한 의도적 경계**(locgovCode 쿼리·키워드검색 q는 지원). [[donation-service-deferred-items]]의 지도선택 보류와 일치 → 재현누락 아님.

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

## 4. admin ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-admin.md`

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

## 5. admin parity ê°ì¬

> íµí© ì  íì¼: `docs/admin-parity-audit.md`

## admin 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS opmanager(운영관리)를 "서비스로직도 화면단도 똑같이" 재현하기 위한
> **전수 갭 목록**. 방식 `[[as-is-parity-exhaustive-audit-method]]` — "발견되면그때"가 아니라 이 목록
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
| V3 | 56/57 카테고리그룹/그룹배너 | **[2026-09-23 판정: 신 O / 구 오분류→잠정 X]** 신버전(CategoriesTeamGroup·GroupBanner)은 TO-BE `CategoryTeamAdminController`(+gift `CategoryTeam/Group/GroupBanner`, OP_CATEGORY_TEAM/GROUP/GROUP_BANNER)로 CRUD 커버 O. **단 09-03 감사가 #56 `GroupManagerController`(saleson.shop.group, `OP_GROUP`)를 "카테고리그룹"으로 오분류** — 실제로는 **회원그룹(고객 세그먼트)** 관리다(`coupon-mapper.xml`이 `OP_GROUP ON UD.GROUP_CODE`로 조인해 쿠폰 대상·`display-mapper` 기획전 SpotApplyGroup 타겟팅에 사용, JSP `opmanager/group/{list,form}.jsp` 생존). TO-BE엔 **회원그룹(OP_GROUP) 관리 없음**. **[후속 확정]** TO-BE 쿠폰은 **회원등급(`targetUserLevel`)+선택회원+특정상품**으로 타겟팅 → 회원그룹 없이 성립. AS-IS OP_GROUP(회원 세그먼트)은 구버전 쿠폰/기획전 타겟팅용인데 TO-BE가 등급+선택회원으로 **의도적 축소**. → **최종: 회원그룹 X(legacy 축소·[[defer-saleson-dependent-unused-features]], 실사용 확인 시 재검토)**. 低 |
| V4 | 64 로그인화면 전용 배너 | **[2026-09-23 판정: O 흡수완료]** AS-IS는 로그인배너를 `CategoriesEdit`(code=loginWeb/loginMobile)로 관리(진입점 `/index-old`, legacy). TO-BE는 이를 **배너관리(`OperationContentController`/`Banner`)의 노출위치 필드 `bannerType`(MAIN/LOGIN_WEB/LOGIN_MOBILE)로 흡수 통합** — `OperationContentService.activeBanners(bannerType)`가 위치별 서빙, 코드 주석에도 명시. 별도 화면 불필요. ⚠경미: 스토어프론트 로그인화면이 `LOGIN_WEB` 배너를 실제 렌더하는지는 미확인(AS-IS 고객 로그인화면도 표시근거 약함·legacy) → 표시 연동만 후속 |
| V5 | B4/B7 상품문의·내부문의(QnaItem/QnaAdminManager) | **[2026-09-23 판정: 상품문의 O / 내부문의 X]** ①**상품문의(B4)=O**: TO-BE `InquiryAdminController`(`/admin/gift-inquiries`)가 gift `G_ITEM_INQUIRY`를 admin에서 답변/블라인드(SFR-005 라운드). ②**내부문의(B7, 지자체담당자↔본사)=X 진짜 갭**: 테이블 `G_QNA_ADMIN`(+ANSWER/ANSWER_FILE/FILE) 4종이 admin DB에 이관됐으나 **참조 Java 0·컨트롤러/화면 미구현**. TO-BE `QnaAdminController`(`/qna-admin`)는 **일반 1:1문의(OP_QNA)** 로 별개(경로만 우연히 동일). AS-IS `QnaAdminManagerController`는 locgov 스코프 + 문의유형(extentionCode 'locgov') + 시도코드 필터. **DB 준비됨 → 컨트롤러+화면만 추가**. 우선순위 中(지자체 담당자 운영지원) |
| V6 | C7/C8/C10~C12 판매자 셀프포털·출고·정산확인 | **[2026-09-23 판정: 별도 audit로 위임 — 기능 O(admin전용) / 셀프이관 보류]** `gift-seller-selfservice-parity-audit.md`에 확정: 출고/송장=order `/api/admin/orders/{id}/invoice`, 정산=admin `settlements`, 반품출고=order `DeliveryReturnAdminApiController` — 전부 **admin(운영관리) 전용으로 구현됨**. 판매자 공지(C8/P4)=`SellerISysNoticeController`(/seller/sys-notice) 2026-09-22 구현완료. **판매자 셀프 이관(P1~P3·P5)만 [[provider-portal-split-deferred]] 결정 대기로 보류**(P6~P8 SalesOn 보류). admin-parity C-series 중복은 그 문서 소관 |
| V7 | B13 방문통계 | **[2026-09-23 판정: X 미구현]** AS-IS `StatsManagerController`(`/opmanager/stats/visit`)는 **접속/방문 통계**(방문자·일별 접속, `getVisitSummary`, VisitExeclView). TO-BE `StatsController`(`/stats`)는 **SFR 기부/주문/포인트/gift 비즈니스 통계**로 성격 다름 → 방문통계 아님. TO-BE에 방문자 집계용 **접속 로깅 인프라 없음**(`ManagerActionLog`=관리자 액션감사 전용). **로깅 인프라(스토어프론트 접속 계측+서버 적재)부터 선결 필요**. 中 |
| V8 | A8 지자체공지 RBAC 스코핑 | **[2026-09-23 판정: O 해소됨]** TO-BE `Notice.locgovCode`(전역=NULL/지자체=코드) + `OperationContentController`가 스코핑 완비: `adminNotices`(list)는 `isLocgovScoped`면 `viewer.locgovCode==n.locgovCode` 필터, `createNotice`는 `effectiveLocgovCode(viewer,·)`로 자기 지자체 강제, `editNoticeForm`은 `requireOwnLocgovOrThrow`로 타 지자체 공지 수정 차단. 09-03 "스코핑 빠진 권한버그" fixed |

---

### 2. AS-IS 죽은코드(재현 불필요) — 재확인만

09-03 감사에서 **죽은코드로 판정된 것(렌더 대상 JSP 물리적 부재)**: BoardCfg/Board(범용CMS),
AccountNumber, Attendance, BanWord, Calendar/CalendarManager, CampaignManager, CardBenefits,
CategoriesEdit/Filter/FeaturedBanner, Condition, Customer(고객사), Display, EmptyPage,
EventStatistics, GiftIGroup, GiftItem(사은품, 전체 주석), GoogleAnalytics(중복), Island,
Newsletter(빈스텁), OzViewTest, Transfer(주석), Store(소스에 "미사용" 명시) 등. → **재현 대상 아님**
([[as-is-parity-includes-disabled-state]]: AS-IS에서 죽어있으면 우리도 만들지 않음).

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

## 6. admin ë©ë´í¸ë¦¬ parity

> íµí© ì  íì¼: `docs/admin-menu-tree-parity.md`

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

## 7. admin ê°­ ì¬ì¸µê°ì¬ part1

> íµí© ì  íì¼: `docs/as-is-admin-gap-deep-audit-part1.md`

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

## 8. admin ê°­ ì¬ì¸µê°ì¬ part2 (ë°°ì¹D)

> íµí© ì  íì¼: `docs/as-is-admin-gap-deep-audit-part2.md`

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

## 9. ìë¡ë íì¼ ê·ì¹ parity (íì¥ìÂ·ì©ëÂ·íì¼ì­ì )

> íµí© ì  íì¼: `docs/upload-file-parity-audit.md`

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

## 10. member ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-member.md`

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

## 11. member ìì ì°ëí´ì§ parity

> íµí© ì  íì¼: `docs/member-social-unlink-parity-audit.md`

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
- **`/api/auth/onepass-unlink`(원패스 연동만 해제, 탈퇴X)**: MSA는 원패스가 가입경로(300)여서 "탈퇴 없는 원패스 부가연동 해제" 상태가 성립하지 않음(onepassCancel은 300→항상 secede). 부가연동 도입/DA 설계 시. → [[member-service-deferred-items]]
- **실 연계해지 API 본체**(InterLockRelease/카카오 톡키트 unlink): 방화벽·실 IdP 개방 시 client 내부만 교체. 현재 off 통과.
- 카카오 mock 가입이 kakaoUserKey를 채우지 않는 점은 mock 한계(실연계 on이면 채워짐).

---

## 12. donation ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-donation.md`

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

## 13. donation parity ê°ì¬

> íµí© ì  íì¼: `docs/donation-parity-audit.md`

## donation 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS를 "서비스로직도 화면단도 똑같이" 재현하기 위한 **전수 갭 목록**. `[[as-is-parity-exhaustive-audit-method]]` 방식대로 프론트 이벤트 + 서비스 검증 분기 + 매퍼를 대조한다. "발견되면 그때"가 아니라 **이 목록 기준으로** 구현한다. NEW/CHANGED(ISP·RFP) 요건은 표에 표시하고 **착수 전 사용자 확인**.
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

> **donation audit 완료(1차).** 구현은 "전 서비스 목록화 완료 후" 시작(방식 [[as-is-parity-exhaustive-audit-method]]). 다음: member 전수조사.

---

## 14. gift ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-gift.md`

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

## 15. gift ìµì ì²´ê³ parity

> íµí© ì  íì¼: `docs/gift-option-parity-audit.md`

## gift 옵션(다형 옵션 체계) AS-IS 전수대조 갭목록

> 방법: [[as-is-parity-exhaustive-audit-method]] — 프론트 이벤트 + 검증로직 + 매퍼를 전수로 훑어
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

## 16. gift ì¹´íê³ ë¦¬ 3ë¨ í¸ë¦¬ parity

> íµí© ì  íì¼: `docs/gift-category-tree-parity-audit.md`

## 카테고리 3단 트리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]]. 신규 기준 적용: [[defer-saleson-dependent-unused-features]]
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

- **live 답례품 카테고리 3단 트리(대분류 GIFT_CATEGORY + 중분류/3단 gift_subcategory 메가메뉴 + 관리자 CRUD)는 MSA에 이미 구현 완료** → [[defer-saleson-dependent-unused-features]]대로 그대로 둔다.
- **C1~C3(SalesOn op_category 4단 트리 · op_item_category 필터 · 품목↔중분류 매핑)은 SalesOn 종속·현재 미사용(DB 0건)** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "카테고리 3단 트리"는 **추가 구현 없이 마감**하고 다음 항목(검색어 관리)로 이동 권고. — 최종 결정은 사용자.

---

## 17. gift ê²ìì´ ê´ë¦¬ parity

> íµí© ì  íì¼: `docs/gift-search-keyword-parity-audit.md`

## 검색어 관리 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] + [[defer-saleson-dependent-unused-features]].

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

- **답례품 키워드 검색(q 필터)과 추천검색어 관리자 CRUD(OP_SEARCH)는 MSA에 이미 구현 완료** → 유지([[defer-saleson-dependent-unused-features]]: 이미 구현된 건 그대로 둠). 추천검색어 프론트 미노출도 AS-IS와 일치.
- **S2~S4(인기검색어·자동완성·일별집계·금지어·통합검색)는 SalesOn/외부 인프라 종속·현재 미사용** → **기록만 하고 구현 보류**(DA 설계안 확정 후 재검토).
- 따라서 "검색어 관리"는 **추가 구현 없이 마감** 권고. — 최종 결정은 사용자.

---

## 18. gift ë°°ì¡ë¹ ì ì± parity

> íµí© ì  íì¼: `docs/gift-delivery-fee-parity-audit.md`

## 배송비 정책 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] (프론트이벤트+검증로직+매퍼 전수 → 갭목록 →
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

**화면 parity 조치(2026-09-22)**: MSA admin `gift-items/form.html`이 이전 세션("SFR-005 재검토")에 배송비 설정을 **드롭다운으로 노출**하고 있었으나, AS-IS는 `<tr class="hidden">`로 숨김. 기준([[as-is-parity-includes-disabled-state]])대로 **배송비 설정 5행(구분·기본배송비·조건부무료·제주·도서산간)을 `display:none`으로 숨김**(input은 남겨 저장 시 값 보존). 택배사·반품가능여부는 AS-IS와 동일하게 노출 유지. 구매자 상세는 이미 "무료" 하드코딩으로 일치.

**G6/G7 (다른 서브시스템 downstream)**: 필드는 order까지 전달 완료. 단, 실제 효과는 배송비 *계산*이 아니라 다른 서브시스템에 있다 —
- **G6 반품배송비**: AS-IS `OrderClaimApplyServiceImpl:2513 apply.setCollectionShippingAmount(orderItem.getShippingReturn())` — 반품 클레임 시 고객이 부담하는 회수배송비. MSA `ClaimService`는 반품 시 포인트 전액복원(회수비 미차감)하는 단순화 모델. 재현하려면 클레임 환불 SAGA(order→point)를 회수비 차감으로 재구성해야 함. 실데이터 shipping_return 0건.
- **G7 본사/업체 정산**: AS-IS `Shipping`가 deliveryType=1(본사배송)이면 배송비 정산 대상을 운영자(DEFAULT_OPMANAGER_SELLER_ID)로 잡음 — *배송비 remittance* 레코드용(품목 정산과 별개). MSA엔 판매자별 배송비 정산 레코드가 없어 올바른 귀속처가 없음. OrderItem.sellerId에 얹으면 품목정산이 오귀속됨(부정확). 실데이터 delivery_type 전건 NULL.

### 5. 판정 결론 / 권고

- **실사용 범위(type3 조건부무료 + 무료)는 이미 MSA가 서버·화면 모두 AS-IS와 동등하게 재현 완료.**
- **화면 표기도 AS-IS와 동일**(무료 하드코딩, shippingTypeText 죽은코드까지 parity).
- 갭 G1~G7은 **AS-IS 코드엔 살아있으나 답례품 실데이터에 0건** → [[as-is-parity-includes-disabled-state]]의 "기능 있으면 다 만들되"에 해당하나, 실운영 데이터가 없어 검증 불가능한 순수 코드 parity.

권고: **배송비 정책은 실사용분 재현 완료로 마감**하고, 나머지 항목(카테고리 3단 트리 / 검색어 관리 / 판매자 셀프서비스)로 이동. G1~G7 순수코드 parity는 운영데이터 확보 후 재검토(보류). — 최종 결정은 사용자.

---

## 19. gift íë§¤ì ìíìë¹ì¤ parity

> íµí© ì  íì¼: `docs/gift-seller-selfservice-parity-audit.md`

## 판매자 셀프서비스 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] + [[defer-saleson-dependent-unused-features]].
연관 결정: [[provider-portal-split-deferred]](제공자웹 분리는 현행 gift 내 /seller/* 유지·보류), [[gift-seller-portal-unauthenticated]].

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
- **P1~P3(출고/송장·정산·반품출고)은 답례품 live 기능이나 현재 MSA는 admin(운영관리) 전용**이다. 이걸 판매자 셀프로 이관/추가하는 것은 **[[provider-portal-split-deferred]](제공자웹 분리 보류) 결정**에 걸린다 → 보류.
  - ⚠ **정정(2026-09-22)**: 이전 판에 "ISP상 판매자가 직접 출고/정산하는가 범위"라고 적었으나, **원본 ISP 문서는 리포지토리에 없고(요약본 `docs/requirements.md` 후반부 ISP 요약만 존재), 특정 페이지로 뒷받침되지 않는다.** 오히려 요약본상 **정산(op_settlement)은 "운영관리(admin) MSA" 소관**으로 설계됨(원본 p.259/373/379). 출고/운송장등록은 "주문관리 MSA" 기능으로 나열되나 수행 주체(판매자 vs 운영자)는 요약본에 명시 없음. 판매자 출고/정산 주체 근거는 AS-IS 소스(Seller*Controller)와 접근권한 매트릭스(답례품제공자 롤·UI_S* 화면ID)뿐.
- **사용자 결정(2026-09-22)**: P4만 지금 구현(P5는 위 정정으로 P1과 동일영역 판명 → 보류), P1~P3·P5는 제공자웹 분리 결정과 함께 보류, P6~P8(SalesOn)은 기록+보류.

### 5. P4 구현 (2026-09-22, 완료)

AS-IS `SellerISysNoticeController`(/seller/sys-notice) 그대로 재현. 판매자 공지 데이터(admin op_sys_notice_seller)는 admin 소유라 gift 셀프포털이 admin 내부 API를 호출한다.
- **admin** `SellerNoticeInternalApiController`(/api/seller-notices): 목록(노출필터 useYn/displayFlag≠'N', 제목·기간 검색)/상세(조회수 증가)/첨부다운로드. X-Internal-Secret 가드.
- **gift** `SellerNoticeClient`(gift→admin, X-Internal-Secret) + `SellerNoticeController`(/seller/sys-notice/list·/detail/{id}·/file-download/{fileId}, JWT ROLE_PROVIDER 인증) + 템플릿 `seller-notice-list.html`(공지사항, No./제목/조회수/등록일시, 공지 라벨·첨부표시, 총 N건, 제목·기간 검색)·`seller-notice-detail.html`(제목/등록일시/조회/내용/첨부자료). 대시보드에 "공지사항" 링크 추가. gift config `ghlove.admin-service.base-url` 추가.
- admin·gift 컴파일 OK. 검증 시드: admin.op_sys_notice_seller 2건(1002 공지·1003 일반). **재기동 시 활성**.

---

## 20. order ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-order.md`

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

## 21. point ê¸°ë¥ ëì¡°

> íµí© ì  íì¼: `docs/as-is-feature-audit-point.md`

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

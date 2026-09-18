# AS-IS 코드 인벤토리 — admin (운영관리 + 판매자 포털)

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`** / `보류`
> 작성 2026-09-10. 6개 도메인 중 마지막. 앞선 5개 도메인이 운영관리 컨트롤러를 전부 이 시트로 이월했다.

## 0. 규모

| 축 | AS-IS | MSA |
|---|---|---|
| 컨트롤러 | 운영관리 121 + 판매자 20 = **141** | admin 92 |
| 엔드포인트 | 운영관리 1,459 + 판매자 124 = **1,583** | admin 543 |
| 화면 | JSP 531(운영관리) + 129(판매자) = **660** | Thymeleaf 221 |
| 기능 그룹 | 운영관리 77 + 판매자 18 | 메뉴 121건(`admin.op_menu`) |
| 매퍼 쿼리 | **1,030** (검사분) | JPA |
| 엔티티/리포지토리/서비스 | MyBatis | 68 / 64 / 76 |

`saleson/shop/**` 207개 컨트롤러 2,043 엔드포인트를 경로·명칭으로 4분류한 결과:

| 분류 | 컨트롤러 | 엔드포인트 | 판정 |
|---|---|---|---|
| 운영관리 (`/opmanager/*` 또는 `*Manager*Controller`) | 121 | 1,459 | 이관 대상 |
| 판매자 (`saleson/shop/seller/*`, `*Seller*Controller`) | 20 | 124 | 이관 대상 |
| 원제품 PC 쇼핑몰 | 46 | 276 | **사장** (§1) |
| 모바일 `/m/*` | 19 | 181 | **사장** (§1) |
| API 인증 (`/api/auth/token`) | 1 | 3 | member 소관 |

---

## 1. 원제품 쇼핑몰·모바일 컨트롤러 61개 / 440 엔드포인트 — 통째로 사장

common 시트 §9의 범용 게시판 프레임워크와 **같은 구조의 발견**이다.

근거 3중:

1. **뷰가 없다.** `ghlove-web`의 JSP 705개 분포는 `opmanager 531 / seller 129 / layouts 26 /
   front 16 / 기타 3`. 그런데 `views/front/` 16개는 전부 `i18n/categories/*`(카테고리 옵션 조각)와
   `i18n/smarteditor/*`(업로드 결과창)뿐이다. `item`·`mypage`·`order`·`users`·`main` 뷰는
   **하나도 없다**. `ItemController:167`의 404 핸들러가 가리키는 `//default/views/front/ko/error/404`
   경로의 `front/ko/` 스킨 디렉터리 자체가 존재하지 않는다.
2. **프론트가 부르지 않는다.** AS-IS Vue SPA(`ghlove-frontend`)의 백엔드 호출은
   `modules/*.js` 전체에서 **198건 전부 `/api/*`** 이고, 예외는 `/op_security_logout` 하나뿐이다.
   `saleson/shop/*`의 비-`/opmanager` 경로를 부르는 곳이 0건이다.
3. **운영관리 화면에서도 거의 안 부른다.** 531개 opmanager JSP + 129개 seller JSP를 훑어
   호출되는 것은 4개뿐 — `ZipcodeController`(1), `IslandController`(1),
   `CategoriesController`(6), `UserSnsController`(9) = **17 엔드포인트**.

→ **사장 확정: 61 컨트롤러 / 440 엔드포인트** (원제품 42 + 모바일 19 컨트롤러).
   위 4개(17 엔드포인트)는 운영관리 경유로 살아 있다.

주요 사장 컨트롤러: `ItemController`(24), `OrderController`(38), `UserController`(36),
`MypageController`(23), `MainController`(14), `OrderClaimApplyController`(11),
`FeaturedController`(13), `EventViewController`(9), `SnsUserController`(9),
`/m/*` 19종 전부(`MainMobileController` 9, `MypageMobileController`, `ItemMobileController` 등).

이는 point의 `OP_POINT` vs 기부포인트, order의 `save`/`pay` vs `giveGoodsSavePay`,
donation의 `/api/ngdonation` vs `/api/regiontax`와 **완전히 같은 계열** — 원제품(SalesOn)
경로가 통째로 남아 있고 고향사랑e음 전용 경로만 실사용된다.

---

## 2. 운영관리 기능 그룹 77개 — MSA 대조

AS-IS 기준은 `WEB-INF/views/opmanager/i18n/<group>/` 531 JSP.
MSA 기준은 `admin/src/main/resources/templates/<dir>/` 221 HTML + `admin.op_menu` 121행.

### 2-1. 대응 있음 (주요)

| AS-IS 그룹 (JSP) | MSA |
|---|---|
| `user` (74) | `member-admin`, `person-in-charge`, `off-person-in-charge`, `manager-admin`, `locgov-admin`, `secede-user-admin`, `sleep-user-admin`, `user-level-admin`, `role-admin`, `honor-user-admin` |
| `order` (54) | `order-admin`(6), `settlements`(3) |
| `give` (39) | `give`(9) — 기부금모금/지출/포인트/변경신청 |
| `shop-statistics` (38) | `shop-statistics`(27) |
| `item` (25) | `gift-items`(5), `gift-categories`(3) |
| `community` (24) | `community`(6) — board·databoard·locv-faq |
| `offgive` (19) | `offgive`(4) |
| `designated-donation` (17) | `designated`(6) |
| `config` (17) | `site-config`(8), `isms-config`(1) |
| `log` (15) | `log`(7), `send-log-admin`(3) |
| `remittance` (9) | `settlements` — **이름만 다르고 대응** (예정/확인/완료 → 목록/대기/상세+세금계산서·입금·마감) |
| `qna`·`qna-open`·`qna-admin`·`qna-item` (18) | `qna`(3), `qna-admin`(2), `gift-inquiries`(1) |
| `manual`(4)·`featured`(4)·`banner`(4)·`categories-team-group`(4) | `manual-admin`, `featured-admin`, `main-banner-admin`+`content`, `category-teams` |
| `menu`(3)·`maintenance`(3)·`mail-config`(3)·`mail`(3) | `menu-admin`, `maintenance-admin`, `mail-config`, `email` |
| `welfareCenter`(5)·`lclgvHnrUser`(3)·`brand`(3)·`user-level`(2) | `welfare-center-admin`, `honor-user-admin`, `brand`, `user-level-admin` |
| `qustnr`(3) | `survey-admin`(3) — **운영자 등록만, 사용자 참여는 common §7 조치대상** |
| `point-check`(1) | `reconciliation`(1) — 주문·포인트 대사 |
| `batch-job`(2)·`batch-log`(1)·`access`(2)·`code`(2) | `batch-job`, `batch`, `access`, `codes` |
| `user-group`(3)·`user-login-banner`(1)·`order-agency`(1)·`claim-memo`(1) | 각각 대응 클래스 존재 |

### 2-2. 권한체계 — 대응

AS-IS `role-mapper`(11, 벤더 jar), `userrole-mapper`(2), `OP_MENU_RIGHT`, `menu-manager-mapper`(14).
MSA `RoleAdminController` 8종 — 역할 CRUD + **`/{authority}/matrix` 권한 매트릭스**(GET/POST).
`admin.op_role`, `admin.op_menu_right`(36행), `admin.op_menu`(121행) 실데이터 존재. → 대응있음.

### 2-3. MSA 미구현 — 14개 그룹

| AS-IS 그룹 | JSP | 성격 | 비고 |
|---|---|---|---|
| `proposal` + `proposal-statistics` | 16 | 제안 관리·통계 | MSA 0 — 최대 미구현 |
| `magicline` | 12 | 공동인증서 운영관리 | MSA에 클래스 4개뿐 → **부분** |
| `catalog` | 8 | 전자 카탈로그 | common §6과 동일 건 |
| `temp-process` | 5 | 임시처리 | MSA 클래스 1 → **부분** |
| `newsletter` | 4 | 소식지 발행 | common §6과 동일 건 |
| `seasonal-food` | 3 | 제철식품관 운영 | **사용자 화면(`/gifts/seasonal`)은 있는데 운영관리가 없다** |
| `locgov-notice` | 3 | 지자체 공지 | **대응있음** — 통합 `/admin/notices`(LOCGOV_CODE)에 흡수 (§11) |
| `shipment-return` | 3 | 반품 배송 | order 클레임과 별개의 회수 배송 관리 |
| `speciality-item` | 2 | 특산품관 운영 | 제철식품관과 동일 유형 |
| `delivery-company` | 2 | 택배사 마스터 | MSA는 배송추적·상품별 택배사 지정만 → **부분** |
| `group` + `group-banner` | 4 | 그룹/그룹배너 | 원제품 잔재 가능성 |
| `cntnts-stsfdg` | 2 | 콘텐츠 만족도 | common `stsfdg-mapper`(1)와 한 쌍 |
| `sellerconfirm` + `sellerconfirmOrder` | 2 | 판매자 확인 | |
| `oz` · `chatbot` | 2 | OZ리포트 · 챗봇 설정 | |

`board-cfg`(2)는 common §9에서 사장 판정한 범용 게시판의 설정화면이므로 **재현 불필요**.

---

## 3. 판매자 포털 — 18 그룹 / 129 JSP vs MSA 3화면

[[provider-portal-split-deferred]] 대로 현행은 gift 서비스 안의 `/seller/*` 다.
[[gift-seller-portal-unauthenticated]] 로 인증은 해소했고, 여기서는 **기능 범위**를 본다.

| AS-IS 판매자 그룹 | JSP | MSA |
|---|---|---|
| `order` | **50** | `seller-dashboard.html` 의 "최근 주문현황(최근 20)" 한 블록 |
| `user` | 13 | 없음 (판매자 계정관리) |
| `item` | 13 | `register.html`, `edit.html` |
| `remittance` | 7 | 없음 (판매자용 정산 조회) |
| `temp-process` | 5 | 없음 |
| `qna-locgov` | 5 | 없음 |
| `mall` | 5 | 없음 (몰 설정) |
| `qna-item` | 4 | `POST /inquiries/{inquiryId}/answer` |
| `qna` | 4 | 없음 |
| `magicline` | 4 | 없음 |
| `gift-item` | 4 | `/gifts/{itemId}/stock`, `/stop`, `/discontinue` |
| `shipment-return` · `shipment` | 6 | 없음 |
| `sale-edit` | 3 | 없음 |
| `sellerNotice` · `notice` | 4 | admin `seller-notice-admin`(운영자 발행측만) |
| `main` | 1 | `seller-dashboard.html` |

MSA 판매자 포털 실질 기능 = **답례품 등록/수정 · 재고조정 · 판매중지 · 문의답변 · 주문 20건 조회**.
AS-IS의 주문관리 50화면(발주·발송·송장·취소승인·반품승인 등)과 정산 조회가 통째로 빠져 있다.
→ **부분**. 운영데이터·개발DB 이후 재결정 대상이므로 조치대상이 아니라 **보류 항목으로 기록**한다.

`seller-menu-mapper.xml` 7쿼리 중 3개(`getChildMenuList`, `getFirstMenuList_`, `getFirstMenuUrl`)가
호출 0 → 판매자 메뉴 동적 구성이 AS-IS에서도 반쯤 죽어 있다.

---

## 4. 매퍼 쿼리 (1,030 검사 / 사장 33 / 보류 25)

### 4-1. 사장 쿼리 33건

| 매퍼 | 전체 | 사장 | 쿼리 |
|---|---|---|---|
| `stats-mapper` | 16 | **6** | `getDayStatsListAnalysys`, `getVisitCountByTime`, `getVisitCountByWeekday`, `getVisitList`, `insertLoginCount`, `selectVisitCountToday` |
| `brand-mapper` | 12 | **5** | `deleteBrandCategoryById`, `insertBrandCategory`, `updateBrandCategory`, `getOpManagerLocgovCode`, `getOpUserLocgovCode` |
| `give-state-mapper` | 47 | 4 | `getCntrTaxTempLog`, `insertCntrTaxTempLog`, `giveDeleteAt`, `giveReqmngSmsUserInfoByReqId` |
| `give-statistics-mapper` | 25 | 3 | `statisticsGiveLocgovByAge`, `statisticsGiveLocgovByHour`, `statisticsGiveLocgovByMonth` |
| `cmnty-mapper` | 96 | 3 | `getFaqBbsCmntFileList`, `getOffSrBbsCmntFileList`, `getSrBbsCmntFileList` |
| `seller-menu-mapper` | 7 | 3 | §3 참조 |
| `email-mapper` | 12 | 2 | `getEmailAuthList`, `getEmailFileList` |
| `statistics-mapper` · `shipment-return-mapper` · `categories-mapper` · `ranking-mapper` · `manager-action-log-mapper` · `remittance-mapper` · `designated-donation-mapper` | - | 각 1 | `getDateStatsListGhloveAnalysis`, `getShipmentReturnById`, `getCategoryListByMaxLevel`, `getSaleRankingListForGroupAndCategory`, `getUserActionLogId`, `updateAddPaymentRemittanceInfo`, `getMaxOrderingDesignatedDonationNoticeImgDesc` |

> `stats-mapper`의 사장 6건은 **접속·방문 통계**(시간대별·요일별·오늘 방문수·로그인수)다.
> common §8-1에서 확인한 것과 맞물린다 — 접속통계 수집(`/api/common/visit`)이 모바일 탭바와
> 카탈로그 헤더에서만 돌고, 그 집계 화면 쪽도 죽어 있다. **AS-IS에서 접속통계는 사실상 비작동**.
> MSA 미구현을 결함으로 볼 이유가 약해진다.

> `brand-mapper`의 사장 5건은 브랜드-카테고리 연결 CRUD 전체다. 브랜드관리는 살아 있으나
> 카테고리 연결 기능만 죽었다.

### 4-2. 판정 보류 25건 — 벤더 jar 내부

`role-mapper`(11, `com.onlinepowers.framework.web.opmanager.role.RoleMapper`)와
`security-mapper`(14, `com.onlinepowers.framework.security.mapper.SecurityMapper`)는
인터페이스가 `libs/opframework-3.15.0.jar` 안에 있어 소스 grep으로 사장 판정이 불가능하다.
common §9-1의 52쿼리와 같은 사유.

### 4-3. 전량 live인 대형 매퍼

`cmnty`(96, 사장 3) · `remittance`(63, 사장 1) · `designated-donation`(60, 사장 1) ·
`categories`(52, 사장 1) · `give-state`(47, 사장 4) · `statistics`(36, 사장 1) ·
`bix5`(35, 사장 0) · `display`(30) · `personincharge`(27) · `generalcustomer`(27) ·
`offgive`(22) · `temp-process`(21) — 운영관리는 본체가 대부분 살아 있다.

---

## 5. MSA 전용 (AS-IS 대응 없음)

RFP/ISP 근거로 추가된 것들 — [[scope-migration-not-greenfield]] 기준 확인 대상.

| MSA | 성격 |
|---|---|
| `open-api`(2) — `OpenApiController` | 외부 개방 API 관리 |
| `nts-receipt-log-admin`(1) | 국세청 전자기부금영수증 전송 로그 |
| `reconciliation`(1) | 주문·포인트 대사 (AS-IS `point-check` 확장) |
| `mobile-category-edit-admin`(2) | AS-IS `mobilecategoriesedit-mapper` 대응 — 실은 AS-IS에도 있음 |
| `stats/sla`(배송 SLA 통계) | AS-IS 미존재 |
| `statistics-locgov`(관심 지자체 통계) | AS-IS `statistics-locgov-mapper`(4) 대응 |
| `delivery-tracking.html` | 스마트택배 연동 조회 |
| `batch`(1) — `MemberBatchAdminController`, `NhExportBatchController` | AS-IS 배치의 수동 실행분 |

`stats/sla`와 `open-api`는 AS-IS에 대응이 없다 — RFP/ISP 근거 확인이 필요하다.

---

## 6. 조치 후보

| # | 항목 | 근거 | 심각도 | 상태 |
|---|---|---|---|---|
| 1 | 제철식품관 운영관리 없음 — **사용자 화면만 있고 운영자가 상품을 지정할 수 없다** | AS-IS `seasonal-food`(3) JSP, MSA 0. GNB 답례품 메뉴에 살아있는 화면 | 높음 | **✅ 조치완료 (§9)** |
| 2 | 판매자 포털 주문관리 50화면 → MSA 조회 1블록 | AS-IS `seller/order` 50 JSP | 중(보류 합의됨) | 보류 |
| 3 | 반품 배송(`shipment-return`) 운영관리 없음 | AS-IS 3 JSP + `shipment-return-mapper` 8쿼리 live | 중 | ✅ 조치완료 (§10) |
| 4 | 택배사 마스터 관리 없음 | AS-IS `delivery-company` 2 JSP + `deliverycompany-mapper` 7쿼리 live | 중 | ✅ 조치완료 (§10) |
| 5 | 지자체 공지(`locgov-notice`) 대응 확인 | AS-IS 3 JSP, MSA `community`에 흡수 여부 미확인 | 중 | **✅ 확인완료 — 대응있음 (§11)** |
| 6 | 제안 관리·통계(`proposal`) 16 JSP 미구현 | AS-IS 최대 미구현 그룹 | 확인필요 | |
| 7 | 콘텐츠 만족도(`cntnts-stsfdg`) 미구현 | common `stsfdg` 1쿼리와 한 쌍 | 낮음 | |
| 8 | 판매자 확인(`sellerconfirm`) 미구현 | `sellerconfirm-mapper` 6쿼리 live | 낮음 | |
| 9 | MSA `stats/sla`·`open-api` RFP/ISP 근거 확인 | AS-IS 대응 없음 | 확인필요 | |

> **특산품관(`speciality-item` 2 JSP)은 조치대상에서 제외했다.** AS-IS에서 진입 링크가
> 전부 주석 처리돼 있고 "오픈 후 주석 제거"(`header_g_20240731.vue:137`) 표기가 붙은
> **미오픈 기능**이다 — MSA 미구현이 정상이다.

## 7. 재현 불필요 (AS-IS 사장)

- **원제품 PC 쇼핑몰 42 컨트롤러 / 259 엔드포인트 + 모바일 19 / 181 = 61 / 440** (§1)
- `board-cfg`(2 JSP) — common §9 범용 게시판의 설정화면
- 매퍼 사장 33쿼리 (§4-1). 특히 **접속·방문 통계 6쿼리**는 수집단까지 함께 죽어 있다
- `seller-menu` 동적 메뉴 3쿼리
- 브랜드-카테고리 연결 CRUD 5쿼리

## 8. 판정 보류

- `role-mapper`·`security-mapper` 25쿼리 — 벤더 jar 내부 (§4-2)
- 판매자 포털 분리 여부 — [[provider-portal-split-deferred]]
- `magicline`(12) · `temp-process`(5) 부분 구현의 완결 범위

---

## 9. 조치 결과 (2026-09-10)

### 9-1. 제철식품관 운영관리 (조치후보 1)

**문제:** 답례품몰 GNB "제철식품관"(`/gifts/seasonal`)은 살아 있는 사용자 화면인데,
그것을 채우는 `G_SEASON_FOOD_ITEM`(월별 답례품 지정)에 **쓰기 경로가 MSA 어디에도 없었다**.
gift `seasonalGifts()`가 이 테이블을 읽기만 했다. 실제 데이터는 8월분 5건뿐이었고 오늘은
9월이라 제철식품관이 **빈 화면**이었다.

**AS-IS 사양:** 별도 관리화면이 아니라 **답례품 등록/수정 폼 안의 "제철 월 선택(최대 3개)"**
체크박스다(`opmanager/i18n/item/form.jsp:934~947`). 저장 시 `ItemServiceImpl:1471~1479`가
`deleteSeasonFoodItem` 후 선택된 월만큼 `insertSeasonFoodItem` — 전삭제 후 재삽입. 3개 상한은
화면 JS(`:5152`)에만 있었다.

**구현:**
- gift `SeasonFoodItemRepository` — `findByItemIdOrderBySeasonFoodMonth`, `deleteByItemId` 추가
- gift `GiftService.seasonFoodMonthsOf()` / `replaceSeasonFoodMonths()` — 전삭제 후 재삽입.
  **3개 상한과 1~12 범위 검증을 서비스 계층에 뒀다** — AS-IS는 화면 JS로만 막았지만, 운영자
  화면을 거치지 않는 호출로 무력화되면 안 되므로 서버에서도 건다.
- gift `ItemAdminApiController` — `GET/PUT /api/admin/gift-items/{id}/season-food-months`
  (AS-IS는 답례품 저장에 묻어가지만, 배송비 설정과 같은 방식으로 하위 리소스로 분리)
- admin `ItemAdminClient.seasonFoodMonths()` / `updateSeasonFoodMonths()`
- admin `ItemAdminController` — 수정폼에 현재 지정을 싣고, `POST /admin/gift-items/{id}/season-food-months` 저장
- admin `gift-items/form.html` — 1~12월 체크박스 + 3개 상한 JS(AS-IS 마크업 이식)

**검증 (실서비스, 데이터 원복 완료):**
| 확인 | 결과 |
|---|---|
| `GET .../1000/season-food-months` (8월 지정 상태) | `[8]` |
| `PUT months=9,10` | `[9,10]`, DB 반영 |
| 제철식품관 화면(`/seasonal`, 9월) | 강남구 한우 선물세트 노출 (빈 화면→노출) |
| 4개 선택 | 400 "제철 월 선택은 3개까지 가능합니다." |
| 13월 | 400 "제철 월은 1~12 사이여야 합니다." |
| 거부 후 DB | 9,10 그대로 유지(부분반영 없음) |
| admin 수정폼 렌더 | 체크박스 12개, 9·10월 checked |
| admin 폼 저장 `months=1,5,9` | DB 1·5·9 반영, `frst_register_id`=로그인 운영자(1003) |

원복: item 1000을 8월 단독으로 되돌리고 시드 타임스탬프까지 나머지 4건과 일치시킴.
임시로 심었던 admin 계정 비밀번호·잠금상태도 원상복구.

---

## 10. 조치 결과 — 택배사 마스터 + 반품지 관리 (2026-09-11)

조치후보 3(반품 배송)·4(택배사 마스터)를 함께 처리했다. 두 테이블 모두 **order 스키마(ord)에
이관은 됐으나 애플리케이션 코드가 전무**했다(`ord.op_delivery_company`, `ord.op_shipment_return`,
둘 다 0행). 조사 중 **반품 배송(shipment-return)의 실제 성격이 "반송 배송 처리"가 아니라
판매자별 반품 회수 주소록(반품지)**임을 확인했다(AS-IS ShipmentReturn: addressName·zipcode·
address·defaultAddressFlag).

**소유·편집 분리:** 두 리소스 모두 ord 스키마 소유라 데이터 주인은 order, 편집 UI는 admin —
gift의 브랜드·답례품 관리와 같은 cross-service 패턴(admin이 시크릿 헤더로 order API 호출).

**PK 시퀀스 누락 수정:** 두 이관 테이블 모두 PK에 시퀀스 DEFAULT가 없었다(`delivery_company_id`
NOT NULL·기본값 없음, `shipment_return_id` DEFAULT 0). [[saleson-original-product-leftovers]]에서
반복 확인된 "이관 테이블 PK 시퀀스 누락"의 또 다른 사례다. 시퀀스 생성 + DEFAULT 설정 후
`database/ddl/service-order.sql`에도 반영.

**구현:**
- order: `DeliveryCompany`·`ShipmentReturn` 엔티티/리포지토리/서비스, `DeliveryReturnAdminApiController`
  (`/api/admin/delivery-companies`, `/api/admin/shipment-returns` 각 CRUD), WebConfig 인터셉터에 경로 등록
- 반품지 기본주소 단일성: `defaultAddressFlag='Y'`는 판매자당 하나만(AS-IS updateDefaultAddressFlag) —
  첫 등록은 자동 기본, 새 기본 지정 시 나머지 자동 해제
- admin: `DeliveryReturnClient`, `DeliveryCompanyAdminController`·`ShipmentReturnAdminController`,
  템플릿 4종(택배사 목록/폼, 반품지 목록/폼)

**검증 (order·admin 재기동, 데이터 원복 완료):**
| 확인 | 결과 |
|---|---|
| 시크릿 없이 API | 401 |
| 택배사 등록 | PK 자동채번(id=1), 생성 |
| 택배사 전체/활성만(useYn=Y) | 2건 / 1건 |
| 택배사 수정·이름공백 등록 | 수정 반영 / 400 |
| 반품지 첫 등록 | 자동 기본(Y) |
| 반품지 둘째를 기본 지정 | 둘째 Y, **첫째 자동 N** (단일성) |
| 반품지 우편번호 누락 | 400 |
| admin 목록·폼 화면 렌더 | 200, 값 표시 |
| admin 폼 등록·삭제 | 302 후 order 반영 |

**남긴 것(조치 아님):** 택배사 마스터를 송장 등록 화면의 택배사 드롭다운에 연결하는 것은
현재 `codesOf("DELIVERY_CARRIER")` 공통코드를 쓰는 기존 경로와의 통합 설계가 필요해 별도 과제로
둔다. 이번 범위는 AS-IS DeliveryCompanyManagerController가 제공하던 **마스터 관리 자체**의 복원이다.

## 11. 근거 확인 — 지자체 공지 (조치후보 5, 2026-09-11)

**판정: 대응있음.** AS-IS `LocgovNoticeManagerController`(별도 컨트롤러, list/create/edit
3 JSP)는 MSA에서 별도 화면을 두지 않고 통합 공지관리(`OperationContentController`
`/admin/notices`)에 **흡수**됐다. 코드 변경 없음 — 흡수 사실을 확인하고 판정만 확정한다.

### 근거 대조

| AS-IS `locgov-notice` | MSA 대응 |
|---|---|
| `list`: `adminRole=="LOC"`면 `noticeParam.setLocgovCode(소속)`으로 필터 | `/admin/notices` 목록: `MenuService.isLocgovScoped(viewer)`면 `viewer.getLocgovCode()`로 필터 |
| `createAction`: `notice.setLocgovCode(locgovCodeDetails.getId())` (LOC는 소속 강제) | `POST /admin/notices`: `MenuService.effectiveLocgovCode(viewer, locgovCode)` — LOC 담당자는 화면에서 고른 값 무시하고 소속 강제, 시스템/행안부는 고른 값(null=전체) |
| `edit`/`delete`: 소속 지자체 공지만 | `requireOwnLocgovOrThrow(id, session)` 소유 가드 |
| OP_NOTICE의 `LOCGOV_CODE` 컬럼으로 전역/지자체 구분 | `Notice.locgovCode` (전역=NULL) 동일 |
| 이용자 노출 = 기금사업소개 "지자체공지사항" 탭 | donation `FundProjectController`/`FundProjectApiController` → `NoticeClient.noticesByLocgov(locgovCode)` → admin `GET /api/notices?locgovCode=` (읽기전용, 실패 시 빈 목록) |

`isLocgovScoped`는 `LOCGOV_SCOPED_ROLES`로 판정 — AS-IS "role 5,6이면 소속 지자체 스코프"
분기 재현.

### 재현 불필요로 정리한 AS-IS 잔재

AS-IS `locgov-notice` 컨트롤러의 공지-판매자 연결(`getNoticeSellerList`,
`delete-notice-seller`, create/edit의 주석 처리된 `sellerList`)은 일반
`NoticeManagerController`에서 복사돼 온 잔재다. `locgov-notice/list.jsp`에 판매자
지정 UI가 없어 지자체 공지의 실제 기능이 아니며, 재현 대상에서 제외한다.

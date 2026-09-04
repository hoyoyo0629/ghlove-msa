# AS-IS 기능 감사 - admin(운영관리) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(`ghlove-web` saleson 백엔드, JSP `WEB-INF/views/opmanager/i18n/**`) → `ghlove-msa/admin`

## 0. 조사 방법

이 감사의 목적은 `admin_console_real_asis_markup_round` 메모의 "AS-IS opmanager 123개 화면 전부 완료" 주장을 실측으로 검증하는 것이다. AS-IS `ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` 하위를 전수 `find`로 나열하고(총 165개 리프 디렉토리, 531개 `.jsp` 파일), 각 디렉토리를 아래 기준으로 분류했다.

- **admin 소관**: 이 감사의 범위. 공통코드/공지/배너/팝업/통계/정산/외부연계/RBAC/시스템로그/배치/설정/게시판/고객센터 등 크로스커팅·운영전용 기능.
- **타 서비스 소관**(member/donation/point/gift/order): 다른 담당 에이전트가 확인 중이므로 화면수만 집계하고 상세 조사 생략.
- 각 admin 소관 항목은 실제 `ghlove-web` Java 컨트롤러(`@RequestMapping`)로 살아있는 경로인지 먼저 확인한 뒤, `ghlove-msa/admin`의 컨트롤러+템플릿 존재 여부와 로직 동등성을 코드 레벨로 대조했다.

## 1. AS-IS opmanager 카테고리별 화면 현황 (집계표)

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

## 2. 이미 알려진 것 재확인만(상세 생략)

지시받은 대로 아래 항목은 이미 완료 기록이 있어 코드 존재만 재확인하고 깊이 파지 않음: 공통코드(`CommonCodeController`), 공지/배너/팝업 CRUD(`NoticeApiController`/`BannerApiController`/`content/popup-*`), 통계(`StatsController`/`GiveStatisticsController`/`ReportStatisticsController`/`ShopStatisticsTierBController`), 정산(`SettlementController`/`ReconciliationController`), 외부연계(`CertLoginController`/`NhExportBatchController`), 배송조회(`DeliveryTrackingController`), RBAC(`ManagerAuthController`/`ManagerRequestController`/`AccessController`), 2차인증+메뉴RBAC(`ManagerAuthAdvice`/`HeaderAuthAdvice`), 지정기부(`DesignatedProjectAdminController` 등), offgive+remittance, Kong(`OpenApiController`), MFA+SLA(`/stats/sla`). 전부 파일 존재 확인됨.

## 3. admin 고유 카테고리 gap 상세

### 우선순위: 높음

**1. 관리자 팀별 그룹 관리 (`categories-team-group`, 4화면)**

AS-IS `saleson.shop.categoriesteamgroup.CategoriesTeamGroupManagerController`(`/opmanager/categories-team-group`)는 관리자(매니저)를 팀/그룹 단위로 조직화하는 CRUD를 제공한다(팀 등록/수정/삭제, 그룹 등록/수정/삭제, 순서변경). `admin_rbac_6tier_role_round`에서 구현한 6단계 역할(ROLE_ADMIN_1~6)과는 별개 축으로, 관리자를 부서/팀으로 묶는 조직 구조 관리 기능이다. TO-BE `admin` 소스 전체를 grep해도 `team`/`Team`/`그룹` 관련 관리자 조직 개념이 전혀 없다.

**2. 그룹별 배너 관리 (`group-banner`, 2화면)**

AS-IS `GroupBannerManagerController`(`/opmanager/group-banner`, 클래스 주석 "그룹별 베너 관리")는 위 팀/그룹 단위로 스코프되는 별도 배너 타입이다. TO-BE `Banner` 엔티티(`OperationContentService`)는 `bannerType` 같은 구분 필드 없이 단일 타입(메인배너)만 다루므로 그룹 스코프 배너는 없음.

**3. 지자체 공지사항 (`locgov-notice`, 3화면)**

AS-IS `LocgovNoticeManagerController`(`/opmanager/locgov-notice/**`, 클래스 주석 "지자체 공지사항")는 일반 공지사항(`notice`, 커버됨)과 별개로 지자체 담당자만을 대상으로 하는 공지 채널이다. TO-BE `NoticeApiController`/`content/notice-*.html`은 대상 구분(전체공지 vs 지자체전용) 필드나 화면이 없어 이 세분화가 없음.

**4. 관리자↔회원 메시지 관리 (`message`, 2화면)**

AS-IS `MessageManagerController`(`/opmanager/message/**`, 메뉴명 "메세지 관리")는 아이디/메시지 내용으로 검색하는 쪽지(메시지) 발송·관리 화면이다. TO-BE `admin` 소스에 `/message` 경로나 관련 컨트롤러가 전혀 없음.

### 우선순위: 중간

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

### 우선순위: 낮음

**13. 게시판 마스터 설정 (`board-cfg`, 2화면)** — `com.onlinepowers.board.BoardCfgController`(`/opmanager/board-cfg`)는 신규 게시판 타입을 동적으로 생성/설정하는 범용 CMS 게시판 엔진 설정 화면이다. TO-BE는 게시판을 4종 고정 스키마(`CmntyBoardController`)로 재구현했으므로 신규 게시판 타입을 동적으로 만들 필요가 실질적으로 없다. 스킵해도 기능상 지장 없을 가능성이 높으나, 명시적 보류 결정은 아니므로 표에는 gap으로 남김.

**14. 파일첨부 유틸리티 (`file`, 1화면)** — 위지윅 에디터(SmartEditor2, 이미 보류 확인됨)에 종속된 첨부파일 브라우저. SmartEditor2 도입 여부가 정해지면 함께 처리 권장.

**15. 주소검색 팝업 (`juso`, 1화면)** — Daum 우편번호 API 연동 팝업. 화면이라기보다 공용 UI 위젯이며, 주소 입력 필드가 있는 각 도메인(회원가입/배송지 등)에서 이미 별도 방식(관련 서비스 자체 주소 입력)으로 처리됐을 가능성이 있음. 낮은 우선순위.

### 추가로 발견한 프로젝트 전역 이슈 (admin 소관은 아니나 기록)

**엑셀 다운로드 기능 전무**: `admin/src/main/java` 전체를 grep한 결과 `excel`/`Excel`/`엑셀` 매치가 0건이었다. AS-IS opmanager는 로그/통계/give-point/order 등 거의 모든 목록 화면에 엑셀 다운로드 버튼이 있었다(`point` 서비스 감사에서도 동일하게 지적됨 — `as-is-feature-audit-point.md` #2). admin 하나만의 문제가 아니라 프로젝트 전역 공통 컴포넌트 부재로 보이며, 여러 서비스 감사 결과를 취합해 "엑셀 다운로드 공통 모듈 도입"으로 한 번에 처리하는 것을 권장.

## 4. "AS-IS opmanager 123개 화면 전부 완료" 주장 검증 결론

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

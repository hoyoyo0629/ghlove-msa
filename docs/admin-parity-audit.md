# admin 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS opmanager(운영관리)를 "서비스로직도 화면단도 똑같이" 재현하기 위한
> **전수 갭 목록**. 방식 `[[as-is-parity-exhaustive-audit-method]]` — "발견되면그때"가 아니라 이 목록
> 기준으로 구현한다. NEW/CHANGED(ISP·RFP) 요건은 표기 + 착수 전 사용자 확인.
>
> 상태 범례: **O** 재현됨 / **X** 누락 / **부분** 일부만 / **확인** 추가 대조(심도검증) 필요.
> 작성 2026-09-23 (5개 서비스 완료 후 admin 라운드).

---

## 0. 이 문서의 위치 — 기존 심층감사 재조정(reconcile)

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

## 1. 09-03 "진짜미구현" 갭 → 현재 상태 재대조

### 1-A. 이미 닫힌 갭 (09-03 미구현 → 현재 TO-BE 컨트롤러 존재)

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

### 1-B. ★ 여전히 남은 갭 (현재도 TO-BE 컨트롤러/화면 없음 = X)

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

### 1-C. 심도검증 결과 (V1~V8 전건 완료 2026-09-23) — 컨트롤러 존재 → 기능parity 확정

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

## 2. AS-IS 죽은코드(재현 불필요) — 재확인만

09-03 감사에서 **죽은코드로 판정된 것(렌더 대상 JSP 물리적 부재)**: BoardCfg/Board(범용CMS),
AccountNumber, Attendance, BanWord, Calendar/CalendarManager, CampaignManager, CardBenefits,
CategoriesEdit/Filter/FeaturedBanner, Condition, Customer(고객사), Display, EmptyPage,
EventStatistics, GiftIGroup, GiftItem(사은품, 전체 주석), GoogleAnalytics(중복), Island,
Newsletter(빈스텁), OzViewTest, Transfer(주석), Store(소스에 "미사용" 명시) 등. → **재현 대상 아님**
([[as-is-parity-includes-disabled-state]]: AS-IS에서 죽어있으면 우리도 만들지 않음).

## 3. 범위밖(다른 도메인 소관)

Common(고객메인 AJAX)·Main(쇼핑몰 메인)·Calendar(고객)·Juso(주소검색 위젯) 등은 storefront(Vue3
SPA)/gift 소관. onepass/payment/simpleauth/magicline 자산 디렉터리는 코드 0(도메인 분산).

---

## 4. ★ admin 최종 갭 목록 (확정 — 구현 단계에서 이 목록대로)

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

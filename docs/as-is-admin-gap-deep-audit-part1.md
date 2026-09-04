# AS-IS opmanager → TO-BE admin 심층 갭 감사 (Part 1, 67개 컨트롤러)

조사 대상: `saleson.shop.**` 및 `com.onlinepowers.board` 하위 67개 AS-IS 컨트롤러.
방법: 각 컨트롤러의 실제 `@RequestMapping`/메서드, 반환 뷰 이름(`ViewUtils.getView(...)`, `"view:..."` 관례)을 확인하고,
`WEB-INF/views/opmanager/i18n/<폴더>`에 대응 JSP가 실제로 존재하는지 대조했다. TO-BE는 `admin/src/main/java/com/ghlove/admin/web/*`
51개 컨트롤러의 실제 매핑을 전수 확인했고, 필요한 경우 `gift` 서비스(답례품 상품/카테고리 API)도 함께 대조했다.
DB 존재여부는 `database/ddl/schema.sql`(원본 550테이블 통짜 덤프, 아직 어느 서비스에도 배분 안 된 테이블 포함)과
`database/ddl/service-admin.sql`(admin 서비스에 실제 마이그레이션된 테이블)을 각각 grep해 구분했다.

**dead-code 판정 기준**: AS-IS 컨트롤러가 반환하는 뷰 이름에 해당하는 JSP 파일이 `WEB-INF/views/opmanager/i18n/` 전체 트리(뿐 아니라 webapp 전체)에 단 하나도 없으면,
그 화면은 AS-IS 운영 환경에서도 렌더링 시 오류가 나는 고아 코드로 판단했다("이름이 낯설다"가 아니라 "렌더링 대상 파일이 물리적으로 없다"는 코드 근거).
단, 로컬 AS-IS 소스가 실제 운영 배포본과 100% 동일하다는 보장은 없으므로(SETUP.md 삭제 이력 등), 이 판정은 "로컬 소스 기준" 임을 전제로 한다.

## 요약

67개 컨트롤러 기준(단, #3 CommonController는 성격이 다른 기능 2개가 한 클래스에 섞여있어 범위밖/진짜미구현 양쪽에 중복 집계됨 - 그래서 합계가 68):

- 이미 커버됨: 26건
- AS-IS 죽은코드: 18건
- 진짜미구현: 21건
- 범위밖: 3건

## 분류표

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

## 진짜미구현 상세 (다음 라운드 구현 지시서)

### 3(b). 관리자 메인 대시보드(홈 화면)
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

### 13. BatchlogManagerController - 배치 실행이력 로그
AS-IS는 `OP_BATCH_JOB`에 등록된 배치가 실제 실행될 때마다 이력을 남기고 `/opmanager/batch-log`에서 조회했다. 그러나 TO-BE `BatchJobController`의 코드 주석에
명시된 대로 "실제 동적 스케줄러 엔진은 없다(각 서비스의 `@Scheduled`로 이미 개별 구현된 것과 별개)" - 즉 `OP_BATCH_JOB` 등록은 메타데이터 표시용일 뿐 실행 엔진이 없어
원래 의미의 "배치 실행이력"을 낼 소스 자체가 없다. **구현 전에 먼저 결정할 사항**: (1) 각 서비스의 실제 `@Scheduled` 메서드에 실행이력 기록을 추가해 배치로그를 재구성할지,
(2) 이 기능 자체를 스코프 아웃할지. 후순위로 유지.

### 19. CatalogManagerController - 카탈로그(연간사업/즐겨찾기상품/카드뉴스/콘텐츠) 관리
4개 서브탭:
- `catalogMng`: 연도별 카탈로그(연간 캠페인 묶음) 등록/수정, 카탈로그명·연도·기간 관리
- `locgovFavItem`: 지자체별 "즐겨찾기 상품"(지자체 담당자가 추천 답례품을 지정) - 연도/지자체 조합 키로 상품 리스트 등록
- `cardNews`: 카드뉴스(이미지 슬라이드형 콘텐츠) 등록/수정, 라벨 노출 토글
- `content`: 일반 콘텐츠(서브타입 존재) 등록/수정

**필요 DB**: `G_CATALOG_MNG`, `G_CATALOG_CARD_NEWS_MNG`, `G_CATALOG_CARD_NEWS_IMG_DESC`, `G_CATALOG_CONTENT_MNG`, `G_CATALOG_CONTENT_IMG_DESC`가 `database/ddl/schema.sql`
원본 덤프에 이미 정의되어 있으나 `service-gift.sql`/`service-admin.sql` 어디에도 마이그레이션되지 않았다 - 어느 서비스 소관인지(gift vs admin) 먼저 결정 후 DDL 이관 필요.

### 20. CategoriesManagerController - 답례품 카테고리 관리
답례품 상품 분류체계(카테고리) CRUD: 목록/생성/수정/삭제, 트리 구조 조회(`tree-list`), 상위-하위 이동(`move`), 노출순서 변경, SEO 메타 편집, 카테고리코드 중복확인.
gift 서비스의 `GiftApiController#/api/categories`는 **읽기 전용**(스토어프론트 카테고리 네비게이션용)이라 이 카테고리 체계를 실제로 만들고 편집하는 화면이 어디에도 없다 -
즉 지금 스토어프론트에 뜨는 카테고리 목록은 초기 시드 데이터 이후 한 번도 관리자가 수정할 수 없는 상태다.
**필요 DB**: `OP_CATEGORY`, `OP_ITEM_CATEGORY`가 원본 schema.sql에만 존재 - gift 서비스 스키마로 이관(현재 gift가 참조하는 카테고리 테이블과의 관계 재정리 필요) 후
admin 서비스에서 REST로 CRUD하거나 gift 서비스에 직접 관리자 API를 추가하는 두 방식 중 택1.

### 23. CategoriesTeamGroupManagerController - 카테고리 팀/그룹 관리
"팀"(대분류 상위 그룹)과 "그룹"(팀 하위, 프로모션성 묶음) 2계층 CRUD + 노출순서 변경 + 그룹→카테고리 전환 기능. `#56/#57`(GroupManagerController/GroupBannerManagerController)과
개념이 유사하나 별도 테이블(`OP_CATEGORY_TEAM`, `OP_CATEGORY_TEAM_ITEM`, `OP_CATEGORY_GROUP`, `OP_CATEGORY_GROUP_BANNER`)을 쓰는 별개 기능이다. 카테고리 관리(#20)와 함께
"카테고리/진열 관리" 화면군으로 묶어서 구현하는 것을 권장.

### 24. ClaimMemoManagerController - 고객 클레임 메모
주문/배송 관련 고객 클레임(불만/문의) 처리 이력을 자유 텍스트로 남기는 간단한 메모 게시판(`list`/`list` POST 검색만 존재, 단일 화면). order 서비스의 클레임/취소반품 처리와
연동되는 상담이력 관리 기능으로 추정. **필요 DB**: `OP_CLAIM_MEMO`(원본 schema.sql에만 존재) - order 서비스 주문ID를 FK로 참조하는 구조라 admin이 order 서비스 REST로
주문 정보를 조회하며 메모만 admin 자체 테이블에 쌓는 방식을 권장.

### 25. CntntsStsfdgManagerController - 콘텐츠 만족도 조사
콘텐츠(공지/이벤트 등) 하단에 "이 페이지가 도움이 되었나요?" 형태의 만족도 조사를 삽입하고, 관리자가 `list`(응답목록)/`detail`(상세)/`statistics`(집계)로 확인하는 기능.
JSP는 살아있으나(`cntnts-stsfdg/form.jsp`, `list.jsp`) 실제 사용 빈도는 낮을 것으로 추정 - 낮은 우선순위.

### 27. CommentManagerController + 28(커뮤니티 댓글 하위기능)
AS-IS 커뮤니티 게시판(자유게시판/SR게시판/FAQ게시판/오프라인SR게시판) 각각에 댓글 CRUD(`cmnt/add`, `cmnt/update`, `cmnt/delete`, 첨부파일 업로드/다운로드)가 있고,
별도로 `CommentManagerController`가 전역 댓글 카운트/목록/CRUD API를 제공한다. TO-BE `CmntyBoardController`는 게시글 CRUD만 구현했고 댓글 관련 엔드포인트가 전혀 없다.
**필요 작업**: `CmntyBoard` 계열 엔티티에 댓글 서브엔티티(또는 별도 `CmntyComment` 테이블) 추가 + `CmntyBoardController`에 댓글 CRUD 서브리소스 확장.

### 33. ConfigManagerController - 쇼핑몰 설정 허브(잔여 ~10개 서브화면)
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

### 38. DataboardManagerController - 고객센터 자료실 관리자 CRUD
TO-BE `DataBoardController`는 코드 주석에 명시된 대로 "로그인 불필요, 공개 화면"인 **조회 전용**이다(`/data-board`, `/data-board/{id}`, 파일다운로드만 존재).
AS-IS `DataboardManagerController`(`/opmanager/data-board`)는 이 자료실 게시물을 실제로 등록/수정/삭제하는 관리자 화면이며 JSP도 살아있다(`data-board/list.jsp`, `form.jsp`).
즉 지금 TO-BE는 "자료실을 보여줄 수는 있지만 아무도 새 자료를 올릴 수 없는" 상태다. **필요 작업**: `DataBoardController`가 참조하는 것과 동일한 `DataBoard`/`DataBoardFile`
엔티티를 대상으로 관리자 CRUD 컨트롤러 추가(list 검색/create/edit/delete/파일첨부, AS-IS는 검색조건 `search-date`, 이미지 삭제 `delete-item-image` 포함). 신규 스키마 불필요, 우선순위 高.

### 39. DeliveryCompanyManagerController - 택배사 마스터 관리
택배사 코드/이름/사이트URL 등록·수정·삭제(목록 조회는 POST 검색 포함). `DeliveryTrackingController`(TO-BE)는 이미 등록된 택배사 코드로 배송조회를 "사용"하는 화면이지
택배사 코드 자체를 등록/관리하는 화면이 아니다(`commonCodeService.labelsOf("DELIVERY_CARRIER")`로 공통코드를 참조). 공통코드(`CommonCodeController`, `/codes`)로
`DELIVERY_CARRIER` 타입 코드를 관리하면 최소 기능은 대체 가능하나, AS-IS는 사이트URL 등 확장 필드를 갖고 있어 완전 대체는 아니다.
**권장**: 별도 화면을 새로 만들기보다 `CommonCodeController`의 코드 확장 필드로 흡수하는 방안을 우선 검토.

### 45. FaqManagerController - FAQ 관리자 CRUD
TO-BE `FaqController`(Thymeleaf)와 `FaqApiController`(JSON)는 둘 다 클래스 주석에 "로그인 불필요, 공개 화면"이라고 명시된 스토어프론트 전용 컨트롤러다.
관리자가 FAQ를 등록/수정/삭제하는 화면이 어디에도 없다 - 즉 현재 FAQ 데이터는 최초 시드 이후 절대 바뀔 수 없다.
AS-IS `FaqManagerController`(`/opmanager/faq`)는 목록(검색조건 포함)/생성/수정/삭제를 제공한다.
**필요 DB**: `OP_FAQ`가 이미 `service-admin.sql`에 존재(라이브, `FaqService`가 이미 사용 중) - `FaqAdminController` 신설 + CRUD 로직만 추가하면 즉시 동작 가능. 우선순위 高(구현 난이도 낮고 가치 확실).

### 46. FeaturedManagerController - 기획전(featured) 관리
상품을 묶어 "기획전" 형태로 노출하는 목록/생성/수정/삭제(+이미지 삭제) 기능. AS-IS 소스 내에서도 배너생성(`banner-create`), URL검색, 이벤트답글관리(`manage-event-reply`)
서브기능은 이미 주석처리되어 죽어있어 실제 살아있는 범위는 CRUD 핵심 4개뿐이다. `DisplayManagerController`의 "spot"(특가/기획전)과 개념이 겹치나 `#41`은 완전 죽은코드이므로
`FeaturedManagerController`가 사실상 유일한 생존 "기획전" 기능이다. **필요 DB**: `OP_FEATURED`, `OP_FEATURED_ITEM`(원본 schema.sql에만 존재).

### 50. GiveNoticeManagerController - 기부접수기관 대상 공지 관리
기부금 접수기관(사업자/판매자)에게 노출되는 공지사항을 관리자가 등록/수정/삭제(+검색조건, 사업자별 삭제)하는 화면. `give-operation`/`give-point`/`give-state`/`give-statistics`
4종 TO-BE 컨트롤러 중 공지 기능을 가진 것은 없다. JSP는 살아있다(`give/give-notice/list.jsp`, `form.jsp`, `edit.jsp`). **선결 조사사항**: 원본 schema.sql에서
정확한 테이블명을 확인하지 못했다(패턴 매칭 실패) - 구현 착수 전 AS-IS MyBatis 매퍼(give-mapper.xml류)에서 실제 테이블명을 재조사 필요.

### 56+57. GroupManagerController / GroupBannerManagerController - 카테고리그룹 + 그룹배너
`GroupManagerController`는 카테고리 그룹(구버전, `OP_GROUP`) CRUD, `GroupBannerManagerController`는 그룹별 배너 등록(`OP_CATEGORY_GROUP_BANNER`)이다.
`#23`(CategoriesTeamGroupManagerController)의 "그룹" 개념과 유사해 보이나 별도 테이블을 쓰는 병행 기능으로 보인다 - 구현 전 두 기능의 실제 차이(신버전/구버전 여부,
AS-IS 내 실사용 화면 대조)를 좀 더 조사해 통합 여부를 결정하는 것을 권장. 두 화면 모두 JSP는 살아있다.

### 58. InquiryManagerController - 1:1 상담 문의(구버전)
`OP_SHOP_INQUIRY` 테이블 기반의 목록/상세/답변/첨부파일다운로드. TO-BE `QnaAdminController`는 클래스 주석에 명시된 대로 `OP_QNA`/`OP_QNA_ANSWER`를 쓰는 **별개 시스템**이라
이 기능을 커버하지 못한다. AS-IS에 `InquiryManagerController`(구, `OP_SHOP_INQUIRY`)와 `QnaManagerController`(신, `OP_QNA` - 이번 조사대상 67개에는 미포함, 이미 이전 라운드에서
QnaAdminController로 이전 완료된 것으로 추정)가 공존하는 것으로 보아, AS-IS 자체가 신/구 시스템 과도기다. **결정 필요**: 구버전 데이터가 실제 운영에 남아있다면 조회 전용
화면만이라도 이식하는 것을 권장(신규 문의 접수는 이미 QNA로 통합됐을 가능성이 높음).

### 62. LclgvHnrUserManagerController - 지자체 명예직원 관리
지자체 "명예사용자/명예직원"(대표 답례품 홍보 등을 위촉받은 사람으로 추정) 등록/수정/삭제 + 열람이력 조회(엑셀다운로드 포함) 2탭 구성. JSP가 풍부하게 존재해 실사용
가능성이 높다. **선결 조사사항**: 원본 schema.sql에서 메인 테이블명을 찾지 못했다(서브테이블 `G_LCLGV_HNR_USER_RWRD_IMG_EXPLN`만 확인) - AS-IS 매퍼 XML에서
정확한 테이블명 재조사 필요.

### 63(부분). LogManagerController - 엑셀다운로드로그 + 기부금영수증 국세청연계로그
`AuditLogController`가 커버하지 못하는 두 그룹:
1. **엑셀다운로드 로그**(`exceldownload-log`, `exceldownload-reason`, 팝업 상세/이력): 관리자가 회원/주문/통계 등에서 엑셀 다운로드를 실행할 때마다 사유를 입력하고
   그 이력을 조회하는 감사로그. `admin-pii-display-no-masking`/`as-is-feature-audit-round` memory에서 이미 "admin에 엑셀다운로드 0건" 패턴이 지적된 바 있는데,
   이 로그 화면 자체도 미구현 상태임이 이번에 재확인됨.
2. **기부금영수증 국세청연계 로그**(`gif-stnd-buga`/`gif-stnd-sunap`/`gif-seoul-buga`/`gif-seoul-sunap`): 표준양식/서울시 특별양식 기부금영수증의 국세청 "부가"(발급)/"승인"
   전송 이력 조회 4종. 국세청 연계 자체가 mock 처리 대상이더라도(외부연계 아키텍처 원칙상), 이력을 조회하는 관리 화면은 필요.

### 64. UserLoginBannerManagerController - 로그인화면 전용 배너
AS-IS는 웹/모바일 로그인 페이지에 별도 배너 이미지를 노출하는 기능(`loginWeb`, `loginMobile` POST, 이미지 삭제)을 갖고 있다. `OperationContentController`의 범용
배너 CRUD(`/banners`)가 이미 있으나, "로그인 페이지"라는 특정 노출 위치/타입을 지원하는지는 코드상 확인하지 못했다 - 지원하지 않는다면 배너 타입 코드 하나 추가하는
수준의 경미한 작업이므로 낮은 우선순위.

### 65. MagicLineController - 전자서명 캡처(매직라인 연계)
오프라인 가입/본인인증 시 태블릿 서명패드로 서명을 캡처해 저장하는 기능(`signedFormR`, `vidClientIDNR`). offgive(오프라인 기부접수) 라운드에서 이 컨트롤러를 참조한
흔적이 없어(grep 0건), `external-integrations-architecture` memory의 "enabled=false mock-gated" 패턴이 적용되지 않은 채 그냥 빠진 것으로 보인다.
전자서명 자체가 법적으로 필수가 아니라면(단순 UX 요소), mock 서명패드 UI만 추가하는 낮은 우선순위 작업으로 충분해 보인다.

### 60. ItemManagerController - 답례품 상품 통합관리 (부분 커버, 잔여 대규모)
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

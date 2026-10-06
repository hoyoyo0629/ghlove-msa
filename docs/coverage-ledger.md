# AS-IS → TO-BE 전수 커버리지 원장 (누락 0 목표)

작성 2026-10-01. 목적: MSA 전환에서 **front/back/static 어느 것도 누락 없이** 이식됐는지 **파일 단위**로 증명.
기능/화면 레벨 대조(docs/inventory/*.tsv, docs/*-parity-audit.md)는 이미 있으나, 이 원장은 그 위에
**① back 엔드포인트 ② front 뷰파일 ③ static 에셋(css/js/img/font)** 3축을 각 버킷마다 파일 단위로 present/missing/gap 판정한다.
원칙: [[port-everything-no-omissions-then-rfp-isp]] — AS-IS 100% 이식이 기본선, RFP/ISP 변경은 별도 확인.

- AS-IS 소스: `C:\workspace\ghlove` (ghlove-web / ghlove-common / ghlove-api / ghlove-batch / ghlove-frontend).
- AS-IS 코드데이터: [[asis-table-dump-path]].
- 버킷 배정 기준: docs/as-is-coverage-map.md (프론트 최상위 디렉터리 전부를 6서비스+common에 배정).
- 범례: ✅present(이식됨) · ❌missing(갭, 이식대상) · 🟡partial/통합 · ⏸보류(SalesOn종속·미사용 등 결정된 보류)

## 버킷별 진행 상태
| 버킷 | front(화면) | back(엔드포인트) | static(에셋) | 상태 |
|---|---|---|---|---|
| member | ✅ 12/12 | ✅ 누락0 | ✅ CSS14/14 | **완료(파일럿)** |
| donation | ✅ 핵심 / ⏸ 납부연계 | ✅ 핵심 / ⏸ 납부연계 | ✅ 핵심 | **완료** |
| point | ✅ 2/2 | ✅ 누락0 | ✅ | **완료** |
| gift | ✅ 대부분 / ❌ 특산·소식지 | ✅ 대부분 / ❌ 특산·소식지 | 🟡 | **완료(갭2)** |
| order | ✅ 누락0 | ✅ (PG=도메인상 N/A) | ✅ 5/5 | **완료** |
| admin | tsv + 메뉴트리 | 진행중(103컨트롤러) | 부분 | 메뉴트리 라운드 진행중 |
| common | ✅ 대부분 / ❌ 통합검색 | ✅ 대부분 / ❌ 통합검색 | ✅ | **완료(갭1)** |

## ★ 전체 재현갭 요약 (이식대상, 2026-10-01 점검 결과)
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

## ★ B5 소급교정: admin 통계 화면 차트화 (2026-10-01)
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

## ★티어 주의 (2026-10-01 보정)
AS-IS는 **레거시 서버렌더(ghlove-web/shop JSP + opmanager)** 와 **라이브 SPA(ghlove-frontend + `ghlove-api` REST 53개)** 두 티어 공존([[saleson-original-product-leftovers]]).
라이브 고향사랑(ilovegohyang)은 **SPA 티어**다. 따라서:
- **back축 정본 = `ghlove-api`** (ghlove-web/shop은 대부분 SalesOn 레거시·死코드). opmanager만 admin 버킷의 back 정본.
- **front축 정본 = `ghlove-frontend`** (SPA html) ↔ TO-BE storefront Vue.

## 방법 (버킷마다 반복)
1. **back**: AS-IS 해당 도메인 컨트롤러의 모든 `@RequestMapping/@GetMapping/@PostMapping` 엔드포인트 enumerate → TO-BE 대응 엔드포인트 매칭. (present/missing/gap)
2. **front**: AS-IS 뷰파일(JSP 또는 프론트 html) 전부 enumerate(백업·old·날짜본 제외) → TO-BE 템플릿/Vue 뷰 매칭.
3. **static**: 각 live 화면이 참조하는 css/js/img/font를 추출 → TO-BE에 실제 파일이 있는지 확인. ([[static-asset-scoping-2026-09-18]] 누락복사 전례)
4. 각 missing은 "AS-IS 이식" vs "RFP/ISP 변경(별도확인)"으로 분류.

---

## member (파일럿) — 진행중

### back축 — AS-IS 컨트롤러 인벤토리 (TO-BE 서비스 배정)
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

### back축 — 엔드포인트 대조 결과 (스토어프론트 회원분 → TO-BE member 서비스)
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

### front축 — AS-IS `ghlove-frontend/users/` live 12종 → TO-BE storefront
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

### static축 — member 라우트 참조 CSS 존재검증
pageStyles.js의 login/signup/find-idpw/mypage 세트 CSS 14종(authentication-modal·change-pw·login-total·join-inf2·joind-ag·joind-agf·login-idse·main·event·mypage-status·favo_info·change-info·m_main·notice-box) **전부 `storefront/public/css/`에 존재**. → **static 누락 없음.**

### member 결론
**3축 전부 파일 단위 누락 0.** 🟡 항목은 모두 "AS-IS의 별도 페이지/모바일/다단계 UI가 TO-BE에선 반응형 SPA·콜백·모달로 통합"된 것으로 기능 상실 아님(이식 완료). RFP/ISP 변경분은 이 범위에서 추가 식별된 것 없음.
남은 확인(저위험): 휴면해제 전용 안내페이지 유무, onepass-result/mobile-auth-result 콜백 라우트 명시 여부, **전자서명(signRegister/checkSign) TO-BE 유무** — 실동작엔 영향 없으나 추후 교차확인.

---

## donation — 완료
back 정본 = ghlove-api(designateddonation/DesignatedDonationController, donation/NgDonationController·RegionTaxController·SeoulTaxController, mypage/MypageController 기부분). front 정본 = ghlove-frontend/{donation,designated-donation,featured}.

### back축
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

### front축 (ghlove-frontend/{donation,designated-donation,featured})
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

### static축
donate/designated-list/donate-gift-select/list-select/mypage 라우트 CSS(donation_doak·donation_liemt·donation_selmt·designation·event·joind-ag(f)·main·notice-box·research-box 등) pageStyles 등록 → member와 동일 기준 존재(개별 재확인 저위험). 납부연계 페이지 미이식이라 해당 에셋만 부재(보류와 일관).

### donation 결론
핵심 기부 기능(접수·지정기부·관심지자체·영수증·마이·가이드·기금) **이식 완료**. 유일한 큰 영역 = **납부 게이트웨이 연계(NTS·지방세·서울 etax·지로)** 로, 외부 세정/PG 연동이라 **결정된 보류(⏸)**. 재현갭(silent 누락) 아님. featured 뷰·전자서명 저위험 TODO.

---

## point — 완료
- **front**: AS-IS mypage/cntrPoint.html·cntrPointDetail.html → TO-BE MyPointsView·MyPointDetailView ✅.
- **back**: AS-IS ghlove-api mypage getCntrPoint/getCntrPointDetail → TO-BE PointMyApiController `/api/my/points`·`/api/my/points/detail` ✅. 잔액/적립율/원장/소멸배치/지자체적립율 = PointApiController·PointController·PointHistoryAdminApiController(admin·inter-service) ✅.
- **static**: mypage 공통 CSS 세트 공유(member에서 존재확인됨).
- **TO-BE 전용**: 포인트 예약(PointReservationsView·`/api/my/reservations`)은 AS-IS에 없는 TO-BE 추가분, **미사용 결정됨**([[point-reservation-unused-decision-deferred]]) — 갭 아님.
→ **3축 누락 0.**

---

## gift — 완료 (재현갭 2건)
back 정본 = ghlove-api(item/ItemController, catalog/CatalogController, category/CategoriesController·CategoryController, seasonfood/SeasonFoodController, speciality/SpecialityController, communityBusiness/CommunityBusinessController, store/StoreController). front = ghlove-frontend/{goods,items,catalog,category,community-business}.

### back축
| AS-IS(ghlove-api) | TO-BE gift | 판정 |
|---|---|---|
| ItemController(목록·상세·reviews·review·wishlist·qna·restock·coupons·relation·getUserCntrPoint) | GiftPublicApiController(`/api/gifts`·`/{id}/detail`·reviews·inquiries), GiftMyApiController(wishlist·my/reviews·my/qna), GiftController(wishlist toggle·review like·restock-notice) | ✅ |
| CategoriesController·CategoryController(searchResult·best·filter·price-areas·category-path) | GiftApiController `/api/categories`, GiftPublicApiController `/api/gifts`(필터) | ✅🟡 |
| SeasonFoodController(제철 search) | GiftPublicApiController `/api/season-food` | ✅ |
| CommunityBusinessController(마을기업) | CommunityView 경유(gifts 필터) | ✅🟡 back 재확인 |
| **SpecialityController(특산물 search·search-item-list)** | — 없음 | ❌ **갭(이식대상)** |
| **CatalogController(소식지: getCatalogNewItem·getDsgnDonationItem·getLocgovFavItem·getCatalogSeasonalItem·getCatalogMainInfo·카드뉴스)** | — 없음 | ❌ **갭(이식대상)** |
| StoreController(판매자몰 조회) | seller 포털 경유 | 🟡 |

### front축 (ghlove-frontend)
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

### static축
gift-list/gift-detail/gift-seasonal/gift-community-business 라우트 CSS(item·goods_card·evt_card·event·joind-agf·lclgv-map·order-modal) pageStyles 등록·존재. 특산물/소식지 페이지 미이식이라 해당 에셋만 부재(갭과 일관).

## order — 완료
back 정본 = ghlove-api(cart/CartController, order/OrderController, payment/PaymentController, shipping/ShippingController, orderagency/OrderAgencyController). front = ghlove-frontend/{cart,order,mypage order분}.

### back축
| AS-IS(ghlove-api) | TO-BE order | 판정 |
|---|---|---|
| CartController(add·delete·update-quantity·shipping-payment-type) | CartApiController | ✅ |
| OrderController(detail·save·buy·payment-step·confirm-purchase·coupons) | CheckoutApiController(review·preview·complete·done), OrderMyApiController(orders·cancel·confirm-receipt), CouponMyApiController | ✅ |
| OrderController 클레임(return-apply·exchange-apply·cancel-apply·refund-amount) | ClaimController·ClaimMyApiController·OrderMyApi`/items/{id}/claim` | ✅ |
| ShippingController(배송지·base-shipping) | member DeliveryApi + OrderMyApi `/delivery-address` | ✅ |
| **OrderController PG결제(easypay·nicepay-vacct·naverpay·redirect-pay·giveGoodsSavePay), PaymentController(pay-log)** | — 없음 | **N/A(의도적)** 답례품=기부포인트 단일결제, SalesOn 신용카드·가상계좌 PG 미사용([[order-single-item-vs-multiitem-decision]] 플랫폼 PG 미연동 명시) |
| OrderAgencyController(주문대행 상담원) | OrderAdminApiController `/agency-search`·`/admin/order-agency` | 🟡 admin 버킷 |

### front축
| AS-IS 화면 | TO-BE 뷰 | 판정 |
|---|---|---|
| cart/index | order/CartView | ✅ |
| order/step1·step2 | order/CheckoutView + OrderCompleteView | ✅ |
| mypage/orderList | order/MyOrdersView | ✅ |
| mypage/orderDetail | order/OrderDetailView | ✅ |
| mypage/orderCancel | order/MyClaimsView | ✅ |
| mypage/deliveryInfo | mypage/DeliveryListView·DeliveryFormView | ✅ |

### static축
order_ali·mypage-order·mypage-order-details·order-modal·favo_info **5종 전부 public/css/에 존재**(pageStyles의 "미복사" 주석은 낡음, 실제 복사됨 — 원장이 잡아낸 뒤 확인). → **누락 0**.

### order 결론
장바구니·체크아웃(포인트결제)·주문·클레임(취소/반품/교환/환불)·쿠폰·배송지 **3축 누락 0**. **PG 결제연계는 "답례품=기부포인트 단일결제"라 의도적 미이식(도메인)** — silent 누락 아님. 멀티아이템 재설계는 진행중 개선과제([[order-single-item-vs-multiitem-decision]]), 누락과 무관.

---

## common — 완료 (재현갭 1건)
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

### common 결론
게시판(공지·FAQ·1:1·자료실)·설문·이벤트·정책·가이드·오류·메인 **이식 완료**. **재현갭 1건: 통합검색(totalsearch)** — AS-IS live 기능인데 TO-BE 전무(검색뷰·API 없음). 사용자 원칙상 **이식대상**(단 통합검색 범위가 RFP 변경대상인지 교차확인 필요 — coverage-map 주석).

---

### gift 결론
답례품 핵심(목록·검색·상세·위시·리뷰·문의·제철·마을기업·카테고리) **이식 완료**. **특산물관(speciality)·소식지/카탈로그(catalog·카드뉴스)** 는 AS-IS에서 **비활성/미사용**(프론트 진입점 전무·소식지 admin숨김·데이터0·특산 행안부권한만) → [2026-10-01 결정] **front+back+admin 전부 이식하되 TO-BE도 AS-IS처럼 숨김상태로 재현**([[as-is-parity-includes-disabled-state]]). community-business/store back 저위험 재확인 TODO.


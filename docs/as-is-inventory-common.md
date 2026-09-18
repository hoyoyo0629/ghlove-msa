# AS-IS 코드 인벤토리 — common (6개 도메인 밖 전 영역)

> 7번째 시트. 배정 근거는 [[as-is-coverage-map]], 판정 절차는 [[as-is-inventory-procedure]].
> 작성 2026-09-10. 대상: GNB/LNB·레이아웃, 게시판류, 안내/정책, 통합검색, 메인화면,
> 소식지, 공통 인프라, 범용 게시판 프레임워크, 배치.

## 0. 규모

| 축 | 수량 |
|---|---|
| 컨트롤러 | 32개 (api 19 + 공통인프라/웹 13) |
| 엔드포인트 | 171개 (api 90 + 인프라 81) |
| 매퍼 쿼리 | 390개 (35개 매퍼 XML) |
| 살아있는 화면 | notice 2 · faq 1 · qna 4 · data-board 2 · qustnr 2 · event 8 · totalsearch 2 · policy 21 · guide 1 · error 1 · popup 1 · newsletter 77 · catalog 2 · 루트 5 |
| 레이아웃 컴포넌트 | 41개 파일 중 **live 14 / 사장 27** |
| 배치 잡 | 78개 (JobService 76 + JobPsintService 2) |

---

## 1. 레이아웃/내비게이션

### 1-1. 살아있는 레이아웃 컴포넌트 판별 (선행 작업)

`components/layouts/` 41개 파일 중 실제로 **주석 아닌 등록**이 있는 것만 live로 본다.

| 컴포넌트 | live 등록 | 판정 |
|---|---|---|
| `header_ali.vue` | 77 | **live** — 주 헤더 |
| `footer_ali.vue` | 81 | **live** — 주 푸터 |
| `mypage-lnb_ali.vue` | 23 | live |
| `header_g.vue` / `m_tabbar_g.vue` | 18 / 16 | live — 모바일 계열 |
| `donation_info-lnb_ali.vue` | 5 | live |
| `header.vue` / `footer.vue` | 2 / 2 | live — `category/index.html`, `featured/detail.html` 2화면 전용 |
| `header_news.vue` | 2 | live — `catalog/*` 전용 |
| `totalSearch.vue` | 5 | **전부 주석 또는 사장 헤더** → 아래 §4 |
| `sitemapModal.vue` | 6 | live |
| `item_tab-ali.vue` / `prj_tab-ali.vue` | 2 / 1 | live |
| `customer-lnb_ali.vue` | 12 | **전부 주석** → 사장 |
| `donation-lnb_ali.vue` | 6 | **전부 주석** → 사장 |
| `statistics-lnb_ali` · `statistics_tab` · `lnbbar_ali` · `sub-header_ali` · `swipemenu_ali` | 0 | 사장 |
| `subsearch_ali.vue` | 11 | **파일 자체가 없음** → 아래 §1-2 |
| `header_*_bak/_as-is/_temp/_20240529/_new`, `header_g_bak/_20240731/_map`, `m_tabbar_*`, `header_dsg_g` 등 | - | 사장 (백업·날짜본) |

### 1-2. 사장 등록 — `subsearch_ali.vue` (없는 파일을 6화면이 등록)

`donation-main.html:669`, `guide2.html:2587`, `list-select.html:440`, `map-select.html:1323`,
`faq/list.html:199`, `users/modify.html:606` 6곳이 주석 없이
`httpVueLoader('/components/layouts/subsearch_ali.vue')` 를 등록하는데 **그 파일은 존재하지 않는다**
(`subsearch_ali_unUsed.vue` 로 개명됨). 6화면 모두 템플릿에 `<layout-subsearch-ali>` 를 놓지
않아 httpVueLoader가 지연 로딩만 하고 실제 404는 나지 않는다 → **무해하지만 완전한 사장 등록**.
mypage 5화면(`inquiryItem`·`orderCancel`·`orderDetail`·`orderList`·`review`)은 같은 줄이 주석 처리돼 있다.

### 1-3. GNB — AS-IS 6 대메뉴 / 23 소메뉴 vs MSA

`header_ali.vue:221~` 의 `menuItems` 하드코딩 트리(서버 조회 아님)와 `AppHeader.vue` 대조.

| 대메뉴 | AS-IS 소메뉴 | MSA |
|---|---|---|
| 기부 | 자치단체에 기부하기(`func`), 특정사업에 기부하기 | `/donate`, `/designated-donation` — 일치 |
| 답례품 | 답례품몰, 제철식품관, 마을기업관 | `/gifts`, `/gifts/seasonal`, `/gifts/community-business` — 일치 |
| 안내사항 | 기금사업 소개, 고향사랑기부제 안내, 온라인 기부방법, 오프라인 기부방법, 연말정산 세액공제 안내, 고향사랑기부 주의사항 | `/list-select`, `/guide1`, `/guide2`, `/guide5`, **`/honor`**, `/guide6` — 일치 |
| 이벤트 | 진행중/종료된 이벤트 | `/events?ing=Y|N` — 일치 |
| 고객센터 | 공지사항, 자료실, Q&A, FAQ | `/notices`, `/data-board`, `/qna/board`, `/faqs` — 일치 |
| 마이페이지 | 14항목 | 16항목 — **차이 있음(§1-4)** |

`/donation/guide3.html`(연말정산 세액공제 안내)의 MSA 라우트 이름이 `/honor`(`TaxCreditGuideView.vue`)인
것은 파일명이 아니라 화면 내용으로 확인했다 — [[verify-screen-by-content-not-route]] 의 guide3 사례와 같다.

### 1-4. GNB 마이페이지 항목 차이

- **AS-IS에 있고 MSA GNB에 없음(2)**: `답례품 Q&A`, `답례품 후기`
  → 화면·라우트는 이미 있다(`/mypage/gift-qna`, `/mypage/gift-reviews`). 푸터 사이트맵
  (`AppFooter.vue:107~108`)과 마이페이지 홈(`MyPageView.vue:115~119`)에도 있다.
  **GNB 드롭다운에만 빠졌다** → 조치대상.
- **MSA에만 있음(4)**: `회원정보수정`, `비밀번호 변경`(AS-IS는 마이페이지 LNB에 존재),
  `지자체담당자·제공자 역할 신청`, `역할신청 승인함`(RFP 신규) → 근거 있음, 유지.

### 1-5. 푸터 FNB — 1:1 일치

AS-IS `footer_ali.vue:160~179` 5항목(개인정보처리방침·저작권정책·이용약관·사이트맵·공지사항)이
MSA `AppFooter.vue:25~48` 과 순서까지 동일. 사이트맵도 AS-IS와 같은 CSS `:target` 모달 방식.

### 1-6. 플로팅 버튼 — 6종 중 4종만 구현

AS-IS `components/ui/fixedBtn.vue` (live 등록 67곳) 기능: **소식지, 챗봇상담, 카탈로그,
카카오채널, TOP, 접기/열기**.
MSA `FloatingButtons.vue`: 카카오채널, 챗봇상담, TOP, 접기/열기.
→ **소식지·카탈로그 버튼 2종 미구현** (§6·§7).

### 1-7. 고객센터 LNB — AS-IS는 사장, MSA는 구현

`customer-lnb_ali.vue` 는 12개 화면 전부에서 주석 처리돼 AS-IS에서 렌더되지 않는다.
MSA는 `components/CustomerLnb.vue` 를 실제로 노출한다 → **AS-IS에 없는 것을 추가**.
[[scope-migration-not-greenfield]] 기준으로는 과잉이지만, 사장 사유가 "메뉴 축소"가 아니라
방치로 보이고 사용성 손해가 없어 **현행 유지 + 기록**으로 둔다.

---

## 2. 게시판류 (공지·FAQ·Q&A·자료실)

AS-IS 프론트는 `op.saleson.js` 래퍼를 거치므로 **2단계(엔드포인트→래퍼→화면)** 로 판정했다.

| AS-IS 엔드포인트 | 래퍼 | 살아있는 호출 화면 | MSA 대응 | 판정 |
|---|---|---|---|---|
| `GET /api/notice` | `getNotice` | `notice/list`, `donation/list-select`, `donation/map-select`, `totalsearch/index` | `GET /api/notices/list` (admin) | 대응 |
| `POST /api/notice/detail` | `detailNotice` | `notice/detail` | `GET /api/notices/{id}` | 대응 |
| `POST /api/notice/hits` | `updateNoticeHits` | `notice/list`, `notice/detail`, `list-select`, `totalsearch` | 조회수 증가 전용 API 없음 — `/api/notices/{id}` 내부 처리 | 대응(방식차) |
| `GET /api/notice/donation/list` | `getDonationNotice` | `donation/list-select` | 없음 | **미구현** |
| `GET /api/faq` | `getFaq` | `faq/list`, `designated-donation/details`, `totalsearch` | `GET /api/faqs` | 대응 |
| `POST /api/faq/hits` | `updateFaqHits` | 위 3화면 | `POST /faqs/{id}/hit` | 대응 |
| `GET /api/data-board` (+`/detail`,`/file-download`) | `getDataboard` 외 | `data-board/list`,`detail`, `totalsearch` | `/api/data-board`, `/{id}`, `/data-board/file-download/{fileId}` | 대응 |
| `GET /api/data-board/dash-board` | — | **래퍼 없음** | 없음 | **사장** |
| `GET /api/qna/qna-open` 계 9종 | `getQnaOpen` 외 | `qna/qna-open`,`qna-detail`,`qna-edit`,`qna-form` | `/api/qna/board`, `/api/qna/board/{qnaId}`, `/api/qna` (등록·목록) | 대응 |
| `GET /api/qna/inquiry` 계 3종 | `getInquiries` 외 | `mypage/inquiry` | member 1:1문의 | 대응 |
| `POST /api/qna/delete-inquiry` | `deleteInquiry` | **없음** | — | **사장** |
| `GET /api/qna/item-inquiry` 계 3종 | `getItemInquiries` 외 | `mypage/inquiryItem`, `items/details-main`, `item_tab-ali` | gift 답례품 Q&A | 대응 |
| `GET /api/help` · `/detail` | `getHelp`,`getHelpDetail` | **없음** | `/admin/manuals` (운영자용) | **사장(사용자 매뉴얼 화면)** |
| `GET /api/help/file-download/{mnlSn}`, `/comm/file-info` | `getHelpFileDownload`,`getCommHelpFileInfo` | `components/ui/helpBtn.vue` | 없음 | **미구현** |

MSA 게시판 공개 읽기 API는 전부 **admin 서비스**가 호스팅하고 storefront가
`api.get('admin', ...)` 로 부른다(`storefront/src/api/http.js` 의 `SERVICE_PREFIX`).

### 2-1. 게시판 매퍼 죽은 쿼리

| 매퍼 | 전체 | 사장 | 사장 쿼리 |
|---|---|---|---|
| `notice-mapper.xml` | 20 | 1 | `getSellerNotice` |
| `qna-mapper.xml` | 36 | 3 | `insertQnaAnswerFile`, `updateQnaAnswerCountZero`, `updatesQna` |
| `qna-admin-mapper.xml` | 21 | 0 | |
| `databoard-mapper.xml` | 15 | 0 | |
| `qustnr-mapper.xml` | 21 | 0 | |
| `help-mapper.xml` · `manual-mapper.xml` | 5 · 13 | 0 | |
| `policy-mapper.xml` · `popup-mapper.xml` | 10 · 8 | 0 | |

---

## 3. 안내/정책 화면

### 3-1. 약관 3종 — 대응

`/api/policy/{protect,clause,copyright,marketing}` → MSA `GET /api/policies/{name}` +
donation `GET /policy/{privacy,copyright,auth}`. 편집 주체는 admin 약관관리(`OP_POLICY`).
`getPolicyMarketing`(마케팅 활용동의)은 **살아있는 호출부 없음 → 사장**.

### 3-2. 개인정보처리방침 이력본 18종 — **본문 링크가 전부 깨짐** → ✅ 조치완료 (§14)

AS-IS `policy/` 21개 중 3개(`privacy`·`copyright`·`auth`)만 실제 화면이고, 나머지 18개는
`privacy-form<날짜>.html` **개정 이력 정적 아카이브**다. 각 이력본이 이전 버전 전부를
`href="/policy/privacy-form....html"` 로 링크하는 자기연쇄 구조이고, 바깥에서 들어오는
링크는 **현행 개인정보처리방침 본문 안에만** 있다.

문제였던 것: MSA로 이관한 본문 `donation/src/main/resources/policy-content/privacy.html` 이
그 아카이브를 **17곳(고유 16종)** 링크하는데, MSA에는 해당 파일도 라우트도 없었다.
→ 개인정보처리방침에서 "이전 버전 보기"를 누르면 전부 404. §14에서 해소.

`privacy-form20231026_temp.html` 은 어디서도 참조되지 않는 작업본 → 사장(이관 제외).
[[policy-content-db-migration-deferred]] 와 연결되지만, 아카이브 16종은 **불변 스냅샷**이라
편집 대상 원문(OP_POLICY)과 성격이 다르다 → 정적 파일로 이관, DB 적재를 기다릴 필요 없음.

### 3-3. 사이트맵 · 오류화면 · 팝업

| AS-IS | MSA | 판정 |
|---|---|---|
| `guide/sitemap.html` + `sitemapModal.vue` | `AppFooter.vue` 내 `#modal_sitemap` (CSS `:target`) | 대응 |
| `error/404.html` (op.saleson.js:406,427 이 `location.replace`) | 전용 오류화면 없음 | **미구현** |
| `GET /api/popup/list`, `/api/popup/index/{id}` (`popup-layer.vue`, `popup-layer-kakao.vue`, `main.html`, `event/seasonal.html`) | admin CRUD(`/popups` 7종) + **공개 API `/api/popups` + storefront `SitePopups.vue`** | ✅ 조치완료 (§15) |

팝업은 admin에서 등록·수정·삭제·토글까지 다 되는데 **사용자 화면에 띄우는 경로가 없었다**
— 공개 조회 API도, storefront 팝업 레이어 컴포넌트도 없었다. AS-IS 메인화면은 `focusPopup()`·
`getJoinComfirmPop()` 으로 실제 노출한다(§5). §15에서 해소.

---

## 4. 통합검색 — AS-IS에서 이미 진입점이 거의 죽어 있다

`TotalSearchController` 15 엔드포인트(지자체·답례품·공지·Q&A·FAQ·자료실·안내·이벤트·특산품
9종 교차검색 + 자동완성·인기어·최근검색어 6종)는 모두 `totalsearch/index.html` 과
`components/layouts/totalSearch.vue` 에서만 쓰인다.

**그런데 `totalSearch.vue` 는 살아있는 화면에 등록되지 않는다:**
- `header_ali.vue:202` — 등록 줄이 **주석**
- `header_ali.vue:43` — 헤더 통합검색 버튼도 **주석**
- 나머지 등록처(`header_ali_20240529`, `header_ali_as-is`, `header_ali_new`)는 전부 사장 헤더
- `header_news.vue:191` 도 주석

살아있는 진입점은 **메인화면 검색창 하나뿐**이다 — `main.html:141` 입력 →
`searchPage()`(`main.html:530~536`) → `/totalsearch/index.html?searchKeyword=`.

| | AS-IS | MSA |
|---|---|---|
| 메인 검색창 | 있음, placeholder "기부할 고향 및 답례품을 검색해보세요" | 있음, **placeholder 동일** |
| 검색 결과 | `/totalsearch/index.html` — 9개 카테고리 교차검색 | `/gifts?q=` — **답례품 검색으로 축소** |
| 헤더 검색 | 주석 처리(사장) | 없음 |
| 최근검색어·인기검색어·자동완성 | `totalSearch.vue`에만 → 사장 | 없음 |

→ **통합검색 미구현은 실사용 기능의 누락이 맞다**(메인 검색창이 살아 있으므로). 다만 헤더
검색·자동완성·최근검색어는 AS-IS에서 이미 사장돼 있어 **재현 대상이 아니다**. 구현 범위를
"메인 검색창 → 9종 교차검색 결과화면"으로 좁힐 수 있다.

`totalsearch-myrecent-mapper.xml`(4) · `search-mapper.xml`(8) · `keyword-mapper.xml`(6, 그중
`getAutoComplete` 사장) 은 소스 기준으로는 호출되지만 화면 도달이 위와 같이 막혀 있다.

---

## 5. 메인화면 — 정의된 섹션 6개가 호출되지 않는다

`main.html` 의 `mounted`(`:1077~1100`)가 실제로 부르는 것은 6개뿐이다:
`getInitBanner()`, `getJoinComfirmPop()`, `makeOpenGraphTag()`, `focusPopup()`,
`getDsgncntrList()`, `confirmPbanc(true)`.

**정의만 되고 한 번도 호출되지 않는 메서드(사장 섹션)**:
`getMdItems`, `getNewItems`, `getGroupBestItems`, `getDisplayStyleBooks`, `getEvent`, `getPromotion`
— 6개 모두 `def=1 / call=0`. 템플릿 `v-for` 도 `swiperList`(배너)·`prjCard`(특정사업)·
`mobTab`(모바일탭) 3개뿐이라 MD추천·신규·인기 답례품, 스타일북, 이벤트, 프로모션 섹션은
**렌더되지 않는다**.

| AS-IS 현행 메인 구성 | MSA `HomeView.vue` | 판정 |
|---|---|---|
| 배너 슬라이더(`swiperList`) | `banners` + 자체 자동재생 | 대응 |
| 총 기부금(`<total-give-state>`) | `giveState` (`GET /api/home`, member) | 대응 |
| 로그인 전/후 박스 | `login-section` | 대응 |
| 특정사업(`prjCard`, `getDsgncntrList`) | `projects` 4건 | 대응 |
| 반쪽 배너(`banner-half`) | `banner-half__*` | 대응 |
| 검색창 | 있음(단, §4대로 답례품 검색) | 부분 |
| **팝업(`focusPopup`, `getJoinComfirmPop`)** | 없음 | **미구현** |
| 모바일 탭바(`m_tabbar_g.vue`) | 없음 | 미구현(모바일) |
| MD추천·신규·인기·스타일북·이벤트·프로모션 | 없음 | **재현 불필요(AS-IS 사장)** |

`main-mapper.xml` 24개 중 4개 사장: `dashBoardReportForCal`, `getOpmanagerMainCall`,
`getOpmanagerMainAmountChart_OLD`, `getOpmanagerMainCountChart_OLD`.

---

## 6. 소식지(newsletter) 77건 — 정적 아카이브

`newsletter/` 77개 html은 발행분 정적 페이지(vol001~)이고 애플리케이션 기능이 아니다.
서버 컨트롤러도 매퍼도 없다. 살아있는 진입점은 **플로팅 버튼 `fixedBtn.vue:75`**
(`$s.redirect("/newsletter/main.html")`) 하나 — 사장 헤더 `header_ali_as-is.vue:889` 에도 있지만 무효.

→ **판정: 콘텐츠(정적 자산). 코드 이관 대상 아님.** 다만 §1-6대로 MSA 플로팅 버튼에
소식지 진입 버튼이 없으므로, 아카이브를 어디에 둘지와 함께 결정이 필요하다(운영데이터 이관 이슈).

`catalog/catalog-main.html`·`catalog-faq.html` (전자 카탈로그, `header_news.vue` 전용 헤더 사용)도
같은 성격이며 진입은 `fixedBtn.vue` 의 `catalogConfirm()` — MSA 미구현.

---

## 7. 온라인 설문(qustnr) — 공개 참여 경로 → ✅ 조치완료 (§16)

| AS-IS | MSA |
|---|---|
| `GET /api/qustnr{qustnrSn}` — 설문 조회 (`qustnr/detail.html`, `detail_srvy.html` 이 직접 호출, 래퍼 없음) | admin `GET /api/surveys/{id}`·`/active` + storefront `SurveyView.vue` |
| `POST /api/qustnr{qustnrSn}` — 응답 등록 | admin `POST /api/surveys/{id}/responses` |
| `QustnrManagerController` (운영자 설문관리) | `QustnrAdminController` `/admin/surveys` 7종 — **대응** |

`qustnr-mapper.xml` 21개 쿼리 전부 살아 있다. **운영자는 설문을 만들 수 있으나 사용자가
참여할 화면이 없었다** → §16에서 공개 참여 경로를 붙여 해소.

---

## 8. 공통 인프라 (`ghlove-common`)

| 영역 | AS-IS | 매퍼 | MSA 대응 |
|---|---|---|---|
| 공통코드 | `code-mapper`(10, 사장 0) | | admin `CommonCodeController` + 각 서비스 `codesOf()` |
| 파일 업로드/다운로드 | `FileController`(2), `FileUploadController`(6), `ProgramFileDownloadController`(1) | `framework-file`(13) | 서비스별 개별 구현 |
| 스마트에디터 | `SmartEditorController`(5) | — | admin 위지윅 |
| 썸네일 | `ThumbnailController`(1) | — | 없음 |
| 메시지 | `MessageController` | `message-mapper`(9), `framework-message`(2) | admin `UmsAdminController` |
| 시퀀스 | — | `framework-sequence`(9) | DB 시퀀스 |
| 토큰 | — | `framework-token`(4) | JWT `GH_AUTH` |
| 캐시 | `GET /api/reload-cache/{cacheName}` | — | **프론트 호출부 0 → 사장** |
| 금칙어 | `BanWordController` | `banword-mapper`(7, `updateBanWord` 사장) | 없음 |
| 접속통계 | `POST /api/common/visit` | `access-mapper`(5) | 없음 — §8-1 |
| 변경이력 | — | `change-log-mapper`(2) | admin `AuditLogController` |
| 엑셀다운로드 로그 | — | `exceldownload-log-mapper`(4) | admin `ExcelDownloadLogAdminController` |
| 점검(maintenance) | `MaintenanceController` | `maintenance-mapper`(11) | admin `MaintenanceAdminController` |
| 사이트설정 | `ConfigManagerController` | `config-mapper`(15) | admin `ShopConfigController`, `SiteConfigExtController` |
| 만족도 | `StsfdgController`(1) | `stsfdg-mapper`(1) | 없음 |
| 오류처리 | `ErrorController`(2), `CommonExceptionAdvice` | — | 서비스별 |

### 8-1. `/api/common` 19종 — 주 헤더가 부르지 않는 것들

`header_ali.vue` 가 실제로 부르는 API는 `isLogin()`·`logout()` 둘뿐이다. 그래서:

| 엔드포인트 | 래퍼 | 살아있는 호출처 | 판정 |
|---|---|---|---|
| `/api/common/visit` (접속통계) | `saveVisitData` | `m_tabbar_g.vue`(모바일 탭바), `header_news.vue`(카탈로그) | **모바일·카탈로그 한정** |
| `/api/common/userStatusInfo` | `userStatusInfo` | `header_news.vue` 뿐 | 카탈로그 2화면 한정 |
| `/api/common/cart-info` | `getCartInfo` | `cart/index`, `category/index`, `items/details-main`, `event/seasonal`, `item_tab-ali`, `m_tabbar_g`, `header_news`, `goods.item.js` | live (단, **주 헤더 장바구니 뱃지는 호출 안 함**) |
| `/api/common/quick-info` | `getQuickInfo` | `category/index`, `item_tab-ali`, `items/details-main`, `event/seasonal` | live |
| `/api/common/about-us`, `/account-numbers` | `getAbout`, `getAccountNumbers` | `footer.vue`(2화면 전용 푸터)만 | 사실상 사장 |
| `/api/common/island-info` | `getIslandType` | `item_tab-ali.vue` | live (도서산간 배송비) |
| `/api/common/confirm-pbanc` | `confirmPbanc` | `main.html`, `users/login.html` | live |
| `/api/common/getGiveState`, `/userInfo` | `healthcheck` | `users/login.html` | live |
| `/api/common/alternate`, `/bank-info`, `/getRealTimeWaitUser`, `/seo` | `getAlternate` 외 | **없음** | **사장 4건** |
| `/api/common/mainBanner/{type}/{bannerId}` | — | 래퍼 없음 | **사장** |

`/api/log/action`(`saveUserActionLog`)도 사장 헤더 4종에서만 호출 → **사장**.
`/api/event-log/{item,order,featured,join-user}` 4종 전부 호출부 없음 → **사장**.

---

## 9. 범용 게시판 프레임워크 (`com.onlinepowers.board`) — 통째로 미사용

| 항목 | 수량 |
|---|---|
| `BoardController` (`/board/*`, `/m/board/*`, `/opmanager/board/*`) | 17 엔드포인트 |
| `BoardCfgController` (`/opmanager/board-cfg`) | 7 엔드포인트 |
| `board-mapper.xml` | 41 쿼리 |
| `comment-mapper.xml` | 6 쿼리 |
| `DemoController` (`/demo`) + `demo-mapper` | 5 엔드포인트 (샘플코드) |

살아있는 프론트에서 `/board/`(=`data-board` 제외) 로 가는 링크가 **0건**이다. 고향사랑e음의
공지·FAQ·Q&A·자료실은 전부 전용 컨트롤러(`saleson/api/*`)를 쓰고 이 범용 게시판을 쓰지 않는다.
`board-mapper` 41개가 소스상 "호출됨"으로 나오는 것은 `BoardController`/`BoardServiceImpl`
자기들끼리 부르기 때문 — 진입점이 없으므로 **연쇄 사장**.

→ **판정: 벤더 프레임워크 기본 탑재분. 이관 대상 아님(70 엔드포인트/47 쿼리 제외).**

### 9-1. 판정 보류 — 벤더 jar 내부 호출

`com.onlinepowers.framework.*` 네임스페이스 매퍼 5종(`framework-code` 5, `framework-file` 13,
`framework-message` 2, `framework-sequence` 9, `framework-token` 4 = 33)과
`menu-mapper.xml`(19, `com.onlinepowers.framework.web.opmanager.menu.MenuMapper`)은
**인터페이스가 소스에 없다** — `libs/opframework-3.15.0.jar` 안에 있다.
소스 grep으로 "호출 0"이 나와도 **사장으로 판정할 수 없다** → 52쿼리 판정보류.

---

## 10. 배치 (`ghlove-batch`) — 78잡 vs MSA 3잡

AS-IS는 `@Scheduled` 하드코딩이 아니라 **DB 테이블(`OP_BATCH_JOB`) 기반 Quartz 동적 스케줄러**다
(`SchedulerServiceImpl.invokeJobChecker()` 가 `batchJobMapper.getBatchJobListByParam()` 으로 잡을
읽어 `MethodInvokingJobDetailFactoryBean` 으로 `JobService`/`JobPsintService` 의 메서드명을 호출).
수동 실행은 `GET /batch/run/{jobName}` 1개.

잡 78개 분류:

| 분류 | 개수 | 예시 | MSA |
|---|---|---|---|
| 통계(BIX5 연계) | 14 | `bix5CntrAmt`, `bix5GiftPoint`, `bix5PointYear` | 없음 |
| 통계(리포트) | 6 | `statisticsReportBatch`, `statisticsYearReport*Batch` | admin `StatsController` 화면만 |
| 주문/배송 자동화 | 7 | `autoConfirmPurchaseBatch`, `autoShippingCompleteBatch`, `cancelWaitingDepositOrderBatch` | **미구현** (order 3-1 자동 구매확정 보류항목과 동일) |
| 포인트 소멸/알림 | 6 | `expirationPointBatch`, `expirationCntrPointBatch` | point `PointService` `@Scheduled` — **대응** |
| 쿠폰 | 2 | `couponRegularBatch`, `expirationCouponSendMessageBatch` | order `CouponBatchScheduler` — **대응** |
| 국세청 전자기부금영수증 | 4 | `sendNtsEreceiptOnBatch`, `sendNtsEreceiptOffBatch` | admin `NtsReceiptLogAdminController` 화면만 |
| NH·누리2 외부연계 | 5 | `locForNhBatch`, `userCntrForNhBatch`, `sendNuri2PresentOrder` | admin `NhExportBatchController` 일부 |
| 수납(sunap) | 5 | `cntrSunapBatch`, `fiveMinuteSunapCheckBatch` | 없음 |
| 회원 휴면/잠금 | 5 | `processInactiveUserBatch`, `updateSleepManager`, `updateLockForManagerBatch` | admin `SleepUserAdminController` 화면만 |
| 메시지 발송 | 6 | `sendCampaignMessageBatch`, `updateKakaoAlimTalkBatch` | 보류(사용자 지시: 알림톡 추후) |
| 답례품 랭킹/노출 | 6 | `itemRankingType1~3Batch`, `itemDisplayContorlBatch` | admin `RankingAdminController` 화면만 |
| 정리성 삭제 | 5 | `deleteCartBatch`, `deleteCntrDataBatch`, `deleteOrderTempInfoBatch` | 없음 |
| 지정기부 상태 | 1 | `dsgnBizStatusUpdateBatch` | donation `DesignatedAdminService` `@Scheduled` — **대응** |
| 기타 | 6 | `autoCompleteKeywordBatch`, `userLevelBatch`, `opUserBirthdayStatBatch` | 없음 |

MSA `admin.op_batch_job` 은 현재 **1행(테스트)** 뿐이라 AS-IS 운영 활성 잡을 DB로 특정할 수
없다. `BatchJobController` 주석대로 MSA에는 동적 스케줄러 엔진이 없고 각 서비스 `@Scheduled` 3개
(donation 지정기부 마감, point 포인트 소멸, order 쿠폰)만 있다.

→ **운영데이터(`OP_BATCH_JOB`) 이관 후 활성 잡을 확정해야 우선순위를 매길 수 있다.**

---

## 11. 조치대상 정리

| # | 항목 | 근거 | 심각도 |
|---|---|---|---|
| 1 | 개인정보처리방침 "이전 버전 보기" 링크 16종 전부 404 | `policy-content/privacy.html` 17곳 링크, MSA에 파일·라우트 없음 | 높음(법정 고지) |
| 2 | 팝업 공개 노출 경로 없음 (admin CRUD만 존재) | AS-IS `main.html` `focusPopup`·`getJoinComfirmPop` | 중 | ✅ 조치완료 (§15) |
| 3 | 통합검색 미구현 (메인 검색창이 답례품 검색으로 축소) | `main.html:141,535` live | 중 |
| 4 | 온라인 설문 공개 참여 화면 없음 (운영자 등록만 가능) | `qustnr/detail.html`, `qustnr-mapper` 21 live | 중 | ✅ 조치완료 (§16) |
| 5 | GNB 마이페이지에 `답례품 Q&A`·`답례품 후기` 누락 (화면은 있음) | `header_ali.vue` 14항목 vs `AppHeader.vue` | 낮음 |
| 6 | 플로팅 버튼 소식지·카탈로그 2종 누락 | `fixedBtn.vue` 6종 | 낮음 |
| 7 | 전용 오류화면(404) 없음 | `error/404.html` | 낮음 |
| 8 | `GET /api/notice/donation/list` (기부화면 전용 공지) 미구현 | `donation/list-select.html` live | 낮음 |
| 9 | 접속통계(`/api/common/visit`) 미구현 | `m_tabbar_g`·`header_news` live | 낮음 |

## 12. 재현 불필요 (AS-IS 사장 — 목록만 유지)

- 범용 게시판 프레임워크 24 엔드포인트 / 47 쿼리 + `DemoController` 5 (§9)
- 메인화면 미호출 섹션 6종 (§5)
- 헤더 통합검색·자동완성·최근검색어 (§4)
- `customer-lnb_ali`·`donation-lnb_ali`·`subsearch_ali` 등 레이아웃 27종 (§1)
- `/api/common` 사장 6종, `/api/log/action`, `/api/event-log` 4종, `/api/reload-cache` (§8-1)
- 매퍼 사장 쿼리 10건: notice 1 · qna 3 · keyword 1 · main 4 · banword 1
- 개인정보처리방침 작업본 `privacy-form20231026_temp.html`
- `/api/data-board/dash-board`, `/api/qna/delete-inquiry`, `/api/help`·`/api/help/detail`, `/api/policy/marketing`

## 13. 판정 보류

- `com.onlinepowers.framework.*` 매퍼 52쿼리 — 인터페이스가 `opframework-3.15.0.jar` 내부 (§9-1)
- 배치 78잡의 운영 활성 여부 — `OP_BATCH_JOB` 운영데이터 필요 (§10)
- 소식지 77건·카탈로그 2건의 이관 방식 — 정적 아카이브를 어디에 둘지 (§6)

---

## 14. 조치 결과 — 개인정보처리방침 이력본 링크 (2026-09-10)

**문제:** 개인정보처리방침 본문(`policy-content/privacy.html`)이 "이전 버전 보기"로
과거 개정본 16종을 링크하는데, MSA에 그 파일도 라우트도 없어 **전부 404**였다. 법정 고지
문서라 우선순위 1순위. 링크 형태는 상대경로 9 + 운영도메인 절대URL 7 혼재였다.

**성격 판정:** 아카이브 16종은 자산 의존성 없는 self-contained 정적 HTML(개정본 원문 +
신·구 대조표)이고 **불변 스냅샷**이다. 편집 대상인 현행 원문(OP_POLICY, DB 적재 보류)과
성격이 달라, DB 적재를 기다리지 않고 **정적 파일로 이관**하는 것이 맞다.

**구현:**
- AS-IS `ghlove-frontend/policy/privacy-form*.html` 16종을
  `donation/src/main/resources/static/policy/` 로 복사 (3.5MB) → `/policy/privacy-form*.html` 로 서빙.
  PolicyController의 `/policy/{privacy,copyright,auth}` 는 정확매핑이라 충돌 없음.
- 복사 과정에서 파일 내부의 운영도메인 절대URL(`https://ilovegohyang.go.kr/policy/`)을
  상대경로(`/policy/`)로 치환 — [[production-domain-migration-plan]]의 절대URL 상대경로화 방침과 동일.
  아카이브끼리의 상호 링크도 이로써 MSA 안에 머문다.
- 본문 `policy-content/privacy.html` 의 절대URL 7곳도 동일하게 상대경로화 → 링크 16개 전부 상대경로.

**검증 (donation 재기동, 실서비스):**
| 확인 | 결과 |
|---|---|
| 본문 렌더 시 링크 상대경로 | `/policy/privacy-form*.html` 로 출력 |
| 이력본 16종 HTTP | **16/16 = 200** (이전 전부 404) |
| 이력본 콘텐츠 렌더 | "행정안전부" 원문 정상 |
| 이력본 내부 상호링크 절대URL 잔존 | 0 (전부 상대경로) |
| 상호링크 실제 도달(20260604→20250630) | 200 |
| 신·구 대조표(compare) 렌더 | `<title>개인정보 처리방침 신·구 대조표</title>` |

`privacy-form20231026_temp.html`(작업본)은 참조 0이라 이관에서 제외.

---

## 15. 조치 결과 — 팝업 공개 노출 (2026-09-10)

**문제:** admin 운영 콘솔은 팝업 등록·수정·삭제·노출토글(`/popups` 7종)이 다 있는데,
**이용자 화면에 실제로 띄우는 경로가 없었다** — 공개 조회 API도, storefront 렌더링
컴포넌트도 없어 등록해도 뜨지 않았다. AS-IS는 메인 진입 시 `popup-layer.vue`가
`GET /api/popup/list`로 노출 팝업을 받아 레이어로 띄운다.

**AS-IS 노출 규칙 (`popup-mapper.xml` displayPopupList):** `CONCAT(START_DATE,START_TIME)
~ CONCAT(END_DATE,END_TIME)` (단위 yyyyMMddHH) 사이. AS-IS는 그 쿼리에서 POPUP_CLOSE=1도
걸지만, MSA는 POPUP_CLOSE를 "닫기버튼 노출여부"로 쓰므로 노출 판정은 운영자 토글값 useYn으로 한다.

**구현:**
- admin `Popup` 엔티티에 미매핑 컬럼 보강 — `START_TIME`·`END_TIME`·`WIDTH`·`HEIGHT`·
  `TOP_POSITION`·`LEFT_POSITION`·`BACKGROUND_COLOR` (테이블엔 이미 있던 컬럼)
- admin `OperationContentService.displayPopups()` — useYn='Y' + 노출기간(yyyyMMddHH 문자열 비교)
  인메모리 필터. `displayBound()`가 yyyyMMdd + HH(기본 00)를 만든다.
- admin `PopupApiController` (신규) — 공개 `GET /api/popups`. NoticeApiController와 같은 패턴.
- storefront `SitePopups.vue` (신규) — `api.get('admin','/api/popups')` 조회 후 레이어 렌더.
  "오늘 하루 이 창을 열지 않음"은 AS-IS 쿠키(`popup_check_{id}`)를 localStorage
  (`popup_dismiss_{id}=YYYYMMDD`)로 옮겨 재현. 이미지(popupStyle '3'/이미지등록, imageLink 클릭이동)·
  텍스트 팝업 모두 지원. 위치·크기값이 있으면 절대배치, 없으면 화면 중앙 기본 크기.
  조회 실패 시 조용히 넘어가 메인이 깨지지 않게 함(AS-IS와 동일).
- storefront `HomeView.vue`에 `<SitePopups />` 마운트 (AS-IS도 메인에서 노출).

**검증 (admin 재기동 + storefront dev 프록시, 데이터 원복 완료):**
| 확인 | 결과 |
|---|---|
| 데이터 없을 때 `/api/popups` | `[]` |
| 활성/기간밖/비활성 3건 중 | **활성 1건만** 반환 |
| 경계: end_time=현재시각-1 | 제외 (막 만료) |
| 경계: end_time=현재시각 | 포함 |
| SPA 프록시 경로 `/admin/api/popups` | 활성 팝업 JSON 반환 |
| storefront 빌드 | SitePopups 번들 포함, 빌드 성공 |

**남은 것(조치 아님):** admin 팝업 등록 폼은 위치·크기·시각 입력을 아직 안 받는다(날짜·타입·
스타일·내용/이미지만). 팝업이 뜨기엔 충분하고, 위치·크기는 렌더러가 기본값으로 처리한다.
필요 시 admin 폼 확장은 별도 폴리싱 과제.

---

## 16. 조치 결과 — 온라인 설문 공개 참여 (2026-09-10)

**문제:** admin `QustnrAdminController`(`/admin/surveys`)로 운영자가 설문을 만들고 결과(총응답수)를
집계할 수 있는데, **이용자가 참여할 화면·경로가 없었다**. AS-IS는 `qustnr/detail.html`이
`GET/POST /api/qustnr/{qustnrSn}`로 조회·응답한다.

**AS-IS 판정 이식 (`getQustnrByApi`):** 로그인 필수, 노출중(isShow=Y)+노출기간(bgnDe~endDe) 내,
**1인 1회**(regCnt>0 → ALREADY_DONE). MSA 설문은 객관식 보기(AS-IS QustnrIem)를 자유서술형
문항으로 단순화한 모델이라(QustnrAdminController 주석) 응답도 문항별 텍스트다.

**구현:**
- admin `QustnrRepository.findByIsShowOrderByQustnrSnDesc` + `QustnrRspnsRepository.existsByQustnrSnAndUserId`
- admin `SurveyApiController`(신규, 공개) — `GET /api/surveys/active`(진행중 최신, 없으면 204),
  `GET /api/surveys/{id}`(팝업·배너 링크 진입), `POST /api/surveys/{id}/responses`.
  응답자 식별은 QnaApiController와 같은 `JwtVerifier`(GH_AUTH 쿠키). 노출기간·1인1회·로그인 검증.
- storefront `SurveyView.vue`(신규) + 라우트 `/survey`(진행중)·`/survey/:id`. 비로그인은 안내 후
  제출 시 로그인 이동, 이미 참여/기간종료/없음 각각 안내. 문항별 textarea 응답.

**검증 (admin 재기동 + SPA 프록시, 데이터 원복 완료):**
| 확인 | 결과 |
|---|---|
| 진행중 설문 없을 때 `/api/surveys/active` | 204 |
| 활성/만료 2건 중 active | **활성만** 반환, 만료 제외 |
| 비로그인 제출 | 401 |
| 로그인(GH_AUTH) 제출 | 204, 문항 2건 저장(user_id 기록) |
| 재제출 | 409 "이미 참여한 설문입니다" |
| alreadyResponded 플래그 | 제출 후 true |
| 만료 설문 제출 | 400 "참여할 수 없는 설문입니다" |
| admin 결과화면 총응답수(countByQustnrSn) | 반영 |
| SPA 프록시 `/admin/api/surveys/active` | 정상 |

**진입 경로:** AS-IS도 설문은 상시 메뉴가 아니라 팝업·배너 캠페인으로 노출됐다(popupId 185가
설문 팝업이었던 흔적). MSA도 `/survey`(진행중)·`/survey/{id}` 라우트를 두고, 운영 시 §15의 팝업
`imageLink`를 `/survey/{id}`로 걸어 노출하는 방식이다.

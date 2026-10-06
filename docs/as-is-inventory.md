# AS-IS 소스 인벤토리 (7영역)

> 2026-10-06 통합. AS-IS 전수 인벤토리 7개 파일을 합친 것이다(절차는 메모리 `as-is-inventory-procedure`).
> 표 형태의 원본은 `docs/inventory/*.tsv`와 `docs/inventory/as-is-inventory.xlsx`에 있고, 이 문서는 그 판정 근거·주석이다.
> 6개 MSA 서비스에 안 걸리는 영역은 `common`으로 모은다.

## 목차

- [1. common (6개 서비스에 안 걸리는 영역)](#1-common-6개-서비스에-안-걸리는-영역) — `as-is-inventory-common.md`
- [2. admin](#2-admin) — `as-is-inventory-admin.md`
- [3. member](#3-member) — `as-is-inventory-member.md`
- [4. donation](#4-donation) — `as-is-inventory-donation.md`
- [5. gift](#5-gift) — `as-is-inventory-gift.md`
- [6. order](#6-order) — `as-is-inventory-order.md`
- [7. point](#7-point) — `as-is-inventory-point.md`

---

## 1. common (6개 서비스에 안 걸리는 영역)

> 통합 전 파일: `docs/as-is-inventory-common.md`

## AS-IS 코드 인벤토리 — common (6개 도메인 밖 전 영역)

> 7번째 시트. 배정 근거는 [[as-is-coverage-map]], 판정 절차는 [[as-is-inventory-procedure]].
> 작성 2026-09-10. 대상: GNB/LNB·레이아웃, 게시판류, 안내/정책, 통합검색, 메인화면,
> 소식지, 공통 인프라, 범용 게시판 프레임워크, 배치.

### 0. 규모

| 축 | 수량 |
|---|---|
| 컨트롤러 | 32개 (api 19 + 공통인프라/웹 13) |
| 엔드포인트 | 171개 (api 90 + 인프라 81) |
| 매퍼 쿼리 | 390개 (35개 매퍼 XML) |
| 살아있는 화면 | notice 2 · faq 1 · qna 4 · data-board 2 · qustnr 2 · event 8 · totalsearch 2 · policy 21 · guide 1 · error 1 · popup 1 · newsletter 77 · catalog 2 · 루트 5 |
| 레이아웃 컴포넌트 | 41개 파일 중 **live 14 / 사장 27** |
| 배치 잡 | 78개 (JobService 76 + JobPsintService 2) |

---

### 1. 레이아웃/내비게이션

#### 1-1. 살아있는 레이아웃 컴포넌트 판별 (선행 작업)

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

#### 1-2. 사장 등록 — `subsearch_ali.vue` (없는 파일을 6화면이 등록)

`donation-main.html:669`, `guide2.html:2587`, `list-select.html:440`, `map-select.html:1323`,
`faq/list.html:199`, `users/modify.html:606` 6곳이 주석 없이
`httpVueLoader('/components/layouts/subsearch_ali.vue')` 를 등록하는데 **그 파일은 존재하지 않는다**
(`subsearch_ali_unUsed.vue` 로 개명됨). 6화면 모두 템플릿에 `<layout-subsearch-ali>` 를 놓지
않아 httpVueLoader가 지연 로딩만 하고 실제 404는 나지 않는다 → **무해하지만 완전한 사장 등록**.
mypage 5화면(`inquiryItem`·`orderCancel`·`orderDetail`·`orderList`·`review`)은 같은 줄이 주석 처리돼 있다.

#### 1-3. GNB — AS-IS 6 대메뉴 / 23 소메뉴 vs MSA

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

#### 1-4. GNB 마이페이지 항목 차이

- **AS-IS에 있고 MSA GNB에 없음(2)**: `답례품 Q&A`, `답례품 후기`
  → 화면·라우트는 이미 있다(`/mypage/gift-qna`, `/mypage/gift-reviews`). 푸터 사이트맵
  (`AppFooter.vue:107~108`)과 마이페이지 홈(`MyPageView.vue:115~119`)에도 있다.
  **GNB 드롭다운에만 빠졌다** → 조치대상.
- **MSA에만 있음(4)**: `회원정보수정`, `비밀번호 변경`(AS-IS는 마이페이지 LNB에 존재),
  `지자체담당자·제공자 역할 신청`, `역할신청 승인함`(RFP 신규) → 근거 있음, 유지.

#### 1-5. 푸터 FNB — 1:1 일치

AS-IS `footer_ali.vue:160~179` 5항목(개인정보처리방침·저작권정책·이용약관·사이트맵·공지사항)이
MSA `AppFooter.vue:25~48` 과 순서까지 동일. 사이트맵도 AS-IS와 같은 CSS `:target` 모달 방식.

#### 1-6. 플로팅 버튼 — 6종 중 4종만 구현

AS-IS `components/ui/fixedBtn.vue` (live 등록 67곳) 기능: **소식지, 챗봇상담, 카탈로그,
카카오채널, TOP, 접기/열기**.
MSA `FloatingButtons.vue`: 카카오채널, 챗봇상담, TOP, 접기/열기.
→ **소식지·카탈로그 버튼 2종 미구현** (§6·§7).

#### 1-7. 고객센터 LNB — AS-IS는 사장, MSA는 구현

`customer-lnb_ali.vue` 는 12개 화면 전부에서 주석 처리돼 AS-IS에서 렌더되지 않는다.
MSA는 `components/CustomerLnb.vue` 를 실제로 노출한다 → **AS-IS에 없는 것을 추가**.
[[scope-migration-not-greenfield]] 기준으로는 과잉이지만, 사장 사유가 "메뉴 축소"가 아니라
방치로 보이고 사용성 손해가 없어 **현행 유지 + 기록**으로 둔다.

---

### 2. 게시판류 (공지·FAQ·Q&A·자료실)

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

#### 2-1. 게시판 매퍼 죽은 쿼리

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

### 3. 안내/정책 화면

#### 3-1. 약관 3종 — 대응

`/api/policy/{protect,clause,copyright,marketing}` → MSA `GET /api/policies/{name}` +
donation `GET /policy/{privacy,copyright,auth}`. 편집 주체는 admin 약관관리(`OP_POLICY`).
`getPolicyMarketing`(마케팅 활용동의)은 **살아있는 호출부 없음 → 사장**.

#### 3-2. 개인정보처리방침 이력본 18종 — **본문 링크가 전부 깨짐** → ✅ 조치완료 (§14)

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

#### 3-3. 사이트맵 · 오류화면 · 팝업

| AS-IS | MSA | 판정 |
|---|---|---|
| `guide/sitemap.html` + `sitemapModal.vue` | `AppFooter.vue` 내 `#modal_sitemap` (CSS `:target`) | 대응 |
| `error/404.html` (op.saleson.js:406,427 이 `location.replace`) | 전용 오류화면 없음 | **미구현** |
| `GET /api/popup/list`, `/api/popup/index/{id}` (`popup-layer.vue`, `popup-layer-kakao.vue`, `main.html`, `event/seasonal.html`) | admin CRUD(`/popups` 7종) + **공개 API `/api/popups` + storefront `SitePopups.vue`** | ✅ 조치완료 (§15) |

팝업은 admin에서 등록·수정·삭제·토글까지 다 되는데 **사용자 화면에 띄우는 경로가 없었다**
— 공개 조회 API도, storefront 팝업 레이어 컴포넌트도 없었다. AS-IS 메인화면은 `focusPopup()`·
`getJoinComfirmPop()` 으로 실제 노출한다(§5). §15에서 해소.

---

### 4. 통합검색 — AS-IS에서 이미 진입점이 거의 죽어 있다

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

### 5. 메인화면 — 정의된 섹션 6개가 호출되지 않는다

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

### 6. 소식지(newsletter) 77건 — 정적 아카이브

`newsletter/` 77개 html은 발행분 정적 페이지(vol001~)이고 애플리케이션 기능이 아니다.
서버 컨트롤러도 매퍼도 없다. 살아있는 진입점은 **플로팅 버튼 `fixedBtn.vue:75`**
(`$s.redirect("/newsletter/main.html")`) 하나 — 사장 헤더 `header_ali_as-is.vue:889` 에도 있지만 무효.

→ **판정: 콘텐츠(정적 자산). 코드 이관 대상 아님.** 다만 §1-6대로 MSA 플로팅 버튼에
소식지 진입 버튼이 없으므로, 아카이브를 어디에 둘지와 함께 결정이 필요하다(운영데이터 이관 이슈).

`catalog/catalog-main.html`·`catalog-faq.html` (전자 카탈로그, `header_news.vue` 전용 헤더 사용)도
같은 성격이며 진입은 `fixedBtn.vue` 의 `catalogConfirm()` — MSA 미구현.

---

### 7. 온라인 설문(qustnr) — 공개 참여 경로 → ✅ 조치완료 (§16)

| AS-IS | MSA |
|---|---|
| `GET /api/qustnr{qustnrSn}` — 설문 조회 (`qustnr/detail.html`, `detail_srvy.html` 이 직접 호출, 래퍼 없음) | admin `GET /api/surveys/{id}`·`/active` + storefront `SurveyView.vue` |
| `POST /api/qustnr{qustnrSn}` — 응답 등록 | admin `POST /api/surveys/{id}/responses` |
| `QustnrManagerController` (운영자 설문관리) | `QustnrAdminController` `/admin/surveys` 7종 — **대응** |

`qustnr-mapper.xml` 21개 쿼리 전부 살아 있다. **운영자는 설문을 만들 수 있으나 사용자가
참여할 화면이 없었다** → §16에서 공개 참여 경로를 붙여 해소.

---

### 8. 공통 인프라 (`ghlove-common`)

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

#### 8-1. `/api/common` 19종 — 주 헤더가 부르지 않는 것들

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

### 9. 범용 게시판 프레임워크 (`com.onlinepowers.board`) — 통째로 미사용

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

#### 9-1. 판정 보류 — 벤더 jar 내부 호출

`com.onlinepowers.framework.*` 네임스페이스 매퍼 5종(`framework-code` 5, `framework-file` 13,
`framework-message` 2, `framework-sequence` 9, `framework-token` 4 = 33)과
`menu-mapper.xml`(19, `com.onlinepowers.framework.web.opmanager.menu.MenuMapper`)은
**인터페이스가 소스에 없다** — `libs/opframework-3.15.0.jar` 안에 있다.
소스 grep으로 "호출 0"이 나와도 **사장으로 판정할 수 없다** → 52쿼리 판정보류.

---

### 10. 배치 (`ghlove-batch`) — 78잡 vs MSA 3잡

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

### 11. 조치대상 정리

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

### 12. 재현 불필요 (AS-IS 사장 — 목록만 유지)

- 범용 게시판 프레임워크 24 엔드포인트 / 47 쿼리 + `DemoController` 5 (§9)
- 메인화면 미호출 섹션 6종 (§5)
- 헤더 통합검색·자동완성·최근검색어 (§4)
- `customer-lnb_ali`·`donation-lnb_ali`·`subsearch_ali` 등 레이아웃 27종 (§1)
- `/api/common` 사장 6종, `/api/log/action`, `/api/event-log` 4종, `/api/reload-cache` (§8-1)
- 매퍼 사장 쿼리 10건: notice 1 · qna 3 · keyword 1 · main 4 · banword 1
- 개인정보처리방침 작업본 `privacy-form20231026_temp.html`
- `/api/data-board/dash-board`, `/api/qna/delete-inquiry`, `/api/help`·`/api/help/detail`, `/api/policy/marketing`

### 13. 판정 보류

- `com.onlinepowers.framework.*` 매퍼 52쿼리 — 인터페이스가 `opframework-3.15.0.jar` 내부 (§9-1)
- 배치 78잡의 운영 활성 여부 — `OP_BATCH_JOB` 운영데이터 필요 (§10)
- 소식지 77건·카탈로그 2건의 이관 방식 — 정적 아카이브를 어디에 둘지 (§6)

---

### 14. 조치 결과 — 개인정보처리방침 이력본 링크 (2026-09-10)

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

### 15. 조치 결과 — 팝업 공개 노출 (2026-09-10)

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

### 16. 조치 결과 — 온라인 설문 공개 참여 (2026-09-10)

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

---

## 2. admin

> 통합 전 파일: `docs/as-is-inventory-admin.md`

## AS-IS 코드 인벤토리 — admin (운영관리 + 판매자 포털)

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`** / `보류`
> 작성 2026-09-10. 6개 도메인 중 마지막. 앞선 5개 도메인이 운영관리 컨트롤러를 전부 이 시트로 이월했다.

### 0. 규모

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

### 1. 원제품 쇼핑몰·모바일 컨트롤러 61개 / 440 엔드포인트 — 통째로 사장

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

### 2. 운영관리 기능 그룹 77개 — MSA 대조

AS-IS 기준은 `WEB-INF/views/opmanager/i18n/<group>/` 531 JSP.
MSA 기준은 `admin/src/main/resources/templates/<dir>/` 221 HTML + `admin.op_menu` 121행.

#### 2-1. 대응 있음 (주요)

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

#### 2-2. 권한체계 — 대응

AS-IS `role-mapper`(11, 벤더 jar), `userrole-mapper`(2), `OP_MENU_RIGHT`, `menu-manager-mapper`(14).
MSA `RoleAdminController` 8종 — 역할 CRUD + **`/{authority}/matrix` 권한 매트릭스**(GET/POST).
`admin.op_role`, `admin.op_menu_right`(36행), `admin.op_menu`(121행) 실데이터 존재. → 대응있음.

#### 2-3. MSA 미구현 — 14개 그룹

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

### 3. 판매자 포털 — 18 그룹 / 129 JSP vs MSA 3화면

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

### 4. 매퍼 쿼리 (1,030 검사 / 사장 33 / 보류 25)

#### 4-1. 사장 쿼리 33건

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

#### 4-2. 판정 보류 25건 — 벤더 jar 내부

`role-mapper`(11, `com.onlinepowers.framework.web.opmanager.role.RoleMapper`)와
`security-mapper`(14, `com.onlinepowers.framework.security.mapper.SecurityMapper`)는
인터페이스가 `libs/opframework-3.15.0.jar` 안에 있어 소스 grep으로 사장 판정이 불가능하다.
common §9-1의 52쿼리와 같은 사유.

#### 4-3. 전량 live인 대형 매퍼

`cmnty`(96, 사장 3) · `remittance`(63, 사장 1) · `designated-donation`(60, 사장 1) ·
`categories`(52, 사장 1) · `give-state`(47, 사장 4) · `statistics`(36, 사장 1) ·
`bix5`(35, 사장 0) · `display`(30) · `personincharge`(27) · `generalcustomer`(27) ·
`offgive`(22) · `temp-process`(21) — 운영관리는 본체가 대부분 살아 있다.

---

### 5. MSA 전용 (AS-IS 대응 없음)

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

### 6. 조치 후보

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

### 7. 재현 불필요 (AS-IS 사장)

- **원제품 PC 쇼핑몰 42 컨트롤러 / 259 엔드포인트 + 모바일 19 / 181 = 61 / 440** (§1)
- `board-cfg`(2 JSP) — common §9 범용 게시판의 설정화면
- 매퍼 사장 33쿼리 (§4-1). 특히 **접속·방문 통계 6쿼리**는 수집단까지 함께 죽어 있다
- `seller-menu` 동적 메뉴 3쿼리
- 브랜드-카테고리 연결 CRUD 5쿼리

### 8. 판정 보류

- `role-mapper`·`security-mapper` 25쿼리 — 벤더 jar 내부 (§4-2)
- 판매자 포털 분리 여부 — [[provider-portal-split-deferred]]
- `magicline`(12) · `temp-process`(5) 부분 구현의 완결 범위

---

### 9. 조치 결과 (2026-09-10)

#### 9-1. 제철식품관 운영관리 (조치후보 1)

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

### 10. 조치 결과 — 택배사 마스터 + 반품지 관리 (2026-09-11)

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

### 11. 근거 확인 — 지자체 공지 (조치후보 5, 2026-09-11)

**판정: 대응있음.** AS-IS `LocgovNoticeManagerController`(별도 컨트롤러, list/create/edit
3 JSP)는 MSA에서 별도 화면을 두지 않고 통합 공지관리(`OperationContentController`
`/admin/notices`)에 **흡수**됐다. 코드 변경 없음 — 흡수 사실을 확인하고 판정만 확정한다.

#### 근거 대조

| AS-IS `locgov-notice` | MSA 대응 |
|---|---|
| `list`: `adminRole=="LOC"`면 `noticeParam.setLocgovCode(소속)`으로 필터 | `/admin/notices` 목록: `MenuService.isLocgovScoped(viewer)`면 `viewer.getLocgovCode()`로 필터 |
| `createAction`: `notice.setLocgovCode(locgovCodeDetails.getId())` (LOC는 소속 강제) | `POST /admin/notices`: `MenuService.effectiveLocgovCode(viewer, locgovCode)` — LOC 담당자는 화면에서 고른 값 무시하고 소속 강제, 시스템/행안부는 고른 값(null=전체) |
| `edit`/`delete`: 소속 지자체 공지만 | `requireOwnLocgovOrThrow(id, session)` 소유 가드 |
| OP_NOTICE의 `LOCGOV_CODE` 컬럼으로 전역/지자체 구분 | `Notice.locgovCode` (전역=NULL) 동일 |
| 이용자 노출 = 기금사업소개 "지자체공지사항" 탭 | donation `FundProjectController`/`FundProjectApiController` → `NoticeClient.noticesByLocgov(locgovCode)` → admin `GET /api/notices?locgovCode=` (읽기전용, 실패 시 빈 목록) |

`isLocgovScoped`는 `LOCGOV_SCOPED_ROLES`로 판정 — AS-IS "role 5,6이면 소속 지자체 스코프"
분기 재현.

#### 재현 불필요로 정리한 AS-IS 잔재

AS-IS `locgov-notice` 컨트롤러의 공지-판매자 연결(`getNoticeSellerList`,
`delete-notice-seller`, create/edit의 주석 처리된 `sellerList`)은 일반
`NoticeManagerController`에서 복사돼 온 잔재다. `locgov-notice/list.jsp`에 판매자
지정 UI가 없어 지자체 공지의 실제 기능이 아니며, 재현 대상에서 제외한다.

---

## 3. member

> 통합 전 파일: `docs/as-is-inventory-member.md`

## AS-IS 인벤토리 — member 도메인

> 기준: [[as-is-logic-is-the-spec]] — AS-IS 서비스로직이 정본, RFP(`docs/requirements.md`)·ISP(`docs/requirements.md` 후반부 ISP 요약)는 신규/변경 건에만 적용.
> 판정: `대응있음` / `재현누락` / `의도적축소` / **`죽은코드`**(AS-IS에서 이미 도달불가·미호출)
> 작성 2026-09-10

### 1. 화면 생사 판정 (users/, mypage/ 회원분)

살아있는 화면은 inbound 참조가 있는 것만 인정했다. `_bak`/`-backup`/`_YYYYMMDD`/`old/`는 라우팅상 도달 경로가 없다.

| AS-IS 화면 | 판정 | 근거 |
|---|---|---|
| `users/login.html` | live | 진입 참조 110 |
| `users/join.html` | live | 진입 참조 155 |
| `users/modify.html` | live | 진입 참조 40 |
| `users/secede.html` | live | 진입 참조 10 |
| `users/find-idpw.html` | live | 진입 참조 14 |
| `users/secede-kakao.html` | live | `modify.html:1429`, `secede.html:287,291` |
| `users/onepass_secede.html` | live | `modify.html:983` |
| `users/onepass-join.html` | live | `onepass-result.html:113,149` |
| `users/onepass-result.html` | live | 서버 리다이렉트 착지 (`AuthController:1663~1717`) |
| `users/mobile-auth-result.html` | live | 서버 리다이렉트 착지 (`AuthController` mobile-auth-callback) |
| `users/jusoPopup.html` | live | join/modify/onepass-join 팝업 |
| `users/sns/naver-callback.html` | live | `op.saleson.js:25 naverLoginCallback` |
| **`users/sign-certificate.html`** | **죽은코드(도달불가)** | 유일 진입점 `login.html:1271`이 존재하지 않는 `sign-certificate1.html`을 가리킴 — AS-IS 오타 결함 |
| `users/login-backup.html` | 죽은코드 | inbound 0 |
| `users/login_bak.html` | 죽은코드 | inbound 0 |
| `users/login_test.html` | 죽은코드 | inbound 0 |
| `users/login_20241115.html` | 죽은코드 | inbound 0 |
| `users/join_20241115.html` | 죽은코드 | inbound 0 |
| `users/modify-backup.html` | 죽은코드 | inbound 0 |
| `users/old/*.html` (8개) | 죽은코드 | 디렉터리 통째 미참조 |

#### 1-1. 화면 상수가 가리키는 존재하지 않는 경로
`modules/op.saleson.js`의 `$s.pages`:
- `SLEEP_USER: "/users/sleep-user.html"` → **파일 없음**(`users/old/`에만 존재). 리다이렉트 호출부 `op.saleson.js:4504,4743` 전부 주석.
- `CHANGE_PASSWORD: "/users/change-password.html"` → **파일 없음**(동일). 호출부 `4751` 주석.

→ AS-IS에서 **휴면회원 전용 화면·비밀번호변경 전용 화면은 비활성**. MSA `DormancyController` 재현 시 이 사실을 근거로 삼아야 한다(RFP/ISP 신규 요건 여부 별도 확인 필요).

### 2. 컨트롤러 엔드포인트 인벤토리

#### 2-1. `saleson.api.user.JoinController` — `/api/join`
| 엔드포인트 | 메서드 | AS-IS 호출처 | 판정 |
|---|---|---|---|
| POST `/entryForm` | `entryForm` | `join.html:1029,1045` | |
| POST `/getSubLocGov` | `getSubLocGov` | `join.html:1071` | |
| POST `/getUserInfoByUserId` | `getUserInfoByUserId` | `join.html:1122` | |
| POST `/join` | `join` | `join.html:796` | |
| POST `/onepassJoin` | `onepassJoin` | `onepass-join.html` | |
| POST `/getPolicyInfo` | `getPolicyInfo` | join/onepass-join | |
| POST `/checkMobileAuth` | `checkMobileAuth` | `join.html:1297` | |
| POST `/checkOnepassMobileAuth` | `checkOnepassMobileAuth` | `onepass-join.html` | |
| POST `/testRandomId` | `testRandomId` | **없음** | **죽은코드** |

#### 2-2. `saleson.api.auth.AuthController` — `/api/auth`
회원기능 상당수가 `UserController`로 이관된 뒤 **잔존**했다. 아래 `호출처 없음`은 전부 `modules/op.saleson.js`에 래퍼만 남고 화면 호출부가 0인 것.

| 엔드포인트 | 래퍼(op.saleson.js) | AS-IS 호출처 | 판정 |
|---|---|---|---|
| POST `/token` | `getAuthToken` | login/join/modify/find-idpw, `static/js/financ.js` | live |
| POST `/sns-token` | `getAuthSnsToken` | `static/js/sns.js` | live |
| POST `/sns-join` | `snsJoin` | `static/js/sns.js` | live |
| POST `/disconnect-sns` | `disconnectSns` | `static/js/sns.js` | live |
| POST `/check-sns-join` | `checkSnsJoin` | `static/js/sns.js` | live |
| GET `/sns-info` | `getSnsInfo` | `order/step1.html`, `admin/order-agency/step1.html` | live |
| POST `/check-password` | `checkPassword` | secede / secede-kakao / onepass_secede | live |
| GET `/mobile-auth` | `mobileAuth` | join/modify/find-idpw/onepass-join, `auth.vue`, `mobile_auth.vue` | live |
| `/mobile-auth-callback` | (서버) | 본인인증 PG 콜백 | live |
| GET `/onepass-login` | `onepassLogin` | `login.html` | live |
| POST `/onepass-callback` | (서버) | 온패스 콜백 | live |
| POST `/onepass-token` | `onepassAuthToken` | `onepass-result.html` | live |
| POST `/onepass-cancel` | — | `login-backup.html`만 | **죽은코드** |
| POST `/onepass-unlink` | — | 확인필요 | |
| POST `/getAccessInfo` | — | `login-backup.html`만 | **죽은코드 후보** |
| POST `/getSimpleAuthResult` | `getSimpleAuthResult` | `login.html` | live |
| POST `/login-statistics` | — | **없음** | **죽은코드** |
| GET/POST `/me` | `getMember`/`updateMember` | **없음** (실제는 `/api/user/getUserInfo`,`/modifyUser`) | **죽은코드** |
| POST `/secede` | `secedeMember` | **없음** (실제는 `/api/user/secede`) | **죽은코드** |
| POST `/join` | `joinMember` | **없음** (실제는 `/api/join/join`) | **죽은코드** |
| POST `/send-auth-number` | `sendAuthNumber` | **없음** | **죽은코드** |
| POST `/check-auth-number` | `checkAuthNumber` | **없음** | **죽은코드** |
| POST `/find-id` | `findId` | **없음** | **죽은코드** |
| POST `/find-password-step1` | `findPasswordStep1` | **없음** | **죽은코드** |
| POST `/find-password-step2` | `findPasswordStep2` | **없음** | **죽은코드** |
| POST `/change-password` | `changePassword` | **없음** (실제는 `/api/user/changeUserPassword`) | **죽은코드** |
| POST `/delay-change-password` | `delayChangePassword` | **없음** | **죽은코드** |
| POST `/recovery` | `recovery` | **없음** | **죽은코드** |
| GET `/saleson-id` | `salesonId` | **없음** | **죽은코드** |
| POST `/guest-token` | `getAuthGuestToken` | **없음** | **죽은코드** |
| GET `/auth-me` | — | **없음** | **죽은코드** |
| POST `/asis-token` | — | **없음** | **죽은코드** |
| GET `/druh-mig-check` | — | **없음** | **죽은코드** |
| GET `/session-timeout` | `getSessionTimeout` | **없음** | **죽은코드** |
| (주석) `/mobile-auth` 1886, `/mobile-auth-callback` 1947 | — | 소스 주석처리 | **죽은코드** |

#### 2-3. `saleson.api.user.UserController` — `/api/user` (회원 실제 동선)
| 엔드포인트 | AS-IS 호출처 | 판정 |
|---|---|---|
| POST `/getUserInfo` | `modify.html`, `popup-layer-kakao.vue` | live |
| POST `/modifyUser` | `modify.html` | live |
| POST `/modifyReceive` | `popup-layer-kakao.vue` | live |
| POST `/confirmPresentPassword` | `modify.html` | live |
| POST `/changeUserPassword` | login/modify/find-idpw, `financ.js` | live |
| POST `/changeUserPasswordForNoLogin` | login/find-idpw, `financ.js` | live |
| POST `/changeUserPasswordLater` | `login.html` | live |
| POST `/changeUserPwdForSignNoLogin` | login/find-idpw, `financ.js` | live |
| POST `/getSecedeInfo` | secede / secede-kakao / onepass_secede | live |
| POST `/secede` | `secede.html` | live |
| POST `/modifyMobileAuth` | `modify.html` | live |
| POST `/checkMobileAuth` | login/modify/find-idpw, `financ.js` | live |
| POST `/checkMobileAuthPwd` | `find-idpw.html`, `financ.js` | live |
| POST `/checkSign` | login/find-idpw | live |
| POST `/signRegister` | `sign-certificate.html`(도달불가)만 | **죽은코드** |
| POST `/signRemove` | `sign-certificate.html`(도달불가)만 | **죽은코드** |
| POST `/financPid` | `financ.js` | live |
| POST `/getLoginCiInfo` | `financ.js` | live |
| POST `/getNonce` | `financ.js` | live |
| POST `/passwordtype` | login / onepass-result / `financ.js` | live |
| POST `/simpleMberCi` | `login.html` | live |

> 전자서명: **검증(`checkSign`)만 살아있고 등록·삭제 경로가 도달불가**. 등록 없이 검증만 도는 반쪽 상태 — MSA 재현 시 그대로 옮길 대상이 아니다.

#### 2-4. `saleson.api.mypage.old.MypageController` — `/api/mypage/2`
클래스 전체 600줄. 프론트 래퍼 `getPoints`(`/api/mypage/points`), `getGrade`(`/api/mypage/grade`)는 **`/2` 없이** 호출해 현행 `MypageController`·`old` 어느 쪽에도 매칭되지 않는다. → **패키지 통째 죽은코드**.


### 3. 매퍼 쿼리 인벤토리 (member 관련 9개 XML / 227 쿼리)

판정 방식: 쿼리 id → 매퍼 인터페이스 선언 여부 → 소스 전체에서 `.<id>(` 호출 존재 여부.
AS-IS는 **master/slave 매퍼 이중 주입**(`slaveSecedeUserMapper` 등)과 대문자 필드명(`JoinMapper.getUserInfoByUserId(...)`)이 섞여 있어, 주입 변수명 가정 방식은 오탐이 난다. 호출 자체를 추적하는 방식으로 교차검증했다.

| 매퍼 | namespace | 쿼리수 | 사장 |
|---|---|---|---|
| `user-mapper.xml` | `saleson.shop.user.UserMapper` | 131 | **12** |
| `mypage-mapper.xml` | `saleson.shop.mypage.MypageMapper` | 32 | 0 |
| `generalcustomer-mapper.xml` | `saleson.shop.user.GeneralCustomerMapper` | 27 | 0 |
| `policy-mapper.xml` | `saleson.shop.policy.PolicyMapper` | 10 | 0 |
| `user-sns-mapper.xml` | `saleson.shop.usersns.UserSnsMapper` | 9 | 0 |
| `join-mapper.xml` | `saleson.shop.user.JoinMapper` | 8 | 0 |
| `userauth-mapper.xml` | `saleson.common.userauth.UserAuthMapper` | 5 | 0 |
| `secedeuser-mapper.xml` | `saleson.shop.user.SecedeUserMapper` | 3 | 0 |
| `sleepuser-mapper.xml` | `saleson.shop.user.SleepUserMapper` | 2 | 0 |
| **합계** | | **227** | **12** |

#### 3-1. `user-mapper.xml` 사장 쿼리 12건 (죽은코드)
- `getUserListForExcel` — **XML에만 존재**. `UserMapper.java` 인터페이스에 선언조차 없음(완전 고아).
- 인터페이스 선언은 있으나 호출부 0:
  `deleteUserById`, `deleteUserRoleByAuthority`, `getConfirmPurchaseRequestUserList`, `getConfirmPurchaseUserList`,
  `getPasswordCount`, `getPasswordCountByUser`, `getUserCountByUserId`, `getUserCountByUserLevel`,
  `getUserRoleListByLoginId`, `getUserRoleListByUserId`, `mergeUserRole`

> `getPasswordCount*`는 **비밀번호 재사용 금지(직전 N개 대조)** 용도로 보이는데 호출부가 없다 → AS-IS에서 해당 정책 미작동. MSA에 이식할 대상이 아니다(RFP/ISP에 신규 요건으로 있는지는 별도 확인 필요).

### 4. 서비스 로직 대조 — 핵심 3개 플로우

#### 4-1. 회원가입 (`JoinController.join` → `joinService.insertUserAndUserDetail`)

AS-IS 실행 순서와 각 단계의 MSA 대응:

| # | AS-IS 규칙 (근거) | MSA (`MemberService.signup`) | 판정 |
|---|---|---|---|
| 1 | `bindingResult.hasErrors() \|\| mberCi 없음 \|\| mberDi 없음 \|\| birthdayFull 없음` → `BAD_REQUEST` (`JoinController:165~170`) | 검사 없음 | **재현누락** — 본인인증(CI/DI) 없이 가입 가능 |
| 2 | `password` 공란 → `BAD_REQUEST` | `password != passwordConfirm` 검사만 | 부분 |
| 3 | `!userInfo.isAuth()` → `NOT_EXIST_AUTH` (`:182`) | 없음 | **재현누락** |
| 4 | `userService.getUserInfoByCi()` 결과 있으면 `DUPLICATION_CI_JOIN_USER` (`:186~193`) — **CI 기준 1인 1계정 강제** | 없음 | **재현누락** (핵심 정책) |
| 5 | `userService.checkDuplication()` — ①`config.deniedId` 콤마목록의 **금지 아이디** 대조 ②`getUserCountByUserInfo` (`UserServiceImpl:1755~1785`) | `userRepository.findByLoginId` 만 | **재현누락** — 금지 아이디 목록 미구현 |
| 6 | `userService.selectNewUserId()` 별도 채번 (`OP_USER` 시퀀스 주석처리됨) | JPA identity | 의도적축소(DB 상이) |
| 7 | `userDetail.setLoginPathCode("100")` | `LOGIN_PATH_IDPW = "100"` | **대응있음** (값 일치) |
| 8 | `joinService.insertUserAndUserDetail(user, userDetail)` | `userRepository.save` + `userDetailRepository.save` + `userRoleRepository.save(ROLE_USER)` | 대응있음 |
| 9 | `parentResponse` 있으면 `OP_USER_PARENT` insert (법정대리인 CI/DI/성명/내외국인/성별) (`:213~241`) | 없음 | **재현누락** — 14세 미만 법정대리인 |

`userModifyDataSet` (`JoinController:384~471`)이 UserDetail에 채우는 항목 중 MSA `SignupForm`(9필드)이 **수집하지 않는 것**:
`telNumber`, `post`/`newPost`(우편번호), `birthdayType`(양/음력), `gender`,
`receiveEmail`·`receiveSms`·`receivePbanc`·`receiveKakao`(**수신동의 4종**),
`locGovList[]`(**관심 지자체 다중선택**), `rtnpsntList[]`(**관심 답례품**),
`mberCi`·`mberDi`·`mberDn`·`mberFinDn`, `locgovCode`, `userKey`

> 단, AS-IS `insertUserDetail`은 `GENDER`를 **리터럴 `null`로 기록**하고 `NEW_POST`/`BIRTHDAY_TYPE`은 INSERT문에 아예 없다. DTO에만 있고 저장되지 않는 필드가 섞여 있으므로, 이식 대상은 **INSERT문 기준**으로 잡아야 한다.
> MSA `UserDetail` 엔티티에 없는 실제 컬럼: `TEL_NUMBER`, `FAX_NUMBER`, `AGE`, `BUY_COUNT`, `BUY_PRICE`, `SITE_FLAG`.

MSA가 **추가로** 하는 것: 가입 환영 알림톡 `WELCOME_SIGNUP` 발송 → AS-IS 대응 여부 확인 필요(RFP/ISP 근거 없으면 [[scope-migration-not-greenfield]] 위반).

#### 4-2. 로그인 (`AuthController.getUserToken` `/api/auth/token`)

AS-IS는 **단일 엔드포인트가 3개 역할을 분기**한다: `ROLE_USER` / `ROLE_OPMANAGER` / `ROLE_SELLER`.
(`ROLE_OPMANAGER`·`ROLE_SELLER`는 loginId에서 `OPMANAGER_LOGIN_KEY`/`SELLER_LOGIN_KEY` 접두를 제거해 처리 — `:1246~1250`)

| AS-IS 규칙 (근거) | MSA | 판정 |
|---|---|---|
| 로그인 실패 5회 이상 → 계정 잠금 안내 (`:1200`) | `MAX_LOGIN_FAIL_COUNT = 5` → `STATUS_LOCKED` | **대응있음** |
| `passwordType`: `N`=일반, `T`=임시, **`P`=카카오** (`:1199` 주석) | `N`/`T`만 존재 — **`P` 없음** | **재현누락** |
| `!"P".equals(passwordType) && loginFailCount >= 5` — 카카오 회원은 잠금안내 **제외** (`:1200`) | 예외 없음 | **재현누락** |
| `!"P".equals(passwordType) && passwordExpiredDateDiff <= 0` → `PASSWORD_EXPIRED` (`:1232`) | `passwordExpiredDate` + `/password?expired=` 있으나 **`P` 예외 없음** | 부분 — SNS 회원이 비번만료 대상이 됨 |
| `"T".equals(passwordType)` → `PASSWORD_TEMP` (`:1235`) | `PASSWORD_TYPE_TEMPORARY` | 대응있음 |
| `"4".equals(statusCode)` → `SLEEP_USER` (`:1238`) | `STATUS_DORMANT` + `/reactivate` | 대응있음(코드체계 상이) |
| `userKey 있음 && loginPath=="300"` → `ONEPASS_USER` (`:1241`) | `ExternalLoginController` onepass (mock) | 의도적축소(외부연계) |
| `ROLE_ADMIN_7`/`ROLE_ADMIN_8`(오프라인 담당자) → 사용자 페이지 접근 차단 `OFF_ACCESS_FRONT` (`:1204~1211`) | 없음 | **재현누락** |
| ASIS 인증 API 통신 (`:1215`) | 없음 | 의도적축소(레거시 연계) |
| `INCORRECT_PASSWORD_LOCK` / `UNAUTHORIZED_LOCK` + `getUserLockMessage()` 분기 | 단일 메시지 | 부분 |

> `code`는 **순차 대입이라 마지막이 이긴다**: 우선순위 `ONEPASS_USER > SLEEP_USER > PASSWORD_TEMP > PASSWORD_EXPIRED`.
> MSA 카카오 가입(`ExternalLoginService:146`)은 랜덤 비밀번호만 넣고 `passwordType`을 설정하지 않아, **SNS 회원이 비밀번호 만료·5회 잠금 정책에 걸린다**. AS-IS와 동작이 다르다.

#### 4-3. 전자서명 (반쪽 상태)
- `checkSign`(검증)만 live — `login.html`, `find-idpw.html`
- `signRegister`/`signRemove`(등록·삭제)는 `sign-certificate.html`에서만 호출되는데 **그 화면이 도달불가**
- → AS-IS 운영상 **등록 경로 없이 검증만 도는 상태**. 그대로 이식할 대상이 아니다. [[member-service-deferred-items]]의 "전자서명" 항목 판단 근거로 사용.

### 5. 화면 이벤트 핸들러 대조

#### 5-1. `users/login.html` → MSA `login.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `setTab(1)` / `setTab(2)` / `setTab(3)` | 로그인 방식 탭 **3개** | `loginSetTab(1)` / `loginSetTab(2)` — **2개** | **재현누락** (탭 1개 부족) |
| `@submit.prevent="submit"` + `@click="submitBefore"` | 일반 로그인(전처리 후 제출) | 폼 submit | 대응있음 |
| `@keydown="enterInput($event)"` | 엔터 제출 | 확인필요 | |
| `@keyup="checkPwd($event)"` | 실시간 비밀번호 유효성 | 확인필요 | |
| `@click.prevent="submitKakao"` | 카카오 로그인 | `startKakaoCert()` | 대응있음(mock) |
| `@click.prevent="submitNaver"` | 네이버 로그인 | 없음 | **재현누락** |
| `@click="financLogin()"` | 금융인증서 로그인 | `/login/finance-cert` (mock) | 의도적축소 |
| `@click="openMobileAuth()"` | 휴대폰 본인인증 레이어 | 확인필요 | |
| `@click="onepassLoginBefore()"` / `@submit.prevent="onepassLoginBefore"` / `@keyup="changeOnePassId()"` / `@keydown="enterInputOnepass($event)"` | 원패스 로그인(전용 입력 4핸들러) | `/onepass-login` (mock) | 의도적축소 |
| `@click="changPwd()"` / `@click="changPwdLater()"` | 비번만료 시 변경 / 나중에 | `/password`, `/password/postpone` | 대응있음 |
| `@click="closePopupLayer()"` | 레이어 닫기 | 확인필요 | |

#### 5-2. `users/join.html` → MSA `signup.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `@click="checkIdUsedYn()"` | 아이디 중복확인 | `checkLoginId()` | 대응있음 |
| `@change="changeId()"` | 아이디 변경 시 중복확인 상태 초기화 | 확인필요 | |
| `@keyup="checkPwd()"` | 실시간 비밀번호 규칙 검사 | 확인필요 | |
| `@click="eyeToggle()"` | 비밀번호 보기 토글 | `eyeToggle('password')` | 대응있음 |
| `@change="emailChange($event)"` | 이메일 도메인 선택 | `onEmailDomainChange()` | 대응있음 |
| `@click="searchAddress()"` | 주소검색 팝업(`jusoPopup.html`) | `signup.html`엔 없음 / `profile.html`엔 있음 | **재현누락**(가입 단계) |
| `@change="getSubLocGovList($event)"` | 상위 지자체 선택 → 하위 목록 로드 (`/api/join/getSubLocGov`) | 없음 | **재현누락** |
| `@click="addRow()"` / `@click="deleteRow(data, index)"` | **관심 지자체 다중 행 추가/삭제** | 없음 (프로필에만 `addInterestLocgov`/`removeInterestLocgov`) | **재현누락**(가입 단계) |
| `@click="openMobileAuth()"` | 휴대폰 본인인증 | `/signup/mobile-auth` (mock) | 의도적축소 |
| `@click="submit()"` | 가입 제출 | `onsubmit="return beforeSubmit()"` | 대응있음 |
| `@click="closePopupKakao()"` | 가입완료 모달 닫기 | 완료 모달 닫기 | 대응있음 |
| `@click="goToMain()"` ×2 | 메인 이동 | `location.href='/'` | 대응있음 |

MSA가 **추가로** 가진 것: `goToStep(1|2|3)` 3단계 위저드, `toggleAll()` 전체약관, `onRequiredChange()`, `onAdParentChange/onAdChildChange` 수신동의 토글, `startNaverJoin()`.

#### 5-3. 수신동의 — 양쪽 모두 비정상 (별개 원인)
- **AS-IS**: `join.html:374~414`의 수신동의 라디오(`receivePbanc`/`receiveSms`/`receiveEmail`/`receiveKakao`)가 **전부 주석 처리**. 기본값은 `"1"`이고 `join.html:827~838`에서 `'0'`일 때만 세션 동의 플래그를 세우므로, 일반 가입은 **항상 4종 미동의('1')로 저장**된다. 화면에 남은 "국민비서·SMS 수신동의 / email 수신동의" 문구는 **가입완료 모달 안의 안내 텍스트**이지 입력이 아니다(“미동의를 선택하실 경우 마이페이지 > 동의 항목을 미선택으로 체크하시기 바랍니다”).
  → 값 규약: **`0` = 동의, `1` = 미동의**
- **MSA**: `signup.html:106~119`의 체크박스 4종(`adSms`/`adEmail`/`adPbanc`/`adKakao`)에 **`name`도 `th:field`도 없다**. `SignupForm`에 대응 필드도 없다. 사용자가 체크해도 서버로 전송되지 않아 `UserDetail.receive*`는 **null**로 남는다. → **배선 끊김(버그)**
- 라벨 대응도 어긋난다: MSA는 `id="adSms"`에 "국민비서", `id="adPbanc"`에 "SMS" 라벨을 붙였다. AS-IS는 `receivePbanc`가 국민비서(공공알림), `receiveSms`가 SMS다. → **id와 라벨이 서로 뒤바뀜**

#### 5-4. 가입 완료 후 동선
| | AS-IS (`join.html:796~845`) | MSA (`AuthController:117~133`) |
|---|---|---|
| 1 | `/api/join/join` 성공 | `memberService.signup(form)` |
| 2 | `$s.ev.log.joinUser(userId)` — GA 이벤트 로깅 | 없음 → **재현누락** |
| 3 | `getAuthToken({loginType:'ROLE_USER', ...})` **자동 로그인** | `session.setAttribute` + `authCookieSupport.issue` **자동 로그인** — 대응있음 |
| 4 | 세션 `LOGIN_FIRST='Y'` | 없음 → **재현누락** |
| 5 | `receive*=='0'`이면 `EMAIL_AGREE`/`SMS_AGREE`/`PBANC_AGREE`/`KAKAO_AGREE` 세션 플래그 | 없음 → 재현누락(단 AS-IS도 기본값 '1'이라 실제 미작동) |
| 6 | `$s.redirect($s.pages.INDEX)` — **메인 이동** | `redirect:/signup?completed=1` — **가입화면 완료 모달** | 차이(경미). MSA 모달은 AS-IS의 인증가입 경로 `#joinComplete` 모달을 재현한 것 |

#### 5-4. `users/modify.html` → MSA `profile.html` / `personal-info.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `@click="getMobileAuth('name')"` / `('phone')` / `('password')` | **용도별 본인인증 3종** | 없음 | **재현누락** |
| `@click="checkPresentPwd()"` | 현재 비밀번호 확인 | `/api/password/verify` | 대응있음 |
| `@click="changPwd()"` | 비밀번호 변경 | `/api/password` | 대응있음 |
| `@click="onepassCancel()"` | 원패스 연동 해제 | 없음 | **재현누락** ([[member-service-deferred-items]] "SNS 연동해지") |
| `@click="kakaoLinkClear()"` | 카카오 연동 해제 | 없음 | **재현누락** (동상) |
| `@click="goToSecede()"` | 탈퇴 화면 이동 | 있음 | 대응있음 |
| `@click="searchAddress()"` | 주소검색 | `searchAddress()` | 대응있음 |
| `@change="upperLocgovChange($event)"` | 상위 지자체 변경 | `filterAddLocgov()` | 대응있음(형태 상이) |
| `@click="addRow()"` / `@click="deleteRow(data,index)"` | 관심 지자체 추가/삭제 | `addInterestLocgov()` / `removeInterestLocgov(this)` | 대응있음 |
| `@click="authLayer()"` | 인증 레이어 열기 | 확인필요 | |
| `@change="emailChange($event)"` | 이메일 도메인 | `onEmailDomainChange()` | 대응있음 |
| `@keyup="checkPwd()"` | 실시간 비번 검사 | 확인필요 | |

#### 5-5. `users/secede.html` → MSA `withdraw.html`
| AS-IS | MSA | 판정 |
|---|---|---|
| `@click="submitConfirm()"` | `onsubmit="return beforeSubmit()"` | 대응있음 |
| `@click="goToModify()"` | `location.href=this.dataset.url` | 대응있음 |
| `@click="focusTar($event)"` | 없음 | 확인필요(접근성 포커스 이동) |

#### 5-6. `users/find-idpw.html` → MSA `find-idpw.html`
| AS-IS | MSA | 판정 |
|---|---|---|
| `swapTab('id')` / `swapTab('password')` | `setTab('id')` / `setTab('pw')` | 대응있음 |
| `@submit.prevent="findId"` | `resetId()` | 대응있음 |
| `@submit.prevent="findPasswordStep1"` / `findPasswordStep2` | `authSendCode()` / `authVerify()` / `pwReset()` | 대응있음 |
| `authLayer()` / `authLayerPwd()` | `openAuthModal('id')` / `openAuthModal('pw')` | 대응있음 |
| `changePwd()` | `resetPw()` | 대응있음 |
| `goLogin()` / `goJoin()` / `goMain()` | `location.href` 3종 | 대응있음 |
| — | `authBypass()` / `startMockAuth()` | MSA 전용(개발 우회) |

### 6. 조치 후보 (우선순위)

**A. 버그 — AS-IS 재현 의도가 있는데 배선이 끊긴 것**
1. `signup.html` 수신동의 4종에 `th:field` 부재 → 폼 전송 누락. `SignupForm`에 `receiveSms/receiveEmail/receivePbanc/receiveKakao` 추가 필요. 값 규약 `0=동의 / 1=미동의`.
2. 동 체크박스의 `id`↔라벨 뒤바뀜(`adSms`=국민비서, `adPbanc`=SMS).
3. `ExternalLoginService`가 SNS 가입 시 `passwordType='P'`를 설정하지 않아 SNS 회원이 비밀번호 만료·5회 잠금 정책에 걸림.

**B. 재현누락 — AS-IS에 살아있는 로직인데 MSA에 없는 것**
4. 가입 시 **CI 기준 1인 1계정** 검사(`DUPLICATION_CI_JOIN_USER`)
5. 가입 시 본인인증 필수(`mberCi`/`mberDi`/`birthdayFull` + `isAuth`)
6. 금지 아이디 목록(`config.deniedId`) 검사
7. 가입 단계의 **관심 지자체 다중선택**(`addRow`/`deleteRow`/`getSubLocGov`) — 프로필엔 있으나 가입엔 없음
8. 가입 단계 주소검색(`searchAddress`)
9. `modify` 화면의 용도별 본인인증 3종(`name`/`phone`/`password`)
10. SNS·원패스 **연동 해제**(`kakaoLinkClear`/`onepassCancel`)
11. 로그인 탭 3번째(AS-IS 3개 / MSA 2개), 네이버 로그인 버튼
12. `ROLE_ADMIN_7/8` 오프라인 담당자의 사용자페이지 접근 차단
13. 14세 미만 법정대리인(`OP_USER_PARENT`)

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `/api/auth/*` 회원기능 16개 엔드포인트, `/api/mypage/2` 패키지 전체, `user-mapper` 사장쿼리 12건
- 전자서명 등록/삭제(`signRegister`/`signRemove`) — 화면 도달불가
- 휴면·비번변경 **전용 화면**(`sleep-user.html`/`change-password.html`) — 파일 자체가 없음
- 비밀번호 재사용 금지(`getPasswordCount*`) — 호출부 0

**D. 근거 확인 필요 (RFP/ISP 대조 대상)**
- MSA 전용 추가분: 가입 환영 알림톡 `WELCOME_SIGNUP`, 3단계 위저드, MFA, `authBypass`/`startMockAuth`

---

### 7. 조치 결과 (2026-09-10)

#### 7-0. 초판 오판 정정 4건
검증 방식의 사각지대 때문에 §5에서 잘못 판정한 항목이다.

| 항목 | 초판 판정 | 실제 | 원인 |
|---|---|---|---|
| 로그인 탭 3개 | 재현누락(탭 부족) | **대응있음** — AS-IS `login.html:273~274`의 `setTab(3)`은 **주석 처리**. 살아있는 탭은 2개 | 주석 여부를 보지 않고 핸들러만 셈 |
| 네이버 로그인 버튼 | 재현누락 | **대응있음** — MSA `login.html:86`에 `<a th:href="@{/login/naver}">` 존재 | `onclick=`만 grep해 `<a href>` 이동을 놓침 |
| `/api/user/modifyReceive` | 재현누락 | **대응있음** — MSA `profile.html:158~161`에 수신동의 라디오 4종(`name` 정상), `ProfileController:143~146`이 바인딩 | 가입 화면만 보고 프로필을 보지 않음 |
| `getMobileAuth('name'/'phone'/'password')` | 재현누락 | **의도적축소** — Siren24 PCC 유료연동 부재로 `/coming-soon` 대체. `profile.html:16~19`에 명시 | 해당 화면의 주석을 읽지 않음 |

> 교훈: 화면 이벤트 대조는 `onclick`/`@click`만으로 부족하다. **`<a href>` 내비게이션, 주석 처리 여부, 대응 화면이 여러 개로 쪼개진 경우**를 함께 봐야 한다. [[verify-screen-by-content-not-route]]

#### 7-1. 완료 (12건)

**버그 수정**
1. **SNS 회원 `passwordType='P'` 누락** — AS-IS `KakaoLinkServiceImpl:702`가 KAKAO/NAVER 로그인·연동 때마다 `P`를 찍고, 이를 3곳에서 읽어 비밀번호 만료(`AuthController:1232`, `JwtTokenAuthenticationFilter:542`)와 5회 잠금(`:1200`)에서 제외한다. MSA는 `P` 자체가 없어 **SNS 회원이 비밀번호 만료 대상이 되고 잠길 수 있었다**.
   - `ExternalLoginService`: `PASSWORD_TYPE_SNS` + `SNS_PASSWORD_TYPE_PROVIDERS`(KAKAO/NAVER) 도입, `linkNewAccount`(신규연동)와 `linkOrJoin`의 기존회원 분기(`stampSnsPasswordType`) 양쪽에 적용 — AS-IS가 **매 로그인마다** 다시 찍는 것을 그대로 따랐다.
   - `MemberService.passwordChangeRequired()`: `P`면 즉시 `false`.
   - `MemberService.checkCredentials()`: `justLocked` 조건에 `&& !P` 추가.

**정책 재현**
2. **가입 시 본인인증 필수** (`JoinController:165~170`, `:182`) — `MemberService.requireVerifiedIdentity()`. 이 환경은 인증 게이트웨이가 없어 `DevBypassSettings.identityVerificationBypass`가 켜진 동안만 건너뛴다(로컬 `application.yml`은 `true`, **기본값·운영은 `false`**라 AS-IS와 동일하게 막힌다).
3. **CI 기준 1인 1계정** (`JoinController:186~193` `DUPLICATION_CI_JOIN_USER`) — 같은 메서드. **CI가 들어온 경우엔 우회 여부와 무관하게 항상 검사**한다(카카오 인증서비스처럼 실제 CI가 오는 경로를 막아야 하므로).
4. `SignupForm`에 `mberCi`/`mberDi` 추가, `signup()`이 `User.MBER_CI`/`MBER_DI`에 저장.
5. **금지 아이디 목록** (`UserServiceImpl:1755~1785` `config.deniedId`) — `MemberPolicySettings`(신규) + `MemberService.loginIdAvailable()`. AS-IS가 **아이디 중복확인 엔드포인트에서도 같은 `checkDuplication()`을 태우므로**(`JoinController:129`), MSA `/api/check-login-id`도 같은 규칙으로 바꿨다. 설정 키 `ghlove.member.denied-login-ids`(admin 설정 이관 시 주입원만 교체).
6. **오프라인 담당자 차단** (`AuthController:1204~1212` `OFF_ACCESS_FRONT`) — `MemberService.rejectOfflineManager()`. `ROLE_ADMIN_7`(주담당자)/`ROLE_ADMIN_8`(부담당자)의 사용자 페이지 로그인을 막는다.
   - **AS-IS와의 의도적 차이**: AS-IS는 비밀번호 확인 *전에* 검사해서 비밀번호를 모르는 사람도 응답만으로 "그 아이디가 오프라인 담당자"임을 알아낼 수 있다. MSA는 비밀번호 확인 *후*로 옮겼다 — 정상 이용자가 겪는 동작은 동일하다.

**가입 화면 재현**
7. **주소찾기** (`join.html` `searchAddress()`) — `signup.html`에 우편번호 입력 + 주소찾기 버튼 + Daum 우편번호 위젯. `SignupForm.post` 추가 → `UserDetail.POST` 저장(AS-IS `insertUserDetail`의 POST 컬럼).
8. **관심 지자체 선택** (`join.html` `getSubLocGovList`/`addRow`/`deleteRow`) — `signup.html` step 3에 시·도 → 시·군·구 필터(`filterAddLocgov`)와 추가/삭제 칩(`addSignupLocgov`/`removeSignupLocgov`). 목록은 `AuthController.signupForm`이 `DonationClient.allLocgovs()`로 공급(263건 확인).
   - 관심지자체는 donation이 보유하고 `POST /interest-locgovs`가 쿠키 인증이라, 가입 시점엔 화면에만 담아두고 `sessionStorage`로 넘겨 **가입완료 모달(자동 로그인으로 `GH_AUTH` 발급된 뒤)** 에서 donation(:8082)으로 등록한다. donation의 `@CrossOrigin`이 이미 `8081` + `allowCredentials`를 허용한다.
   - AS-IS는 시·군·구를 서버(`/api/join/getSubLocGov`)에서 매번 조회하지만, MSA는 전체 목록을 한 번 받아 클라이언트에서 거른다(왕복 제거).

#### 7-2. 검증
`member` 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 금지 아이디 `admin`/`test`/`ghlove` | `{"available":false}` |
| 기존 회원 `test007` | `{"available":false}` |
| 미사용 `newuser999` | `{"available":true}` |
| CI 있는 가입 | `{"status":"OK"}` |
| 같은 CI 재가입 | `{"status":"ERROR","message":"이미 가입된 본인인증 정보입니다..."}` |
| 금지 아이디로 가입 | `{"status":"ERROR","message":"이미 사용 중인 아이디입니다."}` |
| 우회 OFF(8091 별도 기동), 본인인증 없이 가입 | `{"status":"ERROR","message":"본인인증을 완료해야 회원가입을 할 수 있습니다."}` |
| 우회 OFF, CI/DI 갖춰 가입 | `{"status":"OK"}` |
| `/signup` 렌더 | 우편번호 input 1, 주소찾기 버튼, 지자체 option 263 |

검증용 계정 2건(`citest001`/`withci001`)은 `op_user_role`→`op_user_detail`→`op_user` 순으로 삭제 완료(차단된 시도는 애초에 행을 만들지 않았다).

#### 7-3. 미처리 — 결정이 필요한 것
- **수신동의**: 사용자 지시로 현행 유지. 값 규약은 확인해 둠 — **`0`=동의, `1`=미동의**(`join.html:827~838`, `popup-layer-kakao.vue:191~194`).
- **카카오 알림톡**(RFP `requirements.md:94` 신규): 사용자 지시로 추후.
- **SNS·원패스 연동 해제**(`kakaoLinkClear`/`onepassCancel`/`/api/auth/disconnect-sns`): [[member-service-deferred-items]]의 결정 대기 항목.
- **`secede-kakao.html` / `onepass_secede.html`** 전용 탈퇴 화면: SNS 연동 해제와 같은 묶음.
- **14세 미만 법정대리인**(`OP_USER_PARENT`): MSA에 테이블은 이미 존재(`member.op_user_parent`). AS-IS는 보호자 휴대폰인증 모달(`join.html:126 getParentModal`)을 거치는데 이 환경엔 본인인증 연계가 없어, 인증 개방 전에는 재현 불가.
- **`passwordtype` 조회 / `modifyMobileAuth`**: 본인인증 연계 의존.
- **`UserDetail` 미보유 컬럼**(`TEL_NUMBER`/`FAX_NUMBER`/`AGE`/`BUY_COUNT`/`BUY_PRICE`/`SITE_FLAG`): 화면 노출처와 함께 정할 사항.

---

## 4. donation

> 통합 전 파일: `docs/as-is-inventory-donation.md`

## AS-IS 인벤토리 — donation 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

### 1. 화면 생사 판정

#### 1-1. 진입 래퍼 패턴
AS-IS 기부 화면은 **래퍼 → 실제화면** 2단 구조다. 이걸 모르면 실제 화면을 사장으로 오판한다.

| 래퍼 | 하는 일 | 실제 화면 |
|---|---|---|
| `donation/donation.html` | **넷퍼널(NetFunnel) 접속자 대기열** + `2024/02/08 18:00 ~ 02/12 09:00` 세외수입 전환 점검 차단(`:96~104`) | `donation-main.html` |
| `donation/donationNext.html` | 동일 패턴 | `donationNext-main.html` |
| `designated-donation/index.html` | 동일 패턴 | `index-main.html` |

#### 1-2. live (18)
`donation.html`(래퍼), `donation-main.html`(실제 기부하기), `list-select.html`, `map-select.html`, `complete.html`(**기부하기 "처리중"** 로딩화면 — 완료화면이 아니다), `guide1/2/3/5/6.html`, `giro-success.html`·`giro-fail.html`(지로 결과 착지 — `NgDonationRelayServiceImpl:716~717,1361~1362`), `wetaxInfo_m.html`(`mypage/cntrList.html:643` window.open), `designated-donation/index.html`·`index-main.html`·`details.html`

mypage 기부분 live: `cntrList.html`(32), `receiptList.html`(10), `receiptPrint.html`(1), `receiptListPrint.html`(2), `honorList.html`(14, **기부혜택증**), `intrstLocGov.html`(15)

#### 1-3. 죽은코드 (26)
- **inbound 0**: `donation-backup`, `donation_bak`, `donation-tax`, `donation-test`, `donation_buga_test`, `donation_girotest`, `donationNext`, `donationNext-main`(죽은 래퍼로만 도달), `donation-main_20240529`, `guide1/2/5_20240529`, `guide1/2/3/5/6_20240613`, `guide4_bak`, `quick-donation`, `quick-donation_bak`, `ngdonation`, `process`, `wetaxInfo`, `intro`(donation/ 하위 — 루트 `/intro.html`과 다른 파일), `popup-fail`, `popup-success`, `external-etax`, `external-etax-2`, `external-giro`, `external-giro-2`
- `mypage/honorList_new.html`(0), `mypage/qrSample.html`(0), `mypage/qrTest.html`(0), `mypage/honorList.html_20240927`
- **`mPop.html`**: 모든 참조가 `//donation.giroPopup = window.open(...)` 주석 → 도달불가
- **`donation-lnb_ali.vue`**: 이 LNB 컴포넌트는 `httpVueLoader` 등록이 **전부 주석 처리**되어 어느 화면에서도 렌더되지 않는다. 그 안의 메뉴는 `donation.html`/`list-select.html` 둘뿐.
- **`guide4.html`**: 파일 자체가 없고(`guide4_bak.html`만 존재) 참조도 전부 주석. 메뉴 문구는 "종합소득세 확정신고 서비스 안내" → **기능 비활성**

#### 1-4. 살아있는 안내 메뉴 (`components/layouts/header_ali.vue:276~303`)
순서대로: 지자체 선택(`list-select`) → **고향사랑기부제 안내**(guide1) → **온라인 기부방법**(guide2) → **오프라인 기부방법**(guide5) → **연말정산 세액공제 안내**(guide3) → **고향사랑기부 주의사항**(guide6). guide4 자리는 비어 있다.

### 2. 컨트롤러 — 중복 구현 3세트 중 2세트가 사장

같은 기능이 세 벌 존재한다. **살아있는 것은 `/api/ngdonation` 하나뿐**이다.

| 컨트롤러 | 엔드포인트 | 판정 | 근거 |
|---|---|---|---|
| `NgDonationController` `/api/ngdonation` | 20 | **live** (2건 사장) | `donation-main.html` + `modules/op.donation.js` |
| `RegionTaxController` `/api/regiontax` | **12** | **죽은코드(전부)** | 12개 전부 프론트 호출처 0 |
| `SeoulTaxController` `/api/seoultax` | **4** | **죽은코드(전부)** | `modules/op.seoul.donation.js`가 유일 참조인데, 그 모듈을 로드하는 화면이 `donation-tax.html`(inbound 0)뿐 |
| `DonationTestController` `/api/donationTest` | **4** | **죽은코드(전부)** | 호출처 0 |

> `/api/regiontax`는 `/api/ngdonation`의 리팩터링본으로 보인다(`locGovLmtt //getLocGovLmtt` 같은 주석이 원본 메서드명을 달고 있다). **적용되지 않은 채 남았다.**
> `modules/op.donation.external.js`(→`external-etax-2`/`external-giro-2`), `modules/op.donation.test.js`(로드 화면 0)도 사장.

#### 2-1. `/api/ngdonation` 상세
| 엔드포인트 | 역할 | live 호출처 | 판정 |
|---|---|---|---|
| `/userCntrInfo` | 기부자 정보 | `donation-main.html` | live |
| `/sidoList`·`/sigunguList` | 시·도/시·군·구 | `op.saleson.js` 래퍼 | live |
| `/locGovInfo` | 지자체 정보 | `donation-main`, `list-select`, `map-select` | live |
| `/rsgstadresinfo` | **주민등록 주소 조회(거주지 확인)** | `donation-main.html` | live |
| `/rsgstadresinfoForeigner` | 외국인 주소 조회 | `op.saleson.js` | live |
| `/getPublicKey` | 전송 암호화 공개키 | `op.saleson.js` | live |
| `/getLocGovLmtt` | **지자체 기부제한** | `donation-main`, `map-select`, `items/details-main` | live |
| `/getIntrstLocgov`·`/setIntrstLocgov` | 관심지자체 조회·설정 | `list-select`, `map-select`, `goods/searchGoods-main` | live |
| `/sntrBugaInsert` | 서울 부가정보 등록 | `op.donation.js` | live |
| `/etaxSunapInfo` | 서울 이택스 수납정보 | `op.donation.js` | live |
| `/sunapSuccess` | 수납 완료 처리 | `op.donation.js` | live |
| `/contryBugaInsert` | 지방(위택스) 부가정보 등록 | `op.donation.js` | live |
| `/contryNextSunapInfo` | 차세대 세외수입 수납정보 | `op.donation.js` | live |
| `/nextBugaRequest` | 차세대 부가정보 요청 | `op.donation.js` | live |
| `/local-sunap-confirm` | 지방 수납 확인 | `op.donation.js` | live |
| `/giroPay` | **지로 납부** | `op.donation.js` | live |
| `/contrySunapInfo` | 지방 수납정보(구) | **없음** | **죽은코드** |
| `/giroPayTest` | 지로 테스트 | **없음** | **죽은코드** |

#### 2-2. `/api/designated-donation` (8)
`getList`, `getListBySearchEngine`, `getBsnsTypes`, `getDetail`, `getDesignatedCntrList`, `saveCheerMsg`(응원메시지), `getNoticeList` — 전부 live.
**`getFaqList`** — 호출처 0 → **죽은코드**.

#### 2-3. `/api/mypage` 기부분 (14)
live: `getCntrList`, `getCntrInitInfo`, `getReceiptInitInfo`, `getReceiptPopInfo`, `honorList`, `honorList-new`, `saveHonorViewHist`, `intrstLocGovInfo`, `deleteIntrstLocGov`, `deleteIntrstLocGovAll`, `receipt-print`, `getLocGov`
**죽은코드**: `qr`, `qrTest` — 각각 `qrSample.html`/`qrTest.html`에서만 호출되는데 두 화면 모두 inbound 0.
> `honorList-new`는 API는 살아있다(`honorList.html`이 `getHonorListNew`를 호출). 사장인 것은 **화면** `honorList_new.html`이다.

### 3. 매퍼 쿼리 (핵심 7종 / 261 쿼리 중 20 사장)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `ngdonation-mapper.xml` | `saleson.shop.donation.NgDonationMapper` | 83 | **12** |
| `designated-donation-mapper.xml` | `...designateddonation.DesignatedDonationMapper` | 60 | 1 |
| `give-state-mapper.xml` | `...give.givestate.GiveStateMapper` | 47 | 4 |
| `locgov-mapper.xml` | `saleson.shop.user.LocgovMapper` | 34 | 3 |
| `offgive-mapper.xml` | `saleson.shop.offgive.OffgiveMapper` | 22 | 0 |
| `lclgvHnrUser-mapper.xml` | `...lclgvHnrUser.LclgvHnrUserMngMapper` | 11 | 0 |
| `locgov-image-mapper.xml` | `saleson.shop.user.LocgovImageMapper` | 4 | 0 |
| **합계** | | **261** | **20** |

(admin 배정 대상은 별도: `give-statistics` 25, `give-opertaion` 14, `locgov-databoard` 15, `statistics-locgov` 4 / point 배정: `give-point` 8, `give-point-expiration` 5, `order-give-point` 16)

#### 3-1. 사장 쿼리 목록
- `ngdonation` **XML 고아**(인터페이스 미선언): `getCntrLocgovCode`, `getUserCiFromOpUserCi`
- `ngdonation` 호출 0: `deleteNotSunapStndOneBatch`, `getPresentTypeList`, `getProcessDeptCd`, `getSeoulNotSunapList`, `getStandardNotSunapList`, `getUserCI`, **`getUserCntrLimit`**, `insertRelayLog`, `selectHometaxCount`, `updateCntrBlcePoint`
- `locgov` XML 고아: `getLocgById` / 호출 0: `resetHonorCntrUser`, `resetStdrAmt`
- `designated-donation` XML 고아: `getMaxOrderingDesignatedDonationNoticeImgDesc`
- `give-state` 호출 0: `getCntrTaxTempLog`, `insertCntrTaxTempLog`, `giveDeleteAt`, `giveReqmngSmsUserInfoByReqId`

> **`getUserCntrLimit`**(회원 기부한도)와 **`updateCntrBlcePoint`**(기부잔액 포인트)가 호출 0이다. 즉 한도 검증이 이 쿼리로는 돌지 않는다 — **실제 경로는 `DonationVerification`(공통코드 `DONATION_LIMIT_AMT`)이며 §5-1에서 규명했다.**
> `insertRelayLog`(중계 로그)도 호출 0인데, MSA에는 `/api/admin/relay-log`가 있다. 근거 재확인 필요.

### 4. MSA 대응 요약

MSA donation은 컨트롤러 25개 / 엔드포인트 약 130개로 AS-IS보다 넓다(운영관리 API를 함께 갖고 있기 때문).

**대응있음(주요)**
- `list-select.html` → `/list-select` + `/api/list-select/{provinces,cities,fund}`
- `guide1/2/5/6` → `/guide1`,`/guide2`,`/guide5`,`/guide6`
- **`guide3`(연말정산 세액공제 안내) → `/honor`** — 라우트명은 다르나 `honor.html`의 `<title>`이 `안내사항 > 연말정산 세액공제 안내`로 동일하고, 헤더·푸터가 같은 문구로 링크한다. **경로만 보고 누락으로 판정하면 안 되는 사례**([[verify-screen-by-content-not-route]])
- `mypage/honorList.html`(기부혜택증) → `/honor/certificates` + `honor-certificates.html`
- `mypage/cntrList.html` → `/my`, `mypage/receiptList·receiptPrint` → `/receipts`, `/receipts/official/{cntrSn}`
- `mypage/intrstLocGov.html` → `/interest-locgovs`
- `designated-donation/*` → `/designated-donation`, `/designated-donation/{id}`, 응원메시지(`cheerMsg`) 포함
- `getLocGovLmtt` → `/api/locgov-lmtt`
- `rsgstadresinfo`(거주지 확인) → `/donate/verify-residence`

**의도적축소 (외부연계 미개방)**
- **넷퍼널 접속자 대기열** — `donate.html:28`에 "이번 스코프에서는 제외" 명시. CSS(`default_ali.css`)에 `#NetFunnel_Skin_Top` 잔재만 있다.
- 지로 납부(`giroPay`) 및 착지화면 `giro-success`/`giro-fail`, 서울 이택스/위택스 수납 연계(`sntrBugaInsert`·`etaxSunapInfo`·`contryBugaInsert`·`contryNextSunapInfo`·`nextBugaRequest`·`local-sunap-confirm`·`sunapSuccess`)
- `complete.html`(기부하기 **처리중** 화면) — 결제 대기 로딩이라 PG 미연계 시 해당 없음
- `getPublicKey`(전송 암호화), `rsgstadresinfoForeigner`(외국인 주소)
- `wetaxInfo_m.html`(위택스 안내 팝업)

**재현누락**
- **`map-select.html` 지도로 지자체 선택** — MSA에 지도 화면 없음. `LocgovMapProvince.ALL`은 시·도 **목록 데이터**로만 쓰인다(`DesignatedProjectController:64`, `FundProjectApiController:41`). [[donation-service-deferred-items]]의 "지도 선택"
- `/api/designated-donation/getFaqList`는 AS-IS에서도 죽은코드라 이식 대상 아님

**확인필요**
- AS-IS 연 500만원 기부한도의 실제 검증 경로(§3-1 `getUserCntrLimit` 호출 0)
- `receiptListPrint.html`(기부확인증 일괄 인쇄) 대응
- `goods/searchGoods-main.html`의 `setIntrstLocgov` 호출(gift 화면에서 관심지자체 설정) 대응

---

### 5. 조치 결과 (2026-09-10)

#### 5-0. 초판 오판 정정 — 주석 블록 사각지대
| 항목 | 초판 | 실제 |
|---|---|---|
| 기부제한(`getLocGovLmtt`) 진입점 | "AS-IS 4곳 / MSA 2곳 → 부분" | **live 진입점은 2곳뿐, MSA와 일치 → 대응있음**. `list-select.html:830`은 블록 전체가 `/* */` 안이고 호출 경로도 존재하지 않는 `/api/donation/getLocGovLmtt`다. `map-select.html:1443`도 주석이며, 실제로는 `locGovInfo` 응답에 실려 온 `lmttBgnDe`/`violtResnCn`으로 판정한다(`:1430~1435`). |

> member의 "탭 3개/네이버 버튼" 오판과 같은 뿌리다. **핸들러·호출을 셀 때 주석 블록 안인지 반드시 본다.**

#### 5-1. 연간 기부한도 — 규명과 조치
AS-IS 한도 로직은 **두 벌**이고 실제로 도는 것은 공통코드 쪽이다.
- `DonationService:93` `MAX_CNTR_AMT_LIMIT = 5000000` 하드코딩 → `userCntrLimitAmt` 표시용(`:246,:256`)
- **`DonationVerification.isDonationNormalAmount(userId, amount)`** ← 실제 검증. 호출처 3곳: `NgDonationController:358`(서울), `:692`(차세대), `OffgiveServiceImpl:339`(오프라인)

```
limitAmt = 공통코드 DONATION_LIMIT_AMT[올해].label      // 연도별. 하드코딩 아님
maxCntrAmt = limitAmt - getGCntrSumCntrAmt(userId)      // 본인 올해 기부합
                      - getGMberSecsnSumCntrAmt(mberCi) // 올해 탈퇴분 기부합 (CI 기준)
                      - getGcntrWegiveCntrAmt(mberCi)   // 위기부 합
return !(amount % 100 != 0 || amount < 0 || amount > maxCntrAmt)
```

§3-1에서 "호출 0"으로 나온 `getUserCntrLimit`는 **쓰이지 않는 다른 구현**이었다. 실제 경로는 위와 같다 — 규명 완료.

> **AS-IS 자체 결함**: 지방(위택스) 경로 `contryBugaInsert`(`NgDonationController:477~515`)에는 이 검증이 **없다**. 서울·차세대·오프라인에만 걸려 있다. MSA는 전 경로에서 검증하므로 이식 대상이 아니다.

#### 5-2. 완료 (5건)

1. **100원 단위 검증** — `DonationService.requireAmount()`에 `amount % 100 != 0` 추가. 세외수입 고지서가 100원 단위로만 생성되기 때문에 AS-IS가 건 제약이다.
2. **탈퇴회원 기부액 CI 기준 합산** — 가장 큰 구멍이었다. `member.g_mber_secsn` **테이블은 있는데 어느 서비스도 참조하지 않아**, 탈퇴 후 재가입하면 연간 한도가 초기화됐다.
   - member: `MberSecsn` 엔티티/저장소 신설, **탈퇴 두 경로 모두**(`MemberService.withdraw` 본인탈퇴, `AdminMemberService.adminWithdraw` 강제탈퇴)에서 `DonationClient.mySummary().thisYearAmt()`로 그 해 기부액을 스냅샷.
   - member: `GET /api/users/{userId}/withdrawn-donation-carryover` — CI는 개인식별자라 응답에 담지 않고 **합계만** 반환.
   - donation: `MemberClient.withdrawnDonationCarryOver()` → `validateAnnualLimit`의 누적액에 가산. **조회 실패 시 0이 아니라 예외를 던진다** — 실패를 0으로 삼키면 그 순간이 한도 우회 창구가 된다. 표시용 `remainingAnnualLimit`만 실패를 0으로 보고 화면을 살린다(차단은 어차피 제출 시 걸린다).
3. **탈퇴회원 CI로 재가입 허용** — 2번을 검증하다 발견. member 작업에서 넣은 CI 1인1계정 검사가 **탈퇴 회원까지 막고 있었다**. AS-IS `getUserInfoByCi`는 `status_code = '9'`(정상)만 조회하며(상태코드 `1:가입대기 2:차단 3:탈퇴 4:휴면 9:정상`, `GeneralCustomer.java:30`), 애초에 `G_MBER_SECSN`의 존재 자체가 **재가입을 전제**한다. `findFirstByMberCiAndStatusCodeOrderByUserIdDesc(ci, ACTIVE)`로 교정.
   - 부수 효과: 한 CI에 여러 계정이 쌓일 수 있어 `Optional<User> findByMberCi`는 `NonUniqueResultException`을 낸다. `ExternalLoginService.findLinkedUser`도 같은 방식으로 상태를 좁히도록 함께 고쳤다.
4. **기부혜택증 열람이력** — `HonorViewHist` 엔티티/저장소 + `recordHonorCertificateViews()`. AS-IS는 캐러셀 `slideChange`마다 기록하지만(`honorList.html:381`) MSA는 목록으로 전부 보여주므로 화면을 열 때 표시된 지자체를 모두 기록한다.
   - AS-IS `saveHonorViewHist`는 저장 직후 **`throw new OpRuntimeException("test")`가 무조건 실행되어 항상 `SYSTEM_ERROR`를 응답**한다(`MypageController:1078`). 디버그 잔재이므로 옮기지 않았다.
5. 기부제한 진입점 판정 정정(§5-0).

#### 5-3. 검증
donation·member 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 12,345원 기부 | `기부금액은 100원 단위로 입력해 주세요.` |
| 3,000,000원 기부 | 성공(COMPLETED) |
| 탈퇴 실행 → `g_mber_secsn` | `2026 / 1068 / CARRY-CI-0001 / 3000000` |
| 같은 CI로 재가입 | 성공 (교정 전에는 차단됐음) |
| 재가입 계정 이월액 조회 | `{"amount":3000000}` |
| 한도 2,000만 − 이월 300만 = 1,700만 / **1,800만 요청** | `연간 총 기부한도(20,000,000원)를 초과합니다. 올해 누적 기부액: 3,000,000원` |
| **1,700만 요청** | 성공 |

검증 데이터(회원 2, 기부 2, 포인트원장 2, 잔액 2, 스냅샷 1)는 전부 삭제 확인.

#### 5-4. 미처리
- **`map-select.html` 지도 선택** — 규모가 커 별도 판단 필요. [[donation-service-deferred-items]]
- **위기부 합산**(`getGcntrWegiveCntrAmt` / `sumWegiveCntrAmtByWegiveAmtAndMberCi`) — "위기부"의 의미와 데이터 출처 확인 후 결정
- `receiptListPrint.html`(일괄 인쇄), `getListBySearchEngine`(검색엔진 연계), `getNoticeList` 공개 조회 경로
- `goods/searchGoods-main.html`의 `setIntrstLocgov` 호출 대응(gift 화면에서 관심지자체 설정)
- MSA 전용 운영관리 API 5종(`locgov-admin`/`cntr-reqmng`/`ctbny-opratn`/`welfare-centers-admin`/`nts-receipt-logs`)의 admin 도메인 배정 검토

---

### 6. 근거 확인 — 기부금영수증 화면보안(Fasoo Secure Web) 대체 검토 (2026-09-17)

기부금영수증 출력 화면(AS-IS `mypage/receiptPrint.html`)에 걸려있는 상용 화면보안 SW의 정체·용도·요구근거를 규명하고, 탈상용(제거) 가능성을 판정한다. (발단: 상용SW 지속사용 여부 검토)

#### 6-1. AS-IS 실체 — 두 개의 다른 Fasoo 제품 구분

| 구분 | 코드 실재 | 정체 | 위치/근거 |
|---|---|---|---|
| **Fasoo Secure Web (FSW) 4.2.0.0** | ✅ 프론트 코드에 실재 | 브라우저 **화면보호 클라이언트**(네이티브 에이전트, IE=ActiveX `f_swv.dll` / 크롬·엣지=확장+네이티브호스트) | `ghlove-frontend/webDrm/`(fsw.js·ie.js·multi.js·Setup·헬프데스크 HTML 20여개), 운영도메인(`ilovegohyang.go.kr` 등)용 라이선스 시리얼 하드코딩 |
| **Fasoo Enterprise DRM 5** | ❌ 앱 코드에 없음 | 서버/파일 단위 **문서·DB 보안(DBMS문서보안)** | ISP SW 인벤토리(원본 p.325)에 "재활용" 전제로만 기재 |

→ **둘은 다른 제품.** 영수증 화면에서 실제로 동작하는 것은 FSW(화면보호)이고, ISP가 말하는 Fasoo는 Enterprise DRM(문서/DB보안)이다. 혼동 주의.

#### 6-2. FSW 사용처·용도

- **적용 화면: 단 1곳** — `mypage/receiptPrint.html`(마이페이지→기부내역→"기부금영수증 출력" 팝업)에서만 `/webDrm/fsw.js` 로드. 전체 프론트/JSP 통틀어 유일. 관리자(opmanager) 화면엔 없음.
- 이 화면은 **OZReport(OZ Viewer 8.0)**로 `3.cntr_receipt_new.ozr` 리포트를 렌더 → 그 위에 FSW가 화면보호 권한 강제.
- FSW 권한(multi.js `arrRights`) = 캡처차단(SCREENCAPTURE/CAPTURE), 소스보기 차단(VIEWSOURCE), 저장차단(SAVEAS/SAVEIMAGE), 복사·추출 차단(COPY/EXTRACT), 워터마크 인쇄(WATERMARK_PRINT), 키보드보안(SECURE_KEY), 인쇄만 허용.
- 화면 코드도 OZ 저장버튼 disabled+hide, 인쇄버튼만 활성, `viewer.lockopt=true`. 인쇄 실행 시 `OZPrintCommand_OZViewer`→`/api/mypage/receipt-print/insert`로 인쇄이력 기록.
- **용도 결론:** 세액공제 증빙(개인정보+직인 포함)인 영수증의 캡처·저장·소스보기를 막아 위변조/무단복제를 억제하고 인쇄만 허용+추적하는 "출력화면 보안".

#### 6-3. 순수 웹 대체 가능성

- **원리적 한계:** 캡처차단·키보드보안은 **OS 레벨 네이티브 에이전트**가 하는 일이라 브라우저 샌드박스 안의 웹 코드로는 **재현 불가**. "동일 동작"을 원하면 결국 타 상용 화면보안 제품(마크애니/소만사/잉카 등) 네이티브 에이전트로 **교체**하는 것뿐 → 탈상용 목적 미달성.
- **웹표준으로 가능한 실질보호(상용SW 불요):** ① **서버 렌더 워터마크**(기부자명·발급일시·문서번호를 문서에 직접 인쇄 — 캡처당해도 남아 추적 가능, 위변조억제엔 오히려 더 강함) ② 복사/드래그/우클릭 완화 ③ 인쇄이력 기록(MSA 재현완료) ④ 저장버튼 제거·인쇄전용(재현완료). = "차단(enforcement)"이 아닌 "억제(deterrent)" 수준.

#### 6-4. RFP/ISP 요구근거 — **없음**

(저장소에 원본 전문 없음, 요약본 `requirements.md`·`isp-detailed-design-summary.md` 기준)

- **RFP**: 영수증 요구는 "기부영수증 및 기부확인증 발급/재발급(전자 방식)"(발급 기능)뿐. 화면캡처/출력물 유출방지/워터마크 요구 **없음**. 보안요구는 송수신 암호화·역할권한·감사로그(데이터 레벨)뿐.
- **ISP**: "화면보안/캡처/워터마크/출력물 유출방지" 매칭 0건. Fasoo는 Enterprise DRM(DBMS문서보안) "재활용" 항목으로만 등장 — FSW 화면보호를 지목한 기능/보안 요구가 아님. 요구 보안은 N2SF DB암호화·개인정보접속기록(WEDDS)·망분리 등 인프라·데이터 계층.
- **추정:** FSW는 신규 RFP/ISP 요구가 아니라 **AS-IS(SalesOn 원제품/기존 운영환경)에서 관성적으로 딸려온 것**.

#### 6-5. 판정 및 잔여 확인사항

- **판정: 화면보안 필수 요구 근거 없음 → FSW(상용SW) 제거 가능.** 대체안 = 서버 워터마크 + 인쇄이력 + 국세청 전자기부금영수증 연계(정본은 홈택스 전자문서, 종이는 참고용). 이 조합이면 탈상용+위변조억제 동시 달성.
- **MSA 현황:** OZReport는 Thymeleaf 서버렌더+`window.print()`로 대체, 인쇄이력(`/receipts/official/{cntrSn}/print-log`) 재현완료. **FSW 화면보호 계층은 미재현**(의도적, 웹표준 등가 대체 불가). 워터마크는 미적용 상태 → 대체 결정 시 추가 필요.
- **최종 제거 전 안전판:** 공공사업 특성상 RFP 본문 외 **「보안요구사항 정의서」/보안성 심의 산출물** 별첨에 '출력물·화면 유출방지' 항목이 있는지 발주처에 실무 확인 권장(현재 저장소엔 해당 별첨 없음).

---

## 5. gift

> 통합 전 파일: `docs/as-is-inventory-gift.md`

## AS-IS 인벤토리 — gift 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

### 1. 화면 생사 판정

#### 1-1. 래퍼 → 실화면 2단 구조 (donation과 동일)
| 래퍼 | 실화면 |
|---|---|
| `goods/index.html` (inbound 160) | `goods/index-main.html` — `location.replace('/goods/index-main.html' + search)` (`:82~88`) |
| `goods/searchGoods.html` (41) | `goods/searchGoods-main.html` (34) |
| `items/details.html` (58) | `items/details-main.html` (58) — `:71~77` |

#### 1-2. live
`goods/index-main.html`(답례품 목록), `goods/searchGoods-main.html`(검색), `items/details-main.html`(상세),
`category/index.html`(160, 카테고리), `community-business/communityList-main.html`(마을기업),
`featured/eventList.html`(21)·`eventDetail.html`(5)·`detail.html`(12) — **지역이벤트**,
`components/layouts/item_tab-ali.vue` — 상세화면 탭 컴포넌트(`items/details-main.html:1241`에서 활성 등록),
mypage 답례품분: `review.html`(12), `writeReview.html`(2), `favorItem.html`(10), `inquiry.html`(11), `inquiryItem.html`(10)

#### 1-3. **`catalog/` 디렉터리 전체가 도달불가**
`catalog/`에는 `catalog-main.html`·`catalog-faq.html`·`index_old.html` 세 파일이 있는데,
- 유일한 진입 리다이렉트가 `header_ali_20240529.vue:1023`의 `$s.redirect('/catalog/index.html')`인데 **`catalog/index.html`은 존재하지 않는다**
- 그 `header_ali_20240529.vue` 자체도 **활성 등록 0** (사장 레이아웃)
- `catalog-main.html`을 가리키는 유일한 참조는 `header_news.vue:29`의 `menuUrl` 비교문이고, `header_news.vue`는 `catalog-main`/`catalog-faq` 두 화면만 등록한다 → **서로만 참조하는 고립 덩어리**

→ `catalog/` 3화면 + `header_news.vue` + `/api/catalog/*` 6엔드포인트 + `catalog-mapper.xml` 34쿼리가 통째로 사장 후보다.
> member의 `sign-certificate.html`(오타난 파일명), donation의 `guide4.html`(파일 없음)과 같은 유형 — **존재하지 않는 파일을 가리키는 진입점**.

#### 1-4. 지역이벤트의 진입은 푸터다
`featured/eventList.html`은 살아있는 푸터 `footer_ali.vue`(활성 등록 81곳)의 `:225,229`에서 `?ing=Y`(진행중)·`?ing=N`(종료)로 진입한다. `donation-lnb_ali.vue`에도 메뉴가 있으나 그 컴포넌트는 사장이다(donation §1-3).
제목은 `기부하기 > 지역이벤트`지만 데이터는 `featured-mapper`(답례품 기획전)이라 gift에 배정한다.

### 2. 컨트롤러 엔드포인트

#### 2-1. `/api/item` (ItemController, 13 + 상세/목록 2)
프론트는 `modules/op.saleson.js` 래퍼를 거치므로 **래퍼 호출처**로 판정했다.

| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `GET /{itemUserCode}` | `getItem` | `item_tab-ali.vue`, `details-main`, `donation-main`, `totalsearch/index` | live |
| `GET /reviews` | `getItemReviewsForDetail` | `item_tab-ali.vue`, `details-main` | live |
| `GET /review/info/{code}` | `getItemReviewInfo` | `mypage/writeReview.html` | live |
| `POST /review/add-like/{id}` | `addItemReviewLike` | `item_tab-ali.vue` | live |
| `POST /wishlist` | `addToWishList` | 14개 화면(목록·상세·카테고리·이벤트·마을기업·지자체선택 등) | live |
| `POST /remove-wishlist` | `removeToWishList` | `details-main`, `event/seasonal`, `modules/goods.item.js` | live |
| `GET relation` | `viewItemRelations` | `item_tab-ali.vue`, `details-main` | live |
| `GET /qna` | `getItemQna` | `item_tab-ali.vue`, `details-main` | live |
| **`GET /restock`** | `getRestockNotice` | `item_tab-ali.vue`, `details-main` | live |
| **`POST /restock`** | `restockNotice` | `item_tab-ali.vue` | live |
| `GET /list/list-new` | `getItemsNew` | `goods/index-main`, `searchGoods-main`, `donation/list-select` | live |
| `GET /getUserCntrPoint` | (직접) | `items/details-main.html` | live |
| `GET /coupons` | `downloadItemCouponList` | `item_tab-ali.vue` | live (쿠폰은 [[coupon-feature-unused-hide-ui]]) |
| `POST /download-all-coupons` | `downloadAllItemCouponList` | `item_tab-ali.vue` | live (동상) |
| `POST /review` | (직접) | 확인필요 | |

#### 2-2. `/api/category` (7) — 전부 live
`updated-check`(`op.vue.js`), `''`(목록), `/current`, `/filter`, `/price-areas`, `/category-path`, `/best`

#### 2-3. `/api/categories/searchResult` — **죽은코드**
래퍼 `searchGoods`의 호출처가 **0곳**이다. 검색 화면 `goods/searchGoods-main.html`이 실제로 쓰는 것은 `getItemsNew`(`/api/item/list/list-new`) + `getCategoryPath` + 관심지자체 2종이다.

#### 2-4. `/api/catalog` (6) — **전부 죽은코드 후보**
`getCatalogYearNoList`, `getCatalogNewItem`, `getDsgnDonationItem`, `getLocgovFavItem`, `getCatalogSeasonalItem`, `getCatalogMainInfo`. 유일한 소비 화면이 §1-3의 고립된 `catalog/` 덩어리다.

#### 2-5. `/api/display` (7)
`best`, `md`, `new`, `group-best`, `lately`, `promotion`, `style-book` — 래퍼는 전부 존재. 개별 호출처는 메인화면(common 도메인) 소관이 섞여 있어 common 시트에서 함께 정리한다.

### 3. 매퍼 쿼리 (357 중 사장 34)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `item-mapper.xml` | `saleson.shop.item.ItemMapper` | 173 | **22** |
| `categories-mapper.xml` | `...categories.CategoriesMapper` | 52 | 2 |
| `catalog-mapper.xml` | `...catalog.CatalogMngMapper` | 34 | 1 (+§1-3이면 전량) |
| `display-mapper.xml` | `...display.DisplayMapper` | 30 | 0 |
| `featured-mapper.xml` | `...featured.FeaturedMapper` | 25 | 1 |
| `item-front-mapper.xml` | `...item.ItemFrontMapper` | 18 | 0 |
| `wishlist-mapper.xml` | `...wishlist.WishlistMapper` | 13 | 4 |
| `item-addition-mapper.xml` | `...item.ItemAdditionMapper` | 6 | **5 / 6** |
| `inquiry-mapper.xml` | `...inquiry.InquiryMapper` | 6 | 0 |
| **합계** | | **357** | **35** |

#### 3-1. 사장 쿼리의 성격 — 상품옵션·추가상품
`item-mapper` 사장 22건 중 **13건이 `ItemOption` 관련**이다: `getItemOptionCodeById`·`getItemOptionDefaultGroupList`·`getItemOptionGroupList`(XML 고아), `getItemOptionByItemOption`, `getItemOptionListByItemOptionIds(ForOrder)`, `getItemOptionListByItemOptions`, `getItemOptionListByWishlistHash`, `getItemListByOptionCode`, `updateItemOption`, `updateItemOptionStockScheduleDate`, `getItemAddOptionCountByParam`, `getItemAddOptionListByParam`.
`item-addition-mapper`는 **6개 중 5개가 사장**(추가상품 기능 자체가 미사용).
나머지: `deleteItemById`, `insertFullItem`, `insertItemListForExcel`, `mergeItemHits`, `updateItemHitsAll`, `updateItemStockScheduleDate`, `getItemCountByDeliveryChargeId`, `getItemCountByDeliveryId`, `getItemListByOpenMarketItemParam`(오픈마켓 연동 잔재)
`wishlist` 사장 4건: `deleteWishlistById`, `getWishlistById`, `getWishlistDuplicate`, `updateWishlist`(XML 고아)

> 원제품(SalesOn)의 일반 커머스 기능(상품옵션·추가상품·오픈마켓·재고입고예정)이 고향사랑e음 답례품에서는 쓰이지 않는다는 뜻이다. point의 `OP_POINT` 체계, `starpoint-mapper.xmlx`와 같은 계열의 잔재다.

### 4. MSA 대응 요약

MSA gift는 컨트롤러 다수 / 엔드포인트 약 150개로 **대부분이 운영관리 API**다(카테고리 3계층·팀그룹·기획전·랭킹·스타일북·모바일카테고리편집·판매자·브랜드·리뷰관리).

**대응있음**
- `goods/index-main` → `GET /`, `list.html`
- `goods/searchGoods-main` → 검색(9파일에 구현)
- `items/details-main` + `item_tab-ali.vue` → `GET /gifts/{itemId}`, `detail.html`
- `category/index` → `GET /api/categories`
- `community-business/communityList-main` → `GET /community-business`
- mypage `review`/`writeReview` → `GET /my/reviews`, `POST /gifts/{id}/reviews`
- mypage `inquiry`/`inquiryItem` → `GET /my/qna`, `/my/inquiries`, `POST /gifts/{id}/inquiries`
- mypage `favorItem`(관심상품) → `GET /wishlist`, `wishlist.html`

**재현누락**
1. **재입고 알림**(`GET/POST /api/item/restock`) — AS-IS 상세화면 탭에서 살아있다. MSA는 `restock`/`재입고` 참조 **0건**.
2. **지역이벤트**(`featured/eventList`·`eventDetail`·`detail`) — 살아있는 푸터에서 진입하는 사용자 기능인데, MSA `featured`는 **운영관리 API(`/api/admin/featured`)만** 있고 공개 화면이 없다.

**의도적축소 / 이미 결정된 것**
- 쿠폰(`/api/item/coupons`, `download-all-coupons`) — [[coupon-feature-unused-hide-ui]]
- 판매자 포털 — [[provider-portal-split-deferred]], **무인증 문제는 [[gift-seller-portal-unauthenticated]] (최우선 보안 과제)**

### 5. 조치 후보

**A. 보안 — 최우선**
- `gift-seller-portal-unauthenticated`: `sellerId` 쿼리파라미터를 신뢰해 임의 판매자로 등록·수정이 가능하다(쓰기 경로). 이번 인벤토리에서도 `GiftController.register/edit/discontinue`가 `@RequestParam Long sellerId`를 그대로 받는 것을 재확인했다.

**B. 재현누락**
1. 재입고 알림
2. 지역이벤트 사용자 화면

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `catalog/` 3화면 + `header_news.vue` + `/api/catalog/*` 6 + `catalog-mapper` (§1-3 확인 후 전량)
- `/api/categories/searchResult`
- `item-mapper` 22건(옵션 13 포함), `item-addition-mapper` 5/6, `wishlist-mapper` 4건
- `goods/index.html`·`searchGoods.html`·`items/details.html`은 래퍼라 사장이 아니다 — **실화면과 짝으로 봐야 한다**

**D. 확인 필요**
- `POST /api/item/review` 호출처
- `/api/display/*` 7종의 개별 호출처(메인화면 — common 시트에서)
- MSA 운영관리 API 대량분의 admin 도메인 배정

---

### 6. 조치 결과 (2026-09-10)

#### 6-1. **판매자 포털 무인증 해소** (보안 최우선)

`GiftController`의 제공자 화면 전체가 `sellerId`를 **쿼리파라미터·hidden input·화면 입력칸**으로 받아 그대로 신뢰했다. 값만 바꾸면 남의 답례품을 **등록·수정·판매중지·폐지·재고조정**하고 **남의 문의에 답변**할 수 있었다.

**서버가 신원을 결정하도록 전환**
- `GiftController.currentSellerId(request)` — GH_AUTH JWT의 userId → `OP_SELLER.MEMBER_USER_ID` → sellerId. `SellerPortalController`가 이미 쓰던 방식을 그대로 가져왔다.
- 적용 엔드포인트 9개: `GET/POST /register`, `GET/POST /gifts/{id}/edit`, `POST /gifts/{id}/discontinue`, `POST /gifts/{id}/stop`, `POST /gifts/{id}/stock`, `GET /my`, `GET /my/inquiries`, `POST /inquiries/{id}/answer`
- `sellerGate()` — 로그인 없으면 로그인으로, 로그인은 했으나 연결된 판매자가 없으면 안내 화면(어느 쪽인지 구분하지 않으면 로그인 무한루프가 된다).

**서비스 계층에도 소유권 검사** (컨트롤러를 우회해도 막히도록)
- `GiftService.stop(itemId, sellerId)` — **이전에는 소유권 검사가 아예 없었다**(`stop(itemId)`).
- `GiftService.adjustStock(itemId, sellerId, qty)` — 동상.
- `InquiryService.answer(inquiryId, sellerId, answer)` — 문의가 달린 답례품의 소유자만 답변 가능. 운영자 대리답변은 `answerAsOperator()`로 분리해 소유권 검사를 타지 않게 했다(운영관리 경로는 내부 시크릿으로 이미 보호됨).

**화면에서 신원 주장 제거**
`register.html`의 "제공자 ID" 입력칸, `my.html`·`inquiries.html`·`edit.html`의 `sellerId` hidden/입력/링크 파라미터를 모두 제거.

##### 검증
| 시나리오 | 결과 |
|---|---|
| 비로그인 `/my` | 302 → member 로그인 (`target=.../my`) |
| 로그인했으나 판매자 아님(userId 1002) | "연결된 판매자 정보가 없습니다" 안내 |
| 판매자 계정(userId 1014 ↔ seller 9001) | 답례품 관리 화면 정상 |
| **판매자 아닌 회원이 `sellerId=9001`을 실어 재고를 9999로 조작** | 차단, 재고 **46 유지** |
| **판매자 9001이 판매자 9002의 답례품(1001) 재고 조작** | `본인이 등록한 답례품만 재고를 조정할 수 있습니다.` |
| 동, 판매중지 | `본인이 등록한 답례품만 판매중지할 수 있습니다.` |
| 동, 폐지 | `본인이 등록한 답례품만 폐지할 수 있습니다.` |
| 대상 답례품 상태 | 재고 **30 / APPROVED 유지** |
| 정상 경로(본인 답례품 46→50) | 성공, 이후 46으로 원복 |

#### 6-2. 재입고 알림 (재현누락 → 구현)

AS-IS는 상세화면 탭에서 `GET /api/item/restock`으로 신청 여부를 확인하고 `POST`로 신청한다(`ItemController:850~896`). MSA는 `restock` 참조가 **0건**이었다 — 다만 **`OP_RESTOCK_NOTICE` 테이블은 이미 이관돼 있었다**(코드만 없던 상태).

- `RestockNotice` 엔티티 + `RestockNoticeRepository` + `RestockNoticeService`
- `POST /gifts/{itemId}/restock-notice`, 상세화면은 `restockRequested`로 버튼/완료문구를 가른다
- **품절(`SOLD_OUT='1'`)일 때만** 신청 가능, **중복 신청 차단** — AS-IS는 중복 검사가 없어 같은 신청이 계속 쌓였다. 여기서는 쌓지 않는다.
- 스키마: PK에 시퀀스 DEFAULT가 없어 INSERT가 실패했다. 다른 gift 테이블과 같은 관례로 `op_restock_notice_id_seq`를 만들고 `database/ddl/service-gift.sql`에도 반영했다.

##### 검증
품절 답례품에 버튼 노출 → 신청(302, DB 1행) → 재진입 시 "재입고 알림을 신청하셨습니다" → 중복 신청 차단 → **재고 있는 답례품에 신청 시 거절** → 비로그인 시 로그인 리다이렉트. 검증 데이터 삭제 확인.

#### 6-3. 미처리
- **지역이벤트 사용자 화면**(`featured/eventList`·`eventDetail`·`detail`) — 살아있는 푸터에서 진입하는 기능인데 MSA는 운영관리 API만 있다. 화면 3개 신규 작성이라 규모가 있어 별도 진행.
- 리뷰 좋아요(`POST /api/item/review/add-like/{id}`)
- `POST /api/item/review` 호출처, `/api/category/filter`·`price-areas` 대응, `/api/display/*` (common 시트에서)

#### 6-4. 지역이벤트 공개 화면 (재현누락 → 구현)

운영관리 등록 API(`/api/admin/featured`)는 이미 있었고 **공개 화면만 없었다**. 진입은 살아있는 푸터 `footer_ali.vue:225,229`의 `?ing=Y`(진행중)·`?ing=N`(종료)다.

- `FeaturedService` — AS-IS `EventController.list()`(`:88~97`)의 목록 조건을 옮겼다: `featuredType='1'` + `featuredFlag='Y'` + `displayListFlag='Y'`. AS-IS가 `conditionType='FRONT'`(진행중) / `progression='3'`(종료)로 갈랐던 것을 **시작·종료일 비교**로 표현했다(MSA `OP_FEATURED`에 `progression` 컬럼이 없다).
- `FeaturedController` — `GET /events?ing=Y|N`, `GET /events/{featuredUrl}`. 상세 진입 키는 AS-IS와 동일하게 `featuredUrl`.
- `events.html` / `event-detail.html` — 기존 `list.html`의 실제 카드 마크업(`goods-list-group` / `goods-list-items` / `img_frame` / `card_title`)을 그대로 따랐다.
- 이벤트 답례품 편성은 `OP_FEATURED_ITEM`의 `DISPLAY_ORDER` 순서를 유지한다(`findAllById`는 순서를 보장하지 않아 다시 정렬).
- **범위 제외**: AS-IS의 이벤트 댓글(`getEventReply`/`createEventReply`)은 게시판 성격이라 common 도메인(게시판류)에서 함께 다루는 것이 맞다.

#### 6-5. 상품평 좋아요 (재현누락 → 구현)

`OP_ITEM_REVIEW_LIKE` 테이블과 `OP_ITEM_REVIEW.LIKE_COUNT` 컬럼은 **이미 이관돼 있었고 코드만 없었다**(재입고 알림과 같은 상황). `Review` 엔티티에 `likeCount` 매핑조차 없었다.

- `ReviewLike` 엔티티 + 저장소 + `ReviewService.like(itemReviewId, userId, ip)`
- `POST /reviews/{itemReviewId}/like` (JSON), 상세화면 "도움돼요" 버튼
- **AS-IS 동작을 그대로**: 취소가 없는 **1회성**이다(`ItemServiceImpl:5523~5553`). 토글이 아니라서 이미 누른 상태면 `liked:false`를 돌려주고 아무 일도 하지 않는다 — 관심답례품(wishlist)의 토글과 다르다.
- 중복 판정은 **로그인이면 `USER_ID`, 비로그인이면 `IP`** (AS-IS와 동일). 비로그인도 누를 수 있다.

##### 검증 (6-4, 6-5)
| 확인 | 결과 |
|---|---|
| 이벤트 없음 | "진행중인 지역이벤트가 없습니다." |
| 진행중 목록 | 가을 지역이벤트만 |
| 종료 목록 | 봄 지역이벤트만 |
| 상세(`/events/autumn-fair`) | 이벤트명 + "진행중" 배지 |
| 답례품 편성 후 상세 | "이벤트 답례품" + 편성한 답례품명 |
| 리뷰 좋아요 (로그인) | `{"liked":true}` |
| 동, 중복 | `{"liked":false}` |
| 리뷰 좋아요 (비로그인, IP) | `{"liked":true}` |
| 동, 중복 | `{"liked":false}` |
| `LIKE_COUNT` 집계 | 2 (로그인 1 + 비로그인 1) |

작업 중 발견: `OP_ITEM_REVIEW_LIKE.ID`에도 시퀀스 DEFAULT가 없어(재입고 알림과 같은 문제) `op_item_review_like_id_seq`를 만들고 `database/ddl/service-gift.sql`에 반영했다.

검증 데이터(이벤트 2, 편성 2, 좋아요 2, LIKE_COUNT)는 전부 삭제·원복 확인.

---

## 6. order

> 통합 전 파일: `docs/as-is-inventory-order.md`

## AS-IS 인벤토리 — order 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

### 1. 화면 생사 판정

order 도메인은 화면이 적고 **전부 live**다. 다른 도메인과 달리 백업/날짜본이 `old/`로만 분리돼 있다.

| AS-IS 화면 | inbound | MSA |
|---|---|---|
| `cart/index.html` (장바구니) | 160 | `GET /cart` |
| `order/step1.html` (주문/결제) | 8 | `POST /checkout` |
| `order/step2.html` (주문완료) | 13 | `GET /checkout/done` |
| `mypage/orderList.html` | 28 | `GET /my` |
| `mypage/orderDetail.html` | 4 | `GET /orders/{orderId}` |
| `mypage/orderCancel.html` | 12 | `GET /claims/my` |
| `mypage/deliveryInfo.html` | 9 | member `/delivery` (배송지 관리는 member 배정) |

죽은코드: `cart/old/index_old.html`, `order/old/{no-member_saleson, step1_saleson, step2_saleson}.html`
> 파일명의 `_saleson` 접미사가 원제품 원본이라는 표시다 — point의 `starpoint-mapper.xmlx`, gift의 옵션 쿼리와 같은 계열의 잔재.

**클레임 팝업은 화면이 아니라 컴포넌트다**: `components/ui/modal-order_cancle.vue`(취소), `modal-return.vue`(반품), `modal-exchange.vue`(교환). 목록·상세에서 `getCancelPop`/`getReturnPop`/`getExchangePop`으로 팝업을 열고, 각 모달이 `cancelProcess`/`returnProcess`/`exchangeProcess`로 제출한다.

### 2. 컨트롤러 엔드포인트

#### 2-1. `/api/cart` (5)
| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `GET ''` | (직접) | `cart/index.html` | live |
| `POST /add` | `addToCart` | **17개 화면**(목록·상세·카테고리·이벤트·마을기업·지자체선택·제철식품 등) | live |
| `POST /delete` | `deleteCart` | `cart/index.html`, `components/ui/cart.vue` | live |
| `POST /update-quantity` | `updateCartQuantity` | `cart/index.html` | live |
| `POST shipping-payment-type` | `updateShippingPaymentType` | **없음** | **죽은코드** |

#### 2-2. `/api/order` — 결제 경로가 두 벌, 살아있는 것은 하나
| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `POST /buy` | `buyOrder` | `cart/index.html:652`, `items/details-main.html`(바로선택), `item_tab-ali.vue`, `users/*`(주문 후 로그인 복귀) | live |
| `POST /payment-step` | `paymentStep` | `order/step1.html:836` | live |
| **`POST /giveGoodsSavePay`** | `giveGoodsSavePay` | `order/step1.html:1836` | **live — 실제 주문저장/결제** |
| `GET /detail` | `getOrder` | `orderDetail`, `writeReview`, `order/step2` | live |
| `POST /confirm-purchase` | `confirmPurchase` | `orderDetail`, `orderList` | live |
| `POST /shpping-complete` | `shppingComplete` | `orderDetail`, `orderList` | live |
| `GET /cancel-apply` | `getCancelPop` | `orderDetail`, `orderList` | live |
| `POST /cancel-apply` | `cancelProcess` | `modal-order_cancle.vue` | live |
| `GET /return-apply` | `getReturnPop` | `orderDetail`, `orderList` | live |
| `POST /return-apply` | `returnProcess` | `modal-return.vue` | live |
| `GET /exchange-apply` | `getExchangePop` | `orderDetail`, `orderList` | live |
| `POST /exchange-apply` | `exchangeProcess` | `modal-exchange.vue` | live |
| `POST /refund-amount` | `getRefundAmount` | `modal-order_cancle.vue` | live |
| **`POST /save`** | `orderSave` | **없음** | **죽은코드** |
| **`POST /pay`** | `pay` | **없음** | **죽은코드** |
| **`POST /cancel`** | `orderCancel` | **없음** | **죽은코드** |
| `GET /coupons` | `getCoupons` | **없음** | **죽은코드** ([[coupon-feature-unused-hide-ui]]와 일관) |
| `POST /redirect-pay` | (래퍼 없음) | **없음** | **죽은코드** |
| `GET /easypay/easypay_request` | — | **없음** | **죽은코드** (원제품 PG) |
| `GET /naverpay/payment` | `naverApiPayment` | **없음** | **죽은코드** (원제품 PG) |

> **`save`/`pay`는 원제품(SalesOn)의 일반 커머스 결제 경로이고, 고향사랑e음이 실제로 쓰는 것은 `giveGoodsSavePay`("답례품 저장+결제")다.** donation의 `/api/ngdonation` vs `/api/regiontax`, point의 `OP_POINT` vs 기부포인트와 **완전히 같은 구조** — 원제품 경로가 남아 있고 고향사랑e음 전용 경로가 실사용된다.

### 3. 매퍼 쿼리 (271 중 사장 21)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `order-mapper.xml` | `saleson.shop.order.OrderMapper` | 115 | **14** |
| `order-claim-apply-mapper.xml` | `...order.claimapply.OrderClaimApplyMapper` | 57 | 2 |
| `order-shipping-mapper.xml` | `...order.shipping.OrderShippingMapper` | 36 | 1 |
| `cart-mapper.xml` | `saleson.shop.cart.CartMapper` | 19 | 1 |
| `order-payment-mapper.xml` | `...order.payment.OrderPaymentMapper` | 17 | 2 |
| `order-refund-mapper.xml` | `...order.refund.OrderRefundMapper` | 9 | 0 |
| `order-add-payment-mapper.xml` | `...order.addpayment.OrderAddPaymentMapper` | 7 | 1 |
| `claim-mapper.xml` | `saleson.shop.claim.ClaimMapper` | 6 | 0 |
| `deliveryhope-mapper.xml` | `...deliveryhope.DeliveryHopeMapper` | 5 | 1 |
| **합계** | | **271** | **22** |

(`order-give-point-mapper` 16건은 point 도메인에서 다뤘다)

#### 3-1. 사장 쿼리의 성격 — 배치 취소/반품 경로
`order-mapper` 사장 14건 중 **6건이 `...ForCancelBatch`/`...ForReturnBatch` 계열**이다: `insertOrderItemForCancelBatch`, `insertOrderItemForReturnBatch`, `insertOrderShippingForCancelBatch`, `updateOrderItemForCancelBatch`, `updateOrderItemForReturnBatch`, `updateOrderShippingForCancelBatch`. `updateCancelStatus`·`updateReturnStatus`도 호출 0이다.
→ **배치로 일괄 취소/반품하던 경로가 통째로 죽어 있다.** 실제 클레임 처리는 `OrderClaimApplyServiceImpl`이 건별로 돈다.

나머지: `insertOrderCancelTarget`(XML 고아), `deleteOrderItemSetBuyTemp`, `getActiveItemsByParam`, `getOrderCodeCountForOrderTemp`, `getOrderExchangeApplyByOrderItemId`, `updateOrderPayment`
`order-payment` 사장 2건은 **무통장 입금대기 배치**(`getWaitingDeposit*ForBatch`) — 고향사랑e음은 포인트 결제라 무통장이 없다.
`order-claim-apply` 사장 2건: `getReturnShippingNumberCheck`, `updateReturnStatus`

### 4. MSA 대응 요약

MSA order는 컨트롤러 15개 / 엔드포인트 약 80개. 쿠폰(`CouponApiController` 20 + `CouponMy*` 9)이 큰 비중을 차지하는데, 쿠폰은 [[coupon-feature-unused-hide-ui]]로 **화면 진입점만 숨긴 상태**다(AS-IS `/api/order/coupons`도 호출 0이라 판정이 일치한다).

**대응있음**
- 장바구니 4종 → `/cart`, `/cart/items`, `/cart/items/{id}/quantity`, `/cart/items/delete` (+ API 변형)
- 주문/결제 → `POST /checkout`(검토) → `/checkout/complete`(확정) → `/checkout/done`(완료). AS-IS `paymentStep` → `giveGoodsSavePay` → `step2`와 3단계 구조가 대응한다.
- 주문 목록·상세 → `GET /my`, `GET /orders/{orderId}`
- 클레임 3종(취소/반품/교환) → `POST /orders/{orderId}/claim` + `claimType`, 조회 `GET /claims/my`
- 운영 클레임 처리 → `/claims`, `/claims/{id}/approve|reject|complete` (+ `/api/admin/claims/*`)
- 구매확정 → `POST /orders/{orderId}/confirm-receipt`
- 송장·배송상태 → `POST /orders/{orderId}/invoice`, `/delivery-status`
- 배송지 변경 → `POST /orders/{orderId}/delivery-address`

**의도적축소**
- PG 연계 전부(`easypay`, `naverpay`, `redirect-pay`) — AS-IS에서도 이미 호출 0이라 **사실상 이식 대상이 아니다**
- 무통장 입금대기 배치

**확인필요 / 미해결**
- **`shpping-complete`(배송완료) 대응**: MSA는 `POST /orders/{orderId}/delivery-status`로 운영자가 바꾸는데, AS-IS는 회원이 목록·상세에서 직접 호출한다(`orderList`/`orderDetail`). 주체가 다르다.
- **자동 구매확정**: [[cart-review-remaining-tasks]]에 기록된 order 3-1 "배송완료 자동처리·자동 구매확정" — 정책 결정 대기.
- `getRefundAmount`(환불예상액 계산) 대응 — MSA 클레임에 환불액 미리보기가 있는지
- `POST /api/cart/shipping-payment-type`(배송비 결제방식) — AS-IS도 죽은코드라 이식 불요

### 5. 조치 후보

**A. 확인 필요**
1. 배송완료 처리의 **주체**(회원 vs 운영자) — AS-IS는 회원이 누른다
2. 환불예상액 계산(`refund-amount`) 대응
3. 자동 구매확정 정책 (기존 보류 항목)

**B. 이식 금지 — AS-IS에서 이미 죽은 것**
- `/api/order/{save, pay, cancel, coupons, redirect-pay}`, `easypay`, `naverpay`
- `/api/cart/shipping-payment-type`
- `order-mapper` 배치 취소/반품 6건 + 8건, `order-payment` 무통장 배치 2건
- `cart/old/*`, `order/old/*_saleson.html`

---

### 6. 조치 결과 (2026-09-10)

#### 6-1. AS-IS 주문상태 규칙 규명

`mypage/orderList.html:285~345`의 버튼별 `v-show` 조건에서 상태 전이 규칙 전체가 나온다.

| 주문상태 | 의미 | 가능한 액션 |
|---|---|---|
| 10, 20 | 결제완료 / 배송준비 | **주문취소** |
| 30 | 배송중 | **배송완료**(회원이 누른다) |
| 35 | 배송완료 | **구매확정**, **교환신청**, **반품신청** |
| 55 | 교환배송중 | 교환배송완료 |
| 58 | 교환배송완료 | 구매확정 |
| 40 | 구매확정 | 후기작성 |

교환·반품에는 조건이 더 붙는다: `itemReturnFlag == 'Y'`(반품가능 상품) + `mobileItemYn == 'N'`(모바일교환권 제외).
목록 상단 안내(`:180`)가 이 설계를 한 줄로 요약한다 — **"배송중인 답례품은 교환/반품이 불가합니다. 배송완료 버튼 클릭 후 신청 가능합니다."**

즉 AS-IS의 "배송완료"는 배송사 상태 갱신이 아니라 **구매자의 수령 확인**이고, 그것이 교환/반품의 관문이다.

#### 6-2. 발견한 갭 3건과 조치

MSA는 `orderStatus`(PENDING/CONFIRMED/CANCELLED)와 `deliveryStatus`(SHIPPED/IN_TRANSIT/DELIVERED/CONFIRMED) **두 축**을 쓰는데, 전이 검증이 `orderStatus`만 보고 있었다.

| # | 갭 | 조치 |
|---|---|---|
| 1 | **발송 후에도 주문취소가 됐다** — `cancel()`이 `orderStatus == CONFIRMED`만 확인했다. 이미 나간 물건의 재고·포인트가 보상 트랜잭션으로 되돌아간다. | `OrderService.isShipped()` 추가 → 발송 이후 취소 차단 |
| 2 | **배송 전에도 반품/교환이 됐다** — `ClaimService.request()`가 배송상태를 안 봤다. | `OrderService.isDelivered()` → 배송완료 이후에만 신청 가능 |
| 3 | **회원이 배송완료를 누를 수 없었다** — MSA는 운영자용 `/delivery-status`만 있었다. AS-IS의 관문이 통째로 빠져 있어, 2번을 고치면 회원이 반품을 신청할 방법 자체가 사라진다. | `OrderService.markDelivered(orderId, userId)` + `POST /orders/{id}/mark-delivered` (+ API 변형). 운영자용과 달리 **본인 주문만**, 목적지는 배송완료로 고정 |

화면도 AS-IS 조건에 맞췄다(`detail.html`): 주문취소는 발송 전에만, 배송완료 버튼은 `SHIPPED`/`IN_TRANSIT`일 때만, 반품/교환 폼은 `DELIVERED` 이후에만. 배송중에는 AS-IS와 같은 안내 문구를 띄운다.

> 2번과 3번은 **한 묶음**이다. 3번 없이 2번만 넣었으면 반품 경로가 막혀버린다 — 상태 전이를 고칠 때 그 상태로 가는 수단이 있는지 함께 봐야 한다.

#### 6-3. 환불예상액 (`refund-amount`)

AS-IS의 이 API는 **PG 환불 계산**이 본체다 — `ConfigPg`/`pgType` 분기, `OrderPgData`, kspay 현금영수증, 카드 부분취소(`partCancel`) 수량·금액 계산. MSA는 **포인트 단일 결제**라 그 계산 구조 자체가 성립하지 않는다(환불액 = 결제 포인트 그대로).

그래서 API를 옮기는 대신 **사용자 가치만** 재현했다 — 반품/교환 폼 위에 "반품 승인 시 N P가 복원됩니다. (교환은 포인트 복원 없이 재발송됩니다.)"를 표시한다. 결제 포인트는 이미 결제정보 블록에 있어 별도 조회가 필요 없다.

#### 6-4. 검증
order 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 발송 전(`delivery_status` null) 주문에 반품신청 | `배송완료된 주문만 반품/교환을 신청할 수 있습니다...` |
| `SHIPPED` 상태에서 주문취소 | `이미 발송된 주문은 취소할 수 없습니다. 반품/교환을 신청해 주세요.` |
| 배송중 → 회원이 배송완료 | 204, `delivery_status = DELIVERED` |
| 배송완료 후 반품신청 | 204, 클레임 `REQUESTED` 생성 |
| **타인이 배송완료 시도** | `본인 주문만 배송완료 처리할 수 있습니다.` |

검증에 쓴 주문(`O202609101029031357`)의 상태와 생성된 클레임은 원복·삭제 확인.

#### 6-5. 미처리
- **자동 구매확정 / 배송완료 자동처리** — [[cart-review-remaining-tasks]] order 3-1. AS-IS 배치 존재 여부 확인 후 정책 결정 필요.
- 교환·반품의 추가 조건(`itemReturnFlag`, `mobileItemYn`) — MSA `Gift`에 "반품 가능 여부" 플래그가 있는지 확인 후 적용.

#### 6-6. 교환·반품의 상품 조건 (`itemReturnFlag` / `mobileItemYn`)

두 컬럼 모두 **MSA에 이미 이관돼 있었다** — `op_item.item_return_flag`(DDL 주석 "반품 가능여부 (Y/N)", 기본 `Y`), `op_item.mobile_item_yn`("모바일상품여부", 기본 `N`). `Gift` 엔티티에는 `itemReturnFlag`만 매핑돼 있고 `mobileItemYn`은 빠져 있었으며, 둘 다 **어디서도 읽지 않았다**.

AS-IS는 교환·반품 버튼을 `itemReturnFlag == 'Y' && mobileItemYn == 'N'`일 때만 보여준다(`mypage/orderList.html:318,321`). 모바일교환권은 실물 배송이 없어 반품 대상이 아니다.

- gift: `Gift.mobileItemYn` 매핑 추가, `GiftItemInfoDto`에 두 플래그 포함
- order: `GiftItemInfo.returnable()` — `itemReturnFlag == 'Y' && mobileItemYn != 'Y'`. **값이 없으면 DDL 기본값대로 가능으로 본다.**
- `ClaimService.request()`가 RETURN/EXCHANGE일 때 검사, `OrderController.detail()`이 화면에서 폼을 숨김(조회 실패 시엔 폼을 보여주고 실제 차단은 신청 시 — 표시 실패로 기능을 막지 않는다)

#### 6-7. 이 과정에서 잡은 자기 실수 — 취소에까지 배송완료 조건이 걸려 있었다

§6-2에서 넣은 배송완료 조건이 `ClaimService.request()` 진입부에 있어 **`claimType`과 무관하게 모든 클레임에 적용**됐다. AS-IS는 정반대다 — **주문취소는 발송 전(10·20)에만, 교환·반품은 배송완료(35) 후에만** 가능하다. 그대로 뒀으면 클레임 경로의 주문취소가 영영 불가능했다.

유형별로 분기하도록 고쳤다:
```
RETURN/EXCHANGE → isDelivered(order) 필수 + gift.returnable() 필수
그 외(CANCEL)   → isShipped(order)이면 거부  (상품 플래그는 보지 않음)
```

> MSA는 취소 경로가 둘이다 — `OrderService.cancel()`(전용)과 `ClaimService.request(CANCEL)`(클레임). AS-IS는 취소도 클레임(`/api/order/cancel-apply`)으로 처리하므로 **둘 다 같은 규칙**이어야 한다. 한쪽만 고치면 규칙이 갈린다.

#### 6-8. 검증 (6-6, 6-7)
| 확인 | 결과 |
|---|---|
| gift API 응답 | `"itemReturnFlag":"Y","mobileItemYn":"N"` |
| 배송완료 + 반품가능 상품 → RETURN | 204 |
| `item_return_flag='N'` → RETURN | `이 답례품은 교환·반품이 불가합니다.` |
| `mobile_item_yn='Y'` → EXCHANGE | 동일 차단 |
| 반품불가 상품의 주문상세 화면 | 반품/교환 폼 블록 **미노출**(0개) |
| 원복 후 | 폼 블록 **노출**(1개) |
| **발송 전 + 반품불가 상품 → CANCEL** | **204 (상품 조건 미적용)** |
| 발송 후 → CANCEL | `이미 발송된 주문은 취소할 수 없습니다...` |

검증에 쓴 주문 상태·클레임과 답례품 플래그는 전부 원복 확인.

---

## 7. point

> 통합 전 파일: `docs/as-is-inventory-point.md`

## AS-IS 인벤토리 — point 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

### 0. 이 도메인의 핵심 — AS-IS에는 "포인트 원장"이 없다

AS-IS에는 **서로 무관한 포인트 체계가 두 개** 있고, 고향사랑e음이 실제로 쓰는 것은 두 번째다.

| | 원제품(SalesOn) 포인트 | **기부포인트 (실사용)** |
|---|---|---|
| 테이블 | `OP_POINT`, `OP_POINT_USED`, `OP_POINT_CONFIG` | `G_CNTR.CNTR_POINT` + `G_CNTR_USE_POINT` |
| 모델 | 만료일별 **버킷(lot)** + 사용이력 | **원장 없음** — 기부 레코드의 컬럼과 사용 테이블을 그때그때 **집계** |
| 적립 진입 | `PointServiceImpl.earnPoint(mode, point)` | 기부 완료 시 `G_CNTR.CNTR_POINT`에 기록 |
| 잔액 | `OP_POINT` 잔량 합 | `CNTR_POINT − SUM(G_CNTR_USE_POINT)` **계산값** |
| 만료 | `expirationPoint()` 배치 | `G_CNTR.POINT_END_DE` 기준 `GivePointExpirationServiceImpl` |

`earnPoint`의 실제 호출처는 **4곳뿐이고 전부 원제품 기능**이다 — `"join"`(회원가입 포인트, `UserServiceImpl:530,575`), `"admin"`(운영자 수동적립, `UserManagerController:1399`), `"return"`(주문취소 환급, `OrderClaimApplyServiceImpl:1130` / `OrderServiceImpl:5495`), `"review"`(상품평 포인트, `ItemServiceImpl:2188`).
→ **기부 완료 적립은 `earnPoint`를 타지 않는다.**

> `"review"` 모드는 `earnPoint`의 `if/else` 분기에 **없다**. `join`/`admin`/`return` 어디에도 해당하지 않아 기본 경로로 떨어진다 — 적립은 되지만 mode별 처리(사유 문구·관리자ID 기록)를 받지 못한다.

#### 0-1. MSA의 원장 재설계는 RFP 요건이다
MSA는 `PT_POINT_LEDGER`(원장) + `PT_POINT_BALANCE`(잔액) + `PT_LOCGOV_POINT_RATE`(지자체별 적립률) 3종으로 **재설계**했다. AS-IS 테이블(`op_point*`, `g_cntr_use_point`)은 이관돼 있으나 **자바 참조 0**이다.

이 재설계는 [[scope-migration-not-greenfield]] 위반이 아니다 — RFP **SFR-004**가 명시 요구한다:
- `requirements.md:58` "포인트 **거래 원장과 회원별 잔액 테이블을 분리**하여 관리"
- `:57` "기부금액의 **30%를 기준**으로 지자체별 적립 규칙 적용 (지자체마다 다름 — **하드코딩 금지, DB 관리 필수**)"
- `:59` "적립, 차감(주문결제), **예약/예약해제**, 소멸 처리, 오입금취소/반품에 따른 복원·조정"
- `:61` "거래내역·잔액 조회, 지자체별 사용현황, **만료 예정 포인트 안내**를 CQRS 기반 ReadModel로 제공"
- ISP `:160` "포인트적립/예약/사용/해제/환불/소멸" → 산출물 "포인트잔액/**원장**/만료뷰"

> [[point-reservation-unused-decision-deferred]]에 이미 기록된 대로 예약 기능은 RFP·ISP 양쪽에 있는 정식 신규 요건이며, **(a) 체크아웃 재배선 / (b) 틀만 유지하고 회원화면은 감춤** 중 어느 쪽인지는 보류된 제품 결정이다. 이번 대조에서 근거가 하나 더 붙었다 - RFP `:59`(예약/예약해제)뿐 아니라 `:61`(만료예정 안내 ReadModel)·ISP `:160`(산출물 "포인트잔액/원장/만료뷰")까지 같은 SFR-004 묶음이라, 원장·잔액·만료뷰를 이미 구현한 이상 예약만 빼는 것은 설계 일관성이 떨어진다. 판단은 사용자 몫이다.

### 1. 화면 생사 판정

| AS-IS 화면 | inbound | 판정 | MSA |
|---|---|---|---|
| `mypage/cntrPoint.html` (기부포인트 조회) | 12 | live | `/` (`my.html`) |
| `mypage/cntrPointDetail.html` (지자체별 상세) | 2 | live | `/detail` (`detail.html`) |

포인트를 **사용**하는 화면(장바구니·주문·답례품 상세)은 order/gift 도메인 소관.

### 2. 컨트롤러 엔드포인트

#### 2-1. 사용자 (`/api/mypage`)
| 엔드포인트 | live 호출처 | MSA | 판정 |
|---|---|---|---|
| `GET /getCntrPoint` | `cntrPoint.html`, `cntrPointDetail.html`, `orderList.html` | `GET /`, `/api/my/points`, `/api/balance` | 대응있음 |
| `GET /getCntrPointDetail` | `cntrPointDetail.html` | `GET /detail` | 대응있음 |
| `GET /points` (`/api/mypage/2/points`) | 래퍼만 있고 경로 불일치 | — | **죽은코드** (donation §2-3과 동일 원인) |

#### 2-2. 운영관리 (ghlove-web) — admin 배정 대상
`PointManagerController`, `GivePointManagerController`, `PointCheckManagerController` 3종. MSA는 `/api/admin/point-history`, `/api/admin/point-daily-stats`, `/api/admin/locgov-point-rate`, `/api/admin/credit-for-donation`로 일부 대응.

### 3. 매퍼 쿼리 (78 + 사장 19)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `point-mapper.xml` | `saleson.shop.point.PointMapper` | 47 | **9** |
| `order-give-point-mapper.xml` | `...order.givepoint.OrderGivePointMapper` | 16 | 0 |
| `give-point-mapper.xml` | `...give.givepoint.GivePointMapper` | 8 | 0 |
| `give-point-expiration-mapper.xml` | `...givepointexpiration.GivePointExpirationMapper` | 5 | 0 |
| `pointcheck-mapper.xml` | `...pointcheck.PointCheckMapper` | 2 | 0 |
| **`starpoint-mapper.xmlx`** | `jp.worldjb.web.StarPointMapper` | **19** | **19 (전부)** |
| **합계** | | **97** | **28** |

#### 3-1. `starpoint-mapper.xmlx` — 파일 확장자 때문에 통째 사장
`DatabaseConfig:87`의 스캔 패턴은 `classpath*:sqlmapper/<db>/*-mapper.xml`이다. 이 파일은 확장자가 **`.xmlx`**라 **MyBatis에 로드되지 않는다**. 자바 쪽 `StarPoint` 참조도 **0건**. 원제품(일본 SalesOn) 잔재다.

#### 3-2. `point-mapper.xml` 사장 9건
- **XML 고아**(인터페이스 미선언): `getPointConfigById`, `getPointConfigList`, `updatePointConfig`
- 호출 0: `getAvailablePointListByUserId`, `getExpirationPointSendMessage`, `getOrderReturnPointByParam`, `getOrderReturnPointTargetUsedHistoryByParam`, `getPointExpirationDateByPointId`, `getReturnPointListByParam`

> `getOrderReturnPoint*`/`getReturnPointListByParam`이 호출 0이라 "반품 포인트 회수가 안 도는가" 싶지만 아니다. 실제 경로는 `OrderClaimApplyServiceImpl.returnPoint()`(`:1120~1152`)이며 **적립 메서드를 재사용**한다 — `pointService.earnPoint("return", point)`. donation의 `getUserCntrLimit`와 같은 유형(쓰이지 않는 병행 구현).

### 4. 서비스 로직 대조

#### 4-1. 기부포인트 잔액 집계 (`getCntrPointInfo`, `mypage-mapper.xml:79~127`)
```sql
SELECT ... , CNTR_POINT - CNTR_USE_POINT AS CNTR_BLCE_POINT
FROM G_CNTR A JOIN G_LOCGOV C ...
WHERE A.CNTR_STTUS_CODE = '200' AND A.DELETE_AT = 'N'
  AND A.STTEMNT_PAY_DE IS NOT NULL       -- 납부일이 있어야 포인트로 인정
GROUP BY A.CNTR_LOCGOV_CODE
```
- 사용포인트: `G_CNTR_USE_POINT`를 **회원+지자체** 기준으로 합산(기부건 단위가 아니다)
- `USER_GRADE`: `G_HONOR_CNTRBTR` 존재 여부로 `'일반'`/`'명예기부자'` — **쿼리는 계산하지만 화면에서 쓰지 않는다**(`cntrPoint.html`에 표시 없음) → 사실상 죽은 컬럼
- `DETAIL`: 연계기관(`LINK_INSTT_CD`) 중 건수 1위를 뽑아 `"OOO 등 N건"`, 없으면 `'고향사랑e음'`

#### 4-2. 포인트 반환 (`processReturnPoint`, `PointServiceImpl:762~`)
AS-IS는 새 포인트를 발행하지 않고 **사용이력을 역순으로 되감는다**: `getPointUsedListByOrderCode(orderCode)`로 그 주문의 사용분을 가져와 `remainingPoint`를 복구하고 원본 lot을 되살린다.
MSA는 원장 모델이라 `RESTORE` 행을 추가하고 lot 잔량을 되돌린다 — 모델이 달라 형태는 다르지만 **결과(원래 lot으로 복원)는 같다**.

#### 4-3. 만료 정책
| | AS-IS | MSA |
|---|---|---|
| 기부포인트 만료 기준 | `G_CNTR.POINT_END_DE < 오늘` (`give-point-expiration-mapper.xml`) | `PT_POINT_LEDGER.EXPIRATION_DATE` (lot별) |
| 처리 | `updateCntrBlcePointDel` + **`insertCntrUsePoint`** — 소멸분을 **사용 이력으로 기록** | `EXPIRE` 원장행 추가 + 잔액 차감 |
| 원제품 포인트 만료 | `expirationPoint()` 일배치. **휴면회원도 대상**(회원상태코드를 조회에서 제외, `:365` 주석) | 해당 없음(원제품 체계 미사용) |
| 만료 예고 | `expirationPointSendMessage()` (`getExpirationPointSendMessage` 쿼리는 호출 0) | `/api/my/points` 만료예정 + `pt_rm_expiring_point` |
| 만료일 없을 때 | `PointUtils.getExpirationMonth()`가 0이면 **200년 후로 설정**("만료일 없으면 무제한 한 200년쯤 더해버려") | 확인필요 |

#### 4-4. **탈퇴 시 잔여 기부포인트 소멸 — 재현누락**
AS-IS `GeneralCustomerServiceImpl:286~296`은 회원 탈퇴 시 `getCntrBlcePointList(userId)` → `updateCntrBlcePointDel` + `insertCntrUsePoint`로 **잔여 기부포인트를 소멸 처리**한다(만료 배치와 같은 처리).
MSA는 탈퇴 화면에 잔여포인트를 **보여주기만** 하고(`PointApiController:114` `/api/locgov-point-summary`) 소멸시키지 않는다. → 탈퇴 회원의 포인트가 원장에 남는다.

### 5. 화면 대조

#### 5-1. `cntrPoint.html` → MSA `my.html`
| AS-IS 이벤트 | MSA | 판정 |
|---|---|---|
| `@change="paging"` | 페이징 있음 | 대응있음 |
| `@click="closeTableOverlap()"` (가로스크롤 안내 오버레이 닫기) | `onclick="this.parentElement.style.display='none'"` | 대응있음 |
| `@click="goToLocgovMall(data)"` (답례품 몰 가기) | `td.loc_mall` 버튼 | 대응있음 |

**표 열**
- AS-IS: No / 기부지자체(시·도, 시·군·구) / 적립 / 사용 / 잔여 / 기부처 / 답례품
- MSA: No / **기부연도** / 기부지자체 / **기부금액** / 적립 / 사용 / 잔여 / 기부처 / 답례품
→ MSA에 **기부연도·기부금액 2열이 추가**됐다. RFP/ISP 근거 확인 필요.

**기부처 열이 하드코딩이다** — MSA `my.html:128`은 항상 `고향사랑e음`을 출력한다. AS-IS는 `data.detail`이 비었을 때만 그 문구를 쓰고, 값이 있으면 `"<연계기관> 등 N건"`을 보여준다(`cntrPoint.html:127~129`). → **연계기관 경유 기부의 기부처 표시가 틀린다.**

#### 5-2. `cntrPointDetail.html` → MSA `detail.html`
| AS-IS 이벤트 | MSA | 판정 |
|---|---|---|
| `@change="paging"` | 있음 | 대응있음 |
| `@click="goToList()"` (목록으로) | 있음 | 대응있음 |
| `@click="goOrderInfo(data)"` (주문번호 → 주문상세) | 링크 없음 | **재현누락** |
| `@click="closeTableOverlap()"` | 있음 | 대응있음 |

**표 열**
- AS-IS: No / 발생일자 / 기부액(실납부액) / 적립포인트 / 사용포인트 / **잔여포인트** / 답례품 주문번호
- MSA: No / 발생일자 / **구분** / 기부액(실납부액) / 적립포인트 / 사용포인트 / 답례품 주문번호
→ MSA는 **잔여포인트 열이 없고** 원장 모델이라 **구분(txnType)** 열이 추가됐다.

### 6. 조치 후보

**A. 재현누락**
1. **탈퇴 시 잔여 기부포인트 소멸**(§4-4) — 회계상 남으면 안 되는 잔액이다. 가장 시급.
2. **기부처 열 하드코딩**(§5-1) — 연계기관 표시.
3. `cntrPointDetail`의 주문번호 → 주문상세 링크.
4. `cntrPointDetail`의 잔여포인트 열.

**B. 근거 확인 필요**
- MSA `my.html`의 추가 2열(기부연도·기부금액)
- MSA `detail.html`의 구분 열 (원장 모델상 필요해 보이나 RFP 근거 확인)
- `STTEMNT_PAY_DE IS NOT NULL`(납부일 있어야 포인트 인정)이 MSA의 완료상태 판정과 등가인지
- 만료일 미설정 시 AS-IS의 "200년 후" 처리에 대응하는 MSA 동작

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `starpoint-mapper.xmlx` 19쿼리(확장자 탓에 미로딩, 자바 참조 0)
- `point-mapper` 사장 9건
- `/api/mypage/2/points`
- `USER_GRADE` 계산(쿼리만 있고 화면 미사용)
- 원제품 포인트 체계 전체(`OP_POINT`/`OP_POINT_USED`/`OP_POINT_CONFIG`) — 회원가입·상품평·관리자적립 포인트는 고향사랑e음 기능이 아니다

**D. 갱신할 판단**
- [[point-reservation-unused-decision-deferred]] — 예약 기능은 RFP SFR-004·ISP 명시 요건. 숨김이 아니라 **체크아웃 재배선**이 맞다.
- [[point-readmodel-paused-pending-db-design]] — "만료 예정 포인트 안내 ReadModel"도 SFR-004 명시 요건.

---

### 7. 조치 결과 (2026-09-10)

#### 7-1. 완료 (4건)

**1. 탈퇴 시 잔여 기부포인트 소멸** (§4-4)
AS-IS `GeneralCustomerServiceImpl:286~296`은 탈퇴 처리 안에서 만료 배치와 **똑같은 소멸**을 돈다. MSA는 탈퇴 화면에 잔여를 보여주기만 하고 소멸시키지 않아, 탈퇴 회원의 포인트가 원장에 살아 있었다.
- point: `PointService.expireAllOnWithdrawal(userId)` — 만료 배치와 같은 모양으로 `EXPIRE` 원장행을 남기고 잔액을 깎는다(사유 문구만 "회원 탈퇴에 따른 소멸"로 구분). `POST /api/admin/expire-on-withdrawal`.
- member: `PointClient.expireAllOnWithdrawal()` — **조회 실패를 0으로 삼키는 다른 메서드와 달리 예외를 그대로 올린다.** 소멸에 실패했는데 탈퇴만 진행되면 잔액이 남는다.
- **탈퇴 두 경로 모두**에 걸었다: `MemberService.withdraw`(본인), `AdminMemberService.adminWithdraw`(강제).
- 배선 중 발견: member의 `PointClient`가 `X-Internal-Secret`을 보내지 않아 point의 `/api/admin/*`에서 401이 났다(donation의 `MemberClient`는 보내고 있었다). 헤더를 추가했다.

**2. "기부처" 열 하드코딩 제거** (§5-1)
MSA `my.html:128`이 항상 `고향사랑e음`을 출력하던 것을 AS-IS `DETAIL` 계산으로 교체했다.
- donation: `Donation.linkInsttCd` 필드 추가(컬럼은 있었으나 **엔티티에 매핑돼 있지 않았다**), `DonationService.donationSourcesByLocgov(userId)` — 지자체별로 연계기관을 건수 내림차순으로 세어 1위를 고르고, 둘 이상이면 `"OOO 등 N건"`. `GET /api/donation-sources`.
- point: `LocgovClient.donationSourcesOf(userId)` — 표시용이라 실패 시 빈 맵(그러면 전 행이 `고향사랑e음`, 지금과 같은 모습). `PointLocgovRow.donationSource` 추가.
- AS-IS의 DETAIL 서브쿼리는 상태 필터가 없으므로 여기서도 상태로 거르지 않았다.

**3. 상세화면 주문번호 → 주문상세 링크** (§5-2)
AS-IS `cntrPointDetail.html:121`은 주문번호가 있을 때만 버튼으로 만든다. MSA에서 `REF_KEY`는 거래유형마다 의미가 달라(적립=기부번호, 사용/복원=주문번호) **`USE`/`RESTORE` 행에만** 링크를 건다.

**4. 상세화면 잔여포인트 열** (§5-2)
AS-IS는 SQL 상관 서브쿼리(`CURRENT_CNTR_POINT − CURRENT_CNTR_USE_POINT`)로 **그 시점까지의 누적 잔액**을 만든다. 원장 모델에서는 발생순으로 훑으며 더하면 같은 값이 나온다(`ledgerDetail()`이 이미 `createdDate` 오름차순). `PointDetailRow` 래퍼로 행마다 붙였다.

#### 7-2. 검증
member·donation·point 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 100만원 기부 → 적립 | `EARN 300000`, 잔액 300,000 |
| 탈퇴 실행 | `EXPIRE -300000` 원장행 생성, **잔액 0** |
| 연계기관 없음(현 데이터) | 전 행 `고향사랑e음` — AS-IS와 동일 |
| 연계기관 1종 주입 | `{"26350":"농협","46150":"우체국"}` → 화면에 기관명, 나머지는 `고향사랑e음` |
| 같은 지자체에 2종 | `{"46150":"농협 등 2건"}` → **"등 N건" 분기 동작** |
| 잔여포인트 누적 (원장 15행) | `450,000 → 465,000 → 385,000 → … → 338,500` — DB 원장과 일치 |

검증 데이터(회원 1, 기부 1, 포인트원장·잔액, 탈퇴스냅샷, 연계기관 공통코드 2건, `link_instt_cd` 2건)는 전부 삭제·원복 확인.

#### 7-3. 미처리 — 확인이 필요한 것
- **`STTEMNT_PAY_DE IS NOT NULL`** (납부일이 있어야 포인트로 인정)이 MSA의 `COMPLETED` 판정과 등가인지. AS-IS는 상태와 **별개로** 납부일 존재를 요구한다.
- MSA `my.html`의 추가 2열(기부연도·기부금액) RFP/ISP 근거.
- 만료일 미설정 시 AS-IS의 "200년 후" 처리에 대응하는 MSA 동작.
- `PointCheckManagerController`(포인트 점검) 대응.

---
name: static-asset-scoping-2026-09-18
description: "2026-09-18 SPA 정적에셋 꼬임 해결 - CSS 전역누수 라우트별 스코핑 + 누락이미지 144개 복사, storefront 전용(재기동 불필요) 시각확인 대기"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-18T05:14:30.284Z
---

**2026-09-18 착수.** "Vue3 전환 후 정적요소(css/js/이미지)가 다 꼬였다"는 관찰의 원인 규명·수정. 상세 = `docs/static-asset-audit-2026-09-18.md`.

**핵심 원인 2가지 + 수정:**
1. **CSS 전역 누수** — SPA가 화면 전용 CSS 30개를 `index.html`에 전역 로드 → 화면 간 bleed. AS-IS(MPA)는 페이지별로만 로드함. → `index.html`을 공통 base 9개만 남기고, 화면 전용 CSS는 `storefront/src/router/pageStyles.js`의 라우트→CSS 매핑으로 `beforeEach`에서 주입/회수. 매핑은 AS-IS 각 페이지 `<head>` 링크 목록이 정본.
2. **이미지 404 144개** — AS-IS `static/images` 일부만 `public`에 복사돼 아이콘·별점·체크박스 등 깨짐. AS-IS에서 참조된-누락 144개 복사 → 404가 158→14로 감소. 남은 14개(npay 스프라이트·cclogo·ico_depth1~3 등)는 **AS-IS 전체에도 없음 = 운영도 동일**, 무해.

**JS는 대부분 정상 폐기**(jQuery/Vue2/op.saleson→Vue3+fetch, swiper는 Vue 재구현). counterup/waypoints 애니메이션만 미재현(값은 정적표기, 기능결함 아님).

**홈 충실 재이식(2026-09-18)**: 슬라이더는 npm `swiper` 설치 후 AS-IS main.html `initSwiper` 설정 그대로(slidesPerView1/spaceBetween16/speed800/autoplay6s, **pagination type:'custom'+renderCustom** ← fraction이면 `.swiper-pagination-custom` 스타일 안 먹음, 주입 `.swiper-navigation-icon`은 :deep로 숨김-AS-IS는 `::after` mask-image 화살표). 검색바는 AS-IS `.search-bar.form-conts.btn-ico-wrap`+btn-group 마크업. **`site.css`가 옛 손수 마크업에 맞춘 오버라이드를 갖고 있어 AS-IS 마크업 복원 시 충돌**(검색 border 제거·버튼 절대배치 → 삭제함). AS-IS 마크업 복원할 때마다 site.css 오버라이드 동반 점검 필요. GNB는 라우트 변경 시 자동 닫힘(watch route).

**폰트는 이미 AS-IS와 동일**(`.btn-navi`=`--pc-font-small`=14px/400/Pretendard GOV, 토큰체인 new.css→@import output.css→@import krds_tokens.css 정상). 사용자가 본 `<button>`·폰트차이는 **캐시**였음. 미세 폰트/간격 차이는 **UI/UX 개선 단계로 보류**(사용자 결정 2026-09-18).

**에러페이지 AS-IS화(2026-09-18)**: (1) storefront에 `ErrorView.vue`(AS-IS error/404.html 그대로)+catch-all 라우트(meta.bare)+`error404.png`. 배너 클릭은 같은 오리진이면 `router.push`로 SPA 태워 catch-all→ErrorView. (2) **Whitelabel 근본**: 배너 linkUrl이 `http://localhost:8082/`(donation 백엔드)처럼 절대 URL이라 백엔드 도달→Spring Boot Whitelabel. → 대민 5개 서비스에 `src/main/resources/static/error/404.html`(AS-IS 404, base64 이미지 인라인) 배치, Boot가 404시 자동 서빙(보안필터 없음). **반영엔 서비스 재기동 필요.** 배너 데이터(잘못된 백엔드 절대URL)는 admin에서 정정 권고.

**결함 배치감사(2026-09-18)**: `docs/spa-static-defect-audit-2026-09-18.md`. 근본원인 = AS-IS CSS/JS가 jQuery/부트스트랩 런타임 의존(페이지별 CSS 로딩 + `.active`/`.show` 클래스 토글)인데 Vue3 전환이 화면마다 제각각 재현 → 잠복버그. **탭 버그 근본**: 부트스트랩 `.tab-content>.tab-pane{display:none}`(전역)을 `v-show`로는 못 이김 → 활성 패널에 `:class="{active}"` 필요(FindIdPwView·GiftDetailView 수정). **CSS 매핑 오류 일괄 교정**: orders/order-detail/claims(order_ali→mypage-order(-details)+order-modal), list-select(donation_liemt/selmt), gift-community/seasonal(evt_card), events/event-detail, honor-guide(+donation_doak), find-idpw(login-idse). 누락 CSS 3개(order-modal/mypage-order/mypage-order-details) 복사.

**header_g(답례품/장바구니 전용 헤더) 충실 재이식 완료(2026-09-18)**: `AppHeaderShopping.vue`(전체카테고리 메가메뉴 via `GET /gift/api/categories` + 제철/마을기업 링크 + 답례품 검색) + `MapSelectPopup.vue`(지자체 지도선택 - vtmap 이미지+17시도, 시군구는 `GET /donation/api/locgovs`를 upperLocgovCode로 그룹핑, 선택 시 `/gifts?locgovCode=`). `App.vue`가 쇼핑 라우트(gift-*/cart/events)에서만 AppHeader 아래 마운트(AS-IS는 header_ali+header_g 스택). 스타일은 전역 base(default_ali/layout/new)에 있음.

**header_g 메가메뉴 마무리(2026-09-18)**: 카테고리 응답 필드는 `name`/`children`(내가 처음 label/items로 오바인딩→수정). 그리고 `layout.css`의 `.main_lnb{display:none}`(전역)을 `v-show`로 못 이겨 안 열리던 것 → **인라인 `:style="{display:isOpen?'block':'none'}"`로 강제**(탭과 동일 근본버그: CSS display:none vs v-show/클래스토글). 검색창 흰배경 → AS-IS `input{background:transparent;color:#fff}` 스코프 스타일 추가. **미확인(개발데이터 대기)**: 소고기 등 서브카테고리 `children`이 테스트DB엔 `[]`라 품목(한우) 드릴다운 확인 불가 → 실개발데이터 받으면 확인. MSA 답례품 필터는 대분류·키워드만 지원(서브카테고리 코드 필터 없음, 데이터 후 검토). **미이식**: 제철식품관/마을기업관은 AS-IS가 2단계(키워드카드→상품)인데 MSA GiftListView는 상품그리드 단일 → 별도 재이식 대기.

**제철식품관/마을기업관 이식(2026-09-18)**: AS-IS 제철식품관은 2단계(월별 제철키워드 카드→그 달 상품). 백엔드 추가: `SeasonFood` 엔티티/`SeasonFoodId`/`SeasonFoodRepository`(G_SEASON_FOOD), `GiftService.seasonFoodKeywords()`(1~12월 키워드)·`seasonalGifts(month)`, `GET /api/season-food`+`/api/gifts?mode=seasonal&month=N`. 프론트: `SeasonalView.vue`(season_group 카드→goods-list-group, 스타일은 event.css의 .season_group), 라우트 gift-seasonal→SeasonalView. 마을기업관은 AS-IS도 단일 상품그리드라 GiftListView 그대로(데이터 0건이라 달라보였음). **데이터 생성(docker exec ghlove-postgres psql, gift DB)**: g_season_food 12개월 키워드, g_season_food_item 39건(월별 상품분산), op_seller 9007/9008/9012/9013 community_business_yn='Y'. 상품 이미지는 OP_ITEM_IMAGE에 대부분 존재. **gift(8084) 재기동해야 제철 API 반영**(마을기업은 데이터만이라 즉시 작동, 검증 4건). 개발데이터 오면 g_season_food/g_season_food_item 실데이터로 교체.

**사이트맵 닫힘(2026-09-18)**: `:target`(#modal_sitemap) 방식은 router 이동/replaceState로 브라우저가 :target을 갱신 안 해 안 닫힘 → **공유 상태 `composables/useSitemap.js`(sitemapOpen)로 전환**. AppHeader 전체메뉴버튼·AppFooter 링크가 openSitemap, 모달 `:class="{'is-open':sitemapOpen}"`+닫기버튼+nav클릭 closeSitemap, site.css `#modal_sitemap.is-open`.

**제철/마을기업 지역필터(2026-09-18)**: 백엔드 `seasonalGifts(month, locgovCode)`·`communityBusinessGifts(locgovCode)`에 locgov 필터 추가(matchesLocgov: 시도코드 끝000이면 앞2자리, 시군구면 정확매칭). 프론트 공용 `RegionSelect.vue`(시도 17개+시군구, /donation/api/locgovs 그룹핑, change로 코드 emit). SeasonalView에 `.loc_select_area`(월+RegionSelect+검색/초기화) 추가, Stage2는 "전체 N건"만(운영대로, 돌아가기 버튼 제거). 신규 `CommunityView.vue`(마을기업관 헤더+RegionSelect+상품그리드, gift-community-business 라우트 연결). **gift 재기동 필요**(locgov 필터·season API).

**공용 모달 + alert/confirm 전면 전환(2026-09-18)**: AS-IS op-modal($s.alert/$s.confirm) 대체 = `composables/useModal.js`(modalAlert/modalConfirm, 상태) + `components/GlobalModal.vue`(App.vue 상시 마운트). **모달은 운영과 동일한 순수 bootstrap 구조**(`.modal.show`+`.modal-backdrop.show`+`.modal-dialog-centered`+`.modal-content`; 커스텀 오버레이 CSS 금지 - 그게 크기·클래스 어긋남 원인이었음). `.modal-content`는 MSA bootstrap.css가 운영과 바이트동일(padding:10%/border-radius:20px). **앱 전체 `window.alert/confirm`·bare alert/confirm 85건을 25개 파일에서 modalAlert/`await modalConfirm`으로 전환**(confirm 함수는 async 필요). DonateView: 한글금액("기부금은 X원")=koreanAmount, 한도 축약("2천만")=koreanAmountShort(utils/koreanNumber.js), 적립예상 0원에도 표시. 주의: modalAlert는 non-blocking(window.alert와 달리 await 안하면 즉시 다음 줄 실행) - 성공후 이동/리로드 케이스는 필요시 await.

**상태**: `npm run build` clean. storefront 변경은 재기동 불필요(새로고침), 백엔드(gift 제철 API·static 에러페이지)는 **재기동 필요**. **몰아서 시각확인 대기**(사용자 요청): (1) 결함 배치수정 화면들 레이아웃 (2) 탭 전환(find-idpw·답례품상세) (3) 쇼핑 전용 헤더+메가메뉴+지도선택. 메가메뉴는 gift(8084), 지도선택은 donation(8082) 실행 필요.

관련: [[check-as-is-source-when-analyzing]](CSS 링크목록까지 AS-IS 대조 원칙), [[verify-screen-by-content-not-route]].

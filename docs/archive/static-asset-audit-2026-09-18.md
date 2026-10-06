# 정적 에셋 감사 (Vue3 SPA 전환 후) — 2026-09-18

Vue3 SPA 전환 후 "정적요소(css/js/이미지)가 다 꼬였다"는 관찰에 대한 원인 규명·수정 기록.
정본은 AS-IS(`C:\workspace\ghlove\ghlove-frontend`, MPA)의 페이지별 정적 로딩.

## 요약

| 영역 | 진단 | 조치 | 상태 |
|---|---|---|---|
| CSS 전역 누수 | SPA가 화면 전용 CSS 30개를 전 페이지에 전역 로드 → 화면 간 bleed | index.html을 공통 base만 남기고, 화면 전용 CSS는 라우트별 주입(pageStyles.js) | ✅ 수정 |
| 이미지 404 | AS-IS `static/images` 중 144개가 MSA `public`에 미복사 → 아이콘·별점·체크박스 등 깨짐 | AS-IS에서 참조된-누락 144개를 경로 보존 복사 | ✅ 수정 |
| 남은 이미지 404 | 14개(npay 스프라이트·cclogo·ico_depth1~3 등) | AS-IS 운영에도 없는 죽은 참조 → 무해, 미조치 | ➖ 정상(패리티) |
| JS(vendor/page) | jQuery·Vue2·op.saleson 등 대량 스크립트 | Vue3+fetch로 대체돼 정상 폐기. swiper/카운트업은 Vue로 재구현 | ✅ 정상 |
| 웹폰트 | Pretendard GOV | `public/fonts`에 존재·경로 정상. NotoSansKR 등 누락분 복사 | ✅ 정상 |

## 1. CSS 전역 누수 (bleed)

**원인**: AS-IS(MPA)는 화면마다 `<head>` CSS 링크를 다르게 큐레이션한다(예: 장바구니=order_ali, main·event 제외 / 답례품 상세=item만). SPA는 이를 전부 `index.html`에 전역 로드해, 서로 다른 화면용 규칙이 새어나가(bleed) 각 화면이 AS-IS와 다르게 렌더됐다.

**정본 확인** — AS-IS 페이지별 활성 CSS 목록(공통 base 제외):
- main: main, goods_card, event, joind-agf, map
- mypage/index: m_main, main, event / cntrList: mypage-status, notice-box, joind-agf, main, event
- cart: order_ali, joind-agf, map
- items/details: item, joind-agf, map / designated/details: designation, item
- login: authentication-modal, change-pw, event, joind-agf, login-total, main
- join: join-inf2, joind-ag, joind-agf, event, main
- donation-main: donation_doak, event, joind-ag, joind-agf, main, notice-box
- designated index: designation, event, joind-agf, main, map
- event/seasonList: event, evt_card, goods_card, joind-agf, map
- notice/faq: ct_nov, research-box, main, event / qna: data_v, main, event
- guide1: donation_guge, event, main, research-box / policy: donation_guge

**공통 base(전 페이지 = 전역 유지)**: bootstrap, swiper.min, common, new, default_ali, header-footer_ali, popup, layout (+ site.css = MSA 자체 오버라이드, 네임스페이스 O).

**수정**:
- `storefront/index.html` — base 9개만 남김
- `storefront/src/router/pageStyles.js` (신규) — 라우트명 → 화면 전용 CSS 매핑 + `applyPageStyles()`(head에서 `data-page-style` 링크를 목표 목록과 diff해 주입/회수)
- `storefront/src/router/index.js` — `beforeEach`에서 `applyPageStyles(to.name)` (auth 가드보다 먼저 등록)
- AS-IS `map.css` → MSA `lclgv-map.css`로 매핑. AS-IS `order-modal/mypage-order/mypage-order-details.css`는 MSA 미복사(별도 갭, 범위 밖).

## 2. 이미지 404 (144개 복사)

**원인**: SPA 전환 시 AS-IS `static/images`의 일부만 `public/images`로 옮겨져, 참조 550개 중 **158개가 404**. 대부분 `/images/icon/*`(체크박스·라디오·하트·별점·화살표·장바구니·페이지네이션·swiper 버튼), `/images/goods/*`(상품카드 장식·월별 배지·장바구니 썸네일), `/images/map/*`(지역지도 핀), 폰트 3개.

**수정**: AS-IS `static/images/**`·`common/fonts/**`에서 참조된-누락 **144개**를 경로 보존 복사. 재검증 결과 404가 158→14로 감소.

**남은 14개** (조치 안 함 — AS-IS 전체에도 없음 = 운영도 동일하게 미표시):
`btn_share_w, cclogo, ico_depth1~3(_on), icon_item_point_link, npay_sp_cart/icon/payment2/talk2/talk_text/zzim2`. CSS 내 죽은 참조로 판단.

## 3. JS / 슬라이더

AS-IS main.html이 로드하던 스크립트 대부분은 SPA에서 **정상 폐기**:
- Vue2/httpVueLoader/op.saleson/op.common/axios/babel/es6-promise/jquery 본체 → Vue3 + fetch로 대체
- swiper.min.js → 메인 배너는 라이브러리 없이 Vue로 재구현(HomeView: current/prev/next/toggleAutoplay). 상세 갤러리(GiftDetail=클릭 썸네일, DesignatedDetail)도 라이브러리 없이 처리
- counterup/waypoints(숫자 카운트업·스크롤 등장) → 미재현. 값은 정적으로 표기되므로 기능결함 아님(애니메이션만 없음). 필요 시 후속.
- netfunnel(트래픽 대기열) → 운영 인프라, 비주얼 무관

## 검증 방법 (런타임)
- dev 새로고침 후 각 화면에서: 체크박스/라디오/별점/하트/화살표/장바구니 아이콘이 보이는지, 페이지네이션·swiper 버튼 아이콘이 뜨는지
- 홈·장바구니·답례품상세·기부·마이페이지가 AS-IS와 레이아웃이 맞는지(전역 누수 제거 효과)
- 빌드: `npm run build` = clean(2026-09-18 기준)

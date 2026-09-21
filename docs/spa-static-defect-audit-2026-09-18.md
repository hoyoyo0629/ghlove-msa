# SPA 정적요소 결함 감사 & 배치 수정 — 2026-09-18

Vue3 SPA 전환 후 화면별로 계속 드러나는 정적요소 문제를, 하나씩 밟는 대신 **선제적으로 전 화면을 대조**해 결함을 모으고 한 번에 수정한다.

## 왜 계속 문제가 나오는가 (근본)
AS-IS CSS/JS는 **jQuery/부트스트랩 런타임에 의존**한다: 각 페이지가 **자기 CSS만** 로드하고, 탭/모달/슬라이더는 **JS가 `.active`/`.show`를 토글**해야 보인다. Vue3 전환 때 그 JS는 (맞게) 버렸지만, CSS가 기대하는 **클래스 토글·페이지별 CSS 로딩을 화면마다 제각각으로** 재현해서, 안 맞는 화면이 잠복 버그로 남았다. 지금 화면을 열 때마다 그게 드러나는 것.

대조 축 3가지:
1. **CSS 목록** — 라우트가 AS-IS와 동일 CSS를 로드하는가 (`pageStyles.js` vs AS-IS `<head>`)
2. **인터랙션 클래스 토글** — 탭/모달/아코디언이 `.active`/`.show`를 Vue가 붙이는가 (부트스트랩 `.tab-pane{display:none}`을 `v-show`로는 못 이김)
3. **마크업/클래스** — AS-IS와 같은 구조/클래스인가

---

## A. CSS 매핑 결함 (`storefront/src/router/pageStyles.js`)
AS-IS `map.css` = MSA `lclgv-map.css`. base(전역)=bootstrap/common/default_ali/header-footer_ali/layout/new/popup/swiper.

| 라우트 | 현재(잘못) | AS-IS 정본(page-specific) | 조치 |
|---|---|---|---|
| **find-idpw** | login-total(로그인용) | authentication-modal, event, **login-idse**, main | ✅ 수정완료(선행) |
| **orders** | order_ali(장바구니용) | **mypage-order**, order-modal, favo_info, main, event | 🔧 수정 |
| **order-detail** | order_ali, favo_info | **mypage-order-details**, order-modal, main, event | 🔧 수정 |
| **gift-detail** | item, joind-agf, lclgv-map | + **order-modal** | 🔧 추가 |
| **list-select** | donation_guge | **donation_liemt, donation_selmt**, goods_card, joind-agf, event, main, research-box | 🔧 수정 |
| **gift-community-business** | gift-list와 동일(main…) | event, **evt_card**, goods_card, joind-agf, lclgv-map (main 없음) | 🔧 수정 |
| **gift-seasonal** | gift-list와 동일 | event, **evt_card**, goods_card, joind-agf, lclgv-map | 🔧 수정 |
| **events**(고객 이벤트목록) | 제철식품 세트(goods_card…) | **evt_card, event** 만 | 🔧 수정 |
| **event-detail** | 제철식품 세트 | event, **evt_detail**, goods_card, main | 🔧 수정 |
| **honor-guide** | donation_guge, event, main, research-box | + **donation_doak** | 🔧 추가 |
| mypage(계열) | superset(m_main/favo_info/change-* 포함) | cntrList=event, joind-agf, main, mypage-status, notice-box | ⚠️ 무해(계열 공유). 유지 |
| checkout/claims-my | order_ali 근사 | (AS-IS 대응 페이지 불명확) | ⚠️ 근사 유지, 추후 확인 |

### 누락 CSS 파일 (AS-IS→MSA `public/css` 복사) — 완료
`order-modal.css`, `mypage-order.css`, `mypage-order-details.css` (기존 미복사 → 복사함)

## B. 인터랙션(탭/모달) 결함 — 이번 세션 수정 완료
| 화면 | 문제 | 조치 |
|---|---|---|
| **find-idpw** | 탭 패널이 `v-show`만 써서 부트스트랩 `.tab-pane{display:none}`에 눌려 안 보임 | ✅ `:class="{active}"` |
| **gift-detail** | 후기/Q&A/배송·반품/상품고시 패널에 active 바인딩 없음(nav-detail만 고정) | ✅ 5개 패널 activeTab 바인딩 |
| DesignatedDetail / ListSelect / FAQ / 모달(black-bg .show) | active/show를 Vue가 이미 토글 | ✅ 정상(수정 불필요) |

## C. 확인된 정상 (수정 불필요)
home(슬라이더/검색 재이식 완료), donate, designated, login, signup, cart, notices/faqs/data-board/qna, guide류, policy, 인쇄 화면.

## 검증 방법 (배치 수정 후)
- orders/order-detail: 주문목록·상세 레이아웃이 AS-IS와 맞는지(mypage-order CSS 적용)
- list-select(기금사업 소개): 레이아웃 정상화
- 고객 이벤트 목록/상세: 카드/상세 스타일
- 답례품 상세: 탭 전환 시 내용 바뀌는지
- find-idpw: 탭 아래 내용·전환

전 항목 storefront 전용(재기동 불필요). 커밋돼 있어 문제 시 원복 가능.

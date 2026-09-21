// AS-IS(MPA)는 화면마다 <head>의 CSS 링크 목록을 다르게 큐레이션한다(예: 장바구니는 order_ali.css를
// 쓰지만 main.css·event.css는 안 쓰고, 답례품 상세는 item.css만 쓴다). SPA에서 이 CSS를 전부 전역
// 로드하면 서로 다른 화면용 규칙이 새어나가(bleed) 각 화면이 AS-IS와 다르게 렌더된다. 그래서 전 페이지
// 공통 base(index.html)만 전역으로 두고, 아래 표의 "페이지 전용" CSS는 해당 라우트에서만 주입/회수한다.
//
// 값은 AS-IS 각 페이지 html의 <link> 목록에서 공통 base를 뺀 나머지를 그대로 옮긴 것이다.
//   전역 base: bootstrap, swiper.min, common, new, default_ali, header-footer_ali, popup, layout, (site=MSA)
// AS-IS의 map.css는 MSA에서 lclgv-map.css로 존재하므로 그 이름으로 매핑한다.
// AS-IS의 order-modal.css / mypage-order.css / mypage-order-details.css는 MSA에 아직 복사되지 않아
// 참조하지 않는다(별도 갭 — 이번 스코핑 범위 밖).
const PAGE_STYLES = {
  // 메인/목록 (AS-IS main.html, goods/index-main.html)
  home: ['main.css', 'goods_card.css', 'event.css', 'joind-agf.css', 'lclgv-map.css'],
  'gift-list': ['main.css', 'goods_card.css', 'event.css', 'joind-agf.css', 'lclgv-map.css'],
  // 제철식품관(event/seasonList)·마을기업관(community) = 이벤트 카드형 목록 (AS-IS: evt_card·goods_card, main 없음)
  'gift-seasonal': ['event.css', 'evt_card.css', 'goods_card.css', 'joind-agf.css', 'lclgv-map.css'],
  'gift-community-business': ['event.css', 'evt_card.css', 'goods_card.css', 'joind-agf.css', 'lclgv-map.css'],
  // 답례품 상세 (AS-IS items/details-main.html — main/event 미로드, order-modal 사용)
  'gift-detail': ['item.css', 'joind-agf.css', 'lclgv-map.css', 'order-modal.css'],

  // 기부 (AS-IS donation/donation-main.html)
  donate: ['donation_doak.css', 'event.css', 'joind-ag.css', 'joind-agf.css', 'main.css', 'notice-box.css'],
  'donate-gift-select': ['donation_doak.css', 'event.css', 'joind-ag.css', 'joind-agf.css', 'main.css', 'notice-box.css'],
  // 지정기부 목록/상세 (AS-IS designated-donation/index-main.html · details.html)
  'designated-list': ['designation.css', 'event.css', 'joind-agf.css', 'main.css', 'lclgv-map.css'],
  'designated-detail': ['designation.css', 'item.css'],

  // 로그인/회원 (AS-IS users/login.html · join.html)
  login: ['authentication-modal.css', 'change-pw.css', 'event.css', 'joind-agf.css', 'login-total.css', 'main.css'],
  signup: ['event.css', 'join-inf2.css', 'joind-ag.css', 'joind-agf.css', 'main.css'],
  // AS-IS users/find-idpw.html은 로그인(login-total)이 아니라 아이디/비번찾기 전용 login-idse.css를
  // 쓴다(탭/결과 레이아웃이 전부 여기 있음). 본인인증 모달은 authentication-modal.css.
  'find-idpw': ['authentication-modal.css', 'event.css', 'login-idse.css', 'main.css'],

  // 마이페이지 계열 (AS-IS mypage/index.html · cntrList.html 등 — 서브페이지별 css의 합집합,
  // 전부 mypage 네임스페이스라 계열 내 교차로딩은 무해)
  mypage: ['main.css', 'event.css', 'm_main.css', 'mypage-status.css', 'notice-box.css', 'joind-agf.css', 'favo_info.css', 'change-info.css', 'change-pw.css'],

  // 주문/장바구니 (AS-IS cart/index.html · mypage/orderList.html)
  cart: ['order_ali.css', 'joind-agf.css', 'lclgv-map.css'],
  checkout: ['order_ali.css', 'favo_info.css', 'main.css', 'event.css'],
  'checkout-done': ['order_ali.css', 'favo_info.css', 'main.css', 'event.css'],
  // 주문목록/상세 (AS-IS mypage/orderList.html·orderDetail.html — order_ali가 아니라 mypage-order(-details))
  orders: ['mypage-order.css', 'order-modal.css', 'favo_info.css', 'main.css', 'event.css'],
  'order-detail': ['mypage-order-details.css', 'order-modal.css', 'main.css', 'event.css'],
  'claims-my': ['mypage-order.css', 'order-modal.css', 'favo_info.css', 'main.css', 'event.css'],

  // 고객센터 (AS-IS notice/list.html · faq/list.html · qna/qna-form.html)
  notices: ['ct_nov.css', 'research-box.css', 'main.css', 'event.css'],
  'notice-detail': ['ct_nov.css', 'research-box.css', 'main.css', 'event.css'],
  'data-board': ['ct_nov.css', 'research-box.css', 'main.css', 'event.css'],
  'data-board-detail': ['ct_nov.css', 'research-box.css', 'main.css', 'event.css'],
  'qna-board': ['data_v.css', 'main.css', 'event.css'],
  'qna-board-detail': ['data_v.css', 'main.css', 'event.css'],
  faqs: ['ct_nov.css', 'research-box.css', 'main.css', 'event.css'],

  // 고객 이벤트 목록/상세 (AS-IS featured/eventList.html · eventDetail.html)
  events: ['evt_card.css', 'event.css'],
  'event-detail': ['event.css', 'evt_detail.css', 'goods_card.css', 'main.css'],
  'survey-active': ['data_v.css', 'main.css', 'event.css'],
  'survey-detail': ['data_v.css', 'main.css', 'event.css'],

  // 안내/정책 (AS-IS donation/guide1.html · policy/privacy.html)
  // 연말정산 세액공제 안내 (AS-IS donation/guide3.html — donation_doak 추가)
  'honor-guide': ['donation_doak.css', 'donation_guge.css', 'event.css', 'main.css', 'research-box.css'],
  'guide-donation': ['donation_guge.css', 'event.css', 'main.css', 'research-box.css'],
  'guide-online-method': ['donation_guge.css', 'event.css', 'main.css', 'research-box.css'],
  'guide-offline-method': ['donation_guge.css', 'event.css', 'main.css', 'research-box.css'],
  'guide-caution': ['donation_guge.css', 'event.css', 'main.css', 'research-box.css'],
  // 기금사업 소개 (AS-IS donation/list-select.html — donation_liemt/selmt·goods_card·joind-agf)
  'list-select': ['donation_liemt.css', 'donation_selmt.css', 'goods_card.css', 'joind-agf.css', 'event.css', 'main.css', 'research-box.css'],
  policy: ['donation_guge.css'],

  // 영수증/확인증 인쇄(meta.bare) — 사이트 크롬 없이 인쇄 콘텐츠 전용 스타일만
  'print-receipt-certificate': ['receipt-print.css', 'receipt-modal.css'],
  'print-official-receipt': ['receipt-print.css', 'receipt-modal.css'],
}

// /mypage/* 하위 라우트는 이름이 제각각(mypage-profile, mypage-donations …)이지만 AS-IS 마이페이지
// 계열 CSS를 공유하므로, 개별 키가 없으면 mypage 세트를 물려준다.
function stylesFor(routeName) {
  if (PAGE_STYLES[routeName]) return PAGE_STYLES[routeName]
  if (typeof routeName === 'string' && routeName.startsWith('mypage')) return PAGE_STYLES.mypage
  return []
}

// 현재 <head>에 붙은 페이지 전용 <link>를 목표 목록과 비교해 없앨 것은 지우고 없는 것만 새로 추가한다.
// data-page-style 표식으로 base(index.html) 링크는 절대 건드리지 않는다.
export function applyPageStyles(routeName) {
  if (typeof document === 'undefined') return
  const wanted = stylesFor(routeName)
  const head = document.head
  const current = head.querySelectorAll('link[data-page-style]')
  const currentByFile = new Map()
  current.forEach((link) => currentByFile.set(link.dataset.pageStyle, link))

  // 더 이상 필요 없는 링크 제거
  currentByFile.forEach((link, file) => {
    if (!wanted.includes(file)) {
      link.remove()
      currentByFile.delete(file)
    }
  })
  // 부족한 링크 추가(항상 <head> 끝에 붙어 base보다 뒤 = 우선순위 높음)
  wanted.forEach((file) => {
    if (currentByFile.has(file)) return
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = `/css/${file}`
    link.dataset.pageStyle = file
    head.appendChild(link)
  })
}

import { createRouter, createWebHistory } from 'vue-router'
// 첫 화면(HomeView)만 정적 import - 나머지 뷰는 라우트에 진입할 때 그 청크만 받는다.
// 전부 정적으로 묶으면 로그인 화면 하나 보려고 마이페이지·주문 화면까지 전부 내려받게 된다.
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '../stores/auth'
import { applyPageStyles } from './pageStyles'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/gifts', name: 'gift-list', component: () => import('../views/gift/GiftListView.vue') },
    { path: '/gifts/seasonal', name: 'gift-seasonal', component: () => import('../views/gift/GiftListView.vue') },
    { path: '/gifts/community-business', name: 'gift-community-business', component: () => import('../views/gift/GiftListView.vue') },
    { path: '/gifts/:itemId', name: 'gift-detail', component: () => import('../views/gift/GiftDetailView.vue'), props: true },
    { path: '/donate', name: 'donate', component: () => import('../views/donation/DonateView.vue'), meta: { requiresAuth: true } },
    { path: '/donate/gift-select', name: 'donate-gift-select', component: () => import('../views/donation/DonateGiftSelectView.vue'), meta: { requiresAuth: true } },
    { path: '/designated-donation', name: 'designated-list', component: () => import('../views/donation/DesignatedListView.vue') },
    { path: '/designated-donation/:id', name: 'designated-detail', component: () => import('../views/donation/DesignatedDetailView.vue'), props: true },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/signup', name: 'signup', component: () => import('../views/SignupView.vue') },
    { path: '/find-idpw', name: 'find-idpw', component: () => import('../views/FindIdPwView.vue') },
    { path: '/mypage/password', name: 'mypage-password', component: () => import('../views/mypage/PasswordChangeView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/withdraw', name: 'mypage-withdraw', component: () => import('../views/mypage/WithdrawView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/role-request', name: 'mypage-role-request', component: () => import('../views/mypage/RoleRequestView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/role-queue', name: 'mypage-role-queue', component: () => import('../views/mypage/RoleQueueView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage', name: 'mypage', component: () => import('../views/mypage/MyPageView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/profile', name: 'mypage-profile', component: () => import('../views/mypage/ProfileView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/delivery', name: 'mypage-delivery', component: () => import('../views/mypage/DeliveryListView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/delivery/new', name: 'mypage-delivery-new', component: () => import('../views/mypage/DeliveryFormView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/delivery/:id/edit', name: 'mypage-delivery-edit', component: () => import('../views/mypage/DeliveryFormView.vue'), meta: { requiresAuth: true }, props: true },
    { path: '/mypage/interest-locgovs', name: 'mypage-interest-locgovs', component: () => import('../views/mypage/InterestLocgovsView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/honor-certificates', name: 'mypage-honor-certificates', component: () => import('../views/mypage/HonorCertificatesView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/donations', name: 'mypage-donations', component: () => import('../views/mypage/MyDonationsView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/donations/offline', name: 'mypage-donations-offline', component: () => import('../views/mypage/OfflineDonationView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/receipts', name: 'mypage-receipts', component: () => import('../views/mypage/ReceiptListView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/tax-credit-estimate', name: 'mypage-tax-credit-estimate', component: () => import('../views/mypage/TaxCreditEstimateView.vue'), meta: { requiresAuth: true } },
    { path: '/print/receipt-certificate', name: 'print-receipt-certificate', component: () => import('../views/mypage/ReceiptPrintView.vue'), meta: { requiresAuth: true, bare: true } },
    { path: '/print/official-receipt/:cntrSn', name: 'print-official-receipt', component: () => import('../views/mypage/OfficialReceiptPrintView.vue'), props: true, meta: { requiresAuth: true, bare: true } },
    { path: '/honor', name: 'honor-guide', component: () => import('../views/donation/TaxCreditGuideView.vue') },
    { path: '/guide1', name: 'guide-donation', component: () => import('../views/donation/GuideDonationView.vue') },
    { path: '/guide2', name: 'guide-online-method', component: () => import('../views/donation/GuideOnlineMethodView.vue') },
    { path: '/guide5', name: 'guide-offline-method', component: () => import('../views/donation/GuideOfflineMethodView.vue') },
    { path: '/guide6', name: 'guide-caution', component: () => import('../views/donation/GuideCautionView.vue') },
    { path: '/list-select', name: 'list-select', component: () => import('../views/donation/ListSelectView.vue') },
    { path: '/policy/:slug(privacy|copyright|auth)', name: 'policy', component: () => import('../views/donation/PolicyView.vue') },
    { path: '/mypage/wishlist', name: 'mypage-wishlist', component: () => import('../views/mypage/WishlistView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/gift-reviews', name: 'mypage-gift-reviews', component: () => import('../views/mypage/GiftReviewsView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/gift-qna', name: 'mypage-gift-qna', component: () => import('../views/mypage/GiftQnaView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/points', name: 'mypage-points', component: () => import('../views/mypage/MyPointsView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/points/reservations', name: 'mypage-points-reservations', component: () => import('../views/mypage/PointReservationsView.vue'), meta: { requiresAuth: true } },
    { path: '/mypage/qna', name: 'mypage-qna', component: () => import('../views/mypage/QnaView.vue'), meta: { requiresAuth: true } },
    { path: '/cart', name: 'cart', component: () => import('../views/order/CartView.vue'), meta: { requiresAuth: true } },
    { path: '/checkout', name: 'checkout', component: () => import('../views/order/CheckoutView.vue'), meta: { requiresAuth: true } },
    { path: '/checkout/done', name: 'checkout-done', component: () => import('../views/order/OrderCompleteView.vue'), meta: { requiresAuth: true } },
    { path: '/orders', name: 'orders', component: () => import('../views/order/MyOrdersView.vue'), meta: { requiresAuth: true } },
    { path: '/orders/:orderId', name: 'order-detail', component: () => import('../views/order/OrderDetailView.vue'), meta: { requiresAuth: true }, props: true },
    { path: '/claims/my', name: 'claims-my', component: () => import('../views/order/MyClaimsView.vue'), meta: { requiresAuth: true } },
    // 쿠폰 화면 진입점 차단 (2026-09-09). 쿠폰 기능은 AS-IS에서도 현재 사용되지 않아
    // 자동발급을 전부 멈췄고(order: CouponBatchScheduler 설정 OFF,
    // OrderService.issuePurchaseTriggeredCoupons 본문 주석), 화면도 감춘다.
    // 서비스 로직/DB/API와 화면 컴포넌트 파일은 그대로 남겨뒀으므로, 아래 주석 3줄을 풀고
    // redirect 3줄을 지우면 그대로 복구된다. 상세 경위는 docs/cart-review-2026-09-09.md §4-5.
    // { path: '/my/coupons', name: 'my-coupons', component: () => import('../views/order/MyCouponsView.vue'), meta: { requiresAuth: true } },
    // { path: '/coupons', name: 'coupons', component: () => import('../views/order/ClaimableCouponsView.vue'), meta: { requiresAuth: true } },
    // { path: '/coupons/offline', name: 'coupons-offline', component: () => import('../views/order/OfflineCouponClaimView.vue'), meta: { requiresAuth: true } },
    // 이 SPA에는 404 라우트가 없어 라우트를 그냥 빼면 북마크/외부링크로 들어온 사용자가
    // 빈 화면을 보게 되므로 마이페이지로 보낸다.
    { path: '/my/coupons', redirect: '/mypage' },
    { path: '/coupons', redirect: '/mypage' },
    { path: '/coupons/offline', redirect: '/mypage' },
    { path: '/notices', name: 'notices', component: () => import('../views/customer/NoticeListView.vue') },
    { path: '/notices/:id', name: 'notice-detail', component: () => import('../views/customer/NoticeDetailView.vue'), props: true },
    { path: '/data-board', name: 'data-board', component: () => import('../views/customer/DataBoardListView.vue') },
    { path: '/data-board/:id', name: 'data-board-detail', component: () => import('../views/customer/DataBoardDetailView.vue'), props: true },
    { path: '/qna/board', name: 'qna-board', component: () => import('../views/customer/QnaBoardListView.vue') },
    { path: '/qna/board/:id', name: 'qna-board-detail', component: () => import('../views/customer/QnaBoardDetailView.vue'), props: true },
    { path: '/faqs', name: 'faqs', component: () => import('../views/customer/FaqListView.vue') },
    { path: '/events', name: 'events', component: () => import('../views/customer/EventListView.vue') },
    { path: '/events/:id', name: 'event-detail', component: () => import('../views/customer/EventDetailView.vue'), props: true },
    { path: '/survey', name: 'survey-active', component: () => import('../views/SurveyView.vue') },
    { path: '/survey/:id', name: 'survey-detail', component: () => import('../views/SurveyView.vue') },
    // 매칭되지 않는 모든 경로(잘못된 배너 링크·삭제된 게시물·오타 주소 등)는 AS-IS error/404.html과
    // 동일한 페이지 오류 화면으로 보낸다. bare=사이트 헤더/푸터 없이 단독 렌더(AS-IS도 독립 페이지).
    { path: '/:pathMatch(.*)*', name: 'error-404', component: () => import('../views/ErrorView.vue'), meta: { bare: true } },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

// AS-IS는 화면마다 필요한 CSS만 <head>에 링크한다. SPA에서도 라우트가 바뀔 때마다 그 화면 전용
// CSS만 주입/회수해, 전역 로딩으로 인한 화면 간 CSS 누수(bleed)를 없앤다(src/router/pageStyles.js).
// 목적지가 확정되기 전(auth 가드가 리다이렉트할 수도 있음)에 붙여도, 리다이렉트되면 다음 사이클에서
// 목적지에 맞게 다시 정리되므로 안전하다. 항상 통과시킨다.
router.beforeEach((to) => {
  applyPageStyles(to.name)
  return true
})

// 백엔드 각 컨트롤러의 loginRedirect("/xxx") 패턴(비로그인 접근시 /login?target=으로 튕기고
// 로그인 성공 후 원래 화면으로 돌아옴)을 SPA 라우트에서도 동일하게 재현한다. 새로고침 등으로
// auth store가 아직 채워지기 전이면 먼저 /api/auth/me를 한 번 기다린다.
router.beforeEach(async (to) => {
  if (!to.meta.requiresAuth) return true
  const auth = useAuthStore()
  if (auth.loading) await auth.fetchMe()
  if (!auth.loggedIn) {
    return { path: '/login', query: { target: to.fullPath } }
  }
  return true
})

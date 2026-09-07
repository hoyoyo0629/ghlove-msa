import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import FindIdPwView from '../views/FindIdPwView.vue'
import PasswordChangeView from '../views/mypage/PasswordChangeView.vue'
import WithdrawView from '../views/mypage/WithdrawView.vue'
import RoleRequestView from '../views/mypage/RoleRequestView.vue'
import RoleQueueView from '../views/mypage/RoleQueueView.vue'
import MyPageView from '../views/mypage/MyPageView.vue'
import ProfileView from '../views/mypage/ProfileView.vue'
import DeliveryListView from '../views/mypage/DeliveryListView.vue'
import DeliveryFormView from '../views/mypage/DeliveryFormView.vue'
import InterestLocgovsView from '../views/mypage/InterestLocgovsView.vue'
import HonorCertificatesView from '../views/mypage/HonorCertificatesView.vue'
import MyDonationsView from '../views/mypage/MyDonationsView.vue'
import OfflineDonationView from '../views/mypage/OfflineDonationView.vue'
import ReceiptListView from '../views/mypage/ReceiptListView.vue'
import ReceiptPrintView from '../views/mypage/ReceiptPrintView.vue'
import TaxCreditEstimateView from '../views/mypage/TaxCreditEstimateView.vue'
import TaxCreditGuideView from '../views/donation/TaxCreditGuideView.vue'
import GuideDonationView from '../views/donation/GuideDonationView.vue'
import GuideOnlineMethodView from '../views/donation/GuideOnlineMethodView.vue'
import GuideOfflineMethodView from '../views/donation/GuideOfflineMethodView.vue'
import GuideCautionView from '../views/donation/GuideCautionView.vue'
import ListSelectView from '../views/donation/ListSelectView.vue'
import PolicyView from '../views/donation/PolicyView.vue'
import WishlistView from '../views/mypage/WishlistView.vue'
import GiftReviewsView from '../views/mypage/GiftReviewsView.vue'
import GiftQnaView from '../views/mypage/GiftQnaView.vue'
import MyPointsView from '../views/mypage/MyPointsView.vue'
import PointReservationsView from '../views/mypage/PointReservationsView.vue'
import QnaView from '../views/mypage/QnaView.vue'
import GiftListView from '../views/gift/GiftListView.vue'
import GiftDetailView from '../views/gift/GiftDetailView.vue'
import DonateView from '../views/donation/DonateView.vue'
import DonateGiftSelectView from '../views/donation/DonateGiftSelectView.vue'
import DesignatedListView from '../views/donation/DesignatedListView.vue'
import DesignatedDetailView from '../views/donation/DesignatedDetailView.vue'
import CartView from '../views/order/CartView.vue'
import CheckoutView from '../views/order/CheckoutView.vue'
import OrderCompleteView from '../views/order/OrderCompleteView.vue'
import MyOrdersView from '../views/order/MyOrdersView.vue'
import NoticeListView from '../views/customer/NoticeListView.vue'
import NoticeDetailView from '../views/customer/NoticeDetailView.vue'
import DataBoardListView from '../views/customer/DataBoardListView.vue'
import DataBoardDetailView from '../views/customer/DataBoardDetailView.vue'
import QnaBoardListView from '../views/customer/QnaBoardListView.vue'
import QnaBoardDetailView from '../views/customer/QnaBoardDetailView.vue'
import FaqListView from '../views/customer/FaqListView.vue'
import EventListView from '../views/customer/EventListView.vue'
import EventDetailView from '../views/customer/EventDetailView.vue'
import OrderDetailView from '../views/order/OrderDetailView.vue'
import MyClaimsView from '../views/order/MyClaimsView.vue'
import MyCouponsView from '../views/order/MyCouponsView.vue'
import ClaimableCouponsView from '../views/order/ClaimableCouponsView.vue'
import OfflineCouponClaimView from '../views/order/OfflineCouponClaimView.vue'
import { useAuthStore } from '../stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/gifts', name: 'gift-list', component: GiftListView },
    { path: '/gifts/seasonal', name: 'gift-seasonal', component: GiftListView },
    { path: '/gifts/community-business', name: 'gift-community-business', component: GiftListView },
    { path: '/gifts/:itemId', name: 'gift-detail', component: GiftDetailView, props: true },
    { path: '/donate', name: 'donate', component: DonateView, meta: { requiresAuth: true } },
    { path: '/donate/gift-select', name: 'donate-gift-select', component: DonateGiftSelectView, meta: { requiresAuth: true } },
    { path: '/designated-donation', name: 'designated-list', component: DesignatedListView },
    { path: '/designated-donation/:id', name: 'designated-detail', component: DesignatedDetailView, props: true },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/signup', name: 'signup', component: SignupView },
    { path: '/find-idpw', name: 'find-idpw', component: FindIdPwView },
    { path: '/mypage/password', name: 'mypage-password', component: PasswordChangeView, meta: { requiresAuth: true } },
    { path: '/mypage/withdraw', name: 'mypage-withdraw', component: WithdrawView, meta: { requiresAuth: true } },
    { path: '/mypage/role-request', name: 'mypage-role-request', component: RoleRequestView, meta: { requiresAuth: true } },
    { path: '/mypage/role-queue', name: 'mypage-role-queue', component: RoleQueueView, meta: { requiresAuth: true } },
    { path: '/mypage', name: 'mypage', component: MyPageView, meta: { requiresAuth: true } },
    { path: '/mypage/profile', name: 'mypage-profile', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/mypage/delivery', name: 'mypage-delivery', component: DeliveryListView, meta: { requiresAuth: true } },
    { path: '/mypage/delivery/new', name: 'mypage-delivery-new', component: DeliveryFormView, meta: { requiresAuth: true } },
    { path: '/mypage/delivery/:id/edit', name: 'mypage-delivery-edit', component: DeliveryFormView, meta: { requiresAuth: true }, props: true },
    { path: '/mypage/interest-locgovs', name: 'mypage-interest-locgovs', component: InterestLocgovsView, meta: { requiresAuth: true } },
    { path: '/mypage/honor-certificates', name: 'mypage-honor-certificates', component: HonorCertificatesView, meta: { requiresAuth: true } },
    { path: '/mypage/donations', name: 'mypage-donations', component: MyDonationsView, meta: { requiresAuth: true } },
    { path: '/mypage/donations/offline', name: 'mypage-donations-offline', component: OfflineDonationView, meta: { requiresAuth: true } },
    { path: '/mypage/receipts', name: 'mypage-receipts', component: ReceiptListView, meta: { requiresAuth: true } },
    { path: '/mypage/tax-credit-estimate', name: 'mypage-tax-credit-estimate', component: TaxCreditEstimateView, meta: { requiresAuth: true } },
    { path: '/print/receipt-certificate', name: 'print-receipt-certificate', component: ReceiptPrintView, meta: { requiresAuth: true, bare: true } },
    { path: '/honor', name: 'honor-guide', component: TaxCreditGuideView },
    { path: '/guide1', name: 'guide-donation', component: GuideDonationView },
    { path: '/guide2', name: 'guide-online-method', component: GuideOnlineMethodView },
    { path: '/guide5', name: 'guide-offline-method', component: GuideOfflineMethodView },
    { path: '/guide6', name: 'guide-caution', component: GuideCautionView },
    { path: '/list-select', name: 'list-select', component: ListSelectView },
    { path: '/policy/:slug(privacy|copyright|auth)', name: 'policy', component: PolicyView },
    { path: '/mypage/wishlist', name: 'mypage-wishlist', component: WishlistView, meta: { requiresAuth: true } },
    { path: '/mypage/gift-reviews', name: 'mypage-gift-reviews', component: GiftReviewsView, meta: { requiresAuth: true } },
    { path: '/mypage/gift-qna', name: 'mypage-gift-qna', component: GiftQnaView, meta: { requiresAuth: true } },
    { path: '/mypage/points', name: 'mypage-points', component: MyPointsView, meta: { requiresAuth: true } },
    { path: '/mypage/points/reservations', name: 'mypage-points-reservations', component: PointReservationsView, meta: { requiresAuth: true } },
    { path: '/mypage/qna', name: 'mypage-qna', component: QnaView, meta: { requiresAuth: true } },
    { path: '/cart', name: 'cart', component: CartView, meta: { requiresAuth: true } },
    { path: '/checkout', name: 'checkout', component: CheckoutView, meta: { requiresAuth: true } },
    { path: '/checkout/done', name: 'checkout-done', component: OrderCompleteView, meta: { requiresAuth: true } },
    { path: '/orders', name: 'orders', component: MyOrdersView, meta: { requiresAuth: true } },
    { path: '/orders/:orderId', name: 'order-detail', component: OrderDetailView, meta: { requiresAuth: true }, props: true },
    { path: '/claims/my', name: 'claims-my', component: MyClaimsView, meta: { requiresAuth: true } },
    { path: '/my/coupons', name: 'my-coupons', component: MyCouponsView, meta: { requiresAuth: true } },
    { path: '/coupons', name: 'coupons', component: ClaimableCouponsView, meta: { requiresAuth: true } },
    { path: '/coupons/offline', name: 'coupons-offline', component: OfflineCouponClaimView, meta: { requiresAuth: true } },
    { path: '/notices', name: 'notices', component: NoticeListView },
    { path: '/notices/:id', name: 'notice-detail', component: NoticeDetailView, props: true },
    { path: '/data-board', name: 'data-board', component: DataBoardListView },
    { path: '/data-board/:id', name: 'data-board-detail', component: DataBoardDetailView, props: true },
    { path: '/qna/board', name: 'qna-board', component: QnaBoardListView },
    { path: '/qna/board/:id', name: 'qna-board-detail', component: QnaBoardDetailView, props: true },
    { path: '/faqs', name: 'faqs', component: FaqListView },
    { path: '/events', name: 'events', component: EventListView },
    { path: '/events/:id', name: 'event-detail', component: EventDetailView, props: true },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
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

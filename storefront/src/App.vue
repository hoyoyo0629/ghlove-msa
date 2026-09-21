<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppHeaderShopping from './components/AppHeaderShopping.vue'
import AppFooter from './components/AppFooter.vue'
import FloatingButtons from './components/FloatingButtons.vue'
import GlobalModal from './components/GlobalModal.vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
onMounted(() => {
  auth.fetchMe()
})

// AS-IS는 답례품몰/장바구니/이벤트 계열 페이지에서 메인 헤더(header_ali) 아래에 쇼핑 전용
// GNB 바(header_g)를 함께 마운트한다. 그 라우트에서만 AppHeaderShopping을 얹는다.
const SHOPPING_ROUTES = new Set([
  'gift-list', 'gift-seasonal', 'gift-community-business', 'gift-detail',
  'cart', 'events', 'event-detail',
])
const isShoppingPage = computed(() => SHOPPING_ROUTES.has(route.name))
</script>

<template>
  <!-- 기부확인증 출력(/print/receipt-certificate)처럼 브라우저 인쇄 대상이 되는 화면은
       사이트 헤더/푸터 없이 인쇄 콘텐츠만 단독으로 보여줘야 한다(기존 Thymeleaf
       certificate-print.html과 동일한 목적) - route.meta.bare로 사이트 크롬을 건너뛴다. -->
  <template v-if="route.meta.bare">
    <router-view />
  </template>
  <template v-else>
    <AppHeader />
    <AppHeaderShopping v-if="isShoppingPage" />
    <router-view />
    <FloatingButtons />
    <AppFooter />
  </template>
  <!-- AS-IS op-modal($s.alert/$s.confirm) 대체 공용 모달 - 어느 화면에서든 modalAlert/modalConfirm으로 사용 -->
  <GlobalModal />
</template>

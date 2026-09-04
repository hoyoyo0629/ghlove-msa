<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import FloatingButtons from './components/FloatingButtons.vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
onMounted(() => {
  auth.fetchMe()
})
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
    <router-view />
    <FloatingButtons />
    <AppFooter />
  </template>
</template>

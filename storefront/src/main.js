import { createApp } from 'vue'
import { modalAlert } from './composables/useModal'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import { setUnauthorizedHandler } from './api/http'
import { useAuthStore } from './stores/auth'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia).use(router)

// AS-IS op.saleson.js:4492 handleApiExeption의 401 처리 재현 - 이미 열린 화면에서 세션이 만료돼
// 보호 API가 401을 주면, 화면이 조용히 깨지지 않도록 안내 후 로그인(?target=)으로 보낸다.
// requiresAuth 라우터 가드는 "진입 시점"만 커버하므로 이 전역 처리가 그 사이 만료를 메운다.
let redirectingToLogin = false
setUnauthorizedHandler(() => {
  if (redirectingToLogin || router.currentRoute.value.path === '/login') return
  redirectingToLogin = true
  try {
    const auth = useAuthStore(pinia)
    auth.loggedIn = false
    auth.me = null
  } catch {
    // 스토어 접근 실패해도 리다이렉트는 진행한다.
  }
  modalAlert('로그인 후 이용이 가능합니다.')
  const target = router.currentRoute.value.fullPath
  router.push({ path: '/login', query: { target } }).finally(() => {
    redirectingToLogin = false
  })
})

app.mount('#app')

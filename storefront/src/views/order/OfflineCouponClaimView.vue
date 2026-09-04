<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// Thymeleaf 버전 order/coupon/offline-claim.html과 동일 출처.
const router = useRouter()
const code = ref('')
const errorMessage = ref('')

async function submit() {
  errorMessage.value = ''
  try {
    await api.post('order', '/api/coupons/offline/claim', { code: code.value })
    router.push('/my/coupons')
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/coupons">쿠폰 다운로드</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        코드 입력
      </span>
      <h2 class="page-title-txt">쿠폰 코드 입력</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <p class="error" v-if="errorMessage" style="text-align: center">{{ errorMessage }}</p>
      <form style="max-width: 420px; margin: 0 auto; display: flex; gap: 8px" @submit.prevent="submit">
        <input type="text" v-model="code" placeholder="쿠폰 코드를 입력하세요" required style="flex: 1" />
        <button type="submit" class="blueBtn u-confirm">쿠폰받기</button>
      </form>
    </div>
  </section>
</template>

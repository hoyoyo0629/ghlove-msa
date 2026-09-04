<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage/honorList.html(및 Thymeleaf 버전 honor-certificates.html) 재현 - 별도
// 발급 신청 없이 완료된 기부가 쌓일 때마다 자동으로 등급이 산정되는 화면.
const certificates = ref([])

onMounted(async () => {
  certificates.value = await api.get('donation', '/api/honor/certificates')
})
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부혜택증
      </span>
      <h2 class="page-title-txt">기부혜택증</h2>
    </div>
  </section>

  <section class="s-con receipt-con">
    <div class="center">
      <div class="honor-empty-banner" v-if="!certificates.length">
        <div class="honor-empty-card">
          <p>발급된<br />기부혜택증이<br />없습니다.</p>
          <img class="honor-empty-icon" src="/images/honors/honor0.png" alt="" />
        </div>
      </div>

      <ul class="honor-cert-list" v-else>
        <li class="honor-cert-card" v-for="c in certificates" :key="`${c.stdrYear}-${c.locgovCode}`">
          <div class="honor-cert-level">{{ c.levelLabel ?? c.levelCode }}</div>
          <div class="honor-cert-locgov">{{ c.locgovName }}</div>
          <div class="honor-cert-year">{{ c.stdrYear }}년도 명예기부자</div>
        </li>
      </ul>
    </div>
  </section>

  <div class="center" style="padding: 20px 0">
    <router-link to="/mypage/tax-credit-estimate">세액공제 예상액 확인하기</router-link>
  </div>
</template>

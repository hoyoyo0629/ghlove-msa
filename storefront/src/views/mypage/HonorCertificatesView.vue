<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'
import MypageLnb from '../../components/MypageLnb.vue'

// AS-IS mypage/honorList.html(및 Thymeleaf 버전 honor-certificates.html) 재현 - 별도
// 발급 신청 없이 완료된 기부가 쌓일 때마다 자동으로 등급이 산정되는 화면.
const certificates = ref([])

onMounted(async () => {
  certificates.value = await api.get('donation', '/api/honor/certificates')
})
</script>

<template>
  <MypageLnb current="honor" />
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
  <!-- AS-IS honorList.html(기부혜택증)에는 세액공제 관련 링크가 없다(그 문구 자체가 AS-IS
       전무). tax-credit-estimate 화면은 MSA 신규작이고 원래 GNB 미연결 의도였으므로, AS-IS
       파리티를 위해 이 화면에서 링크를 노출하지 않는다(화면·계산 로직은 유지, 라우트는 존치). -->
</template>

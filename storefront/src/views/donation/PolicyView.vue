<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../../api/http'

// AS-IS policy/privacy·copyright·auth.html(및 Thymeleaf 버전) 재현. 셋 다 서버 CMS
// 원문(policy-content/*.html)을 v-html로 그대로 뿌리는 얇은 셸이라 화면 하나를 슬러그로
// 재사용한다(footer의 개인정보처리방침/저작권정책/이용약관 3개 링크가 전부 이 화면으로 옴).
const route = useRoute()
const data = ref(null)

async function load() {
  data.value = null
  data.value = await api.get('donation', `/api/policy/${route.params.slug}`)
}
onMounted(load)
watch(() => route.params.slug, load)
</script>

<template>
  <section class="center" v-if="data">
    <div class="page-title-box">
      <img src="/images/icon/cli-icon-bullet.png" alt="" />
      <h2 class="page-title-txt">{{ data.title }}</h2>
    </div>

    <div class="contents-wrap" id="contents">
      <div class="s-contents fnb">
        <h3 v-if="route.params.slug === 'privacy'">고향사랑e음 개인정보처리방침</h3>
        <article class="fnb-detail" style="overflow: auto" v-html="data.content"></article>
      </div>
    </div>
  </section>
</template>

<style>
@import '/css/donation_guge.css';
</style>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS notice/detail.html 재현.
const route = useRoute()
const router = useRouter()
const notice = ref(null)

async function load() {
  notice.value = null
  try {
    notice.value = await api.get('admin', `/api/notices/${route.params.id}`)
  } catch {
    router.replace('/notices')
  }
}
onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        고객센터
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/notices">공지사항</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        공지사항 보기
      </span>
      <h2 class="page-title-txt">공지사항 보기</h2>
    </div>
  </section>

  <CustomerLnb current="notices" />

  <div class="contents-wrap" id="contents" v-if="notice">
    <div class="center">
      <div class="s-contents list-page-wrapper">
        <div class="list-page-area">
          <div class="list-page-body notice">
            <div class="page-title">{{ notice.subject }}</div>
            <div class="page-info">
              <div class="page-info-list">
                <div class="date-created">등록일 {{ notice.displayDate }}</div>
                <div class="views">조회 {{ notice.displayHits }}</div>
              </div>
            </div>
            <div class="page-contents" v-html="notice.content"></div>
          </div>
        </div>
        <div class="btn-box">
          <button type="button" class="blueBtn u-confirm" @click="router.push('/notices')">
            목록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

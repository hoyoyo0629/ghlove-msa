<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS featured/eventDetail.html 재현. 답례품 연관 그리드(itemListMap)는 백엔드 EventController와
// 동일하게 이번 라운드 스코프 밖 - 이벤트 본문/기간/담당부서/연락처/바로가기 링크만 다룬다.
const route = useRoute()
const router = useRouter()
const event = ref(null)

async function load() {
  event.value = null
  try {
    event.value = await api.get('admin', `/api/events/${route.params.id}`)
  } catch {
    router.replace('/events')
  }
}
onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <section class="center" id="contents" v-if="event">
    <div class="contents-wrap">
      <div class="list-page-area">
        <div class="list-page-body">
          <div class="page-title">{{ event.title }}</div>
          <div class="page-info">
            <div class="page-info-list">
              <div class="writer">{{ event.displayDate }}</div>
              <div class="date-created" v-if="event.hostName">{{ event.hostName }}</div>
              <div class="views" v-if="event.phone">{{ event.phone }}</div>
            </div>
            <a v-if="event.linkUrl" :href="event.linkUrl" class="share-con pointblue moreView" target="_blank" rel="noopener">
              <img class="icon-img" src="/images/icon/cli-icon_share-con.png" alt="링크이동" />
              <span>{{ event.linkUrl }}</span>
            </a>
          </div>
          <div class="page-contents">
            <div class="top_banner" v-if="event.listImage">
              <img :src="`/images/events/${event.listImage}`" alt="" />
            </div>
            <div class="result_con" v-html="event.content"></div>
          </div>
        </div>
      </div>
      <div class="btn-box">
        <button type="button" class="blueBtn u-confirm last-btn" @click="router.push('/events')">
          목록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
      </div>
    </div>
  </section>
</template>

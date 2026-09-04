<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS data-board/detail.html 재현. 첨부파일 다운로드는 admin 서비스가 이미 스트리밍으로
// 내려주는(Content-Disposition: attachment) 기존 엔드포인트를 그대로 링크로 재사용한다.
const route = useRoute()
const router = useRouter()
const board = ref(null)

async function load() {
  board.value = null
  try {
    board.value = await api.get('admin', `/api/data-board/${route.params.id}`)
  } catch {
    router.replace('/data-board')
  }
}
onMounted(load)
watch(() => route.params.id, load)

function downloadUrl(fileId) {
  return api.assetUrl('admin', `/data-board/file-download/${fileId}`)
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        고객센터
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/data-board">자료실</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        자료실 보기
      </span>
      <h2 class="page-title-txt">자료실 보기</h2>
    </div>
  </section>

  <CustomerLnb current="data-board" />

  <div class="contents-wrap" id="contents" v-if="board">
    <div class="center">
      <div class="s-contents list-page-wrapper">
        <div class="list-page-area">
          <div class="list-page-body">
            <div class="page-title">{{ board.subject }}</div>
            <div class="page-info">
              <div class="page-info-list">
                <div class="writer">관리자</div>
                <div class="date-created">{{ board.displayDate }}</div>
                <div class="views">조회 {{ board.hits }}</div>
              </div>
            </div>
            <div class="page-contents" v-html="board.content"></div>
            <div class="page-file-list" v-if="board.files.length">
              <div class="file-list-label">
                <img src="/images/icon/cli-icon_file-list.png" alt="첨부자료" /> 첨부자료
              </div>
              <div class="file-list-title">
                <span v-for="f in board.files" :key="f.dataFileId">
                  <a :href="downloadUrl(f.dataFileId)" class="downloadFn">{{ f.orgFileName }}</a><br />
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="btn-box">
          <button type="button" class="blueBtn u-confirm" @click="router.push('/data-board')">
            목록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

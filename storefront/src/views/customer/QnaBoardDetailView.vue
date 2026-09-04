<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS qna/qna-detail.html 재현. 비밀글이고 작성자 본인이 아니면(detail.locked) 서버가
// 아예 질문/답변 내용을 내려주지 않는다(QnaService.publicBoardDetail) - 데이터 자체를 안 줌.
const route = useRoute()
const router = useRouter()
const detail = ref(null)

async function load() {
  detail.value = null
  try {
    detail.value = await api.get('admin', `/api/qna/board/${route.params.id}`)
  } catch {
    router.replace('/qna/board')
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
        <router-link to="/qna/board">Q&amp;A</router-link>
      </span>
      <h2 class="page-title-txt">Q&amp;A</h2>
    </div>
  </section>

  <CustomerLnb current="qna" />

  <div class="contents-wrap" id="contents" v-if="detail">
    <div class="center">
      <div class="s-contents list-wrapper qna">
        <div v-if="detail.locked" class="list-none">
          <img src="/images/icon/non-list.png" alt="비밀글" />
          비밀글입니다. 작성자 본인만 열람할 수 있습니다.
        </div>

        <div v-else class="list-search-body inquiry faq">
          <ul class="list-search-group">
            <li class="list-items dropdown show">
              <div class="notice-header inquiry dropdown-toggle">
                <div class="notice-header-txt">
                  <img class="faq-icon" src="/images/icon/cli-icon_faq-q.png" alt="" />
                  <div class="notice-txt">
                    <span class="subject">{{ detail.subject }}</span>
                    <div class="q_contents" style="white-space: pre-wrap">{{ detail.question }}</div>
                    <div class="q-add_file" v-if="detail.files.length">
                      <button class="moreView" type="button" v-for="f in detail.files" :key="f.qnaFileId">{{ f.orgFileName }}</button>
                    </div>
                  </div>
                </div>
                <div class="notice-header-status">
                  <div class="date-col">{{ detail.userName }}</div>
                  <div class="date-col">{{ detail.createdDate?.slice(0, 8) }}</div>
                  <div class="date-col">조회 {{ detail.hits }}</div>
                </div>
              </div>
              <div class="notice-body dropdown-menu show" v-if="detail.answer">
                <div class="notice-wrap">
                  <img class="faq-icon" src="/images/icon/cli-icon_faq-a.png" alt="" />
                  <div class="notice-txt">
                    <span class="subject">{{ detail.answer.title }}</span>
                    <div class="q_contents" style="white-space: pre-wrap">{{ detail.answer.answer }}</div>
                  </div>
                </div>
              </div>
              <div class="notice-body dropdown-menu show" v-else>
                <div class="notice-wrap">
                  <div class="notice-txt">
                    <span class="q_contents">아직 답변이 등록되지 않았습니다.</span>
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </div>

        <div class="btn-box">
          <button type="button" class="formBtn" @click="router.push('/qna/board')">목록으로</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage 답례품Q&A(및 Thymeleaf 버전 my-qna.html) 재현 - 본인이 작성한 상품문의
// 목록(작성 자체는 답례품 상세화면에서 한다, 판매자용 문의답변 화면과는 다른 화면).
// 비밀글은 답변 전까지 질문 내용을 가려서 보여준다(백엔드가 이미 question=null로 내려줌).
const rows = ref([])
const loading = ref(true)

const searchStartDate = ref('')
const searchEndDate = ref('')
const itemName = ref('')

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (searchStartDate.value) params.set('searchStartDate', searchStartDate.value)
    if (searchEndDate.value) params.set('searchEndDate', searchEndDate.value)
    if (itemName.value) params.set('itemName', itemName.value)
    rows.value = await api.get('gift', `/api/my/qna?${params}`)
  } finally {
    loading.value = false
  }
}
onMounted(load)

function reset() {
  searchStartDate.value = ''
  searchEndDate.value = ''
  itemName.value = ''
  load()
}

function quickRange(mode, value) {
  const end = new Date()
  const start = new Date()
  if (mode === 'week') start.setDate(start.getDate() - value * 7)
  else if (mode === 'month') start.setMonth(start.getMonth() - value)
  const iso = (d) => d.toISOString().slice(0, 10)
  searchStartDate.value = iso(start)
  searchEndDate.value = iso(end)
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        답례품Q&amp;A
      </span>
      <h2 class="page-title-txt">답례품Q&amp;A</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div class="s-contents mypageS">
        <div class="mypage-top">
          <label class="card_title" for="searchStartDate">기간</label>
          <div class="selected-field-line">
            <div class="selected-item">
              <label for="searchStartDate" class="sr-only">시작일</label>
              <input type="date" id="searchStartDate" v-model="searchStartDate" />
              <span class="s_txt"> ~ </span>
              <label for="searchEndDate" class="sr-only">종료일</label>
              <input type="date" id="searchEndDate" v-model="searchEndDate" />
            </div>
            <div class="formbtn-box">
              <button class="formBtn" type="button" @click="quickRange('week', 1)">1주일</button>
              <button class="formBtn" type="button" @click="quickRange('month', 1)">1개월</button>
              <button class="formBtn" type="button" @click="quickRange('month', 3)">3개월</button>
              <button class="formBtn" type="button" @click="quickRange('month', 6)">6개월</button>
            </div>
          </div>
        </div>
        <div class="line"></div>
        <div class="mypage-top">
          <label class="card_title" for="itemName">답례품명</label>
          <div class="selected-item">
            <input type="text" id="itemName" v-model="itemName" />
          </div>
        </div>
        <div class="btn-box many mid">
          <button type="button" class="blueBtn cancellation" @click="reset">
            초기화<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          </button>
          <button type="button" class="blueBtn u-confirm" @click="load">
            조회<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          </button>
        </div>
      </div>

      <div class="contents-wrap" v-if="!loading">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="rows.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">답례품Q&amp;A - 답례품정보, 문의내용, 답변상태, 작성일로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col g_info_wrap" scope="col">답례품정보</th>
                    <th class="date-col" scope="col">문의내용</th>
                    <th class="date-col" scope="col">답변상태</th>
                    <th class="date-col" scope="col">작성일</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="q in rows" :key="q.inquiryId">
                    <td class="date-col g_info_wrap">
                      <div class="g_info__txt">
                        <div class="info_title"><router-link :to="`/gifts/${q.itemId}`">{{ q.itemName }}</router-link></div>
                      </div>
                    </td>
                    <td class="date-col">
                      <div v-if="!q.secret">{{ q.question }}</div>
                      <div v-else>🔒 비밀글입니다.</div>
                      <div v-if="q.answer" class="g_info__txt" style="margin-top: 6px">
                        <strong>답변:</strong> <span>{{ q.answer }}</span>
                      </div>
                    </td>
                    <td class="date-col">{{ q.statusLabel }}</td>
                    <td class="date-col">{{ q.createdDateDisplay }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="common_none" v-else>
            <p>작성한 답례품Q&amp;A 문의가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
@import '/css/mypage-status.css';
</style>

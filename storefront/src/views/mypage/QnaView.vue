<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage/inquiry.html(및 Thymeleaf 버전 qna/list.html) 재현. 목록은 테이블이 아니라
// FAQ 아코디언 - 제목을 누르면 그 자리에서 질문 전문과(답변이 있으면) 답변이 펼쳐진다.
// 캡차 이미지/음성듣기는 이미 순수 GET(이미지·텍스트)이라 admin 서비스 엔드포인트를 그대로
// 프록시 경유로 재사용한다(새로 만들지 않음).
const qnaGroups = ref({})
const inquiries = ref([])
const errorMessage = ref('')
const openIds = ref(new Set())

const searchDatePreset = ref('')
const searchStartDate = ref('')
const searchEndDate = ref('')

const qnaGroup = ref('')
const subject = ref('')
const question = ref('')
const secretFlag = ref(false)
const files = ref(null)
const captcha = ref('')
const captchaSrc = ref('')

function refreshCaptcha() {
  captchaSrc.value = api.assetUrl('admin', '/qna/captcha') + '?t=' + Date.now()
  captcha.value = ''
}

function listenCaptcha() {
  fetch(api.assetUrl('admin', '/qna/captcha/audio-text'), { credentials: 'include' })
    .then((r) => r.text())
    .then((text) => {
      if (!text || !window.speechSynthesis) return
      const utter = new SpeechSynthesisUtterance(text.split('').join(', '))
      utter.lang = 'ko-KR'
      utter.rate = 0.8
      window.speechSynthesis.cancel()
      window.speechSynthesis.speak(utter)
    })
}

async function load() {
  const params = new URLSearchParams()
  if (searchStartDate.value) params.set('searchStartDate', searchStartDate.value)
  if (searchEndDate.value) params.set('searchEndDate', searchEndDate.value)
  const data = await api.get('admin', `/api/qna?${params}`)
  qnaGroups.value = data.qnaGroups
  inquiries.value = data.inquiries
}

onMounted(() => {
  load()
  refreshCaptcha()
})

function toDateInput(d) {
  const pad2 = (n) => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}
function setSearchRange() {
  const value = searchDatePreset.value
  if (!value) {
    searchStartDate.value = ''
    searchEndDate.value = ''
    return
  }
  const end = new Date()
  const start = new Date()
  if (value === 'week-1') start.setDate(start.getDate() - 7)
  else if (value === 'month-1') start.setMonth(start.getMonth() - 1)
  else if (value === 'month-3') start.setMonth(start.getMonth() - 3)
  else if (value === 'month-6') start.setMonth(start.getMonth() - 6)
  searchStartDate.value = toDateInput(start)
  searchEndDate.value = toDateInput(end)
}

function toggle(qnaId) {
  const next = new Set(openIds.value)
  next.has(qnaId) ? next.delete(qnaId) : next.add(qnaId)
  openIds.value = next
}

function clearForm() {
  if (!confirm('1:1문의를 취소하시겠습니까?')) return
  qnaGroup.value = ''
  subject.value = ''
  question.value = ''
  files.value = null
  secretFlag.value = false
  refreshCaptcha()
}

async function submit() {
  errorMessage.value = ''
  const form = new FormData()
  form.set('qnaGroup', qnaGroup.value)
  form.set('subject', subject.value)
  form.set('question', question.value)
  form.set('secretFlag', String(secretFlag.value))
  form.set('captcha', captcha.value)
  if (files.value) {
    for (const f of files.value) form.append('files', f)
  }
  try {
    await api.postForm('admin', '/api/qna', form)
    clearForm()
    await load()
  } catch (e) {
    errorMessage.value = e.message
    refreshCaptcha()
  }
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
        1:1 문의
      </span>
      <h2 class="page-title-txt">1:1 문의</h2>
    </div>
  </section>

  <div class="contents-wrap" id="contents">
    <div class="center">
      <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

      <div class="s-contents list-page-wrapper">
        <div class="list-page-area">
          <form @submit.prevent="submit">
            <div class="list-page-body qna">
              <div class="page-title">
                <div class="title-select">
                  <div class="form-item">
                    <select id="qnaGroup" v-model="qnaGroup" required>
                      <option value="">말머리선택</option>
                      <option v-for="[code, label] in Object.entries(qnaGroups)" :key="code" :value="code">{{ label }}</option>
                    </select>
                  </div>
                  <div class="form-item">
                    <input id="subject" v-model="subject" type="text" placeholder="제목을 입력해주세요" maxlength="100" required />
                  </div>
                  <div class="form-item secret-check">
                    <input type="checkbox" id="secretFlag" v-model="secretFlag" />
                    <label for="secretFlag">비밀글로 등록(작성자 본인만 열람 가능, 고객센터 &gt; Q&amp;A 게시판에 공개됩니다)</label>
                  </div>
                </div>
              </div>
              <div class="page-contents">
                <textarea id="question" v-model="question" maxlength="500" required></textarea>
              </div>
              <div class="page-file-list qna">
                <div class="page-file-list-wrap">
                  <div class="file-list-label">
                    <input type="file" id="files" multiple @change="files = $event.target.files" />
                    <label for="files"><img class="icon-img" src="/images/icon/addfile-search.png" alt="첨부자료" />파일찾기</label>
                    <span class="grayTxt">jpg, gif, png, hwp, doc, ppt, pdf 등 이미지/문서파일만 등록 가능합니다.</span>
                  </div>
                </div>
                <div class="captcha-area">
                  <img :src="captchaSrc" class="captcha-img" alt="자동입력 방지문자" />
                  <button type="button" class="formBtn" @click="listenCaptcha">음성듣기</button>
                  <button type="button" class="formBtn" @click="refreshCaptcha">새로고침</button>
                  <div class="captcha-input-row">
                    <input type="text" v-model="captcha" placeholder="자동입력 방지문자를 입력해주세요" required autocomplete="off" />
                  </div>
                </div>
              </div>
            </div>
            <div class="btn-box inquiry many">
              <button type="button" class="blueBtn cancellation" @click="clearForm">취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
              <button type="submit" class="blueBtn u-confirm">등록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

  <div class="contents-wrap" id="listContents">
    <div class="center">
      <div class="s-contents list-wrapper qna">
        <div class="list-select-area">
          <header class="list-page-body inquiry">
            <form class="title-select" @submit.prevent="load">
              <div class="form-item">
                <select v-model="searchDatePreset" class="searchDate" @change="setSearchRange">
                  <option value="">찾고자 하는 날짜를 설정해 주세요.</option>
                  <option value="week-1">1주일</option>
                  <option value="month-1">1개월</option>
                  <option value="month-3">3개월</option>
                  <option value="month-6">6개월</option>
                </select>
              </div>
              <button type="submit" class="formBtn">조회</button>
            </form>
          </header>

          <div class="list-search-body inquiry faq">
            <ul class="list-search-group">
              <li class="list-items dropdown" :class="{ show: openIds.has(row.qnaId) }" v-for="row in inquiries" :key="row.qnaId">
                <div class="notice-header inquiry dropdown-toggle" tabindex="0" @click="toggle(row.qnaId)" @keydown.enter="toggle(row.qnaId)" @keydown.space.prevent="toggle(row.qnaId)">
                  <div class="notice-header-txt">
                    <img class="faq-icon" src="/images/icon/cli-icon_faq-q.png" alt="" />
                    <div class="notice-txt">
                      <button class="subject" type="button">{{ row.subject }}</button>
                      <div class="q_contents">{{ row.question }}</div>
                      <div class="q-add_file" v-if="row.files.length">
                        <button class="moreView" type="button" v-for="f in row.files" :key="f.qnaFileId">{{ f.orgFileName }}</button>
                      </div>
                    </div>
                  </div>
                  <div class="notice-header-status">
                    <div class="date-col">
                      <span class="pointblue" v-if="row.answer">답변완료</span>
                      <span class="pointRed" v-else>답변대기</span>
                    </div>
                    <div class="date-col">{{ row.createdDate ? row.createdDate.slice(0, 8) : '' }}</div>
                  </div>
                </div>
                <div class="notice-body dropdown-menu" :class="{ show: openIds.has(row.qnaId) }" v-if="row.answer">
                  <div class="notice-wrap">
                    <img class="faq-icon" src="/images/icon/cli-icon_faq-a.png" alt="" />
                    <div class="notice-txt">
                      <button class="subject" type="button">{{ row.answerTitle }}</button>
                      <div class="q_contents">{{ row.answer }}</div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
            <div class="list-none" v-if="!inquiries.length">
              <img src="/images/icon/non-list.png" alt="게시물이 없음" />
              1:1문의 정보가 존재하지 않습니다.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

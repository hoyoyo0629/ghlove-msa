<script setup>
// AS-IS qustnr/detail.html 재현 - 설문 참여 화면. 특정 설문(/survey/:id) 또는 현재
// 진행중 설문(/survey)을 받아 문항별 답변을 제출한다. AS-IS getQustnrByApi의 판정
// (로그인 필수·노출기간·1인 1회)은 admin SurveyApiController가 하고, 여기서는 그 결과에
// 따라 폼/안내를 보여준다. MSA 설문은 자유서술형 문항이라 답변도 텍스트다.
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/http'

const route = useRoute()
const router = useRouter()

const survey = ref(null)
const answers = ref({}) // qustnrQesitmSn -> 답변 텍스트
const loading = ref(true)
const notFound = ref(false)
const errorMessage = ref('')
const done = ref(false)

async function load() {
  loading.value = true
  notFound.value = false
  try {
    const path = route.params.id ? `/api/surveys/${route.params.id}` : '/api/surveys/active'
    const data = await api.get('admin', path)
    if (!data) {
      // active 조회에서 진행중 설문이 없으면 204 → null
      notFound.value = true
      survey.value = null
    } else {
      survey.value = data
      answers.value = Object.fromEntries((data.questions ?? []).map((q) => [q.qustnrQesitmSn, '']))
    }
  } catch {
    notFound.value = true
    survey.value = null
  } finally {
    loading.value = false
  }
}

async function submit() {
  errorMessage.value = ''
  if (!survey.value) return
  if (!survey.value.loggedIn) {
    router.push({ path: '/login', query: { target: route.fullPath } })
    return
  }
  const payload = (survey.value.questions ?? []).map((q) => ({
    qustnrQesitmSn: q.qustnrQesitmSn,
    rspnsCn: answers.value[q.qustnrQesitmSn] ?? '',
  }))
  try {
    await api.post('admin', `/api/surveys/${survey.value.qustnrSn}/responses`, payload)
    done.value = true
  } catch (e) {
    errorMessage.value = e.message || '설문 제출에 실패했습니다.'
  }
}

onMounted(load)
</script>

<template>
  <div id="contents" class="contents-page">
    <section class="center">
      <div class="page-title-box">
        <h2 class="page-title-txt">온라인 설문조사</h2>
      </div>

      <div v-if="loading" class="survey-msg">불러오는 중…</div>

      <div v-else-if="notFound || !survey" class="survey-msg">
        현재 진행중인 설문조사가 없습니다.
      </div>

      <div v-else-if="done" class="survey-msg">
        설문에 참여해 주셔서 감사합니다.
      </div>

      <div v-else-if="survey.alreadyResponded" class="survey-msg">
        이미 참여한 설문입니다. 참여해 주셔서 감사합니다.
      </div>

      <form v-else class="survey-form" @submit.prevent="submit">
        <h3 class="survey-form__title">{{ survey.qustnrSj }}</h3>
        <p v-if="survey.qustnrBgnDe || survey.qustnrEndDe" class="survey-form__period">
          설문기간: {{ survey.qustnrBgnDe }} ~ {{ survey.qustnrEndDe }}
        </p>

        <p v-if="!survey.loggedIn" class="survey-form__notice">
          설문 참여는 로그인 후 가능합니다. 제출 시 로그인 화면으로 이동합니다.
        </p>

        <ol class="survey-form__questions">
          <li v-for="q in survey.questions" :key="q.qustnrQesitmSn" class="survey-q">
            <label :for="'q' + q.qustnrQesitmSn" class="survey-q__label">{{ q.qestnCn }}</label>
            <textarea
              :id="'q' + q.qustnrQesitmSn"
              v-model="answers[q.qustnrQesitmSn]"
              class="survey-q__input"
              rows="3"
            ></textarea>
          </li>
        </ol>

        <p v-if="errorMessage" class="survey-form__error">{{ errorMessage }}</p>

        <div class="survey-form__actions">
          <button type="submit" class="krds-btn primary">제출</button>
        </div>
      </form>
    </section>
  </div>
</template>

<style scoped>
.survey-msg {
  padding: 60px 0;
  text-align: center;
  color: #555;
  font-size: 15px;
}
.survey-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px 0 60px;
}
.survey-form__title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
}
.survey-form__period {
  color: #777;
  font-size: 14px;
  margin-bottom: 16px;
}
.survey-form__notice {
  background: #fff7e6;
  border: 1px solid #ffe0a3;
  padding: 10px 12px;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 16px;
}
.survey-form__questions {
  list-style: none;
  padding: 0;
  margin: 0;
}
.survey-q {
  margin-bottom: 20px;
}
.survey-q__label {
  display: block;
  font-weight: 600;
  margin-bottom: 6px;
}
.survey-q__input {
  width: 100%;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 8px;
  box-sizing: border-box;
  font-size: 14px;
}
.survey-form__error {
  color: #d32f2f;
  font-size: 14px;
  margin: 8px 0;
}
.survey-form__actions {
  text-align: center;
  margin-top: 24px;
}
</style>

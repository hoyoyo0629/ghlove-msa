<script setup>
// AS-IS 설문 참여 화면(별도 프론트의 qustnr/detail_srvy.html) 재현 - 특정 설문(/survey/:id)
// 또는 현재 진행중 설문(/survey)을 받아 문항별 답변을 제출한다. AS-IS getQustnrByApi의 판정
// (로그인 필수·노출기간·1인 1회)은 admin SurveyApiController가 하고, 여기서는 그 결과에
// 따라 폼/안내를 보여준다.
//
// 2026-10-03 수정: 설문 데이터모델을 AS-IS(G_QESTNAR 계열)로 교체했는데 이 화면이 따라오지
// 않아 **응답이 빈 값으로 저장되고 있었다** - 보내는 키가 rspnsCn인데 API(AS-IS
// QustnrRspnsResult)가 받는 키는 qustnrIemSn/respondAnswerCn이라 서버에서 조용히 버려졌다.
// 또 모든 문항을 textarea로 그려서 객관식 선택지가 아예 보이지 않았다.
//   - 문항 종류(qestnTyCode)대로 rtype=객관식(선택지 라디오) / stype=주관식(textarea)
//   - 제출 payload를 AS-IS 필드명(qustnrQesitmSn / qustnrIemSn / respondAnswerCn)으로
//   - 답하지 않은 문항은 보내지 않는다(빈 행이 쌓이는 것을 막는다)
//   - 연계질문(parentSn)은 부모 문항 아래에 들여써 보여준다. AS-IS가 부모 답에 따라
//     조건부로 펼치는지는 그 프론트 소스가 AS-IS 저장소에 없어 확인하지 못했다(항상 노출).
// ETC_ANSWER_CN(기타 답변)은 AS-IS DTO·컬럼에는 있지만 입력 UI 근거를 찾지 못해 두지 않았다.
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/http'

const route = useRoute()
const router = useRouter()

const survey = ref(null)
// qustnrQesitmSn -> { qustnrIemSn, respondAnswerCn }
const answers = ref({})
const loading = ref(true)
const notFound = ref(false)
const errorMessage = ref('')
const done = ref(false)

const MULTIPLE_CHOICE = 'rtype'

function isTopLevel(q) {
  return q.parentSn === null || q.parentSn === undefined || q.parentSn === 0
}

/** 부모 문항 + 그 아래 연계질문 목록으로 묶는다. */
const questionGroups = computed(() => {
  const all = survey.value?.questions ?? []
  return all.filter(isTopLevel).map((parent) => ({
    parent,
    children: all.filter((c) => !isTopLevel(c) && c.parentSn === parent.qustnrQesitmSn),
  }))
})

function emptyAnswer() {
  return { qustnrIemSn: null, respondAnswerCn: '' }
}

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
      answers.value = Object.fromEntries(
        (data.questions ?? []).map((q) => [q.qustnrQesitmSn, emptyAnswer()]),
      )
    }
  } catch {
    notFound.value = true
    survey.value = null
  } finally {
    loading.value = false
  }
}

/** 답한 문항만 AS-IS 필드명으로 만든다(객관식은 선택지 id, 주관식은 텍스트). */
function buildPayload() {
  return (survey.value.questions ?? [])
    .map((q) => {
      const a = answers.value[q.qustnrQesitmSn] ?? emptyAnswer()
      if (q.qestnTyCode === MULTIPLE_CHOICE) {
        return a.qustnrIemSn
          ? { qustnrQesitmSn: q.qustnrQesitmSn, qustnrIemSn: a.qustnrIemSn, respondAnswerCn: null }
          : null
      }
      const text = (a.respondAnswerCn ?? '').trim()
      return text
        ? { qustnrQesitmSn: q.qustnrQesitmSn, qustnrIemSn: null, respondAnswerCn: text }
        : null
    })
    .filter(Boolean)
}

async function submit() {
  errorMessage.value = ''
  if (!survey.value) return
  if (!survey.value.loggedIn) {
    router.push({ path: '/login', query: { target: route.fullPath } })
    return
  }
  const payload = buildPayload()
  if (payload.length === 0) {
    errorMessage.value = '응답을 입력해 주세요.'
    return
  }
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
          <li v-for="g in questionGroups" :key="g.parent.qustnrQesitmSn" class="survey-q">
            <p class="survey-q__label">{{ g.parent.qestnCn }}</p>

            <!-- 객관식: 선택지 라디오 -->
            <ul v-if="g.parent.qestnTyCode === 'rtype'" class="survey-q__choices">
              <li v-for="c in g.parent.choices" :key="c.qustnrIemSn">
                <input
                  :id="'c' + g.parent.qustnrQesitmSn + '-' + c.qustnrIemSn"
                  v-model="answers[g.parent.qustnrQesitmSn].qustnrIemSn"
                  type="radio"
                  :name="'q' + g.parent.qustnrQesitmSn"
                  :value="c.qustnrIemSn"
                />
                <label :for="'c' + g.parent.qustnrQesitmSn + '-' + c.qustnrIemSn">{{ c.iemCn }}</label>
              </li>
            </ul>

            <!-- 주관식: 텍스트 -->
            <textarea
              v-else
              :id="'q' + g.parent.qustnrQesitmSn"
              v-model="answers[g.parent.qustnrQesitmSn].respondAnswerCn"
              class="survey-q__input"
              rows="3"
            ></textarea>

            <!-- 연계질문 -->
            <ul v-if="g.children.length" class="survey-q__children">
              <li v-for="c in g.children" :key="c.qustnrQesitmSn" class="survey-q">
                <p class="survey-q__label">{{ c.qestnCn }}</p>
                <ul v-if="c.qestnTyCode === 'rtype'" class="survey-q__choices">
                  <li v-for="i in c.choices" :key="i.qustnrIemSn">
                    <input
                      :id="'c' + c.qustnrQesitmSn + '-' + i.qustnrIemSn"
                      v-model="answers[c.qustnrQesitmSn].qustnrIemSn"
                      type="radio"
                      :name="'q' + c.qustnrQesitmSn"
                      :value="i.qustnrIemSn"
                    />
                    <label :for="'c' + c.qustnrQesitmSn + '-' + i.qustnrIemSn">{{ i.iemCn }}</label>
                  </li>
                </ul>
                <textarea
                  v-else
                  :id="'q' + c.qustnrQesitmSn"
                  v-model="answers[c.qustnrQesitmSn].respondAnswerCn"
                  class="survey-q__input"
                  rows="3"
                ></textarea>
              </li>
            </ul>
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
.survey-q__choices {
  list-style: none;
  padding: 0;
  margin: 0;
}
.survey-q__choices li {
  padding: 3px 0;
  font-size: 14px;
}
.survey-q__choices label {
  margin-left: 6px;
  cursor: pointer;
}
.survey-q__children {
  list-style: none;
  margin: 14px 0 0 16px;
  padding: 0 0 0 12px;
  border-left: 2px solid #eee;
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

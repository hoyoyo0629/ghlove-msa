<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// 기존 donation 서비스의 /donations/offline(offline.html, SFR-003 "기탁서(오프라인) 기부 등록")은
// 사이트 헤더/디자인이 전혀 적용되지 않은 내부용 임시 폼이었고, 로그인 세션과 무관하게 회원ID를
// 직접 입력받는 보안 결함이 있었다(누구나 남의 이름으로 기부를 등록할 수 있었음). admin의 offgive
// (현장접수) 모듈이 운영자용 동일 기능을 RBAC까지 갖춰 완전히 처리하므로, 여기서는 정식 마이페이지
// 화면으로 새로 만들며 다른 /api/my/... 엔드포인트와 동일하게 로그인한 본인 명의로만 등록되도록
// 고쳤다(사용자 확인 완료). 실제 검증/저장 로직(연간한도 검증, REQUESTED 상태 생성)은
// DonationService.registerOfflineDonation()을 그대로 재사용 - 등록 후에는 일반기부와 동일하게
// 마이페이지 "기부내역조회"에서 결제완료 처리를 진행하면 된다.
const router = useRouter()

const loading = ref(true)
const errorMessage = ref('')
const successCntrSn = ref('')

const allLocgovs = ref([])
const upperLocgovCode = ref('')
const locgovCode = ref('')
const amountDisplay = ref('')
const amount = ref(0)
const rceptBankNm = ref('')
const rceptBankCode = ref('')

async function load() {
  loading.value = true
  try {
    allLocgovs.value = await api.get('donation', '/api/locgovs')
  } finally {
    loading.value = false
  }
}
onMounted(load)

const provinces = computed(() => {
  const map = new Map()
  allLocgovs.value.forEach((l) => {
    if (!map.has(l.upperLocgovCode)) map.set(l.upperLocgovCode, l.upperLocgovNm)
  })
  return [...map.entries()]
})
const filteredLocgovs = computed(() =>
  allLocgovs.value.filter((l) => !upperLocgovCode.value || l.upperLocgovCode === upperLocgovCode.value),
)
const selectedLocgovName = computed(() => {
  const l = allLocgovs.value.find((x) => x.locgovCode === locgovCode.value)
  return l ? `${l.upperLocgovNm ?? ''} ${l.locgovNm}`.trim() : ''
})

function onProvinceChanged() {
  locgovCode.value = ''
}

function onAmountInput(e) {
  const raw = Number(String(e.target.value).replace(/[^\d]/g, '')) || 0
  amount.value = raw
  amountDisplay.value = raw ? new Intl.NumberFormat('ko-KR').format(raw) : ''
}

async function submit() {
  errorMessage.value = ''
  if (!locgovCode.value) {
    alert('기부 지자체를 선택해 주세요.')
    return
  }
  if (amount.value < 1) {
    alert('기부금액을 입력해 주세요.')
    return
  }
  try {
    const res = await api.post('donation', '/api/my/donations/offline', {
      locgovCode: locgovCode.value,
      amount: amount.value,
      rceptBankCode: rceptBankCode.value || null,
      rceptBankNm: rceptBankNm.value || null,
    })
    successCntrSn.value = res.cntrSn
  } catch (e) {
    errorMessage.value = e.message
  }
}

function resetForm() {
  successCntrSn.value = ''
  upperLocgovCode.value = ''
  locgovCode.value = ''
  amount.value = 0
  amountDisplay.value = ''
  rceptBankNm.value = ''
  rceptBankCode.value = ''
}
</script>

<template>
  <section class="center donation_steps" v-if="!loading">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage/donations">기부내역 조회</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기탁서(오프라인) 등록
      </span>
      <h2 class="page-title-txt">기탁서(오프라인) 등록</h2>
      <p class="point-txt">우편·방문 등으로 은행에 직접 입금하신 기부금을 등록합니다.</p>
    </div>
  </section>

  <section id="contents" class="input_area" v-if="!loading">
    <div v-if="successCntrSn" class="center" style="max-width: 900px; margin: 0 auto">
      <div class="info-field-items selected-info">
        <img src="/images/icon/cli-icon_donation-ok.png" alt="" />
        <div class="selected-info-txt">
          <p>기탁서 등록이 접수되었습니다.<br />접수번호 : <span class="pointblue">{{ successCntrSn }}</span></p>
          <p class="s-txt">입금이 확인되면 마이페이지 &gt; 기부내역조회에서 결제완료 처리를 진행해 주세요.</p>
        </div>
      </div>
      <div class="btn-box many mb_b60" style="margin-top: 24px">
        <button type="button" class="formBtn" @click="router.push('/mypage/donations')">기부내역 조회로 이동</button>
        <button type="button" class="blueBtn u-confirm" @click="resetForm">기탁서 추가 등록</button>
      </div>
    </div>

    <form v-else id="offline-donate-form" class="donation" @submit.prevent="submit">
      <fieldset>
        <div class="donation-info-area joind center">
          <p class="error" v-if="errorMessage" style="margin-bottom: 20px">{{ errorMessage }}</p>

          <div class="add-info-group">
            <div class="lable-field"><h3 style="width: 100%">기부지자체</h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="offlineUpperLocgov" class="flied-title">지역선택</label>
                  <span class="form-field selectCity">
                    <div class="select-location-items">
                      <select id="offlineUpperLocgov" aria-label="시·도 선택" v-model="upperLocgovCode" @change="onProvinceChanged">
                        <option value="">시·도 선택</option>
                        <option v-for="[code, name] in provinces" :key="code" :value="code">{{ name }}</option>
                      </select>
                    </div>
                    <label for="offlineLocgov" class="sr-only">시·군·구 선택</label>
                    <div class="select-location-items">
                      <select id="offlineLocgov" aria-label="시·군·구 선택" v-model="locgovCode" required>
                        <option value="">시·군·구 선택</option>
                        <option v-for="l in filteredLocgovs" :key="l.locgovCode" :value="l.locgovCode">
                          {{ l.upperLocgovNm ? l.upperLocgovNm + ' ' : '' }}{{ l.locgovNm }}
                        </option>
                      </select>
                    </div>
                  </span>
                </div>
                <div class="info-field-items selected-info" v-if="locgovCode">
                  <img src="/images/icon/cli-icon_donation-ok.png" alt="" />
                  <div class="selected-info-txt">
                    <p><span class="pointblue">{{ selectedLocgovName }}</span>에 기탁서를 등록합니다.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="line"></div>

          <div class="add-info-group">
            <div class="lable-field"><h3 style="width: 100%">기부금</h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="offlineAmount" class="flied-title">기부금액</label>
                  <div class="form-field">
                    <div class="m-field">
                      <span class="price-input">
                        <input
                          type="text"
                          id="offlineAmount"
                          class="user_input price"
                          inputmode="numeric"
                          :value="amountDisplay"
                          @input="onAmountInput"
                          aria-label="기부액"
                          autocomplete="off"
                        />
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="line"></div>

          <div class="add-info-group">
            <div class="lable-field"><h3 style="width: 100%">입금 정보<span class="s-txt d-block">(선택)</span></h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="rceptBankNm" class="flied-title">입금자명 / 수납은행 메모</label>
                  <div class="form-field">
                    <input
                      type="text"
                      id="rceptBankNm"
                      v-model="rceptBankNm"
                      placeholder="예: 홍길동 / 국민은행 입금"
                      style="width: 100%; max-width: 360px"
                    />
                  </div>
                </div>
                <div class="info-field-items">
                  <label for="rceptBankCode" class="flied-title">수납은행 코드</label>
                  <div class="form-field">
                    <input type="text" id="rceptBankCode" v-model="rceptBankCode" style="width: 100%; max-width: 200px" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="line"></div>

          <div class="info-field-items selected-info">
            <div class="selected-info-txt">
              <p class="note-mark">
                ※ 우편 또는 방문 등으로 기탁서를 작성해 은행에 직접 입금하신 경우, 접수 확인용으로 등록해 주세요. 온라인 기부와 동일하게
                연간 기부한도 검증을 거치며, 입금이 확인되면 마이페이지 &gt; 기부내역조회에서 결제완료 처리를 진행합니다.
              </p>
            </div>
          </div>
        </div>

        <div class="center">
          <div class="btn-box many mb_b60">
            <button type="submit" class="blueBtn u-confirm" id="offlineSubmitBtn">
              기탁서 등록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="기탁서 등록" /></span>
            </button>
          </div>
        </div>
      </fieldset>
    </form>
  </section>
</template>

<style>
#offline-donate-form input[type='radio']:checked {
  background-image: url('/images/icon/cli-icon_radio-c.png') !important;
}
</style>

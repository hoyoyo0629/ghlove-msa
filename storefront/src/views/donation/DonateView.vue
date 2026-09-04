<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS donation-main.html 재현(donation 서비스 donate.html과 동일 출처, GNB "기부 > 자치단체에
// 기부하기"). 의도적 축소: 거주지 확인은 행정정보공동이용센터 연계가 없어(다른 외부연계와 동일한
// mock-gated 대상) 마이페이지에 등록된 주소로 판정한다 - 주민등록번호 입력칸 UI는 그대로 두되
// 서버가 실제로 검증에 쓰지는 않는다(백엔드 주석 참고). "특정사업 선택" 드롭다운에서 사업을 고르면
// 지정기부(POST /api/donate/designated, 응원메시지 포함)로 제출된다 - 기존 Thymeleaf donate.html은
// 이 드롭다운을 상세페이지 "보러가기" 이동에만 쓰고 실제로는 항상 일반기부(/donate)만 제출했다
// (DonationController의 POST /donate/designated 자체는 이미 있었지만 그걸 호출하는 폼이 어디에도
// 없었다 - 지정기부사업 상세 라운드에서 실제로 발견, 응원메시지 탭을 채우려면 필요).
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const errorMessage = ref('')
const form = ref(null)

const upperLocgovCode = ref('')
const locgovCode = ref('')
const projectJump = ref('0')
const cheerMsg = ref('')
const amountDisplay = ref('')
const amount = ref(0)
const presentType = ref('100')
const psitnLocgovCode = ref('')
const juminNo2 = ref('')
const checkA = ref(false)
const checkB = ref(false)

const residenceVerified = ref(false)
const selectedRegionTxt = ref('')
const userRegionTxt = ref('')
const pointRatePercent = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await api.get('donation', '/api/donate/form')
    form.value = data
    if (route.query.locgovCode) {
      const target = data.locgovs.find((l) => l.locgovCode === route.query.locgovCode)
      if (target) {
        upperLocgovCode.value = target.upperLocgovCode
        locgovCode.value = target.locgovCode
        await onLocgovChanged()
        if (route.query.prjId && filteredProjects.value.some((p) => String(p.dsgnDntnBizId) === route.query.prjId)) {
          projectJump.value = route.query.prjId
        }
      }
    }
  } catch {
    router.push({ path: '/login', query: { target: route.fullPath } })
  } finally {
    loading.value = false
  }
}
onMounted(load)

const filteredLocgovs = computed(() =>
  (form.value?.locgovs ?? []).filter((l) => !upperLocgovCode.value || l.upperLocgovCode === upperLocgovCode.value),
)
const filteredProjects = computed(() =>
  (form.value?.projects ?? []).filter((p) => p.lclgvCd === locgovCode.value),
)
const selectedLocgov = computed(() => (form.value?.locgovs ?? []).find((l) => l.locgovCode === locgovCode.value))
const selectedLocgovName = computed(() =>
  selectedLocgov.value ? `${selectedLocgov.value.upperLocgovNm ?? ''} ${selectedLocgov.value.locgovNm}`.trim() : '',
)
const pointEstimateTxt = computed(() => {
  if (presentType.value !== '100' || pointRatePercent.value == null || !amount.value) return ''
  const points = Math.floor((amount.value * pointRatePercent.value) / 100)
  return `( ${new Intl.NumberFormat('ko-KR').format(points)} 포인트 적립예상 )`
})

function onProvinceChanged() {
  locgovCode.value = ''
  onLocgovChanged()
}

async function onLocgovChanged() {
  residenceVerified.value = false
  psitnLocgovCode.value = ''
  projectJump.value = '0'
  cheerMsg.value = ''
  pointRatePercent.value = null
  if (!locgovCode.value) return
  try {
    const res = await api.get('donation', `/api/donate/point-rate?locgovCode=${encodeURIComponent(locgovCode.value)}`)
    pointRatePercent.value = res.rate != null ? Number(res.rate) : null
  } catch {
    pointRatePercent.value = null
  }
}

function goProjectDetail() {
  if (projectJump.value === '0') return
  router.push(`/designated-donation/${projectJump.value}`)
}

async function verifyResidence() {
  if (!locgovCode.value) {
    alert('기부지자체를 먼저 선택해 주세요.')
    return
  }
  try {
    const res = await api.post('donation', '/api/donate/verify-residence', { locgovCode: locgovCode.value })
    residenceVerified.value = true
    psitnLocgovCode.value = res.psitnLocgovCode
    selectedRegionTxt.value = selectedLocgovName.value
    userRegionTxt.value = res.userRegion
    alert(`귀하는 ${selectedLocgovName.value}에 납부가 가능합니다.`)
  } catch (e) {
    if (e.message.includes('로그인')) {
      router.push({ path: '/login', query: { target: route.fullPath } })
      return
    }
    alert(e.message)
  }
}

function addAmount(add, reset) {
  amount.value = reset ? 0 : amount.value + add
  amountDisplay.value = amount.value ? new Intl.NumberFormat('ko-KR').format(amount.value) : ''
}

function onAmountInput(e) {
  const raw = Number(String(e.target.value).replace(/[^\d]/g, '')) || 0
  amount.value = raw
  amountDisplay.value = raw ? new Intl.NumberFormat('ko-KR').format(raw) : ''
}

function toggleAllAgree(e) {
  checkA.value = e.target.checked
  checkB.value = e.target.checked
}

async function submit() {
  errorMessage.value = ''
  if (!locgovCode.value) {
    alert('기부지자체 시·군·구가 선택되지 않았습니다.')
    return
  }
  if (!residenceVerified.value) {
    alert('거주지 확인이 되지 않았습니다.')
    return
  }
  if (!checkA.value) {
    alert('고용관계 기부 납부여부 항목을 체크해주시기 바랍니다.')
    return
  }
  if (!checkB.value) {
    alert('기부자 확인사항을 체크해주시기 바랍니다.')
    return
  }
  if (amount.value < 100) {
    alert('기부금액은 최소 100원입니다.')
    return
  }
  if (amount.value % 100 !== 0) {
    alert('기부 금액 단위는 100원 단위입니다.')
    return
  }
  try {
    if (projectJump.value !== '0') {
      await api.post('donation', '/api/donate/designated', {
        dsgnDntnBizId: Number(projectJump.value),
        amount: amount.value,
        cheerMsg: cheerMsg.value || null,
      })
    } else {
      await api.post('donation', '/api/donate', {
        locgovCode: locgovCode.value,
        amount: amount.value,
        psitnLocgovCode: psitnLocgovCode.value,
        presentType: presentType.value,
      })
    }
    router.push('/mypage/donations')
  } catch (e) {
    errorMessage.value = e.message
  }
}

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="center donation_steps" v-if="!loading && form">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부하기
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부하기
      </span>
      <h2 class="page-title-txt">기부하기</h2>
    </div>
  </section>

  <section id="contents" class="input_area" v-if="!loading && form">
    <p class="error" v-if="errorMessage" style="max-width: 900px; margin: 0 auto 20px">{{ errorMessage }}</p>

    <form id="donate-form" class="donation" @submit.prevent="submit">
      <fieldset>
        <div class="donation-info-area joind center">
          <div class="add-info-group">
            <div class="lable-field"><h3 style="width: 100%">기부지자체</h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="upperLocgovCode" class="flied-title">지역선택</label>
                  <span class="form-field selectCity">
                    <div class="select-location-items">
                      <select aria-label="시·도 선택" id="upperLocgovCode" v-model="upperLocgovCode" @change="onProvinceChanged">
                        <option value="">시·도 선택</option>
                        <option v-for="[code, name] in Object.entries(form.provinces)" :key="code" :value="code">{{ name }}</option>
                      </select>
                    </div>
                    <label for="locgovCode" class="sr-only">시·군·구 선택</label>
                    <div class="select-location-items">
                      <select aria-label="시·군·구 선택" id="locgovCode" v-model="locgovCode" required @change="onLocgovChanged">
                        <option value="">시·군·구 선택</option>
                        <option v-for="l in filteredLocgovs" :key="l.locgovCode" :value="l.locgovCode">
                          {{ l.upperLocgovNm ? l.upperLocgovNm + ' ' : '' }}{{ l.locgovNm }}
                        </option>
                      </select>
                    </div>
                  </span>
                </div>
                <div class="info-field-items" v-if="locgovCode">
                  <label for="projectJump" class="flied-title">특정사업 선택</label>
                  <span class="form-field selectCity" style="align-items: center">
                    <div class="select-location-items">
                      <select aria-label="특정사업 선택" id="projectJump" v-model="projectJump" style="padding-right: 40px">
                        <option value="0">자치단체에 기부하기</option>
                        <option v-for="p in filteredProjects" :key="p.dsgnDntnBizId" :value="String(p.dsgnDntnBizId)">{{ p.dsgnDntnBizTtl }}</option>
                      </select>
                    </div>
                    <button class="formBtn" type="button" v-if="projectJump !== '0'" @click="goProjectDetail">보러가기</button>
                  </span>
                </div>
                <div class="info-field-items" v-if="projectJump !== '0'">
                  <label for="cheerMsg" class="flied-title">응원메시지</label>
                  <span class="form-field">
                    <textarea
                      id="cheerMsg"
                      v-model="cheerMsg"
                      maxlength="100"
                      rows="2"
                      placeholder="특정사업 상세페이지 응원메시지(기부내역) 탭에 표시됩니다. (선택, 최대 100자)"
                      style="width: 100%; resize: vertical"
                    ></textarea>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="line"></div>

          <div class="add-info-group">
            <div class="lable-field"><h3 style="width: 100%">거주지 확인</h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items" style="color: blue">주민등록번호 또는 외국인등록번호(거소신고번호)뒷자리를 입력하세요.</div>
                <div class="info-field-items dtar">
                  <label for="inputUserName" class="sr-only">이름</label>
                  <input id="inputUserName" type="text" :value="form.userName" readonly aria-label="이름" />
                  <div class="form-field userNum">
                    <label for="juminNo1" class="sr-only">주민등록번호 앞자리</label>
                    <input id="juminNo1" type="text" maxlength="6" readonly aria-label="주민등록번호 앞자리"
                      :value="form.birthday && form.birthday.length === 8 ? form.birthday.substring(2, 8) : form.birthday" />
                    <span class="s-txt">-</span>
                    <label for="juminNo2" class="sr-only">주민등록번호 뒷자리</label>
                    <input id="juminNo2" type="password" maxlength="7" aria-label="주민등록번호 뒷자리" autocomplete="off" v-model="juminNo2" />
                  </div>
                  <button class="formBtn" type="button" id="postBtn" @click="verifyResidence">주소확인하기</button>
                </div>

                <div class="info-field-items selected-info" v-if="!residenceVerified">
                  <img src="/images/icon/cli-icon_donation-warning.png" alt="" />
                  <div class="selected-info-txt">
                    <p v-if="locgovCode">
                      <span><span class="pointblue">{{ selectedLocgovName }}</span>에 기부하기를 선택하셨습니다.</span>
                    </p>
                    <p class="pointRed">※앞자리 6자리가 본인과 다르다면 생년월일을 변경하셔야 정상적으로 기부가 가능합니다.</p>
                    <router-link style="color: blue" to="/mypage/profile">마이페이지&gt;회원정보수정 화면으로 이동</router-link>
                  </div>
                </div>
                <div class="info-field-items selected-info" v-else>
                  <img src="/images/icon/cli-icon_donation-ok.png" alt="" />
                  <div class="selected-info-txt">
                    <p>귀하는 <span class="pointblue">{{ selectedRegionTxt }}</span>에 기부가 가능합니다.<br />해당 주소지 : {{ userRegionTxt }}</p>
                  </div>
                </div>

                <div class="info-field-items selected-info">
                  <div class="selected-info-txt">
                    <p class="note-mark">
                      ※ 고향사랑e음은 고향사랑기부금법 제4조, 제8조에 따라 주소지확인과 부과정보 등록 및 조세특례제한법 제58조에 따라 세액공제를
                      위하여 개인정보를 처리하고 있습니다. 또한, 고향사랑e음은 주민등록번호를 저장하지 않습니다.
                    </p>
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
                  <label for="cntrAmtDisplay" class="flied-title">기부금액 설정</label>
                  <div class="form-field">
                    <ul class="give_cntr">
                      <li><button class="give_cntr_amt" type="button" @click="addAmount(10000)">10,000</button></li>
                      <li><button class="give_cntr_amt" type="button" @click="addAmount(50000)">50,000</button></li>
                      <li><button class="give_cntr_amt" type="button" @click="addAmount(100000)">100,000</button></li>
                      <li><button class="give_cntr_refresh" type="button" @click="addAmount(0, true)">초기화</button></li>
                    </ul>
                    <div class="m-field">
                      <span class="price-input">
                        <input type="text" id="cntrAmtDisplay" class="user_input price" inputmode="numeric"
                          :value="amountDisplay" @input="onAmountInput" aria-label="기부액" autocomplete="off" />
                      </span>
                    </div>
                    <span class="num_txt">{{ pointEstimateTxt }}</span>
                  </div>
                </div>
                <div class="info-field-items">
                  <label for="doLimit" class="flied-title d-none d-md-block">기부가능 한도</label>
                  <div class="form-field">
                    <div class="m-field">
                      <span class="price-input limit" data-label="기부가능한도">
                        <input id="doLimit" class="price" type="text" readonly aria-label="기부가능한도" :value="formatN(form.remainingLimit)" />
                      </span>
                    </div>
                    <span class="s-txt text-right" v-if="form.annualLimit != null">( 개인 최대 기부 한도 금액 : 연 {{ formatN(form.annualLimit) }}원 )</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="line"></div>

          <div class="add-info-group">
            <div class="lable-field"><h3>답례품<span class="d-inline d-md-block"></span>제공 여부</h3></div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="goodsY" class="flied-title sr-only">답례품</label>
                  <div class="form-field">
                    <div class="m-field radio_b">
                      <div>
                        <input type="radio" name="presentType" id="goodsY" value="100" v-model="presentType" />
                        <label for="goodsY">답례품을 제공 받음</label>
                      </div>
                      <div>
                        <input type="radio" name="presentType" id="goodsN" value="300" v-model="presentType" />
                        <label for="goodsN">답례품을 제공 받지 않음</label>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="info-field-items">
                  <ul>
                    <li class="s-txt">알림서비스는 국민비서 회원에게 제공되니 회원가입 해 주시기 바랍니다.</li>
                    <li class="s-txt">
                      <a href="https://www.ips.go.kr/pot/forwardMain.do" target="_blank" rel="noopener" style="text-decoration: underline; color: blue">
                        <strong>국민비서 가입하기</strong></a
                      >
                      국민비서 홈 &gt; 알림설정(기타-고향사랑e음 안내 알림 선택)
                    </li>
                    <li class="s-txt">※ 알림서비스 설정 후 다음날부터 되는 점 참고 바랍니다.</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
          <div class="line"></div>

          <div class="accept-terms-item">
            <div class="accept-check last_check" style="margin-bottom: 25px">
              <input type="checkbox" id="checkAll" @change="toggleAllAgree" />
              <label for="checkAll" style="font-weight: bold">전체 동의</label>
            </div>
            <div class="accept-check last_check" style="padding-left: 15px; margin-bottom: 15px">
              <input type="checkbox" id="checkA" class="agree-item" v-model="checkA" />
              <label for="checkA"
                >공무원, 공공기관 및 지방자치단체 간 업무나 고용관계에 의한 기부금 납부가 아님을 확인합니다.<br />
                <span class="s-txt">※ 고향사랑 기부금법상 기부금 강요·독려임이 밝혀질 경우 모금이 제한될 수 있습니다.</span></label
              >
            </div>
            <div class="accept-check last_check" style="padding-left: 15px; margin-bottom: 15px">
              <input type="checkbox" id="checkB" class="agree-item" v-model="checkB" />
              <label for="checkB" style="word-break: break-all">
                기부자의 안내 사항을 확인합니다.<br />
                <span class="s-txt" style="margin-top: 5px; margin-left: 88px">
                  <span style="margin-left: -88px; font-weight: bold; color: #5a5a5a">(기부금의 반환) </span>
                  기부한 기부금은 반환·취소되지 않습니다.</span
                ><br />
                <span class="s-txt" style="margin-left: 58px">
                  <span style="margin-left: -58px; font-weight: bold; color: #5a5a5a">(세액공제) </span>
                  <span class="pointRed">기부자 본인이 기부한 당해년도에 한해서만 세액공제</span>되며, 당해년도 결정세액이 없는 경우(전액
                  환급대상인 경우) 세액공제가 적용되지 않습니다.<br />
                  ＊기부자 외 <span class="pointRed">소득이 없는 배우자, 직계존비속</span> 등의 기부금은 세액공제 불가</span
                >
                <span class="s-txt" style="margin-left: 125px">
                  <span style="margin-left: -125px; font-weight: bold; color: #5a5a5a">(특정사업에 기부하기) </span>
                  특정사업에 기부하는 경우<br
                /></span>
                <span class="s-txt" style="margin-left: 58px">
                  - 목표액을 초과하는 모금액은 다른 고향사랑기금 사업 재원으로 활용됩니다.<br />
                  - 목표액 미달시 사업의 변경(축소, 폐지)이 가능하며, 폐지되는 경우 모금액은 타 기금사업 재원으로 활용됩니다.</span
                >
              </label>
            </div>
          </div>
        </div>

        <div class="center">
          <div class="btn-box many mb_b60">
            <button type="submit" class="blueBtn u-confirm" id="submitBtn">
              기부금 납부하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="기부금 납부하기" /></span>
            </button>
          </div>
        </div>
      </fieldset>
    </form>

    <div class="notice-box center">
      <div class="notice-area">
        <div class="notice-img"><img src="/images/cli_notice.png" alt="" /></div>
        <div class="notice-area research-area">
          <div class="notice-group">
            <div class="notice-title">
              <span>고향사랑 기부금은 지방자치단체의</span>
              <strong>4가지 사업 목적</strong>으로만 사용됩니다.
            </div>
            <ul class="notice-list">
              <li><span class="l_num">1</span> <span class="l_txt">사회적 취약계층의 지원 및 청소년의 육성ㆍ보호</span></li>
              <li><span class="l_num">2</span> <span class="l_txt">지역 주민의 문화ㆍ예술ㆍ보건 등의 증진</span></li>
              <li><span class="l_num">3</span> <span class="l_txt">시민참여, 자원봉사 등 지역공동체 활성화 지원</span></li>
              <li><span class="l_num">4</span> <span class="l_txt">그 밖에 주민의 복리 증진에 필요한 사업의 추진</span></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
/* default_ali.css의 라디오 체크 아이콘이 '/static/images/...' 절대경로로 박혀있는데, 이
   프로젝트는 static 리소스를 루트 경로('/images/...')로 서빙한다(default_ali.css의 기존
   버그, 원래 Thymeleaf donate.html도 이 화면 전용으로만 보정했다 - 공용 CSS는 다른 화면들도
   참조하므로 건드리지 않는다). */
#donate-form input[type='radio']:checked {
  background-image: url('/images/icon/cli-icon_radio-c.png') !important;
}
</style>

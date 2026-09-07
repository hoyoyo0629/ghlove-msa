<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

const router = useRouter()

// AS-IS mypage/cntrList.html(및 Thymeleaf 버전 my.html) 재현. 결제완료 처리/취소 버튼은
// AS-IS에는 없지만(실 PG 연동이라 결제창에서 바로 완료됨) 이 MSA는 completeDonation()을
// 수동으로 트리거해야 해서 "처리" 컬럼에 남겨둔다.
const donations = ref([])
const totalCntrAmt = ref(0)
const years = ref([])
const allLocgovs = ref([])
const errorMessage = ref('')

const year = ref('')
const period = ref('ALL')
const upperLocgovCode = ref('')
const locgovCode = ref('')

const provinces = computed(() => {
  const map = new Map()
  allLocgovs.value.forEach((l) => { if (!map.has(l.upperLocgovCode)) map.set(l.upperLocgovCode, l.upperLocgovNm) })
  return [...map.entries()]
})
const filteredLocgovs = computed(() =>
  allLocgovs.value.filter((l) => !upperLocgovCode.value || l.upperLocgovCode === upperLocgovCode.value),
)

async function search() {
  errorMessage.value = ''
  const params = new URLSearchParams()
  if (year.value) params.set('year', year.value)
  params.set('period', period.value)
  if (upperLocgovCode.value) params.set('upperLocgovCode', upperLocgovCode.value)
  if (locgovCode.value) params.set('locgovCode', locgovCode.value)
  const data = await api.get('donation', `/api/my/donations?${params}`)
  donations.value = data.donations
  totalCntrAmt.value = data.totalCntrAmt
  years.value = data.years
  allLocgovs.value = data.locgovs
}
onMounted(search)

function reset() {
  year.value = ''
  period.value = 'ALL'
  upperLocgovCode.value = ''
  locgovCode.value = ''
  search()
}

// SFR-003 "기부금 납부 시 답례품 선택 기능 추가" - 답례품을 받기로 한(presentType '100') 기부는
// 결제완료 직후 답례품 선택 화면으로 안내한다.
async function complete(cntrSn) {
  try {
    const res = await api.post('donation', `/api/my/donations/${cntrSn}/complete`)
    if (res.presentType === '100') {
      router.push({ name: 'donate-gift-select', query: { locgovCode: res.locgovCode } })
      return
    }
    await search()
  } catch (e) {
    errorMessage.value = e.message
  }
}
async function cancel(cntrSn) {
  try {
    await api.post('donation', `/api/my/donations/${cntrSn}/cancel`)
    await search()
  } catch (e) {
    errorMessage.value = e.message
  }
}

function formatAmount(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
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
        기부내역 조회
      </span>
      <h2 class="page-title-txt">기부내역 조회</h2>
      <p class="point-txt">해당 내역은 고향사랑 기부에만 한정된 내역입니다.</p>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div class="contents-wrap myDonation">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

        <div>
          <div class="s-contents mypageS">
            <div class="mypage-top">
              <div class="mypage-top-list">
                <label class="search-title" for="totalAmt">
                  <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="총 기부금액" />
                  <span class="label-title"> 총 기부금액</span>
                </label>
                <span class="m-field">
                  <input type="text" id="totalAmt" readonly :value="formatAmount(totalCntrAmt)" /><span class="s_txt">원</span>
                </span>
                <span class="s_txt">(실 납부액 기준)</span>
              </div>
            </div>

            <div class="line"></div>

            <div class="mypage-top">
              <label class="search-title">검색</label>
              <div class="selected-field">
                <div class="selected-field-line">
                  <div class="selected-item">
                    <select v-model="year" aria-label="년도 선택">
                      <option value="">년도</option>
                      <option v-for="y in years" :key="y" :value="y">{{ y }}년</option>
                    </select>
                  </div>
                  <div class="formbtn-box period-radio">
                    <label><input type="radio" value="ALL" v-model="period" /> 전체</label>
                    <label><input type="radio" value="TODAY" v-model="period" /> 오늘</label>
                    <label><input type="radio" value="7D" v-model="period" /> 7일</label>
                    <label><input type="radio" value="1M" v-model="period" /> 1개월</label>
                    <label><input type="radio" value="3M" v-model="period" /> 3개월</label>
                    <label><input type="radio" value="6M" v-model="period" /> 6개월</label>
                    <label><input type="radio" value="1Y" v-model="period" /> 1년</label>
                    <span class="s_txt">(기부일자 기준으로 검색)</span>
                  </div>
                </div>
                <div class="selected-field-line city">
                  <div class="selected-item">
                    <select v-model="upperLocgovCode" aria-label="시·도 선택">
                      <option value="">시·도 선택</option>
                      <option v-for="[code, name] in provinces" :key="code" :value="code">{{ name }}</option>
                    </select>
                  </div>
                  <div class="selected-item">
                    <select v-model="locgovCode" aria-label="시·군·구 선택">
                      <option value="">시·군·구 선택</option>
                      <option v-for="l in filteredLocgovs" :key="l.locgovCode" :value="l.locgovCode">{{ l.upperLocgovNm }} {{ l.locgovNm }}</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="btn-box many">
            <button type="button" class="blueBtn cancellation" @click="reset">초기화<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="초기화" /></span></button>
            <button type="button" class="blueBtn u-confirm" @click="search">검색<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="검색" /></span></button>
          </div>
        </div>
      </div>
    </div>
  </section>

  <section class="s-con receipt-con">
    <div class="center">
      <div class="s-contents table-wrapper myDonation">
        <div class="table-container">
          <div class="table-result-body">
            <table>
              <caption class="sr-only">기부내역 목록</caption>
              <tr class="result-title">
                <th>No.</th>
                <th>기부지자체</th>
                <th>기부액</th>
                <th>기부일자</th>
                <th>기부포인트</th>
                <th>전자납부번호</th>
                <th>납부일자</th>
                <th>특정사업기부 사업명</th>
                <th>기부처</th>
                <th>비고</th>
                <th>처리</th>
              </tr>
              <tr class="result-row" v-for="(d, i) in donations" :key="d.cntrSn">
                <td>{{ i + 1 }}</td>
                <td>{{ d.locgovName }}</td>
                <td>{{ formatAmount(d.cntrAmt) }}</td>
                <td>{{ d.cntrDe }}</td>
                <td>{{ formatAmount(d.earnedPoints ?? 0) }}</td>
                <td style="white-space: normal; word-break: break-all; max-width: 120px">{{ d.bugaNo ?? '-' }}</td>
                <td>{{ d.sunapDate ? d.sunapDate.slice(0, 10) : '-' }}</td>
                <td>{{ d.projectTitle }}</td>
                <td>고향사랑e음</td>
                <td>
                  <a v-if="d.statusCode === 'COMPLETED'" :href="`http://localhost:8082/receipts/official/${d.cntrSn}`" target="_blank">영수증출력</a>
                  <span v-else>{{ d.statusLabel ?? d.statusCode }}</span>
                </td>
                <td>
                  <button v-if="d.canComplete" type="button" class="small-btn" @click="complete(d.cntrSn)">결제완료 처리</button>
                  <button v-if="d.canCancel" type="button" class="small-btn danger" @click="cancel(d.cntrSn)">취소</button>
                </td>
              </tr>
              <tr v-if="!donations.length">
                <td colspan="11" class="empty">기부 내역이 없습니다.</td>
              </tr>
            </table>
          </div>
        </div>

        <p class="s_txt" style="margin-top: 16px">
          전자기부금영수증 발급이 필요하시면 <a href="http://localhost:8081/coming-soon">홈택스 &gt; 조회/발급 &gt; 전자기부금영수증 &gt; 메인화면</a> 메뉴에서 신청이 가능합니다.
        </p>
      </div>
    </div>
  </section>

  <section class="s-con" style="background: var(--bg-basic); padding: 32px 0">
    <div class="center">
      <p style="font-weight: 700; font-size: 1.8rem; margin-bottom: 16px">고향사랑 기부금은 지방자치단체의<br />4가지 사업 목적으로만 사용됩니다.</p>
      <ol style="padding-left: 20px">
        <li>사회적 취약계층의 지원 및 청소년의 육성·보호</li>
        <li>지역 주민의 문화·예술·보건 등의 증진</li>
        <li>시민참여, 자원봉사 등 지역공동체 활성화 지원</li>
        <li>그 밖에 주민의 복리 증진에 필요한 사업의 추진</li>
      </ol>
    </div>
  </section>

  <div class="center" style="padding: 20px 0">
    <router-link to="/donate">기부하러 가기</router-link> ·
    <router-link to="/mypage/donations/offline">기탁서(오프라인) 등록</router-link>
  </div>
</template>

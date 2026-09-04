<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../../api/http'

// 기존 donation 서비스의 /honor/estimate(honor-estimate.html)는 사이트 헤더/디자인이 전혀
// 적용되지 않은 내부용 임시 화면이었다(offline.html과 같은 성격) - AS-IS 자체에도 이 계산을
// 보여줄 전용 화면이 없어(마이페이지 기부내역조회 소스 주석: "세액공제 예상금액 임시데이터,
// 개발필요") GNB에는 연결되지 않지만, DonationService.taxCreditOf()/honorBenefitOf() 계산
// 로직 자체는 실제로 동작하므로 "기부혜택증"과 같은 화면군으로 묶어 정식 마이페이지 화면으로
// 새로 만든다.
const loading = ref(true)
const errorMessage = ref('')
const data = ref(null)
const year = ref(new Date().getFullYear())

const years = computed(() => {
  const current = new Date().getFullYear()
  return Array.from({ length: 6 }, (_, i) => current - i)
})

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await api.get('donation', `/api/my/tax-credit-estimate?year=${year.value}`)
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}
onMounted(load)

function formatN(n) {
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
        세액공제 예상액
      </span>
      <h2 class="page-title-txt">세액공제 예상액</h2>
      <p class="point-txt">
        실제 공제액은 종합소득 신고 시 확정됩니다. 세액공제 산정 기준은
        <router-link to="/honor">연말정산 세액공제 안내</router-link>에서 확인할 수 있습니다.
      </p>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading">
    <div class="center">
      <div class="contents-wrap myDonation">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

        <div class="s-contents mypageS" v-if="data">
          <div class="mypage-top">
            <label class="search-title" for="taxYear">귀속연도</label>
            <span class="m-field">
              <select id="taxYear" v-model.number="year" @change="load">
                <option v-for="y in years" :key="y" :value="y">{{ y }}년</option>
              </select>
            </span>
          </div>
        </div>
      </div>
    </div>
  </section>

  <section class="s-con receipt-con" v-if="!loading && data">
    <div class="center">
      <div class="s-contents table-wrapper myDonation">
        <div class="tax-credit-summary">
          <p class="s_txt" style="text-align: center; margin-bottom: 8px">{{ data.userName }}님 · {{ data.year }}년 귀속</p>
          <p style="text-align: center; margin-bottom: 12px">기부 {{ data.donationCount }}건, 총 {{ formatN(data.totalAmount) }}원</p>
          <p class="pointblue" style="text-align: center; font-size: 1.6rem; font-weight: 700">
            예상 세액공제액 {{ formatN(data.taxCredit) }}원
          </p>
          <p class="s_txt" style="text-align: center; margin-top: 8px">
            (연간 누계 10만원까지 100%, 10~20만원 구간 44%, 20만원 초과분 16.5%(특별재난지역은 33%) 세액공제)
          </p>
        </div>

        <div class="total_top" style="margin-top: 32px">
          <h3 class="total">지자체 추가 기부혜택</h3>
        </div>
        <ul class="honor-cert-list" v-if="data.benefits.length">
          <li class="honor-cert-card" v-for="b in data.benefits" :key="b.locgovName">
            <div class="honor-cert-locgov">{{ b.locgovName }}</div>
            <div class="honor-cert-year">{{ b.benefitDesc }}</div>
          </li>
        </ul>
        <p class="empty" v-else>등록된 추가 기부혜택 안내가 없습니다.</p>
      </div>
    </div>
  </section>
</template>

<style>
.tax-credit-summary {
  background: #fdf6ef;
  border: 1px solid #f0ddc8;
  border-radius: 8px;
  padding: 24px;
}
</style>

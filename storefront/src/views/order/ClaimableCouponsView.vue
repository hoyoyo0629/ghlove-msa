<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// Thymeleaf 버전 order/coupon/claimable.html과 동일 출처.
const router = useRouter()
const coupons = ref([])
const errorMessage = ref('')
const loading = ref(true)

async function load() {
  loading.value = true
  coupons.value = await api.get('order', '/api/coupons')
  loading.value = false
}
onMounted(load)

async function claim(couponId) {
  errorMessage.value = ''
  try {
    await api.post('order', `/api/coupons/${couponId}/claim`)
    router.push('/my/coupons')
  } catch (e) {
    errorMessage.value = e.message
  }
}

function benefitLabel(c) {
  return c.payType === '1' ? `${new Intl.NumberFormat('ko-KR').format(c.pay)}P 할인` : `${c.pay}% 할인`
}
function conditionLabel(c) {
  return c.payRestriction && c.payRestriction > 0 ? `${new Intl.NumberFormat('ko-KR').format(c.payRestriction)}P 이상 구매시` : '조건없음'
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/my/coupons">내 쿠폰함</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        쿠폰 다운로드
      </span>
      <h2 class="page-title-txt">쿠폰 다운로드</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading">
    <div class="center">
      <p class="error" v-if="errorMessage" style="text-align: center">{{ errorMessage }}</p>
      <p style="margin-bottom: 12px"><router-link to="/coupons/offline">코드 직접입력으로 쿠폰받기 &rarr;</router-link></p>

      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="coupons.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">다운로드 가능한 쿠폰 목록</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">쿠폰명</th>
                    <th class="date-col" scope="col">혜택</th>
                    <th class="date-col" scope="col">사용조건</th>
                    <th class="date-col" scope="col">처리</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="c in coupons" :key="c.couponId">
                    <td class="date-col">{{ c.couponName }}</td>
                    <td class="date-col">{{ benefitLabel(c) }}</td>
                    <td class="date-col">{{ conditionLabel(c) }}</td>
                    <td class="date-col"><button type="button" class="formBtn" @click="claim(c.couponId)">다운로드</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else><p>지금 다운로드할 수 있는 쿠폰이 없습니다.</p></div>
        </div>
      </div>
    </div>
  </section>
</template>

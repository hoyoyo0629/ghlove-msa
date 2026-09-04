<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// Thymeleaf 버전 order/coupon/my.html과 동일 출처.
const usable = ref([])
const used = ref([])
const loading = ref(true)

onMounted(async () => {
  const data = await api.get('order', '/api/my/coupons')
  usable.value = data.usable
  used.value = data.used
  loading.value = false
})

function benefitLabel(c) {
  return c.payType === '1' ? `${new Intl.NumberFormat('ko-KR').format(c.pay)}P 할인` : `${c.pay}% 할인`
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
        내 쿠폰함
      </span>
      <h2 class="page-title-txt">내 쿠폰함</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading">
    <div class="center">
      <p style="margin-bottom: 12px"><router-link to="/coupons">받을 수 있는 쿠폰 보러가기 &rarr;</router-link></p>

      <h3 class="deepSubBlue">사용가능 쿠폰</h3>
      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="usable.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">사용가능 쿠폰 목록</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">쿠폰명</th>
                    <th class="date-col" scope="col">혜택</th>
                    <th class="date-col" scope="col">사용가능기간</th>
                    <th class="date-col" scope="col">다운로드일</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="c in usable" :key="c.couponUserId">
                    <td class="date-col">{{ c.couponName }}</td>
                    <td class="date-col">{{ benefitLabel(c) }}</td>
                    <td class="date-col">{{ c.applyStartDate ? `${c.applyStartDate} ~ ${c.applyEndDate}` : '기간제한 없음' }}</td>
                    <td class="date-col">{{ c.downloadDate?.slice(0, 8) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else><p>사용 가능한 쿠폰이 없습니다.</p></div>
        </div>
      </div>

      <h3 class="deepSubBlue" style="margin-top: 32px">사용/만료 쿠폰</h3>
      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="used.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">사용 또는 만료된 쿠폰 목록</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">쿠폰명</th>
                    <th class="date-col" scope="col">상태</th>
                    <th class="date-col" scope="col">주문번호</th>
                    <th class="date-col" scope="col">할인금액</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="c in used" :key="c.couponUserId">
                    <td class="date-col">{{ c.couponName }}</td>
                    <td class="date-col">{{ c.dataStatusCode === '1' ? '사용완료' : '기간만료' }}</td>
                    <td class="date-col">{{ c.orderCode ?? '-' }}</td>
                    <td class="date-col">{{ c.discountAmount != null ? `${new Intl.NumberFormat('ko-KR').format(c.discountAmount)}P` : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else><p>사용/만료된 쿠폰이 없습니다.</p></div>
        </div>
      </div>
    </div>
  </section>
</template>

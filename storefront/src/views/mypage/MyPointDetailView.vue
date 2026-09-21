<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { formatN } from '../../utils/format'
import MypageLnb from '../../components/MypageLnb.vue'

// AS-IS mypage/cntrPointDetail.html 재현 - 한 지자체(+연도)의 기부건별 포인트 현황 상세.
// 기부포인트 조회(MyPointsView) 목록의 "사용포인트" 칸 상세 아이콘으로 진입한다.
const route = useRoute()
const router = useRouter()
const data = ref(null)

async function load() {
  const locgovCode = route.query.locgovCode
  if (!locgovCode) {
    router.replace({ name: 'mypage-points' })
    return
  }
  // AS-IS와 동일하게 상세는 지자체 단위로만 본다(연도 필터 없음).
  const params = new URLSearchParams()
  params.set('locgovCode', locgovCode)
  data.value = await api.get('point', `/api/my/points/detail?${params}`)
}
onMounted(load)
watch(() => route.query, load)

function goOrder(orderCode) {
  router.push({ name: 'order-detail', params: { orderId: orderCode } })
}
</script>

<template>
  <MypageLnb current="points" />
  <section class="center" v-if="data">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부포인트 현황
      </span>
      <h2 class="page-title-txt">기부포인트 현황</h2>
      <div class="user-grade">
        <span class="grade-bg">
          <span class="blue-txt">{{ data.upperLocgovNm }} {{ data.locgovNm }}</span>
        </span>
      </div>
    </div>

    <div class="contents-wrap myDonation">
      <div class="s-contents mypageP">
        <div class="mypage-point">
          <div class="mypage-point-list">
            <label class="col3_label"><span class="label-title"> 적립포인트</span></label>
            <div class="m-field"><input type="text" :value="formatN(data.earnedTotal)" readonly /><span class="s_txt">P</span></div>
          </div>
          <div class="mypage-point-list">
            <label class="col3_label"><span class="label-title"> 사용포인트</span></label>
            <div class="m-field"><input type="text" :value="formatN(data.usedTotal)" readonly /><span class="s_txt">P</span></div>
          </div>
          <div class="mypage-point-list">
            <label class="col3_label"><span class="label-title"> 총 잔여포인트</span></label>
            <div class="m-field"><input type="text" :value="formatN(data.remainingTotal)" readonly /><span class="s_txt">P</span></div>
          </div>
        </div>
      </div>

      <div class="s-contents table-wrapper pointD">
        <div class="table-container">
          <div class="table-result-body">
            <table>
              <caption class="sr-only">기부포인트 현황 상세</caption>
              <tr class="result-title">
                <th>No.</th>
                <th>발생일자</th>
                <th>기부액(실납부액)</th>
                <th>적립포인트</th>
                <th>사용포인트</th>
                <th>잔여포인트</th>
                <th>답례품 주문번호</th>
              </tr>
              <!-- 적립(기부) 행이면 기부액/적립만, 사용(구매) 행이면 사용/주문번호만 채워진다(AS-IS UNION ALL) -->
              <tr class="result-row" v-for="(row, i) in data.rows" :key="i">
                <td class="idx">{{ data.rows.length - i }}</td>
                <td>{{ row.cntrDe }}</td>
                <td class="amount">{{ row.cntrAmt != null ? formatN(row.cntrAmt) : '' }}</td>
                <td class="amount">{{ row.earned != null ? formatN(row.earned) : '' }}</td>
                <td class="amount">{{ row.used != null ? formatN(row.used) : '' }}</td>
                <td class="amount">{{ formatN(row.remaining) }}</td>
                <td>
                  <button v-if="row.orderCode" type="button" class="moreView margin0"
                          @click="goOrder(row.orderCode)">{{ row.orderCode }}</button>
                  <span v-else>-</span>
                </td>
              </tr>
              <tr v-if="!data.rows.length">
                <td colspan="7" class="empty">상세 내역이 없습니다.</td>
              </tr>
            </table>
          </div>
        </div>
      </div>

      <div class="btn-box">
        <button type="button" class="blueBtn u-confirm" @click="router.push({ name: 'mypage-points' })">
          목록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
      </div>
    </div>
  </section>
</template>

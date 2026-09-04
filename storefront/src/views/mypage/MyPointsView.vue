<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS mypage/cntrPoint.html(및 Thymeleaf 버전 point/my.html) 재현.
const router = useRouter()
const data = ref(null)
const useAmount = ref('')
const useOrderCode = ref('')
const useMessage = ref('')

async function load() {
  data.value = await api.get('point', '/api/my/points')
}
onMounted(load)

async function usePoints() {
  useMessage.value = ''
  try {
    await api.post('point', '/use', { amount: Number(useAmount.value), orderCode: useOrderCode.value || undefined })
    useMessage.value = '포인트가 사용되었습니다.'
    useAmount.value = ''
    useOrderCode.value = ''
    await load()
  } catch (e) {
    useMessage.value = e.message
  }
}

function formatAmount(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section v-if="data">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부포인트 조회
      </span>
      <h2 class="page-title-txt">기부포인트 조회</h2>
      <p>포인트는 기부지역의 답례품만 구매 가능합니다.</p>
      <div class="user-grade point" v-if="data.userName">
        <span class="grade-bg">
          <img class="icon-img" src="/images/icon/cli-icon_my-user-name.png" alt="" />
          <span class="blue-txt">{{ data.userName }}</span> 회원님
        </span>
      </div>
      <p class="point-txt">기부 결제 후 연계 시스템 지연에 따라 <span style="font-weight: bold">최대 3일</span>까지 반영되지 않을 수 있으므로 참고 바랍니다.</p>
    </div>
  </section>

  <section id="point-summary" v-if="data">
    <div class="center">
      <div class="contents-wrap myDonation">
        <div class="s-contents mypageP">
          <div class="mypage-point">
            <div class="mypage-point-list">
              <label class="col3_label" for="cntrPointTotal">
                <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="총 기부금액" />
                <span class="label-title">총 적립 포인트</span>
              </label>
              <div class="m-field"><input type="text" id="cntrPointTotal" :value="formatAmount(data.totalEarned)" readonly /><span class="s_txt">P</span></div>
            </div>
            <div class="mypage-point-list">
              <label class="col3_label" for="usePointTotal">
                <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="총 기부금액" />
                <span class="label-title">총 사용 포인트</span>
              </label>
              <div class="m-field"><input type="text" id="usePointTotal" :value="formatAmount(data.totalUsed)" readonly /><span class="s_txt">P</span></div>
            </div>
            <div class="mypage-point-list">
              <label class="col3_label" for="blcePointTotal">
                <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="총 기부금액" />
                <span class="label-title">총 잔여 포인트</span>
              </label>
              <div class="m-field"><input type="text" id="blcePointTotal" :value="formatAmount(data.balance)" readonly /><span class="s_txt">P</span></div>
            </div>
          </div>
          <div style="font-size: 12px; color: #888; margin-top: 6px" v-if="data.reservedAmount > 0">
            예약중 {{ formatAmount(data.reservedAmount) }}P 제외 가용잔액 {{ formatAmount(data.availableBalance) }}P
          </div>
        </div>
      </div>
    </div>
  </section>

  <section class="s-con" v-if="data">
    <div class="center">
      <div class="s-contents table-wrapper myDonation">
        <div class="table-container">
          <div class="table-result-body">
            <table>
              <caption class="sr-only">기부포인트 표</caption>
              <tr class="result-title">
                <th rowspan="2">No.</th>
                <th colspan="2">기부지자체</th>
                <th rowspan="2">적립 포인트</th>
                <th rowspan="2">사용 포인트</th>
                <th rowspan="2">잔여 포인트</th>
                <th rowspan="2">기부처</th>
                <th rowspan="2">답례품</th>
              </tr>
              <tr class="result-title">
                <th>시·도</th>
                <th>시·군·구</th>
              </tr>
              <tr class="result-row" v-for="(row, i) in data.locgovSummary" :key="row.locgovCode">
                <td>{{ data.locgovSummary.length - i }}</td>
                <td>{{ row.upperLocgovNm }}</td>
                <td>{{ row.locgovNm }}</td>
                <td>{{ formatAmount(row.earned) }}</td>
                <td>{{ formatAmount(row.used) }}</td>
                <td>{{ formatAmount(row.remaining) }}</td>
                <td>고향사랑e음</td>
                <td class="loc_mall">
                  <button type="button" class="deepBlue moreView" @click="router.push({ path: '/gifts', query: { locgovCode: row.locgovCode } })">
                    답례품 몰 가기
                    <img src="/images/icon/simple-arrow-right.png" alt="이동" />
                  </button>
                </td>
              </tr>
              <tr v-if="!data.locgovSummary.length">
                <td colspan="8" class="empty">기부포인트 내역이 없습니다.</td>
              </tr>
            </table>
          </div>
        </div>
      </div>
    </div>
  </section>

  <div class="wrap" v-if="data">
    <div>
      <div class="notice-box" v-if="data.upcomingExpirations.length">
        <span>곧 소멸 예정인 포인트가 {{ data.upcomingExpirations.length }}건 있습니다:</span>
        <ul style="margin: 6px 0 0; padding-left: 18px">
          <li v-for="(lot, i) in data.upcomingExpirations" :key="i">
            <span>{{ formatAmount(lot.remainingAmount) }}P</span> - <span>{{ lot.expirationDate }}</span> 소멸 예정
          </li>
        </ul>
      </div>

      <div class="section-title">포인트 사용 (테스트)</div>
      <p v-if="useMessage">{{ useMessage }}</p>
      <form class="inline-form" @submit.prevent="usePoints">
        <input type="number" v-model="useAmount" placeholder="사용 포인트" min="1" step="1" required />
        <input type="text" v-model="useOrderCode" placeholder="주문코드(선택)" />
        <button type="submit">사용하기</button>
      </form>

      <div class="section-title">거래내역</div>
      <table>
        <thead>
          <tr>
            <th>일시</th>
            <th>구분</th>
            <th>포인트</th>
            <th>사유</th>
            <th>참조</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(l, i) in data.ledger" :key="i">
            <td>{{ l.createdDate ? l.createdDate.replace('T', ' ').slice(0, 16) : '' }}</td>
            <td>{{ l.txnTypeLabel ?? l.txnType }}</td>
            <td :class="l.pointAmount > 0 ? 'positive' : 'negative'">{{ l.pointAmount > 0 ? '+' : '' }}{{ formatAmount(l.pointAmount) }}P</td>
            <td>{{ l.reason }}</td>
            <td>{{ l.refKey }}</td>
          </tr>
        </tbody>
      </table>
      <p class="empty" v-if="!data.ledger.length">거래내역이 없습니다.</p>
    </div>

    <div class="links">
      <router-link to="/mypage/points/reservations">포인트 예약 관리</router-link>
    </div>
  </div>
</template>

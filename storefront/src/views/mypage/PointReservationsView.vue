<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS point/reservations.html(SFR-004 예약/예약해제) 재현. 기존 화면은 골격만 있는
// 내부용(css/style.css)이었는데, RoleRequestView/RoleQueueView와 같은 방식으로 나머지
// 마이페이지 화면들과 동일한 정식 디자인으로 재구축했다. 실제 주문 결제는 아직 이
// 예약 단계를 거치지 않고 바로 차감/복원되므로(order SAGA 미연동), 독립적인 테스트/
// 관리 용도로 쓰인다.
const data = ref(null)
const loading = ref(true)

const amount = ref('')
const refKey = ref('')
const reason = ref('')
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    data.value = await api.get('point', '/api/my/reservations')
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function onSubmit() {
  errorMessage.value = ''
  try {
    await api.post('point', '/api/my/reservations', {
      amount: Number(amount.value),
      refKey: refKey.value || undefined,
      reason: reason.value || undefined,
    })
    amount.value = ''
    refKey.value = ''
    reason.value = ''
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function confirmReservation(id) {
  errorMessage.value = ''
  try {
    await api.post('point', `/api/my/reservations/${id}/confirm`)
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function releaseReservation(id) {
  errorMessage.value = ''
  try {
    await api.post('point', `/api/my/reservations/${id}/release`)
    await load()
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
        <router-link to="/mypage/points">기부포인트 조회</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        포인트 예약 관리
      </span>
      <h2 class="page-title-txt">포인트 예약 관리</h2>
      <p class="point-txt">결제 확정 전 포인트를 미리 잡아두는 기능입니다. 예약중 포인트는 가용잔액에서 제외되며, 확정 시에만 실제로 차감됩니다.</p>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading && data">
    <div class="center">
      <div class="contents-wrap myDonation">
        <div class="s-contents mypageP">
          <div class="mypage-point">
            <div class="mypage-point-list">
              <label class="col3_label" for="availableBalance">
                <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="가용 잔액" />
                <span class="label-title">가용 잔액 (보유 - 예약중)</span>
              </label>
              <div class="m-field"><input type="text" id="availableBalance" :value="formatAmount(data.availableBalance)" readonly /><span class="s_txt">P</span></div>
            </div>
          </div>
        </div>
      </div>

      <div class="s-contents mypageS">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>
        <form @submit.prevent="onSubmit">
          <div class="info-field-group">
            <div class="info-field-items">
              <label for="amount" class="flied-title">예약 포인트</label>
              <span class="form-field"><input type="number" id="amount" v-model="amount" min="1" step="1" placeholder="예약할 포인트" required /></span>
            </div>
            <div class="info-field-items">
              <label for="refKey" class="flied-title">참조키 (선택)</label>
              <span class="form-field"><input type="text" id="refKey" v-model="refKey" placeholder="주문코드 등" /></span>
            </div>
            <div class="info-field-items">
              <label for="reason" class="flied-title">사유 (선택)</label>
              <span class="form-field"><input type="text" id="reason" v-model="reason" /></span>
            </div>
          </div>
          <div class="btn-box many mid">
            <button type="submit" class="blueBtn u-confirm">
              예약하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
            </button>
          </div>
        </form>
      </div>

      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="data.reservations.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">예약 내역 - 예약ID, 포인트, 참조키, 사유, 상태, 일시, 처리로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">예약ID</th>
                    <th class="date-col" scope="col">포인트</th>
                    <th class="date-col" scope="col">참조키</th>
                    <th class="date-col" scope="col">사유</th>
                    <th class="date-col" scope="col">상태</th>
                    <th class="date-col" scope="col">일시</th>
                    <th class="date-col" scope="col">처리</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="r in data.reservations" :key="r.reservationId">
                    <td class="date-col">{{ r.reservationId }}</td>
                    <td class="date-col">{{ formatAmount(r.amount) }}P</td>
                    <td class="date-col">{{ r.refKey }}</td>
                    <td class="date-col">{{ r.reason }}</td>
                    <td class="date-col">{{ r.statusLabel ?? r.status }}</td>
                    <td class="date-col">{{ r.createdDate?.replace('T', ' ').slice(0, 16) }}</td>
                    <td class="date-col">
                      <div class="btn-box many" style="justify-content: center" v-if="r.status === 'RESERVED'">
                        <button type="button" class="formBtn" @click="confirmReservation(r.reservationId)">확정</button>
                        <button type="button" class="formBtn cancellation" @click="releaseReservation(r.reservationId)">해제</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else>
            <p>예약 내역이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
@import '/css/mypage-status.css';
@import '/css/change-info.css';
</style>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage/orderList.html을 단순화 재현한 Thymeleaf 버전 order/my.html과 동일 출처.
const orders = ref([])
const searchStartDate = ref('')
const searchEndDate = ref('')
const itemName = ref('')
const loading = ref(true)

async function search() {
  loading.value = true
  const params = new URLSearchParams()
  if (searchStartDate.value) params.set('searchStartDate', searchStartDate.value)
  if (searchEndDate.value) params.set('searchEndDate', searchEndDate.value)
  if (itemName.value) params.set('itemName', itemName.value)
  orders.value = await api.get('order', `/api/orders?${params}`)
  loading.value = false
}
onMounted(search)

function pad2(n) {
  return n < 10 ? '0' + n : '' + n
}
function toDateInput(d) {
  return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate())
}
function setRange(unit, amount) {
  const end = new Date()
  const start = new Date()
  if (unit === 'week') start.setDate(start.getDate() - 7 * amount)
  else if (unit === 'month') start.setMonth(start.getMonth() - amount)
  searchStartDate.value = toDateInput(start)
  searchEndDate.value = toDateInput(end)
}
function reset() {
  searchStartDate.value = ''
  searchEndDate.value = ''
  itemName.value = ''
  search()
}

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
        주문조회
      </span>
      <h2 class="page-title-txt">주문조회</h2>
      <p class="point-txt">전체 주문내역을 조회합니다. 진행 상태에 따라 주문상세에서 취소, 교환, 반품신청이 가능합니다.</p>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div class="s-contents mypageS">
        <div class="mypage-top">
          <label class="card_title" for="searchStartDate">기간</label>
          <div class="selected-field-line">
            <div class="selected-item">
              <label for="searchStartDate" class="sr-only">시작일</label>
              <input type="date" id="searchStartDate" v-model="searchStartDate" />
              <span class="s_txt"> ~ </span>
              <label for="searchEndDate" class="sr-only">종료일</label>
              <input type="date" id="searchEndDate" v-model="searchEndDate" />
            </div>
            <div class="formbtn-box">
              <button class="formBtn" type="button" @click="setRange('week', 1)">1주일</button>
              <button class="formBtn" type="button" @click="setRange('month', 1)">1개월</button>
              <button class="formBtn" type="button" @click="setRange('month', 3)">3개월</button>
              <button class="formBtn" type="button" @click="setRange('month', 6)">6개월</button>
            </div>
          </div>
        </div>
        <div class="line"></div>
        <div class="mypage-top">
          <label class="card_title" for="itemName">답례품명</label>
          <div class="selected-item"><input type="text" id="itemName" v-model="itemName" /></div>
        </div>
        <div class="btn-box many mid">
          <button type="button" class="blueBtn cancellation" @click="reset">초기화<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
          <button type="button" class="blueBtn u-confirm" @click="search">조회<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
        </div>
      </div>

      <div class="s-contents" v-if="!loading">
        <div class="list_body table-container w3c_v_2410" v-if="orders.length">
          <div class="list_wrap">
            <table>
              <caption class="sr-only">주문조회 - 주문번호, 답례품, 수량, 사용 포인트, 상태로 구성</caption>
              <thead class="list-title">
                <tr class="items_wrap">
                  <th class="date-col" scope="col">주문번호</th>
                  <th class="date-col g_info_wrap" scope="col">답례품정보</th>
                  <th class="date-col" scope="col">수량</th>
                  <th class="date-col" scope="col">사용 포인트</th>
                  <th class="date-col" scope="col">상태</th>
                </tr>
              </thead>
              <tbody class="item_list-group">
                <tr class="list-items" v-for="o in orders" :key="o.orderId">
                  <td class="date-col"><router-link :to="`/orders/${o.orderId}`">{{ o.orderId }}</router-link></td>
                  <td class="date-col g_info_wrap">
                    <div class="g_info__txt"><div class="info_title"><router-link :to="`/orders/${o.orderId}`">{{ o.itemName }}</router-link></div></div>
                  </td>
                  <td class="date-col">{{ o.quantity }}</td>
                  <td class="date-col">{{ formatN(o.pointAmount) }}P</td>
                  <td class="date-col">{{ o.orderStatusLabel }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="common_none" v-else>
          <p>주문 내역이 없습니다.</p>
        </div>
      </div>
    </div>
  </section>
</template>

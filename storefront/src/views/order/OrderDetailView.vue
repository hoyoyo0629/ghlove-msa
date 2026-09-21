<script setup>
import { modalAlert, modalConfirm } from '../../composables/useModal'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { formatN } from '../../utils/format'
import MypageLnb from '../../components/MypageLnb.vue'

// AS-IS mypage/orderDetail.html 레이아웃 재현 - 주문번호/일자, 지자체 그룹 + 답례품정보/
// 주문·배송상태 카드, 배송지 정보, 결제정보(주문/취소/총 포인트). MSA는 1주문=1답례품이라
// 지자체 그룹은 단일이다. 발송 전 배송지 변경/반품·교환 신청은 하단 폼으로 둔다(PG 없는 범위 축소).
const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const order = ref(null)
const errorMessage = ref('')
const loading = ref(true)
const showClaim = ref(false)
let pollTimer = null

const claimForm = reactive({ claimType: '', reason: '' })
const addressForm = reactive({ address: '', addressDetail: '' })

async function load() {
  try {
    order.value = await api.get('order', `/api/orders/${orderId}`)
    addressForm.address = order.value.deliveryAddress ?? ''
    addressForm.addressDetail = order.value.deliveryAddressDetail ?? ''
    if (order.value.claimTypes.length && !claimForm.claimType) claimForm.claimType = order.value.claimTypes[0].key
    if (order.value.orderStatus === 'PENDING' && !pollTimer) {
      pollTimer = setInterval(load, 2000)
    } else if (order.value.orderStatus !== 'PENDING' && pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}
onMounted(load)
onUnmounted(() => pollTimer && clearInterval(pollTimer))

function formatDate(iso) {
  if (!iso) return '-'
  return String(iso).slice(0, 10).replace(/-/g, '.')
}

const statusClass = computed(() => {
  if (!order.value) return ''
  if (order.value.orderStatus === 'PENDING') return 'pending'
  if (order.value.orderStatus === 'CONFIRMED') return 'confirmed'
  return 'cancelled'
})

async function cancelOrder() {
  if (!(await modalConfirm('주문을 취소하시겠습니까?'))) return
  try {
    await api.post('order', `/api/orders/${orderId}/cancel`)
    modalAlert('취소신청 되었습니다.') // AS-IS modal-order_cancle.vue:360
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

// AS-IS는 반품/교환/취소를 유형별 모달로 확인받고 각각 다른 완료 문구를 띄운다.
const CLAIM_LABEL = { RETURN: '반품', EXCHANGE: '교환', CANCEL: '취소' }
const CLAIM_DONE = { RETURN: '반품신청 되었습니다.', EXCHANGE: '교환신청 되었습니다.', CANCEL: '취소신청 되었습니다.' }

async function submitClaim() {
  const t = claimForm.claimType
  if (!(await modalConfirm(`${CLAIM_LABEL[t] ?? ''}을(를) 신청하시겠습니까?`))) return
  try {
    await api.post('order', `/api/orders/${orderId}/claim`, { claimType: t, reason: claimForm.reason })
    modalAlert(CLAIM_DONE[t] ?? '신청 되었습니다.')
    showClaim.value = false
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function saveAddress() {
  try {
    await api.post('order', `/api/orders/${orderId}/delivery-address`, addressForm)
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function confirmReceipt() {
  if (!(await modalConfirm('해당 답례품을 구매확정 하시겠습니까?'))) return
  try {
    await api.post('order', `/api/orders/${orderId}/confirm-receipt`)
    modalAlert('구매확정이 완료되었습니다.') // AS-IS mypage/orderDetail.html:976
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

function writeReview() {
  router.push(`/gifts/${order.value.itemId}?orderCode=${order.value.orderId}`)
}
</script>

<template>
  <MypageLnb current="orders" />
  <section class="center">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        답례품
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        주문조회
      </span>
      <h2 class="page-title-txt">주문조회</h2>
      <p>전체 주문내역을 조회합니다. 진행 상태에 따라 취소, 교환, 반품신청이 가능합니다.</p>
    </div>
  </section>

  <section id="contents" v-if="!loading && order">
    <div class="contents-wrap">
      <div class="s-contents center">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

        <!-- 주문번호 / 주문일자 -->
        <div class="row_con flex order_case">
          <div class="o__num"><span>주문번호 : </span><span>{{ order.orderId }}</span></div>
          <div class="o__date"><span>주문일자 : </span><span>{{ formatDate(order.createdDate) }}</span></div>
        </div>

        <!-- 답례품 목록 (지자체 그룹) -->
        <div class="row_con">
          <div class="row_con_wrap locGroup">
            <div class="list_title_area">{{ order.locgovName }}</div>
            <div class="list-body">
              <div class="honor-list-wrap">
                <div class="list-title">
                  <div class="date-col g_info">답례품정보</div>
                  <div class="date-col o_status">주문/배송상태</div>
                </div>
                <ul class="list-items-group">
                  <li class="list-items">
                    <div class="date-col g_info">
                      <div class="g_info_wrap">
                        <div class="g_info__txt">
                          <div class="info_title">{{ order.itemName }}</div>
                          <div class="info_total">
                            <span class="deepBlue">{{ formatN(order.quantity) }}</span>개
                            <span class="dvide"></span>
                            <span class="deepBlue">{{ formatN(order.pointAmount) }}P</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="date-col o_status">
                      <div class="o_status_wrap">
                        <div class="o_status-spteps">
                          <div class="spteps_status" :class="statusClass">{{ order.orderStatusLabel }}</div>
                          <div class="spteps_status" v-if="order.deliveryStatusLabel">
                            {{ order.deliveryStatusLabel }}
                            <template v-if="order.invoiceNo"> · {{ order.carrierLabel }} {{ order.invoiceNo }}</template>
                          </div>
                          <div class="spteps_status s_txt" v-if="order.orderStatus === 'PENDING'">재고·포인트 처리 중입니다 (자동 새로고침)...</div>
                          <div class="spteps_status pointRed" v-if="order.cancelReason">사유 : {{ order.cancelReason }}</div>
                        </div>
                        <div class="o_status-btn">
                          <button type="button" class="orderBtn" v-if="order.orderStatus === 'CONFIRMED' && !order.deliveryStatus" @click="cancelOrder">주문취소</button>
                          <button type="button" class="orderBtn comp" v-if="order.deliveryStatus === 'DELIVERED'" @click="confirmReceipt">구매확정</button>
                          <button type="button" class="orderBtn" v-if="order.orderStatus === 'CONFIRMED'" @click="showClaim = !showClaim">교환/반품신청</button>
                          <button type="button" class="orderBtn comp" v-if="order.orderStatus === 'CONFIRMED'" @click="writeReview">후기작성</button>
                        </div>
                      </div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 배송지 정보 + 결제정보 -->
        <div class="row_con flex">
          <div class="row_con_wrapper">
            <div class="list_title_area">배송지 정보</div>
            <ul class="list_wrapper">
              <li class="list_items"><span class="label">받으시는분</span><span class="data_val">{{ order.receiverName || '-' }}</span></li>
              <li class="list_items"><span class="label">배송지 주소</span><span class="data_val">{{ order.deliveryAddress }} {{ order.deliveryAddressDetail }}</span></li>
              <li class="list_items"><span class="label">휴대폰번호(수취인)</span><span class="data_val">{{ order.receiverPhone || '-' }}</span></li>
              <li class="list_items"><span class="label">배송시 요구사항</span><span class="data_val">{{ order.requestNote || '-' }}</span></li>
            </ul>
          </div>
          <div class="row_con_wrapper">
            <div class="list_title_area">결제정보</div>
            <div class="list_wrapper">
              <ul class="payment">
                <li class="payment_list">
                  <span class="payment__label">주문포인트</span>
                  <span class="payment__val">{{ formatN(order.orderPoint) }} P</span>
                </li>
                <li class="payment_list" v-if="order.cancelPoint > 0">
                  <span class="payment__label">취소포인트</span>
                  <span class="payment__val">- {{ formatN(order.cancelPoint) }} P</span>
                </li>
                <li class="payment_list">
                  <span class="payment__label">배송비</span>
                  <span class="payment__val">{{ order.deliveryFee ? formatN(order.deliveryFee) + ' P' : '무료배송' }}</span>
                </li>
              </ul>
              <div class="payment_list total_pay">
                <strong class="label">총 결제 포인트</strong>
                <span class="payment_val pointblue">{{ formatN(order.totalPoint) }} P</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 발송 전 배송지 변경 -->
        <div class="row_con" v-if="order.orderStatus === 'CONFIRMED' && !order.deliveryStatus">
          <div class="row_con_wrapper">
            <div class="list_title_area">배송지 변경 (발송 전)</div>
            <ul class="list_wrapper">
              <li class="list_items"><span class="label">주소</span><input type="text" v-model="addressForm.address" placeholder="주소" /></li>
              <li class="list_items"><span class="label">상세주소</span><input type="text" v-model="addressForm.addressDetail" placeholder="상세주소" /></li>
            </ul>
            <button type="button" class="formBtn" @click="saveAddress">배송지 저장</button>
          </div>
        </div>

        <!-- 반품/교환 신청 -->
        <div class="row_con" v-if="showClaim && order.orderStatus === 'CONFIRMED'">
          <div class="row_con_wrapper">
            <div class="list_title_area">반품/교환 신청</div>
            <ul class="list_wrapper">
              <li class="list_items">
                <span class="label">유형</span>
                <select v-model="claimForm.claimType">
                  <option v-for="t in order.claimTypes" :key="t.key" :value="t.key">{{ t.label }}</option>
                </select>
              </li>
              <li class="list_items">
                <span class="label">사유 (선택)</span>
                <input type="text" v-model="claimForm.reason" />
              </li>
            </ul>
            <button type="button" class="formBtn" @click="submitClaim">신청하기</button>
          </div>
        </div>
      </div>

      <div class="btn-box center">
        <router-link class="blueBtn u-confirm" to="/orders">목록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></router-link>
      </div>
    </div>
  </section>
</template>

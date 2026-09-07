<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// 주문 상세 - Thymeleaf 버전 order/detail.html과 동일 출처(같은 기능, AS-IS 다른 화면들과
// 일관된 page-title-box/s-contents 스타일로 재현). PG 결제 게이트웨이가 없는 이 MSA는
// 발송처리/배송상태변경(제공자·운영자용)도 같은 화면 안에서 노출한다(범위 축소, [[cart-checkout-order-complete-round]]).
const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const order = ref(null)
const errorMessage = ref('')
const loading = ref(true)
let pollTimer = null

const claimForm = reactive({ claimType: '', reason: '' })
const addressForm = reactive({ address: '', addressDetail: '' })
const invoiceForm = reactive({ carrierCode: '', invoiceNo: '' })

async function load() {
  try {
    order.value = await api.get('order', `/api/orders/${orderId}`)
    addressForm.address = order.value.deliveryAddress ?? ''
    addressForm.addressDetail = order.value.deliveryAddressDetail ?? ''
    if (order.value.claimTypes.length && !claimForm.claimType) claimForm.claimType = order.value.claimTypes[0].key
    if (order.value.carriers.length && !invoiceForm.carrierCode) invoiceForm.carrierCode = order.value.carriers[0].key
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

const statusClass = computed(() => {
  if (!order.value) return ''
  if (order.value.orderStatus === 'PENDING') return 'pending'
  if (order.value.orderStatus === 'CONFIRMED') return 'confirmed'
  return 'cancelled'
})

async function cancelOrder() {
  if (!confirm('주문을 취소하시겠습니까?')) return
  try {
    await api.post('order', `/api/orders/${orderId}/cancel`)
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function submitClaim() {
  try {
    await api.post('order', `/api/orders/${orderId}/claim`, { claimType: claimForm.claimType, reason: claimForm.reason })
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

async function registerInvoice() {
  try {
    await api.post('order', `/api/orders/${orderId}/invoice`, invoiceForm)
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function updateDeliveryStatus(deliveryStatus) {
  try {
    await api.post('order', `/api/orders/${orderId}/delivery-status`, { deliveryStatus })
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function confirmReceipt() {
  if (!confirm('해당 답례품을 구매확정 하시겠습니까?')) return
  try {
    await api.post('order', `/api/orders/${orderId}/confirm-receipt`)
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

function writeReview() {
  router.push(`/gifts/${order.value.itemId}?orderCode=${order.value.orderId}`)
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
        <router-link to="/orders">주문조회</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        주문상세
      </span>
      <h2 class="page-title-txt">주문 상세</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading && order">
    <div class="center">
      <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

      <div class="s-contents">
        <div class="row_con_wrapper orderC">
          <div class="list_title_area">
            {{ order.orderStatusLabel }}
            <span v-if="order.orderStatus === 'PENDING'" class="s_txt">재고·포인트 처리 중입니다 (자동 새로고침)...</span>
            <span v-if="order.orderStatus === 'CLAIM_REQUESTED'" class="s_txt">반품/교환 신청이 접수되어 처리 중입니다.</span>
            <span v-if="order.cancelReason" class="s_txt pointRed"> {{ order.cancelReason }}</span>
          </div>
          <ul class="list_wrapper">
            <li class="list_items"><span class="label">주문번호</span><span class="data_val">{{ order.orderId }}</span></li>
            <li class="list_items"><span class="label">답례품</span><span class="data_val">{{ order.itemName }}</span></li>
            <li class="list_items"><span class="label">수량</span><span class="data_val">{{ order.quantity }}</span></li>
            <li class="list_items"><span class="label">단가</span><span class="data_val">{{ formatN(order.unitPrice) }}원</span></li>
            <li class="list_items" v-if="order.deliveryFee"><span class="label">배송비</span><span class="data_val">{{ formatN(order.deliveryFee) }}P</span></li>
            <li class="list_items"><span class="label">사용 포인트</span><span class="data_val">{{ formatN(order.pointAmount) }}P</span></li>
          </ul>
        </div>
      </div>

      <div class="s-contents" v-if="order.orderStatus === 'CONFIRMED'" style="margin-top: 16px">
        <button type="button" class="blueBtn cancellation" @click="cancelOrder">주문 취소</button>
        <button type="button" class="formBtn" style="margin-left: 8px" @click="writeReview">후기 작성</button>
      </div>

      <div class="s-contents" v-if="order.orderStatus === 'CONFIRMED'" style="margin-top: 16px">
        <div class="list_title_area">반품/교환 신청</div>
        <div class="info-field-items">
          <label for="claimType">유형</label>
          <select id="claimType" v-model="claimForm.claimType">
            <option v-for="t in order.claimTypes" :key="t.key" :value="t.key">{{ t.label }}</option>
          </select>
        </div>
        <div class="info-field-items">
          <label for="claimReason">사유 (선택)</label>
          <input type="text" id="claimReason" v-model="claimForm.reason" />
        </div>
        <button type="button" class="formBtn" @click="submitClaim">신청하기</button>
      </div>

      <div class="s-contents" v-if="order.orderStatus === 'CONFIRMED'" style="margin-top: 16px">
        <div class="list_title_area">배송관리</div>

        <div v-if="!order.deliveryStatus">
          <p class="s_txt">아직 발송 전입니다. 배송지 변경이나 송장 등록이 가능합니다.</p>
          <div class="info-field-items">
            <label for="address">배송지 변경</label>
            <input type="text" id="address" v-model="addressForm.address" placeholder="주소" />
          </div>
          <div class="info-field-items">
            <input type="text" v-model="addressForm.addressDetail" placeholder="상세주소" />
          </div>
          <button type="button" class="formBtn" @click="saveAddress">배송지 저장</button>

          <div class="line"></div>
          <div class="info-field-items">
            <label for="carrierCode">송장 등록 (제공자/운영자용)</label>
            <select id="carrierCode" v-model="invoiceForm.carrierCode">
              <option v-for="c in order.carriers" :key="c.key" :value="c.key">{{ c.label }}</option>
            </select>
          </div>
          <div class="info-field-items">
            <input type="text" v-model="invoiceForm.invoiceNo" placeholder="송장번호" />
          </div>
          <button type="button" class="formBtn" @click="registerInvoice">송장 등록 (발송처리)</button>
        </div>

        <div v-else>
          <ul class="list_wrapper">
            <li class="list_items"><span class="label">배송상태</span><span class="data_val">{{ order.deliveryStatusLabel }}</span></li>
            <li class="list_items"><span class="label">택배사</span><span class="data_val">{{ order.carrierLabel }}</span></li>
            <li class="list_items"><span class="label">송장번호</span><span class="data_val">{{ order.invoiceNo }}</span></li>
            <li class="list_items" v-if="order.deliveryAddress"><span class="label">배송지</span><span class="data_val">{{ order.deliveryAddress }} {{ order.deliveryAddressDetail }}</span></li>
          </ul>

          <button type="button" class="formBtn" v-if="order.deliveryStatus === 'SHIPPED'" @click="updateDeliveryStatus('IN_TRANSIT')">배송중으로 변경 (운영자용)</button>
          <button type="button" class="formBtn" v-if="order.deliveryStatus === 'SHIPPED' || order.deliveryStatus === 'IN_TRANSIT'" @click="updateDeliveryStatus('DELIVERED')">배송완료로 변경 (운영자용)</button>
          <button type="button" class="blueBtn u-confirm" v-if="order.deliveryStatus === 'DELIVERED'" @click="confirmReceipt">수취확인 (구매확정)</button>
        </div>
      </div>

      <div style="margin-top: 24px">
        <router-link to="/orders">내 주문내역</router-link>
      </div>
    </div>
  </section>
</template>

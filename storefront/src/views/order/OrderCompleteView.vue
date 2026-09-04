<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS order/step2.html 재현 (Thymeleaf 버전 order/order-complete.html과 동일 출처).
const route = useRoute()
const router = useRouter()
const groups = ref([])
const totalPoint = ref(0)
const loading = ref(true)

const allOrders = computed(() => groups.value.flatMap((g) => g.orders))
const orderIdsText = computed(() => allOrders.value.map((o) => o.orderId).join(', '))
const orderedDate = computed(() => (allOrders.value[0] ? allOrders.value[0].createdDate?.replace('T', ' ').slice(0, 16) : ''))
const first = computed(() => allOrders.value[0] ?? {})

onMounted(async () => {
  const orderIds = route.query.orderIds
  if (!orderIds) {
    router.replace('/cart')
    return
  }
  try {
    const data = await api.get('order', `/api/checkout/done?orderIds=${encodeURIComponent(orderIds)}`)
    groups.value = data.groups
    totalPoint.value = data.totalPoint
  } catch {
    router.replace('/cart')
    return
  } finally {
    loading.value = false
  }
})

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="cart_contents">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        장바구니
      </span>
      <h2 class="page-title-txt">주문완료</h2>
      <div class="steps-box">
        <div class="step_status cart"><div class="icon-bg"><span class="sr-only">장바구니</span></div></div>
        <div class="step_status payment"><div class="icon-bg"><span class="sr-only">주문결제</span></div></div>
        <div class="step_status complete on"><div class="icon-bg"><span class="sr-only">현재단계 주문완료</span></div></div>
      </div>
    </div>
  </section>

  <section id="contents" v-if="!loading">
    <div class="center s-contents">
      <div class="row_con flex order_case">
        <div class="o__num"><span>주문번호 : </span><span>{{ orderIdsText }}</span></div>
        <div class="o__date"><span>주문일자 : </span><span>{{ orderedDate }}</span></div>
      </div>

      <div class="contents_wrap" v-for="g in groups" :key="g.locgovNm">
        <div class="margin_b80">
          <div class="cart_top"><div class="chk_box check-level1"><label>{{ g.locgovNm }}</label></div></div>
          <div class="list_body">
            <div class="list_wrap">
              <div class="list-title">
                <div class="date-col chk_date">답례품정보</div>
                <div class="date-col g_amtprc">
                  <div class="g_info_wrap">
                    <div class="date-col g_info__amount">수량</div>
                    <div class="date-col g_info__price">답례품 포인트</div>
                  </div>
                </div>
              </div>
              <ul class="item_list-group">
                <li class="list-items" v-for="o in g.orders" :key="o.orderId">
                  <div class="date-col g_info">
                    <div class="g_info_wrap"><div class="link_wrap"><div class="g_info__txt"><div class="info_title"><span>{{ o.itemName }}</span></div></div></div></div>
                  </div>
                  <div class="date-col g_amtprc">
                    <div class="g_info_wrap">
                      <div class="g_info__amount">{{ o.quantity }}개</div>
                      <div class="g_info__price">{{ formatN(o.pointAmount) }} P</div>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="row_con flex" v-if="groups.length">
        <div class="mobile_section_bar"></div>
        <div class="contents_wrap">
          <div class="row_con_wrapper orderC">
            <div class="list_title_area">배송지 정보</div>
            <ul class="list_wrapper">
              <li class="list_items"><span class="label">받으시는분</span><span class="data_val">{{ first.receiverName }}</span></li>
              <li class="list_items"><span class="label">배송지 주소</span><span class="data_val">{{ first.deliveryAddress }} {{ first.deliveryAddressDetail }}</span></li>
              <li class="list_items"><span class="label">휴대폰번호(수취인)</span><span class="data_val">{{ first.receiverPhone }}</span></li>
              <li class="list_items"><span class="label">기타 요구사항</span><span class="data_val">{{ first.requestNote }}</span></li>
            </ul>
          </div>
        </div>
        <div class="mobile_section_bar"></div>
        <div class="contents_wrap">
          <div class="row_con_wrapper orderC">
            <div class="list_title_area">결제정보</div>
            <div class="list_wrapper" v-for="g in groups" :key="g.locgovNm">
              <div class="list_loc">{{ g.locgovNm }}</div>
              <ul class="paymentC">
                <li class="payment_list"><span class="payment__label">답례품 포인트</span><span class="payment__val">{{ formatN(g.groupTotal) }}P</span></li>
                <li class="payment_list"><span class="payment__label">배송비</span><span class="payment__val">무료배송</span></li>
              </ul>
              <div class="payment_list total_pay">
                <strong class="label">결제 포인트</strong>
                <div class="payment_wrap"><div class="payment_val pointblue">{{ formatN(g.groupTotal) }}P</div></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="btn-box order">
      <button type="button" class="blueBtn cancellation" @click="router.push('/gifts')">
        답례품 더 보기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
      </button>
      <button type="button" class="blueBtn u-confirm" @click="router.push('/orders')">
        주문 조회<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { modalAlert, modalConfirm } from '../../composables/useModal'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { loadDaumPostcode } from '../../utils/daumPostcode'
import { formatN } from '../../utils/format'

// AS-IS order/step1.html 재현 (Thymeleaf 버전 order/checkout.html과 동일 출처). 장바구니에서
// 선택한 cartItemId 목록을 쿼리로 받아 읽기전용 재표시 + 배송지입력 + 결제하기.
const route = useRoute()
const router = useRouter()

const cartItemIds = Array.isArray(route.query.cartItemId)
  ? route.query.cartItemId.map(Number)
  : route.query.cartItemId
    ? [Number(route.query.cartItemId)]
    : []

const groups = ref([])
// 쿠폰 할인/배송비까지 반영된 실제 결제 금액. 화면에서 직접 계산하지 않고 서버의
// /api/checkout/preview를 그대로 쓴다 - 결제(complete)와 완전히 같은 CartService 계산이라
// "화면에 뜬 포인트 != 실제 차감 포인트"가 구조적으로 생길 수 없다.
const quote = ref(null)
const couponsByCartItem = ref({})
const errorMessage = ref('')
const loading = ref(true)
const submitting = ref(false)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  post: '',
  address: '',
  addressDetail: '',
  requestNote: '',
  agree: false,
})
const couponSelections = reactive({})

async function load() {
  if (cartItemIds.length === 0) {
    router.replace('/cart?errorMessage=' + encodeURIComponent('답례품을 선택해 주세요.'))
    return
  }
  loading.value = true
  try {
    const data = await api.post('order', '/api/checkout/review', { cartItemId: cartItemIds })
    groups.value = data.groups
    couponsByCartItem.value = data.couponsByCartItem
    await refreshQuote()
  } catch (e) {
    router.replace('/cart?errorMessage=' + encodeURIComponent(e.message))
  } finally {
    loading.value = false
  }
}
onMounted(load)

function selectedCoupons() {
  const couponByCartItem = {}
  Object.entries(couponSelections).forEach(([cartItemId, couponUserId]) => {
    if (couponUserId) couponByCartItem[cartItemId] = Number(couponUserId)
  })
  return couponByCartItem
}

async function refreshQuote() {
  try {
    quote.value = await api.post('order', '/api/checkout/preview', {
      cartItemId: cartItemIds,
      couponByCartItem: selectedCoupons(),
      deliveryAddress: form.address || null,
    })
    errorMessage.value = ''
  } catch (e) {
    // 같은 쿠폰 중복선택 등은 결제 전에 여기서 먼저 드러난다.
    quote.value = null
    errorMessage.value = e.message
  }
}

// 쿠폰 선택과 배송지(제주·도서산간 추가배송비)가 바뀌면 금액을 다시 받아온다.
watch([couponSelections, () => form.address], () => {
  if (!loading.value) refreshQuote()
})

const lineQuotes = computed(() => {
  const byCartItem = {}
  quote.value?.groups.forEach((g) => g.lines.forEach((l) => (byCartItem[l.cartItemId] = l)))
  return byCartItem
})
const groupQuotes = computed(() => {
  const byLocgov = {}
  quote.value?.groups.forEach((g) => (byLocgov[g.locgovCode] = g))
  return byLocgov
})

async function searchAddress() {
  await loadDaumPostcode()
  new window.daum.Postcode({
    oncomplete(data) {
      form.post = data.zonecode
      form.address = data.roadAddress || data.jibunAddress
    },
  }).open()
}

// 쿠폰 사용 UI 숨김 (2026-09-09). 쿠폰 기능이 AS-IS에서도 미사용이라 자동발급을 멈추고
// 화면 진입점을 전부 감췄다 - 여기만 라우트가 아니라 결제 화면 안의 select라서 플래그로
// 끈다. 서버의 /api/checkout/review·preview·complete는 쿠폰 계약을 그대로 유지하므로
// (선택된 쿠폰이 없으면 할인 0) 이 값을 true로 되돌리면 그대로 복구된다.
// 쿠폰을 고를 수 없으면 할인은 항상 0이라, 아래 할인 표시들(라인/그룹/합계)은 각자
// discount > 0 조건 때문에 자동으로 사라진다.
const COUPON_ENABLED = false

function couponLabel(c) {
  return c.payType === '1' ? `${c.couponName} (${formatN(c.pay)}P 할인)` : `${c.couponName} (${c.pay}% 할인)`
}

/** 포인트 부족 판정은 반드시 배송비까지 더한 groupPayable 기준 (서버 체크아웃과 동일). */
function insufficient(g) {
  const q = groupQuotes.value[g.locgovCode]
  return q ? q.groupPayable > q.givePoint : false
}

async function submit() {
  errorMessage.value = ''
  // AS-IS order/step1.html:1562~1667 - 결제 직전 배송지/받는분 필드와 포인트 잔액을 각각 확인한다.
  // (입력칸의 required는 폼 submit이 아니라 발동하지 않으므로 여기서 직접 막는다.)
  if (!form.receiverName.trim()) {
    modalAlert('받으시는 분을 입력해 주세요.')
    return
  }
  if (!form.receiverPhone.trim()) {
    modalAlert('연락처를 입력해 주세요.')
    return
  }
  if (!form.post.trim() || !form.address.trim()) {
    modalAlert('배송지 주소를 입력해 주세요.')
    return
  }
  if (!form.addressDetail.trim()) {
    modalAlert('배송지 상세주소를 입력해 주세요.')
    return
  }
  const short = groups.value.find((g) => insufficient(g))
  if (short) {
    // AS-IS order/step1.html:1425
    modalAlert(`${short.locgovNm || short.locgovCode} 포인트가 부족합니다.`)
    return
  }
  if (!form.agree) {
    // AS-IS order/step1.html:1578
    modalAlert('구매에 동의해주시기 바랍니다.')
    return
  }
  submitting.value = true
  try {
    const couponByCartItem = selectedCoupons()
    const res = await api.post('order', '/api/checkout/complete', {
      cartItemId: cartItemIds,
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      deliveryAddress: form.address,
      deliveryAddressDetail: form.addressDetail,
      requestNote: form.requestNote,
      couponByCartItem,
    })
    router.push({ path: '/checkout/done', query: { orderIds: res.orderIds.join(',') } })
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="cart_contents">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/cart">장바구니</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        주문결제
      </span>
      <h2 class="page-title-txt">주문/결제</h2>
      <div class="steps-box">
        <div class="step_status cart"><div class="icon-bg"><span class="sr-only">장바구니</span></div></div>
        <div class="step_status payment on"><div class="icon-bg"><span class="sr-only">현재단계 주문결제</span></div></div>
        <div class="step_status complete"><div class="icon-bg"><span class="sr-only">주문완료</span></div></div>
      </div>
    </div>
  </section>

  <section id="contents" v-if="!loading">
    <p class="error" v-if="errorMessage" style="text-align: center">{{ errorMessage }}</p>

    <div class="contents_wrap" v-for="g in groups" :key="g.locgovCode">
      <div class="center">
        <div class="s-contents">
          <div class="cart_top">
            <div class="chk_box check-level1"><label>{{ g.locgovNm }}</label></div>
            <div class="grade-bg">잔여 포인트 : <span class="blue-txt">{{ formatN(g.givePoint) }} P</span></div>
          </div>
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
                <li class="list-items" v-for="line in g.lines" :key="line.cartItemId">
                  <div class="date-col g_info">
                    <div class="g_info_wrap">
                      <div class="link_wrap">
                        <div class="g_info__img"><img :src="line.thumbnailUrl ?? '/images/thumb.png'" :alt="line.itemName + ' 이미지'" /></div>
                        <div class="g_info__txt"><div class="info_title"><span>{{ line.itemName }}</span></div></div>
                      </div>
                    </div>
                  </div>
                  <div class="date-col g_amtprc">
                    <div class="g_info_wrap">
                      <div class="g_info__amount">{{ line.quantity }}개</div>
                      <div class="g_info__price">
                        {{ formatN(line.lineTotal) }} P
                        <span class="pointRed" v-if="lineQuotes[line.cartItemId]?.discount > 0">
                          - {{ formatN(lineQuotes[line.cartItemId].discount) }} P (쿠폰)
                        </span>
                        <span v-if="lineQuotes[line.cartItemId]?.deliveryFee > 0">
                          + {{ formatN(lineQuotes[line.cartItemId].deliveryFee) }} P (배송비)
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="date-col coupon-select" v-if="COUPON_ENABLED && couponsByCartItem[line.cartItemId]?.length">
                    <label :for="'coupon_' + line.cartItemId">쿠폰 사용</label>
                    <select :id="'coupon_' + line.cartItemId" v-model="couponSelections[line.cartItemId]">
                      <option value="">쿠폰 미사용</option>
                      <option v-for="c in couponsByCartItem[line.cartItemId]" :key="c.couponUserId" :value="c.couponUserId">
                        {{ couponLabel(c) }}
                      </option>
                    </select>
                  </div>
                </li>
              </ul>
            </div>
            <div class="cart_bot" v-if="groupQuotes[g.locgovCode]">
              <div class="smallTxt">
                답례품 포인트 <span class="bot_P">{{ formatN(groupQuotes[g.locgovCode].groupTotal) }} P</span>
                <template v-if="groupQuotes[g.locgovCode].groupDiscount > 0">
                  - 쿠폰할인 <span class="bot_P">{{ formatN(groupQuotes[g.locgovCode].groupDiscount) }} P</span>
                </template>
                <template v-if="groupQuotes[g.locgovCode].groupDeliveryFee > 0">
                  + 배송비 <span class="bot_P">{{ formatN(groupQuotes[g.locgovCode].groupDeliveryFee) }} P</span> =
                </template>
                <template v-else>+ 무료배송 =</template>
              </div>
              <div class="bigTxt">
                <span class="bot_txt">결제 예정 포인트</span>
                <span class="bot_price" :class="insufficient(g) ? 'pointRed' : 'pointblue'">{{ formatN(groupQuotes[g.locgovCode].groupPayable) }}P</span>
                <span v-if="insufficient(g)"> ( 구매불가 )</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mobile_section_bar"></div>
    <div class="contents_wrap">
      <div class="center user_form">
        <div class="s-contents deliver">
          <fieldset>
            <div class="add-info-area">
              <div class="lable-field">
                <h3><span>배송지</span></h3>
                <p class="essential"><span>*</span><span>필수 입력 정보</span></p>
              </div>
              <div class="info-field">
                <div class="info-field-group">
                  <div class="info-field-items">
                    <label for="receiverName" class="flied-title">받으시는분<span class="essential"> *</span></label>
                    <span class="form-field"><input type="text" id="receiverName" v-model="form.receiverName" required /></span>
                  </div>
                  <div class="info-field-items userAddress">
                    <label for="post" class="flied-title">배송지주소<span class="essential"> *</span></label>
                    <div class="form-field">
                      <div class="search-address m-field">
                        <input type="text" id="post" v-model="form.post" placeholder="우편번호" readonly required />
                        <button type="button" class="formBtn" @click="searchAddress">주소찾기</button>
                      </div>
                      <div class="input-address">
                        <input type="text" id="address" v-model="form.address" placeholder="주소" readonly required />
                        <input type="text" id="addressDetail" v-model="form.addressDetail" placeholder="상세주소" aria-label="상세주소" />
                      </div>
                    </div>
                  </div>
                  <div class="info-field-items userNum">
                    <label for="receiverPhone" class="flied-title">휴대폰번호<span class="essential"> *</span></label>
                    <span class="form-field"><input type="text" id="receiverPhone" v-model="form.receiverPhone" placeholder="010-0000-0000" required /></span>
                  </div>
                  <div class="info-field-items">
                    <label for="requestNote" class="flied-title">배송시 요구사항</label>
                    <span class="form-field"><input type="text" id="requestNote" v-model="form.requestNote" maxlength="150" /></span>
                  </div>
                </div>
              </div>
            </div>
          </fieldset>
        </div>

        <div class="mobile_section_bar"></div>
        <div class="s-contents payment">
          <h3>결제하기</h3>
          <ul class="each_loc_list" v-if="quote">
            <li class="loc_list" v-for="q in quote.groups" :key="q.locgovCode">
              <div class="loc_name">{{ q.locgovNm }}</div>
              <div class="last_point_info">
                <span class="lebel_title">최종 결제 포인트</span>
                <span class="pointblue">{{ formatN(q.groupPayable) }}P</span>
              </div>
              <div class="last_point_info">
                <span class="lebel_title">포인트 예상 잔액</span>
                <span :class="q.groupPayable > q.givePoint ? 'pointRed' : ''">{{ formatN(q.givePoint - q.groupPayable) }}P</span>
              </div>
            </li>
          </ul>
          <div class="total_point" v-if="quote">
            <div class="last_point_info"><span class="lebel_title">답례품 포인트</span><span>{{ formatN(quote.totalPoint) }}P</span></div>
            <div class="last_point_info" v-if="quote.totalDiscount > 0">
              <span class="lebel_title">쿠폰 할인</span><span class="pointRed">- {{ formatN(quote.totalDiscount) }}P</span>
            </div>
            <div class="last_point_info">
              <span class="lebel_title">배송비</span>
              <span>{{ quote.totalDeliveryFee > 0 ? formatN(quote.totalDeliveryFee) + 'P' : '무료배송' }}</span>
            </div>
            <div class="last_point_info"><span class="lebel_title">최종 결제 포인트</span><span class="pointblue bigTxt">{{ formatN(quote.totalPayable) }}P</span></div>
          </div>
          <div class="cart_info_txt">
            <ul>
              <li class="txt__list">주문할 답례품의 답례품명, 답례품가격, 배송정보를 확인하였으며 구매에 동의 하시겠습니까?</li>
              <li class="txt__list"><input type="checkbox" id="agree" v-model="form.agree" required /><label for="agree">동의</label></li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div class="btn-box order">
      <button type="button" class="blueBtn cancellation" @click="router.push('/cart')">
        취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="이전화면으로 이동" /></span>
      </button>
      <button type="button" class="blueBtn u-confirm" :disabled="submitting || !quote" @click="submit">
        <span>{{ formatN(quote?.totalPayable ?? 0) }} P</span>&nbsp;결제하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="답례품 주문하기" /></span>
      </button>
    </div>
  </section>
</template>

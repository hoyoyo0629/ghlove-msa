<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { loadDaumPostcode } from '../../utils/daumPostcode'

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
const totalPoint = ref(0)
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
    totalPoint.value = data.totalPoint
    couponsByCartItem.value = data.couponsByCartItem
  } catch (e) {
    router.replace('/cart?errorMessage=' + encodeURIComponent(e.message))
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function searchAddress() {
  await loadDaumPostcode()
  new window.daum.Postcode({
    oncomplete(data) {
      form.post = data.zonecode
      form.address = data.roadAddress || data.jibunAddress
    },
  }).open()
}

function couponLabel(c) {
  return c.payType === '1' ? `${c.couponName} (${new Intl.NumberFormat('ko-KR').format(c.pay)}P 할인)` : `${c.couponName} (${c.pay}% 할인)`
}

function insufficient(g) {
  return g.groupTotal > g.givePoint
}

async function submit() {
  errorMessage.value = ''
  if (!form.agree) {
    errorMessage.value = '구매 동의가 필요합니다.'
    return
  }
  submitting.value = true
  try {
    const couponByCartItem = {}
    Object.entries(couponSelections).forEach(([cartItemId, couponUserId]) => {
      if (couponUserId) couponByCartItem[cartItemId] = Number(couponUserId)
    })
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
                      <div class="g_info__price">{{ formatN(line.lineTotal) }} P</div>
                    </div>
                  </div>
                  <div class="date-col coupon-select" v-if="couponsByCartItem[line.cartItemId]?.length">
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
            <div class="cart_bot">
              <div class="smallTxt">답례품 포인트 <span class="bot_P">{{ formatN(g.groupTotal) }} P</span> + 무료배송 =</div>
              <div class="bigTxt">
                <span class="bot_txt">결제 예정 포인트</span>
                <span class="bot_price" :class="insufficient(g) ? 'pointRed' : 'pointblue'">{{ formatN(g.groupTotal) }}P</span>
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
          <ul class="each_loc_list">
            <li class="loc_list" v-for="g in groups" :key="g.locgovCode">
              <div class="loc_name">{{ g.locgovNm }}</div>
              <div class="last_point_info">
                <span class="lebel_title">최종 결제 포인트</span>
                <span class="pointblue">{{ formatN(g.groupTotal) }}P</span>
              </div>
              <div class="last_point_info">
                <span class="lebel_title">포인트 예상 잔액</span>
                <span>{{ formatN(g.givePoint - g.groupTotal) }}P</span>
              </div>
            </li>
          </ul>
          <div class="total_point">
            <div class="last_point_info"><span class="lebel_title">배송비</span><span>무료배송</span></div>
            <div class="last_point_info"><span class="lebel_title">답례품 포인트</span><span class="pointblue bigTxt">{{ formatN(totalPoint) }}P</span></div>
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
      <button type="button" class="blueBtn u-confirm" :disabled="submitting" @click="submit">
        <span>{{ formatN(totalPoint) }} P</span>&nbsp;결제하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="답례품 주문하기" /></span>
      </button>
    </div>
  </section>
</template>

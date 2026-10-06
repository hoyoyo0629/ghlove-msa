<script setup>
import { modalAlert, modalConfirm } from '../../composables/useModal'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { formatN } from '../../utils/format'

// AS-IS items/details-main.html 재현(gift 서비스 detail.html과 동일 출처). AS-IS의 옵션(단일/조합형)
// 선택 UI는 이 프로젝트의 Gift 도메인에 옵션 개념 자체가 없어 스코프 밖, 이미지 갤러리도 swiper 없이
// 메인이미지/썸네일 교체만 재현한다. 5개 탭은 AS-IS와 동일하게 전부 세로로 렌더링하고 스크롤 이동만 한다.
const props = defineProps({ itemId: { type: [String, Number], required: true } })
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const detail = ref(null)
const loading = ref(true)
const mainImage = ref('')
const activeTab = ref('nav-detail')
const errorMessage = ref(route.query.errorMessage ?? '')

const reviewForm = ref({ userName: '', orderCode: route.query.orderCode ?? '', subject: '', content: '', score: 5, recommend: false, images: null })
const inquiryForm = ref({ question: '', secret: false })

// 옵션 선택 (AS-IS 동일) - 옵션 있는 답례품은 선택해야 담기/구매 가능하다.
// 단일형(S)은 selectedOptionId 드롭다운, 조합형(S2·S3)은 sel1→sel2→sel3 종속 드롭다운으로
// 최종 옵션행(itemOptionId)을 해석한다. (S2·T는 판매자 화면에서 숨겨 사실상 S/S3만 노출되나,
// 데이터가 있으면 구매자 화면은 AS-IS처럼 렌더한다.)
const selectedOptionId = ref('')
const sel1 = ref('')
const sel2 = ref('')
const sel3 = ref('')
const optType = computed(() => detail.value?.gift?.itemOptionType || 'S')

const level1 = computed(() =>
  [...new Set((detail.value?.options || []).map((o) => o.optionName))].filter(Boolean))
const level2 = computed(() =>
  [...new Set((detail.value?.options || []).filter((o) => o.optionName === sel1.value)
    .map((o) => o.optionName2))].filter(Boolean))
const level3 = computed(() =>
  [...new Set((detail.value?.options || [])
    .filter((o) => o.optionName === sel1.value && o.optionName2 === sel2.value)
    .map((o) => o.optionName3))].filter(Boolean))

// 선택 결과를 최종 옵션행으로 해석 (단일: 드롭다운 값, 조합: name 조합 매칭).
const chosenOption = computed(() => {
  const opts = detail.value?.options || []
  if (!opts.length) return null
  if (optType.value === 'S') return opts.find((o) => o.itemOptionId === Number(selectedOptionId.value)) || null
  return opts.find((o) => o.optionName === sel1.value
    && o.optionName2 === sel2.value
    && (optType.value !== 'S3' || o.optionName3 === sel3.value)) || null
})

// 각인(필수 추가정보) - itemTextOptionFlag=Y면 제목별 입력칸을 띄운다.
const textValues = ref(['', '', ''])
const textTitles = computed(() => {
  const g = detail.value?.gift
  if (!g || g.itemTextOptionFlag !== 'Y') return []
  return [g.itemTextOptionTitle1, g.itemTextOptionTitle2, g.itemTextOptionTitle3].filter((t) => t && t.trim())
})

// 추가구성 - 본품과 별도로 함께 담을 부가 답례품 선택.
const selectedAdditions = ref([])

// 수량 선택 (AS-IS 답례품 상세 +/- · 최대 주문 수량) - 1 ~ min(최대주문수량, 재고).
const quantity = ref(1)
const maxQuantity = computed(() => {
  const g = detail.value?.gift
  if (!g) return 1
  const stock = g.stockQuantity != null ? g.stockQuantity : Infinity
  const max = g.orderMaxQuantity != null && g.orderMaxQuantity > 0 ? g.orderMaxQuantity : Infinity
  const lim = Math.min(stock, max)
  return Number.isFinite(lim) ? Math.max(1, lim) : 999
})
function incQuantity() {
  if (quantity.value < maxQuantity.value) quantity.value++
  else modalAlert(`최대 주문 수량은 ${maxQuantity.value}개입니다.`)
}
function decQuantity() {
  if (quantity.value > 1) quantity.value--
}
function onQuantityInput() {
  let q = Math.floor(Number(quantity.value) || 1)
  if (q < 1) q = 1
  if (q > maxQuantity.value) q = maxQuantity.value
  quantity.value = q
}

function cartPayload() {
  const payload = { itemId: Number(props.itemId), quantity: quantity.value }
  if (detail.value?.options?.length) {
    const opt = chosenOption.value
    if (!opt) {
      modalAlert('옵션을 선택해 주세요.')
      return null
    }
    if (opt.soldOut) {
      modalAlert('품절된 옵션입니다.')
      return null
    }
    payload.itemOptionId = opt.itemOptionId
  }
  // 필수 추가정보 - AS-IS textOption 저장 포맷 "제목 : 값 || 제목 : 값".
  if (textTitles.value.length) {
    for (let i = 0; i < textTitles.value.length; i++) {
      if (!textValues.value[i] || !textValues.value[i].trim()) {
        modalAlert(`'${textTitles.value[i]}'을(를) 입력해 주세요.`)
        return null
      }
    }
    payload.textOption = textTitles.value.map((t, i) => `${t} : ${textValues.value[i].trim()}`).join('||')
  }
  // 추가구성 (order 장바구니/체크아웃 배선은 Phase 4에서 소비. 서버는 미지원 필드를 무시한다.)
  if (selectedAdditions.value.length) {
    payload.additionItemIds = [...selectedAdditions.value]
  }
  return payload
}

async function load() {
  loading.value = true
  try {
    detail.value = await api.get('gift', `/api/gifts/${props.itemId}/detail`)
    mainImage.value = detail.value.imageUrls[0] ? api.assetUrl('gift', detail.value.imageUrls[0]) : '/images/thumb.png'
  } finally {
    loading.value = false
  }
  if (route.query.orderCode) {
    requestAnimationFrame(() => document.getElementById('review-write')?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
  }
}
onMounted(load)

const imageUrls = computed(() => (detail.value?.imageUrls ?? []).map((p) => api.assetUrl('gift', p)))

function showItemTab(name) {
  activeTab.value = name
  requestAnimationFrame(() => document.getElementById(name)?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
}

function requireLogin() {
  router.push({ path: '/login', query: { target: route.fullPath } })
}

async function toggleWishlist() {
  if (!auth.loggedIn) return requireLogin()
  const res = await api.post('gift', `/wishlist/${props.itemId}/toggle`)
  detail.value.wishlisted = res.wishlisted
  // 운영은 선택/해제 각각 알림 모달을 띄운다(확인만).
  modalAlert(res.wishlisted ? '관심답례품에 추가 되었습니다.' : '해당 답례품이 관심답례품에서 삭제되었습니다.')
}

async function addToCart() {
  if (!auth.loggedIn) return requireLogin()
  const payload = cartPayload()
  if (!payload) return
  try {
    await api.post('order', '/api/cart/items', payload)
    // 운영은 이동 여부를 묻지 않고 단순 알림(확인만)만 띄운다.
    modalAlert('장바구니에 담았습니다.')
  } catch (e) {
    modalAlert(e.message)
  }
}

async function buyNow() {
  if (!auth.loggedIn) return requireLogin()
  const payload = cartPayload()
  if (!payload) return
  try {
    await api.post('order', '/api/cart/items', payload)
    const cart = await api.get('order', '/api/cart')
    const line = cart.flatMap((g) => g.lines).find((l) => l.itemId === Number(props.itemId)
      && (!payload.itemOptionId || l.optionName != null))
    if (!line) throw new Error('장바구니 담기에 실패했습니다.')
    router.push({ path: '/checkout', query: { cartItemId: [line.cartItemId] } })
  } catch (e) {
    modalAlert(e.message)
  }
}

async function submitReview() {
  if (!auth.loggedIn) return requireLogin()
  const f = reviewForm.value
  if (!f.subject.trim() || !f.content.trim()) {
    modalAlert('제목과 내용을 입력해 주세요.')
    return
  }
  const form = new FormData()
  if (f.userName) form.append('userName', f.userName)
  if (f.orderCode) form.append('orderCode', f.orderCode)
  form.append('subject', f.subject)
  form.append('content', f.content)
  form.append('score', f.score)
  form.append('recommend', f.recommend)
  if (f.images) for (const file of f.images) form.append('images', file)
  try {
    await api.postForm('gift', `/api/gifts/${props.itemId}/reviews`, form)
    reviewForm.value = { userName: '', orderCode: '', subject: '', content: '', score: 5, recommend: false, images: null }
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function submitInquiry() {
  if (!auth.loggedIn) return requireLogin()
  const f = inquiryForm.value
  if (!f.question.trim()) {
    modalAlert('문의 내용을 입력해 주세요.')
    return
  }
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/inquiries`, { question: f.question, secret: f.secret })
    inquiryForm.value = { question: '', secret: false }
    // AS-IS items/details-main.html:2612
    modalAlert('답례품Q&A가 등록되었습니다.')
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function reportReview(review) {
  if (!auth.loggedIn) return requireLogin()
  if (review.reportedByMe) { modalAlert('이미 신고한 리뷰입니다.'); return }
  if (!(await modalConfirm('이 리뷰를 신고하시겠습니까?'))) return
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/reviews/${review.itemReviewId}/report`, {})
    await load()
  } catch (e) {
    modalAlert(e.message)
  }
}

async function reportInquiry(inquiry) {
  if (!auth.loggedIn) return requireLogin()
  if (inquiry.reportedByMe) { modalAlert('이미 신고한 문의입니다.'); return }
  if (!(await modalConfirm('이 문의를 신고하시겠습니까?'))) return
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/inquiries/${inquiry.inquiryId}/report`, {})
    await load()
  } catch (e) {
    modalAlert(e.message)
  }
}

// 재입고 알림 신청 (AS-IS ItemController:879, 품절 답례품 상세의 "재입고 알림" 버튼).
async function requestRestock() {
  if (!auth.loggedIn) return requireLogin()
  try {
    const res = await api.post('gift', `/gifts/${props.itemId}/restock-notice`)
    detail.value.restockRequested = true
    modalAlert(res.message || '재입고 시 알려드리겠습니다.')
  } catch (e) {
    modalAlert(e.message)
  }
}

// 상품평 좋아요 (AS-IS ItemController:899, 비로그인도 IP로 가능·취소 없음). liked=false면 이미 누른 상태.
async function likeReview(r) {
  try {
    const res = await api.post('gift', `/reviews/${r.itemReviewId}/like`)
    if (res.liked) {
      r.likeCount = (r.likeCount ?? 0) + 1
      r.likedByMe = true
    }
  } catch (e) {
    modalAlert(e.message)
  }
}
</script>

<template>
  <template v-if="detail">
    <section class="page-title-box mall center">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/gifts">답례품</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <span>{{ detail.gift.itemName }}</span>
      </span>
    </section>

    <section class="center" id="contents">
      <p class="error" v-if="errorMessage" style="color: #d40000">{{ errorMessage }}</p>

      <div class="goods_view_top">
        <div class="goods_img">
          <div class="target_img">
            <div class="img_area">
              <img id="mainItemImage" class="swiper_img" :src="mainImage" :alt="detail.gift.itemName" />
            </div>
            <img class="sold_out" v-if="detail.gift.soldOut === '1'" src="/images/goods/goods_sold-out.png" alt="품절" />
          </div>
          <div class="thumb_box" v-if="imageUrls.length > 1">
            <div class="thumb_area">
              <button type="button" class="cursor swiper_img" v-for="src in imageUrls" :key="src" @click="mainImage = src">
                <img :src="src" :alt="detail.gift.itemName + ' 썸네일'" />
              </button>
            </div>
          </div>
        </div>

        <div class="goods_info">
          <div class="info_row">
            <div class="brand_mall d_area" v-if="detail.locgovName">
              <img src="/images/icon/d-area.png" alt="" />
              <span>{{ detail.locgovName }}</span>
            </div>
          </div>

          <div class="info_box">
            <div class="info_title">
              <h2>{{ detail.gift.itemName }}</h2>
              <p class="s-txt">{{ detail.gift.itemSummary }}</p>
            </div>
            <div class="present_price"><strong>{{ formatN(detail.gift.salePrice) }}</strong> P</div>
          </div>

          <!-- SFR-005 옵션 선택 (AS-IS 동일) - 선택한 옵션이 장바구니·주문·주문상세까지 반영된다. -->
          <!-- 단일형(S) -->
          <div class="info_row" v-if="detail.options.length && optType === 'S'">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">옵션 선택</p>
              <select v-model="selectedOptionId" style="width: 100%; padding: 8px">
                <option value="">옵션을 선택하세요</option>
                <option v-for="o in detail.options" :key="o.itemOptionId" :value="o.itemOptionId" :disabled="o.soldOut">
                  {{ o.optionName }}<template v-if="o.optionPrice"> (+{{ formatN(o.optionPrice) }}P)</template><template v-if="o.soldOut"> [품절]</template><template v-else-if="o.stockTracked && o.stockQuantity > 0"> | 재고 {{ o.stockQuantity }}개</template>
                </option>
              </select>
            </div>
          </div>

          <!-- 조합형(S2·S3) 종속 드롭다운 -->
          <div class="info_row" v-if="detail.options.length && (optType === 'S2' || optType === 'S3')">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">옵션 선택</p>
              <select v-model="sel1" @change="sel2 = ''; sel3 = ''" style="width: 100%; padding: 8px; margin-bottom: 4px">
                <option value="">1단계 선택</option>
                <option v-for="n in level1" :key="n" :value="n">{{ n }}</option>
              </select>
              <select v-if="sel1" v-model="sel2" @change="sel3 = ''" style="width: 100%; padding: 8px; margin-bottom: 4px">
                <option value="">2단계 선택</option>
                <option v-for="n in level2" :key="n" :value="n">{{ n }}</option>
              </select>
              <select v-if="optType === 'S3' && sel2" v-model="sel3" style="width: 100%; padding: 8px">
                <option value="">3단계 선택</option>
                <option v-for="n in level3" :key="n" :value="n">{{ n }}</option>
              </select>
              <p class="s-txt" v-if="chosenOption" style="margin-top: 4px">
                선택: {{ chosenOption.optionName }}<template v-if="chosenOption.optionName2"> / {{ chosenOption.optionName2 }}</template><template v-if="chosenOption.optionName3"> / {{ chosenOption.optionName3 }}</template><template v-if="chosenOption.optionPrice"> (+{{ formatN(chosenOption.optionPrice) }}P)</template>
              </p>
            </div>
          </div>

          <!-- 필수 추가정보 (AS-IS itemTextOptionFlag) -->
          <div class="info_row" v-if="textTitles.length">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">필수 추가정보 입력</p>
              <div v-for="(t, i) in textTitles" :key="i" style="margin-bottom: 4px">
                <input type="text" v-model="textValues[i]" :placeholder="t" maxlength="30" style="width: 100%; padding: 8px" />
              </div>
            </div>
          </div>

          <!-- 추가구성 -->
          <div class="info_row" v-if="detail.additions && detail.additions.length">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">추가구성</p>
              <label v-for="a in detail.additions" :key="a.itemId" style="display: block; margin-bottom: 2px">
                <input type="checkbox" :value="a.itemId" v-model="selectedAdditions" :disabled="a.soldOut" />
                {{ a.itemName }}<template v-if="a.salePrice"> (+{{ formatN(a.salePrice) }}P)</template><template v-if="a.soldOut"> [품절]</template>
              </label>
            </div>
          </div>

          <!-- 수량 선택 (AS-IS 답례품 상세 +/- · 최대 주문 수량) -->
          <div class="info_row">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">수량</p>
              <div style="display: flex; align-items: center; gap: 4px">
                <button type="button" class="btn_minus" @click="decQuantity" aria-label="수량 감소">−</button>
                <input type="text" class="number" style="width: 64px; text-align: center" v-model="quantity" @input="onQuantityInput" aria-label="수량" />
                <button type="button" class="btn_plus" @click="incQuantity" aria-label="수량 증가">+</button>
              </div>
              <span class="s-txt" v-if="detail.gift.orderMaxQuantity">최대 주문 수량 : {{ detail.gift.orderMaxQuantity }}</span>
            </div>
          </div>

          <div class="info_row rev_donation_wrap">
            <div class="item_grade">
              <div class="grade">
                <div class="rating_star" v-if="detail.reviews.length">
                  <span v-for="i in 5" :key="i" :class="{ on: i <= Math.round(detail.averageScore) }"></span>
                </div>
                <p class="num" v-if="detail.reviews.length"><b>{{ detail.averageScore.toFixed(1) }}</b></p>
              </div>
              <div class="item_grade txt">
                <a href="javascript:void(0)" @click="showItemTab('nav-review')">
                  <span>후기 {{ detail.reviews.length }}개 보기</span>
                  <img src="/images/icon/btn_do-arrow.png" alt="" />
                </a>
              </div>
            </div>
            <button type="button" class="formBtn donation" @click="router.push({ path: '/donate', query: { locgovCode: detail.gift.locgovCode } })">
              <img src="/images/icon/cli-icon_btn-donation2.png" alt="" />
              <span>기부하기</span>
            </button>
          </div>

          <div class="line"></div>

          <div class="info_row etc">
            <div class="shop_info">
              <div class="title_col"><p>판매자</p></div>
              <div class="para_col"><p class="txt">{{ detail.seller?.companyName ?? '미등록 판매자' }}</p></div>
            </div>
            <div class="shop_info">
              <div class="title_col"><p>배송</p></div>
              <div class="para_col"><p class="txt">배송방법 : 택배</p><p class="txt">배송비 : 무료</p></div>
            </div>
            <div class="shop_info" v-if="detail.gift.minDonationAmount">
              <div class="title_col"><p>수령조건</p></div>
              <div class="para_col"><p class="txt">해당 지자체 {{ formatN(detail.gift.minDonationAmount) }}원 이상 기부 시 수령 가능</p></div>
            </div>
          </div>

          <div class="info_row warning" v-if="detail.gift.soldOut === '1'">
            <img class="icon-img" src="/images/icon/donation-warning.png" alt="주의" />
            <span class="s-txt">해당 답례품은 판매 종료 되었습니다.</span>
            <button v-if="!detail.restockRequested" type="button" class="formBtn" style="margin-left: 10px; padding: 2px 12px; font-size: 13px" @click="requestRestock">재입고 알림</button>
            <span v-else class="s-txt" style="margin-left: 10px; color: #1b6b3a">재입고 알림 신청됨</span>
          </div>
          <div class="info_row warning" v-if="detail.gift.soldOut !== '1' && detail.gift.dataStatusCode !== 'APPROVED'">
            <img class="icon-img" src="/images/icon/donation-warning.png" alt="주의" />
            <span class="s-txt">현재 판매중지 상태인 답례품입니다.</span>
          </div>

          <div class="buyBtn_box" v-if="detail.gift.dataStatusCode === 'APPROVED' && detail.gift.soldOut !== '1'">
            <div class="btn_wrap">
              <button class="buyBtn addToCart" type="button" @click="addToCart">장바구니</button>
              <button class="buyBtn buyOrder" type="button" @click="buyNow">바로선택</button>
              <button
                type="button"
                class="wishBtn addToWishList hidden_txt"
                :class="{ on: detail.wishlisted }"
                :aria-label="detail.wishlisted ? '관심 답례품 등록 상태, 해제하기' : '관심 답례품 해제 상태, 등록하기'"
                @click="toggleWishlist"
              >
                관심답례품
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section>
      <div class="item_tab" id="item_tab_wrap">
        <ul class="nav nav-tabs nav-justified center">
          <li class="nav-item"><a href="#nav-detail" class="nav-link" :class="{ active: activeTab === 'nav-detail' }" @click.prevent="showItemTab('nav-detail')"><span class="txt">답례품정보</span></a></li>
          <li class="nav-item"><a href="#nav-review" class="nav-link" :class="{ active: activeTab === 'nav-review' }" @click.prevent="showItemTab('nav-review')"><span class="txt">답례품후기<span class="pointblue"> {{ detail.reviews.length }}</span></span></a></li>
          <li class="nav-item"><a href="#nav-qna" class="nav-link" :class="{ active: activeTab === 'nav-qna' }" @click.prevent="showItemTab('nav-qna')"><span class="txt">답례품Q&amp;A<span class="pointblue"> {{ detail.inquiries.length }}</span></span></a></li>
          <li class="nav-item"><a href="#nav-buyer" class="nav-link" :class="{ active: activeTab === 'nav-buyer' }" @click.prevent="showItemTab('nav-buyer')"><span class="txt">배송/반품/교환</span></a></li>
          <li class="nav-item"><a href="#nav-notice" class="nav-link" :class="{ active: activeTab === 'nav-notice' }" @click.prevent="showItemTab('nav-notice')"><span class="txt">상품고시</span></a></li>
        </ul>
      </div>
      <div class="center tab_container">
        <div class="tab-content item_view">
          <div id="nav-detail" class="tab-pane" :class="{ 'show active': activeTab === 'nav-detail' }">
            <h3 class="sr-only">답례품정보</h3>
            <div class="item_detail" v-html="detail.gift.detailContent"></div>
          </div>

          <div id="nav-review" class="tab-pane" :class="{ 'show active': activeTab === 'nav-review' }">
            <div class="item_review">
              <div class="total_top"><h3 class="total">답례품후기 <span class="pointblue">{{ detail.reviews.length }}</span></h3></div>
              <div class="list_wrap review_list">
                <ul>
                  <li class="list_area" v-for="r in detail.reviews" :key="r.itemReviewId">
                    <div class="list_top">
                      <div class="m-field">
                        <div class="review_grade">
                          <div class="rating_star"><span v-for="i in 5" :key="i" :class="{ on: i <= r.score }"></span></div>
                        </div>
                        <div class="review_id"><p>{{ r.userName }}</p></div>
                        <div class="review_date"><p>{{ r.createdDate }}</p></div>
                      </div>
                    </div>
                    <div class="list_header">
                      <div class="con_wrap">
                        <div class="txt_area">
                          <p class="title">{{ r.subject }}</p>
                          <p>{{ r.content }}</p>
                        </div>
                        <div class="img_area" v-if="r.imageUrls.length">
                          <img v-for="src in r.imageUrls" :key="src" :src="api.assetUrl('gift', src)" style="width: 80px; height: 80px; object-fit: cover; margin-right: 6px" />
                        </div>
                      </div>
                      <div style="margin-top:6px; display:flex; gap:6px;">
                        <button type="button" class="formBtn" style="padding:2px 10px; font-size:12px;" :disabled="r.likedByMe" @click="likeReview(r)">
                          좋아요 {{ r.likeCount ?? 0 }}
                        </button>
                        <button type="button" class="formBtn" style="padding:2px 10px; font-size:12px;" @click="reportReview(r)">
                          {{ r.reportedByMe ? '신고됨' : '신고' }}
                        </button>
                      </div>
                    </div>
                  </li>
                </ul>
                <p class="empty" v-if="!detail.reviews.length">등록된 리뷰가 없습니다.</p>
              </div>

              <form id="review-write" class="board_write" style="margin-top: 20px" @submit.prevent="submitReview">
                <table class="board_write_table">
                  <colgroup><col style="width: 150px" /><col /></colgroup>
                  <tbody>
                    <tr>
                      <td class="label">닉네임 / 주문코드(선택)</td>
                      <td>
                        <input type="text" v-model="reviewForm.userName" placeholder="닉네임(선택)" style="width: 200px" />
                        <input type="text" v-model="reviewForm.orderCode" placeholder="주문코드(선택)" style="width: 200px" />
                      </td>
                    </tr>
                    <tr>
                      <td class="label"><label for="subject">제목</label></td>
                      <td><input type="text" id="subject" v-model="reviewForm.subject" required style="width: 100%" /></td>
                    </tr>
                    <tr>
                      <td class="label"><label for="content">내용</label></td>
                      <td><textarea id="content" v-model="reviewForm.content" rows="3" required style="width: 100%"></textarea></td>
                    </tr>
                    <tr>
                      <td class="label"><label for="score">평점 (1~5)</label></td>
                      <td><input type="number" id="score" v-model.number="reviewForm.score" min="1" max="5" required style="width: 80px" /></td>
                    </tr>
                    <tr>
                      <td class="label"><label for="reviewImages">사진 첨부(선택)</label></td>
                      <td><input type="file" id="reviewImages" accept="image/*" multiple @change="reviewForm.images = $event.target.files" /></td>
                    </tr>
                  </tbody>
                </table>
                <button type="submit" class="formBtn" style="margin-top: 12px">리뷰 등록</button>
              </form>
            </div>
          </div>

          <div id="nav-qna" class="tab-pane" :class="{ 'show active': activeTab === 'nav-qna' }">
            <div class="item_review">
              <div class="total_top"><h3 class="total">답례품Q&amp;A <span class="pointblue">{{ detail.inquiries.length }}</span></h3></div>
              <div class="list_wrap review_list">
                <ul>
                  <li class="list_area" v-for="q in detail.inquiries" :key="q.inquiryId">
                    <div class="list_top">
                      <div class="m-field">
                        <span class="status-tag" :class="{ answered: q.status === 'ANSWERED' }">{{ q.statusLabel }}</span>
                      </div>
                    </div>
                    <div class="list_header">
                      <div class="con_wrap">
                        <div class="txt_area">
                          <p v-if="q.secretYn === 'Y'" style="color: #999">비밀글입니다.</p>
                          <p v-else>{{ q.question }}</p>
                          <p v-if="q.answer" style="color: #00215a; margin-top: 8px">ㄴ {{ q.answer }}</p>
                        </div>
                      </div>
                      <button type="button" class="formBtn" style="margin-top:6px; padding:2px 10px; font-size:12px;" @click="reportInquiry(q)">
                        {{ q.reportedByMe ? '신고됨' : '신고' }}
                      </button>
                    </div>
                  </li>
                </ul>
                <p class="empty" v-if="!detail.inquiries.length">등록된 문의가 없습니다.</p>
              </div>

              <form class="board_write" style="margin-top: 20px" @submit.prevent="submitInquiry">
                <table class="board_write_table">
                  <colgroup><col style="width: 150px" /><col /></colgroup>
                  <tbody>
                    <tr>
                      <td class="label"><label for="question">문의 내용</label></td>
                      <td><textarea id="question" v-model="inquiryForm.question" rows="3" required style="width: 100%"></textarea></td>
                    </tr>
                    <tr>
                      <td class="label">비밀글</td>
                      <td><input type="checkbox" id="secret" v-model="inquiryForm.secret" /> <label for="secret" style="margin: 0">비밀글로 문의</label></td>
                    </tr>
                  </tbody>
                </table>
                <button type="submit" class="formBtn" style="margin-top: 12px">문의 등록</button>
              </form>
            </div>
          </div>

          <div id="nav-buyer" class="tab-pane" :class="{ 'show active': activeTab === 'nav-buyer' }">
            <h3 class="sr-only">배송/반품/교환</h3>
            <div class="item_detail">
              <p>배송방법 : 택배 (배송비 무료)</p>
              <p>교환/반품 : 답례품 특성상 단순 변심에 의한 교환/반품이 제한될 수 있습니다. 상품 문의를 통해 판매자에게 문의해 주세요.</p>
            </div>
          </div>

          <div id="nav-notice" class="tab-pane" :class="{ 'show active': activeTab === 'nav-notice' }">
            <h3 class="sr-only">상품고시</h3>
            <table class="board_write_table">
              <colgroup><col style="width: 150px" /><col /></colgroup>
              <tbody>
                <tr><td class="label">품목</td><td>{{ detail.categoryLabel ?? detail.gift.categoryCode }}</td></tr>
                <tr><td class="label">지자체</td><td>{{ detail.locgovName ?? detail.gift.locgovCode }}</td></tr>
                <tr><td class="label">판매자</td><td>{{ detail.seller?.companyName ?? '미등록 판매자' }}</td></tr>
                <tr><td class="label">소비자상담 관련 전화번호</td><td>{{ detail.seller?.telephoneNumber ?? '판매자에게 문의' }}</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </template>
</template>

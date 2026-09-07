<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

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
}

async function addToCart() {
  if (!auth.loggedIn) return requireLogin()
  try {
    await api.post('order', '/api/cart/items', { itemId: Number(props.itemId), quantity: 1 })
    if (confirm('장바구니에 담았습니다. 장바구니로 이동할까요?')) router.push('/cart')
  } catch (e) {
    alert(e.message)
  }
}

async function buyNow() {
  if (!auth.loggedIn) return requireLogin()
  try {
    await api.post('order', '/api/cart/items', { itemId: Number(props.itemId), quantity: 1 })
    const cart = await api.get('order', '/api/cart')
    const line = cart.flatMap((g) => g.lines).find((l) => l.itemId === Number(props.itemId))
    if (!line) throw new Error('장바구니 담기에 실패했습니다.')
    router.push({ path: '/checkout', query: { cartItemId: [line.cartItemId] } })
  } catch (e) {
    alert(e.message)
  }
}

async function submitReview() {
  if (!auth.loggedIn) return requireLogin()
  const f = reviewForm.value
  if (!f.subject.trim() || !f.content.trim()) {
    alert('제목과 내용을 입력해 주세요.')
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
    alert('문의 내용을 입력해 주세요.')
    return
  }
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/inquiries`, { question: f.question, secret: f.secret })
    inquiryForm.value = { question: '', secret: false }
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function reportReview(review) {
  if (!auth.loggedIn) return requireLogin()
  if (review.reportedByMe) { alert('이미 신고한 리뷰입니다.'); return }
  if (!confirm('이 리뷰를 신고하시겠습니까?')) return
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/reviews/${review.itemReviewId}/report`, {})
    await load()
  } catch (e) {
    alert(e.message)
  }
}

async function reportInquiry(inquiry) {
  if (!auth.loggedIn) return requireLogin()
  if (inquiry.reportedByMe) { alert('이미 신고한 문의입니다.'); return }
  if (!confirm('이 문의를 신고하시겠습니까?')) return
  try {
    await api.post('gift', `/api/gifts/${props.itemId}/inquiries/${inquiry.inquiryId}/report`, {})
    await load()
  } catch (e) {
    alert(e.message)
  }
}

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
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

          <!-- SFR-005 "카탈로그 관리: 옵션" - 카탈로그 정보 표시만(장바구니/주문에는 아직
               반영 안 됨, admin에서 등록/관리는 완전히 동작). -->
          <div class="info_row" v-if="detail.options.length">
            <div class="s-txt" style="width: 100%">
              <p style="font-weight: bold; margin-bottom: 4px">선택 가능 옵션</p>
              <ul>
                <li v-for="o in detail.options" :key="o.itemOptionId">
                  {{ o.optionName }}
                  <span v-if="o.optionPrice">(+{{ formatN(o.optionPrice) }}P)</span>
                  <span v-if="o.soldOut" style="color: #c00">품절</span>
                </li>
              </ul>
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
          </div>
          <div class="info_row warning" v-if="detail.gift.soldOut !== '1' && detail.gift.dataStatusCode !== 'APPROVED'">
            <img class="icon-img" src="/images/icon/donation-warning.png" alt="주의" />
            <span class="s-txt">현재 판매중지 상태인 답례품입니다.</span>
          </div>

          <div class="buyBtn_box" v-if="detail.gift.dataStatusCode === 'APPROVED' && detail.gift.soldOut !== '1'">
            <div class="btn_wrap">
              <button class="buyBtn addToCart" type="button" @click="addToCart">장바구니</button>
              <button class="buyBtn buyOrder" type="button" @click="buyNow">포인트로 주문하기</button>
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
          <div id="nav-detail" class="tab-pane show active">
            <h3 class="sr-only">답례품정보</h3>
            <div class="item_detail" v-html="detail.gift.detailContent"></div>
          </div>

          <div id="nav-review" class="tab-pane">
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
                      <button type="button" class="formBtn" style="margin-top:6px; padding:2px 10px; font-size:12px;" @click="reportReview(r)">
                        {{ r.reportedByMe ? '신고됨' : '신고' }}
                      </button>
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

          <div id="nav-qna" class="tab-pane">
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

          <div id="nav-buyer" class="tab-pane">
            <h3 class="sr-only">배송/반품/교환</h3>
            <div class="item_detail">
              <p>배송방법 : 택배 (배송비 무료)</p>
              <p>교환/반품 : 답례품 특성상 단순 변심에 의한 교환/반품이 제한될 수 있습니다. 상품 문의를 통해 판매자에게 문의해 주세요.</p>
            </div>
          </div>

          <div id="nav-notice" class="tab-pane">
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

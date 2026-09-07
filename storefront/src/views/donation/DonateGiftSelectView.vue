<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// SFR-003 "기부금 납부 시 답례품 선택 기능 추가" - 기부 결제완료(MyDonationsView의 완료 처리) 직후
// "답례품을 제공 받음"을 선택한 기부 건이면 여기로 이동한다. AS-IS/기존 TO-BE는 기부→포인트적립까지만
// 하고 답례품은 별도로 답례품몰을 다시 찾아가야 했는데, 이 화면이 그 사이를 이어준다 - 목록은 기존
// GiftListView와 같은 /api/gifts?locgovCode= API를 그대로 재사용하고, 담기는 기존 장바구니 API를
// 그대로 쓴다(새 결제 수단을 만들지 않음).
const route = useRoute()
const router = useRouter()

const locgovCode = route.query.locgovCode ?? ''

const loading = ref(true)
const gifts = ref([])
const locgovName = ref('')
const remainingPoints = ref(null)
const addedItemIds = ref(new Set())
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    const [giftData, pointData] = await Promise.all([
      api.get('gift', `/api/gifts?${new URLSearchParams({ locgovCode }).toString()}`),
      api.get('point', '/api/my/points'),
    ])
    gifts.value = giftData.gifts
    locgovName.value = giftData.gifts[0]?.locgovName ?? ''
    const summary = pointData.locgovSummary.find((s) => s.locgovCode === locgovCode)
    remainingPoints.value = summary ? summary.remaining : 0
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}
onMounted(load)

function imgUrl(gift) {
  return gift.thumbnailUrl ? api.assetUrl('gift', gift.thumbnailUrl) : '/images/thumb.png'
}

async function addToCart(gift) {
  errorMessage.value = ''
  try {
    await api.post('order', '/api/cart/items', { itemId: gift.itemId, quantity: 1 })
    addedItemIds.value = new Set([...addedItemIds.value, gift.itemId])
  } catch (e) {
    errorMessage.value = e.message
  }
}

function goCart() {
  router.push('/cart')
}

function skip() {
  router.push('/mypage/donations')
}

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="center donation_steps">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부하기
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        답례품 선택
      </span>
      <h2 class="page-title-txt">답례품 선택</h2>
    </div>
  </section>

  <div class="center" id="itemArea" v-if="!loading">
    <p class="result-box" style="margin-bottom: 20px">
      기부가 완료되었습니다. <strong>{{ locgovName }}</strong>에서 사용 가능한 포인트
      <strong class="pointblue">{{ formatN(remainingPoints) }} P</strong>로 답례품을 선택해 보세요.
    </p>
    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <div class="section all_goods" id="contents">
      <ul class="goods-list-group">
        <li class="goods-list-items" v-for="g in gifts" :key="g.itemId">
          <div class="list_inner">
            <span class="img_frame">
              <router-link class="img_cover_link" :to="`/gifts/${g.itemId}`">
                <img class="item_img" :src="imgUrl(g)" :alt="g.itemName" />
                <img v-if="g.soldOut" src="/images/goods/goods_sold-out.png" alt="품절" />
              </router-link>
            </span>
            <router-link class="card_title" :to="`/gifts/${g.itemId}`" :class="{ grayTxt_so: g.soldOut }">{{ g.itemName }}</router-link>
            <span class="deepBlue" :class="{ grayTxt_so: g.soldOut }">
              <span class="pointSize">{{ formatN(g.salePrice) }}</span> P
            </span>
            <button
              class="buyBtn addToCart"
              type="button"
              style="margin-top: 8px; width: 100%"
              :disabled="g.soldOut || addedItemIds.has(g.itemId)"
              @click="addToCart(g)"
            >
              {{ addedItemIds.has(g.itemId) ? '담기 완료' : '장바구니 담기' }}
            </button>
          </div>
        </li>
      </ul>
      <div class="common_none" v-if="gifts.length === 0">
        <p>{{ locgovName || '이 지자체' }}에 등록된 답례품이 없습니다.</p>
      </div>
    </div>

    <div class="center">
      <div class="btn-box many mb_b60">
        <button type="button" class="blueBtn u-confirm" @click="goCart" :disabled="addedItemIds.size === 0">
          장바구니에서 결제하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
        <button type="button" class="blueBtn cancellation" @click="skip">나중에 선택하기</button>
      </div>
    </div>
  </div>
</template>

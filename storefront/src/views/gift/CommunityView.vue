<script setup>
// AS-IS community-business/communityList-main.html(마을기업관) 재현. 헤더 + 지역(시도/시군구) 필터 +
// 상품 그리드(전체 N건). 상품은 /gift/api/gifts?mode=community-business&locgovCode=.
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { formatN } from '../../utils/format'
import RegionSelect from '../../components/RegionSelect.vue'

const route = useRoute()
const router = useRouter()

const gifts = ref([])
const regionCode = ref('')
const regionRef = ref(null)

async function load() {
  const params = new URLSearchParams({ mode: 'community-business' })
  if (regionCode.value) params.set('locgovCode', regionCode.value)
  try {
    const res = await api.get('gift', `/api/gifts?${params.toString()}`)
    gifts.value = res.gifts ?? []
  } catch {
    gifts.value = []
  }
}
onMounted(load)

function search() {
  load()
}
function reset() {
  regionCode.value = ''
  regionRef.value?.reset()
  load()
}
function onRegionChange(code) {
  regionCode.value = code
}

function imgUrl(gift) {
  return gift.thumbnailUrl ? api.assetUrl('gift', gift.thumbnailUrl) : '/images/thumb.png'
}
async function toggleWishlist(gift) {
  try {
    const res = await api.post('gift', `/wishlist/${gift.itemId}/toggle`)
    gift.wishlisted = res.wishlisted
  } catch {
    router.push({ path: '/login', query: { target: route.fullPath } })
  }
}
</script>

<template>
  <div>
    <section id="contents" class="communitybusiness page-title-box">
      <div class="center">
        <h2 class="page-title-txt">마을기업관</h2>
        <p class="deepGray">
          지역주민이 주도하여 지역자원을 활용하고,<br />
          지역문제 해결과 지역사회 공헌을 목적으로 운영되는 마을기업의 상품을 소개합니다.<br />
          <span class="pointblue">지역 일자리 창출과 지역경제 순환에 기여합니다.<br />지역과 함께 성장하는 가치소비에 동참해보세요.</span>
        </p>
        <div class="loc_select_area">
          <RegionSelect ref="regionRef" @change="onRegionChange" />
          <div class="submit_btns">
            <button type="button" class="formBtn" @click="search">검색</button>
            <button type="button" class="formBtn reset_img" @click="reset">
              <img src="/images/icon/loc_reset.png" alt="초기화" class="icon-img" />
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="center" id="itemArea">
      <div class="section all_goods">
        <div class="main_con_top">
          <div class="amount_goods">전체 {{ gifts.length }}건</div>
        </div>
        <ul class="goods-list-group">
          <li class="goods-list-items" v-for="g in gifts" :key="g.itemId">
            <div class="list_inner">
              <span class="img_frame">
                <router-link class="img_cover_link" :to="`/gifts/${g.itemId}`">
                  <img class="item_img" :src="imgUrl(g)" :alt="g.itemName" />
                  <img v-if="g.soldOut" src="/images/goods/goods_sold-out.png" alt="품절" />
                </router-link>
                <button
                  type="button"
                  class="icon-md-img hidden_txt"
                  :class="{ on: g.wishlisted }"
                  :aria-label="g.wishlisted ? '관심 답례품 등록 상태, 해제하기' : '관심 답례품 해제 상태, 등록하기'"
                  @click="toggleWishlist(g)"
                >
                  관심답례품
                </button>
                <span class="tag_box" v-if="g.isNew">신규</span>
              </span>
              <button type="button" class="do_btn" @click="router.push({ path: '/donate', query: { locgovCode: g.locgovCode } })">
                <span class="btn_icon"></span>
                <span class="btn_txt">{{ g.locgovName }} 기부</span>
                <span class="btn_arrow"></span>
              </button>
              <router-link class="card_title" :to="`/gifts/${g.itemId}`" :class="{ grayTxt_so: g.soldOut }">{{ g.itemName }}</router-link>
              <span class="deepBlue" :class="{ grayTxt_so: g.soldOut }">
                <span class="pointSize">{{ formatN(g.salePrice) }}</span> P
              </span>
            </div>
          </li>
        </ul>
        <div class="common_none" v-if="gifts.length === 0">
          <p>등록된 답례품이 없습니다.</p>
        </div>
      </div>
    </section>
  </div>
</template>

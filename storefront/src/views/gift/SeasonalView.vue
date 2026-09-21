<script setup>
// AS-IS event/seasonList-main.html(제철식품관) 재현. 2단계: (1) 월별 제철 키워드 카드(season_group) →
// (2) 월 클릭 시 그 달의 제철 답례품 목록(goods-list-group). 키워드는 /gift/api/season-food,
// 월별 상품은 /gift/api/gifts?mode=seasonal&month=N.
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { formatN } from '../../utils/format'
import RegionSelect from '../../components/RegionSelect.vue'

const route = useRoute()
const router = useRouter()

const months = ref([]) // [{ month, keyword }]
const isShowDetail = ref(false)
const selectedMonth = ref(null)
const gifts = ref([])

// 필터: 월 선택 + 지역(시도/시군구)
const monthFilter = ref('')
const regionCode = ref('')
const regionRef = ref(null)

async function loadMonths() {
  try {
    months.value = await api.get('gift', '/api/season-food')
  } catch {
    months.value = []
  }
}

async function loadGifts(month) {
  selectedMonth.value = month
  isShowDetail.value = true
  const params = new URLSearchParams({ mode: 'seasonal', month: String(month) })
  if (regionCode.value) params.set('locgovCode', regionCode.value)
  try {
    const res = await api.get('gift', `/api/gifts?${params.toString()}`)
    gifts.value = res.gifts ?? []
  } catch {
    gifts.value = []
  }
}

// 월별 키워드 카드 클릭
function monthClick(month) {
  monthFilter.value = String(month)
  loadGifts(month)
}

// 검색 버튼: 선택한 월(없으면 현재 월) + 지역으로 조회
function search() {
  const m = monthFilter.value ? Number(monthFilter.value) : new Date().getMonth() + 1
  loadGifts(m)
}

// 초기화: 필터를 비우고 월별 카드 화면으로 돌아간다
function reset() {
  monthFilter.value = ''
  regionCode.value = ''
  regionRef.value?.reset()
  isShowDetail.value = false
  selectedMonth.value = null
  gifts.value = []
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

onMounted(loadMonths)
</script>

<template>
  <div id="contents">
    <section class="season page-title-box">
      <div class="center">
        <h2 class="page-title-txt">제철 식품관</h2>
        <p class="deepGray">
          식품의 영양이 최고일때는 제철에 나온 음식이 단연 으뜸입니다.<br />
          <span class="pointblue">고향사랑e음에서는 243개 지자체에서 제공하는 제철 식품코너를 운영하고 있습니다.<br />월별 다양한 제철식품을 만나보시기 바랍니다.</span>
        </p>
        <div class="loc_select_area">
          <select title="월 선택" v-model="monthFilter">
            <option value="">월 선택</option>
            <option v-for="m in 12" :key="m" :value="String(m)">{{ m }}월</option>
          </select>
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

    <!-- (1) 월별 제철 키워드 카드 -->
    <section class="contents seasonCard" v-if="!isShowDetail">
      <div class="center">
        <ul class="season_group">
          <li
            class="season_list cursor"
            tabindex="0"
            v-for="v in months"
            :key="v.month"
            @click="monthClick(v.month)"
            @keydown.enter="monthClick(v.month)"
          >
            <span>
              <a href="javascript:void(0)" role="button">
                <div class="card_tit"><span>{{ v.month }}월</span></div>
                <div class="card_list_items">{{ v.keyword || '준비중' }}</div>
              </a>
            </span>
          </li>
        </ul>
      </div>
    </section>

    <!-- (2) 선택한 월의 제철 답례품 -->
    <section class="center" v-else id="itemArea">
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
          <p>해당 월에 등록된 제철 답례품이 없습니다.</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS goods/index-main.html 재현(gift 서비스 list.html과 동일 출처). GNB "답례품몰"(전체/카테고리/검색/
// 지자체몰) · "제철식품관" · "마을기업관" 3개 메뉴가 전부 이 화면 하나(mode 파라미터)로 들어온다.
// "지자체몰 선택하기" 지도 팝업(gift/fragments/mall-header.html의 별도 GNB 위젯)은 order 라운드가
// order 서비스 자체 헤더 프래그먼트를 재현하지 않은 것과 같은 경계로 이번 라운드 범위 밖 - locgovCode
// 쿼리 파라미터 자체는 API가 이미 지원한다.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    data.value = await api.get('gift', `/api/gifts?${new URLSearchParams(cleanParams()).toString()}`)
  } finally {
    loading.value = false
  }
}

function cleanParams() {
  const params = {}
  if (route.query.categoryCode) params.categoryCode = route.query.categoryCode
  if (route.query.q) params.q = route.query.q
  if (route.query.locgovCode) params.locgovCode = route.query.locgovCode
  if (route.name === 'gift-seasonal') params.mode = 'seasonal'
  if (route.name === 'gift-community-business') params.mode = 'community-business'
  return params
}

onMounted(load)
watch(() => [route.query, route.name], load)

const isBrowseMode = computed(() => data.value && data.value.pageTitle == null)

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

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="page-title-box mall center" v-if="data">
    <span class="ali_breadcrumb">
      <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
      <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
      <router-link to="/gifts">답례품</router-link>
      <template v-if="data.pageTitle || (data.selectedCategory && !data.q)">
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <span v-if="data.pageTitle">{{ data.pageTitle }}</span>
        <span v-else>{{ data.categories[data.selectedCategory] }}</span>
      </template>
    </span>
    <h2 class="page-title-txt">
      {{ data.pageTitle ?? (data.selectedCategory ? data.categories[data.selectedCategory] : '답례품몰') }}
    </h2>
  </section>

  <div class="center" id="itemArea" v-if="data">
    <div class="section condi_section" v-if="isBrowseMode">
      <div class="category_group">
        <ul class="condition_list">
          <li>
            <router-link to="/gifts" :class="{ on: !data.selectedCategory && !data.q }">
              <span class="icon_txt">전체</span>
            </router-link>
          </li>
          <li v-for="(label, code) in data.categories" :key="code">
            <router-link :to="{ path: '/gifts', query: { categoryCode: code } }" :class="{ on: data.selectedCategory === code }">
              <span class="icon_img"><img class="de-img" :src="`/images/goods/${data.categoryIcons[code]}`" alt="" /></span>
              <span class="icon_txt">{{ label }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </div>

    <p style="font-size: 13px; color: #777; margin: 8px 0 16px" v-if="data.q">
      '<span>{{ data.q }}</span>' 검색 결과 <span>{{ data.gifts.length }}</span>건
    </p>

    <div class="section all_goods" id="contents">
      <ul class="goods-list-group">
        <li class="goods-list-items" v-for="g in data.gifts" :key="g.itemId">
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
      <div class="common_none" v-if="data.gifts.length === 0">
        <p>등록된 답례품이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

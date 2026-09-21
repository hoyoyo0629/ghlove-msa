<script setup>
import { modalAlert, modalConfirm } from '../composables/useModal'
// AS-IS components/layouts/header_g.vue(답례품몰/장바구니/이벤트 페이지 전용 쇼핑 GNB 바)를 재현한다.
// AS-IS는 shopping 페이지에서 header_ali(메인 헤더) 아래에 이 바를 함께 마운트한다.
// 구성: [전체 카테고리 메가메뉴] · 제철식품관/마을기업관 링크 · 답례품 검색 · 지자체몰 선택.
// 스타일은 전역 base(default_ali/layout/new.css)와 item.css가 담당(#header_g.header2/.main_lnb/.top_category).
//
// Stage 1(현재): 카테고리 메가메뉴 + 제철/마을기업 + 검색까지 동작. 지자체몰 지도선택 팝업은 Stage 2.
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/http'
import MapSelectPopup from './MapSelectPopup.vue'

const router = useRouter()

// AS-IS goodsMenu1: 제철식품관/마을기업관 (지역이벤트는 AS-IS도 주석처리라 제외)
const topMenus = [
  { name: '제철식품관', to: '/gifts/seasonal' },
  { name: '마을기업관', to: '/gifts/community-business' },
]

const groups = ref([]) // [{ code, label, categories: [{ name, items: [str] }] }]
const isOpen = ref(false) // 전체 카테고리 메가메뉴 열림
const activeGroupIndex = ref(0) // 현재 hover된 대분류
const searchKeyword = ref('')

async function loadCategories() {
  try {
    groups.value = await api.get('gift', '/api/categories')
  } catch {
    groups.value = [] // 조회 실패해도 바가 깨지지 않도록 빈 상태
  }
}
onMounted(loadCategories)

function toggleAllMenu() {
  if (groups.value.length === 0) return
  isOpen.value = !isOpen.value
  if (isOpen.value) activeGroupIndex.value = 0
}
function closeAllMenu() {
  isOpen.value = false
}
function overGroup(i) {
  activeGroupIndex.value = i
}

// AS-IS categoryGroupLink/categoryLink: 대분류는 categoryCode 필터로, 세부 품목명은 검색어로 이동한다
// (MSA 답례품 목록은 categoryCode·q 필터를 지원하며 서브카테고리 단위 필터는 없음).
function goGroup(group) {
  closeAllMenu()
  router.push({ path: '/gifts', query: { categoryCode: group.code } })
}
function goKeyword(name) {
  closeAllMenu()
  router.push({ path: '/gifts', query: { q: name } })
}

function search() {
  const kw = searchKeyword.value.trim()
  if (!kw) {
    modalAlert('검색할 단어를 입력해주세요.')
    return
  }
  router.push({ path: '/gifts', query: { q: kw } })
}
function keydown(e) {
  if (e.key === 'Enter') search()
}

// 지자체몰 선택: AS-IS layout-map-select(지도 팝업) 열기 → 시군구 선택 시 /gifts?locgovCode= 로 필터.
const mapOpen = ref(false)
const locLabel = ref('지자체몰 선택하기')
function openMap() {
  mapOpen.value = true
}
function onLocgovSelect(sel) {
  mapOpen.value = false
  locLabel.value = sel.code ? `${sel.upperNm} ${sel.nm}`.trim() : '지자체몰 선택하기'
  const query = sel.code ? { locgovCode: sel.code } : {}
  router.push({ path: '/gifts', query })
}
</script>

<template>
  <header id="header_g" class="header2">
    <nav class="gnb">
      <div class="center">
        <div class="gnb_slider_m">
          <button type="button" class="total_menu" @click="toggleAllMenu">
            <span class="total_menu_h">
              <img :src="isOpen ? '/images/goods/all-menu_close.png' : '/images/goods/all-menu_open.png'" alt="전체메뉴열기" />
            </span>
            <span>전체 카테고리</span>
          </button>
          <span class="v-line"></span>
          <ul class="top_menu">
            <li v-for="(m, i) in topMenus" :key="i">
              <router-link :to="m.to">{{ m.name }}</router-link>
            </li>
          </ul>
          <span class="goods_search">
            <input type="text" placeholder="답례품 전체 검색" title="답례품 전체 검색" v-model="searchKeyword" @keydown="keydown" />
            <button type="button" @click.prevent="search">
              <img class="icon-img" src="/images/icon/search-white.png" alt="검색하기" />
            </button>
          </span>
          <span class="local_shop">
            <button type="button" class="shop_select" @click="openMap">
              <span class="local_shop_wrap">
                <img class="icon-img" src="/images/goods/local-loc.png" alt="" />
                <span class="locNameArea">{{ locLabel }}</span>
              </span>
              <img src="/images/goods/cli-icon_local-arrow.png" alt="지자체 선택 팝업 열기" />
            </button>
          </span>
        </div>
      </div>
    </nav>

    <!-- 전체 카테고리 메가메뉴. layout.css의 .main_lnb{display:none}을 v-show로는 못 이기므로
         (AS-IS는 jQuery slideDown으로 강제) 인라인 display로 직접 연다. -->
    <nav class="main_lnb" id="allMenu" :style="{ display: isOpen ? 'block' : 'none' }" @mouseleave="closeAllMenu">
      <div class="center">
        <ul class="top_category">
          <li v-for="(group, i) in groups" :key="group.code">
            <button
              type="button"
              :id="'cate' + i"
              :class="{ on: i === activeGroupIndex }"
              @mouseover.stop="overGroup(i)"
              @focus="overGroup(i)"
              @click.prevent="goGroup(group)"
            >
              {{ group.name }}
              <span><img src="/images/icon/cli-icon_btn-illust-arrow1.png" alt="" /></span>
            </button>
          </li>
        </ul>
        <div class="detail_cate" :style="{ display: groups.length ? 'flex' : 'none' }">
          <ul class="middle_category" v-for="(sub, j) in (groups[activeGroupIndex]?.categories || [])" :key="j">
            <li>
              <button type="button" @click.prevent="goKeyword(sub.name)">{{ sub.name }}</button>
              <ul class="detail_category">
                <li v-for="(item, k) in sub.children" :key="k">
                  <button type="button" @click.prevent="goKeyword(item)">{{ item }}</button>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </header>

  <MapSelectPopup v-if="mapOpen" @select="onLocgovSelect" @close="mapOpen = false" />
</template>

<style scoped>
/* AS-IS header_g.vue의 검색 input 스코프 스타일 - 파란 바 위라 배경 투명 + 흰 글자 */
.goods_search input[type='text'] {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 14px;
  outline: 1px;
}
.goods_search input[type='text']::placeholder {
  color: #fff;
}
</style>

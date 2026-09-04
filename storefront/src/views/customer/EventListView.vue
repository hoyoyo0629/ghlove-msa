<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS featured/eventList.html 재현(event.css/evt_card.css). 시도/시군구 캐스케이드는
// /api/events 응답에 이미 선택된 upperLocgovCode 기준 cities가 같이 내려온다.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)
const province = ref('')
const city = ref('')

const ing = computed(() => route.query.ing ?? 'Y')
const upperLocgovCode = computed(() => route.query.upperLocgovCode ?? '')
const locgovCode = computed(() => route.query.locgovCode ?? '')
const page = computed(() => Number(route.query.page ?? '1'))

async function load() {
  loading.value = true
  try {
    const params = { ing: ing.value, page: String(page.value) }
    if (upperLocgovCode.value) params.upperLocgovCode = upperLocgovCode.value
    if (locgovCode.value) params.locgovCode = locgovCode.value
    data.value = await api.get('admin', `/api/events?${new URLSearchParams(params).toString()}`)
    province.value = upperLocgovCode.value
    city.value = locgovCode.value
  } finally {
    loading.value = false
  }
}
onMounted(load)
watch(() => route.query, load)

async function onProvinceChange() {
  city.value = ''
  if (!province.value) return
  const cities = await api.get('admin', `/events/cities?upperLocgovCode=${encodeURIComponent(province.value)}`)
  if (data.value) data.value.cities = cities
}
function submitSearch() {
  router.push({ path: '/events', query: { ing: ing.value, upperLocgovCode: province.value, locgovCode: city.value, page: '1' } })
}
function resetFilter() {
  router.push({ path: '/events', query: { ing: ing.value } })
}
function setIng(value) {
  router.push({ path: '/events', query: { ing: value, upperLocgovCode: upperLocgovCode.value, locgovCode: locgovCode.value } })
}
function goPage(p) {
  router.push({ path: '/events', query: { ...route.query, page: String(p) } })
}
function eventImg(e) {
  return e.listImage ? `/images/events/${e.listImage}` : ''
}
</script>

<template>
  <section class="loc_evt page-title-box">
    <div class="page-title-box" style="margin-bottom: 0">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기부하기
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        지역 이벤트
      </span>
      <h2 class="page-title-txt" style="padding-top: 15px">지역 이벤트</h2>
    </div>

    <div class="center" style="padding-top: 15px" v-if="data">
      <p>지자체별 이벤트진행 현황을 확인하실 수 있습니다.</p>
      <form id="filterForm" @submit.prevent="submitSearch">
        <div class="loc_select_area">
          <label for="upperLocgovCode" class="sr-only">시·도 선택</label>
          <select title="시·도 선택" id="upperLocgovCode" v-model="province" @change="onProvinceChange">
            <option value="">시·도 선택</option>
            <option v-for="[code, label] in Object.entries(data.provinces)" :key="code" :value="code">{{ label }}</option>
          </select>
          <label for="locgovCode" class="sr-only">시·군·구 선택</label>
          <select title="시·군·구 선택" id="locgovCode" v-model="city">
            <option value="">시·군·구 선택</option>
            <option v-for="[code, label] in Object.entries(data.cities)" :key="code" :value="code">{{ label }}</option>
          </select>
          <div class="submit_btns">
            <button type="submit" class="formBtn" id="eventSearchBtn">검색</button>
            <button type="button" class="formBtn reset_img" @click="resetFilter">
              <img src="/images/icon/loc_reset.png" alt="초기화" class="icon-img" />
            </button>
          </div>
        </div>
      </form>

      <div class="btn-box many mid" style="margin: 40px 0 0 0">
        <button type="button" class="blueBtn" :class="ing === 'Y' ? 'u-confirm' : 'cancellation'" @click="setIng('Y')">
          진행중인 이벤트<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          <span v-if="ing === 'Y'" class="sr-only">선택됨</span>
        </button>
        <button type="button" class="blueBtn" :class="ing === 'N' ? 'u-confirm' : 'cancellation'" @click="setIng('N')">
          종료된 이벤트<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          <span v-if="ing === 'N'" class="sr-only">선택됨</span>
        </button>
      </div>
    </div>
  </section>

  <section class="contents new_contents" v-if="!loading && data">
    <h3 class="sr-only">{{ ing === 'Y' ? '진행중인 이벤트' : '종료된 이벤트' }}</h3>
    <div class="container">
      <div class="section all_evt">
        <div class="main_con_top" v-if="data.events.length">
          <div class="amount_goods">전체 {{ data.totalCount }}건</div>
        </div>

        <ul class="goods-evt-group" v-if="data.events.length">
          <li class="evt-list-items" v-for="e in data.events" :key="e.eventId">
            <div class="list_inner">
              <span class="img_frame">
                <router-link :to="`/events/${e.eventId}`" class="img_cover_link" :title="`${e.title} 이벤트 상세정보 페이지 이동`">
                  <img class="item_img" v-if="e.listImage" :src="eventImg(e)" alt="" />
                  <img src="/images/new/ico_end.svg" alt="종료된 이벤트" v-if="ing === 'N'" />
                </router-link>
              </span>
              <span class="cardTxt card_evt-du">{{ e.displayDate }}</span>
              <router-link :to="`/events/${e.eventId}`"><span class="cardTxt card_title">{{ e.title }}</span></router-link>
              <span class="cardTxt pointGray">{{ e.hostName }}</span>
              <span class="cardTxt pointGray" v-if="e.phone">{{ e.phone }}</span>
            </div>
          </li>
        </ul>
        <div class="list-none" v-else>
          <img src="/images/icon/cli-icon_no-location.png" alt="이벤트가 없습니다" />
        </div>

        <div class="card-pagination" v-if="data.totalPages > 1">
          <div class="pagination_ali">
            <ul class="pagination-frame">
              <li><a class="fist arrow_btn" href="javascript:void(0)" @click="goPage(1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
              <li><a class="prev arrow_btn" href="javascript:void(0)" @click="goPage(Math.max(1, page - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
              <li v-for="p in data.totalPages" :key="p" :class="{ 'selected-page': p === page }">
                <a class="page-text" href="javascript:void(0)" @click="goPage(p)">{{ p }}</a>
              </li>
              <li><a class="next arrow_btn" href="javascript:void(0)" @click="goPage(Math.min(data.totalPages, page + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
              <li><a class="last arrow_btn" href="javascript:void(0)" @click="goPage(data.totalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

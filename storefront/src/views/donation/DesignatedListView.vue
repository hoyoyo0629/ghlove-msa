<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS designated-donation/index-main.html 재현(donation 서비스 designated-list.html과 동일
// 출처, GNB "기부 > 특정사업에 기부하기"). 상태(진행중/종료)·정렬(최근등록순/참여금액순/모금율순/
// 종료임박순)·사업구분 필터와 페이지네이션을 그대로 재현한다. "지자체별 검색" 지도 팝업
// (fragments/dsg-search-header.html)은 gift 답례품몰 라운드의 "지자체몰 선택하기"·order 장바구니
// 라운드가 각각 자기 헤더의 같은 위젯을 미포팅으로 남긴 것과 동일한 경계로 이번 라운드도 범위
// 밖(locgovCode 쿼리 파라미터 자체는 API가 이미 지원) - 대신 키워드 검색(q)은 단순 입력창이라 포함.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)
const sortOpen = ref(false)
const bsnsOpen = ref(false)
const qInput = ref('')

const status = computed(() => route.query.status ?? 'OPEN')
const bsnsType = computed(() => route.query.bsnsType ?? '')
const sort = computed(() => route.query.sort ?? 'RATE')
const page = computed(() => Number(route.query.page ?? '1'))

async function load() {
  loading.value = true
  qInput.value = route.query.q ?? ''
  try {
    const params = {
      status: status.value,
      sort: sort.value,
      page: String(page.value),
    }
    if (bsnsType.value) params.bsnsType = bsnsType.value
    if (route.query.q) params.q = route.query.q
    if (route.query.locgovCode) params.locgovCode = route.query.locgovCode
    data.value = await api.get('donation', `/api/designated-donation/projects?${new URLSearchParams(params).toString()}`)
  } finally {
    loading.value = false
  }
}
onMounted(load)
watch(() => route.query, load)

const sortLabel = computed(() => {
  if (sort.value === 'AMOUNT') return '참여금액순'
  if (sort.value === 'RATE') return '모금율순'
  if (sort.value === 'ENDING') return '종료임박순'
  return '최근등록순'
})
const bsnsLabel = computed(() => (bsnsType.value && data.value ? data.value.bsnsTypes[bsnsType.value] : '사업구분 전체'))

function reload(overrides) {
  const query = { ...route.query, ...overrides, page: '1' }
  Object.keys(query).forEach((k) => {
    if (!query[k]) delete query[k]
  })
  router.push({ path: '/designated-donation', query })
}
function setStatus(value) {
  reload({ status: value })
}
function applySort(value) {
  sortOpen.value = false
  reload({ sort: value })
}
function applyBsnsType(value) {
  bsnsOpen.value = false
  reload({ bsnsType: value })
}
function submitSearch() {
  reload({ q: qInput.value })
}
function goPage(p) {
  router.push({ path: '/designated-donation', query: { ...route.query, page: String(p) } })
}

function imgUrl(p) {
  return p.imageUrl || '/images/thumb.png'
}
function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <nav id="header_g" class="header2">
    <div class="gnb">
      <div class="center">
        <div class="gnb_slider_m dsg">
          <form class="goods_search" @submit.prevent="submitSearch">
            <input type="text" v-model="qInput" placeholder="지자체별 특정사업 보기" title="특정사업에 기부하기 검색" />
            <button type="submit"><img class="icon-img" src="/images/icon/search-white.png" alt="검색하기" /></button>
          </form>
        </div>
      </div>
    </div>
  </nav>

  <section class="main_contents" v-if="!loading && data">
    <div class="center">
      <div class="prj-filters">
        <div class="filter_radio">
          <div class="radio-items">
            <input type="radio" name="prjStatus" id="prjStat0" value="OPEN" :checked="status === 'OPEN'" @change="setStatus('OPEN')" />
            <label for="prjStat0">진행중</label>
          </div>
          <div class="radio-items">
            <input type="radio" name="prjStatus" id="prjStat1" value="CLOSED" :checked="status === 'CLOSED'" @change="setStatus('CLOSED')" />
            <label for="prjStat1">종료</label>
          </div>
        </div>
        <div class="filter_area">
          <div class="filter_price" @click="sortOpen = !sortOpen; bsnsOpen = false">
            <span class="s-txt">{{ sortLabel }}</span>
            <img src="/images/icon/filter_drop.png" class="icon-img" alt="정렬순서 필터 띄우기" />
            <div class="drop_select" :class="{ view: sortOpen }">
              <ul>
                <li><button type="button" @click="applySort('LATEST')">최근등록순</button></li>
                <li><button type="button" @click="applySort('AMOUNT')">참여금액순</button></li>
                <li><button type="button" @click="applySort('RATE')">모금율순</button></li>
                <li><button type="button" @click="applySort('ENDING')">종료임박순</button></li>
              </ul>
            </div>
          </div>
          <div class="v-line"></div>
          <div class="filter_price" @click="bsnsOpen = !bsnsOpen; sortOpen = false">
            <span class="s-txt">{{ bsnsLabel }}</span>
            <img src="/images/icon/filter_drop.png" class="icon-img" alt="사업구분 필터 띄우기" />
            <div class="drop_select" :class="{ view: bsnsOpen }">
              <ul>
                <li style="width: 300px">
                  <button type="button" style="white-space: break-spaces; text-align: left" @click="applyBsnsType('')">사업구분 전체</button>
                </li>
                <li v-for="(label, code) in data.bsnsTypes" :key="code" style="width: 300px">
                  <button type="button" style="white-space: break-spaces; text-align: left" @click="applyBsnsType(code)">{{ label }}</button>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="section all_prj" id="itemArea" v-if="data.projects.length">
        <ul class="prj-list-group">
          <li class="prj-list-items" v-for="p in data.projects" :key="p.dsgnDntnBizId">
            <router-link :to="`/designated-donation/${p.dsgnDntnBizId}`" style="display: block; color: inherit; text-decoration: none">
              <div class="list_inner">
                <span class="img_frame">
                  <div class="img_cover_link">
                    <img class="item_img" :src="imgUrl(p)" :alt="p.dsgnDntnBizTtl" />
                  </div>
                </span>
                <div class="inner-group">
                  <span class="prj_period deepGray" style="max-width: 100%">{{ p.periodText }}</span>
                  <span class="prj_title deepGray">{{ p.dsgnDntnBizTtl }}</span>
                  <span class="prj_loc pointGray">{{ p.locgovName }}</span>
                </div>
                <div class="inner-group">
                  <span class="prj_amount deepGray">{{ formatN(p.goalAmt) }}원</span>
                  <div class="prj_gauge">
                    <div class="gauge_back">
                      <div class="gauge_result" :style="{ width: p.rate + '%' }" :class="p.gaugeClass"></div>
                    </div>
                  </div>
                  <div class="inner_wrap">
                    <div class="prj_gaugePer">
                      <img v-if="p.overGoal" src="/images/icon/cli-icon_btn-donation2.png" alt="초과달성" />
                      <span class="perNum">{{ p.rate }}</span>%
                    </div>
                    <div class="prj_status" :class="{ on: status === 'OPEN' }">{{ status === 'OPEN' ? '진행' : '종료' }}</div>
                  </div>
                </div>
              </div>
            </router-link>
          </li>
        </ul>
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
      <div class="list-none" v-else>
        <img src="/images/icon/empty-prj.png" alt="결과 없음" />
        검색된 특정사업기부 사업이 없습니다.
      </div>
    </div>
  </section>
</template>

<style>
/* default_ali.css의 라디오 체크 아이콘이 '/static/images/...' 절대경로로 박혀있는데, 이
   프로젝트는 static 리소스를 루트 경로('/images/...')로 서빙한다(DonateView.vue와 동일한
   기존 버그 보정 - 공용 CSS는 건드리지 않는다). */
.prj-filters input[type='radio']:checked {
  background-image: url('/images/icon/cli-icon_radio-c.png') !important;
}
</style>

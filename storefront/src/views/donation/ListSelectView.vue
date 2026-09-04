<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS donation/list-select.html("기금사업 소개") 재현. Thymeleaf 버전(FundProjectController)은
// 진입만 서버 렌더링하고 이후 상호작용은 fetch로 조각(fragment) HTML을 받아 교체하는 SSR
// 우회였지만, 이 storefront는 진짜 Vue SPA라 그 우회 없이 시·도/지자체 선택·탭 전환·
// 페이지네이션 전부 클라이언트 상태로 처리한다(receipt 라운드에서 정립한 원칙과 동일).
const route = useRoute()
const router = useRouter()

const provinces = ref([])
const cities = ref([])
const fund = ref(null)
const tab = ref('info')
const loadingCities = ref(false)
const loadingFund = ref(false)

const upperLocgovCode = computed(() => route.query.upperLocgovCode ?? '')
const locgovCode = computed(() => route.query.locgovCode ?? '')

async function loadProvinces() {
  provinces.value = await api.get('donation', '/api/list-select/provinces')
}

async function loadCities(code) {
  if (!code) {
    cities.value = []
    return
  }
  loadingCities.value = true
  try {
    cities.value = await api.get('donation', `/api/list-select/cities?upperLocgovCode=${encodeURIComponent(code)}`)
  } finally {
    loadingCities.value = false
  }
}

async function loadFund(code, projectPage = 1, noticePage = 1) {
  if (!code) {
    fund.value = null
    return
  }
  loadingFund.value = true
  try {
    const params = new URLSearchParams({ locgovCode: code, projectPage: String(projectPage), noticePage: String(noticePage) })
    fund.value = await api.get('donation', `/api/list-select/fund?${params.toString()}`)
  } finally {
    loadingFund.value = false
  }
}

onMounted(async () => {
  await loadProvinces()
  if (upperLocgovCode.value) await loadCities(upperLocgovCode.value)
  if (locgovCode.value) await loadFund(locgovCode.value)
})

watch(upperLocgovCode, async (code) => {
  await loadCities(code)
})
watch(locgovCode, async (code) => {
  tab.value = 'info'
  await loadFund(code)
})

function selectProvince(e) {
  const code = e.target.value
  if (!code) {
    router.push({ path: '/list-select' })
    return
  }
  router.push({ path: '/list-select', query: { upperLocgovCode: code } })
}
function selectCity(code) {
  router.push({ path: '/list-select', query: { upperLocgovCode: upperLocgovCode.value, locgovCode: code } })
}
function projectPage(p) {
  loadFund(locgovCode.value, p, fund.value.noticeCurrentPage)
}
function noticePage(p) {
  loadFund(locgovCode.value, fund.value.projectCurrentPage, p)
}

async function addInterestLocgov() {
  try {
    const added = await api.postUrlEncoded('donation', '/interest-locgovs', { locgovCode: locgovCode.value })
    alert(`${added.locgovName ?? ''} 이(가) 관심 지자체로 등록되었습니다.`)
  } catch (e) {
    alert(e.message)
  }
}

function seletedLoc() {
  if (!fund.value) return ''
  return `${fund.value.locgov.upperLocgovNm ?? ''} ${fund.value.locgov.locgovNm}`
}
</script>

<template>
  <section class="center donation_steps">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        안내사항
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        기금사업 소개
      </span>
      <h2 class="page-title-txt">기금사업 소개</h2>
    </div>
  </section>

  <section id="contents">
    <div class="contents-wrap">
      <div class="center">
        <div class="s-contents list-wrapper">
          <div class="list-select-area">
            <div class="list-select-header">
              <div class="list-title"><strong>시·도</strong>를 선택해 주세요.</div>
              <div class="list-select">
                <label for="selectArea">지역선택</label>
                <select id="selectArea" :value="upperLocgovCode" @change="selectProvince">
                  <option value="">시·도 선택</option>
                  <option v-for="p in provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                </select>
              </div>
            </div>

            <div v-if="upperLocgovCode && !loadingCities" class="list-select-body">
              <ul class="list-select-group">
                <li v-for="c in cities" :key="c.locgovCode" tabindex="0" class="list-items"
                    :class="{ on: c.locgovCode === locgovCode }" @click="selectCity(c.locgovCode)">
                  {{ c.locgovNm }}
                </li>
              </ul>
            </div>
            <div v-else-if="!upperLocgovCode" class="list-select-body">
              <img src="/images/cli-list-select.png" alt="시도를 선택해 주세요" style="max-width: 72%; margin-top: 5rem" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="fund" class="contents-wrap">
      <div class="center">
        <div class="s-contents map-info" id="listContent">
          <div class="tab-container">
            <div class="tab_tit">
              <ul class="tab-menu">
                <li><a href="#" :class="{ active: tab === 'info' }" @click.prevent="tab = 'info'">지자체정보</a></li>
                <li><a href="#" :class="{ active: tab === 'project' }" @click.prevent="tab = 'project'">기금사업 소개</a></li>
                <li><router-link :to="{ path: '/gifts', query: { locgovCode } }">답례품</router-link></li>
                <li><a href="#" :class="{ active: tab === 'notice' }" @click.prevent="tab = 'notice'">지자체공지사항</a></li>
              </ul>
            </div>

            <div class="tab-content">
              <div v-show="tab === 'info'" class="tab-pane" :class="{ 'show active': tab === 'info' }">
                <div class="info-wrap">
                  <div class="city-Info">
                    <h3 class="seletedLoc">{{ seletedLoc() }}</h3>
                    <table>
                      <caption class="ir_txt">선택 지자체 상세정보</caption>
                      <tr><th>예산</th><td>{{ fund.locgov.displayBudgetAmt }} (단위:백만원)</td></tr>
                      <tr><th>인구수</th><td>{{ fund.locgov.displayPopltnCo }} 명</td></tr>
                      <tr><th>기부 담당자</th><td>{{ fund.locgov.chargerPsitnDept }}-{{ fund.locgov.chargerNm }} ({{ fund.locgov.chargerCttpc }})</td></tr>
                      <tr><th>홈페이지</th><td>{{ fund.locgov.locgovHmpg }}</td></tr>
                      <tr><th>포인트지급률</th><td>{{ fund.locgov.pointRate }}%</td></tr>
                    </table>
                  </div>
                  <div class="city-Info">
                    <div class="city-Info-title">
                      <img src="/images/icon/cli-icon_bullet-c.png" alt="" />
                      <span>지자체 소개 내용</span>
                    </div>
                    <div class="city-Info-txt" v-html="fund.locgov.locgovIntrcnCn"></div>
                  </div>
                </div>
              </div>

              <div v-show="tab === 'project'" class="tab-pane" :class="{ 'show active': tab === 'project' }">
                <div class="city-notice">
                  <h3 class="seletedLoc">{{ seletedLoc() }}</h3>
                  <table>
                    <caption>기금사업 소개 – 기금사업, 등록일로 구성</caption>
                    <thead><tr><th scope="row" class="tb_tit">기금사업</th><th scope="row" class="tb_dat">등록일</th></tr></thead>
                    <tbody>
                      <tr class="cursor" tabindex="0" v-for="p in fund.projects" :key="p.dsgnDntnBizId"
                          @click="router.push(`/designated-donation/${p.dsgnDntnBizId}`)">
                        <td class="tb_tit">{{ p.dsgnDntnBizTtl }}</td>
                        <td class="tb_dat">{{ p.dateText }}</td>
                      </tr>
                      <tr class="list-none" v-if="!fund.projects.length"><td colspan="2">기금사업을 준비중입니다.</td></tr>
                    </tbody>
                  </table>
                  <div class="pagination_ali" v-if="fund.projectTotalPages > 1">
                    <ul class="pagination-frame">
                      <li><a class="fist arrow_btn" href="javascript:void(0)" @click="projectPage(1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
                      <li><a class="prev arrow_btn" href="javascript:void(0)" @click="projectPage(Math.max(1, fund.projectCurrentPage - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
                      <li v-for="p in fund.projectTotalPages" :key="p" :class="{ 'selected-page': p === fund.projectCurrentPage }">
                        <a class="page-text" href="javascript:void(0)" @click="projectPage(p)">{{ p }}</a>
                      </li>
                      <li><a class="next arrow_btn" href="javascript:void(0)" @click="projectPage(Math.min(fund.projectTotalPages, fund.projectCurrentPage + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
                      <li><a class="last arrow_btn" href="javascript:void(0)" @click="projectPage(fund.projectTotalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
                    </ul>
                  </div>
                </div>
              </div>

              <div v-show="tab === 'notice'" class="tab-pane" :class="{ 'show active': tab === 'notice' }">
                <div class="city-notice">
                  <h3 class="seletedLoc">{{ seletedLoc() }}</h3>
                  <table>
                    <caption>지자체 공지사항 – 지자체 공지사항, 등록일로 구성</caption>
                    <thead><tr><th scope="row" class="tb_tit">지자체 공지사항</th><th scope="row" class="tb_dat">등록일</th></tr></thead>
                    <tbody>
                      <tr class="cursor" tabindex="0" v-for="n in fund.notices" :key="n.noticeId"
                          @click="router.push(`/notices/${n.noticeId}`)">
                        <td class="tb_tit">{{ n.subject }}</td>
                        <td class="tb_dat">{{ n.createdDate }}</td>
                      </tr>
                      <tr class="list-none" v-if="!fund.notices.length"><td colspan="2">지자체 공지사항을 준비중입니다.</td></tr>
                    </tbody>
                  </table>
                  <div class="pagination_ali" v-if="fund.noticeTotalPages > 1">
                    <ul class="pagination-frame">
                      <li><a class="fist arrow_btn" href="javascript:void(0)" @click="noticePage(1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
                      <li><a class="prev arrow_btn" href="javascript:void(0)" @click="noticePage(Math.max(1, fund.noticeCurrentPage - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
                      <li v-for="p in fund.noticeTotalPages" :key="p" :class="{ 'selected-page': p === fund.noticeCurrentPage }">
                        <a class="page-text" href="javascript:void(0)" @click="noticePage(p)">{{ p }}</a>
                      </li>
                      <li><a class="next arrow_btn" href="javascript:void(0)" @click="noticePage(Math.min(fund.noticeTotalPages, fund.noticeCurrentPage + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
                      <li><a class="last arrow_btn" href="javascript:void(0)" @click="noticePage(fund.noticeTotalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="btn-box tab-btn">
            <button type="button" class="formBtn intrst" @click="addInterestLocgov">
              <span class="icon_area"></span>
              <span class="btn_txt"> 관심 지자체 등록</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
.tab-pane {
  display: none;
}
.tab-pane.show.active {
  display: block;
}
</style>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const searchQuery = ref('')
function submitSearch() {
  router.push({ path: '/gifts', query: searchQuery.value ? { q: searchQuery.value } : {} })
}

const banners = ref([])
const projects = ref([])
const giveState = ref({ nowYearTotalAmt: 0, prevYearTotalAmt: 0, dDay: 0, nowDayPercent: 0 })

async function loadHome() {
  try {
    const data = await api.get('member', '/api/home')
    banners.value = data.banners ?? []
    projects.value = (data.projects ?? []).slice(0, 4)
    giveState.value = data.giveState
  } catch {
    // AS-IS와 동일하게 메인화면은 조회 실패로 깨지면 안 되므로 빈 상태로 조용히 둔다.
  }
}

const yoyPercent = computed(() => {
  const now = giveState.value.nowYearTotalAmt ?? 0
  const prev = giveState.value.prevYearTotalAmt ?? 0
  if (!prev || prev <= 0) return null
  return Math.floor((now * 100) / prev)
})

function formatAmount(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}

// AS-IS는 Swiper.js로 배너를 돌리지만, Thymeleaf 버전과 동일하게 별도 라이브러리 없는
// 최소한의 자동재생/이전·다음/페이지네이션 로직을 그대로 유지한다.
const current = ref(0)
let timer = null
let paused = false

function showSlide(index) {
  const len = banners.value.length
  if (len === 0) return
  current.value = (index + len) % len
}
function restart() {
  clearInterval(timer)
  if (!paused && banners.value.length > 1) {
    timer = setInterval(() => showSlide(current.value + 1), 5000)
  }
}
function prevSlide() {
  showSlide(current.value - 1)
  restart()
}
function nextSlide() {
  showSlide(current.value + 1)
  restart()
}
function toggleAutoplay() {
  paused = !paused
  restart()
}

onMounted(async () => {
  await loadHome()
  restart()
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div id="contents" class="contents-page">
    <section class="main-content inner">
      <div class="main-content__left">
        <div class="banner-wrapper">
          <div class="main-d-ban-swiper" id="main-banner" v-if="banners.length">
            <a
              v-for="(b, i) in banners"
              :key="b.bannerId"
              class="banner-slide"
              :class="{ active: i === current }"
              :href="b.linkUrl || '#'"
              :style="{ backgroundImage: `url(${b.imageUrl})` }"
              :title="b.title"
            >
              <span class="sr-only">{{ b.title }}</span>
            </a>
            <div class="swiper-indicator" v-if="banners.length > 1">
              <button type="button" class="swiper-autoplay-toggle" @click="toggleAutoplay"><span class="sr-only">일시정지</span></button>
              <div class="swiper-navigation">
                <button type="button" class="swiper-button-prev" @click="prevSlide"><span class="sr-only">이전 배너</span></button>
                <span class="swiper-pagination swiper-pagination-custom">
                  <span class="swiper-pagination-current">{{ current + 1 }}</span> / <span>{{ banners.length }}</span>
                </span>
                <button type="button" class="swiper-button-next" @click="nextSlide"><span class="sr-only">다음 배너</span></button>
              </div>
            </div>
          </div>
          <div class="main-d-ban-swiper" v-else style="background-image:url(/images/new/2026-main-banner-pc-01.png)"></div>
        </div>
        <div class="search-bar">
          <form @submit.prevent="submitSearch">
            <i class="ico-gov"></i>
            <input class="krds-input" type="text" v-model="searchQuery" placeholder="기부할 고향 및 답례품을 검색해보세요" />
            <button type="submit" class="search-bar__btn"><i class="ico-search"></i><span class="sr-only">검색</span></button>
          </form>
        </div>
      </div>

      <aside class="main-content__aside">
        <div class="donation">
          <div class="donation__header">
            <h3 class="donation__title">총 기부금</h3>
            <span class="donation__date">(전일 기준)</span>
          </div>
          <div class="donation__amount">
            <span class="amount__number">{{ formatAmount(giveState.nowYearTotalAmt) }}원</span>
            <span class="amount__days" v-if="giveState.dDay > 0">D-{{ giveState.dDay }}</span>
          </div>
          <div class="donation__progress">
            <div class="progress__bar">
              <div class="progress__fill" :style="{ width: giveState.nowDayPercent + '%' }">
                <div class="progress__mark"></div>
              </div>
            </div>
            <div class="progress__target">
              <span>전년 동기 대비</span>
              <strong v-if="yoyPercent !== null">{{ yoyPercent }}%</strong>
              <strong v-else>-</strong>
            </div>
          </div>
        </div>

        <div class="login-section" v-if="!auth.loggedIn">
          <div class="login__message">여러분의 소중한 기부가<br />고향에 큰 힘이 됩니다.</div>
          <router-link class="login__btn" to="/login">로그인</router-link>
          <div class="login__links">
            <router-link class="login__link" to="/find-idpw">아이디 찾기 · 비밀번호 찾기</router-link>
            <i class="login__bar"></i>
            <router-link class="login__link" to="/signup">회원가입</router-link>
          </div>
        </div>

        <div class="login-section" v-else-if="auth.me">
          <div class="login__top">
            <router-link class="login__mark" to="/mypage/honor-certificates">
              <i class="ico-gift-mark"></i>
              <span>기부혜택증</span>
            </router-link>
            <div class="login__user">
              <p class="login__name">{{ auth.me.userName }} 님</p>
              <p class="login__txt">환영합니다.</p>
            </div>
          </div>
          <div class="login__donation">
            <ul class="login__items">
              <li class="login__item"><span class="login__tit">올해 기부액</span><span class="login__num">{{ formatAmount(auth.me.donationSummary?.thisYearAmt) }}원</span></li>
              <li class="login__item"><span class="login__tit">기부총액</span><span class="login__num">{{ formatAmount(auth.me.donationSummary?.totalAmt) }}원</span></li>
            </ul>
          </div>
          <div class="login__point">
            <ul class="login__items">
              <li class="login__item"><span class="login__tit">보유 포인트</span><span class="login__num">{{ formatAmount(auth.me.pointBalance) }}P</span></li>
            </ul>
          </div>
          <a class="login__btn--out" href="#" @click.prevent="auth.logout()">
            로그아웃
            <i class="svg-icon ico-logout"></i>
          </a>
        </div>
      </aside>
    </section>

    <section class="banner-half inner">
      <router-link class="banner-half__regular" to="/donate">
        <i class="ico_banner_heart"></i>
        <div class="banner-half__text">
          <div>
            <div class="banner-half__tit">자치단체에 기부하기</div>
            <div class="banner-half__sub">거주지 외 원하는 지자체를 선택해 자유롭게 기부합니다.</div>
          </div>
          <span>일반기부</span>
        </div>
        <i class="ico_arrow_right_gray"></i>
      </router-link>
      <router-link class="banner-half__designated" to="/designated-donation">
        <i class="ico_banner_hand"></i>
        <div class="banner-half__text">
          <div>
            <div class="banner-half__tit">특정사업에 기부하기</div>
            <div class="banner-half__sub">지자체가 추진하는 특정 사업을 선택해 목적있는 기부를 합니다.</div>
          </div>
          <span>지정기부</span>
        </div>
        <i class="ico_arrow_right_gray"></i>
      </router-link>
    </section>

    <section class="main-box inner">
      <div class="main-box__head">
        <div class="main-box__tit">답례품 보러가기</div>
        <router-link class="main-box__link" to="/gifts"><i class="ico_arrow_right"></i><span class="sr-only">답례품 전체보기</span></router-link>
      </div>
      <p class="main-box__sub">기부한 지역의 답례품만 선택할 수 있습니다.</p>
      <ul class="gift__items">
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'TOUR' } }"><span class="gift__img01"></span><span class="gift__txt">관광서비스</span></router-link></li>
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'AGRI' } }"><span class="gift__img02"></span><span class="gift__txt">농축산물</span></router-link></li>
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'SEAFOOD' } }"><span class="gift__img03"></span><span class="gift__txt">수산물</span></router-link></li>
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'PROCESSED' } }"><span class="gift__img04"></span><span class="gift__txt">가공식품</span></router-link></li>
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'LIVING' } }"><span class="gift__img05"></span><span class="gift__txt">생활용품</span></router-link></li>
        <li><router-link class="gift__link" :to="{ path: '/gifts', query: { categoryCode: 'VOUCHER' } }"><span class="gift__img06"></span><span class="gift__txt">지역상품권</span></router-link></li>
      </ul>
    </section>

    <section class="main-box inner" id="projects">
      <div class="main-box__head">
        <div class="main-box__tit">특정사업에 기부하기</div>
        <router-link class="main-box__link" to="/designated-donation"><i class="ico_arrow_right"></i><span class="sr-only">특정사업 전체보기</span></router-link>
      </div>
      <p class="main-box__sub">조금 더 특별한 특정사업에 기부하기와 함께 기적을 만들어 갑니다.</p>
      <ul class="prj__items">
        <li class="prj__item" v-for="p in projects" :key="p.dsgnDntnBizId">
          <div class="prj__img">
            <router-link class="prj__link" :to="`/designated-donation/${p.dsgnDntnBizId}`">
              <img v-if="p.imageUrl" :src="p.imageUrl" alt="" />
            </router-link>
          </div>
          <div class="prj__tit">{{ p.title }}</div>
          <div class="location-label"><span>{{ p.locgovName }}</span></div>
          <div class="prj__stat">
            <span>{{ formatAmount(p.raisedAmt) }}원</span>
            <span class="prj__stat-ratio">{{ p.percent }}%</span>
          </div>
          <div class="ratio-bar"><div class="ratio-bar__fill" :style="{ width: p.percent + '%' }"></div></div>
          <div class="prj__date">{{ p.beginYmd }} ~ {{ p.endYmd }}</div>
        </li>
      </ul>
      <p class="empty-note" v-if="!projects.length">현재 진행중인 특정사업이 없습니다.</p>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
// AS-IS main.html과 동일하게 Swiper로 메인 배너를 구동한다(자동재생/이전·다음/페이지네이션).
// 스타일은 new.css가 @import하는 swiper-bundle.min.css(전역)가 담당하므로 여기서는 동작만 붙인다.
import Swiper from 'swiper'
import { Navigation, Pagination, Autoplay } from 'swiper/modules'
import { api } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { formatN } from '../utils/format'
import SitePopups from '../components/SitePopups.vue'

const auth = useAuthStore()
const router = useRouter()

// 검색바 - AS-IS main.html: Enter 또는 검색버튼 클릭 시 답례품몰로 이동, 삭제버튼은 입력 비우기
const searchKeyword = ref('')
function search() {
  router.push({ path: '/gifts', query: searchKeyword.value ? { q: searchKeyword.value } : {} })
}
function keydown(e) {
  if (e.key === 'Enter') search()
}
function clearSearch() {
  searchKeyword.value = ''
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

// 배너 클릭: 같은 도메인 링크는 무조건 SPA 라우터로 태운다(절대 URL이어도 origin이 같으면 경로만
// 추출). 그래야 존재하지 않는 게시물/경로일 때 백엔드 Whitelabel 404가 아니라 SPA catch-all →
// AS-IS 404 페이지가 뜬다. 진짜 외부 도메인만 기본 이동, 링크가 없으면 아무것도 하지 않는다.
function onBannerClick(item, e) {
  const raw = item?.linkUrl
  if (!raw || raw === '#') {
    e.preventDefault()
    return
  }
  let path = raw
  if (/^https?:\/\//i.test(raw)) {
    try {
      const u = new URL(raw)
      if (u.origin !== window.location.origin) return // 진짜 외부 도메인 → 기본 이동
      path = u.pathname + u.search + u.hash // 같은 도메인 → SPA 경로로 전환
    } catch {
      return // URL 파싱 실패 시 기본 동작에 맡긴다
    }
  }
  e.preventDefault()
  router.push(path)
}

// 날짜를 YYYY-MM-DD로 통일한다(원본이 20260914이든 2026-09-14이든 동일 출력).
function fmtDate(s) {
  if (!s) return ''
  const d = String(s).replace(/[^0-9]/g, '')
  if (d.length >= 8) return `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}`
  return String(s)
}

const yoyPercent = computed(() => {
  const now = giveState.value.nowYearTotalAmt ?? 0
  const prev = giveState.value.prevYearTotalAmt ?? 0
  if (!prev || prev <= 0) return null
  return Math.floor((now * 100) / prev)
})

// AS-IS main.html initSwiper()와 동일한 설정으로 메인 배너를 구동한다(slidesPerView/speed/autoplay
// delay·pagination type:custom·navigation 셀렉터까지 그대로). pagination을 custom으로 둬야
// new.css의 .swiper-pagination-custom 스타일이 적용된다(fraction이면 클래스가 달라 안 먹음).
let swiper = null
const paused = ref(false)
async function initSwiper() {
  await nextTick()
  if (swiper) {
    swiper.destroy(true, true)
    swiper = null
  }
  if (banners.value.length === 0) return
  swiper = new Swiper('.main-d-ban-swiper .swiper', {
    modules: [Navigation, Pagination, Autoplay],
    slidesPerView: 1,
    spaceBetween: 16,
    speed: 800,
    loop: banners.value.length > 1,
    autoplay: { delay: 6000, disableOnInteraction: false },
    navigation: {
      nextEl: '.main-d-ban-swiper .swiper-button-next',
      prevEl: '.main-d-ban-swiper .swiper-button-prev',
    },
    pagination: {
      el: '.main-d-ban-swiper .swiper-pagination',
      type: 'custom',
      renderCustom(sw, current, total) {
        return `<div class="swiper-pagination-current"><span class="sr-only">현재 페이지</span>${current}</div> / <div class="swiper-pagination-total"><span class="sr-only">전체 페이지</span>${total}</div>`
      },
    },
  })
  paused.value = false
}
// AS-IS의 재생/멈춤 버튼과 동일 - 자동재생 중이면 멈춤버튼만, 멈추면 재생버튼만 보인다
function playBtnEvnet() {
  swiper?.autoplay?.start()
  paused.value = false
}
function stopBtnEvnet() {
  swiper?.autoplay?.stop()
  paused.value = true
}

onMounted(async () => {
  await loadHome()
  await initSwiper()
})
onBeforeUnmount(() => {
  if (swiper) {
    swiper.destroy(true, true)
    swiper = null
  }
})
</script>

<template>
  <div id="contents" class="contents-page">
    <SitePopups />
    <section class="main-content inner">
      <div class="main-content__left" id="tmpMove">
        <!-- 메인 배너 (AS-IS main.html 마크업 그대로, Swiper 구동) -->
        <div class="banner-wrapper">
          <div class="main-d-ban-swiper" v-if="banners.length">
            <div class="swiper">
              <ul class="swiper-wrapper">
                <li class="swiper-slide" v-for="(item, i) in banners" :key="item.bannerId ?? i">
                  <a :href="item.linkUrl || '#'" :title="item.title" class="swiper-img-full" @click="onBannerClick(item, $event)">
                    <img class="pc-only" :src="item.imageUrl" :alt="item.title" />
                    <img class="mob-only" :src="item.mobileImageUrl || item.imageUrl" :alt="item.title" />
                  </a>
                </li>
              </ul>
            </div>
            <div class="swiper-indicator">
              <div class="swiper-controller">
                <button type="button" class="swiper-button-play" v-show="paused" @click="playBtnEvnet"><span class="sr-only">메인 배너 자동 롤링 재생</span></button>
                <button type="button" class="swiper-button-stop" v-show="!paused" @click="stopBtnEvnet"><span class="sr-only">메인 배너 자동 롤링 멈춤</span></button>
              </div>
              <div class="swiper-navigation">
                <button type="button" class="swiper-button-prev"><span class="sr-only">메인 배너 이전 슬라이드</span></button>
                <div class="swiper-pagination"></div>
                <button type="button" class="swiper-button-next"><span class="sr-only">메인 배너 다음 슬라이드</span></button>
              </div>
            </div>
          </div>
          <!-- 배너 데이터가 없을 때도 기본 배너 이미지로 영역을 유지한다 -->
          <div class="main-d-ban-swiper" v-else>
            <div class="swiper">
              <ul class="swiper-wrapper">
                <li class="swiper-slide">
                  <a href="#" class="swiper-img-full">
                    <img class="pc-only" src="/images/new/2026-main-banner-pc-01.png" alt="" />
                    <img class="mob-only" src="/images/new/2026-main-banner-mo-01.png" alt="" />
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <!-- //메인 배너 -->
        <!-- 검색바 (AS-IS main.html 마크업 그대로) -->
        <div class="search-bar form-conts btn-ico-wrap" data-delete="true">
          <i class="ico-gov"></i>
          <label for="search-input" class="sr-only">답례품 검색</label>
          <input type="text" class="krds-input" placeholder="기부할 고향 및 답례품을 검색해보세요" id="search-input" @keydown="keydown" v-model="searchKeyword" />
          <div class="btn-group">
            <button type="button" class="krds-btn medium icon pure btn-delete-input" @click="clearSearch"><span class="sr-only">내용 삭제</span><i class="svg-icon ico-delete-fill"></i></button>
            <button type="button" class="search-bar__btn" @click.prevent="search"><i class="ico-search"></i><span class="sr-only">검색</span></button>
          </div>
        </div>
        <!-- //검색바 -->
      </div>

      <aside class="main-content__aside">
        <div class="donation">
          <div class="donation__header">
            <h3 class="donation__title">총 기부금</h3>
            <span class="donation__date">(전일 기준)</span>
          </div>
          <div class="donation__amount">
            <span class="amount__number">{{ formatN(giveState.nowYearTotalAmt) }}원</span>
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
          <button type="button" class="login__btn" @click="router.push('/login')">로그인</button>
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
              <li class="login__item"><span class="login__tit">올해 기부액</span><span class="login__num">{{ formatN(auth.me.donationSummary?.thisYearAmt) }}원</span></li>
              <li class="login__item"><span class="login__tit">기부총액</span><span class="login__num">{{ formatN(auth.me.donationSummary?.totalAmt) }}원</span></li>
            </ul>
          </div>
          <div class="login__point">
            <ul class="login__items">
              <li class="login__item"><span class="login__tit">보유 포인트</span><span class="login__num">{{ formatN(auth.me.pointBalance) }}P</span></li>
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
            <span>{{ formatN(p.raisedAmt) }}원</span>
            <span class="prj__stat-ratio">{{ p.percent }}%</span>
          </div>
          <div class="ratio-bar"><div class="ratio-bar__fill" :style="{ width: p.percent + '%' }"></div></div>
          <div class="prj__date">{{ fmtDate(p.beginYmd) }} ~ {{ fmtDate(p.endYmd) }}</div>
        </li>
      </ul>
      <p class="empty-note" v-if="!projects.length">현재 진행중인 특정사업이 없습니다.</p>
    </section>
  </div>
</template>

<style scoped>
/* npm swiper(v11)가 nav 버튼에 기본 화살표 svg를 주입하는데, AS-IS는 new.css의 ::after(mask-image)로
   화살표를 그리므로 주입 svg를 숨겨 AS-IS와 동일하게 보이도록 한다. */
.main-d-ban-swiper :deep(.swiper-navigation-icon) {
  display: none;
}
</style>

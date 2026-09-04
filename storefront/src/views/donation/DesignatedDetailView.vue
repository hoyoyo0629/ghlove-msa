<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS designated-donation/details.html 재현(donation 서비스 designated-detail.html과 동일
// 출처). 이미지 갤러리는 AS-IS도 대표 이미지 1장 구조가 대부분이라(donation 서비스 라운드에서
// 실제 운영 사이트로 확인 완료) 메인 이미지+썸네일 1장만 재현한다 - 사업별 갤러리 관리는 그때도
// 범위 밖으로 확정됐다. "기부하기" 버튼은 AS-IS와 동일하게 이 화면에서 바로 금액을 받지 않고
// 기부하기 화면(DonateView, 특정사업 선택이 미리 채워진 채)으로 이동만 한다.
const route = useRoute()
const router = useRouter()

const project = ref(null)
const loading = ref(true)
const activeTab = ref('detail')
const openNoticeId = ref(null)
const sticky = ref(false)

async function load() {
  loading.value = true
  activeTab.value = 'detail'
  try {
    project.value = await api.get('donation', `/api/designated-donation/projects/${route.params.id}`)
  } catch {
    router.push('/designated-donation')
  } finally {
    loading.value = false
  }
}
onMounted(load)

let tabOffsetTop = 0
function onScroll() {
  sticky.value = window.pageYOffset >= tabOffsetTop
}
function captureTabOffset() {
  const el = document.getElementById('item_tab_wrap')
  tabOffsetTop = el ? el.offsetTop : 0
}
onMounted(() => {
  window.addEventListener('scroll', onScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})

function switchTab(tab, event) {
  activeTab.value = tab
  captureTabOffset()
  document.getElementById('item_tab_wrap')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  event?.currentTarget?.blur()
}
function toggleNotice(id) {
  openNoticeId.value = openNoticeId.value === id ? null : id
}
function goDonate() {
  router.push({ path: '/donate', query: { locgovCode: project.value.lclgvCd, prjId: String(project.value.dsgnDntnBizId) } })
}
function imgUrl() {
  return project.value.imageUrl || '/images/thumb.png'
}
function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="page-title-box mall center" v-if="!loading && project">
    <span class="ali_breadcrumb">
      <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
      <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
      <a href="javascript:void(0)">기부하기</a>
      <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
      <router-link to="/designated-donation">특정사업에 기부하기</router-link>
    </span>
  </section>

  <section class="center prj_details" id="contents" style="padding-top: 0" v-if="!loading && project">
    <div class="goods_view_top prj">
      <div class="prj_img">
        <div class="target_img">
          <img class="item_img" style="width: 100%" :src="imgUrl()" :alt="project.dsgnDntnBizTtl + ' 대표 이미지'" />
        </div>
        <div class="thumb_box">
          <button type="button" class="swiper-button-prev" disabled>
            <img src="/images/goods/thumb_arrow-prev.png" alt="이전 특정사업에 기부하기 이미지 보기" />
          </button>
          <img class="item_img" style="width: 64px; height: 64px; object-fit: cover" :src="imgUrl()" :alt="project.dsgnDntnBizTtl + ' 사진1 보기'" />
          <button type="button" class="swiper-button-next" disabled>
            <img src="/images/goods/thumb_arrow-next.png" alt="다음 특정사업에 기부하기 이미지 보기" />
          </button>
        </div>
      </div>

      <div class="goods_info prj_info">
        <div class="info_box">
          <div class="prj_progress">
            <div class="brand_mall d_area" v-if="project.locgovName">
              <img src="/images/icon/d-area.png" alt="지역 위치 모양 마커" />
              <span>{{ project.locgovName }}</span>
            </div>
            <div class="info_title">
              <h2>{{ project.dsgnDntnBizTtl }}</h2>
              <p class="s-txt">{{ project.periodText }}</p>
            </div>
          </div>

          <div class="donation_amount">
            <span class="txt">기부총액 </span>
            <span class="pointblue"><strong>{{ formatN(project.raisedAmount) }}</strong> 원</span>
          </div>

          <div class="prj_progress">
            <div class="prj_gauge">
              <div class="gauge_back">
                <div class="gauge_result" :style="{ width: project.rate + '%' }" :class="project.gaugeClass"></div>
              </div>
            </div>
            <div class="inner_wrap">
              <div class="prj_gaugePer">
                <img v-if="project.overGoal" src="/images/icon/cli-icon_btn-donation2.png" alt="초과달성" />
                <span class="perNum">{{ project.rate }}</span>%
              </div>
              <div class="prj_status" :class="{ on: project.status === 'OPEN' }">{{ project.status === 'OPEN' ? '진행' : '종료' }}</div>
            </div>
          </div>

          <div class="info_row etc nomargin">
            <div class="line top-nomargin"></div>
            <div class="shop_info">
              <div class="title_col"><p>목표금액</p></div>
              <div class="para_col"><p class="txt">{{ formatN(project.goalAmt) }} 원</p></div>
            </div>
            <div class="shop_info">
              <div class="title_col"><p>기부참여</p></div>
              <div class="para_col"><p class="txt">{{ project.participantCount }} 명</p></div>
            </div>
            <div class="line bt-nomargin"></div>
          </div>

          <p class="txt">특정사업에 기부하기는 지역이 가진 문제를 해결하기 위해 고향사랑기부제를 프로젝트화하고 그 취지에 공감하는 특정사업을 직접 선택하여 기부할 수 있습니다.</p>

          <div class="prj_donation-btns" v-if="project.status === 'OPEN'">
            <button type="button" class="formBtn donation" @click="goDonate">
              <img src="/images/icon/cli-icon_btn-donation2.png" alt="하트와 월계수" /><span>기부하기</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="item_tab prj_tab" id="item_tab_wrap" style="margin-top: 40px" :class="{ sticky }">
      <ul class="nav nav-tabs nav-justified">
        <li class="nav-item">
          <a href="javascript:void(0)" class="nav-link" :class="{ active: activeTab === 'detail' }" @click="switchTab('detail', $event)">
            <span class="txt">사업소개</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="javascript:void(0)" class="nav-link" :class="{ active: activeTab === 'review' }" @click="switchTab('review', $event)">
            <span class="txt">응원메시지<br />(기부내역)</span>
          </a>
        </li>
        <li class="nav-item">
          <a href="javascript:void(0)" class="nav-link" :class="{ active: activeTab === 'qna' }" @click="switchTab('qna', $event)">
            <span class="txt">공지사항</span>
          </a>
        </li>
      </ul>
    </div>
    <div class="center tab_container">
      <div class="tab-content item_view">
        <div class="tab-pane" :class="{ active: activeTab === 'detail' }" v-show="activeTab === 'detail'">
          <div class="item_detail">
            <div class="total_top">
              <h3 class="total">사업 소개<span class="pointblue"></span></h3>
            </div>
            <div class="detailContent">
              <div class="sampleContents" v-html="project.dsgnDntnBizCn"></div>
            </div>
          </div>
        </div>

        <div class="tab-pane" :class="{ active: activeTab === 'review' }" v-show="activeTab === 'review'">
          <h3 class="sr-only">응원메시지(기부내역)</h3>
          <div class="item_review">
            <div class="total-noti-wrap">
              <div class="noti_wrap">
                <div class="noti-box">
                  <div class="noti_txt">총 {{ project.cheerMessages.length }}건이 기부되었습니다.</div>
                </div>
              </div>
            </div>
            <div class="list_wrap review_list">
              <ul v-if="project.cheerMessages.length">
                <li class="list_area" v-for="(c, i) in project.cheerMessages" :key="i">
                  <div class="review_open">
                    <div class="list_top">
                      <div class="review_para" style="width: 70%">
                        <p class="prj-tab-list_point-txt"><strong class="pointRed">{{ formatN(c.cntrAmt) }}</strong>원 참여</p>
                      </div>
                      <div class="review_para" style="width: 100%">
                        <p v-if="c.cheerMsg">{{ c.cheerMsg }}</p>
                      </div>
                      <div class="m-field" style="min-width: 30%">
                        <div class="review_id"><p>{{ c.maskedUserName }}</p></div>
                        <div class="review_id"><p>{{ c.maskedLoginId }}</p></div>
                        <div class="review_date"><p>{{ c.cntrDeFormatted }}</p></div>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
              <div class="list-none" v-else>
                <img src="/images/icon/non-list.png" alt="기부내역이 없음" />
                기부내역이 없습니다.
              </div>
            </div>
          </div>
        </div>

        <div class="tab-pane" :class="{ active: activeTab === 'qna' }" v-show="activeTab === 'qna'">
          <div class="item_qna">
            <div class="total_top">
              <h3 class="total">공지사항<span class="pointblue">{{ project.notices.length }}</span></h3>
            </div>
            <div class="list_wrap qna_list" v-if="project.notices.length">
              <ul>
                <li class="list_top dropdown cursor" v-for="n in project.notices" :key="n.prjNoticeId" :class="{ show: openNoticeId === n.prjNoticeId }">
                  <button type="button" class="dropdown-toggle" @click="toggleNotice(n.prjNoticeId)">
                    <div class="header-faq">
                      <div class="faq_txt" style="white-space: normal; padding-right: 70px">{{ n.prjNoticeSubject }}</div>
                      <div class="col_group" style="right: 50px; position: absolute">
                        <div>{{ n.dateText }}</div>
                      </div>
                    </div>
                  </button>
                  <div class="dropdown-menu">
                    <div class="notice-wrap">
                      <div class="header-faq">
                        <div class="faq_txt" style="padding: 0 20px" v-html="n.prjNoticeCn"></div>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
            <div class="list-none" v-else>
              <img src="/images/icon/non-list.png" alt="" />
              게시글이 없습니다.
            </div>
          </div>
        </div>
      </div>

      <div class="center" style="margin-top: 60px">
        <div class="btn-box many mb_b60">
          <button type="button" class="blueBtn u-confirm" @click="router.push('/designated-donation')">목록 페이지</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
/* item.css는 .nav-tabs의 justify-content/width만 정의하고 기본 flex 레이아웃은 부트스트랩
   base CSS(.nav{display:flex}) 몫으로 가정한다(원래 Thymeleaf designated-detail.html과 동일한
   보정 - 이 storefront는 부트스트랩을 들여오지 않았다). */
.item_tab .nav-tabs {
  display: flex;
  list-style: none;
  padding-left: 0;
  margin-bottom: 0;
}
.item_tab .nav-tabs.nav-justified .nav-item {
  list-style: none;
  flex: 1 1 0;
  text-align: center;
}
.item_tab.sticky {
  top: 56px !important;
  margin-top: 0 !important;
}
.qna_list .list_top.dropdown .dropdown-toggle::after {
  background-image: url('/images/icon/list-dropdown.png') !important;
}
</style>

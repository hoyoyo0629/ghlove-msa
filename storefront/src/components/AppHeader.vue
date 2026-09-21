<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { openSitemap } from '../composables/useSitemap'

// AS-IS header_ali.vue 마크업/클래스를 그대로 재현한다(Thymeleaf 버전 fragments/header.html과
// 동일 출처). 스타일은 new.css/output.css 캐스케이드가 담당하므로 여기서는 구조/클래스만
// 맞추고, GNB 아코디언 열고닫기만 Vue reactive state로 구현한다(AS-IS도 클릭 토글, 마우스오버 아님).
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const headerWrap = ref(null)
const openIndex = ref(null)
function toggleMenu(i) {
  openIndex.value = openIndex.value === i ? null : i
}
function closeMenu() {
  openIndex.value = null
}
// AS-IS와 동일하게 하위메뉴를 클릭해 화면이 이동하면 GNB를 닫는다(라우트 변경 감지).
watch(() => route.fullPath, closeMenu)

// GNB가 펼쳐진 상태에서 헤더 바깥(페이지 본문)을 클릭하면 닫는다. 헤더 내부 클릭은 header-wrap의
// closeMenu/nav의 @click.stop이 처리하므로, 여기서는 header-wrap을 벗어난 클릭만 닫는다.
function onDocumentClick(e) {
  if (openIndex.value === null) return
  if (headerWrap.value && !headerWrap.value.contains(e.target)) closeMenu()
}
onMounted(() => document.addEventListener('click', onDocumentClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocumentClick))

// member 서비스에서 아직 Vue로 이관되지 않은 화면(마이페이지 등)은 계속 옛 Thymeleaf
// 화면(8081)으로 보낸다 - 라운드가 진행되며 하나씩 내부 라우트로 바뀐다.
const MEMBER_LEGACY = '/member'

async function onLogout() {
  await auth.logout()
  router.push('/')
}
</script>

<template>
  <div id="header-wrap" ref="headerWrap" @click="closeMenu">
    <div id="krds-skip-link"><a href="#contents">본문 바로가기</a></div>

    <div id="krds-masthead">
      <div class="toggle-wrap">
        <div class="toggle-head">
          <div class="inner">
            <span class="nuri-txt">이 누리집은 대한민국 공식 전자정부 누리집입니다.</span>
          </div>
        </div>
      </div>
    </div>

    <header id="krds-header">
      <div class="header-in">
        <div class="header-container">
          <div class="inner">
            <div class="header-branding">
              <h1 class="logo"><router-link to="/" title="메인페이지로 이동"><span class="sr-only">고향사랑e음 로고</span></router-link></h1>
              <div class="header-actions">
                <a v-if="auth.loggedIn" class="btn-navi navi-row logout" href="#" @click.prevent="onLogout">로그아웃</a>
                <router-link v-else class="btn-navi navi-row login" to="/login">로그인</router-link>
                <router-link v-if="!auth.loggedIn" class="btn-navi navi-row join" to="/signup">회원가입</router-link>
                <router-link class="btn-navi navi-row my" to="/mypage">마이페이지</router-link>
                <router-link class="btn-navi navi-row basket" to="/cart">장바구니</router-link>
                <router-link class="btn-navi navi-row confirm" to="/mypage/receipts">기부확인증</router-link>
              </div>
            </div>
          </div>
        </div>

        <nav class="krds-main-menu" @click.stop>
          <div class="inner">
            <ul class="gnb-menu" aria-label="메인 메뉴">
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 0 }" :aria-expanded="openIndex === 0" @click="toggleMenu(0)">기부</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 0 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
                        <ul class="type-description">
                          <li><strong class="tit"><router-link to="/donate">자치단체에 기부하기</router-link></strong><p class="txt">내 고향에 직접 기부 할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/designated-donation">특정사업에 기부하기</router-link></strong><p class="txt">내 고향의 특정사업에 기부 할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 1 }" :aria-expanded="openIndex === 1" @click="toggleMenu(1)">답례품</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 1 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
                        <ul class="type-description">
                          <li><strong class="tit"><router-link to="/gifts">답례품몰</router-link></strong><p class="txt">지역의 답례품을 확인 할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/gifts/seasonal">제철식품관</router-link></strong><p class="txt">지역에서 제공하는 제철 식품들을 확인 할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/gifts/community-business">마을기업관</router-link></strong><p class="txt">마을기업이 생산한 답례품들을 확인 할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 2 }" :aria-expanded="openIndex === 2" @click="toggleMenu(2)">안내사항</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 2 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
                        <ul class="type-description">
                          <li><strong class="tit"><router-link to="/list-select">기금사업 소개</router-link></strong><p class="txt">지역의 기금사업들을 볼 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/guide1">고향사랑기부제 안내</router-link></strong><p class="txt">고향사랑기부제에 대해 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/guide2">온라인 기부방법</router-link></strong><p class="txt">온라인 기부방법에 대해 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/guide5">오프라인 기부방법</router-link></strong><p class="txt">오프라인 기부방법에 대해 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/honor">연말정산 세액공제 안내</router-link></strong><p class="txt">내가 기부한 기부금이 연말정산 때 어떻게 세액공제 되는지 확인 할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/guide6">고향사랑기부 주의사항</router-link></strong><p class="txt">고향사랑기부를 하기 전에 주의해야 할 사항을 확인할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 3 }" :aria-expanded="openIndex === 3" @click="toggleMenu(3)">이벤트</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 3 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
                        <ul class="type-description">
                          <li><strong class="tit"><router-link :to="{ path: '/events', query: { ing: 'Y' } }">진행중인 이벤트</router-link></strong><p class="txt">진행중인 이벤트를 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link :to="{ path: '/events', query: { ing: 'N' } }">종료된 이벤트</router-link></strong><p class="txt">종료된 이벤트를 확인할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 4 }" :aria-expanded="openIndex === 4" @click="toggleMenu(4)">고객센터</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 4 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
                        <ul class="type-description">
                          <li><strong class="tit"><router-link to="/notices">공지사항</router-link></strong><p class="txt">새소식, 운영점검 등 서비스 이용에 필요한 정보를 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/data-board">자료실</router-link></strong><p class="txt">고향사랑e음 서비스 이용 관련 자료를 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/qna/board">Q&amp;A</router-link></strong><p class="txt">고향사랑e음 이용 중 궁금한 사항에 대한 상담을 받을 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/faqs">FAQ</router-link></strong><p class="txt">가장 많이 질문 받은 내용을 FAQ 형식으로 확인할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
              <li>
                <button class="gnb-main-trigger" :class="{ active: openIndex === 5 }" :aria-expanded="openIndex === 5" @click="toggleMenu(5)">마이페이지</button>
                <div class="gnb-toggle-wrap" :class="{ 'is-open': openIndex === 5 }">
                  <div class="gnb-main-list inner">
                    <div class="gnb-sub-list single-list">
                      <div class="gnb-sub-content">
        <!-- AS-IS header_ali.vue 마이페이지 서브메뉴 그대로: 5열(column) 구조·14개 항목.
                             AS-IS엔 없는 비밀번호변경/회원정보수정/역할신청/승인함은 GNB에서 제외(각 화면은
                             라우트로 직접 접근 가능). 회원정보수정·비밀번호변경은 마이페이지 안에서 진입. -->
                        <ul class="type-description">
                          <li>
                            <ul class="type-description--sub">
                              <li><strong class="tit"><router-link to="/mypage/donations">기부내역 조회</router-link></strong><p class="txt">내가 기부한 내역을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/points">기부포인트 조회</router-link></strong><p class="txt">나의 기부포인트를 확인할 수 있어요</p></li>
                            </ul>
                          </li>
                          <li>
                            <ul class="type-description--sub">
                              <li><strong class="tit"><router-link to="/orders">주문조회</router-link></strong><p class="txt">나의 주문내역을 조회할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/cart">장바구니</router-link></strong><p class="txt">내가 담은 답례품을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/claims/my">취소반품교환</router-link></strong><p class="txt">나의 취소반품교환내역을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/delivery">배송지 관리</router-link></strong><p class="txt">나의 배송지를 관리할 수 있어요</p></li>
                            </ul>
                          </li>
                          <li>
                            <ul class="type-description--sub">
                              <li><strong class="tit"><router-link to="/mypage/receipts">기부확인증 보기</router-link></strong><p class="txt">내가 기부한 내역을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/honor-certificates">기부혜택증 보기</router-link></strong><p class="txt">내가 기부한 고향에서 제공하는 혜택을 확인할 수 있어요</p></li>
                            </ul>
                          </li>
                          <li>
                            <ul class="type-description--sub">
                              <li><strong class="tit"><router-link to="/mypage/gift-qna">답례품 Q&amp;A</router-link></strong><p class="txt">답례품에 대해 궁금한 사항에 대한 상담을 받을 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/gift-reviews">답례품 후기</router-link></strong><p class="txt">내가 남긴 답례품 후기를 확인할 수 있어요</p></li>
                            </ul>
                          </li>
                          <li>
                            <ul class="type-description--sub">
                              <li><strong class="tit"><router-link to="/mypage/qna">1:1 문의</router-link></strong><p class="txt">고향사랑e음 이용 중 궁금한 사항에 대한 상담을 받을 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/interest-locgovs">관심지자체</router-link></strong><p class="txt">내가 관심있는 지역을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/wishlist">관심답례품</router-link></strong><p class="txt">내가 관심있는 답례품을 확인할 수 있어요</p></li>
                              <li><strong class="tit"><router-link to="/mypage/withdraw">회원탈퇴</router-link></strong><p class="txt">회원탈퇴를 할 수 있어요</p></li>
                            </ul>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>

            <a href="#modal_sitemap" class="gnb-all-btn open-modal" @click.prevent="openSitemap"><span class="sr-only">사이트맵</span></a>
          </div>
        </nav>
      </div>
    </header>
  </div>
</template>

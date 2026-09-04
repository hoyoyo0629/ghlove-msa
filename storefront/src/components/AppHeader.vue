<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// AS-IS header_ali.vue 마크업/클래스를 그대로 재현한다(Thymeleaf 버전 fragments/header.html과
// 동일 출처). 스타일은 new.css/output.css 캐스케이드가 담당하므로 여기서는 구조/클래스만
// 맞추고, GNB 아코디언 열고닫기만 Vue reactive state로 구현한다(AS-IS도 클릭 토글, 마우스오버 아님).
const auth = useAuthStore()
const router = useRouter()

const openIndex = ref(null)
function toggleMenu(i) {
  openIndex.value = openIndex.value === i ? null : i
}
function closeMenu() {
  openIndex.value = null
}

// member 서비스에서 아직 Vue로 이관되지 않은 화면(마이페이지 등)은 계속 옛 Thymeleaf
// 화면(8081)으로 보낸다 - 라운드가 진행되며 하나씩 내부 라우트로 바뀐다.
const MEMBER_LEGACY = 'http://localhost:8081'

async function onLogout() {
  await auth.logout()
  router.push('/')
}
</script>

<template>
  <div id="header-wrap" @click="closeMenu">
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
                        <ul class="type-description">
                          <li><strong class="tit"><router-link to="/mypage/donations">기부내역 조회</router-link></strong><p class="txt">내 기부내역을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/points">기부포인트 조회</router-link></strong><p class="txt">내 포인트 잔액/내역을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/orders">주문조회</router-link></strong><p class="txt">내 답례품 주문내역을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/cart">장바구니</router-link></strong><p class="txt">담아둔 답례품을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/claims/my">취소반품교환</router-link></strong><p class="txt">주문 취소/반품/교환 내역을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/delivery">배송지 관리</router-link></strong><p class="txt">배송지 목록을 관리할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/donations">기부확인증 보기</router-link></strong><p class="txt">기부확인증을 발급받을 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/honor-certificates">기부혜택증 보기</router-link></strong><p class="txt">세액공제 예상액을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/qna">1:1 문의</router-link></strong><p class="txt">궁금한 점을 문의할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/profile">회원정보수정</router-link></strong><p class="txt">회원 정보를 수정할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/interest-locgovs">관심지자체</router-link></strong><p class="txt">관심있는 지자체를 관리할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/wishlist">관심답례품</router-link></strong><p class="txt">찜한 답례품을 관리할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/password">비밀번호 변경</router-link></strong><p class="txt">비밀번호를 변경할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/role-request">지자체담당자·제공자 역할 신청</router-link></strong><p class="txt">추가 역할을 신청할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/role-queue">역할신청 승인함</router-link></strong><p class="txt">역할 신청 승인 대기 목록을 확인할 수 있어요</p></li>
                          <li><strong class="tit"><router-link to="/mypage/withdraw">회원탈퇴</router-link></strong><p class="txt">회원 탈퇴를 진행할 수 있어요</p></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>

            <a href="#modal_sitemap" class="gnb-all-btn open-modal"><span class="sr-only">사이트맵</span></a>
          </div>
        </nav>
      </div>
    </header>
  </div>
</template>

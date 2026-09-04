<script setup>
// AS-IS footer_ali.vue 마크업/클래스를 그대로 재현(Thymeleaf 버전 fragments/footer.html과 동일
// 출처). 사이트맵 모달은 AS-IS와 동일하게 CSS :target(#modal_sitemap)으로 여닫는다 - 헤더의
// "사이트맵" 버튼(href="#modal_sitemap")과 이 컴포넌트의 닫기 버튼(href="#")만으로 동작하고
// 별도 JS/컴포넌트 상태가 필요 없다.
</script>

<template>
  <div class="footer_wrap">
    <footer id="krds-footer">
      <div class="inner">
        <div class="f-logo"><span class="sr-only">행정안전부</span></div>
        <div class="f-info">
          <p class="f-addr">
            <span>본관 : 30112 세종특별자치시 도움6로 42(어진동)</span><span class="f-addr-bar">/</span>
            <span>별관 : 30116 세종특별자치시 가름로 143(어진동)</span>
          </p>
          <p class="f-addr f-addr--mob">
            <span>본관 : 30112 세종특별자치시 도움6로 42(어진동)</span>
            <span>별관 : 30116 세종특별자치시 가름로 143(어진동)</span>
          </p>
          <div class="info-cs--pc">
            <div class="f-title">고향사랑e음 고객센터</div>
            <ul class="info-cs">
              <li>
                <strong class="key-info"><strong>1522-2431</strong><span>(월 ~ 금 9:00 ~ 18:00, 공휴일 제외)</span></strong>
                <span class="more-info">고향사랑e음 기부 가능 시간 : 1:00 ~ 23:30</span>
              </li>
            </ul>
          </div>
        </div>
        <div class="f-btm">
          <ul class="f-menu">
            <li><router-link class="krds-btn text point" to="/policy/privacy">개인정보처리방침</router-link></li>
            <li><router-link class="krds-btn text" to="/policy/copyright">저작권정책</router-link></li>
            <li><router-link class="krds-btn text" to="/policy/auth">이용약관</router-link></li>
            <li><a class="krds-btn text open-modal" href="#modal_sitemap">사이트맵</a></li>
            <li><router-link class="krds-btn text" to="/notices">공지사항</router-link></li>
          </ul>
          <p class="f-copy">© Ministry of the Interior and Safety. All rights reserved.</p>
        </div>
        <a href="http://www.kwacc.or.kr/CertificationSite/WA/2326/Detail?page=1" class="btn-accessibility" target="_blank" rel="noopener">
          <img src="/images/accessibility-logo_2026.png" alt="국가공인 정보통신접근성 품질인증마크" style="width:100%;height:100%;">
        </a>
      </div>
    </footer>

    <section id="modal_sitemap" class="krds-modal" role="dialog" data-type="full">
      <div class="modal-dialog">
        <div class="modal-content sitemap">
          <div class="modal-header">
            <h2 class="modal-title sitemap__tit">사이트맵</h2>
          </div>
          <div class="modal-conts">
            <nav class="sitemap__nav" aria-label="사이트맵">
              <div class="sitemap__grid">
                <div class="sitemap__col sitemap__col--donation">
                  <h3 class="sitemap__title">기부</h3>
                  <ul class="sitemap__list">
                    <li><router-link to="/donate">자치단체에 기부하기</router-link></li>
                    <li><router-link to="/designated-donation">특정사업에 기부하기</router-link></li>
                  </ul>
                </div>
                <div class="sitemap__col sitemap__col--goods">
                  <h3 class="sitemap__title">답례품</h3>
                  <ul class="sitemap__list">
                    <li><router-link to="/gifts">답례품몰</router-link></li>
                    <li><router-link to="/gifts/seasonal">제철식품관</router-link></li>
                  </ul>
                </div>
                <div class="sitemap__col sitemap__col--center">
                  <h3 class="sitemap__title">고객센터</h3>
                  <ul class="sitemap__list">
                    <li><router-link to="/notices">공지사항</router-link></li>
                    <li><router-link to="/data-board">자료실</router-link></li>
                    <li><router-link to="/qna/board">Q&amp;A</router-link></li>
                    <li><router-link to="/faqs">FAQ</router-link></li>
                  </ul>
                </div>
                <div class="sitemap__col sitemap__col--guide">
                  <h3 class="sitemap__title">안내사항</h3>
                  <ul class="sitemap__list">
                    <li><router-link to="/list-select">기금사업 소개</router-link></li>
                    <li><router-link to="/guide1">고향사랑기부제 안내</router-link></li>
                    <li><router-link to="/guide2">온라인 기부방법</router-link></li>
                    <li><router-link to="/guide5">오프라인 기부방법</router-link></li>
                    <li><router-link to="/honor">연말정산 세액공제 안내</router-link></li>
                    <li><router-link to="/guide6">고향사랑기부 주의사항</router-link></li>
                  </ul>
                </div>
                <div class="sitemap__col sitemap__col--event">
                  <h3 class="sitemap__title">이벤트</h3>
                  <ul class="sitemap__list">
                    <li><router-link :to="{ path: '/events', query: { ing: 'Y' } }">진행중인 이벤트</router-link></li>
                    <li><router-link :to="{ path: '/events', query: { ing: 'N' } }">종료된 이벤트</router-link></li>
                  </ul>
                </div>
                <div class="sitemap__col sitemap__col--mypage">
                  <h3 class="sitemap__title">마이페이지</h3>
                  <ul class="sitemap__list">
                    <li><router-link to="/mypage/donations">기부내역 조회</router-link></li>
                    <li><router-link to="/mypage/points">기부포인트 조회</router-link></li>
                    <li><router-link to="/orders">주문조회</router-link></li>
                    <li><router-link to="/cart">장바구니</router-link></li>
                    <li><router-link to="/claims/my">취소반품교환</router-link></li>
                    <li><router-link to="/mypage/delivery">배송지 관리</router-link></li>
                    <li><router-link to="/mypage/gift-qna">답례품 Q&amp;A</router-link></li>
                    <li><router-link to="/mypage/gift-reviews">답례품 후기</router-link></li>
                    <li><router-link to="/mypage/donations">기부확인증 보기</router-link></li>
                    <li><router-link to="/mypage/honor-certificates">기부혜택증 보기</router-link></li>
                    <li><router-link to="/mypage/qna">1:1 문의</router-link></li>
                    <li><router-link to="/mypage/profile">회원정보수정</router-link></li>
                    <li><router-link to="/mypage/interest-locgovs">관심지자체</router-link></li>
                    <li><router-link to="/mypage/wishlist">관심답례품</router-link></li>
                    <li><router-link to="/mypage/withdraw">회원탈퇴</router-link></li>
                  </ul>
                </div>
              </div>
            </nav>
          </div>
          <a class="krds-btn icon btn-close close-modal" href="#">
            <span class="btn-close-txt">닫기</span>
            <i class="svg-icon ico-popup-close"></i>
          </a>
        </div>
      </div>
      <div class="modal-back"></div>
    </section>
  </div>
</template>

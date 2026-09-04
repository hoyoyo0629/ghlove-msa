<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage/index.html(및 Thymeleaf 버전 mypage.html) 재현 - .row_con/.half_con/
// .con_wrap/.wrap_items 클래스 그대로. AS-IS는 각 카드가 div @click 네비게이션이지만
// 여기서는 router-link/그냥 <a>로 링크한다.
const summary = ref(null)

onMounted(async () => {
  summary.value = await api.get('member', '/api/mypage/summary')
})

function formatAmount(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="find-idpw center">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        마이페이지
      </span>
      <h2 class="page-title-txt">마이페이지</h2>
    </div>
  </section>

  <section id="contents" v-if="summary">
    <div class="center">
      <div class="row_con flex">
        <div class="half_con dona">
          <h3 class="deepSubBlue">기부</h3>
          <div class="con_wrap">
            <router-link class="wrap_items left_list" to="/mypage/donations">
              <div class="top_area">
                <span class="link_tit"><span>기부내역</span><span>조회</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">기부내역 조회 바로가기</span></span>
              </div>
              <div class="data_area">
                <span class="data_val">{{ formatAmount(summary.donationTotal) }}</span>
                <span class="unit">원</span>
              </div>
            </router-link>
            <router-link class="wrap_items right_list" to="/mypage/points">
              <div class="top_area">
                <span class="link_tit"><span>기부포인트</span><span>조회</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">기부포인트 조회 바로가기</span></span>
              </div>
              <div class="data_area">
                <span class="data_val">{{ formatAmount(summary.pointBalance) }}</span>
                <span class="unit">P</span>
              </div>
            </router-link>
          </div>
        </div>

        <div class="half_con intrst">
          <h3 class="deepSubBlue">확인(혜택)증</h3>
          <div class="con_wrap">
            <router-link class="wrap_items left_list" to="/mypage/donations">
              <div class="top_area">
                <span class="link_tit"><span>기부확인증</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">기부확인증 바로가기</span></span>
              </div>
            </router-link>
            <router-link class="wrap_items right_list" to="/mypage/honor-certificates">
              <div class="top_area">
                <span class="link_tit"><span>기부혜택증</span><span>(지자체별)</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">기부혜택증 (지자체별) 바로가기</span></span>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <div class="row_con flex">
        <div class="half_con intrst">
          <h3 class="deepSubBlue">관심정보</h3>
          <div class="con_wrap">
            <router-link class="wrap_items left_list" to="/mypage/interest-locgovs">
              <div class="top_area">
                <span class="link_tit"><span>관심지자체</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">관심지자체 바로가기</span></span>
              </div>
              <div class="data_area">
                <span class="data_val count">{{ summary.interestLocgovCount }}</span>
                <span class="unit">건</span>
              </div>
            </router-link>
            <router-link class="wrap_items right_list" to="/mypage/wishlist">
              <div class="top_area">
                <span class="link_tit"><span>관심답례품</span></span>
                <span class="round-illustBtn_s"><span class="hidden_txt">관심답례품 바로가기</span></span>
              </div>
              <div class="data_area">
                <span class="data_val count">{{ summary.giftSummary.wishlistCount }}</span>
                <span class="unit">건</span>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <div class="row_con">
        <h3 class="deepSubBlue">답례품</h3>
        <div class="con_wrap goods">
          <router-link class="wrap_items" to="/orders">
            <div class="top_area"><span class="items_label">주문조회</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.orderSummary.orderCount }}</span><span class="unit">건</span></div>
          </router-link>
          <router-link class="wrap_items" to="/claims/my">
            <div class="top_area"><span class="items_label">취소반품교환</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.orderSummary.claimCount }}</span><span class="unit">건</span></div>
          </router-link>
          <router-link class="wrap_items" to="/mypage/gift-reviews">
            <div class="top_area"><span class="items_label">답례품 후기</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.giftSummary.reviewCount }}</span><span class="unit">건</span></div>
          </router-link>
          <router-link class="wrap_items" to="/mypage/gift-qna">
            <div class="top_area"><span class="items_label">답례품Q&amp;A</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.giftSummary.inquiryCount }}</span><span class="unit">건</span></div>
          </router-link>
          <router-link class="wrap_items" to="/mypage/delivery">
            <div class="top_area"><span class="items_label">배송지 관리</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.deliveryCount }}</span><span class="unit">건</span></div>
          </router-link>
          <router-link class="wrap_items" to="/my/coupons">
            <div class="top_area"><span class="items_label">쿠폰함</span></div>
            <div class="data_area"><span class="data_val count">{{ summary.orderSummary.usableCouponCount }}</span><span class="unit">건</span></div>
          </router-link>
        </div>
      </div>

      <div class="row_con flex cols3">
        <router-link class="wrap_items cursor" to="/mypage/profile">
          <span class="link_tit"><span class="deepSubBlue">회원정보수정</span></span>
          <span class="round-illustBtn_s"><span class="hidden_txt">회원정보수정 바로가기</span></span>
        </router-link>
        <router-link class="wrap_items cursor" to="/mypage/qna">
          <span class="link_tit"><span class="deepSubBlue">1:1 문의</span></span>
          <span class="round-illustBtn_s"><span class="hidden_txt">일대일 문의 바로가기</span></span>
        </router-link>
      </div>
    </div>
  </section>
</template>

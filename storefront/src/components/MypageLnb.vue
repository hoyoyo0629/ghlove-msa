<script setup>
import { computed } from 'vue'

// AS-IS components/layouts/mypage-lnb_ali.vue 재현 - 마이페이지 좌측 LNB.
// 2뎁스(마이페이지 메인/기부/답례품/관심정보/회원정보수정/1:1 문의)와, 현재 섹션의
// 3뎁스 서브메뉴를 함께 보여준다. current는 3뎁스 항목 키(없으면 2뎁스 키).
const props = defineProps({
  current: { type: String, required: true },
})

const menu = [
  { key: 'main', label: '마이페이지 메인', to: '/mypage', children: [] },
  {
    key: 'donation', label: '기부', to: '/mypage/donations',
    children: [
      { key: 'donations', label: '기부내역현황', to: '/mypage/donations' },
      { key: 'points', label: '기부포인트현황', to: '/mypage/points' },
      { key: 'receipts', label: '기부확인증', to: '/mypage/receipts' },
      { key: 'honor', label: '기부혜택증(지자체별)', to: '/mypage/honor-certificates' },
    ],
  },
  {
    key: 'gift', label: '답례품', to: '/orders',
    children: [
      { key: 'orders', label: '주문조회', to: '/orders' },
      { key: 'claims', label: '취소반품교환', to: '/claims/my' },
      { key: 'reviews', label: '답례품후기', to: '/mypage/gift-reviews' },
      { key: 'giftqna', label: '답례품Q&A', to: '/mypage/gift-qna' },
      { key: 'delivery', label: '배송지관리', to: '/mypage/delivery' },
    ],
  },
  {
    key: 'interest', label: '관심정보', to: '/mypage/interest-locgovs',
    children: [
      { key: 'interest-locgovs', label: '관심지자체', to: '/mypage/interest-locgovs' },
      { key: 'wishlist', label: '관심답례품', to: '/mypage/wishlist' },
    ],
  },
  { key: 'profile', label: '회원정보수정', to: '/mypage/profile', children: [] },
  { key: 'inquiry', label: '1:1 문의', to: '/mypage/qna', children: [] },
]

// current가 2뎁스 키면 그 항목, 3뎁스 키면 그 부모 섹션.
const activeSection = computed(() => {
  const direct = menu.find((m) => m.key === props.current)
  if (direct) return direct
  return menu.find((m) => m.children.some((c) => c.key === props.current)) || menu[0]
})
</script>

<template>
  <nav class="lnb">
    <div class="lnb-bar_2dep">
      <ul>
        <li v-for="m in menu" :key="m.key" :class="{ currentT: m.key === activeSection.key }">
          <router-link :to="m.to">{{ m.label }}</router-link>
          <span v-if="m.key === activeSection.key" class="sr-only">선택됨</span>
        </li>
      </ul>
    </div>
    <div class="lnb-bar_3dep" v-if="activeSection.children.length">
      <ul>
        <li v-for="c in activeSection.children" :key="c.key">
          <router-link :to="c.to" :class="{ currentP: c.key === current }">{{ c.label }}</router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

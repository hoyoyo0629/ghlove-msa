<script setup>
// AS-IS components/layouts/customer-lnb_ali.vue(및 Thymeleaf 버전 fragments/customer-lnb.html)
// 재현 - 고객센터 하위 4개 화면(공지사항/자료실/Q&A/FAQ) 공통 2뎁스 내비게이션.
defineProps({
  current: { type: String, required: true },
})
</script>

<template>
  <nav class="lnb">
    <div class="lnb-bar_2dep">
      <ul>
        <li><router-link to="/notices" :class="{ currentT: current === 'notices' }">공지사항</router-link></li>
        <li><router-link to="/data-board" :class="{ currentT: current === 'data-board' }">자료실</router-link></li>
        <li><router-link to="/qna/board" :class="{ currentT: current === 'qna' }">Q&amp;A</router-link></li>
        <li><router-link to="/faqs" :class="{ currentT: current === 'faqs' }">FAQ</router-link></li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
// 목록 화면 9곳이 글자 하나까지 똑같이 복붙해 쓰던 페이지네이션 마크업.
// card=true는 AS-IS designation.css의 .card-pagination(margin-bottom만 있는 래퍼) 여백용 -
// 래퍼 div를 따로 두지 않고 같은 요소에 클래스만 얹었다(자식 선택자가 없어 렌더 결과 동일).
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  card: { type: Boolean, default: false },
})
const emit = defineEmits(['change'])
</script>

<template>
  <div v-if="totalPages > 1" class="pagination_ali" :class="{ 'card-pagination': card }">
    <ul class="pagination-frame">
      <li><a class="fist arrow_btn" href="javascript:void(0)" @click="emit('change', 1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
      <li><a class="prev arrow_btn" href="javascript:void(0)" @click="emit('change', Math.max(1, page - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
      <li v-for="p in totalPages" :key="p" :class="{ 'selected-page': p === page }">
        <a class="page-text" href="javascript:void(0)" @click="emit('change', p)">{{ p }}</a>
      </li>
      <li><a class="next arrow_btn" href="javascript:void(0)" @click="emit('change', Math.min(totalPages, page + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
      <li><a class="last arrow_btn" href="javascript:void(0)" @click="emit('change', totalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
    </ul>
  </div>
</template>

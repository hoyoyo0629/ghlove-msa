<script setup>
// AS-IS fixedBtn.vue 재현. 접기/열기 토글은 AS-IS Thymeleaf 버전과 동일하게 숨김 체크박스
// 트릭(site.css .float-toggle-input:checked)을 그대로 쓴다.
function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <input type="checkbox" id="float-toggle-input" class="float-toggle-input" hidden />

  <div class="float-area" id="fixedBtn">
    <div class="float__item float__item--toggle">
      <a class="float__btn" title="새 창 열림" href="http://pf.kakao.com/_cxdtlG" target="_blank" rel="noopener">
        <i class="ico_ch"></i>
        <span class="sr-only">카카오채널 추가</span>
      </a>
    </div>
    <div class="float__item float__item--toggle">
      <a
        class="float__btn"
        title="새 창 열림"
        href="http://www.chatbot.go.kr/chatbotPop.ndo?bnrId=2Qe7TBzEHOthcAi"
        target="bnrPopup"
        @click="() => window.open('', 'bnrPopup', 'width=600,height=800')"
      >
        <i class="ico_chatbot"></i>
        <span class="sr-only">챗봇상담</span>
      </a>
    </div>
    <div class="float__item">
      <a class="float__btn" href="#top" @click.prevent="scrollTop">
        <i class="ico_top"></i>
        <span class="sr-only">TOP</span>
      </a>
    </div>
    <div class="float__item">
      <label for="float-toggle-input" class="float__btn-toggle float_btn">
        <i class="ico_toggle"></i>
        <span class="sr-only">접기/열기</span>
      </label>
    </div>
  </div>
</template>

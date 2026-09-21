<script setup>
// AS-IS op-modal($s.alert/$s.confirm)을 운영과 동일한 bootstrap 모달 구조로 재현. 커스텀 CSS 없이
// 운영과 같은 클래스(.modal.show / .modal-backdrop.show / .modal-dialog-centered / .modal-content /
// .pop_txt / .btn-group)만 쓴다. bootstrap은 JS로 display를 토글하므로 여기서는 display:block만 인라인.
import { onMounted, onUnmounted } from 'vue'
import { modalState, modalResolve } from '../composables/useModal'

function ok() {
  modalResolve(true)
}
function cancel() {
  modalResolve(false)
}

// 모달이 떠 있을 때 Enter를 누르면 '확인'으로 닫는다(운영과 동일). GlobalModal은 App.vue에
// 상시 마운트되므로 document 레벨에서 듣고 visible일 때만 처리한다.
function onKeydown(e) {
  if (!modalState.visible) return
  if (e.key === 'Enter') {
    e.preventDefault()
    ok()
  }
}
onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <template v-if="modalState.visible">
    <div class="modal-backdrop show"></div>
    <div class="modal show" style="display: block" role="dialog" @click.self="cancel">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-body">
            <div class="pop_txt">
              <p v-for="(line, i) in modalState.lines" :key="i">{{ line }}</p>
            </div>
            <div class="row no-gutters btn-group alert-type" v-if="!modalState.isConfirm">
              <div><button type="button" class="btn btn_lg btn_primary op-modal-ok" @click="ok">확인</button></div>
            </div>
            <div class="row no-gutters btn-group confirm-type" v-else>
              <div><button type="button" class="btn btn_lg btn_default op-modal-cancel" @click="cancel">취소</button></div>
              <div><button type="button" class="btn btn_lg btn_primary op-modal-ok" @click="ok">확인</button></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
</template>

<style scoped>
/* AS-IS op-modal 컴포넌트(components/layouts/alert.vue)의 scoped 스타일 그대로.
   common.css의 .btn-group .btn{width:100%}를 이겨야 취소/확인이 한 줄에 나온다
   (안 그러면 각 버튼이 100%라 두 줄로 감김). */
.modal .modal-body {
  padding: 0;
}
.btn-group .btn {
  min-width: 120px;
  padding: 16px 32px;
}
.confirm-type {
  justify-content: space-between;
}
</style>

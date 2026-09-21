import { reactive } from 'vue'

// AS-IS op.saleson.js의 $s.alert / $s.confirm(op-modal)을 대체하는 앱 공용 모달.
// window.alert/confirm 대신 이걸 써서 운영과 동일한 모달 UI로 통일한다.
// 사용: await modalAlert('메시지')  /  const ok = await modalConfirm('메시지')
export const modalState = reactive({
  visible: false,
  lines: [],
  isConfirm: false,
  _resolve: null,
})

function open(message, isConfirm) {
  return new Promise((resolve) => {
    // \n으로 나눠 여러 <p>로 표시(AS-IS pop_txt와 동일)
    modalState.lines = String(message ?? '').split('\n')
    modalState.isConfirm = isConfirm
    modalState.visible = true
    modalState._resolve = resolve
  })
}

export function modalAlert(message) {
  return open(message, false)
}
export function modalConfirm(message) {
  return open(message, true)
}
export function modalResolve(result) {
  modalState.visible = false
  const r = modalState._resolve
  modalState._resolve = null
  if (r) r(result)
}

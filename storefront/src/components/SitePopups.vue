<script setup>
// AS-IS components/ui/popup-layer.vue 재현. 메인 진입 시 노출중인 팝업을 받아
// 레이어로 띄운다. "오늘 하루 이 창을 열지 않음"은 AS-IS가 쿠키(popup_check_{id})로
// 하루 숨기던 것을 localStorage(popup_dismiss_{id}=YYYYMMDD)로 옮겨 재현한다.
// 노출기간·사용여부 판정은 admin displayPopups()가 하고 여기서는 표시만 한다.
import { onMounted, ref } from 'vue'
import { api } from '../api/http'

const popups = ref([])

function today() {
  const d = new Date()
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`
}

function dismissedToday(id) {
  try {
    return localStorage.getItem(`popup_dismiss_${id}`) === today()
  } catch {
    return false // 프라이빗 모드 등에서 접근 불가하면 그냥 보여준다
  }
}

function close(id) {
  popups.value = popups.value.filter((p) => p.popupId !== id)
}

function dontShowToday(id) {
  try {
    localStorage.setItem(`popup_dismiss_${id}`, today())
  } catch {
    // 저장 실패해도 닫기는 진행한다
  }
  close(id)
}

// AS-IS는 leftPosition/topPosition/width/height로 절대배치했다(popup-layer.vue:83~91).
// 값이 없으면(운영 콘솔이 아직 위치를 안 받는다) 화면 중앙 기본 크기로 띄운다.
function layerStyle(p) {
  const w = p.width && p.width > 0 ? p.width + 20 : 400
  if (p.leftPosition > 0 || p.topPosition > 0) {
    const style = { position: 'fixed', left: p.leftPosition + 'px', top: p.topPosition + 'px', width: w + 'px', zIndex: 9999 }
    if (p.height && p.height > 0) style.height = p.height + 'px'
    return style
  }
  return { position: 'fixed', left: '50%', top: '50%', transform: 'translate(-50%, -50%)', width: w + 'px', zIndex: 9999 }
}

async function load() {
  try {
    const list = await api.get('admin', '/api/popups')
    popups.value = (list ?? []).filter((p) => !dismissedToday(p.popupId))
  } catch {
    // AS-IS와 동일하게 팝업 조회 실패로 메인이 깨지면 안 되므로 조용히 넘어간다.
  }
}

onMounted(load)
</script>

<template>
  <div
    v-for="p in popups"
    :key="p.popupId"
    class="site-popup"
    :style="layerStyle(p)"
    role="dialog"
    :aria-label="p.subject"
  >
    <div class="site-popup__body">
      <!-- 이미지 팝업(popupStyle '3' 또는 이미지 등록됨): imageLink가 있으면 클릭 이동 -->
      <template v-if="p.popupStyle === '3' || p.popupImage">
        <a v-if="p.imageLink" :href="p.imageLink" target="_blank" rel="noopener">
          <img :src="p.popupImage" :alt="p.subject" style="max-width: 100%; display: block" />
        </a>
        <img v-else :src="p.popupImage" :alt="p.subject" style="max-width: 100%; display: block" />
      </template>
      <!-- 텍스트 팝업 -->
      <div v-else class="site-popup__content" v-html="p.content"></div>
    </div>
    <div class="site-popup__foot">
      <!-- popupClose 'N'이면 "오늘 하루" 옵션을 숨긴다(엔티티 주석: 닫기버튼 노출여부) -->
      <label v-if="p.popupClose !== 'N'" class="site-popup__today">
        <input type="checkbox" @change="dontShowToday(p.popupId)" />
        오늘 하루 이 창을 열지 않음
      </label>
      <button type="button" class="site-popup__close" @click="close(p.popupId)">닫기</button>
    </div>
  </div>
</template>

<style scoped>
.site-popup {
  background: #fff;
  border: 1px solid #d1d1d1;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  overflow: auto;
}
.site-popup__body {
  padding: 0;
}
.site-popup__content {
  padding: 16px;
  font-size: 14px;
  line-height: 1.5;
}
.site-popup__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid #eee;
  background: #f7f7f7;
}
.site-popup__today {
  font-size: 13px;
  color: #555;
  cursor: pointer;
}
.site-popup__close {
  border: 1px solid #ccc;
  background: #fff;
  padding: 4px 14px;
  cursor: pointer;
  border-radius: 3px;
}
</style>

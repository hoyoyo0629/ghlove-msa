<script setup>
// AS-IS .loc_select_group(시·도/시·군·구 선택)을 재현. 제철식품관·마을기업관의 지역 필터에 공용으로 쓴다.
// 시도 목록은 AS-IS와 동일(전남광주통합 등), 시군구는 /donation/api/locgovs를 upperLocgovCode로 묶어 채운다.
// 선택이 바뀌면 가장 구체적인 코드(시군구 우선, 없으면 시도)를 change 이벤트로 올린다.
import { onMounted, ref } from 'vue'
import { api } from '../api/http'

const emit = defineEmits(['change'])

const upperList = [
  { nm: '서울특별시', code: '11000' },
  { nm: '전남광주통합특별시', code: '12000' },
  { nm: '부산광역시', code: '26000' },
  { nm: '대구광역시', code: '27000' },
  { nm: '인천광역시', code: '28000' },
  { nm: '대전광역시', code: '30000' },
  { nm: '울산광역시', code: '31000' },
  { nm: '세종특별자치시', code: '36000' },
  { nm: '경기도', code: '41000' },
  { nm: '강원특별자치도', code: '51000' },
  { nm: '충청북도', code: '43000' },
  { nm: '충청남도', code: '44000' },
  { nm: '전북특별자치도', code: '52000' },
  { nm: '경상북도', code: '47000' },
  { nm: '경상남도', code: '48000' },
  { nm: '제주특별자치도', code: '50000' },
]

const byUpper = ref({})
const sido = ref('')
const sigungu = ref('')
const sigunguList = ref([])

async function load() {
  try {
    const all = await api.get('donation', '/api/locgovs')
    const map = {}
    for (const l of all) {
      const up = l.upperLocgovCode || ''
      if (!map[up]) map[up] = []
      map[up].push({ nm: l.locgovNm, code: l.locgovCode })
    }
    byUpper.value = map
  } catch {
    byUpper.value = {}
  }
}
onMounted(load)

function onSidoChange() {
  sigungu.value = ''
  sigunguList.value = byUpper.value[sido.value] || []
  emitCode()
}
function emitCode() {
  emit('change', sigungu.value || sido.value || '')
}
function reset() {
  sido.value = ''
  sigungu.value = ''
  sigunguList.value = []
  emitCode()
}
defineExpose({ reset })
</script>

<template>
  <span class="loc_select_group">
    <select title="시·도 선택" name="upperLocgovCode" id="upperLocgovCode" class="shirink_s" v-model="sido" @change="onSidoChange">
      <option value="">시·도 선택</option>
      <option v-for="u in upperList" :key="u.code" :value="u.code">{{ u.nm }}</option>
    </select>
    <select title="시·군·구 선택" name="locgovCodeSet" id="locgovCodeSet" class="shirink_s" v-model="sigungu" @change="emitCode">
      <option value="">시·군·구 선택</option>
      <option v-for="s in sigunguList" :key="s.code" :value="s.code">{{ s.nm }}</option>
    </select>
  </span>
</template>

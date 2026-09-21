<script setup>
// AS-IS components/ui/lclgv_chc/map.vue(지자체몰 지도선택)를 재현한다. 전국 지도 이미지 위에 17개
// 시도 라벨을 얹고(위치는 lclgv-map.css의 .text0~16), 시도 클릭 → 그 시도의 시군구 목록 → 시군구
// 클릭 시 select 이벤트로 locgovCode를 넘긴다. AS-IS는 getSiGunGu API를 쓰지만, MSA는 전체
// 지자체(/donation/api/locgovs: {locgovCode, upperLocgovCode, ...})를 한 번 받아 시도별로 묶는다.
import { onMounted, ref } from 'vue'
import { api } from '../api/http'

const emit = defineEmits(['select', 'close'])

// AS-IS map.vue lclgvUpperList 그대로(코드/라벨 위치 클래스/지도 하이라이트 이미지). 전남광주통합(12000)
// 등 AS-IS의 행정구역 통합 상태를 동일하게 반영한다.
const upperList = [
  { nm: '서울특별시', code: '11000', textClass: 'text', img: 'vtmap00.png' },
  { nm: '부산광역시', code: '26000', textClass: 'text15', img: 'vtmap01.png' },
  { nm: '대구광역시', code: '27000', textClass: 'text10', img: 'vtmap02.png' },
  { nm: '인천광역시', code: '28000', textClass: 'text1', img: 'vtmap03.png' },
  { nm: '전남광주통합특별시', code: '12000', textClass: 'text12', img: 'vtmap04.png' },
  { nm: '대전광역시', code: '30000', textClass: 'text7', img: 'vtmap05.png' },
  { nm: '울산광역시', code: '31000', textClass: 'text11', img: 'vtmap06.png' },
  { nm: '세종특별자치시', code: '36000', textClass: 'text6', img: 'vtmap07.png' },
  { nm: '경기도', code: '41000', textClass: 'text2', img: 'vtmap08.png' },
  { nm: '강원자치도', code: '51000', textClass: 'text3', img: 'vtmap09.png' },
  { nm: '충청북도', code: '43000', textClass: 'text5', img: 'vtmap10.png' },
  { nm: '충청남도', code: '44000', textClass: 'text4', img: 'vtmap11.png' },
  { nm: '전북특별자치도', code: '52000', textClass: 'text9', img: 'vtmap12.png' },
  { nm: '경상북도', code: '47000', textClass: 'text8', img: 'vtmap14.png' },
  { nm: '경상남도', code: '48000', textClass: 'text16', img: 'vtmap15.png' },
  { nm: '제주특별자치도', code: '50000', textClass: 'text14', img: 'vtmap16.png' },
]

const IMG_PATH = '/images/lclgv_chc/'
const mapImg = ref('vtmap.png')
const selectedUpperCode = ref('')
const selectedUpperNm = ref('시도를 선택해주세요.')
const INIT_LIST = [{ nm: '전체', code: '' }]
const sigunguList = ref([...INIT_LIST])

// 시도별 시군구 묶음: /donation/api/locgovs 한 번 받아 upperLocgovCode로 그룹핑
const byUpper = ref({})
async function loadLocgovs() {
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
onMounted(loadLocgovs)

// 시도 클릭 - 같은 시도 재클릭이면 해제
function chooseLocal(upperCode) {
  if (selectedUpperCode.value === upperCode) {
    selectedUpperCode.value = ''
    mapImg.value = 'vtmap.png'
    selectedUpperNm.value = '시도를 선택해주세요.'
    sigunguList.value = [...INIT_LIST]
    return
  }
  const info = upperList.find((u) => u.code === upperCode)
  selectedUpperCode.value = upperCode
  mapImg.value = info ? info.img : 'vtmap.png'
  selectedUpperNm.value = info ? info.nm : ''
  sigunguList.value = [{ nm: '전체', code: '' }, ...(byUpper.value[upperCode] || [])]
}

// 시군구 클릭 → 선택 확정(전체면 코드 없이)
function selectLclgv(code, nm) {
  emit('select', {
    upperCode: selectedUpperCode.value,
    code,
    upperNm: code ? selectedUpperNm.value : '',
    nm: code ? nm : '',
  })
}
</script>

<template>
  <div class="black-bg show" @click.self="emit('close')">
    <div class="map-popup">
      <button type="button" class="map-popup__close" @click="emit('close')" aria-label="닫기">×</button>
      <div class="mapWrap">
        <div class="area">
          <div class="image">
            <img :src="IMG_PATH + mapImg" class="mapImg" alt="전국 지도" />
            <div
              v-for="u in upperList"
              :key="u.code"
              class="addrName"
              :class="[u.textClass, { active: selectedUpperCode === u.code }]"
              tabindex="0"
              :title="u.nm + ' 시군구 조회하기'"
              @click="chooseLocal(u.code)"
              @keyup.enter="chooseLocal(u.code)"
            >
              <span>{{ u.nm.replace('특별시', '').replace('광역시', '').replace('특별자치도', '자치도').replace('특별자치시', '') }}</span>
            </div>
          </div>
        </div>
        <div class="addr">
          <div class="text_area active">
            <div class="addrTit">{{ selectedUpperNm }}</div>
            <div class="addrBox">
              <ul class="addrList">
                <li v-for="(s, i) in sigunguList" :key="i">
                  <a
                    href="javascript:void(0);"
                    style="cursor: pointer"
                    :title="s.code ? selectedUpperNm + ' ' + s.nm + ' 답례품 조회' : '전체 답례품 조회'"
                    @click="selectLclgv(s.code, s.nm)"
                    >{{ s.nm }}</a
                  >
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 팝업 껍데기만 최소 보정 - 지도/라벨 배치는 전역 lclgv-map.css(.mapWrap/.addrName/.textN)가 담당 */
.map-popup {
  position: relative;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  max-width: 900px;
  margin: 5vh auto;
  max-height: 90vh;
  overflow: auto;
}
.map-popup__close {
  position: absolute;
  top: 12px;
  right: 16px;
  border: none;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}
</style>

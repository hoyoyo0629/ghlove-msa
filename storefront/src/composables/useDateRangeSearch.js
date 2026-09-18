import { ref } from 'vue'
import { recentDateRange } from '../utils/format'

/** 조회기간(시작일~종료일) + 1주일/1·3·6개월 빠른선택 - 마이페이지 조회 화면 5곳이
 * 같은 UI를 쓴다. 각 화면이 ref 두 개와 setRange를 따로 들고 있던 걸 하나로 모은 것. */
export function useDateRangeSearch() {
  const searchStartDate = ref('')
  const searchEndDate = ref('')

  function setRange(unit, amount) {
    const { start, end } = recentDateRange(unit, amount)
    searchStartDate.value = start
    searchEndDate.value = end
  }

  function clearRange() {
    searchStartDate.value = ''
    searchEndDate.value = ''
  }

  return { searchStartDate, searchEndDate, setRange, clearRange }
}

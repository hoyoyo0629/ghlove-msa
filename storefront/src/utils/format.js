/** 금액·포인트 표기 - 화면 전체가 이 한 곳만 쓴다(원 단위 절사는 AS-IS 표기 규칙). */
export function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}

/** 필수 추가정보(textOption) 표시 - AS-IS cart/index.html·mypage/orderDetail.html formatTextOption 그대로.
 *  저장 포맷은 "제목 : 값 || 제목 : 값"이고, 값이 20자 초과면 잘라 '...', 항목은 <br>로 잇는다.
 *  v-html로 렌더한다(AS-IS 동일). */
export function formatTextOption(textOption) {
  if (!textOption) return ''
  return textOption
    .split('||')
    .map((item) => {
      const parts = item.split(' : ')
      const title = parts[0] ? parts[0].trim() : ''
      let value = parts[1] ? parts[1].trim() : ''
      if (value.length > 20) {
        value = value.substring(0, 20) + '...'
      }
      return `${title} : ${value}`
    })
    .join('<br>')
}

/** <input type="date">에 넣을 YYYY-MM-DD - toISOString()은 UTC로 바꿔버려서 KST 새벽
 * 0~9시에 하루 전 날짜가 나오므로(실제 버그였음) 반드시 로컬 필드로 조립한다. */
export function toDateInput(d) {
  const pad2 = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

/** 조회기간 빠른선택(1주일/1개월/...) - 오늘부터 거슬러 올라간 [시작일, 오늘]을 준다. */
export function recentDateRange(unit, amount) {
  const end = new Date()
  const start = new Date()
  if (unit === 'week') start.setDate(start.getDate() - 7 * amount)
  else if (unit === 'month') start.setMonth(start.getMonth() - amount)
  return { start: toDateInput(start), end: toDateInput(end) }
}

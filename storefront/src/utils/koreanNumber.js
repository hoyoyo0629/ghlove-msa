// 숫자를 한글 금액 표기로 변환한다(AS-IS donation-main의 numTxt "기부금은 X 원 입니다").
// 예: 0→"영", 10000→"일만", 20000→"이만", 2000000→"이백만".
export function koreanAmount(num) {
  const n = Math.floor(Number(num) || 0)
  if (n <= 0) return '영'
  const digit = ['', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구']
  const smallUnit = ['', '십', '백', '천']
  const bigUnit = ['', '만', '억', '조', '경']
  let result = ''
  let rest = n
  let i = 0
  while (rest > 0) {
    const group = rest % 10000
    if (group > 0) {
      let groupStr = ''
      let g = group
      let j = 0
      while (g > 0) {
        const d = g % 10
        if (d > 0) {
          groupStr = digit[d] + smallUnit[j] + groupStr
        }
        g = Math.floor(g / 10)
        j++
      }
      result = groupStr + bigUnit[i] + result
    }
    rest = Math.floor(rest / 10000)
    i++
  }
  return result
}

// 아라비아 숫자 + 한글 단위 축약(AS-IS 한도 표기 "2천만"). 예: 20000000→"2천만", 10000→"1만",
// 15000000→"1천5백만". 0이면 "0".
export function koreanAmountShort(num) {
  let n = Math.floor(Number(num) || 0)
  if (n <= 0) return '0'
  const bigUnit = ['', '만', '억', '조', '경']
  const smallUnit = ['', '십', '백', '천']
  let result = ''
  let i = 0
  while (n > 0) {
    const group = n % 10000
    if (group > 0) {
      let s = ''
      let g = group
      let j = 0
      while (g > 0) {
        const d = g % 10
        if (d > 0) {
          s = d + smallUnit[j] + s
        }
        g = Math.floor(g / 10)
        j++
      }
      result = s + bigUnit[i] + result
    }
    n = Math.floor(n / 10000)
    i++
  }
  return result
}

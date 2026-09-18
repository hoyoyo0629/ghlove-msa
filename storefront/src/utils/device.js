// AS-IS op.saleson.js:1059 `$s.isMobile()` 재현 - UA + 터치포인트 기반 모바일 판별.
// AS-IS 화면들이 PC 전용 기능(영수증 OZReport 출력 등)을 모바일에서 막을 때 쓰던 판별과 동일하게 맞춘다.
export function isMobile() {
  return (
    /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
    navigator.maxTouchPoints > 0
  )
}

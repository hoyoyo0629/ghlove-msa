// AS-IS 여러 화면(회원정보수정/배송지 추가)이 공유하는 Daum 우편번호 위젯 로더.
// <script> 태그를 템플릿 안에 그냥 적어두면 Vue가 DOM에 꽂아넣을 뿐 브라우저가 실행하지
// 않으므로, 최초 1회만 실제로 스크립트를 주입하고 이후 호출은 캐시된 Promise를 재사용한다.
let loadPromise = null

export function loadDaumPostcode() {
  if (window.daum?.Postcode) return Promise.resolve()
  if (loadPromise) return loadPromise
  loadPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('우편번호 서비스를 불러오지 못했습니다.'))
    document.head.appendChild(script)
  })
  return loadPromise
}

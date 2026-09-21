import { ref } from 'vue'
import { modalAlert } from './useModal'
import { api } from '../api/http'

/**
 * 카카오 인증서비스(카카오톡 지갑 본인인증) - 회원가입/로그인 두 화면이 같은 흐름을 쓴다.
 * AS-IS agree.vue submitKakao() + join.html joinFormData()의 code 처리에 해당한다.
 *
 * 흐름:
 *  1) start(): member에서 SDK 호출값(JS 앱키/settleId/redirectUri/요청항목/signData)을 받아
 *     Kakao.Auth.authorizeForCert()로 인증창을 띄운다. 실연계가 꺼져 있으면 지금까지처럼
 *     member의 모의 인증 안내화면으로 넘긴다.
 *  2) 인증 후 카카오가 redirectUri(= 이 화면)로 ?code=... 를 붙여 돌려보낸다.
 *  3) consumeCode(): 그 code를 member에 넘겨 검증 + 로그인/간편가입까지 끝낸다.
 *     응답 code가 JOIN_MEMBER면 신규가입이라 가입완료 안내를 띄운다(AS-IS와 동일).
 *
 * AS-IS는 signData를 브라우저 sessionStorage에 뒀지만 여기서는 member 세션이 들고 있어서
 * 화면이 따로 보관할 것이 없다.
 */
const KAKAO_SDK_SRC = 'https://t1.kakaocdn.net/kakao_js_sdk/2.7.2/kakao.min.js'
/** AS-IS join.html이 쓰던 값 그대로 - SDK 버전을 올릴 때 해시도 같이 갱신해야 한다. */
const KAKAO_SDK_INTEGRITY = 'sha384-TiCUE00h649CAMonG018J2ujOgDKW/kVWlChEuu4jK2vxfAAD0eZxzCKakxg55G4'

export function useKakaoCert() {
  const busy = ref(false)
  const errorMessage = ref('')

  function loadSdk() {
    if (window.Kakao) return Promise.resolve()
    const existing = document.querySelector(`script[src="${KAKAO_SDK_SRC}"]`)
    if (existing) {
      return new Promise((resolve, reject) => {
        existing.addEventListener('load', resolve)
        existing.addEventListener('error', () => reject(new Error('sdk')))
      })
    }
    return new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = KAKAO_SDK_SRC
      script.integrity = KAKAO_SDK_INTEGRITY
      script.crossOrigin = 'anonymous'
      script.onload = resolve
      script.onerror = () => reject(new Error('sdk'))
      document.head.appendChild(script)
    })
  }

  /** type: 'JOIN'(회원가입 화면 경유) | undefined(로그인 화면 경유) */
  async function start(type) {
    errorMessage.value = ''
    busy.value = true
    try {
      const query = type ? `?type=${encodeURIComponent(type)}` : ''
      const config = await api.get('member', `/api/external-auth/kakao/config${query}`)
      if (!config.enabled) {
        // 실연계 비활성 - member의 모의 인증 안내화면으로 흐름을 이어간다.
        window.location.href = '/member' + config.mockUrl
        return
      }
      await loadSdk()
      if (!window.Kakao.isInitialized()) {
        window.Kakao.init(config.jsKey)
      }
      window.Kakao.Auth.authorizeForCert({
        redirectUri: config.redirectUri,
        settleId: config.settleId,
        signData: config.signData,
        identifyItems: config.identifyItems,
      })
    } catch (e) {
      busy.value = false
      // AS-IS isKakaoInit() 실패 시 문구와 동일하게 안내한다.
      errorMessage.value = '카카오 기능 불러오기에 실패했습니다.\n새로고침 후 다시 진행해주세요.\n지속 문제 발생시 고객센터로 문의바랍니다.'
      modalAlert(errorMessage.value)
    }
  }

  /**
   * 인증 후 되돌아온 code 처리. 결과를 그대로 돌려주므로 화면이 JOIN_MEMBER/LOGIN에 따라
   * 가입완료 안내를 띄우거나 이동하면 된다. code가 없으면 null.
   */
  async function consumeCode(route, type) {
    const code = route.query.code
    if (!code) return null
    // AS-IS history.replaceState - 새로고침으로 같은 code를 두 번 쓰지 않게 즉시 지운다.
    window.history.replaceState({}, '', route.path)
    busy.value = true
    try {
      const data = await api.post('member', '/api/external-auth/kakao/verify', { code: String(code), type })
      if (data.status !== 'OK') {
        errorMessage.value = data.errMsg || '카카오톡 인증 로그인에 실패했습니다.'
        modalAlert(errorMessage.value)
        return null
      }
      return data
    } catch (e) {
      errorMessage.value = e.message
      modalAlert(errorMessage.value)
      return null
    } finally {
      busy.value = false
    }
  }

  return { busy, errorMessage, start, consumeCode }
}

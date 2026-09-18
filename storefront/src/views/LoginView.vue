<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/http'
import { useKakaoCert } from '../composables/useKakaoCert'
import { useAuthStore } from '../stores/auth'

// AS-IS users/login.html(및 Thymeleaf 버전 login.html)의 로그인 카드 재현. 탭 전환은
// AS-IS도 순수 클라이언트 상태 전환이라 Vue reactive state로 그대로 옮긴다.
// 카카오는 인증서비스(카카오톡 지갑 본인인증)라 이 화면이 JS SDK로 인증창을 띄우고
// 돌아온 인가코드를 member에 검증시킨다(AS-IS login.html과 같은 구조). 네이버/금융인증서/
// 간편인증은 member의 진입점으로 넘긴다.
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const tab = ref(1)
const loginId = ref('')
const password = ref('')
const saveId = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const mfaStep = ref(false)
const maskedPhone = ref('')
const devCode = ref('')
const mfaCode = ref('')

// 비밀번호 만료(PASSWORD_EXPIRED) 시 로그인 화면에서 띄우는 변경 모달 상태 (AS-IS pwdChangeModal).
const pwExpiredStep = ref(false)
const newPw = ref('')
const newPwC = ref('')

const target = typeof route.query.target === 'string' ? route.query.target : null

const kakaoCert = useKakaoCert()
/** 카카오 인증은 이 화면을 떠났다 ?code=로 되돌아오므로 target이 쿼리에서 사라진다 - AS-IS도
 *  같은 이유로 인증 시작 전에 target을 따로 보관했다(sessionStorage 'kakao-login-target'). */
const KAKAO_TARGET_KEY = 'ghlove_kakao_login_target'

/** 카카오 인증서비스 시작 - 로그인 화면 경유라 type을 싣지 않는다(신규가입이어도 메인으로 간다). */
function startKakaoLogin() {
  try {
    if (target) window.localStorage.setItem(KAKAO_TARGET_KEY, target)
    else window.localStorage.removeItem(KAKAO_TARGET_KEY)
  } catch {
    // 보관 실패해도 인증 자체는 진행한다 - 복귀 후 이동만 기본값(메인)이 된다.
  }
  kakaoCert.start()
}

onMounted(async () => {
  // 인증 후 이 화면으로 ?code=가 돌아온 경우 - 검증까지 끝나면 로그인 상태가 된다.
  const certResult = await kakaoCert.consumeCode(route, null)
  if (certResult) {
    await auth.fetchMe()
    let savedTarget = null
    try {
      savedTarget = window.localStorage.getItem(KAKAO_TARGET_KEY)
      window.localStorage.removeItem(KAKAO_TARGET_KEY)
    } catch {
      // 무시 - 메인으로 보낸다.
    }
    navigateAfterLogin(savedTarget)
    return
  }

  if (route.query.signup != null) successMessage.value = '회원가입이 완료되었습니다. 로그인해 주세요.'
  else if (route.query.passwordChanged != null) successMessage.value = '비밀번호가 변경되었습니다. 다시 로그인해 주세요.'
  else if (route.query.withdrawn != null) successMessage.value = '탈퇴 처리가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.'
  else if (route.query.reactivated != null) successMessage.value = '휴면 해제가 완료되었습니다. 다시 로그인해 주세요.'

  const saved = window.localStorage.getItem('ghlove_saved_login_id')
  if (saved) {
    loginId.value = saved
    saveId.value = true
  }
})

function afterLoginSuccess() {
  if (saveId.value) {
    window.localStorage.setItem('ghlove_saved_login_id', loginId.value)
  } else {
    window.localStorage.removeItem('ghlove_saved_login_id')
  }
  navigateAfterLogin(target)
}

/** 로그인 성공 후 이동 - 아이디저장 처리와 분리해 둔다(카카오 인증 복귀 경로는 이 화면에서
 *  아이디를 입력한 게 아니라서 저장된 아이디를 건드리면 안 된다). */
function navigateAfterLogin(to) {
  if (to && to.startsWith('/')) {
    router.push(to)
  } else if (to && /^http:\/\/localhost:808[1-6](\/.*)?$/.test(to)) {
    window.location.href = to
  } else {
    router.push('/')
  }
}

async function onSubmit() {
  errorMessage.value = ''
  try {
    const data = await auth.login(loginId.value, password.value, target ?? undefined)
    if (data.status === 'MFA_REQUIRED') {
      mfaStep.value = true
      maskedPhone.value = data.maskedPhone
      devCode.value = data.devCode ?? ''
    } else if (data.status === 'SLEEP_USER') {
      // AS-IS(op.saleson.js:1171): 휴면회원은 아이디/비번 본인확인까지 통과한 뒤, 로그인 화면에서
      // 그 자리에서 "휴면해제 하시겠습니까?"를 묻고, 해제하면 재로그인을 유도한다(별도 페이지·재입력 X).
      await onReactivateConfirm()
    } else if (data.status === 'PASSWORD_TEMP') {
      // AS-IS(op.saleson.js:1201): 임시비밀번호 사용자는 비밀번호찾기로 보내 새 비번을 설정하게 한다.
      alert('임시 비밀번호 사용자 입니다.')
      router.push('/find-idpw')
    } else if (data.status === 'PASSWORD_EXPIRED') {
      // AS-IS(op.saleson.js:1182): 비밀번호 만료 - 로그인 화면에서 변경 모달을 띄운다.
      pwExpiredStep.value = true
    } else if (data.status === 'OK') {
      afterLoginSuccess()
    } else {
      errorMessage.value = data.message || '로그인에 실패했습니다.'
    }
  } catch (e) {
    errorMessage.value = e.message
  }
}

/** 휴면회원 로그인 시도 후의 인라인 휴면해제 확인 (AS-IS op.saleson.js:1171~1177). 로그인
 *  단계에서 이미 본인확인을 마쳤으므로 recovery는 자격증명을 다시 받지 않고, 해제 후에는
 *  AS-IS와 동일하게 재로그인을 유도한다. */
async function onReactivateConfirm() {
  if (!window.confirm('휴면해제 하시겠습니까?')) {
    return
  }
  try {
    const res = await api.post('member', '/api/auth/recovery')
    if (res.status === 'OK') {
      successMessage.value = '휴면 해제가 완료되었습니다. 다시 로그인해 주세요.'
      password.value = ''
    } else {
      errorMessage.value = res.message || '휴면 해제에 실패했습니다.'
    }
  } catch (e) {
    errorMessage.value = e.message
  }
}

/** 만료 비밀번호 변경 (AS-IS op.saleson.js changePassword → /api/auth/change-password). 변경 성공 시
 *  서버가 세션·쿠키를 발급하므로 me를 다시 읽고 로그인 후 이동한다. */
async function changeExpiredPassword() {
  errorMessage.value = ''
  try {
    const res = await api.post('member', '/api/auth/change-password', { newPassword: newPw.value, newPasswordConfirm: newPwC.value })
    if (res.status === 'OK') {
      await auth.fetchMe()
      afterLoginSuccess()
    } else {
      errorMessage.value = res.message || '비밀번호 변경에 실패했습니다.'
    }
  } catch (e) {
    errorMessage.value = e.message
  }
}

/** "나중에 변경" (AS-IS delayChangePassword) - 만료만 미루고 로그인 진행. */
async function delayExpiredPassword() {
  errorMessage.value = ''
  try {
    const res = await api.post('member', '/api/auth/delay-change-password')
    if (res.status === 'OK') {
      await auth.fetchMe()
      afterLoginSuccess()
    } else {
      errorMessage.value = res.message || '처리에 실패했습니다.'
    }
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function onMfaSubmit() {
  errorMessage.value = ''
  try {
    const data = await auth.verifyMfa(mfaCode.value)
    if (data.status === 'OK') {
      afterLoginSuccess()
    } else {
      errorMessage.value = data.message || '인증번호가 일치하지 않습니다.'
    }
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <div id="contents">
    <section class="login-total center">
      <div class="login-box general">
        <div class="loginType">
          <ul>
            <li :class="{ currentB: tab === 1 }"><button type="button" @click="tab = 1">아이디 로그인</button></li>
            <li :class="{ currentB: tab === 2 }"><button type="button" @click="tab = 2">금융/간편 인증</button></li>
          </ul>
        </div>

        <div v-if="tab === 1 && !mfaStep && !pwExpiredStep">
          <div class="login-title-box">
            <img src="/images/icon/cli-icon-bullet.png" alt="" />
            <h2 class="login-title">아이디 <strong>로그인</strong></h2>
          </div>

          <p class="success" v-if="successMessage">{{ successMessage }}</p>
          <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

          <form id="login-general" @submit.prevent="onSubmit">
            <div class="login-field-group">
              <span class="login-field-items">
                <input type="text" placeholder="아이디" id="loginId" v-model="loginId" required autofocus />
              </span>
              <span class="login-field-items">
                <input type="password" placeholder="비밀번호" id="password" v-model="password" required />
              </span>
              <span class="login-field-check saveId">
                <input type="checkbox" id="id_save" v-model="saveId" />
                <label for="id_save"><span class="marker"></span>아이디저장</label>
              </span>
            </div>
            <div class="login-field-group">
              <button id="loginBtn" class="login" type="submit">로그인</button>
            </div>
            <div class="login-field-group">
              <div class="login-check-wrap">
                <span class="find-wrap"><router-link to="/find-idpw">아이디·비밀번호찾기</router-link></span>
                <span class="find-wrap"><router-link to="/signup" class="moreView deepGray"><strong>회원가입</strong></router-link></span>
              </div>
            </div>
          </form>

          <button type="button" class="btn-simple btn-simple--kakao" id="kakaoLoginBtn" @click="startKakaoLogin">
            <span class="btn-simple__logo"><img src="/images/kakao/kakaotalk_symbol_screen.png" class="btn-simple__img" alt="" aria-hidden="true" /></span>
            <span class="btn-simple__txt">카카오톡 인증 로그인</span>
          </button>
          <a href="/member/login/naver" class="btn-simple btn-simple--naver">
            <span class="btn-simple__logo"><img src="/images/new/naver_logo_2.png" class="btn-simple__img" alt="" aria-hidden="true" /></span>
            <span class="btn-simple__txt">네이버 인증 로그인</span>
          </a>
        </div>

        <div v-if="pwExpiredStep">
          <div class="login-title-box">
            <img src="/images/icon/cli-icon-bullet.png" alt="" />
            <h2 class="login-title">비밀번호 <strong>변경</strong></h2>
          </div>
          <p class="s-txt">비밀번호를 변경한 지 오래되었습니다. 안전을 위해 새 비밀번호로 변경해 주세요.</p>
          <p class="error" v-if="errorMessage">{{ errorMessage }}</p>
          <form @submit.prevent="changeExpiredPassword">
            <div class="login-field-group">
              <span class="login-field-items">
                <input type="password" placeholder="새 비밀번호" v-model="newPw" required autofocus />
              </span>
              <span class="login-field-items">
                <input type="password" placeholder="새 비밀번호 확인" v-model="newPwC" required />
              </span>
            </div>
            <div class="login-field-group">
              <button class="login" type="submit">변경하고 로그인</button>
            </div>
            <div class="login-field-group">
              <div class="login-check-wrap">
                <span class="find-wrap"><a href="#" @click.prevent="delayExpiredPassword">나중에 변경</a></span>
              </div>
            </div>
          </form>
        </div>

        <div v-if="mfaStep">
          <div class="login-title-box">
            <img src="/images/icon/cli-icon-bullet.png" alt="" />
            <h2 class="login-title">추가 <strong>인증</strong></h2>
          </div>
          <p class="s-txt">등록된 휴대폰({{ maskedPhone }})으로 인증번호를 보냈습니다.</p>
          <p class="error" v-if="devCode">[테스트용] 인증번호: {{ devCode }}</p>
          <p class="error" v-if="errorMessage">{{ errorMessage }}</p>
          <form @submit.prevent="onMfaSubmit">
            <div class="login-field-group">
              <span class="login-field-items">
                <input type="text" placeholder="인증번호 6자리" v-model="mfaCode" maxlength="6" required autofocus />
              </span>
            </div>
            <div class="login-field-group">
              <button class="login" type="submit">인증 확인</button>
            </div>
          </form>
        </div>

        <div v-if="tab === 2" style="display: block">
          <div class="login-title-box">
            <img src="/images/icon/cli-icon-bullet.png" alt="" />
            <h2 class="login-title">금융/간편 <strong>인증</strong></h2>
          </div>
          <div class="auth_wrap pt-0">
            <form action="/member/login/finance-cert">
              <div class="simple-login">
                <div class="financ-login-wrap">
                  <div class="finance-img"><img src="/images/icon/financ.png" alt="금융인증서" /></div>
                  <p class="s-txt"><span>금융기관에 등록된</span><span>금융인증서로 본인 인증 하기</span></p>
                </div>
              </div>
              <div class="login-field-group">
                <button id="finAuthBtn" class="login logins" type="submit" title="새 창 열림"><span>금융인증서 로그인</span></button>
              </div>
            </form>
            <form action="/member/login/simple-auth">
              <div class="simple-login" id="authBtn">
                <div class="simple-login-wrap">
                  <span class="sim_auth"><img src="/images/icon/sim_auth/kakao.png" alt="카카오톡" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/kb.png" alt="국민인증서" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/payco.png" alt="페이코" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/pass.png" alt="통신사PASS" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/samsung.png" alt="SAMSUNG PASS" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/naver.png" alt="네이버" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/shinhan.png" alt="신한인증서" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/toss.png" alt="토스" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/nh.png" alt="NH인증서" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/hana.png" alt="하나인증서" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/dream.png" alt="드림인증" /></span>
                  <span class="sim_auth"><img src="/images/icon/sim_auth/banks.png" alt="뱅크샐러드" /></span>
                </div>
              </div>
              <div class="login-field-group">
                <button class="login" type="submit" title="새 창 열림"><span>간편인증 로그인</span></button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

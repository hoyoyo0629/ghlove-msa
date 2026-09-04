<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// AS-IS users/login.html(및 Thymeleaf 버전 login.html)의 로그인 카드 재현. 탭 전환은
// AS-IS도 순수 클라이언트 상태 전환이라 Vue reactive state로 그대로 옮긴다. 금융/간편
// 인증(탭2)과 카카오/네이버 인증은 이 라운드 범위 밖이라 기존 member 서비스의 목업
// 스텁 화면(8081)으로 계속 보낸다.
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

const target = typeof route.query.target === 'string' ? route.query.target : null

onMounted(() => {
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
  if (target && target.startsWith('/')) {
    router.push(target)
  } else if (target && /^http:\/\/localhost:808[1-6](\/.*)?$/.test(target)) {
    window.location.href = target
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
    } else if (data.status === 'OK') {
      afterLoginSuccess()
    } else {
      errorMessage.value = data.message || '로그인에 실패했습니다.'
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

        <div v-if="tab === 1 && !mfaStep">
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

          <a href="http://localhost:8081/login/kakao" class="btn-simple btn-simple--kakao">
            <span class="btn-simple__logo"><img src="/images/kakao/kakaotalk_symbol_screen.png" class="btn-simple__img" alt="" aria-hidden="true" /></span>
            <span class="btn-simple__txt">카카오톡 인증 로그인</span>
          </a>
          <a href="http://localhost:8081/login/naver" class="btn-simple btn-simple--naver">
            <span class="btn-simple__logo"><img src="/images/new/naver_logo_2.png" class="btn-simple__img" alt="" aria-hidden="true" /></span>
            <span class="btn-simple__txt">네이버 인증 로그인</span>
          </a>
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
            <form action="http://localhost:8081/login/finance-cert">
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
            <form action="http://localhost:8081/login/simple-auth">
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

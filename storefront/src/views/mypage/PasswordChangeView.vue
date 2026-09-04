<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

// AS-IS users/modify.html "비밀번호 변경"(및 Thymeleaf 버전 password.html) 재현. 2단계로
// 동작한다: (1) 현재 비밀번호를 먼저 "인증" 버튼으로 재확인해야, (2) 새 비밀번호 입력칸이
// 열린다. 규칙 체크리스트는 FindIdPwView.vue의 비밀번호 재설정 화면과 완전히 동일한 로직.
// 성공 시 AS-IS와 동일하게 세션/JWT쿠키가 서버에서 모두 무효화되므로 클라이언트도 auth
// store를 비우고 로그인 화면으로 보낸다.
const router = useRouter()
const auth = useAuthStore()

const currentPassword = ref('')
const confirmed = ref(false)
const presentPwdMsg = ref('현재 사용중인 비밀번호로 인증해주세요.')
const presentPwdOk = ref(false)

const newPassword = ref('')
const newPasswordConfirm = ref('')
const errorMessage = ref('')
const pwRules = ref({ all: false, r1: false, r2: false, r3: false, r4: false })

async function checkPresentPwd() {
  if (!currentPassword.value) {
    alert('현재 사용중인 비밀번호를 입력해주세요.')
    return
  }
  try {
    await api.post('member', '/api/password/verify', { currentPassword: currentPassword.value })
    confirmed.value = true
    presentPwdMsg.value = '인증 완료되었습니다.'
    presentPwdOk.value = true
  } catch {
    alert('현재 사용중인 비밀번호가 아닙니다. 다시 입력해주세요.')
  }
}

function isContinued(str) {
  for (let i = 0; i < str.length - 2; i++) {
    const a = str.charCodeAt(i)
    const b = str.charCodeAt(i + 1)
    const c = str.charCodeAt(i + 2)
    if ((b - a === 1 && c - b === 1) || (b - a === -1 && c - b === -1)) return true
  }
  return false
}
function checkPwd() {
  const pwd = newPasswordConfirm.value
  const hasDigit = /[0-9]/.test(pwd)
  const hasLetter = /[a-zA-Z]/.test(pwd)
  const hasSymbol = /[{}[\]/?.,;:|)*~`!^\-_+<>@#$%&\\=('"]/.test(pwd)
  const rule1 = hasDigit && hasLetter && hasSymbol
  const rule2 = !isContinued(pwd) && !/(\w)\1\1/.test(pwd)
  const rule3 = pwd.indexOf(auth.me?.loginId ?? '') === -1
  const rule4 = pwd.length >= 9 && pwd.length <= 20
  pwRules.value = { r1: rule1, r2: rule2, r3: rule3, r4: rule4, all: rule1 && rule2 && rule3 && rule4 }
}
function ruleIcon(ok) {
  return ok ? '/images/icon/pw-available.png' : '/images/icon/pw-unavailable.png'
}

async function onSubmit() {
  errorMessage.value = ''
  if (!confirmed.value) {
    alert('현재 비밀번호 인증을 먼저 진행해 주세요.')
    return
  }
  if (!newPassword.value) {
    alert('비밀번호를 설정해주세요.')
    return
  }
  if (!newPasswordConfirm.value || newPassword.value !== newPasswordConfirm.value) {
    alert('비밀번호를 확인해주세요.')
    return
  }
  try {
    await api.post('member', '/api/password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
      newPasswordConfirm: newPasswordConfirm.value,
    })
    await auth.fetchMe()
    router.push('/login?passwordChanged=success')
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section class="find-idpw center">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage/profile">회원정보 수정</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        비밀번호 변경
      </span>
      <h2 class="page-title-txt">비밀번호 변경</h2>
    </div>

    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <form @submit.prevent="onSubmit">
      <div class="change-info-area">
        <div class="info-mess">
          <p class="sub-title">인증이 완료 되었습니다. 비밀번호를 초기화 합니다.</p>
          <p class="title"><strong>새로운 비밀번호</strong>를<br />입력해 주세요.</p>
        </div>
        <div class="line"></div>
        <div class="info-field-group">
          <div class="info-field-items newpw">
            <label for="currentPassword" class="flied-title-custom">현재 비밀번호 입력</label>
            <input
              type="password"
              id="currentPassword"
              v-model="currentPassword"
              autocomplete="current-password"
              :readonly="confirmed"
              @keydown.enter.prevent="checkPresentPwd"
            />
            <button
              type="button"
              style="background-color: var(--sub-color1); color: white; border-radius: 4px; width: 18%; height: 40px; margin-right: 3px"
              :disabled="confirmed"
              @click="checkPresentPwd"
            >
              인증
            </button>
          </div>
          <div class="info-field-items newpw" style="justify-content: flex-start">
            <label class="flied-title"></label>
            <p :class="presentPwdOk ? 'pointblue' : 'pointRed'" style="font-size: 12px">{{ presentPwdMsg }}</p>
          </div>
          <div class="info-field-items newpw">
            <label for="newPassword" class="flied-title">새 비밀번호 입력</label>
            <input type="password" id="newPassword" v-model="newPassword" autocomplete="new-password" :disabled="!confirmed" @input="checkPwd" />
          </div>
          <div class="info-field-items newpw">
            <label for="newPasswordConfirm" class="flied-title">새 비밀번호 확인</label>
            <input type="password" id="newPasswordConfirm" v-model="newPasswordConfirm" autocomplete="new-password" :disabled="!confirmed" @input="checkPwd" />
          </div>
          <div class="info-field-items">
            <div class="pw-validation-area">
              <ul>
                <li>
                  <span class="pw-validation"><img :src="ruleIcon(pwRules.all)" alt="" /></span>
                  <span class="txt_box">
                    <span class="txt_items">비밀번호 보안도 <strong class="pointRed">{{ pwRules.all ? '강함' : '약함' }}</strong></span>
                    <span class="txt_items">( 4가지 체크 완료 시 V 표시 )</span>
                  </span>
                </li>
                <li><span class="pw-validation"><img :src="ruleIcon(pwRules.r1)" alt="" /></span>1. 숫자, 기호, 영문자 포함</li>
                <li><span class="pw-validation"><img :src="ruleIcon(pwRules.r2)" alt="" /></span>2. 3개 이상 연속 문자/숫자 제외</li>
                <li><span class="pw-validation"><img :src="ruleIcon(pwRules.r3)" alt="" /></span>3. 아이디를 포함할 수 없음</li>
                <li><span class="pw-validation"><img :src="ruleIcon(pwRules.r4)" alt="" /></span>4. 최소 9~20자</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="btn-box">
        <button type="submit" class="blueBtn u-confirm">
          확인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
      </div>
    </form>
  </section>
</template>

<style>
@import '/css/change-info.css';
@import '/css/change-pw.css';
@import '/css/join-inf2.css';
</style>

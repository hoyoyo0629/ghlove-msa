<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/http'

// AS-IS users/find-idpw.html(및 Thymeleaf 버전 find-idpw.html) 재현. 아이디찾기 탭은
// "본인인증" 버튼 하나, 비밀번호찾기 탭은 이름+아이디 입력 후 "본인인증" 버튼이다. 실제
// 입력(이름/휴대폰/인증번호)은 본인인증 모달(auth.vue의 #authModal 마크업) 안에서 받는다.
// 이 프로젝트엔 유료 본인인증 게이트웨이가 없어 두 카드(금융인증서/휴대폰) 중 어느 쪽을
// 눌러도 같은 대체인증(이름+휴대폰 일치 확인 후 인증번호 발송/검증)으로 넘어간다
// - Thymeleaf 버전이 이미 JSON(@ResponseBody)으로 열어둔 /find-idpw/* 엔드포인트를
// form-urlencoded로 그대로 재사용한다(새 API 불필요).
const router = useRouter()
const NOT_FOUND_MESSAGE = '일치하는 회원 정보를 찾을 수 없습니다.'

const tab = ref('id')
const idStep = ref(1) // 1=검색, 3=결과, 0=notfound
const pwStep = ref(1) // 1=검색, 2=재설정, 0=notfound

const pwUserName = ref('')
const pwLoginId = ref('')
const idResultLoginId = ref('')

const authModalOpen = ref(false)
const authStep = ref('choice') // choice, form, code
const authFlow = ref(null) // 'id' | 'pw'
const authUserName = ref('')
const authPhoneNumber = ref('')
const authCode = ref('')
const authError = ref('')
const authVerifyError = ref('')
const authSentMessage = ref('')
const authDevCodeHint = ref('')

const pwNewPassword = ref('')
const pwNewPasswordConfirm = ref('')
const pwResetError = ref('')
const pwRules = ref({ all: false, r1: false, r2: false, r3: false, r4: false })

function setTab(t) {
  tab.value = t
}
function resetId() {
  idStep.value = 1
}
function resetPw() {
  pwStep.value = 1
}

function openAuthModal(flow) {
  if (flow === 'pw' && (!pwUserName.value || !pwLoginId.value)) {
    alert('이름과 아이디를 입력하세요.')
    return
  }
  authFlow.value = flow
  authStep.value = 'choice'
  authUserName.value = ''
  authPhoneNumber.value = ''
  authCode.value = ''
  authError.value = ''
  authVerifyError.value = ''
  authDevCodeHint.value = ''
  authModalOpen.value = true
}
function closeAuthModal() {
  authModalOpen.value = false
}
function startMockAuth() {
  authStep.value = 'form'
}

async function authSendCode() {
  authError.value = ''
  try {
    let res
    if (authFlow.value === 'id') {
      res = await api.postUrlEncoded('member', '/find-idpw/id/send-code', {
        userName: authUserName.value,
        phoneNumber: authPhoneNumber.value,
      })
    } else {
      res = await api.postUrlEncoded('member', '/find-idpw/pw/send-code', {
        loginId: pwLoginId.value,
        userName: pwUserName.value,
        phoneNumber: authPhoneNumber.value,
      })
    }
    if (res.status === 'CODE_SENT') {
      authSentMessage.value = res.message
      authDevCodeHint.value = res.devCode ? `개발모드(휴대폰 인증 미연동): 인증번호는 ${res.devCode} 입니다.` : ''
      authStep.value = 'code'
    } else if (res.message === NOT_FOUND_MESSAGE) {
      closeAuthModal()
      if (authFlow.value === 'id') idStep.value = 0
      else pwStep.value = 0
    } else {
      authError.value = res.message || '오류가 발생했습니다.'
    }
  } catch (e) {
    authError.value = e.message
  }
}

async function authVerify() {
  authVerifyError.value = ''
  try {
    const url = authFlow.value === 'id' ? '/find-idpw/id/verify' : '/find-idpw/pw/verify'
    const res = await api.postUrlEncoded('member', url, { code: authCode.value })
    if (res.status === 'OK') {
      closeAuthModal()
      if (authFlow.value === 'id') {
        idResultLoginId.value = res.loginId
        idStep.value = 3
      } else {
        pwStep.value = 2
      }
    } else {
      authVerifyError.value = res.message || '오류가 발생했습니다.'
    }
  } catch (e) {
    authVerifyError.value = e.message
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
  const pwd = pwNewPasswordConfirm.value
  const password = pwNewPassword.value
  const hasDigit = /[0-9]/.test(pwd)
  const hasLetter = /[a-zA-Z]/.test(pwd)
  const hasSymbol = /[{}[\]/?.,;:|)*~`!^\-_+<>@#$%&\\=('"]/.test(pwd)
  const rule1 = hasDigit && hasLetter && hasSymbol
  const rule2 = pwd !== '' && password !== '' && !isContinued(pwd) && !/(\w)\1\1/.test(pwd)
  const rule3 = pwd.indexOf(pwLoginId.value) === -1
  const rule4 = pwd.length >= 9 && pwd.length <= 20
  pwRules.value = { r1: rule1, r2: rule2, r3: rule3, r4: rule4, all: rule1 && rule2 && rule3 && rule4 }
}

async function pwReset() {
  pwResetError.value = ''
  try {
    const res = await api.postUrlEncoded('member', '/find-idpw/pw/reset', {
      newPassword: pwNewPassword.value,
      newPasswordConfirm: pwNewPasswordConfirm.value,
    })
    if (res.status === 'OK') {
      router.push('/mypage')
    } else {
      pwResetError.value = res.message || '오류가 발생했습니다.'
    }
  } catch (e) {
    pwResetError.value = e.message
  }
}

function ruleIcon(ok) {
  return ok ? '/images/icon/pw-available.png' : '/images/icon/pw-unavailable.png'
}
</script>

<template>
  <section class="find-idpw center" id="contents">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        로그인
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        아이디/비밀번호 찾기
      </span>
      <h2 class="page-title-txt">아이디 / 비밀번호 찾기</h2>
    </div>

    <div class="find-tab-area">
      <div class="tab-container">
        <div class="tab_tit">
          <ul class="tab-menu">
            <li><button type="button" :class="{ active: tab === 'id' }" @click="setTab('id')">아이디찾기</button></li>
            <li><button type="button" :class="{ active: tab === 'pw' }" @click="setTab('pw')">비밀번호찾기</button></li>
          </ul>
        </div>

        <div class="tab-content">
          <!-- ===================== 아이디 찾기 ===================== -->
          <div v-show="tab === 'id'" class="tab-pane">
            <div v-if="idStep === 1">
              <div class="form_wrap_line">
                <p>금융인증서와 본인 명의의 휴대폰 중<br /><strong>선택하여 인증을 진행합니다.</strong></p>
                <div class="btn-group">
                  <button type="button" class="blueBtn u-confirm" @click="openAuthModal('id')">본인인증</button>
                </div>
                <div class="line top-nomargin"></div>
                <ul class="needs-validation">
                  <li>아이디찾기는 본인 명의로 등록된 휴대폰번호만 가능합니다.</li>
                  <li>본인인증을 진행하지 않은 계정은 아이디 찾기가 불가합니다.</li>
                  <li>계정을 찾을 수 없는 경우 고향사랑e음 고객센터로 문의 바랍니다.</li>
                </ul>
              </div>
            </div>

            <div v-if="idStep === 3">
              <div class="find_result">
                <p><strong>회원님의 아이디</strong>를<br />확인하세요.</p>
                <div class="resultID-box">아이디 : <strong>{{ idResultLoginId }}</strong></div>
                <div class="btn-box many">
                  <button type="button" class="blueBtn cancellation" @click="router.push('/login')">
                    로그인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                  <button type="button" class="blueBtn joinBtn" @click="setTab('pw')">
                    비밀번호 찾기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                </div>
              </div>
            </div>

            <div v-if="idStep === 0">
              <div class="find_result">
                <p><strong>회원가입을 하시면 다양한 혜택</strong>을<br />받으실 수 있습니다.</p>
                <a href="javascript:void(0);" class="refind" @click="resetId">아이디 찾기 다시 시도</a>
                <div class="resultID-box">아이디가 존재하지 않습니다.</div>
                <div class="btn-box many">
                  <button type="button" class="blueBtn cancellation" @click="router.push('/login')">
                    로그인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                  <button type="button" class="blueBtn joinBtn" @click="router.push('/signup')">
                    회원가입<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- ===================== 비밀번호 찾기 ===================== -->
          <div v-show="tab === 'pw'" class="tab-pane">
            <div v-if="pwStep === 1">
              <div class="form_wrap_line">
                <p><strong>본인인증</strong>을 통해 비밀번호를<br />변경하실 수 있습니다.</p>
                <div class="line"></div>
                <div class="info-field-group">
                  <div class="info-field-items">
                    <label for="pwUserName">이름</label>
                    <span class="form-field"><input type="text" id="pwUserName" v-model="pwUserName" required /></span>
                  </div>
                  <div class="info-field-items">
                    <label for="pwLoginId">아이디</label>
                    <span class="form-field"><input type="text" id="pwLoginId" v-model="pwLoginId" autocomplete="off" required /></span>
                  </div>
                </div>
                <div class="line"></div>
                <div class="btn-group nomargin">
                  <button type="button" class="blueBtn u-confirm" @click="openAuthModal('pw')">본인인증</button>
                </div>
              </div>
            </div>

            <div v-if="pwStep === 2">
              <form class="needs-validation" @submit.prevent="pwReset">
                <div class="form_wrap_line info-field-items">
                  <div class="form-group">
                    <ul><li>인증이 완료 되었습니다. 비밀번호를 초기화 합니다.</li></ul>
                    <p><strong>새로운 비밀번호</strong>를<br />입력해 주세요.</p>
                  </div>
                  <div class="line"></div>
                  <div class="info-field-items">
                    <label for="pwNewPassword">새 비밀번호 입력</label>
                    <span class="form-field"><input type="password" id="pwNewPassword" v-model="pwNewPassword" @input="checkPwd" required /></span>
                  </div>
                  <div class="info-field-items">
                    <label for="pwNewPasswordConfirm">새 비밀번호 확인</label>
                    <span class="form-field"><input type="password" id="pwNewPasswordConfirm" v-model="pwNewPasswordConfirm" @input="checkPwd" required /></span>
                  </div>
                  <div class="info-field-items vali-group">
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
                  <p class="error" v-if="pwResetError">{{ pwResetError }}</p>
                  <div class="btn-group">
                    <button type="submit" class="blueBtn u-confirm">확인</button>
                  </div>
                </div>
              </form>
            </div>

            <div v-if="pwStep === 0">
              <div class="find_result">
                <p>회원 가입을 원하시면<br /><strong>회원가입 페이지로 이동해주세요.</strong></p>
                <a href="javascript:void(0);" class="refind" @click="resetPw">다시시도하기</a>
                <div class="resultID-box">입력하신 정보와 일치하는 계정이 없습니다.<br />정보를 다시 확인해 주세요.</div>
                <div class="btn-box many">
                  <button type="button" class="blueBtn cancellation" @click="router.push('/')">
                    확인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                  <button type="button" class="blueBtn joinBtn" @click="router.push('/signup')">
                    회원가입<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- 본인인증 모달 -->
  <div class="black-bg" :class="{ show: authModalOpen }" id="authModal" tabindex="0">
    <div class="modal-overlayer join-complete">
      <img src="/images/icon/cli-icon_btn-close-modal.png" alt="닫기" class="closeModal" @click="closeAuthModal" />
      <div class="overlayer-header">본인인증</div>
      <div class="overlayer-body">
        <div class="overlayer-body-wrap">
          <div v-if="authStep === 'choice'">
            <h2>아래의 본인 인증 수단 중<br />가능한 방식을 선택해서 인증을 진행하시기 바랍니다.</h2>
            <p>휴대폰 인증은 본인 소유의 휴대폰만 해당 됩니다.</p>
            <div class="authentication-area">
              <div class="authentication-box financ">
                <div class="auth_tit"><h3><span>금융</span> <span>인증서</span></h3></div>
                <button class="formBtn financ" type="button" @click="startMockAuth">인증하기</button>
              </div>
              <div class="authentication-box">
                <div class="auth_tit">
                  <h3>휴대폰</h3>
                  <p class="s-txt">본인 명의로 등록된 휴대폰으로<br />본인 인증 하기</p>
                </div>
                <button class="formBtn financ" type="button" @click="startMockAuth">인증하기</button>
              </div>
            </div>
          </div>

          <div v-if="authStep === 'form'">
            <h2>본인확인</h2>
            <div class="form_wrap_line certificate">
              <div class="info-field-items" v-if="authFlow === 'id'">
                <label for="authUserName">이름</label>
                <span class="form-field"><input type="text" id="authUserName" v-model="authUserName" /></span>
              </div>
              <div class="info-field-items">
                <label for="authPhoneNumber">휴대폰번호</label>
                <span class="form-field"><input type="text" id="authPhoneNumber" v-model="authPhoneNumber" placeholder="010-0000-0000" /></span>
              </div>
              <p class="error" v-if="authError">{{ authError }}</p>
              <div class="btn-group">
                <button type="button" class="blueBtn u-confirm" @click="authSendCode">인증번호 받기</button>
              </div>
            </div>
          </div>

          <div v-if="authStep === 'code'">
            <h2>인증번호 확인</h2>
            <div class="form_wrap_line certificate">
              <p>{{ authSentMessage }}</p>
              <p v-if="authDevCodeHint" style="color: #eb474d; font-size: 13px; text-align: center">{{ authDevCodeHint }}</p>
              <div class="info-field-items">
                <label for="authCode">인증번호</label>
                <span class="form-field"><input type="text" id="authCode" v-model="authCode" maxlength="6" placeholder="인증번호 6자리" /></span>
              </div>
              <p class="error" v-if="authVerifyError">{{ authVerifyError }}</p>
              <div class="btn-group">
                <button type="button" class="blueBtn u-confirm" @click="authVerify">확인</button>
              </div>
            </div>
          </div>

          <div class="btn-box">
            <button type="button" class="blueBtn cancellation" @click="closeAuthModal">
              취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
@import '/css/join-inf2.css';
@import '/css/login-idse.css';
@import '/css/authentication-modal.css';
</style>

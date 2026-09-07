<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/http'
import { useAuthStore } from '../stores/auth'

// AS-IS users/join.html(및 Thymeleaf 버전 signup.html)의 3단계(약관동의/본인인증/정보입력)
// 위저드를 그대로 재현한다. 본인인증은 실제 연계가 없는 환경이라 로그인 화면과 동일하게
// dev-bypass로 건너뛴다(AS-IS에는 없는, 이 프로젝트가 테스트를 위해 추가한 우회).
const auth = useAuthStore()
const router = useRouter()

const STEP_TITLES = { 1: '회원가입', 2: '본인인증', 3: '회원 정보 입력' }
const step = ref(1)
const errorMessage = ref('')

const terms = reactive({
  agreeTerms: false,
  agreePrivacy: false,
  agreeAd: false,
  adSms: false,
  adEmail: false,
  adPbanc: false,
  adKakao: false,
})
const allChecked = computed({
  get: () => terms.agreeTerms && terms.agreePrivacy && terms.agreeAd && terms.adSms && terms.adEmail && terms.adPbanc && terms.adKakao,
  set: (v) => {
    terms.agreeTerms = v
    terms.agreePrivacy = v
    terms.agreeAd = v
    terms.adSms = v
    terms.adEmail = v
    terms.adPbanc = v
    terms.adKakao = v
  },
})
function onAdParentChange() {
  terms.adSms = terms.agreeAd
  terms.adEmail = terms.agreeAd
  terms.adPbanc = terms.agreeAd
  terms.adKakao = terms.agreeAd
}
function onAdChildChange() {
  terms.agreeAd = terms.adSms && terms.adEmail && terms.adPbanc && terms.adKakao
}

function goToStep(n) {
  if (n === 2) {
    if (!terms.agreeTerms) { alert('이용약관에 동의해주세요'); return }
    if (!terms.agreePrivacy) { alert('개인정보 수집·이용에 동의해주세요'); return }
  }
  step.value = n
  window.scrollTo(0, 0)
}

const form = reactive({
  userName: '',
  birthday: '',
  loginId: '',
  password: '',
  passwordConfirm: '',
  address: '',
  addressDetail: '',
})
const phoneCode = ref('010')
const phoneMid = ref('')
const phoneLast = ref('')
const emailFirst = ref('')
const emailDomain = ref('naver.com')
const emailAddressCustom = ref('')

const idChecked = ref(false)
const idCheckMsg = ref('')
const idCheckOk = ref(false)
function onIdChange() {
  idChecked.value = false
  idCheckMsg.value = ''
}
async function checkLoginId() {
  if (!/^[a-z0-9_]{6,20}$/.test(form.loginId)) {
    idCheckMsg.value = '아이디는 영문 소문자/숫자/밑줄(_)만 사용해 6~20자로 입력해 주세요.'
    idCheckOk.value = false
    return
  }
  const data = await api.get('member', `/api/check-login-id?loginId=${encodeURIComponent(form.loginId)}`)
  idChecked.value = data.available
  idCheckOk.value = data.available
  idCheckMsg.value = data.available ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.'
}

const pwVisible = ref(false)

const pwRules = computed(() => {
  const pwd = form.passwordConfirm
  const hasDigit = /[0-9]/.test(pwd)
  const hasLetter = /[a-zA-Z]/.test(pwd)
  const hasSymbol = /[{}[\]/?.,;:|)*~`!^\-_+<>@#$%&\\=('"]/.test(pwd)
  const rule1 = hasDigit && hasLetter && hasSymbol
  const rule2 = !isContinued(pwd) && !/(\w)\1\1/.test(pwd)
  const rule3 = form.loginId === '' || pwd.indexOf(form.loginId) === -1
  const rule4 = pwd.length >= 9 && pwd.length <= 20
  return { rule1, rule2, rule3, rule4, allOk: rule1 && rule2 && rule3 && rule4 }
})
function isContinued(str) {
  for (let i = 0; i < str.length - 2; i++) {
    const a = str.charCodeAt(i)
    const b = str.charCodeAt(i + 1)
    const c = str.charCodeAt(i + 2)
    if ((b - a === 1 && c - b === 1) || (b - a === -1 && c - b === -1)) return true
  }
  return false
}
function ruleIcon(ok) {
  return ok ? '/images/icon/pw-available.png' : '/images/icon/pw-unavailable.png'
}

async function onSubmit() {
  errorMessage.value = ''
  if (!idChecked.value) { alert('아이디 중복확인을 해주세요.'); return }
  if (form.password !== form.passwordConfirm) { alert('비밀번호와 비밀번호 확인이 일치하지 않습니다.'); return }

  const domain = emailDomain.value || emailAddressCustom.value
  const payload = {
    ...form,
    phoneNumber: phoneCode.value + phoneMid.value + phoneLast.value,
    email: `${emailFirst.value}@${domain}`,
  }
  try {
    const data = await auth.signup(payload)
    if (data.status !== 'OK') {
      errorMessage.value = data.message
      return
    }
    router.push('/')
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section class="joind">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        회원가입
      </span>
      <h2 class="page-title-txt">{{ STEP_TITLES[step] }}</h2>
    </div>

    <div class="steps-box">
      <div class="agreement" :class="{ on: step === 1 }"><div class="steps-txt">약관<br />동의</div><div class="icon-bg"></div></div>
      <div class="authentication" :class="{ on: step === 2 }"><div class="steps-txt">본인<br />인증</div><div class="icon-bg"></div></div>
      <div class="enter-info" :class="{ on: step === 3 }"><div class="steps-txt">정보<br />입력</div><div class="icon-bg"></div></div>
      <div class="sign-up-complete"><div class="steps-txt">가입<br />완료</div><div class="icon-bg"></div></div>
    </div>

    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <div v-show="step === 1" class="step-panel">
      <fieldset>
        <h3 class="sr-only">약관 동의</h3>
        <div class="accept-terms-area step1">
          <div class="accept-terms-item all_check">
            <div class="accept-terms_wrap">
              <div class="accept-check step1">
                <input type="checkbox" id="all_check" v-model="allChecked" />
                <label for="all_check">전체약관 동의하기</label>
              </div>
            </div>
            <div class="line nomargin"></div>
          </div>

          <div class="accept-terms-item agree_check">
            <div class="accept-terms_wrap">
              <div class="accept-check">
                <input type="checkbox" id="agreeTerms" v-model="terms.agreeTerms" />
                <label for="agreeTerms">이용약관 동의 <strong>(필수)</strong></label>
              </div>
              <router-link to="/policy/auth" target="_blank" title="새창 열림" class="moreView"><span class="sr-only">이용약관 </span>자세히 보기</router-link>
            </div>
            <div class="terms-box" tabindex="0">
              <div class="terms-article">
                <h3>제1조 (목적)</h3>
                <p class="article-txt">이 약관은 고향사랑e음(이하 "회사")이 제공하는 고향사랑기부제 관련 서비스의 이용조건 및 절차, 회원과 회사의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.</p>
                <h3>제2조 (회원가입)</h3>
                <p class="article-txt">회원가입은 이용자가 약관 내용에 동의를 하고 회원정보를 기입하여 가입신청을 한 후 회사가 이러한 신청에 대하여 승낙함으로써 체결됩니다.</p>
              </div>
            </div>
          </div>

          <div class="accept-terms-item agree_check">
            <div class="accept-terms_wrap">
              <div class="accept-check">
                <input type="checkbox" id="agreePrivacy" v-model="terms.agreePrivacy" />
                <label for="agreePrivacy">개인정보 수집·이용 동의 <strong>(필수)</strong></label>
              </div>
              <router-link to="/policy/privacy" target="_blank" title="새창 열림" class="moreView"><span class="sr-only">개인정보처리방침 </span>자세히 보기</router-link>
            </div>
            <div class="terms-box" tabindex="0">
              <div class="terms-article">
                <h3>수집 항목</h3>
                <p class="article-txt">아이디, 비밀번호, 이름, 생년월일, 이메일, 휴대폰번호, 주소</p>
                <h3>수집 및 이용 목적</h3>
                <p class="article-txt">회원 식별 및 가입의사 확인, 고향사랑기부 신청·처리, 기부확인증 발급, 답례품 배송</p>
              </div>
            </div>
          </div>

          <div class="accept-terms-item agree_check">
            <div class="accept-terms_wrap" style="padding-bottom: 0">
              <div class="accept-check">
                <input type="checkbox" id="agreeAd" v-model="terms.agreeAd" @change="onAdParentChange" />
                <label for="agreeAd">알림서비스 수신 동의 <strong>(선택)</strong></label>
              </div>
            </div>
            <div class="ad-info-agree">
              <div class="agree-accept-check">
                <input type="checkbox" id="adSms" v-model="terms.adSms" @change="onAdChildChange" />
                <label for="adSms">국민비서 <strong>(선택)</strong></label>
              </div>
              <div class="agree-accept-check">
                <input type="checkbox" id="adEmail" v-model="terms.adEmail" @change="onAdChildChange" />
                <label for="adEmail">Email <strong>(선택)</strong></label>
              </div>
              <div class="agree-accept-check">
                <input type="checkbox" id="adPbanc" v-model="terms.adPbanc" @change="onAdChildChange" />
                <label for="adPbanc">SMS <strong>(선택)</strong></label>
              </div>
              <div class="agree-accept-check">
                <input type="checkbox" id="adKakao" v-model="terms.adKakao" @change="onAdChildChange" />
                <label for="adKakao">알림톡 <strong>(선택)</strong></label>
              </div>
            </div>
          </div>
        </div>

        <div class="btn-box many">
          <button type="button" class="blueBtn cancellation" @click="router.push('/')">취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
          <button type="button" class="blueBtn u-confirm" @click="goToStep(2)">다음<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
        </div>
      </fieldset>
    </div>

    <div v-show="step === 2" class="step-panel">
      <fieldset>
        <h3 class="sr-only">본인 인증</h3>
        <div class="accept-terms-area">
          <div class="accept-terms-item">
            <p class="info-txt">
              아래의 본인 인증 수단중 가능한 방식을 선택해서 인증을 진행하시기 바랍니다.<br />
              휴대폰 인증은 본인 소유의 휴대폰만 해당 됩니다.<br />
              국내 전자금융거래서비스를 가입한 해외 체류중인 국민(재외국민)도 금융인증서를 활용하여 고향사랑e음 이용이 가능합니다.
            </p>
          </div>
          <div class="auth_wrap">
            <div class="authentication-area financ">
              <div class="authentication-box">
                <div class="auth_tit">
                  <h3>금융인증서</h3>
                  <p class="s-txt"><span>금융기관에 등록된</span> <span>금융인증서로 본인 인증 하기</span></p>
                  <p class="pointRed"><span>※ 해외 체류중인 국민</span> <span> (재외국민) 활용 가능</span></p>
                </div>
                <button type="button" class="formBtn financ" title="새 창 알림" @click="location.href='http://localhost:8081/signup/finance-cert'">인증하기</button>
              </div>
            </div>
            <div class="authentication-area mobi">
              <div class="authentication-box">
                <div class="auth_tit">
                  <h3>휴대폰</h3>
                  <p class="s-txt">본인 명의로 등록된 휴대폰으로<br /> 본인 인증 하기</p>
                </div>
                <button type="button" class="formBtn financ" title="새 창 알림" @click="location.href='http://localhost:8081/signup/mobile-auth'">인증하기</button>
              </div>
            </div>
          </div>
        </div>

        <p class="dev-bypass">
          이 환경에는 실제 본인인증 연계가 열려있지 않습니다 -
          <button type="button" @click="goToStep(3)">본인인증 없이 진행 (테스트용)</button>
        </p>

        <div class="btn-box">
          <button type="button" class="blueBtn cancellation" @click="router.push('/')">취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
        </div>
      </fieldset>
    </div>

    <div v-show="step === 3" class="step-panel">
      <form id="signupForm" @submit.prevent="onSubmit">
        <fieldset>
          <div class="add-info-area">
            <div class="lable-field">
              <h3><span>회원정보&nbsp;</span><span>입력</span></h3>
              <p class="essential"><span>필수*</span> <span>입력정보</span></p>
            </div>
            <div class="info-field">
              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="memberLevel" class="flied-title">회원구분<span class="essential"> *</span></label>
                  <span class="form-field"><input type="text" id="memberLevel" value="일반회원" readonly tabindex="-1" /></span>
                </div>
                <div class="info-field-items">
                  <label for="userName" class="flied-title">이름<span class="essential"> *</span></label>
                  <span class="form-field"><input type="text" id="userName" v-model="form.userName" required /></span>
                </div>
                <div class="info-field-items">
                  <label for="birthday" class="flied-title">생년월일<span class="essential"> *</span></label>
                  <span class="form-field"><input type="date" id="birthday" v-model="form.birthday" /></span>
                </div>
              </div>

              <div class="line"></div>

              <div class="info-field-group">
                <div class="info-field-items">
                  <label for="loginId" class="flied-title">아이디 <span class="essential"> *</span></label>
                  <div class="form-field">
                    <div class="m-field">
                      <input type="text" id="loginId" v-model="form.loginId" autocomplete="off" required @input="onIdChange" />
                      <button id="userIdBtn" class="formBtn" type="button" @click="checkLoginId">중복확인</button>
                    </div>
                    <div class="s-txt">아이디는 영문 소문자만 허용, 6~20자 내외 <span>(특수문자 제외, _ 언더바 가능)</span></div>
                    <div class="id-check-msg" :class="{ ok: idCheckOk, fail: !idCheckOk && idCheckMsg }">{{ idCheckMsg }}</div>
                  </div>
                </div>
                <div>
                  <div class="info-field-items">
                    <label for="password" class="flied-title">비밀번호<span class="essential"> * </span></label>
                    <div class="form-field m-field">
                      <input :type="pwVisible ? 'text' : 'password'" id="password" v-model="form.password" autocomplete="off" required />
                      <button type="button" class="formBtn" @click="pwVisible = !pwVisible">표시</button>
                    </div>
                  </div>
                  <div class="info-field-items">
                    <label for="passwordConfirm" class="flied-title">비밀번호 확인<span class="essential"> * </span></label>
                    <div class="form-field">
                      <input type="password" id="passwordConfirm" v-model="form.passwordConfirm" autocomplete="new-password" required />
                      <div class="pw-validation-area">
                        <ul>
                          <li>
                            <span class="pw-validation"><img :src="ruleIcon(pwRules.allOk)" alt="" /></span>
                            <span class="txt_box">
                              <span class="txt_items">비밀번호 보안도 <strong class="pointRed">{{ pwRules.allOk ? '강함' : '약함' }}</strong></span>
                              <span class="txt_items">(4가지 체크 완료 시 V 표시)</span>
                            </span>
                          </li>
                          <li><span class="pw-validation"><img :src="ruleIcon(pwRules.rule1)" alt="" /></span>1. 숫자, 기호, 영문자 포함</li>
                          <li><span class="pw-validation"><img :src="ruleIcon(pwRules.rule2)" alt="" /></span>2. 3개 이상 연속 문자/숫자 제외</li>
                          <li><span class="pw-validation"><img :src="ruleIcon(pwRules.rule3)" alt="" /></span>3. 아이디를 포함할 수 없음</li>
                          <li><span class="pw-validation"><img :src="ruleIcon(pwRules.rule4)" alt="" /></span>4. 최소 9~20자</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="line"></div>

              <div class="info-field-group">
                <div class="info-field-items userNum">
                  <span class="flied-title">휴대폰번호<span class="essential"> *</span></span>
                  <span class="form-field">
                    <select v-model="phoneCode" aria-label="휴대폰번호 앞자리를 선택하세요">
                      <option v-for="c in ['010', '011', '016', '017', '018', '019']" :key="c" :value="c">{{ c }}</option>
                    </select>
                    <span class="s-txt">-</span>
                    <input type="text" v-model="phoneMid" maxlength="4" inputmode="numeric" aria-label="휴대폰번호 가운데 자리" />
                    <span class="s-txt">-</span>
                    <input type="text" v-model="phoneLast" maxlength="4" inputmode="numeric" aria-label="휴대폰번호 마지막 4자리" />
                  </span>
                </div>
                <div class="info-field-items userEmail">
                  <span class="flied-title">이메일주소<span class="essential"> * </span></span>
                  <span class="form-field">
                    <span class="m-field">
                      <input type="text" v-model="emailFirst" aria-label="이메일 주소 @앞자리를 입력하세요" />
                      <span class="s-txt">@</span>
                    </span>
                    <input v-model="emailAddressCustom" type="text" :style="{ display: emailDomain === '' ? '' : 'none' }" aria-label="이메일 도메인 직접 입력" />
                    <select v-model="emailDomain" aria-label="이메일 주소 @뒷자리를 선택하세요">
                      <option value="naver.com">naver.com</option>
                      <option value="gmail.com">gmail.com</option>
                      <option value="daum.net">daum.net</option>
                      <option value="hanmail.net">hanmail.net</option>
                      <option value="nate.com">nate.com</option>
                      <option value="">직접입력</option>
                    </select>
                  </span>
                </div>
                <div class="info-field-items">
                  <span class="flied-title">주소</span>
                  <div class="form-field">
                    <input type="text" v-model="form.address" placeholder="주소" />
                    <input type="text" v-model="form.addressDetail" placeholder="상세주소" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="btn-box many">
            <button type="button" class="blueBtn cancellation" @click="goToStep(1)">이전<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
            <button type="submit" class="blueBtn u-confirm">회원등록<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
          </div>
        </fieldset>
      </form>
    </div>
  </section>
</template>

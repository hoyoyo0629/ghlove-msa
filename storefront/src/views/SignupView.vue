<script setup>
import { modalAlert, modalConfirm } from '../composables/useModal'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/http'
import { useKakaoCert } from '../composables/useKakaoCert'
import { useAuthStore } from '../stores/auth'
import { loadDaumPostcode } from '../utils/daumPostcode'

// AS-IS users/join.html(및 Thymeleaf 버전 signup.html)의 3단계(약관동의/본인인증/정보입력)
// 위저드를 그대로 재현한다. 본인인증은 실제 연계가 없는 환경이라 로그인 화면과 동일하게
// dev-bypass로 건너뛴다(AS-IS에는 없는, 이 프로젝트가 테스트를 위해 추가한 우회).
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const STEP_TITLES = { 1: '회원가입', 2: '본인인증', 3: '회원 정보 입력' }
const step = ref(1)
const errorMessage = ref('')

const terms = reactive({
  agreeTerms: false,
  disagreeTerms: false,
  agreePrivacy: false,
  disagreePrivacy: false,
  agreeAd: false,
  adSms: false,
  adEmail: false,
  adPbanc: false,
  adKakao: false,
})

// AS-IS agree.vue getPolicyInfo() - 약관동의 요약 박스 본문은 admin 약관관리(OP_POLICY)가
// 원본이다(체크박스 라벨 문구도 AS-IS는 하드코딩이 아니라 이 title을 그대로 쓴다).
// "알림서비스 수신 동의" 박스는 AS-IS도 DB가 아니라 agree.vue에 하드코딩된 고정 문구라
// 그 원문을 그대로 옮긴다(동의 체크박스 4개 - 국민비서/Email/SMS/알림톡 - 는 이미 있던 것).
const policyContent = reactive({ terms: null, collectionAgree: null })
const AD_INFO_CONTENT = `<p><span style="font-family: 나눔고딕; letter-spacing: 0pt; font-weight: bold; font-size: 14pt;">■</span><span lang="EN-US" style="font-weight: bold; font-size: 11pt;">&nbsp;<font face="나눔고딕">알림서비스 수신 동의</font></span>&nbsp;</p><p class="0" style="line-height:146%;margin-left:18.9pt;text-indent:-18.9pt;margin-top:2.0pt;text-autospace:none;"></p><div><font face="나눔고딕"><span style="font-size: 13.3333px;">&nbsp;고향사랑e음에서 제공하는 유익한 홍보성 정보를 SMS나 이메일 또는 카카오톡으로 받아 보실 수 있습니다.</span></font></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;">단, 주요 정책과 관련된 내용은 수신 동의 여부와 관계없이 발송됩니다.</span></font></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;">선택 약관에 동의하지 않으셔도 회원가입은 가능하며, 회원가입 후 <a href="/mypage/profile" target="_blank" title="새창 열림" rel="noopener" style="text-decoration: underline; color: blue; "><strong>마이페이지 &gt; 회원정보수정</strong></a>에서 언제든지 수신여부를 변경하실 수 있습니다.</span></font></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;"></span></font></div><p></p><div><span style="font-family: 나눔고딕; letter-spacing: 0pt; font-weight: bold; font-size: 14pt;"><br></span></div><div><span style="font-family: 나눔고딕; letter-spacing: 0pt; font-weight: bold; font-size: 14pt;"><span style="letter-spacing: 0pt; font-family: 나눔고딕; font-weight: bold; font-size: 14pt;">■</span><span lang="EN-US" style="font-weight: bold; font-size: 11pt;"><span style="font-size: 14pt;">&nbsp;</span>국민비서 알림서비스</span><br></span></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;">&nbsp;알림서비스는 국민비서 회원에게 제공되니 회원가입 해 주시기 바랍니다.</span></font></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;"><a href="https://www.ips.go.kr/pot/forwardMain.do" target="_blank" title="새창 열림" rel="noopener" style="text-decoration: underline; color: blue; "><strong>국민비서 가입하기</strong></a> 국민비서 홈 &gt; 알림설정(기타-고향사랑e음 안내 알림 선택)</span></font></div><div><font face="나눔고딕"><span style="font-size: 13.3333px;">※ 알림서비스 설정 후 다음날부터 되는 점 참고 바랍니다.</span></font></div>`
async function loadPolicyContent() {
  try {
    const data = await api.get('member', '/api/signup-policies')
    policyContent.terms = data.terms
    policyContent.collectionAgree = data.collectionAgree
  } catch (e) {
    // 조회 실패해도 가입 자체는 막지 않는다(AS-IS도 체크박스 검증만 하지 조회를 필수로 두지 않음)
  }
}
onMounted(loadPolicyContent)
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
    // AS-IS allCheck() - 전체동의를 누르면(켜든 끄든) 비동의 선택은 항상 비운다.
    terms.disagreeTerms = false
    terms.disagreePrivacy = false
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

// AS-IS agree.vue selected()/selectDisagree() - 동의·비동의는 서로소 토글이다(같은 항목을
// 동의로 체크하면 비동의가 풀리고, 비동의로 체크하면 동의가 풀린다). AS-IS는 두 체크박스를
// 서로 다른 배열(checkResult/disagreeResult)에 담아 상대 배열에서 값을 지우는 방식으로
// 구현했는데, TO-BE는 불리언 한 쌍이라 직접 상대를 끈다 - 결과는 동일하다.
function onAgreeTermsChange() {
  if (terms.agreeTerms) terms.disagreeTerms = false
}
function onDisagreeTermsChange() {
  if (terms.disagreeTerms) terms.agreeTerms = false
}
function onAgreePrivacyChange() {
  if (terms.agreePrivacy) terms.disagreePrivacy = false
}
function onDisagreePrivacyChange() {
  if (terms.disagreePrivacy) terms.agreePrivacy = false
}

/** 필수약관 2건 - 다음 단계로 넘어갈 때와 카카오톡/네이버 인증으로 넘어갈 때 모두 검사한다
 *  (AS-IS agree.vue nextStep()/submitKakao()/submitNaver()가 각각 같은 검사를 했다). */
function requiredTermsAgreed() {
  if (!terms.agreeTerms) { modalAlert('이용약관에 동의해주세요'); return false }
  if (!terms.agreePrivacy) { modalAlert('개인정보 수집·이용에 동의해주세요'); return false }
  return true
}

function goToStep(n) {
  if (n === 2 && !requiredTermsAgreed()) return
  step.value = n
  window.scrollTo(0, 0)
}

// AS-IS agree.vue submitKakao()/submitNaver() - 약관동의 단계에서 바로 카카오톡/네이버
// 인증으로 간편가입한다(본인인증·정보입력 단계를 건너뛴다).
//
// window.location을 명시적으로 쓴다 - Vue 템플릿 표현식의 `location`은 컴포넌트 인스턴스
// (_ctx.location)로 해석되어 undefined가 되므로 템플릿에서 직접 대입하면 동작하지 않는다.
function externalAuth(path) {
  window.location.href = '/member' + path
}

// 카카오는 인증서비스(카카오톡 지갑 본인인증)라 화면이 JS SDK로 인증창을 띄운다(AS-IS와 동일).
const kakaoCert = useKakaoCert()
function startKakaoJoin() {
  if (!requiredTermsAgreed()) return
  kakaoCert.start('JOIN')
}
// 네이버는 member가 리다이렉트를 전담하는 일반 OAuth라 진입점으로 넘기기만 한다. type=JOIN을
// 실어야 신규가입으로 끝났을 때 서버가 이 화면으로 되돌려준다(AS-IS code=JOIN_MEMBER).
function startNaverJoin() {
  if (!requiredTermsAgreed()) return
  externalAuth('/login/naver?type=JOIN')
}

// 가입완료 안내(AS-IS join.html의 code=JOIN_MEMBER 모달)가 열리는 경로는 두 가지다.
//  - 카카오 인증서비스: 이 화면으로 ?code=가 돌아오고, verify 응답이 JOIN_MEMBER인 경우
//  - 네이버/모의 인증: member가 ?joined=<provider>로 되돌려주는 경우
// 안내문에 쓰는 이름은 member ExternalLoginController.AUTH_NAMES와 같은 매핑이다 - 리다이렉트
// Location 헤더에는 한글을 담을 수 없어(헤더가 유실된다) provider 코드로만 오간다.
const AUTH_NAMES = { KAKAO: '카카오톡', NAVER: '네이버', ONEPASS: '디지털원패스', FINANCE_CERT: '금융인증서', ANYID: '간편인증' }
const joinedAuthName = ref('')
onMounted(async () => {
  const certResult = await kakaoCert.consumeCode(route, 'JOIN')
  if (certResult) {
    await auth.fetchMe()
    if (certResult.code === 'JOIN_MEMBER') {
      joinedAuthName.value = certResult.authName
    } else {
      router.push('/')
    }
    return
  }

  const joined = route.query.joined
  if (!joined) return
  await auth.fetchMe()
  if (!auth.loggedIn) return
  joinedAuthName.value = AUTH_NAMES[joined] || String(joined)
  // AS-IS history.replaceState - 새로고침해도 안내가 다시 뜨지 않게 쿼리를 지운다.
  window.history.replaceState({}, '', '/signup')
})

const form = reactive({
  userName: '',
  birthday: '',
  loginId: '',
  password: '',
  passwordConfirm: '',
  post: '',
  address: '',
  addressDetail: '',
})

// 주소찾기 - 회원정보수정/배송지와 동일한 다음 우편번호 위젯(utils/daumPostcode). AS-IS 회원가입은
// juso.go.kr 팝업이지만 이 프로젝트는 이미 daum.Postcode로 통일해 다른 화면과 동작을 맞춘다.
async function searchAddress() {
  await loadDaumPostcode()
  new window.daum.Postcode({
    oncomplete(data) {
      form.post = data.zonecode
      form.address = data.roadAddress || data.jibunAddress
    },
  }).open()
}
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
  if (!idChecked.value) { modalAlert('아이디 중복확인을 해주세요.'); return }
  if (form.password !== form.passwordConfirm) { modalAlert('비밀번호와 비밀번호 확인이 일치하지 않습니다.'); return }

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
                <input type="checkbox" id="agreeTerms" v-model="terms.agreeTerms" @change="onAgreeTermsChange" />
                <label for="agreeTerms">{{ policyContent.terms?.title || '이용약관 동의' }} <strong>(필수)</strong></label>
                <input class="disagreeInput" type="checkbox" id="disagreeUsePolicy" v-model="terms.disagreeTerms" @change="onDisagreeTermsChange" style="margin:0 0 0 20px" />
                <label for="disagreeUsePolicy">비동의</label>
              </div>
              <router-link to="/policy/auth" target="_blank" title="새창 열림" class="moreView"><span class="sr-only">이용약관 </span>자세히 보기</router-link>
            </div>
            <div class="terms-box" tabindex="0">
              <div class="terms-article" v-html="policyContent.terms?.content"></div>
            </div>
          </div>

          <div class="accept-terms-item agree_check">
            <div class="accept-terms_wrap">
              <div class="accept-check">
                <input type="checkbox" id="agreePrivacy" v-model="terms.agreePrivacy" @change="onAgreePrivacyChange" />
                <label for="agreePrivacy">{{ policyContent.collectionAgree?.title || '개인정보 수집·이용 동의' }} <strong>(필수)</strong></label>
                <input class="disagreeInput" type="checkbox" id="disagreeInfoPolicy" v-model="terms.disagreePrivacy" @change="onDisagreePrivacyChange" style="margin:0 0 0 20px" />
                <label for="disagreeInfoPolicy">비동의</label>
              </div>
              <router-link to="/policy/privacy" target="_blank" title="새창 열림" class="moreView"><span class="sr-only">개인정보처리방침 </span>자세히 보기</router-link>
            </div>
            <div class="terms-box" tabindex="0">
              <div class="terms-article" v-html="policyContent.collectionAgree?.content"></div>
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
            <div class="terms-box" tabindex="0">
              <div class="terms-article" v-html="AD_INFO_CONTENT"></div>
            </div>
          </div>
        </div>

        <div class="btn-box many">
          <button type="button" class="blueBtn cancellation" @click="router.push('/')">취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
          <button type="button" class="blueBtn u-confirm" @click="goToStep(2)">다음<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>

          <button type="button" class="btn-simple btn-simple--kakao" id="kakaoLoginBtn" title="카카오톡 인증 로그인" @click="startKakaoJoin">
            <div class="btn-simple__logo">
              <img src="/images/kakao/kakaotalk_symbol_screen.png" class="btn-simple__img" alt="" aria-hidden="true" />
            </div>
            <span class="btn-simple__txt">카카오톡 인증</span>
          </button>

          <button type="button" class="btn-simple btn-simple--naver" id="naverLoginBtn" title="네이버 인증 로그인" @click="startNaverJoin">
            <div class="btn-simple__logo">
              <img src="/images/new/naver_logo_2.png" style="height: 44px; border-radius: 8px" alt="" aria-hidden="true" />
            </div>
            <span class="btn-simple__txt">네이버 인증</span>
          </button>
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
                <button type="button" class="formBtn financ" title="새 창 알림" @click="externalAuth('/signup/finance-cert')">인증하기</button>
              </div>
            </div>
            <div class="authentication-area mobi">
              <div class="authentication-box">
                <div class="auth_tit">
                  <h3>휴대폰</h3>
                  <p class="s-txt">본인 명의로 등록된 휴대폰으로<br /> 본인 인증 하기</p>
                </div>
                <button type="button" class="formBtn financ" title="새 창 알림" @click="externalAuth('/signup/mobile-auth')">인증하기</button>
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
                <div class="info-field-items userAddress">
                  <span class="flied-title">주소</span>
                  <div class="form-field">
                    <div class="search-address m-field">
                      <input type="text" v-model="form.post" placeholder="우편번호" readonly />
                      <button type="button" class="formBtn" @click="searchAddress">주소찾기</button>
                    </div>
                    <div class="input-address">
                      <input type="text" v-model="form.address" placeholder="주소" readonly />
                      <input type="text" v-model="form.addressDetail" placeholder="상세주소" />
                    </div>
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

    <!-- AS-IS join.html의 가입완료 축하 모달 - 카카오톡/네이버 인증이 신규가입으로 끝났을
         때만(AS-IS code=JOIN_MEMBER) 열린다. 확인은 AS-IS closePopupKakao()와 동일하게
         기부하기 화면으로 보낸다. -->
    <div class="black-bg show" id="joinComplete" v-if="joinedAuthName && auth.me">
      <div class="overlayer join-complete">
        <div class="overlayer-header"></div>
        <div class="overlayer-body">
          <h2>고향사랑e음<br />{{ joinedAuthName }} 인증 로그인 통한 회원가입 완료</h2>
          <h2 style="color: red">{{ auth.me.userName }}회원님의 ID는 {{ auth.me.loginId }}입니다.</h2>
          <img src="/images/icon/cli-icon_join-complete.png" alt="가입을 축하합니다" />
          <div class="line"></div>
          <h2 class="s-txt">국민비서·SMS 수신동의</h2>
          <h2 class="s-txt">email 수신동의</h2>
          <p>미동의를 선택하실 경우 마이페이지 &gt; 동의 항목을</p>
          <p>미선택으로 체크하시기 바랍니다.</p>
          <div class="btn-box">
            <button type="button" class="blueBtn u-confirm" @click="router.push('/donate')">
              확인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

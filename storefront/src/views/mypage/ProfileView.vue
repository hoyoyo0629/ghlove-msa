<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { loadDaumPostcode } from '../../utils/daumPostcode'

// AS-IS users/modify.html(및 Thymeleaf 버전 profile.html) 재현. 이름/생년월일은 본인인증
// 연계가 없어 읽기전용, "개인정보 수정"(정적 안내뿐이라 아직 옛 member 서비스(8081) 화면
// 유지)과 달리 "비밀번호 변경"은 정식 라우트로 포팅됨.
const auth = useAuthStore()
const router = useRouter()

const profile = ref(null)
const errorMessage = ref('')
const successMessage = ref('')

const phoneCode = ref('010')
const phoneMid = ref('')
const phoneLast = ref('')
const emailFirst = ref('')
const emailDomain = ref('naver.com')
const emailAddressCustom = ref('')
const post = ref('')
const address = ref('')
const addressDetail = ref('')
const receiveEmail = ref(false)
const receiveSms = ref(false)
const receiveKakao = ref(false)

const addUpperLocgovCode = ref('')
const addLocgovCode = ref('')
const interestLocgovs = ref([])

const filteredAddLocgovs = computed(() =>
  (profile.value?.allLocgovs ?? []).filter((l) => !addUpperLocgovCode.value || l.upperLocgovCode === addUpperLocgovCode.value),
)

async function load() {
  const data = await api.get('member', '/api/profile')
  profile.value = data
  interestLocgovs.value = data.interestLocgovs
  post.value = data.post ?? ''
  address.value = data.address ?? ''
  addressDetail.value = data.addressDetail ?? ''
  receiveEmail.value = data.receiveEmail
  receiveSms.value = data.receiveSms
  receiveKakao.value = data.receiveKakao

  // 저장된 번호에 하이픈이 섞여 있는 기존 데이터(예: "010-1234-5678")도 있어 숫자만 남기고
  // 자른다 - AS-IS/Thymeleaf 버전은 원본 문자열을 그대로 잘라 하이픈이 중간에 끼면 깨졌었다.
  const phone = (data.phoneNumber ?? '').replace(/\D/g, '')
  if (phone.length >= 10) {
    phoneCode.value = phone.substring(0, 3)
    phoneMid.value = phone.substring(3, phone.length - 4)
    phoneLast.value = phone.substring(phone.length - 4)
  }
  const email = data.email ?? ''
  const at = email.indexOf('@')
  if (at > -1) {
    emailFirst.value = email.substring(0, at)
    const domain = email.substring(at + 1)
    const known = ['naver.com', 'gmail.com', 'daum.net', 'hanmail.net', 'nate.com']
    if (known.includes(domain)) {
      emailDomain.value = domain
    } else {
      emailDomain.value = ''
      emailAddressCustom.value = domain
    }
  }
}
onMounted(load)

async function searchAddress() {
  await loadDaumPostcode()
  new window.daum.Postcode({
    oncomplete(data) {
      post.value = data.zonecode
      address.value = data.roadAddress || data.jibunAddress
    },
  }).open()
}

async function onSubmit() {
  errorMessage.value = ''
  successMessage.value = ''
  const domain = emailDomain.value || emailAddressCustom.value
  try {
    await api.put('member', '/api/profile', {
      phoneNumber: phoneCode.value + phoneMid.value + phoneLast.value,
      email: `${emailFirst.value}@${domain}`,
      post: post.value,
      address: address.value,
      addressDetail: addressDetail.value,
      receiveEmail: receiveEmail.value,
      receiveSms: receiveSms.value,
      receiveKakao: receiveKakao.value,
    })
    successMessage.value = '회원 정보가 수정되었습니다.'
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function toggleMfa() {
  await api.post('member', '/api/profile/mfa', { enabled: !profile.value.mfaEnabled })
  await load()
}

async function addInterestLocgov() {
  if (!addLocgovCode.value) {
    alert('관심 지자체를 선택해 주세요.')
    return
  }
  if (interestLocgovs.value.some((l) => l.locgovCode === addLocgovCode.value)) {
    alert('이미 추가된 지자체입니다.')
    return
  }
  try {
    const added = await api.postUrlEncoded('donation', '/interest-locgovs', { locgovCode: addLocgovCode.value })
    interestLocgovs.value.push(added)
  } catch (e) {
    alert(e.message || '추가에 실패했습니다.')
  }
}

async function removeInterestLocgov(locgovCode) {
  try {
    await api.post('donation', `/interest-locgovs/${locgovCode}/delete`)
    interestLocgovs.value = interestLocgovs.value.filter((l) => l.locgovCode !== locgovCode)
  } catch {
    alert('삭제에 실패했습니다.')
  }
}
</script>

<template>
  <section class="changeInfo center" v-if="profile">
    <div class="page-title-box" id="contents">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
      </span>
      <h2 class="page-title-txt">회원 정보 수정</h2>
      <div class="user-grade" v-if="profile.addressRegion">
        <span class="grade-bg">회원님의 주소는 <span class="blue-txt">{{ profile.addressRegion }}</span> 입니다.</span>
      </div>
    </div>

    <p class="success" v-if="successMessage">{{ successMessage }}</p>
    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <form id="profileForm" class="member-edit" @submit.prevent="onSubmit">
      <fieldset>
        <div class="add-info-area center">
          <div class="lable-field"><h3><span>회원정보&nbsp;</span><span>입력</span></h3></div>
          <div class="info-field">
            <div class="info-field-group">
              <div class="info-field-items memberGroup">
                <label for="memberLevel" class="flied-title">회원구분<span class="essential"> *</span></label>
                <div class="form-field"><div class="m-field"><input type="text" id="memberLevel" :value="profile.userTypeLabel" readonly /></div></div>
              </div>
              <div class="info-field-items member">
                <label for="userName" class="flied-title">이름<span class="essential"> *</span></label>
                <div class="form-field"><input type="text" id="userName" :value="profile.userName" readonly /></div>
              </div>
              <div class="info-field-items member">
                <label for="birthday" class="flied-title">생년월일<span class="essential"> *</span></label>
                <div class="form-field"><input type="text" id="birthday" :value="profile.birthday" readonly /></div>
              </div>
              <div class="info-field-items userNum">
                <label for="phoneCode" class="flied-title">휴대폰<span class="essential"> *</span></label>
                <span class="form-field">
                  <select id="phoneCode" v-model="phoneCode" aria-label="휴대폰번호 앞자리를 선택하세요">
                    <option v-for="c in ['010', '011', '016', '017', '018', '019']" :key="c" :value="c">{{ c }}</option>
                  </select>
                  <span class="s-txt">-</span>
                  <input type="text" v-model="phoneMid" maxlength="4" inputmode="numeric" aria-label="휴대폰번호 가운데 자리" />
                  <span class="s-txt">-</span>
                  <input type="text" v-model="phoneLast" maxlength="4" inputmode="numeric" aria-label="휴대폰번호 마지막 4자리" />
                </span>
              </div>
              <div class="info-field-items member">
                <label class="flied-title"><span class="essential">&nbsp;</span></label>
                <div class="form-field">
                  <span class="form-field"><button type="button" class="formBtn" @click="location.href = 'http://localhost:8081/profile/personal-info'">개인정보 수정</button></span>
                  <span class="form-field" style="font-size: 10px; color: red">개인정보수정은 인증기관에서 변경된 데이터를 반영합니다.</span>
                </div>
              </div>
            </div>

            <div class="line"></div>

            <div class="info-field-group">
              <div class="info-field-items">
                <label for="loginId" class="flied-title">아이디<span class="essential"> *</span></label>
                <span class="form-field"><input type="text" id="loginId" :value="profile.loginId" readonly /></span>
              </div>
              <div class="info-field-items password">
                <label class="flied-title">비밀번호<span class="essential"> *</span></label>
                <span class="form-field"><button type="button" class="formBtn" @click="router.push('/mypage/password')">비밀번호 변경</button></span>
              </div>
            </div>

            <div class="line"></div>

            <div class="info-field-group">
              <div class="info-field-items userEmail">
                <label for="emailFirst" class="flied-title">이메일<span class="essential"> *</span></label>
                <span class="form-field">
                  <span class="m-field">
                    <input type="text" id="emailFirst" v-model="emailFirst" aria-label="이메일 주소 @앞자리를 입력하세요" />
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
                <label for="post" class="flied-title">주소</label>
                <div class="form-field">
                  <div class="search-address m-field">
                    <input type="text" id="post" v-model="post" placeholder="우편번호" readonly />
                    <button type="button" class="formBtn" @click="searchAddress">주소찾기</button>
                  </div>
                  <div class="input-address">
                    <input type="text" id="address" v-model="address" placeholder="주소" readonly />
                    <input type="text" id="addressDetail" v-model="addressDetail" placeholder="상세주소" />
                  </div>
                </div>
              </div>
            </div>

            <div class="line"></div>

            <div class="info-field-group">
              <div class="info-field-items subscribe">
                <label class="flied-title">알림서비스 수신 동의</label>
                <div class="form-field">
                  <div class="m-field">
                    <span class="s-txt">국민비서 수신동의</span>
                    <div class="subscribe-select"><input type="radio" id="pbancAgree" disabled /><label for="pbancAgree">동의</label></div>
                    <div class="subscribe-select"><input type="radio" id="pbancDisAgree" checked disabled /><label for="pbancDisAgree">동의안함</label></div>
                  </div>
                  <div class="m-field">
                    <span class="s-txt">Email 수신동의</span>
                    <div class="subscribe-select"><input type="radio" id="emailAgree" :checked="receiveEmail" @change="receiveEmail = true" /><label for="emailAgree">동의</label></div>
                    <div class="subscribe-select"><input type="radio" id="emailDisAgree" :checked="!receiveEmail" @change="receiveEmail = false" /><label for="emailDisAgree">동의안함</label></div>
                  </div>
                  <div class="m-field">
                    <span class="s-txt">SMS 수신동의</span>
                    <div class="subscribe-select"><input type="radio" id="smsAgree" :checked="receiveSms" @change="receiveSms = true" /><label for="smsAgree">동의</label></div>
                    <div class="subscribe-select"><input type="radio" id="smsDisAgree" :checked="!receiveSms" @change="receiveSms = false" /><label for="smsDisAgree">동의안함</label></div>
                  </div>
                  <div class="m-field">
                    <span class="s-txt">알림톡 수신동의</span>
                    <div class="subscribe-select"><input type="radio" id="kakaoAgree" :checked="receiveKakao" @change="receiveKakao = true" /><label for="kakaoAgree">동의</label></div>
                    <div class="subscribe-select"><input type="radio" id="kakaoDisAgree" :checked="!receiveKakao" @change="receiveKakao = false" /><label for="kakaoDisAgree">동의안함</label></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </fieldset>
    </form>

    <div class="section-title-box">
      <h3>로그인 보안</h3>
    </div>
    <div class="info_wrap">
      <div class="info_txt_wrap">
        <p class="s_txt">로그인 시 아이디·비밀번호 확인 후 휴대폰 인증번호를 한 번 더 확인합니다.</p>
      </div>
      <button type="button" class="formBtn" @click="toggleMfa">{{ profile.mfaEnabled ? '다중인증 끄기' : '다중인증 켜기' }}</button>
      <span class="s_txt" v-if="profile.mfaEnabled" style="margin-left: 8px">현재 사용중</span>
    </div>

    <div class="add-info-area center">
      <h3><span>관심정보&nbsp;</span><span> 선택</span></h3>
      <div class="info-field">
        <div class="info-field-group">
          <div class="info-field-items favo">
            <label for="addUpperLocgovCode" class="flied-title">관심지자체</label>
            <div class="form-field">
              <span class="s-txt">관심 지자체를 선택해 주세요</span>
              <div class="select-location">
                <div class="select-location-items">
                  <span class="s-txt">시·도</span>
                  <select id="addUpperLocgovCode" v-model="addUpperLocgovCode">
                    <option value="">시·도 선택</option>
                    <option v-for="[code, name] in Object.entries(profile.provinces)" :key="code" :value="code">{{ name }}</option>
                  </select>
                </div>
                <div class="select-location-items">
                  <span class="s-txt">시·군·구</span>
                  <select id="addLocgovCode" v-model="addLocgovCode">
                    <option value="">시·군·구 선택</option>
                    <option v-for="l in filteredAddLocgovs" :key="l.locgovCode" :value="l.locgovCode">{{ l.upperLocgovNm }} {{ l.locgovNm }}</option>
                  </select>
                </div>
                <button class="formBtn addbtn" type="button" @click="addInterestLocgov"><img src="/images/icon/btn_add.png" alt="추가하기" /></button>
              </div>
              <div class="selected-location">
                <div class="attention-location" v-for="l in interestLocgovs" :key="l.locgovCode">
                  <span>{{ l.locgovName }}</span>
                  <button type="button" class="delete" title="삭제" @click="removeInterestLocgov(l.locgovCode)">&times;</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="line favo"></div>
        <div class="info_txt_wrap">
          <ul>
            <li class="s_txt">※ 관심 지자체를 추가해 두시면 마이페이지 &gt; 관심지자체에서 기부금 현황을 쉽게 확인할 수 있고 바로 기부하기 페이지로 이동도 가능합니다.</li>
            <li class="s_txt">※ 자신의 주민등록 주소지에는 기부가 불가능합니다.</li>
          </ul>
        </div>
      </div>
    </div>

    <div class="btn-box many">
      <button type="button" class="blueBtn cancellation" @click="router.push('/mypage/withdraw')">회원탈퇴<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
      <button type="submit" class="blueBtn u-confirm" form="profileForm">확인<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
    </div>
  </section>
</template>

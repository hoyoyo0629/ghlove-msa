<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

// AS-IS users/secede.html(및 Thymeleaf 버전 withdraw.html) 재현. 잔여포인트 표는 point
// 서비스 "기부포인트 조회"의 지자체별 집계를 그대로 재사용한다(탈퇴 시 이 포인트가 전부
// 소멸된다는 경고 목적이라 잔여 0인 지자체는 백엔드에서 이미 제외됨). 성공 시 AS-IS와
// 동일하게 세션/JWT쿠키가 서버에서 모두 무효화되므로 클라이언트도 auth store를 비운다.
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const loginId = ref('')
const userName = ref('')
const leaveCodeList = ref({})
const pointSummary = ref([])

const leaveCode = ref('')
const reason = ref('')
const password = ref('')
const errorMessage = ref('')

onMounted(async () => {
  try {
    const data = await api.get('member', '/api/withdraw-info')
    loginId.value = data.loginId
    userName.value = data.userName
    leaveCodeList.value = data.leaveCodeList
    pointSummary.value = data.pointSummary
  } finally {
    loading.value = false
  }
})

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(n ?? 0)
}

async function onSubmit() {
  errorMessage.value = ''
  if (!leaveCode.value) {
    alert('탈퇴사유를 선택해 주세요')
    return
  }
  if (!password.value) {
    alert('비밀번호를 입력해 주세요')
    return
  }
  if (!confirm('회원 탈퇴 시 회원 서비스를 모두 사용할 수 없습니다.\n정말 탈퇴하시겠습니까?')) {
    return
  }
  try {
    await api.post('member', '/api/withdraw', { password: password.value, leaveCode: leaveCode.value, reason: reason.value })
    await auth.fetchMe()
    router.push('/login?withdrawn=success')
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section class="find-idpw center" v-if="!loading">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        회원탈퇴
      </span>
      <h2 class="page-title-txt">회원탈퇴</h2>
    </div>

    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <form @submit.prevent="onSubmit">
      <div class="change-info-area member-secession">
        <div class="info-mess">
          <p class="sub-title">회원 탈퇴 시 회원 서비스를 모두 사용할 수 없습니다.</p>
          <p class="title pointRed">기부 포인트, 주문내역 등<br /><strong class="pointRed">모든 정보가 삭제</strong>됩니다.</p>
          <p class="s-txt">탈퇴 시 기존 정보의 복구가 불가능 하므로 신중이 탈퇴를 진행해주시기 바랍니다.</p>
        </div>
        <div class="line"></div>
        <div class="info-note">
          <ul>
            <li>
              탈퇴 후, 서비스에 등록한 게시물 및 댓글은 삭제되지 않고 보존되며 탈퇴 후에는 회원정보가 삭제되어 본인 여부를 확인할 수 없으므로
              게시글을 임의로 삭제해드릴 수 없습니다. 먼저 해당 게시물을 삭제하신 후 탈퇴를 신청하시기 바랍니다.
            </li>
            <li>회원 탈퇴후 재가입시에는 신규 회원 가입 처리 되며 탈퇴전 사용한 아이디로 재가입은 불가합니다.</li>
          </ul>
        </div>
        <div class="line"></div>
        <div class="info-field-group">
          <div class="table-container" v-if="pointSummary.length">
            <div class="table-result-body">
              <table>
                <caption>잔여포인트 - No, 기부지자체(시도/시군구), 잔여 포인트로 구성</caption>
                <thead>
                  <tr class="result-title">
                    <th scope="col" rowspan="2">No.</th>
                    <th scope="col" colspan="2">기부지자체</th>
                    <th scope="col" rowspan="2">잔여 포인트</th>
                  </tr>
                  <tr class="result-title">
                    <th scope="col">시 · 도</th>
                    <th scope="col">시 · 군 · 구</th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="result-row" v-for="(s, idx) in pointSummary" :key="s.locgovCode">
                    <td class="idx">{{ pointSummary.length - idx }}</td>
                    <td>{{ s.upperLocgovNm }}</td>
                    <td>{{ s.locgovNm }}</td>
                    <td class="amount">{{ formatN(s.remaining) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="info-field-items" style="margin-bottom: 10px">
            <label for="userIdConfirm" class="flied-title">회원아이디</label>
            <div class="form-field"><input type="text" id="userIdConfirm" :value="loginId" readonly /></div>
          </div>
          <div class="info-field-items" style="margin-bottom: 10px">
            <label for="userNameConfirm" class="flied-title">회원명</label>
            <div class="form-field"><input type="text" id="userNameConfirm" :value="userName" readonly /></div>
          </div>
          <div class="info-field-items">
            <label for="password" class="flied-title">비밀번호 입력</label>
            <div class="form-field"><input type="password" id="password" v-model="password" autocomplete="off" /></div>
          </div>
        </div>
      </div>
      <div class="change-info-area member-secession">
        <div class="info-field-group">
          <div class="info-field-items">
            <label class="flied-title">탈퇴사유</label>
            <div class="form-field reason">
              <div class="reason-select">
                <ul>
                  <li v-for="(label, code) in leaveCodeList" :key="code">
                    <input type="radio" name="leaveCode" :id="code" :value="code" v-model="leaveCode" />
                    <label :for="code">{{ label }}</label>
                  </li>
                </ul>
              </div>
              <div class="reason-typing">
                <label for="reason" class="sr-only">불편사항</label>
                <p>아래 항목에 불편하신 사항을 입력해 주세요</p>
                <textarea id="reason" v-model="reason" placeholder="불편사항"></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="btn-box many">
        <button type="button" class="blueBtn cancellation" @click="router.push('/mypage/profile')">
          회원정보<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
        <button type="submit" class="blueBtn u-confirm">
          회원탈퇴<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
      </div>
    </form>
  </section>
</template>

<style>
@import '/css/change-pw.css';
</style>

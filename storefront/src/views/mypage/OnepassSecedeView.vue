<script setup>
import { modalAlert } from '../../composables/useModal'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

// AS-IS users/onepass_secede.html 재현 - 디지털원패스 연동해지(= 탈퇴). 회원정보수정의
// "디지털원패스 연동 해지" 버튼(원패스 가입=loginPathCode 300)에서 진입한다. 일반 회원탈퇴와
// 달리 비밀번호 입력이 없고, 잔여포인트 표도 없다. 성공 시 서버가 세션/쿠키를 무효화한다.
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const userName = ref('')
const leaveCodeList = ref({})
const leaveCode = ref('')
const reason = ref('')

onMounted(async () => {
  try {
    const data = await api.get('member', '/api/withdraw-info')
    userName.value = data.userName
    leaveCodeList.value = data.leaveCodeList
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  if (!leaveCode.value) {
    modalAlert('탈퇴사유를 선택해 주세요')
    return
  }
  const res = await api.post('member', '/api/auth/onepass-cancel', { leaveCode: leaveCode.value, leaveReason: reason.value })
  if (res.info && res.info.value === '00') {
    await auth.fetchMe()
    await modalAlert(res.info.message)
    router.push('/')
  } else {
    modalAlert(res.info ? res.info.message : '연동해지에 실패했습니다.')
  }
}
</script>

<template>
  <section class="find-idpw center" v-if="!loading">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        회원정보 수정
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        연동해지
      </span>
      <h2 class="page-title-txt">연동해지</h2>
    </div>

    <form @submit.prevent="onSubmit">
      <div class="change-info-area member-secession">
        <div class="info-mess">
          <p class="sub-title">
            <strong>디지털 원패스 회원연동을 해지 합니다.<br />회원연동 해지와 동시에 회원탈퇴가 진행됩니다.</strong><br />
            회원 탈퇴 시 회원 서비스를 모두 사용할 수 없습니다.
          </p>
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
          <div class="info-field-items">
            <label for="userNameConfirm" class="flied-title">회원명</label>
            <div class="form-field"><input type="text" id="userNameConfirm" :value="userName" readonly /></div>
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
          연동해지<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
        </button>
      </div>
    </form>
  </section>
</template>

<style>
@import '/css/change-pw.css';
</style>

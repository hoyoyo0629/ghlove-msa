<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS SFR-002 "지자체담당자·제공자 역할 신청" - 기존 Thymeleaf roles/request.html은
// 골격만 있는 내부용 화면(css/style.css)이었는데, 이번 라운드에서 나머지 마이페이지
// 화면들과 동일한 정식 디자인(info-field-items 폼 + list_body 표)으로 재구축했다.
const data = ref(null)
const loading = ref(true)

const requestedRole = ref('ROLE_LOCALGOV')
const reason = ref('')
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    data.value = await api.get('member', '/api/roles/request')
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function onSubmit() {
  errorMessage.value = ''
  try {
    await api.post('member', '/api/roles/request', { requestedRole: requestedRole.value, reason: reason.value })
    reason.value = ''
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        지자체담당자·제공자 역할 신청
      </span>
      <h2 class="page-title-txt">지자체담당자·제공자 역할 신청</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading && data">
    <div class="center">
      <div class="s-contents mypageS">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>
        <form @submit.prevent="onSubmit">
          <div class="info-field-group">
            <div class="info-field-items">
              <label for="requestedRole" class="flied-title">신청할 역할</label>
              <span class="form-field">
                <select id="requestedRole" v-model="requestedRole">
                  <option value="ROLE_LOCALGOV">{{ data.roleLabels.ROLE_LOCALGOV ?? '지자체 담당자' }}</option>
                  <option value="ROLE_PROVIDER">{{ data.roleLabels.ROLE_PROVIDER ?? '답례품 제공자' }}</option>
                </select>
              </span>
            </div>
            <div class="info-field-items">
              <label for="reason" class="flied-title">신청 사유</label>
              <span class="form-field"><textarea id="reason" v-model="reason" rows="3" placeholder="소속 지자체명, 사업자 정보 등을 입력해 주세요"></textarea></span>
            </div>
          </div>
          <div class="btn-box many mid">
            <button type="submit" class="blueBtn u-confirm">
              신청하기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
            </button>
          </div>
        </form>
      </div>

      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="data.myRequests.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">내 신청 내역 - 역할, 사유, 상태, 신청일로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">역할</th>
                    <th class="date-col" scope="col">사유</th>
                    <th class="date-col" scope="col">상태</th>
                    <th class="date-col" scope="col">신청일</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="r in data.myRequests" :key="r.requestId">
                    <td class="date-col">{{ r.requestedRoleLabel }}</td>
                    <td class="date-col">{{ r.reason }}</td>
                    <td class="date-col">{{ r.statusLabel }}</td>
                    <td class="date-col">{{ r.createdDate?.replace('T', ' ').slice(0, 16) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else>
            <p>신청 내역이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
@import '/css/mypage-status.css';
@import '/css/change-info.css';
</style>

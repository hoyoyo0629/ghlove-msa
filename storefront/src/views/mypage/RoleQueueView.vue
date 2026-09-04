<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS SFR-002 "역할신청 승인함" - 기존 Thymeleaf roles/queue.html과 동일하게 승인권자
// 인증 게이트웨이가 아직 없어 로그인만 하면 누구나 대기중인 신청을 볼 수 있다("임시로
// 이 화면에서 바로 처리" - RoleRequestController 주석과 동일). 골격만 있던 내부용 화면을
// 나머지 마이페이지 화면들과 동일한 정식 디자인으로 재구축했다.
const rows = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    rows.value = await api.get('member', '/api/roles/queue')
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function approve(requestId) {
  await api.post('member', `/api/roles/${requestId}/approve`)
  await load()
}
async function reject(requestId) {
  await api.post('member', `/api/roles/${requestId}/reject`)
  await load()
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
        역할신청 승인함
      </span>
      <h2 class="page-title-txt">역할신청 승인함</h2>
      <p class="point-txt">승인권자 인증은 별도 게이트웨이가 아직 없어, 로그인한 회원이면 누구나 이 화면에서 바로 승인/반려를 처리할 수 있습니다.</p>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading">
    <div class="center">
      <div class="contents-wrap">
        <div class="s-contents">
          <div class="list_body table-container w3c_v_2410" v-if="rows.length">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">역할신청 승인함 - 회원ID, 신청역할, 사유, 신청일, 처리로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col" scope="col">회원 ID</th>
                    <th class="date-col" scope="col">신청 역할</th>
                    <th class="date-col" scope="col">사유</th>
                    <th class="date-col" scope="col">신청일</th>
                    <th class="date-col" scope="col">처리</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="r in rows" :key="r.requestId">
                    <td class="date-col">{{ r.userId }}</td>
                    <td class="date-col">{{ r.requestedRoleLabel }}</td>
                    <td class="date-col">{{ r.reason }}</td>
                    <td class="date-col">{{ r.createdDate?.replace('T', ' ').slice(0, 16) }}</td>
                    <td class="date-col">
                      <div class="btn-box many" style="justify-content: center">
                        <button type="button" class="formBtn" @click="approve(r.requestId)">승인</button>
                        <button type="button" class="formBtn cancellation" @click="reject(r.requestId)">반려</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="common_none" v-else>
            <p>대기중인 신청이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
@import '/css/mypage-status.css';
</style>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../../api/http'

// AS-IS mypage/receiptListPrint.html(및 Thymeleaf 버전 certificate-print.html) 재현 - 새 탭에서
// window.print()를 호출하는 A4 두 페이지(1페이지 확인증 앞면, 2페이지 기부내역 뒷면). App.vue가
// route.meta.bare로 사이트 헤더/푸터를 빼서 인쇄 시 섞여 나오지 않게 한다.
const route = useRoute()
const certificate = ref(null)

onMounted(async () => {
  const cntrSnList = Array.isArray(route.query.cntrSn) ? route.query.cntrSn : [route.query.cntrSn].filter(Boolean)
  const params = new URLSearchParams()
  cntrSnList.forEach((v) => params.append('cntrSn', v))
  try {
    certificate.value = await api.get('donation', `/api/my/receipts/certificate?${params}`)
    setTimeout(() => window.print(), 200)
  } catch (e) {
    document.body.textContent = e.message
  }
})

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <div v-if="certificate">
    <div class="page">
      <div class="lineBox card-front">
        <div class="title-area">
          <img class="donation-comment" src="/images/donation_receipt/donation-comment.png" alt="" />
          <h5>고향사랑기부 확인증</h5>
        </div>
        <div class="section-area">
          <table class="table-area">
            <caption class="sr-only">고향사랑기부 확인증</caption>
            <colgroup><col style="width: 40%" /><col style="width: 60%" /></colgroup>
            <tr><th style="letter-spacing: 2.55em">성명</th><td>{{ certificate.userName }}</td></tr>
            <tr><th style="letter-spacing: 0.25em">생년월일</th><td>{{ certificate.birthdayDisplay }}</td></tr>
            <tr><th>기부지자체</th><td>{{ certificate.topLocGovDisplay }}</td></tr>
            <tr><th style="letter-spacing: 0.25em">기부금액</th><td>총 {{ formatN(certificate.totalCntrAmt) }}원</td></tr>
          </table>
        </div>
        <div class="bottom-area">
          <div class="comment-area">위와 같이 고향사랑기부에 참여하였음을 확인함</div>
          <div class="date-area">{{ certificate.nowDateDisplay }}</div>
          <div class="icon-area"><img src="/images/donation_receipt/logo.png" alt="고향사랑e음" style="width: 100%" /></div>
        </div>
      </div>
    </div>

    <div class="page">
      <div class="lineBox card-back">
        <div class="title-area"><h5>고향사랑기부내역</h5></div>
        <div class="section-area">
          <div class="receipt-result-body">
            <hr />
            <table>
              <caption class="sr-only">고향사랑기부내역</caption>
              <colgroup><col style="width: 15%" /><col style="width: 70%" /><col style="width: 15%" /></colgroup>
              <tr class="result-title"><th>기부일자</th><th>기부지자체</th><th>기부액 (원)</th></tr>
              <tr class="result-row" v-for="row in certificate.rows" :key="row.cntrSn">
                <td>{{ row.cntrDeDisplay }}</td>
                <td>
                  <span>{{ row.upperLocgovNm ? row.upperLocgovNm + ' ' : '' }}{{ row.locgovNm }}{{ row.spelDstrYn === 'Y' ? '(특별재난지역)' : '' }}</span><br />
                  <span>(사업자번호 : {{ row.bizRno }})</span>
                </td>
                <td class="amount">{{ formatN(row.cntrAmt) }}</td>
              </tr>
            </table>
            <hr />
          </div>
        </div>
        <div class="bottom-area">
          <div class="comment-area">본 기부확인증은 고향사랑e음 기부내역을 바탕으로 발급되었으며,</div>
          <div class="comment-area">전체 기부내역은 고향사랑e음을 통해서 확인이 가능합니다.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
@import '/css/receipt-print.css';
</style>

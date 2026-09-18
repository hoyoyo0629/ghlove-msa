<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../../api/http'
import { formatN } from '../../utils/format'

// AS-IS mypage/receiptPrint.html(OZReport+FSW) / Thymeleaf receipt-official-print.html 재현 -
// 새 탭에서 여는 조회용 공식 기부금영수증. 직인은 화면에 표시하지 않고(직인 미전송) PDF 출력물에만
// 서버에서 합성된다. App.vue가 route.meta.bare로 사이트 헤더/푸터를 빼서 인쇄 시 섞이지 않게 한다.
const route = useRoute()
const receipt = ref(null)
const errorMessage = ref('')

onMounted(async () => {
  try {
    receipt.value = await api.get('donation', `/api/my/receipts/official/${encodeURIComponent(route.params.cntrSn)}`)
  } catch (e) {
    // AS-IS mypage/receiptPrint.html:126 - 영수증 데이터가 없으면(소유/완료 아님 등) 알럿을
    // 띄우고 이 팝업 창을 그대로 닫는다(self.close()). 서버가 내려주는 "영수증 정보가 없습니다."
    // 문구도 AS-IS와 동일하다(OfficialReceiptService.ownedCompletedDonation).
    errorMessage.value = e.message || '영수증 정보가 없습니다.'
    window.alert(errorMessage.value)
    window.close()
  }
})

// 직인 합성 PDF는 조회 경로가 아니라 donation 서비스의 /pdf가 서버에서 만들어 반출한다.
function openPdf() {
  window.open(api.assetUrl('donation', `/receipts/official/${encodeURIComponent(route.params.cntrSn)}/pdf`), '_blank', 'noopener')
}
</script>

<template>
  <div v-if="receipt">
    <div class="page">
      <div class="lineBox official-receipt">
        <div class="title-area">
          <h5>고향사랑 기부금 영수증</h5>
        </div>
        <p class="sub-title">
          「고향사랑 기부금에 관한 법률」에 따라 아래와 같이 기부금을 접수하였음을 증명합니다.
        </p>
        <table>
          <tr><th>접수번호</th><td>{{ receipt.cntrSn }}</td></tr>
          <tr><th>전자납부번호</th><td>{{ receipt.elctrnPayNo || '-' }}</td></tr>
          <tr><th>기부자 성명</th><td>{{ receipt.userName }}</td></tr>
          <tr><th>생년월일</th><td>{{ receipt.birthdayDisplay }}</td></tr>
          <tr><th>기부지자체</th>
            <td>{{ receipt.locgovDisplay }}<span v-if="receipt.bizrno"> (사업자번호 : {{ receipt.bizrno }})</span></td>
          </tr>
          <tr><th>기부금액</th><td>{{ formatN(receipt.cntrAmt) }}원</td></tr>
          <tr><th>기부일자</th><td>{{ receipt.cntrDeDisplay }}</td></tr>
          <tr><th>발급일자</th><td>{{ receipt.issueDateDisplay }}</td></tr>
        </table>

        <div class="issue-area">
          <span class="issuer">{{ receipt.locgovDisplay }} {{ receipt.offcsNm || '지자체' }} 발급</span>
          <!-- 직인은 화면에 표시하지 않는다(직인 미전송). PDF 출력물에만 서버 합성됨. -->
        </div>
      </div>
    </div>

    <div class="receipt-actions">
      <button type="button" class="btn-pdf" @click="openPdf">PDF 출력 · 저장</button>
      <p class="notice">
        보안을 위해 직인은 화면에 표시되지 않으며, <b>PDF 출력물에만 서버에서 합성</b>됩니다.<br />
        세액공제의 정본은 국세청 홈택스 전자기부금영수증입니다.
      </p>
    </div>
  </div>
  <div v-else-if="errorMessage" class="receipt-actions">
    <p class="notice">{{ errorMessage }}</p>
  </div>
</template>

<style scoped>
.page { max-width: 720px; margin: 24px auto; padding: 0 16px; }
.official-receipt { border: 2px solid #1b3a6b; border-radius: 6px; padding: 28px 32px; }
.official-receipt .title-area { text-align: center; border-bottom: 2px solid #1b3a6b; padding-bottom: 14px; margin-bottom: 18px; }
.official-receipt .title-area h5 { font-size: 24px; font-weight: 800; color: #1b3a6b; margin: 0; }
.official-receipt .sub-title { font-size: 13px; color: #555; text-align: center; margin: 0 0 20px; line-height: 1.6; }
.official-receipt table { width: 100%; border-collapse: collapse; }
.official-receipt th, .official-receipt td { border: 1px solid #ccc; padding: 10px 14px; font-size: 14px; text-align: left; }
.official-receipt th { width: 30%; background: #f4f6fa; color: #333; font-weight: 700; white-space: nowrap; }
.official-receipt .issue-area { text-align: center; margin-top: 24px; }
.official-receipt .issuer { font-size: 15px; font-weight: 700; color: #1b3a6b; }

.receipt-actions { max-width: 720px; margin: 24px auto 0; text-align: center; padding: 0 16px; }
.receipt-actions .btn-pdf {
  display: inline-block; padding: 12px 28px; background: #1b3a6b; color: #fff;
  border: 0; border-radius: 6px; font-size: 16px; font-weight: 700; cursor: pointer;
  text-decoration: none;
}
.receipt-actions .notice { margin-top: 14px; font-size: 13px; color: #777; line-height: 1.6; }

@media print {
  .receipt-actions { display: none; }
}
</style>

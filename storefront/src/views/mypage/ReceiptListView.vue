<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/http'

// AS-IS mypage/receiptList.html(및 Thymeleaf 버전 receipts.html/certificate.html) 재현.
// AS-IS 원본은 Vue SPA라 "기부확인증 보기"를 목록 화면 위 모달로 띄운다(components/ui/
// modal-receipt_view.vue) - 이 storefront도 진짜 SPA이므로 Thymeleaf가 어쩔 수 없이 썼던
// "별도 페이지" 우회 없이 원래 설계(모달)를 그대로 재현한다. "출력"은 새 탭(/print/
// receipt-certificate)에서 window.print()로 여는데, 이 탭은 App.vue가 route.meta.bare로
// 사이트 헤더/푸터를 뺀 인쇄 전용 레이아웃을 준다(기존 certificate-print.html과 동일 목적).
const data = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const selected = ref([])

const searchStartDate = ref('')
const searchEndDate = ref('')
const upperLocgovCode = ref('')
const locgovCode = ref('')

const certificate = ref(null)
const flipped = ref(false)

async function load(page = 1) {
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(page) })
    if (searchStartDate.value) params.set('searchStartDate', searchStartDate.value)
    if (searchEndDate.value) params.set('searchEndDate', searchEndDate.value)
    if (upperLocgovCode.value) params.set('upperLocgovCode', upperLocgovCode.value)
    if (locgovCode.value) params.set('locgovCode', locgovCode.value)
    data.value = await api.get('donation', `/api/my/receipts?${params}`)
    selected.value = []
  } finally {
    loading.value = false
  }
}
onMounted(() => load())

function reset() {
  searchStartDate.value = ''
  searchEndDate.value = ''
  upperLocgovCode.value = ''
  locgovCode.value = ''
  load()
}

function quickRange(mode, value) {
  const end = new Date()
  const start = new Date()
  if (mode === 'week') start.setDate(start.getDate() - value * 7)
  else if (mode === 'month') start.setMonth(start.getMonth() - value)
  const iso = (d) => d.toISOString().slice(0, 10)
  searchStartDate.value = iso(start)
  searchEndDate.value = iso(end)
}

function toggleAll(e) {
  selected.value = e.target.checked ? data.value.rows.map((r) => r.cntrSn) : []
}

function certQuery() {
  const params = new URLSearchParams()
  selected.value.forEach((cntrSn) => params.append('cntrSn', cntrSn))
  return params
}

async function viewCertificate() {
  errorMessage.value = ''
  if (!selected.value.length) {
    alert('선택한 기부내역이 없습니다.\n선택한 기부내역만 기부확인증에 출력됩니다.')
    return
  }
  try {
    certificate.value = await api.get('donation', `/api/my/receipts/certificate?${certQuery()}`)
    flipped.value = false
  } catch (e) {
    errorMessage.value = e.message
  }
}

function printCertificate() {
  if (!selected.value.length) {
    alert('선택한 기부내역이 없습니다.\n선택한 기부내역만 기부확인증에 출력됩니다.')
    return
  }
  window.open(`/print/receipt-certificate?${certQuery()}`, '_blank')
}

function closeModal() {
  certificate.value = null
}

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
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
        기부확인증
      </span>
      <h2 class="page-title-txt">기부확인증</h2>
      <p class="point-txt">
        기부 결제 후 연계 시스템 지연에 따라 <span style="font-weight: bold">기부 결제 내역이 최대 3일</span>까지 반영되지 않을 수 있으므로 참고 바랍니다.
      </p>
    </div>
  </section>

  <section id="contents" class="receipt-contents" v-if="!loading && data">
    <div class="center">
      <div class="contents-wrap myDonation">
        <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

        <div>
          <div class="s-contents mypageS">
            <div class="mypage-top">
              <div class="mypage-top-list">
                <label class="search-title" for="totalAmt">
                  <img class="icon-img" src="/images/icon/cli-icon_my-amount-all.png" alt="총 기부금액" />
                  <span class="label-title"> 총 기부금액</span>
                </label>
                <span class="m-field">
                  <input type="text" id="totalAmt" readonly :value="formatN(data.totalCntrAmt)" /><span class="s_txt">원</span>
                </span>
                <span class="s_txt">(실 납부액 기준)</span>
              </div>
            </div>

            <div class="line"></div>

            <div class="mypage-top">
              <label class="search-title">검색</label>
              <div class="selected-field">
                <div class="selected-field-line">
                  <div class="selected-item">
                    <label for="startDate" class="sr-only">시작일</label>
                    <input type="date" id="startDate" v-model="searchStartDate" />
                    <span class="s_txt"> ~ </span>
                    <input type="date" id="endDate" v-model="searchEndDate" />
                    <label for="endDate" class="sr-only">종료일</label>
                  </div>
                  <div class="formbtn-box">
                    <button class="formBtn week-1" type="button" @click="quickRange('week', 1)">1주일</button>
                    <button class="formBtn month-1" type="button" @click="quickRange('month', 1)">1개월</button>
                    <button class="formBtn month-3" type="button" @click="quickRange('month', 3)">3개월</button>
                    <button class="formBtn month-6" type="button" @click="quickRange('month', 6)">6개월</button>
                  </div>
                </div>
                <div class="selected-field-line city">
                  <div class="selected-item">
                    <select aria-label="시·도 선택" v-model="upperLocgovCode">
                      <option value="">시·도 선택</option>
                      <option v-for="[code, name] in [...new Map(data.locgovs.map((l) => [l.upperLocgovCode, l.upperLocgovNm]))]" :key="code" :value="code">
                        {{ name }}
                      </option>
                    </select>
                  </div>
                  <div class="selected-item">
                    <select aria-label="시·군·구 선택" v-model="locgovCode">
                      <option value="">시·군·구 선택</option>
                      <option
                        v-for="l in data.locgovs.filter((x) => !upperLocgovCode || x.upperLocgovCode === upperLocgovCode)"
                        :key="l.locgovCode"
                        :value="l.locgovCode"
                      >
                        {{ l.upperLocgovNm }} {{ l.locgovNm }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="btn-box many">
            <button type="button" class="blueBtn cancellation" @click="reset">초기화<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="초기화" /></span></button>
            <button type="button" class="blueBtn u-confirm" @click="load()">검색<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="검색" /></span></button>
          </div>
        </div>
      </div>
    </div>
  </section>

  <section class="s-con receipt-con" v-if="!loading && data">
    <div class="center">
      <div class="s-contents table-wrapper myDonation">
        <div class="table-top">
          <div class="btn-box many" style="margin-bottom: 20px">
            <button type="button" class="blueBtn u-confirm" @click="viewCertificate">
              기부확인증 보기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="기부확인증 보기" /></span>
            </button>
            <button type="button" class="blueBtn u-confirm" @click="printCertificate">
              기부확인증 출력<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="기부확인증 출력" /></span>
            </button>
          </div>
        </div>

        <div class="table-container">
          <div class="table-result-body">
            <table>
              <caption class="sr-only">기부확인증 목록</caption>
              <colgroup><col style="width: 10%" /><col style="width: 20%" /><col style="width: 50%" /><col style="width: 20%" /></colgroup>
              <tr class="result-title">
                <th><label for="toggleCheckbox" class="sr-only">기부확인증 전체 선택</label><input id="toggleCheckbox" type="checkbox" @change="toggleAll" /></th>
                <th>기부일자</th>
                <th>기부지자체</th>
                <th>기부액 (원)</th>
              </tr>
              <tr class="result-row" v-for="r in data.rows" :key="r.cntrSn">
                <td>
                  <label class="sr-only" :for="'cs' + r.cntrSn">기부확인증 {{ r.upperLocgovNm }} 선택</label>
                  <input type="checkbox" :id="'cs' + r.cntrSn" :value="r.cntrSn" v-model="selected" />
                </td>
                <td>{{ r.cntrDeDisplay }}</td>
                <td>{{ r.upperLocgovNm ? r.upperLocgovNm + ' ' : '' }}{{ r.locgovNm }}</td>
                <td>{{ formatN(r.cntrAmt) }}</td>
              </tr>
              <tr v-if="!data.rows.length">
                <td colspan="4" class="empty">기부확인증을 발급할 완료된 기부 내역이 없습니다.</td>
              </tr>
            </table>
          </div>

          <div class="pagination_ali" v-if="data.totalPages > 1">
            <ul class="pagination-frame">
              <li><a class="fist arrow_btn" href="javascript:void(0)" @click="load(1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
              <li><a class="prev arrow_btn" href="javascript:void(0)" @click="load(Math.max(1, data.currentPage - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
              <li v-for="p in data.totalPages" :key="p" :class="{ 'selected-page': p === data.currentPage }">
                <a class="page-text" href="javascript:void(0)" @click="load(p)">{{ p }}</a>
              </li>
              <li><a class="next arrow_btn" href="javascript:void(0)" @click="load(Math.min(data.totalPages, data.currentPage + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
              <li><a class="last arrow_btn" href="javascript:void(0)" @click="load(data.totalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- 기부확인증 보기 모달 - AS-IS components/ui/modal-receipt_view.vue 재현 -->
  <div id="receipt_modal" style="display: flex; align-items: center; justify-content: center" v-if="certificate">
    <div class="modal_con">
      <button type="button" class="closeBtn" style="all: unset; cursor: pointer" @click="closeModal">
        <img src="/images/icon/cli-icon_btn-close-modal.png" alt="닫기" />
      </button>
      <div class="modal-body-receipt">
        <div class="modal_body_wrap">
          <div class="receipt_card_area">
            <div class="card-wrapper">
              <div class="card-face" id="cardContainer" :class="{ 'is-flipped': flipped }" @click="flipped = !flipped">
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
          </div>

          <div class="receipt_btn-box">
            <button type="button" class="blueBtn u-confirm" @click="printCertificate">
              확인증 출력<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="확인증 출력" /></span>
            </button>
            <button type="button" class="blueBtn u-confirm" @click="closeModal">
              닫기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="닫기" /></span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
@import '/css/receipt-modal.css';
</style>

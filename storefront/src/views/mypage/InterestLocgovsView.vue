<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS mypage/intrstLocGov.html(및 Thymeleaf 버전 interest-locgovs.html) 재현.
// 추가는 이 화면이 아니라 회원정보수정 화면에서 이뤄진다(AS-IS도 동일 - 여기는 조회+선택삭제만).
const router = useRouter()
const locgovs = ref([])
const selected = ref([])

async function load() {
  locgovs.value = await api.get('donation', '/api/my/interest-locgovs')
  selected.value = []
}
onMounted(load)

function toggleAll(e) {
  selected.value = e.target.checked ? locgovs.value.map((l) => l.locgovCode) : []
}

async function deleteSelected() {
  if (selected.value.length === 0) {
    alert('선택된 지자체가 없습니다.')
    return
  }
  if (!confirm('정보를 수정 하시겠습니까?')) return
  for (const code of selected.value) {
    await api.post('donation', `/interest-locgovs/${code}/delete`)
  }
  await load()
}

function formatAmount(n) {
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
        관심지자체
      </span>
      <h2 class="page-title-txt">관심지자체</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div v-if="locgovs.length" class="contents_wrap">
        <div class="s-contents">
          <div class="cart_top">
            <div class="chk_box check-level1">
              <input type="checkbox" id="selectAll" @click="toggleAll" />
              <label for="selectAll">전체선택</label>
            </div>
            <div class="btn_area">
              <button type="button" class="formBtn del" @click="deleteSelected">선택삭제</button>
            </div>
          </div>

          <div class="list_body table-container w3c_v_2410">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">관심지자체 - 기부지자체, 나의 기부현황, 기부하기, 바로가기로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col chk_date" scope="col"><span class="sr-only">선택</span></th>
                    <th class="date-col index" scope="col">No.</th>
                    <th class="date-col info_loc" scope="col">기부지자체</th>
                    <th class="date-col do_status" scope="col">나의 기부현황</th>
                    <th class="date-col btn_col" scope="col">기부하기</th>
                    <th class="date-col btn_col" scope="col">바로가기</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="(l, i) in locgovs" :key="l.locgovCode">
                    <td class="date-col chk_date">
                      <div class="check_input">
                        <input type="checkbox" class="row-check" :value="l.locgovCode" v-model="selected" />
                      </div>
                    </td>
                    <td class="date-col index">{{ locgovs.length - i }}</td>
                    <td class="date-col info_loc">{{ l.locgovName }}</td>
                    <td class="date-col do_status">{{ formatAmount(l.myTotal) }}</td>
                    <td class="date-col btn_col">
                      <button type="button" class="formBtn dona" @click="router.push({ path: '/donate', query: { locgovCode: l.locgovCode } })">기부하기</button>
                    </td>
                    <td class="date-col btn_col">
                      <button type="button" class="formBtn" @click="router.push({ path: '/gifts', query: { locgovCode: l.locgovCode } })">답례품몰</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="common_none" v-else>
        <p>관심지자체가 없습니다.</p>
      </div>
    </div>
  </section>
</template>

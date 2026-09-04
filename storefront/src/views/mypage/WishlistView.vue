<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

const router = useRouter()

// AS-IS mypage/favorItem.html(및 Thymeleaf 버전 wishlist.html) 재현. 개별 삭제 버튼 없이
// 체크박스로 골라 "선택 삭제"만 지원 - 삭제는 이미 JSON인 toggle 엔드포인트를 항목별로
// 재사용한다(gift 서비스에 새 bulk-delete API를 따로 만들지 않음).
const items = ref([])
const selected = ref([])

async function load() {
  items.value = await api.get('gift', '/api/wishlist')
  selected.value = []
}
onMounted(load)

function toggleAll(e) {
  selected.value = e.target.checked ? items.value.map((w) => w.itemId) : []
}

async function deleteSelected() {
  if (selected.value.length === 0) {
    alert('선택된 답례품이 없습니다.')
    return
  }
  if (!confirm('정보를 수정 하시겠습니까?')) return
  for (const itemId of selected.value) {
    await api.post('gift', `/wishlist/${itemId}/toggle`)
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
        관심답례품
      </span>
      <h2 class="page-title-txt">관심답례품</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div v-if="items.length" class="contents_wrap">
        <div class="s-contents">
          <div class="cart_top">
            <div class="chk_box check-level1">
              <input type="checkbox" id="selectAll" @click="toggleAll" />
              <label for="selectAll">전체선택</label>
            </div>
            <div class="btn_area">
              <button type="button" class="formBtn del" @click="deleteSelected">선택 삭제</button>
            </div>
          </div>

          <div class="list_body table-container w3c_v_2410">
            <div class="list_wrap">
              <table>
                <caption class="sr-only">관심답례품 - 지자체명, 답례품정보, 상태, 바로가기로 구성</caption>
                <thead class="list-title">
                  <tr class="items_wrap">
                    <th class="date-col chk_date"><span class="sr-only">선택</span></th>
                    <th class="date-col info_loc" scope="col">지자체명</th>
                    <th class="date-col g_info_wrap" scope="col">답례품정보</th>
                    <th class="date-col sal_status" scope="col">상태</th>
                    <th class="date-col btn_col" scope="col">바로가기</th>
                  </tr>
                </thead>
                <tbody class="item_list-group">
                  <tr class="list-items" v-for="w in items" :key="w.wishlistId">
                    <td class="date-col chk_date">
                      <div class="check_input"><input type="checkbox" class="row-check" :value="w.itemId" v-model="selected" /></div>
                    </td>
                    <td class="date-col info_loc">{{ w.locgovName ?? w.locgovCode }}</td>
                    <td class="date-col g_info_wrap">
                      <router-link class="g_info__img" :to="`/gifts/${w.itemId}`">
                        <img v-if="w.thumbnailUrl" :src="`/gift/uploads/${w.thumbnailUrl}`" alt="답례품이미지" />
                        <img v-else src="/images/icon/non-list.png" alt="답례품이미지 없음" />
                      </router-link>
                      <div class="g_info__txt">
                        <div class="info_title"><router-link :to="`/gifts/${w.itemId}`">{{ w.itemName }}</router-link></div>
                        <div class="g_info__price">{{ formatAmount(w.salePrice) }} P</div>
                      </div>
                    </td>
                    <td class="date-col sal_status" v-if="w.soldOut"><span>품절</span></td>
                    <td class="date-col sal_status" v-else-if="w.onSale"><span>판매중</span></td>
                    <td class="date-col sal_status" v-else><span>판매중지</span></td>
                    <td class="date-col btn_col">
                      <button type="button" class="formBtn" @click="router.push(`/gifts/${w.itemId}`)">답례품몰</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="common_none" v-else>
        <p>관심답례품이 없습니다.</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

const router = useRouter()

// AS-IS mypage/deliveryInfo.html(및 Thymeleaf 버전 delivery/list.html) 재현 - 라디오로
// 배송지 하나를 고른 뒤 하단 "기본배송지로 선택"/"삭제" 버튼으로 동작한다(행마다 버튼을
// 두지 않음).
const deliveries = ref([])
const selectedId = ref(null)

async function load() {
  deliveries.value = await api.get('member', '/api/delivery')
  selectedId.value = null
}
onMounted(load)

async function setDefault() {
  if (!selectedId.value) {
    alert('배송지를 선택해주세요.')
    return
  }
  if (!confirm('정보를 수정 하시겠습니까?')) return
  await api.post('member', `/api/delivery/${selectedId.value}/default`)
  await load()
}

async function remove() {
  if (!selectedId.value) {
    alert('배송지를 선택해주세요.')
    return
  }
  const selected = deliveries.value.find((d) => d.userDeliveryId === selectedId.value)
  const message = selected?.defaultFlag ? '기본배송지입니다. 삭제하시겠습니까?' : '선택하신 배송지를 삭제 하시겠습니까?'
  if (!confirm(message)) return
  await api.delete('member', `/api/delivery/${selectedId.value}`)
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
        배송지 관리
      </span>
      <h2 class="page-title-txt">배송지 관리</h2>
    </div>
  </section>

  <section id="contents" class="receipt-contents">
    <div class="center">
      <div class="s-contents mylist">
        <div class="btn_area">
          <button type="button" class="formBtn del" @click="router.push('/mypage/delivery/new')">배송지 추가</button>
        </div>

        <div class="list_body table-container w3c_v_2410" v-if="deliveries.length">
          <div class="list_wrap mypage">
            <table>
              <caption>배송지관리 - 배송지명, 주소, 받는사람, 휴대폰전화번호로 구성</caption>
              <thead class="list-title">
                <tr class="items_wrap">
                  <th class="date-col s_radio" scope="col">선택</th>
                  <th class="date-col a_name" scope="col">배송지명</th>
                  <th class="date-col a_address" scope="col">주소</th>
                  <th class="date-col p_name" scope="col">받는사람</th>
                  <th class="date-col p_phone" scope="col">휴대전화번호</th>
                  <th class="date-col modify" scope="col">수정</th>
                </tr>
              </thead>
              <tbody class="item_list-group">
                <tr class="list-items" v-for="d in deliveries" :key="d.userDeliveryId">
                  <td class="date-col s_radio">
                    <input type="radio" name="selectedDelivery" :value="d.userDeliveryId" v-model="selectedId" />
                  </td>
                  <td class="date-col a_name">
                    <router-link :to="`/mypage/delivery/${d.userDeliveryId}/edit`">{{ d.title }}</router-link>
                  </td>
                  <td class="date-col a_address">
                    <span class="a_tag" v-if="d.defaultFlag"><span class="formBtn default">기본배송지</span></span>
                    <span>{{ d.address }}{{ d.addressDetail ? ' ' + d.addressDetail : '' }}</span>
                  </td>
                  <td class="date-col p_name">{{ d.userName }}</td>
                  <td class="date-col p_phone">{{ d.mobile }}</td>
                  <td class="date-col modify">
                    <router-link :to="`/mypage/delivery/${d.userDeliveryId}/edit`" aria-label="배송지 수정">
                      <img class="icon-img" src="/images/icon/list-modify.png" alt="" />
                    </router-link>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="list-none" v-else>등록된 배송지가 없습니다.</div>
      </div>

      <div class="btn-box many">
        <button type="button" class="blueBtn cancellation" @click="setDefault">기본배송지로 선택<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
        <button type="button" class="blueBtn u-confirm" @click="remove">삭제<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span></button>
      </div>
    </div>
  </section>
</template>

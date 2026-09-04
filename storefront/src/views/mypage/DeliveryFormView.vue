<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { loadDaumPostcode } from '../../utils/daumPostcode'

// AS-IS mypage/deliveryInfo.html의 배송지 추가/수정 팝업을 별도 화면으로 재현
// (Thymeleaf 버전 delivery/write.html과 동일한 축소).
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const errorMessage = ref('')

const form = reactive({
  title: '',
  userName: '',
  phone: '',
  mobile: '',
  post: '',
  address: '',
  addressDetail: '',
  makeDefault: false,
})

onMounted(async () => {
  if (isEdit.value) {
    const list = await api.get('member', '/api/delivery')
    const existing = list.find((d) => String(d.userDeliveryId) === route.params.id)
    if (existing) {
      form.title = existing.title
      form.userName = existing.userName
      form.phone = existing.phone ?? ''
      form.mobile = existing.mobile
      form.post = existing.post ?? ''
      form.address = existing.address
      form.addressDetail = existing.addressDetail ?? ''
      form.makeDefault = existing.defaultFlag
    }
  }
})

async function searchAddress() {
  await loadDaumPostcode()
  new window.daum.Postcode({
    oncomplete(data) {
      form.post = data.zonecode
      form.address = data.roadAddress || data.jibunAddress
    },
  }).open()
}

async function onSubmit() {
  errorMessage.value = ''
  try {
    if (isEdit.value) {
      await api.put('member', `/api/delivery/${route.params.id}`, form)
    } else {
      await api.post('member', '/api/delivery', form)
    }
    router.push('/mypage/delivery')
  } catch (e) {
    errorMessage.value = e.message
  }
}
</script>

<template>
  <section class="changeInfo center">
    <div class="page-title-box" id="contents">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage/delivery">배송지 관리</router-link>
      </span>
      <h2 class="page-title-txt">{{ isEdit ? '배송지 수정' : '배송지 추가' }}</h2>
    </div>

    <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

    <form id="deliveryForm" @submit.prevent="onSubmit">
      <fieldset>
        <div class="add-info-area center">
          <div class="lable-field"><h3><span>배송지&nbsp;</span><span>정보</span></h3></div>
          <div class="info-field">
            <div class="field-row">
              <label for="title">배송지명</label>
              <input type="text" id="title" v-model="form.title" maxlength="50" required placeholder="예: 우리집, 회사" />
            </div>
            <div class="field-row">
              <label for="userName">받는사람</label>
              <input type="text" id="userName" v-model="form.userName" maxlength="50" required />
            </div>
            <div class="field-row">
              <label for="mobile">휴대전화번호</label>
              <input type="text" id="mobile" v-model="form.mobile" maxlength="20" required placeholder="010-0000-0000" />
            </div>
            <div class="field-row">
              <label for="phone">전화번호</label>
              <input type="text" id="phone" v-model="form.phone" maxlength="20" placeholder="선택 입력" />
            </div>
            <div class="field-row">
              <label for="post">우편번호</label>
              <input type="text" id="post" v-model="form.post" readonly />
              <button type="button" class="formBtn" @click="searchAddress">주소찾기</button>
            </div>
            <div class="field-row">
              <label for="address">주소</label>
              <input type="text" id="address" v-model="form.address" readonly required />
            </div>
            <div class="field-row">
              <label for="addressDetail">상세주소</label>
              <input type="text" id="addressDetail" v-model="form.addressDetail" maxlength="100" />
            </div>
            <div class="field-row">
              <label for="makeDefault"><input type="checkbox" id="makeDefault" v-model="form.makeDefault" /> 기본배송지로 설정</label>
            </div>
          </div>
        </div>
      </fieldset>
      <div class="btn-box order">
        <button type="button" class="blueBtn cancellation" @click="router.back()">취소</button>
        <button type="submit" class="blueBtn u-confirm">저장</button>
      </div>
    </form>
  </section>
</template>

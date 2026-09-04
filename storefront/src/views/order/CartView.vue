<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'

// AS-IS cart/index.html 재현 (Thymeleaf 버전 order/cart.html과 동일 출처). AS-IS는 체크박스
// change 이벤트마다 순수 JS로 합계를 재계산하지만, 여기서는 selected Set을 reactive로 두고
// computed로 그룹별 합계/포인트부족 여부를 파생시킨다.
const router = useRouter()
const groups = ref([])
const selected = reactive(new Set())
const qtyEdits = reactive({})
const errorMessage = ref('')
const loading = ref(true)

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await api.get('order', '/api/cart')
    groups.value = data
    selected.clear()
    data.forEach((g) => g.lines.forEach((l) => {
      selected.add(l.cartItemId)
      qtyEdits[l.cartItemId] = l.quantity
    }))
  } finally {
    loading.value = false
  }
}
onMounted(load)

const allSelected = computed({
  get: () => groups.value.length > 0 && groups.value.every((g) => g.lines.every((l) => selected.has(l.cartItemId))),
  set: (val) => {
    groups.value.forEach((g) => g.lines.forEach((l) => (val ? selected.add(l.cartItemId) : selected.delete(l.cartItemId))))
  },
})

function groupSelected(g) {
  return g.lines.every((l) => selected.has(l.cartItemId))
}
function toggleGroup(g, val) {
  g.lines.forEach((l) => (val ? selected.add(l.cartItemId) : selected.delete(l.cartItemId)))
}
function toggleLine(cartItemId, val) {
  if (val) selected.add(cartItemId)
  else selected.delete(cartItemId)
}

function groupSelectedTotal(g) {
  return g.lines.filter((l) => selected.has(l.cartItemId)).reduce((sum, l) => sum + l.lineTotal, 0)
}
function insufficient(g) {
  return groupSelectedTotal(g) > g.givePoint
}

async function changeQuantity(line) {
  const qty = Number(qtyEdits[line.cartItemId])
  if (!qty || qty < 1) {
    alert('수량을 확인해 주세요.')
    return
  }
  try {
    await api.put('order', `/api/cart/items/${line.cartItemId}/quantity`, { quantity: qty })
    await load()
  } catch (e) {
    errorMessage.value = e.message
  }
}

async function deleteSelected() {
  const ids = [...selected]
  if (ids.length === 0) {
    alert('삭제할 답례품을 선택해 주세요.')
    return
  }
  await api.post('order', '/api/cart/items/delete', { cartItemId: ids })
  await load()
}

function goCheckout() {
  const ids = [...selected]
  if (ids.length === 0) {
    alert('답례품을 선택해 주세요.')
    return
  }
  for (const g of groups.value) {
    if (g.lines.some((l) => selected.has(l.cartItemId)) && insufficient(g)) {
      alert(g.locgovNm + '의 포인트가 부족합니다.')
      return
    }
  }
  router.push({ path: '/checkout', query: { cartItemId: ids } })
}

function formatN(n) {
  return new Intl.NumberFormat('ko-KR').format(Math.floor(n ?? 0))
}
</script>

<template>
  <section class="cart_contents">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        <router-link to="/mypage">마이페이지</router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        장바구니
      </span>
      <h2 class="page-title-txt">장바구니</h2>
      <div class="steps-box">
        <div class="step_status cart on"><div class="icon-bg"><span class="sr-only">현재단계 장바구니</span></div></div>
        <div class="step_status payment"><div class="icon-bg"><span class="sr-only">주문결제</span></div></div>
        <div class="step_status complete"><div class="icon-bg"><span class="sr-only">주문완료</span></div></div>
      </div>
    </div>
  </section>

  <section id="contents" class="cart_contents" v-if="!loading">
    <div class="center">
      <p class="error" v-if="errorMessage">{{ errorMessage }}</p>

      <div class="common_none" v-if="groups.length === 0">
        <div class="list-none">
          <img src="/images/icon/empty-cart.png" alt="상품 없음" />
          장바구니에 담겨있는 답례품이 없습니다.
        </div>
      </div>

      <div v-else>
        <div class="cart_top">
          <div class="chk_box check-level1">
            <input type="checkbox" id="selectAll" v-model="allSelected" />
            <label for="selectAll">전체선택</label>
          </div>
          <div class="btn_area">
            <button type="button" class="formBtn del" @click="deleteSelected">선택삭제</button>
          </div>
        </div>

        <div v-for="g in groups" :key="g.locgovCode">
          <div class="mobile_section_bar"></div>
          <div class="contents_wrap">
            <div class="s-contents cart-group">
              <div class="cart_top">
                <div class="chk_box check-level1">
                  <input type="checkbox" :id="'grp-' + g.locgovCode" :checked="groupSelected(g)"
                         @change="toggleGroup(g, $event.target.checked)" />
                  <label :for="'grp-' + g.locgovCode">{{ g.locgovNm }}</label>
                </div>
                <div class="grade-bg">
                  잔여 포인트 : <span class="blue-txt">{{ formatN(g.givePoint) }} P</span>
                </div>
              </div>

              <div class="list_body">
                <div class="list_wrap">
                  <div class="list-title">
                    <div class="date-col chk_date">답례품정보</div>
                    <div class="date-col g_amtprc">
                      <div class="g_info_wrap">
                        <div class="date-col g_info__amount">수량</div>
                        <div class="date-col g_info__price">답례품 포인트</div>
                      </div>
                    </div>
                  </div>

                  <ul class="item_list-group">
                    <li class="list-items" v-for="line in g.lines" :key="line.cartItemId">
                      <div class="date-col g_info">
                        <div class="g_info_wrap">
                          <div class="date-col chk_date">
                            <div class="check_input">
                              <label class="sr-only" :for="'chk-' + line.cartItemId">{{ line.itemName }} 선택</label>
                              <input type="checkbox" :id="'chk-' + line.cartItemId" :checked="selected.has(line.cartItemId)"
                                     @change="toggleLine(line.cartItemId, $event.target.checked)" />
                            </div>
                          </div>
                          <div class="link_wrap">
                            <div class="g_info__img">
                              <img :src="line.thumbnailUrl ?? '/images/thumb.png'" :alt="line.itemName + ' 이미지'" />
                            </div>
                            <div class="g_info__txt">
                              <div class="info_title"><span>{{ line.itemName }}</span></div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="date-col g_amtprc">
                        <div class="g_info_wrap">
                          <div class="g_info__amount">
                            <label class="sr-only" :for="'qty-' + line.cartItemId">주문 수량</label>
                            <input type="number" min="1" :id="'qty-' + line.cartItemId" v-model="qtyEdits[line.cartItemId]" />
                            <button type="button" class="formBtn modify" @click="changeQuantity(line)">변경</button>
                          </div>
                          <div class="g_info__price">{{ formatN(line.lineTotal) }} P</div>
                        </div>
                      </div>
                    </li>
                  </ul>
                </div>
                <div class="cart_bot">
                  <div class="smallTxt">
                    답례품 포인트
                    <span class="bot_P">{{ formatN(groupSelectedTotal(g)) }} P</span>
                    + 무료배송 =
                  </div>
                  <div class="bigTxt">
                    <span class="bot_txt">결제 예정 포인트</span>
                    <span class="bot_price" :class="insufficient(g) ? 'pointRed' : 'pointblue'">{{ formatN(groupSelectedTotal(g)) }}P</span>
                  </div>
                  <span class="pointRed" v-if="insufficient(g)"> ( 주문불가 )</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="btn-box order">
          <button type="button" class="blueBtn cancellation" @click="router.back()">
            취소<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="이전화면으로 이동" /></span>
          </button>
          <button type="button" class="blueBtn u-confirm" @click="goCheckout">
            선택 답례품 주문<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="선택한 답례품 주문페이지로 이동" /></span>
          </button>
        </div>

        <div class="cart_info_txt center">
          <ul>
            <li class="txt__list"><span class="spch">※</span> <span class="list__txt">장바구니답례품은 30일간 보관됩니다. 더 오래 보관하시려면 [관심답례품]으로 등록하세요.</span></li>
            <li class="txt__list"><span class="spch">※</span> <span class="list__txt">장바구니답례품이 품절되면 자동으로 목록에서 삭제됩니다.</span></li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS faq/list.html 재현. 아코디언을 펼칠 때 AS-IS의 addHits처럼 조회수를 AJAX로 올린다.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)
const qInput = ref('')
const openIds = ref(new Set())

const faqType = computed(() => route.query.faqType ?? '')
const sort = computed(() => route.query.sort ?? 'updated,DESC')
const size = computed(() => Number(route.query.size ?? '10'))
const page = computed(() => Number(route.query.page ?? '1'))

async function load() {
  loading.value = true
  qInput.value = route.query.q ?? ''
  try {
    const params = { sort: sort.value, size: String(size.value), page: String(page.value) }
    if (faqType.value) params.faqType = faqType.value
    if (route.query.q) params.q = route.query.q
    data.value = await api.get('admin', `/api/faqs?${new URLSearchParams(params).toString()}`)
  } finally {
    loading.value = false
  }
}
onMounted(load)
watch(() => route.query, load)

function reload(overrides) {
  const query = { ...route.query, ...overrides, page: '1' }
  Object.keys(query).forEach((k) => {
    if (!query[k]) delete query[k]
  })
  router.push({ path: '/faqs', query })
}
function submitSearch() {
  reload({ q: qInput.value })
}
function goPage(p) {
  router.push({ path: '/faqs', query: { ...route.query, page: String(p) } })
}
function toggle(f) {
  const wasOpen = openIds.value.has(f.id)
  const next = new Set(openIds.value)
  wasOpen ? next.delete(f.id) : next.add(f.id)
  openIds.value = next
  if (!wasOpen) {
    api.post('admin', `/faqs/${f.id}/hit`).catch(() => {})
  }
}
</script>

<template>
  <section>
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        고객센터
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        FAQ
      </span>
      <h2 class="page-title-txt">FAQ</h2>
      <p>분류를 선택하시면 해당 항목의 질문 답변을 확인하실 수 있습니다.</p>
    </div>
  </section>

  <CustomerLnb current="faqs" />

  <div class="contents-wrap" id="contents" v-if="!loading && data">
    <div class="center">
      <div class="s-contents list-wrapper">
        <div class="list-select-area">
          <header class="list-search-header">
            <div class="list-count-box">전체 글 : {{ data.totalCount }} 건</div>
            <form class="search-select" @submit.prevent="submitSearch">
              <div class="form-item category">
                <label for="faqType" class="sr-only">분류선택</label>
                <select id="faqType" :value="faqType" @change="reload({ faqType: $event.target.value })">
                  <option value="">분류선택</option>
                  <option v-for="[code, label] in Object.entries(data.faqTypes)" :key="code" :value="code">{{ label }}</option>
                </select>
              </div>
              <div class="form-item">
                <label for="category-where" class="sr-only">구분</label>
                <select id="category-where" aria-label="구분">
                  <option value="title" selected>제목</option>
                </select>
              </div>
              <div class="form-item search-field">
                <label for="q" class="sr-only">검색 키워드</label>
                <input type="search" id="q" v-model="qInput" placeholder="검색 키워드" />
                <button type="submit" class="button search" aria-label="조회"></button>
              </div>
              <div class="m-field">
                <div class="form-item">
                  <label for="sort" class="sr-only">정렬순서</label>
                  <select id="sort" :value="sort" @change="reload({ sort: $event.target.value })">
                    <option value="updated,DESC">최신순</option>
                    <option value="updated,ASC">오래된순</option>
                  </select>
                </div>
                <div class="form-item">
                  <label for="size" class="sr-only">목록수</label>
                  <select id="size" :value="size" @change="reload({ size: $event.target.value })">
                    <option value="10">10개</option>
                    <option value="20">20개</option>
                    <option value="30">30개</option>
                    <option value="50">50개</option>
                  </select>
                </div>
              </div>
            </form>
          </header>

          <div class="list-search-body faq">
            <ul class="list-search-group" v-if="data.faqs.length">
              <li class="list-items dropdown" :class="{ show: openIds.has(f.id) }" v-for="f in data.faqs" :key="f.id">
                <div class="notice-header dropdown-toggle" tabindex="0" @click="toggle(f)" @keydown.enter="toggle(f)" @keydown.space.prevent="toggle(f)">
                  <img class="faq-icon" src="/images/icon/cli-icon_faq-q.png" alt="질문" />
                  <div class="header-faq">
                    <div class="status_c category">{{ f.faqTypeLabel ?? f.faqType }}</div>
                    <div class="faq_txt" style="white-space: normal">{{ f.subject }}</div>
                    <div class="col_group">
                      <div class="status_c date">{{ f.updatedDate }}</div>
                    </div>
                  </div>
                </div>
                <div class="notice-body dropdown-menu" :class="{ show: openIds.has(f.id) }">
                  <div class="notice-wrap">
                    <img class="faq-icon" src="/images/icon/cli-icon_faq-a.png" alt="답변" />
                    <div class="header-faq">
                      <span class="status_c category">&nbsp;</span>
                      <div class="faq_txt">{{ f.content }}</div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
            <div class="list-none" v-else>
              <img src="/images/icon/non-list.png" alt="게시물이 없음" />
              추후 업데이트 예정입니다.
            </div>
          </div>

          <div class="card-pagination" v-if="data.totalPages > 1">
            <div class="pagination_ali">
              <ul class="pagination-frame">
                <li><a class="fist arrow_btn" href="javascript:void(0)" @click="goPage(1)"><img src="/images/icon/paging_btn-first.png" alt="처음" /></a></li>
                <li><a class="prev arrow_btn" href="javascript:void(0)" @click="goPage(Math.max(1, page - 1))"><img src="/images/icon/paging_btn-prev.png" alt="이전" /></a></li>
                <li v-for="p in data.totalPages" :key="p" :class="{ 'selected-page': p === page }">
                  <a class="page-text" href="javascript:void(0)" @click="goPage(p)">{{ p }}</a>
                </li>
                <li><a class="next arrow_btn" href="javascript:void(0)" @click="goPage(Math.min(data.totalPages, page + 1))"><img src="/images/icon/paging_btn-next.png" alt="다음" /></a></li>
                <li><a class="last arrow_btn" href="javascript:void(0)" @click="goPage(data.totalPages)"><img src="/images/icon/paging_btn-last.png" alt="마지막" /></a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

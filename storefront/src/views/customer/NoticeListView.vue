<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS notice/list.html 재현.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)
const qInput = ref('')

const sort = computed(() => route.query.sort ?? 'CREATED_DATE__DESC')
const size = computed(() => Number(route.query.size ?? '10'))
const page = computed(() => Number(route.query.page ?? '1'))

async function load() {
  loading.value = true
  qInput.value = route.query.q ?? ''
  try {
    const params = { sort: sort.value, size: String(size.value), page: String(page.value) }
    if (route.query.category) params.category = route.query.category
    if (route.query.q) params.q = route.query.q
    data.value = await api.get('admin', `/api/notices/list?${new URLSearchParams(params).toString()}`)
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
  router.push({ path: '/notices', query })
}
function submitSearch() {
  reload({ q: qInput.value })
}
function goPage(p) {
  router.push({ path: '/notices', query: { ...route.query, page: String(p) } })
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
        공지사항
      </span>
      <h2 class="page-title-txt">공지사항</h2>
    </div>
  </section>

  <CustomerLnb current="notices" />

  <div class="contents-wrap" id="contents" v-if="!loading && data">
    <div class="center">
      <div class="s-contents list-wrapper">
        <div class="list-select-area">
          <header class="list-search-header">
            <div class="list-count-box">전체 글 : {{ data.totalCount }} 건</div>
            <form class="search-select" @submit.prevent="submitSearch">
              <div class="form-item">
                <label for="category-where" class="sr-only">검색구분</label>
                <select id="category-where" aria-label="구분">
                  <option value="SUBJECT" selected>제목</option>
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
                    <option value="CREATED_DATE__DESC">최신순</option>
                    <option value="CREATED_DATE__ASC">오래된순</option>
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

          <div class="table-container">
            <div class="list-notice-body w3c_v_2410">
              <table>
                <caption>공지사항 - 제목, 등록일, 조회수로 구성</caption>
                <thead class="list-notice-title">
                  <tr class="items_wrap">
                    <th class="date-col title" scope="col">제목</th>
                    <th class="date-col date" scope="col">등록일</th>
                    <th class="date-col view" scope="col">조회수</th>
                  </tr>
                </thead>
                <tbody class="list-notice-group" v-if="data.notices.length">
                  <tr v-for="n in data.notices" :key="n.noticeId">
                    <td>
                      <router-link :to="`/notices/${n.noticeId}`" class="list-items">
                        <div class="notice_tit">
                          <span class="notice-icon" v-if="n.noticeFlag">공지</span>
                          <div class="notice_txt">{{ n.subject }}</div>
                        </div>
                      </router-link>
                    </td>
                    <td><div class="date-col date">{{ n.displayDate }}</div></td>
                    <td>
                      <div class="date-col view_c">
                        <img src="/images/icon/cli-icon_pw-show.png" alt="조회수" class="board_icon" />
                        <span>{{ n.hits }}</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div class="list-none" v-if="!data.notices.length">
                <img src="/images/icon/non-list.png" alt="게시물이 없음" />
                공지사항이 존재하지 않습니다.
              </div>
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

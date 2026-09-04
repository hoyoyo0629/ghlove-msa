<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import CustomerLnb from '../../components/CustomerLnb.vue'

// AS-IS qna/qna-open.html 재현 - 로그인 없이도 누구나 볼 수 있는 공개 Q&A 게시판.
// 마이페이지 "1:1 문의"(QnaView.vue)와 OP_QNA를 공유한다.
const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(true)
const qInput = ref('')

const where = computed(() => route.query.where ?? 'SUBJECT')
const orderBy = computed(() => route.query.orderBy ?? 'CREATED_DATE')
const sort = computed(() => route.query.sort ?? 'DESC')
const size = computed(() => Number(route.query.size ?? '10'))
const page = computed(() => Number(route.query.page ?? '1'))
const orderingValue = computed(() => `${orderBy.value}__${sort.value}`)

async function load() {
  loading.value = true
  qInput.value = route.query.q ?? ''
  try {
    const params = { where: where.value, orderBy: orderBy.value, sort: sort.value, size: String(size.value), page: String(page.value) }
    if (route.query.q) params.q = route.query.q
    data.value = await api.get('admin', `/api/qna/board?${new URLSearchParams(params).toString()}`)
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
  router.push({ path: '/qna/board', query })
}
function submitSearch() {
  reload({ q: qInput.value })
}
function applyOrdering(value) {
  const [ob, s] = value.split('__')
  reload({ orderBy: ob, sort: s })
}
function goPage(p) {
  router.push({ path: '/qna/board', query: { ...route.query, page: String(p) } })
}
function lockedClick() {
  alert('비밀글입니다.')
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
        Q&amp;A
      </span>
      <h2 class="page-title-txt">Q&amp;A</h2>
      <p>해당 Q&amp;A는 기부 관련 사항에 대해 질문을 해주시기 바랍니다.<br />(예: 기부결제오류 , 접속장애, 페이지에러 등....)</p>
    </div>
  </section>

  <CustomerLnb current="qna" />

  <div class="contents-wrap" id="contents" v-if="!loading && data">
    <div class="center">
      <div class="s-contents list-wrapper qna">
        <div class="list-select-area">
          <header class="list-search-header">
            <div class="list-count-box">전체 글 : {{ data.totalCount }} 건</div>
            <form class="search-select" @submit.prevent="submitSearch">
              <div class="form-item">
                <label for="category" class="sr-only">검색구분</label>
                <select id="category" :value="where" @change="reload({ where: $event.target.value })" aria-label="구분">
                  <option value="SUBJECT">제목</option>
                  <option value="USER_NAME">작성자</option>
                </select>
              </div>
              <div class="form-item search-field">
                <label for="search" class="sr-only">검색 키워드</label>
                <input type="search" id="search" v-model="qInput" placeholder="검색 키워드" />
                <button type="submit" class="button search" aria-label="조회"></button>
              </div>
              <div class="m-field">
                <div class="form-item">
                  <label for="ordering" class="sr-only">정렬순서</label>
                  <select id="ordering" :value="orderingValue" @change="applyOrdering($event.target.value)">
                    <option value="CREATED_DATE__DESC">최신순</option>
                    <option value="CREATED_DATE__ASC">오래된순</option>
                    <option value="HITS__DESC">조회수순</option>
                  </select>
                </div>
                <div class="form-item">
                  <label for="page-size" class="sr-only">목록수</label>
                  <select id="page-size" :value="size" @change="reload({ size: $event.target.value })">
                    <option value="10">10개</option>
                    <option value="20">20개</option>
                    <option value="30">30개</option>
                    <option value="50">50개</option>
                  </select>
                </div>
              </div>
            </form>
          </header>

          <div class="list-notice-body table-container qna">
            <table>
              <caption>QNA - 순번, 작성자, 제목, 등록일, 조회수로 구성</caption>
              <thead class="list-notice-title">
                <tr class="items_wrap">
                  <th class="date-col content" scope="col">No.</th>
                  <th class="date-col content" scope="col">작성자</th>
                  <th class="date-col title" scope="col">제목</th>
                  <th class="date-col date" scope="col">등록일</th>
                  <th class="date-col content" scope="col">조회수</th>
                </tr>
              </thead>
              <tbody class="list-notice-group" v-if="data.rows.length">
                <tr v-for="row in data.rows" :key="`${row.qnaId}-${row.type}`">
                  <td class="date-col content">{{ row.no }}</td>
                  <td class="date-col content">{{ row.userName }}</td>
                  <td class="notice_tit">
                    <router-link v-if="!row.locked" :to="`/qna/board/${row.qnaId}`" class="list-items">
                      <span v-if="row.type === 'A'" class="pointRed">
                        <img src="/images/icon/bottom_curved_arrow.png" alt="답글 화살표" />
                      </span>
                      <span>{{ row.subject }}</span>
                    </router-link>
                    <a v-else href="javascript:void(0)" class="list-items" @click="lockedClick">🔒 {{ row.subject }}</a>
                  </td>
                  <td><div class="date-col date" aria-label="등록일">{{ row.createdDate?.slice(0, 8) }}</div></td>
                  <td><div class="date-col content" aria-label="조회수">{{ row.hits }}</div></td>
                </tr>
              </tbody>
            </table>
            <div class="list-none" v-if="!data.rows.length">
              <img src="/images/icon/non-list.png" alt="게시물이 없음" />
              Q&amp;A 정보가 존재하지 않습니다.
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

        <div class="btn-box">
          <button type="button" class="blueBtn u-confirm" @click="router.push('/mypage/qna')">
            글쓰기<span><img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" /></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

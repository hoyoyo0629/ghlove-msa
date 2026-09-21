import { ref } from 'vue'

// 사이트맵 모달 열림 상태(헤더의 전체메뉴 버튼과 푸터 링크가 함께 여는데, AS-IS의 CSS :target
// 방식은 router 이동(pushState)이나 replaceState로는 브라우저가 :target을 갱신하지 않아 안 닫힌다.
// 그래서 공유 reactive 상태로 여닫는다.
export const sitemapOpen = ref(false)
export function openSitemap() {
  sitemapOpen.value = true
}
export function closeSitemap() {
  sitemapOpen.value = false
}

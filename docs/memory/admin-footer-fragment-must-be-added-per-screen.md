---
name: admin-footer-fragment-must-be-added-per-screen
description: AS-IS는 푸터가 전역 레이아웃이라 모든 운영관리 화면에 있는데 TO-BE는 템플릿마다 직접 넣어야 해서 42화면이 빠져 있었다
metadata:
  type: project
---

AS-IS는 `layouts/common/inc_footer.jsp`를 **전역 레이아웃**에서 포함해 모든 운영관리 화면에
푸터(저작권 한 줄)가 깔린다. TO-BE는 레이아웃 데코레이터가 없어 **각 템플릿이 직접**
`<div th:replace="~{fragments/footer :: admin-footer}"></div>`를 넣어야 하고, 빠뜨리면
그 화면만 푸터 없이 뜬다 - 화면을 열어보기 전엔 드러나지 않는다.

**2026-10-12 전수 점검 결과**: `class="opmanager"` 화면 269개 중 **42개가 누락**이었다
(사용자가 관리자매뉴얼에서 발견). 커뮤니티 19 · 이번 고객센터 라운드 산출물 14
(locgov-notice·notice-admin·data-board-admin·maintenance-admin·seller-notice-admin·qna-admin) ·
manual-admin 3 · faq-admin 2 · inquiry-admin 2 · designated 2. 전부 `</body>` 직전에 추가했고
Playwright로 13개 대표 화면에서 `#footer` 렌더를 확인했다.

**Why:** 푸터는 눈에 잘 안 띄는 요소라 화면을 만들 때 빠뜨리기 쉽고, 컴파일·테스트로는
절대 안 잡힌다. AS-IS에는 **예외 없이 있는** 요소라 누락은 곧 parity 불일치다.

**How to apply:**
- 새 운영관리 화면을 만들 때 템플릿 뼈대에 푸터를 **처음부터** 포함한다
  (헤더 `admin-nav :: header()` / lnb / breadcrumb / **footer** 네 조각이 한 세트다).
- 팝업 화면(별도 창, `admin-nav :: header()`를 안 쓰는 템플릿)은 푸터가 없는 게 맞다.
- 점검 한 줄:
  ```bash
  for f in $(grep -rl 'class="opmanager"' templates); do
    grep -q "admin-nav :: header" "$f" || continue
    grep -q "fragments/footer" "$f" || echo "$f"
  done
  ```

---
name: admin-count-title-span-block-css-trap
description: count_title h5 안의 <span>을 th:remove="tag" 없이 쓰면 "총 N건"이 세 줄로 쪼개짐(opmanager.css가 h5 span을 block으로 바꿈) - 5개 화면에서 실제로 터짐, 2026-10-14 전부 수정
metadata:
  type: feedback
---

`admin/.../static/content/opmanager/css/opmanager.css:1123`:

```css
.count_title h5 span { display: block; margin-top: 8px; margin-left: 3px; ... }
```

AS-IS JSP는 `<h5>총 <c:out value='${count}'/>건</h5>`처럼 **태그 없이** 숫자를 찍어서 이 규칙이
문제가 안 됐다. Thymeleaf로 옮기며 `<h5>총 <span th:text="${count}">0</span>건</h5>`처럼 숫자를
`<span>`으로 감싸면, 그 span이 `display:block`이 돼 "총"/숫자/"건"이 **세 줄로 쪼개져 보인다**
(가로 플렉스 레이아웃이 아니라 세로로 깨짐 - 레이아웃 스크린샷으로만 잡힌다, HTML 구조 검토로는
안 보임).

기존 코드 40여 화면 대부분(`member-admin/list.html`, `locgov-admin/list.html` 등)은 이미
`<span th:text="${count}" th:remove="tag">0</span>`처럼 **`th:remove="tag"`**를 붙여 렌더링
결과에서 span 태그 자체를 제거하는 식으로 피해가고 있었다. 그런데 이 패턴을 빠뜨린 화면이
2026-10-14 기준 5곳 있었다: `statistics-locgov/list.html`(관심지자체, 같은 세션 작업분) ·
`designated/approval-log-popup.html` · `featured-admin/list.html` · 신규 작성한
`shop-statistics/sales-locgov-mois.html`/`sales-locgov.html`. 지자체별 구매현황
([[admin-sales-day-statistics-port]]) 작업 중 스크린샷 검증으로 발견해 5곳 전부 수정했다.

**Why**: Thymeleaf 템플릿에서 숫자를 동적으로 찍으려면 보통 `<span th:text="...">`로 감싸는
것이 직관적인데, 이 코드베이스의 AS-IS CSS 중 일부가 "별도 태그 없는 텍스트"를 전제로 짜여
있어서 그 직관이 깨진다.

**How to apply**: `count_title` 영역(`총 N건` 안내문)에 숫자를 넣을 때는 항상
`<span th:text="${count}" th:remove="tag">0</span>` 형태로 쓸 것(span을 완전히 없애는
`th:remove="tag"` 필수). 새 목록화면을 포팅할 때 이 패턴이 맞는지 스크린샷으로 확인하는 걸
체크리스트에 넣을 것 - HTML만 봐서는 깨진 걸 못 잡는다.

---
name: admin-lnb-toggle-script-missing
description: "LNB 섹션 헤더(예: '시스템 관리') 클릭 시 하위메뉴 접기/펼치기 스크립트가 통째로 빠져있던 것을 복원(2026-10-08)"
metadata:
  node_type: memory
  type: project
---

AS-IS `layouts/opmanager/inc_header.jsp`에 섹션 헤더 클릭 토글 스크립트가 있다:

```js
function Lnb(){
    var menu_a = $('.menu > a');
    menu_a.click(function(e) {
        e.preventDefault();
        if (!$(this).hasClass('on')) {
            $(this).addClass('on').next().stop(true,true).slideDown('800');
        } else {
            $(this).removeClass('on');
            $(this).next().stop(true,true).slideUp('800');
        }
    });
}
Lnb();
```

`.menu > a`(LNB 2차 섹션 헤더, 예: "시스템 관리")를 클릭하면 바로 아래 `.depth2`(3차 리프 목록)를
slideDown/slideUp으로 접고 펼친다. TO-BE `fragments/admin-nav.html`의 `lnb()` fragment는
AS-IS와 같은 `.menu > a.on` + `.depth2` 마크업은 이미 옮겨놨지만 **이 스크립트 자체가 아예
없었다** - 사용자가 "클릭해도 토글이 안 되는 것 같다"고 지적해서 발견. 클릭해도 `href="#"`가
없어(서버 렌더 `<a class="on">`에 href 자체가 없음) 아무 반응이 없었다.

**수정**: `admin-nav.html`의 `lnb()` fragment 안, `메뉴 닫기` 앵커 다음에 같은 스크립트를
`th:if="${activeTopMenuId != null}"`로 감싸 추가(AS-IS도 `<c:if test="${main != 'main'}">`
안에만 있다 - lnb 자체가 없는 화면에선 스크립트도 필요 없음).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

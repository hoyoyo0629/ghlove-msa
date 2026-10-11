---
name: admin-locgov-charger-edit-layout-and-breadcrumb-fix
description: "지자체담당자관리(4402)·운영관리자(4501) 상세화면의 수정/목록 버튼 레이아웃 + 메뉴경로 '상세' 누락 수정, 2026-10-08"
metadata:
  node_type: memory
  type: project
---

**①버튼 레이아웃**: AS-IS `locgov-charger/edit.jsp`·`oper-charger/edit.jsp`는 수정·삭제·목록
세 버튼이 **같은 `.flex_box gap-08` 안**에 있다(`<c:if>`가 수정/삭제만 감싸고 목록은 같은
div 안 마지막 자식). TO-BE `person-in-charge/locgov-edit.html`·`oper-edit.html`은 목록 버튼을
`.flex_box` **바깥**(`.btn_all`의 직계자식)으로 빼놨었다 - 같은 flex 컨테이너에 있어야 할 게
분리돼 레이아웃이 달라 보였다. `th:if`를 div 자체에 걸어서 생긴 차이로 보인다. `th:block
th:if`로 바꿔 수정/삭제만 조건부로 감싸고 목록은 항상 같은 flex_box 안에 두도록 복원.
oper-edit.html은 추가로 **`mb15` 클래스도 빠져 있었다**(AS-IS `btn_all btn_right mb15`) - 같이
복원.

**②메뉴경로("상세") 누락**: AS-IS 두 JSP 모두 메인 `$(function(){...})` 안에서
```js
$(".contents .contents_inner").find("div.location a").removeClass("on");
$(".contents .contents_inner").find("div.location").append('> <a href="..." class="on">상세</a>');
```
를 실행해 공통 브레드크럼(`Manager.setLnbHeader()`가 1~3번째 크럼을 "회원관리 > 지자체관리 >
지자체담당자관리"로 채운 뒤) 맨 끝에 4번째 "상세" 크럼을 덧붙인다. **이 스크립트 자체가 TO-BE
두 화면 모두에 없었다**(oper-edit.html은 `$(function(){...})` 블록 자체가 없어서 새로 만들어
넣었다) - 그래서 경로가 3단("회원관리 > 지자체관리 > 지자체담당자관리")에서 멈춰 있었다.
1~3번째 크럼 자체(메뉴 트리·`Manager.setLnbHeader()`)는 **이미 정상**이었다 - `admin.op_menu`에
4000(회원관리)→4400(지자체관리, 그룹헤더)→4402(지자체담당자관리) 체인이 AS-IS와 정확히
일치하는 걸 직접 확인했다. 빠진 건 4번째 크럼 스크립트뿐이었다.

[[admin-lnb-toggle-script-missing]]와 같은 유형의 갭(JSP의 스크립트 블록이 마크업만 옮기고
누락된 경우) - 이번에 2곳(4402·4501) 찾아 고쳤다. 같은 유형이 다른 상세화면에도 더 있을 수
있어 전수점검은 아직 안 했다(필요하면 요청).

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**

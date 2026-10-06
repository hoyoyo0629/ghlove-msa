---
name: admin-inc-common-globals-missing
description: "AS-IS inc_common.jsp가 모든 운영관리 페이지에 깔던 전역(isMobileLayer, userId)이 TO-BE에 없어 Common.popup이 ReferenceError로 죽어 13개 화면의 팝업 버튼이 전부 먹통이었다"
metadata:
  node_type: memory
  type: project
---

**2026-10-06, 사용자가 1406(관리자 권한 승인관리)의 상태·이력 팝업, 1405(권한그룹 관리)의
'권한그룹 생성' 버튼이 "먹통"이라고 신고.** 링크·함수·팝업 화면은 다 있는데 클릭하면 아무 일도
안 일어났다. 근본 원인은 개별 화면이 아니라 공통 전역 누락이었다.

**원인:** AS-IS는 `layouts/common/inc_common.jsp`가 **모든 페이지 하단**에 전역 몇 개를
`<script>`로 깔아둔다. TO-BE 공통 head(`fragments/opmanager-head.html`)는 그중
`RequestContext`·`OP_MANAGER_TIMEOUT` 만 옮겨서, 나머지 둘이 빠져 있었다:
- **`isMobileLayer`** — `op.common.js:642` `Common.popup`의 **첫 줄**이 읽는다. 전역이 없으면
  함수 진입 즉시 `ReferenceError: isMobileLayer is not defined` 로 죽어 **모든 팝업이 실패**했다.
  복사해 온 자산이라 참조를 지울 수도 없다. AS-IS는 `shopContext.mobileLayer`('true'면 true)이고
  운영관리 콘솔은 데스크톱이라 **항상 false** → false면 `window.open`, true면 body 안 iframe 레이어.
- **`userId`** — `op.common.js:2160` `Common.userId()` 가 읽는다(현재 호출부는 없지만 자산이 참조).

**영향 범위:** `Common.popup` 을 쓰는 **운영관리 13개 화면 / 호출 23곳 전부**였다 - 1404 권한그룹
수정, 1405 권한그룹 생성, 1406 상태·이력, 메뉴/공통코드/지자체/배치/이메일설정/회원상세/탈퇴회원/
엑셀다운로드로그/오프라인담당자 등. 한 화면 버그가 아니라 공통 조각 하나로 다 고쳐진다.

**조치:** `opmanager-head.html` 에 `var isMobileLayer = false;` 와
`var userId = ${loginManager?.userId}` 를 추가. 2026-10-02에 같은 유형으로 `RequestContext`
(op.link.js가 읽는 전역)를 이미 한 번 메꿨는데([[asis-screen-port-procedure]] 공통 head 주석),
그때 `isMobileLayer`·`userId` 를 빠뜨린 것이다.

**교훈:** 복사해 온 jQuery 자산(op.common.js/op.manager.js)이 **전역 변수에 의존**하는데 그 전역은
AS-IS에서 inc_common.jsp/inc_head.jsp가 깔아준다. TO-BE 공통 head로 옮길 때 **JSP가 깔던 전역을
전수로 떠서** 넣어야 한다. 한 번에 안 떠서 팝업·링크가 화면마다 조용히 죽는 걸 반복 발견 중이다.
`op.common.js`/`op.manager.js`에서 선언 없이 참조되는 식별자를 grep으로 훑어 남은 게 없는지
점검할 것(확인된 소비: isMobileLayer, userId, RequestContext, OP_MANAGER_TIMEOUT,
OP_MANAGER_TIMEOUT_TYPE, OP_CONTEXT_PATH, OP_LANGUAGE - 전부 지금 head에 있음).

**검증 한계(미완):** 팝업이 실제로 열리는 것은 **로그인 세션이 필요**해 런타임으로 못 눌러봤다
(admin01 평문 비번이 어디에도 문서화돼 있지 않고, 비번 추측 시도는 분류기가 막는다 - 정당하다).
빌드·기동·인라인 가드테스트는 통과. **사용자가 재기동 후 직접 클릭 확인**해야 최종 확정된다.

---
name: admin-opmanager-ajax-needs-csrf-meta
description: "admin에서 op.common.js(AS-IS opmanager 공용 JS) 쓰는 화면은 _csrf/_csrf_header meta 필수, 없으면 모든 $.ajax가 조용히 취소됨"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T06:57:59.403Z
---

**AS-IS opmanager 공용 스크립트 `op.common.js`를 로드하는 admin 화면은 `<head>`에 반드시 아래 두 meta를 넣어야 한다. 없으면 모든 ajax가 발사 전 취소되고 화면이 조용히 빈다.**

```html
<meta name="_csrf_header" content="X-CSRF-TOKEN">
<meta name="_csrf" content="">
```

**Why:** op.common.js의 전역 `$.ajaxSetup({beforeSend})`가 `xhr.setRequestHeader(Common.getCsrfHeader(), Common.getCsrf())`를 호출한다. 이 두 함수는 `<meta name="_csrf_header">`/`<meta name="_csrf">`를 읽어 없으면 `''`를 반환 → `setRequestHeader('', '')`는 **빈 헤더명이라 SyntaxError**로 터지고, beforeSend 예외로 `$.post`가 요청도 못 보내고 중단된다(서버 로그·op_manager_action_log에 흔적 0, 차트/목록 공란). AS-IS opmanager 레이아웃은 Spring Security가 이 meta를 채워 내려줬으나, TO-BE admin은 Spring Security/CSRF가 없다([[as-is-logic-is-the-spec]] 스키마 치환과 같은 성격). 헤더명만 유효하면 되고 토큰은 빈 값이어도 서버가 무시한다.

**How to apply:**
- 2026-10-01 지정기부 월별통계(`designated/analysis-month.html`)에서 이 함정으로 차트가 안 떠서 meta 2개 추가해 해결. [[copy-asis-css-js-assets-verbatim]]로 op.common.js를 그대로 쓰는 화면은 전부 해당.
- 진단법: ajax가 서버에 안 닿고(admin.log·감사로그에 요청 없음) 화면만 비면 CSRF meta 누락부터 의심. 콘솔엔 setRequestHeader SyntaxError가 뜬다.
- admin은 Spring Security가 없다(세션+AdminAuthInterceptor 게이팅). 그래서 CSRF 토큰 자체가 없음=정상.

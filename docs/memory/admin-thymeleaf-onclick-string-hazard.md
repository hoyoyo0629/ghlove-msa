---
name: admin-thymeleaf-onclick-string-hazard
description: "th:onclick(DOM 이벤트 속성)에 문자열 변수식을 넣으면 렌더링 중 TemplateProcessingException으로 응답이 끊긴다 - th:attr로 전환+가드테스트 추가(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

**증상**: 1405 사용자 권한그룹 관리(`/admin/roles`) 목록에서 "수정" 버튼·메뉴경로·행이 1건만
보이는 등 화면이 통째로 깨져 보임. 브라우저는 view-source 백지, DevTools Network
"Failed to load response data"(= `ERR_INCOMPLETE_CHUNKED_ENCODING`).

**원인**: [role-admin/list.html](../../admin/src/main/resources/templates/role-admin/list.html)의
`th:onclick="|fnGrpUpdate('${row.role.authority}')|"`. Thymeleaf 3.1부터 `th:onclick` 등
`th:onXXX`(DOM 이벤트 속성, `StandardDOMEventAttributeTagProcessor`)는 "신뢰할 수 없는 컨텍스트"로
취급되어, 그 안의 `${...}` 변수식이 **숫자·불린이 아니면** `TemplateProcessingException("Only
variable expressions returning numbers or booleans are allowed...")`을 던진다. `authority`가
문자열이라 렌더링 중 예외가 났고, 그 시점엔 이미 헤더·상단 네비 마크업이 응답으로 flush된
뒤라(8KB 버퍼 초과) 에러페이지도 못 띄우고 응답이 그대로 끊겼다(`ERROR
s.e.ErrorMvcAutoConfiguration$StaticView : Cannot render error page ... response has already
been committed`). 서버 콘솔(디버그 로그)에서 그 스택트레이스를 직접 봐야만 확정할 수 있었다 -
DB·템플릿 소스·컴파일 결과만 정적으로 훑어서는 "캐시 문제"로 오판했다
([[check-as-is-source-when-analyzing]] 류와 같은 교훈: 실제 렌더링 결과를 봐야 한다).

**조치(2026-10-07)**: 코드베이스 전체에서 `th:on[a-z]+=` 패턴 10곳(7개 파일: role-admin/list,
codes/list, designated/analysis-locgov×2, content/banner-list, give/give-statistics-{locgov,
operate-list}, survey-admin/form, policy/form, batch-job/list)을 전부 `th:attr="onclick=|...|"`로
전환(이미 community/qna-admin/manual-admin 등 15곳 이상에서 쓰던 안전한 패턴 - th:attr은 이
"신뢰 컨텍스트" 제한이 없다). 숫자 파라미터(batchJobId 등)는 지금은 안 터지지만 같은 함정이라
함께 바꿨다.

**가드**: `ThymeleafOnEventAttributeHazardTest` 추가 - 템플릿에 `th:on[a-zA-Z]+=`가 하나라도
있으면 빌드 실패. [[thymeleaf-js-inline-bracket-hazard]](대괄호2개 가드)와 같은 클래스의
"컴파일로는 안 잡히고 렌더링해봐야 터지는" Thymeleaf 함정.

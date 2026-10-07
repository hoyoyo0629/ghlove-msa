---
name: admin-frontend-is-thymeleaf-not-jsp
description: admin 서비스 프론트는 Vue3 전환 미정 상태로 유지 - AS-IS는 JSP지만 ghlove-msa/admin은 이미 Thymeleaf로 구현됨
metadata: 
  node_type: memory
  type: project
  originSessionId: 1e39a986-9565-4fcd-b13f-11e940dcb77f
  modified: 2026-09-08T08:43:34.658Z
---

admin 서비스 프론트는 Vue3로 전환할지 정해진 바 없어 현 상태를 유지하기로 했다(2026-09-08). 단, "AS-IS와 동일하게 JSP" 는 사실과 다르다: AS-IS `C:\workspace\ghlove\ghlove-web`의 opmanager는 JSP 531개가 맞지만, `ghlove-msa/admin`은 이미 **Thymeleaf 220개**로 구현돼 있고 JSP 파일과 jasper/jstl 의존성은 0이다.

**Why:** admin은 Spring Boot 3.3.4 + jar 패키징이라 JSP가 아예 동작하지 않는다(war + tomcat-embed-jasper로 되돌려야 하는데 Boot가 권장하지 않는 조합). Thymeleaf든 JSP든 서버 렌더링이라는 성격은 같아서, 나중에 Vue3로 갈 때 버리는 양도 동일하다 - 즉 아무것도 안 바꾸는 것이 "미정이니 그대로 두자"의 올바른 이행이다.

**How to apply:** admin 프론트 이야기가 나오면 JSP 전환을 제안하지 말고 Thymeleaf 유지 또는 Vue3 전환 두 갈래로만 논의한다. 이 결정이 [[thymeleaf-duplicate-cleanup-deferred]]의 보류 사유다.

**[2026-10-07 재확인]** 사용자가 작업 중 반복되는 Thymeleaf 결함(th:onclick/인라인JS 함정 등)에
답답함을 표하며 Vue3 전면전환을 문의 → 범위(이미 AS-IS와 맞춘 225개 화면 전부 재작업)를
설명한 뒤 "일단 지금 상태로 유지, **플랫폼 구성 끝나고 SW/웹 기술스택이 확정되면 그때 재검토**"로
명시적으로 재확인했다. 즉 보류 사유가 "미정"에서 "플랫폼/기술스택 확정 이후"로 더 구체화됨 -
다음에 이 얘기가 나오면 그 트리거(기술스택 확정 시점)를 먼저 확인할 것.

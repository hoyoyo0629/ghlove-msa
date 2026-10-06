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

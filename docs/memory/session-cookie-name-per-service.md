---
name: session-cookie-name-per-service
description: 쿠키는 포트를 구분하지 않아 localhost에서 admin·member가 JSESSIONID를 서로 덮어썼다 - 서비스별 쿠키 이름 분리(ADMIN_SESSION/MEMBER_SESSION)
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T02:24:22.615Z
---

**admin과 member를 같이 띄워 쓰면 운영자 화면이 수시로 로그인 페이지로 튕기던 원인은
세션 쿠키 이름이 둘 다 기본값 `JSESSIONID`였기 때문이다.** 쿠키는 **포트를 구분하지 않는다**
(host `localhost`, path `/`가 같으면 같은 쿠키다). 그래서 member(8081)가 세션을 만들면
admin(8086)의 쿠키를 덮어쓰고, 다음 클릭에서 `AdminAuthInterceptor`가 `loginManager`를 못 찾아
`/admin/login`으로 보낸다. 반대 방향도 같다. **타임아웃 문제가 아니다.**

→ 서비스별로 이름을 분리했다(2026-10-06): admin `ADMIN_SESSION` / member `MEMBER_SESSION`
(`server.servlet.session.cookie.name`). 세션을 쓰는 서비스는 이 둘뿐이고
donation·gift·order·point는 `HttpSession`을 쓰지 않는다(gift 판매자 인증은 JWT 쿠키 `GH_AUTH`).
**세션을 쓰는 서비스를 새로 만들면 반드시 고유 이름을 준다.**

**같이 맞춘 것:**
- `server.servlet.session.timeout: 30m` (AS-IS `SessionListener`가 모든 세션에
  `setMaxInactiveInterval(30*60)`을 건다 - TO-BE는 스프링 기본값 30분에 의존하던 것을 명시)
- `tracking-modes: cookie` (admin에 없어서 리다이렉트 URL에 `;ADMIN_SESSION=...`이 붙었다)
- **`OP_MANAGER_TIMEOUT`은 "분"이다.** `op.manager.js`의 `Manager.procSessionTimeout`이
  `값 * 60 * 1000`으로 쓴다. 내가 초로 알고 `'3600'`을 넣어 60시간이 돼 있었다 → AS-IS처럼
  ISMS 설정 `SESSION_TIMEOUT_MANAGER`(기본 60)에서 읽어 내려준다
  (`ManagerAuthAdvice.managerTimeout`, 시드 `database/ddl/seed-admin-isms-session-timeout.sql`).
- 자동 로그아웃이 호출하는 AS-IS URL `/op_security_logout?target=/opmanager`가 TO-BE에 없어
  404였다 → 복사해 온 자산을 고치지 않고 `ManagerAuthController.securityLogout`을 추가했다.

**How to apply:** 증상이 "로그인은 되는데 돌아다니다 튕긴다"면 먼저
브라우저 개발자도구 Application > Cookies에서 **세션 쿠키가 다른 서비스 것으로 바뀌는지** 본다.
서버 로그에는 아무 예외도 안 남는다(정상 리다이렉트라서).

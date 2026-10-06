---
name: production-domain-migration-plan
description: 운영 전환 시 localhost:808x 노출 제거 계획 - Kong은 이미 구축됨, 남은 29개 절대URL은 B군 삭제 이후 처리
metadata:
  node_type: memory
  type: project
---

운영 전환 때 "포트 없이 도메인만 노출" 을 어떻게 달성할지 2026-09-08에 전수 조사한 결과와 처리 순서.

**이미 된 것:** API GW(Kong)는 구축 완료다. `infra/kong/kong-local.yaml`(로컬 Docker) / `infra/k8s/kong.yaml`(K8s) 두 벌이 같은 라우팅 구조를 갖고, 경로 접두사 6개(/member /donation /point /gift /order /admin) + `strip_path: true` + JWT(GH_AUTH 쿠키, member-route만 제외) + rate-limiting(내부 300/분, 민간개방 60/분) + key-auth(개방 API)까지 얹혀 있다. storefront SPA는 `src/api/http.js`의 SERVICE_PREFIX로 상대경로만 쓰므로 **프론트 쪽은 이미 도메인 하나만 보이는 상태**다.

**남은 노출 4종:**
1. 컨트롤러 절대 리다이렉트 **29곳**(donation 7, order 5, admin 2, gift 2, point 1, member 1). 전부 `"redirect:http://localhost:8081/login?target=" + encode("http://localhost:808x" + returnPath)` 패턴. 브라우저에 노출됨.
2. 서비스간 `base-url`(각 application.yml). 서버→서버라 노출 아님, 프로파일 분리만 필요.
3. member OAuth 콜백(application.yml의 onepass return-url, kakao/naver redirect-uri). 노출됨 + 카카오/네이버 콘솔 등록값이라 도메인 확정 시 양쪽 동시 변경.
4. admin의 Kong Admin API `admin-url: http://localhost:8001`. K8s에선 이미 ClusterIP라 맞게 돼 있으나 Ingress에 절대 붙이면 안 됨.

**Why (처리 순서가 중요한 이유):** 29개 절대 URL은 서비스마다 오리진이 달라서(8081≠8082) 절대경로를 쓸 수밖에 없던 것이고, Kong 뒤에서 오리진이 하나가 되면 `redirect:/member/login?target=/donation/...` 상대경로로 충분해진다(각 서비스가 자기 게이트웨이 접두사를 알 `app.gateway.prefix` 프로퍼티 하나만 추가하면 됨). 그런데 이 29곳은 **전부 Thymeleaf loginRedirect 경로**라 [[thymeleaf-duplicate-cleanup-deferred]]의 B군 48개를 지우면 대부분 같이 사라진다. B군보다 먼저 손대면 지울 코드를 리팩터링하는 헛일이 된다. 같은 이유로 InterestLocgovController의 `@CrossOrigin` 2곳과 AuthController/LoginView.vue의 `localhost:808[1-6]` 허용목록도 단일 오리진이 되면 CORS째로 불필요해진다.

**아직 비어 있는 것:** `infra/k8s/`에 storefront 라우트도 6개 서비스 Deployment도 없다(kafka/kong/postgres×6/redis 뿐). 저장소 전체 Dockerfile 0개. Kong 라우트에 `/`를 받는 게 없어 SPA를 어디서 서빙할지 미정 - catch-all 라우트(정적파일 nginx upstream) 추가가 필요하다. K8s ConfigMap의 JWT 시크릿도 평문이라 Secret으로 빼야 한다.

**How to apply:** 순서는 **B군 결정 → ① 상대경로화(+app.gateway.prefix 도입) → SPA 서빙 라우트 추가 → ②③④ 프로파일/Secret 분리**. 이 조사는 이미 끝났으니 다시 grep하지 말고 위 수치를 그대로 쓴다.

---
name: admin-login-gate-whitelist-gaps
description: admin 로그인 게이트가 화이트리스트라 새 화면을 넣을 때마다 빠뜨린다 - 13개 화면이 로그인 없이 열려 있었고 그 중 3개는 NPE 500이었다
metadata:
  type: project
---

admin의 로그인 게이트는 `WebConfig#addInterceptors` 의 **화이트리스트**다
(`adminAuthInterceptor.addPathPatterns(...)` 에 적힌 경로만 보호한다).
**새 운영자 화면을 만들 때 여기에 추가하지 않으면 그 화면은 로그인 없이 열린다.**

## 2026-10-13 전수 점검 결과 - 13개 화면이 게이트 밖이었다

사용자가 전체 재기동 후 **통계 > 지자체별에서 Whitelabel 500**을 만난 것이 단서였다.
`admin.log` 의 실제 예외:
`NullPointerException: Cannot invoke "Manager.getAuthority()" because "manager" is null`
→ `MenuService.isLocgovScoped(MenuService.java:93)` → `GiveStatisticsController.locgov`.

세션이 없는데도 **로그인으로 리다이렉트되지 않고 컨트롤러까지 들어와서** 터진 것이다.
원인은 목록에 `"/give-statistics"` 만 있고 **`"/give-statistics/**"` 가 없었던 것**.

| 경로 | 수정 전 | 비고 |
|---|---|---|
| `/give-statistics/locgov` | **500** | 사용자가 본 화면 |
| `/give-statistics/operate` | **500** | |
| `/admin/internal-inquiry` | **500** | |
| `/give-statistics/all` | 200 | |
| `/admin/content-satisfaction` | 200 | |
| `/admin/delivery-companies` | 200 | 표 12행 렌더 |
| `/admin/ranking` | 200 | |
| `/admin/shipment-returns` | 200 | |
| `/open-api/clients` · `/open-api/stats` | 200 | 민간개방 API **관리화면**(외부 API는 Kong) |
| `/point-history` · `/point-history/daily` | 200 | **표 69행이 그대로 노출** |
| `/statistics/interest-locgov` | 200 | |

전부 `WebConfig` 에 추가해 **13개 모두 302(로그인)** 로 바뀐 것을 확인했고,
공개로 남아야 하는 경로(`/notices`·`/faqs`·`/events`·`/data-board`·`/qna/*`·`/api/*`·
`/auth/cert-login`·`/admin/login`·`/admin/access-denied`·`/admin/manager-requests/{new,complete}`)는
수정 전후 응답이 같아 회귀가 없다.

**Why:** 500은 눈에 띄지만 **200으로 조용히 열려 있는 쪽이 더 위험하다**(로그인 없이 운영 데이터
노출). 컴파일·테스트로는 절대 안 잡히고, 평소 브라우저에는 세션이 남아 있어 개발 중에도 안 드러난다.
재기동으로 세션이 날아간 직후에만 보인다.

**How to apply:**
- 새 운영자 화면을 만들면 **WebConfig 게이트 목록에 `"/경로"` 와 `"/경로/**"` 를 같이** 넣는다
  (둘 중 하나만 넣는 것이 이번 사고의 형태다).
- 점검 한 줄 - 세션 없이 때려 보고 302가 아니면 게이트 밖이다:
  ```bash
  curl -s -o /dev/null -w '%{http_code} %{redirect_url}\n' http://localhost:8086/some-admin-path
  ```
- 전수 점검은 컨트롤러의 클래스 레벨 `@RequestMapping` 값을 모아 같은 방식으로 돌리면 된다
  (메서드 레벨만 모으면 클래스 접두어가 빠져 오탐·누락이 난다 - 이번에 겪었다).

## 같은 날 B안으로 전환 완료 - 화이트리스트 → 블랙리스트

위 13개를 추가한 뒤(A안), 사용자가 **근본 수정(B안)을 선택**해 바로 뒤집었다.

```java
.addPathPatterns("/**")
.excludePathPatterns(/* 공개 경로만 */)
```

이제 실패 방향이 **"빠뜨리면 열린다" → "빠뜨리면 막힌다"** 로 바뀐다.
조용한 정보노출보다 눈에 띄는 차단이 낫다는 판단.

**제외(공개) 목록은 실측으로 뽑았다** - 347개 경로를 세션 없이 전부 호출해 공개로 응답하던 것만:
- 정적: `/content/**` `/css/**` `/images/**` `/fonts/**` `/favicon.ico` `/uploads/**`
  `/admin/uploads/**` · **`/error`**(막으면 오류가 로그인 리다이렉트로 둔갑한다)
- 진입: `/admin/login(/**)` `/admin/logout` `/admin/access-denied` `/auth/cert-login(/**)`
  `/auth-swap` `/admin/manager-requests/{new,complete,confirm}` `/op_security_logout`
- 고객센터 공개: `/notices` `/data-board` `/events` `/faqs` `/qna` (+ 각 `/**`) `/policies/**` `/surveys/**`
- API: `/api/**` `/common/message(s)`

★ **인터셉터는 리소스 핸들러 요청에도 걸린다** - 정적 경로를 빼먹으면 CSS·JS가 로그인으로
튕겨 화면이 통째로 깨진다. 전환할 때 가장 위험한 지점이다.

**검증(전/후 전수 비교)**: 같은 347개 경로를 A안 jar와 B안 jar에서 각각 호출해 비교했다.
- **공개(200) 22개 집합이 완전히 동일**(diff 차이 없음)
- 달라진 것은 **5건뿐이고 전부 "존재하지 않는 경로"의 404 → 302**
  (`/form`, `/multipart/form-data`, 중복결합된 경로 등 - 라우트 추출 과정의 가짜 경로다).
  없는 admin 경로를 404로 알려주지 않게 된 것이라 오히려 낫다.
- 로그인 후 22개 대표 화면(통계·특정사업·커뮤니티·회원·고객센터 등) 전부 200,
  로그인 튕김 0, **정적자원이 302로 새는 일 0**.
- admin `test`+`bootJar` EXIT=0.

**How to apply(바뀐 규칙):** 이제 운영자 화면은 **아무것도 안 해도 보호된다.**
대신 **공개 화면/API를 새로 만들면 `excludePathPatterns` 에 반드시 추가**해야 한다.
안 하면 고객 화면이 로그인으로 튕긴다 - 증상이 바로 보이므로 놓치기 어렵다.

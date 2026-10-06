# 기능 재검증 (2차) — 회원(member) 서비스

검증일: 2026-09-08
기준 문서: `docs/requirements.md`(RFP SFR-002 외), `docs/isp-detailed-design-summary.md`(ISP 상세설계 6차)
대상 커밋: `f567f7c 기능 상세 구현 1차`

## 0. 1차 검증과 무엇이 다른가

1차 감사(`docs/as-is-feature-audit-member.md`, 2026-09-03)는 **소스코드 대조** 방식이었다.
"컨트롤러/엔드포인트가 존재하는가"를 기준으로 판정했기 때문에 "존재하지만 실제로는 실패하는"
기능을 걸러내지 못했다. 이번 2차 검증은 방식을 바꿨다.

- Postgres/Kafka 컨테이너 + member/donation/point 서비스를 **실제 기동**
- 신규 회원을 실제로 가입시켜 **모든 기능을 HTTP로 직접 호출**하고 DB 반영까지 확인
- 실패 시 애플리케이션 로그의 스택트레이스로 근본 원인까지 추적

그 결과 **1차에서 "완료"로 판정했던 항목 중 4건이 실제로는 동작하지 않거나 보안상 열려 있음**을
확인했다. 아래 §2가 그 목록이다.

참고로 프론트(Vue3 storefront) → 백엔드 URL 매핑은 member/donation/point 62개 호출 전부
실제 매핑이 존재한다. 즉 **"링크가 없어서" 깨지는 게 아니라, 호출은 도달하는데 그 안에서 실패**한다.
이것이 1차 검증이 놓친 이유다.

---

## 1. 검증 결과 요약

| 구분 | 건수 |
|---|---|
| 🔴 심각 (기능 불능 또는 보안 결함) | 4 |
| 🟠 중간 (요건 미충족·운영 불가·운영배포시 파손) | 6 |
| ✅ 정상 동작 확인 | 21 |

---

## 2. 🔴 심각 결함

### 2-1. 로그인 사용자의 **모든 DELETE 요청이 500** — 배송지 삭제·회원등급 삭제 전면 불능

가장 영향이 큰 결함이다. 근본 원인은 감사로그 테이블의 컬럼 길이다.

```
member.op_user_action_log.request_method  character varying(5)
```

`"DELETE"`는 6글자다. 감사로그 인터셉터(`AuditLogService`)가 매 요청마다 이 테이블에 INSERT하는데,
DELETE 요청에서 `value too long for type character varying(5)`로 터지고, 이 예외가 그대로 전파되어
**요청 자체가 500으로 실패**한다.

재현 (실제 실행 결과):

```
로그인 상태 DELETE /api/delivery/1003          -> 500
로그인 상태 DELETE /api/admin/user-levels/2    -> 500
비로그인 DELETE /api/admin/user-levels/1       -> 204  (감사로그는 로그인 사용자만 남기므로 통과)
```

로그 근거:

```
DataIntegrityViolationException: could not execute statement
[ERROR: value too long for type character varying(5)]
[insert into op_user_action_log (created_date,login_id,remote_addr,request_method,request_uri,action_log_id) ...]
```

**영향**
- 고객 화면: 마이페이지 → 배송지 관리 → **삭제 버튼이 항상 실패** (`DeliveryListView.vue`)
- 관리자 화면: 회원등급 **삭제 불가** (`user-level-admin`)
- 앞으로 member에 추가되는 모든 DELETE 엔드포인트가 동일하게 깨짐

**조치**: `request_method`를 varchar(10)으로 확장. 더불어 감사로그 INSERT 실패가 본 요청을
실패시키지 않도록 예외를 삼키거나 별도 트랜잭션(`REQUIRES_NEW` + try/catch)으로 분리해야 한다.
감사로그가 서비스 가용성의 단일 실패점이 되어 있는 구조 자체가 문제다.

### 2-2. `/api/admin/**` 전 구간 **무인증** — PII 조회·강제탈퇴·개인정보 파기까지 열림

member 서비스의 `WebConfig`에는 세션 재수화(`SessionRehydrateInterceptor`) 인터셉터만 등록되어 있고
**관리자 API 인증 인터셉터가 없다.** order 서비스에는 `AdminApiAuthInterceptor`(내부 공유 시크릿 검증)가
존재하는데, member/donation/point에는 동일한 방어선이 누락되었다.

쿠키·토큰·헤더 **아무것도 없이** 호출해서 성공한 것들:

```
GET  /api/admin/members/search?keyword=vtest001
  -> {"content":[{"userId":1036,"loginId":"vtest001","userName":"검증테스터",
      "email":"vtest001@example.com","phoneNumber":"01012345678", ...}]}   ← 회원 PII 전량

POST /api/admin/members/1037/withdraw       -> 강제탈퇴 성공 (status_code=WITHDRAWN)
POST /api/admin/members/1036/roles/ROLE_LOCALGOV/revoke  -> 204, 권한 회수됨
POST /api/admin/batch/dormancy              -> {"count":1}
POST /api/admin/batch/data-destruction/withdrawn -> 개인정보 파기 배치 실행
GET  /api/admin/login-log | /user-action-log | /change-log -> 감사로그 전량 조회
GET  /api/admin/secede-users/search         -> 탈퇴사유 포함 전량
POST /api/admin/user-levels                 -> 회원등급 생성
POST /api/users/walk-in                     -> 임의 회원 계정 생성 (§2-4)
```

RFP SFR-002의 "RBAC 적용", "권한 부여·회수 절차 표준화", ISP의 N2SF 보안설계(회원정보 = S등급)를
정면으로 위반한다. 게이트웨이(Kong)에서 `/api/admin/*` 라우트를 막는다 해도, 서비스 자체가
클러스터 내부에서 무방비인 상태는 다층 방어 원칙에 어긋난다.

**조치**: order의 `AdminApiAuthInterceptor`와 동일한 내부 시크릿 검증 인터셉터를 member(및 donation/point)에
추가하고, `/api/admin/**` 전체에 매핑.

### 2-3. **권한 상승 가능** — 일반회원이 지자체 담당자 권한을 스스로 승인해서 획득

`RoleRequestApiController`의 `queue` / `approve` / `reject`는 **로그인 여부만 확인하고 권한 검사를 하지 않는다.**
(소스 주석에도 "승인권자 인증 게이트웨이가 아직 없어 로그인만 요구"라고 적혀 있다.)

실제 재현 — 방금 가입한 일반회원 계정 하나로 전부 수행했다:

```
POST /api/roles/request  {"requestedRole":"ROLE_LOCALGOV"}   -> {"status":"OK"}
GET  /api/roles/queue    -> 본인 신청 건 조회됨 (requestId=4)
POST /api/roles/4/approve -> 204
GET  /api/auth/me        -> roles: ["ROLE_USER","ROLE_LOCALGOV"]   ← 획득 완료
DB   op_user_role: 1036 | ROLE_LOCALGOV
```

지자체 담당자(ROLE_LOCALGOV)는 기부·정산 관련 권한을 갖는 역할이므로 영향이 크다.
답례품 제공자(ROLE_PROVIDER)도 동일하게 획득 가능하다.

부수 문제로 `/api/roles/queue`는 **로그인만 하면 타인의 신청 내역(userId·신청사유)을 전부 노출**한다.

*완화 요소*: `ROLE_ADMIN` 신청은 서버에서 차단된다(`{"message":"신청할 수 없는 역할입니다."}`).
즉 시스템 운영자 권한까지 직접 탈취되지는 않는다.

**조치**: queue/approve/reject에 승인권자 역할 검사 추가 + 본인 신청 자가승인 금지 + 승인 이력에 승인자 기록.

### 2-4. `/api/users/walk-in` 무인증 계정 생성

오프라인 접수 담당자가 현장에서 대신 가입시키는 용도의 API인데 인증이 없다.
누구나 임의의 이름/휴대폰으로 계정을 무제한 생성할 수 있다(§2-2와 동일 원인).

```
POST /api/users/walk-in (인증 없음)
  -> {"userId":1037,"loginId":"walkin433247","tempPassword":"kSY23SQ3KETe"}
```

---

## 3. 🟠 중간 결함

### 3-1. 약관·개인정보 동의 이력이 저장되지 않음

회원가입 화면(`SignupView.vue`)은 동의 7종(이용약관/개인정보/광고성 전체 + SMS·이메일·우편·카카오)을
체크박스로 받는다. 그러나 제출 시 payload는 `{...form, phoneNumber, email}` 뿐이고 **`terms` 객체가
서버로 전송되지 않는다.** 백엔드 `SignupForm`에도 동의 필드가 없고, DB의 `member.op_user_agree`
테이블은 **애플리케이션 코드 어디에서도 참조되지 않는다**(전수 grep 결과 0건).

개인정보보호법상 수집·이용 동의 이력은 보관 의무가 있고, 광고 수신동의는 마케팅 발송의 법적 근거다.
현재는 화면에서 동의를 받되 **증적이 남지 않는다.**

### 3-2. 관리자 "휴면회원 조회"가 기본 조회에서 항상 0건

`searchSleep()`의 날짜 범위 기본값이 **오늘 하루**다(`rangeStart/rangeEnd` → `parseOrToday`).
휴면회원은 정의상 최종 로그인일이 1년 이상 지난 계정이므로, **기본 조회는 구조적으로 절대 결과가 없다.**

```
휴면 전환 배치 실행 -> {"count":1}  (vtest002가 DORMANT로 전환됨)
GET /api/admin/sleep-users/search                                  -> totalElements: 0   ← 있어야 정상
GET /api/admin/sleep-users/search?fromDate=2024-01-01&toDate=2024-12-31 -> totalElements: 1
```

소스 주석은 "AS-IS 화면 그대로"라고 설명하지만, 운영자가 기간을 넓히기 전엔 아무것도 안 보이므로
"휴면회원 관리 기능이 안 된다"는 민원으로 직결된다. 기본값을 "전체" 또는 "최근 3년"으로 바꿔야 한다.

### 3-3. 회원등급 마스터가 0건인데 가입 시 `levelId=1`을 하드코딩 — 죽은 참조

```
member.op_user_level  -> 0건
MemberService.signup() -> detail.setLevelId(1)   (하드코딩)
결과: op_user_detail.level_id = 1  이지만 참조 대상 등급이 존재하지 않음
```

회원등급 관리 API/화면은 이번에 추가되어 동작하지만(§4 참조), **등급 데이터가 하나도 없어
등급 기능 자체가 무의미한 상태**다. 또한 `levelId=1` 하드코딩은 RFP의 "하드코딩 금지" 원칙 위반이다.

### 3-4. 재외국민 구분 실질 미구현

RFP SFR-002는 통합 계정체계 대상으로 "일반회원, **재외국민**, 지자체 담당자, 답례품 제공자, 시스템운영자"를
명시한다. DB에 `op_user.foreign_status_code` 컬럼은 있으나 **member 서비스 Java 코드와 storefront 전체에서
이 값을 읽거나 쓰는 곳이 단 한 곳도 없다.** 가입/로그인 화면에 "재외국민도 금융인증서로 이용 가능"이라는
**안내 문구만** 존재한다. 재외국민 전용 경로인 금융인증서 인증도 §3-5처럼 미연동 상태라, 실제로
재외국민이 가입할 방법이 없다.

### 3-5. 외부 인증 진입점이 SPA를 이탈 + `localhost:8081` 하드코딩

로그인 화면의 카카오/네이버/금융인증서/간편인증 버튼이 **절대 URL로 하드코딩**되어 있다.

```html
<a href="http://localhost:8081/login/kakao">
<form action="http://localhost:8081/login/finance-cert">
<form action="http://localhost:8081/login/simple-auth">
```

- 운영(Kong 게이트웨이) 배포 시 **100% 파손**된다. 다른 모든 호출은 `/member/...` 접두사 규칙
  (`vite.config.js` 프록시 = Kong strip_path)을 지키는데 여기만 예외다.
- 로컬에서도 SPA(5173)를 벗어나 8081의 Thymeleaf 페이지로 이동하며, 그 페이지는 CSS가 깨진 채
  "연계는 코드 구현이 완료되었지만…"이라는 안내만 띄우고 **돌아올 링크가 없다.**

연계 자체가 `enabled=false` 목업인 것은 방화벽 미개통 상 합리적이나, **URL 하드코딩은 별개의 실제 결함**이다.

### 3-6. 금액성 데이터의 조회 실패를 조용히 `0`으로 대체

`PointClient.balanceOf()`, `DonationClient.mySummary()` 등은 호출 실패 시 예외를 삼키고 `0` / 빈 목록을
반환한다(코드 주석상 의도된 설계). SFR-001의 "서비스 오류 전파 방지" 취지에는 부합하지만,
**포인트 잔액·기부 총액처럼 금액성 값이 장애 시 사용자에게 "0원"으로 표시**된다. 실제로 point 서비스가
꺼진 상태에서 `/api/auth/me`가 `"pointBalance":0`을 정상 응답으로 반환하는 것을 확인했다.

RFP SFR-009는 이런 조회를 **CQRS ReadModel**로 제공하라고 요구한다. 올바른 해법은 동기 호출 +
무음 fallback이 아니라 member 로컬 ReadModel을 이벤트로 동기화하는 것이다. 최소한 "일시적으로
조회할 수 없습니다" 상태를 UI에 구분 표시해야 한다.

---

## 4. ✅ 정상 동작 확인 (실호출 검증)

| # | 기능 | 검증 방법 / 결과 |
|---|---|---|
| 1 | 회원가입 | 신규 가입 성공, `op_user`+`op_user_detail` 저장, **한글 정상**(기존 DB의 깨진 이름은 과거 CLI 테스트 흔적으로 확인됨) |
| 2 | 아이디 중복확인 | 기존ID `{"available":false}` / 신규ID `{"available":true}` |
| 3 | 로그인 / 로그아웃 | 세션+쿠키 발급, 로그아웃 후 `{"loggedIn":false}` |
| 4 | 토큰 갱신 | `POST /api/auth/refresh` → `{"status":"OK"}` |
| 5 | **계정 잠금(5회 초과)** | 4회까지 "불일치", 5회째 잠금 전환, 6회째 "이용할 수 없는 계정", DB `status_code=LOCKED`, `login_fail_count=5` |
| 6 | 비밀번호 찾기 | 본인확인(이름+휴대폰) → 인증번호 → 재설정 → **잠금 자동 해제**(`LOCKED`→`ACTIVE`, fail_count=0) → 새 비번 로그인 성공 |
| 7 | 아이디 찾기 | 인증번호 검증 후 `{"status":"OK","loginId":"vtest001"}`, 틀린 코드는 정상 거부 |
| 8 | 비밀번호 정책 | 연속문자 차단 확인(`NewPass123!` → "3개 이상 반복/연속 문자 불가") |
| 9 | **MFA** | 활성화 → 로그인 시 `MFA_REQUIRED`+마스킹 번호 반환, 미인증 상태에선 `loggedIn:false`, 틀린 코드 거부, 정상 코드로 인증 완료 |
| 10 | 프로필 조회/수정 | 조회·수정 모두 정상 |
| 11 | 비밀번호 확인/변경 | 확인 204/400 분기 정상, 변경 후 새 비번 로그인 성공 |
| 12 | 배송지 등록/수정/기본배송지 | 등록·수정 정상, 기본배송지 전환 시 **단일성 유지 확인** (삭제만 §2-1로 불능) |
| 13 | 권한 신청 | 신청 정상, **`ROLE_ADMIN` 신청은 차단**(정상 방어) |
| 14 | 권한 회수 | `revoke` 호출 시 `op_user_role`에서 제거 확인 |
| 15 | 휴면 전환 배치 | 최종로그인 365일 경과 계정 → `DORMANT` 전환, 기준일수는 **공통코드(`SYSTEM_CONFIG/DORMANT_INACTIVE_DAYS`)에서 조회**(하드코딩 금지 원칙 준수) |
| 16 | 휴면 셀프 해제 | `/reactivate` → `ACTIVE` 복귀 |
| 17 | 회원탈퇴 | 본인 탈퇴 성공, `WITHDRAWN`+`leave_date`, **탈퇴사유/코드 저장 및 관리자 조회에서 확인** |
| 18 | 개인정보 파기 | 탈퇴회원 파기·로그 익명화 배치 실행, **파기 이력 조회 정상**(파기 필드 목록·사유·일시 기록) |
| 19 | 감사 로그 3종 | 로그인로그(성공/실패/잠금 사유별), 액션로그, 변경로그(`ROLE_GRANTED: ROLE_LOCALGOV` 등) 모두 기록·조회 확인 |
| 20 | 현장(walk-in) 즉석가입 | 임시ID/비번 발급, 한글 이름 정상 저장 |
| 21 | 회원등급 CRUD | 생성·조회·수정 정상 (삭제만 §2-1로 불능), 아이콘 업로드 경로 존재 |

### 1차 감사에서 "미구현"이던 항목 중 해소된 것

1차 감사(`as-is-feature-audit-member.md` §1-B)는 "admin 서비스에 회원 통합 관리 화면이 전혀 없다"를
핵심 gap으로 지적했다. 이후 커밋에서 해소되었음을 확인했다 — admin에 `member-admin`,
`secede-user-admin`, `sleep-user-admin`, `user-level-admin`, `honor-user-admin` 템플릿과
대응 컨트롤러(`MemberAdminController` 등)가 존재하고, member의 `/api/admin/*` API와 연결되어 있다.
다만 그 API가 §2-2처럼 무인증이라는 점이 새로 드러난 문제다.

---

## 5. 권고 조치 순서

| 순위 | 항목 | 근거 |
|---|---|---|
| 1 | `op_user_action_log.request_method` varchar(10) 확장 + 감사로그 실패 격리 | §2-1, 고객 기능이 실제로 실패 중 |
| 2 | member/donation/point에 관리자 API 인증 인터셉터 추가 | §2-2, §2-4, PII·파기·강제탈퇴 노출 |
| 3 | 역할 승인 API에 승인권자 검사 + 자가승인 금지 | §2-3, 권한 상승 |
| 4 | 동의 이력 저장(`op_user_agree`) 및 프론트 payload 포함 | §3-1, 법적 증적 |
| 5 | 외부 인증 진입점 URL을 `/member/*` 상대경로로 교체 | §3-5, 운영 배포 시 파손 |
| 6 | 휴면회원 조회 기본 기간 변경, 회원등급 시드 데이터 정비 | §3-2, §3-3 |
| 7 | 포인트/기부 요약을 ReadModel로 전환(또는 최소한 오류 상태 표시) | §3-6, SFR-009 |
| 8 | 재외국민 구분 처리 설계·구현 | §3-4, SFR-002 명시 요건 |

---

## 검증 환경 메모

- 기동: `ghlove-postgres`(15432) / `ghlove-kafka`(9092) 컨테이너 + member(8081)·donation(8082)·point(8083) 부트 JAR
- 테스트 계정 `vtest001`/`vtest002`/`walkin433247` 및 생성한 배송지·회원등급 데이터는 **검증 후 전량 삭제**하여
  `op_user` 22건 원상복구 완료.
- curl에서 한글 입력 시 Git Bash가 CP949로 인코딩해 400이 발생하므로, 본 검증은 UTF-8 파일을
  `--data-binary @file` / `--data-urlencode key@file`로 전달했다. (초기에 관측된 400은 앱 결함이 아님)

# member 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적/독자**: AS-IS를 "서비스로직도 화면단도 똑같이" 재현하기 위한 전수 갭 목록. 방식 `[[as-is-parity-exhaustive-audit-method]]`. 상태: **O**/**X**/**부분**/**확인**. 작성 2026-09-21 (진행중).
>
> **⚠ 진행중** — 아래 플로우 인벤토리 기준으로 순차 대조. 이 문서가 완성돼야 member 구현 착수(전 서비스 목록화 후 구현 원칙).

## 0. 플로우 인벤토리 (AS-IS `users/*.html` + JoinController/UserController)

| 플로우 | AS-IS 화면 | MSA | audit 상태 |
|---|---|---|---|
| 로그인 | `login.html` | `LoginView.vue` / `AuthApiController`·`MemberService.loginWithMfaCheck` | **부분(진행중, §1)** |
| 회원가입 | `join.html` | `SignupView.vue` / `JoinController`대응 | 미착수 |
| 아이디/비번찾기 | `find-idpw.html` | `FindIdPwView.vue` | 부분(탈퇴회원 차단만 확인됨) |
| 회원정보수정 | `modify.html` | `ProfileView.vue` | 미착수 |
| 탈퇴 | `secede.html` | `WithdrawView.vue` | **완료(이번 세션)** — 개인정보삭제·관심삭제·CI백업·권한삭제 재현 |
| 비밀번호 만료변경 | `login.html` 팝업 | `LoginView.vue` 팝업 | **완료(이번 세션)** — 3필드·4규칙·6개월갱신 |
| 소셜(원패스/카카오) 가입·해지 | `onepass-*.html`·`secede-kakao.html` | (미대응) | 확인(외부연계 축소 여부) |
| 전자서명(공동인증서) | `sign-certificate.html` | (미대응) | 확인(외부연계 축소 여부) |

## 1. 로그인 (login.html / MemberService.loginWithMfaCheck) — **대부분 O**

MSA `MemberService`가 AS-IS 라인번호(op.saleson.js:1169/1182/1201, AuthController:1200/1232)까지 인용하며 충실 재현.

| # | AS-IS 검증 | MSA | 상태 |
|---|---|---|---|
| 1-1 | 탈퇴/차단/가입대기 계정 즉시 차단 | 즉시 에러("차단된 계정 상태") | **O** |
| 1-2 | 휴면 계정 → 휴면해제 유도(SLEEP_USER) | DORMANT → SLEEP 신호 | **O** |
| 1-3 | 로그인 실패 5회 초과 → 계정 잠금 | `MAX_LOGIN_FAIL_COUNT` 잠금 | **O** |
| 1-4 | 비밀번호 만료/임시비번 → 변경요구 | PASSWORD_EXPIRED/TEMP 신호 | **O**(이번 세션 팝업 완성) |
| 1-5 | 비밀번호 보안도 4규칙 | `validatePasswordComplexity` 4규칙 | **O** |
| 1-6 | SNS(카카오/네이버, passwordType P)은 만료·잠금 예외 | 동일 예외 처리 | **O** |
| 1-7 | MFA(다중인증) | `loginWithMfaCheck` 인증번호/시간초과 | **O** |
| 1-8 | 오프라인 담당자 대민페이지 차단 | "운영관리에서 로그인" 차단 | **O** |

## 2. 회원가입 (join.html / MemberService.signup) — **대부분 O**

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 2-1 | 아이디 중복확인 | "이미 사용 중인 아이디입니다" | **O** |
| 2-2 | 본인인증 필수 | "본인인증을 완료해야 회원가입" (단 로컬 `identity-verification-bypass`) | **O(로컬우회)** |
| 2-3 | CI 중복(이미 가입) 차단 | "이미 가입된 본인인증 정보입니다. 아이디 찾기를…" | **O** |
| 2-4 | 비번 확인 일치 + 4규칙(숫자영문기호/9-20/반복연속/아이디포함) | 동일 4규칙 | **O** |
| 2-5 | 약관 동의 | SignupView 재현 | **확인(경미)** |

## 3. 아이디/비밀번호 찾기 (find-idpw.html) — **축소(CI)**

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 3-1 | **아이디찾기: 금융인증서/휴대폰 본인인증 필수** ("본인인증 안한 계정은 아이디 찾기 불가") | **이름+휴대폰 조회**(CI 미가용) | **부분(축소)** |
| 3-2 | 비번찾기: 본인인증 후 재설정 | 이름+휴대폰 확인 후 재설정 | **부분(축소)** |
| 3-3 | 탈퇴회원 아이디/비번찾기 제외 | 탈퇴 시 이름/전화 NULL로 자동 제외 | **O**(이번 세션 확인) |

> 3-1/3-2는 **외부 본인인증(금융인증서/휴대폰 CI) 미개방에 따른 물리적 축소**. 연계 개방 시 재검토 — 물리차단이라 사용자 확인 대상.

## 4. 회원정보수정 (modify.html / ProfileView) — 미착수(경미 예상)

비번변경(현재비번확인·새비번 4규칙·현재와 다름)은 MSA에 존재 **O**. 프로필 필드 수정 상세는 2차.

## 5. 소셜(원패스/카카오)·전자서명 — **축소(deferred)**

`[[member-service-deferred-items]]` 참고 — 소셜 연동해지 UI·전자서명(공동인증서)은 외부연계 미개방으로 미대응. 개방 시 착수(사용자 확인 대상).

---

## 6. ★ member 최종 갭 목록 (확정)

**member는 핵심 인증·계정 로직이 매우 충실히 재현돼 있어 순수 재현 갭이 거의 없음.** 남은 것은 대부분 **외부연계 축소**:

| 갭 | 상태 | 성격 |
|---|---|---|
| 아이디/비번찾기 본인인증(금융인증서/휴대폰) | 부분 | 외부 CI 축소 — 개방 시 |
| 회원가입 본인인증 실연계 | 로컬우회 | `identity-verification-bypass` — 개방 시 |
| 소셜 연동해지 UI / 전자서명 | 미대응 | deferred, 외부연계 |
| 약관동의·정보수정 상세 필드 | 확인(경미) | 2차 대조 |
| (이벤트발행·조회모델·Keycloak) | deferred | `[[member-service-deferred-items]]` |

> **member audit 완료.** 순수 AS-IS 재현 갭은 거의 없고(로그인/가입/비번/탈퇴 O), 나머지는 외부연계 축소(개방 시 사용자 확인). 다음: point 전수조사.

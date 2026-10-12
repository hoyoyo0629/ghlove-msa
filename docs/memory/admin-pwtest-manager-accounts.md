---
name: admin-pwtest-manager-accounts
description: Playwright 등 자동화 검증용으로 만든 admin 테스트 계정 2개(pwtest_sys/pwtest_loc) - 비밀번호·역할·위치
metadata:
  type: reference
---

2026-10-12 지자체공지사항(5110) 화면을 Playwright로 검증하면서 `admin.op_manager`에 전용
테스트 계정 2개를 직접 추가했다(기존 `admin01`류 14개 계정은 평문 비번이 어디에도 문서화돼
있지 않아 추측 시도 자체가 부적절 - [[admin-op-manager-migration-deferred]]).

| user_id | login_id | 비밀번호 | authority | locgov_code | 용도 |
|---|---|---|---|---|---|
| 900001 | `pwtest_sys` | `Test1234!` | ROLE_ADMIN_1 | NULL | 시스템(비지자체) 권한 뷰 검증 |
| 900002 | `pwtest_loc` | `Test1234!` | ROLE_ADMIN_6 | `11230` | 지자체 담당자(LOC) 권한 뷰 검증 |

둘 다 `status_code='ACTIVE'`, bcrypt 해시(`BCryptPasswordEncoder` 기본 강도 10)로 저장했다.
로그인은 2단계(ID/비번 → 이메일 인증번호)인데 개발 프로파일은 `devCode`를 응답에 그대로
실어주므로, 로그인 화면의 `#devCodeSkipBtn`(개발모드 전용 버튼)을 누르면 인증번호 입력 없이
바로 통과한다.

**Why:** 두 역할(LOC 스코프 vs 시스템 무제한)의 화면 분기(등록/삭제 버튼 노출, 조회 범위)를
한 세션 안에서 실제 로그인 플로우로 오가며 확인하려면 비밀번호를 아는 계정이 필요했다.
기존 계정 재사용은 비번 미상이라 불가능했고, 새 계정 생성은 "추측"이 아니라 "새 자격증명
발급"이라 분류기 제약과 무관하다.

**How to apply:** 앞으로 admin 화면을 Playwright로 검증할 때 재사용한다(삭제하지 않고 남겨
둠 - DB가 공유 서버라 다른 개발자도 쓸 수 있다). 역할별 화면 분기를 바꾸는 작업이면 이 두
계정으로 양쪽 다 확인할 것. 계정을 지우거나 비번을 바꿀 경우 이 메모도 갱신해야 한다.

---
name: admin-op-manager-migration-deferred
description: "admin.op_manager AS-IS 실데이터 이관 보류 - login_id/password/user_name까지 암호화라 옮겨도 안 읽힘, 사용자가 패스 결정(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

로그 화면 6개에 AS-IS 샘플 데이터를 적재한 뒤, "관리자ID가 비어 보이는 문제"를 근본적으로
고치려면 `admin.op_manager`도 AS-IS 데이터로 채워야 하지 않겠냐는 이야기가 나와 확인했다.

**막힌 이유**: AS-IS `op_manager`는 `login_id`/`password`/`user_name`까지(이메일·전화번호뿐
아니라) pCrypto로 암호화돼 있다. 그대로 옮겨도 "관리자ID" 칸에 `^`tymaIU1m4FaERtwiGPbSgg==`
같은 암호문이 뜬다 - TO-BE에 복호화 모듈이 없어서다([[admin-log-screens-asis-sample-data]]에
적어둔 "빈 칸으로 보일 수 있다" 캐뱃이 "암호문으로 보인다"로 바뀔 뿐, 실제로 읽히는 상태가
되지는 않는다).

**규모**: `op_manager` 자체 약 13,495건(전국 농협/수협 지점 담당자 포함). 관련 표 중
`op_manager_hist`(~1,500)/`op_manager_login`(~1,000)/`op_manager_password_log`(~2,000)/
`op_manager_notice`(~5)는 양이 적당하지만, `op_manager_login_email`(~40,000)과 특히
`op_manager_action_log`(**5,700만 줄**)은 전수 이전 대상이 전혀 아니다.
`op_manager_bakup`/`mig_op_manager`는 AS-IS 자체 백업/마이그레이션 스테이징 표라 대상 제외.

**현재 TO-BE 상태**: `admin.op_manager`에 로그인 테스트용 계정 14개(admin01, hoyoyo0629 등
user_id 1000번대/9700번대 - 전부 합성 테스트 계정, AS-IS 실데이터 아님)가 있다. AS-IS
user_id 범위(3608, 4997472 등)와 겹치지 않아 충돌 걱정은 없지만, 이 발견 이후 사용자가
"패스하자"고 결정 - 이관을 보류했다.

**재론의 시 체크할 것**: (1) 복호화 모듈을 만들 것인지(AS-IS pCrypto 키 필요, 별도 과제),
(2) 복호화 없이도 쓸모가 있는 범위(예: user_id/locgov_code/psitn_dept_nm처럼 암호화 안 된
컬럼만 참조하는 기능)가 있는지, (3) 관련 표 중 어디까지(앞서 언급한 "적당한 양" 4개) 포함할지.

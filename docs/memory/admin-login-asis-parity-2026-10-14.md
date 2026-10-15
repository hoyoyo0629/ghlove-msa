---
name: admin-login-asis-parity-2026-10-14
description: 관리자 로그인(/admin/login) AS-IS login_main.jsp 재이식 - 결과코드 응답·로컬 인증번호 자동입력·권한요청 진입·잠김화면·비밀번호 변경 팝업·관리자 공지 팝업·로그인 국민비서 문자. 남은 판단(EMPTY_CI) 포함
metadata:
  type: project
---

2026-10-14 사용자 지시 "관리자 로그인부터 AS-IS랑 비교분석해서 수정". AS-IS `/opmanager/login`은 NetFunnel 중간 페이지, 실제 화면은
`/opmanager/login-main`(login_main.jsp).

**바뀐 응답 계약(테스트용 curl 절차도 바뀜)**: `POST /admin/login/check`(loginId,password) → `{data:{code}}`(AS-IS 결과코드
SUCC/REQ/REQ_NOT/ERR_ROCK/ERR_MNG_PASS/ERR_USER_PASS/ERR_USER_NOT/ERR/PASS_CHANGE_*) → `POST /admin/login/email` →
`{data:"SUCC"+이메일, devCode}`(이메일 연계 꺼진 로컬만 devCode) → `POST /admin/login/verify`(code) → `{data:"SUCC",redirect}`.
화면은 devCode가 오면 자동 입력해 바로 로그인(AS-IS IS_LOCAL_DEV 250324 우회와 같은 방식).

**이식한 것**: 화면 verbatim(국민비서 안내·안내문구·alert 문구·error 파라미터 알림·아이디저장 포커스), 발명 요소 제거("권한 요청하기"
링크·빨간 오류줄·개발모드 안내/버튼). REQ_NOT → `POST /admin/manager-requests/form`(아이디·비번으로 회원 확인, 세션에 신청자 저장 -
대민 로그인 없이 신청). `/admin/login-lock`(계정 잠김 해지 안내). 비밀번호 변경 팝업 `/admin/login/popup/temp-password-change` +
저장 `/admin/login/temp-password-change`(Manager에 PASSWORD_TYPE·PASSWORD_EXPIRED_DATE 매핑, 만료 = 오늘+ISMS LIFE_TIME_PASSWORD).
관리자 공지 팝업 API `/common/manager/notice/list`(AS-IS '%Y%m%d%H%m%s' 분자리=월 오타까지 그대로) + AS-IS 15건 적재
(`migration-admin-op-manager-notice-asis-load.sql`, 전부 기간 만료라 지금은 안 뜸 - 15102 공지 팝업도 이 API). 로그인 성공 시
국민비서 MANAGER_LOGIN 문자(같은 USER_ID 회원, 수신동의 무시 - AS-IS).

**남은 것/구조 문제**:
- ★EMPTY_CI 미이식: AS-IS는 관리자=회원(같은 USER_ID·같은 계정)이라 권한 7·8이 아니면 회원 CI 없을 때 차단. TO-BE op_manager는 회원과
  별개 계정이라 시스템관리자 테스트계정 전부 CI 없음 → 그대로 넣으면 다 막힘. 사용자 판단 대기. 같은 이유로 MANAGER_LOGIN 문자는
  우연히 같은 USER_ID인 회원(admin01=1000 ↔ testuser01)에게 갈 수 있다.
- 비밀번호 변경 팝업은 TO-BE가 PASSWORD_TYPE 'T'·만료일을 아무 데서도 채우지 않아 실제로는 안 뜬다(관리자 등록·초기화·오프라인담당자 생성 쪽, 전수조사).
- 잠김 해지: AS-IS 안내는 "비밀번호 찾기"(회원 비번=관리자 비번)인데 TO-BE 관리자 비번은 별도라 그 경로로 안 풀린다.
- 인증서 등록/삭제 버튼은 로그인 필요 화면(/admin/my-cert)으로 감 - AS-IS 로그인 전 등록화면(/opmanager/user/pki/form) 미이식.
  AS-IS는 회원 정보(생년월일+휴대폰+CI, 오프라인은 OP_MANAGER.emp_id)로 본인 확인 후 DN을 **OP_USER**에 저장 → 계정 연결 결정에 묶임.
- 권한 요청 폼은 문구 차이가 아니라 구조 차이: 소속구분 라디오+구분별 칸(은행·지점·지점연락처/지정기부부서/복지센터), 생년월일 행,
  이메일 안내문. TO-BE ManagerRequest에 psitnCode·dsgncntrPartId 등 없음, 승인은 AS-IS(회원에 권한 부여)와 달리 새 관리자계정+임시비번 발급.
- ★위 구조 문제(EMPTY_CI·로그인문자·잠김해지·비번팝업·인증서등록·권한요청폼)는 같은 날 A안으로 전부 해소 → [[admin-manager-member-account-link]].
- 2026-10-14 사용자: 관리자↔회원 계정 연결은 RFP SFR-002·ISP 검토 후 상의하기로. 기존 결정은 docs/design-decisions.md §1(2026-09-09
  개인/업무계정 분리). 인증서 등록·권한 요청 폼은 이 결정 뒤에 이식.
- AS-IS 정확한 구조: OP_MANAGER는 별도 테이블이지만 승인 시 회원 정보(USER_ID·LOGIN_ID·PASSWORD 해시·이름·이메일·휴대폰)를
  복사(insertManagerByUserSync)하고, 회원 비번 변경/찾기 시 "관리자동기화"로 OP_MANAGER 비번도 갱신. 승인 메뉴 1406(1·3·4 전체, 5는 자기 지자체 부담당자만).
- ISP 원문 확인(pdftotext로 텍스트 있는 쪽만): Ⅲ-12(PDF p.204) 목표 서비스 구성에서 **회원관리 서비스**에 운영자 관리·지자체 담당자 관리·
  오프라인 담당자 관리·답례품제공자 회원관리·인증서등록/해지·아이디/비번 찾기가 들어 있고, Ⅲ-217 회원 애그리거트에 op_manager_login 포함.
  → TO-BE는 관리자 계정이 admin 소유라 "서비스 소유" 측면은 ISP와 다름(A/B 선택과 별개 쟁점, PM 확인 대상).
관련 [[service-logic-full-audit-planned]], [[report-structural-landmines-to-user]].

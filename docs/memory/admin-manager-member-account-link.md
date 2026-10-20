---
name: admin-manager-member-account-link
description: 2026-10-14 A안 적용 - 관리자(OP_MANAGER)는 회원(OP_USER)과 같은 USER_ID·아이디. 승인 복사·비번 동기화·CI·인증서 DN이 회원 기준. 테스트계정 번호 변경(admin01=1113, operator01=1114 등)
metadata:
  type: project
---

2026-10-14 사용자 결정 "A안으로 진행"(RFP SFR-002 "전체 계정 통합", ISP Ⅲ-12 회원관리에 운영자 관리 포함 근거). AS-IS 그대로
관리자 = 같은 번호의 회원 계정 복사본.

**구현**
- member `ManagerAccountLinkService` + `/api/admin/members/{id}/manager-sync`, `next-user-id`, `manager-account`, `password-from-manager`,
  `manager-profile`, `tel-number/clear`, `pki-user`, `{id}/dn`, `{id}/dn/delete`, `by-dn`. User에 MBER_FIN_DN, UserDetail에 TEL_NUMBER 매핑.
- 회원 비번 변경(마이페이지·찾기·로그인 시 변경) → `MEMBER_PASSWORD_CHANGED`(member.lifecycle, 해시만) → admin `MemberPasswordSyncListener`
  (그룹 admin-manager-sync)가 관리자 비번·유형·만료일 갱신 + 실패횟수 0 = AS-IS "관리자동기화"(잠김 해지 경로).
- admin Manager.userId 자체 채번 제거(@GeneratedValue 삭제) - 승인=신청 회원 번호, 오프라인 담당자=member가 만든 회원 번호, 관리자 직접등록=회원 시퀀스.
- 로그인: EMPTY_CI(7·8 제외), 잠김은 실패횟수만(상태 LOCKED로 안 바꿈 - LOCKED는 '중지'와 같은 값), 임시비번 팝업 저장 시 회원 비번도.
- 인증서: `/admin/user/pki/form`(AS-IS pki/form.jsp verbatim, 로그인 전), DN은 회원에 저장. 로그인 `/admin/login/cert`(certType P/F) →
  ERR_MNG_NOT이면 `/admin/manager-requests/pkiForm(Fin)`로 권한요청. SDK 대신 인증서 파일 업로드 유지. update/delete는 세션의 본인확인 회원·읽은 DN만 허용(AS-IS는 무검증).
  TO-BE 발명 `/admin/my-cert`·CERT_SUBJECT_DN 매핑 제거.
- 권한요청 폼 verbatim(소속구분 라디오, ROLE_ADMIN_8 제외, 10/11 분기 스크립트 유지), `create` JSON, 대민 JWT 진입(`/new`·`/complete`) 제거.
  승인 결과 AS-IS 코드(ERR_STS 3/4 문구). ★`/admin/manager-requests/confirm`이 로그인 게이트 예외에 들어가 있어 무로그인 승인 가능했던 것 막음.
- 오프라인 담당자: 등록 시 회원 먼저 생성, 수정 시 회원 이름·이메일·휴대폰 동기화, 삭제 시 회원 지점연락처 비움, 임시비번은 회원·관리자 둘 다 'T'.

**데이터**: `database/ddl/migration-admin-manager-member-link-test-accounts.sql` 적용. admin01 1000→1113, operator01 1001→1114,
locgovtest2 1002→1015, hoyoyo0629 9708→1016(관리자번호 담는 34개 칸 같이 이동). 나머지는 같은 번호로 회원 생성(비번=관리자 해시),
7·8 제외 테스트 CI 'TEST-CI-아이디', 생년월일 19800101, 휴대폰 010-7777-번호끝4자리(인증서 등록 화면 확인용).
locgovtest2·hoyoyo0629은 회원·관리자 비번이 서로 다른 채 남아 있음(다음 비번 변경 때 맞춰짐).

**승인 범위(AS-IS getReqstSeCode)**: 1·3 전체 / 5 자기 지자체의 6 신청만 / 7 → 8 신청만 / 그 외(4 포함) 0건. 목록·상세·confirm 모두 적용
(그전 TO-BE는 누구나 전체를 보고 승인 가능했음). 분석 문서: claude.ai Docs "고향사랑e음 계정 유형별 생성·승인 분석"
(https://claude.ai/code/artifact/b9670dea-4671-461b-b6eb-80d0e87d82b5).

**로컬 Kong**: CUBRID 매니저가 8001을 써서 컨테이너를 `-p 8000:8000 -p 18001:8001`로 재생성(옛 경로 마운트 오류도 해소),
admin은 `admin/config/application.yml`(git 제외, .git/info/exclude)로 `ghlove.kong.admin-url=http://localhost:18001` 덮어씀.

관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-login-asis-parity-2026-10-14), [[tooling-and-local-env]].

**2026-10-15 후속**: ①운영관리자(4501) 수정이 이름·이메일을 null로 덮어쓰던 버그 수정(AS-IS updatePersonInCharge는 안 건드림, guest01 복구). ②회원정보수정(ProfileView)에 AS-IS [본인인증] 버튼·"미완료" 문구·비밀번호변경 차단 이식 - 로컬은 `POST /api/profile/mobile-auth/test-bypass`(dev bypass 설정 켜졌을 때만 TEST-CI-아이디). 관리자 로그인 EMPTY_CI가 보내는 화면이라 막다른 길이었음. 로컬 가입("본인인증 없이 진행")한 회원은 CI가 없어 관리자 로그인 시 이 화면으로 감(AS-IS 흐름 그대로).
③가입 화면 "본인인증 없이 진행(테스트용)"도 이제 dev bypass 설정일 때 TEST-CI-아이디/TEST-DI-아이디를 넣는다(MemberService.signup) - 그 전 로컬 가입 계정(guest03 등)은 CI 없음, 회원정보수정 [본인인증]으로 채움.

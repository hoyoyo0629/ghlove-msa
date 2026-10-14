---
name: member-sbscrb-se-code-general-mismatch
description: "[해결 2026-10-14] OP_USER 가입구분이 발명값 'GENERAL'이던 것 → AS-IS SBSCRB_SE(100 온라인/200 오프라인)로, OP_USER.LOCGOV_CODE 매핑·저장 신설(G_INSTT_CODE 313건 적재). 회원구분 표시는 코드 아닌 상수 '일반회원'"
metadata:
  type: project
---

**AS-IS 규칙(소스 확인)**:
- 가입구분 SBSCRB_SE_CODE: 온라인·간편가입 = join-mapper `updateUserInfo`의 '100', 기탁서 즉석가입 = `insertOffgive`의 '200', 탈퇴 = NULL.
- 회원 지자체코드 LOCGOV_CODE: 가입·회원정보수정 = 주소검색 admCd 앞 5자리 → `G_INSTT_CODE.locgov_mapng_code`
  (`updateUserInfo`/`updateUserEtcInfo`, 수정화면은 재검색 안 하면 저장값을 다시 보내 재매핑, 못 찾으면 NULL),
  즉석가입 = 행정지 지자체(psitnLocgovCode, 매핑 없음), 탈퇴 = NULL. AS-IS에서도 **저장만 하고 읽는 화면/로직은 없다**.
- 회원가입·수정 화면 "회원구분"은 코드가 아니라 화면 상수 `디폴트등급: ["일반회원"]`.

**TO-BE 수정(2026-10-14)**: User.locgovCode 매핑, MemberService(signup/walk-in/updateProfile/withdraw)·ExternalLoginService,
member DonationClient.locgovMapngCode(donation `/api/offgive/locgov-mapng-code`, 조회 실패는 예외 - null로 삼키면 저장값을 지운다),
storefront Signup/Profile의 jusoCallBack 7번째 인자 admCd, 즉석가입은 donation→member로 psitnLocgovCode 전달.
`USER_TYPE` 코드 라벨 조회(AuthApi /auth/me, ProfileApi)는 상수 `MemberService.MEMBER_LEVEL_LABEL`로 교체.
데이터: `migration-donation-g-instt-code-asis-load.sql`(AS-IS export 313건, 표가 0건이었다),
`migration-member-sbscrb-se-code-asis.sql`(GENERAL→100 67건, walkin→200 7건, 즉석가입 지자체 4건 채움).

**How to apply:** 온라인 가입자 기존 67건의 LOCGOV_CODE는 원천값이 없어 NULL - 회원정보 수정 저장 시 채워진다.
member.op_common_code의 USER_TYPE(GENERAL 등)은 이제 쓰는 곳이 없는 TO-BE 발명 코드(지우지는 않았다).

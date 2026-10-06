---
name: receive-sms-encoding-split
description: "TO-BE가 수신동의 컬럼을 Y/N과 0 두 가지로 쓰고 있어 운영자화면 오표시·문자 발송 누락이 생긴다(AS-IS는 0=수신, 1=비수신)"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-04T23:48:29.245Z
---

**AS-IS 정본**: 수신여부 컬럼은 **`0`=수신, `1`=비수신**이다
(`saleson.shop.user.domain.GeneralCustomer` 주석 "이메일 수신여부 (0: 수신, 1:비수신)").

**TO-BE는 두 갈래로 쓰고 있다**(2026-10-05 발견, 미수정):
- 쓰기: `member/service/MemberService.java:652`는 **`Y`/`N`**, `member/service/ExternalLoginService.java:214`는 **`0`**
- 읽기: `member/web/ProfileApiController.java:62`는 `"Y".equals(...)`,
  admin `templates/member-admin/details.html`·`info-access.html`은 `== '0'`(AS-IS대로 이식한 쪽)

**지금 벌어지는 일**: 일반 가입으로 수신동의한 회원은 `Y`가 저장되어
① 운영자 회원상세에서 **"비동의"로 잘못 표시**되고 ② Q&A 답변 국민비서 문자 대상에서도 빠진다
([[ips-sms-sender-lives-in-admin]]은 AS-IS대로 `'0'`만 동의로 본다 - 그쪽을 고치는 게 아니다).

**고치려면**: member 쓰기·읽기를 `0`/`1`로 통일 + **기존 행 변환**(`Y`→`0`, `N`→`1`).
같은 의심이 `receive_email`·`receive_pbanc`·`receive_kakao`에도 있어 네 컬럼을 함께 봐야 한다.
데이터 변환은 사용자 판단 대상이라 보류 중이다([[commit-only-when-asked]]와 같은 선).

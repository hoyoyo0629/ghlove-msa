---
name: admin-mail-config-asis-data-loaded
description: "이메일 설정(mail-config) 10개 템플릿 중 실제 등록 콘텐츠가 있는 3개(포인트소멸예정·관리자권한승인·거절)를 AS-IS export로 적재 완료(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

AS-IS `OP_MAIL_CONFIG`는 [[admin-date-format-opdate-helper]]류와 달리 템플릿 10종 중
**실제 제목+본문이 등록된 건 3개뿐**이다(나머지는 행 자체가 없음, 미등록 상태 - AS-IS 그대로
비워 둔다). 사용자가 CUBRID 개발DB에서 export한 `OP_MAIL_CONFIG_202610071417.sql` 기준으로
`migration-admin-mail-config-asis-data.sql`에 담아 적재했다:

- `expiration_point`(포인트소멸예정) - created_date 20221219
- `manager_request_approval`(관리자 권한 승인) - created_date 20221117
- `manager_request_reject`(관리자 권한 거절) - created_date 20221117

셋 다 `BUYER_SEND_FLAG`/`ADMIN_SEND_FLAG` = **'N'(미발송)** 까지 AS-IS 그대로다 - 발송을 켜는
건 이 작업 범위가 아니다([[as-is-parity-includes-disabled-state]]). 본문은 AS-IS의 인라인
HTML 이메일 템플릿(로고·대체코드 `{site_name}`/`{login_id}`/`{admin_role}`/`{reject_resn}`/
`{expiration_point}`/`{expiration_date}`/`{search_date}`/`{name}` 등)을 글자 그대로 옮겼다.

기존에 사용자가 화면 테스트용으로 만들어 둔 `order_deposit_wait`(title='입금대기',
buyer_subject='test') 행은 건드리지 않았다 - 사용자 테스트 데이터이고 이번 요청(3개 템플릿)
범위 밖.

같은 화면에서 바로 전에 고친 "메일 대체코드" 팝업의 `bankVirtualNo` 라벨 오류
(`MailTemplateCodes.java`, "가상계좌번호"→AS-IS 실제값 "입금은행정보")는 코드 수정이고,
이 메모는 그 다음에 처리한 **데이터** 적재 건이다.

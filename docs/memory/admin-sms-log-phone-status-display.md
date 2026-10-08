---
name: admin-sms-log-phone-status-display
description: "문자전송이력 목록의 전화번호·처리상태 표시를 AS-IS(TifIpsSndngMDisplay)와 동일하게 수정(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

AS-IS 소스(`ghlove-web`/`saleson.common.sms.domain.TifIpsSndngMDisplay`)를 대조해
문자전송이력(`/admin/send-sms-logs`) 목록의 두 컬럼을 바로잡았다.

1. **전화번호**: 암호화/복호화 문제가 아니었다. AS-IS는 `PRVC_IDNTFC_INFO`(회원 CI, 암호화됨)를
   보여주는 게 아니라 발송내용(`SNDNG_CNTNTS`)의 **마지막 `|` 구분 항목**에서 전화번호를
   파싱해 보여준다(`getPhoneNumber()` - 숫자만 뽑아 10~11자리 패턴이면 원문을, 아니면 빈
   문자열을 돌려준다). TO-BE는 `prvcIdntfcInfo`를 그대로 바인딩하고 있던 **필드 오바인딩
   버그**였다.
2. **처리상태**: AS-IS `getEsbStatus()`가 `N→대기`, `S→성공`, `F→실패`로 변환한다. TO-BE는
   `esbStatusCd` 코드값을 그대로 찍고 있었다.

**수정**: `IpsSendingMaster` 엔티티에 `@Transient` getter 두 개 추가 -
`getPhoneNumberText()`(AS-IS `getPhoneNumber` 로직 그대로, 정규식 `^\d{3}\d{3,4}\d{4}$`)와
`getEsbStatusText()`(AS-IS `getEsbStatus` 그대로). `send-log-admin/sms-list.html`에서
`item.prvcIdntfcInfo` → `item.phoneNumberText`, `item.esbStatusCd` → `item.esbStatusText`로
교체.

**참고**: `esbInitTime`/`esbComptTime`(전송시작·완료일시)은 AS-IS도 `DateUtils.datetime()`으로
포맷하는 별도 getter(`getEsbInitTimeF`/`getEsbComptTimeF`)가 있는데, 이번엔 손대지 않았다
(사용자가 전화번호·처리상태 두 가지만 지적했음 - 날짜 포맷도 같은 화면에 또 문제가 있을 수
있어 다음에 확인 필요).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

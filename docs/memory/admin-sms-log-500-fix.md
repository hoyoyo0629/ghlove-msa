---
name: admin-sms-log-500-fix
description: "문자전송이력(시스템관리) 500에러 - IpsSendingMasterRepository의 (:x is null or ...) JPQL이 null LocalDateTime 바인딩 때 타입추론 실패, 조건부 조립으로 수정(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

시스템관리 > 문자전송이력(`/admin/send-sms-logs`) 접속 시
`PSQLException: could not determine data type of parameter $6` 500에러.

**원인**: `IpsSendingMasterRepository.search`가 `(:startDate is null or m.infoCrtDt >= :startDate)`
형태의 정적 JPQL이었다 - `svcId`(String)는 cast 없이도 우연히 괜찮았지만 `startDate`/`endDate`
(LocalDateTime)가 null일 때 PostgreSQL이 바인드 파라미터 타입을 못 정한다.
[[admin-excel-download-log-500-fix]]와 완전히 같은 버그 패턴(LocalDateTime/Long은
`cast(:x as Type)`로도 안 고쳐진다 - [[hql-null-param-needs-cast]] 참고).

**수정**: `IpsSendingMasterRepository`에서 `search` `@Query` 메서드를 제거하고(나머지
`nextListSn()`만 남김), 같은 테이블을 쓰는 `SmsIpsService`(기존에 발송 적재만 하던 서비스)에
`EntityManager` 기반 조건부 JPQL 조립 메서드로 옮겼다 - 값이 null이 아닐 때만 조건절+파라미터를
추가한다(`QestnarRepository`/`BatchLogRepository`/`PrivacyAccessLogService`와 같은 패턴).
`SendLogAdminController.smsList`는 이제 `ipsSendingMasterRepository.search(...)` 대신
`smsIpsService.search(...)`를 호출한다.

**검증**: `SmsIpsServiceTest.searchAllowsNullDateRange` - 실제 서비스 메서드를
`startDate=null, endDate=null`로 직접 호출하는 `@SpringBootTest`(MOCK 웹 환경)를 작성해
통과 확인(psql 텍스트 프로토콜 검증은 [[admin-excel-download-log-500-fix]] 사건으로
불충분하다고 판명됐으므로 반복하지 않음).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

---
name: admin-excel-download-log-500-fix
description: "엑셀다운로드사유관리(1411) 500에러 수정 - PrivacyAccessLogRepository의 null 파라미터 타입추론 실패([[hql-null-param-needs-cast]] 적용)"
metadata:
  node_type: memory
  type: project
---

`/admin/excel-download-logs` 접속 시 500. admin.log 실제 스택트레이스로 원인 확인:
`org.postgresql.util.PSQLException: ERROR: could not determine data type of parameter $4`.

`PrivacyAccessLogRepository.searchExcelDownloadLogs`의 `(:from is null or p.createdAt >= :from)`
패턴이 [[hql-null-param-needs-cast]]와 같은 문제 - **1차 시도(cast) 실패**: `cast(:managerId as
Long)`처럼 양쪽 occurrence를 cast로 감쌌더니 "could not determine data type" 에러는 없어졌지만
재기동 후 **새 에러** `cannot cast type bytea to bigint`가 났다 - Hibernate가 null Long 값
자체를 bytea로 바인딩해버려서 cast(bytea→bigint)가 거절당한 것(String의 cast는 되는데 Long은
안 되는 비대칭 - 정확한 내부 이유는 못 밝혔다).

**2차(최종) 수정**: cast를 버리고 다른 레포지토리(QestnarRepository·BatchLogRepository 등)와
같은 **조건부 쿼리 조립** 방식으로 전환 - `PrivacyAccessLogRepository`에서 이 메서드를 완전히
빼고, `PrivacyAccessLogService`에 `EntityManager`를 직접 넣어 managerId/from/to가 있을 때만
그 조건과 파라미터를 문자열로 붙인다. null 값 자체를 바인딩하는 경로가 없어지므로 타입추론
문제가 원천적으로 발생하지 않는다.

**검증 교훈**: 1차 수정 때 "컨텍스트 기동 성공 + psql PREPARE/EXECUTE 성공"으로 검증했다고
판단했는데 실제로는 틀렸다 - psql의 `EXECUTE(... NULL ...)`은 텍스트 프로토콜 리터럴 NULL이라
JDBC 바이너리 프로토콜의 실제 바인딩 방식과 다르다(가짜 안심). **이번엔 실제 리포지토리 메서드를
`@SpringBootTest`(webEnvironment 기본값=MOCK, NONE은 request-scope 빈 때문에 실패)로 직접
호출하는 회귀테스트(`PrivacyAccessLogServiceTest`)를 추가해 진짜로 재현·검증했다** - 1차
수정 때는 이 테스트가 없어서 실패를 못 잡고 재기동 후에야 사용자가 발견했다.

**상태**: admin compileJava+test(PrivacyAccessLogServiceTest 포함)+bootJar 통과. **재기동 필요: admin.**

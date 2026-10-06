---
name: possible-db-change-firstdb-unconfirmed
description: "[미확정] TO-BE DB가 또 바뀔 수 있다는 얘기(관리파트). '퍼스트DB'?라는 제품 언급 - 확인 필요"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T02:19:34.967Z
---

**[미확정·소문 단계]** 2026-10-01 사용자 전언: TO-BE DB가 **또 바뀐다는 말**이 관리 파트에서 나옴. 제품명이 **"퍼스트DB"(FirstDB?)** 비슷하게 들렸다는데 사용자도 재확인 예정.

- 현재 TO-BE DB = PostgreSQL 17(`ghlove-postgres` 컨테이너). AS-IS = CUBRID.
- "퍼스트DB"가 어떤 제품인지 Claude도 확신 못 함(국산 DBMS Tibero/CUBRID/Altibase/SUNDB/GoldiLocks 등과의 관계 불명). **지어내지 말고** 벤더/영문표기 확인 필요.

**영향 판단(선제):** DB 교체되어도 이식 작업 대부분 유효. 실제 영향 범위는 ① DDL 방언(`database/ddl/*.sql`) ② 네이티브/raw SQL ③ 시퀀스·타입매핑. JPA 기반이라 방언설정으로 상당부분 흡수. front/back/static·기능 parity는 DB-독립.

**대응 방침:** DB-독립적인 이식(커버리지 원장 → 누락보완)을 그대로 진행. DB 확정 시 DDL 레이어만 별도 대응. 확정되면 이 메모 갱신.

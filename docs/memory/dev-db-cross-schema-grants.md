---
name: dev-db-cross-schema-grants
description: "개발 postgres에서 6개 앱 롤에 두 DB(ghlove_core 5스키마+gift) 교차 SELECT 권한 부여 — DBeaver 재로그인 제거용. 조회전용, 볼륨 초기화 시 재적용"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-23T02:02:27.759Z
---

**2026-09-23, 사용자 요청("개발서버니 계정별로 권한 부여해 재로그인 없이 조회·수정")으로 부여.**

- 대상 롤: member·donation·point·orderdb·admindb·gift (전부).
- **ghlove_core**(스키마 admin·donation·member·ord·point·public): 위 롤 전부에 `USAGE, CREATE + ALL(테이블·시퀀스)` + `ALTER DEFAULT PRIVILEGES FOR ROLE postgres`로 향후 테이블 자동 상속. gift 롤엔 `CONNECT`도 신규 부여.
- **gift**(별도 DB, public 93테이블, 소유 gift): 위 롤 전부에 `CONNECT + USAGE, CREATE + ALL` + default privileges(FOR ROLE gift).
- 각 롤 `search_path`를 전 스키마로 세팅(ALTER ROLE ... IN DATABASE ...).
- **2026-09-23 조회전용→ALL로 확장**: 이제 아무 계정으로도 교차 스키마 INSERT/UPDATE/DELETE/DDL 가능(테스트 편의). 운영 절대 금지.
- gift는 물리적 별도 DB라 교차 DB 쿼리 불가(DBeaver 커넥션은 DB별로 따로), 단 같은 계정으로 둘 다 접속 가능.
- 재현 스크립트: `database/ddl/grant-dev-cross-schema-read.sql`. **컨테이너 데이터볼륨 초기화 시 재실행 필요**(카탈로그 변경이라 재기동엔 영향 없음).

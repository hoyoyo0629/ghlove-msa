---
name: asis-table-dump-path
description: "AS-IS 테이블 덤프/실데이터 export 로컬 경로 - DDL전용 550개 폴더 + [2026-10-07] 테이블별 실데이터 대량 export 폴더 추가"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-07T03:52:00.000Z
---

AS-IS(CUBRID 운영) 테이블 DDL이 로컬에 통째로 있다:
`C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\db-dump\테이블dump\<table>.sql` (550개)

**이 550개 파일은 전부 `DROP TABLE` + `CREATE TABLE`만이고 INSERT가 0건이다**(2026-10-02 확인). 이전에 "INSERT 덤프"라고 적어둔 건 틀렸고, 그걸 근거로 `mig_op_menu.sql`(19줄)을 보고 "AS-IS에 기부금 운영현황 메뉴가 없다"고 오판한 사례가 있다. **쓰임**: 테이블·컬럼·주석(한글 설명)·타입·PK 확인. 스키마 대조에는 1순위로 유효하다.

**★[2026-10-07] 실데이터는 이제 `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB` 바로 밑에
테이블별로 대량 export돼 있다** (`<TABLE>_<timestamp>.sql`, UTF-8 `INSERT INTO ... VALUES` 형식,
단일 테이블 1GB 넘는 것도 있음 - `_g_cntr__...sql`). 사용자가 "필요한거 있으면 말만 해, 사무실
일 때 직접 export 해줄게"(반복 확인, 2026-10-02/2026-10-07) - **AS-IS 실데이터 확인이 필요하면
먼저 이 폴더에 이미 받아둔 파일이 있는지 보고, 없으면 사용자에게 요청한다.** 전체 디스크를
`find /`로 뒤지는 식의 비효율적 탐색은 하지 말 것(2026-10-07 사용자 피드백 - 직접 물어보는 게 빠름).
- 받은 것(일부): `OP_MENU`/`OP_MENU_RIGHT`([[menu-visibility-verify-against-live-asis]]),
  `OP_COMMON_MESSAGE`([[asis-message-catalog-loaded]]), `OP_COMMON_CODE`, `OP_CONFIG_ISMS`,
  `OP_FEATURED`, 그리고 2026-10-07에 커뮤니티·기부통계 등 대량 테이블(`_g_*`, `_dntn_*`, `_event_temp_` 등).
- 적재법: `perl -pe` 로 테이블명에 스키마를 붙이고 `psql -f` (psql에 `-c "BEGIN;"`를 같이 주면 커밋 안 되니 `-f`만 쓸 것).
- 문구/라벨/코드값은 **내가 짓지 말고 verbatim 확인**([[copy-as-is-verbatim-never-invent]]).
- ★데이터뿐 아니라 **테이블 이름 자체도 AS-IS 기준으로 검증**할 것 - TO-BE가 지은 이름(예:
  `ADMIN_COMMON_CODE`, AS-IS에는 없었다)으로 별도 테이블을 만들어 화면이 그쪽을 읽고 있으면
  실데이터를 다른 쪼긴 (올바른 이름의) 테이블에 넣어도 화면엔 안 보인다 - 2026-10-07
  공통코드 사례([[admin-common-code-asis-sync-2026-10-07]]).

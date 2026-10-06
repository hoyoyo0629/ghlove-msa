---
name: asis-table-dump-path
description: "AS-IS 테이블 덤프 로컬 경로 - [2026-10-02 정정] 550개 파일 전부 CREATE TABLE만이고 INSERT 0건. 실데이터는 사용자 export 요청으로만 확보 가능"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-02T09:02:36.716Z
---

AS-IS(CUBRID 운영) 테이블 DDL이 로컬에 통째로 있다:
`C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\db-dump\테이블dump\<table>.sql` (550개)

**[2026-10-02 정정] 이 550개 파일은 전부 `DROP TABLE` + `CREATE TABLE`만이고 INSERT가 0건이다.** 사용자도 "as-is dump는 데이터 없이 DDL만 있더라"라고 확인. 이전에 "INSERT 덤프"라고 적어둔 건 틀렸고, 그걸 근거로 `mig_op_menu.sql`(19줄)을 보고 "AS-IS에 기부금 운영현황 메뉴가 없다"고 오판한 사례가 있다.

- **쓰임**: 테이블·컬럼·주석(한글 설명)·타입·PK 확인. 스키마 대조에는 1순위로 유효하다.
- **실데이터가 필요하면 사용자에게 export를 요청한다** - "필요한거 있으면 내가 직접 조회해서 export 해줄테니 말만 해"(2026-10-02). 쿼리를 적어서 요청하면 같은 폴더 상위(`1. DB\<TABLE>_<timestamp>.sql`)에 UTF-8 `INSERT INTO <TABLE> ("col",...) VALUES` 형식으로 떨어진다(iconv 불필요).
- 받은 것: `OP_MENU`/`OP_MENU_RIGHT`([[menu-visibility-verify-against-live-asis]]), `OP_COMMON_MESSAGE`([[asis-message-catalog-loaded]]).
- 적재법: `perl -pe` 로 테이블명에 스키마를 붙이고 `psql -f` (psql에 `-c "BEGIN;"`를 같이 주면 커밋 안 되니 `-f`만 쓸 것).
- 문구/라벨/코드값은 **내가 짓지 말고 verbatim 확인**([[copy-as-is-verbatim-never-invent]]).

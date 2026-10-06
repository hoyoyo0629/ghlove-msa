---
name: asis-cubrid-dev-db-access
description: "AS-IS 운영데이터는 CUBRID 개발DB(10.0.120.51:30000/ghlove2)를 직접 조회해 확인한다. 덤프는 DDL만이다. 접속 방법·함정(예약어 LANGUAGE, 인코딩, 암호화 컬럼)"
metadata:
  node_type: memory
  type: reference
---

**AS-IS 실데이터는 CUBRID 개발DB를 직접 조회해 확인한다.** 2026-10-06 사용자가 접속정보를
알려주며 "니가 직접 조회할래?"라고 제안했고, 그 뒤로 1404/1405/1406 판정을 전부 이 방법으로 했다.

- 서버: `10.0.120.51:30000`, DB `ghlove2`, 사용자 `dba`.
  **비밀번호는 메모리에 적지 않는다**(메모리가 저장소·원격에 올라간다 -
  [[memory-lives-in-repo-docs-memory]]). 필요하면 사용자에게 다시 요청한다.
- DB명 근거: AS-IS `ghlove-common/src/main/resources/application-local.yml` 의
  `jdbc:log4jdbc:cubrid:10.0.120.51:30000:ghlove2:::?charset=UTF-8`.
- 드라이버: `C:\workspace\ghlove\libs\JDBC-10.2.8.8904-cubrid.jar`. Java 17 단일파일 실행
  (`java -cp <jar> Q.java "<SQL>" ...`)으로 충분하다. 접속정보는 환경변수로 넘겨 스크래치패드에만 둔다.

**★덤프에는 데이터가 없다.** `Desktop\고향사랑e음\1.AS-IS\1. DB\db-dump\테이블dump\*.sql` 은
**DDL 전용**이다([[asis-table-dump-path]]). 코드성/운영 데이터가 필요하면 덤프를 뒤지지 말고
바로 이 DB를 조회한다 - 2026-10-06에 또 덤프를 먼저 열어봐서 사용자가 "dump는 DDL만 있다니까,
지난주에 확인했던 사항인데"라고 지적했다.

**함정 3가지(실제로 다 밟았다):**
1. **`LANGUAGE` 는 CUBRID 예약어**다. `WHERE LANGUAGE='ko'` 는 문법오류 → `"LANGUAGE"` 로 감싼다.
2. **콘솔 출력 인코딩**: Java 17은 `-Dstdout.encoding` 을 보지 않는다(그건 18+). UTF-8
   `PrintStream` 을 직접 만들어 파일로 쓰고 그 파일을 읽는 게 확실하다.
3. **조회 결과 행수를 제한하라.** `OP_USER_ROLE` 처럼 `ROLE_SELLER_*` 가 수천 행인 표가 있어
   무심코 group by 하면 컨텍스트를 날린다.

**AS-IS 암호화 컬럼**: `G_MNGR_REQST.LOGIN_ID`·`OFCPS_NM`·`CTTPC` 등은 `^`...==` 형태로
**암호화돼 저장**돼 있다. TO-BE는 평문이므로 값 자체를 그대로 옮길 수 없다 - 구조·코드값만 참고한다.

---
name: asis-sources-and-data
description: AS-IS 소스·데이터 위치 - CUBRID 개발DB 직접조회(10.0.120.51)·테이블 덤프/export 경로·실행 소스 C:\workspace\ghlove·인벤토리 절차·문구사전(op_common_message)
metadata:
  type: reference
---

AS-IS를 확인할 때 어디를 보는지.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## asis-cubrid-dev-db-access

> AS-IS 운영데이터는 CUBRID 개발DB(10.0.120.51:30000/ghlove2)를 직접 조회해 확인한다. 덤프는 DDL만이다. 접속 방법·함정(예약어 LANGUAGE, 인코딩, 암호화 컬럼)


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
**DDL 전용**이다([[asis-sources-and-data]]). 코드성/운영 데이터가 필요하면 덤프를 뒤지지 말고
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

## asis-table-dump-path

> AS-IS 테이블 덤프/실데이터 export 로컬 경로 - DDL전용 550개 폴더 + [2026-10-07] 테이블별 실데이터 대량 export 폴더 추가


AS-IS(CUBRID 운영) 테이블 DDL이 로컬에 통째로 있다:
`C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\db-dump\테이블dump\<table>.sql` (550개)

**이 550개 파일은 전부 `DROP TABLE` + `CREATE TABLE`만이고 INSERT가 0건이다**(2026-10-02 확인). 이전에 "INSERT 덤프"라고 적어둔 건 틀렸고, 그걸 근거로 `mig_op_menu.sql`(19줄)을 보고 "AS-IS에 기부금 운영현황 메뉴가 없다"고 오판한 사례가 있다. **쓰임**: 테이블·컬럼·주석(한글 설명)·타입·PK 확인. 스키마 대조에는 1순위로 유효하다.

**★[2026-10-07] 실데이터는 이제 `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB` 바로 밑에
테이블별로 대량 export돼 있다** (`<TABLE>_<timestamp>.sql`, UTF-8 `INSERT INTO ... VALUES` 형식,
단일 테이블 1GB 넘는 것도 있음 - `_g_cntr__...sql`). 사용자가 "필요한거 있으면 말만 해, 사무실
일 때 직접 export 해줄게"(반복 확인, 2026-10-02/2026-10-07) - **AS-IS 실데이터 확인이 필요하면
먼저 이 폴더에 이미 받아둔 파일이 있는지 보고, 없으면 사용자에게 요청한다.** 전체 디스크를
`find /`로 뒤지는 식의 비효율적 탐색은 하지 말 것(2026-10-07 사용자 피드백 - 직접 물어보는 게 빠름).
- 받은 것(일부): `OP_MENU`/`OP_MENU_RIGHT`([[admin-screen-pitfalls]]),
  `OP_COMMON_MESSAGE`([[asis-sources-and-data]]), `OP_COMMON_CODE`, `OP_CONFIG_ISMS`,
  `OP_FEATURED`, 그리고 2026-10-07에 커뮤니티·기부통계 등 대량 테이블(`_g_*`, `_dntn_*`, `_event_temp_` 등).
- 적재법: `perl -pe` 로 테이블명에 스키마를 붙이고 `psql -f` (psql에 `-c "BEGIN;"`를 같이 주면 커밋 안 되니 `-f`만 쓸 것).
- 문구/라벨/코드값은 **내가 짓지 말고 verbatim 확인**([[asis-porting-principles]]).
- ★데이터뿐 아니라 **테이블 이름 자체도 AS-IS 기준으로 검증**할 것 - TO-BE가 지은 이름(예:
  `ADMIN_COMMON_CODE`, AS-IS에는 없었다)으로 별도 테이블을 만들어 화면이 그쪽을 읽고 있으면
  실데이터를 다른 쪼긴 (올바른 이름의) 테이블에 넣어도 화면엔 안 보인다 - 2026-10-07
  공통코드 사례(admin-common-code-asis-sync-2026-10-07(보관: docs/archive/memory-fix-log-2026-10.md)).

## asis-live-source-is-workspace-ghlove

> localhost:8080 AS-IS가 실제로 도는 소스는 C:\workspace\ghlove(Desktop 1.AS-IS 소스는 분석용 사본) + AS-IS 재기동 방법과 함정


**AS-IS(localhost:8080 opmanager / 9080 api / 3000 frontend)가 실제로 실행되는 소스 = `C:\workspace\ghlove`.**
`Desktop\고향사랑e음\1.AS-IS\0. 소스코드\ghlove`는 분석용 사본이다 - 거기를 고쳐도 실행 중인 AS-IS에 반영 안 되고,
다른 세션들이 verbatim 대조 원본으로 읽으므로 **수정 금지**(2026-10-13 실수로 고쳤다가 원복).

**재기동** (사용자가 요청할 때만, [[build-is-mine-restart-is-users]]):
- 공식 스크립트 `C:\workspace\ghlove\scripts\start-all.bat`(stop-all → 공통 빌드 → `_run-web/_run-api/_run-frontend.bat` 새 창).
  도구에서 Start-Process로 돌리면 stop만 되고 새 창이 안 떠서 **세 서버가 다 내려간 채** 끝난다 →
  대신 같은 명령을 백그라운드로 직접: `gradlew.bat -Dspring.profiles.active=local -x :ghlove-common:copyApplicationYml -x :ghlove-common:processResources :ghlove-web:bootRun`
  (api는 `--init-script scripts/enable-api-bootrun.init.gradle ... :ghlove-api:bootRun`, frontend는 `ghlove-frontend`에서 `node index.js`).
- 빌드가 `Failed to create Jar file ...spring-boot-gradle-plugin-2.7.0.jar` / "다른 프로세스가 파일을 사용 중"이면
  오래 떠 있던 Gradle 7.4.1 데몬이 jar를 잡고 있는 것 → `gradlew.bat --stop` 후 재시도.
- JSP 수정은 bootRun이 소스 webapp을 그대로 서빙하지만, Java(컨트롤러) 수정은 재기동해야 반영된다.

**로컬 테스트용 우회(2026-10-13, 사용자 요청)**: 기탁서 등록의 실명인증(`offgive/sci-call.jsp`)과
행정주소검색(`OffgiveManagerController.getUserAddressInfo/getUserAddressInfoForeign`)에 `localhost` 접근일 때만
항상 성공하는 분기(경기도 41000/시흥시 41390, CI=`MOCKCI`+주민번호)를 넣어 뒀다. TO-BE 우회도 같은 값이다.

**opframework 소스가 없을 때**: `C:\workspace\ghlove\libs\opframework-3.15.0.jar`를 `javap -J-Dfile.encoding=UTF-8 -p -c -constants`로
풀면 AbstractSXSSFExcelView 등 프레임워크 동작을 바이트코드로 정확히 확인할 수 있다(→ admin `AsisSxssfExcelView`).

## as-is-inventory-procedure

> AS-IS 전수 인벤토리 추출 절차와 산출물 위치 (도메인 순서 member→donation→point→gift→order→admin, 2026-09-10 6도메인+common 전수 완료)


[[asis-porting-principles]] / [[asis-porting-principles]] 기준을 실행하는 구체 절차. 2026-09-10 사용자 지시로 착수했다.

**도메인 순서**: member → donation → point → gift → order → admin **+ 7번째 시트 `common`**(6개 도메인 밖 전 영역 - GNB/LNB·게시판·약관·통합검색·메인·소식지·공통인프라·배치). 2026-09-10 전부 완료, 누적 726행. 도메인마다 표 4개(컨트롤러 엔드포인트 / 서비스 메서드 / 매퍼 쿼리 / 화면 이벤트 핸들러)를 뽑아 MSA와 1:1 매핑한다.
**판정 5분류**: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**. 죽은코드는 지우지 말고 목록에 남기되 따로 표시한다(사용자 지시).

**산출물**
- 서술 분석: `docs/as-is-inventory-<domain>.md`
- 표 데이터: `docs/inventory/<domain>.tsv` (7열: 계층·AS-IS구분·AS-IS항목·근거·MSA대응·판정·비고)
- 엑셀: `docs/inventory/as-is-inventory.xlsx` — `docs/inventory/build-xlsx.ps1` 실행하면 tsv들을 **서비스별 sheet**로 묶어 생성(Excel COM 사용, 판정별 행 색상·틀고정·자동필터 포함). **.ps1은 반드시 UTF-8 BOM으로 저장**해야 한다(Windows PowerShell 5.1이 BOM 없으면 ANSI로 읽어 한글 파서 에러).

**추출 요령 (실측 검증된 것)**
- **소스 인덱스를 먼저 만든다**: `find ghlove-api ghlove-common ghlove-web ghlove-batch -name "*.java" -print0 | xargs -0 grep -Hn "" > /tmp/java_index.txt` — 1.2초, 41MB. 이후 모든 조회를 이 파일 하나에 건다. 매퍼 131개를 모듈 전체 재귀 grep으로 돌리면 120초를 넘겨 타임아웃되지만 인덱스로는 9초다.
- **죽은 화면 판별**: `ghlove-frontend`는 정적 HTML 라우팅이라 inbound 참조가 0이면 도달불가다. 제외 패턴 `-backup.html`, `_bak.html`, `_test.html`, `_YYYYMMDD.html`, `/old/`, `node_modules`. 단 **서버 리다이렉트 착지 페이지**(onepass-result, mobile-auth-result)는 프론트 참조가 0이어도 살아있으므로 Java 쪽 `sendRedirect`/`setUrl`을 반드시 같이 확인한다.
- **엔드포인트 사용여부는 2단계**: `modules/op.saleson.js`는 URL 레지스트리라 URL이 등록돼 있는 것만으로는 증거가 안 된다. 레지스트리의 래퍼 함수명을 뽑아 화면에서 `.<래퍼명>(` 호출을 찾아야 한다.
- **매퍼 사장쿼리 판별에 주입 변수명을 가정하지 말 것**. AS-IS는 master/slave 이중 주입(`slaveSecedeUserMapper`)과 대문자 필드명(`JoinMapper.getUserInfoByUserId(...)`)이 섞여 있어 오탐이 난다. 인덱스에서 `\.<queryId>\(` 존재 여부로 판정하고, 매퍼 인터페이스 선언 여부와 교차검증한다.

**Why:** 화면 단위로 보면 재현 누락이 "원래 그런 것"으로 묻히고, MSA 주석의 "AS-IS는 …" 서술을 검증할 수 없다. 실제로 이 절차 첫 적용(member)에서 AS-IS `/api/auth/*` 회원기능 16개가 `/api/user/*`로 이관된 뒤 남은 죽은코드라는 것, 전자서명 등록화면이 오타(`sign-certificate1.html`)로 도달불가라 등록 없이 검증만 도는 반쪽 상태라는 것, MSA 수신동의 체크박스 4종에 `th:field`가 없어 폼 전송이 통째로 누락된다는 것을 찾았다.

**How to apply:** 도메인 착수 시 인덱스부터 만들고, 화면 생사 → 엔드포인트 → 매퍼 → 서비스로직 → 화면이벤트 순으로 내려간다. 각 판정에는 `파일:라인` 근거를 반드시 남긴다. tsv를 추가한 뒤 `build-xlsx.ps1`을 다시 돌리면 sheet가 누적된다.

## asis-message-catalog-loaded

> AS-IS 화면 문구 사전(OP_COMMON_MESSAGE) 운영DB export 받아 admin에 적재 완료 - 템플릿은 ${msg.get('M00730')}, JS는 /common/message. AS-IS 덤프 550개는 전부 DDL만이라 데이터는 사용자 export로만 확보


**AS-IS 화면 문구는 전부 코드다** - JSP는 `${op:message('M00730')}`(고유 741코드 / 5,653곳), JS는 `Message.get("M00745")`. 2026-10-02 사용자가 운영DB에서 export(`Desktop\고향사랑e음\1.AS-IS\1. DB\OP_COMMON_MESSAGE_202610021746.sql`, **UTF-8**, ko 2034 / ja 98, 고유 M코드 1692)해줘서 `admin.op_common_message`에 적재 완료(`database/ddl/migration-admin-common-message-asis-load.sql`, 재실행 가능).

**How to apply:**
- 템플릿: `th:text="${msg.get('M00730')}"` - `ManagerAuthAdvice`의 `@ModelAttribute("msg")`가 모든 화면에 주입한다. 한글 직접 입력 금지([[asis-porting-principles]])
- JS: 복사해온 `op.common.js`의 `Message.get`이 `$.post('/common/message', {messageCode:...})`를 **동기**로 때린다. `CommonMessageApiController`가 경로·파라미터명·응답모양(`{isSuccess, data}`)까지 verbatim으로 받는다 - 바꾸면 공통 JS가 깨진다
- 조회는 `CommonMessageService` 메모리 캐시(동기 ajax라 DB 왕복 금지). 문구 수정 후엔 `reload()`
- 미등록 코드는 코드 문자열을 그대로 반환 - AS-IS와 동일, 빠진 게 화면에 드러난다

**중요: AS-IS 테이블 덤프(`db-dump\테이블dump\*.sql` 550개)는 전부 CREATE TABLE만, INSERT 0건이다.** [[asis-sources-and-data]]에 "코드성데이터 verbatim 확인용"으로 적어둔 건 **틀렸다**. 데이터가 필요하면 사용자에게 `SELECT ... FROM <table>` export를 요청해야 한다(사용자가 해주겠다고 했음). 이미 받은 것: `op_menu`, `op_menu_right`, `op_common_message`. **다음에 필요한 것: `op_common_code`**(AS-IS 매퍼 60곳이 `FROM OP_COMMON_CODE` 참조 - 화면 셀렉트박스 옵션의 원본), 그 외 `op_business_code`(5곳). 관련: [[asis-screen-port-procedure]]

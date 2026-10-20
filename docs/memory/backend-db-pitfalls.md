---
name: backend-db-pitfalls
description: 백엔드·DB 함정 모음 - JPQL LIKE cast·null 바인딩(bytea/LocalDateTime) 500·native varchar(1)=Character·NOT NULL DEFAULT 매핑 NULL·PG numeric 나눗셈 스케일·admin→member 날짜포맷 API·서비스별 세션 쿠키 이름
metadata:
  type: feedback
---

쿼리·엔티티·서비스 간 호출에서 반복된 결함과 고치는 법.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## hql-null-param-needs-cast

> 조건부 검색 @Query에서 LIKE 검색어는 cast(:x as String) 필수 - 안 하면 null이 bytea로 바인딩돼 PostgreSQL이 거절


**AS-IS 매퍼의 `<if>` 조건부 WHERE를 `(:x is null or ...)`로 옮길 때, `concat`/`LIKE` 안에만 쓰이는
검색어 파라미터는 반드시 `cast(:x as String)`으로 타입을 줘야 한다.** 양쪽 occurrence 모두:

```java
and (cast(:query as String) is null or p.subject like concat('%', cast(:query as String), '%'))
```

**Why:** Hibernate 6은 파라미터 타입을 쓰이는 문맥에서 추론한다. `p.col = :x` / `>= :x`처럼 비교
대상이 있으면 잡아내지만, **`concat(...)`은 인자 타입을 알려주지 않아** 추론에 실패하고 null을
`Serializable`(=bytea)로 바인딩한다. 그러면 PostgreSQL이
`operator does not exist: character varying ~~ bytea`로 쿼리를 거절한다(검색어를 비우고 목록에
들어가는 순간 500). 2026-10-06 아침 사용자가 **팝업관리 목록**에서 실제로 맞았고, 같은 패턴이던
admin `PopupRepository`·`PolicyRepository`·`OpEmailRepository`·`CommonCodeRepository`·
`IpsSendingMasterRepository` 5개를 그때 고쳤다.

**[2026-10-07 추가] ★Long/LocalDateTime은 cast로 안 고쳐진다**: `admin.PrivacyAccessLogRepository`의
`(:managerId is null or ...)` / `(:from is null or ...)`에서도 같은 "could not determine data
type of parameter"로 500이 났다(1411 엑셀다운로드사유관리). `cast(:x as Long)`로 양쪽
occurrence를 감쌌더니 그 에러는 없어졌지만, **재기동 후 새 에러** `cannot cast type bytea to
bigint`가 났다 - Hibernate가 null **Long** 값 자체를 bytea로 바인딩해버려서 cast(bytea→bigint)가
거절당한 것이다(String의 cast(:x as String)은 운영에서 실제로 잘 작동하는데 Long은 안 되는
비대칭 - 내부 이유는 못 밝혔으니 "String은 되고 Long/LocalDateTime은 안 된다"는 경험적 사실만
믿을 것). **결론: String이 아닌 타입에 cast를 시도하지 말고 바로 "조건부 조립"으로 가라** -
`PrivacyAccessLogService`가 `EntityManager`로 JPQL을 문자열 조립하는 방식(아래 "해당 없음"
목록과 같은 패턴)으로 바꿔서 해결했다. [[backend-db-pitfalls]] 참고.

**How to apply:**
- **String** 파라미터의 `= :x` / `>= :x`조건은 cast 불필요. **String이 아닌 타입**(Long/
  LocalDateTime 등)이 "is null" 분기에만 쓰이면 cast로 때우려 하지 말고 바로 조건부 조립으로
  전환할 것 - cast는 String에서만 검증된 해법이다.
- cast 대상은 `varchar`가 아니라 Hibernate 문서화 타입명(`String`). `varchar`/`bigint` 같은
  SQL 타입명은 네이티브 쿼리에만.
- **nativeQuery=true는 해당 없다** - 네이티브는 자바 타입으로 바인딩되므로 문제가 없다
  (`CommonMessageRepository`, point `GCntrUsePointRepository`가 그 예).
- SQL을 조건부로 조립하는 방식(`QestnarRepository`·`BatchLogRepository`·donation
  `OffgiveAdminRepository`)도 해당 없다 - 값이 있을 때만 파라미터가 붙는다.
- 이 결함은 **기동 시점에도 안 잡힌다**(실행해야 터진다). 조건부 검색을 새로 만들면
  "검색어 비움" 경로를 한 번은 실제로 호출해 볼 것 - [[build-is-mine-restart-is-users]]의 여분 포트 검증.

## native-query-varchar1-returns-character

> 네이티브 쿼리에서 varchar(1) 컬럼은 Character로 돌아온다 - (String) 캐스팅하면 목록이 ClassCastException 500


Hibernate 네이티브 쿼리가 `Object[]`로 돌려주는 행에서 **`varchar(1)` 컬럼은 `String`이 아니라
`java.lang.Character`**다. record로 매핑하며 `(String) r[n]`으로 캐스팅하면 그 순간
`ClassCastException: class java.lang.Character cannot be cast to class java.lang.String`으로
**화면이 통째로 500**이 난다.

**실제 사례**
- 2026-10-12 답례품제공자 공지사항(5114) 목록: `NOTICE_FLAG`·`DISPLAY_FLAG`가 `varchar(1)`이라
  목록 진입이 바로 500이었다. `SysNoticeSellerAdminRepository.str()`(= `value.toString()`)로 교정.
- 메뉴관리 답례품관리자 화면에서도 같은 원인으로 500이 났었다
  (admin-menu-admin-seller-data-and-pagination(보관: docs/archive/memory-fix-log-2026-10.md)).
- `QestnarRepository`에도 같은 함정 주석이 이미 달려 있다
  (`CASE WHEN ... THEN 'Y' ELSE 'N' END` 결과도 길이 1이라 Character로 온다).

**Why:** 컬럼 정의가 `character varying(1)`이어도 길이가 1이면 드라이버/Hibernate 조합이
Character로 올려준다. 엔티티 매핑(`createNativeQuery(sql, Xxx.class)`)이나 JPQL을 쓰면
`@Column` 타입대로 변환돼 문제가 없고, **record/Object[] 수동 매핑에서만** 터진다.
컴파일로는 안 잡히고 그 화면을 열어야 드러난다.

**How to apply:** 네이티브 쿼리를 record로 매핑할 때 문자열 칸은 `(String)` 캐스팅 대신
**`value == null ? null : value.toString()`** 류의 헬퍼를 쓴다. 특히 `*_YN`·`*_FLAG`·
`CASE WHEN ... 'Y'/'N'` 처럼 한 글자가 오는 칸은 반드시 그렇게 한다.

**남은 점검거리**: `grep -rn "(String) r\[" admin/src/main/java/com/ghlove/admin/repository/`로
나오는 리포지토리들(Maintenance·ManagerManual·Mnl·QnaIndividual 등)은 현재 select 목록에
길이 1 컬럼이 없어 통과하고 있을 뿐이다. 그 쿼리에 `*_YN` 류를 추가하는 순간 같은 500이 난다.

## jpa-mapped-not-null-default-column-sends-null

> NOT NULL DEFAULT 컬럼을 엔티티에 매핑하면 JPA가 insert에 NULL을 명시해 DB 기본값이 무력화된다 - g_cntr.delete_at 매핑(10-06) 뒤로 기부 insert 전부 실패, 10-14 15102 테스트에서 발견


DB 기본값에 기대던 컬럼(`NOT NULL DEFAULT ...`)을 엔티티 필드로 매핑하면, 값을 안 넣어도 Hibernate가 insert 컬럼 목록에
포함시켜 NULL을 보낸다 → 기본값이 안 먹고 NOT NULL 위반.

**Why:** 2026-10-06 커밋 04bc80d에서 집계 조건용으로 `Donation.deleteAt`(G_CNTR.DELETE_AT, NOT NULL DEFAULT 'N')을
매핑한 뒤 `donation.g_cntr` insert가 한 건도 성공하지 못했다(10-01 이후 행 0건 - 온라인·기탁서 기부 전부). 10-14 기탁서
등록(15102) 테스트에서 "기탁서 등록에 실패하였습니다. 고객센터에 문의 하세요."로 드러남. 새 Donation을 만드는 유일한 곳
`DonationService.saveRequested`에서 `setDeleteAt("N")`으로 수정(AS-IS insert는 컬럼을 빼고 기본값을 탄다).

**How to apply:** 기존 테이블 컬럼을 엔티티에 새로 매핑할 때 `information_schema.columns`로 is_nullable/column_default를
확인하고, NOT NULL+기본값이면 생성 지점에서 같은 값을 넣거나 `@Column(insertable=false)`/`@ColumnDefault`+`@DynamicInsert`를 쓴다.
g_cntr의 다른 기본값 컬럼(cntr_point, foreign_status_code)은 미매핑이라 안전 - 매핑하면 같은 문제.

## pg-numeric-division-scale-breaks-display

> PostgreSQL numeric 나눗셈이 스케일 16+로 나와 비율 컬럼이 0E-20 / 0.61000000000000000000 으로 찍힌다


AS-IS(CUBRID) 식 `FLOOR(a*10000.0/b)/100` 을 그대로 옮기면 PostgreSQL에서는 결과 스케일이
**16자리 이상**으로 잡힌다. 실측(2026-10-08, 개발DB):

```
FLOOR(16662000*10000.0/2699500000)/100  ->  1.5700000000000000
```

이 값이 `BigDecimal`로 올라오면 화면까지 스케일이 따라가서 **요약은 `0.61000000000000000000%`,
0은 `BigDecimal(0, scale 20).toString()` 인 `0E-20%`** 로 찍힌다. 특정사업 지자체별통계에서 실제로
그렇게 깨져 있었다.

**Why:** CUBRID는 같은 식에서 2자리를 돌려줬다. DB만 바꿔 이식하면 SQL은 "AS-IS와 글자까지 같은데"
화면 숫자가 달라지므로, 소스 대조만으로는 안 잡히고 화면을 봐야 발견된다.

**How to apply:** 비율·달성율·점유율처럼 나눗셈 결과를 내려주는 컬럼은 세 군데를 같이 맞춘다.
1. SQL에서 `CAST(... AS numeric(20, 2))` 로 **스케일만** 고정(값은 AS-IS 식 그대로 둔다).
2. Java 타입을 AS-IS DTO와 같게 — AS-IS가 `float`면 `float`로 받는다(`BigDecimal` 금지).
3. 표시도 AS-IS를 따른다. AS-IS는 **요약과 목록이 다르다** - 요약은 `op:numberFormat`(=
   `NumberFormat.getNumberInstance()`, 소수 최대 3자리·0이면 생략)을 거치고, 목록은 포맷터 없이
   float을 그대로 찍는다(`0.0`, `0.61`). 그래서 `#numbers.formatDecimal`을 쓰면 자리수가 고정돼
   틀린다. 요약용으로 `admin/web/support/OpNum.java`(`${@opNum.format(값)}`)를 만들어 뒀다.

다른 통계 화면(월별통계·기부금현황·판매통계)에 같은 형태의 비율 컬럼이 더 있을 수 있어 소급 점검
대상이다. [[admin-screen-pitfalls]] 와 같은 성격의 전수 교정 건이다.

## admin-excel-download-log-500-fix

> 엑셀다운로드사유관리(1411) 500에러 수정 - PrivacyAccessLogRepository의 null 파라미터 타입추론 실패([[backend-db-pitfalls]] 적용)


`/admin/excel-download-logs` 접속 시 500. admin.log 실제 스택트레이스로 원인 확인:
`org.postgresql.util.PSQLException: ERROR: could not determine data type of parameter $4`.

`PrivacyAccessLogRepository.searchExcelDownloadLogs`의 `(:from is null or p.createdAt >= :from)`
패턴이 [[backend-db-pitfalls]]와 같은 문제 - **1차 시도(cast) 실패**: `cast(:managerId as
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

## admin-sms-log-500-fix

> 문자전송이력(시스템관리) 500에러 - IpsSendingMasterRepository의 (:x is null or ...) JPQL이 null LocalDateTime 바인딩 때 타입추론 실패, 조건부 조립으로 수정(2026-10-07)


시스템관리 > 문자전송이력(`/admin/send-sms-logs`) 접속 시
`PSQLException: could not determine data type of parameter $6` 500에러.

**원인**: `IpsSendingMasterRepository.search`가 `(:startDate is null or m.infoCrtDt >= :startDate)`
형태의 정적 JPQL이었다 - `svcId`(String)는 cast 없이도 우연히 괜찮았지만 `startDate`/`endDate`
(LocalDateTime)가 null일 때 PostgreSQL이 바인드 파라미터 타입을 못 정한다.
[[backend-db-pitfalls]]와 완전히 같은 버그 패턴(LocalDateTime/Long은
`cast(:x as Type)`로도 안 고쳐진다 - [[backend-db-pitfalls]] 참고).

**수정**: `IpsSendingMasterRepository`에서 `search` `@Query` 메서드를 제거하고(나머지
`nextListSn()`만 남김), 같은 테이블을 쓰는 `SmsIpsService`(기존에 발송 적재만 하던 서비스)에
`EntityManager` 기반 조건부 JPQL 조립 메서드로 옮겼다 - 값이 null이 아닐 때만 조건절+파라미터를
추가한다(`QestnarRepository`/`BatchLogRepository`/`PrivacyAccessLogService`와 같은 패턴).
`SendLogAdminController.smsList`는 이제 `ipsSendingMasterRepository.search(...)` 대신
`smsIpsService.search(...)`를 호출한다.

**검증**: `SmsIpsServiceTest.searchAllowsNullDateRange` - 실제 서비스 메서드를
`startDate=null, endDate=null`로 직접 호출하는 `@SpringBootTest`(MOCK 웹 환경)를 작성해
통과 확인(psql 텍스트 프로토콜 검증은 [[backend-db-pitfalls]] 사건으로
불충분하다고 판명됐으므로 반복하지 않음).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-member-date-range-api-format-bug

> 탈퇴회원리스트(4105)·휴면회원관리(4107)·대시보드 오늘가입 위젯이 member 검색 API에 yyyyMMdd를 그대로 보내 500→빈 결과로 집어삼켜지던 버그, 2026-10-08 수정


**사고 경위**: 사용자가 일반회원관리에서 회원 하나를 탈퇴시켰는데 탈퇴회원리스트(4105,
`/admin/secede-users`)에 조회가 안 된다고 보고했다. 실데이터 확인 결과 탈퇴 자체는 정상
처리됐다(`member.op_user` user_id 1080, status_code=WITHDRAWN, leave_date=오늘). member의
검색 API를 "yyyy-MM-dd" 형식으로 직접 호출하면 정확히 그 행을 돌려준다 - **member 쪽은
멀쩡하다.**

**진짜 원인**: `SecedeUserAdminController`/`SleepUserAdminController`가 화면 날짜입력칸의
`yyyyMMdd`(8자리, 구분자 없음) 값을 **변환 없이 그대로** `MemberAdminClient.searchSecede()`/
`searchSleep()`에 넘겼다. member 쪽 `AdminMemberService.rangeStart/rangeEnd`는
`LocalDate.parse(dateStr)`를 쓰는데 이건 기본적으로 "yyyy-MM-dd"만 받는다 - "20261008"처럼
구분자 없는 문자열을 넣으면 `DateTimeParseException`이 터져 member API가 500을 낸다.
그런데 `MemberAdminClient`의 모든 검색 메서드는 `catch (RestClientException e)`로 **에러를
빈 결과(`List.of(), 0, 0`)로 조용히 삼켜버리는 패턴**이라, 화면에는 그냥 "검색결과 없음"으로만
보이고 로그를 안 보면 원인을 알 수 없다.

같은 패턴이 하나 더 있었다: `AdminHomeController`(관리자 홈 대시보드)의 "오늘 가입 회원수"
위젯도 `yyyyMMdd` 포맷의 `today`를 그대로 `memberAdminClient.search(today, today, ...)`에
넘기고 있어서, **대시보드가 항상 0으로 보이던 상태**였다(같은 try/catch(Exception ignored)
패턴). 이건 사용자가 보고하지 않았지만 같은 버그라 같이 고쳤다.

`GeneralCustomerSearchParam`(일반회원관리, 4101)는 처음부터 `getFromDateForApi()`/
`getToDateForApi()`로 yyyyMMdd→yyyy-MM-dd 변환을 해주고 있어서 **그 화면만 이 버그가
없었다** - 그래서 지금까지 안 걸렸다.

**조치**: `SecedeUserSearchParam`/`SleepUserSearchParam`에도 같은 모양의
`getSrchStart*DateForApi()`/`getSrchEnd*DateForApi()`(8자리 아니면 null, 맞으면
"yyyy-MM-dd"로 변환)를 추가하고, 두 컨트롤러가 그걸 쓰도록 바꿨다. `AdminHomeController`는
멤버 호출 직전에 `LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)`로 별도 변환한
값을 쓰도록(주문 쪽 `today`는 yyyyMMdd 그대로 유지 - 두 서비스 요구 포맷이 다르다).

**교훈/재발방지**: admin이 다른 서비스의 날짜범위 검색 API를 호출하는 자리를 새로 만들 때마다
"그 서비스가 yyyyMMdd를 받는지 yyyy-MM-dd를 받는지" 확인하고, 화면 입력칸 포맷(yyyyMMdd)과
다르면 반드시 변환 메서드를 만들 것. `MemberAdminClient`처럼 예외를 빈 결과로 삼키는 클라이언트는
이런 포맷 버그를 "데이터 없음"으로 둔갑시키므로, 검색결과가 이상하게 0건이면 **먼저 해당
서비스 API를 올바른 포맷으로 직접 curl 호출해 재현**해보는 게 디버깅 1순위다.

**검증**: member API를 `fromDate=20261008`(500 재현) vs `fromDate=2026-10-08`(정상, 1건)으로
직접 비교해 원인 확정. 수정 후 admin `compileJava+test+bootJar` EXIT=0.

**관련**: [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-member-area-port-progress)(4105/4107 최초 포팅 당시 기록), 휴면회원관리
시드데이터는 admin-sleep-user-seed-data(보관: docs/archive/memory-fix-log-2026-10.md) 참고. **재기동 필요: admin.**

## session-cookie-name-per-service

> 쿠키는 포트를 구분하지 않아 localhost에서 admin·member가 JSESSIONID를 서로 덮어썼다 - 서비스별 쿠키 이름 분리(ADMIN_SESSION/MEMBER_SESSION)


**admin과 member를 같이 띄워 쓰면 운영자 화면이 수시로 로그인 페이지로 튕기던 원인은
세션 쿠키 이름이 둘 다 기본값 `JSESSIONID`였기 때문이다.** 쿠키는 **포트를 구분하지 않는다**
(host `localhost`, path `/`가 같으면 같은 쿠키다). 그래서 member(8081)가 세션을 만들면
admin(8086)의 쿠키를 덮어쓰고, 다음 클릭에서 `AdminAuthInterceptor`가 `loginManager`를 못 찾아
`/admin/login`으로 보낸다. 반대 방향도 같다. **타임아웃 문제가 아니다.**

→ 서비스별로 이름을 분리했다(2026-10-06): admin `ADMIN_SESSION` / member `MEMBER_SESSION`
(`server.servlet.session.cookie.name`). 세션을 쓰는 서비스는 이 둘뿐이고
donation·gift·order·point는 `HttpSession`을 쓰지 않는다(gift 판매자 인증은 JWT 쿠키 `GH_AUTH`).
**세션을 쓰는 서비스를 새로 만들면 반드시 고유 이름을 준다.**

**같이 맞춘 것:**
- `server.servlet.session.timeout: 30m` (AS-IS `SessionListener`가 모든 세션에
  `setMaxInactiveInterval(30*60)`을 건다 - TO-BE는 스프링 기본값 30분에 의존하던 것을 명시)
- `tracking-modes: cookie` (admin에 없어서 리다이렉트 URL에 `;ADMIN_SESSION=...`이 붙었다)
- **`OP_MANAGER_TIMEOUT`은 "분"이다.** `op.manager.js`의 `Manager.procSessionTimeout`이
  `값 * 60 * 1000`으로 쓴다. 내가 초로 알고 `'3600'`을 넣어 60시간이 돼 있었다 → AS-IS처럼
  ISMS 설정 `SESSION_TIMEOUT_MANAGER`(기본 60)에서 읽어 내려준다
  (`ManagerAuthAdvice.managerTimeout`, 시드 `database/ddl/seed-admin-isms-session-timeout.sql`).
- 자동 로그아웃이 호출하는 AS-IS URL `/op_security_logout?target=/opmanager`가 TO-BE에 없어
  404였다 → 복사해 온 자산을 고치지 않고 `ManagerAuthController.securityLogout`을 추가했다.

**How to apply:** 증상이 "로그인은 되는데 돌아다니다 튕긴다"면 먼저
브라우저 개발자도구 Application > Cookies에서 **세션 쿠키가 다른 서비스 것으로 바뀌는지** 본다.
서버 로그에는 아무 예외도 안 남는다(정상 리다이렉트라서).

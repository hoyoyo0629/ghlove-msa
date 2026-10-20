---
name: da-design-pending
description: DA 설계안 대기 항목 - 조회모델 전반 보류·포인트 ReadModel 중지·통계 분석DB(통계표 22종) 위치/배치·DB 변경설·op_manager 이관 보류·주문 표 인덱스(로컬 보관)
metadata:
  type: project
---

DA 설계안이 나와야 결정·진행할 수 있는 것들. 그 전엔 손대지 않는다.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## defer-readmodels-pending-da-design

> 모든 조회모델(ReadModel/rwd_*/조회전용 프로젝션) 작업은 DA 설계안이 나올 때까지 보류. 특정 도메인이 아니라 전 도메인 공통.


**사용자 결정(2026-09-22)**: 조회모델(ReadModel) 작업은 **전부 보류**하고, **DA쪽 설계안이 나오면** 그때 진행한다. 특정 도메인만이 아니라 **모든 도메인의 조회모델**이 동일.

**Why:** 조회모델 스키마·프로젝션 구조는 DA(데이터아키텍트) 설계안에 좌우되므로, 지금 만들면 나중에 어긋나 헛일이 될 수 있다. [[asis-porting-principles]](SalesOn 미사용 보류)·[[da-design-pending]](point 조회모델 중지)와 같은 맥락.

**How to apply:**
- 갭목록에서 "조회모델/ReadModel/rwd_*/조회전용 프로젝션/목록 표시 캐시 보강"이 나오면 **기록만 하고 보류**. 억지로 만들지 않는다.
- 해당 예: order 멀티아이템 Phase 6 ②(admin 주문목록 조회모델), point 조회모델, donation 조회모델, member 조회모델 등.
- 이미 만들어진 조회모델은 그대로 둔다([[asis-porting-principles]]의 "이미 구현된 건 유지"와 동일 원칙).
- 조회모델이 아닌 실사용 표시 버그(예: [[order-single-item-vs-multiitem-decision]] Phase 6 ③ point 주문번호 '#' 앞부분 표시는 displayOrderCode로 이미 완료)는 보류 대상 아님.

관련 [[da-design-pending]] [[order-single-item-vs-multiitem-decision]] [[asis-porting-principles]].

## point-readmodel-paused-pending-db-design

> point 서비스 CQRS ReadModel 신설을 2026-09-09 중간에 중지 - DB 설계 완료 후 재개. 테이블/엔티티는 이미 만들어졌고 배포는 안 됨


SFR-004 "CQRS 기반 ReadModel 제공" 미충족 건을 착수했다가 **DB 설계가 끝난 뒤 재개**하기로 하고 2026-09-09 중지했다.

**이미 적용된 것 (되돌리려면 이것들을 지우면 된다):**
- 개발 DB(`ghlove_core`, `point` 스키마)에 조회 전용 테이블 2개 생성, 둘 다 **비어 있음**:
  - `pt_rm_locgov_point` (PK: user_id, locgov_code, stdr_year / cntr_amt, earned_amount, used_amount, remaining_amount, lot_remaining, updated_date)
  - `pt_rm_expiring_point` (PK: user_id, expiration_date, locgov_code / remaining_amount, updated_date)
- `database/ddl/service-point.sql` 끝에 위 DDL + 설계 의도 주석 블록 추가
- `point/src/main/java/com/ghlove/point/readmodel/` 에 6개 파일 (엔티티 2, 복합키 2, 리포지토리 2)

**아직 안 한 것:** 프로젝션 서비스(원장에서 재계산), `point.ledger` 자가 구독 리스너, 조회 경로 전환, 기존 데이터 백필, 재동기화 엔드포인트.

**중요:** 자바 코드는 **컴파일·배포하지 않았다.** 8083에 떠 있는 point jar는 이전 빌드라 새 테이블·새 클래스를 전혀 모르며 동작에 영향이 없다. 재개 시 `cd point && ./gradlew bootJar` 후 재기동해야 반영된다.

**Why 중지:** 사용자가 DB 설계를 진행 중이며, ReadModel 테이블 구조가 그 결과와 어긋나면 두 번 일하게 된다.

**How to apply (재개 시 지켜야 할 설계 결정):**
1. 쓰기모델(`PT_POINT_LEDGER`/`PT_POINT_BALANCE`)은 손대지 않는다. 원장행마다 이미 발행되는 `point.ledger` 이벤트를 **point가 스스로 구독**해 해당 회원분을 **통째로 재계산**한다 - 델타 누적이 아니라 전량 재계산이라 Kafka at-least-once 재전달·컨슈머 재시작에도 값이 어긋나지 않는다. 컨슈머 group-id는 admin 것과 겹치지 않게 할 것(현재 point의 기본 group-id는 `point-service`).
2. **차감·예약 가능 여부 판단에는 ReadModel을 쓰지 않는다.** 이벤트 지연만큼 뒤처질 수 있어 초과 사용이 가능해진다 - `deductForOrder`/`reserve`/`confirmReservation`/`usePoints`와 order가 부르는 `/api/balance-by-locgov`는 계속 쓰기모델(`balanceByLocgov`)을 직접 읽는다. ReadModel은 **표시용 조회 전용**(마이페이지 요약·상세, 만료 안내).
3. `remaining_amount`(원장 부호합)와 `lot_remaining`(미소진 lot 합)을 한 행에 나란히 둔다 - 정상이면 같아야 하고, 어긋나면 지자체 없는 차감 같은 사고 신호다([[deferred-features]]에 적힌 과거 사고와 같은 유형).

분석 원문과 잔여 목록은 `docs/point-service-analysis-2026-09-09.md` §3-1, §7.

## report-statistics-analysis-db-pending-da

> 통계>보고서 6223~6227 - AS-IS 분석DB+월간배치 구조. TO-BE는 10-15에 통계표 22종을 admin 스키마에 만들고 AS-IS 데이터 적재해 화면 이식 완료, 채우는 배치는 미이식(데이터 고정). 표 위치·배치 원천은 DA 설계안 이후(2026-10-19 사용자). PL 공유 문서 있음


AS-IS에 "ReadModel" 이름은 없고, 별도 분석DB(ghlove_analysis, `DatabaseAnalysis.java`·`analysis-mapper.xml`)에
statisticsReportBatch(사용)·statisticsYearReport* 4(중지)가 월별/연간 통계를 쌓고 통계>보고서 5화면(6223~6227)이 그것만 읽는다.
★정정(10-19, pull 후 확인): TO-BE 5화면은 2026-10-15 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-report-general-port)에서 AS-IS 방식으로 재이식됨 - 통계표 22종(화면·엑셀 14 + 연간 8)을 admin 스키마에 AS-IS 이름 그대로 두고 AS-IS 데이터 적재. 조회모델 실시간 계산은 옛 구현(폐기). 표를 채우는 statisticsReportBatch 등은 미이식이라 데이터가 늘지 않음, 통계표 지자체코드는 옛 체계.

**Why:** 사용자 "현재는 분석DB가 없으니까 DA설계안이 나온 뒤에 어떻게 할지 결정하는게 맞다"(2026-10-19). PL 공유용 분석 문서:
https://claude.ai/code/artifact/13347c46-107a-48d5-b338-2e3e1995a220 (2번째 탭 "통계 보고서 분석DB 구조", 10-20 통합)
**How to apply:** DA 설계안 전에는 통계 배치 이식·통계표 위치 변경에 손대지 말 것(화면은 이미 AS-IS 방식). 공유 문서도 10-19 정정 반영함.
관련 [[da-design-pending]], [[batch-job-port-decisions]](원장 남은 사항 6번).

## possible-db-change-firstdb-unconfirmed

> [미확정] TO-BE DB가 또 바뀔 수 있다는 얘기(관리파트). '퍼스트DB'?라는 제품 언급 - 확인 필요


**[미확정·소문 단계]** 2026-10-01 사용자 전언: TO-BE DB가 **또 바뀐다는 말**이 관리 파트에서 나옴. 제품명이 **"퍼스트DB"(FirstDB?)** 비슷하게 들렸다는데 사용자도 재확인 예정.

- 현재 TO-BE DB = PostgreSQL 17(`ghlove-postgres` 컨테이너). AS-IS = CUBRID.
- "퍼스트DB"가 어떤 제품인지 Claude도 확신 못 함(국산 DBMS Tibero/CUBRID/Altibase/SUNDB/GoldiLocks 등과의 관계 불명). **지어내지 말고** 벤더/영문표기 확인 필요.

**영향 판단(선제):** DB 교체되어도 이식 작업 대부분 유효. 실제 영향 범위는 ① DDL 방언(`database/ddl/*.sql`) ② 네이티브/raw SQL ③ 시퀀스·타입매핑. JPA 기반이라 방언설정으로 상당부분 흡수. front/back/static·기능 parity는 DB-독립.

**대응 방침:** DB-독립적인 이식(커버리지 원장 → 누락보완)을 그대로 진행. DB 확정 시 DDL 레이어만 별도 대응. 확정되면 이 메모 갱신.

## admin-op-manager-migration-deferred

> admin.op_manager AS-IS 실데이터 이관 보류 - login_id/password/user_name까지 암호화라 옮겨도 안 읽힘, 사용자가 패스 결정(2026-10-07)


로그 화면 6개에 AS-IS 샘플 데이터를 적재한 뒤, "관리자ID가 비어 보이는 문제"를 근본적으로
고치려면 `admin.op_manager`도 AS-IS 데이터로 채워야 하지 않겠냐는 이야기가 나와 확인했다.

**막힌 이유**: AS-IS `op_manager`는 `login_id`/`password`/`user_name`까지(이메일·전화번호뿐
아니라) pCrypto로 암호화돼 있다. 그대로 옮겨도 "관리자ID" 칸에 `^`tymaIU1m4FaERtwiGPbSgg==`
같은 암호문이 뜬다 - TO-BE에 복호화 모듈이 없어서다(admin-log-screens-asis-sample-data(보관: docs/archive/memory-fix-log-2026-10.md)에
적어둔 "빈 칸으로 보일 수 있다" 캐뱃이 "암호문으로 보인다"로 바뀔 뿐, 실제로 읽히는 상태가
되지는 않는다).

**규모**: `op_manager` 자체 약 13,495건(전국 농협/수협 지점 담당자 포함). 관련 표 중
`op_manager_hist`(~1,500)/`op_manager_login`(~1,000)/`op_manager_password_log`(~2,000)/
`op_manager_notice`(~5)는 양이 적당하지만, `op_manager_login_email`(~40,000)과 특히
`op_manager_action_log`(**5,700만 줄**)은 전수 이전 대상이 전혀 아니다.
`op_manager_bakup`/`mig_op_manager`는 AS-IS 자체 백업/마이그레이션 스테이징 표라 대상 제외.

**현재 TO-BE 상태**: `admin.op_manager`에 로그인 테스트용 계정 14개(admin01, hoyoyo0629 등
user_id 1000번대/9700번대 - 전부 합성 테스트 계정, AS-IS 실데이터 아님)가 있다. AS-IS
user_id 범위(3608, 4997472 등)와 겹치지 않아 충돌 걱정은 없지만, 이 발견 이후 사용자가
"패스하자"고 결정 - 이관을 보류했다.

**재론의 시 체크할 것**: (1) 복호화 모듈을 만들 것인지(AS-IS pCrypto 키 필요, 별도 과제),
(2) 복호화 없이도 쓸모가 있는 범위(예: user_id/locgov_code/psitn_dept_nm처럼 암호화 안 된
컬럼만 참조하는 기능)가 있는지, (3) 관련 표 중 어디까지(앞서 언급한 "적당한 양" 4개) 포함할지.

## order-table-missing-indexes

> ord 스키마 주문 표들에 인덱스가 PK 하나뿐이었다 - 보강 DDL은 작성했으나 테이블 소유자가 postgres라 내가 적용 못 함


## 주문 도메인 인덱스 누락 (2026-10-13)

통계(일자별/월별 매출) 쿼리를 timestamp 비교로 바꾸면서 인덱스를 확인했더니
**`ord.od_order` 에 인덱스가 PK(`od_order_pkey`) 하나뿐**이었다. 기간 조건이 걸리는 화면이
통계·주문목록·클레임 등 여러 곳인데 전부 풀스캔이다.

### 실측한 현황

| 표 | 인덱스 수 | 행수 | 비고 |
|---|---|---|---|
| od_order | **1 (PK만)** | 44 | created_date·locgov_code 로 늘 조회 |
| od_claim | **1 (PK만)** | 5 | `findByOrderIdIn` 인데 order_id 인덱스 없음 |
| od_claim_memo | **1 (PK만)** | 9 | `findByClaimId...` 인데 claim_id 인덱스 없음 |
| od_excel_download_log | 1 (PK만) | 2 | 조회 메서드가 없어 제외 |
| od_cart_item / od_shipment / od_order_item | 2 / 2 / 4 | - | 이미 있음 |

### ★이 DDL은 저장소에 올리지 않았다 (2026-10-13 사용자 지시)

"마이그레이션은 다른 동료랑 협의를 해야 할 것 같으니 git 올리지 마라" →
`database/ddl/migration-order-date-and-child-indexes.sql` 은 **작성만 해 두고
`.git/info/exclude` 에 넣어 로컬 보관** 중이다. 그래서 다른 사람 저장소에는 이 파일이 없다.
**협의가 끝나면 exclude 에서 그 한 줄만 지우면** 바로 커밋 대상이 된다.
(인덱스는 스키마 공용 자원이라 혼자 넣으면 동료 작업과 충돌할 수 있다 - 합리적인 보류다.)

### 작성한 DDL - `database/ddl/migration-order-date-and-child-indexes.sql` (로컬 전용)

코드가 **실제로 그 컬럼으로 조회하는 것만** 넣었다(추측 인덱스 금지).
- `ix_od_order_created_date (created_date)` - 통계는 기간만 걸고 전체를 훑는다
- `ix_od_order_locgov_created (locgov_code, created_date)` - 지자체담당자는 자기 지자체 고정이라
  등치+범위가 늘 같이 온다
- `ix_od_claim_order (order_id)` / `ix_od_claim_memo_claim (claim_id, created_date DESC)` - 자식 표
- 상태(order_status)별은 **일부러 뺐다** - 쓰기 비용 대비 효과가 불확실하고 보통 기간/지자체와
  같이 걸린다. 필요해지면 그때.

### ★아직 적용 안 됨 - 내 권한으로는 못 한다

`ord` 스키마 테이블은 전부 **owner 가 `postgres`** 다. 인덱스 생성은 테이블 소유자만 가능해서
서비스 계정(`orderdb`)으로는 `ERROR: must be owner of table od_order` 가 난다.
개발DB는 원격(10.0.110.37:15432)이고 이 PC에는 docker 도, postgres 수퍼유저 접속 정보도 없다.

**사용자가 실행해야 한다**(저장소 관례는 `grant-dev-cross-schema-read.sql` 머리말과 같다):
```bash
docker exec -i ghlove-postgres psql -U postgres -d ghlove_core \
  -f - < database/ddl/migration-order-date-and-child-indexes.sql
```

**How to apply(다음에 같은 상황):** DDL 변경이 필요하면 **먼저 소유자를 확인**한다 -
`select tablename, tableowner from pg_tables where schemaname='...'`.
owner 가 postgres 면 내가 적용할 수 없으니 파일만 만들고 실행 명령을 드린다.
(admin 스키마는 `admindb` 가 소유해서 [[backend-db-pitfalls]] 때처럼 내가 적용할 수 있었다.)

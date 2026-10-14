---
name: order-table-missing-indexes
description: ord 스키마 주문 표들에 인덱스가 PK 하나뿐이었다 - 보강 DDL은 작성했으나 테이블 소유자가 postgres라 내가 적용 못 함
metadata:
  type: project
---

# 주문 도메인 인덱스 누락 (2026-10-13)

통계(일자별/월별 매출) 쿼리를 timestamp 비교로 바꾸면서 인덱스를 확인했더니
**`ord.od_order` 에 인덱스가 PK(`od_order_pkey`) 하나뿐**이었다. 기간 조건이 걸리는 화면이
통계·주문목록·클레임 등 여러 곳인데 전부 풀스캔이다.

## 실측한 현황

| 표 | 인덱스 수 | 행수 | 비고 |
|---|---|---|---|
| od_order | **1 (PK만)** | 44 | created_date·locgov_code 로 늘 조회 |
| od_claim | **1 (PK만)** | 5 | `findByOrderIdIn` 인데 order_id 인덱스 없음 |
| od_claim_memo | **1 (PK만)** | 9 | `findByClaimId...` 인데 claim_id 인덱스 없음 |
| od_excel_download_log | 1 (PK만) | 2 | 조회 메서드가 없어 제외 |
| od_cart_item / od_shipment / od_order_item | 2 / 2 / 4 | - | 이미 있음 |

## ★이 DDL은 저장소에 올리지 않았다 (2026-10-13 사용자 지시)

"마이그레이션은 다른 동료랑 협의를 해야 할 것 같으니 git 올리지 마라" →
`database/ddl/migration-order-date-and-child-indexes.sql` 은 **작성만 해 두고
`.git/info/exclude` 에 넣어 로컬 보관** 중이다. 그래서 다른 사람 저장소에는 이 파일이 없다.
**협의가 끝나면 exclude 에서 그 한 줄만 지우면** 바로 커밋 대상이 된다.
(인덱스는 스키마 공용 자원이라 혼자 넣으면 동료 작업과 충돌할 수 있다 - 합리적인 보류다.)

## 작성한 DDL - `database/ddl/migration-order-date-and-child-indexes.sql` (로컬 전용)

코드가 **실제로 그 컬럼으로 조회하는 것만** 넣었다(추측 인덱스 금지).
- `ix_od_order_created_date (created_date)` - 통계는 기간만 걸고 전체를 훑는다
- `ix_od_order_locgov_created (locgov_code, created_date)` - 지자체담당자는 자기 지자체 고정이라
  등치+범위가 늘 같이 온다
- `ix_od_claim_order (order_id)` / `ix_od_claim_memo_claim (claim_id, created_date DESC)` - 자식 표
- 상태(order_status)별은 **일부러 뺐다** - 쓰기 비용 대비 효과가 불확실하고 보통 기간/지자체와
  같이 걸린다. 필요해지면 그때.

## ★아직 적용 안 됨 - 내 권한으로는 못 한다

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
(admin 스키마는 `admindb` 가 소유해서 [[admin-excel-download-log-500-fix]] 때처럼 내가 적용할 수 있었다.)

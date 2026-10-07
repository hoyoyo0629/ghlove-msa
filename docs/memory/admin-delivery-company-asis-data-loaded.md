---
name: admin-delivery-company-asis-data-loaded
description: "시스템관리 > 배송업체 관리(ord.op_delivery_company) AS-IS 실데이터 29건 적재 완료(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

admin "배송업체 관리"(`DeliveryCompanyAdminController`, 표는 order 서비스의
`ord.op_delivery_company`)는 테이블/컬럼은 이미 AS-IS와 동일하게 존재했지만 데이터가
0건이었다. AS-IS export(`Desktop\고향사랑e음\1.AS-IS\1. DB\_op_delivery_company__202610071247.sql`)
로 실데이터 29건(한진택배/롯데택배/CJ대한통운 등 실 택배사 + 우편등기 중복 4건(31~34, use_flag
혼재) + id 2000101 '투데이')을 그대로 적재했다.

**적용**: `database/ddl/migration-order-delivery-company-asis-data.sql`(신규, 적용됨) -
`ON CONFLICT DO NOTHING`으로 INSERT 후 `op_delivery_company_delivery_company_id_seq`를
`2000101`로 `setval`(관리자 신규등록 시 ID 충돌 방지, 기존 공통코드/정책 적재 때와 동일 패턴).

**상태**: order 서비스 DB에 바로 적용 완료(psql, docker exec). 코드 변경 없음 - 재기동 불필요,
admin 화면에서 즉시 보인다(order API를 매 요청 다시 조회하는 구조라서).

---
name: admin-policy-asis-data-loaded
description: "약관관리(OP_POLICY) AS-IS 실데이터 22건 적재 완료(2026-10-07) - 테이블 하나뿐, 컬럼 구조 이미 일치, 시퀀스 보정"
metadata:
  node_type: memory
  type: project
---

[[policy-content-db-migration-deferred]]에서 "운영데이터 받아 적재 후 재검토"로 보류해 둔 것을
사용자가 export해준 `OP_POLICY_202610071425.sql`(22건, 9.1MB - 약관 전문이 커서 큼)로 완료했다.

AS-IS 테이블은 `OP_POLICY` 하나뿐(policy-mapper.xml 확인). TO-BE DDL·`Policy` 엔티티 컬럼이
이미 export와 1:1 일치해(POLICY_ID/POLICY_TYPE/CONTENT/CREATED_DATE/CREATED_USER_ID/TITLE/
EXHIBITION_PERIOD/EXHIBITION_STATUS/UPDATED_DATE/UPDATED_LOGIN_ID/EXHIBITION_START_DATE/
EXHIBITION_END_DATE) 변환 없이 그대로 `psql`로 적재. 적재 후 `op_policy_policy_id_seq`를
최대 ID(2000125) 이후로 `setval` 보정 - 안 했으면 화면에서 신규 등록 시 기존 ID와 충돌했을 것.

**데이터 특징**: POLICY_TYPE별 여러 버전이 날짜순으로 쌓여있다(개인정보처리방침만 15건,
생성일 2022~2026 범위) - AS-IS가 전시기간 지정형 버전관리로 쓰는 것과 일치
(`Policy.java` 클래스 주석의 "버전 관리처럼" 설명 그대로). 타입별 전시중(Y) 행은 정확히
하나씩이다(0=1·1=1·5=1, policy_id=2000125는 title에 "테스트"라고 적혀 있는데 개발DB
원본 그대로라 안 고쳤다). TYPE 2·3(특정상거래법·마케팅이용약관)은 등록된 행이 아예 없다 -
결함이 아니라 AS-IS에 그 타입 데이터가 없는 것(donation의 해당 API는 이 경우 정적
폴백파일을 쓰는데, 이것도 AS-IS와 같은 결과). TYPE 6(개인정보 수집이용 동의) 행이 여럿
있는데, `Policy.getPolicyTypeLabel()`에 '6' 분기가 없어 목록에 구분이 빈칸으로 나오는 것도
AS-IS 그대로([[as-is-parity-includes-disabled-state]], 건들지 않음).

**상태**: 순수 데이터 적재, 코드 변경 없음(빌드·재기동 불필요). 화면에서 바로 확인 가능.

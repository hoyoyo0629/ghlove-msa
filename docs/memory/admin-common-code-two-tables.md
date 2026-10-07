---
name: admin-common-code-two-tables
description: "admin 공통코드는 표가 둘이다 - JPA(CommonCodeService.labelsOf)는 ADMIN_COMMON_CODE, 네이티브 SQL은 op_common_code. 코드 추가 전 읽는 경로부터 확인할 것"
metadata:
  type: project
---

admin 서비스에서 공통코드를 읽는 경로가 **두 갈래**이고, 각자 **다른 표**를 본다.

| 읽는 경로 | 표 | 비고 |
|---|---|---|
| JPA 엔티티 `CommonCode` → `CommonCodeService.labelsOf()` / `listByType()` | `admin.ADMIN_COMMON_CODE` | 언어 컬럼명이 `code_language` |
| 네이티브 SQL 10여 곳(Mnl·Qna·OffSrBbs·HonorUser·OffPersonInCharge…) | `admin.op_common_code` | 언어 컬럼명이 `language` |

2026-10-07 실측: 코드유형 **71개가 양쪽에 중복**. `ADMIN_COMMON_CODE` 전용 7개
(`BSNS_PURPS_CODE`·`DELIVERY_CARRIER`·`MANAGER_STATUS`·`NOTICE_CATEGORY`·`POLICY_TYPE`·
`POPUP_TYPE`·`SETTLEMENT_STATUS`), `op_common_code` 전용 1개(`CMNTY_FAQ_TYPE`).

## 이미 두 번 사고가 났다

1. **담당자용 FAQ(11405) 질문유형 탭이 '전체' 하나만 보임** — `CMNTY_FAQ_TYPE` 11건을
   `op_common_code`에만 넣었는데 화면은 `labelsOf` = JPA 경로였다.
   → `migration-admin-cmnty-faq-type-codes-admin-table.sql`로 같은 11건을 `ADMIN_COMMON_CODE`에도 적재.
   **사용자 결정(2026-10-07): 이 화면은 ADMIN_COMMON_CODE를 보게 둔다**(코드 수정 없이 데이터만 맞춤).
2. **"ADMIN_COMMON_CODE는 없는 표"라는 오판** — `LocgFaqAdminController` 주석이 그렇게 단정하고
   있었으나 표는 실재하고 1,134행이 있다. 그 주석은 2026-10-07에 정정했다.

## 규칙

**공통코드를 새로 넣거나 "코드 라벨이 빈 값"이 나오면, 읽는 쪽이 JPA인지 네이티브 SQL인지부터 본다.**
`labelsOf(...)`면 `ADMIN_COMMON_CODE`, 쿼리에 `op_common_code`가 박혀 있으면 그쪽이다.
확신이 안 서면 양쪽 표를 다 조회해 본다.

표 일원화(엔티티를 `op_common_code`로 돌리고 전용 7유형 23행 이관)는 **별도 과제로 보류** -
`labelsOf()` 호출이 53곳·관련 파일 31개이고, 같은 코드의 `use_yn`이 양쪽에서 다른 경우가 있어
켜짐/꺼짐이 바뀔 수 있다. 관련: [[report-structural-landmines-to-user]]

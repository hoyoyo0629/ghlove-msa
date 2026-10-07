---
name: admin-common-code-two-tables
description: "[2026-10-07 후속 조율로 해소] admin 공통코드가 표 둘로 갈려 있던 상태 기록 - 같은 날 op_common_code 단일표로 통합 확정, ADMIN_COMMON_CODE는 DROP됨"
metadata:
  type: project
---

**★[2026-10-07 같은 날 후속 조율] 이 메모가 기록한 "두 표 유지" 상태는 해소됐다.** 병행 조사에서
AS-IS 소스 41개 매퍼 전체에 `ADMIN_COMMON_CODE`가 0건임이 확인돼, 그 표는 TO-BE가 지은
이름이었다고 결론 - 사용자 확인 후 `op_common_code`(AS-IS 실명) 하나로 통합하고
`ADMIN_COMMON_CODE`는 DROP했다. 아래 "이미 두 번 사고가 났다"의 1번(FAQ 11405)은 애초에
`CMNTY_FAQ_TYPE`이 `op_common_code`에 있었으므로 통합만으로도 그대로 해결된다. 자세한 경위·
위험성 검토는 [[admin-common-code-asis-sync-2026-10-07]] 참고. 아래 본문은 당시 상태 기록으로
그대로 둔다.

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

---
name: admin-common-code-two-tables
description: "[해소됨 - 과거 기록] admin 공통코드가 2026-10-07 하루 동안 표 둘로 갈려 있던 사건. 지금은 op_common_code 단일표이고 ADMIN_COMMON_CODE는 DROP됐다"
metadata:
  type: project
---

> **⚠ 이 메모는 끝난 사건의 기록이다. 지금 따라야 할 규칙은 여기 없다.**
> 현재 공통코드는 **`admin.op_common_code` 하나뿐**이고 `CommonCode` 엔티티가 그 표를 가리킨다
> (언어 컬럼 `LANGUAGE`). `ADMIN_COMMON_CODE`는 **DROP됐다** - 이 이름이 보이면 옛 코드다.
> 현행 내용은 [[admin-common-code-asis-sync-2026-10-07]]를 본다.

## 무슨 일이 있었나 (2026-10-07 하루)

admin 공통코드를 읽는 경로가 **두 갈래**였고 각자 **다른 표**를 봤다.

| 읽는 경로 | 당시 보던 표 |
|---|---|
| JPA 엔티티 `CommonCode` → `CommonCodeService.labelsOf()` | `admin.ADMIN_COMMON_CODE` (언어컬럼 `code_language`) |
| 네이티브 SQL 10여 곳(Mnl·Qna·OffSrBbs·HonorUser…) | `admin.op_common_code` (언어컬럼 `language`) |

코드유형 71개가 양쪽에 중복이었다. 이 상태에서 **담당자용 FAQ(11405) 질문유형 탭이 '전체'
하나만 보이는 증상**이 났다 - `CMNTY_FAQ_TYPE` 11건이 `op_common_code`에만 있었는데 화면은
JPA 경로였기 때문이다.

처음엔 "두 표가 둘 다 실재하니 화면이 보는 쪽에 데이터를 맞추자"로 좁게 패치했다
(`ADMIN_COMMON_CODE`에도 11건 복제). **그 판단은 뒤집혔다.** 같은 날 병행 조사에서
**AS-IS 소스 41개 매퍼 전체에 `ADMIN_COMMON_CODE`가 0건**임이 확인됐다 - AS-IS의 공통코드
표는 `OP_COMMON_CODE` 하나뿐이고, `ADMIN_COMMON_CODE`는 TO-BE가 지어낸 이름이었다.
사용자 확인 후 **단일표(`op_common_code`)로 통합하고 `ADMIN_COMMON_CODE`는 DROP**했다.

좁은 패치용으로 만들었던 `migration-admin-cmnty-faq-type-codes-admin-table.sql`은
**사용자 결정으로 삭제했다**(대상 표가 없어져 재실행하면 `relation does not exist`).
FAQ 11405는 `CMNTY_FAQ_TYPE`이 원래부터 `op_common_code`에 있었으므로 통합만으로 해결됐고,
`migration-admin-cmnty-faq-type-codes.sql`(원본, `op_common_code` 대상)은 지금도 유효하다.

## 배울 점 - 이게 이 메모를 남겨 두는 이유

1. **"표가 둘 다 실재한다"는 실측이 "둘 다 맞다"는 뜻은 아니다.** 나는 TO-BE 양쪽 표를
   조회해 중복을 확인하는 데서 멈췄고, **AS-IS 소스에 그 표 이름이 있는지**를 묻지 않았다.
   그 한 번의 grep이 결론을 뒤집었다. [[as-is-logic-is-the-spec]]대로 정본은 AS-IS다.
2. **TO-BE에만 있는 이름은 의심 대상이다.** `ADMIN_COMMON_CODE`·`CODE_LANGUAGE` 모두
   TO-BE 발명이었다. [[no-invented-features-ask-first]]
3. 같은 날 **다른 세션이 정반대 결론을 병행으로 내고 있었다.** 구조적 결함을 발견하면
   좁게 때우기 전에 사용자에게 범위와 선택지를 보고할 것 - [[report-structural-landmines-to-user]]

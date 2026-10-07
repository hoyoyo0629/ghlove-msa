---
name: admin-common-code-asis-sync-2026-10-07
description: "시스템관리 > 공통코드 관리 2건 수정(2026-10-07) - 코드(id) 필드 readonly 제거, ORDERING NULL 정렬이 CUBRID/Postgres 기본값 차이로 반대순서 되던 것 수정"
metadata:
  node_type: memory
  type: project
---

**1) 코드(id) 입력 readonly 제거**: AS-IS `code/form.jsp`는 "코드구분"만 readonly이고 "코드"(id)는
수정화면에서도 입력 가능하다. TO-BE [codes/form.html](../../admin/src/main/resources/templates/codes/form.html)이
`th:readonly="${!isNew}"`를 넣어 수정화면에서 막아 둔 걸 제거했다(AS-IS와 동일하게 항상 편집 가능).
**단 알아둘 것**: AS-IS JSP의 `<form:form>`은 action 속성이 없어 Spring이 현재 URL(쿼리스트링
제외)로 self-post하므로 `whereCodeType`/`whereId`(원래 키) 히든필드가 애초에 없다 - id를 실제로
바꿔 저장했을 때 AS-IS가 제대로 rename하는지는 의심스럽다(코드상 추적 결과 0건 UPDATE로 끝날
가능성). TO-BE는 `update(code.getCodeType(), code.getId(), ...)`가 제출된(새) id로 기존 행을 찾으므로
**id를 바꾸면 "코드를 찾을 수 없습니다" 에러**가 난다 - AS-IS의 모호한 동작을 그대로 재현하진
않았고, 사용자가 명시적으로 요청한 "필드 편집 가능"만 맞췄다. rename을 실제로 지원해야 하면
추가 작업 필요(사용자 확인 후).

**2) 목록 정렬 - ORDERING NULL 처리 차이**: 데이터는 틀리지 않았다(TEL/ORDER_PAY_TYPE/
CANCEL_REASON/EXCHANGE_REASON/RETURN_REASON/REMITTANCE_TYPE 등은 AS-IS 원본부터 ORDERING이
비어 있다 - TO-BE가 지어낸 게 아니라 export 그대로임). 원인은 **DB 엔진 기본 NULL 정렬 방향
차이**: CUBRID(MySQL 계열)는 ASC에서 NULL을 가장 작은 값으로 취급해 맨 앞(NULLS FIRST), PostgreSQL
ASC 기본은 반대로 맨 뒤(NULLS LAST)다. `ORDER BY ordering`만 쓰면 ORDERING이 비어 있는 행들이
AS-IS와 반대쪽 끝에 몰린다. [CommonCodeRepository](../../admin/src/main/java/com/ghlove/admin/repository/CommonCodeRepository.java)의
ordering 정렬 쿼리 3곳(`search`, `findByCodeTypeOrderByOrdering`, `findAllByOrderByCodeTypeAscOrderingAsc`)
전부 `order by ... nulls first`로 명시해 CUBRID와 같은 순서로 맞췄다. `findByCodeTypeOrderByOrdering`은
ManualAdminService/OpEmailService/PrivacyAccessLogService도 같이 쓰므로 그 화면들의 코드 드롭다운
순서도 같이 고쳐졌다.

**★3)[2026-10-07 후속] 테이블 자체가 틀렸다 - ADMIN_COMMON_CODE는 AS-IS에 없다**: 사용자가
"as-is도 admin_common_code를 보는 게 맞는지"를 재확인하라고 지적. AS-IS 소스 전체(41개 매퍼 +
code-mapper.xml) grep 결과 **"ADMIN_COMMON_CODE"는 0건** - AS-IS의 유일한 공통코드 테이블은
`OP_COMMON_CODE`뿐이다(실사용, 60곳 이상에서 셀렉트박스 옵션을 읽음). 그런데 `CommonCode`
엔티티가 지금까지 `ADMIN_COMMON_CODE`(TO-BE가 지은 이름)를 가리키고 있었다 - AS-IS에 없는
이름을 TO-BE가 임의로 만든 것([[no-invented-features-ask-first]]류). 실제로 화면이 읽는 그
테이블의 데이터가 부실했다(MAINTEN_STATE_CODE의 DETAIL이 전부 빈 값 등) - "코드상세가 AS-IS와
다르다"(사용자 지적 3번)의 진짜 원인이었다. 반대로 `OP_COMMON_CODE`(AS-IS 이름과 동일, 앞서
2026-10-02 export로 정확히 적재해 둠)는 아무도 안 읽는 고아 테이블이었다.

**조치**: `migration-admin-common-code-retire-wrong-table.sql`로 ADMIN_COMMON_CODE에만 있던
TO-BE 전용 의미코드 7종(BSNS_PURPS_CODE/DELIVERY_CARRIER/MANAGER_STATUS/NOTICE_CATEGORY/
POLICY_TYPE/POPUP_TYPE/SETTLEMENT_STATUS, 29행)을 OP_COMMON_CODE로 옮긴 뒤(79개 타입 전부
확인), `CommonCode.java`를 `@Table(name="OP_COMMON_CODE")` + 언어 컬럼명 `LANGUAGE`(AS-IS와
동일, 기존 `CODE_LANGUAGE`는 TO-BE가 지은 이름)로 전환. ADMIN_COMMON_CODE는 이제 완전히
미사용 - DROP은 사용자 확인 후(보류 원칙).

**★4) 수정 저장 시 에러**: "정렬순서" 입력이 비어 있는 상태(AS-IS op:negativeNumberToEmpty가
음수/빈값을 빈 문자열로 보여주는 그 상태)로 저장하면 `CommonCode.ordering`(Integer) 자동
바인딩이 터져 500 에러. `@ModelAttribute`에 BindingResult가 없어 컨트롤러 메서드 진입 전에
던져지는 예외라 메서드 내부 try/catch로 못 잡는다 - `CommonCodeController`에 `@InitBinder`로
빈 문자열→null 허용 `CustomNumberEditor` 등록. 겸사겸사 `edit()`에도 `create()`와 같은
try/catch(CommonCodeException) 추가(기존엔 수정만 에러 처리가 빠져 있었다).

**★5)[후속, 같은 날] ADMIN_COMMON_CODE DROP 완료**: 사용자가 "as-is도 admin_common_code를
보는 게 맞는지"를 재확인하라고 지적 → 위 3)에서 AS-IS에 그 이름이 없다는 걸 재확인한 뒤
`DROP TABLE admin.admin_common_code` 실행(FK/뷰 의존 0건 확인 후). `service-admin.sql`에서
`CREATE TABLE ADMIN_COMMON_CODE` + 거기 묶여 있던 INSERT 11건(NOTICE_CATEGORY/POPUP_TYPE/
SETTLEMENT_STATUS×2/DELIVERY_CARRIER/FAQ_TYPE/MANAGER_STATUS/REQST_SE_CODE/BSNS_PURPS_CODE/
11ST_DELIVERY_COMPANY/POLICY_TYPE)도 제거 - 재기동/볼륨초기화 시 되살아나지 않도록. 제거 전
전부 OP_COMMON_CODE에 동등하거나 더 정확한 데이터가 이미 있는지 확인했다(REQST_SE_CODE는
AS-IS 진짜 값이 ROLE_ADMIN_* 코드로 이미 들어있었고, TO-BE가 지었던 LOCALGOV/PROVIDER/OPERATOR는
애초에 틀린 값이었다). 빈 깡통 주석 블록·스테일 주석 4곳(OP_NOTICE 연결 주석, QNA_GROUPS
임포트 주석, xlsx 마스터데이터 주석, POLICY_TYPE 주석)도 OP_COMMON_CODE 기준으로 정정.
`LocgFaqAdminController`의 "FAQ_TYPE이 빈 값으로 나온다" 결함 주석도 이미 해소된 상태였음을
확인해 갱신(별도 migration-admin-faq-type-codes-fix.sql로 전에 고쳐져 있었다). 임시 DB에
`service-admin.sql` 전체를 처음부터 적용해 문법 무결성 확인(EXIT=0).

**상태**: 빌드·테스트·스페어포트 기동·DDL 전체재적용 전부 통과, admin bootJar 완료
(2026-10-07 11:51, DROP은 그 이후 DB에 바로 적용). 재기동 대기.

---
name: admin-isms-config-port-progress
description: "시스템관리 > ISMS관리(/isms-config) AS-IS 동기화 완료(2026-10-07) - 발명 시드 제거, 실데이터 12건+정렬 버그 수정"
metadata:
  node_type: memory
  type: project
---

**AS-IS**: `saleson.shop.config.ConfigIsmsManagerController`(`/opmanager/isms/isms-config`) +
`isms-config.jsp` + `config-isms-mapper.xml`(`OP_CONFIG_ISMS`, `ORDER BY ISMS_TYPE, ORDERING`).
구분(0=공통/1=관리자/2=회원)별 rowspan 묶음 표, 값만 수정 가능(키·설명·정렬은 편집 불가),
저장은 전체 입력칸을 JSON 배열로 모아 한 번에 POST.

**발견된 문제(2026-10-07, 사용자 지적)**:
1. **발명 데이터**: 기존 시드(`service-admin.sql`)가 `AD_SEND_START_HOUR`/`AD_SEND_END_HOUR`/
   `AD_SEND_RESTRICT`(광고성 메일/문자 발송시간) 3건을 "배치 스캔 당시 0건이라 실제 키를
   몰라 합리적인 기본값으로" **지어내서** 시드해 둔 상태였다 - AS-IS에 존재하지 않는 키.
   [[no-invented-features-ask-first]] 위반 사례. 사용자가 CUBRID 개발DB에서 직접 export한
   `OP_CONFIG_ISMS_202610071026.sql`(12행)로 확인·교체했다(`migration-admin-isms-config-asis-sync.sql`).
   실제 12키: 관리자5(LIFE_TIME_MANAGER_ACTION_LOG·CHANGE_LOG, LIFE_TIME_LOGIN_LOG,
   SESSION_TIMEOUT_MANAGER, UNUSED_MANAGER) / 회원4(LIFE_TIME_USER_CHANGE_LOG·LOGIN_LOG,
   SESSION_TIMEOUT_USER, LOCK_TIME_PASSWORD) / 공통3(FAIL_PASSWORD_COUNT, LIFE_TIME_PASSWORD,
   LIFE_TIME_QUERY_LOG). SESSION_TIMEOUT_MANAGER는 이미 있었으나 ISMS_TYPE이 '0'(공통)으로
   잘못 들어가 있었다 - AS-IS는 '1'(관리자).
2. **정렬 버그**: `ConfigIsmsRepository.findAllByOrderByOrdering()`이 ORDERING만으로 정렬해
   AS-IS `ORDER BY ISMS_TYPE, ORDERING`과 달랐다. 실데이터는 ORDERING이 타입을 가로질러
   섞여 있어서(회원 LOCK_TIME_PASSWORD=13이 공통 LIFE_TIME_PASSWORD=11과
   LIFE_TIME_QUERY_LOG=14 사이에 낌) ORDERING 단독 정렬 시 구분 rowspan 묶음이 깨진다 -
   `findAllByOrderByIsmsTypeAscOrderingAsc()`로 수정.

**기능 커버리지(기록만, 범위 밖)**: 12키 중 `SESSION_TIMEOUT_MANAGER`만 실제로 소비되고
([[admin-date-format-opdate-helper]]와 같은 축, `ManagerAuthAdvice.managerTimeout()`) 나머지
11개(패스워드 만료 180일, 로그인 실패 5회 잠금, 잠금시간, 관리자 계정 미사용 35일, 각종
로그 보관기간)는 **값만 있고 그 값을 읽어 동작하는 로직이 TO-BE에 없다**. 화면/데이터 자체는
AS-IS와 일치하나, 이 키들을 실제로 적용하는 기능(로그인 실패 카운트/잠금, 비번 만료 강제,
계정 휴면 처리, 로그 보관주기 배치삭제)은 별도 과제로 남아 있다 - 구현 여부는 사용자 확인 필요.

**상태**: 빌드·테스트 통과, admin bootJar 완료. 재기동 대기.

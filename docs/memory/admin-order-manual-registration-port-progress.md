---
name: admin-order-manual-registration-port-progress
description: "AS-IS '관리자 주문 수기/엑셀 등록'(OrderAdminServiceImpl) 기능을 TO-BE에 포팅, 7210 정합성검증이 보는 AB코드 주문 생성경로 확보(2026-10-08)"
metadata:
  node_type: memory
  type: project
---

[[order-code-format-matches-asis]]에서 발견한 "AB" 접두사 주문코드의 출처를 추적한 결과물.
AS-IS에는 **두 개의 별개 주문코드 생성기**가 있다:
- `OrderServiceImpl.getNewOrderCode(OrderCodePrefix.FRONT)` = **"K"**+10자리(일반 고객 체크아웃)
- `OrderAdminServiceImpl.getNewOrderCode("A")` = **"A"**+프레임워크 시퀀스(실데이터는 "AB"+yyMMdd+
  8자리) - **관리자가 전화주문·오프라인거래를 직접 등록**하는 별개 기능(`/opmanager/order/admin/
  {list,upload-excel}`, 엑셀 업로드→스테이징(OP_ORDER_ADMIN/_DETAIL)→목록검수→확정의 2단계
  워크플로). 포인트사용 정합성검증(7210, `pointcheck-mapper`)은 네 쿼리 전부
  `ORDER_CODE LIKE 'AB%'`로 **이 관리자-등록 주문만** 본다.

TO-BE에는 이 기능 자체가 없어 7210이 엉뚱하게 일반 고객주문 전체를 보여주고 있었다
(구조적 결함, [[report-structural-landmines-to-user]]).

**포팅 범위(이번 라운드)**: AS-IS 엑셀 업로드+스테이징 워크플로(옵션그룹 UI·Daum 주소검색·
동일주문 묶음판정 포함, 500줄+ JSP)는 전체를 옮기면 별도 다일 과제라, **핵심(답례품 1건을
"AB" 코드로 직접 등록 - 7210이 보는 바로 그 데이터)만 단건 입력 폼**으로 먼저 구현했다.
Excel 업로드·스테이징 검수는 미포팅(필요하면 다음 라운드).

**order 서비스**:
- `OrderRepository.nextAdminOrderSeqForDate(datePrefix)` - `coalesce(max(substring(...)),0)+1`
  패턴(QestnarRepository 등과 동일), 날짜별로 1부터 다시 시작.
- `OrderAdminService.createManualOrder(ManualOrderRequest)` - AS-IS `insertOrderAdmin`과 같이
  **SAGA(PENDING→비동기 확정)를 타지 않고 바로 CONFIRMED**로 Order+Shipment+OrderItem을 만든다
  (이미 성립된 거래의 사후 기록이라서). **재고 차감 안 함**(AS-IS도 이 경로엔 재고갱신 호출이
  없다 - 관찰된 사실, 보존). 비회원은 `userId=0`(AS-IS와 동일한 게스트 관례) + `adminMemo`에
  구매자 이름/연락처 기록(TO-BE `Order`엔 비회원 구매자명 컬럼이 아예 없어서의 타협).
  admin readmodel(`stat_order_ledger`) 동기화를 위해 `publishCreated`+`publishConfirmed`를
  순서대로 발행한다(CONFIRMED만 보내면 선행 CREATED가 없어 readmodel에 반영 안 됨).
- `POST /api/admin/orders/manual` (`OrderAdminApiController`) 신규.
- `OrderReconciliationRepository.orderAmounts()`에 `WHERE o.order_id LIKE 'AB%'` 필터 추가
  (AS-IS `pointcheck-mapper` 네 군데와 동일 스코프) - **이게 진짜 버그 수정**이다.

**admin 서비스**: `OrderAdminClient.createManualOrder()` + `OrderAdminController`
`GET/POST /admin/orders/manual` + `order-admin/manual-form.html`(신규 화면, AS-IS verbatim
아님 - 단건 입력으로 축소했다고 템플릿 주석에 명시).

**★2026-10-08 정정 - 메뉴 위치를 확인 안 하고 2가지를 임의로 했다가 사용자 지적으로 되돌림**:
① 범위를 혼자 판단해서 단건입력으로 줄였다(위 "포팅 범위" 문단) ② 주문목록 화면에 "관리자
주문 등록" 링크를 **발명**해서 추가했었다. `asis_dump.op_menu`(AS-IS 원본, TO-BE가 remap하지
않은 값) 직접 조회로 진짜 위치를 찾았다: **3000(주문관리,status_code='2' 사용안함) >
3700(대량주문 관리,display_flag='N') > 3701(작업 목록,display_flag='N' →
`/opmanager/order/admin/list`)**. 최상위부터 전부 AS-IS 자체 비활성 - 운영자도 메뉴로는
못 들어간다. "오프라인 주문관리"(16409, `/opmanager/order/offList`)는 이름만 비슷한
**완전히 다른 기능**(결제수단이 무통장입금/ARS인 주문의 읽기전용 조회화면, 엑셀업로드 없음) -
처음에 이걸로 착각했다.
[[as-is-parity-includes-disabled-state]] 원칙대로 TO-BE도 이 화면을 메뉴/링크 어디에도
노출하지 않기로 되돌렸다(링크 제거) - 사용자 결정: "미사용 기능도 일단 동일하게 구현하기로
약속했잖아"(기능/코드는 유지, 노출 상태만 AS-IS처럼 숨김). `admin.op_menu`에 3700/3701을
AS-IS와 같은 display_flag='N'으로 자리만 심었다(`migration-admin-menu-order-admin-hidden.sql`,
2026-10-02 전수동기화가 "가시 트리만" 뽑을 때 이 가지 전체가 빠졌던 것을 보충). URL은
직접 입력해야 들어온다.

**검증**:
- `OrderRepositoryAdminCodeTest`(`@SpringBootTest` + `@Transactional` 롤백) - 실제 DB로
  "AB"+날짜+8자리 채번이 행 생성 후 이어지는지 확인.
- `OrderAdminServiceManualOrderTest`(Mockito) - CONFIRMED 즉시확정, 금액계산, CREATED→CONFIRMED
  발행 순서 확인.
- **검증용 샘플 데이터 25건**(`seed-order-admin-reconciliation-sample.sql` +
  `seed-point-reconciliation-sample.sql`, **실제 AS-IS 값이 아닌 현실적인 가짜 데이터** -
  사용자 결정, 2.7GB 단일행 AS-IS 덤프에서 샘플 추출이 비효율적이라 판단) - 18건 정상일치 +
  3건 취소(0=0으로 일치) + 4건 의도적 불일치(포인트 사용기록 누락/부분누락). 직접 SQL로 재계산해
  21일치/4불일치 정확히 확인함(화면 코드와 동일한 식).

**상태**: order·admin 모두 compileJava+test+bootJar 통과, 시드 적용 완료. **재기동 필요: order, admin.**

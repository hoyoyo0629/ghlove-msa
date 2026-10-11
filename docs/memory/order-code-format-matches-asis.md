---
name: order-code-format-matches-asis
description: "주문번호 생성 포맷을 TO-BE 발명값(O+시각+난수)에서 AS-IS와 동일한 K+10자리 시퀀스로 교체, 기존 19건도 일괄 변환(2026-10-08)"
metadata:
  node_type: memory
  type: project
---

**발견 경위**: 시스템관리 > 포인트사용 정합성검증(7210) 화면을 보다가 AS-IS 주문번호가
"AB23060903668531" 형태인 걸 사용자가 지적 - 추적해보니 AB는 **관리자 수기/엑셀 주문등록**
기능(`OrderAdminServiceImpl.getNewOrderCode("A")` = "A" + `sequenceService.getId(...)`가
만드는 "B..." 값)에서만 나오는 별개 코드이고, **일반 고객 주문**은
`OrderServiceImpl.getNewOrderCode(OrderCodePrefix.FRONT)` = **"K" + 10자리 0-패딩 숫자**
(`orderMapper.getOrderCodeNum()`, 예: K0000006106)라는 걸 확인했다. 사용자가 이어서
"일반 주문번호도 K로 시작하게 바꾸고 기존 데이터도 같은 포맷으로 바꿔달라"고 요청.

**수정**: `OrderService.generateOrderId()`를 `"O" + yyyyMMddHHmmss + 난수4자리`(TO-BE가
지어낸 값, AS-IS와 무관)에서 `"K" + String.format("%010d", nextval)`로 교체.
`OrderRepository.nextOrderCodeSeq()`(네이티브 `nextval('ord.op_order_code_seq')`) 추가 -
AS-IS `getOrderCodeNum()`과 같은 원자적 채번. `CartService`(멀티아이템 체크아웃)도 같은
메서드를 쓰므로 한 곳만 고치면 됨.

**기존 데이터 일괄 변환** (`migration-order-code-asis-format.sql`): 기존 19건을
`created_date` 오름차순으로 K0000000001~19로 매핑해 **order_id를 참조하는 모든 활성
테이블을 한 트랜잭션에서 같이** 바꿨다 - 하나만 바꾸면 조인이 끊어진다:
`ord.od_order`(PK) · `od_order_item` · `od_shipment` · `od_claim` ·
`point.g_cntr_use_point`(멀티아이템 접미사 `#N` 보존 필요) ·
`admin.stat_order_ledger`/`stat_claim_ledger`(order 서비스 readmodel 사본).
`ord.op_order*`류(AS-IS DDL만 있고 TO-BE 코드가 안 쓰는 레거시 표)는 전수 확인 결과 전부
0행이라 건드릴 필요 없었다. 시퀀스는 `setval(..., 19, true)`로 맞춰 다음 채번이 20부터
이어지게 했다.

**검증**: `OrderCodeGenerationTest`(`@SpringBootTest`, 실제 DB로 `K\d{10}` 포맷 확인 +
연속 호출 시 증가 확인 - 레포지토리를 목으로 때우면 SQL 자체가 실행되는지 검증이 안 돼서
([[admin-excel-download-log-500-fix]] 교훈) 실제 컨텍스트로 돌렸다). 마이그레이션 적용 후
`ord.od_order`/`point.g_cntr_use_point` 직접 조회로 전부 K-코드로 바뀐 것 확인.

**후속**: AS-IS의 "관리자 수기/엑셀 주문등록" 기능(AB코드, OrderAdminServiceImpl)을
TO-BE에 포팅하고 7210(포인트사용 정합성검증)이 AS-IS처럼 "AB%" 주문만 보도록 고쳤다 -
[[admin-order-manual-registration-port-progress]] 참고(2026-10-08 완료, 핵심 단건등록만
우선구현·엑셀업로드는 미포팅).

**상태**: order compileJava+test+bootJar 통과, DB 마이그레이션 적용 완료. **재기동 필요: order.**

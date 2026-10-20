---
name: offgive-gift-order-asis-flow
description: "기탁서 등록(15102) 대표답례품 주문 - AS-IS saveOffGiveOrder/procOffOrder 이식(2026-10-13): 등록=입금대기(재고·포인트 미차감), 수납 후 포인트 적립 이벤트로 출고 SAGA → 확정. 남은 갭 4건"
metadata:
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T05:50:11.120Z
---

**AS-IS 실제 동작(소스로 확인)**:
- 등록(`OrderServiceImpl.saveOffGiveOrder`): 포인트 부족 판정은 **주석처리**, 재고·판매가능·배송지(0331~0335)만 검증 →
  OP_ORDER_ITEM ORDER_STATUS **'0' 입금대기**, FILLER9=CNTR_SN, OFF_PROC_YN 기본 'N'. 끝의 "재고 차감"은
  `buy.setStockMap()` 호출이 없어 `updateStockDeduction(null)` → **등록 시 재고 안 줄어듦**(죽은 블록).
  실패해도 `insertOffgive`가 예외를 삼켜 기부 등록은 유지.
- 수납 후(`NgDonationBatchServiceImpl.procOffOrder`, cntrSunapBatch 안): 수납완료(200) 기부의 OFF_PROC_YN='N' 주문 →
  0→10 결제완료, 포인트 사용("답례품 구매", 금액=SUM(SALE_PRICE*QUANTITY), 배송비 제외, 부족해도 부분차감),
  재고 차감, OFF_PROC_YN='Y', 국민비서 PRESENT_ORDER 문자(구매자·판매자).

**TO-BE 구현**(order `OffgiveOrderService`, `/api/admin/offgive-orders`):
- 등록: OD_ORDER/OD_SHIPMENT/OD_ORDER_ITEM 전부 `WAITING_DEPOSIT`(라벨 "입금대기", ord.op_common_code),
  od_order_item.cntr_sn·off_proc_yn 신설(`migration-order-offgive-order-asis.sql`), SAGA 미발행.
  pointAmount=품목합(배송비 제외), deliveryFee는 기록만. 주문번호 K+10자리(AS-IS FRONT).
- 수납: 기부 COMPLETED → point 적립 → `point.ledger` EARN(**refKey=cntrSn 추가, 커밋 후 발행으로 변경**) →
  order `PointLedgerListener` → 미처리 품목 PENDING + `SHIPMENT_CREATED` → gift 재고/point 차감 → CONFIRMED.
  적립 이벤트를 기다리는 이유: 기부완료 이벤트에 바로 반응하면 차감이 적립보다 먼저 도착할 수 있다.

**2026-10-14 갭 4건 이식 완료**(재기동 대기): ① 미납 기부 삭제 배치 donation `NgDonationBatchScheduler`(00:03 서울
deleteCntrDataBatch / 00:05 서울외 deleteNotSunapStndBatch, 미납=REQUESTED로 번역 - TO-BE는 완료건도 STTEMNT_PAY_DE가 비어서)
+ order `OffgiveOrderBatchScheduler`(03:30 allDayOffItemUpdateBatch, 90~10일 전 입금대기 → donation `/api/offgive/deleted-unpaid`로
확인 → CANCELLED). op_batch_job에 AS-IS ID(26/29/2000113)·job_method 그대로 등록(`seed-admin-batch-job-unpaid-offgive.sql`).
② 국민비서 문자: 헤더가 PENDING→CONFIRMED될 때 cntrSn 품목이면 order.saga `OFFGIVE_ORDER_PAID` → admin
`PresentOrderSmsListener`(그룹 admin-sms-service) → `PresentOrderSmsService`(구매자+(판매자,답례품) 중복제거, 제공자 문자 이름칸=구매자명).
판매자 수신정보는 gift `/api/admin/sellers/{id}/sms-receiver`(op_seller_user에 mber_ci·receive_sms 추가, 데이터 0건이라 실제 발송 없음).
③ 모바일 수량분할(수량1 + N-1 복제, 출고 안에서 뒤에 붙임). ④ 서울 외 CNTR_SN=폼 #linkMngKey, 서울은 새 채번,
SEOUL_TRGET_AT·PAY_VALID_DE(서울 +1일) 저장.

**2026-10-14 후속(사용자 지시 "미이식 없게")**: 이벤트명 OFFGIVE_ORDER_PAID → `PRESENT_ORDER_PAID`(온라인 공용, admin은 옛 이름도 받음),
헤더가 PENDING→CONFIRMED/PARTIALLY_CONFIRMED면 cntrSn 무관하게 발행(부분확정은 확정 품목만). 온라인 체크아웃
(`CartService.checkoutMultiItem`)에 모바일 분할(복제분은 전 출고 원본 뒤에 저장 - AS-IS buyItems.addAll 순서, 배송비·할인은 첫 품목)
+AS-IS 배송지 검사 5종+header 우편번호 저장. 기탁서 수납 포인트는 AS-IS처럼 **부족해도 남은 만큼만 차감하고 성공**
(SHIPMENT_CREATED에 cntrSn 추가 → point `deductForShipment(partialAllowed)`).
재고도 같은 날 AS-IS대로(사용자 지시): gift `deductStockForOffgiveShipment` - 실패 없음. STOCK_FLAG='Y'면 재고>0일 때만 수량만큼 빼고(음수 가능),
아니면 S3 옵션만(옵션 재고연동·재고>0·이름 LIKE 포함 행 전부) 뺀다. S2는 3번째 이름이 NULL이라 CUBRID `'%'||NULL||'%'`=NULL로 안 맞아 안 빠진다
(CUBRID 기본 동작 기준 판정, AS-IS DB로 실측은 안 함). 복원 기록은 실제로 뺀 품목 자기 행만. 품절표시는 안 건드림.
**남은 것**: 온라인 기부 seoul_trget_at/pay_valid_de는 납부게이트웨이 라운드.

관련: [[offgive-gift-order-asis-flow]], [[domain-code-facts]]

## offgive-15102-verbatim-port-data-gaps

> 기탁서 등록(15102) 2026-10-13 verbatim 재이식 + 테스트 데이터 적재. 결함 2개(답례품 주문 미생성, S형 옵션 저장규칙 반대)는 같은 날 해결, 연계 우회도 로컬 전용으로 정리


15102 기탁서 등록을 form.jsp 1,995줄 전체 + OffgiveManagerController/OffgiveServiceImpl/offgive·give-state·ngdonation·
designated-donation 매퍼 기준으로 재이식(2026-10-13): 마크업·CSS·JS verbatim, 엔드포인트 AS-IS 이름,
실명인증 iframe(sci-call→sci-result), 지정기부목록 엑셀(`AsisSxssfExcelView`, 암호화 없음), PDF 버튼 같은 href.

**적재한 데이터(2026-10-13, 사용자 요청 "기능테스트에 필요한 일부만")**:
- `donation.g_locgov.ordering` AS-IS값 243건 (`migration-donation-g-locgov-ordering-asis-load.sql`) - 시군구 순서 AS-IS와 동일.
  남은 NULL 3건(46150·46530·36110)은 AS-IS에 대응값 없는 구코드.
- `gift.g_lclgv_off_rprs_gds_mng` **테스트 매핑 5건**(`seed-gift-offgive-representative-goods-test.sql`, frst_rgtr_id=0):
  강남구 11230(1000·1020, 서울 경로) / 해운대 26350(1001, 비서울 경로) / 제주 50000(1003·1014, 지정기부 1100 같이).
  AS-IS 실데이터는 AS-IS 상품ID라 TO-BE 시드상품(1000~1028)과 안 맞아 못 넣는다.
- **안 넣은 것**: g_cntr_lmtt(AS-IS 4건 전부 기간 지남), g_instt_code·g_adm_locgov(로컬 우회 중엔 화면 영향 없음, 실경로엔 필요).

**같은 날 해결한 결함**:
1. 대표답례품 주문 미생성(장바구니 체크아웃 재사용 → 포인트부족) → AS-IS saveOffGiveOrder/procOffOrder 이식,
   [[offgive-gift-order-asis-flow]].
2. S형 옵션 저장규칙 반대 → AS-IS(name1 제목/name2 내용)로 전환+이관, [[domain-code-facts]].
3. 연계 우회가 단일설정이라 운영에서도 우회되던 것 → 실경로 AS-IS 그대로 + 로컬 전용 우회, [[integration-bypass-local-only]].

**2026-10-14 등록 후 상세화면(detail.jsp "기탁서") 재이식**: 경로 `/offgive/detail/{접수번호}`(예전 `/offgive/{sn}`은 302),
getOffgive 한 줄을 donation(`/api/offgive/{sn}/detail`)·member(post·addressDetail 추가)·order(`/api/admin/offgive-orders/summary`,
품목명+옵션표시문자열 `<br/>`, `od_order.delivery_zipcode` 신설)·WDR로 조립. 기부취소(`/offgive/offgiveCancel` → donation
`/api/cntr-reqmng/offgive-cancel`, 입력검증 없음)·납부신청서 OZ 팝업(`ghlove.integrations.oz.url`) 이식. AS-IS `cancelTemPointSet`
(포인트 임시칸 이동)은 포인트가 point 원장이라 없음(변경신청 화면과 동일). TO-BE 전용 "수납처리" 버튼은 form 밖에 남김(사용자 결정 대기).

**How to apply:** 대표답례품 관리화면(AS-IS 답례품 목록의 "오프라인 대표 답례품 관리", ROLE_ADMIN_5/6,
`saveLclgvOffRprsGds`)은 미이식 - 매핑은 SQL로만 넣을 수 있다.

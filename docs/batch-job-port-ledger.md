# 배치 job 이식 원장 (2026-10-15)

기준: AS-IS `OP_BATCH_JOB` 실데이터 76행(`Desktop/.../1. DB/_op_batch_job__202610071247.sql`) + `ghlove-batch` `JobServiceImpl`(1,097줄)·`JobPsintServiceImpl`.
AS-IS는 `SchedulerServiceImpl.invokeJobChecker`가 15초마다 OP_BATCH_JOB을 읽어 Quartz에 등록/재등록/중지한다
(`BATCH_STATUS` 1=실행/2=중지, `TRIGGER_TYPE` 1=심플(초)/2=크론, 바뀌면 `BATCH_APPLY_FLAG=0`) - **관리 화면(1403) 설정이 곧 스케줄**이다.

TO-BE 현황: `admin.op_batch_job` 5행. 실제로 도는 것은 각 서비스에 cron을 박아 둔 `@Scheduled` 5개이고 화면 설정은 스케줄에 영향이 없다.
실행 결과는 `BatchExecutionReporter`(donation·point·order) → admin `op_batch_execution`(7209 화면).

## A. AS-IS 사용 중(BATCH_STATUS=1) - 41행

| id | job_method | 내용 | 소유 서비스 | 외부연계 | TO-BE |
|---|---|---|---|---|---|
| 26 | deleteCntrDataBatch | 서울 미결제분 삭제 00:03 | donation | - | ✅ 이식됨 |
| 29 | deleteNotSunapStndBatch | 지방세외 미납 삭제 00:05 | donation | - | ✅ 이식됨 |
| 2000113 | allDayOffItemUpdateBatch | 오프라인 미수납 답례품 상태 03:30 | order | - | ✅ 이식됨 |
| 3000000 | dsgnBizStatusUpdateBatch | 특정기부사업 모금완료·기간만료 종료 30분마다 | donation | - | △ `DesignatedAdminService.closeExpiredProjects`(1000, 매일 01:00) - 주기·job_method 다름, 로직 대조 필요 |
| 2000109 | cntrSunapBatch | 차세대 수납결과 → 수납완료·기탁서 답례품 주문 5분마다 | donation(+order) | 차세대 세외 | ❌ (임시 "수납처리" 버튼으로 대체 중) |
| 34 | fiveMinuteSunapCheckBatch | 세외 수납결과 실시간 반영 5분마다 | donation | 세외 | ❌ |
| 35 | dayCompleteSunapCheckBatch | 세외 수납결과 일정산 23:40 | donation | 세외 | ❌ |
| 2000105 | getNoBugaLocgovListBatch | 3일 기부내역 없는 지자체 월·목 09:00 | donation | (알림 발송 확인 필요) | ❌ |
| 2000114 | dailyGramtInsertBatch | 매일 총 기부금(G_DY_GRAMT) 03:00 | donation | - | ❌ |
| 2000103 | confirmdonationForReportBatch | 보고용 대시보드(REPORT_DASHBOARD) 18:00 | admin(+donation·point·gift·member) | - | ❌ (메인 "기부현황 복사" 0의 원인) |
| 2000111 | statisticsReportBatch | 월간 통계 현황보고 매월 1일 07:00 | ? | - | ❌ 통계표 TO-BE에 없음 |
| 2000001/2000106/2000107 | userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch | 농협 연계 04:25/04:30/04:35 | admin? | 농협 | ❌ (`NhDataClient` 있음) |
| 2000120 | sendNuri2PresentOrder | 답례품 주문내역 알림톡 09:00 | order→admin(nuri2) | 누리2 알림톡 | ❌ (nuri2 표는 admin에 있음) |
| 2000119 | updateWaitUserCacheBatch | 넷퍼넬 대기자 캐시 5분마다 | ? | NetFunnel | ❌ |
| 36 | itemDisplayContorlBatch | 마감일자 공개여부 02:10 | donation? | - | ❌ |
| 10 | autoConfirmPurchaseBatch | 구매확정(배송완료 7일) 01:20 | order(+point) | - | ❌ |
| 1000001 | autoShippingCompleteBatch | 배송완료(배송시작 7일) 01:10 | order | - | ❌ |
| 1000005 | autoSmartDeliveryShippingCompleteBatch | 스마트택배 배송완료 23:30 | order | 스마트택배 | ❌ (`SmartDeliveryClient` 있음) |
| 1000002 | deleteItemNewBatch | 상품 NEW 삭제 00:10 | gift | - | ❌ |
| 1000003 | deleteCartBatch | 장바구니 30일 삭제 00:10 | order | - | ❌ |
| 1000004 | sendRemittanceExpectedMsgBatch | 정산예정 문자 매월 1일 09:00 | order/admin | 문자 | ❌ |
| 3000001~3000020 | bix5* 20개 | BIX5 통계 적재 | - | (외부 BI) | ❌ BIX5 표 TO-BE에 없음 |

## B. AS-IS 중지(BATCH_STATUS=2) - 35행

- ghlove 고유 로직: statisticsYearReport·AgeMonth·Psint·Goods(연간통계 4), opUserBirthdayStatBatch, cntrTaxTempBatch, confirmSunapForDeletedListBatch,
  updateLockForManagerBatch, sendNtsEreceipt(On/Off/Old/One/전체 5), expirationPointBatch·expirationPointSendMessageBatch(포인트 만료 - TO-BE는 1001이 매일 돌고 있음 ★),
  processInactiveUserBatch·sendMailToInactiveUserBatch(휴면), cntrBugaTestBatch(테스트)
- SalesOn 원제품: 캠페인 3, 카카오 알림톡 업데이트, UMS 2, 쿠폰 2, 랭킹 3, 회원등급, 자동완성, 품절옵션, 입금지연 취소, 판매자 주문 SMS, 포인트 만료 메시지
- 다수가 JobServiceImpl에서 본문이 비어 있다("미사용 주석처리").

## 확인된 구조 차이

1. 스케줄 주체: AS-IS = OP_BATCH_JOB(화면) / TO-BE = 코드 고정 cron. 화면에서 중지·cron 변경이 반영되지 않는다.
2. TO-BE 등록 job_method가 AS-IS와 다른 것: 1000 `DesignatedAdminService.closeExpiredProjects`(AS-IS dsgnBizStatusUpdateBatch), 1001 `PointService.runExpirationBatch`(AS-IS는 포인트 만료 배치가 **중지** 상태).

## 결정 (2026-10-15 사용자)

1. **실행 방식 = AS-IS처럼 화면(op_batch_job) 설정으로.** 각 서비스가 admin의 op_batch_job을 주기적으로 읽어 자기 job_method의 사용/중지·cron·심플주기를 반영한다.
2. **중지 job = 76행 전부 AS-IS 상태로 등록.** 고향사랑 고유 로직(연간통계·국세청 영수증·관리자 잠금 등)은 구현하되 중지 유지, SalesOn 원제품 job은 행만 등록·기록.
3. **BIX5 20개 = 행만 등록, 적재 로직은 보류**(외부 BI·DA 설계 후).
4. **포인트 소멸 = AS-IS처럼 중지.** TO-BE 1001(PointService.runExpirationBatch, 매일 실행)을 AS-IS job_method·중지 상태로 바꾼다.

## 진행

### 1단계 - 실행 구조 전환 (2026-10-15 완료)
- admin `BatchJobInternalApiController`: `GET /api/admin/batch-jobs`(설정 목록), `POST /api/admin/batch-jobs/{id}/applied`(적용여부 '1'). 화면 수정 저장 시 적용여부 '0'(AS-IS updateBatchJob).
- 서비스별 `BatchJobScheduler` + `BatchJobSource`(donation·point·order, 공유모듈이 없어 같은 클래스 복제): 15초마다 설정 조회 →
  자기 job_method만 등록/재등록/해제(1=실행·2=중지, 트리거 1=심플(값은 밀리초, 고정주기)·2=크론), 반영 후 적용여부 '1'.
  실행로그는 AS-IS Aspect처럼 이름이 Batch로 끝나는 job만, 배치구분 `JobServiceImpl.메서드()`.
- 수동 실행 `POST /api/admin/batch/run/{jobMethod}`(AS-IS ManualBatchController) - 각 서비스.
- 기존 고정 @Scheduled 4개 제거 → `DonationBatchJobs`(deleteCntrDataBatch·deleteNotSunapStndBatch·dsgnBizStatusUpdateBatch),
  `PointBatchJobs`(expirationPointBatch, 중지), `OrderBatchJobs`(allDayOffItemUpdateBatch).
- dsgnBizStatusUpdateBatch는 AS-IS 쿼리로 새로 구현(목표금액 달성 + 기간만료, 공개분만, 이력 없음) - 예전 closeExpiredProjects는 기간만료만 봤다.
- 데이터: `migration-admin-op-batch-job-asis-load.sql`로 76행 적재(적용여부 0으로 시작).
- 검증: 기동 15초 내 소유 5행 적용여부 1 / 화면에서 cron을 매분으로 바꾸자 재등록·13:23 실행·7209에
  `JobServiceImpl.dsgnBizStatusUpdateBatch()` 정상 "종료 0건" 기록 → 원복.

### 2단계 - job 이식 1차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| confirmdonationForReportBatch | admin `AdminMainService.reportDashboardBatch` | 메인 하단표·등록답례품·콜·전체회원 → REPORT_DASHBOARD(오늘 지우고 넣기). 메인 "기부현황 복사" 0 해소 |
| dailyGramtInsertBatch | donation `DonationBatchService.insertDailyGramt` | G_DY_GRAMT 적재(AS-IS 화면은 더 이상 안 읽음) |
| itemDisplayContorlBatch | gift `GiftBatchService.closeDisplayOfEndedItems` | 마감일 어제인 공개 답례품 비공개 |
| deleteItemNewBatch | gift `GiftBatchService.clearNewLabel` | NEW 라벨 15일 경과 해제 |
| deleteCartBatch | order `OrderBatchService.deleteOldCartItems` | od_cart_item 30일 경과 삭제 |
| dsgnBizStatusUpdateBatch | donation `DesignatedAdminService.updateDesignatedDonationStatus` | 1단계에서 AS-IS 쿼리로 재구현 |

admin·gift에 스케줄러 추가(@EnableScheduling 신규, admin은 리포지토리 직접). 시퀀스 `op_batch_job_batch_job_id_seq`를 max로 보정.
검증: 여분 포트에서 수동 실행 5건 정상·7209 기록·적용여부 1, get-user 응답에 값(기부 34·회원 65·답례품 23·콜 126).

### 남은 사용 중 job (다음)
- 세외 수납: cntrSunapBatch(procOffOrder) · fiveMinuteSunapCheckBatch · dayCompleteSunapCheckBatch - 이식 시 기탁서 임시 "수납처리" 버튼 제거
- 주문: autoConfirmPurchaseBatch · autoShippingCompleteBatch · autoSmartDeliveryShippingCompleteBatch
- 연계: userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch(농협) · sendNuri2PresentOrder(누리2 알림톡) · updateWaitUserCacheBatch(넷퍼넬)
- 기타: sendRemittanceExpectedMsgBatch · statisticsReportBatch(통계표 없음) · getNoBugaLocgovListBatch(★수신자 결정 필요)
- 중지 상태 고향사랑 로직(연간통계 4·국세청 영수증 5·관리자 잠금 등)

### 2단계 - job 이식 2차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| getNoBugaLocgovListBatch | admin `NoBugaLocgovMailService` (+ donation `GET /api/admin/batch/no-buga-locgovs`) | AS-IS 수신자 8명 그대로(사용자 결정), 로컬은 `ghlove.batch.no-buga-mail.test-mode=true` → `test-recipients`(비면 등록만). 관리 화면 "농협배치테스트" 버튼도 이 본문(AS-IS batchForNh) |
| fiveMinuteSunapCheckBatch · dayCompleteSunapCheckBatch | donation `DonationBatchJobs.sunapCheck` + `DonationBatchService` | 대상 SQL·서울 이택스/차세대 수납확인 전문(`LocalTaxClient.sendEtaxSunapInfo`·`sendLocalSunapConfirm`, local 프로파일은 AS-IS LOCAL 가짜 "납부됨")·성공 시 변경신청 300 등록+승인·G_NEXT_SUNAP_RESPONSE "시스템 수납처리"·수납일=기부일자·SUNAP_PROCSS_YN='Y' |
| cntrSunapBatch | order `OrderBatchJobs.processPaidOffgiveOrders` | AS-IS procOffOrder = TO-BE 포인트 적립 이벤트 경로와 같은 처리를 5분마다 폴링(적립 존재 = 수납완료). 이벤트가 놓친 건 보정 |

- 기탁서 상세의 임시 "수납처리" 버튼·안내문·`POST /offgive/{sn}/confirm`·OffgiveClient.confirm·donation `/api/offgive/{sn}/confirm`·OffgiveService.confirm 제거.
- ★로컬(local 프로파일)에서는 AS-IS처럼 5분 안에 신고 기부가 자동 수납처리된다(AS-IS LOCAL 분기가 "납부됨"으로 응답).

### 2단계 - job 이식 3차 (2026-10-15)
| job | 위치 | 비고 |
|---|---|---|
| autoShippingCompleteBatch | order `OrderBatchService.autoShippingComplete` | 배송완료 기준일(OP_CONFIG, admin `GET /api/admin/shop-config/batch-days`) 지난 SHIPPED·IN_TRANSIT(AS-IS 30) → DELIVERED + 배송상태 이벤트 |
| autoConfirmPurchaseBatch | order `OrderBatchService.autoConfirmPurchase` | 구매확정 기준일 지난 DELIVERED → CONFIRMED. AS-IS 정산예정일·SalesOn 구매포인트는 TO-BE 대응 없음 |
| autoSmartDeliveryShippingCompleteBatch | admin `SmartDeliveryBatchService` + order `/api/admin/orders/batch/*` | 스마트택배 기준일·택배사→SMART_DELIVERY_COMPANY(MAPPING_CODE) 코드·level 6이면 배송완료. `SmartDeliveryClient.trackingInfo`(AS-IS sweettracker.url 내부 연계서버, disabled면 빈 응답 = 처리 없음) |

- OP_CONFIG 기준일이 TO-BE에서 비어 있던 것을 AS-IS 값으로(배송완료 7·스마트택배 2, `migration-admin-op-config-batch-days-asis.sql`).
- ★구조 문제: TO-BE 송장 등록은 택배사를 자유 문자열("CJ")로 저장하는데 AS-IS는 배송업체 ID(OP_DELIVERY_COMPANY)다 →
  스마트택배 코드 매핑이 안 돼 현재 데이터로는 스마트택배 배치가 늘 건너뛴다(송장 등록 화면·API 쪽 정리 필요).
- AS-IS 교환배송중(55)→교환배송완료(58)는 TO-BE 교환이 클레임 흐름이라 주문 배송상태에 대응 값이 없다.

### 2단계 - job 이식 4차: 외부 연계 (2026-10-19)
| job | 위치 | 비고 |
|---|---|---|
| userCntrForNhBatch · userCntrLocForNhBatch · locForNhBatch | admin `NhBatchService` + `NhDataClient`(재작성) | AS-IS analysis DB(운영 CUBRID ghlove_analysis) `stat_cntr_mber`·`stat_cntr_mber_loc`·`stat_locgov` 전부 지우고 다시 넣기. 원천 donation `/api/admin/batch/nh/*` + member `/api/admin/members/batch/ci-map`(CI 있는 회원만 = AS-IS INNER JOIN). disabled면 건수만. ★운영에서 켜려면 CUBRID JDBC 드라이버 필요 |
| sendNuri2PresentOrder | admin `Nuri2PresentOrderService` + order `/api/admin/orders/batch/present-order-counts` | 어제 0시 이후 결제완료(배송 전) 품목을 판매자별로 세어 사용(2) 판매자 휴대폰으로 NURI2_NRMSG_DATA에 KR002 알림톡 등록. sender-key는 배포 설정(저장소에 두지 않음) |

- **제거한 TO-BE 발명**: NH 반출 화면 `/batch/nh-export`(메뉴 없음)·NhExportBatchService·NhExportLog·NhExportRow(기부 건별 NH_USER_CNTR) - AS-IS에 없는 구조. 빈 표 `admin.nh_export_log`는 남김(DROP은 사용자 판단).
- **보류(기록만, 행은 등록·적용여부 0)**
  - updateWaitUserCacheBatch(넷퍼넬): AS-IS도 배치 서버 JVM 메모리 캐시만 갱신하고, 읽는 API(getRealTimeWaitUser)는 다른 JVM이며 프론트 호출부도 0 - 효과가 없는 job. TO-BE에 NetFunnel 없음.
  - sendRemittanceExpectedMsgBatch(정산예정 문자): 품목의 정산예정일·정산ID에 의존 - 정산 영역(판매자 셀프서비스 P1~P3 보류) 이식 때 함께.
  - statisticsReportBatch(월간 통계) + 중지 상태 statisticsYearReport* 4: AS-IS 분석 DB에 통계를 쌓고 현황보고 메뉴(6223~6227)가 그걸 읽는다. TO-BE 현황보고는 admin 조회모델(DonationLedger)로 구현돼 있어 "분석 DB vs 조회모델" 결정 필요 → DA 설계([[defer-readmodels-pending-da-design]])와 함께(BIX5와 같은 성격).

### 송장 택배사 = 배송업체 ID 전환 (2026-10-19, 사용자 지시)
- order `OrderService.registerInvoice`: 택배사를 사용 중(USE_FLAG='Y') 배송업체 ID로만 받는다. '-선택-'(0)·옛 코드("CJ" 등)는
  AS-IS 문구 "배송업체를 선택해 주세요"로 거절. `DeliveryCompanyService.active()` 정렬을 AS-IS getActiveDeliveryCompanyListAll(ID 내림차순)로.
- admin 주문 상세 송장 등록 select: 하드코딩 4종(CJ/POST/HANJIN/LOTTE) → "-선택-" + 사용 중 배송업체(AS-IS shipping-ready.jsp와 같은 값).
  등록된 택배사는 업체명으로 표시.
- 데이터: `migration-order-carrier-code-delivery-company-id.sql`(CJ→2, POST→3, HANJIN→4, LOTTE→5) - od_order 4건.
  이제 SMART_DELIVERY_COMPANY 매핑(2→04, 3→01)이 맞아 스마트택배 배치가 조회까지 간다.
- 판매자 출고 화면(AS-IS seller 송장등록)은 판매자 셀프서비스 P1(출고) 보류 범위라 TO-BE에 아직 없다.

### 2단계 - job 이식 5차: AS-IS 중지 상태 고향사랑 고유 job (2026-10-19)
전부 **구현만 하고 중지 유지**(AS-IS BATCH_STATUS=2 그대로). 화면에서 사용으로 바꾸거나 수동 실행(`POST /api/admin/batch/run/{jobMethod}`)하면 동작한다.

| job | 위치 | 비고 |
|---|---|---|
| sendNtsEreceiptOnBatch · OffBatch · BatchOld | donation `DonationBatchJobs` → `DonationBatchService.ntsTargets/recordNts` + `NtsClient.relayAccessToken/sendEreceipt` | AS-IS NgDonationBatchServiceImpl 중계서버 전문(RSA(CI)·RSA(사업자번호)). 주소·IP는 AS-IS 값, 키·ID·비밀번호는 배포 설정(빈 값) |
| cntrTaxTempBatch | donation `DonationBatchService.taxTemp*`·`insertTaxLog/updateTaxLog` | conbCd 43/44, 2024.02 추가 항목 elcrAplcd·elcrAdmNo |
| confirmSunapForDeletedListBatch | donation `deletedSunapTargets/completeSunapForDeleted/markSunapRecheckedUnpaid` | ★미납 재확인 표시가 AS-IS 리터럴 '900' - TO-BE 상태코드 체계에 대응 값이 없어 그대로 둠(지뢰) |
| updateLockForManagerBatch | admin `ManagerLockBatchService` | ISMS UNUSED_MANAGER 일수, 실패횟수 10000으로 잠금 |
| opUserBirthdayStatBatch | member `MemberBatchJobs`(member에 스케줄러 신설·@EnableScheduling) | OP_USER_BIRTHDAY 적재(통계 DB 부분은 AS-IS도 주석). 실측: 1회 34건·재실행 0건(테스트 후 표 비움) |
| expirationPointSendMessageBatch | admin `PointExpirationMailService` + point `GET /api/admin/batch/expiring-points` + member `GET /api/admin/members/batch/mail-targets` | 만료 90일 전 잔여 포인트(TO-BE 적립 원장 = AS-IS G_CNTR 잔여·만료일)를 회원·만료일별로 합쳐 `expiration_point` 템플릿으로 EMS. 템플릿 buyer_send_flag가 AS-IS 실데이터 'N'이라 실제로는 발송 안 됨(AS-IS 동일). 정상·휴면만. TO-BE 휴면은 OP_USER에 그대로 있어 복호화 단계 없음 |

- **행만(AS-IS에 메서드 없음)**: sendNtsEreceiptBatch · sendNtsEreceiptOneBatch · cntrBugaTestBatch - JobServiceImpl에 구현이 없어 레지스트리에 넣지 않음.
- **보류 - 휴면(processInactiveUserBatch · sendMailToInactiveUserBatch)**: AS-IS `setSleepUser`는 개인정보를 OP_USER_SLEEP로 암호화 이관 후 OP_USER/OP_USER_DETAIL을 비우는데,
  ① AS-IS SQL 자체가 깨져 있다(user-mapper.xml `updateUserDetailToSleep` 1603행 `RECEIVE_EMAIL = '1'` 뒤 쉼표 누락 → 실행 시 오류),
  ② TO-BE 휴면은 OP_USER에 정보를 둔 채 상태만 DORMANT라, 그대로 이식하면 휴면 해제(wakeup)가 OP_USER_SLEEP에서 복원하도록 함께 바뀌어야 한다.
  → 별도 과제로 사용자 결정 대기.

## 남은 사항 - 결정 후 처리 (2026-10-19 배치 작업 일단 종료, 사용자 지시)
| # | 항목 | 필요한 결정/조치 |
|---|---|---|
| 1 | 휴면 전환·안내메일(processInactiveUserBatch·sendMailToInactiveUserBatch) | AS-IS SQL 쉼표 누락 + OP_USER_SLEEP 이관 시 wakeup 복원 변경 필요. A 별도 과제(추천)/B 지금 이식 |
| 2 | confirmSunapForDeletedListBatch의 '900' 리터럴 | TO-BE 상태코드 대응값 결정 전엔 켜지 말 것 |
| 3 | 3일 기부내역 없는 지자체 메일 테스트 수신자 | 사용자가 주소 알려주면 `ghlove.batch.no-buga-mail.test-recipients`에 입력 |
| 4 | 농협 연계 3종 운영 활성화 | CUBRID JDBC 드라이버 추가 필요 |
| 5 | 국세청 영수증·누리2·EMS 등 연계 비밀값 | 배포 설정에 주입(저장소에 두지 않음) |
| 6 | 통계 분석DB(statisticsReportBatch·statisticsYearReport* 4)·BIX5 20 | DA 설계(분석DB vs 조회모델) 후 |
| 7 | sendRemittanceExpectedMsgBatch(정산예정 문자) | 정산 영역(판매자 셀프서비스 P1~P3) 이식 때 |
| 8 | updateWaitUserCacheBatch(넷퍼넬) | AS-IS에서도 효과 없음 - 행만 유지, 추가 조치 여부 결정 |
| 9 | 판매자 출고(송장 등록) 화면 | 판매자 셀프서비스 P1 보류 범위 |
| 10 | 빈 표 admin.nh_export_log | DROP 여부 사용자 판단 |

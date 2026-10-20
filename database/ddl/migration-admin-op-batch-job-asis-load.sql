-- 2026-10-15 배치 일괄 이식: admin.op_batch_job을 AS-IS OP_BATCH_JOB 실데이터 78행으로 교체(사용자 결정 - docs/as-is-parity.md 23절(배치 job 이식 원장)).
-- 원본: Desktop/고향사랑e음/1.AS-IS/1. DB/_op_batch_job__202610071247.sql (값 그대로).
-- 기존 TO-BE 전용 행 1000(DesignatedAdminService.closeExpiredProjects)·1001(PointService.runExpirationBatch)은
-- AS-IS 3000000 dsgnBizStatusUpdateBatch·1 expirationPointBatch(중지)로 대체된다. 실행 DB: ghlove_core.
-- BATCH_APPLY_FLAG는 AS-IS 기동 시처럼 전부 '0'으로 시작 - 각 서비스 BatchJobScheduler가 자기 job을 반영하면 '1'이 된다.
BEGIN;
DELETE FROM admin.op_batch_job;
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (3000008,'BIX5 10. 기부자 주소지 기준 모금 순위 TOP 10','bix5CntrAddr','2','','0 45 0 * * ?','1','','1',NULL),
	 (3000007,'BIX5 9. 연령대별 기부 건수','bix5CntrOld','2','','0 40 0 * * ?','1','','1',NULL),
	 (3000006,'BIX5 8. 기부 금액별 건수','bix5CntrAmtCat','2','','0 35 0 * * ?','1','','1',NULL),
	 (3000005,'BIX5 7. 기부 접수창구별 모금액','bix5CntrPath','2','','0 30 0 * * ?','1','','1',NULL),
	 (3000004,'BIX5 6. 특정사업별 모금현황','bix5CntrBiz','2','','0 25 0 * * ?','1','','1',NULL),
	 (3000003,'BIX5 5. 대상별 기부금액','bix5CntrAmtBiz','2','','0 20 0 * * ?','1','','1',NULL),
	 (3000002,'BIX5 4. 전년대비 월별 기부금액','bix5CntrAmtMonth','2','','0 15 0 * * ?','1','','1',NULL),
	 (3000001,'BIX5 1. 기부금액(전년비), 2. 기부횟수(전년비)','bix5CntrAmt','2','','0 10 0 * * ?','1','','1',NULL),
	 (2000120,'답례품 주문내역 알림톡 전송','sendNuri2PresentOrder','2','','0 0 9 * * ?','1','','1',NULL),
	 (3000000,'모금완료  및 기간만료된 특정기부사업 상태변경','dsgnBizStatusUpdateBatch','2','','0 0/30 * * * ?','1',NULL,'1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (2000119,'넷퍼넬 Api Cache 배치','updateWaitUserCacheBatch','2','','0 */5 * * * ?','1','','1',NULL),
	 (1000005,'배송 완료 처리(스마트택배)','autoSmartDeliveryShippingCompleteBatch','2','99999999999','0 30 23 * * ?','1','','1',NULL),
	 (2000118,'연간통계답례품-statisticsYearReportGoodsBatch()','statisticsYearReportGoodsBatch','2','','0 56 6 28 2 ?','2','','1',NULL),
	 (2000117,'연간통계-statisticsYearReportAgeMonthBatch()','statisticsYearReportAgeMonthBatch','2','','0 54 6 28 2 ?','2','','1',NULL),
	 (2000116,'연간통계거주지-statisticsYearReportPsintBatch()','statisticsYearReportPsintBatch','2','','0 52 6 28 2 ?','2','','1',NULL),
	 (2000115,'연간통계-statisticsYearReportBatch()','statisticsYearReportBatch','2','','0 50 6 28 2 ?','2','','1',NULL),
	 (2000114,'매일 총 기부금 생성 배치','dailyGramtInsertBatch','2','','0 0 3 * * ?','1','','1',NULL),
	 (2000113,'오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트','allDayOffItemUpdateBatch','2','','0 30 3 * * ?','1','','1',NULL),
	 (36,'마감일자 공개여부 처리 배치','itemDisplayContorlBatch','2',NULL,'0 10 2 * * ?','1',NULL,'1',NULL),
	 (35,'세외 수납결과 확인하여 수납결과 반영(일정산)','dayCompleteSunapCheckBatch','2','','0 40 23 * * ?','1','','1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (34,'세외 수납결과 확인하여 수납결과 반영','fiveMinuteSunapCheckBatch','2','','0 0/5 * * * ?','1','','1',NULL),
	 (2000112,'부과 등록 테스트','cntrBugaTestBatch','2','','* * 5 * * ?','2','','0',NULL),
	 (2000111,'통계 현황보고 데이터 인입 처리','statisticsReportBatch','2','','0 0 7 1 * ?','1','','1',NULL),
	 (2000110,'사용자 연령데이터 통계 처리','opUserBirthdayStatBatch','2','','0 0 3 * * ?','2','','1',NULL),
	 (2000109,'차세대 수납결과 배치->오프라인 기탁서 답례품 주문건 처리','cntrSunapBatch','2','','0 0/5 * * * ?','1','','1',NULL),
	 (2000108,'2023기부금영수증처리','cntrTaxTempBatch','2','','0 0/4 * * * ?','2','','1',NULL),
	 (2000107,'농협 연계','locForNhBatch','2','','0 35 4 * * ?','1','','1',NULL),
	 (2000106,'농협 연계','userCntrLocForNhBatch','2','','0 30 4 * * ?','1','','1',NULL),
	 (2000105,'3일 기부현황 내역 없는 지자체','getNoBugaLocgovListBatch','2','','0 0 9 ? * MON,THU','1','','1',NULL),
	 (2000103,'보고용대시보드 ','confirmdonationForReportBatch','2','99999999999','0 0 18 * * ?','1','','1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (2000002,'전날 미수납건 재수납 확인','confirmSunapForDeletedListBatch','2','','0 0/15 1-7 * * ?','2','','1',NULL),
	 (2000001,'농협 연계','userCntrForNhBatch','2','','0 25 4 * * ?','1','','1',NULL),
	 (24,'사용되지 않는 사용자 잠금처리','updateLockForManagerBatch','2','','0 35 1 * * ?','2','','1',NULL),
	 (22,'포인트 만료 안내 메시지 발송','expirationPointMessageBatch','2',NULL,'0 0 13 * * ?','2',NULL,'1',NULL),
	 (21,'쿠폰 만료 안내 메시지 발송','expirationCouponSendMessageBatch','2',NULL,'0 0 13 * * ?','2',NULL,'1',NULL),
	 (20,'구매확정 요청 UMS 배치','autoConfirmPurchaseRequestUmsBatch','2','','0 30 12 * * ?','2',NULL,'1',NULL),
	 (19,'구매확정 UMS 배치','autoConfirmPurchaseUmsBatch','2','','0 0 12 * * ?','2',NULL,'1',NULL),
	 (18,'캠페인용 발송통계 배치','updateCampaignSentBatch','2',NULL,'0 30 4 * * ?','2',NULL,'1',NULL),
	 (17,'캠페인용 예약발송 배치','sendCampaignMessageBatch','2','','0 0 0/1 * * ?','2',NULL,'1',NULL),
	 (16,'캠페인용 유저 업데이트 배치','updateUserCampaignBatch','2','','0/1 0 0 * * ?','2',NULL,'1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (15,'카카오 알림톡 업데이트 배치','updateKakaoAlimTalkBatch','2',NULL,'10 0 0 * * ?','2',NULL,'1',NULL),
	 (14,'입금지연 주문 취소 배치','cancelWaitingDepositOrderBatch','2','99999999999','0 0 * * * ?','2',NULL,'1',NULL),
	 (13,'회원 등급 산정','userLevelBatch','2','','1 0 0 * * ?','2',NULL,'1',2018),
	 (12,'자동 완성 데이터 처리','autoCompleteKeywordBatch','1','30000','0 30 1 * * ?','2','','1',NULL),
	 (11,'정기발행쿠폰','couponRegularBatch','2','','0 0 0 1 * ?','2','','1',NULL),
	 (10,'구매 확정 처리','autoConfirmPurchaseBatch','2','99999999999','0 20 1 * * ?','1','','1',NULL),
	 (9,'상품 옵션 품절 배치','updateItemOptionSoldoutBatch','1','30000',NULL,'2','','1',NULL),
	 (8,'계정 휴면처리','processInactiveUserBatch','2','99999999999','0 0 03 * * ?','2','','1',NULL),
	 (7,'휴면안내메일 발송','sendMailToInactiveUserBatch','2','99999999999','0 10 0-8 * * ?','2','','1',NULL),
	 (6,'상품 랭킹 산정 - 1~4 Category','itemRankingType3Batch','2','99999999999','0 0 3 * * ?','2','','1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (5,'상품 랭킹 산정 - Category Group','itemRankingType2Batch','2','99999999999','0 0 2 * * ?','2','','1',NULL),
	 (4,'상품 랭킹 산정 - TOP 100','itemRankingType1Batch','2','99999999999','0 0 1 * * ?','2','','1',NULL),
	 (3,'판매자 신규주문 SMS','sendOrderSmsToSellerBatch','2','99999999999','0 0 8-18 * * ?','2','','1',NULL),
	 (2,'기부 포인트 만료 안내 메일발송','expirationPointSendMessageBatch','2','99999999999','0 30 2 * * ?','2',NULL,'1',NULL),
	 (1,'기부 포인트 만료 처리','expirationPointBatch','2','','0 30 03 * * ?','2',NULL,'1',NULL),
	 (26,'기부 데이터 미결재분(서울시) 삭제','deleteCntrDataBatch','2','','0 3 0 * * ?','1','','1',NULL),
	 (27,'국세청 전자기부영수증 데이터 처리','sendNtsEreceiptBatch','2','99999999999','0 0 20 * * ?','2','','0',NULL),
	 (29,'기부 데이터 미결재분(지방세외 현세대) 삭제','deleteNotSunapStndBatch','2','','0 5 0 * * ?','1','','1',NULL),
	 (30,'국세청 전자기부영수증 데이터 처리(건당)','sendNtsEreceiptOneBatch','2','','0 0/10 * * * ?','2','','0',NULL),
	 (31,'국세청 전자기부영수증 데이터(online) 처리','sendNtsEreceiptOnBatch','2','','0 0/10 * * * ?','2','','1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (32,'국세청 전자기부영수증 데이터(offline) 처리','sendNtsEreceiptOffBatch','2','','0 0/10 0-8 * * ?','2','','1',NULL),
	 (33,'국세청 이전데이터 처리','sendNtsEreceiptBatchOld','2','','0 0/10 1-9 * * ?','2','','1',NULL),
	 (1000001,'배송 완료 처리','autoShippingCompleteBatch','2','99999999999','0 10 1 * * ?','1','','1',NULL),
	 (1000002,'상품 NEW 삭제 배치','deleteItemNewBatch','2','','0 10 0 * * ?','1','','1',NULL),
	 (1000003,'장바구니 삭제 배치','deleteCartBatch','2','','0 10 0 * * ?','1','','1',NULL),
	 (1000004,'정산예정내역 확인 문자 전송','sendRemittanceExpectedMsgBatch','2','','0 0 9 1 * ?','1','','1',NULL),
	 (3000014,'BIX5 14. 답례품 등록 현황-카테고리 기준','bix5GiftCat','2','','0 53 14 * * ?','1','','1',NULL),
	 (3000015,'BIX5 15. 답례품 등록 현황(포인트 구간 기준)','bix5GiftPoint','2','','0 7 15 * * ?','1','','1',NULL),
	 (3000016,'BIX5 16. 카테고리별 판매수량','bix5GiftCatSell','2','','0 17 15 * * ?','1','','1',NULL),
	 (3000017,'BIX5 17. 답례품 제공자 발송 소요 시간 TOP 5','bix5GiftDeli','2','','0 21 15 * * ?','1','','1',NULL);
INSERT INTO admin.op_batch_job ("batch_job_id","job_name","job_method","trigger_type","trigger_repeat_seconds","trigger_cron_expression","batch_status","batch_excute_date","batch_apply_flag","ordering") VALUES
	 (3000018,'BIX5 18. 총 잔여 포인트(소진율)','bix5PointUse','2','','0 23 15 * * ?','1','','1',NULL),
	 (3000019,'BIX5 19. 연간 발생 포인트','bix5PointUseYear','2','','0 25 15 * * ?','1','','1',NULL),
	 (3000020,'BIX5 20. 포인트 현황','bix5PointYear','2','','0 44 15 * * ?','1','','1',NULL),
	 (3000011,'BIX5 11. 판매 포인트(전년비)','bix5GiftUsePoint','2','','0 10 16 * * ?','1','','1',NULL),
	 (3000012,'BIX5 12. 답례품 총 개수','bix5GiftCnt','2','','0 23 16 * * ?','1','','1',NULL),
	 (3000013,'BIX5 13. 평균 판매 포인트 비교','bix5GiftUsePointCpr','2','','0 31 16 * * ?','1','','1',NULL);
UPDATE admin.op_batch_job SET batch_apply_flag = '0';
-- 화면 신규 등록이 기존 ID(2000114 등)와 부딪히지 않게 시퀀스를 최대값으로.
SELECT setval('admin.op_batch_job_batch_job_id_seq', (SELECT max(batch_job_id) FROM admin.op_batch_job));
COMMIT;

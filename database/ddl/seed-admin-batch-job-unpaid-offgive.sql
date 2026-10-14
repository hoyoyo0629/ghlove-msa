-- 배치 작업 등록(OP_BATCH_JOB) - 미납 기부 삭제 2종 + 오프라인 미수납 답례품 정리 (2026-10-13)
-- 대상 DB: ghlove_core (admin 스키마)
--
-- 이번에 이식한 AS-IS ghlove-batch 정기배치 3개를 Batch Job 화면(1403)과 배치 실행로그(7209)에 보이게 등록한다.
-- 값은 AS-IS OP_BATCH_JOB 실데이터(_op_batch_job__202610071247.sql) 그대로다 - ID·작업명·job_method·cron·상태.
--   26       기부 데이터 미결재분(서울시) 삭제                           deleteCntrDataBatch       0 3 0 * * ?   (donation NgDonationBatchScheduler)
--   29       기부 데이터 미결재분(지방세외 현세대) 삭제                  deleteNotSunapStndBatch   0 5 0 * * ?   (donation NgDonationBatchScheduler)
--   2000113  오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트  allDayOffItemUpdateBatch  0 30 3 * * ?  (order OffgiveOrderBatchScheduler)
-- 7209는 BATCH_TYPE LIKE '%' || JOB_METHOD || '%'로 작업명을 붙이므로, 각 스케줄러가 보고하는 BATCH_TYPE
-- ("클래스.메서드()")이 위 job_method를 품게 해 두었다.
--
-- 끝에서 batch_job_id 시퀀스를 현재 최대값으로 맞춘다 - 기존 시드가 1000·1001을 직접 넣으면서 시퀀스는 1000에
-- 머물러 있어(다음 값 1001 = 이미 있는 ID) 화면에서 작업을 새로 만들면 PK 충돌이 날 상태였다.
--
-- 재실행 안전: 같은 JOB_METHOD가 이미 있으면 넣지 않는다.
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres psql -U postgres -d ghlove_core -f /dev/stdin < seed-admin-batch-job-unpaid-offgive.sql

INSERT INTO admin.op_batch_job
    (batch_job_id, job_name, job_method, trigger_type, trigger_repeat_seconds,
     trigger_cron_expression, batch_status, batch_excute_date, batch_apply_flag, ordering)
SELECT 26, '기부 데이터 미결재분(서울시) 삭제', 'deleteCntrDataBatch',
       '2', '', '0 3 0 * * ?', '1', '', '1', NULL
 WHERE NOT EXISTS (SELECT 1 FROM admin.op_batch_job WHERE job_method = 'deleteCntrDataBatch');

INSERT INTO admin.op_batch_job
    (batch_job_id, job_name, job_method, trigger_type, trigger_repeat_seconds,
     trigger_cron_expression, batch_status, batch_excute_date, batch_apply_flag, ordering)
SELECT 29, '기부 데이터 미결재분(지방세외 현세대) 삭제', 'deleteNotSunapStndBatch',
       '2', '', '0 5 0 * * ?', '1', '', '1', NULL
 WHERE NOT EXISTS (SELECT 1 FROM admin.op_batch_job WHERE job_method = 'deleteNotSunapStndBatch');

INSERT INTO admin.op_batch_job
    (batch_job_id, job_name, job_method, trigger_type, trigger_repeat_seconds,
     trigger_cron_expression, batch_status, batch_excute_date, batch_apply_flag, ordering)
SELECT 2000113, '오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트', 'allDayOffItemUpdateBatch',
       '2', '', '0 30 3 * * ?', '1', '', '1', NULL
 WHERE NOT EXISTS (SELECT 1 FROM admin.op_batch_job WHERE job_method = 'allDayOffItemUpdateBatch');

SELECT setval('admin.op_batch_job_batch_job_id_seq', (SELECT max(batch_job_id) FROM admin.op_batch_job));

SELECT batch_job_id, job_name, job_method, trigger_cron_expression, batch_apply_flag
  FROM admin.op_batch_job
 ORDER BY ordering NULLS LAST, batch_job_id;

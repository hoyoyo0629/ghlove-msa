-- 배치 작업 등록(OP_BATCH_JOB) - 실제로 도는 정기배치를 Batch Job 화면(1403)에 등록
--
-- "배치 실행로그 조회"(메뉴 7209)는 실행로그를
--   OP_BATCH_EXECUTION.BATCH_TYPE LIKE '%' || OP_BATCH_JOB.JOB_METHOD || '%'
-- 로 조인해 **작업명**을 가져온다(AS-IS 쿼리 그대로). 즉 OP_BATCH_JOB에 등록되지 않은 배치는
-- 실행로그를 남겨도 7209 목록에 나오지 않는다.
--
-- TO-BE에는 지정기부 자동전환 1건만 등록돼 있었는데, 실제로 도는 @Scheduled 배치는 3개다.
-- 나머지 2개를 같은 형식으로 등록한다.
--   TRIGGER_TYPE 2 = 크론, BATCH_STATUS 1 = 실행중, BATCH_APPLY_FLAG 0 = 적용전
--   (기존 1000번 행과 동일한 값 체계를 따랐다)
--
-- 쿠폰 자동발급은 쿠폰 기능이 OFF(ghlove.coupon.enabled=false)라 현재 빈이 등록되지 않아
-- 실제로는 돌지 않는다 - 등록만 해 두고 기능을 켜면 그대로 로그가 쌓인다.
--
-- 재실행 안전: 같은 JOB_METHOD가 이미 있으면 넣지 않는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < seed-admin-batch-job-registrations.sql

INSERT INTO admin.op_batch_job
    (batch_job_id, job_name, job_method, trigger_type, trigger_repeat_seconds,
     trigger_cron_expression, batch_status, batch_excute_date, batch_apply_flag, ordering)
SELECT 1001, '기부포인트 유효기간 소멸 처리', 'PointService.runExpirationBatch',
       '2', NULL, '0 30 1 * * *', '1', NULL, '0', 2
 WHERE NOT EXISTS (SELECT 1 FROM admin.op_batch_job
                    WHERE job_method = 'PointService.runExpirationBatch');

INSERT INTO admin.op_batch_job
    (batch_job_id, job_name, job_method, trigger_type, trigger_repeat_seconds,
     trigger_cron_expression, batch_status, batch_excute_date, batch_apply_flag, ordering)
SELECT 1002, '쿠폰 자동발급(가입·생일·정기)', 'CouponBatchScheduler.runDailyCouponBatch',
       '2', NULL, '0 0 1 * * *', '1', NULL, '0', 3
 WHERE NOT EXISTS (SELECT 1 FROM admin.op_batch_job
                    WHERE job_method = 'CouponBatchScheduler.runDailyCouponBatch');

SELECT batch_job_id, job_name, job_method, trigger_cron_expression
  FROM admin.op_batch_job
 ORDER BY ordering, batch_job_id;

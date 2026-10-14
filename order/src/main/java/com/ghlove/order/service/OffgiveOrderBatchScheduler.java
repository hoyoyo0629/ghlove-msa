package com.ghlove.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * "오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트" 정기배치 - AS-IS ghlove-batch
 * {@code JobServiceImpl.allDayOffItemUpdateBatch}(매일 03:30, AS-IS OP_BATCH_JOB cron 그대로).
 * 정리 로직은 {@link OffgiveOrderService#cancelUnpaidDeleted}에 있고, 여기서는 프록시를 타는 호출로
 * 트랜잭션을 보존하면서 실행결과를 배치 실행로그(admin 7209)에 보고한다 - {@link CouponBatchScheduler}와
 * 같은 "스케줄러는 별도 빈" 패턴이다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OffgiveOrderBatchScheduler {

    /**
     * 7209 화면이 작업명을 찾는 키 - 화면 쿼리가 {@code BATCH_TYPE LIKE '%' || JOB_METHOD || '%'}라
     * op_batch_job.job_method(AS-IS 값 그대로 allDayOffItemUpdateBatch)를 품어야 한다.
     */
    private static final String BATCH_TYPE = "OffgiveOrderBatchScheduler.allDayOffItemUpdateBatch()";

    private final OffgiveOrderService offgiveOrderService;
    private final BatchExecutionReporter batchExecutionReporter;

    @Scheduled(cron = "0 30 3 * * *")
    public void allDayOffItemUpdateBatch() {
        batchExecutionReporter.runAndReport(BATCH_TYPE, () -> {
            log.info(">>->> process allDayOffItemUpdateBatch() >>->>");
            int cancelled = offgiveOrderService.cancelUnpaidDeleted(LocalDate.now());
            return "주문취소 " + cancelled + "건";
        });
    }
}

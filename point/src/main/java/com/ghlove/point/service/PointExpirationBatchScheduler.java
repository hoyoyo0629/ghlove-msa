package com.ghlove.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 기부포인트 소멸 처리 정기배치 트리거 (SFR-004 "소멸 처리", 매일 01:30).
 *
 * <p>소멸 로직 자체는 {@link PointService#runExpirationBatch()}에 있고, 여기서는
 * <b>프록시를 타는 호출</b>로 트랜잭션을 보존하면서 실행결과를 배치 실행로그
 * (admin 메뉴 7209)에 보고한다 - 같은 빈 안에서 self-invocation하면 @Transactional이 무효다.
 *
 * <p>7209 목록에 작업명이 붙으려면 {@code admin.op_batch_job}에 같은 {@code job_method}가
 * 등록돼 있어야 한다({@code seed-admin-batch-job-registrations.sql}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointExpirationBatchScheduler {

    /** 7209 화면이 작업명을 찾는 키 - op_batch_job.job_method와 같아야 한다. */
    private static final String BATCH_TYPE = "PointService.runExpirationBatch";

    private final PointService pointService;
    private final BatchExecutionReporter batchExecutionReporter;

    @Scheduled(cron = "0 30 1 * * *")
    public void runDailyExpirationBatch() {
        batchExecutionReporter.runAndReport(BATCH_TYPE, () -> {
            int expired = pointService.runExpirationBatch();
            log.info("기부포인트 소멸 처리 {}건", expired);
            return "소멸 처리 " + expired + "건";
        });
    }
}

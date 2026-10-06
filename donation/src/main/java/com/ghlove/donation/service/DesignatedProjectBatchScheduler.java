package com.ghlove.donation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지정기부 사업 자동전환 정기배치 트리거.
 *
 * <p>{@code op_batch_job}에 등록된 작업({@code DesignatedAdminService.closeExpiredProjects},
 * 매일 01:00)의 실제 실행 지점이다. 전환 로직 자체는 {@link DesignatedAdminService}에 있고,
 * 여기서는 <b>프록시를 타는 호출</b>로 트랜잭션을 보존하면서 실행결과를 배치 실행로그
 * (admin 메뉴 7209)에 보고한다 - 같은 빈 안에서 self-invocation하면 @Transactional이 무효가 된다.
 *
 * <p>order의 {@code CouponBatchScheduler}와 같은 "스케줄러는 별도 빈" 패턴이다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DesignatedProjectBatchScheduler {

    /** 7209 화면이 작업명을 찾는 키 - op_batch_job.job_method와 같아야 한다. */
    private static final String BATCH_TYPE = "DesignatedAdminService.closeExpiredProjects";

    private final DesignatedAdminService designatedAdminService;
    private final BatchExecutionReporter batchExecutionReporter;

    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyCloseExpiredProjects() {
        batchExecutionReporter.runAndReport(BATCH_TYPE, () -> {
            int closed = designatedAdminService.closeExpiredProjects();
            log.info("지정기부 기간종료 자동전환 {}건", closed);
            return "자동 종료 " + closed + "건";
        });
    }
}

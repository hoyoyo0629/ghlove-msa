package com.ghlove.donation.service;

import com.ghlove.donation.repository.UnpaidDonationBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 미납 기부 삭제 정기배치 - AS-IS ghlove-batch {@code JobServiceImpl.deleteCntrDataBatch}(매일 00:03,
 * "기부 데이터 미결재분(서울시) 삭제")와 {@code deleteNotSunapStndBatch}(매일 00:05, "기부 데이터
 * 미결재분(지방세외 현세대) 삭제"). 시각은 AS-IS OP_BATCH_JOB의 cron 그대로다.
 *
 * <p>op_batch_job에 등록된 작업(job_method 키)의 실제 실행 지점이고, 실행결과를 배치 실행로그(admin 7209)에
 * 보고한다 - {@link DesignatedProjectBatchScheduler}와 같은 "스케줄러는 별도 빈" 패턴이다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NgDonationBatchScheduler {

    /**
     * 7209 화면이 작업명을 찾는 키 - 화면 쿼리가 {@code BATCH_TYPE LIKE '%' || JOB_METHOD || '%'}라
     * op_batch_job.job_method(AS-IS 값 그대로 deleteCntrDataBatch·deleteNotSunapStndBatch)를 품어야 한다.
     * 모양은 AS-IS BatchExecutionAspect가 남기던 {@code 클래스.메서드()}다.
     */
    static final String BATCH_DELETE_SEOUL = "NgDonationBatchScheduler.deleteCntrDataBatch()";
    static final String BATCH_DELETE_NOT_SUNAP = "NgDonationBatchScheduler.deleteNotSunapStndBatch()";

    private final UnpaidDonationBatchRepository unpaidDonationBatchRepository;
    private final BatchExecutionReporter batchExecutionReporter;

    /** AS-IS deleteCntrData - "서울 미결제분 삭제". */
    @Scheduled(cron = "0 3 0 * * *")
    public void deleteCntrDataBatch() {
        batchExecutionReporter.runAndReport(BATCH_DELETE_SEOUL, () -> {
            log.info("========= deleteCntrData {}", " 서울 미결제분 삭제");
            int deleted = unpaidDonationBatchRepository.deleteSeoulUnpaid();
            return "삭제 " + deleted + "건";
        });
    }

    /** AS-IS deleteNotSunapStndBatch - "지방세외(현세대) 미납건 데이터 삭제 처리". */
    @Scheduled(cron = "0 5 0 * * *")
    public void deleteNotSunapStndBatch() {
        batchExecutionReporter.runAndReport(BATCH_DELETE_NOT_SUNAP, () -> {
            int deleted = unpaidDonationBatchRepository.deleteNotSunapStnd();
            log.info("========= deleteNotSunapStndBatch 지방세외(현세대) 미납건 삭제 {}건", deleted);
            return "삭제 " + deleted + "건";
        });
    }
}

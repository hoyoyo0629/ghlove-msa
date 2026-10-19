package com.ghlove.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * point 소유 배치 - 키는 AS-IS OP_BATCH_JOB.JOB_METHOD, 실행 여부·시각은 관리 화면 설정({@link BatchJobScheduler}).
 * 본문은 프록시를 타는 {@link PointService}를 불러 트랜잭션을 보존한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointBatchJobs implements BatchJobSource {

    private final PointService pointService;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS id 1 "기부 포인트 만료 처리"(pointService.expirationPoint) - AS-IS에서 중지 상태다(2026-10-15 사용자 결정으로 같게 중지).
        jobs.put("expirationPointBatch", () -> {
            int expired = pointService.runExpirationBatch();
            log.info("기부포인트 소멸 처리 {}건", expired);
            return "소멸 처리 " + expired + "건";
        });
        return jobs;
    }
}

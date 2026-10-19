package com.ghlove.gift.web;

import com.ghlove.gift.service.BatchJobScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** 배치 수동 실행 - AS-IS ghlove-batch {@code ManualBatchController GET /batch/run/{jobName}}. 내부 전용(/api/admin/** 시크릿). */
@RestController
@RequiredArgsConstructor
public class BatchRunApiController {

    private final BatchJobScheduler batchJobScheduler;

    @PostMapping("/api/admin/batch/run/{jobMethod}")
    public String run(@PathVariable String jobMethod) {
        try {
            return "Job triggered manually : " + jobMethod + " - " + batchJobScheduler.runNow(jobMethod);
        } catch (RuntimeException e) {
            return "Error triggering job : " + e.getMessage();
        }
    }
}

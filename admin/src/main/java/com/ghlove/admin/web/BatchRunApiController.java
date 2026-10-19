package com.ghlove.admin.web;

import com.ghlove.admin.service.BatchJobScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** 배치 수동 실행 - AS-IS ghlove-batch {@code ManualBatchController GET /batch/run/{jobName}}. 내부 전용(X-Internal-Secret). */
@RestController
@RequiredArgsConstructor
public class BatchRunApiController {

    private final BatchJobScheduler batchJobScheduler;

    @Value("${ghlove.internal.admin-secret}")
    private String adminSecret;

    @PostMapping("/api/admin/batch/run/{jobMethod}")
    public String run(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                      @PathVariable String jobMethod) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return "Job triggered manually : " + jobMethod + " - " + batchJobScheduler.runNow(jobMethod);
        } catch (RuntimeException e) {
            return "Error triggering job : " + e.getMessage();
        }
    }
}

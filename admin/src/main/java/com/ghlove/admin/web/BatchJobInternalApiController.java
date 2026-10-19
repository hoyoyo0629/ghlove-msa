package com.ghlove.admin.web;

import com.ghlove.admin.domain.BatchJob;
import com.ghlove.admin.repository.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 배치 스케줄 설정 조회(내부 전용) - AS-IS {@code SchedulerServiceImpl.invokeJobChecker}가 15초마다
 * {@code OP_BATCH_JOB}을 읽어 Quartz에 반영하던 것을, MSA에서는 각 서비스의 {@code BatchJobScheduler}가
 * 이 API로 읽어 자기 job_method만 반영한다. 반영을 마치면 {@code BATCH_APPLY_FLAG='1'}(AS-IS updateBatchApplyFlagInBatchJob).
 * 관리 화면(1403)에서 저장하면 '0'으로 돌아가 다음 조회 때 다시 반영된다.
 */
@RestController
@RequestMapping("/api/admin/batch-jobs")
@RequiredArgsConstructor
public class BatchJobInternalApiController {

    private final BatchJobRepository batchJobRepository;

    @Value("${ghlove.internal.admin-secret}")
    private String adminSecret;

    public record BatchJobSetting(Integer batchJobId, String jobMethod, String triggerType,
                                  String triggerRepeatSeconds, String triggerCronExpression,
                                  String batchStatus, String batchApplyFlag) {
    }

    @GetMapping
    public List<BatchJobSetting> list(@RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        checkSecret(secret);
        return batchJobRepository.findAllByOrderByOrderingAscBatchJobIdAsc().stream()
                .map(j -> new BatchJobSetting(j.getBatchJobId(), j.getJobMethod(), j.getTriggerType(),
                        j.getTriggerRepeatSeconds(), j.getTriggerCronExpression(), j.getBatchStatus(),
                        j.getBatchApplyFlag()))
                .toList();
    }

    @PostMapping("/{batchJobId}/applied")
    @Transactional
    public void applied(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                        @PathVariable Integer batchJobId) {
        checkSecret(secret);
        batchJobRepository.findById(batchJobId).ifPresent((BatchJob j) -> {
            j.setBatchApplyFlag("1");
            batchJobRepository.save(j);
        });
    }

    private void checkSecret(String secret) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

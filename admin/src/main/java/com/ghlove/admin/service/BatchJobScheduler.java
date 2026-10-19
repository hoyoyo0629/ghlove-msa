package com.ghlove.admin.service;

import com.ghlove.admin.domain.BatchJob;
import com.ghlove.admin.repository.BatchJobRepository;
import com.ghlove.admin.repository.BatchLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

/**
 * admin 소유 배치를 관리 화면(1403, op_batch_job) 설정대로 돌린다 - AS-IS ghlove-batch
 * {@code SchedulerServiceImpl.invokeJobChecker} 대응. 다른 서비스의 {@code BatchJobScheduler}와 규칙이 같고,
 * admin은 표를 직접 가지므로 REST 대신 리포지토리로 읽고 쓴다.
 * (BATCH_STATUS 1=실행/2=중지, TRIGGER_TYPE 1=심플(값은 밀리초, 고정주기)/2=크론, 반영 후 BATCH_APPLY_FLAG='1')
 */
@Component
@Slf4j
public class BatchJobScheduler {

    private static final String STATUS_RUN = "1";
    private static final String TRIGGER_SIMPLE = "1";
    private static final String TRIGGER_CRON = "2";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");

    private final BatchJobRepository batchJobRepository;
    private final BatchLogRepository batchLogRepository;
    private final TransactionTemplate tx;
    private final Map<String, Supplier<String>> jobs = new HashMap<>();
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    private final Map<String, Active> scheduled = new HashMap<>();

    private record Active(String settingKey, ScheduledFuture<?> future) {
    }

    public BatchJobScheduler(BatchJobRepository batchJobRepository, BatchLogRepository batchLogRepository,
                             TransactionTemplate tx, List<BatchJobSource> sources) {
        this.batchJobRepository = batchJobRepository;
        this.batchLogRepository = batchLogRepository;
        this.tx = tx;
        sources.forEach(s -> jobs.putAll(s.batchJobs()));
        taskScheduler.setPoolSize(4);
        taskScheduler.setThreadNamePrefix("batch-job-");
        taskScheduler.initialize();
    }

    /** 수동 실행(AS-IS ManualBatchController /batch/run/{jobName}). */
    public String runNow(String jobMethod) {
        Supplier<String> job = jobs.get(jobMethod);
        if (job == null) {
            throw new IllegalArgumentException("이 서비스에 없는 배치입니다: " + jobMethod);
        }
        return execute(jobMethod, job);
    }

    /** AS-IS JobChecker 주기(15초). */
    @Scheduled(fixedDelay = 15000, initialDelay = 5000)
    public synchronized void sync() {
        List<BatchJob> settings;
        try {
            settings = batchJobRepository.findAllByOrderByOrderingAscBatchJobIdAsc();
        } catch (RuntimeException e) {
            log.debug("배치 설정 조회 실패 - 기존 스케줄 유지: {}", e.getMessage());
            return;
        }
        Map<String, BatchJob> mine = new HashMap<>();
        for (BatchJob s : settings) {
            if (s.getJobMethod() != null && jobs.containsKey(s.getJobMethod())) {
                mine.put(s.getJobMethod(), s);
            }
        }
        scheduled.keySet().stream().filter(m -> !mine.containsKey(m)).toList().forEach(this::cancel);

        for (BatchJob s : mine.values()) {
            String key = s.getBatchStatus() + "|" + s.getTriggerType() + "|" + s.getTriggerRepeatSeconds() + "|"
                    + s.getTriggerCronExpression();
            Active current = scheduled.get(s.getJobMethod());
            if (current == null || !current.settingKey().equals(key)) {
                cancel(s.getJobMethod());
                ScheduledFuture<?> future = STATUS_RUN.equals(s.getBatchStatus()) ? schedule(s) : null;
                scheduled.put(s.getJobMethod(), new Active(key, future));
                log.info("배치 스케줄 반영 - {} 상태={} 트리거={} {}", s.getJobMethod(), s.getBatchStatus(),
                        s.getTriggerType(), TRIGGER_CRON.equals(s.getTriggerType())
                                ? s.getTriggerCronExpression() : s.getTriggerRepeatSeconds());
            }
            if (!"1".equals(s.getBatchApplyFlag())) {
                Integer id = s.getBatchJobId();
                tx.executeWithoutResult(t -> batchJobRepository.findById(id).ifPresent(j -> {
                    j.setBatchApplyFlag("1");
                    batchJobRepository.save(j);
                }));
            }
        }
    }

    private ScheduledFuture<?> schedule(BatchJob s) {
        String jobMethod = s.getJobMethod();
        Runnable task = () -> {
            try {
                execute(jobMethod, jobs.get(jobMethod));
            } catch (RuntimeException e) {
                log.error("[배치] {} 실행 오류", jobMethod, e);
            }
        };
        try {
            if (TRIGGER_CRON.equals(s.getTriggerType())) {
                return taskScheduler.schedule(task,
                        new CronTrigger(Objects.requireNonNull(s.getTriggerCronExpression()).trim()));
            }
            if (TRIGGER_SIMPLE.equals(s.getTriggerType())) {
                long interval;
                try {
                    interval = Long.parseLong(s.getTriggerRepeatSeconds().trim());
                } catch (RuntimeException e) {
                    interval = 99999999999L;
                }
                return taskScheduler.scheduleAtFixedRate(task, Duration.ofMillis(interval));
            }
        } catch (RuntimeException e) {
            log.error("[배치] {} 스케줄 설정 오류(트리거 {} / {})", jobMethod, s.getTriggerType(),
                    s.getTriggerCronExpression(), e);
        }
        return null;
    }

    /** AS-IS BatchExecutionAspect - 이름이 Batch로 끝나는 job만 {@code JobServiceImpl.메서드()}로 실행로그에 남긴다. */
    private String execute(String jobMethod, Supplier<String> job) {
        if (!jobMethod.endsWith("Batch")) {
            return job.get();
        }
        String batchType = "JobServiceImpl." + jobMethod + "()";
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            String result = job.get();
            record(batchType, startedAt, "1", result);
            return result;
        } catch (RuntimeException e) {
            record(batchType, startedAt, "0", e.toString());
            throw e;
        }
    }

    private void record(String batchType, LocalDateTime startedAt, String result, String message) {
        try {
            tx.executeWithoutResult(t -> batchLogRepository.recordExecution(batchType, DAY.format(startedAt),
                    TIME.format(startedAt), TIME.format(LocalDateTime.now()), result, message));
        } catch (RuntimeException e) {
            log.warn("배치 실행로그 기록 실패 - batchType={}", batchType, e);
        }
    }

    private void cancel(String jobMethod) {
        Active current = scheduled.remove(jobMethod);
        if (current != null && current.future() != null) {
            current.future().cancel(false);
        }
    }
}

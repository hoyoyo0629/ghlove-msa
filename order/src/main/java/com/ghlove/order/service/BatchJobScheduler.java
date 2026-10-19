package com.ghlove.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

/**
 * 관리 화면(1403, {@code admin.op_batch_job}) 설정대로 배치를 돌린다 - AS-IS ghlove-batch
 * {@code SchedulerServiceImpl.invokeJobChecker}(15초마다 OP_BATCH_JOB을 읽어 Quartz 등록/재등록/중지) 대응.
 *
 * <p>AS-IS 규칙 그대로: {@code BATCH_STATUS} 1=실행 / 2=중지, {@code TRIGGER_TYPE} 1=심플 / 2=크론.
 * 심플의 {@code TRIGGER_REPEAT_SECONDS}는 이름과 달리 <b>밀리초</b>다(AS-IS가 SimpleTriggerFactoryBean.setRepeatInterval에
 * 그대로 넘긴다 - 30000 = 30초). 같은 job은 겹쳐 돌지 않는다(AS-IS concurrent=false).
 *
 * <p>MSA에서는 job마다 소유 서비스가 달라, 각 서비스가 {@link BatchJobSource}로 가진 job_method만 반영한다.
 * 반영한 행은 admin에 알려 {@code BATCH_APPLY_FLAG='1'}로 바꾼다(화면에서 저장하면 '0'으로 돌아간다).
 * admin에 닿지 못하면 기존 스케줄을 그대로 유지한다. 실행 결과는 {@link BatchExecutionReporter}로 7209에 남는다.
 */
@Component
@Slf4j
public class BatchJobScheduler {

    private static final String STATUS_RUN = "1";
    private static final String TRIGGER_SIMPLE = "1";
    private static final String TRIGGER_CRON = "2";

    private final RestClient restClient;
    private final BatchExecutionReporter reporter;
    private final Map<String, Supplier<String>> jobs = new HashMap<>();
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    /** job_method → 지금 걸려 있는 스케줄과 그 설정. */
    private final Map<String, Active> scheduled = new HashMap<>();

    private record Active(String settingKey, ScheduledFuture<?> future) {
    }

    public record BatchJobSetting(Integer batchJobId, String jobMethod, String triggerType,
                                  String triggerRepeatSeconds, String triggerCronExpression,
                                  String batchStatus, String batchApplyFlag) {

        String settingKey() {
            return batchStatus + "|" + triggerType + "|" + triggerRepeatSeconds + "|" + triggerCronExpression;
        }
    }

    public BatchJobScheduler(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl,
                             @Value("${ghlove.internal.admin-secret}") String adminSecret,
                             BatchExecutionReporter reporter, List<BatchJobSource> sources) {
        this.restClient = RestClient.builder().baseUrl(adminServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
        this.reporter = reporter;
        sources.forEach(s -> jobs.putAll(s.batchJobs()));
        taskScheduler.setPoolSize(4);
        taskScheduler.setThreadNamePrefix("batch-job-");
        taskScheduler.initialize();
    }

    /** 이 서비스가 가진 job_method인지. */
    public boolean owns(String jobMethod) {
        return jobs.containsKey(jobMethod);
    }

    /** 수동 실행(AS-IS ManualBatchController /batch/run/{jobName}) - 설정과 무관하게 지금 한 번 돈다. */
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
        List<BatchJobSetting> settings;
        try {
            settings = restClient.get().uri("/api/admin/batch-jobs").retrieve()
                    .body(new ParameterizedTypeReference<List<BatchJobSetting>>() {
                    });
        } catch (RestClientException e) {
            log.debug("배치 설정 조회 실패 - 기존 스케줄 유지: {}", e.getMessage());
            return;
        }
        if (settings == null) {
            return;
        }
        Map<String, BatchJobSetting> mine = new HashMap<>();
        for (BatchJobSetting s : settings) {
            if (s.jobMethod() != null && jobs.containsKey(s.jobMethod())) {
                mine.put(s.jobMethod(), s);
            }
        }
        // 화면에서 지운 job은 내린다.
        scheduled.keySet().stream().filter(m -> !mine.containsKey(m)).toList().forEach(this::cancel);

        for (BatchJobSetting s : mine.values()) {
            Active current = scheduled.get(s.jobMethod());
            if (current == null || !current.settingKey().equals(s.settingKey())) {
                cancel(s.jobMethod());
                ScheduledFuture<?> future = STATUS_RUN.equals(s.batchStatus()) ? schedule(s) : null;
                scheduled.put(s.jobMethod(), new Active(s.settingKey(), future));
                log.info("배치 스케줄 반영 - {} 상태={} 트리거={} {}", s.jobMethod(), s.batchStatus(), s.triggerType(),
                        TRIGGER_CRON.equals(s.triggerType()) ? s.triggerCronExpression() : s.triggerRepeatSeconds());
            }
            if (!"1".equals(s.batchApplyFlag())) {
                markApplied(s.batchJobId());
            }
        }
    }

    private ScheduledFuture<?> schedule(BatchJobSetting s) {
        Runnable task = () -> {
            try {
                execute(s.jobMethod(), jobs.get(s.jobMethod()));
            } catch (RuntimeException e) {
                log.error("[배치] {} 실행 오류", s.jobMethod(), e);
            }
        };
        try {
            if (TRIGGER_CRON.equals(s.triggerType())) {
                return taskScheduler.schedule(task, new CronTrigger(Objects.requireNonNull(s.triggerCronExpression()).trim()));
            }
            if (TRIGGER_SIMPLE.equals(s.triggerType())) {
                // AS-IS: 숫자가 아니면 99999999999(사실상 한 번)로 둔다.
                long interval;
                try {
                    interval = Long.parseLong(s.triggerRepeatSeconds().trim());
                } catch (RuntimeException e) {
                    interval = 99999999999L;
                }
                return taskScheduler.scheduleAtFixedRate(task, Duration.ofMillis(interval));
            }
        } catch (RuntimeException e) {
            log.error("[배치] {} 스케줄 설정 오류(트리거 {} / {})", s.jobMethod(), s.triggerType(),
                    s.triggerCronExpression(), e);
        }
        return null;
    }

    /**
     * AS-IS BatchExecutionAspect: pointcut이 {@code JobServiceImpl.*Batch(..)}라 이름이 Batch로 끝나는 job만 실행로그에 남고
     * (bix5*·sendNuri2PresentOrder 등은 안 남는다), 배치구분은 {@code JobServiceImpl.메서드()}(Signature.toShortString)다.
     */
    private String execute(String jobMethod, Supplier<String> job) {
        if (!jobMethod.endsWith("Batch")) {
            return job.get();
        }
        return reporter.runAndReport("JobServiceImpl." + jobMethod + "()", job);
    }

    private void cancel(String jobMethod) {
        Active current = scheduled.remove(jobMethod);
        if (current != null && current.future() != null) {
            current.future().cancel(false);
        }
    }

    private void markApplied(Integer batchJobId) {
        try {
            restClient.post().uri("/api/admin/batch-jobs/{id}/applied", batchJobId).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.debug("배치 적용여부 갱신 실패 - id={}", batchJobId);
        }
    }
}

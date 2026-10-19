package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배치 작업 스케줄러 등록 (AS-IS opmanager/batch-job, OP_BATCH_JOB) - 실행 메서드명+주기(심플 밀리초 또는
 *  크론식)+사용여부. AS-IS처럼 이 등록값이 곧 스케줄이다(2026-10-15~): 각 서비스의 {@code BatchJobScheduler}가
 *  15초마다 읽어 자기 job_method를 등록/재등록/중지하고 BATCH_APPLY_FLAG를 '1'로 바꾼다. */
@Entity
@Table(name = "OP_BATCH_JOB")
@Getter
@Setter
@NoArgsConstructor
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opBatchJobIdSeq")
    @SequenceGenerator(name = "opBatchJobIdSeq", sequenceName = "op_batch_job_batch_job_id_seq", allocationSize = 1)
    @Column(name = "BATCH_JOB_ID")
    private Integer batchJobId;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "JOB_METHOD")
    private String jobMethod;

    /** 1:심플(초 반복), 2:크론. */
    @Column(name = "TRIGGER_TYPE")
    private String triggerType;

    @Column(name = "TRIGGER_REPEAT_SECONDS")
    private String triggerRepeatSeconds;

    @Column(name = "TRIGGER_CRON_EXPRESSION")
    private String triggerCronExpression;

    /** 1:실행중, 2:중지. */
    @Column(name = "BATCH_STATUS")
    private String batchStatus;

    /** yyyyMMddHHmmss. */
    @Column(name = "BATCH_EXCUTE_DATE")
    private String batchExecuteDate;

    @Column(name = "BATCH_APPLY_FLAG")
    private String batchApplyFlag;

    @Column(name = "ORDERING")
    private Integer ordering;

    /** AS-IS getBatchJobList의 CASE - 1=심플 2=크론, 그 외 '-'. 목록은 코드가 아니라 이 라벨을 보여준다. */
    @Transient
    public String getTriggerTypeLabel() {
        if ("1".equals(triggerType)) {
            return "심플";
        }
        return "2".equals(triggerType) ? "크론" : "-";
    }

    /** AS-IS CASE - 1=실행중 2=정지, 그 외 '-'. */
    @Transient
    public String getBatchStatusLabel() {
        if ("1".equals(batchStatus)) {
            return "실행중";
        }
        return "2".equals(batchStatus) ? "정지" : "-";
    }

    /** AS-IS CASE - 0=적용전 1=적용완료, 그 외 '-'. */
    @Transient
    public String getBatchApplyFlagLabel() {
        if ("0".equals(batchApplyFlag)) {
            return "적용전";
        }
        return "1".equals(batchApplyFlag) ? "적용완료" : "-";
    }
}

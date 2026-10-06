package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배치 작업 스케줄러 등록 (AS-IS opmanager/batch-job) - 실행 메서드명+주기(단순반복초 또는
 *  크론식)를 등록/관리한다. 이 프로젝트엔 이 등록값을 실제로 읽어 실행하는 동적 스케줄러
 *  엔진이 없다 - 등록/조회 화면까지만 AS-IS와 동일하게 재현하고, 실제 정기 실행이 필요한
 *  작업(예: DesignatedAdminService.closeExpiredProjects())은 이미 각 서비스의 @Scheduled로
 *  개별 구현돼 있다(mail-config와 동일한 성격의 "설정 화면 vs 실제 트리거" 분리). */
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

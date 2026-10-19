package com.ghlove.member.service;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 이 서비스가 실행하는 배치 job 목록 - 키는 AS-IS {@code OP_BATCH_JOB.JOB_METHOD} 그대로, 값은 실행 본문
 * (반환 문자열은 배치 실행로그(7209)의 메시지). 언제·실행할지는 {@link BatchJobScheduler}가 관리 화면 설정대로 정한다.
 */
public interface BatchJobSource {

    Map<String, Supplier<String>> batchJobs();
}

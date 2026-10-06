package com.ghlove.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * 정기배치 실행결과를 admin의 "배치 실행로그 조회"(메뉴 7209)가 읽는 표
 * ({@code admin.op_batch_execution})에 보고한다.
 *
 * <p>AS-IS는 운영관리 한 덩어리라 배치가 그 표에 직접 썼다. MSA에서는 표가 admin 소유라
 * 공유시크릿을 싣고 {@code POST /api/admin/batch-executions}로 보고한다.
 *
 * <p>{@code batchType}은 7209 화면이 작업명을 찾는 키다 - 화면 쿼리가
 * {@code BATCH_TYPE LIKE '%' || OP_BATCH_JOB.JOB_METHOD || '%'}이므로
 * {@code op_batch_job.job_method}와 같은 문자열을 써야 작업명이 붙는다.
 *
 * <p>보고 실패가 배치 자체를 실패시키면 안 되므로 예외는 로그만 남기고 삼킨다.
 *
 * <p>서비스마다 같은 내용의 작은 클래스를 둔다 - 이 프로젝트는 서비스별 독립 Gradle
 * 프로젝트라 공유 모듈이 없다(donation·point에도 동일한 클래스가 있다).
 */
@Component
@Slf4j
public class BatchExecutionReporter {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");

    /** AS-IS 화면: RESULT='1'이면 '정상', 그 외는 '오류'. */
    private static final String RESULT_OK = "1";
    private static final String RESULT_ERROR = "0";

    private final RestClient restClient;
    private final String adminSecret;

    public BatchExecutionReporter(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl,
                                  @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
        this.adminSecret = adminSecret;
    }

    private record ExecutionReport(String batchType, String executionDate, String startTime,
                                   String endTime, String result, String message) {
    }

    /**
     * 배치를 실행하고 시작·종료시각과 결과를 보고한다.
     *
     * @param batchType op_batch_job.job_method와 같은 문자열
     * @param job       실행할 배치 - 반환 문자열이 화면의 "오류내용"(정상일 때는 처리요약)에 들어간다
     */
    public <T> T runAndReport(String batchType, Supplier<T> job) {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            T result = job.get();
            report(batchType, startedAt, RESULT_OK, String.valueOf(result));
            return result;
        } catch (RuntimeException e) {
            report(batchType, startedAt, RESULT_ERROR, e.toString());
            throw e;
        }
    }

    private void report(String batchType, LocalDateTime startedAt, String result, String message) {
        LocalDateTime endedAt = LocalDateTime.now();
        try {
            restClient.post()
                    .uri("/api/admin/batch-executions")
                    .header("X-Internal-Secret", adminSecret)
                    .body(new ExecutionReport(batchType, DAY.format(startedAt), TIME.format(startedAt),
                            TIME.format(endedAt), result, message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("배치 실행로그 보고 실패 - batchType={}", batchType, e);
        }
    }
}

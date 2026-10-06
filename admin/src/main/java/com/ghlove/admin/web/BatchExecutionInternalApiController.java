package com.ghlove.admin.web;

import com.ghlove.admin.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 배치 실행로그 기록 - "배치 실행로그 조회"(메뉴 7209)가 읽는 {@code admin.op_batch_execution}에
 * 다른 서비스의 배치가 결과를 남기는 내부 전용 엔드포인트다.
 *
 * <p>AS-IS는 운영관리 한 덩어리라 배치가 이 표에 직접 썼지만, MSA에서는 정기배치가 각 서비스에
 * 흩어져 있고({@code DesignatedAdminService.closeExpiredProjects} 등) 표는 admin 소유라
 * 공유시크릿을 싣고 이 API로 보고한다(gift→admin 판매자공지와 같은 패턴).
 *
 * <p>7209 화면은 {@code BATCH_TYPE LIKE '%' || OP_BATCH_JOB.JOB_METHOD || '%'}로 작업명을 찾으므로,
 * {@code batchType}에는 <b>op_batch_job에 등록된 job_method와 같은 문자열</b>
 * (예: {@code DesignatedAdminService.closeExpiredProjects})을 넣어야 목록에 작업명이 붙는다.
 */
@RestController
@RequestMapping("/api/admin/batch-executions")
@RequiredArgsConstructor
public class BatchExecutionInternalApiController {

    private final BatchLogRepository batchLogRepository;

    @Value("${ghlove.internal.admin-secret}")
    private String adminSecret;

    /** 실행 결과 - result는 AS-IS 그대로 '1'이 정상, 그 외는 오류로 표시된다. */
    public record ExecutionReport(String batchType, String executionDate, String startTime,
                                  String endTime, String result, String message) {
    }

    @PostMapping
    @Transactional
    public void record(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                       @RequestBody ExecutionReport report) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (report.batchType() == null || report.batchType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "batchType은 필수입니다.");
        }
        batchLogRepository.recordExecution(report.batchType(), report.executionDate(),
                report.startTime(), report.endTime(), report.result(), report.message());
    }
}

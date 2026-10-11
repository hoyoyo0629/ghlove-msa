package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 배치 실행로그 조회 (메뉴 7209) - AS-IS batch-log-mapper.xml 이식.
 *
 * AS-IS는 실행로그 표 {@code OP_BATCH_EXECUTION}(배치구분/실행날짜/시작·종료시간/결과/메시지)과
 * 배치작업 설정표 {@code OP_BATCH_JOB}을 {@code BE.BATCH_TYPE LIKE '%'||BJ.JOB_METHOD||'%'}로
 * 이어서 작업명을 가져온다 - 외래키가 아니라 LIKE 조인이다(그대로 옮겼다).
 *
 * {@code OP_BATCH_EXECUTION}에는 기본키가 없어 엔티티를 만들지 않고 네이티브 쿼리로만 읽는다
 * (조회 전용 화면이다).
 */
@Repository
@RequiredArgsConstructor
public class BatchLogRepository {

    private final EntityManager entityManager;

    /** AS-IS BatchLogResult 결과행. */
    public record BatchLogRow(String jobName, String batchType, String executionDate,
                              String startTime, String endTime, String rsltCode,
                              String rsltCodeDesc, String message) {

        /** AS-IS op:date - yyyyMMdd 저장값을 날짜로 보여준다. */
        public String getExecutionDateText() {
            if (executionDate == null || executionDate.length() < 8) {
                return executionDate == null ? "" : executionDate;
            }
            return executionDate.substring(0, 4) + "-" + executionDate.substring(4, 6)
                    + "-" + executionDate.substring(6, 8);
        }

        /**
         * AS-IS START_TIME/END_TIME은 CUBRID TIME 컬럼이라 JDBC가 "09:30:00" 모양으로 바로
         * 내려준다. TO-BE는 같은 값을 VARCHAR(8) "HHmmss"(예: "013000")로 저장해서 그대로
         * 찍으면 다르게 보인다 - 여기서 콜론을 끼워 맞춘다.
         */
        public String getStartTimeText() {
            return toHms(startTime);
        }

        public String getEndTimeText() {
            return toHms(endTime);
        }

        private static String toHms(String raw) {
            if (raw == null || raw.length() < 6) {
                return raw == null ? "" : raw;
            }
            return raw.substring(0, 2) + ":" + raw.substring(2, 4) + ":" + raw.substring(4, 6);
        }
    }

    /**
     * AS-IS {@code getBatchLogList} - 작업명 LIKE + 실행날짜 BETWEEN.
     * 날짜가 비면 AS-IS는 {@code NVL(..., to_char(sys_date,'YYYYMMDD'))}로 오늘을 쓴다
     * (화면 컨트롤러도 비어 있으면 오늘로 채워 보낸다).
     */
    public List<BatchLogRow> getBatchLogList(String jobNameQuery, String searchStartDate, String searchEndDate) {
        StringBuilder sql = new StringBuilder("""
                select bj.job_name as job_name
                     , be.batch_type
                     , be.execution_date
                     , be.start_time
                     , be.end_time
                     , be.result as rslt_code
                     , case when be.result = '1' then '정상' else '오류' end as rslt_code_desc
                     , be.message
                  from admin.op_batch_execution be
                     , admin.op_batch_job bj
                 where 1 = 1
                   and be.batch_type like '%' || bj.job_method || '%'
                """);
        if (jobNameQuery != null && !jobNameQuery.isBlank()) {
            sql.append(" and bj.job_name like concat('%', :query, '%')");
        }
        sql.append("""
                   and be.execution_date between coalesce(:startDate, to_char(now(), 'YYYYMMDD'))
                                             and coalesce(:endDate, to_char(now(), 'YYYYMMDD'))
                 order by be.execution_date desc, be.start_time, be.end_time
                """);

        Query query = entityManager.createNativeQuery(sql.toString());
        if (jobNameQuery != null && !jobNameQuery.isBlank()) {
            query.setParameter("query", jobNameQuery);
        }
        query.setParameter("startDate", blankToNull(searchStartDate));
        query.setParameter("endDate", blankToNull(searchEndDate));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                // SQL 리터럴에서 나온 컬럼(결과 라벨 CASE)은 bpchar로 와서 캐스팅이 깨질 수 있어
                // toString으로 받는다 - QestnarRepository.asString 주석 참고
                .map(r -> new BatchLogRow(asString(r[0]), asString(r[1]), asString(r[2]), asString(r[3]),
                        asString(r[4]), asString(r[5]), asString(r[6]), asString(r[7])))
                .toList();
    }

    /**
     * 배치 실행 결과 1건 기록 - AS-IS는 배치가 끝날 때마다 {@code OP_BATCH_EXECUTION}에
     * 같은 키(배치구분+실행날짜+시작시간)로 MERGE한다. 이 표에는 기본키가 없어 중복 방지가
     * DB 차원에서 되지 않으므로, 같은 키가 이미 있으면 UPDATE하고 없으면 INSERT한다
     * (AS-IS의 ON DUPLICATE KEY MERGE와 같은 뜻).
     */
    public void recordExecution(String batchType, String executionDate, String startTime,
                                String endTime, String result, String message) {
        int updated = entityManager.createNativeQuery("""
                        update admin.op_batch_execution
                           set end_time = :endTime, result = :result, message = :message
                         where batch_type = :batchType
                           and execution_date = :executionDate
                           and start_time = :startTime
                        """)
                .setParameter("batchType", batchType)
                .setParameter("executionDate", executionDate)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("result", result)
                .setParameter("message", message)
                .executeUpdate();
        if (updated == 0) {
            entityManager.createNativeQuery("""
                            insert into admin.op_batch_execution
                              (batch_type, execution_date, start_time, end_time, result, message)
                            values (:batchType, :executionDate, :startTime, :endTime, :result, :message)
                            """)
                    .setParameter("batchType", batchType)
                    .setParameter("executionDate", executionDate)
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime)
                    .setParameter("result", result)
                    .setParameter("message", message)
                    .executeUpdate();
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}

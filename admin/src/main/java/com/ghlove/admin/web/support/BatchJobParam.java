package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.batch.support.BatchJobParam 재현 - Batch Job 목록 검색조건.
 * 작업 검색은 메서드명(ID → JOB_METHOD)·작업명(LABEL → JOB_NAME) LIKE이고,
 * 트리거 종류(triType 1=심플 2=크론)·배치 상태(batchType 1=실행중 2=정지)는 빈값이면 전체다.
 */
@Getter
@Setter
public class BatchJobParam {

    private String searchType = "ID";
    private String query = "";
    private String triType = "";
    private String batchType = "";

    public boolean matches(String jobMethod, String jobName, String triggerType, String batchStatus) {
        if (query != null && !query.isBlank()) {
            String keyword = query.trim();
            boolean hit = "LABEL".equals(searchType)
                    ? (jobName != null && jobName.contains(keyword))
                    : (jobMethod != null && jobMethod.contains(keyword));
            if (!hit) {
                return false;
            }
        }
        if (triType != null && !triType.isBlank() && !triType.equals(triggerType)) {
            return false;
        }
        return batchType == null || batchType.isBlank() || batchType.equals(batchStatus);
    }
}

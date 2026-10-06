package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.batch.support.BatchLogParam - 배치 실행로그 조회 검색조건. */
@Getter
@Setter
public class BatchLogParam {

    /** 작업 검색 - AS-IS는 JOBNAME 하나뿐이다(메서드명 option은 주석처리). */
    private String searchType = "JOBNAME";

    private String query;

    /** 실행날짜 - AS-IS도 EXECUTION_DATE 하나뿐이다. */
    private String searchDateType = "EXECUTION_DATE";

    private String searchStartDate;

    private String searchEndDate;

    private int page = 1;

    /** 화면 출력수 - AS-IS는 10/50/100/200/500이다. */
    private int itemsPerPage = 10;
}

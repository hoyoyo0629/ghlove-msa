package com.ghlove.donation.repository;

import com.ghlove.donation.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 기부 통계(AS-IS opmanager/give/statistics) 집계 - give-statistics-mapper.xml의
 * getGiveStatisticsAllCount / statisticsGiveAllTotalByMonth / statisticsGiveAllTotalByHour를
 * CUBRID→Postgres로 포팅한 것. 스키마 치환은 지정기부 통계와 동일:
 *  - 정산연월: AS-IS YEAR(STTEMNT_PAY_DE)/MONTH(STTEMNT_PAY_DE) 그대로(yyyyMMdd 문자열의 substr).
 *    확정판정 = CNTR_STTUS_CODE='COMPLETED'(AS-IS '200') AND STTEMNT_PAY_DE IS NOT NULL - 2026-10-14 수납완료가
 *    수납일을 넣게 되어 기부일(CNTR_DE)로 대신하던 번역을 AS-IS 기준으로 되돌렸다.
 *  - 시간대: HOUR(FRST_REGIST_PNTTM) → substr(FRST_REGIST_PNTTM,9,2)(yyyyMMddHHmmss).
 *  - 온/오프라인: CNTR_PATH_CODE '100'=온라인, '200'/'300'=오프라인.
 * foreignerFlag(외국인): AS-IS는 G_CNTR.FOREIGN_STATUS_CODE != '0' 로 거른다 - 같은 컬럼 조건을 :foreigner('Y') 인자로 옮겼다(전체 요약·월별·시간대별).
 */
public interface GiveStatRepository extends JpaRepository<Donation, String> {

    /** 전체 요약: 총기부건수/총기부인원(중복제외)/총기부금액. */
    @Query(nativeQuery = true, value = """
        SELECT COALESCE(COUNT(USER_ID), 0) AS cntr_cnt
             , COALESCE(COUNT(DISTINCT USER_ID), 0) AS cntr_person
             , COALESCE(SUM(CNTR_AMT), 0) AS cntr_amt
        FROM donation.G_CNTR
        WHERE substr(STTEMNT_PAY_DE, 1, 4) = :year
          AND DELETE_AT = 'N'
          AND CNTR_STTUS_CODE = 'COMPLETED'
          AND STTEMNT_PAY_DE IS NOT NULL
          AND (:locgovCode = '' OR CNTR_LOCGOV_CODE = :locgovCode)
          AND (:foreigner <> 'Y' OR FOREIGN_STATUS_CODE <> '0')
        """)
    List<Object[]> allSummary(@Param("year") String year, @Param("locgovCode") String locgovCode,
            @Param("foreigner") String foreigner);

    /** 월별(1~12): 건수/인원(중복제외)/금액/온라인건수/오프라인건수. */
    @Query(nativeQuery = true, value = """
        SELECT CAST(substr(STTEMNT_PAY_DE, 5, 2) AS INTEGER) AS cntr_month
             , COALESCE(COUNT(USER_ID), 0) AS cntr_cnt
             , COALESCE(COUNT(DISTINCT USER_ID), 0) AS cntr_person
             , COALESCE(SUM(CNTR_AMT), 0) AS cntr_amt
             , COALESCE(COUNT(CASE WHEN CNTR_PATH_CODE = '100' THEN 1 END), 0) AS cntr_online
             , COALESCE(COUNT(CASE WHEN CNTR_PATH_CODE IN ('200', '300') THEN 1 END), 0) AS cntr_offline
        FROM donation.G_CNTR
        WHERE substr(STTEMNT_PAY_DE, 1, 4) = :year
          AND DELETE_AT = 'N'
          AND CNTR_STTUS_CODE = 'COMPLETED'
          AND STTEMNT_PAY_DE IS NOT NULL
          AND (:locgovCode = '' OR CNTR_LOCGOV_CODE = :locgovCode)
          AND (:foreigner <> 'Y' OR FOREIGN_STATUS_CODE <> '0')
        GROUP BY substr(STTEMNT_PAY_DE, 5, 2)
        ORDER BY 1
        """)
    List<Object[]> allByMonth(@Param("year") String year, @Param("locgovCode") String locgovCode,
            @Param("foreigner") String foreigner);

    /** 시간대별(0~23): 특정일(cntr_de=yyyyMMdd)의 시간대별 기부금액. */
    @Query(nativeQuery = true, value = """
        SELECT CAST(substr(FRST_REGIST_PNTTM, 9, 2) AS INTEGER) AS cntr_hour
             , COALESCE(SUM(CNTR_AMT), 0) AS cntr_amt
        FROM donation.G_CNTR
        WHERE CNTR_DE = :date
          AND DELETE_AT = 'N'
          AND CNTR_STTUS_CODE = 'COMPLETED'
          AND STTEMNT_PAY_DE IS NOT NULL
          AND (:locgovCode = '' OR CNTR_LOCGOV_CODE = :locgovCode)
          AND (:foreigner <> 'Y' OR FOREIGN_STATUS_CODE <> '0')
        GROUP BY substr(FRST_REGIST_PNTTM, 9, 2)
        ORDER BY 1
        """)
    List<Object[]> allByHour(@Param("date") String date, @Param("locgovCode") String locgovCode,
            @Param("foreigner") String foreigner);

    /** 지자체별(AS-IS getGivePersonStatisticsList): 연도+지자체 그룹, 기부인원(중복제외)/금액/건수.
     *  정렬(itemsOrder)·페이징은 호출측 메모리. 기본 정렬 연도desc·상위지자체·지자체. */
    @Query(nativeQuery = true, value = """
        SELECT CAST(substr(GC.STTEMNT_PAY_DE, 1, 4) AS INTEGER) AS cntr_year
             , GL.UPPER_LOCGOV_CODE AS upper_locgov_code
             , GL.UPPER_LOCGOV_NM AS upper_locgov_nm
             , GL.LOCGOV_CODE AS locgov_code
             , GL.LOCGOV_NM AS locgov_nm
             , COALESCE(SUM(GC.CNTR_AMT), 0) AS cntr_amt
             , COUNT(DISTINCT GC.USER_ID) AS give_persons
             , COUNT(GC.USER_ID) AS give_cnt
        FROM donation.G_CNTR GC
            JOIN donation.G_LOCGOV GL ON GC.CNTR_LOCGOV_CODE = GL.LOCGOV_CODE
        WHERE GC.DELETE_AT = 'N'
          AND GC.CNTR_STTUS_CODE = 'COMPLETED'
          AND GC.STTEMNT_PAY_DE IS NOT NULL
          AND (:shLocgovCode = '' OR GL.LOCGOV_CODE = :shLocgovCode)
          AND (:year = '' OR substr(GC.STTEMNT_PAY_DE, 1, 4) = :year)
          AND (:foreigner <> 'Y' OR GC.FOREIGN_STATUS_CODE <> '0')
        GROUP BY substr(GC.STTEMNT_PAY_DE, 1, 4), GL.UPPER_LOCGOV_CODE, GL.UPPER_LOCGOV_NM, GL.LOCGOV_CODE, GL.LOCGOV_NM
        ORDER BY cntr_year DESC, GL.UPPER_LOCGOV_CODE, GL.LOCGOV_CODE
        """)
    List<Object[]> locgovList(@Param("year") String year, @Param("shLocgovCode") String shLocgovCode,
            @Param("foreigner") String foreigner);
}

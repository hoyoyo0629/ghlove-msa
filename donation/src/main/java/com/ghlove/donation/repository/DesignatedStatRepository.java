package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 지정기부 월별통계(특정사업 월별통계) 집계 - AS-IS designated-donation-mapper.xml의
 * selectDesignatedLocgovMonth{Campaign,AmountRaised,Amount} 3종을 CUBRID→Postgres로 포팅한 것.
 * AS-IS 로직을 그대로 재현하되, AS-IS와 TO-BE의 스키마 값 사전(dictionary)이 다른 부분만 치환했다:
 *  - 확정기부 판정: AS-IS `CNTR_STTUS_CODE='200' AND STTEMNT_PAY_DE IS NOT NULL` →
 *    TO-BE는 기부확정 상태를 CNTR_STTUS_CODE='COMPLETED'로 저장하고 정산일자(STTEMNT_PAY_DE)를
 *    채우지 않으므로(전 행 NULL) 'COMPLETED'를 확정조건으로 쓴다(기존 analysis 코드와 동일).
 *  - 사업상태: AS-IS '1'(대기)/'2'(진행)/'9'(종료) → TO-BE PENDING/OPEN/CLOSED. 쿼리 내부에서
 *    AS-IS 코드값으로 환산해 상태파생(진행→모금≥목표 또는 종료일경과 시 종료)과 prjStatus
 *    필터(0전체/2진행/9종료)가 AS-IS와 동일하게 동작하도록 했다.
 *  - 조인키: AS-IS G_CNTR.PRJ_ID → TO-BE는 DSGN_DNTN_BIZ_ID(PRJ_ID는 TO-BE 死컬럼으로 전부 0).
 * 바인딩 파라미터(shWdr/shLocgovCode/selYear/prjStatus)는 빈값("")으로 정규화되어 들어온다.
 */
public interface DesignatedStatRepository extends JpaRepository<DesignatedProject, Long> {

    /** 1. 사업구분별 사업 진행건 '건수/비율'. 결과 2행(tp=1 비율%, tp=2 건수). */
    @Query(nativeQuery = true, value = """
        SELECT T_COPY.TP AS tp
             , CASE WHEN T_COPY.TP = 1 THEN '사업별 캠페인 진행건 비율' ELSE '캠페인 건수' END AS column_tp_desc
             , CASE WHEN T_COPY.TP = 1 THEN PRJ_RT_100 ELSE PRJ_CNT_100 END AS prj_bsns_100
             , CASE WHEN T_COPY.TP = 1 THEN PRJ_RT_200 ELSE PRJ_CNT_200 END AS prj_bsns_200
             , CASE WHEN T_COPY.TP = 1 THEN PRJ_RT_300 ELSE PRJ_CNT_300 END AS prj_bsns_300
             , CASE WHEN T_COPY.TP = 1 THEN PRJ_RT_400 ELSE PRJ_CNT_400 END AS prj_bsns_400
             , CASE WHEN T_COPY.TP = 1 THEN PRJ_RT_TOT ELSE PRJ_CNT_TOT END AS prj_bsns_tot
        FROM (
            SELECT
                  CASE WHEN COUNT(*) <> 0 THEN FLOOR(SUM(CASE WHEN BSNS_TYPE = '100' THEN 1 ELSE 0 END) * 10000 / COUNT(*) / 100) ELSE 0 END AS PRJ_RT_100
                , CASE WHEN COUNT(*) <> 0 THEN FLOOR(SUM(CASE WHEN BSNS_TYPE = '200' THEN 1 ELSE 0 END) * 10000 / COUNT(*) / 100) ELSE 0 END AS PRJ_RT_200
                , CASE WHEN COUNT(*) <> 0 THEN FLOOR(SUM(CASE WHEN BSNS_TYPE = '300' THEN 1 ELSE 0 END) * 10000 / COUNT(*) / 100) ELSE 0 END AS PRJ_RT_300
                , CASE WHEN COUNT(*) <> 0 THEN FLOOR(SUM(CASE WHEN BSNS_TYPE = '400' THEN 1 ELSE 0 END) * 10000 / COUNT(*) / 100) ELSE 0 END AS PRJ_RT_400
                , CASE WHEN COUNT(*) <> 0 THEN 100 ELSE 0 END AS PRJ_RT_TOT
                , COALESCE(SUM(CASE WHEN BSNS_TYPE = '100' THEN 1 ELSE 0 END), 0) AS PRJ_CNT_100
                , COALESCE(SUM(CASE WHEN BSNS_TYPE = '200' THEN 1 ELSE 0 END), 0) AS PRJ_CNT_200
                , COALESCE(SUM(CASE WHEN BSNS_TYPE = '300' THEN 1 ELSE 0 END), 0) AS PRJ_CNT_300
                , COALESCE(SUM(CASE WHEN BSNS_TYPE = '400' THEN 1 ELSE 0 END), 0) AS PRJ_CNT_400
                , COUNT(*) AS PRJ_CNT_TOT
            FROM (
                SELECT
                      P.DSGN_DNTN_BIZ_SE_CD AS BSNS_TYPE
                    , CASE WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'OPEN'
                                THEN CASE WHEN COALESCE(C.CNTR_AMT, 0) >= P.GOAL_AMT THEN '9'
                                          WHEN P.DSGN_DNTN_BIZ_END_YMD < to_char(now(), 'YYYYMMDD') THEN '9'
                                          ELSE '2' END
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'CLOSED'  THEN '9'
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'PENDING' THEN '1'
                           ELSE P.DSGN_DNTN_BIZ_STTS_CD END AS PRJ_STATUS
                FROM donation.G_DSGN_DNTN_BIZ_MNG P
                    LEFT OUTER JOIN (
                        SELECT SUM(CAST(CNTR_AMT AS BIGINT)) AS CNTR_AMT, COUNT(*) AS CNT, DSGN_DNTN_BIZ_ID
                        FROM donation.G_CNTR
                        WHERE CNTR_DE <= to_char(now(), 'YYYYMMDD')
                            AND DELETE_AT = 'N'
                            AND CNTR_STTUS_CODE = 'COMPLETED'
                            AND DSGN_DNTN_BIZ_ID > 0
                        GROUP BY DSGN_DNTN_BIZ_ID
                    ) C ON P.DSGN_DNTN_BIZ_ID = C.DSGN_DNTN_BIZ_ID
                WHERE 1 = 1
                    AND ( (:shLocgovCode <> '' AND P.LCLGV_CD = :shLocgovCode)
                       OR (:shLocgovCode = '' AND :shWdr <> '' AND P.LCLGV_CD IN (SELECT LOCGOV_CODE FROM donation.G_LOCGOV WHERE UPPER_LOCGOV_CODE = :shWdr))
                       OR (:shLocgovCode = '' AND :shWdr = '') )
                    AND ( (P.DSGN_DNTN_BIZ_BGNG_YMD BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR (P.DSGN_DNTN_BIZ_END_YMD  BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR ((:selYear || '0101') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD)
                       OR ((:selYear || '1231') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD) )
            ) P
            WHERE ( (:prjStatus = '0') OR (:prjStatus <> '0' AND P.PRJ_STATUS = :prjStatus) )
        ) T_A
        CROSS JOIN (SELECT 1 AS TP UNION ALL SELECT 2 AS TP) T_COPY
        ORDER BY T_COPY.TP
        """)
    List<Object[]> selectMonthCampaign(@Param("shWdr") String shWdr,
                                       @Param("shLocgovCode") String shLocgovCode,
                                       @Param("selYear") String selYear,
                                       @Param("prjStatus") String prjStatus);

    /** 2. 사업구분별 모금액/목표모금액/비율. 결과 3행(tp=1 비율%, tp=2 모금액, tp=3 목표모금액). */
    @Query(nativeQuery = true, value = """
        SELECT T_COPY.TP AS tp
             , CASE WHEN T_COPY.TP = 1 THEN '모금액 비율' WHEN T_COPY.TP = 2 THEN '모금액' ELSE '목표모금액' END AS column_tp_desc
             , CASE WHEN T_COPY.TP = 1 THEN CNTR_AMT_RT_100 WHEN T_COPY.TP = 2 THEN CNTR_AMT_100 ELSE TARGET_AMT_100 END AS prj_bsns_100
             , CASE WHEN T_COPY.TP = 1 THEN CNTR_AMT_RT_200 WHEN T_COPY.TP = 2 THEN CNTR_AMT_200 ELSE TARGET_AMT_200 END AS prj_bsns_200
             , CASE WHEN T_COPY.TP = 1 THEN CNTR_AMT_RT_300 WHEN T_COPY.TP = 2 THEN CNTR_AMT_300 ELSE TARGET_AMT_300 END AS prj_bsns_300
             , CASE WHEN T_COPY.TP = 1 THEN CNTR_AMT_RT_400 WHEN T_COPY.TP = 2 THEN CNTR_AMT_400 ELSE TARGET_AMT_400 END AS prj_bsns_400
             , CASE WHEN T_COPY.TP = 1 THEN CNTR_AMT_RT_TOT WHEN T_COPY.TP = 2 THEN CNTR_AMT_TOT ELSE TARGET_AMT_TOT END AS prj_bsns_tot
        FROM (
            SELECT
                  CASE WHEN SUM(CNTR_AMT) <> 0 THEN FLOOR(SUM(CNTR_AMT_100) * 10000 / SUM(CNTR_AMT) / 100) ELSE 0 END AS CNTR_AMT_RT_100
                , CASE WHEN SUM(CNTR_AMT) <> 0 THEN FLOOR(SUM(CNTR_AMT_200) * 10000 / SUM(CNTR_AMT) / 100) ELSE 0 END AS CNTR_AMT_RT_200
                , CASE WHEN SUM(CNTR_AMT) <> 0 THEN FLOOR(SUM(CNTR_AMT_300) * 10000 / SUM(CNTR_AMT) / 100) ELSE 0 END AS CNTR_AMT_RT_300
                , CASE WHEN SUM(CNTR_AMT) <> 0 THEN FLOOR(SUM(CNTR_AMT_400) * 10000 / SUM(CNTR_AMT) / 100) ELSE 0 END AS CNTR_AMT_RT_400
                , CASE WHEN SUM(CNTR_AMT) <> 0 THEN 100 ELSE 0 END AS CNTR_AMT_RT_TOT
                , COALESCE(SUM(CNTR_AMT_100),  0) AS CNTR_AMT_100
                , COALESCE(SUM(CNTR_AMT_200),  0) AS CNTR_AMT_200
                , COALESCE(SUM(CNTR_AMT_300),  0) AS CNTR_AMT_300
                , COALESCE(SUM(CNTR_AMT_400),  0) AS CNTR_AMT_400
                , COALESCE(SUM(CNTR_AMT),      0) AS CNTR_AMT_TOT
                , COALESCE(SUM(TARGET_AMT_100),0) AS TARGET_AMT_100
                , COALESCE(SUM(TARGET_AMT_200),0) AS TARGET_AMT_200
                , COALESCE(SUM(TARGET_AMT_300),0) AS TARGET_AMT_300
                , COALESCE(SUM(TARGET_AMT_400),0) AS TARGET_AMT_400
                , COALESCE(SUM(TARGET_AMT),    0) AS TARGET_AMT_TOT
            FROM (
                SELECT
                      CASE WHEN P.DSGN_DNTN_BIZ_END_YMD <= (:selYear || '1231') AND P.DSGN_DNTN_BIZ_SE_CD = '100' THEN P.GOAL_AMT ELSE 0 END AS TARGET_AMT_100
                    , CASE WHEN P.DSGN_DNTN_BIZ_END_YMD <= (:selYear || '1231') AND P.DSGN_DNTN_BIZ_SE_CD = '200' THEN P.GOAL_AMT ELSE 0 END AS TARGET_AMT_200
                    , CASE WHEN P.DSGN_DNTN_BIZ_END_YMD <= (:selYear || '1231') AND P.DSGN_DNTN_BIZ_SE_CD = '300' THEN P.GOAL_AMT ELSE 0 END AS TARGET_AMT_300
                    , CASE WHEN P.DSGN_DNTN_BIZ_END_YMD <= (:selYear || '1231') AND P.DSGN_DNTN_BIZ_SE_CD = '400' THEN P.GOAL_AMT ELSE 0 END AS TARGET_AMT_400
                    , CASE WHEN P.DSGN_DNTN_BIZ_END_YMD <= (:selYear || '1231') THEN P.GOAL_AMT ELSE 0 END AS TARGET_AMT
                    , CASE WHEN P.DSGN_DNTN_BIZ_SE_CD = '100' THEN COALESCE(C.CNTR_AMT, 0) ELSE 0 END AS CNTR_AMT_100
                    , CASE WHEN P.DSGN_DNTN_BIZ_SE_CD = '200' THEN COALESCE(C.CNTR_AMT, 0) ELSE 0 END AS CNTR_AMT_200
                    , CASE WHEN P.DSGN_DNTN_BIZ_SE_CD = '300' THEN COALESCE(C.CNTR_AMT, 0) ELSE 0 END AS CNTR_AMT_300
                    , CASE WHEN P.DSGN_DNTN_BIZ_SE_CD = '400' THEN COALESCE(C.CNTR_AMT, 0) ELSE 0 END AS CNTR_AMT_400
                    , COALESCE(C.CNTR_AMT, 0) AS CNTR_AMT
                    , CASE WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'OPEN'
                                THEN CASE WHEN COALESCE(C.CNTR_AMT, 0) >= P.GOAL_AMT THEN '9'
                                          WHEN P.DSGN_DNTN_BIZ_END_YMD < to_char(now(), 'YYYYMMDD') THEN '9'
                                          ELSE '2' END
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'CLOSED'  THEN '9'
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'PENDING' THEN '1'
                           ELSE P.DSGN_DNTN_BIZ_STTS_CD END AS PRJ_STATUS
                FROM donation.G_DSGN_DNTN_BIZ_MNG P
                    LEFT OUTER JOIN (
                        SELECT SUM(CAST(CNTR_AMT AS BIGINT)) AS CNTR_AMT, COUNT(*) AS CNT, DSGN_DNTN_BIZ_ID
                        FROM donation.G_CNTR
                        WHERE CNTR_DE <= to_char(now(), 'YYYYMMDD')
                            AND DELETE_AT = 'N'
                            AND CNTR_STTUS_CODE = 'COMPLETED'
                            AND DSGN_DNTN_BIZ_ID > 0
                        GROUP BY DSGN_DNTN_BIZ_ID
                    ) C ON P.DSGN_DNTN_BIZ_ID = C.DSGN_DNTN_BIZ_ID
                WHERE 1 = 1
                    AND ( (:shLocgovCode <> '' AND P.LCLGV_CD = :shLocgovCode)
                       OR (:shLocgovCode = '' AND :shWdr <> '' AND P.LCLGV_CD IN (SELECT LOCGOV_CODE FROM donation.G_LOCGOV WHERE UPPER_LOCGOV_CODE = :shWdr))
                       OR (:shLocgovCode = '' AND :shWdr = '') )
                    AND ( (P.DSGN_DNTN_BIZ_BGNG_YMD BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR (P.DSGN_DNTN_BIZ_END_YMD  BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR ((:selYear || '0101') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD)
                       OR ((:selYear || '1231') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD) )
            ) T_A
            WHERE ( (:prjStatus = '0') OR (:prjStatus <> '0' AND T_A.PRJ_STATUS = :prjStatus) )
        ) T_B
        CROSS JOIN (SELECT 1 AS TP UNION ALL SELECT 2 AS TP UNION ALL SELECT 3 AS TP) T_COPY
        ORDER BY T_COPY.TP
        """)
    List<Object[]> selectMonthAmountRaised(@Param("shWdr") String shWdr,
                                           @Param("shLocgovCode") String shLocgovCode,
                                           @Param("selYear") String selYear,
                                           @Param("prjStatus") String prjStatus);

    /** 3. 월별(m01~m12+합계) 추이. 결과 4행(tp=1 목표금액, tp=2 모금액, tp=3 참여자수, tp=4 사업건수). */
    @Query(nativeQuery = true, value = """
        SELECT T_COPY.TP AS tp
             , CASE WHEN T_COPY.TP = 1 THEN '목표금액(원)' WHEN T_COPY.TP = 2 THEN '모금액(원)' WHEN T_COPY.TP = 3 THEN '참여자수(명)' ELSE '사업건수(건)' END AS column_tp_desc
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_01),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_01),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_01),0) ELSE COALESCE(SUM(PRJ_CNT_01),0) END AS m01
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_02),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_02),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_02),0) ELSE COALESCE(SUM(PRJ_CNT_02),0) END AS m02
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_03),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_03),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_03),0) ELSE COALESCE(SUM(PRJ_CNT_03),0) END AS m03
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_04),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_04),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_04),0) ELSE COALESCE(SUM(PRJ_CNT_04),0) END AS m04
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_05),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_05),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_05),0) ELSE COALESCE(SUM(PRJ_CNT_05),0) END AS m05
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_06),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_06),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_06),0) ELSE COALESCE(SUM(PRJ_CNT_06),0) END AS m06
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_07),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_07),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_07),0) ELSE COALESCE(SUM(PRJ_CNT_07),0) END AS m07
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_08),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_08),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_08),0) ELSE COALESCE(SUM(PRJ_CNT_08),0) END AS m08
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_09),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_09),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_09),0) ELSE COALESCE(SUM(PRJ_CNT_09),0) END AS m09
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_10),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_10),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_10),0) ELSE COALESCE(SUM(PRJ_CNT_10),0) END AS m10
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_11),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_11),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_11),0) ELSE COALESCE(SUM(PRJ_CNT_11),0) END AS m11
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_12),0) WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_12),0) WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_12),0) ELSE COALESCE(SUM(PRJ_CNT_12),0) END AS m12
             , CASE WHEN T_COPY.TP = 1 THEN COALESCE(SUM(TARGET_AMT_TOT),0)
                    WHEN T_COPY.TP = 2 THEN COALESCE(SUM(CNTR_AMT_TOT),0)
                    WHEN T_COPY.TP = 3 THEN COALESCE(SUM(CNTR_CNT_TOT),0)
                    ELSE COALESCE(SUM(PRJ_CNT_01),0)+COALESCE(SUM(PRJ_CNT_02),0)+COALESCE(SUM(PRJ_CNT_03),0)+COALESCE(SUM(PRJ_CNT_04),0)
                        +COALESCE(SUM(PRJ_CNT_05),0)+COALESCE(SUM(PRJ_CNT_06),0)+COALESCE(SUM(PRJ_CNT_07),0)+COALESCE(SUM(PRJ_CNT_08),0)
                        +COALESCE(SUM(PRJ_CNT_09),0)+COALESCE(SUM(PRJ_CNT_10),0)+COALESCE(SUM(PRJ_CNT_11),0)+COALESCE(SUM(PRJ_CNT_12),0) END AS total
        FROM (
            SELECT  P.PRJ_ID
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='01' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_01
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='02' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_02
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='03' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_03
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='04' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_04
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='05' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_05
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='06' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_06
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='07' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_07
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='08' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_08
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='09' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_09
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='10' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_10
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='11' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_11
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') AND SUBSTR(P.PRJ_ED_DT,5,2)='12' THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_12
                  , MAX(CASE WHEN P.PRJ_ED_DT <= (:selYear || '1231') THEN P.TARGET_AMT ELSE 0 END) AS TARGET_AMT_TOT
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='01' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_01
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='02' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_02
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='03' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_03
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='04' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_04
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='05' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_05
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='06' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_06
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='07' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_07
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='08' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_08
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='09' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_09
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='10' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_10
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='11' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_11
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='12' THEN C.CNTR_AMT ELSE 0 END) AS CNTR_AMT_12
                  , SUM(C.CNTR_AMT) AS CNTR_AMT_TOT
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='01' THEN 1 ELSE 0 END) AS CNTR_CNT_01
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='02' THEN 1 ELSE 0 END) AS CNTR_CNT_02
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='03' THEN 1 ELSE 0 END) AS CNTR_CNT_03
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='04' THEN 1 ELSE 0 END) AS CNTR_CNT_04
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='05' THEN 1 ELSE 0 END) AS CNTR_CNT_05
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='06' THEN 1 ELSE 0 END) AS CNTR_CNT_06
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='07' THEN 1 ELSE 0 END) AS CNTR_CNT_07
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='08' THEN 1 ELSE 0 END) AS CNTR_CNT_08
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='09' THEN 1 ELSE 0 END) AS CNTR_CNT_09
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='10' THEN 1 ELSE 0 END) AS CNTR_CNT_10
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='11' THEN 1 ELSE 0 END) AS CNTR_CNT_11
                  , SUM(CASE WHEN SUBSTR(C.CNTR_DE,5,2)='12' THEN 1 ELSE 0 END) AS CNTR_CNT_12
                  , COUNT(C.CNTR_SN) AS CNTR_CNT_TOT
                  , CASE WHEN (:selYear || '01') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_01
                  , CASE WHEN (:selYear || '02') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_02
                  , CASE WHEN (:selYear || '03') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_03
                  , CASE WHEN (:selYear || '04') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_04
                  , CASE WHEN (:selYear || '05') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_05
                  , CASE WHEN (:selYear || '06') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_06
                  , CASE WHEN (:selYear || '07') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_07
                  , CASE WHEN (:selYear || '08') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_08
                  , CASE WHEN (:selYear || '09') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_09
                  , CASE WHEN (:selYear || '10') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_10
                  , CASE WHEN (:selYear || '11') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_11
                  , CASE WHEN (:selYear || '12') BETWEEN SUBSTR(MAX(P.PRJ_ST_DT),1,6) AND SUBSTR(MAX(P.PRJ_ED_DT),1,6) THEN 1 ELSE 0 END AS PRJ_CNT_12
            FROM (
                SELECT
                      P.DSGN_DNTN_BIZ_ID AS PRJ_ID
                    , P.DSGN_DNTN_BIZ_SE_CD AS BSNS_TYPE
                    , CASE WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'OPEN'
                                THEN CASE WHEN COALESCE(C.CNTR_AMT, 0) >= P.GOAL_AMT THEN '9'
                                          WHEN P.DSGN_DNTN_BIZ_END_YMD < to_char(now(), 'YYYYMMDD') THEN '9'
                                          ELSE '2' END
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'CLOSED'  THEN '9'
                           WHEN P.DSGN_DNTN_BIZ_STTS_CD = 'PENDING' THEN '1'
                           ELSE P.DSGN_DNTN_BIZ_STTS_CD END AS PRJ_STATUS
                    , P.DSGN_DNTN_BIZ_BGNG_YMD AS PRJ_ST_DT
                    , P.DSGN_DNTN_BIZ_END_YMD  AS PRJ_ED_DT
                    , P.GOAL_AMT AS TARGET_AMT
                FROM donation.G_DSGN_DNTN_BIZ_MNG P
                    LEFT OUTER JOIN (
                        SELECT SUM(CAST(CNTR_AMT AS BIGINT)) AS CNTR_AMT, COUNT(*) AS CNT, DSGN_DNTN_BIZ_ID
                        FROM donation.G_CNTR
                        WHERE CNTR_DE <= to_char(now(), 'YYYYMMDD')
                            AND DELETE_AT = 'N'
                            AND CNTR_STTUS_CODE = 'COMPLETED'
                            AND DSGN_DNTN_BIZ_ID > 0
                        GROUP BY DSGN_DNTN_BIZ_ID
                    ) C ON P.DSGN_DNTN_BIZ_ID = C.DSGN_DNTN_BIZ_ID
                WHERE 1 = 1
                    AND ( (:shLocgovCode <> '' AND P.LCLGV_CD = :shLocgovCode)
                       OR (:shLocgovCode = '' AND :shWdr <> '' AND P.LCLGV_CD IN (SELECT LOCGOV_CODE FROM donation.G_LOCGOV WHERE UPPER_LOCGOV_CODE = :shWdr))
                       OR (:shLocgovCode = '' AND :shWdr = '') )
                    AND ( (P.DSGN_DNTN_BIZ_BGNG_YMD BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR (P.DSGN_DNTN_BIZ_END_YMD  BETWEEN (:selYear || '0101') AND (:selYear || '1231'))
                       OR ((:selYear || '0101') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD)
                       OR ((:selYear || '1231') BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD) )
            ) P
            LEFT OUTER JOIN donation.G_CNTR C
                ON  P.PRJ_ID = C.DSGN_DNTN_BIZ_ID
                AND C.CNTR_DE BETWEEN (:selYear || '0101') AND (CASE WHEN (:selYear || '1231') <= to_char(now() - INTERVAL '1 day', 'YYYYMMDD') THEN (:selYear || '1231') ELSE to_char(now() - INTERVAL '1 day', 'YYYYMMDD') END)
                AND C.DELETE_AT = 'N'
                AND C.CNTR_STTUS_CODE = 'COMPLETED'
            WHERE ( (:prjStatus = '0') OR (:prjStatus <> '0' AND P.PRJ_STATUS = :prjStatus) )
            GROUP BY P.PRJ_ID
        ) T_A
        CROSS JOIN (SELECT 1 AS TP UNION ALL SELECT 2 AS TP UNION ALL SELECT 3 AS TP UNION ALL SELECT 4 AS TP) T_COPY
        GROUP BY T_COPY.TP
        ORDER BY T_COPY.TP
        """)
    List<Object[]> selectMonthAmount(@Param("shWdr") String shWdr,
                                     @Param("shLocgovCode") String shLocgovCode,
                                     @Param("selYear") String selYear,
                                     @Param("prjStatus") String prjStatus);

    // ---- 지자체별 통계(AS-IS designated-donation/analysis/locgov) ----
    // selectDesignatedLocgovStat(요약 1행) + selectDesignatedLocgovCntrStat(지자체별 목록) 포팅.
    // 상태파생: 진행('2'=OPEN)인데 모금>목표(strict) 또는 종료일<오늘(IN_DATE=3)이면 종료('9'). 월별통계와
    // 동일한 스키마 치환(확정기부=COMPLETED, 상태 OPEN/CLOSED/PENDING→2/9/1, 조인키 DSGN_DNTN_BIZ_ID).
    // 기간필터 frDt~toDt(yyyyMMdd)는 사업기간과 겹치면 포함.

    /** 요약: 총목표금액/총모금액/총달성율(%)/총기부건수(=참여건수). */
    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(TARGET_AMT), 0) AS tot_target_amt
             , COALESCE(SUM(CNTR_AMT),   0) AS tot_cntr_amt
             , CASE WHEN COALESCE(SUM(TARGET_AMT), 0) <> 0 THEN FLOOR(SUM(CNTR_AMT) * 10000.0 / SUM(TARGET_AMT)) / 100 ELSE 0 END AS tot_achv_rt
             , COALESCE(SUM(CNTR_CNT),   0) AS tot_cntr_cnt
        FROM (
            SELECT X.TARGET_AMT, X.CNTR_AMT, X.CNTR_CNT, X.PRJ_STATUS
            FROM (
                SELECT
                      P.GOAL_AMT AS TARGET_AMT
                    , COALESCE(C.CNTR_AMT, 0) AS CNTR_AMT
                    , COALESCE(C.CNT, 0) AS CNTR_CNT
                    , P.LCLGV_CD AS LOCGOV_CODE
                    , P.DSGN_DNTN_BIZ_SE_CD AS BSNS_TYPE
                    , CASE WHEN (CASE P.DSGN_DNTN_BIZ_STTS_CD WHEN 'OPEN' THEN '2' WHEN 'CLOSED' THEN '9' WHEN 'PENDING' THEN '1' ELSE P.DSGN_DNTN_BIZ_STTS_CD END) = '2'
                                THEN CASE WHEN COALESCE(C.CNTR_AMT, 0) > P.GOAL_AMT THEN '9'
                                          WHEN P.DSGN_DNTN_BIZ_END_YMD < to_char(now(), 'YYYYMMDD') THEN '9'
                                          ELSE '2' END
                           ELSE (CASE P.DSGN_DNTN_BIZ_STTS_CD WHEN 'OPEN' THEN '2' WHEN 'CLOSED' THEN '9' WHEN 'PENDING' THEN '1' ELSE P.DSGN_DNTN_BIZ_STTS_CD END)
                           END AS PRJ_STATUS
                FROM donation.G_DSGN_DNTN_BIZ_MNG P
                    LEFT OUTER JOIN (
                        SELECT SUM(CAST(CNTR_AMT AS BIGINT)) AS CNTR_AMT, COUNT(*) AS CNT, DSGN_DNTN_BIZ_ID
                        FROM donation.G_CNTR
                        WHERE CNTR_DE <= to_char(now(), 'YYYYMMDD') AND DELETE_AT = 'N' AND CNTR_STTUS_CODE = 'COMPLETED' AND DSGN_DNTN_BIZ_ID > 0
                        GROUP BY DSGN_DNTN_BIZ_ID
                    ) C ON P.DSGN_DNTN_BIZ_ID = C.DSGN_DNTN_BIZ_ID
                WHERE 1 = 1
                    AND ( (:shLocgovCode <> '' AND P.LCLGV_CD = :shLocgovCode)
                       OR (:shLocgovCode = '' AND :shWdr <> '' AND P.LCLGV_CD IN (SELECT LOCGOV_CODE FROM donation.G_LOCGOV WHERE UPPER_LOCGOV_CODE = :shWdr))
                       OR (:shLocgovCode = '' AND :shWdr = '') )
                    AND ( (:bsnsType = '0') OR (:bsnsType <> '0' AND P.DSGN_DNTN_BIZ_SE_CD = :bsnsType) )
                    AND ( (P.DSGN_DNTN_BIZ_BGNG_YMD BETWEEN :frDt AND :toDt)
                       OR (P.DSGN_DNTN_BIZ_END_YMD  BETWEEN :frDt AND :toDt)
                       OR (:frDt BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD)
                       OR (:toDt BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD) )
            ) X
        ) T_B
        WHERE ( (:prjStatus = '0') OR (:prjStatus <> '0' AND T_B.PRJ_STATUS = :prjStatus) )
        """)
    List<Object[]> selectLocgovSummary(@Param("shWdr") String shWdr, @Param("shLocgovCode") String shLocgovCode,
                                       @Param("bsnsType") String bsnsType, @Param("frDt") String frDt,
                                       @Param("toDt") String toDt, @Param("prjStatus") String prjStatus);

    /** 지자체별 목록(페이징은 호출측 메모리에서). 컬럼: locgovCode, locgovNm, prjCnt, statu2, statu9,
     *  totCntrCnt(기부건수), bsns100~400, targetAmt, cntrAmt, achvRt(%). locgovCode 오름차순. */
    @Query(nativeQuery = true, value = """
        SELECT T_B.LOCGOV_CODE AS locgov_code
             , CONCAT(MAX(G.UPPER_LOCGOV_NM), ' ', MAX(G.LOCGOV_NM)) AS locgov_nm
             , SUM(PRJ_CNT)           AS prj_cnt
             , SUM(STATU_2_CNT)       AS statu_2_cnt
             , SUM(STATU_9_CNT)       AS statu_9_cnt
             , SUM(CNTR_CNT)          AS tot_cntr_cnt
             , SUM(BSNS_TYPE_100_CNT) AS bsns_type_100_cnt
             , SUM(BSNS_TYPE_200_CNT) AS bsns_type_200_cnt
             , SUM(BSNS_TYPE_300_CNT) AS bsns_type_300_cnt
             , SUM(BSNS_TYPE_400_CNT) AS bsns_type_400_cnt
             , SUM(TARGET_AMT)        AS target_amt
             , SUM(CNTR_AMT)          AS cntr_amt
             , CASE WHEN SUM(TARGET_AMT) <> 0 THEN FLOOR(SUM(COALESCE(CNTR_AMT, 0)) * 10000.0 / SUM(TARGET_AMT)) / 100 ELSE 0 END AS achv_rt
        FROM (
            SELECT  X.PRJ_ID
                  , X.LOCGOV_CODE
                  , 1 AS PRJ_CNT
                  , CASE WHEN X.PRJ_STATUS = '2' THEN 1 ELSE 0 END AS STATU_2_CNT
                  , CASE WHEN X.PRJ_STATUS = '9' THEN 1 ELSE 0 END AS STATU_9_CNT
                  , CASE WHEN X.BSNS_TYPE = '100' THEN 1 ELSE 0 END AS BSNS_TYPE_100_CNT
                  , CASE WHEN X.BSNS_TYPE = '200' THEN 1 ELSE 0 END AS BSNS_TYPE_200_CNT
                  , CASE WHEN X.BSNS_TYPE = '300' THEN 1 ELSE 0 END AS BSNS_TYPE_300_CNT
                  , CASE WHEN X.BSNS_TYPE = '400' THEN 1 ELSE 0 END AS BSNS_TYPE_400_CNT
                  , X.TARGET_AMT
                  , X.CNTR_AMT
                  , X.CNTR_CNT
            FROM (
                SELECT
                      P.GOAL_AMT AS TARGET_AMT
                    , COALESCE(C.CNTR_AMT, 0) AS CNTR_AMT
                    , COALESCE(C.CNT, 0) AS CNTR_CNT
                    , P.DSGN_DNTN_BIZ_ID AS PRJ_ID
                    , P.LCLGV_CD AS LOCGOV_CODE
                    , P.DSGN_DNTN_BIZ_SE_CD AS BSNS_TYPE
                    , CASE WHEN (CASE P.DSGN_DNTN_BIZ_STTS_CD WHEN 'OPEN' THEN '2' WHEN 'CLOSED' THEN '9' WHEN 'PENDING' THEN '1' ELSE P.DSGN_DNTN_BIZ_STTS_CD END) = '2'
                                THEN CASE WHEN COALESCE(C.CNTR_AMT, 0) > P.GOAL_AMT THEN '9'
                                          WHEN P.DSGN_DNTN_BIZ_END_YMD < to_char(now(), 'YYYYMMDD') THEN '9'
                                          ELSE '2' END
                           ELSE (CASE P.DSGN_DNTN_BIZ_STTS_CD WHEN 'OPEN' THEN '2' WHEN 'CLOSED' THEN '9' WHEN 'PENDING' THEN '1' ELSE P.DSGN_DNTN_BIZ_STTS_CD END)
                           END AS PRJ_STATUS
                FROM donation.G_DSGN_DNTN_BIZ_MNG P
                    LEFT OUTER JOIN (
                        SELECT SUM(CAST(CNTR_AMT AS BIGINT)) AS CNTR_AMT, COUNT(*) AS CNT, DSGN_DNTN_BIZ_ID
                        FROM donation.G_CNTR
                        WHERE CNTR_DE <= to_char(now(), 'YYYYMMDD') AND DELETE_AT = 'N' AND CNTR_STTUS_CODE = 'COMPLETED' AND DSGN_DNTN_BIZ_ID > 0
                        GROUP BY DSGN_DNTN_BIZ_ID
                    ) C ON P.DSGN_DNTN_BIZ_ID = C.DSGN_DNTN_BIZ_ID
                WHERE 1 = 1
                    AND ( (:shLocgovCode <> '' AND P.LCLGV_CD = :shLocgovCode)
                       OR (:shLocgovCode = '' AND :shWdr <> '' AND P.LCLGV_CD IN (SELECT LOCGOV_CODE FROM donation.G_LOCGOV WHERE UPPER_LOCGOV_CODE = :shWdr))
                       OR (:shLocgovCode = '' AND :shWdr = '') )
                    AND ( (:bsnsType = '0') OR (:bsnsType <> '0' AND P.DSGN_DNTN_BIZ_SE_CD = :bsnsType) )
                    AND ( (P.DSGN_DNTN_BIZ_BGNG_YMD BETWEEN :frDt AND :toDt)
                       OR (P.DSGN_DNTN_BIZ_END_YMD  BETWEEN :frDt AND :toDt)
                       OR (:frDt BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD)
                       OR (:toDt BETWEEN P.DSGN_DNTN_BIZ_BGNG_YMD AND P.DSGN_DNTN_BIZ_END_YMD) )
            ) X
            WHERE ( (:prjStatus = '0') OR (:prjStatus <> '0' AND X.PRJ_STATUS = :prjStatus) )
        ) T_B
        JOIN donation.G_LOCGOV G ON G.LOCGOV_CODE = T_B.LOCGOV_CODE
        GROUP BY T_B.LOCGOV_CODE
        ORDER BY T_B.LOCGOV_CODE
        """)
    List<Object[]> selectLocgovList(@Param("shWdr") String shWdr, @Param("shLocgovCode") String shLocgovCode,
                                    @Param("bsnsType") String bsnsType, @Param("frDt") String frDt,
                                    @Param("toDt") String toDt, @Param("prjStatus") String prjStatus);
}

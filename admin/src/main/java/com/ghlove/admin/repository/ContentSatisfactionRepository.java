package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ContentSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 콘텐츠 만족도 조회(AS-IS CntntsStsfdgService 재현). 집계는 Postgres FILTER로 네이티브 처리.
 * AS-IS가 하드코딩으로 제외하던 URL(/donation/guide4.html, /donation/map-select.html)은 그대로 제외한다.
 */
public interface ContentSatisfactionRepository extends JpaRepository<ContentSatisfaction, Integer> {

    /** URL별 만족도 집계: [menu_url, menu_nm, 매우만족(4), 만족(3), 불만족(2), 매우불만족(1)]. */
    @Query(value = """
            SELECT menu_url, MAX(menu_nm) AS menu_nm,
                   COUNT(*) FILTER (WHERE stsfdg = 4) AS s4,
                   COUNT(*) FILTER (WHERE stsfdg = 3) AS s3,
                   COUNT(*) FILTER (WHERE stsfdg = 2) AS s2,
                   COUNT(*) FILTER (WHERE stsfdg = 1) AS s1
            FROM admin.g_stsfdg
            WHERE menu_url NOT IN ('/donation/guide4.html', '/donation/map-select.html')
              AND (:menuUrl = '' OR menu_url LIKE '%' || :menuUrl || '%')
            GROUP BY menu_url
            ORDER BY menu_url
            """, nativeQuery = true)
    List<Object[]> aggregateByUrl(@Param("menuUrl") String menuUrl);

    /** 한 URL의 (년도 선택 시 해당 년도) 합계: [매우만족, 만족, 불만족, 매우불만족, 합계, menu_nm]. */
    @Query(value = """
            SELECT COUNT(*) FILTER (WHERE stsfdg = 4) AS s4,
                   COUNT(*) FILTER (WHERE stsfdg = 3) AS s3,
                   COUNT(*) FILTER (WHERE stsfdg = 2) AS s2,
                   COUNT(*) FILTER (WHERE stsfdg = 1) AS s1,
                   COUNT(*) AS total,
                   MAX(menu_nm) AS menu_nm
            FROM admin.g_stsfdg
            WHERE menu_url = :menuUrl
              AND (:year = '' OR to_char(frst_regist_pnttm, 'YYYY') = :year)
            """, nativeQuery = true)
    List<Object[]> summaryOf(@Param("menuUrl") String menuUrl, @Param("year") String year);

    /** 한 URL의 월별 추이: [YYYY-MM, 매우만족, 만족, 불만족, 매우불만족]. */
    @Query(value = """
            SELECT to_char(frst_regist_pnttm, 'YYYY-MM') AS ym,
                   COUNT(*) FILTER (WHERE stsfdg = 4) AS s4,
                   COUNT(*) FILTER (WHERE stsfdg = 3) AS s3,
                   COUNT(*) FILTER (WHERE stsfdg = 2) AS s2,
                   COUNT(*) FILTER (WHERE stsfdg = 1) AS s1
            FROM admin.g_stsfdg
            WHERE menu_url = :menuUrl
              AND (:year = '' OR to_char(frst_regist_pnttm, 'YYYY') = :year)
            GROUP BY to_char(frst_regist_pnttm, 'YYYY-MM')
            ORDER BY ym
            """, nativeQuery = true)
    List<Object[]> monthlyOf(@Param("menuUrl") String menuUrl, @Param("year") String year);

    /** 한 URL에 데이터가 존재하는 년도 목록(내림차순). */
    @Query(value = """
            SELECT DISTINCT to_char(frst_regist_pnttm, 'YYYY') AS yyyy
            FROM admin.g_stsfdg WHERE menu_url = :menuUrl ORDER BY yyyy DESC
            """, nativeQuery = true)
    List<String> yearsOf(@Param("menuUrl") String menuUrl);
}

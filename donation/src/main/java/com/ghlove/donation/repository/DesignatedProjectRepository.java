package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DesignatedProjectRepository extends JpaRepository<DesignatedProject, Long> {
    List<DesignatedProject> findByRlsYnAndDsgnDntnBizSttsCdOrderByDsgnDntnBizIdDesc(String rlsYn, String sttsCd);

    /**
     * AS-IS {@code designated-donation-mapper.selectDesinatedDonationGoalAmt} - 진행중(AS-IS '2' = TO-BE OPEN)·공개 사업 중
     * ① 같은 지자체로 수납완료(AS-IS '200' = COMPLETED)된 기부 합계가 목표금액 이상이거나 ② 모금 종료일이 지난 것.
     * AS-IS가 UNION ALL이라 두 조건에 다 걸리면 같은 ID가 두 번 나온다(UPDATE를 두 번 할 뿐 결과는 같다).
     */
    @Query(nativeQuery = true, value = """
        SELECT b.DSGN_DNTN_BIZ_ID
          FROM donation.G_DSGN_DNTN_BIZ_MNG b
          JOIN donation.G_CNTR c ON c.DSGN_DNTN_BIZ_ID = b.DSGN_DNTN_BIZ_ID
         WHERE b.DSGN_DNTN_BIZ_STTS_CD = :open
           AND c.CNTR_STTUS_CODE = 'COMPLETED'
           AND c.CNTR_LOCGOV_CODE = b.LCLGV_CD
           AND b.RLS_YN = 'Y'
           AND c.DELETE_AT = 'N'
         GROUP BY b.DSGN_DNTN_BIZ_ID, b.GOAL_AMT
        HAVING SUM(c.CNTR_AMT) >= b.GOAL_AMT
        UNION ALL
        SELECT b.DSGN_DNTN_BIZ_ID
          FROM donation.G_DSGN_DNTN_BIZ_MNG b
         WHERE b.DSGN_DNTN_BIZ_STTS_CD = :open
           AND b.DSGN_DNTN_BIZ_END_YMD < :today
           AND b.RLS_YN = 'Y'
        """)
    List<Number> findGoalReachedOrEnded(@Param("open") String open, @Param("today") String today);

    /** AS-IS {@code updateDesinatedDonationStatus} - 상태만 종료(AS-IS '9' = TO-BE CLOSED)로 바꾼다(이력은 남기지 않는다). */
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE donation.G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_STTS_CD = :closed "
            + "WHERE DSGN_DNTN_BIZ_ID = :id")
    int updateStatus(@Param("id") Long id, @Param("closed") String closed);
}

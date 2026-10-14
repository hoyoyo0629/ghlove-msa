package com.ghlove.donation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 기탁서 등록(admin 메뉴 15102, AS-IS opmanager/offgive/form.jsp)이 화면에서 부르는 조회들 -
 * AS-IS 매퍼 SQL을 그대로 옮긴 <b>조회 전용</b> 레포지토리.
 *
 * <ul>
 *   <li>{@link #locgovOptions} - give-state-mapper {@code getLocgovCodeList}</li>
 *   <li>{@link #cntrLmtt} - ngdonation-mapper {@code getCntrLmtt}</li>
 *   <li>{@link #locgovMapngCode} - offgive-mapper {@code getLocgovMapngCode}</li>
 *   <li>{@link #designatedDonationList} - designated-donation-mapper {@code selectDesignatedDonationList}
 *       (display=FRONT, prjStatus=2, conditionType=DONATION)</li>
 * </ul>
 *
 * <p><b>TO-BE 번역</b>: 지정기부 상태는 AS-IS 숫자({@code 2} 모금중)가 아니라 이 프로젝트 낱말
 * ({@code OPEN})이고, 수납완료 기부는 {@code '200'}이 아니라 {@code COMPLETED}다. 정렬의 NULL
 * 위치는 CUBRID 기본값(ASC=앞, DESC=뒤)에 맞춰 명시한다.
 */
@Repository
@RequiredArgsConstructor
public class OffgiveFormRepository {

    private final EntityManager entityManager;

    /** AS-IS getLocgovCodeList - 시도(UPPER_LOCGOV_CODE)의 사용중 시군구, ORDERING 순. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> locgovOptions(String upperLocgovCode) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT locgov_code, locgov_nm
                  FROM donation.g_locgov
                 WHERE upper_locgov_code = :code
                   AND use_at = 'Y'
                 ORDER BY ordering NULLS FIRST
                """)
                .setParameter("code", upperLocgovCode)
                .getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("LOCGOV_CODE", r[0]);
            m.put("LOCGOV_NM", r[1]);
            result.add(m);
        }
        return result;
    }

    /** AS-IS getCntrLmtt - 오늘 기부제한 기간에 걸린 지자체면 제한기간/사유, 아니면 null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> cntrLmtt(String cityCode) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT to_char(to_date(lmtt_bgn_de, 'YYYYMMDD'), 'YYYY.MM.DD') AS lmtt_bgn_de
                     , to_char(to_date(lmtt_end_de, 'YYYYMMDD'), 'YYYY.MM.DD') AS lmtt_end_de
                     , violt_resn_cn
                  FROM donation.g_cntr_lmtt
                 WHERE locgov_code = :cityCode
                   AND lmtt_bgn_de <= to_char(current_date, 'YYYYMMDD')
                   AND lmtt_end_de >= to_char(current_date, 'YYYYMMDD')
                """)
                .setParameter("cityCode", cityCode)
                .getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lmttBgnDe", r[0]);
        m.put("lmttEndDe", r[1]);
        m.put("violtResnCn", r[2]);
        return m;
    }

    /** AS-IS getLocgovMapngCode - 행정기관코드(5자리)를 고향사랑 지자체코드로 매핑, 없으면 null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> locgovMapngCode(String locgovCode) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT (SELECT upper_locgov_code FROM donation.g_locgov WHERE locgov_code = i.locgov_mapng_code)
                     , i.locgov_mapng_code
                     , (SELECT locgov_nm FROM donation.g_locgov WHERE locgov_code = i.locgov_mapng_code)
                  FROM donation.g_instt_code i
                 WHERE i.locgov_code = :locgovCode
                """)
                .setParameter("locgovCode", locgovCode)
                .getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("upper_locgov_mapng_code", r[0]);
        m.put("locgov_mapng_code", r[1]);
        m.put("locgov_nm", r[2]);
        return m;
    }

    /** selectDesignatedDonationList 한 행 - 기탁서 화면/지정기부목록 엑셀이 쓰는 컬럼만. */
    public record DesignatedRow(Long prjId, String prjSubject, String locgovNm, String bsnsType,
                                String bsnsSubType, String prjStDt, String prjEdDt, Long targetAmt, Long sumAmt) {
    }

    /**
     * AS-IS selectDesignatedDonationList(display=FRONT, prjStatus=2, conditionType=DONATION).
     * 모금중(OPEN)이라도 모금액이 목표를 넘었거나 종료일이 지났으면 종료로 보고 빼고, 오늘이
     * 기간 안이며 공개(RLS_YN=Y)인 사업만 남긴다. {@code sort}가 BSNS면 사업부문 순(지정기부목록
     * 엑셀), 아니면 등록일 최신순(기탁서 화면 사업 선택).
     *
     * @param locgovCode 비면 전 지자체
     */
    @SuppressWarnings("unchecked")
    public List<DesignatedRow> designatedDonationList(String locgovCode, String sort) {
        boolean byLocgov = locgovCode != null && !locgovCode.isBlank();
        String orderBy = "BSNS".equals(sort) ? "t.bsns_sub_type NULLS FIRST" : "t.frst_reg_dt DESC NULLS LAST";
        String sql = """
                SELECT t.prj_id, t.prj_subject, t.locgov_nm, t.bsns_type, t.bsns_sub_type,
                       t.prj_st_dt, t.prj_ed_dt, t.target_amt, t.sum_amt
                  FROM (
                        SELECT p.dsgn_dntn_biz_id AS prj_id
                             , p.dsgn_dntn_biz_ttl AS prj_subject
                             , concat(g.upper_locgov_nm, ' ', g.locgov_nm) AS locgov_nm
                             , p.dsgn_dntn_biz_se_cd AS bsns_type
                             , p.dsgn_dntn_biz_se_dtl_cd AS bsns_sub_type
                             , p.dsgn_dntn_biz_bgng_ymd AS prj_st_dt
                             , p.dsgn_dntn_biz_end_ymd AS prj_ed_dt
                             , p.goal_amt AS target_amt
                             , coalesce(b.cntr_amt, 0) AS sum_amt
                             , p.frst_reg_dt
                             , p.rls_yn
                             , CASE WHEN p.dsgn_dntn_biz_stts_cd = 'OPEN'
                                         AND (coalesce(b.cntr_amt, 0) > p.goal_amt
                                              OR p.dsgn_dntn_biz_end_ymd < to_char(current_date, 'YYYYMMDD'))
                                    THEN 'ENDED' ELSE p.dsgn_dntn_biz_stts_cd END AS prj_status
                          FROM donation.g_dsgn_dntn_biz_mng p
                          JOIN donation.g_locgov g ON g.locgov_code = p.lclgv_cd
                          LEFT JOIN (
                                SELECT c.dsgn_dntn_biz_id, sum(c.cntr_amt) AS cntr_amt
                                  FROM donation.g_cntr c
                                 WHERE c.dsgn_dntn_biz_id > 0
                                   AND c.cntr_de <= to_char(current_date, 'YYYYMMDD')
                                   AND c.delete_at = 'N'
                                   AND c.cntr_sttus_code = 'COMPLETED'
                                 GROUP BY c.dsgn_dntn_biz_id
                          ) b ON b.dsgn_dntn_biz_id = p.dsgn_dntn_biz_id
                         WHERE 1 = 1
                """ + (byLocgov ? "   AND p.lclgv_cd = :locgovCode\n" : "") + """
                  ) t
                 WHERE t.prj_status = 'OPEN'
                   AND to_char(current_date, 'YYYYMMDD') BETWEEN t.prj_st_dt AND t.prj_ed_dt
                   AND t.rls_yn = 'Y'
                 ORDER BY
                """ + orderBy;
        var query = entityManager.createNativeQuery(sql);
        if (byLocgov) {
            query.setParameter("locgovCode", locgovCode);
        }
        List<Object[]> rows = query.getResultList();
        List<DesignatedRow> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new DesignatedRow(toLong(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
                    (String) r[5], (String) r[6], toLong(r[7]), toLong(r[8])));
        }
        return result;
    }

    /**
     * AS-IS selectDesignatedDonationDetail(checkDonation=true, 관리자 화면) + {@code DesignatedDonation.getResultCode()}
     * - 부과요청 전에 특정사업이 지금 기부 가능한지 본다. 오늘이 사업기간 밖이면 행이 없어 null
     * (AS-IS "현재 기부 가능한 특정사업에 기부하기 사업이 아닙니다."), 있으면 resultCode:
     * 모금중(OPEN)이 아니면 STATUS, 모금률 100% 이상이면 ATTAINMENT, 아니면 SUCCESS.
     * 모금액은 AS-IS처럼 수납완료(TO-BE COMPLETED)·삭제 아님 기부의 합계이고, 모금률은
     * {@code FLOOR(모금액 * 10000.0 / 목표액) / 100}.
     */
    @SuppressWarnings("unchecked")
    public String designatedDonationResultCode(long prjId) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT p.dsgn_dntn_biz_stts_cd
                     , p.goal_amt
                     , coalesce((SELECT sum(c.cntr_amt)
                                   FROM donation.g_cntr c
                                  WHERE c.delete_at = 'N'
                                    AND c.cntr_sttus_code = 'COMPLETED'
                                    AND c.dsgn_dntn_biz_id = p.dsgn_dntn_biz_id), 0)
                  FROM donation.g_dsgn_dntn_biz_mng p
                 WHERE p.dsgn_dntn_biz_id = :prjId
                   AND to_char(current_date, 'YYYYMMDD') BETWEEN p.dsgn_dntn_biz_bgng_ymd AND p.dsgn_dntn_biz_end_ymd
                """)
                .setParameter("prjId", prjId)
                .getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        long goal = r[1] == null ? 0 : ((Number) r[1]).longValue();
        double cntrAmt = r[2] == null ? 0 : ((Number) r[2]).doubleValue();
        // AS-IS PRJ_STATUS: 모금중('2')이라도 모금액이 목표액을 넘으면 '9'(종료)로 바꿔 내려 STATUS가 된다
        if (!"OPEN".equals(r[0]) || cntrAmt > goal) {
            return "STATUS";
        }
        double rateAmt = goal > 0 ? Math.floor(cntrAmt * 10000.0 / goal) / 100 : 0;
        return rateAmt >= 100f ? "ATTAINMENT" : "SUCCESS";
    }

    /** AS-IS selectLocgovByAdmCd - 행정기관코드 앞 5자리로 고향사랑 지자체코드(G_ADM_LOCGOV), 없으면 null. */
    @SuppressWarnings("unchecked")
    public String locgovCodeByAdmCd(String admCd5) {
        List<Object> rows = entityManager.createNativeQuery("""
                SELECT locgov_code
                  FROM donation.g_adm_locgov
                 WHERE adm_cd LIKE concat(:admCd, '%')
                 LIMIT 1
                """)
                .setParameter("admCd", admCd5)
                .getResultList();
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toString();
    }

    private static Long toLong(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }
}

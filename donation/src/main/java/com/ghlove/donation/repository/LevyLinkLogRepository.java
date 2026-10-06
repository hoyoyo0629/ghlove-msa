package com.ghlove.donation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 연계 로그 조회 (admin 메뉴 1413~1416) - AS-IS {@code ngdonation-mapper.xml}의 네 쿼리를
 * 그대로 옮긴 조회 전용 레포지토리다.
 *
 * <ul>
 *   <li>1413 서울세외 부과연계 로그 - {@code selectGifSeoulBugaList} / {@code gif_seoul}</li>
 *   <li>1414 서울 수납연계 로그 - {@code selectGifSeoulSunapList} / {@code gif_etax_sunap}</li>
 *   <li>1415 지방세외 부과연계 로그 - {@code selectNextBugaLogList} / {@code g_next_buga_request}
 *       + {@code g_locgov} LEFT JOIN(sgb_cd = administ_instt_code)</li>
 *   <li>1416 지방세외 수납연계 로그 - {@code selectNextSunapLogList} / {@code g_next_sunap_response}</li>
 * </ul>
 *
 * 네 표 모두 기본키가 없거나 조회 전용이라 엔티티를 만들지 않고 네이티브 쿼리로만 읽는다.
 * 검색조건·정렬·날짜 비교방식(부과/수납은 yyyyMMdd 문자열 비교, 서울은 연계시작일시 타임스탬프
 * 범위)까지 AS-IS 그대로다.
 */
@Repository
@RequiredArgsConstructor
public class LevyLinkLogRepository {

    private final EntityManager entityManager;

    /* ---------------- 1413 서울세외 부과연계 로그 (gif_seoul) ---------------- */

    /** AS-IS 화면 11컬럼. */
    public record SeoulBugaRow(String enapbuNo, String siguCd, String semokCd, String taxYm,
                               String taxGubun, String sidoCd, String napGubun, Long taxAmt,
                               String errorCd, String errorMsg, String ifStDt) {
    }

    public List<SeoulBugaRow> seoulBugaList(String srchTxt, String srchErrorCd,
                                            LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
                select a.enapbu_no, a.sigu_cd, a.semok_cd, a.tax_ym, a.tax_gubun, a.sido_cd,
                       a.nap_gubun, a.tax_amt, a.error_cd, a.error_msg, a.if_st_dt
                  from donation.gif_seoul a
                 where a.if_st_dt between :fromDate and :toDate
                """);
        if (notBlank(srchTxt)) {
            // AS-IS는 전자납부번호를 완전일치로 본다
            sql.append("   and a.enapbu_no = :srchTxt\n");
        }
        if (notBlank(srchErrorCd)) {
            sql.append("SUCCESS".equals(srchErrorCd)
                    ? "   and a.error_cd = '0'\n" : "   and a.error_cd <> '0'\n");
        }
        sql.append(" order by a.if_st_dt desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        q.setParameter("fromDate", from);
        q.setParameter("toDate", to);
        if (notBlank(srchTxt)) {
            q.setParameter("srchTxt", srchTxt);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new SeoulBugaRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]),
                        str(r[5]), str(r[6]), lng(r[7]), str(r[8]), str(r[9]), timestamp(r[10])))
                .toList();
    }

    /* ---------------- 1414 서울 수납연계 로그 (gif_etax_sunap) ---------------- */

    /** AS-IS 화면 9컬럼. */
    public record SeoulSunapRow(String epayNo, String comReqMeche, String orgC, String sunapYn,
                                Long sunapAmt, String sunapDt, String rstCd, String rstMsg,
                                String ifStDt) {
    }

    public List<SeoulSunapRow> seoulSunapList(String srchTxt, String srchRstCd,
                                              LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
                select a.epay_no, a.com_req_meche, a.org_c, a.sunap_yn, a.sunap_amt,
                       a.sunap_dt, a.rst_cd, a.rst_msg, a.if_st_dt
                  from donation.gif_etax_sunap a
                 where a.if_st_dt between :fromDate and :toDate
                """);
        if (notBlank(srchTxt)) {
            sql.append("   and a.epay_no = :srchTxt\n");
        }
        if (notBlank(srchRstCd)) {
            sql.append("SUCCESS".equals(srchRstCd)
                    ? "   and a.sunap_yn = 'Y'\n" : "   and a.sunap_yn <> 'Y'\n");
        }
        sql.append(" order by a.if_st_dt desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        q.setParameter("fromDate", from);
        q.setParameter("toDate", to);
        if (notBlank(srchTxt)) {
            q.setParameter("srchTxt", srchTxt);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new SeoulSunapRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), lng(r[4]),
                        str(r[5]), str(r[6]), str(r[7]), timestamp(r[8])))
                .toList();
    }

    /* ------------- 1415 지방세외 부과연계 로그 (g_next_buga_request) ------------- */

    /** AS-IS 화면 34컬럼(앞 2개는 g_locgov 조인). */
    public record StndBugaRow(String upperLocgovNm, String locgovNm, String linkMngKey, String sgbCd,
                              String linkTrgtCd, String dptCd, String spclFisBizCd, String fyr,
                              String actSeCd, String rprsTxmCd, String operItemCd, String lvyYmd,
                              String frstPctAmt, String frstPidYmd, String pyrSeCd, String pyrNo,
                              String pyrNm, String rprsPyrNo, String rprsPyrNm, String pyrSttCd,
                              String lotnoRoadAddrSeCd, String zip, String roadNmCd, String bmno,
                              String bsno, String stdgCd, String dongCd, String roadNmDaddr,
                              String glNm, String mngItemCn1, String bugaStatusCd, String linkRstCd,
                              String linkRstMsg, String frstRegistPnttm) {
    }

    public List<StndBugaRow> stndBugaList(String startDate, String endDate, String bugaStatusCd,
                                          String linkRstCd, String epayNo, String srchlinkRstYn,
                                          String srchpyrNm) {
        StringBuilder sql = new StringBuilder("""
                select gov.upper_locgov_nm, gov.locgov_nm, buga.link_mng_key, buga.sgb_cd,
                       buga.link_trgt_cd, buga.dpt_cd, buga.spcl_fis_biz_cd, buga.fyr,
                       buga.act_se_cd, buga.rprs_txm_cd, buga.oper_item_cd, buga.lvy_ymd,
                       buga.frst_pct_amt, buga.frst_pid_ymd, buga.pyr_se_cd, buga.pyr_no,
                       buga.pyr_nm, buga.rprs_pyr_no, buga.rprs_pyr_nm, buga.pyr_stt_cd,
                       buga.lotno_road_addr_se_cd, buga.zip, buga.road_nm_cd, buga.bmno,
                       buga.bsno, buga.stdg_cd, buga.dong_cd, buga.road_nm_daddr,
                       buga.gl_nm, buga.mng_item_cn1, buga.buga_status_cd, buga.link_rst_cd,
                       buga.link_rst_msg, buga.frst_regist_pnttm
                  from donation.g_next_buga_request buga
                  left join donation.g_locgov gov on buga.sgb_cd = gov.administ_instt_code
                 where 1 = 1
                """);
        // AS-IS는 시작일·종료일이 둘 다 있을 때만 부과일자 범위를 건다
        if (notBlank(startDate) && notBlank(endDate)) {
            sql.append("   and buga.lvy_ymd >= :startDate and buga.lvy_ymd <= :endDate\n");
        }
        if (notBlank(bugaStatusCd)) {
            sql.append("   and buga.buga_status_cd = :bugaStatusCd\n");
        }
        if (notBlank(linkRstCd)) {
            sql.append("   and buga.link_rst_cd = :linkRstCd\n");
        }
        if (notBlank(epayNo)) {
            // AS-IS: 전자납부번호는 연계결과메시지 안에서 찾는다(LIKE)
            sql.append("   and buga.link_rst_msg like concat('%', :epayNo, '%')\n");
        }
        if ("Y".equals(srchlinkRstYn)) {
            sql.append("   and buga.link_rst_cd = '000'\n");
        } else if ("N".equals(srchlinkRstYn)) {
            sql.append("   and (buga.link_rst_cd <> '000' or buga.link_rst_cd is null)\n");
        }
        if (notBlank(srchpyrNm)) {
            sql.append("   and buga.pyr_nm = :srchpyrNm\n");
        }
        sql.append(" order by buga.frst_regist_pnttm desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        if (notBlank(startDate) && notBlank(endDate)) {
            q.setParameter("startDate", startDate);
            q.setParameter("endDate", endDate);
        }
        if (notBlank(bugaStatusCd)) {
            q.setParameter("bugaStatusCd", bugaStatusCd);
        }
        if (notBlank(linkRstCd)) {
            q.setParameter("linkRstCd", linkRstCd);
        }
        if (notBlank(epayNo)) {
            q.setParameter("epayNo", epayNo);
        }
        if (notBlank(srchpyrNm)) {
            q.setParameter("srchpyrNm", srchpyrNm);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new StndBugaRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]),
                        str(r[5]), str(r[6]), str(r[7]), str(r[8]), str(r[9]), str(r[10]), str(r[11]),
                        str(r[12]), str(r[13]), str(r[14]), str(r[15]), str(r[16]), str(r[17]),
                        str(r[18]), str(r[19]), str(r[20]), str(r[21]), str(r[22]), str(r[23]),
                        str(r[24]), str(r[25]), str(r[26]), str(r[27]), str(r[28]), str(r[29]),
                        str(r[30]), str(r[31]), str(r[32]), timestamp(r[33])))
                .toList();
    }

    /* ------------ 1416 지방세외 수납연계 로그 (g_next_sunap_response) ------------ */

    /** AS-IS 화면 37컬럼 - 표 컬럼 순서 그대로다. */
    public record StndSunapRow(String linkMngKey, String sgbCd, String sgbNm, String taxnNo,
                               String untyTaxnNo, String dptCd, String dptNm, String spclFisBizCd,
                               String spclFisBizNm, String fyr, String actSeCd, String actSeNm,
                               String rprsTxmCd, String rprsTxmNm, String operItemCd, String operItemNm,
                               String lvyNo, String itmNo, String epayNo, String rcvmtNo,
                               String rcvmtSeCd, String rcvmtSeNm, String rcvmtYmd, String actYmd,
                               String tsfYmd, String rcvmtPctAmt, String rcvmtAdtnAmt,
                               String rcvmtIntrAmt, String bankNm, String rcvmtTyCd, String rcvmtTy,
                               String rsveItem1, String rsveItem2, String rsveItem3, String rsveItem4,
                               String rsveItem5, String frstRegistPnttm) {
    }

    public List<StndSunapRow> stndSunapList(String startDate, String endDate, String epayNo) {
        StringBuilder sql = new StringBuilder("""
                select link_mng_key, sgb_cd, sgb_nm, taxn_no, unty_taxn_no, dpt_cd, dpt_nm,
                       spcl_fis_biz_cd, spcl_fis_biz_nm, fyr, act_se_cd, act_se_nm,
                       rprs_txm_cd, rprs_txm_nm, oper_item_cd, oper_item_nm, lvy_no, itm_no,
                       epay_no, rcvmt_no, rcvmt_se_cd, rcvmt_se_nm, rcvmt_ymd, act_ymd, tsf_ymd,
                       rcvmt_pct_amt, rcvmt_adtn_amt, rcvmt_intr_amt, bank_nm, rcvmt_ty_cd, rcvmt_ty,
                       rsve_item1, rsve_item2, rsve_item3, rsve_item4, rsve_item5, frst_regist_pnttm
                  from donation.g_next_sunap_response
                 where 1 = 1
                """);
        if (notBlank(startDate) && notBlank(endDate)) {
            sql.append("   and rcvmt_ymd >= :startDate and rcvmt_ymd <= :endDate\n");
        }
        if (notBlank(epayNo)) {
            sql.append("   and epay_no = :epayNo\n");
        }
        sql.append(" order by frst_regist_pnttm desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        if (notBlank(startDate) && notBlank(endDate)) {
            q.setParameter("startDate", startDate);
            q.setParameter("endDate", endDate);
        }
        if (notBlank(epayNo)) {
            q.setParameter("epayNo", epayNo);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new StndSunapRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]),
                        str(r[5]), str(r[6]), str(r[7]), str(r[8]), str(r[9]), str(r[10]), str(r[11]),
                        str(r[12]), str(r[13]), str(r[14]), str(r[15]), str(r[16]), str(r[17]),
                        str(r[18]), str(r[19]), str(r[20]), str(r[21]), str(r[22]), str(r[23]),
                        str(r[24]), str(r[25]), str(r[26]), str(r[27]), str(r[28]), str(r[29]),
                        str(r[30]), str(r[31]), str(r[32]), str(r[33]), str(r[34]), str(r[35]),
                        timestamp(r[36])))
                .toList();
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static Long lng(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    /** 화면은 연계시작일시/생성일시를 그대로 찍는다(AS-IS도 포맷 없이 출력). */
    private static String timestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toString().replace('T', ' ');
        }
        return value.toString();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}

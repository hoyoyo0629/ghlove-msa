package com.ghlove.donation.service;

import com.ghlove.donation.domain.Donation;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * donation 배치 본문(AS-IS ghlove-batch JobServiceImpl·NgDonationBatchServiceImpl 중 새로 옮긴 것) - SQL은 AS-IS 매퍼 그대로,
 * 코드값만 TO-BE로 치환(CNTR_STTUS_CODE '200' → 'COMPLETED').
 */
@Service
@RequiredArgsConstructor
public class DonationBatchService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** AS-IS DATE_FORMAT(SYS_DATETIME, '%Y%m%d%H%i%s%f') - %f는 밀리초 3자리. */
    private static final DateTimeFormatter CREATED_AT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final EntityManager entityManager;
    private final DonationService donationService;
    private final CntrReqmngService cntrReqmngService;

    /**
     * dailyGramtInsertBatch(매일 총 기부금) - AS-IS main-mapper {@code getGiveTotalAmt}("prev"/"now") + {@code insertDyGramtInfo}.
     * 작년: 작년 1월 1일 ~ 1년 전 오늘 전날 / 올해: 올해 1월 1일 ~ 어제. 수납완료 기부만, 삭제여부는 보지 않는다(AS-IS 그대로).
     * AS-IS 화면은 이제 이 표를 읽지 않는다(storefront 총 기부금은 그때그때 계산) - 적재만 AS-IS대로 남긴다.
     */
    @Transactional
    public String insertDailyGramt() {
        LocalDate today = LocalDate.now();
        Long lastYear = sum(today.withDayOfYear(1).minusYears(1), today.minusYears(1));
        Long nowYear = sum(today.withDayOfYear(1), today);
        entityManager.createNativeQuery("insert into donation.g_dy_gramt (created_at, stdr_de, last_year_gramt, now_year_gramt) "
                        + "values (:createdAt, :stdrDe, :lastYear, :nowYear)")
                .setParameter("createdAt", LocalDateTime.now().format(CREATED_AT))
                .setParameter("stdrDe", today.format(YMD))
                .setParameter("lastYear", lastYear)
                .setParameter("nowYear", nowYear)
                .executeUpdate();
        return "작년 " + lastYear + " / 올해 " + nowYear;
    }

    /** 수납확인 대상 - AS-IS GiveStateTest 중 배치가 쓰는 칸. */
    public record SunapTarget(String enapbuNo, String cntrSn, String cntrDe, String seoulTrgetAt,
                              String cntrLocgovCode, Long userId) {
    }

    /**
     * AS-IS {@code getCntrNonSunapFiveMinList}(5분) / {@code getCntrNonSunapAlldayList}(일정산) -
     * 신고(AS-IS '100' = REQUESTED)·지자체 있음, 5분은 아직 확인 안 한(SUNAP_PROCSS_YN='N') 어제 이후 기부 200건,
     * 일정산은 확인여부와 무관하게 7일 전 이후 전부. 기부일자 내림차순.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<SunapTarget> sunapTargets(boolean fiveMinute) {
        LocalDate today = LocalDate.now();
        String sql = "select elctrn_pay_no, cntr_sn, cntr_de, seoul_trget_at, cntr_locgov_code, user_id from donation.g_cntr "
                + "where cntr_sttus_code = 'REQUESTED' and cntr_locgov_code is not null "
                + (fiveMinute ? "and sunap_procss_yn = 'N' " : "")
                + "and cntr_de >= :from order by cntr_de desc" + (fiveMinute ? " limit 200" : "");
        java.util.List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("from", today.minusDays(fiveMinute ? 1 : 7).format(YMD))
                .getResultList();
        return rows.stream().map(r -> new SunapTarget((String) r[0], (String) r[1], (String) r[2], (String) r[3],
                (String) r[4], r[5] == null ? null : ((Number) r[5]).longValue())).toList();
    }

    /**
     * 수납확인 성공 처리(AS-IS {@code extractedSunapSuccess} → 변경신청 300 등록·승인): 서울 외면 차세대 수납정보
     * (G_NEXT_SUNAP_RESPONSE)가 없을 때 부과요청(G_NEXT_BUGA_REQUEST) 기준으로 "시스템 수납처리" 행을 넣고
     * (AS-IS insertGiveSunapInfo), 변경신청 이력을 남기고, 기부를 수납완료로 바꾼다.
     */
    @Transactional
    public void processSunapSuccess(SunapTarget target, String loginId, String userName) {
        if (!"Y".equals(target.seoulTrgetAt())) {
            Number cnt = (Number) entityManager.createNativeQuery(
                            "select count(*) from donation.g_next_sunap_response where link_mng_key = :cntrSn")
                    .setParameter("cntrSn", target.cntrSn()).getSingleResult();
            if (cnt.longValue() == 0) {
                entityManager.createNativeQuery("""
                        insert into donation.g_next_sunap_response (link_mng_key, sgb_cd, sgb_nm, taxn_no, unty_taxn_no,
                            dpt_cd, dpt_nm, spcl_fis_biz_cd, spcl_fis_biz_nm, fyr, act_se_cd, act_se_nm, rprs_txm_cd,
                            rprs_txm_nm, oper_item_cd, oper_item_nm, lvy_no, itm_no, epay_no, rcvmt_no, rcvmt_se_cd,
                            rcvmt_se_nm, rcvmt_ymd, act_ymd, tsf_ymd, rcvmt_pct_amt, rcvmt_adtn_amt, rcvmt_intr_amt,
                            bank_nm, rcvmt_ty_cd, rcvmt_ty, rsve_item1, rsve_item2, rsve_item3, rsve_item4, rsve_item5,
                            frst_regist_pnttm)
                        select gc.cntr_sn, br.sgb_cd, concat(loc.upper_locgov_nm, ' ', loc.locgov_nm), null, null,
                            br.dpt_cd, loc.charger_psitn_dept, br.spcl_fis_biz_cd, null, br.fyr, br.act_se_cd,
                            case when br.act_se_cd = '61' then '특별회계(시군구사업외수입)'
                                 when br.act_se_cd = '41' then '일반회계(시군구세외)'
                                 when br.act_se_cd = '51' then '특별회계(시도사업외수입)'
                                 when br.act_se_cd = '31' then '일반회계(시도세외)' else null end,
                            br.rprs_txm_cd, '고향사랑기부금', br.oper_item_cd, '고향사랑기부금', null, '00', gc.elctrn_pay_no,
                            '01', '01', '완납', gc.cntr_de, gc.cntr_de, null, cast(gc.cntr_amt as varchar), '0', '0',
                            null, null, null, null, null, null, null, '시스템 수납처리', now()
                          from donation.g_cntr gc
                          left join donation.g_locgov loc on gc.cntr_locgov_code = loc.locgov_code
                          left join donation.g_next_buga_request br on br.link_mng_key = gc.cntr_sn
                         where gc.cntr_sn = :cntrSn
                        """).setParameter("cntrSn", target.cntrSn()).executeUpdate();
            }
        }
        Donation donation = donationService.completeBySunapCheck(target.cntrSn());
        cntrReqmngService.recordBatchSunap(donation, loginId, userName);
    }

    /** AS-IS {@code updateSunapBatchProcessById} - 확인한 대상은 결과와 무관하게 SUNAP_PROCSS_YN='Y'(5분 배치가 다시 보지 않는다). */
    @Transactional
    public void markSunapChecked(SunapTarget target) {
        entityManager.createNativeQuery("update donation.g_cntr set last_updt_pnttm = :now, sunap_procss_yn = 'Y' "
                        + "where elctrn_pay_no = :epayNo and user_id = :userId")
                .setParameter("now", LocalDateTime.now())
                .setParameter("epayNo", target.enapbuNo())
                .setParameter("userId", target.userId())
                .executeUpdate();
    }

    /** 농협 연계 행 - AS-IS selectUserCntrForNhList / selectUserCntrLocgovForNhList(지자체는 회원별 집계에서 null). */
    public record NhUserCntr(Long userId, String sttemntPayDe, String cntrLocgovCode, long cntrAmt) {
    }

    /**
     * 농협 연계 배치(userCntrForNhBatch·userCntrLocForNhBatch) 원천 - AS-IS ngdonation-mapper: 삭제 안 된 수납완료 기부를
     * 수납일이 어제(포함) 이전인 것만 회원·수납일(·기부지자체)로 묶은 합계. 회원 CI는 member 소유라 admin이 붙인다
     * (AS-IS INNER JOIN op_user ... mber_ci IS NOT NULL - CI 없는 회원은 admin이 버린다).
     */
    @SuppressWarnings("unchecked")
    public java.util.List<NhUserCntr> nhUserCntr(boolean byLocgov) {
        String until = LocalDate.now().minusDays(1).format(YMD);
        String groupBy = byLocgov ? "user_id, sttemnt_pay_de, cntr_locgov_code" : "user_id, sttemnt_pay_de";
        java.util.List<Object[]> rows = entityManager.createNativeQuery("select user_id, sttemnt_pay_de, "
                        + (byLocgov ? "cntr_locgov_code" : "null") + ", coalesce(sum(cntr_amt), 0) from donation.g_cntr "
                        + "where delete_at = 'N' and sttemnt_pay_de is not null and cntr_sttus_code = 'COMPLETED' "
                        + "and sttemnt_pay_de <= :until group by " + groupBy)
                .setParameter("until", until)
                .getResultList();
        return rows.stream().map(r -> new NhUserCntr(r[0] == null ? null : ((Number) r[0]).longValue(), (String) r[1],
                (String) r[2], ((Number) r[3]).longValue())).toList();
    }

    /** 농협 연계 배치(locForNhBatch) 원천 - AS-IS selectLocgovForNhList: 사용 중 지자체. */
    public record NhLocgov(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }

    @SuppressWarnings("unchecked")
    public java.util.List<NhLocgov> nhLocgovs() {
        java.util.List<Object[]> rows = entityManager.createNativeQuery("select locgov_code, locgov_nm, upper_locgov_code, "
                + "upper_locgov_nm from donation.g_locgov where use_at = 'Y'").getResultList();
        return rows.stream().map(r -> new NhLocgov((String) r[0], (String) r[1], (String) r[2], (String) r[3])).toList();
    }

    /** 국세청 전자기부영수증 대상 - AS-IS NtsParam(user_id는 AS-IS가 gc.frst_register_id를 그 이름으로 뽑는다). */
    public record NtsTarget(Long registerId, Long userId, String locgovCode, long taxAmt, String sttemntPayDe,
                            String bizNo, String enapbuNo) {
    }

    /**
     * AS-IS {@code getNtsEreceiptNotList}(On=경로 100 / Off=200, 수납일 = 어제) / {@code getNtsEreceiptNotListOld}(경로·날짜
     * 조건 없음) - 삭제 안 된 수납완료 기부 중 국세청 결과 메시지가 아직 없는 것, 기부일자순. 회원 CI는 member 소유라
     * 호출측이 붙이고, AS-IS {@code rownum <= 100 AND user_ci IS NOT null}처럼 CI 있는 건만 100건 쓴다(그래서 여기선 넉넉히 뽑는다).
     */
    @SuppressWarnings("unchecked")
    public java.util.List<NtsTarget> ntsTargets(String cntrPathCode) {
        String sql = "select gc.frst_register_id, gc.user_id, gc.cntr_locgov_code, gc.cntr_amt, gc.sttemnt_pay_de, "
                + "(select bizrno from donation.g_locgov where locgov_code = gc.cntr_locgov_code), gc.elctrn_pay_no "
                + "from donation.g_cntr gc where gc.sttemnt_pay_de is not null and gc.cntr_sttus_code = 'COMPLETED' "
                + "and gc.delete_at = 'N' and (gc.nts_sttus_mssage is null or gc.nts_sttus_mssage = 'null') "
                + (cntrPathCode != null ? "and gc.cntr_path_code = :path and gc.sttemnt_pay_de = :yesterday " : "")
                + "order by gc.cntr_de limit 1000";
        var q = entityManager.createNativeQuery(sql);
        if (cntrPathCode != null) {
            q.setParameter("path", cntrPathCode).setParameter("yesterday", LocalDate.now().minusDays(1).format(YMD));
        }
        java.util.List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new NtsTarget(r[0] == null ? null : ((Number) r[0]).longValue(),
                r[1] == null ? null : ((Number) r[1]).longValue(), (String) r[2],
                r[3] == null ? 0L : ((Number) r[3]).longValue(), (String) r[4], (String) r[5], (String) r[6])).toList();
    }

    /** AS-IS {@code insertHometaxGif} + {@code updateNtsStatus} - 전송 이력(암호화 CI·사업자번호·결과)과 기부의 국세청 결과 메시지. */
    @Transactional
    public void recordNts(NtsTarget t, String encryptedCi, String encryptedBizNo, String resCode, String resMsg) {
        String ifNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%016d", new java.security.SecureRandom().nextInt(100000));
        entityManager.createNativeQuery("insert into donation.gif_hometax (if_no, elctrn_pay_no, user_id, dnt_dt, conb_cd, "
                        + "dnt_amt, cntr_type, mem_ci, biz_no, result_code, result_msg, if_st_dt) values (:ifNo, :epay, :userId, "
                        + ":dntDt, '43', :amt, '01', :ci, :biz, :code, :msg, now())")
                .setParameter("ifNo", ifNo).setParameter("epay", t.enapbuNo()).setParameter("userId", t.registerId())
                .setParameter("dntDt", t.sttemntPayDe()).setParameter("amt", String.valueOf(t.taxAmt()))
                .setParameter("ci", encryptedCi).setParameter("biz", encryptedBizNo)
                .setParameter("code", resCode).setParameter("msg", resMsg).executeUpdate();
        entityManager.createNativeQuery("update donation.g_cntr set nts_sttus_mssage = :msg, last_updt_pnttm = now(), "
                        + "last_updusr_id = 0 where elctrn_pay_no = :epay")
                .setParameter("msg", resMsg).setParameter("epay", t.enapbuNo()).executeUpdate();
    }

    /** 기부금영수증 처리 대상(G_CNTR_TAX_TEMP) - AS-IS getCntrTaxTempList. */
    public record TaxTempTarget(String enapbuNo, String sttemntPayDe, String mberCi, Long userId, long taxAmt,
                                String bizNo, String elcrAplCd, String conbCd) {
    }

    /**
     * AS-IS {@code getCntrTaxTempList}: 미처리(IS_SEND=0) 100건, 수납일순. AS-IS는 표의 MBER_CI가 없으면 op_user_ci에서
     * 회원 CI를 가져오는데 TO-BE CI는 member 소유라 호출측이 채운다.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<TaxTempTarget> taxTempTargets() {
        java.util.List<Object[]> rows = entityManager.createNativeQuery("select elctrn_pay_no, sttemnt_pay_de, mber_ci, user_id, "
                + "cntr_amt, biz_no, elcr_apl_cd, conb_cd from donation.g_cntr_tax_temp where is_send = 0 "
                + "order by sttemnt_pay_de limit 100").getResultList();
        return rows.stream().map(r -> new TaxTempTarget((String) r[0], (String) r[1], (String) r[2],
                r[3] == null ? null : ((Number) r[3]).longValue(), r[4] == null ? 0L : ((Number) r[4]).longValue(),
                (String) r[5], (String) r[6], (String) r[7])).toList();
    }

    /** AS-IS {@code insertCntrTaxLog} - 상태 100(신고)·301(CI 없음)·302(사업자번호 없음) 등으로 이력 한 줄. */
    @Transactional
    public void insertTaxLog(TaxTempTarget t, String taxStatusCode) {
        entityManager.createNativeQuery("insert into donation.g_cntr_tax_log (elctrn_pay_no, sttemnt_pay_de, cntr_amt, "
                        + "tax_status_code, conb_cd, cntr_type, nts_res_code, nts_res_mssage, elcr_apl_cd, frst_regist_pnttm) "
                        + "values (:epay, :de, :amt, :status, :conb, '01', null, null, :apl, now())")
                .setParameter("epay", t.enapbuNo()).setParameter("de", t.sttemntPayDe()).setParameter("amt", t.taxAmt())
                .setParameter("status", taxStatusCode).setParameter("conb", t.conbCd()).setParameter("apl", t.elcrAplCd())
                .executeUpdate();
    }

    /** AS-IS {@code updateCntrTaxLog}(결과) + 선택적으로 {@code updateCntrTaxTemp}(IS_SEND=1). */
    @Transactional
    public void updateTaxLog(TaxTempTarget t, String taxStatusCode, String resCode, String resMsg, boolean markSent) {
        entityManager.createNativeQuery("update donation.g_cntr_tax_log set tax_status_code = :status, nts_res_code = :code, "
                        + "nts_res_mssage = :msg, last_updt_pnttm = now() where elctrn_pay_no = :epay")
                .setParameter("status", taxStatusCode).setParameter("code", resCode).setParameter("msg", resMsg)
                .setParameter("epay", t.enapbuNo()).executeUpdate();
        if (markSent) {
            markTaxTempSent(t);
        }
    }

    /** AS-IS {@code updateCntrTaxTemp} - IS_SEND = 1. */
    @Transactional
    public void markTaxTempSent(TaxTempTarget t) {
        entityManager.createNativeQuery("update donation.g_cntr_tax_temp set is_send = 1 where elctrn_pay_no = :epay")
                .setParameter("epay", t.enapbuNo()).executeUpdate();
    }

    /** 전날 미수납 재확인 대상 - AS-IS confirmSunapForDeletedList. */
    public record DeletedSunapTarget(String cntrSn, String enapbuNo, String jijacheCd, String cntrDe, Long userId) {
    }

    /**
     * confirmSunapForDeletedListBatch 대상 - AS-IS: 어제 신고된 서울(SEOUL_TRGET_AT='Y') 온라인(경로 100) 기부 중 아직 신고
     * (AS-IS '100' = REQUESTED) 상태인 것, 최근 등록순 100건.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<DeletedSunapTarget> deletedSunapTargets() {
        java.util.List<Object[]> rows = entityManager.createNativeQuery("select cntr_sn, elctrn_pay_no, cntr_locgov_code, cntr_de, "
                        + "user_id from donation.g_cntr where cntr_sttus_code = 'REQUESTED' and cntr_path_code = '100' "
                        + "and seoul_trget_at = 'Y' and cntr_de = :yesterday order by frst_regist_pnttm desc limit 100")
                .setParameter("yesterday", LocalDate.now().minusDays(1).format(YMD))
                .getResultList();
        return rows.stream().map(r -> new DeletedSunapTarget((String) r[0], (String) r[1], (String) r[2], (String) r[3],
                r[4] == null ? null : ((Number) r[4]).longValue())).toList();
    }

    /** AS-IS {@code sunapSuccess}(재확인 수납) - 5분 수납확인과 같은 수납처리(수납일=기부일자·삭제여부 N·완료 이벤트), 변경신청 이력은 없다. */
    @Transactional
    public void completeSunapForDeleted(String cntrSn) {
        donationService.completeBySunapCheck(cntrSn);
    }

    /**
     * AS-IS {@code updateSunapBatchCompleted} - 재확인했는데 미수납이면 상태를 AS-IS 값 '900' 그대로 둔다(다음 재확인·
     * 신고 상태 조회에서 빠진다). ★TO-BE 상태코드(REQUESTED/COMPLETED/CANCELLED)에 대응 값이 없어 화면에는 알 수 없는 상태로
     * 보인다 - AS-IS에서 중지된 배치라 켜기 전에 상태 체계를 정해야 한다(원장 기록).
     */
    @Transactional
    public void markSunapRecheckedUnpaid(String enapbuNo) {
        entityManager.createNativeQuery("update donation.g_cntr set cntr_sttus_code = '900', last_updusr_id = 0, "
                        + "last_updt_pnttm = now() where elctrn_pay_no = :epay")
                .setParameter("epay", enapbuNo).executeUpdate();
    }

    public record NoBugaLocgov(String upperLocgovCode, String locgovCode, String upperLocgovNm, String locgovNm) {
    }

    /**
     * getNoBugaLocgovListBatch 대상 - AS-IS ngdonation-mapper {@code getNoBugaLocgovList}: 사용 중 지자체 중 최근 3일(오늘-3 ~ 오늘)
     * 수납완료 기부가 없는 곳. 서울(코드 11로 시작)은 서울 세외 대상분(SEOUL_TRGET_AT='Y'), 그 외는 'N'만 센다.
     * AS-IS {@code substring(locgov_code, 0, 2)}는 CUBRID에서 앞 2자리라 PostgreSQL에서는 {@code left(..., 2)}로 옮겼다.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<NoBugaLocgov> noBugaLocgovs() {
        LocalDate today = LocalDate.now();
        java.util.List<Object[]> rows = entityManager.createNativeQuery("""
                        WITH locgov AS (
                          SELECT upper_locgov_code, locgov_code, upper_locgov_nm, locgov_nm
                            FROM donation.g_locgov WHERE use_at = 'Y'
                        )
                        SELECT upper_locgov_code, locgov_code, upper_locgov_nm, locgov_nm FROM (
                          SELECT upper_locgov_code, locgov_code, upper_locgov_nm, locgov_nm,
                                 sum(seo_cnt) AS seo_cnt, sum(con_cnt) AS con_cnt
                            FROM (
                              SELECT upper_locgov_code, locgov_code, upper_locgov_nm, locgov_nm, 0 AS seo_cnt, 0 AS con_cnt
                                FROM locgov
                              UNION ALL
                              SELECT a.upper_locgov_code, a.locgov_code, a.upper_locgov_nm, a.locgov_nm,
                                     count(b.cntr_sn) AS seo_cnt, 0 AS con_cnt
                                FROM donation.g_locgov a
                               INNER JOIN donation.g_cntr b ON a.locgov_code = b.cntr_locgov_code AND b.delete_at = 'N'
                                 AND b.sttemnt_pay_de IS NOT NULL AND b.cntr_sttus_code = 'COMPLETED'
                                 AND b.cntr_de BETWEEN :from AND :to AND b.seoul_trget_at = 'Y'
                               GROUP BY a.upper_locgov_code, a.locgov_code, a.upper_locgov_nm, a.locgov_nm
                              UNION ALL
                              SELECT a.upper_locgov_code, a.locgov_code, a.upper_locgov_nm, a.locgov_nm,
                                     0 AS seo_cnt, count(b.cntr_sn) AS con_cnt
                                FROM donation.g_locgov a
                               INNER JOIN donation.g_cntr b ON a.locgov_code = b.cntr_locgov_code AND b.delete_at = 'N'
                                 AND b.sttemnt_pay_de IS NOT NULL AND b.cntr_sttus_code = 'COMPLETED'
                                 AND b.cntr_de BETWEEN :from AND :to AND b.seoul_trget_at = 'N'
                               GROUP BY a.upper_locgov_code, a.locgov_code, a.upper_locgov_nm, a.locgov_nm
                            ) a
                           GROUP BY upper_locgov_code, locgov_code, upper_locgov_nm, locgov_nm
                        ) a
                        WHERE (a.locgov_code LIKE '11%' AND seo_cnt = 0) OR (left(locgov_code, 2) != '11' AND con_cnt = 0)
                        """)
                .setParameter("from", today.minusDays(3).format(YMD))
                .setParameter("to", today.format(YMD))
                .getResultList();
        return rows.stream().map(r -> new NoBugaLocgov((String) r[0], (String) r[1], (String) r[2], (String) r[3])).toList();
    }

    /** CNTR_DE가 from 이상 toExclusive 미만인 수납완료 기부금 합(없으면 null - AS-IS SUM 그대로). */
    private Long sum(LocalDate from, LocalDate toExclusive) {
        Object o = entityManager.createNativeQuery("select sum(cntr_amt) from donation.g_cntr "
                        + "where cntr_sttus_code = 'COMPLETED' and cntr_de >= :from and cntr_de < :to")
                .setParameter("from", from.format(YMD)).setParameter("to", toExclusive.format(YMD))
                .getSingleResult();
        return o == null ? null : ((Number) o).longValue();
    }
}

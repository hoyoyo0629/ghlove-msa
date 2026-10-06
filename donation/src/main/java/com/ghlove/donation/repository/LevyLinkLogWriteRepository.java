package com.ghlove.donation.repository;

import com.ghlove.donation.service.integration.NextBugaRequest;
import com.ghlove.donation.service.integration.SeoulBugaRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 납부 연계 요청/응답을 연계로그 표에 적재한다 - admin 1413(서울세외 부과연계 로그)·
 * 1415(지방세외 부과연계 로그) 화면이 읽는 그 표다({@link LevyLinkLogRepository}가 조회쪽).
 *
 * <p><b>왜 우리가 쓰는가(중요)</b>: AS-IS에서 {@code g_next_buga_request}·{@code gif_seoul}에
 * <b>INSERT하는 코드는 우리 애플리케이션에 없다</b>(실측: AS-IS 매퍼에 SELECT만 있다) - 연계
 * 서버/상대 시스템이 적재한다. 그래서 이 적재는 <b>연계가 꺼져 있을 때(모크)만</b> 수행해
 * 개발환경에서 요청 흐름을 1413·1415 화면으로 눈으로 확인할 수 있게 하는 용도다.
 * 실제 연계를 켜면({@code enabled=true}) 적재하지 않는다 - 상대 시스템이 쓰는 표에 끼어들면
 * 안 된다.
 *
 * <p>두 표 모두 기본키가 없는 연계로그라 엔티티를 만들지 않고 네이티브 INSERT로만 쓴다
 * (조회쪽 {@code LevyLinkLogRepository}와 같은 방식). 컬럼 길이가 짧아(예: {@code error_cd} 3자,
 * {@code link_rst_cd} 30자) 자르지 않으면 적재가 실패하므로 길이를 맞춰 넣는다.
 */
@Repository
@RequiredArgsConstructor
public class LevyLinkLogWriteRepository {

    private final EntityManager entityManager;

    /**
     * 지방세외 차세대 부과요청 로그. 요청 전문이 그대로 컬럼에 대응하고 응답
     * ({@code buga_status_cd}/{@code link_rst_cd}/{@code link_rst_msg})이 뒤에 붙는다.
     */
    @Transactional
    public void insertNextBuga(NextBugaRequest request, String bugaStatusCd, String linkRstCd,
                                String linkRstMsg) {
        Query q = entityManager.createNativeQuery("""
                insert into donation.g_next_buga_request (
                    link_mng_key, sgb_cd, link_trgt_cd, dpt_cd, spcl_fis_biz_cd, fyr, act_se_cd,
                    rprs_txm_cd, oper_item_cd, lvy_ymd, frst_pct_amt, frst_pid_ymd, pyr_se_cd,
                    pyr_no, pyr_nm, rprs_pyr_no, rprs_pyr_nm, pyr_stt_cd, lotno_road_addr_se_cd,
                    zip, road_nm_cd, bmno, bsno, stdg_cd, dong_cd, road_nm_daddr, gl_nm,
                    mng_item_cn1, buga_status_cd, link_rst_cd, link_rst_msg, frst_regist_pnttm
                ) values (
                    :linkMngKey, :sgbCd, :linkTrgtCd, :dptCd, :spclFisBizCd, :fyr, :actSeCd,
                    :rprsTxmCd, :operItemCd, :lvyYmd, :frstPctAmt, :frstPidYmd, :pyrSeCd,
                    :pyrNo, :pyrNm, :rprsPyrNo, :rprsPyrNm, :pyrSttCd, :lotnoRoadAddrSeCd,
                    :zip, :roadNmCd, :bmno, :bsno, :stdgCd, :dongCd, :roadNmDaddr, :glNm,
                    :mngItemCn1, :bugaStatusCd, :linkRstCd, :linkRstMsg, :registPnttm
                )
                """);
        q.setParameter("linkMngKey", cut(request.linkMngKey(), 50));
        q.setParameter("sgbCd", nvl(request.sgbCd()));
        q.setParameter("linkTrgtCd", nvl(request.linkTrgtCd()));
        q.setParameter("dptCd", nvl(request.dptCd()));
        q.setParameter("spclFisBizCd", nvl(request.spclFisBizCd()));
        q.setParameter("fyr", nvl(request.fyr()));
        q.setParameter("actSeCd", nvl(request.actSeCd()));
        q.setParameter("rprsTxmCd", nvl(request.rprsTxmCd()));
        q.setParameter("operItemCd", nvl(request.operItemCd()));
        q.setParameter("lvyYmd", nvl(request.lvyYmd()));
        q.setParameter("frstPctAmt", cut(nvl(request.frstPctAmt()), 15));
        q.setParameter("frstPidYmd", nvl(request.frstPidYmd()));
        q.setParameter("pyrSeCd", nvl(request.pyrSeCd()));
        q.setParameter("pyrNo", nvl(request.pyrNo()));
        q.setParameter("pyrNm", nvl(request.pyrNm()));
        q.setParameter("rprsPyrNo", nvl(request.rprsPyrNo()));
        q.setParameter("rprsPyrNm", nvl(request.rprsPyrNm()));
        q.setParameter("pyrSttCd", nvl(request.pyrSttCd()));
        q.setParameter("lotnoRoadAddrSeCd", nvl(request.lotnoRoadAddrSeCd()));
        q.setParameter("zip", nvl(request.zip()));
        q.setParameter("roadNmCd", nvl(request.roadNmCd()));
        q.setParameter("bmno", nvl(request.bmno()));
        q.setParameter("bsno", nvl(request.bsno()));
        q.setParameter("stdgCd", nvl(request.stdgCd()));
        q.setParameter("dongCd", nvl(request.dongCd()));
        q.setParameter("roadNmDaddr", nvl(request.roadNmDaddr()));
        q.setParameter("glNm", nvl(request.glNm()));
        q.setParameter("mngItemCn1", nvl(request.mngItemCn1()));
        q.setParameter("bugaStatusCd", cut(nvl(bugaStatusCd), 3));
        q.setParameter("linkRstCd", cut(nvl(linkRstCd), 30));
        q.setParameter("linkRstMsg", cut(nvl(linkRstMsg), 200));
        q.setParameter("registPnttm", LocalDateTime.now());
        q.executeUpdate();
    }

    /**
     * 서울 세외 부과요청 로그. AS-IS 응답의 전자납부번호({@code enapbuNo})와 오류코드·메시지가
     * 같이 쌓이고, 1413 화면은 {@code error_cd = '0'}을 정상으로 본다.
     */
    @Transactional
    public void insertSeoulBuga(SeoulBugaRequest request, String enapbuNo, String errorCd, String errorMsg) {
        Map<String, Object> payload = request.toPayload();
        Query q = entityManager.createNativeQuery("""
                insert into donation.gif_seoul (
                    if_no, enapbu_no, sigu_cd, semok_cd, tax_ym, tax_gubun, sido_cd,
                    nap_id, nap_nm, nap_gubun, tax_amt, sise, reside_status, mul_gubun, mul_nm,
                    book_no, sys_gubun, error_cd, error_msg, if_st_dt, if_ed_dt
                ) values (
                    :ifNo, :enapbuNo, :siguCd, :semokCd, :taxYm, :taxGubun, :sidoCd,
                    :napId, :napNm, :napGubun, :taxAmt, :sise, :resideStatus, :mulGubun, :mulNm,
                    :bookNo, :sysGubun, :errorCd, :errorMsg, :stDt, :edDt
                )
                """);
        LocalDateTime now = LocalDateTime.now();
        q.setParameter("ifNo", cut(request.bookNo(), 30));
        q.setParameter("enapbuNo", cut(nvl(enapbuNo), 19));
        q.setParameter("siguCd", cut(nvl(request.siguCd()), 7));
        q.setParameter("semokCd", cut(nvl(request.semokCd()), 8));
        q.setParameter("taxYm", cut(nvl(request.taxYm()), 6));
        q.setParameter("taxGubun", cut(nvl(request.taxGubun()), 1));
        q.setParameter("sidoCd", cut(nvl(request.sidoCd()), 2));
        q.setParameter("napId", cut(nvl(request.napId()), 1000));
        q.setParameter("napNm", cut(nvl(request.napNm()), 80));
        q.setParameter("napGubun", cut(nvl(request.napGubun()), 2));
        q.setParameter("taxAmt", request.taxAmt());
        q.setParameter("sise", payload.get("sise"));
        q.setParameter("resideStatus", cut(nvl(request.resideStatus()), 2));
        q.setParameter("mulGubun", cut(nvl(request.mulGubun()), 2));
        q.setParameter("mulNm", cut(nvl(request.mulNm()), 100));
        q.setParameter("bookNo", cut(nvl(request.bookNo()), 30));
        q.setParameter("sysGubun", cut(nvl(request.sysGubun()), 10));
        q.setParameter("errorCd", cut(nvl(errorCd), 3));
        q.setParameter("errorMsg", cut(nvl(errorMsg), 2000));
        q.setParameter("stDt", now);
        q.setParameter("edDt", now);
        q.executeUpdate();
    }

    /** 연계관리키 - AS-IS: yyyyMMddHHmmss + 시퀀스 next value. */
    public String nextLinkMngKey() {
        Object value = entityManager
                .createNativeQuery("select to_char(now(),'YYYYMMDDHH24MISS') || nextval('donation.g_cntr_link_mng_key_seq')")
                .getSingleResult();
        return value == null ? null : value.toString();
    }

    /** 서울 대장번호 - AS-IS: gif_seoul_book_no_seq.next_value. */
    public String nextSeoulBookNo() {
        Object value = entityManager
                .createNativeQuery("select nextval('donation.gif_seoul_book_no_seq')")
                .getSingleResult();
        return value == null ? null : value.toString();
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

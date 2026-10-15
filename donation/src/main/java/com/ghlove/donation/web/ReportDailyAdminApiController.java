package com.ghlove.donation.web;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 보고서 > 일일 현황보고의 기부 집계(AS-IS {@code ReportServiceImpl#donationStatistics} /
 * {@code dayDonationStatistics} + {@code DonationRepositoryImpl}). 조회 전용 - admin이 호출해 JSON을 조립한다.
 *
 * <p>AS-IS 조건 그대로: 상태 "200"(TO-BE {@code COMPLETED}), 누계는 <b>그해 1월 1일 ~ searchDay</b>,
 * "증가"는 {@code startDay ~ endDay}(일일은 같은 날). 지정기부는 {@code DSGN_DNTN_BIZ_ID != 0} -
 * TO-BE는 일반기부가 null/0 두 가지로 저장돼 있어 둘 다 일반으로 본다. 민간플랫폼(연계기관)은
 * {@code LINK_INSTT_CD}, "민간 제외"는 그 값이 null인 건이다.
 */
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDailyAdminApiController {

    private final EntityManager em;

    /** @param searchDay 누계의 끝(yyyyMMdd) @param startDay "증가" 구간 시작 @param endDay "증가" 구간 끝 */
    @GetMapping("/daily-donation")
    public Map<String, Object> dailyDonation(@RequestParam String searchDay,
                                              @RequestParam String startDay,
                                              @RequestParam String endDay) {
        String ytdStart = searchDay.substring(0, 4) + "0101";
        Map<String, Object> r = new LinkedHashMap<>();

        long[] total = cntAmt(ytdStart, searchDay, "");
        r.put("totalCount", total[0]);
        r.put("totalCntrAmt", total[1]);

        long[] online = cntAmt(ytdStart, searchDay, "and cntr_path_code='100' and coalesce(dsgn_dntn_biz_id,0)=0");
        long[] offline = cntAmt(ytdStart, searchDay, "and cntr_path_code='200' and coalesce(dsgn_dntn_biz_id,0)=0");
        long[] onlineDsgn = cntAmt(ytdStart, searchDay, "and cntr_path_code='100' and coalesce(dsgn_dntn_biz_id,0)<>0");
        long[] offlineDsgn = cntAmt(ytdStart, searchDay, "and cntr_path_code='200' and coalesce(dsgn_dntn_biz_id,0)<>0");
        r.put("onlineCount", online[0]);
        r.put("onlineCntrAmt", online[1]);
        r.put("offlineCount", offline[0]);
        r.put("offlineCntrAmt", offline[1]);
        r.put("onlineDsgnDntnBizCount", onlineDsgn[0]);
        r.put("onlineDsgnDntnBizAmt", onlineDsgn[1]);
        r.put("offlineDsgnDntnBizCount", offlineDsgn[0]);
        r.put("offlineDsgnDntnBizAmt", offlineDsgn[1]);

        long[] notLink = cntAmt(ytdStart, searchDay, "and link_instt_cd is null");
        r.put("totalCountNotLinkInstt", notLink[0]);
        r.put("totalCntrAmtNotLinkInstt", notLink[1]);

        long[] inc = cntAmt(startDay, endDay, "");
        r.put("increasedCount", inc[0]);
        r.put("increasedAmt", inc[1]);

        r.put("linkInsttCumulative", byLink(ytdStart, searchDay));
        r.put("linkInsttCurrent", byLink(startDay, endDay));
        return r;
    }

    private long[] cntAmt(String from, String to, String extraCond) {
        Object[] row = (Object[]) em.createNativeQuery(
                        "select count(*), coalesce(sum(cntr_amt),0) from donation.g_cntr "
                                + "where cntr_sttus_code='COMPLETED' and cntr_de between :f and :t " + extraCond)
                .setParameter("f", from).setParameter("t", to).getSingleResult();
        return new long[]{((Number) row[0]).longValue(), ((Number) row[1]).longValue()};
    }

    /** 연계기관 코드별 건수·금액(기간 안에 건이 없으면 행이 없다 - admin이 코드 목록에 0으로 채운다). */
    private Map<String, long[]> byLink(String from, String to) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select link_instt_cd, count(*), coalesce(sum(cntr_amt),0) from donation.g_cntr "
                                + "where cntr_sttus_code='COMPLETED' and cntr_de between :f and :t "
                                + "and link_instt_cd is not null group by 1")
                .setParameter("f", from).setParameter("t", to).getResultList();
        Map<String, long[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()});
        }
        return map;
    }
}

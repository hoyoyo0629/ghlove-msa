package com.ghlove.admin.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 보고서 > 일일 현황보고의 "행안부 일일보고/주간보고" JSON (AS-IS {@code ReportController} +
 * {@code ReportServiceImpl#dailyReportTotalStatistics}/{@code dayReportTotalStatistics}).
 * {@code daily.report.js}가 이 JSON({@code donation}/{@code item}/{@code user}/{@code customerCenter})을 읽어
 * 보고 문안을 조립한다 - 키 이름과 구조를 AS-IS 그대로 맞춘다.
 *
 * <p>원천이 서비스별로 나뉘어 있다: 기부(donation G_CNTR)·사용포인트(point)·회원(member)은 각 서비스의
 * 집계 API, 콜센터(OP_CALL)는 admin 자체 테이블, 등록 답례품 수는 gift. 한 곳이라도 못 불러오면 그 항목만 0이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyReportService {

    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final MemberClient memberClient;
    private final GiftClient giftClient;
    private final CommonCodeService commonCodeService;
    private final EntityManager em;

    /** AS-IS /daily/{searchDay} - 일일보고(그날 기준, 누계는 그해 1월 1일부터). */
    public Map<String, Object> daily(String searchDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("donation", donation(searchDay, searchDay, searchDay, true));
        result.put("item", item(searchDay));
        result.put("customerCenter", customerCenter(searchDay));
        Map<String, Object> user = new LinkedHashMap<>();
        Map<String, Object> u = memberClient.dailyUser(searchDay, null, null);
        user.put("userCount", num(u, "userCountUpToDay"));
        result.put("user", user);
        return result;
    }

    /** AS-IS /daily/{startDay}/{endDay} - 주간보고(endDay 기준 누계, startDay~endDay 증가). */
    public Map<String, Object> week(String startDay, String endDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("donation", donation(endDay, startDay, endDay, false));
        result.put("item", item(endDay));
        Map<String, Object> user = new LinkedHashMap<>();
        Map<String, Object> u = memberClient.dailyUser(endDay, startDay, endDay);
        user.put("userCount", num(u, "userCountAll"));
        user.put("increaseUserCount", num(u, "increaseUserCount"));
        result.put("user", user);
        return result;
    }

    private Map<String, Object> donation(String searchDay, String startDay, String endDay, boolean daily) {
        Map<String, Object> src = donationClient.dailyReport(searchDay, startDay, endDay);
        Map<String, Object> r = new LinkedHashMap<>();
        for (String k : List.of("totalCount", "increasedCount", "onlineCount", "offlineCount", "onlineDsgnDntnBizCount",
                "offlineDsgnDntnBizCount", "totalCntrAmt", "increasedAmt", "onlineCntrAmt", "offlineCntrAmt",
                "onlineDsgnDntnBizAmt", "offlineDsgnDntnBizAmt", "totalCountNotLinkInstt", "totalCntrAmtNotLinkInstt")) {
            r.put(k, num(src, k));
        }
        Map<String, List<Number>> cumulative = linkMap(src, "linkInsttCumulative");
        Map<String, List<Number>> current = linkMap(src, "linkInsttCurrent");
        Map<String, String> codes = commonCodeService.labelsOf("LINK_INSTT_CD");
        r.put("linkInsttCumulative", linkList(codes, cumulative));
        if (daily) {
            List<Map<String, Object>> currentList = linkList(codes, current);
            r.put("linkInsttCurrent", currentList);
            long dayCnt = 0;
            long dayAmt = 0;
            for (Map<String, Object> e : currentList) {
                dayCnt += (Long) e.get("linkInsttCnt");
                dayAmt += (Long) e.get("linkInsttAmt");
            }
            // 일일 기부 현황(민간 제외) = 그날 전체 - 그날 민간플랫폼
            r.put("increasedTotalCountNotLinkInstt", num(src, "increasedCount") - dayCnt);
            r.put("increasedTotalCntrAmtNotLinkInstt", num(src, "increasedAmt") - dayAmt);
        }
        return r;
    }

    private Map<String, Object> item(String searchDay) {
        Map<String, Object> p = pointClient.dailyUsePoint(searchDay);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orderCount", num(p, "orderCount"));
        r.put("usePointAmt", num(p, "usePointAmt"));
        r.put("itemRegCount", registeredItemCount());
        return r;
    }

    /**
     * AS-IS opmanageMainAmountGiftA+B = 지자체가 있는 판매자의 답례품(상태 0/1/90, 품절 여부로 둘로 쪼갠 합) = 사실상
     * 등록 답례품 전체. TO-BE는 지자체가 있는 품목(승인/판매중지 포함) 수로 옮겼다.
     */
    private long registeredItemCount() {
        return giftClient.allForResync().stream().filter(i -> i.locgovCode() != null).count();
    }

    /** AS-IS callRepository.findByCallDate - 그날 행이 없으면 null(JS가 ?. 로 0 처리). */
    private Map<String, Object> customerCenter(String searchDay) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select call_kookmin, call_lov, call_giver, call_nhbank, call_platform from admin.op_call "
                                + "where call_date = :d")
                .setParameter("d", searchDay).getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = rows.get(0);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("callKookmin", row[0] == null ? 0L : ((Number) row[0]).longValue());
        r.put("callLov", row[1] == null ? 0L : ((Number) row[1]).longValue());
        r.put("callGiver", row[2] == null ? 0L : ((Number) row[2]).longValue());
        r.put("callNhbank", row[3] == null ? 0L : ((Number) row[3]).longValue());
        r.put("callPlatform", row[4] == null ? 0L : ((Number) row[4]).longValue());
        return r;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Number>> linkMap(Map<String, Object> src, String key) {
        Map<String, List<Number>> out = new HashMap<>();
        if (src != null && src.get(key) instanceof Map<?, ?> m) {
            ((Map<String, Object>) m).forEach((code, v) -> out.put(code, (List<Number>) v));
        }
        return out;
    }

    /** 연계기관 코드 전체를 AS-IS와 같은 순서로 돌려주고, 건이 없는 코드는 0으로 채운다. */
    private static List<Map<String, Object>> linkList(Map<String, String> codes, Map<String, List<Number>> values) {
        List<Map<String, Object>> list = new ArrayList<>();
        codes.forEach((code, label) -> {
            List<Number> v = values.get(code);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("linkInsttNm", label);
            m.put("linkInsttCnt", v == null ? 0L : v.get(0).longValue());
            m.put("linkInsttAmt", v == null ? 0L : v.get(1).longValue());
            list.add(m);
        });
        return list;
    }

    private static long num(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v instanceof Number n ? n.longValue() : 0L;
    }
}

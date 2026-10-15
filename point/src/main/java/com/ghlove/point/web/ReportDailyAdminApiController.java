package com.ghlove.point.web;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 통계 > 보고서 > 일일 현황보고의 "답례품 신청"(AS-IS {@code CntrUsePointRepositoryImpl}) - 포인트 사용
 * (USE_SE_CODE='1') 이력을 그해 1월 1일 ~ searchDay 로 센다. 신청건수는 주문코드 distinct, 금액은 사용포인트 합.
 */
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDailyAdminApiController {

    private final EntityManager em;

    @GetMapping("/daily-use-point")
    public Map<String, Object> dailyUsePoint(@RequestParam String searchDay) {
        Object[] row = (Object[]) em.createNativeQuery(
                        "select count(distinct order_code), coalesce(sum(cntr_use_point),0) from point.g_cntr_use_point "
                                + "where use_se_code='1' and point_use_de between :f and :t")
                .setParameter("f", searchDay.substring(0, 4) + "0101").setParameter("t", searchDay).getSingleResult();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orderCount", ((Number) row[0]).longValue());
        r.put("usePointAmt", ((Number) row[1]).longValue());
        return r;
    }
}

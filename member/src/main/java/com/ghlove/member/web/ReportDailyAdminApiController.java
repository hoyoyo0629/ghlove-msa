package com.ghlove.member.web;

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
 * 통계 > 보고서 > 일일 현황보고의 회원 수(AS-IS {@code UserReportRepositoryImpl}). AS-IS는 상태코드 9(정상)만 센다 -
 * TO-BE {@code ACTIVE}. 일일보고는 searchDay 23:59:59 까지의 누적, 주간보고는 전체 정상회원과 [startDay, endDay] 증가분.
 */
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDailyAdminApiController {

    private final EntityManager em;

    @GetMapping("/daily-user")
    public Map<String, Object> dailyUser(@RequestParam String searchDay,
                                          @RequestParam(required = false) String startDay,
                                          @RequestParam(required = false) String endDay) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("userCountUpToDay", count("and created_date <= :t", null, searchDay + "235959"));
        r.put("userCountAll", count("", null, null));
        if (startDay != null && endDay != null) {
            r.put("increaseUserCount", count("and created_date between :f and :t", startDay + "000000", endDay + "235959"));
        }
        return r;
    }

    private long count(String cond, String from, String to) {
        var q = em.createNativeQuery("select count(*) from member.op_user where status_code='ACTIVE' " + cond);
        if (from != null) {
            q.setParameter("f", from);
        }
        if (to != null) {
            q.setParameter("t", to);
        }
        return ((Number) q.getSingleResult()).longValue();
    }
}

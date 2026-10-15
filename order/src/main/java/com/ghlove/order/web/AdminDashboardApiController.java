package com.ghlove.order.web;

import com.ghlove.order.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 관리자 메인화면의 답례품 건수 - admin이 cross-service로 호출(/api/admin/orders/** = 내부 시크릿 보호). */
@RestController
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final AdminDashboardService service;

    /** {thisYear: 올해 답례품 건수, total: 전체 답례품 건수}. */
    @GetMapping("/api/admin/orders/stats/dashboard-present")
    public Map<String, Long> present(@RequestParam String userRole, @RequestParam(required = false) String locgovCd) {
        return Map.of("thisYear", service.presentCount(userRole, locgovCd, true),
                "total", service.presentCount(userRole, locgovCd, false));
    }
}

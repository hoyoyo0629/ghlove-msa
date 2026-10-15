package com.ghlove.point.web;

import com.ghlove.point.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 관리자 메인화면의 답례품 포인트 사용 집계 - admin이 cross-service로 호출(/api/admin/** = X-Internal-Secret 보호). */
@RestController
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final AdminDashboardService service;

    @GetMapping("/api/admin/dashboard/main-info")
    public List<AdminDashboardService.CountRow> mainInfo(@RequestParam String userRole,
            @RequestParam(required = false) String locgovCd) {
        return service.mainInfo(userRole, locgovCd);
    }

    /** date = 기부일자(yyyyMMdd). */
    @GetMapping("/api/admin/dashboard/table-info")
    public List<AdminDashboardService.CountRow> tableInfo(@RequestParam String date) {
        return service.tableInfo(date);
    }
}

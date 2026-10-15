package com.ghlove.donation.web;

import com.ghlove.donation.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 관리자 메인화면의 기부(G_CNTR) 집계 - admin이 cross-service로 호출(/api/admin/** = X-Internal-Secret 보호). */
@RestController
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final AdminDashboardService service;

    @GetMapping("/api/admin/dashboard/main-info")
    public List<AdminDashboardService.CountRow> mainInfo(@RequestParam String userRole,
            @RequestParam(required = false) String locgovCd, @RequestParam(required = false) String bankCode,
            @RequestParam(required = false) String bankNm) {
        return service.mainInfo(userRole, locgovCd, bankCode, bankNm);
    }

    @GetMapping("/api/admin/dashboard/amount-chart")
    public List<AdminDashboardService.ChartRow> amountChart(@RequestParam String userRole,
            @RequestParam(required = false) String locgovCd) {
        return service.amountChart(userRole, locgovCd);
    }

    @GetMapping("/api/admin/dashboard/count-chart")
    public List<AdminDashboardService.ChartRow> countChart(@RequestParam String userRole,
            @RequestParam(required = false) String locgovCd) {
        return service.countChart(userRole, locgovCd);
    }

    @GetMapping("/api/admin/dashboard/chart-info")
    public List<AdminDashboardService.CountRow> chartInfo(@RequestParam String userRole,
            @RequestParam(required = false) String locgovCd) {
        return service.chartInfo(userRole, locgovCd);
    }

    /** date = 기부일자(yyyyMMdd). */
    @GetMapping("/api/admin/dashboard/table-info")
    public List<AdminDashboardService.CountRow> tableInfo(@RequestParam String date) {
        return service.tableInfo(date);
    }
}

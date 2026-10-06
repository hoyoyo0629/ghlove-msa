package com.ghlove.donation.web;

import com.ghlove.donation.service.GiveStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 기부 통계(AS-IS opmanager/give/statistics) - admin이 cross-service로 호출. 전체(all) 집계.
 *  InternalApiAuthInterceptor(X-Internal-Secret)로 보호되고 admin의 OP_MANAGER 로그인이 실제 게이트. */
@RestController
@RequiredArgsConstructor
public class GiveStatAdminApiController {

    private final GiveStatService giveStatService;

    @GetMapping("/api/give-statistics/admin/all/summary")
    public GiveStatService.AllSummary allSummary(@RequestParam String year,
            @RequestParam(required = false) String locgovCode) {
        return giveStatService.allSummary(year, locgovCode);
    }

    @GetMapping("/api/give-statistics/admin/all/{year}/month")
    public List<GiveStatService.MonthRow> allMonth(@PathVariable String year,
            @RequestParam(required = false) String locgovCode) {
        return giveStatService.allByMonth(year, locgovCode);
    }

    @GetMapping("/api/give-statistics/admin/all/{date}/hour")
    public List<GiveStatService.HourRow> allHour(@PathVariable String date,
            @RequestParam(required = false) String locgovCode) {
        return giveStatService.allByHour(date, locgovCode);
    }

    /** 지자체별 통계 목록(AS-IS give/statistics/locgov). */
    @GetMapping("/api/give-statistics/admin/locgov/list")
    public List<GiveStatService.LocgovYearRow> locgovList(@RequestParam(required = false) String year,
            @RequestParam(required = false) String locgovCode) {
        return giveStatService.locgovList(year, locgovCode);
    }
}

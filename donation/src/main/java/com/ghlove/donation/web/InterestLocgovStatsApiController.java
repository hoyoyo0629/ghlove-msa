package com.ghlove.donation.web;

import com.ghlove.donation.service.InterestLocgovStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 콘솔 "관심 지자체 통계" 화면(AS-IS opmanager/statistics - StatisticsLocgovManagerController)이
 * 부르는 cross-service API - 다른 admin cross-service API와 동일하게 별도 인증 없이 열려있다
 * (브라우저에 직접 노출되지 않고 admin 콘솔에서만 호출되는 내부용).
 */
@RestController
@RequestMapping("/api/admin/interest-locgov-stats")
@RequiredArgsConstructor
public class InterestLocgovStatsApiController {

    private final InterestLocgovStatsService service;

    @GetMapping("/years")
    public List<String> years() {
        return service.availableYears();
    }

    @GetMapping
    public InterestLocgovStatsService.ListResult search(@RequestParam(required = false) String year,
                                                          @RequestParam(required = false) String locgovNm) {
        return service.search(year, locgovNm);
    }

    @GetMapping("/{locgovCode}")
    public InterestLocgovStatsService.DetailResult detail(@PathVariable String locgovCode,
                                                            @RequestParam(required = false) String year) {
        return service.detail(locgovCode, year);
    }
}

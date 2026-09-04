package com.ghlove.point.web;

import com.ghlove.point.service.PointHistoryAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 콘솔 "포인트 사용내역"/"일별 포인트 현황" 화면(AS-IS opmanager/point -
 * PointManagerController, B2)이 부르는 cross-service API - 다른 admin cross-service API와
 * 동일하게 별도 인증 없이 열려있다(브라우저에 직접 노출되지 않고 admin 콘솔에서만 호출).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PointHistoryAdminApiController {

    private final PointHistoryAdminService service;

    @GetMapping("/point-history")
    public List<PointHistoryAdminService.HistoryRow> history(@RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String locgovCode,
                                      @RequestParam(required = false) String txnType,
                                      @RequestParam(required = false) String fromDate,
                                      @RequestParam(required = false) String toDate) {
        return service.search(userId, locgovCode, txnType, fromDate, toDate);
    }

    @GetMapping("/point-daily-stats")
    public List<PointHistoryAdminService.DailyStat> dailyStats(@RequestParam(required = false) String fromDate,
                                                                 @RequestParam(required = false) String toDate,
                                                                 @RequestParam(required = false) String locgovCode) {
        return service.dailyStats(fromDate, toDate, locgovCode);
    }
}

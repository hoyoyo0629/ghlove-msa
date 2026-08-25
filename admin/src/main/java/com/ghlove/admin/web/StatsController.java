package com.ghlove.admin.web;

import com.ghlove.admin.service.ResyncService;
import com.ghlove.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final ResyncService resyncService;

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("donationByLocgov", statsService.donationStatsByLocgov());
        model.addAttribute("orderBySeller", statsService.orderStatsBySeller());
        model.addAttribute("totalDonationCount", statsService.totalCompletedDonationCount());
        model.addAttribute("totalDonationAmount", statsService.totalCompletedDonationAmount());
        model.addAttribute("totalOrderCount", statsService.totalConfirmedOrderCount());
        model.addAttribute("totalOrderPoints", statsService.totalConfirmedOrderPoints());
        model.addAttribute("pointByTxnType", statsService.pointStatsByTxnType());
        model.addAttribute("giftByStatus", statsService.giftCountByStatus());
        return "stats/dashboard";
    }

    /** ReadModel 재동기화 수동 실행 (SFR-009) - 배포/DB 재구성 직후, 또는 Kafka 보존기간을
     *  넘긴 장애 복구 시 사용. */
    @PostMapping("/stats/resync")
    public String resync(Model model) {
        ResyncService.Result result = resyncService.resyncAll();
        return "redirect:/stats?resynced=" + result.donationCount() + "," + result.orderCount()
                + "," + result.pointLedgerCount() + "," + result.giftCount();
    }

    /** 기부포인트 적립 백필 - point 서비스 자체의 원장 누락분(오늘 실제로 겪은 사고: point DB
     *  재구성 후 과거 완료 기부에 대한 EARN 적립이 재처리되지 않음)을 복구한다. ReadModel
     *  재동기화(읽기전용)와 달리 실제 잔액을 변경하는 쓰기 작업이라 별도 버튼으로 분리했다. */
    @PostMapping("/stats/backfill-point-credits")
    public String backfillPointCredits() {
        ResyncService.PointBackfillResult result = resyncService.backfillPointCredits();
        return "redirect:/stats?backfilled=" + result.newlyCredited() + "," + result.alreadyCredited()
                + "," + result.failed();
    }
}

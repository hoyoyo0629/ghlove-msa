package com.ghlove.admin.web;

import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.service.PointHistoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 포인트 사용내역/일별 현황 (AS-IS opmanager/point - PointManagerController, B2 부분미구현).
 *  기존 {@link GivePointController}(/give-point)는 기부 건별 포인트 발생-사용-잔액만 다루는데,
 *  AS-IS는 이와 별개로 지자체 무관 "전체 사용내역 로그"(모든 EARN/USE/EXPIRE 트랜잭션)와
 *  "일별 발생/사용현황 집계" 화면이 있다. */
@Controller
@RequiredArgsConstructor
public class PointHistoryAdminController {

    private final PointHistoryClient pointHistoryClient;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;

    @GetMapping("/point-history")
    public String list(@RequestParam(required = false) Long userId,
                        @RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String txnType,
                        @RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        Model model) {
        List<PointHistoryClient.HistoryRow> rows = pointHistoryClient.history(userId, locgovCode, txnType, fromDate, toDate);

        Map<Long, String> nameByUserId = new LinkedHashMap<>();
        for (PointHistoryClient.HistoryRow r : rows) {
            nameByUserId.computeIfAbsent(r.userId(), id -> {
                MemberClient.MemberInfo info = memberClient.fetchOrNull(id);
                return info != null && info.userName() != null ? info.userName() : ("회원#" + id);
            });
        }

        model.addAttribute("rows", rows);
        model.addAttribute("nameByUserId", nameByUserId);
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("userId", userId);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("txnType", txnType);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "point-history/list";
    }

    @GetMapping("/point-history/daily")
    public String daily(@RequestParam(required = false) String locgovCode,
                         @RequestParam(required = false) String fromDate,
                         @RequestParam(required = false) String toDate,
                         Model model) {
        List<PointHistoryClient.DailyStat> stats = pointHistoryClient.dailyStats(fromDate, toDate, locgovCode);
        long totalEarnAmount = stats.stream().mapToLong(PointHistoryClient.DailyStat::earnAmount).sum();
        long totalUseAmount = stats.stream().mapToLong(PointHistoryClient.DailyStat::useAmount).sum();
        long totalExpireAmount = stats.stream().mapToLong(PointHistoryClient.DailyStat::expireAmount).sum();

        model.addAttribute("stats", stats);
        model.addAttribute("totalEarnAmount", totalEarnAmount);
        model.addAttribute("totalUseAmount", totalUseAmount);
        model.addAttribute("totalExpireAmount", totalExpireAmount);
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "point-history/daily";
    }
}

package com.ghlove.admin.web;

import com.ghlove.admin.service.ReportStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** AS-IS opmanager/shop-statistics/report 스위트 (총괄/총괄누계/지자체별(광역·기초)/연간) 재구현. */
@Controller
@RequiredArgsConstructor
public class ReportStatisticsController {

    private final ReportStatisticsService reportStatisticsService;

    @GetMapping("/shop-statistics/report/general")
    public String general(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : reportStatisticsService.currentYear();
        int throughMonth = y == reportStatisticsService.currentYear()
                ? reportStatisticsService.currentMonth() - 1 : 12;
        throughMonth = Math.max(throughMonth, 1);
        model.addAttribute("year", y);
        model.addAttribute("memberCounts", reportStatisticsService.memberCountsByMonth(y, throughMonth));
        model.addAttribute("donationCounts", reportStatisticsService.donationCountsByMonth(y, throughMonth, false));
        model.addAttribute("donationCountsCapped", reportStatisticsService.donationCountsByMonth(y, throughMonth, true));
        model.addAttribute("giftCounts", reportStatisticsService.giftCountsByMonth(y, throughMonth));
        return "shop-statistics/report-general";
    }

    @GetMapping("/shop-statistics/report/general-total")
    public String generalTotal(Model model) {
        model.addAttribute("byPathCode", reportStatisticsService.donationsByPathCode());
        model.addAttribute("byAmountBracket", reportStatisticsService.donationsByAmountBracket());
        model.addAttribute("byAgeBracket", reportStatisticsService.donationsByAgeBracket());
        model.addAttribute("residenceToDonation", reportStatisticsService.donationsByResidenceToDonation());
        return "shop-statistics/report-general-total";
    }

    @GetMapping("/shop-statistics/report/mctpv")
    public String mctpv(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : reportStatisticsService.currentYear();
        int throughMonth = y == reportStatisticsService.currentYear()
                ? reportStatisticsService.currentMonth() - 1 : 12;
        throughMonth = Math.max(throughMonth, 1);
        model.addAttribute("year", y);
        model.addAttribute("throughMonth", throughMonth);
        model.addAttribute("donationStats", reportStatisticsService.donationStatsByLocgov(y, throughMonth, false, true));
        model.addAttribute("donationStatsCapped", reportStatisticsService.donationStatsByLocgov(y, throughMonth, true, true));
        model.addAttribute("giftStats", reportStatisticsService.giftStatsByLocgov(y, throughMonth, true));
        return "shop-statistics/report-locgov";
    }

    @GetMapping("/shop-statistics/report/lclgv")
    public String lclgv(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : reportStatisticsService.currentYear();
        int throughMonth = y == reportStatisticsService.currentYear()
                ? reportStatisticsService.currentMonth() - 1 : 12;
        throughMonth = Math.max(throughMonth, 1);
        model.addAttribute("year", y);
        model.addAttribute("throughMonth", throughMonth);
        model.addAttribute("donationStats", reportStatisticsService.donationStatsByLocgov(y, throughMonth, false, false));
        model.addAttribute("donationStatsCapped", reportStatisticsService.donationStatsByLocgov(y, throughMonth, true, false));
        model.addAttribute("giftStats", reportStatisticsService.giftStatsByLocgov(y, throughMonth, false));
        return "shop-statistics/report-locgov";
    }

    @GetMapping("/shop-statistics/report/year")
    public String year(Model model) {
        int currentYear = reportStatisticsService.currentYear();
        model.addAttribute("yearlyStats", reportStatisticsService.yearlyTrend(currentYear - 4, currentYear));
        return "shop-statistics/report-year";
    }
}

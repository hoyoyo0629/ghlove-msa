package com.ghlove.admin.web;

import com.ghlove.admin.service.VisitStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * 통계 > 접속통계 (AS-IS {@code StatsManagerController#visit}/{@code visitPost}/{@code visitDay} +
 * {@code stats/visit/index.jsp}·{@code day.jsp}). GET은 요약만 채우고 월별/일별 차트는 비워 둔다(AS-IS가
 * 빈 목록을 하드코딩), 검색(POST)에서 월별 막대와 "이번 달" 일별 막대를 채운다. 월 이름을 누르면 그 달의
 * 일별 막대만 ajax로 갈아끼운다.
 *
 * <p>엑셀 다운로드(AS-IS {@code visit/download-excel})는 {@code ROLE_EXCEL}(판매자 전용 권한)에게만 버튼이
 * 보이고 운영자에게는 안 보이는 비활성 기능이라([[admin-sales-day-statistics-port]]의 일자별 매출 엑셀과 같은
 * 사정) 만들지 않았다.
 */
@Controller
@RequiredArgsConstructor
public class VisitStatsController {

    private static final int FIRST_YEAR = 2013;

    private final VisitStatsService service;

    @GetMapping("/stats/visit/index")
    public String visit(@ModelAttribute("searchParam") VisitSearchParam param, Model model) {
        attrs(model, true);
        model.addAttribute("list", List.<VisitStatsService.MonthStats>of());
        model.addAttribute("dayStatsList", List.<VisitStatsService.DayStats>of());
        return "stats/visit";
    }

    @PostMapping("/stats/visit/index")
    public String visitPost(@ModelAttribute("searchParam") VisitSearchParam param, Model model) {
        attrs(model, true);
        LocalDate today = LocalDate.now();
        model.addAttribute("list", service.monthStats(param.getStartDate(), param.getEndDate()));
        model.addAttribute("dayStatsList",
                service.dayStats(String.format("%04d%02d", today.getYear(), today.getMonthValue())));
        return "stats/visit";
    }

    /** 월별 막대의 월 이름 클릭 - 그 달의 일별 접속통계 조각만 돌려준다(AS-IS layout=blank). */
    @GetMapping("/stats/visit/day")
    public String visitDay(@RequestParam("yearMonth") String yearMonth, Model model) {
        if (yearMonth != null && yearMonth.length() >= 6) {
            model.addAttribute("year", yearMonth.substring(0, 4));
            model.addAttribute("month", yearMonth.substring(4, 6));
            model.addAttribute("dayStatsList", service.dayStats(yearMonth.substring(0, 6)));
        }
        return "stats/visit-day";
    }

    private void attrs(Model model, boolean withSummary) {
        LocalDate today = LocalDate.now();
        model.addAttribute("currentYear", today.getYear());
        model.addAttribute("currentMonth", String.format("%02d", today.getMonthValue()));
        List<Integer> years = new java.util.ArrayList<>();
        for (int y = FIRST_YEAR; y <= today.getYear(); y++) {
            years.add(y);
        }
        model.addAttribute("years", years);
        if (withSummary) {
            model.addAttribute("summary", service.summary());
        }
    }

    /** AS-IS StatsSearchParam - 시작/종료 "년도". 비면 올해. */
    public static class VisitSearchParam {
        private String startDate;
        private String endDate;

        public String getStartDate() {
            return (startDate == null || startDate.isBlank()) ? String.valueOf(LocalDate.now().getYear()) : startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return (endDate == null || endDate.isBlank()) ? String.valueOf(LocalDate.now().getYear()) : endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}

package com.ghlove.admin.web;

import com.ghlove.admin.service.ShopStatisticsReportGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.Collections;

/**
 * 통계 > 보고서 > 지자체별 누계 현황 (AS-IS {@code ShopStatisticsManagerController#mctpvStats/searchMctpvStats} +
 * {@code report/mctpv.jsp}). 이전 구현(연도 입력·원장 즉석집계·탭 3개 단순표)을 AS-IS 사양으로 교체했다(2026-10-15).
 * 총괄 현황과 같은 틀 - 탭 3개(지자체별 기부금 누계 / 2000만원 누계 / 지자체별 답례품 제공현황)를 한 페이지에 그려 두고
 * JS가 전환하며 <b>GET은 표를 비우고 POST(검색)에서 채운다</b>. 기본 탭은 3번. "엑셀"은 report/lclgv/excel-download.
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsReportMctpvController {

    private final ShopStatisticsReportGeneralService service;

    @GetMapping("/shop-statistics/report/mctpv")
    public String mctpvStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param, Model model) {
        fill(model, param);
        model.addAttribute("mctpvDntnStats", Collections.emptyList());
        model.addAttribute("mctpvDntnMaxAmtStats", Collections.emptyList());
        model.addAttribute("mctpvGdsStats", Collections.emptyList());
        return "shop-statistics/report-mctpv";
    }

    @PostMapping("/shop-statistics/report/mctpv")
    public String searchMctpvStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param, Model model) {
        fill(model, param);
        String shYear = param.getShYear();
        int shMonth = param.getShMonth();
        model.addAttribute("mctpvDntnStats", service.mctpvRows("mctpv_dntn_stats", shYear, shMonth, false));
        model.addAttribute("mctpvDntnMaxAmtStats", service.mctpvRows("mctpv_dntn_max_amt_stats", shYear, shMonth, true));
        model.addAttribute("mctpvGdsStats", service.mctpvRows("mctpv_gds_stats", shYear, shMonth, false));
        return "shop-statistics/report-mctpv";
    }

    /** AS-IS 공통 모델 - 검색월이 없으면 직전월. 기준년도 select는 기부 통계표에서. */
    private void fill(Model model, ShopStatisticsReportGeneralController.ReportParam param) {
        LocalDate now = LocalDate.now();
        if (param.getShMonth() < 1) {
            LocalDate lastMonth = now.minusMonths(1);
            param.setShMonth(lastMonth.getMonthValue());
            param.setShYear("" + lastMonth.getYear());
        }
        model.addAttribute("crtrYear", service.crtrYears("2", true));
        model.addAttribute("nowYear", String.valueOf(now.getYear()));
        model.addAttribute("nowMonth", now.getMonthValue());
        model.addAttribute("tabId", param.getTabId());
    }
}

package com.ghlove.admin.web;

import com.ghlove.admin.service.ShopStatisticsReportGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * 통계 > 보고서 > 총괄 누계 현황 (AS-IS {@code ShopStatisticsManagerController#generalTotalStats/searchGeneralTotalStats} +
 * {@code report/generalTotal.jsp}). 이전 구현(원장 즉석집계 4표, 기간·탭·엑셀 없음)을 AS-IS 사양으로 교체했다(2026-10-15).
 * 총괄 현황과 같은 구조 - 탭 4개(기부방법별 / 금액별 / 연령별 / 거주지역->기부지역)를 한 페이지에 다 그려 두고 JS가 전환하며,
 * <b>GET은 표를 비우고 POST(검색)에서 채운다</b>. 기간은 년도+월 select 둘 다 보인다. "엑셀"은 총괄 현황과 같은
 * {@code report/lclgv/excel-download}를 부른다({@link ShopStatisticsReportGeneralController}).
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsReportGeneralTotalController {

    private static final ShopStatisticsReportGeneralService.GeneralTotalView EMPTY =
            new ShopStatisticsReportGeneralService.GeneralTotalView("", List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    private final ShopStatisticsReportGeneralService service;

    @GetMapping("/shop-statistics/report/general-total")
    public String generalTotalStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param,
                                    Model model) {
        fill(model, param);
        model.addAttribute("total", EMPTY);
        return "shop-statistics/report-general-total";
    }

    @PostMapping("/shop-statistics/report/general-total")
    public String searchGeneralTotalStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param,
                                          Model model) {
        fill(model, param);
        model.addAttribute("total", service.generalTotal(param.getShYear(), param.getShMonth()));
        return "shop-statistics/report-general-total";
    }

    /** AS-IS 공통 모델 - 검색월이 없으면 직전월(전월이 작년 12월이면 작년으로). 기준년도 select는 기부 통계표(DNTN_TNOCS_STATS)에서. */
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

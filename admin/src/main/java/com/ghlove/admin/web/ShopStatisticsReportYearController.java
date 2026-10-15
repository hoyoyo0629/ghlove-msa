package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ShopStatisticsReportGeneralService;
import com.ghlove.admin.service.ShopStatisticsReportGeneralService.CodeName;
import com.ghlove.admin.service.ShopStatisticsReportYearService;
import com.ghlove.admin.web.support.ShopStatisticsStoredPastYearExcelView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 통계 > 보고서 > 연간통계 현황 (AS-IS {@code ShopStatisticsManagerController#yearStats} + {@code report/year.jsp} +
 * {@code report/lclgv/excel-pastYearStatsStored}). 이전 구현(최근 5년 연도별 건수·금액 표)을 AS-IS 사양으로 교체했다(2026-10-15).
 * 화면은 조회 버튼이 없다 - 년도·시/도·시/군/구를 고르고 "엑셀"을 누르면 그 지자체의 연간 집계 시트 8장이 내려간다.
 * 시/군/구 목록은 지자체 현황과 같은 {@code report/lclgv/options-by-lclgv}를 쓴다.
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsReportYearController {

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShopStatisticsReportGeneralService generalService;
    private final ShopStatisticsReportYearService service;

    @GetMapping("/shop-statistics/report/year")
    public String yearStats(@ModelAttribute("statisticsParam") YearParam param, HttpSession session, Model model) {
        List<CodeName> mctpv = generalService.mctpvOptions(param.getTabId());
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        ShopStatisticsReportGeneralService.Scope scope = generalService.scope(manager, mctpv);
        model.addAttribute("yyyy", service.yearOptions());
        model.addAttribute("mctpv", mctpv);
        model.addAttribute("scoped", scope.scoped());
        model.addAttribute("scopedName", scope.name());
        return "shop-statistics/report-year";
    }

    /**
     * AS-IS GET report/lclgv/excel-pastYearStatsStored. 지자체 담당자(ROLE_ADMIN_5/6)는 소속 지자체가 있으면 시/도·지자체가
     * 그것으로 덮어써진다(광역이면 둘 다 광역 코드). 시스템/행안부는 화면에서 고른 값 그대로 - 값이 없으면 결과 시트가 비어 나온다.
     */
    @GetMapping("/shop-statistics/report/lclgv/excel-pastYearStatsStored")
    public void excelPastYearStatsStored(@ModelAttribute("statisticsParam") YearParam param, HttpServletRequest request,
                                         HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        ShopStatisticsReportGeneralService.Scope scope = generalService.scope(manager, List.of());
        String yr = param.getShYear();
        String wdr = param.getShWdr();
        String locgov = param.getShLocgovCode();
        if (scope.hasLocgov()) {
            wdr = scope.mctpvCd();
            locgov = scope.lclgvCd();
        }

        ShopStatisticsStoredPastYearExcelView excel = new ShopStatisticsStoredPastYearExcelView();
        excel.build(service.sum(yr, wdr, locgov), service.path(yr, wdr, locgov), service.price(yr, wdr, locgov),
                service.ages(yr, wdr, locgov), service.psint(yr, wdr, locgov), service.psintLocgov(yr, wdr, locgov),
                service.month(yr, wdr, locgov), service.goods(yr, wdr, locgov));
        excel.view().write(request, response, "연통계_보고서_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx");
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 탭, 년도, 시/도(shWdr), 시/군/구(shLocgovCode). */
    public static class YearParam {
        private String tabId;
        private String shYear;
        private String shWdr;
        private String shLocgovCode;

        public String getTabId() {
            return tabId;
        }

        public void setTabId(String tabId) {
            this.tabId = tabId;
        }

        public String getShYear() {
            return shYear;
        }

        public void setShYear(String shYear) {
            this.shYear = shYear;
        }

        public String getShWdr() {
            return shWdr;
        }

        public void setShWdr(String shWdr) {
            this.shWdr = shWdr;
        }

        public String getShLocgovCode() {
            return shLocgovCode;
        }

        public void setShLocgovCode(String shLocgovCode) {
            this.shLocgovCode = shLocgovCode;
        }
    }
}

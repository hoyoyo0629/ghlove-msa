package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.ShopStatisticsReportGeneralService;
import com.ghlove.admin.service.ShopStatisticsReportGeneralService.CodeName;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 보고서 > 지자체 현황 (AS-IS {@code ShopStatisticsManagerController#lclgvStats/searchLclgvStats/optionslclgvByLclgv}
 * + {@code report/lclgv.jsp}). 이전 구현(연도 입력·원장 즉석집계)을 AS-IS 사양으로 교체했다(2026-10-15).
 * 총괄 누계·지자체별 누계와 같은 틀(탭 3개, GET 빈 표, POST 채움, 엑셀=lclgv/excel-download)에 <b>시/도 → 시/군/구 2단 select</b>가
 * 더해진다. 지자체 담당자(ROLE_ADMIN_5/6)는 select 대신 자기 소속 이름만 보이고 조회 범위가 소속으로 고정된다.
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsReportLclgvController {

    private final ShopStatisticsReportGeneralService service;

    @GetMapping("/shop-statistics/report/lclgv")
    public String lclgvStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param,
                             HttpSession session, Model model) {
        fill(model, param, session);
        model.addAttribute("lclgvAodStats", Collections.emptyList());
        model.addAttribute("lclgvAodMaxAmtStats", Collections.emptyList());
        model.addAttribute("lclgvGdsStats", Collections.emptyList());
        return "shop-statistics/report-lclgv";
    }

    @PostMapping("/shop-statistics/report/lclgv")
    public String searchLclgvStats(@ModelAttribute("statisticsParam") ShopStatisticsReportGeneralController.ReportParam param,
                                   HttpSession session, Model model) {
        fill(model, param, session);
        String shYear = param.getShYear();
        int shMonth = param.getShMonth();
        String shMctpv = param.getShMctpv();
        String shLclgv = param.getShLclgv();
        model.addAttribute("lclgvAodStats", service.lclgvRows("lclgv_aod_stats", shYear, shMonth, shMctpv, shLclgv, true));
        model.addAttribute("lclgvAodMaxAmtStats", service.lclgvRows("lclgv_aod_max_amt_stats", shYear, shMonth, shMctpv, shLclgv, true));
        model.addAttribute("lclgvGdsStats", service.lclgvRows("lclgv_gds_stats", shYear, shMonth, shMctpv, shLclgv, false));
        return "shop-statistics/report-lclgv";
    }

    /** AS-IS POST report/lclgv/options-by-lclgv - 시/도를 고르면 시/군/구 select를 채운다(3번 탭은 답례품 표, 그 외는 기부 표 기준). */
    @PostMapping("/shop-statistics/report/lclgv/options-by-lclgv")
    @ResponseBody
    public List<Map<String, String>> optionsByLclgv(@RequestParam(name = "mctpv", defaultValue = "") String mctpv,
                                                    @RequestParam(name = "tabId", defaultValue = "1") String tabId) {
        return service.lclgvOptions(tabId, mctpv).stream().map(CodeName::asLclgvJson).toList();
    }

    /**
     * AS-IS 공통 모델. 지자체 담당자(ROLE_ADMIN_5/6)는 소속 지자체가 있으면 검색 조건을 그것으로 덮어쓴다 -
     * 광역이면 시/도만, 기초면 시/도+지자체.
     */
    private void fill(Model model, ShopStatisticsReportGeneralController.ReportParam param, HttpSession session) {
        LocalDate now = LocalDate.now();
        if (param.getShMonth() < 1) {
            LocalDate lastMonth = now.minusMonths(1);
            param.setShMonth(lastMonth.getMonthValue());
            param.setShYear("" + lastMonth.getYear());
        }
        String tabId = param.getTabId();
        List<CodeName> mctpv = service.mctpvOptions(tabId);

        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        ShopStatisticsReportGeneralService.Scope scope = service.scope(manager, mctpv);
        if (scope.hasLocgov()) {
            param.setShMctpv(scope.mctpvCd());
            if (!scope.lclgvCd().equals(scope.mctpvCd())) {
                param.setShLclgv(scope.lclgvCd());
            }
        }

        model.addAttribute("scoped", scope.scoped());
        model.addAttribute("scopedName", scope.name());
        model.addAttribute("crtrYear", service.crtrYears("2", true));
        model.addAttribute("nowYear", String.valueOf(now.getYear()));
        model.addAttribute("nowMonth", now.getMonthValue());
        model.addAttribute("tabId", tabId);
        model.addAttribute("mctpv", mctpv);
    }
}

package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.ShopStatisticsCategoriesService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 카테고리별 (AS-IS {@code ShopStatisticsManagerController#salesCategories} +
 * {@code sales/categories.jsp} + {@code getCategoriesStatsList}). 이전 구현(ShopStatisticsTierBController
 * "H.")은 카테고리 코드별 건수/금액 한 줄짜리 축소판이라 전면 교체했다(2026-10-14).
 *
 * <p>GET은 조회하지 않고(AS-IS가 빈 목록을 하드코딩) POST에서만 집계한다. 지자체담당자(ROLE_ADMIN_5/6)는
 * 지자체 select가 안 보이고 자기 지자체로 고정된다(AS-IS adminRole=='LOC').
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsCategoriesController {

    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final ShopStatisticsCategoriesService service;
    private final LocgovClient locgovClient;

    @GetMapping("/shop-statistics/sales/categories")
    public String categories(@ModelAttribute("statisticsParam") CategoriesParam param,
                              HttpSession session, Model model) {
        formAttrs(model, manager(session));
        model.addAttribute("list", List.<ShopStatisticsCategoriesService.Row>of());
        return "shop-statistics/sales-categories";
    }

    @PostMapping("/shop-statistics/sales/categories")
    public String searchCategories(@ModelAttribute("statisticsParam") CategoriesParam param,
                                    HttpSession session, Model model) {
        Manager manager = manager(session);
        String locgovCode = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : param.getShLocgovCode();
        List<ShopStatisticsCategoriesService.Row> rows =
                service.search(manager, param.getStartDate(), param.getEndDate(), locgovCode);
        formAttrs(model, manager);
        model.addAttribute("list", rows);
        return "shop-statistics/sales-categories";
    }

    private void formAttrs(Model model, Manager manager) {
        boolean sys = !MenuService.isLocgovScoped(manager);
        model.addAttribute("showLocgov", sys);
        // AS-IS: ROLE_ADMIN_1 또는 ROLE_ADMIN_5 에게만 "전체" 날짜버튼
        model.addAttribute("showAllButton",
                "ROLE_ADMIN_1".equals(manager.getAuthority()) || "ROLE_ADMIN_5".equals(manager.getAuthority()));
        Map<String, String> provinces = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            provinces.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        model.addAttribute("provinces", provinces);
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 기간은 게터가 기본값(이번 달 1일~오늘)을 만든다. */
    public static class CategoriesParam {
        private String startDate;
        private String endDate;
        private String shWdr;
        private String shLocgovCode;

        public String getStartDate() {
            return (startDate == null || startDate.isBlank())
                    ? LocalDate.now().withDayOfMonth(1).format(YMD) : startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return (endDate == null || endDate.isBlank()) ? LocalDate.now().format(YMD) : endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
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

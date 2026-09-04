package com.ghlove.admin.web;

import com.ghlove.admin.service.ShopStatisticsTierBService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** AS-IS opmanager/shop-statistics의 "판매(sales)/대시보드/선호도" 스위트 재구현
 *  (report 스위트는 {@link ReportStatisticsController}가 별개로 담당). */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsTierBController {

    private final ShopStatisticsTierBService service;

    // ---- A. 기간별 매출 통계 ----

    @GetMapping({"/shop-statistics/sales/day", "/shop-statistics/sales/month", "/shop-statistics/sales/year"})
    public String period(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
                          jakarta.servlet.http.HttpServletRequest request, Model model) {
        String uri = request.getRequestURI();
        String type = uri.substring(uri.lastIndexOf('/') + 1);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stats", service.periodStats(type, startDate, endDate));
        return "shop-statistics/sales-period";
    }

    // ---- B. 답례품 구매현황 전체 ----

    @GetMapping("/shop-statistics/sales/all")
    public String all(@RequestParam(required = false) String year, Model model) {
        model.addAttribute("summary", service.allSales(year));
        return "shop-statistics/sales-all";
    }

    // ---- C. 결제타입별 매출 통계 ----

    @GetMapping("/shop-statistics/sales/payment")
    public String payment(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, Model model) {
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stats", service.paymentStats(startDate, endDate));
        return "shop-statistics/sales-payment";
    }

    // ---- D. 회원별 매출 통계 ----

    @GetMapping("/shop-statistics/sales/user")
    public String user(Model model) {
        model.addAttribute("stats", service.userStats());
        return "shop-statistics/sales-user";
    }

    @GetMapping({"/shop-statistics/sales/user/user-order", "/shop-statistics/sales/user/order-detail"})
    public String userDetail(@RequestParam Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("orders", service.userOrders(userId));
        return "shop-statistics/sales-user-detail";
    }

    // ---- E. 판매자별 통계 ----

    @GetMapping("/shop-statistics/sales/seller")
    public String seller(Model model) {
        model.addAttribute("stats", service.sellerStats());
        return "shop-statistics/sales-seller";
    }

    // ---- F. 브랜드별 통계 ----

    @GetMapping("/shop-statistics/sales/brand")
    public String brand(Model model) {
        model.addAttribute("stats", service.brandStats());
        return "shop-statistics/sales-brand";
    }

    @GetMapping("/shop-statistics/sales/brand/brand-detail")
    public String brandDetail(@RequestParam(required = false) Integer brandId, Model model) {
        model.addAttribute("brandId", brandId);
        model.addAttribute("orders", service.brandOrders(brandId));
        return "shop-statistics/sales-brand-detail";
    }

    // ---- G. 상품별 판매 통계 ----

    @GetMapping("/shop-statistics/sales/item")
    public String item(Model model) {
        model.addAttribute("stats", service.itemStats());
        return "shop-statistics/sales-item";
    }

    @GetMapping("/shop-statistics/sales/item/day")
    public String itemDay(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, Model model) {
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stats", service.itemStatsByPeriod(startDate, endDate));
        return "shop-statistics/sales-item-day";
    }

    @GetMapping("/shop-statistics/sales/item/{itemId}")
    public String itemDetail(@PathVariable Long itemId, Model model) {
        model.addAttribute("itemId", itemId);
        model.addAttribute("orders", service.itemOrders(itemId));
        return "shop-statistics/sales-item-detail";
    }

    // ---- H. 카테고리별 통계 (AS-IS /sales/category는 미사용 코드주석 - /sales/categories로 통합) ----

    @GetMapping("/shop-statistics/sales/category")
    public String categoryLegacyRedirect() {
        return "redirect:/shop-statistics/sales/categories";
    }

    @GetMapping("/shop-statistics/sales/categories")
    public String categories(Model model) {
        model.addAttribute("stats", service.categoryStats());
        return "shop-statistics/sales-categories";
    }

    // ---- I. 지역별 통계 ----

    @GetMapping("/shop-statistics/sales/area")
    public String area(Model model) {
        model.addAttribute("stats", service.areaStats());
        return "shop-statistics/sales-area";
    }

    @GetMapping("/shop-statistics/sales/area/detail")
    public String areaDetail(@RequestParam String locgovCode, Model model) {
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("orders", service.areaOrders(locgovCode));
        return "shop-statistics/sales-area-detail";
    }

    // ---- J. 매출 제로 상품 내역 ----

    @GetMapping("/shop-statistics/sales/no-sales")
    public String noSales(Model model) {
        model.addAttribute("items", service.noSaleItems());
        return "shop-statistics/sales-no-sales";
    }

    // ---- K. 미이용자 목록 ----

    @GetMapping("/shop-statistics/no-user")
    public String noUser(Model model) {
        model.addAttribute("users", service.noUsers());
        return "shop-statistics/no-user";
    }

    // ---- L. 월별 지자체별 답례품 현황 ----

    @GetMapping("/shop-statistics/sales/month-locgov")
    public String monthLocgov(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, Model model) {
        int y = year != null ? year : service.currentYear();
        int m = month != null ? month : service.currentMonth();
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        model.addAttribute("stats", service.monthLocgovTop3(y, m));
        return "shop-statistics/sales-month-locgov";
    }

    // ---- M. 지자체별 구매현황 ----

    @GetMapping("/shop-statistics/sales/locgov")
    public String locgov(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : service.currentYear();
        model.addAttribute("year", y);
        model.addAttribute("stats", service.locgovStats(y));
        return "shop-statistics/sales-locgov";
    }

    @GetMapping("/shop-statistics/sales/locgov/detail")
    public String locgovDetail(@RequestParam String locgovCode, @RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : service.currentYear();
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("year", y);
        model.addAttribute("stats", service.locgovUserStats(locgovCode, y));
        return "shop-statistics/sales-locgov-detail";
    }

    // ---- N. 답례품 선호도 ----

    @GetMapping("/shop-statistics/wish/list")
    public String wishList(Model model) {
        model.addAttribute("stats", service.wishStats());
        return "shop-statistics/wish-list";
    }

    // ---- O/P. 대시보드 ----

    @GetMapping("/shop-statistics/dashboard/day")
    public String dashboardDay(@RequestParam(required = false) String date, Model model) {
        model.addAttribute("dashboard", service.dashboardDay(date));
        return "shop-statistics/dashboard-day";
    }

    @GetMapping("/shop-statistics/dashboard/month")
    public String dashboardMonth(@RequestParam(required = false) String yearMonth, Model model) {
        model.addAttribute("dashboard", service.dashboardMonth(yearMonth));
        return "shop-statistics/dashboard-month";
    }

    // ---- R. 대시보드 텍스트 리포트 (AS-IS opmanager/report - ReportController, 행안부 일일/주간보고
    // 클립보드복사용 텍스트 생성. 이미 있는 대시보드 데이터를 정형 텍스트로만 포맷팅한다) ----

    @GetMapping("/shop-statistics/dashboard/day/report")
    @org.springframework.web.bind.annotation.ResponseBody
    public String dashboardDayReport(@RequestParam(required = false) String date) {
        return service.dashboardDayReportText(service.dashboardDay(date));
    }

    @GetMapping("/shop-statistics/dashboard/month/report")
    @org.springframework.web.bind.annotation.ResponseBody
    public String dashboardMonthReport(@RequestParam(required = false) String yearMonth) {
        return service.dashboardMonthReportText(service.dashboardMonth(yearMonth));
    }

    // ---- Q. 농협 사업자 답례품 현황 ----

    @GetMapping("/shop-statistics/sales/nh-item-sales")
    public String nhItemSales(Model model) {
        model.addAttribute("stats", service.nhItemSales());
        return "shop-statistics/sales-nh-item-sales";
    }
}

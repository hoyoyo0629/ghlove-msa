package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ShopStatisticsNhItemSalesService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 농협 사업자 답례품 현황
 * (AS-IS {@code ShopStatisticsManagerController#nhItemSalesStat} + {@code sales/nh-item-sales.jsp}
 * + {@code getNhItemSalesStats} 매퍼). 이전 구현({@code ShopStatisticsTierBController} "Q. 농협
 * 사업자 답례품 현황")은 bankName에 "농협"이 포함된 판매자를 가리는 휴리스틱이었다 - AS-IS는
 * {@code G_NH_STAT_DATA_TEMP}(외부에서 받은 농협 사업자번호 목록)와 사업자번호를 매칭한다.
 * 전면 교체했다(2026-10-14).
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsNhItemSalesController {

    private final ShopStatisticsNhItemSalesService service;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final ExcelDownloadWriter excelDownloadWriter;

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** AS-IS GET - 조회조건만 채우고 집계는 하지 않는다(다른 매출통계 화면들과 같은 패턴). */
    @GetMapping("/shop-statistics/sales/nh-item-sales")
    public String nhItemSales(@ModelAttribute("statisticsParam") NhItemSalesParam param,
                               HttpServletRequest request, Model model) {
        formAttrs(model, param);
        model.addAttribute("list", List.<ShopStatisticsNhItemSalesService.NhItemRow>of());
        model.addAttribute("totalCount", 0);
        model.addAttribute("pagination", Pagination.of(0, param.getPage(), param.getItemsPerPage()).withLinkFrom(request));
        return "shop-statistics/sales-nh-item-sales";
    }

    @PostMapping("/shop-statistics/sales/nh-item-sales")
    public String searchNhItemSales(@ModelAttribute("statisticsParam") NhItemSalesParam param,
                                     HttpServletRequest request, HttpSession session, Model model) {
        formAttrs(model, param);
        ShopStatisticsNhItemSalesService.SearchResult result = service.search(manager(session), param.toSearchParam());
        model.addAttribute("list", result.rows());
        model.addAttribute("totalCount", result.totalCount());
        model.addAttribute("pagination",
                Pagination.of(result.totalCount(), param.getPage(), param.getItemsPerPage()).withLinkFrom(request));
        return "shop-statistics/sales-nh-item-sales";
    }

    /** AS-IS Shop.downloadExcelOrder(..., false) - 개인정보 사유 모달 없이 바로 다운로드. */
    @GetMapping("/shop-statistics/sales/nh-item-sales/excel")
    public void excelDownload(@ModelAttribute("statisticsParam") NhItemSalesParam param,
                               HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        ShopStatisticsNhItemSalesService.SearchParam searchParam = new ShopStatisticsNhItemSalesService.SearchParam(
                param.getUpperLocgovCode(), param.getLocgovCode(), param.getWhere(), param.getQuery(),
                param.getSearchYear(), param.getSearchMonth(), 1, Integer.MAX_VALUE);
        ShopStatisticsNhItemSalesService.SearchResult result = service.search(manager, searchParam);

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("농협 사업자 답례품 현황");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);
        String[] headers = {"No", "사업자번호", "아이디", "상호명", "지자체", "답례품명", "카테고리", "하위카테고리",
                "가격(단가)", "주문수", "판매개수", "총 판매액", "상품코드"};
        int[] widths = {700, 3500, 3500, 4500, 3000, 7000, 3000, 3500, 3000, 2500, 2500, 3500, 3000};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "농협 사업자 답례품 현황", headers, widths);

        // AS-IS idx = list.size() 부터 거꾸로 - 화면의 내림차순 No와 같은 숫자가 나와야 한다.
        int no = result.rows().size();
        for (ShopStatisticsNhItemSalesService.NhItemRow r : result.rows()) {
            Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(no--));
            excelDownloadWriter.dataCell(xl, c++, styles, r.businessNumber());
            excelDownloadWriter.dataCell(xl, c++, styles, r.loginId());
            excelDownloadWriter.dataCell(xl, c++, styles, r.companyName());
            excelDownloadWriter.dataCell(xl, c++, styles,
                    (r.upperLocgovNm() != null ? r.upperLocgovNm() + " " : "") + r.locgovNm());
            excelDownloadWriter.dataCell(xl, c++, styles, r.itemName());
            excelDownloadWriter.dataCell(xl, c++, styles, r.categoryNm());
            excelDownloadWriter.dataCell(xl, c++, styles, r.categoryDetailNm());
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.price()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.orderCnt()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.saleCnt()));
            excelDownloadWriter.dataCell(xl, c++, styles, String.format("%,d", r.totalSalePrice()));
            excelDownloadWriter.dataCell(xl, c, styles, r.itemUserCode());
        }

        String fileName = "농협_사업자_답례품_현황_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    private void formAttrs(Model model, NhItemSalesParam param) {
        model.addAttribute("statisticsParam", param);
        model.addAttribute("years", yearCodes());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private List<String> yearCodes() {
        List<String> years = new java.util.ArrayList<>(commonCodeService.labelsOf("YYYY").keySet());
        if (years.isEmpty()) {
            years.add(String.valueOf(LocalDate.now().getYear()));
        }
        return years;
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 지자체 2단 + 검색구분/검색어 + 년/월 + 페이징. */
    public static class NhItemSalesParam {
        private String upperLocgovCode;
        private String locgovCode;
        private String where = "";
        private String query;
        private String searchYear;
        private String searchMonth;
        private int page = 1;
        private int itemsPerPage = 10;

        public String getUpperLocgovCode() {
            return upperLocgovCode;
        }

        public void setUpperLocgovCode(String upperLocgovCode) {
            this.upperLocgovCode = upperLocgovCode;
        }

        public String getLocgovCode() {
            return locgovCode;
        }

        public void setLocgovCode(String locgovCode) {
            this.locgovCode = locgovCode;
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /** AS-IS StatisticsParam.searchYear 기본값 - 비었으면 올해. */
        public String getSearchYear() {
            return (searchYear == null || searchYear.isBlank())
                    ? String.valueOf(LocalDate.now().getYear()) : searchYear;
        }

        public void setSearchYear(String searchYear) {
            this.searchYear = searchYear;
        }

        /** AS-IS StatisticsParam.getSearchMonth() - 2자리로 0패딩. 비었으면 이번 달. */
        public String getSearchMonth() {
            String m = (searchMonth == null || searchMonth.isBlank())
                    ? LocalDate.now().format(DateTimeFormatter.ofPattern("MM")) : searchMonth;
            m = "0" + m;
            return m.substring(m.length() - 2);
        }

        public void setSearchMonth(String searchMonth) {
            this.searchMonth = searchMonth;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }

        ShopStatisticsNhItemSalesService.SearchParam toSearchParam() {
            return new ShopStatisticsNhItemSalesService.SearchParam(
                    upperLocgovCode, locgovCode, where, query, getSearchYear(), getSearchMonth(), page, itemsPerPage);
        }
    }
}

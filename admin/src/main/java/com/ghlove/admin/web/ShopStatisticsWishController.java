package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.ShopStatisticsWishService;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 선호도 (AS-IS {@code ShopStatisticsManagerController#wishList} +
 * {@code wish/list.jsp} + {@code getOpWishlistList} + {@code ShopWish*ExcelView}).
 * 이전 구현(TierB "N.")은 상품ID·찜 건수만 한 표로 보여주는 축소판(지자체 필터·검색·페이징·엑셀 없음)이라
 * 전면 교체했다(2026-10-14).
 *
 * <p>역할: 시스템/행안부(ROLE_ADMIN_1~4="mois")는 지자체 2단 select + 지자체 열, 지자체담당자
 * (ROLE_ADMIN_5/6)는 자기 지자체명 고정·지자체 열 없음. GET은 조회하지 않는다.
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsWishController {

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShopStatisticsWishService service;
    private final LocgovClient locgovClient;
    private final ExcelDownloadWriter excelDownloadWriter;

    @GetMapping("/shop-statistics/wish/list")
    public String wishList(@ModelAttribute("wishlistParam") WishParam param,
                            HttpServletRequest request, HttpSession session, Model model) {
        attrs(model, manager(session));
        model.addAttribute("dateList", List.<ShopStatisticsWishService.WishRow>of());
        model.addAttribute("count", 0);
        model.addAttribute("pagination", Pagination.of(0, param.getPage(), param.getItemsPerPage()).withLinkFrom(request));
        return "shop-statistics/wish-list";
    }

    @PostMapping("/shop-statistics/wish/list")
    public String wishListPost(@ModelAttribute("wishlistParam") WishParam param,
                                HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        List<ShopStatisticsWishService.WishRow> all = service.search(
                param.getUpperLocgovCode(), effectiveLocgov(manager, param), param.getItemName());
        Pagination pagination = Pagination.of(all.size(), param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        int from = Math.min(pagination.getStartRow(), all.size());
        int to = Math.min(from + pagination.getItemsPerPage(), all.size());
        attrs(model, manager);
        model.addAttribute("dateList", all.subList(from, to));
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "shop-statistics/wish-list";
    }

    /** AS-IS GET wish/list/mois/download-excel - 조건 전체(페이징 없음), No. 는 1부터 오름차순. */
    @GetMapping("/shop-statistics/wish/list/mois/download-excel")
    public void excelMois(@ModelAttribute("wishlistParam") WishParam param, HttpSession session,
                           HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        List<ShopStatisticsWishService.WishRow> rows = service.search(
                param.getUpperLocgovCode(), param.getLocgovCode(), param.getItemName());
        // AS-IS ShopWishMoisExcelView - 제목행 문구가 "답례품 구매현황"(복붙 흔적 그대로), 파일명은 "답례품 선호도_"
        write(response, manager, rows, "답례품 구매현황", "답례품 선호도_", true);
    }

    /** AS-IS GET wish/list/locgov/download-excel - 자기 지자체로 고정, No. 는 총건수부터 내림차순. */
    @GetMapping("/shop-statistics/wish/list/locgov/download-excel")
    public void excelLocgov(@ModelAttribute("wishlistParam") WishParam param, HttpSession session,
                             HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        String locgovCode = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : param.getLocgovCode();
        List<ShopStatisticsWishService.WishRow> rows = service.search(
                param.getUpperLocgovCode(), locgovCode, param.getItemName());
        write(response, manager, rows, "답례품 선호도", "답례품선호도_", false);
    }

    private void write(HttpServletResponse response, Manager manager, List<ShopStatisticsWishService.WishRow> rows,
                        String title, String filePrefix, boolean mois) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("main");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);
        CellStyle numStyle = workbook.createCellStyle();
        numStyle.cloneStyleFrom(styles.data());
        numStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        String[] headers = mois
                ? new String[]{"No.", "등록수", "답례품명", "상호명", "금액", "지자체"}
                : new String[]{"No.", "등록수", "답례품명", "상호명", "금액"};
        int[] widths = mois
                ? new int[]{700, 3500, 3500, 3500, 3500, 3500}
                : new int[]{700, 1000, 10000, 3500, 3500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, title, headers, widths);

        int no = mois ? 0 : rows.size();
        for (ShopStatisticsWishService.WishRow r : rows) {
            Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(mois ? ++no : no--));
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(r.registrationNumber()));
            excelDownloadWriter.dataCell(xl, c++, styles, r.itemName());
            excelDownloadWriter.dataCell(xl, c++, styles, r.companyName());
            Cell amount = xl.createCell(c++);
            amount.setCellValue(r.salePrice());
            amount.setCellStyle(numStyle);
            if (mois) {
                excelDownloadWriter.dataCell(xl, c, styles, r.locgovNm());
            }
        }
        String fileName = filePrefix + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    private String effectiveLocgov(Manager manager, WishParam param) {
        return MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : param.getLocgovCode();
    }

    private void attrs(Model model, Manager manager) {
        String authority = manager.getAuthority();
        boolean mois = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4").contains(authority);
        boolean locgov = MenuService.isLocgovScoped(manager);
        model.addAttribute("role", mois ? "mois" : (locgov ? "locgov" : ""));
        Map<String, String> provinces = new LinkedHashMap<>();
        String locgovNm = "";
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            provinces.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
            if (locgov && l.locgovCode().equals(manager.getLocgovCode())) {
                // AS-IS: UPPER_LOCGOV_NM + 코드라벨(공백 없이 붙인다)
                locgovNm = (l.upperLocgovNm() == null ? "" : l.upperLocgovNm()) + l.locgovNm();
            }
        }
        model.addAttribute("provinces", provinces);
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovNm", locgovNm);
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS WishlistParam 중 이 화면이 쓰는 것만. */
    public static class WishParam {
        private String upperLocgovCode;
        private String locgovCode;
        private String itemName;
        private int page = 1;
        private int itemsPerPage = 10;

        public String getUpperLocgovCode() {
            return upperLocgovCode;
        }

        public void setUpperLocgovCode(String v) {
            this.upperLocgovCode = v;
        }

        public String getLocgovCode() {
            return locgovCode;
        }

        public void setLocgovCode(String v) {
            this.locgovCode = v;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String v) {
            this.itemName = v;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int v) {
            this.page = v;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(int v) {
            this.itemsPerPage = v;
        }
    }
}

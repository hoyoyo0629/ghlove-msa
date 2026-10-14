package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.StatisticsLocgovClient;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 관심 지자체 (AS-IS opmanager/statistics/locgov/like -
 * {@code StatisticsLocgovManagerController}/{@code StatisticsLocgovServiceImpl}/
 * {@code statistics-locgov-mapper.xml}).
 *
 * <p>마이페이지 "관심지자체"(G_INTRST_LOCGOV) 등록행을 지자체별로 묶은 목록과, 지자체 하나의
 * 전체/연도별/월별 추이를 보여주는 상세 화면이다. ★이전 구현(ListResult 하나로 지자체별+월별을
 * 합쳐 돌려주고 텍스트검색·"전체 연도"를 쓰던 축소판)은 AS-IS와 구조가 전혀 달라 폐기하고
 * 이 화면에서 다시 만들었다(2026-10-14).
 */
@Controller
@RequiredArgsConstructor
public class StatisticsLocgovController {

    private final StatisticsLocgovClient client;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final ExcelDownloadWriter excelDownloadWriter;

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ---- 목록 (AS-IS like) ----

    /** AS-IS GET like - 지자체담당자는 바로 자기 지자체 상세로 리다이렉트, 그 외는 조회 없이 빈 목록. */
    @GetMapping("/statistics/interest-locgov")
    public String list(@ModelAttribute("searchParam") ListSearchParam param,
                        HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager)) {
            return "redirect:/statistics/interest-locgov/" + manager.getLocgovCode();
        }
        listFormAttrs(model);
        model.addAttribute("rows", List.<StatisticsLocgovClient.LocgovRow>of());
        model.addAttribute("pagination", Pagination.of(0, param.getPage(), param.getItemsPerPage()).withLinkFrom(request));
        model.addAttribute("count", 0);
        return "statistics-locgov/list";
    }

    /** AS-IS POST like - 실제 조회. */
    @PostMapping("/statistics/interest-locgov")
    public String search(@ModelAttribute("searchParam") ListSearchParam param,
                          HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager)) {
            return "redirect:/statistics/interest-locgov/" + manager.getLocgovCode();
        }
        Pagination pagination = Pagination.of(0, param.getPage(), param.getItemsPerPage());
        StatisticsLocgovClient.ListResult result = client.search(
                param.getShLocgovCode(), pagination.getStartRow(), pagination.getItemsPerPage());

        listFormAttrs(model);
        model.addAttribute("rows", result.rows());
        model.addAttribute("pagination",
                Pagination.of(result.total(), param.getPage(), param.getItemsPerPage()).withLinkFrom(request));
        model.addAttribute("count", result.total());
        return "statistics-locgov/list";
    }

    /**
     * AS-IS GET like/excel - 페이징 없이 조건 전체. 지자체담당자는 자기 지자체로 고정되고,
     * {@code Shop.downloadExcelOrder(..., true)} 라 개인정보 사유 모달을 먼저 거친다.
     */
    @GetMapping("/statistics/interest-locgov/excel")
    public void excel(@ModelAttribute("searchParam") ListSearchParam param,
                       HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        String shLocgovCode = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : param.getShLocgovCode();
        StatisticsLocgovClient.ListResult result = client.search(shLocgovCode, 0, Integer.MAX_VALUE);

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("locgov_like");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);

        String[] headers = {"No.", "지자체", "등록수"};
        int[] widths = {700, 3500, 3500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "관심 지자체", headers, widths);

        int no = 0;
        for (StatisticsLocgovClient.LocgovRow r : result.rows()) {
            Row xl = sheet.createRow(rowIndex++);
            xl.setHeight((short) 400);
            int c = 0;
            excelDownloadWriter.dataCell(xl, c++, styles, String.valueOf(++no));
            excelDownloadWriter.dataCell(xl, c++, styles,
                    (r.upperLocgovNm() != null ? r.upperLocgovNm() + " " : "") + r.locgovNm());
            excelDownloadWriter.dataCell(xl, c, styles, String.format("%,d", r.count()));
        }

        String fileName = "관심_지자체_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager == null ? null : manager.getLoginId());
    }

    private void listFormAttrs(Model model) {
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
    }

    // ---- 상세 (AS-IS like/{locgovCode}) ----

    /** AS-IS GET like/{locgovCode} - 지자체담당자가 자기 지자체가 아닌 곳을 보려 하면 막는다. */
    @GetMapping("/statistics/interest-locgov/{locgovCode}")
    public String detail(@PathVariable String locgovCode,
                          @ModelAttribute("searchParam") DetailSearchParam param,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager) && !manager.getLocgovCode().equals(locgovCode)) {
            return "redirect:/statistics/interest-locgov";
        }
        detailAttrs(model, manager, locgovCode, param);
        return "statistics-locgov/detail";
    }

    @PostMapping("/statistics/interest-locgov/{locgovCode}")
    public String searchDetail(@PathVariable String locgovCode,
                                @ModelAttribute("searchParam") DetailSearchParam param,
                                HttpSession session, Model model) {
        Manager manager = manager(session);
        if (MenuService.isLocgovScoped(manager) && !manager.getLocgovCode().equals(locgovCode)) {
            return "redirect:/statistics/interest-locgov";
        }
        detailAttrs(model, manager, locgovCode, param);
        return "statistics-locgov/detail";
    }

    /** AS-IS statisticsLocgovLikeByMonth ajax - {isSuccess,data} 봉투로 감싼다. */
    @PostMapping("/statistics/interest-locgov/{locgovCode}/{year}")
    @ResponseBody
    public Map<String, Object> month(@PathVariable String locgovCode, @PathVariable String year) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", true);
        body.put("data", client.monthlyOf(locgovCode, year));
        return body;
    }

    private void detailAttrs(Model model, Manager manager, String locgovCode, DetailSearchParam param) {
        StatisticsLocgovClient.DetailTotal total = client.totalOf(locgovCode, param.getShCntrYear());
        model.addAttribute("total", total);
        model.addAttribute("years", yearCodes());
        model.addAttribute("locgovCode", locgovCode);
        // AS-IS: adminRole != 'LOC' 일 때만 [목록] 버튼이 보인다 - 지자체담당자는 목록 자체가 없다.
        model.addAttribute("showListButton", !MenuService.isLocgovScoped(manager));
    }

    // ---- 공통 ----

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS detail.jsp 의 년도 select - YYYY 공통코드(매출통계와 같은 함정, availableYears() 로
     *  "데이터 있는 연도만" 뽑으면 선택지가 줄어든다). */
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

    /** AS-IS StatisticsLocgovSearch 중 목록이 쓰는 것만. */
    public static class ListSearchParam {
        private String shWdr;
        private String shLocgovCode;
        private int page = 1;
        private int itemsPerPage = 10;

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
    }

    /** AS-IS StatisticsLocgovSearch 중 상세가 쓰는 것만 - 연도. 비면 올해(AS-IS 컨트롤러가 채운다). */
    public static class DetailSearchParam {
        private String shCntrYear;

        public String getShCntrYear() {
            if (shCntrYear == null || shCntrYear.isBlank()) {
                return String.valueOf(LocalDate.now().getYear());
            }
            return shCntrYear;
        }

        public void setShCntrYear(String shCntrYear) {
            this.shCntrYear = shCntrYear;
        }
    }
}

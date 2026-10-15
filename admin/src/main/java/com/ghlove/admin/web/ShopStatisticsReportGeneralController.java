package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.ShopStatisticsReportGeneralService;
import com.ghlove.admin.web.support.ShopStatisticsReportExcelView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 보고서 > 총괄 현황 (AS-IS {@code ShopStatisticsManagerController#generalStats/searchGeneralStats} +
 * {@code report/general.jsp} + {@code report/lclgv/excel-download}).
 *
 * <p>이전 구현(연도 입력 하나·탭 없음·엑셀 없음, 원장 즉석집계)을 AS-IS 사양으로 전면 교체했다(2026-10-15).
 * 화면은 탭 4개(회원 수 / 기부건수 / 기부건수(2000만원) / 답례품건수)를 한 페이지에 모두 그려 두고 JS가 보이는 표를
 * 바꾼다. <b>GET은 표를 비워 두고 POST(검색)에서 채운다</b>(AS-IS 빈 목록 하드코딩).
 */
@Controller
@RequiredArgsConstructor
public class ShopStatisticsReportGeneralController {

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShopStatisticsReportGeneralService service;

    @GetMapping("/shop-statistics/report/general")
    public String generalStats(@ModelAttribute("statisticsParam") ReportParam param, Model model) {
        List<String> crtrYear = service.crtrYears(param.getTabId(), false);
        fill(model, param, crtrYear);
        model.addAttribute("mbrTnocsStats", Collections.emptyList());
        model.addAttribute("dntnTnocsStats", Collections.emptyList());
        model.addAttribute("dntnTnocsMaxAmtStats", Collections.emptyList());
        model.addAttribute("gdsTnocsStats", Collections.emptyList());
        return "shop-statistics/report-general";
    }

    @PostMapping("/shop-statistics/report/general")
    public String searchGeneralStats(@ModelAttribute("statisticsParam") ReportParam param, Model model) {
        List<String> crtrYear = service.crtrYears(param.getTabId(), true);
        fill(model, param, crtrYear);
        String shYear = param.getShYear();
        int shMonth = param.getShMonth();
        model.addAttribute("mbrTnocsStats", service.mbrRows(shYear, shMonth));
        model.addAttribute("dntnTnocsStats", service.provinceRows("dntn_tnocs_stats", shYear, shMonth));
        model.addAttribute("dntnTnocsMaxAmtStats", service.provinceRows("dntn_tnocs_max_amt_stats", shYear, shMonth));
        model.addAttribute("gdsTnocsStats", service.provinceRows("gds_tnocs_stats", shYear, shMonth));
        return "shop-statistics/report-general";
    }

    /** AS-IS 공통 모델 - 검색월이 없으면 직전월(전월이 작년 12월이면 작년으로), 현재 년월, 탭. */
    private void fill(Model model, ReportParam param, List<String> crtrYear) {
        LocalDate now = LocalDate.now();
        if (param.getShMonth() < 1) {
            LocalDate lastMonth = now.minusMonths(1);
            param.setShMonth(lastMonth.getMonthValue());
            param.setShYear("" + lastMonth.getYear());
        }
        model.addAttribute("crtrYear", crtrYear);
        model.addAttribute("nowYear", String.valueOf(now.getYear()));
        model.addAttribute("nowMonth", now.getMonthValue());
        model.addAttribute("tabId", param.getTabId());
    }

    /**
     * AS-IS GET report/lclgv/excel-download - 총괄 현황의 "엑셀" 버튼이 부르는 곳(지자체별 화면과 같은 주소).
     * 지자체 담당자(ROLE_ADMIN_5/6)는 자기 지자체로 범위가 고정되고 시트도 3장뿐이다. 암호화·사유 모달은 없다
     * ({@code Shop.downloadExcelOrder(.., false)}).
     */
    @GetMapping("/shop-statistics/report/lclgv/excel-download")
    public void excelDownload(@ModelAttribute("statisticsParam") ReportParam param, HttpServletRequest request,
                              HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String shYear = param.getShYear();
        int shMonth = param.getShMonth();
        // 시스템/행안부는 지자체 현황 화면에서 고른 시/도·지자체 그대로(없으면 전체), 지자체 담당자는 아래에서 자기 소속으로 고정
        String shMctpv = param.getShMctpv();
        String shLclgv = param.getShLclgv();

        boolean locgovScoped = manager != null && MenuService.isLocgovScoped(manager);
        if (locgovScoped) {
            String lclgvCd = manager.getLocgovCode() == null ? "" : manager.getLocgovCode();
            if (lclgvCd.length() > 0) {
                String mctpvCd = service.upperLocgovCode(lclgvCd);
                shMctpv = mctpvCd;
                if (!lclgvCd.equals(mctpvCd)) {
                    shLclgv = lclgvCd;
                }
            }
        }

        ShopStatisticsReportExcelView excel = new ShopStatisticsReportExcelView(shMonth);
        List<Map<String, Object>> lclgvAod = service.byLocgov("lclgv_aod_stats", true, shYear, shMctpv, shLclgv);
        List<Map<String, Object>> lclgvAodMaxAmt = service.byLocgov("lclgv_aod_max_amt_stats", true, shYear, shMctpv, shLclgv);
        List<Map<String, Object>> lclgvGds = service.byLocgov("lclgv_gds_stats", true, shYear, shMctpv, shLclgv);
        if (locgovScoped) {
            excel.buildForLocgov(lclgvAod, lclgvAodMaxAmt, lclgvGds);
        } else {
            excel.buildForAll(
                    service.totals("mbr_tnocs_stats", shYear),
                    service.totals("dntn_tnocs_stats", shYear),
                    service.totals("dntn_tnocs_max_amt_stats", shYear),
                    service.totals("gds_tnocs_stats", shYear),
                    service.byLocgov("mctpv_dntn_stats", false, shYear, shMctpv, null),
                    service.byLocgov("mctpv_dntn_max_amt_stats", false, shYear, shMctpv, null),
                    lclgvAod, lclgvAodMaxAmt,
                    service.byLocgov("mctpv_gds_stats", false, shYear, shMctpv, null),
                    lclgvGds,
                    service.byYearMonth("dntn_path_tnocs_stats", "dntn_path_cd", shYear, shMonth),
                    service.byYearMonth("dntn_amt_tnocs_stats", "dntn_amt_cd", shYear, shMonth),
                    service.byYearMonth("dntn_age_tnocs_stats", "dntn_age_cd, dntn_age", shYear, shMonth),
                    service.byYearMonth("hab_dntn_mctpv_tnocs_stats", "hab_mctpv_cd", shYear, shMonth));
        }
        excel.view().write(request, response, "통계_보고서_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx");
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 탭, 기준년·월. */
    public static class ReportParam {
        private String tabId;
        private String shYear;
        private int shMonth;
        private String shMctpv;
        private String shLclgv;

        public String getShMctpv() {
            return shMctpv;
        }

        public void setShMctpv(String shMctpv) {
            this.shMctpv = shMctpv;
        }

        public String getShLclgv() {
            return shLclgv;
        }

        public void setShLclgv(String shLclgv) {
            this.shLclgv = shLclgv;
        }

        /** AS-IS StatisticsParam.getTabId() - 비어 있으면 "1"(첫 탭). 컨트롤러·화면 모두 이 getter를 쓰므로 첫 진입은 1번 탭이다. */
        public String getTabId() {
            return (tabId == null || tabId.isEmpty()) ? "1" : tabId;
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

        public int getShMonth() {
            return shMonth;
        }

        public void setShMonth(int shMonth) {
            this.shMonth = shMonth;
        }
    }
}

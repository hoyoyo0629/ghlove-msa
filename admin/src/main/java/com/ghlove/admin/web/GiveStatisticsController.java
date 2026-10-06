package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.GiveOperateStatService;
import com.ghlove.admin.service.GiveStatClient;
import com.ghlove.admin.service.GiveStateService;
import com.ghlove.admin.service.GiveStatisticsService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 기부 통계 (AS-IS opmanager/give/statistics) - AS-IS의 8개+ 화면(전체/지자체별/기부인원/
 *  기부금액/기부건수/개인별/일자별/운영통계 + 외국인 변형) 중 지자체별·운영통계는 이미
 *  구현된 기능(give-state/give-operation/stats)과 중복이라 제외하고, 나머지를 하나의
 *  통합 화면으로 재구성했다 - 사용자 확인 하에 결정된 스코프(제안요청서 SFR-007/009가
 *  화면 단위가 아닌 분석 내용 단위로 요구사항을 기술하는 점 확인 후). 외국인 변형은 이
 *  시스템에 내/외국인 구분 자체가 없어 제외. */
@Controller
@RequiredArgsConstructor
public class GiveStatisticsController {

    private final GiveStatisticsService giveStatisticsService;
    private final GiveStateService giveStateService;
    private final GiveStatClient giveStatClient;
    private final LocgovClient locgovClient;
    private final com.ghlove.admin.service.GiveOperateStatService giveOperateStatService;

    @GetMapping("/give-statistics")
    public String view(@RequestParam(required = false) String cntrYear,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        Model model) {
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);

        model.addAttribute("monthRows", giveStatisticsService.monthlyTrend(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("ageRows", giveStatisticsService.ageBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("amountRows", giveStatisticsService.amountBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("personalRows", giveStatisticsService.personalRanking(cntrYear, upperLocgovCode, locgovCode, 20));
        model.addAttribute("levyRows", giveStatisticsService.levyReport(cntrYear, upperLocgovCode, locgovCode));
        return "give/give-statistics";
    }

    // ---- 전체(all) - AS-IS give/statistics/all/detail.jsp 재현(요약 3카드 + Chart.js 차트 5종) ----

    @GetMapping("/give-statistics/all")
    public String all(@RequestParam(required = false) String shCntrYear, Model model) {
        List<String> years = giveStateService.availableYears();
        String year = (shCntrYear != null && !shCntrYear.isBlank()) ? shCntrYear
                : (years.isEmpty() ? String.valueOf(java.time.LocalDate.now().getYear()) : years.get(0));
        model.addAttribute("years", years);
        model.addAttribute("shCntrYear", year);
        model.addAttribute("total", giveStatClient.allSummary(year, ""));
        return "give/give-statistics-all";
    }

    @RequestMapping(value = "/give-statistics/all/{year}/month", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> allMonth(@PathVariable String year) {
        return success(giveStatClient.allByMonth(year, ""));
    }

    @RequestMapping(value = "/give-statistics/all/{date}/hour", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> allHour(@PathVariable String date) {
        return success(giveStatClient.allByHour(date, ""));
    }

    private static Map<String, Object> success(Object data) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", true);
        m.put("data", data);
        return m;
    }

    // ---- 지자체별 - AS-IS give/statistics/locgov/list.jsp 재현(차트 없는 서버렌더 표) ----

    @GetMapping("/give-statistics/locgov")
    public String locgov(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "") String shCntrYear,
            @RequestParam(required = false, defaultValue = "") String itemsOrder,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        boolean scoped = MenuService.isLocgovScoped(manager);
        String effLocgov = scoped ? manager.getLocgovCode() : shLocgovCode;

        List<GiveStatClient.LocgovYearRow> all =
                new java.util.ArrayList<>(giveStatClient.locgovList(shCntrYear, effLocgov));
        // 정렬(AS-IS itemsOrder): 지정 시 해당 컬럼 내림차순(2차 정렬은 서버 기본순 유지).
        if ("person".equals(itemsOrder)) {
            all.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::givePersons).reversed());
        } else if ("amt".equals(itemsOrder)) {
            all.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::cntrAmt).reversed());
        } else if ("cnt".equals(itemsOrder)) {
            all.sort(java.util.Comparator.comparingLong(GiveStatClient.LocgovYearRow::giveCnt).reversed());
        }

        int total = all.size();
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int cur = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((cur - 1) * size, total);
        int to = Math.min(from + size, total);

        model.addAttribute("rows", all.subList(from, to));
        model.addAttribute("startNo", total - from);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", cur);
        model.addAttribute("itemsPerPage", size);
        model.addAttribute("itemsOrder", itemsOrder);
        model.addAttribute("scoped", scoped);
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        model.addAttribute("shCntrYear", shCntrYear);
        return "give/give-statistics-locgov";
    }

    // ---- 운영현황 - AS-IS give/statistics/operate (지자체×용도 지출 피벗 + 지자체별 연도 상세) ----

    @GetMapping("/give-statistics/operate")
    public String operateList(HttpSession session,
            @RequestParam(required = false, defaultValue = "") String shCntrYear,
            @RequestParam(required = false, defaultValue = "") String shWdr,
            @RequestParam(required = false, defaultValue = "") String shLocgovCode,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        // AS-IS: 지자체 담당자는 목록 없이 자기 지자체 상세로 바로 이동.
        if (MenuService.isLocgovScoped(manager)) {
            return "redirect:/give-statistics/operate/" + manager.getLocgovCode();
        }
        String year = shCntrYear.isBlank() ? String.valueOf(java.time.LocalDate.now().getYear()) : shCntrYear;

        List<GiveOperateStatService.OperateListRow> all = giveOperateStatService.operateList(year, shLocgovCode);
        int total = all.size();
        int size = itemsPerPage <= 0 ? 10 : itemsPerPage;
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int cur = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((cur - 1) * size, total);
        int to = Math.min(from + size, total);

        model.addAttribute("useList", giveOperateStatService.codeList());
        model.addAttribute("list", all.subList(from, to));
        model.addAttribute("startNo", total - from);
        model.addAttribute("count", total);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", cur);
        model.addAttribute("itemsPerPage", size);
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("shCntrYear", year);
        model.addAttribute("shWdr", shWdr);
        model.addAttribute("shLocgovCode", shLocgovCode);
        return "give/give-statistics-operate-list";
    }

    @GetMapping("/give-statistics/operate/{locgovCode}")
    public String operateDetail(HttpSession session, @PathVariable String locgovCode, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        boolean scoped = MenuService.isLocgovScoped(manager);
        String eff = scoped ? manager.getLocgovCode() : locgovCode;
        model.addAttribute("codeList", giveOperateStatService.codeList());
        model.addAttribute("shLocgovCode", eff);
        model.addAttribute("scoped", scoped);
        model.addAttribute("locgovFullNm", locgovFullNm(eff));
        return "give/give-statistics-operate-detail";
    }

    @RequestMapping(value = "/give-statistics/operate/{locgovCode}/detail", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> operateDetailData(HttpSession session, @PathVariable String locgovCode) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String eff = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : locgovCode;
        List<GiveOperateStatService.CodeLabel> codes = giveOperateStatService.codeList();
        List<GiveOperateStatService.OperateDetailRow> rows = giveOperateStatService.operateDetailByLocgov(eff);
        // AS-IS detail.jsp JS가 data['amt'+id]/['cnt'+id] 평면키를 읽으므로 그 형태로 직렬화.
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (GiveOperateStatService.OperateDetailRow r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cntrYear", r.cntrYear());
            for (GiveOperateStatService.CodeLabel c : codes) {
                m.put("amt" + c.id(), r.amts().getOrDefault(c.id(), 0L));
                m.put("cnt" + c.id(), r.cnts().getOrDefault(c.id(), 0L));
            }
            m.put("expndtrSum", r.expndtrSum());
            m.put("expndtrCnt", r.expndtrCnt());
            list.add(m);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("codeList", codes);
        data.put("list", list);
        return success(data);
    }

    private String locgovFullNm(String locgovCode) {
        if (locgovCode == null || locgovCode.isBlank()) {
            return "";
        }
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            if (locgovCode.equals(l.locgovCode())) {
                return (l.upperLocgovNm() + " " + l.locgovNm()).trim();
            }
        }
        return "";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}

package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovAdminClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.PointClient;
import com.ghlove.admin.web.support.LocgovSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지자체관리 (메뉴 4401) - AS-IS saleson.shop.user.LocgovManagerController
 * ({@code /opmanager/user/locgov}) 재현.
 *
 * <p><b>권한 분기</b>(AS-IS {@code getLoginUserAdminRoleCheck}):
 * <ul>
 *   <li>SYS(ROLE_ADMIN_1~4): 목록 + 등록 + 모든 지자체 수정</li>
 *   <li>LOC(ROLE_ADMIN_5·6): 목록이 없다 - 자기 지자체가 <b>이미 등록돼 있으면 수정화면,
 *       아니면 등록화면</b>으로 바로 보낸다. 남의 지자체를 열면 자기 것으로 되돌린다.</li>
 *   <li>그 외(오프라인 담당자 등): 홈으로</li>
 * </ul>
 *
 * <p>AS-IS 동작 그대로: <b>진입(GET)에서는 조회하지 않고</b> 빈 목록을 내려준다. 등록일 범위에는
 * 기본값이 없다(담당자 화면들과 달리 오늘로 채우지 않고, 날짜버튼에 '전체'도 없다).
 * 정렬은 {@code g_locgov.ordering}이다.
 *
 * <p><b>MSA 분담</b>: 지자체 마스터·직인·배경이미지·부서이력·기부제한은 donation
 * ({@code g_locgov} 등), <b>포인트 지급률은 point</b>({@code PT_LOCGOV_POINT_RATE})다.
 * AS-IS는 지급률을 {@code G_CTBNY_SETUP}에 썼지만 TO-BE에서 실제 적립 계산
 * ({@code PointService#currentPointRateOf})이 읽는 표는 point 쪽이라 거기에 쓴다
 * (자세한 내용은 {@link LocgovAdminClient}/{@link PointClient} 주석). 변경이력 팝업도 같은 표를
 * 읽는다 - 연도별 행 자체가 이력이고 작성자명까지 들어 있어 AS-IS 팝업 컬럼과 1:1로 맞는다.
 *
 * <p>AS-IS에 있던 {@code POST /test}(본문이 {@code System.out.println("test")}뿐인 디버그
 * 스텁)는 옮기지 않았다. 주석처리된 {@code GET /imageView/{locgovCode}}도 AS-IS에서 죽은
 * 상태이므로 살리지 않았다.
 *
 * <p>TO-BE 전용으로 이미 있던 "지자체별 기부혜택 안내문구"(SFR-003)는 AS-IS 4401에 없는
 * 기능이라, 엔드포인트를 그대로 두고 수정화면 맨 아래 별도 블록으로 분리해 노출한다.
 */
@Controller
@RequestMapping("/admin/locgovs")
@RequiredArgsConstructor
public class LocgovAdminController {

    /** 년도 공통코드 - AS-IS는 포인트 지급률 연도 select와 이력 팝업 필터에 이걸 쓴다. */
    private static final String CODE_TYPE_YEAR = "YYYY";

    /** AS-IS 부서코드 이력 팝업은 페이지당 5건이다({@code params.setItemsPerPage(5)}). */
    private static final int DEPT_HIST_PAGE_SIZE = 5;

    private final LocgovAdminClient locgovAdminClient;
    private final PointClient pointClient;
    private final CommonCodeService commonCodeService;
    private final ManagerRepository managerRepository;

    /** AS-IS 목록 한 행 - 8컬럼. */
    public record Row(String locgovCode, String upperLocgovNm, String locgovNm, Long locgovBudgetAmt,
                      String locgovPopltnCo, String chargerNm, String chargerCttpc, BigDecimal pointRate,
                      String frstRegistPnttm) {

        public String getLocgovText() {
            return (upperLocgovNm == null ? "" : upperLocgovNm) + " " + (locgovNm == null ? "" : locgovNm);
        }

        /** AS-IS는 인구수를 #,###.## 로 찍는다. 컬럼이 VARCHAR라 숫자가 아니면 원문 그대로 둔다. */
        public String getLocgovPopltnCoText() {
            if (locgovPopltnCo == null || locgovPopltnCo.isBlank()) {
                return "";
            }
            try {
                return String.format("%,.2f", new BigDecimal(locgovPopltnCo.replace(",", "")));
            } catch (NumberFormatException e) {
                return locgovPopltnCo;
            }
        }

        /** AS-IS는 예산을 #,###.## 로 찍고 값이 없으면 빈칸이다. */
        public String getLocgovBudgetAmtText() {
            return locgovBudgetAmt == null ? "" : String.format("%,.2f", new BigDecimal(locgovBudgetAmt));
        }

        /** AS-IS 포인트 지급률 포맷은 #.## 다(값이 없으면 빈칸). */
        public String getPointRateText() {
            if (pointRate == null) {
                return "";
            }
            return pointRate.stripTrailingZeros().toPlainString();
        }
    }

    /** 부서코드 변경이력 한 행 - AS-IS 팝업의 "등록자"는 이름(아이디)다. */
    public record DeptHistRow(Integer deptHistNo, String lastUpdtPnttm, String processDeptCode,
                              String userName, String loginId) {

        public String getRegisterText() {
            if (userName == null && loginId == null) {
                return "";
            }
            return (userName == null ? "" : userName) + "(" + (loginId == null ? "" : loginId) + ")";
        }
    }

    /* ==================== 목록 ==================== */

    /** AS-IS GET /list - 권한분기 후, 조회하지 않고 빈 목록. */
    @GetMapping
    public String list(@ModelAttribute("searchParam") LocgovSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyDefaults();
        fillList(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "locgov-admin/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping
    public String search(@ModelAttribute("searchParam") LocgovSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyDefaults();

        // donation 검색 API는 page가 0부터다(화면은 1부터)
        LocgovAdminClient.PageResult result = locgovAdminClient.search(null,
                searchParam.locgovNmQuery(), searchParam.chargerNmQuery(), searchParam.chargerCttpcQuery(),
                searchParam.getSrchStartCreatedForApi(), searchParam.getSrchEndCreatedForApi(),
                Math.max(searchParam.getPage() - 1, 0), searchParam.getItemsPerPage());

        List<String> codes = result.content().stream().map(LocgovAdminClient.Locgov::locgovCode).toList();
        // AS-IS 목록의 포인트 지급률은 '올해' 설정값이다
        Map<String, BigDecimal> rates = pointClient.currentLocgovPointRates(currentYear(), codes);

        List<Row> rows = result.content().stream()
                .map(l -> new Row(l.locgovCode(), l.upperLocgovNm(), l.locgovNm(), l.locgovBudgetAmt(),
                        l.locgovPopltnCo(), l.chargerNm(), l.chargerCttpc(), rates.get(l.locgovCode()),
                        formatDate(l.frstRegistPnttm())))
                .toList();

        int count = (int) result.totalElements();
        fillList(model, rows, count,
                Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "locgov-admin/list";
    }

    /* ==================== 등록 ==================== */

    /** AS-IS GET /create - 등록 폼. SYS는 시도 select, LOC는 자기 지자체 고정. */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        Manager viewer = manager(session);
        String adminRole = adminRole(viewer);
        if (adminRole == null) {
            return "redirect:/admin";
        }
        model.addAttribute("adminRole", adminRole);

        if ("SYS".equals(adminRole)) {
            // AS-IS는 상위 지자체를 공통코드 WDR에서 가져오는데 TO-BE 공통코드엔 WDR이 없어
            // 지자체 목록에서 시도를 추린다(값 집합은 동일) - 4402에서 한 것과 같은 처리.
            model.addAttribute("upperLocgovCodeList", provinces());
            model.addAttribute("registrable", locgovAdminClient.registrable());
        } else {
            model.addAttribute("userLocgovCode", viewer.getLocgovCode());
            model.addAttribute("userLocgovName", locgovName(viewer.getLocgovCode()));
        }
        fillCodeLists(model);
        return "locgov-admin/form";
    }

    /** AS-IS POST /create - ajax(multipart). 응답 data는 SUCC/DUP/FAIL 문자열이다. */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> create(@ModelAttribute LocgovFormRequest form,
                                      @RequestParam(required = false) MultipartFile offcsFile,
                                      @RequestParam(required = false) MultipartFile addPcFile,
                                      @RequestParam(required = false) MultipartFile addMbFile,
                                      HttpSession session) {
        Manager viewer = manager(session);
        try {
            locgovAdminClient.register(form.locgovCode(), form.toFields(), offcsFile, addPcFile, addMbFile,
                    viewer == null ? null : viewer.getUserId());
            upsertPointRate(form.locgovCode(), form.stdrYear(), form.pointRate(), viewer);
            applyLmtt(form, viewer);
            return data("SUCC");
        } catch (ManagerException e) {
            // AS-IS는 이미 등록된 지자체면 DUP를 내려 "이미 등록된 지자체 정보가 존재합니다."를 띄운다
            String message = e.getMessage() == null ? "" : e.getMessage();
            return data(message.contains("이미") || message.contains("중복") ? "DUP" : "FAIL");
        }
    }

    /* ==================== 수정 ==================== */

    /** AS-IS GET /edit/{locgovCode}. LOC가 남의 지자체를 열면 자기 것으로 되돌린다. */
    @GetMapping("/edit/{locgovCode}")
    public String editForm(@PathVariable String locgovCode, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String adminRole = adminRole(viewer);
        if (adminRole == null) {
            return "redirect:/admin";
        }
        if ("LOC".equals(adminRole) && !locgovCode.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/locgovs/edit/" + viewer.getLocgovCode();
        }

        LocgovAdminClient.LocgovDetail detail;
        try {
            detail = locgovAdminClient.get(locgovCode);
        } catch (ManagerException e) {
            // AS-IS는 상세가 없으면 화면에서 "지자체 정보가 존재하지 않습니다." 후
            // SYS는 목록, LOC는 등록화면으로 보낸다(템플릿의 pageValidator가 처리한다).
            detail = null;
        }
        model.addAttribute("adminRole", adminRole);
        model.addAttribute("details", detail);
        model.addAttribute("locgovCode", locgovCode);
        // AS-IS가 상세 조회 때 g_cntr_lmtt를 LEFT JOIN해 한 건을 같이 내려주는 자리
        model.addAttribute("lmtt", firstLmtt(locgovCode));
        model.addAttribute("locFisSpList", fisSpList(locgovCode));
        model.addAttribute("honorBenefit", locgovAdminClient.honorBenefit(locgovCode));
        fillCodeLists(model);
        return "locgov-admin/edit";
    }

    /** AS-IS POST /edit - ajax(multipart). */
    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> update(@ModelAttribute LocgovFormRequest form,
                                      @RequestParam(required = false) MultipartFile offcsFile,
                                      @RequestParam(required = false) MultipartFile addPcFile,
                                      @RequestParam(required = false) MultipartFile addMbFile,
                                      HttpSession session) {
        Manager viewer = manager(session);
        String adminRole = adminRole(viewer);
        if (adminRole == null
                || ("LOC".equals(adminRole) && !form.locgovCode().equals(viewer.getLocgovCode()))) {
            return data("FAIL");
        }
        try {
            locgovAdminClient.update(form.locgovCode(), form.toFields(), offcsFile, addPcFile, addMbFile,
                    viewer == null ? null : viewer.getUserId());
            applyLmtt(form, viewer);
            return data("SUCC");
        } catch (ManagerException e) {
            return data("FAIL");
        }
    }

    /* ==================== 포인트 지급률 ==================== */

    /** AS-IS GET /point/{stdrYear}/{locgovCode} - 그 연도 지급률 ajax 조회. */
    @GetMapping("/point/{stdrYear}/{locgovCode}")
    @ResponseBody
    public Map<String, Object> pointDetails(@PathVariable String stdrYear, @PathVariable String locgovCode) {
        BigDecimal rate = pointClient.locgovPointRateHistory(locgovCode).stream()
                .filter(r -> stdrYear.equals(r.stdrYear()))
                .map(PointClient.LocgovPointRate::pointRate)
                .findFirst().orElse(null);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stdrYear", stdrYear);
        data.put("locgovCode", locgovCode);
        data.put("pointRate", rate);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        return response;
    }

    /** AS-IS POST /point/edit - 지급률 등록/수정. 응답 data는 SUCC/FAIL 문자열. */
    @PostMapping("/point/edit")
    @ResponseBody
    public Map<String, Object> editPointRate(@RequestParam String stdrYear, @RequestParam String locgovCode,
                                             @RequestParam BigDecimal pointRate, HttpSession session) {
        Manager viewer = manager(session);
        try {
            pointClient.upsertLocgovPointRate(stdrYear, locgovCode, pointRate,
                    viewer == null ? null : viewer.getUserName());
            return data("SUCC");
        } catch (ManagerException e) {
            return data("FAIL");
        }
    }

    /** AS-IS GET /popup/point/list/{locgovCode} - 포인트 지급률 변경이력 팝업(600x675). */
    @GetMapping("/popup/point/list/{locgovCode}")
    public String pointHistoryPopup(@PathVariable String locgovCode,
                                    @RequestParam(required = false) String stdrYear,
                                    @RequestParam(defaultValue = "1") int page,
                                    Model model) {
        List<PointClient.LocgovPointRate> all = pointClient.locgovPointRateHistory(locgovCode).stream()
                .filter(r -> stdrYear == null || stdrYear.isBlank() || stdrYear.equals(r.stdrYear()))
                // AS-IS 정렬: 변경일 내림차순, 같으면 년도 내림차순
                .sorted(Comparator.comparing((PointClient.LocgovPointRate r) ->
                                r.lastUpdtPnttm() == null ? "" : r.lastUpdtPnttm().toString())
                        .thenComparing(PointClient.LocgovPointRate::stdrYear).reversed())
                .toList();

        Pagination pagination = Pagination.of(all.size(), page)
                .withLink("javascript:movePage([page])");
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("stdrYear", stdrYear);
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("list", all.stream().skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage()).toList());
        model.addAttribute("yearCodeList", commonCodeService.labelsOf(CODE_TYPE_YEAR));
        return "locgov-admin/point-history-popup";
    }

    /* ==================== 부서코드 이력 ==================== */

    /** AS-IS GET /edit/{locgovCode}/dept/popup - 부서코드 변경이력 팝업(800x600, 페이지당 5건). */
    @GetMapping("/edit/{locgovCode}/dept/popup")
    public String deptHistPopup(@PathVariable String locgovCode,
                                @RequestParam(defaultValue = "1") int page,
                                Model model) {
        List<LocgovAdminClient.DeptHist> all = locgovAdminClient.deptHist(locgovCode);
        Pagination pagination = Pagination.of(all.size(), page, DEPT_HIST_PAGE_SIZE)
                .withLink("javascript:movePage([page])");

        List<DeptHistRow> rows = all.stream().skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .map(h -> {
                    Manager register = h.lastUpdusrId() == null ? null
                            : managerRepository.findById(h.lastUpdusrId()).orElse(null);
                    return new DeptHistRow(h.deptHistNo(), formatDate(h.changedAt()), h.processDeptCode(),
                            register == null ? null : register.getUserName(),
                            register == null ? null : register.getLoginId());
                }).toList();

        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("list", rows);
        return "locgov-admin/dept-popup";
    }

    /* ==================== 삭제 ==================== */

    /**
     * AS-IS POST /delete - 지자체 삭제. 응답 data는 SUCC/NOT_DEL/FAIL 문자열이다.
     *
     * <p><b>AS-IS 비활성 상태 보존</b>: 목록의 삭제 버튼은 2022-11-18부터 주석처리되어 있어
     * 화면에서 호출되지 않는다. 엔드포인트만 남기고 버튼은 주석 상태로 옮겼다.
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(name = "locgovCodeList", required = false) List<String> codes,
                                      HttpSession session) {
        Manager viewer = manager(session);
        if (codes == null || codes.isEmpty() || viewer == null || MenuService.isLocgovScoped(viewer)) {
            return data("FAIL");
        }
        try {
            for (String code : codes) {
                locgovAdminClient.deactivate(code, viewer.getUserId());
            }
            return data("SUCC");
        } catch (ManagerException e) {
            return data("NOT_DEL");
        }
    }

    /** AS-IS POST /delete/offcs - 직인 삭제. 응답 data는 true/false다. */
    @PostMapping("/delete/offcs")
    @ResponseBody
    public Map<String, Object> deleteOffcs(@RequestParam String locgovCode, HttpSession session) {
        Manager viewer = manager(session);
        boolean deleted = locgovAdminClient.deleteSeal(locgovCode, viewer == null ? null : viewer.getUserId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", deleted);
        return response;
    }

    /** AS-IS POST /delete/itemFile - 답례품 배경이미지(PC/MB) 삭제. */
    @PostMapping("/delete/itemFile")
    @ResponseBody
    public Map<String, Object> deleteItemFile(@RequestParam String locgovCode, @RequestParam String type,
                                             HttpSession session) {
        Manager viewer = manager(session);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        try {
            locgovAdminClient.deleteImage(locgovCode, "PC".equals(type) ? "pc" : "mobile",
                    viewer == null ? null : viewer.getUserId());
            response.put("data", true);
        } catch (ManagerException e) {
            response.put("data", false);
        }
        return response;
    }

    /* ==================== 이미지 ==================== */

    /** AS-IS GET /sealView/{locgovCode} - 직인 이미지(암호화 저장 → 복호화 스트리밍). */
    @GetMapping("/sealView/{locgovCode}")
    @ResponseBody
    public ResponseEntity<byte[]> sealView(@PathVariable String locgovCode) {
        byte[] bytes = locgovAdminClient.sealBytes(locgovCode);
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    /**
     * 답례품 배경이미지 조회. AS-IS는 이미지 경로({@code pcFilePath}/{@code mbFilePath})를 그대로
     * img src에 넣지만 TO-BE는 파일을 서비스 저장소에 두고 바이트로 내려준다.
     */
    @GetMapping("/image/{locgovCode}/{type}")
    @ResponseBody
    public ResponseEntity<byte[]> image(@PathVariable String locgovCode, @PathVariable String type) {
        byte[] bytes = locgovAdminClient.imageBytes(locgovCode, "PC".equals(type) ? "pc" : "mobile");
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    /* ==================== TO-BE 전용 (AS-IS 4401에는 없음) ==================== */

    /** 지자체별 기부혜택 안내문구 (SFR-003) - AS-IS 4401에 없는 TO-BE 추가기능. */
    @PostMapping("/{locgovCode}/honor-benefit")
    public String updateHonorBenefit(@PathVariable String locgovCode, @RequestParam String benefitDesc,
                                      HttpSession session) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer) && !locgovCode.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/locgovs/edit/" + viewer.getLocgovCode();
        }
        locgovAdminClient.updateHonorBenefit(locgovCode, benefitDesc);
        return "redirect:/admin/locgovs/edit/" + locgovCode;
    }

    /* ==================== 내부 ==================== */

    /**
     * AS-IS 목록 진입 권한분기. SYS면 null(목록 표시), LOC면 등록여부에 따라 수정/등록화면,
     * 그 외는 홈으로.
     */
    private String listGate(Manager viewer) {
        String adminRole = adminRole(viewer);
        if ("SYS".equals(adminRole)) {
            return null;
        }
        if ("LOC".equals(adminRole)) {
            String code = viewer.getLocgovCode();
            if (code == null || code.isBlank()) {
                return "redirect:/admin/locgovs/create";
            }
            try {
                locgovAdminClient.get(code);
                return "redirect:/admin/locgovs/edit/" + code;
            } catch (ManagerException e) {
                return "redirect:/admin/locgovs/create";
            }
        }
        return "redirect:/admin";
    }

    /** AS-IS getLoginUserAdminRoleCheck - SYS(1~4) / LOC(5·6) / 그 외 null. */
    private static String adminRole(Manager viewer) {
        if (viewer == null || viewer.getAuthority() == null) {
            return null;
        }
        return switch (viewer.getAuthority()) {
            case "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4" -> "SYS";
            case "ROLE_ADMIN_5", "ROLE_ADMIN_6" -> "LOC";
            default -> null;
        };
    }

    private void fillList(Model model, List<Row> list, int count, Pagination pagination) {
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);

        // AS-IS list.jsp 하단의 숨은 "날짜 셋팅 영역"
        LocalDate today = LocalDate.now();
        Map<String, String> searchDates = new LinkedHashMap<>();
        searchDates.put("today", today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        searchDates.put("week", today.minusDays(7).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        searchDates.put("month1", today.minusMonths(1).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        searchDates.put("month3", today.minusMonths(3).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        searchDates.put("year1", today.minusMonths(12).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        model.addAttribute("searchDates", searchDates);
    }

    /** 연락처(PHONE+TEL)·년도 공통코드 - AS-IS가 등록·수정 화면에 함께 내려주는 것들. */
    private void fillCodeLists(Model model) {
        Map<String, String> phones = new LinkedHashMap<>(commonCodeService.labelsOf("PHONE"));
        phones.putAll(commonCodeService.labelsOf("TEL"));
        model.addAttribute("phoneCodeList", phones);
        model.addAttribute("yearCodeList", commonCodeService.labelsOf(CODE_TYPE_YEAR));
        model.addAttribute("currentYear", currentYear());
    }

    /**
     * 회계구분 목록 - AS-IS는 지자체코드 3~5자리가 '000'(광역)이면 31/51, 아니면 41/61을
     * <b>자바 코드에서 직접 만들어</b> 내려준다(공통코드가 아니다). 그대로 옮겼다.
     */
    private static List<Map<String, String>> fisSpList(String locgovCode) {
        boolean wide = locgovCode != null && locgovCode.length() >= 5
                && "000".equals(locgovCode.substring(2, 5));
        List<Map<String, String>> list = new ArrayList<>();
        list.add(Map.of("id", wide ? "31" : "41", "label", "일반회계"));
        list.add(Map.of("id", wide ? "51" : "61", "label", "특별회계"));
        return list;
    }

    /** 시도 목록 - AS-IS 공통코드 WDR 대신 지자체 목록에서 추린다(4402와 같은 처리). */
    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovAdminClient.Locgov l : locgovAdminClient.registrable()) {
            if (l.upperLocgovCode() != null) {
                map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
            }
        }
        return map;
    }

    private String locgovName(String locgovCode) {
        if (locgovCode == null) {
            return "";
        }
        try {
            LocgovAdminClient.LocgovDetail detail = locgovAdminClient.get(locgovCode);
            return (detail.upperLocgovNm() == null ? "" : detail.upperLocgovNm()) + " "
                    + (detail.locgovNm() == null ? "" : detail.locgovNm());
        } catch (ManagerException e) {
            return "";
        }
    }

    /** AS-IS 상세는 g_cntr_lmtt를 LEFT JOIN해 한 건만 쓴다 - 시작일이 가장 이른 것을 쓴다. */
    private LocgovAdminClient.Lmtt firstLmtt(String locgovCode) {
        return locgovAdminClient.lmttList(locgovCode).stream()
                .min(Comparator.comparing(l -> l.lmttBgnDe() == null ? "" : l.lmttBgnDe()))
                .orElse(null);
    }

    /**
     * 기부금 모금제한 저장 - AS-IS는 지자체 수정(POST /edit) 안에서 기간·사유가 있으면
     * insert/update하고 비었으면 delete한다({@code LocgovServiceImpl}의 insertCntrLmtt/
     * updateCntrLmmt/deleteCntrLmmt). TO-BE는 donation에 등록/삭제 API가 따로 있어 그걸 묶어 쓴다.
     */
    private void applyLmtt(LocgovFormRequest form, Manager viewer) {
        boolean hasPeriod = notBlank(form.lmttBgnDe()) && notBlank(form.lmttEndDe());
        LocgovAdminClient.Lmtt existing = firstLmtt(form.locgovCode());

        if (!hasPeriod) {
            if (existing != null) {
                locgovAdminClient.deleteLmtt(form.locgovCode(), existing.lmttBgnDe(), existing.lmttEndDe());
            }
            return;
        }
        String bgn = stripDashes(form.lmttBgnDe());
        String end = stripDashes(form.lmttEndDe());
        if (existing != null && !(bgn.equals(existing.lmttBgnDe()) && end.equals(existing.lmttEndDe()))) {
            locgovAdminClient.deleteLmtt(form.locgovCode(), existing.lmttBgnDe(), existing.lmttEndDe());
        }
        locgovAdminClient.createLmtt(form.locgovCode(), bgn, end,
                existing == null ? null : existing.violtResnCode(), form.violtResnCn(),
                viewer == null ? null : viewer.getUserName(), viewer == null ? null : viewer.getUserId());
    }

    private void upsertPointRate(String locgovCode, String stdrYear, BigDecimal pointRate, Manager viewer) {
        if (pointRate == null) {
            return;
        }
        pointClient.upsertLocgovPointRate(
                notBlank(stdrYear) ? stdrYear : currentYear(), locgovCode, pointRate,
                viewer == null ? null : viewer.getUserName());
    }

    /** AS-IS JsonViewUtils.success(code) 모양 - {isSuccess, data:"SUCC"}. */
    private static Map<String, Object> data(String code) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", code);
        return response;
    }

    private static String currentYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    private static String formatDate(java.time.LocalDateTime value) {
        return value == null ? "" : value.toLocalDate().toString();
    }

    private static String stripDashes(String ymd) {
        return ymd == null ? null : ymd.replace("-", "");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /**
     * AS-IS setParam()의 FormData와 1:1 대응. 사업자번호·연락처는 화면에서 3분할 입력받아
     * 합쳐 보내므로(AS-IS와 동일) 서버는 합쳐진 값 하나를 받는다.
     */
    public record LocgovFormRequest(String locgovCode, String upperLocgovCode, String bizrno, String chargerNm,
                                     String chargerCttpc, String chargerEmail, String locgovHmpg,
                                     String locgovIntrcnCn, String locgovZip, String bassAdres, String dtlAdres,
                                     Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                                     String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                                     String chargerPsitnDept, String processDeptCode, String administInsttCode,
                                     String fisSp, String offcsNm, BigDecimal pointRate, String stdrYear,
                                     String lmttBgnDe, String lmttEndDe, String violtResnCn) {

        Map<String, Object> toFields() {
            Map<String, Object> m = new HashMap<>();
            m.put("bizrno", bizrno);
            m.put("chargerNm", chargerNm);
            m.put("chargerCttpc", chargerCttpc);
            m.put("chargerEmail", chargerEmail);
            m.put("locgovHmpg", locgovHmpg);
            m.put("locgovIntrcnCn", locgovIntrcnCn);
            m.put("locgovZip", locgovZip);
            m.put("bassAdres", bassAdres);
            m.put("dtlAdres", dtlAdres);
            m.put("locgovBudgetAmt", locgovBudgetAmt);
            m.put("locgovPopltnCo", locgovPopltnCo);
            m.put("locgovAr", locgovAr);
            m.put("locgovSpcprd", locgovSpcprd);
            m.put("gcctUseAt", nvl(gcctUseAt, "N"));
            m.put("etrcshUseAt", nvl(etrcshUseAt, "N"));
            m.put("achlqrSleAt", nvl(achlqrSleAt, "N"));
            m.put("chargerPsitnDept", chargerPsitnDept);
            m.put("processDeptCode", processDeptCode);
            m.put("administInsttCode", administInsttCode);
            m.put("fisSp", fisSp);
            m.put("offcsNm", offcsNm);
            return m;
        }

        private static String nvl(String v, String def) {
            return (v == null || v.isBlank()) ? def : v;
        }
    }
}

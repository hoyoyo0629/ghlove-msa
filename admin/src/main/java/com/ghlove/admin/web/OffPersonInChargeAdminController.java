package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.OffPersonInChargeAdminService;
import com.ghlove.admin.web.support.OffPersonInChargeSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 오프라인담당자 (메뉴 4601) - AS-IS saleson.shop.user.OffPersonInChargeManagerController
 * ({@code /opmanager/user/off-charger}) 13개 엔드포인트 재현.
 *
 * <p><b>권한 분기가 이 화면의 핵심이다.</b>
 * <ul>
 *   <li>목록은 ROLE_ADMIN_1~4·7만 볼 수 있다. <b>오프라인 부담당자(ROLE_ADMIN_8)는 목록 대신
 *       자기 상세로 리다이렉트</b>되고, 그 외 권한은 홈으로 돌려보낸다. 상세도 부담당자가 남의
 *       것을 열면 자기 것으로 되돌린다.</li>
 *   <li>오프라인담당자(7·8)가 조회하면 목록이 <b>자기 지점(BANK_CODE)</b>으로 한정된다.</li>
 *   <li>주담당자 승격은 3분기다 - 요청자가 주담당자(7)면 정원 2명 초과는 {@code ERR_MAIN_CNT},
 *       정확히 2명이고 대상이 남이면 {@code NEED_AUTH_SWAP}(권한 이관 동의를 받는다), 그 외는 저장.
 *       요청자가 시스템/행안부 관리자면 <b>2명부터 바로 막는다</b> - 강등할 주담당자를 지정할
 *       방법이 없기 때문이다(AS-IS 주석 그대로).</li>
 *   <li>권한 이관({@code /auth-swap})은 대상을 주담당자로 올리면서 <b>요청자 본인을 부담당자 +
 *       중지</b>로 내린다 → {@code AUTH_CHANGED_LOGOUT}으로 즉시 로그아웃시킨다.</li>
 * </ul>
 *
 * <p>AS-IS 동작 그대로: 진입(GET)에서는 조회하지 않고 빈 목록을 내려준다(검색해야 조회된다).
 * 삭제 허용권한은 ROLE_ADMIN_1~4·7이고, 삭제 대상에 본인이 포함되면 {@code isLogout=Y}다.
 *
 * <p><b>AS-IS 결함 1건 - 고쳤다</b>: 등록 시 STATUS_CODE가 문자열 "null"로 들어가 방금 만든
 * 담당자가 목록에도 안 보이고 로그인도 못 하는 문제. 자세한 내용은
 * {@link OffPersonInChargeAdminService} 클래스 주석.
 *
 * <p>엑셀 다운로드는 개인정보가 포함되므로 AS-IS처럼 사유 모달
 * ({@code fragments/privacy-access})을 먼저 띄워 접근로그를 남긴다
 * ({@code PrivacyAccess.OFF_CHARGER} = "오프라인 담당자 목록"). AS-IS는 xlsx(POI)로 내려주지만
 * TO-BE의 다른 이식 화면들과 같이 CSV로 내려준다(컬럼·파일명 어간은 AS-IS 그대로).
 */
@Controller
@RequestMapping("/admin/off-person-in-charge")
@RequiredArgsConstructor
public class OffPersonInChargeAdminController {

    /** AS-IS: 목록을 볼 수 있는 권한(시스템·행안부 + 오프라인 주담당자). */
    private static final List<String> LIST_VISIBLE_ROLES =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
                    OffPersonInChargeAdminService.OFF_MAIN);

    /** AS-IS offChargerDelete의 allowedRole 문자열과 같은 집합. */
    private static final List<String> DELETE_ALLOWED_ROLES = LIST_VISIBLE_ROLES;

    /** AS-IS form.jsp/edit.jsp의 adminRoleType=SYS 판정. */
    private static final List<String> SYS_ROLES =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");

    /** 등록 버튼 노출권한 - AS-IS list.jsp는 1·2·7만 보여준다(3·4는 등록할 수 없다). */
    private static final List<String> CREATE_VISIBLE_ROLES =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", OffPersonInChargeAdminService.OFF_MAIN);

    /** 등록화면에서 소속지점 코드목록을 받는 권한 - AS-IS /create는 1~4·7이다. */
    private static final List<String> BANK_LIST_ROLES_ON_CREATE =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
                    OffPersonInChargeAdminService.OFF_MAIN);

    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OffPersonInChargeAdminService service;

    /** AS-IS GET /list - 권한 분기 후, 조회하지 않고 빈 목록. */
    @GetMapping
    public String list(@ModelAttribute("searchParam") OffPersonInChargeSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyDefaults();
        fill(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request),
                viewer);
        return "off-person-in-charge/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping
    public String search(@ModelAttribute("searchParam") OffPersonInChargeSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyDefaults();

        int count = service.search(searchParam, viewer, 0, Integer.MAX_VALUE).totalElements();
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        OffPersonInChargeAdminService.SearchResult result =
                service.search(searchParam, viewer, pagination.getStartRow(), pagination.getItemsPerPage());

        fill(model, result.content(), count, pagination, viewer);
        return "off-person-in-charge/list";
    }

    /**
     * AS-IS GET /list/download-excel - 전체 목록 엑셀 다운로드(페이징 없이 조건 전체).
     * 개인정보 사유 모달이 접근로그를 남긴 뒤 이 URL로 이동한다.
     */
    @GetMapping("/list/download-excel")
    public void downloadExcel(@ModelAttribute("searchParam") OffPersonInChargeSearchParam searchParam,
                              HttpSession session, HttpServletResponse response) throws IOException {
        Manager viewer = viewer(session);
        searchParam.applyDefaults();
        List<OffPersonInChargeAdminService.Row> rows =
                service.search(searchParam, viewer, 0, Integer.MAX_VALUE).content();

        // AS-IS OffPersonInChargeExcelView의 헤더 8개 그대로
        StringBuilder csv = new StringBuilder("﻿");
        csv.append("No.,구분,소속지점,아이디,이름,개인번호,사용여부,중지일자\n");
        int no = rows.size();
        for (OffPersonInChargeAdminService.Row row : rows) {
            csv.append(no--).append(',')
                    .append(csvEscape(row.getAuthorityText())).append(',')
                    .append(csvEscape(row.getBranchText())).append(',')
                    .append(csvEscape(row.loginId())).append(',')
                    .append(csvEscape(row.userName())).append(',')
                    .append(csvEscape(row.empId())).append(',')
                    .append("2".equals(row.getAsIsStatusCode()) ? "중지" : "사용").append(',')
                    .append("2".equals(row.getAsIsStatusCode()) ? csvEscape(row.getDenyDateText()) : "")
                    .append('\n');
        }

        String fileName = "오프라인담당자회원목록_" + LocalDateTime.now().format(FILE_DATETIME) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"off-person-in-charge.csv\"; filename*=UTF-8''"
                + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** AS-IS POST /status/edit - 목록에서 선택한 계정의 사용/중지 일괄변경. */
    @PostMapping("/status/edit")
    @ResponseBody
    public Map<String, Object> statusCodeEdit(
            @RequestParam(name = "userIdList", required = false) List<Long> userIdList,
            @RequestParam String statusCode) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", "FAIL");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);

        if (userIdList == null || userIdList.isEmpty()) {
            data.put("code", "ERR");
            return response;
        }
        try {
            if (service.updateStatusCode(userIdList, statusCode) > 0) {
                data.put("code", "SUCC");
            }
        } catch (RuntimeException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    /** AS-IS GET /create - 등록 폼. */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        Manager viewer = viewer(session);
        String adminRole = authorityOf(viewer);
        model.addAttribute("adminRole", adminRole);
        model.addAttribute("loginUserId", viewer == null ? null : viewer.getUserId());
        model.addAttribute("adminRoleType", adminRoleType(adminRole));
        model.addAttribute("adminRoleGrade", adminRoleGrade(adminRole));
        model.addAttribute("phoneCodeList", service.phoneAndTelCodeList());
        model.addAttribute("bankCodeList", BANK_LIST_ROLES_ON_CREATE.contains(adminRole)
                ? service.bankCodeList() : Map.of());
        return "off-person-in-charge/form";
    }

    /** AS-IS POST /createProcess - ajax. */
    @PostMapping("/createProcess")
    @ResponseBody
    public Map<String, Object> createProcess(@RequestParam String loginId,
                                             @RequestParam(required = false) String password,
                                             @RequestParam(required = false) String userName,
                                             @RequestParam(required = false) String phoneNumber,
                                             @RequestParam(required = false) String bankCode,
                                             @RequestParam(required = false) String psitnNm,
                                             @RequestParam(required = false) String empId,
                                             @RequestParam(required = false) String email) {
        return result(() -> service.insertOffPersonInCharge(loginId, password, userName, phoneNumber,
                bankCode, psitnNm, empId, email));
    }

    /**
     * AS-IS POST /getUserInfoByUserId - 아이디 중복확인(ajax).
     *
     * <p><b>응답 의미가 거꾸로다</b>: AS-IS는 이미 점유된 아이디일 때 {@code SUCC}를 내려주고
     * 화면이 "중복된 아이디입니다."를 띄운다(쓸 수 있으면 FAIL + "사용가능한 아이디 입니다.").
     * 혼동하기 쉬우나 화면과 짝이 맞으므로 그대로 뒀다.
     */
    @PostMapping("/getUserInfoByUserId")
    @ResponseBody
    public Map<String, Object> getUserInfoByUserId(@RequestParam(required = false) String loginId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", service.isOccupiedLoginId(loginId) ? "SUCC" : "FAIL");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        return response;
    }

    /** AS-IS GET /edit/{userId} - 상세. 부담당자가 남의 것을 열면 자기 것으로 되돌린다. */
    @GetMapping("/edit/{userId}")
    public String editForm(@PathVariable Long userId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        String adminRole = authorityOf(viewer);

        if (OffPersonInChargeAdminService.OFF_SUB.equals(adminRole) && viewer != null
                && !userId.equals(viewer.getUserId())) {
            return "redirect:/admin/off-person-in-charge/edit/" + viewer.getUserId();
        }

        Manager details = service.getChargerDetails(userId);
        model.addAttribute("details", details);
        model.addAttribute("bankNm", details == null ? null : service.bankName(details.getBankCode()));
        model.addAttribute("adminRole", adminRole);
        model.addAttribute("loginUserId", viewer == null ? null : viewer.getUserId());
        model.addAttribute("adminRoleType", adminRoleType(adminRole));
        model.addAttribute("adminRoleGrade", adminRoleGrade(adminRole));
        model.addAttribute("phoneCodeList", service.phoneAndTelCodeList());
        // AS-IS /edit/{userId}는 소속지점 코드목록을 ROLE_ADMIN_1~4에만 내려준다(등록과 달리 7 제외)
        model.addAttribute("bankCodeList", SYS_ROLES.contains(adminRole) ? service.bankCodeList() : Map.of());
        return "off-person-in-charge/edit";
    }

    /**
     * AS-IS POST /edit - ajax. 주담당자 승격 시 정원·권한이관 분기를 그대로 따른다
     * (클래스 주석의 3분기).
     */
    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> edit(@RequestParam Long userId,
                                    @RequestParam(required = false) String authority,
                                    @RequestParam(required = false) String userName,
                                    @RequestParam(required = false) String phoneNumber,
                                    @RequestParam(required = false) String bankCode,
                                    @RequestParam(required = false) String psitnNm,
                                    @RequestParam(required = false) String empId,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String statusCode,
                                    HttpSession session) {
        Manager viewer = viewer(session);
        String adminRole = authorityOf(viewer);

        if (!OffPersonInChargeAdminService.OFF_MAIN.equals(authority)) {
            return result(() -> service.updateOffPersonInCharge(userId, authority, userName, phoneNumber,
                    bankCode, psitnNm, empId, email, statusCode));
        }

        int mainCount = service.getOffPersonInChargeMainCount(bankCode, userId);
        if (OffPersonInChargeAdminService.OFF_MAIN.equals(adminRole)) {
            // 수정을 요청한 사람이 오프라인 주관리자인 경우
            if (mainCount > OffPersonInChargeAdminService.MAIN_LIMIT) {
                return code("ERR_MAIN_CNT");
            }
            if (mainCount == OffPersonInChargeAdminService.MAIN_LIMIT
                    && viewer != null && !userId.equals(viewer.getUserId())) {
                // 2명인 경우 권한 이관 알림 필요
                return code("NEED_AUTH_SWAP");
            }
        } else if (mainCount >= OffPersonInChargeAdminService.MAIN_LIMIT) {
            // 시스템관리자가 업데이트 하는 경우에는, 강등해야할 주관리자를 지정할수 없으므로 2명인 경우부터 막음
            return code("ERR_MAIN_CNT");
        }
        return result(() -> service.updateOffPersonInCharge(userId, authority, userName, phoneNumber,
                bankCode, psitnNm, empId, email, statusCode));
    }

    /**
     * AS-IS POST /auth-swap - 권한 이관. 요청자 본인이 부담당자 + 중지로 내려가므로
     * {@code AUTH_CHANGED_LOGOUT}을 받은 화면이 곧바로 로그아웃시킨다.
     */
    @PostMapping("/auth-swap")
    @ResponseBody
    public Map<String, Object> authSwap(@RequestParam Long userId,
                                        @RequestParam(required = false) String authority,
                                        @RequestParam(required = false) String userName,
                                        @RequestParam(required = false) String phoneNumber,
                                        @RequestParam(required = false) String bankCode,
                                        @RequestParam(required = false) String psitnNm,
                                        @RequestParam(required = false) String empId,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String statusCode,
                                        HttpSession session) {
        Manager viewer = viewer(session);
        // AS-IS: 주담당자로의 승격이고, 대상이 본인이 아닐 때만 이관한다
        if (!OffPersonInChargeAdminService.OFF_MAIN.equals(authority) || viewer == null
                || userId.equals(viewer.getUserId())) {
            return code("FAIL");
        }
        try {
            service.authSwap(userId, viewer, userName, phoneNumber, bankCode, psitnNm, empId, email, statusCode);
        } catch (RuntimeException e) {
            return code("FAIL");
        }
        return code("AUTH_CHANGED_LOGOUT");
    }

    /** AS-IS POST /delete - ajax. 삭제 대상에 본인이 포함되면 isLogout=Y. */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(name = "userIdList", required = false) List<Long> userIdList,
                                      HttpSession session) {
        Manager viewer = viewer(session);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", "FAIL");
        data.put("isLogout", "N");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);

        if (!DELETE_ALLOWED_ROLES.contains(authorityOf(viewer))) {
            data.put("code", "ERR_NOT_ALLOW");
            return response;
        }
        try {
            service.deleteCharger(userIdList);
            data.put("code", "SUCC");
            data.put("isLogout", userIdList.contains(viewer.getUserId()) ? "Y" : "N");
        } catch (RuntimeException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    /**
     * AS-IS GET /popup/password-init/{userId} - 임시 비밀번호 발급 팝업.
     * 발급 불가면 빈 문자열을 내려주고 팝업이 스스로 닫힌다.
     */
    @GetMapping("/popup/password-init/{userId}")
    public String passwordInit(@PathVariable Long userId, HttpSession session, Model model) {
        model.addAttribute("password", service.updatePasswordInit(userId, viewer(session)));
        return "off-person-in-charge/password-init";
    }

    /**
     * AS-IS 목록 진입 권한분기 - 볼 수 있으면 null, 부담당자면 자기 상세로, 그 외는 홈으로.
     */
    private String listGate(Manager viewer) {
        String adminRole = authorityOf(viewer);
        if (LIST_VISIBLE_ROLES.contains(adminRole)) {
            return null;
        }
        if (OffPersonInChargeAdminService.OFF_SUB.equals(adminRole) && viewer != null) {
            return "redirect:/admin/off-person-in-charge/edit/" + viewer.getUserId();
        }
        return "redirect:/admin";
    }

    private void fill(Model model, List<OffPersonInChargeAdminService.Row> list, int count,
                      Pagination pagination, Manager viewer) {
        String adminRole = authorityOf(viewer);
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("adminRole", adminRole);
        model.addAttribute("loginUserId", viewer == null ? null : viewer.getUserId());
        model.addAttribute("creatable", CREATE_VISIBLE_ROLES.contains(adminRole));

        // AS-IS list.jsp 하단의 숨은 "날짜 셋팅 영역"(display:none) - 이 화면에는 날짜 입력칸이
        // 없어 serachDate()가 읽을 일이 없지만, AS-IS에 있는 비활성 마크업이라 그대로 둔다.
        LocalDate today = LocalDate.now();
        Map<String, String> searchDates = new LinkedHashMap<>();
        searchDates.put("today", today.format(DAY));
        searchDates.put("week", today.minusDays(7).format(DAY));
        searchDates.put("month1", today.minusMonths(1).format(DAY));
        searchDates.put("month3", today.minusMonths(3).format(DAY));
        searchDates.put("year1", today.minusMonths(12).format(DAY));
        model.addAttribute("searchDates", searchDates);
    }

    /** AS-IS JSP가 c:choose로 계산하는 adminRoleType(SYS/OFF). */
    private static String adminRoleType(String adminRole) {
        if (SYS_ROLES.contains(adminRole)) {
            return "SYS";
        }
        if (OffPersonInChargeAdminService.OFF_MAIN.equals(adminRole)
                || OffPersonInChargeAdminService.OFF_SUB.equals(adminRole)) {
            return "OFF";
        }
        return null;
    }

    /** AS-IS JSP가 계산하는 adminRoleGrade(주담당자 M / 부담당자 S). */
    private static String adminRoleGrade(String adminRole) {
        if (OffPersonInChargeAdminService.OFF_MAIN.equals(adminRole)) {
            return "M";
        }
        return OffPersonInChargeAdminService.OFF_SUB.equals(adminRole) ? "S" : null;
    }

    private Map<String, Object> result(Runnable action) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        try {
            action.run();
            data.put("code", "SUCC");
        } catch (ManagerException e) {
            data.put("code", "FAIL");
            data.put("message", e.getMessage());
        } catch (RuntimeException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    private static Map<String, Object> code(String code) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", code);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        return response;
    }

    private static String authorityOf(Manager viewer) {
        return viewer == null ? null : viewer.getAuthority();
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")
                ? "\"" + escaped + "\"" : escaped;
    }
}

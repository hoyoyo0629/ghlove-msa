package com.ghlove.admin.web;

import com.ghlove.admin.domain.LoginLog;
import com.ghlove.admin.domain.Role;
import com.ghlove.admin.repository.LoginLogRepository;
import com.ghlove.admin.repository.ManagerActionLogRepository;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.web.support.LoginLogParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 로그인 로그 관리 - AS-IS saleson.shop.log.LogManagerController(/opmanager/log) 재현.
 * <ul>
 *   <li>6401 관리자로그관리 = {@code /log/login} (+ 상세 {@code /details/{id}} + 그 안의
 *       메뉴사용이력 조각 {@code /action-log-list})</li>
 *   <li>1408 사용자로그관리 = {@code /log/user-login} (같은 구조, 권한그룹 컬럼만 없다)</li>
 * </ul>
 * 상세화면은 그 로그인 시각부터 같은 계정의 다음 로그인 시각까지의 메뉴사용이력을 보여준다
 * (AS-IS getLoginLogDetailsDateInfo의 nextLoginDate). 예전 TO-BE는 검색·페이징·상세가 없이
 * 최근 200건만 뿌리고, 액션이력을 AS-IS에 없는 독립 화면으로 분리해 뒀다.
 */
@Controller
@RequiredArgsConstructor
public class AuditLogController {

    private final LoginLogRepository loginLogRepository;
    private final ManagerActionLogRepository managerActionLogRepository;
    private final ManagerRepository managerRepository;
    private final RoleRepository roleRepository;
    private final MemberClient memberClient;

    /** 목록 한 줄 - AS-IS는 로그인ID로 권한그룹명(ROLE_NAME)을 함께 조인해 내려준다. */
    public record LoginLogRow(LoginLog log, String roleName) {
    }

    // ---- 6401 관리자로그관리 ----

    /** AS-IS loginLog / searchLoginLog - GET·POST 동일 동작. */
    @RequestMapping(value = "/log/login", method = { RequestMethod.GET, RequestMethod.POST })
    public String loginLog(@ModelAttribute("loginLogParam") LoginLogParam loginLogParam,
                           HttpServletRequest request, Model model) {
        applyItemsPerPage(loginLogParam);

        List<LoginLogRow> all = loginLogRepository
                .searchByPeriod(blankToNull(loginLogParam.getSrchStartLoginDate()),
                        blankToNull(loginLogParam.getSrchEndLoginDate()))
                .stream()
                .filter(l -> loginLogParam.matchesKeyword(l.getLoginId(), l.getRemoteAddr()))
                .filter(l -> loginLogParam.matchesSuccess(l.getSuccessFlag()))
                .map(l -> new LoginLogRow(l, roleNameOf(l.getLoginId())))
                .filter(row -> matchesRole(loginLogParam.getSrcRole(), row.log().getLoginId()))
                .toList();

        Pagination pagination = Pagination.of(all.size(), loginLogParam.getPage(), loginLogParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("adminMenuRoles", roleRepository.findAdminRoles());
        return "log/login-list";
    }

    /** AS-IS loginLogDetails - 로그인ID·권한그룹·접속IP를 보여주고 메뉴사용이력을 ajax로 채운다. */
    @GetMapping("/log/login/details/{loginLogId}")
    public String loginLogDetails(@PathVariable Integer loginLogId, Model model) {
        LoginLog log = loginLogRepository.findById(loginLogId).orElse(null);
        model.addAttribute("details", log);
        model.addAttribute("roleName", log == null ? "" : roleNameOf(log.getLoginId()));
        model.addAttribute("nextLoginDate", nextLoginDate(log));
        model.addAttribute("loginLogId", loginLogId);
        return "log/login-details";
    }

    /** AS-IS actionLogList - 상세화면이 ajax로 끼워 넣는 조각(HTML)이다. */
    @GetMapping("/log/login/details/{loginLogId}/action-log-list")
    public String actionLogList(@PathVariable Integer loginLogId,
                                @RequestParam(required = false) String srchStartCreated,
                                @RequestParam(required = false) String srchEndCreated,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int itemsPerPage,
                                HttpServletRequest request, Model model) {
        LoginLog log = loginLogRepository.findById(loginLogId).orElse(null);
        String loginId = log == null ? "" : log.getLoginId();

        List<com.ghlove.admin.domain.ManagerActionLog> all = managerActionLogRepository
                .searchByLoginAndPeriod(loginId, blankToNull(srchStartCreated), blankToNull(srchEndCreated));

        Pagination pagination = Pagination.of(all.size(), page, itemsPerPage).withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "log/action-log-list";
    }

    // ---- 1408 사용자로그관리 ----

    /**
     * AS-IS userLoginLog / searchUserLoginLog. 사용자 로그인 로그는 member 서비스가 들고 있어
     * {@link MemberClient}로 전체를 받아 admin에서 걸러 페이징한다(member API에 검색 파라미터가
     * 없어서다 - member 쪽 API는 건드리지 않았다).
     */
    @RequestMapping(value = "/log/user-login", method = { RequestMethod.GET, RequestMethod.POST })
    public String userLoginLog(@ModelAttribute("loginLogParam") LoginLogParam loginLogParam,
                               HttpServletRequest request, Model model) {
        applyItemsPerPage(loginLogParam);

        List<MemberClient.LoginLog> all = memberClient.loginLogs().stream()
                .filter(l -> loginLogParam.matchesDate(l.loginDate()))
                .filter(l -> loginLogParam.matchesKeyword(l.loginId(), l.remoteAddr()))
                .filter(l -> loginLogParam.matchesSuccess(l.successFlag()))
                .toList();

        Pagination pagination = Pagination.of(all.size(), loginLogParam.getPage(), loginLogParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "log/user-login-list";
    }

    // ---- AS-IS에 대응 메뉴가 없는 보조 화면(기존 유지) ----

    @GetMapping("/log/manager-action")
    public String actionLog(@RequestParam(required = false) String loginId, Model model) {
        var logs = managerActionLogRepository.findTop200ByOrderByActionLogIdDesc();
        if (loginId != null && !loginId.isBlank()) {
            logs = logs.stream().filter(l -> loginId.equals(l.getLoginId())).toList();
        }
        model.addAttribute("logs", logs);
        model.addAttribute("loginId", loginId);
        return "log/manager-action-list";
    }

    @GetMapping("/log/user-action")
    public String userActionLog(@RequestParam(required = false) String loginId, Model model) {
        var logs = memberClient.userActionLogs();
        if (loginId != null && !loginId.isBlank()) {
            logs = logs.stream().filter(l -> loginId.equals(l.loginId())).toList();
        }
        model.addAttribute("logs", logs);
        model.addAttribute("loginId", loginId);
        return "log/user-action-list";
    }

    /** SFR-002 "권한 변경, 계정 잠금/해제" 감사로그 조회 (member의 OP_USER_CHANGE_LOG). */
    @GetMapping("/log/user-change")
    public String userChangeLog(@RequestParam(required = false) String loginId, Model model) {
        var logs = memberClient.changeLogs();
        if (loginId != null && !loginId.isBlank()) {
            logs = logs.stream().filter(l -> loginId.equals(l.loginId())).toList();
        }
        model.addAttribute("logs", logs);
        model.addAttribute("loginId", loginId);
        return "log/user-change-list";
    }

    /** AS-IS 화면이 itemsPerPage를 숨은 입력으로 넘기고, 비어 있으면 10으로 쓴다. */
    private void applyItemsPerPage(LoginLogParam param) {
        if (param.getItemsPerPage() <= 0) {
            param.setItemsPerPage(10);
        }
    }

    /** 로그인ID → 매니저 → 권한코드 → 권한그룹명. AS-IS는 OP_USER_ROLE 조인으로 같은 값을 얻는다. */
    private String roleNameOf(String loginId) {
        if (loginId == null) {
            return "";
        }
        return managerRepository.findByLoginId(loginId)
                .map(m -> roleRepository.findById(m.getAuthority()).map(Role::getRoleName).orElse(m.getAuthority()))
                .orElse("");
    }

    private boolean matchesRole(String srcRole, String loginId) {
        if (srcRole == null || srcRole.isBlank()) {
            return true;
        }
        return managerRepository.findByLoginId(loginId)
                .map(m -> srcRole.equals(m.getAuthority()))
                .orElse(false);
    }

    /** 같은 계정의 다음 로그인 시각 - 없으면(마지막 로그인) 빈 값이고 화면은 이후 전체를 본다. */
    private String nextLoginDate(LoginLog log) {
        if (log == null || log.getLoginId() == null || log.getLoginDate() == null) {
            return "";
        }
        return loginLogRepository.findNextLoginDate(log.getLoginId(), log.getLoginDate()).orElse("");
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /** 화면출력 셀렉트 옵션 - AS-IS 10/20/50/100. */
    @ModelAttribute("displayCounts")
    public Map<String, String> displayCounts() {
        Map<String, String> counts = new LinkedHashMap<>();
        counts.put("10", "10개 출력");
        counts.put("20", "20개 출력");
        counts.put("50", "50개 출력");
        counts.put("100", "100개 출력");
        return counts;
    }
}

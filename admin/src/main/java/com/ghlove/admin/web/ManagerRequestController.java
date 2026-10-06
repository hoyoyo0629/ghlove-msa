package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.service.*;
import com.ghlove.admin.web.support.ManagerRequestParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 권한 요청 (AS-IS opmanager/manager-request/*, G_MNGR_REQST) - 이미 회원가입돼
 * 있는 사람이 운영관리 콘솔 접근을 신청하고 시스템관리자가 승인/거절한다. AS-IS는 로그인
 * 화면에서 아이디/비번 재입력으로 신원을 확인하지만, 여기서는 SFR-010 공유 JWT로 이미
 * 로그인된 회원임을 확인한다(신청서 자체는 로그인 불필요 URL이지만 회원 로그인은 필요).
 */
@Controller
@RequiredArgsConstructor
public class ManagerRequestController {

    private final ManagerRequestService managerRequestService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CommonCodeService commonCodeService;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;
    private final com.ghlove.admin.repository.RoleRepository roleRepository;

    @GetMapping("/admin/manager-requests/new")
    public String newForm(HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8086/admin/manager-requests/new");
        }
        MemberClient.MemberInfo member = memberClient.fetchOrNull(userId.get());
        if (member == null) {
            return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8086/admin/manager-requests/new");
        }
        model.addAttribute("member", member);
        model.addAttribute("reqstSeCodeList", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("provinces", provinces());
        return "admin/manager-request-form";
    }

    @PostMapping("/admin/manager-requests/new")
    public String submit(HttpServletRequest request,
                          @RequestParam String locgovCode, @RequestParam String reqstSeCode,
                          @RequestParam(required = false) String psitnNm,
                          @RequestParam(required = false) String psitnDeptNm,
                          @RequestParam(required = false) String ofcpsNm,
                          @RequestParam(required = false) String cttpc,
                          Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8086/admin/manager-requests/new");
        }
        MemberClient.MemberInfo member = memberClient.fetchOrNull(userId.get());
        if (member == null) {
            return "redirect:/admin/manager-requests/new";
        }
        try {
            managerRequestService.submit(userId.get(), member.loginId(), locgovCode, reqstSeCode,
                    psitnNm, psitnDeptNm, ofcpsNm, cttpc);
            return "redirect:/admin/manager-requests/complete";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("member", member);
            model.addAttribute("reqstSeCodeList", commonCodeService.labelsOf("REQST_SE_CODE"));
            model.addAttribute("provinces", provinces());
            return "admin/manager-request-form";
        }
    }

    @GetMapping("/admin/manager-requests/complete")
    public String complete() {
        return "admin/manager-request-complete";
    }

    /**
     * 승인관리 목록(1406) - AS-IS user/manager-request/list.jsp 재현.
     * 검색구분(아이디/이름/이메일)·등록일 범위·상태(전체/대기200/승인100/거절300)로 걸러
     * 화면출력·페이징과 함께 보여준다. 상태는 상세 팝업 링크이고 "이력"은 신청이력 팝업이다.
     *
     * AS-IS는 보는 사람의 권한에 따라 컬럼을 감춘다: 시스템/행안부(ALL)만 "구분"을 보고,
     * 지자체 담당자(ROLE_ADMIN_6)는 "소속"을 보지 않는다. 그 판정값을 loginUserAuth로 내려준다.
     *
     * 예전 TO-BE는 대기 건만 뿌리고 검색·페이징·상태필터·상세/이력 팝업이 없었다.
     */
    @RequestMapping(value = "/admin/manager-requests", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("searchParam") ManagerRequestParam searchParam,
                        @RequestParam(required = false) String errorMessage,
                        @RequestParam(required = false) String approvedLoginId,
                        @RequestParam(required = false) String tempPassword,
                        HttpSession session, HttpServletRequest request, Model model) {
        if (searchParam.getItemsPerPage() <= 0) {
            searchParam.setItemsPerPage(10);
        }
        List<ManagerRequest> found = managerRequestService.search(
                blankToNull(searchParam.getSrchConfmSttusCode()),
                blankToNull(searchParam.getSrchStartCreated()),
                blankToNull(searchParam.getSrchEndCreated()));

        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (ManagerRequest r : found) {
            memberByUserId.computeIfAbsent(r.getUserId(), memberClient::fetchOrNull);
        }

        // 아이디/이름/이메일 검색 - 이름·이메일은 member 서비스 값이라 여기서 거른다
        List<ManagerRequest> all = found.stream()
                .filter(r -> matchesKeyword(searchParam, r, memberByUserId.get(r.getUserId())))
                .toList();

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("loginUserAuth", loginUserAuth(session));
        model.addAttribute("memberByUserId", memberByUserId);
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("approvedLoginId", approvedLoginId);
        model.addAttribute("tempPassword", tempPassword);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "admin/manager-request-list";
    }

    /** AS-IS 상세 팝업(manager-request/popup/details/{userId}/{reqstSn}) - 600x650. */
    @GetMapping("/admin/manager-requests/popup/details/{userId}/{reqstSn}")
    public String detailsPopup(@PathVariable Long userId, @PathVariable Integer reqstSn, Model model) {
        ManagerRequest details = managerRequestService.history(userId).stream()
                .filter(r -> reqstSn.equals(r.getReqstSn()))
                .findFirst()
                .orElse(null);
        model.addAttribute("details", details);
        model.addAttribute("member", memberClient.fetchOrNull(userId));
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        return "admin/manager-request-details";
    }

    /** AS-IS 이력 팝업 - 한 사용자의 신청 이력(최신순). */
    @GetMapping("/admin/manager-requests/popup/history/{userId}")
    public String historyPopup(@PathVariable Long userId, Model model) {
        model.addAttribute("list", managerRequestService.history(userId));
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        return "admin/manager-request-history";
    }

    /** AS-IS CONFM_STTUS_CODE - 200=대기 100=승인 300=거절. */
    private static final Map<String, String> CONFM_STTUS_LABELS =
            Map.of("200", "대기", "100", "승인", "300", "거절");

    /** AS-IS가 화면에 내려주는 접근권한 판정값 - 'ALL'(시스템/행안부) / ROLE_ADMIN_6 / ROLE_ADMIN_8. */
    private String loginUserAuth(HttpSession session) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (viewer == null) {
            return "ALL";
        }
        String authority = viewer.getAuthority();
        if (MenuService.UNRESTRICTED_ROLES.contains(authority)) {
            return "ALL";
        }
        if ("ROLE_ADMIN_5".equals(authority) || "ROLE_ADMIN_6".equals(authority)) {
            return "ROLE_ADMIN_6";
        }
        if ("ROLE_ADMIN_7".equals(authority) || "ROLE_ADMIN_8".equals(authority)) {
            return "ROLE_ADMIN_8";
        }
        return "ALL";
    }

    private boolean matchesKeyword(ManagerRequestParam param, ManagerRequest request, MemberClient.MemberInfo member) {
        String keyword = param.getSrchValue();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String trimmed = keyword.trim();
        return switch (param.getSrchKey() == null ? "LOGIN_ID" : param.getSrchKey()) {
            case "USER_NAME" -> member != null && member.userName() != null && member.userName().contains(trimmed);
            case "EMAIL" -> member != null && member.email() != null && member.email().contains(trimmed);
            default -> request.getLoginId() != null && request.getLoginId().contains(trimmed);
        };
    }

    /** locgovCode → "시도 시군구" (목록·상세의 소속 지자체 표시). */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> names.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm()));
        return names;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    @PostMapping("/admin/manager-requests/{userId}/{reqstSn}/approve")
    public String approve(@PathVariable Long userId, @PathVariable Integer reqstSn,
                           @RequestParam String authority, @RequestParam(required = false) String assignedLocgovCode,
                           HttpSession session, Model model) {
        Manager approver = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        MemberClient.MemberInfo member = memberClient.fetchOrNull(userId);
        try {
            String tempPassword = managerRequestService.approve(userId, reqstSn, approver.getUserId(),
                    member != null ? member.userName() : null,
                    member != null ? member.email() : null,
                    member != null ? member.phoneNumber() : null,
                    authority, assignedLocgovCode);
            return "redirect:/admin/manager-requests?approvedLoginId=" + encode(member != null ? member.loginId() : "")
                    + "&tempPassword=" + encode(tempPassword);
        } catch (ManagerException e) {
            return flashRedirect.to("/admin/manager-requests", e.getMessage());
        }
    }

    @PostMapping("/admin/manager-requests/{userId}/{reqstSn}/reject")
    public String reject(@PathVariable Long userId, @PathVariable Integer reqstSn,
                          @RequestParam String reason, HttpSession session) {
        Manager approver = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            managerRequestService.reject(userId, reqstSn, approver.getUserId(), reason);
        } catch (ManagerException e) {
            return flashRedirect.to("/admin/manager-requests", e.getMessage());
        }
        return "redirect:/admin/manager-requests";
    }

    /** upperLocgovCode/upperLocgovNm만 중복 없이 뽑는다 (기존 ReceiptController의 시/도
     *  드롭다운 구성 패턴과 동일). */
    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

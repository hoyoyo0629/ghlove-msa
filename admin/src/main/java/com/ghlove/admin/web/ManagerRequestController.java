package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.service.*;
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

    /** 승인관리 목록 - 운영관리 콘솔 화면(메뉴 RBAC 대상, ROLE_ADMIN 전용). */
    @GetMapping("/admin/manager-requests")
    public String list(@RequestParam(required = false) String errorMessage,
                        @RequestParam(required = false) String approvedLoginId,
                        @RequestParam(required = false) String tempPassword,
                        Model model) {
        List<ManagerRequest> pending = managerRequestService.pending();
        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (ManagerRequest r : pending) {
            memberByUserId.computeIfAbsent(r.getUserId(), memberClient::fetchOrNull);
        }
        model.addAttribute("pending", pending);
        model.addAttribute("memberByUserId", memberByUserId);
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("approvedLoginId", approvedLoginId);
        model.addAttribute("tempPassword", tempPassword);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "admin/manager-request-list";
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
            return "redirect:/admin/manager-requests?errorMessage=" + encode(e.getMessage());
        }
    }

    @PostMapping("/admin/manager-requests/{userId}/{reqstSn}/reject")
    public String reject(@PathVariable Long userId, @PathVariable Integer reqstSn,
                          @RequestParam String reason, HttpSession session) {
        Manager approver = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            managerRequestService.reject(userId, reqstSn, approver.getUserId(), reason);
        } catch (ManagerException e) {
            return "redirect:/admin/manager-requests?errorMessage=" + encode(e.getMessage());
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

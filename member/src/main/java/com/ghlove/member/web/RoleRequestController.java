package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.RoleRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 지자체담당자/제공자 역할 신청·승인 (SFR-002). */
@Controller
@RequiredArgsConstructor
public class RoleRequestController {

    private final RoleRequestService roleRequestService;
    private final MemberService memberService;

    @GetMapping("/roles/request")
    public String requestForm(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return AuthController.loginRedirect("/roles/request");
        }
        model.addAttribute("myRequests", roleRequestService.requestsOf(loginUser.getUserId()));
        model.addAttribute("statusLabels", memberService.codesOf("ROLE_REQUEST_STATUS"));
        model.addAttribute("roleLabels", memberService.codesOf("ROLE"));
        return "roles/request";
    }

    @PostMapping("/roles/request")
    public String request(@RequestParam String requestedRole, @RequestParam(required = false) String reason,
                           HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }
        try {
            roleRequestService.request(loginUser.getUserId(), requestedRole, reason);
        } catch (MemberException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("myRequests", roleRequestService.requestsOf(loginUser.getUserId()));
            model.addAttribute("statusLabels", memberService.codesOf("ROLE_REQUEST_STATUS"));
            model.addAttribute("roleLabels", memberService.codesOf("ROLE"));
            return "roles/request";
        }
        return "redirect:/roles/request";
    }

    @GetMapping("/roles/queue")
    public String queue(Model model) {
        model.addAttribute("requests", roleRequestService.pendingRequests());
        model.addAttribute("roleLabels", memberService.codesOf("ROLE"));
        return "roles/queue";
    }

    @PostMapping("/roles/{requestId}/approve")
    public String approve(@PathVariable Long requestId) {
        roleRequestService.approve(requestId);
        return "redirect:/roles/queue";
    }

    @PostMapping("/roles/{requestId}/reject")
    public String reject(@PathVariable Long requestId) {
        roleRequestService.reject(requestId);
        return "redirect:/roles/queue";
    }
}

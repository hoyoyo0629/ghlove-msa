package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.OpEmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 이메일 발송 (AS-IS opmanager/email). */
@Controller
@RequiredArgsConstructor
public class OpEmailController {

    private final OpEmailService opEmailService;

    @GetMapping("/email")
    public String list(Model model) {
        model.addAttribute("emails", opEmailService.list());
        return "email/list";
    }

    @GetMapping("/email/new")
    public String form() {
        return "email/form";
    }

    @GetMapping("/email/{emailId}")
    public String detail(@org.springframework.web.bind.annotation.PathVariable Long emailId, Model model) {
        model.addAttribute("email", opEmailService.get(emailId));
        return "email/detail";
    }

    @PostMapping("/email")
    public String send(@RequestParam String subject, @RequestParam String content,
                        @RequestParam String authTarget, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        var email = opEmailService.sendNow(subject, content, authTarget, manager.getUserId());
        return "redirect:/email/" + email.getEmailId();
    }
}

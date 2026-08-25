package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.AccessException;
import com.ghlove.admin.service.AccessService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 접속IP허용목록 (AS-IS opmanager/access). */
@Controller
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @GetMapping("/access")
    public String list(Model model) {
        model.addAttribute("list", accessService.list());
        return "access/list";
    }

    @GetMapping("/access/new")
    public String newForm() {
        return "access/form";
    }

    @PostMapping("/access")
    public String register(@RequestParam String accessType, @RequestParam String remoteAddr, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            accessService.register(accessType, remoteAddr, manager.getLoginId());
            return "redirect:/access";
        } catch (AccessException e) {
            return "redirect:/access/new?errorMessage=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/access/{allowIpId}/delete")
    public String delete(@PathVariable Integer allowIpId, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        accessService.remove(allowIpId, manager.getLoginId());
        return "redirect:/access";
    }
}

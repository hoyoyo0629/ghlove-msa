package com.ghlove.admin.web;

import com.ghlove.admin.service.OpenApiClientService;
import com.ghlove.admin.service.OpenApiUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** SFR-010 민간개방 API 관리 (Kong key-auth 컨슈머 발급/회수 + 호출통계). */
@Controller
@RequiredArgsConstructor
public class OpenApiController {

    private final OpenApiClientService openApiClientService;
    private final OpenApiUsageService openApiUsageService;

    @GetMapping("/open-api/clients")
    public String clients(Model model) {
        model.addAttribute("clients", openApiClientService.list());
        return "open-api/clients";
    }

    @PostMapping("/open-api/clients")
    public String issue(@RequestParam String clientName, RedirectAttributes redirectAttributes) {
        try {
            openApiClientService.issue(clientName);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/open-api/clients";
    }

    @PostMapping("/open-api/clients/{clientId}/suspend")
    public String suspend(@PathVariable Long clientId) {
        openApiClientService.suspend(clientId);
        return "redirect:/open-api/clients";
    }

    @PostMapping("/open-api/clients/{clientId}/reactivate")
    public String reactivate(@PathVariable Long clientId) {
        openApiClientService.reactivate(clientId);
        return "redirect:/open-api/clients";
    }

    @GetMapping("/open-api/stats")
    public String stats(Model model) {
        model.addAttribute("callCountByConsumer", openApiUsageService.callCountByConsumer());
        model.addAttribute("recentCalls", openApiUsageService.recentCalls());
        return "open-api/stats";
    }
}

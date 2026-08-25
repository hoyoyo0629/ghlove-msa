package com.ghlove.member.web;

import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 휴면계정 전환(수동 배치)/해제 (SFR-002). */
@Controller
@RequiredArgsConstructor
public class DormancyController {

    private final MemberService memberService;

    @GetMapping("/batch/dormancy")
    public String form(Model model) {
        return "batch-dormancy";
    }

    @PostMapping("/batch/dormancy")
    public String run(Model model) {
        int count = memberService.runDormancyBatch();
        model.addAttribute("resultCount", count);
        return "batch-dormancy";
    }

    @GetMapping("/reactivate")
    public String reactivateForm() {
        return "reactivate";
    }

    @PostMapping("/reactivate")
    public String reactivate(@RequestParam String loginId, @RequestParam String password,
                              HttpServletRequest request, Model model) {
        try {
            memberService.reactivate(loginId, password, request.getRemoteAddr());
        } catch (MemberException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "reactivate";
        }
        return "redirect:/login?reactivated=success";
    }
}

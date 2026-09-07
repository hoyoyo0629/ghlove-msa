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

/**
 * 휴면계정 해제 (SFR-002, 본인 셀프서비스). 휴면 전환 배치 트리거는 무인증으로 직접 두기엔
 * 위험한 운영작업이라 admin 콘솔로 이전했다({@link com.ghlove.member.web.AdminMemberApiController}
 * 의 /api/admin/batch/dormancy - admin 콘솔의 OP_MANAGER 로그인이 실제 게이트).
 */
@Controller
@RequiredArgsConstructor
public class DormancyController {

    private final MemberService memberService;

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

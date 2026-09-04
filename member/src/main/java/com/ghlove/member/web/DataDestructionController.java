package com.ghlove.member.web;

import com.ghlove.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/** SFR-002 "데이터 파기 절차" - 탈퇴회원 개인정보 익명화 + 로그인로그 익명화(수동 배치,
 *  DormancyController와 동일한 패턴) + 파기 이력 조회. */
@Controller
@RequiredArgsConstructor
public class DataDestructionController {

    private final MemberService memberService;

    @GetMapping("/batch/data-destruction")
    public String form(Model model) {
        model.addAttribute("history", memberService.destructionHistory());
        return "batch-data-destruction";
    }

    @PostMapping("/batch/data-destruction/withdrawn")
    public String runWithdrawn(Model model) {
        int count = memberService.purgeWithdrawnUserData();
        model.addAttribute("resultCount", count);
        model.addAttribute("history", memberService.destructionHistory());
        return "batch-data-destruction";
    }

    @PostMapping("/batch/data-destruction/logs")
    public String runLogs(Model model) {
        int count = memberService.anonymizeOldLoginLogs();
        model.addAttribute("logResultCount", count);
        model.addAttribute("history", memberService.destructionHistory());
        return "batch-data-destruction";
    }
}

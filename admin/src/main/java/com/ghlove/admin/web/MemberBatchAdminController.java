package com.ghlove.admin.web;

import com.ghlove.admin.service.MemberAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 회원 배치관리 - 휴면전환/데이터파기 수동 배치 트리거 (SFR-002). 원래 member 자체
 * Thymeleaf 페이지(/batch/dormancy, /batch/data-destruction/*)에 무인증으로 노출돼
 * 있던 것을 admin 콘솔로 옮겼다 - 실시간 스케줄러가 없어 운영자가 수동으로 트리거하는
 * 성격은 그대로 유지하되, admin 콘솔의 OP_MANAGER 로그인이 실제 게이트가 된다.
 */
@Controller
@RequiredArgsConstructor
public class MemberBatchAdminController {

    private final MemberAdminClient memberAdminClient;

    @GetMapping("/admin/members/batch")
    public String form(Model model) {
        model.addAttribute("history", memberAdminClient.destructionHistory());
        return "member-admin/batch";
    }

    @PostMapping("/admin/members/batch/dormancy")
    public String runDormancy(Model model) {
        model.addAttribute("resultCount", memberAdminClient.runDormancyBatch());
        model.addAttribute("history", memberAdminClient.destructionHistory());
        return "member-admin/batch";
    }

    @PostMapping("/admin/members/batch/data-destruction/withdrawn")
    public String runWithdrawnDestruction(Model model) {
        model.addAttribute("resultCount", memberAdminClient.runWithdrawnDestruction());
        model.addAttribute("history", memberAdminClient.destructionHistory());
        return "member-admin/batch";
    }

    @PostMapping("/admin/members/batch/data-destruction/logs")
    public String runLogDestruction(Model model) {
        model.addAttribute("logResultCount", memberAdminClient.runLogDestruction());
        model.addAttribute("history", memberAdminClient.destructionHistory());
        return "member-admin/batch";
    }
}

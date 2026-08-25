package com.ghlove.admin.web;

import com.ghlove.admin.repository.LoginLogRepository;
import com.ghlove.admin.repository.ManagerActionLogRepository;
import com.ghlove.admin.service.MemberClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 관리자 로그인 로그 / 액션(메뉴사용) 이력 (AS-IS opmanager/log의 login-log, action-log 재현).
 *  AS-IS는 액션이력이 로그인 로그 상세의 하위 팝업이지만, OP_MANAGER_ACTION_LOG에 로그인
 *  세션을 가리키는 FK가 없어(loginId+시간만 있음) 독립된 화면 + loginId 필터로 재구성했다. */
@Controller
@RequiredArgsConstructor
public class AuditLogController {

    private final LoginLogRepository loginLogRepository;
    private final ManagerActionLogRepository managerActionLogRepository;
    private final MemberClient memberClient;

    @GetMapping("/log/login")
    public String loginLog(Model model) {
        model.addAttribute("logs", loginLogRepository.findTop200ByOrderByLoginLogIdDesc());
        return "log/login-list";
    }

    @GetMapping("/log/user-login")
    public String userLoginLog(Model model) {
        model.addAttribute("logs", memberClient.loginLogs());
        return "log/user-login-list";
    }

    @GetMapping("/log/manager-action")
    public String actionLog(@RequestParam(required = false) String loginId, Model model) {
        var logs = managerActionLogRepository.findTop200ByOrderByActionLogIdDesc();
        if (loginId != null && !loginId.isBlank()) {
            logs = logs.stream().filter(l -> loginId.equals(l.getLoginId())).toList();
        }
        model.addAttribute("logs", logs);
        model.addAttribute("loginId", loginId);
        return "log/manager-action-list";
    }

    @GetMapping("/log/user-action")
    public String userActionLog(@RequestParam(required = false) String loginId, Model model) {
        var logs = memberClient.userActionLogs();
        if (loginId != null && !loginId.isBlank()) {
            logs = logs.stream().filter(l -> loginId.equals(l.loginId())).toList();
        }
        model.addAttribute("logs", logs);
        model.addAttribute("loginId", loginId);
        return "log/user-action-list";
    }
}

package com.ghlove.member.web;

import com.ghlove.member.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 관리자 메인화면의 회원 집계 - admin이 cross-service로 호출(/api/admin/** = X-Internal-Secret 보호). */
@RestController
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final AdminDashboardService service;

    @GetMapping("/api/admin/members/dashboard/subscribers")
    public Map<String, Long> subscribers() {
        return service.subscribers();
    }

    @GetMapping("/api/admin/members/dashboard/notification-agree")
    public Map<String, Long> notificationAgree() {
        return service.notificationAgree();
    }
}

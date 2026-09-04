package com.ghlove.member.web;

import com.ghlove.member.service.AdminMemberService;
import com.ghlove.member.service.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 회원관리 API (docs/as-is-admin-gap-deep-audit-part2.md 배치D D2~D5) - admin
 * 서비스의 MemberAdminClient가 호출하는 관리자 전용 엔드포인트. 브라우저에 직접 노출되지
 * 않고 admin 콘솔의 OP_MANAGER 로그인+메뉴RBAC이 실제 게이트다(offgive의 /api/users/walk-in과
 * 동일한 관행).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMemberApiController {

    private final AdminMemberService adminMemberService;

    /** D3 일반회원 검색. */
    @GetMapping("/members/search")
    public AdminMemberService.MemberSearchResultDto searchMembers(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String srchKey,
            @RequestParam(required = false) String srchValue,
            @RequestParam(required = false) String sbscrbSeCode,
            @RequestParam(required = false) String receiveEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminMemberService.search(fromDate, toDate, srchKey, srchValue, sbscrbSeCode, receiveEmail, page, size);
    }

    @GetMapping("/members/{userId}")
    public ResponseEntity<AdminMemberService.MemberDetailDto> memberDetail(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminMemberService.detail(userId));
        } catch (MemberException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/members/{userId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long userId, @RequestParam(required = false) String reason) {
        try {
            adminMemberService.adminWithdraw(userId, reason);
            return ResponseEntity.noContent().build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** D4 탈퇴회원 조회. */
    @GetMapping("/secede-users/search")
    public AdminMemberService.SecedeSearchResultDto searchSecedeUsers(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String srchKey,
            @RequestParam(required = false) String srchValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminMemberService.searchSecede(fromDate, toDate, srchKey, srchValue, page, size);
    }

    /** D5 휴면회원 조회/해제. */
    @GetMapping("/sleep-users/search")
    public AdminMemberService.SleepSearchResultDto searchSleepUsers(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String srchKey,
            @RequestParam(required = false) String srchValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminMemberService.searchSleep(fromDate, toDate, srchKey, srchValue, page, size);
    }

    @PostMapping("/sleep-users/wakeup")
    public Map<String, Object> wakeup(@RequestParam List<Long> userIds) {
        int count = adminMemberService.wakeup(userIds);
        return Map.of("count", count);
    }
}

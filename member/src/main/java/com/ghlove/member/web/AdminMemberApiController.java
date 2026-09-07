package com.ghlove.member.web;

import com.ghlove.member.domain.UserDataDestructionLog;
import com.ghlove.member.service.AdminMemberService;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 회원관리 API (docs/as-is-admin-gap-deep-audit-part2.md 배치D D2~D5) - admin
 * 서비스의 MemberAdminClient가 호출하는 관리자 전용 엔드포인트. 브라우저에 직접 노출되지
 * 않고 admin 콘솔의 OP_MANAGER 로그인+메뉴RBAC이 실제 게이트다(offgive의 /api/users/walk-in과
 * 동일한 관행). 휴면전환/데이터파기 배치 트리거도 원래 member 자체 Thymeleaf 페이지로
 * 무인증 노출돼 있던 것을 여기로 옮겼다(SFR-002 재검토 라운드에서 발견한 보안 결함 수정).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMemberApiController {

    private final AdminMemberService adminMemberService;
    private final MemberService memberService;

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

    /** 계정잠금 해제 (SFR-002). */
    @PostMapping("/members/{userId}/unlock")
    public ResponseEntity<?> unlock(@PathVariable Long userId) {
        try {
            adminMemberService.unlock(userId);
            return ResponseEntity.noContent().build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** RBAC 권한 회수 (SFR-002). */
    @PostMapping("/members/{userId}/roles/{authority}/revoke")
    public ResponseEntity<?> revokeRole(@PathVariable Long userId, @PathVariable String authority) {
        try {
            adminMemberService.revokeRole(userId, authority);
            return ResponseEntity.noContent().build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 휴면전환/데이터파기 수동 배치 트리거 (SFR-002) - 원래 member 자체 Thymeleaf 페이지
     * (/batch/dormancy, /batch/data-destruction/*)에 무인증으로 노출돼 있던 것을 여기로
     * 옮겼다. 실시간 스케줄러가 없어 운영자가 admin 콘솔에서 수동으로 트리거하는 건 동일하다.
     */
    @PostMapping("/batch/dormancy")
    public Map<String, Object> runDormancyBatch() {
        return Map.of("count", memberService.runDormancyBatch());
    }

    @PostMapping("/batch/data-destruction/withdrawn")
    public Map<String, Object> runWithdrawnDestruction() {
        return Map.of("count", memberService.purgeWithdrawnUserData());
    }

    @PostMapping("/batch/data-destruction/logs")
    public Map<String, Object> runLogDestruction() {
        return Map.of("count", memberService.anonymizeOldLoginLogs());
    }

    @GetMapping("/batch/data-destruction/history")
    public List<UserDataDestructionLog> destructionHistory() {
        return memberService.destructionHistory();
    }
}

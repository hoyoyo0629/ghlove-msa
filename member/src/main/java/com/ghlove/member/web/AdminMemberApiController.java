package com.ghlove.member.web;

import com.ghlove.member.domain.UserDataDestructionLog;
import com.ghlove.member.service.AdminMemberService;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.OnePassMemberException;
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

    /**
     * 관리자에 의한 회원탈퇴.
     *
     * <p>AS-IS 일반회원관리(4101)의 결과코드를 구분해야 해서 응답 본문에 {@code code}를 담는다 -
     * 이미 탈퇴면 {@code ERR_ALR_SECEDE}, 디지털원패스 회원이면 {@code ERR_ONE_PASS}다.
     * {@code leaveUserId}는 탈퇴를 처리한 운영자이고, 탈퇴회원리스트(4105)의 탈퇴구분·담당자
     * 컬럼이 이 값으로 갈린다.
     */
    @PostMapping("/members/{userId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long userId, @RequestParam(required = false) String reason,
                                      @RequestParam(required = false) Long leaveUserId) {
        try {
            adminMemberService.adminWithdraw(userId, reason, leaveUserId);
            return ResponseEntity.noContent().build();
        } catch (OnePassMemberException e) {
            return ResponseEntity.badRequest().body(Map.of("code", "ERR_ONE_PASS", "message", e.getMessage()));
        } catch (MemberException e) {
            String code = e.getMessage() != null && e.getMessage().contains("이미 탈퇴")
                    ? "ERR_ALR_SECEDE" : "FAIL";
            return ResponseEntity.badRequest().body(Map.of("code", code, "message", e.getMessage()));
        }
    }

    /**
     * 회원 이름 일괄 조회 - admin 목록화면이 회원ID만 가졌을 때 이름을 채운다.
     * 없는 ID는 결과에 들어있지 않다(AS-IS가 INNER JOIN이라 호출부가 그 행을 버린다).
     */
    @GetMapping("/members/names")
    public Map<Long, String> userNames(@RequestParam List<Long> userIds) {
        return adminMemberService.userNames(userIds);
    }

    /** 회원ID → 로그인ID 일괄조회. admin Q&A 관리(5112) 목록이 AS-IS의 OP_USER 조인 대신 쓴다. */
    @GetMapping("/members/login-ids")
    public Map<Long, String> userLoginIds(@RequestParam List<Long> userIds) {
        return adminMemberService.userLoginIds(userIds);
    }

    /** 로그인ID 부분일치 회원ID 목록. admin Q&A 관리(5112)의 검색구분 '아이디'용. */
    @GetMapping("/members/ids-by-login")
    public List<Long> userIdsByLoginIdLike(@RequestParam String keyword) {
        return adminMemberService.userIdsByLoginIdLike(keyword);
    }

    /**
     * 국민비서(IPS) 문자 수신자 정보. admin Q&A 관리(5112) 답변 저장이 AS-IS
     * {@code QnaMapper.getQnaUserInfo}(OP_QNA·OP_USER·OP_USER_DETAIL INNER JOIN) 대신 쓴다.
     * 회원이나 상세정보가 없으면 AS-IS와 같이 <b>본문 없이</b>(204) 돌려준다.
     */
    @GetMapping("/members/{userId}/sms-receiver")
    public ResponseEntity<AdminMemberService.SmsReceiverDto> smsReceiver(@PathVariable Long userId) {
        AdminMemberService.SmsReceiverDto receiver = adminMemberService.smsReceiver(userId);
        return receiver == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(receiver);
    }

    /** 일반회원관리(4101) 상세 > 배송지 관리 팝업. */
    @GetMapping("/members/{userId}/deliveries")
    public List<AdminMemberService.UserDeliveryRowDto> deliveries(@PathVariable Long userId) {
        return adminMemberService.deliveries(userId);
    }

    /**
     * 일반회원관리(4101) 상세 > 개인정보 열람 이력 기록 (AS-IS G_INDVDLINFO_READNG_HIST).
     * admin이 운영자 비밀번호를 재확인한 뒤 비마스킹 정보를 보여주기 직전에 호출한다.
     */
    @PostMapping("/members/{userId}/pii-access")
    public ResponseEntity<?> recordPiiAccess(@PathVariable Long userId, @RequestParam Long managerUserId) {
        adminMemberService.recordPiiAccess(managerUserId, userId);
        return ResponseEntity.noContent().build();
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

package com.ghlove.member.web;

import com.ghlove.member.service.ManagerAccountLinkService;
import com.ghlove.member.service.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 계정 ↔ 회원 계정 연결 내부 API ({@link ManagerAccountLinkService}) - admin의 MemberClient만 부른다.
 * /api/admin/** 라 내부 공유 시크릿이 있어야 통과한다.
 */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminManagerLinkApiController {

    private final ManagerAccountLinkService linkService;

    /** 권한 승인 복사·본인인증(CI) 확인·로그인 문자 수신자 확인용 회원 정보. 없으면 204. */
    @GetMapping("/{userId}/manager-sync")
    public ResponseEntity<ManagerAccountLinkService.ManagerSyncInfo> syncInfo(@PathVariable Long userId) {
        var info = linkService.syncInfo(userId);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }

    @GetMapping("/manager-sync/by-login-id")
    public ResponseEntity<ManagerAccountLinkService.ManagerSyncInfo> syncInfoByLoginId(@RequestParam String loginId) {
        var info = linkService.syncInfoByLoginId(loginId);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }

    @PostMapping("/next-user-id")
    public Map<String, Long> nextUserId() {
        return Map.of("userId", linkService.nextUserId());
    }

    @GetMapping("/login-id-exists")
    public Map<String, Boolean> loginIdExists(@RequestParam String loginId) {
        return Map.of("exists", linkService.loginIdExists(loginId));
    }

    @PostMapping("/manager-account")
    public ResponseEntity<?> createForManager(@RequestParam String loginId, @RequestParam String password,
                                              @RequestParam(required = false) String userName,
                                              @RequestParam(required = false) String email) {
        try {
            return ResponseEntity.ok(Map.of("userId", linkService.createForManager(loginId, password, userName, email)));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/password-from-manager")
    public Map<String, Boolean> applyPasswordFromManager(@PathVariable Long userId, @RequestParam String passwordHash,
                                                         @RequestParam String passwordType,
                                                         @RequestParam(required = false) String passwordExpiredDate) {
        return Map.of("updated", linkService.applyPasswordFromManager(userId, passwordHash, passwordType, passwordExpiredDate));
    }

    @PostMapping("/{userId}/manager-profile")
    public ResponseEntity<Void> syncProfile(@PathVariable Long userId,
                                            @RequestParam(required = false) String userName,
                                            @RequestParam(required = false) String email,
                                            @RequestParam(required = false) String phoneNumber,
                                            @RequestParam(required = false) String telNumber) {
        linkService.syncProfileFromManager(userId, userName, email, phoneNumber, telNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tel-number/clear")
    public ResponseEntity<Void> clearTelNumber(@RequestParam List<Long> userIds) {
        linkService.clearTelNumber(userIds);
        return ResponseEntity.noContent().build();
    }

    /** 인증서 등록 화면 본인 확인 - charge=loc면 생년월일까지, off면 아이디만. 없으면 204. */
    @GetMapping("/pki-user")
    public ResponseEntity<ManagerAccountLinkService.PkiUser> pkiUser(@RequestParam String loginId,
                                                                     @RequestParam(required = false) String birthday,
                                                                     @RequestParam String charge) {
        var user = linkService.pkiUser(loginId, birthday, "loc".equals(charge));
        return user == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/dn")
    public Map<String, String> updateDn(@PathVariable Long userId, @RequestParam(required = false) String mberDn,
                                        @RequestParam(required = false) String mberFinDn) {
        return Map.of("code", linkService.updateDn(userId, mberDn, mberFinDn));
    }

    @PostMapping("/{userId}/dn/delete")
    public Map<String, String> deleteDn(@PathVariable Long userId, @RequestParam String type,
                                        @RequestParam(required = false) String mberDn,
                                        @RequestParam(required = false) String mberFinDn) {
        return Map.of("code", linkService.deleteDn(userId, type, mberDn, mberFinDn));
    }

    @GetMapping("/by-dn")
    public ResponseEntity<ManagerAccountLinkService.ManagerSyncInfo> byDn(@RequestParam(required = false) String mberDn,
                                                                          @RequestParam(required = false) String mberFinDn) {
        var info = linkService.byDn(mberDn, mberFinDn);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }
}

package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.PointClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** storefront(Vue3 SPA)용 "회원 정보 수정"/"비밀번호 변경"/"회원탈퇴" JSON API -
 * {@link ProfileController}, {@link AuthController#passwordForm}/{@link AuthController#withdrawForm}
 * (전부 Thymeleaf)와 완전히 같은 {@link MemberService} 로직을 JSON 요청/응답으로 감싼다. */
@RestController
@RequiredArgsConstructor
public class ProfileApiController {

    private final MemberService memberService;
    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final AuthCookieSupport authCookieSupport;

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(AuthController.SESSION_USER_KEY);
    }

    @GetMapping("/api/profile")
    public ResponseEntity<ProfileResponse> view(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserDetail profile = memberService.profileOf(user.getUserId());
        var locgovs = donationClient.allLocgovs();
        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm()));

        return ResponseEntity.ok(new ProfileResponse(
                user.getUserId(), user.getLoginId(), user.getUserName(),
                memberService.codesOf("USER_TYPE").get(user.getSbscrbSeCode()),
                "Y".equals(user.getMfaEnabled()),
                user.getEmail(), profile.getPhoneNumber(), profile.getBirthday(),
                profile.getPost(), profile.getAddress(), profile.getAddressDetail(),
                addressRegionOf(profile.getAddress()),
                "Y".equals(profile.getReceiveEmail()), "Y".equals(profile.getReceiveSms()),
                "Y".equals(profile.getReceiveKakao()),
                donationClient.interestLocgovsOf(user.getUserId()),
                locgovs, provinces));
    }

    public record ProfileResponse(Long userId, String loginId, String userName, String userTypeLabel,
                                   boolean mfaEnabled, String email, String phoneNumber, String birthday,
                                   String post, String address, String addressDetail, String addressRegion,
                                   boolean receiveEmail, boolean receiveSms, boolean receiveKakao,
                                   java.util.List<DonationClient.InterestLocgovInfo> interestLocgovs,
                                   java.util.List<DonationClient.LocgovInfo> allLocgovs,
                                   Map<String, String> provinces) {
    }

    public record ProfileUpdateRequest(String phoneNumber, String email, String post, String address,
                                        String addressDetail, boolean receiveEmail, boolean receiveSms,
                                        boolean receiveKakao) {
    }

    @PutMapping("/api/profile")
    public ResponseEntity<Map<String, Object>> update(HttpSession session, HttpServletRequest request,
                                                        @RequestBody ProfileUpdateRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            memberService.updateProfile(user.getUserId(), req.phoneNumber(), req.email(), req.post(),
                    req.address(), req.addressDetail(), req.receiveEmail(), req.receiveSms(), req.receiveKakao(),
                    request.getRemoteAddr());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    public record MfaRequest(boolean enabled) {
    }

    @PostMapping("/api/profile/mfa")
    public ResponseEntity<Map<String, Object>> setMfa(HttpSession session, @RequestBody MfaRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        memberService.setMfaEnabled(user.getUserId(), req.enabled());
        user.setMfaEnabled(req.enabled() ? "Y" : "N");
        session.setAttribute(AuthController.SESSION_USER_KEY, user);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/api/password/verify")
    public ResponseEntity<Void> verifyCurrentPassword(HttpSession session, @RequestBody Map<String, String> body) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (memberService.verifyPassword(user.getUserId(), body.get("currentPassword"))) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(400).build();
    }

    public record PasswordChangeRequest(String currentPassword, String newPassword, String newPasswordConfirm) {
    }

    /** 성공 시 AS-IS와 동일하게 세션/JWT쿠키를 모두 지워 재로그인을 강제한다. */
    @PostMapping("/api/password")
    public ResponseEntity<Map<String, Object>> changePassword(HttpSession session, HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                @RequestBody PasswordChangeRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            memberService.changePassword(user.getUserId(), req.currentPassword(), req.newPassword(),
                    req.newPasswordConfirm(), request.getRemoteAddr());
            session.invalidate();
            authCookieSupport.clear(request, response);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    public record WithdrawInfoResponse(String loginId, String userName, Map<String, String> leaveCodeList,
                                        java.util.List<PointClient.LocgovPointSummary> pointSummary) {
    }

    @GetMapping("/api/withdraw-info")
    public ResponseEntity<WithdrawInfoResponse> withdrawInfo(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new WithdrawInfoResponse(user.getLoginId(), user.getUserName(),
                memberService.codesOf("LEAVE_CODE"), pointClient.locgovSummaryOf(user.getUserId())));
    }

    public record WithdrawRequest(String password, String leaveCode, String reason) {
    }

    @PostMapping("/api/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(HttpSession session, HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          @RequestBody WithdrawRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (req.leaveCode() == null || req.leaveCode().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "탈퇴사유를 선택해 주세요"));
        }
        if (req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "비밀번호를 입력해 주세요"));
        }
        try {
            memberService.withdraw(user.getUserId(), req.password(), req.leaveCode(), req.reason(), request.getRemoteAddr());
            session.invalidate();
            authCookieSupport.clear(request, response);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /** AS-IS는 Daum 우편번호 API로 시도/시군구를 채우지만, 이 MSA는 그 연동이 없어 저장된
     * 주소 문자열의 앞 두 어절로 대신한다({@link ProfileController}와 동일한 규칙). */
    private String addressRegionOf(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String[] tokens = address.trim().split("\\s+");
        return tokens.length == 1 ? tokens[0] : tokens[0] + " " + tokens[1];
    }
}

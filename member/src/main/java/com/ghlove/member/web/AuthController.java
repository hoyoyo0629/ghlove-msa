package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.DevBypassSettings;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 아이디/비밀번호 찾기 AJAX 엔드포인트.
 *
 * <p>로그인·회원가입·비밀번호변경·탈퇴 등 서버렌더(Thymeleaf) 화면 흐름은 storefront Vue3 SPA
 * + {@code /api/*}(AuthApiController 등)로 이관되어 제거됐다(2026-09-17 Thymeleaf 폐기).
 * 남은 것은 storefront의 아이디/비밀번호 찾기 화면이 직접 호출하는 이 AJAX 엔드포인트들뿐이다.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_USER_KEY = "loginUser";

    private final MemberService memberService;
    private final AuthCookieSupport authCookieSupport;
    private final DevBypassSettings devBypassSettings;

    private static final String SESSION_FIND_ID_USER_ID = "pendingFindIdUserId";
    private static final String SESSION_FIND_ID_CODE = "pendingFindIdCode";
    private static final String SESSION_FIND_ID_ISSUED_AT = "pendingFindIdIssuedAt";
    private static final String SESSION_RESET_USER_ID = "pendingResetUserId";
    private static final String SESSION_RESET_CODE = "pendingResetCode";
    private static final String SESSION_RESET_ISSUED_AT = "pendingResetIssuedAt";
    private static final String SESSION_RESET_VERIFIED = "pendingResetVerified";
    private static final int VERIFICATION_CODE_VALID_MINUTES = 5;

    /** 아이디 찾기 1단계: 이름+휴대폰번호 본인확인, 통과하면 인증번호 발송. */
    @PostMapping("/find-idpw/id/send-code")
    @ResponseBody
    public java.util.Map<String, Object> findIdSendCode(@RequestParam String userName, @RequestParam String phoneNumber,
                                                          HttpSession session) {
        try {
            var start = memberService.startFindId(userName, phoneNumber);
            session.setAttribute(SESSION_FIND_ID_USER_ID, start.userId());
            session.setAttribute(SESSION_FIND_ID_CODE, start.code());
            session.setAttribute(SESSION_FIND_ID_ISSUED_AT, java.time.LocalDateTime.now());
            return result("CODE_SENT", start.maskedPhone() + " (으)로 인증번호를 발송했습니다.", start.devCode());
        } catch (MemberException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    @PostMapping("/find-idpw/id/verify")
    @ResponseBody
    public java.util.Map<String, Object> findIdVerify(@RequestParam String code, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_FIND_ID_USER_ID);
        if (userId == null || !verifyPendingCode(session, SESSION_FIND_ID_CODE, SESSION_FIND_ID_ISSUED_AT, code)) {
            return result("ERROR", "인증번호가 일치하지 않거나 만료되었습니다.", null);
        }
        session.removeAttribute(SESSION_FIND_ID_USER_ID);
        session.removeAttribute(SESSION_FIND_ID_CODE);
        session.removeAttribute(SESSION_FIND_ID_ISSUED_AT);
        java.util.Map<String, Object> body = result("OK", null, null);
        body.put("loginId", memberService.loginIdOf(userId));
        return body;
    }

    /**
     * 아이디 찾기 - 인증번호 단계를 건너뛰고 이름+휴대폰번호만으로 아이디를 알려준다.
     * 실제 본인인증 게이트웨이가 없는 환경의 테스트용이라 {@link DevBypassSettings}로 막는다.
     */
    @PostMapping("/find-idpw/id/bypass")
    @ResponseBody
    public java.util.Map<String, Object> findIdBypass(@RequestParam String userName, @RequestParam String phoneNumber) {
        if (!devBypassSettings.isIdentityVerificationBypass()) {
            return result("ERROR", "본인인증 우회가 허용되지 않는 환경입니다.", null);
        }
        try {
            Long userId = memberService.verifyIdentityByNameAndPhone(userName, phoneNumber);
            java.util.Map<String, Object> body = result("OK", null, null);
            body.put("loginId", memberService.loginIdOf(userId));
            return body;
        } catch (MemberException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    /**
     * 비밀번호 찾기 - 인증번호 단계를 건너뛰고 곧바로 "새 비밀번호 설정" 단계로 보낸다.
     * 아이디+이름+휴대폰번호 일치 검사는 정상 경로와 똑같이 거친다(그마저 없으면 아무 계정이나
     * 바꿀 수 있게 된다). 그래도 계정 탈취 경로이므로 우회 스위치로 막는다.
     */
    @PostMapping("/find-idpw/pw/bypass")
    @ResponseBody
    public java.util.Map<String, Object> resetPwBypass(@RequestParam String loginId, @RequestParam String userName,
                                                         @RequestParam String phoneNumber, HttpSession session) {
        if (!devBypassSettings.isIdentityVerificationBypass()) {
            return result("ERROR", "본인인증 우회가 허용되지 않는 환경입니다.", null);
        }
        try {
            Long userId = memberService.verifyIdentityForReset(loginId, userName, phoneNumber);
            session.setAttribute(SESSION_RESET_USER_ID, userId);
            session.setAttribute(SESSION_RESET_VERIFIED, Boolean.TRUE);
            session.removeAttribute(SESSION_RESET_CODE);
            session.removeAttribute(SESSION_RESET_ISSUED_AT);
            return result("OK", null, null);
        } catch (MemberException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    /** 비밀번호 찾기 1단계: 아이디+이름+휴대폰번호 본인확인, 통과하면 인증번호 발송. */
    @PostMapping("/find-idpw/pw/send-code")
    @ResponseBody
    public java.util.Map<String, Object> resetPwSendCode(@RequestParam String loginId, @RequestParam String userName,
                                                           @RequestParam String phoneNumber, HttpSession session) {
        try {
            var start = memberService.startResetPassword(loginId, userName, phoneNumber);
            session.setAttribute(SESSION_RESET_USER_ID, start.userId());
            session.setAttribute(SESSION_RESET_CODE, start.code());
            session.setAttribute(SESSION_RESET_ISSUED_AT, java.time.LocalDateTime.now());
            session.removeAttribute(SESSION_RESET_VERIFIED);
            return result("CODE_SENT", start.maskedPhone() + " (으)로 인증번호를 발송했습니다.", start.devCode());
        } catch (MemberException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    @PostMapping("/find-idpw/pw/verify")
    @ResponseBody
    public java.util.Map<String, Object> resetPwVerify(@RequestParam String code, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_RESET_USER_ID);
        if (userId == null || !verifyPendingCode(session, SESSION_RESET_CODE, SESSION_RESET_ISSUED_AT, code)) {
            return result("ERROR", "인증번호가 일치하지 않거나 만료되었습니다.", null);
        }
        session.setAttribute(SESSION_RESET_VERIFIED, Boolean.TRUE);
        return result("OK", null, null);
    }

    /** 비밀번호 찾기 2단계: 본인인증을 마친 상태에서만 새 비밀번호를 설정한다 - AS-IS처럼
     *  임시비밀번호를 보여주지 않고 곧바로 자동 로그인시킨다. */
    @PostMapping("/find-idpw/pw/reset")
    @ResponseBody
    public java.util.Map<String, Object> resetPwSubmit(@RequestParam String newPassword, @RequestParam String newPasswordConfirm,
                                                         HttpSession session, HttpServletResponse response) {
        Long userId = (Long) session.getAttribute(SESSION_RESET_USER_ID);
        Boolean verified = (Boolean) session.getAttribute(SESSION_RESET_VERIFIED);
        if (userId == null || !Boolean.TRUE.equals(verified)) {
            return result("ERROR", "본인인증을 먼저 완료해 주세요.", null);
        }
        try {
            memberService.resetPasswordVerified(userId, newPassword, newPasswordConfirm);
            session.removeAttribute(SESSION_RESET_USER_ID);
            session.removeAttribute(SESSION_RESET_CODE);
            session.removeAttribute(SESSION_RESET_ISSUED_AT);
            session.removeAttribute(SESSION_RESET_VERIFIED);

            User user = memberService.byId(userId);
            session.setAttribute(SESSION_USER_KEY, user);
            authCookieSupport.issue(response, user);
            java.util.Map<String, Object> body = result("OK", null, null);
            body.put("redirect", "/mypage");
            return body;
        } catch (MemberException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    private boolean verifyPendingCode(HttpSession session, String codeKey, String issuedAtKey, String inputCode) {
        String expected = (String) session.getAttribute(codeKey);
        java.time.LocalDateTime issuedAt = (java.time.LocalDateTime) session.getAttribute(issuedAtKey);
        if (expected == null || issuedAt == null) {
            return false;
        }
        if (issuedAt.isBefore(java.time.LocalDateTime.now().minusMinutes(VERIFICATION_CODE_VALID_MINUTES))) {
            return false;
        }
        return inputCode != null && inputCode.trim().equals(expected);
    }

    private static java.util.Map<String, Object> result(String status, String message, String devCode) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", status);
        if (message != null) {
            body.put("message", message);
        }
        if (devCode != null) {
            body.put("devCode", devCode);
        }
        return body;
    }
}

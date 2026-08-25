package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.BannerClient;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.JwtSupport;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.PointClient;
import com.ghlove.member.service.SignupForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_USER_KEY = "loginUser";

    /** 로그인 필요 화면 진입시 공용 리다이렉트 - 로그인 성공 후 원래 보려던 화면(마이페이지/
     *  회원정보수정/배송지관리 등)으로 돌아가도록 target을 실어 보낸다. 다른 서비스가 이미
     *  쓰는 "8081~8086 자기 오리진으로만 리다이렉트 허용" 규칙({@link #safeRedirectTarget})과
     *  동일한 안전장치를 통과하도록 절대URL로 인코딩한다. */
    public static String loginRedirect(String returnPath) {
        return "redirect:/login?target=" + java.net.URLEncoder.encode(
                "http://localhost:8081" + returnPath, java.nio.charset.StandardCharsets.UTF_8);
    }

    private final MemberService memberService;
    private final DonationClient donationClient;
    private final BannerClient bannerClient;
    private final PointClient pointClient;
    private final JwtSupport jwtSupport;

    /**
     * 메인 화면 (AS-IS https://www.ilovegohyang.go.kr/main.html 과 동일하게 로그인 없이도
     * 공개 열람 가능 - 실제 사이트도 답례품/지정기부사업 둘러보기는 비로그인으로 되고
     * 기부 신청 시점에만 로그인을 요구한다). 로그인 상태면 개인화 영역만 추가로 보여준다.
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser != null) {
            model.addAttribute("user", loginUser);
            model.addAttribute("userTypeLabel", memberService.codesOf("USER_TYPE").get(loginUser.getSbscrbSeCode()));
            model.addAttribute("roles", memberService.rolesOf(loginUser.getUserId()));
            model.addAttribute("roleLabels", memberService.codesOf("ROLE"));
            model.addAttribute("donationSummary", donationClient.mySummary(loginUser.getUserId()));
            model.addAttribute("pointBalance", pointClient.balanceOf(loginUser.getUserId()));
        }
        // 메인화면은 지정기부사업을 4건까지만 보여준다 (AS-IS main.html과 동일 - 전체
        // 목록은 별도 화면이 생기면 그쪽으로).
        var projects = donationClient.openProjects();
        model.addAttribute("projects", projects.size() > 4 ? projects.subList(0, 4) : projects);
        model.addAttribute("banners", bannerClient.activeBanners());
        model.addAttribute("giveState", donationClient.giveState());
        return "home";
    }

    /** AS-IS GNB에는 있지만 이 MVP 범위 밖인 화면들(장바구니, FAQ, 이벤트 등)이 공통으로 향하는 안내 페이지. */
    @GetMapping("/coming-soon")
    public String comingSoon(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser != null) {
            model.addAttribute("user", loginUser);
        }
        return "coming-soon";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            memberService.signup(form);
        } catch (MemberException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
        return "redirect:/login?signup=success";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId, @RequestParam String password,
                         @RequestParam(required = false) String target,
                         HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        try {
            User user = memberService.login(loginId, password, request.getRemoteAddr());
            session.setAttribute(SESSION_USER_KEY, user);
            issueAuthCookie(response, user);
            return "redirect:" + safeRedirectTarget(target);
        } catch (MemberException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        }
    }

    /** 다른 서비스 화면(장바구니 등)에서 로그인 없이 들어와 /login?target=...으로 넘어온
     *  경우, 로그인 성공 후 원래 있던 곳으로 돌려보낸다. 임의 사이트로 열린 리다이렉트가
     *  되지 않도록 이 프로젝트가 아는 로컬 서비스 오리진(8081~8086)으로만 제한한다. */
    private String safeRedirectTarget(String target) {
        if (target != null && target.matches("^http://localhost:808[1-6](/.*)?$")) {
            return target;
        }
        return "/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        clearAuthCookie(response);
        return "redirect:/login";
    }

    private static final String SESSION_FIND_ID_USER_ID = "pendingFindIdUserId";
    private static final String SESSION_FIND_ID_CODE = "pendingFindIdCode";
    private static final String SESSION_FIND_ID_ISSUED_AT = "pendingFindIdIssuedAt";
    private static final String SESSION_RESET_USER_ID = "pendingResetUserId";
    private static final String SESSION_RESET_CODE = "pendingResetCode";
    private static final String SESSION_RESET_ISSUED_AT = "pendingResetIssuedAt";
    private static final String SESSION_RESET_VERIFIED = "pendingResetVerified";
    private static final int VERIFICATION_CODE_VALID_MINUTES = 5;

    @GetMapping("/find-idpw")
    public String findIdPwForm() {
        return "find-idpw";
    }

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
            issueAuthCookie(response, user);
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

    @GetMapping("/password")
    public String passwordForm(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return loginRedirect("/password");
        }
        model.addAttribute("loginId", loginUser.getLoginId());
        return "password";
    }

    /** "비밀번호 변경" 화면의 "인증" 버튼 (AS-IS checkPresentPwd) - AJAX로 현재 비밀번호만
     *  먼저 재확인해, 통과해야 새 비밀번호 입력칸이 열린다. */
    @PostMapping("/password/verify")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Void> verifyCurrentPassword(@RequestParam String currentPassword, HttpSession session) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return org.springframework.http.ResponseEntity.status(401).build();
        }
        if (memberService.verifyPassword(loginUser.getUserId(), currentPassword)) {
            return org.springframework.http.ResponseEntity.ok().build();
        }
        return org.springframework.http.ResponseEntity.status(400).build();
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
                                  @RequestParam String newPasswordConfirm,
                                  HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        try {
            memberService.changePassword(loginUser.getUserId(), currentPassword, newPassword, newPasswordConfirm,
                    request.getRemoteAddr());
            session.invalidate();
            clearAuthCookie(response);
            return "redirect:/login?passwordChanged=success";
        } catch (MemberException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("loginId", loginUser.getLoginId());
            return "password";
        }
    }

    @GetMapping("/withdraw")
    public String withdrawForm(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return loginRedirect("/withdraw");
        }
        model.addAttribute("loginId", loginUser.getLoginId());
        model.addAttribute("userName", loginUser.getUserName());
        model.addAttribute("leaveCodeList", memberService.codesOf("LEAVE_CODE"));
        model.addAttribute("pointSummary", pointClient.locgovSummaryOf(loginUser.getUserId()));
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String password, @RequestParam(required = false) String leaveCode,
                            @RequestParam(required = false) String reason,
                            HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        if (leaveCode == null || leaveCode.isBlank()) {
            return withdrawFormWithError(loginUser, "탈퇴사유를 선택해 주세요", model);
        }
        if (password == null || password.isBlank()) {
            return withdrawFormWithError(loginUser, "비밀번호를 입력해 주세요", model);
        }
        try {
            memberService.withdraw(loginUser.getUserId(), password, leaveCode, reason, request.getRemoteAddr());
            session.invalidate();
            clearAuthCookie(response);
            return "redirect:/login?withdrawn=success";
        } catch (MemberException e) {
            return withdrawFormWithError(loginUser, e.getMessage(), model);
        }
    }

    private String withdrawFormWithError(User loginUser, String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("loginId", loginUser.getLoginId());
        model.addAttribute("userName", loginUser.getUserName());
        model.addAttribute("leaveCodeList", memberService.codesOf("LEAVE_CODE"));
        model.addAttribute("pointSummary", pointClient.locgovSummaryOf(loginUser.getUserId()));
        return "withdraw";
    }

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER_KEY);
    }

    /**
     * localhost의 다른 포트(8082~8086)에도 이 쿠키가 자동으로 같이 전송된다(쿠키 스코프는
     * 포트를 구분하지 않음) - Domain 속성을 일부러 지정하지 않는다. secure=false는 이
     * 로컬 개발환경이 평문 http라서 그런 것 - 운영에서는 반드시 true로 바꿔야 한다.
     */
    private void issueAuthCookie(HttpServletResponse response, User user) {
        String token = jwtSupport.issue(user.getUserId(), user.getLoginId());
        ResponseCookie cookie = ResponseCookie.from(JwtSupport.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtSupport.expirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(JwtSupport.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

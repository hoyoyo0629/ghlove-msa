package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.BannerClient;
import com.ghlove.member.service.BannerInfo;
import com.ghlove.member.service.DesignatedProjectInfo;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.GiveStateInfo;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.PointClient;
import com.ghlove.member.service.RefreshTokenService;
import com.ghlove.member.service.SignupForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * storefront(Vue3 SPA)용 JSON 인증/메인화면 API - Thymeleaf 화면용 AuthController와 완전히
 * 같은 로직(MemberService.loginWithMfaCheck, 세션+GH_AUTH 쿠키 발급)을 JSON 요청/응답으로만
 * 감싼다. 로그인 세션은 member 자체 화면과 동일하게 HttpSession(loginUser)을 그대로 쓰고,
 * 다른 서비스(donation/gift/order/point)는 지금처럼 GH_AUTH 쿠키만으로 인증한다 - 이 컨트롤러가
 * 그 세션의 유일한 JSON 창구(/api/auth/me)를 새로 연다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthApiController {

    private final MemberService memberService;
    private final DonationClient donationClient;
    private final BannerClient bannerClient;
    private final PointClient pointClient;
    private final AuthCookieSupport authCookieSupport;
    private final RefreshTokenService refreshTokenService;

    private static final String SESSION_USER_KEY = AuthController.SESSION_USER_KEY;
    private static final String SESSION_PENDING_MFA_USER_ID = "pendingMfaUserId";

    @GetMapping("/auth/me")
    public Map<String, Object> me(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        Map<String, Object> body = new LinkedHashMap<>();
        if (user == null) {
            body.put("loggedIn", false);
            return body;
        }
        body.put("loggedIn", true);
        body.put("userId", user.getUserId());
        body.put("loginId", user.getLoginId());
        body.put("userName", user.getUserName());
        body.put("userTypeLabel", memberService.codesOf("USER_TYPE").get(user.getSbscrbSeCode()));
        body.put("roles", memberService.rolesOf(user.getUserId()));
        body.put("donationSummary", donationClient.mySummary(user.getUserId()));
        body.put("pointBalance", pointClient.balanceOf(user.getUserId()));
        return body;
    }

    /** 메인화면(비로그인도 열람 가능한 공개 데이터)용 - AuthController.home()의 공개 영역과 동일. */
    @GetMapping("/home")
    public HomeResponse home() {
        List<DesignatedProjectInfo> projects = donationClient.openProjects();
        List<BannerInfo> banners = bannerClient.activeBanners();
        GiveStateInfo giveState = donationClient.giveState();
        return new HomeResponse(banners, projects.size() > 4 ? projects.subList(0, 4) : projects, giveState);
    }

    public record HomeResponse(List<BannerInfo> banners, List<DesignatedProjectInfo> projects, GiveStateInfo giveState) {
    }

    public record LoginRequest(String loginId, String password, String target) {
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpServletRequest request,
                                      HttpServletResponse response, HttpSession session) {
        try {
            MemberService.LoginOutcome outcome = memberService.loginWithMfaCheck(req.loginId(), req.password(), request.getRemoteAddr());
            if (outcome.mfaRequired()) {
                session.setAttribute(SESSION_PENDING_MFA_USER_ID, outcome.user().getUserId());
                return statusBody("MFA_REQUIRED", null, Map.of(
                        "maskedPhone", outcome.maskedPhone(),
                        "devCode", outcome.devCode() != null ? outcome.devCode() : ""));
            }
            session.setAttribute(SESSION_USER_KEY, outcome.user());
            authCookieSupport.issue(response, outcome.user());
            return statusBody("OK", null, Map.of());
        } catch (MemberException e) {
            return statusBody("ERROR", e.getMessage(), Map.of());
        }
    }

    public record MfaVerifyRequest(String code) {
    }

    @PostMapping("/auth/login/mfa-verify")
    public Map<String, Object> mfaVerify(@RequestBody MfaVerifyRequest req, HttpServletResponse response, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_PENDING_MFA_USER_ID);
        if (userId == null) {
            return statusBody("ERROR", "인증 세션이 만료되었습니다. 다시 로그인해 주세요.", Map.of());
        }
        try {
            User user = memberService.verifyMfaAndCompleteLogin(userId, req.code());
            session.removeAttribute(SESSION_PENDING_MFA_USER_ID);
            session.setAttribute(SESSION_USER_KEY, user);
            authCookieSupport.issue(response, user);
            return statusBody("OK", null, Map.of());
        } catch (MemberException e) {
            return statusBody("ERROR", e.getMessage(), Map.of());
        }
    }

    @PostMapping("/auth/logout")
    public Map<String, Object> logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        session.invalidate();
        authCookieSupport.clear(request, response);
        return statusBody("OK", null, Map.of());
    }

    /**
     * SFR-002 "인증토큰/세션 관리(재인증 정책)" - GH_AUTH 액세스 토큰이 만료돼도 GH_REFRESH
     * 쿠키가 아직 유효하면 재로그인 없이 새 액세스 토큰(+새 refresh 토큰, 회전)을 발급한다.
     */
    @PostMapping("/auth/refresh")
    public Map<String, Object> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieSupport.readRefreshToken(request);
        return refreshTokenService.consume(refreshToken)
                .flatMap(memberService::findById)
                .map(user -> {
                    authCookieSupport.issue(response, user);
                    return statusBody("OK", null, Map.of());
                })
                .orElseGet(() -> statusBody("ERROR", "다시 로그인해 주세요.", Map.of()));
    }

    /** AS-IS join.html submit() - 가입 저장 성공 즉시 자동 로그인 처리(세션+쿠키 발급)한다. */
    @PostMapping("/auth/signup")
    public Map<String, Object> signup(@Valid @RequestBody SignupForm form, BindingResult bindingResult,
                                       HttpServletResponse response, HttpSession session) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors().stream()
                    .findFirst().map(e -> e.getDefaultMessage()).orElse("입력값을 확인해 주세요.");
            return statusBody("ERROR", message, Map.of());
        }
        try {
            User user = memberService.signup(form);
            session.setAttribute(SESSION_USER_KEY, user);
            authCookieSupport.issue(response, user);
            return statusBody("OK", null, Map.of());
        } catch (MemberException e) {
            return statusBody("ERROR", e.getMessage(), Map.of());
        }
    }

    private static Map<String, Object> statusBody(String status, String message, Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (message != null) {
            body.put("message", message);
        }
        body.putAll(extra);
        return body;
    }
}

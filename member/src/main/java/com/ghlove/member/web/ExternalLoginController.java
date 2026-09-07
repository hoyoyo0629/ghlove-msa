package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.ExternalLoginService;
import com.ghlove.member.service.integration.ExternalIdentity;
import com.ghlove.member.service.integration.OAuth2LoginClient;
import com.ghlove.member.service.integration.OnePassClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * 디지털원패스 SSO / 카카오·네이버 SNS 로그인 연계 (AS-IS AuthController
 * `/onepass-login`,`/onepass-callback`, NaverController, kakao 로그인 경로).
 * 처음 연결되는 신원이면 즉시 간편가입 후 바로 로그인 처리한다.
 */
@Controller
public class ExternalLoginController {

    /** enabled=false일 때 표시할 화면용 라벨 - /login/mock/{provider}가 실제로 처리 가능한 값만. */
    private static final Map<String, String> MOCK_PROVIDER_LABELS = Map.of(
            "ONEPASS", "디지털원패스", "KAKAO", "카카오", "NAVER", "네이버",
            "FINANCE_CERT", "금융인증서", "ANYID", "간편인증");

    private final OnePassClient onePassClient;
    private final OAuth2LoginClient kakaoLoginClient;
    private final OAuth2LoginClient naverLoginClient;
    private final ExternalLoginService externalLoginService;
    private final AuthCookieSupport authCookieSupport;

    public ExternalLoginController(OnePassClient onePassClient,
                                    @Qualifier("kakaoLoginClient") OAuth2LoginClient kakaoLoginClient,
                                    @Qualifier("naverLoginClient") OAuth2LoginClient naverLoginClient,
                                    ExternalLoginService externalLoginService,
                                    AuthCookieSupport authCookieSupport) {
        this.onePassClient = onePassClient;
        this.kakaoLoginClient = kakaoLoginClient;
        this.naverLoginClient = naverLoginClient;
        this.externalLoginService = externalLoginService;
        this.authCookieSupport = authCookieSupport;
    }

    @GetMapping("/onepass-login")
    public String onePassLogin(Model model) {
        if (!onePassClient.isEnabled()) {
            return mockScreen(model, "ONEPASS");
        }
        return "redirect:" + onePassClient.buildLoginRedirectUrl();
    }

    @GetMapping("/onepass-callback")
    public String onePassCallback(@RequestParam Map<String, String> params, HttpSession session,
                                   HttpServletResponse response, Model model) {
        try {
            ExternalIdentity identity = onePassClient.verifyCallback(params);
            return completeLogin(identity, session, response);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "디지털원패스 로그인에 실패했습니다: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/login/kakao")
    public String kakaoLogin(HttpSession session, Model model) {
        return oauthLogin(kakaoLoginClient, "KAKAO", session, model);
    }

    @GetMapping("/login/kakao/callback")
    public String kakaoCallback(@RequestParam String code, HttpSession session,
                                 HttpServletResponse response, Model model) {
        return oauthCallback(kakaoLoginClient, code, session, response, model);
    }

    @GetMapping("/login/naver")
    public String naverLogin(HttpSession session, Model model) {
        return oauthLogin(naverLoginClient, "NAVER", session, model);
    }

    @GetMapping("/login/naver/callback")
    public String naverCallback(@RequestParam String code, HttpSession session,
                                 HttpServletResponse response, Model model) {
        return oauthCallback(naverLoginClient, code, session, response, model);
    }

    /**
     * 금융인증서 로그인 (AS-IS financLogin() - 금융결제원 YesKey SDK를 스크립트로 주입해서
     * 뜨는 인증창) / 간편인증 로그인 (AS-IS openSimpeAuth() - AnyID 통합인증 팝업창).
     * 둘 다 카카오/네이버 SNS 로그인과 달리 우리 도메인이 아닌 외부 인증기관 화면을 직접
     * 띄우는 방식이라(SDK 스크립트 주입/팝업창) 리다이렉트형 OAuth 클라이언트로 흉내낼
     * 대상 자체가 없다 - 화면/버튼은 동일하게 두고 {@link #mockLogin}으로 모의 통과시킨다
     * ([[feedback-mock-integration-same-screen]]).
     */
    @GetMapping("/login/finance-cert")
    public String financeCertLogin(Model model) {
        return mockScreen(model, "FINANCE_CERT");
    }

    @GetMapping("/login/simple-auth")
    public String simpleAuthLogin(Model model) {
        return mockScreen(model, "ANYID");
    }

    /** 5개 외부인증수단 공용 모의 통과 - 실제 연계가 열려(enabled=true) 진짜 프로토콜을 타야
     *  하는 provider는 여기서 막는다(모의 로그인이 실연계를 우회하지 못하게 하는 방어). */
    @PostMapping("/login/mock/{provider}")
    public String mockLogin(@PathVariable String provider, HttpSession session, HttpServletResponse response, Model model) {
        String label = MOCK_PROVIDER_LABELS.get(provider);
        if (label == null || isRealClientEnabled(provider)) {
            model.addAttribute("errorMessage", "모의 로그인을 사용할 수 없는 연계 수단입니다.");
            return "login";
        }
        ExternalIdentity identity = new ExternalIdentity(provider, "MOCK", label + " 모의회원", null, null);
        return completeLogin(identity, session, response);
    }

    private boolean isRealClientEnabled(String provider) {
        return switch (provider) {
            case "ONEPASS" -> onePassClient.isEnabled();
            case "KAKAO" -> kakaoLoginClient.isEnabled();
            case "NAVER" -> naverLoginClient.isEnabled();
            default -> false; // FINANCE_CERT/ANYID는 진짜 클라이언트 자체가 없다
        };
    }

    private String mockScreen(Model model, String providerCode) {
        model.addAttribute("providerCode", providerCode);
        model.addAttribute("providerName", MOCK_PROVIDER_LABELS.get(providerCode));
        return "external-login-mock";
    }

    /**
     * 회원가입 2단계(본인인증)의 금융인증서/휴대폰 인증 - AS-IS mobile_auth.vue의 두 방식 모두
     * (financ.js SDK / Siren24 PCC 팝업) 로그인 화면과 동일하게 외부 인증기관 화면을 직접
     * 띄우는 방식이라 흉내낼 대상이 없다. signup.html에는 이 두 버튼과 별도로 "본인인증 없이
     * 진행(테스트용)" 우회 링크를 두어, 이 스텁을 거치고도 회원가입 자체는 끝까지 테스트할
     * 수 있게 해둔다.
     */
    @GetMapping("/signup/finance-cert")
    public String signupFinanceCert(Model model) {
        model.addAttribute("providerName", "금융인증서");
        model.addAttribute("backUrl", "/signup");
        model.addAttribute("backLabel", "회원가입으로 돌아가기");
        return "external-login-disabled";
    }

    @GetMapping("/signup/mobile-auth")
    public String signupMobileAuth(Model model) {
        model.addAttribute("providerName", "휴대폰 본인인증");
        model.addAttribute("backUrl", "/signup");
        model.addAttribute("backLabel", "회원가입으로 돌아가기");
        return "external-login-disabled";
    }

    private String oauthLogin(OAuth2LoginClient client, String providerCode, HttpSession session, Model model) {
        if (!client.isEnabled()) {
            return mockScreen(model, providerCode);
        }
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauthState", state);
        return "redirect:" + client.buildAuthorizeUrl(state);
    }

    private String oauthCallback(OAuth2LoginClient client, String code, HttpSession session,
                                  HttpServletResponse response, Model model) {
        try {
            ExternalIdentity identity = client.exchange(code);
            return completeLogin(identity, session, response);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "SNS 로그인에 실패했습니다: " + e.getMessage());
            return "login";
        }
    }

    /** AuthController/AuthApiController와 동일하게 HttpSession(Thymeleaf 화면용)과
     *  GH_AUTH JWT 쿠키(storefront SPA 등 JWT 기반 서비스용)를 항상 같이 발급한다 -
     *  이 메서드가 세션만 채우고 쿠키 발급을 빠뜨리면 SNS/디지털원패스로 로그인해도
     *  storefront에서는 로그인 상태가 반영되지 않는다. */
    private String completeLogin(ExternalIdentity identity, HttpSession session, HttpServletResponse response) {
        User user = externalLoginService.findLinkedUser(identity)
                .orElseGet(() -> externalLoginService.linkNewAccount(identity));
        session.setAttribute(AuthController.SESSION_USER_KEY, user);
        authCookieSupport.issue(response, user);
        return "redirect:/";
    }
}

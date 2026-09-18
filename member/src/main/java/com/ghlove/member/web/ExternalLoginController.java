package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.ExternalLoginService;
import com.ghlove.member.service.integration.ExternalAuthException;
import com.ghlove.member.service.integration.ExternalIdentity;
import com.ghlove.member.service.integration.KakaoCertClient;
import com.ghlove.member.service.integration.OAuth2LoginClient;
import com.ghlove.member.service.integration.OnePassClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * 디지털원패스 SSO / 카카오 인증서비스 / 네이버 SNS 로그인 연계 (AS-IS AuthController
 * `/onepass-login`,`/onepass-callback`, NaverController, KakaoLinkController).
 * 처음 연결되는 신원이면 즉시 간편가입 후 바로 로그인 처리한다.
 *
 * <p>카카오는 인가 단계를 화면(JS SDK authorizeForCert)이 맡고 검증은 JSON API
 * ({@link ExternalAuthApiController})로 처리한다 - 이 컨트롤러의 /login/kakao는 실연계가
 * 꺼져 있을 때의 모의 통과 경로이고, 켜져 있으면 SDK를 띄우는 화면으로 되돌린다.
 *
 * <p>진입 경로가 로그인 화면이냐 회원가입 화면이냐를 type 파라미터로 받아 세션에 기억한다 -
 * AS-IS도 kakao-link-login에 type=JOIN을 실어 보내고, 그 결과가 신규가입(JOIN_MEMBER)이면
 * "카카오톡 인증 로그인 통한 회원가입 완료" 안내를 띄웠다.
 *
 * <p>이 컨트롤러가 돌려주는 화면/리다이렉트는 member를 직접(:8081) 열었을 때와 Kong/vite
 * 프록시의 /member 접두사를 거쳐 storefront SPA에서 열었을 때 모두 동작해야 한다. 그래서
 * 모의 인증 안내화면의 form은 action=""(현재 URL로 그대로 POST)으로 두고 CSS도 인라인으로
 * 넣는다 - 루트 절대경로(/css/style.css, /login/mock/...)는 접두사 아래에서 프록시 대상
 * 밖으로 나가버려 깨진다.
 */
@Controller
public class ExternalLoginController {

    /** enabled=false일 때 표시할 화면용 라벨 - 모의 인증을 실제로 처리할 수 있는 값만. */
    private static final Map<String, String> MOCK_PROVIDER_LABELS = Map.of(
            "ONEPASS", "디지털원패스", "KAKAO", "카카오", "NAVER", "네이버",
            "FINANCE_CERT", "금융인증서", "ANYID", "간편인증");
    /** 가입완료 안내문에 쓰는 이름 - AS-IS join.html의 authName("카카오톡"/"네이버")과 동일.
     *  리다이렉트 URL에는 이 한글이 아니라 provider 코드를 실어 보낸다(Location 헤더에
     *  non-ASCII를 담으면 헤더 자체가 유실된다) - 라벨 변환은 화면을 렌더링하는 쪽에서 한다. */
    static final Map<String, String> AUTH_NAMES = Map.of(
            "ONEPASS", "디지털원패스", "KAKAO", "카카오톡", "NAVER", "네이버",
            "FINANCE_CERT", "금융인증서", "ANYID", "간편인증");

    /** 회원가입 화면에서 시작한 인증인지(JOIN) 로그인 화면에서 시작한 인증인지(LOGIN). */
    static final String SESSION_EXTERNAL_AUTH_FLOW = "externalAuthFlow";
    static final String FLOW_JOIN = "JOIN";

    private final OnePassClient onePassClient;
    private final KakaoCertClient kakaoCertClient;
    private final OAuth2LoginClient naverLoginClient;
    private final ExternalLoginService externalLoginService;
    private final AuthCookieSupport authCookieSupport;

    public ExternalLoginController(OnePassClient onePassClient,
                                    KakaoCertClient kakaoCertClient,
                                    @Qualifier("naverLoginClient") OAuth2LoginClient naverLoginClient,
                                    ExternalLoginService externalLoginService,
                                    AuthCookieSupport authCookieSupport) {
        this.onePassClient = onePassClient;
        this.kakaoCertClient = kakaoCertClient;
        this.naverLoginClient = naverLoginClient;
        this.externalLoginService = externalLoginService;
        this.authCookieSupport = authCookieSupport;
    }

    @GetMapping("/onepass-login")
    public String onePassLogin(@RequestParam(required = false) String type, HttpSession session, Model model) {
        rememberFlow(session, type);
        if (!onePassClient.isEnabled()) {
            return mockScreen(model, "ONEPASS", session);
        }
        return "redirect:" + onePassClient.buildLoginRedirectUrl();
    }

    @GetMapping("/onepass-callback")
    public String onePassCallback(@RequestParam Map<String, String> params, HttpSession session,
                                   HttpServletResponse response, Model model) {
        try {
            ExternalIdentity identity = onePassClient.verifyCallback(params);
            return completeLogin(identity, session, response);
        } catch (ExternalAuthException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "디지털원패스 로그인에 실패했습니다: " + e.getMessage());
            return "login";
        }
    }

    /**
     * 카카오 인증서비스는 실연계가 켜지면 화면이 JS SDK로 인증창을 띄운다(AS-IS와 동일) -
     * 서버가 authorize URL을 조립하지 않으므로 이 URL은 실연계에서 쓰이지 않는다. 직접
     * 들어온 경우엔 버튼이 있는 화면으로 돌려보내기만 한다(회원가입은 필수약관 동의를 먼저
     * 확인해야 해서 여기서 인증을 자동으로 시작하면 안 된다). 꺼져 있으면 지금까지처럼
     * 모의 통과 안내화면을 띄운다.
     */
    @GetMapping("/login/kakao")
    public String kakaoLogin(@RequestParam(required = false) String type, HttpSession session, Model model) {
        rememberFlow(session, type);
        if (kakaoCertClient.isEnabled()) {
            return isJoinFlow(session) ? "redirect:/signup" : "redirect:/login";
        }
        return mockScreen(model, "KAKAO", session);
    }

    @GetMapping("/login/naver")
    public String naverLogin(@RequestParam(required = false) String type, HttpSession session, Model model) {
        return oauthLogin(naverLoginClient, "NAVER", type, session, model);
    }

    @GetMapping("/login/naver/callback")
    public String naverCallback(@RequestParam String code, HttpSession session,
                                 HttpServletResponse response, Model model) {
        return oauthCallback(naverLoginClient, code, session, response, model);
    }

    /**
     * 금융인증서 로그인 (AS-IS financLogin() - 금융결제원 YesKey SDK를 스크립트로 주입해서
     * 뜨는 인증창) / 간편인증 로그인 (AS-IS openSimpeAuth() - AnyID 통합인증 팝업창).
     * 둘 다 우리 도메인이 아닌 외부 인증기관 화면을 직접 띄우는 방식이라(SDK 스크립트 주입/
     * 팝업창) 리다이렉트형으로 흉내낼 대상 자체가 없다 - 화면/버튼은 동일하게 두고 모의
     * 인증으로 통과시킨다.
     */
    @GetMapping("/login/finance-cert")
    public String financeCertLogin(@RequestParam(required = false) String type, HttpSession session, Model model) {
        rememberFlow(session, type);
        return mockScreen(model, "FINANCE_CERT", session);
    }

    @GetMapping("/login/simple-auth")
    public String simpleAuthLogin(@RequestParam(required = false) String type, HttpSession session, Model model) {
        rememberFlow(session, type);
        return mockScreen(model, "ANYID", session);
    }

    /**
     * 5개 외부인증수단 공용 모의 통과 - 안내화면을 띄운 GET과 같은 경로로 POST를 받는다
     * (안내화면 form의 action=""). 루트 절대경로 form action은 SPA의 /member 접두사 아래에서
     * 프록시 대상 밖으로 나가버려서 못 쓴다. 실제 연계가 열려(enabled=true) 진짜 프로토콜을
     * 타야 하는 provider는 여기서 막는다(모의 인증이 실연계를 우회하지 못하게 하는 방어).
     */
    @PostMapping("/onepass-login")
    public String onePassMock(HttpSession session, HttpServletResponse response, Model model) {
        return mockPass("ONEPASS", session, response, model);
    }

    @PostMapping("/login/kakao")
    public String kakaoMock(HttpSession session, HttpServletResponse response, Model model) {
        return mockPass("KAKAO", session, response, model);
    }

    @PostMapping("/login/naver")
    public String naverMock(HttpSession session, HttpServletResponse response, Model model) {
        return mockPass("NAVER", session, response, model);
    }

    @PostMapping("/login/finance-cert")
    public String financeCertMock(HttpSession session, HttpServletResponse response, Model model) {
        return mockPass("FINANCE_CERT", session, response, model);
    }

    @PostMapping("/login/simple-auth")
    public String simpleAuthMock(HttpSession session, HttpServletResponse response, Model model) {
        return mockPass("ANYID", session, response, model);
    }

    private String mockPass(String provider, HttpSession session, HttpServletResponse response, Model model) {
        String label = MOCK_PROVIDER_LABELS.get(provider);
        if (label == null || isRealClientEnabled(provider)) {
            model.addAttribute("errorMessage", "모의 인증을 사용할 수 없는 연계 수단입니다.");
            return "login";
        }
        // 회원가입 경유는 매번 새 신원이어야 한다 - 고정 externalId를 쓰면 두 번째부터는
        // 기가입자로 붙어서 "회원가입 완료"가 아니라 그냥 로그인이 되어버린다(AS-IS의
        // 휴대폰 인증 모의통과도 같은 이유로 fresh CI를 발급했다).
        String externalId = isJoinFlow(session) ? "MOCK-" + UUID.randomUUID() : "MOCK";
        // 모의 인증은 CI를 만들지 않는다 - 실제 본인확인을 거치지 않았으므로 CI 기반 신원으로
        // 취급하면 안 되고(필수정보/14세 검사 대상도 아니다) SNS 프로필 신원과 같게 다룬다.
        ExternalIdentity identity = ExternalIdentity.ofProfile(provider, externalId, label + " 모의회원", null, null);
        return completeLogin(identity, session, response);
    }

    private boolean isRealClientEnabled(String provider) {
        return switch (provider) {
            case "ONEPASS" -> onePassClient.isEnabled();
            case "KAKAO" -> kakaoCertClient.isEnabled();
            case "NAVER" -> naverLoginClient.isEnabled();
            default -> false; // FINANCE_CERT/ANYID는 진짜 클라이언트 자체가 없다
        };
    }

    private String mockScreen(Model model, String providerCode, HttpSession session) {
        boolean join = isJoinFlow(session);
        model.addAttribute("providerCode", providerCode);
        model.addAttribute("providerName", MOCK_PROVIDER_LABELS.get(providerCode));
        model.addAttribute("backUrl", join ? "/signup" : "/login");
        model.addAttribute("backLabel", join ? "회원가입으로 돌아가기" : "로그인으로 돌아가기");
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

    private String oauthLogin(OAuth2LoginClient client, String providerCode, String type,
                               HttpSession session, Model model) {
        rememberFlow(session, type);
        if (!client.isEnabled()) {
            return mockScreen(model, providerCode, session);
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
        } catch (ExternalAuthException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "SNS 로그인에 실패했습니다: " + e.getMessage());
            return "login";
        }
    }

    private void rememberFlow(HttpSession session, String type) {
        session.setAttribute(SESSION_EXTERNAL_AUTH_FLOW, FLOW_JOIN.equalsIgnoreCase(type) ? FLOW_JOIN : "LOGIN");
    }

    private boolean isJoinFlow(HttpSession session) {
        return FLOW_JOIN.equals(session.getAttribute(SESSION_EXTERNAL_AUTH_FLOW));
    }

    /** AuthController/AuthApiController와 동일하게 HttpSession(Thymeleaf 화면용)과
     *  GH_AUTH JWT 쿠키(storefront SPA 등 JWT 기반 서비스용)를 항상 같이 발급한다 -
     *  이 메서드가 세션만 채우고 쿠키 발급을 빠뜨리면 SNS/디지털원패스로 로그인해도
     *  storefront에서는 로그인 상태가 반영되지 않는다.
     *
     *  <p>돌아가는 곳은 AS-IS와 같은 규칙이다: 회원가입 화면에서 시작해서 실제로 신규가입이
     *  된 경우(AS-IS code=JOIN_MEMBER)만 회원가입 화면으로 되돌려 "인증 로그인 통한 회원가입
     *  완료" 안내를 띄우고, 그 밖에는 메인으로 보낸다. 리다이렉트를 루트 상대경로(/signup)로
     *  두면 member를 직접 연 경우엔 Thymeleaf signup.html, SPA를 통해 온 경우엔 같은 경로의
     *  SPA 라우트로 각각 알맞게 떨어진다. */
    private String completeLogin(ExternalIdentity identity, HttpSession session, HttpServletResponse response) {
        ExternalLoginService.LinkResult result = externalLoginService.linkOrJoin(
                identity, AUTH_NAMES.getOrDefault(identity.provider(), identity.provider()));
        User user = result.user();
        session.setAttribute(AuthController.SESSION_USER_KEY, user);
        authCookieSupport.issue(response, user);

        boolean joinFlow = isJoinFlow(session);
        session.removeAttribute(SESSION_EXTERNAL_AUTH_FLOW);
        if (joinFlow && result.newlyJoined()) {
            return "redirect:/signup?joined=" + identity.provider();
        }
        return "redirect:/";
    }
}

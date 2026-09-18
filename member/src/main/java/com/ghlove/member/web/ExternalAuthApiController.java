package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.ExternalLoginService;
import com.ghlove.member.service.integration.ExternalAuthException;
import com.ghlove.member.service.integration.ExternalIdentity;
import com.ghlove.member.service.integration.KakaoCertClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 카카오 인증서비스(카카오톡 지갑 본인인증)를 화면에서 진행하기 위한 JSON API -
 * AS-IS {@code /api/kakao-link/kakao-link-login}에 대응한다.
 *
 * <p>왜 화면이 인증을 시작하는가: 카카오 인증서비스의 인가 요청은 JS SDK의
 * {@code Kakao.Auth.authorizeForCert()}가 담당한다(정산ID/서명데이터/요청항목을 SDK가 조립).
 * AS-IS도 같은 구조여서 서버는 ①SDK 호출에 필요한 값을 내려주고({@link #kakaoConfig})
 * ②돌아온 인가코드를 툴킷에 검증시켜 로그인/가입을 끝낸다({@link #kakaoVerify}).
 *
 * <p>storefront SPA와 member Thymeleaf 화면이 이 API를 같이 쓴다. 응답 모양은 AS-IS와 같게
 * {@code code}에 JOIN_MEMBER/LOGIN을, 실패는 {@code errMsg}에 담는다 - 화면은 JOIN_MEMBER일
 * 때만 가입완료 안내를 띄운다.
 */
@RestController
@RequestMapping("/api/external-auth")
@RequiredArgsConstructor
@Slf4j
public class ExternalAuthApiController {

    /** SDK가 만든 인가코드를 검증할 때 같은 값을 다시 보내야 해서 세션에 들고 있는다. */
    private static final String SESSION_KAKAO_SIGN_DATA = "kakaoCertSignData";

    private final KakaoCertClient kakaoCertClient;
    private final ExternalLoginService externalLoginService;
    private final AuthCookieSupport authCookieSupport;

    /**
     * 화면이 카카오 JS SDK를 호출하는 데 필요한 값. 실연계가 꺼져 있으면 {@code enabled:false}만
     * 내려주고, 화면은 기존 모의 통과 안내화면(/login/kakao)으로 넘긴다.
     *
     * <p>signData는 요청마다 새로 만들어 세션에 저장한다 - AS-IS는 이 값을 브라우저
     * sessionStorage에 뒀는데, 서버가 들고 있으면 화면이 위조한 값을 되돌려줄 수 없다.
     */
    @GetMapping("/kakao/config")
    public Map<String, Object> kakaoConfig(@RequestParam(required = false) String type, HttpSession session) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (!kakaoCertClient.isEnabled()) {
            body.put("enabled", false);
            // 화면이 이 주소로 넘기면 지금까지처럼 모의 인증으로 흐름을 이어갈 수 있다.
            body.put("mockUrl", "/login/kakao" + (isJoin(type) ? "?type=JOIN" : ""));
            return body;
        }
        String signData = kakaoCertClient.newSignData();
        session.setAttribute(SESSION_KAKAO_SIGN_DATA, signData);
        session.setAttribute(ExternalLoginController.SESSION_EXTERNAL_AUTH_FLOW,
                isJoin(type) ? ExternalLoginController.FLOW_JOIN : "LOGIN");

        KakaoCertClient.CertConfig config = kakaoCertClient.configFor(isJoin(type), signData);
        body.put("enabled", true);
        body.put("jsKey", config.jsKey());
        body.put("settleId", config.settleId());
        body.put("redirectUri", config.redirectUri());
        body.put("identifyItems", config.identifyItems());
        body.put("signData", config.signData());
        return body;
    }

    /** 인증 후 화면으로 돌아온 인가코드 검증 + 로그인/간편가입 확정 (AS-IS kakao-link-login). */
    @PostMapping("/kakao/verify")
    public Map<String, Object> kakaoVerify(@RequestBody KakaoVerifyRequest request, HttpSession session,
                                            HttpServletResponse response) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (request == null || request.code() == null || request.code().isBlank()) {
            body.put("status", "ERROR");
            body.put("errMsg", "카카오톡 인증 로그인 연결에 실패했습니다.");
            return body;
        }
        boolean joinFlow = isJoin(request.type());
        String signData = (String) session.getAttribute(SESSION_KAKAO_SIGN_DATA);
        session.removeAttribute(SESSION_KAKAO_SIGN_DATA);

        try {
            ExternalIdentity identity = kakaoCertClient.verify(request.code(), signData, joinFlow);
            ExternalLoginService.LinkResult result = externalLoginService.linkOrJoin(
                    identity, ExternalLoginController.AUTH_NAMES.get("KAKAO"));
            User user = result.user();
            session.setAttribute(AuthController.SESSION_USER_KEY, user);
            authCookieSupport.issue(response, user);
            session.removeAttribute(ExternalLoginController.SESSION_EXTERNAL_AUTH_FLOW);

            body.put("status", "OK");
            // AS-IS와 같은 구분값 - 회원가입 화면에서 시작해 신규가입이 된 경우만 JOIN_MEMBER다.
            body.put("code", joinFlow && result.newlyJoined() ? "JOIN_MEMBER" : "LOGIN");
            body.put("loginId", user.getLoginId());
            body.put("name", user.getUserName());
            body.put("authName", ExternalLoginController.AUTH_NAMES.get("KAKAO"));
            return body;
        } catch (ExternalAuthException e) {
            body.put("status", "ERROR");
            body.put("errMsg", e.getMessage());
            return body;
        } catch (RuntimeException e) {
            log.error("카카오 인증서비스 검증 중 예상치 못한 오류", e);
            body.put("status", "ERROR");
            body.put("errMsg", "카카오톡 인증 로그인에 실패했습니다.");
            return body;
        }
    }

    private boolean isJoin(String type) {
        return ExternalLoginController.FLOW_JOIN.equalsIgnoreCase(type);
    }

    public record KakaoVerifyRequest(String code, String type) {}
}

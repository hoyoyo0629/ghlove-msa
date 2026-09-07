package com.ghlove.member.service;

import com.ghlove.member.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 로그인 시 발급하는 GH_AUTH 쿠키(JWT)/GH_REFRESH 쿠키(재인증용 장기 토큰) 발급/해제 로직 -
 * Thymeleaf 화면용 AuthController와 SPA용 AuthApiController가 동일한 로그인/로그아웃
 * 규칙을 공유해야 해서 분리했다. localhost의 다른 포트(8082~8086)에도 이 쿠키가 자동으로
 * 같이 전송된다(쿠키 스코프는 포트를 구분하지 않음) - Domain 속성을 일부러 지정하지 않는다.
 * secure=false는 이 로컬 개발환경이 평문 http라서 그런 것 - 운영에서는 반드시 true로
 * 바꿔야 한다.
 */
@Component
@RequiredArgsConstructor
public class AuthCookieSupport {

    public static final String REFRESH_COOKIE_NAME = "GH_REFRESH";

    private final JwtSupport jwtSupport;
    private final RefreshTokenService refreshTokenService;

    public void issue(HttpServletResponse response, User user) {
        String token = jwtSupport.issue(user.getUserId(), user.getLoginId());
        ResponseCookie cookie = ResponseCookie.from(JwtSupport.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtSupport.expirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String refreshToken = refreshTokenService.issue(user.getUserId());
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshTokenService.expirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    /** 로그아웃/비밀번호변경/탈퇴 - 두 쿠키를 모두 지우고, 갖고 있던 refresh 토큰도 서버측에서
     *  폐기한다(쿠키만 지우면 탈취된 토큰이 여전히 유효하게 남는다). */
    public void clear(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.revoke(readRefreshToken(request));

        ResponseCookie cookie = ResponseCookie.from(JwtSupport.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public String readRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE_NAME.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}

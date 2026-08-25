package com.ghlove.member.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 로그인 시 이 서비스(JwtSupport)가 스스로 발급한 GH_AUTH 쿠키를 다시 검증한다 (SFR-010).
 * member는 발급자이기도 하지만, HttpSession(JSESSIONID)이 먼저 타임아웃돼도 이 쿠키는
 * 아직 유효할 수 있어 - SessionRehydrateInterceptor가 그 경우 세션을 다시 채우는 데 쓴다.
 */
@Component
@Slf4j
public class JwtVerifier {

    private static final String COOKIE_NAME = "GH_AUTH";

    private final SecretKey key;

    public JwtVerifier(@Value("${ghlove.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** @return 로그인된 회원 ID, 쿠키가 없거나 서명/만료가 유효하지 않으면 empty. */
    public Optional<Long> currentUserId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<Long> verify(String token) {
        try {
            String subject = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(Long.valueOf(subject));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid GH_AUTH token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

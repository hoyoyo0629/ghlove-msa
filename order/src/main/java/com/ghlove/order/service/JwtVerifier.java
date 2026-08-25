package com.ghlove.order.service;

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
 * member 서비스가 로그인 시 발급한 공유 JWT 쿠키(GH_AUTH)를 검증한다 (SFR-010). 이전에는
 * 이 서비스의 모든 화면이 클라이언트가 보낸 ?userId= 쿼리파라미터를 그냥 신뢰했다 - 다른
 * 회원 ID로 바꿔치기만 하면 누구나 남의 장바구니/주문을 보고 조작할 수 있는 IDOR 취약점
 * 이었다. 이제는 이 쿠키의 서명이 유효할 때만(=member 서비스에서 실제로 로그인한 사람)
 * 그 안의 userId를 신뢰한다 - 쿼리파라미터는 더 이상 신원 증명 수단으로 쓰지 않는다.
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

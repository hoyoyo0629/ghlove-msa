package com.ghlove.member.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * 로그인 시 발급하는 공유 JWT (SFR-010) - member 서비스의 HttpSession은 이 서비스
 * 자신에게만 유효해서(JSESSIONID 쿠키가 8081 origin 전용), 나머지 5개 서비스가 "이 요청이
 * 실제로 그 회원 본인이 보낸 것"임을 신뢰할 방법이 없었다(그동안은 그냥 ?userId= 쿼리파라미터를
 * 그대로 믿었음 - IDOR 취약점). 로그인 성공 시 이 토큰을 HttpOnly 쿠키로 내려주면, 브라우저가
 * localhost의 모든 포트(8081~8086)에 자동으로 같이 보내준다(쿠키 스코프는 host 기준이지 포트
 * 기준이 아님) - 각 서비스는 이 토큰의 서명만 검증하면 그 안의 userId를 신뢰할 수 있다.
 */
@Component
public class JwtSupport {

    public static final String COOKIE_NAME = "GH_AUTH";

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtSupport(@Value("${ghlove.jwt.secret}") String secret,
                       @Value("${ghlove.jwt.expiration-minutes:120}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public long expirationSeconds() {
        return expirationMinutes * 60;
    }

    public String issue(Long userId, String loginId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer("ghlove-app")
                .claim("loginId", loginId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}

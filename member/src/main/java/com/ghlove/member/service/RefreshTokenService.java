package com.ghlove.member.service;

import com.ghlove.member.domain.RefreshToken;
import com.ghlove.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

/**
 * SFR-002 "인증토큰/세션 관리(만료, 재인증 정책)" - GH_AUTH 액세스 JWT(기본 120분 만료)가
 * 끊겨도 재로그인 없이 새 액세스 토큰을 받을 수 있게, 별도의 장기(기본 14일) 불투명
 * 토큰을 발급한다. 매 갱신마다 토큰을 폐기하고 새로 발급(회전) - 탈취된 토큰이 재사용되면
 * 다음 정상 갱신 시 이미 없는 토큰이라 실패하므로 재사용을 알아챌 수 있다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${ghlove.jwt.refresh-expiration-days:14}")
    private long expirationDays;

    public long expirationSeconds() {
        return expirationDays * 24 * 60 * 60;
    }

    @Transactional
    public String issue(Long userId) {
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setToken(generateToken());
        entity.setExpiresAt(DATE_FORMAT.format(LocalDateTime.now().plusDays(expirationDays)));
        entity.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return refreshTokenRepository.save(entity).getToken();
    }

    /** 토큰이 유효하면(존재+미만료) 그 자리에서 폐기하고 userId를 반환한다 - 호출자가 새
     *  토큰을 {@link #issue}로 다시 발급해 회전을 완성한다. */
    @Transactional
    public Optional<Long> consume(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<RefreshToken> found = refreshTokenRepository.findByToken(token);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        RefreshToken entity = found.get();
        refreshTokenRepository.delete(entity);
        if (entity.getExpiresAt().compareTo(DATE_FORMAT.format(LocalDateTime.now())) < 0) {
            return Optional.empty();
        }
        return Optional.of(entity.getUserId());
    }

    @Transactional
    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.deleteByToken(token);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

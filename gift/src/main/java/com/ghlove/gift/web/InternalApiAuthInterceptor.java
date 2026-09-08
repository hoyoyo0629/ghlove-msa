package com.ghlove.gift.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * SFR-002/007: 이 서비스의 관리자/내부 전용 경로를 게이트한다. 예전에는 관리자 콘솔이
 * 소비하는 /api/admin/** 등이 무인증이라 서비스 포트에 도달만 하면 누구나 원장/통계/설정을
 * 조회·변경할 수 있는 실보안결함이었다. 호출측(admin 등)이 공유 시크릿을 {@code X-Internal-Secret}
 * 헤더로 실으면 통과하고, 없거나 틀리면 401로 거부한다(order/member의 동일 인터셉터와 같은 방식).
 */
@Component
@Slf4j
public class InternalApiAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_SECRET = "X-Internal-Secret";

    private final String adminSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InternalApiAuthInterceptor(@Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.adminSecret = adminSecret;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String provided = request.getHeader(HEADER_SECRET);
        if (provided == null || !provided.equals(adminSecret)) {
            log.warn("Rejected unauthenticated internal request: {} {} from {}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("message", "내부 서비스 인증이 필요합니다.")));
            return false;
        }
        return true;
    }
}

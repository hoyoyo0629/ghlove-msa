package com.ghlove.member.web;

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
 * SFR-002: member의 관리자/내부 전용 경로를 게이트한다. 예전에는 {@code /api/admin/**}
 * (회원 검색·상세·강제탈퇴·계정잠금해제·권한회수·휴면/파기 배치·감사로그 조회)와
 * {@code /api/users/*}(회원정보 조회·현장가입)가 완전히 무인증이라, 서비스 포트(8081)에
 * 도달만 하면 누구나 회원 PII를 조회하고 임의 회원을 강제탈퇴/파기할 수 있는 실보안결함이었다.
 *
 * <p>이 API들은 admin/donation/point 서비스가 서버 대 서버로만 호출한다(브라우저 직접 호출
 * 없음). 호출측이 member와만 아는 공유 시크릿을 {@code X-Internal-Secret} 헤더로 실으면
 * 통과하고, 없거나 틀리면 401로 거부한다. order 서비스의 AdminApiAuthInterceptor와 동일한
 * 방식이며 같은 시크릿을 재사용한다. 이 시크릿은 내부망에서만 알아야 하고 외부에 노출되면 안 된다.
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
            log.warn("Rejected unauthenticated internal member request: {} {} from {}",
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

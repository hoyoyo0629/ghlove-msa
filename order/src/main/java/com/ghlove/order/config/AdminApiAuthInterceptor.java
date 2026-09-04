package com.ghlove.order.config;

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
 * admin 콘솔의 주문/클레임 관리 전용 경로를 게이트한다. 예전에는 {@code /claims},
 * {@code /claims/{id}/approve|reject|complete}, {@code /orders/{orderId}/invoice},
 * {@code /orders/{orderId}/delivery-status}가 "아직 이 MSA에 운영자 로그인 모델이 없어서"
 * 라는 이유로 완전히 무인증 상태였다 - 인터넷에서 누구나 클레임을 승인/거절하거나 송장을
 * 조작할 수 있는 실보안결함이었다.
 *
 * admin 서비스는 세션 기반 로그인(OP_MANAGER)을 갖고 있지만 order 서비스는 그 세션을 알 수
 * 없으므로, admin이 이 서비스를 호출할 때마다 (1) admin과 order만 아는 공유시크릿과
 * (2) 관리자 신원(userId/authority/locgovCode)을 커스텀 헤더로 함께 싣게 하고, 이 인터셉터가
 * 시크릿을 검증한다. 시크릿이 없거나 일치하지 않으면 이 요청은 즉시 401로 거부된다 -
 * 이게 없으면 "누구나 X-Manager-Authority: ROLE_ADMIN_1 헤더만 붙이면 통과"가 되어 버려서
 * 무인증 문제를 헤더 위조로 재발시키는 것과 같다. 이 시크릿은 반드시 내부망(서비스간 호출)
 * 에서만 알아야 하고, 브라우저/외부에 노출되면 안 된다.
 */
@Component
@Slf4j
public class AdminApiAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_SECRET = "X-Internal-Secret";
    public static final String HEADER_MANAGER_ID = "X-Manager-Id";
    public static final String HEADER_MANAGER_NAME = "X-Manager-Name";
    public static final String HEADER_MANAGER_AUTHORITY = "X-Manager-Authority";
    public static final String HEADER_MANAGER_LOCGOV = "X-Manager-Locgov-Code";

    private final String adminSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminApiAuthInterceptor(@Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.adminSecret = adminSecret;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String provided = request.getHeader(HEADER_SECRET);
        if (provided == null || !provided.equals(adminSecret)) {
            log.warn("Rejected unauthenticated admin-only order request: {} {} from {}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("message", "관리자 인증이 필요합니다.")));
            return false;
        }
        return true;
    }
}

package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuditLogService;
import com.ghlove.member.service.JwtVerifier;
import com.ghlove.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * SFR-010: HttpSession(JSESSIONID, 이 서비스 origin 전용, Tomcat 기본 타임아웃 30분)이
 * GH_AUTH JWT 쿠키(로그인 시 발급, 만료 120분)보다 먼저 만료되면, 나머지 5개 서비스는
 * 여전히 로그인 상태로 취급하는데 member 자신의 마이페이지 등은 먼저 "로그아웃"된 것처럼
 * 보이는 불일치가 생긴다(장바구니 → 마이페이지 이동 시 로그아웃되는 것처럼 보이는 버그의
 * 원인). 세션에 로그인 정보가 없을 때 이 쿠키가 아직 유효하면 세션을 다시 채워 넣는다.
 *
 * <p>같은 자리에서 회원 액션 로그(AS-IS opmanager/log/user-action-log)도 기록한다 - 이
 * 인터셉터가 이미 모든 요청에서 로그인 상태를 확정하는 지점이라 별도 인터셉터를 새로
 * 두지 않고 재사용했다. 조회(GET)는 제외.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SessionRehydrateInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final JwtVerifier jwtVerifier;
    private final MemberService memberService;
    private final AuditLogService auditLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(AuthController.SESSION_USER_KEY) == null) {
            jwtVerifier.currentUserId(request)
                    .flatMap(memberService::findById)
                    .ifPresent(user -> session.setAttribute(AuthController.SESSION_USER_KEY, user));
        }

        if (MUTATING_METHODS.contains(request.getMethod())) {
            User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
            if (user != null) {
                // 감사 로그 적재 실패는 본 요청(배송지 삭제 등)을 절대 막으면 안 된다 - 로그는
                // best-effort이고 가용성의 단일 실패점이 되어선 안 되므로 삼켜서 격리한다.
                try {
                    auditLogService.recordAction(user.getLoginId(), request.getRemoteAddr(), request.getRequestURI(), request.getMethod());
                } catch (RuntimeException e) {
                    log.warn("회원 액션 감사로그 적재 실패 (요청은 계속 진행): {} {}", request.getMethod(), request.getRequestURI(), e);
                }
            }
        }
        return true;
    }
}

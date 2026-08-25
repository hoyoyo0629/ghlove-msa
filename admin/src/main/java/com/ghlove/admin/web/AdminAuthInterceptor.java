package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.service.AuditLogService;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * 운영관리 콘솔(통계/정산/공통코드/배너/팝업/공지관리/배송조회/NH배치/관리자권한요청 등)을
 * 게이트한다 - (1) 로그인 여부, (2) 로그인했다면 메뉴 단위 권한(AS-IS OP_MENU_RIGHT)까지
 * 확인한다. AS-IS OpmanagerHandlerInterceptor와 동일한 2단계 검사.
 */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final MenuService menuService;
    private final AuditLogService auditLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(true);
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (manager == null) {
            redirectToLogin(request, response);
            return false;
        }

        Optional<Menu> menu = menuService.resolve(request.getRequestURI());
        if (menu.isPresent() && !menuService.hasAccess(manager, menu.get())) {
            response.sendRedirect("/admin/access-denied");
            return false;
        }

        if (MUTATING_METHODS.contains(request.getMethod())) {
            auditLogService.recordAction(manager.getLoginId(), request.getRemoteAddr(),
                    request.getRequestURI(), request.getMethod());
        }
        return true;
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        String returnTo = request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        response.sendRedirect("/admin/login?returnTo=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8));
    }
}

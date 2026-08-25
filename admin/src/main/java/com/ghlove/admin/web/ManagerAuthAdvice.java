package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** fragments/admin-nav.html의 상단 1차메뉴(GNB) + 좌측 사이드바(2차) 렌더링에 필요한
 *  공통 모델 속성을 매 요청마다 주입한다(HeaderAuthAdvice와 동일한 패턴) - 그래야 각
 *  컨트롤러가 일일이 메뉴 트리를 채워주지 않아도 fragment가 항상 최신 상태로 그려진다. */
@ControllerAdvice
@RequiredArgsConstructor
public class ManagerAuthAdvice {

    private final MenuService menuService;

    @ModelAttribute("loginManager")
    public Manager loginManager(HttpServletRequest request) {
        return currentManager(request);
    }

    @ModelAttribute("navTree")
    public MenuService.NavTree navTree(HttpServletRequest request) {
        Manager manager = currentManager(request);
        return manager != null ? menuService.navTreeFor(manager) : new MenuService.NavTree(java.util.List.of(), java.util.Map.of());
    }

    /** 요청 URI와 가장 길게 일치하는 등록된 메뉴 - 상단탭/사이드바에서 "현재 위치"를
     *  하이라이트하는 데 쓴다(개별 화면이 currentPath 문자열을 일일이 넘기지 않아도 됨). */
    @ModelAttribute("activeMenu")
    public Menu activeMenu(HttpServletRequest request) {
        return menuService.resolve(request.getRequestURI()).orElse(null);
    }

    @ModelAttribute("activeTopMenuId")
    public Integer activeTopMenuId(HttpServletRequest request) {
        Menu active = menuService.resolve(request.getRequestURI()).orElse(null);
        if (active == null) {
            return null;
        }
        return active.getMenuParentId() != null ? active.getMenuParentId() : active.getMenuId();
    }

    private static Manager currentManager(HttpServletRequest request) {
        var session = request.getSession(false);
        return session != null ? (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY) : null;
    }
}

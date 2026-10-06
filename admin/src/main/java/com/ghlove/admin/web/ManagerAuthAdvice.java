package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.service.CommonMessageService;
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

    /** AS-IS SellerUserServiceImpl의 기본값과 같다 - ISMS 설정이 없거나 숫자가 아니면 60분. */
    private static final int DEFAULT_MANAGER_TIMEOUT_MINUTES = 60;
    private static final String ISMS_KEY_SESSION_TIMEOUT_MANAGER = "SESSION_TIMEOUT_MANAGER";

    private final MenuService menuService;
    private final CommonMessageService commonMessageService;
    private final com.ghlove.admin.repository.ConfigIsmsRepository configIsmsRepository;

    /**
     * AS-IS {@code layouts/opmanager/inc_head.jsp}의 {@code OP_MANAGER_TIMEOUT}
     * ({@code shopContext.managerTimeout}) - 운영자 화면 자동 로그아웃까지의 <b>분</b>이다.
     * {@code op.manager.js}의 {@code Manager.procSessionTimeout}이 {@code 값 * 60 * 1000}으로
     * 쓰므로 초를 넣으면 안 된다(2026-10-06 교정: '3600'을 초로 알고 넣어 60시간이 돼 있었다).
     * 값은 AS-IS와 같이 ISMS 설정 {@code SESSION_TIMEOUT_MANAGER}에서 읽고 없으면 60이다.
     */
    @ModelAttribute("managerTimeout")
    public int managerTimeout() {
        return configIsmsRepository.findById(ISMS_KEY_SESSION_TIMEOUT_MANAGER)
                .map(com.ghlove.admin.domain.ConfigIsms::getValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException e) {
                        return DEFAULT_MANAGER_TIMEOUT_MINUTES;
                    }
                })
                .orElse(DEFAULT_MANAGER_TIMEOUT_MINUTES);
    }

    /** AS-IS JSP의 {@code ${op:message('M00730')}}에 대응 - 화면에서 {@code ${msg.get('M00730')}}으로
     *  쓴다. 모든 화면이 쓰므로 여기서 한 번만 주입한다(라벨을 템플릿에 한글로 박으면 AS-IS가
     *  문구를 바꿨을 때 갈라지므로 금지). */
    @ModelAttribute("msg")
    public CommonMessageService msg() {
        return commonMessageService;
    }

    @ModelAttribute("loginManager")
    public Manager loginManager(HttpServletRequest request) {
        return currentManager(request);
    }

    /**
     * AS-IS {@code layouts/common/inc_common.jsp}가 내려주는 {@code RequestContext.currentUrl}.
     * {@code op.link.js}의 {@code Link.view}가 "검색조건을 들고 상세로 갔다가 돌아오기"를 위해
     * 상세 URL에 {@code ?url=<현재주소>}로 붙인다. Thymeleaf 3.1에서 {@code #request}가
     * 제거되어 템플릿에서 직접 읽을 수 없어 여기서 넣는다.
     */
    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString == null || queryString.isBlank() ? uri : uri + "?" + queryString;
    }

    @ModelAttribute("navTree")
    public MenuService.NavTree navTree(HttpServletRequest request) {
        Manager manager = currentManager(request);
        return manager != null ? menuService.navTreeFor(manager)
                : new MenuService.NavTree(java.util.List.of(), java.util.Map.of(), java.util.Map.of(), java.util.Map.of());
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
        // AS-IS 3단: 활성 링크(리프)의 상단 탭은 조부모다. 부모 체인을 상단까지 거슬러 올라간다.
        return menuService.topMenuIdOf(active);
    }

    private static Manager currentManager(HttpServletRequest request) {
        var session = request.getSession(false);
        return session != null ? (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY) : null;
    }
}

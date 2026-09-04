package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.repository.MenuRepository;
import com.ghlove.admin.repository.MenuRightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 요청 URI를 OP_MENU의 menuUrl과 매칭해 권한을 확인한다 (AS-IS OpmanagerHandlerInterceptor의
 * getMenuCode 재현). AS-IS 실제 권한은 6단계다 - {@link #UNRESTRICTED_ROLES}(시스템/행안부
 * 정·부담당자, ROLE_ADMIN_1~4)는 AS-IS의 ROLE_SUPERVISOR와 동일하게 이 표를 거치지 않고
 * 항상 통과하고, {@link #LOCGOV_SCOPED_ROLES}(지자체 정·부담당자, ROLE_ADMIN_5~6)는 실제
 * OP_MENU_RIGHT 매핑을 확인한다 - 예전엔 이걸 ROLE_ADMIN/ROLE_OPERATOR 2단계로 단순화해뒀다가
 * 사용자가 실제 AS-IS와 다르다고 지적해 이번에 정식으로 6단계로 맞췄다.
 *
 * AS-IS는 "매칭되는 메뉴가 없으면" 예외를 던지지만, 이 프로젝트는 화면을 계속 늘려가는
 * 중이라 메뉴 등록을 깜빡한 새 라우트가 관리자 자신을 막아버리는 사고를 피하기 위해
 * "매칭되는 메뉴가 없으면 통과, 매칭됐는데 권한이 없으면 차단"으로 완화했다 - 의도적인
 * AS-IS 대비 축소(fail-open on unmapped)다.
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    /** 시스템주/부담당자, 행안부주/부담당자 - AS-IS 컨트롤러 전반에서 반복되는
     *  "1,2,3,4면 전체보기" 분기와 동일(예: GiveStateManagerController, OrderManagerController). */
    public static final Set<String> UNRESTRICTED_ROLES = Set.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");
    /** 지자체주/부담당자 - AS-IS의 "5,6이면 소속 지자체로 스코프" 분기. */
    public static final Set<String> LOCGOV_SCOPED_ROLES = Set.of("ROLE_ADMIN_5", "ROLE_ADMIN_6");

    private static final String DISPLAY_Y = "Y";

    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;

    public List<Menu> visibleMenus() {
        return menuRepository.findByDisplayFlagOrderByMenuSeq(DISPLAY_Y);
    }

    /** 등록된 메뉴 중 요청 URI와 가장 길게 일치하는 것을 찾는다(하위 경로도 같은 메뉴로 취급). */
    public Optional<Menu> resolve(String requestUri) {
        return menuRepository.findByDisplayFlagOrderByMenuSeq(DISPLAY_Y).stream()
                .filter(m -> m.getMenuUrl() != null
                        && (requestUri.equals(m.getMenuUrl()) || requestUri.startsWith(m.getMenuUrl() + "/")))
                .max(Comparator.comparingInt(m -> m.getMenuUrl().length()));
    }

    public boolean hasAccess(Manager manager, Menu menu) {
        if (UNRESTRICTED_ROLES.contains(manager.getAuthority())) {
            return true;
        }
        return menuRightRepository.existsByMenuIdAndAuthority(menu.getMenuId(), manager.getAuthority());
    }

    /** 지자체 정·부담당자(ROLE_ADMIN_5/6)인지 - 조회 범위를 자기 지자체로 강제해야 하는 화면에서 쓴다. */
    public static boolean isLocgovScoped(Manager manager) {
        return LOCGOV_SCOPED_ROLES.contains(manager.getAuthority());
    }

    /** 지자체 담당자는 화면에서 어떤 locgovCode를 골랐든 무시하고 자기 소속으로 강제 적용한다
     *  (AS-IS의 "5,6이면 소속 지자체로 스코프" 분기 재현) - 시스템/행안부는 사용자가 고른
     *  값을 그대로 쓴다(null이면 전체보기). */
    public static String effectiveLocgovCode(Manager manager, String requestedLocgovCode) {
        return isLocgovScoped(manager) ? manager.getLocgovCode() : requestedLocgovCode;
    }

    /**
     * 상단 1차 메뉴(GNB) + 좌측 사이드바(2차) 렌더링용 - AS-IS inc_header.jsp의 실제
     * 레이아웃(상단 탭 + 좌측 lnb)을 재현한다. 카테고리 부모 행(MENU_PARENT_ID=NULL,
     * MENU_URL=NULL)은 그 자체에 OP_MENU_RIGHT를 절대 주지 않는다 - "자식 중 하나라도
     * 접근 가능하면 그 부모도 보여준다"로 판단해야 새 하위메뉴를 추가할 때마다 부모
     * 권한까지 매번 챙길 필요가 없다.
     */
    public NavTree navTreeFor(Manager manager) {
        List<Menu> all = visibleMenus();

        List<Menu> accessibleChildren = all.stream()
                .filter(m -> m.getMenuParentId() != null)
                .filter(m -> hasAccess(manager, m))
                .toList();

        Set<Integer> visibleParentIds = accessibleChildren.stream()
                .map(Menu::getMenuParentId)
                .collect(Collectors.toSet());

        List<Menu> topMenus = all.stream()
                .filter(m -> m.getMenuParentId() == null && visibleParentIds.contains(m.getMenuId()))
                .sorted(Comparator.comparing(Menu::getMenuSeq))
                .toList();

        Map<Integer, List<Menu>> childrenByParent = new LinkedHashMap<>();
        for (Menu top : topMenus) {
            childrenByParent.put(top.getMenuId(), accessibleChildren.stream()
                    .filter(c -> c.getMenuParentId().equals(top.getMenuId()))
                    .sorted(Comparator.comparing(Menu::getMenuSeq))
                    .toList());
        }

        return new NavTree(topMenus, childrenByParent);
    }

    public record NavTree(List<Menu> topMenus, Map<Integer, List<Menu>> childrenByParent) {
    }
}

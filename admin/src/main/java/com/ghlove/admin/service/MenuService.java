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
 * getMenuCode 재현). AS-IS 권한은 8단계(ROLE_ADMIN_1~8, ghlove-common `UserAdminRole`의
 * SYS/MOIS/LOC/OFF 4그룹 × 정·부담당자)이고 이 프로젝트는 그중 1~6을 구현했다(7·8
 * 오프라인담당자 미구현).
 *
 * nav 노출은 AS-IS와 동일하게 {@code STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재}로만
 * 판정한다({@link #navVisible}) - AS-IS에서 이 권한조인을 건너뛰는 건 ROLE_SUPERVISOR 하나뿐이고
 * 그 롤은 이 프로젝트에 없다. 주의: AS-IS menu-mapper.xml은 DISPLAY_FLAG를 SELECT 목록에만 두고
 * WHERE에 전혀 쓰지 않는다 - 노출 필터는 STATUS_CODE다. 화면 접근 허용({@link #hasAccess})만
 * {@link #UNRESTRICTED_ROLES}(시스템/행안부 정·부담당자, ROLE_ADMIN_1~4)를 통과시키는 완화를
 * 유지한다. 예전엔 이걸 ROLE_ADMIN/ROLE_OPERATOR 2단계로 단순화해뒀다가 사용자가 실제 AS-IS와
 * 다르다고 지적해 단계별 역할로 맞췄다.
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

    /** AS-IS menu-mapper.xml이 모든 메뉴 조회에 거는 조건 - '1'=사용, '2'=미사용. */
    private static final String STATUS_IN_USE = "1";

    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;

    public List<Menu> visibleMenus() {
        return menuRepository.findByStatusCodeOrderByMenuSeq(STATUS_IN_USE);
    }

    /** 등록된 메뉴 중 요청 URI와 가장 길게 일치하는 것을 찾는다(하위 경로도 같은 메뉴로 취급). */
    public Optional<Menu> resolve(String requestUri) {
        return visibleMenus().stream()
                .filter(m -> m.getMenuUrl() != null
                        && (requestUri.equals(m.getMenuUrl()) || requestUri.startsWith(m.getMenuUrl() + "/")))
                .max(Comparator.comparingInt(m -> m.getMenuUrl().length()));
    }

    /**
     * 화면 접근 허용 여부 - nav 노출과 달리 여기서는 시스템/행안부(ROLE_ADMIN_1~4)를 통과시킨다.
     * AS-IS는 ROLE_SUPERVISOR만 권한조인을 우회하지만, 이 프로젝트는 화면을 계속 늘려가는 중이라
     * 권한행을 깜빡한 화면이 관리자 자신을 막는 사고를 피하려고 의도적으로 완화해 둔 것이다
     * (메뉴 미등록 URL의 fail-open과 같은 취지). nav 쪽은 {@link #navVisible}이 AS-IS와 동일하게
     * 엄격히 판정하므로, 권한행이 없는 메뉴는 "nav에서 숨김 + URL 직접접근은 가능" 상태가 된다.
     */
    public boolean hasAccess(Manager manager, Menu menu) {
        if (UNRESTRICTED_ROLES.contains(manager.getAuthority())) {
            return true;
        }
        return navVisible(manager, menu);
    }

    /**
     * nav(GNB/LNB)에 이 메뉴를 그릴지 - AS-IS getFirstMenuList/getSecondAndThirdMenuList의
     * {@code OP_MENU H, OP_MENU_RIGHT I WHERE H.MENU_ID = I.MENU_ID AND I.AUTHORITY IN (내 롤)}과
     * 동일하게 롤 우회 없이 OP_MENU_RIGHT만 본다. AS-IS에서 ROLE_SUPERVISOR만 이 조인을 건너뛰는데
     * 그 롤은 이 프로젝트에 없다. 2026-10-02 AS-IS op_menu/op_menu_right export와 전수 대조해서
     * 바로잡은 부분으로, 그 전에는 ROLE_ADMIN_1~4를 우회시켜 AS-IS엔 안 보이는 메뉴(권한행 0건인
     * 기부금 운영현황 6900/6901, 답례품 관리자 4701, 기부혜택증 설정 19102)가 노출되고 있었다.
     */
    public boolean navVisible(Manager manager, Menu menu) {
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

    /** 요청 URI로 찾은 활성 메뉴(리프)의 최상위 1차메뉴 id까지 부모 체인을 거슬러 올라간다
     *  - AS-IS는 3단(GNB → LNB 섹션 → 링크)이라 리프의 "조부모"가 상단 탭이다.
     *  AS-IS 구조에서는 최상위 메뉴의 부모가 root 행(menu_id=0)이므로 거기서 멈춘다
     *  - 안 멈추면 root까지 올라가 상단탭 id로 0을 돌려준다. */
    public Integer topMenuIdOf(Menu menu) {
        Menu cur = menu;
        for (int depth = 0; depth < 6 && cur != null && !cur.isTopLevel(); depth++) {
            Integer parentId = cur.getMenuParentId();
            if (parentId == null || parentId == 0) {
                break;
            }
            cur = menuRepository.findById(parentId).orElse(null);
        }
        return cur != null ? cur.getMenuId() : menu.getMenuId();
    }

    /**
     * 상단 1차 메뉴(GNB) + 좌측 사이드바(2차 섹션 → 3차 링크) 렌더링용 - AS-IS inc_header.jsp의
     * 실제 3단 레이아웃(firstMenuList / secondAndThirdMenuList + childMenu)을 재현한다.
     * 그룹 행(MENU_URL=NULL인 상단/섹션)은 OP_MENU_RIGHT를 주지 않는다 - "하위 링크 중 하나라도
     * 접근 가능하면 그 섹션·상단도 보여준다"로 판단한다(새 링크 추가 때 부모 권한을 매번
     * 챙길 필요가 없게). 링크(리프)는 menuUrl이 있는 행으로, 여기에만 RBAC를 적용한다.
     */
    public NavTree navTreeFor(Manager manager) {
        List<Menu> all = visibleMenus();

        Map<Integer, List<Menu>> byParent = all.stream()
                .filter(m -> m.getMenuParentId() != null)
                .collect(Collectors.groupingBy(Menu::getMenuParentId));

        List<Menu> tops = all.stream()
                .filter(Menu::isTopLevel)
                .sorted(Comparator.comparing(Menu::getMenuSeq))
                .toList();

        List<Menu> topMenus = new java.util.ArrayList<>();
        Map<Integer, String> topFirstUrl = new LinkedHashMap<>();
        Map<Integer, List<Menu>> sectionsByTop = new LinkedHashMap<>();
        Map<Integer, List<Menu>> leavesBySection = new LinkedHashMap<>();

        for (Menu top : tops) {
            List<Menu> visibleSections = new java.util.ArrayList<>();
            String firstUrl = null;
            for (Menu section : sorted(byParent.getOrDefault(top.getMenuId(), List.of()))) {
                List<Menu> leaves = sorted(byParent.getOrDefault(section.getMenuId(), List.of())).stream()
                        .filter(leaf -> leaf.getMenuUrl() != null && !leaf.getMenuUrl().isBlank())
                        .filter(leaf -> navVisible(manager, leaf))
                        .toList();
                if (leaves.isEmpty()) {
                    continue;
                }
                visibleSections.add(section);
                leavesBySection.put(section.getMenuId(), leaves);
                if (firstUrl == null) {
                    firstUrl = leaves.get(0).getMenuUrl();
                }
            }
            if (visibleSections.isEmpty()) {
                continue;
            }
            topMenus.add(top);
            sectionsByTop.put(top.getMenuId(), visibleSections);
            topFirstUrl.put(top.getMenuId(), firstUrl);
        }

        return new NavTree(topMenus, topFirstUrl, sectionsByTop, leavesBySection);
    }

    private static List<Menu> sorted(List<Menu> menus) {
        return menus.stream().sorted(Comparator.comparing(Menu::getMenuSeq)).toList();
    }

    public record NavTree(List<Menu> topMenus,
                          Map<Integer, String> topFirstUrl,
                          Map<Integer, List<Menu>> sectionsByTop,
                          Map<Integer, List<Menu>> leavesBySection) {
    }
}

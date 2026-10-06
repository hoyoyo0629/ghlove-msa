package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonMessage;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.domain.MenuRight;
import com.ghlove.admin.domain.MenuSeller;
import com.ghlove.admin.repository.CommonMessageRepository;
import com.ghlove.admin.repository.MenuRepository;
import com.ghlove.admin.repository.MenuRightRepository;
import com.ghlove.admin.repository.MenuSellerRepository;
import com.ghlove.admin.repository.MenuTreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 메뉴관리 (AS-IS opmanager/menu - MenuManagerController + menu-manager-mapper.xml).
 * AS-IS는 menuGubun으로 관리자(OP_MENU)/답례품관리자(OP_MENU_SELLER) 두 트리를 같은 화면에서
 * 다루고, 메뉴ID를 레벨별 규칙으로 채번하며, 관리자 메뉴일 때는 메뉴명을 OP_COMMON_MESSAGE의
 * {@code MENU_<menuId>} 코드로도 함께 등록·수정·삭제한다(화면들이 제목을 그 코드로 읽는다).
 */
@Service
@RequiredArgsConstructor
public class MenuAdminService {

    /** AS-IS menuGubun 값. */
    public static final String GUBUN_MANAGER = "OPMANAGER";
    public static final String GUBUN_SELLER = "SELLER";

    private static final String MESSAGE_LANGUAGE = "ko";

    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;
    private final MenuSellerRepository menuSellerRepository;
    private final MenuTreeRepository menuTreeRepository;
    private final CommonMessageRepository commonMessageRepository;
    private final CommonMessageService commonMessageService;

    public record MenuGroup(Menu parent, List<Menu> children) {
    }

    /** AS-IS sqlMenuWhere - menuGubun에 따라 테이블을 갈아끼운다. */
    private static String tableOf(String menuGubun) {
        return GUBUN_SELLER.equals(menuGubun) ? MenuTreeRepository.TABLE_SELLER : MenuTreeRepository.TABLE_MANAGER;
    }

    /** AS-IS getManagerMenuList / getSellerMenuList - 계층 전개 + 경로 + 자식 수. */
    public List<MenuTreeRepository.MenuRow> tree(String menuGubun) {
        return menuTreeRepository.tree(tableOf(menuGubun));
    }

    /** AS-IS getFirstMenuList - 등록/수정 팝업의 1차 메뉴 셀렉트. */
    public List<MenuTreeRepository.MenuRow> firstMenus(String menuGubun) {
        return menuTreeRepository.firstMenus(tableOf(menuGubun));
    }

    /** AS-IS getSecondMenuList - 1차를 고르면 ajax로 받아오는 2차 메뉴 목록. */
    public List<MenuTreeRepository.MenuRow> childMenus(String menuGubun, Integer parentId) {
        return menuTreeRepository.childMenus(tableOf(menuGubun), parentId);
    }

    /** AS-IS getMenuId - 레벨별 채번(1레벨 +1000, 2레벨 +100, 3레벨 +1). */
    public int nextMenuId(String menuGubun, int menuType, Integer menuParentId) {
        return menuTreeRepository.nextMenuId(tableOf(menuGubun), menuType, menuParentId);
    }

    public Menu findOrThrow(Integer id) {
        return menuRepository.findById(id).orElseThrow(() -> new ManagerException("메뉴가 존재하지 않습니다: " + id));
    }

    /** 수정 팝업이 보여줄 한 건 - 답례품관리자 트리도 같은 화면에서 다룬다. */
    public MenuForm formOf(String menuGubun, Integer menuId) {
        if (GUBUN_SELLER.equals(menuGubun)) {
            MenuSeller m = menuSellerRepository.findById(menuId)
                    .orElseThrow(() -> new ManagerException("메뉴가 존재하지 않습니다: " + menuId));
            return new MenuForm(m.getMenuId(), m.getMenuParentId(), m.getMenuType(), m.getMenuName(),
                    m.getMenuCode(), m.getMenuUrl(), m.getMenuSeq(), m.getDisplayFlag(), m.getStatusCode());
        }
        Menu m = findOrThrow(menuId);
        return new MenuForm(m.getMenuId(), m.getMenuParentId(), m.getMenuType(), m.getMenuName(),
                m.getMenuCode(), m.getMenuUrl(), m.getMenuSeq(), m.getDisplayFlag(), m.getStatusCode());
    }

    /** 등록/수정 팝업이 주고받는 값 - AS-IS Menu 커맨드 객체의 입력 항목과 같다. */
    public record MenuForm(Integer menuId, Integer menuParentId, Integer menuType, String menuName,
                           String menuCode, String menuUrl, Integer menuSeq, String displayFlag,
                           String statusCode) {
    }

    /** AS-IS insertMenu + insertMenuCode(관리자 메뉴일 때만 OP_COMMON_MESSAGE도 등록). */
    @Transactional
    public void createMenu(String menuGubun, MenuForm form) {
        if (GUBUN_SELLER.equals(menuGubun)) {
            MenuSeller m = new MenuSeller();
            m.setMenuId(form.menuId());
            applySeller(m, form);
            menuSellerRepository.save(m);
            return;
        }
        Menu m = new Menu();
        m.setMenuId(form.menuId());
        applyManager(m, form);
        menuRepository.save(m);
        saveMenuCode(form.menuId(), form.menuName());
    }

    /** AS-IS updateMenu + updateMenuCode. */
    @Transactional
    public void updateMenu(String menuGubun, MenuForm form) {
        if (GUBUN_SELLER.equals(menuGubun)) {
            MenuSeller m = menuSellerRepository.findById(form.menuId())
                    .orElseThrow(() -> new ManagerException("메뉴가 존재하지 않습니다: " + form.menuId()));
            applySeller(m, form);
            menuSellerRepository.save(m);
            return;
        }
        Menu m = findOrThrow(form.menuId());
        applyManager(m, form);
        menuRepository.save(m);
        saveMenuCode(form.menuId(), form.menuName());
    }

    /**
     * AS-IS deleteMenu + deleteMenuCode. AS-IS는 하위메뉴 검사를 화면 JS(menuChild > 0)에서만
     * 하므로 서버에서도 한 번 더 막는다 - 목록을 거치지 않고 호출되면 트리가 끊긴다.
     */
    @Transactional
    public void deleteMenu(String menuGubun, Integer menuId) {
        if (GUBUN_SELLER.equals(menuGubun)) {
            if (menuSellerRepository.countByMenuParentId(menuId) > 0) {
                throw new ManagerException("하위메뉴가 존재하여 삭제할 수 없습니다.");
            }
            menuSellerRepository.deleteById(menuId);
            return;
        }
        if (menuRepository.countByMenuParentId(menuId) > 0) {
            throw new ManagerException("하위메뉴가 존재하여 삭제할 수 없습니다.");
        }
        menuRightRepository.deleteAll(menuRightRepository.findByMenuId(menuId));
        menuRepository.deleteById(menuId);
        deleteMenuCode(menuId);
    }

    private void applyManager(Menu m, MenuForm form) {
        m.setMenuParentId(form.menuParentId());
        m.setMenuType(form.menuType());
        m.setMenuName(form.menuName());
        m.setMenuCode(blankToNull(form.menuCode()));
        m.setMenuUrl(blankToNull(form.menuUrl()));
        m.setMenuSeq(form.menuSeq());
        m.setDisplayFlag(form.displayFlag() != null ? form.displayFlag() : "Y");
        m.setStatusCode(form.statusCode() != null ? form.statusCode() : "1");
    }

    private void applySeller(MenuSeller m, MenuForm form) {
        m.setMenuParentId(form.menuParentId());
        m.setMenuType(form.menuType());
        m.setMenuName(form.menuName());
        m.setMenuCode(blankToNull(form.menuCode()));
        m.setMenuUrl(blankToNull(form.menuUrl()));
        m.setMenuSeq(form.menuSeq());
        m.setDisplayFlag(form.displayFlag() != null ? form.displayFlag() : "Y");
        m.setStatusCode(form.statusCode() != null ? form.statusCode() : "1");
    }

    /** AS-IS insertMenuCode/updateMenuCode - 'MENU_<menuId>' 코드로 메뉴명을 문구표에 함께 넣는다.
     *  화면들이 제목을 이 코드로 읽으므로({@code ${msg.get('MENU_1401')}}) 문구 캐시도 비워준다. */
    private void saveMenuCode(Integer menuId, String menuName) {
        CommonMessage message = new CommonMessage();
        message.setId("MENU_" + menuId);
        message.setLanguage(MESSAGE_LANGUAGE);
        message.setMessage(menuName);
        commonMessageRepository.save(message);
        commonMessageService.reload();
    }

    private void deleteMenuCode(Integer menuId) {
        commonMessageRepository.findByIdAndLanguage("MENU_" + menuId, MESSAGE_LANGUAGE)
                .ifPresent(commonMessageRepository::delete);
        commonMessageService.reload();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // ---- 아래는 다른 화면에서 쓰는 기존 메서드 (유지) ----

    /** 최상위 그룹(부모 없음) 순서대로 하위 메뉴까지 묶어 반환 - 화면 여부 무관하게 전체. */
    public List<MenuGroup> allGrouped() {
        List<Menu> all = menuRepository.findAllByOrderByMenuParentIdAscMenuSeqAsc();
        Map<Integer, List<Menu>> childrenByParent = new LinkedHashMap<>();
        for (Menu m : all) {
            if (m.getMenuParentId() != null) {
                childrenByParent.computeIfAbsent(m.getMenuParentId(), k -> new java.util.ArrayList<>()).add(m);
            }
        }
        return all.stream()
                .filter(Menu::isTopLevel)
                .sorted(Comparator.comparing(Menu::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> new MenuGroup(p, childrenByParent.getOrDefault(p.getMenuId(), List.of())))
                .toList();
    }

    public List<Menu> topLevelMenus() {
        return menuRepository.findAllByOrderByMenuParentIdAscMenuSeqAsc().stream()
                .filter(Menu::isTopLevel)
                .toList();
    }

    public List<String> rightsOf(Integer menuId) {
        return menuRightRepository.findByMenuId(menuId).stream().map(MenuRight::getAuthority).toList();
    }
}

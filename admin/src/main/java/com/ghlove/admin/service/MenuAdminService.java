package com.ghlove.admin.service;

import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.domain.MenuRight;
import com.ghlove.admin.repository.MenuRepository;
import com.ghlove.admin.repository.MenuRightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 메뉴관리 CRUD (AS-IS opmanager/menu - MenuManagerController). 지금까지 이 프로젝트의
 * 모든 구현 라운드는 새 화면을 만들 때마다 OP_MENU에 직접 SQL INSERT를 해왔다 - 이 화면이
 * 그 수동 작업을 admin 콘솔 안에서 할 수 있게 대체한다. */
@Service
@RequiredArgsConstructor
public class MenuAdminService {

    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;

    public record MenuGroup(Menu parent, List<Menu> children) {
    }

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
                .filter(m -> m.getMenuParentId() == null)
                .sorted(Comparator.comparing(Menu::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> new MenuGroup(p, childrenByParent.getOrDefault(p.getMenuId(), List.of())))
                .toList();
    }

    public List<Menu> topLevelMenus() {
        return menuRepository.findAllByOrderByMenuParentIdAscMenuSeqAsc().stream()
                .filter(m -> m.getMenuParentId() == null)
                .toList();
    }

    public Menu findOrThrow(Integer id) {
        return menuRepository.findById(id).orElseThrow(() -> new ManagerException("메뉴가 존재하지 않습니다: " + id));
    }

    public List<String> rightsOf(Integer menuId) {
        return menuRightRepository.findByMenuId(menuId).stream().map(MenuRight::getAuthority).toList();
    }

    @Transactional
    public Menu create(Integer parentId, String menuName, String menuUrl, boolean grantLocgovMain, boolean grantLocgovSub) {
        Menu menu = new Menu();
        menu.setMenuParentId(parentId);
        menu.setMenuName(menuName);
        menu.setMenuUrl(menuUrl != null && menuUrl.isBlank() ? null : menuUrl);
        menu.setMenuSeq(nextSeq(parentId));
        menu.setDisplayFlag("Y");
        menu.setStatusCode("1");
        Menu saved = menuRepository.save(menu);
        applyLocgovRights(saved.getMenuId(), grantLocgovMain, grantLocgovSub);
        return saved;
    }

    @Transactional
    public void update(Integer id, String menuName, String menuUrl, Integer menuSeq, boolean grantLocgovMain, boolean grantLocgovSub) {
        Menu menu = findOrThrow(id);
        menu.setMenuName(menuName);
        menu.setMenuUrl(menuUrl != null && menuUrl.isBlank() ? null : menuUrl);
        if (menuSeq != null) {
            menu.setMenuSeq(menuSeq);
        }
        menuRepository.save(menu);
        menuRightRepository.deleteAll(menuRightRepository.findByMenuId(id));
        applyLocgovRights(id, grantLocgovMain, grantLocgovSub);
    }

    @Transactional
    public void delete(Integer id) {
        if (menuRepository.countByMenuParentId(id) > 0) {
            throw new ManagerException("하위 메뉴가 남아있어 삭제할 수 없습니다. 하위 메뉴를 먼저 삭제하세요.");
        }
        menuRightRepository.deleteAll(menuRightRepository.findByMenuId(id));
        menuRepository.deleteById(id);
    }

    private void applyLocgovRights(Integer menuId, boolean grantLocgovMain, boolean grantLocgovSub) {
        if (grantLocgovMain) {
            grant(menuId, "ROLE_ADMIN_5");
        }
        if (grantLocgovSub) {
            grant(menuId, "ROLE_ADMIN_6");
        }
    }

    private void grant(Integer menuId, String authority) {
        MenuRight r = new MenuRight();
        r.setMenuId(menuId);
        r.setAuthority(authority);
        menuRightRepository.save(r);
    }

    private Integer nextSeq(Integer parentId) {
        return menuRepository.findByMenuParentId(parentId).stream()
                .map(Menu::getMenuSeq)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}

package com.ghlove.admin.service;

import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.domain.MenuRight;
import com.ghlove.admin.domain.Role;
import com.ghlove.admin.repository.MenuRepository;
import com.ghlove.admin.repository.MenuRightRepository;
import com.ghlove.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * D10 역할·메뉴권한 CRUD (AS-IS opmanager/user-group - UserGroupController, 이름과 달리
 * 회원그룹이 아니라 관리자권한관리다, docs/as-is-admin-gap-deep-audit-part2.md D10 참고).
 * 역할(OP_ROLE) 자체의 이름/설명/순서 CRUD와, 역할별로 어떤 메뉴에 접근할 수 있는지를
 * 다루는 메뉴트리×권한 체크박스 매트릭스(OP_MENU_RIGHT) 편집을 함께 제공한다.
 *
 * MenuAdminService(방금 완성된 메뉴 자체 CRUD)의 메뉴트리 조회 로직을 재사용하되, 그쪽은
 * 메뉴 등록/수정 시 ROLE_ADMIN_5/6만 대상으로 한 특수 케이스 저장이라 이 화면(모든 역할에
 * 대해 메뉴 하나씩 권한을 토글)과는 저장 로직이 달라 별도로 구현한다.
 */
@Service
@RequiredArgsConstructor
public class RoleAdminService {

    /** AS-IS 특수 목적 역할(엑셀다운로드/ISMS/MD) - 삭제 금지. 이 프로젝트의 OP_ROLE에는 아직
     *  이 세 행이 없지만(향후 세분화 대비 가드), 구조적으로 하드코딩된 6+2단계 관리자 역할
     *  (ROLE_ADMIN_1~8 - MenuService.UNRESTRICTED_ROLES/LOCGOV_SCOPED_ROLES 등)도 삭제되면
     *  로그인 자체가 깨질 수 있어 함께 보호한다. */
    private static final Set<String> PROTECTED_AUTHORITIES = Set.of(
            "ROLE_EXCEL", "ROLE_ISMS", "ROLE_MD",
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
            "ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_7", "ROLE_ADMIN_8");

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;

    public List<Role> list() {
        return roleRepository.findAllByOrderByRoleSeq();
    }

    public Role get(String authority) {
        return roleRepository.findById(authority).orElseThrow(() -> new ManagerException("역할을 찾을 수 없습니다: " + authority));
    }

    public boolean isProtected(String authority) {
        return PROTECTED_AUTHORITIES.contains(authority);
    }

    @Transactional
    public Role create(String authority, String roleName, String roleDesc, Integer roleSeq) {
        if (authority == null || authority.isBlank()) {
            throw new ManagerException("권한코드를 입력해 주세요.");
        }
        if (roleName == null || roleName.isBlank()) {
            throw new ManagerException("역할명을 입력해 주세요.");
        }
        if (roleRepository.existsById(authority)) {
            throw new ManagerException("이미 존재하는 권한코드입니다.");
        }
        Role role = new Role();
        role.setAuthority(authority);
        role.setRoleName(roleName);
        role.setRoleDesc(roleDesc);
        role.setRoleSeq(roleSeq != null ? roleSeq : nextSeq());
        return roleRepository.save(role);
    }

    @Transactional
    public void update(String authority, String roleName, String roleDesc, Integer roleSeq) {
        Role role = get(authority);
        if (roleName == null || roleName.isBlank()) {
            throw new ManagerException("역할명을 입력해 주세요.");
        }
        role.setRoleName(roleName);
        role.setRoleDesc(roleDesc);
        if (roleSeq != null) {
            role.setRoleSeq(roleSeq);
        }
        roleRepository.save(role);
    }

    @Transactional
    public void delete(String authority) {
        if (isProtected(authority)) {
            throw new ManagerException("이 역할은 시스템에서 사용 중이라 삭제할 수 없습니다.");
        }
        if (!roleRepository.existsById(authority)) {
            throw new ManagerException("역할을 찾을 수 없습니다.");
        }
        menuRightRepository.deleteAll(menuRightRepository.findByAuthority(authority));
        roleRepository.deleteById(authority);
    }

    /** 메뉴트리(부모→자식) + 이 역할이 현재 체크된 메뉴ID 집합. */
    public MatrixData matrixFor(String authority) {
        List<Menu> all = menuRepository.findAllByOrderByMenuParentIdAscMenuSeqAsc();
        Map<Integer, List<Menu>> childrenByParent = new LinkedHashMap<>();
        for (Menu m : all) {
            if (m.getMenuParentId() != null) {
                childrenByParent.computeIfAbsent(m.getMenuParentId(), k -> new java.util.ArrayList<>()).add(m);
            }
        }
        List<Menu> topMenus = all.stream()
                .filter(m -> m.getMenuParentId() == null)
                .sorted(Comparator.comparing(Menu::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Set<Integer> checked = menuRightRepository.findByAuthority(authority).stream()
                .map(MenuRight::getMenuId).collect(java.util.stream.Collectors.toSet());
        return new MatrixData(topMenus, childrenByParent, checked);
    }

    /** 매트릭스 저장 - 이 역할의 기존 OP_MENU_RIGHT를 전부 지우고 체크된 메뉴만 다시 넣는다. */
    @Transactional
    public void saveMatrix(String authority, List<Integer> menuIds) {
        get(authority); // 존재 확인
        menuRightRepository.deleteAll(menuRightRepository.findByAuthority(authority));
        if (menuIds == null) {
            return;
        }
        for (Integer menuId : menuIds) {
            MenuRight r = new MenuRight();
            r.setMenuId(menuId);
            r.setAuthority(authority);
            menuRightRepository.save(r);
        }
    }

    private Integer nextSeq() {
        return roleRepository.findAllByOrderByRoleSeq().stream()
                .map(Role::getRoleSeq).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    public record MatrixData(List<Menu> topMenus, Map<Integer, List<Menu>> childrenByParent, Set<Integer> checkedMenuIds) {
    }
}

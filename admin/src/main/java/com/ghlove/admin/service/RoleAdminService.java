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
 * 회원그룹이 아니라 관리자권한관리다, docs/as-is-parity.md §8 D10 참고).
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

    /** AS-IS 특수 목적 역할(엑셀다운로드/ISMS/MD) - 삭제 금지. AS-IS OP_ROLE에도 이 세 행은
     *  없지만(목록 조회 조건에만 등장한다) 향후 세분화 대비로 가드를 둔다. 여기에 더해
     *  AS-IS가 실제로 가진 ROLE_ADMIN_1~10 전체를 보호한다 - 1~6은
     *  MenuService.UNRESTRICTED_ROLES/LOCGOV_SCOPED_ROLES에 하드코딩돼 있어 지우면 로그인이
     *  깨지고, 9(답례품관리자)·10(지정기부사업자)은 운영 중 사용자가 배정된 권한이다.
     *  AS-IS 삭제 버튼 자체가 JSP에서 주석처리돼 호출 경로는 없다. */
    private static final Set<String> PROTECTED_AUTHORITIES = Set.of(
            "ROLE_EXCEL", "ROLE_ISMS", "ROLE_MD",
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
            "ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_7", "ROLE_ADMIN_8",
            "ROLE_ADMIN_9", "ROLE_ADMIN_10");

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final MenuRightRepository menuRightRepository;
    private final com.ghlove.admin.repository.ManagerRepository managerRepository;

    public List<Role> list() {
        return roleRepository.findAllByOrderByRoleSeq();
    }

    /**
     * AS-IS 권한그룹 목록(user-group/list.jsp) - 그룹명·설명·인원·생성일자를 함께 보여준다.
     * AS-IS는 OP_ROLE LEFT JOIN OP_USER_ROLE으로 인원을 세지만 TO-BE는 매니저가 AUTHORITY를
     * 직접 들고 있어 그 역할의 매니저 수로 센다.
     */
    public List<AdminRoleRow> adminRoleRows() {
        return roleRepository.findAdminRoles().stream()
                .map(r -> new AdminRoleRow(r, managerRepository.countByAuthority(r.getAuthority())))
                .toList();
    }

    /**
     * 1404(사용자 권한 관리) 왼쪽 목록 - AS-IS role/list.jsp는 {@code <c:if test="${role.groupName
     * != '지정기부사업자'}">}로 <b>지정기부사업자만 빼고</b> 그리고, 번호(rowNum)도 그려진 행만 센다.
     * 1405(권한그룹 목록)에는 이 필터가 없어 10개가 다 나온다 - 그래서 목록 조회를 두 개로 나눈다.
     *
     * <p>AS-IS는 이 걸러내기를 화면에서 하지만 여기서는 서버에서 한다. Thymeleaf의
     * {@code th:each} + {@code th:if}는 건너뛴 행까지 {@code i.count}에 포함해서,
     * 화면에서 거르면 첫 행 번호가 2부터 시작한다(AS-IS는 1).
     */
    public List<AdminRoleRow> matrixRoleRows() {
        return adminRoleRows().stream()
                .filter(r -> !"지정기부사업자".equals(r.role().getRoleName()))
                .toList();
    }

    /**
     * AS-IS가 목록 첫 행을 기본 선택하는 동작(role/list.jsp fnGroupSearch의 target==null 분기)용.
     * 그려진 첫 행을 집으므로({@code $("#targetTb tr:first")}) 걸러낸 목록에서 가져와야 한다 -
     * AS-IS 데이터에서 CREATED_DATE가 가장 최근인 건 지정기부사업자(2024-03-25)지만 그 행은
     * 그려지지 않으므로, 실제로 선택되는 건 그 다음인 답례품관리자다.
     */
    public String firstAdminAuthority() {
        List<AdminRoleRow> rows = matrixRoleRows();
        return rows.isEmpty() ? null : rows.get(0).role().getAuthority();
    }

    /** 목록 한 줄 - AS-IS UserGroupResult(groupName=ROLE_NAME, groupExplanation=ROLE_DESC, userCount). */
    public record AdminRoleRow(Role role, long userCount) {
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
        // AS-IS insertRole은 CREATED_DATE/UPDATED_DATE를 CommonMapper.datetime(yyyyMMddHHmmss)로 넣는다
        role.setCreatedDate(nowStamp());
        role.setUpdatedDate(nowStamp());
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
        // AS-IS updateRole이 갱신하는 컬럼은 ROLE_NAME, ROLE_DESC, UPDATED_DATE 셋이다
        role.setUpdatedDate(nowStamp());
        roleRepository.save(role);
    }

    /** AS-IS CommonMapper.datetime - 운영관리 전역에서 쓰는 yyyyMMddHHmmss 문자열. */
    private static String nowStamp() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
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

    /**
     * 메뉴권한 체크박스 트리 + 이 역할이 현재 체크된 메뉴ID 집합.
     *
     * <p>AS-IS {@code MenuMapper.getAllMenuList}의 평면 행(1단,2단,3단)을 받아 AS-IS MyBatis
     * {@code AllMenuListResult} 중첩 resultMap과 같은 3단 트리로 묶는다. 묶는 기준은 AS-IS와
     * 같이 <b>등장 순서</b>이고(쿼리가 이미 단계별 MENU_SEQ로 정렬돼 있다) 중복 1단·2단은 합친다.
     *
     * <p>예전 구현은 {@code findAllByOrderByMenuParentIdAscMenuSeqAsc()} + {@code isTopLevel}로
     * 트리를 직접 걸었는데, 그러면 <b>미사용(STATUS_CODE='2') 메뉴와 자식 없는 빈 묶음까지
     * 전부 그려져</b> AS-IS 화면과 달라졌다(미사용 1단 9개·3단 7개가 노출). 조회 자체를 AS-IS
     * 쿼리로 바꿔 해결한다 - 화면에서 걸러내는 방식은 AS-IS에 없다.
     */
    public MatrixData matrixFor(String authority) {
        Map<Integer, MatrixNode> level1 = new LinkedHashMap<>();
        Map<Integer, MatrixNode> level2 = new LinkedHashMap<>();
        for (Object[] row : menuRepository.findAllMenuListForRightMatrix()) {
            MatrixNode m1 = level1.computeIfAbsent(((Number) row[0]).intValue(),
                    id -> new MatrixNode(id, (String) row[1], new java.util.ArrayList<>()));
            MatrixNode m2 = level2.computeIfAbsent(((Number) row[2]).intValue(), id -> {
                MatrixNode node = new MatrixNode(id, (String) row[3], new java.util.ArrayList<>());
                m1.children().add(node);
                return node;
            });
            m2.children().add(new MatrixNode(((Number) row[4]).intValue(), (String) row[5], List.of()));
        }
        Set<Integer> checked = menuRightRepository.findByAuthority(authority).stream()
                .map(MenuRight::getMenuId).collect(java.util.stream.Collectors.toSet());
        return new MatrixData(List.copyOf(level1.values()), checked);
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

    /** 메뉴권한 트리의 한 칸 - AS-IS getAllMenuList가 내려주는 건 menuId·menuName 둘뿐이다. */
    public record MatrixNode(Integer menuId, String menuName, List<MatrixNode> children) {
    }

    public record MatrixData(List<MatrixNode> topMenus, Set<Integer> checkedMenuIds) {
    }
}

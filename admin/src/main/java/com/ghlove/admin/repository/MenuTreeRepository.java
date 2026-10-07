package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메뉴관리(1409) 목록용 계층 조회. AS-IS getManagerMenuList / getSellerMenuList는 CUBRID의
 * {@code START WITH ... CONNECT BY PRIOR ... ORDER SIBLINGS BY}와 {@code SYS_CONNECT_BY_PATH}로
 * 메뉴트리를 펼치고 경로 문자열(MENU_PATH)과 자식 수(MENU_CHILD)를 함께 뽑는다.
 * PostgreSQL에는 CONNECT BY가 없어 재귀 CTE로 같은 결과를 만든다:
 * <ul>
 *   <li>START WITH MENU_ID='0' → 재귀 시작점을 menu_id = 0(root)으로</li>
 *   <li>ORDER SIBLINGS BY MENU_SEQ → 조상들의 (menu_seq, menu_id)를 누적한 배열로 정렬</li>
 *   <li>SUBSTRING(SYS_CONNECT_BY_PATH(MENU_NAME,'> '), 8) → root 이름을 뺀 'A> B> C' 경로</li>
 *   <li>WHERE MENU_PARENT_ID IS NOT NULL → root 행 자체는 목록에서 제외</li>
 * </ul>
 * AS-IS는 menuGubun으로 OP_MENU / OP_MENU_SELLER 테이블을 갈아끼우므로 테이블명을 받아 쓴다
 * (값은 호출부에서 화이트리스트로 고정한다 - 파라미터 바인딩이 불가능한 자리라서다).
 */
@Repository
@RequiredArgsConstructor
public class MenuTreeRepository {

    public static final String TABLE_MANAGER = "admin.op_menu";
    public static final String TABLE_SELLER = "admin.op_menu_seller";

    private final EntityManager entityManager;

    /** 한 줄 = AS-IS MenuResult + MENU_CHILD + MENU_PATH. */
    public record MenuRow(Integer menuId, Integer menuParentId, Integer menuType, String menuName,
                          String menuCode, String menuUrl, Integer menuSeq, String displayFlag,
                          String statusCode, long menuChild, String menuPath) {
    }

    public List<MenuRow> tree(String table) {
        String safeTable = TABLE_SELLER.equals(table) ? TABLE_SELLER : TABLE_MANAGER;
        String sql = """
                with recursive t as (
                    select m.menu_id, m.menu_parent_id, m.menu_type, m.menu_name, m.menu_code,
                           m.menu_url, m.menu_seq, m.display_flag, m.status_code,
                           cast('' as text) as menu_path,
                           array[]::bigint[] as sort_path
                      from %1$s m
                     where m.menu_id = 0
                    union all
                    select c.menu_id, c.menu_parent_id, c.menu_type, c.menu_name, c.menu_code,
                           c.menu_url, c.menu_seq, c.display_flag, c.status_code,
                           case when t.menu_path = '' then c.menu_name else t.menu_path || '> ' || c.menu_name end,
                           t.sort_path || (coalesce(c.menu_seq, 0)::bigint * 1000000 + c.menu_id)
                      from %1$s c
                      join t on c.menu_parent_id = t.menu_id
                )
                select t.menu_id, t.menu_parent_id, t.menu_type, t.menu_name, t.menu_code,
                       t.menu_url, t.menu_seq, t.display_flag, t.status_code,
                       (select count(*) from %1$s x where x.menu_parent_id = t.menu_id) as menu_child,
                       t.menu_path
                  from t
                 where t.menu_parent_id is not null
                 order by t.sort_path
                """.formatted(safeTable);

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(r -> new MenuRow(
                        asInt(r[0]), asInt(r[1]), asInt(r[2]), asString(r[3]), asString(r[4]),
                        asString(r[5]), asInt(r[6]), asString(r[7]), asString(r[8]),
                        r[9] == null ? 0L : ((Number) r[9]).longValue(), asString(r[10])))
                .toList();
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    /** AS-IS getFirstMenuList - MENU_TYPE=1(상단 메뉴)만, MENU_SEQ 순. */
    public List<MenuRow> firstMenus(String table) {
        return tree(table).stream().filter(r -> Integer.valueOf(1).equals(r.menuType())).toList();
    }

    /** AS-IS getSecondMenuList - 지정한 부모의 바로 아래 자식, MENU_SEQ 순. */
    public List<MenuRow> childMenus(String table, Integer parentId) {
        return tree(table).stream()
                .filter(r -> parentId != null && parentId.equals(r.menuParentId()))
                .toList();
    }

    /**
     * AS-IS getMenuId - 메뉴ID 채번 규칙 그대로.
     * 1레벨 {@code NVL(MAX(MENU_ID),0)+1000}, 2레벨 {@code NVL(MAX(MENU_ID),부모ID)+100},
     * 3레벨 {@code NVL(MAX(MENU_ID),부모ID)+1} (각각 같은 레벨·같은 부모 안에서의 MAX).
     */
    public int nextMenuId(String table, int menuType, Integer menuParentId) {
        String safeTable = TABLE_SELLER.equals(table) ? TABLE_SELLER : TABLE_MANAGER;
        String sql;
        if (menuType == 1) {
            sql = "select coalesce(max(menu_id), 0) + 1000 from " + safeTable + " where menu_type = 1";
            return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).intValue();
        }
        int step = (menuType == 2) ? 100 : 1;
        sql = "select coalesce(max(menu_id), :parentId) + " + step + " from " + safeTable
                + " where menu_type = :menuType and menu_parent_id = :parentId";
        Object result = entityManager.createNativeQuery(sql)
                .setParameter("menuType", menuType)
                .setParameter("parentId", menuParentId == null ? 0 : menuParentId)
                .getSingleResult();
        return ((Number) result).intValue();
    }

    private static Integer asInt(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }
}

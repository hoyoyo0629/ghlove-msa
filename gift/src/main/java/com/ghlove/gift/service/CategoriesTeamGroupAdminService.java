package com.ghlove.gift.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리 그룹 관리(AS-IS opmanager/categories-team-group, 메뉴 16102) - OP_CATEGORY_TEAM("대그룹") +
 * OP_CATEGORY_GROUP(1차 카테고리 그룹) 조회/등록/수정/삭제/순서변경. AS-IS CategoriesTeamGroupServiceImpl + categoriesteamgroup-mapper.xml.
 *
 * <p>AS-IS가 하는 것 중 TO-BE에 대응이 없어 옮기지 않은 것: 쇼핑몰 캐시 갱신(updateCategoryUpdatedDate/sendFrontReload),
 * 시퀀스 서비스(MAX+1 사용).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoriesTeamGroupAdminService {

    private final EntityManager em;

    // ---- 조회 ----

    public record GroupRow(Integer categoryGroupId, Integer categoryTeamId, String name, String code, String flag,
                           String accessType, Integer ordering) {
    }

    public record TeamRow(Integer categoryTeamId, String name, String code, String flag, Integer ordering,
                          List<GroupRow> groups) {
    }

    public record CategoryRef(String categoryCode, String categoryName) {
    }

    public record CategoriesInGroup(Integer categoryGroupId, String name, List<CategoryRef> categoryList) {
    }

    /** AS-IS getCategoriesTeamGroupList - 대그룹 순서 + 그룹 순서(그룹이 없는 대그룹도 나온다). 같은 순서값은 ID순으로 고정한다. */
    @Transactional(readOnly = true)
    public List<TeamRow> teamGroupList() {
        Map<Integer, List<GroupRow>> groupsByTeam = new LinkedHashMap<>();
        for (GroupRow g : groupRows("order by g.ordering asc nulls first, g.category_group_id asc")) {
            groupsByTeam.computeIfAbsent(g.categoryTeamId(), k -> new ArrayList<>()).add(g);
        }
        List<TeamRow> teams = new ArrayList<>();
        for (TeamRow t : teamList()) {
            teams.add(new TeamRow(t.categoryTeamId(), t.name(), t.code(), t.flag(), t.ordering(),
                    groupsByTeam.getOrDefault(t.categoryTeamId(), List.of())));
        }
        return teams;
    }

    /** AS-IS getCategoriesTeamList. */
    @Transactional(readOnly = true)
    public List<TeamRow> teamList() {
        List<TeamRow> list = new ArrayList<>();
        for (Object o : em.createNativeQuery("select category_team_id, name, code, category_team_flag, ordering "
                + "from public.op_category_team order by ordering asc nulls first, category_team_id asc").getResultList()) {
            Object[] r = (Object[]) o;
            list.add(new TeamRow(i(r[0]), s(r[1]), s(r[2]), s(r[3]), i(r[4]), List.of()));
        }
        return list;
    }

    /** AS-IS getCategoriesGroupList - 대그룹 순서, 그룹 순서로 평면 목록. */
    @Transactional(readOnly = true)
    public List<GroupRow> groupList() {
        List<GroupRow> list = new ArrayList<>();
        for (Object o : em.createNativeQuery("select g.category_group_id, g.category_team_id, g.name, g.code, g.category_group_flag, "
                + "g.access_type, g.ordering from public.op_category_group g "
                + "left join public.op_category_team t on g.category_team_id = t.category_team_id "
                + "order by t.ordering asc nulls first, t.category_team_id asc, g.ordering asc nulls first, g.category_group_id asc").getResultList()) {
            list.add(toGroup((Object[]) o));
        }
        return list;
    }

    /** AS-IS getCategoriesGroupById - 없으면 null. */
    @Transactional(readOnly = true)
    public GroupRow group(int categoryGroupId) {
        List<?> rows = em.createNativeQuery("select category_group_id, category_team_id, name, code, category_group_flag, access_type, ordering "
                + "from public.op_category_group where category_group_id = :id").setParameter("id", categoryGroupId).getResultList();
        return rows.isEmpty() ? null : toGroup((Object[]) rows.get(0));
    }

    /** AS-IS getCategoriesListById - 그룹에 속한 공개 1차 카테고리(순서순). */
    @Transactional(readOnly = true)
    public List<CategoryRef> categoriesOf(int categoryGroupId) {
        List<CategoryRef> list = new ArrayList<>();
        for (Object o : em.createNativeQuery("select category_code, category_name from public.op_category "
                + "where category_flag = 'Y' and category_level = '1' and category_group_id = :id and category_group_id > 0 "
                + "order by ordering asc").setParameter("id", categoryGroupId).getResultList()) {
            Object[] r = (Object[]) o;
            list.add(new CategoryRef(s(r[0]), s(r[1])));
        }
        return list;
    }

    /** AS-IS getGroupsInCategoriesList - 공개 1차 카테고리를 그룹별로(그룹이 없는 카테고리는 ID 0 = "미등록"). CUBRID는 NULL이 가장 작아 NULLS FIRST. */
    @Transactional(readOnly = true)
    public List<CategoriesInGroup> categoriesInGroups() {
        Map<Integer, String> names = new LinkedHashMap<>();
        Map<Integer, List<CategoryRef>> byGroup = new LinkedHashMap<>();
        for (Object o : em.createNativeQuery("select cg.name, cg.ordering, cg.category_group_id, c.category_code, c.category_name "
                + "from public.op_category_team a "
                + "left join public.op_category_group cg on a.category_team_id = cg.category_team_id "
                + "right join public.op_category c on cg.category_group_id = c.category_group_id "
                + "where c.category_flag = 'Y' and c.category_level = '1' "
                + "order by a.ordering asc nulls first, cg.ordering asc nulls first, cg.category_group_id asc nulls first").getResultList()) {
            Object[] r = (Object[]) o;
            int gid = r[2] == null ? 0 : i(r[2]);
            names.putIfAbsent(gid, s(r[0]));
            byGroup.computeIfAbsent(gid, k -> new ArrayList<>()).add(new CategoryRef(s(r[3]), s(r[4])));
        }
        List<CategoriesInGroup> list = new ArrayList<>();
        byGroup.forEach((gid, cats) -> list.add(new CategoriesInGroup(gid, names.get(gid), cats)));
        return list;
    }

    // ---- 등록/수정/삭제 ----

    public record GroupForm(Integer categoryGroupId, Integer categoryTeamId, String name, String code,
                            String categoryGroupFlag, String accessType, List<String> categoryCodes) {
    }

    /**
     * AS-IS insertCategoriesGroup - 순서는 같은 대그룹 안의 최대값 + 1(없으면 1). SEO 칼럼은 폼에 없어 NULL.
     * 이어서 선택된 카테고리가 있으면(등록 폼엔 없다) 소속을 저장한다. @return 새 그룹 ID
     */
    public int createGroup(GroupForm f) {
        Object max = em.createNativeQuery("select max(category_group_id) from public.op_category_group").getSingleResult();
        int id = (max == null ? 0 : ((Number) max).intValue()) + 1;
        em.createNativeQuery("insert into public.op_category_group (category_group_id, category_team_id, name, code, created_date, "
                        + "category_group_flag, access_type, ordering) "
                        + "select :id, :team, cast(:name as varchar), cast(:code as varchar), to_char(now(), 'YYYYMMDDHH24MISS'), cast(:flag as varchar), cast(:acc as varchar), "
                        + "coalesce(max(ordering) + 1, 1) from public.op_category_group where category_team_id = :team")
                .setParameter("id", id).setParameter("team", f.categoryTeamId()).setParameter("name", f.name())
                .setParameter("code", f.code()).setParameter("flag", f.categoryGroupFlag()).setParameter("acc", f.accessType())
                .executeUpdate();
        saveCategories(id, f.categoryCodes());
        return id;
    }

    /**
     * AS-IS updateCategoryGroup 컨트롤러 흐름 - ①REMOVE: 이 그룹 소속이면서 선택 목록에 없는 카테고리의 그룹을 0으로(목록이 없으면 전부)
     * ②SAVE: 선택 목록의 카테고리를 이 그룹으로 ③그룹 행 수정. 그룹 행 수정은 SEO 칼럼을 폼에 없는 값으로 덮어 NULL로 만든다
     * (DESCRIPTION만 AS-IS가 주석처리해 보존).
     */
    public void updateGroup(GroupForm f) {
        int id = f.categoryGroupId();
        if (f.categoryCodes() == null) {
            em.createNativeQuery("update public.op_category set category_group_id = 0 where category_group_id = :id")
                    .setParameter("id", id).executeUpdate();
        } else {
            em.createNativeQuery("update public.op_category set category_group_id = 0 where category_group_id = :id "
                            + "and category_code not in (:codes)")
                    .setParameter("id", id).setParameter("codes", f.categoryCodes()).executeUpdate();
        }
        saveCategories(id, f.categoryCodes());
        em.createNativeQuery("update public.op_category_group set category_team_id = :team, name = :name, code = :code, "
                        + "category_group_flag = :flag, access_type = :acc, updated_date = to_char(now(), 'YYYYMMDDHH24MISS'), "
                        + "title = null, keywords = null, header_contents1 = null, header_contents2 = null, header_contents3 = null, "
                        + "themaword_title = null, themaword_description = null, rank_title = null, rank_keywords = null, "
                        + "rank_description = null, rank_headercontents1 = null, rank_themaword_title = null, "
                        + "rank_themaword_description = null where category_group_id = :id")
                .setParameter("team", f.categoryTeamId()).setParameter("name", f.name()).setParameter("code", f.code())
                .setParameter("flag", f.categoryGroupFlag()).setParameter("acc", f.accessType()).setParameter("id", id)
                .executeUpdate();
    }

    private void saveCategories(int categoryGroupId, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        em.createNativeQuery("update public.op_category set category_group_id = :id where category_code in (:codes)")
                .setParameter("id", categoryGroupId).setParameter("codes", codes).executeUpdate();
    }

    /** AS-IS deleteCategoryGorup - 그룹 행만 지운다(소속 카테고리의 그룹 ID는 그대로 남는다). */
    public void deleteGroup(int categoryGroupId) {
        em.createNativeQuery("delete from public.op_category_group where category_group_id = :id")
                .setParameter("id", categoryGroupId).executeUpdate();
    }

    // ---- 순서변경 ----

    /** AS-IS updateCategoriesGroupOrdering(TeamGroupListParam) - ids[i]의 순서를 orderings[i]로. */
    public void updateGroupOrdering(List<Integer> ids, List<Integer> orderings) {
        for (int k = 0; k < ids.size(); k++) {
            em.createNativeQuery("update public.op_category_group set ordering = :ord, updated_date = to_char(now(), 'YYYYMMDDHH24MISS') "
                            + "where category_group_id = :id")
                    .setParameter("ord", orderings.get(k)).setParameter("id", ids.get(k)).executeUpdate();
        }
    }

    /** AS-IS updateCategoriesTeamOrdering(TeamGroupListParam). */
    public void updateTeamOrdering(List<Integer> ids, List<Integer> orderings) {
        for (int k = 0; k < ids.size(); k++) {
            em.createNativeQuery("update public.op_category_team set ordering = :ord, updated_date = to_char(now(), 'YYYYMMDDHH24MISS') "
                            + "where category_team_id = :id")
                    .setParameter("ord", orderings.get(k)).setParameter("id", ids.get(k)).executeUpdate();
        }
    }

    // ---- helpers ----

    private List<GroupRow> groupRows(String orderBy) {
        List<GroupRow> list = new ArrayList<>();
        for (Object o : em.createNativeQuery("select g.category_group_id, g.category_team_id, g.name, g.code, g.category_group_flag, "
                + "g.access_type, g.ordering from public.op_category_group g " + orderBy).getResultList()) {
            list.add(toGroup((Object[]) o));
        }
        return list;
    }

    private static GroupRow toGroup(Object[] r) {
        return new GroupRow(i(r[0]), i(r[1]), s(r[2]), s(r[3]), s(r[4]), s(r[5]), i(r[6]));
    }

    private static String s(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer i(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}

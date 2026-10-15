package com.ghlove.gift.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리관리 (AS-IS {@code saleson.shop.categories.CategoriesServiceImpl} +
 * {@code categories-mapper.xml}) - OP_CATEGORY 1~4차 트리 + 그룹 소속.
 *
 * <p>카테고리 코드는 12자리 = 3자리 x 4단계(CATEGORY_CLASS1~4). 1차는 100부터, 2차 이하는 001부터 비어 있는 번호를 쓴다.
 * 트리 노드 id는 {@code "<클래스들>-<레벨>-<순서>-<하위최대레벨>-<그룹ID>"}(예: 106001-2-1-2-11) 문자열이고 jsTree가
 * 그대로 들고 다니다가 이동(move) 때 서버로 돌려준다 - AS-IS 규칙 그대로다.
 *
 * <p>TO-BE에 없는 것: 상점 캐시 재적재(updateCategoryUpdatedDate - configService.updateCategoryUpdatedDate +
 * FRONT_CATEGORIES reload)와 카테고리 필터 화면(저장은 필터 그룹 ID가 안 넘어오므로 AS-IS도 사실상 삭제만 한다).
 * 카테고리 ID 채번은 AS-IS 시퀀스서비스 대신 MAX+1.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoriesAdminService {

    private static final String ICON_OPEN = "/content/opmanager/images/category/icon_folder.png";
    private static final String ICON_CLOSED = "/content/opmanager/images/category/icon_delete.png";

    private final EntityManager em;

    // ---- 조회 ----

    public record Category(Integer categoryId, String categoryCode, Integer categoryGroupId, String categoryUrl,
                           String categoryName, String categoryType, String categoryClass1, String categoryClass2,
                           String categoryClass3, String categoryClass4, String categoryLevel, Integer ordering,
                           String categoryFlag, String accessType, Seo categoriesSeo) {
    }

    public record Seo(String title, String keywords, String description, String headerContents1,
                      String headerContents2, String headerContents3, String themawordTitle, String themawordDescription) {
    }

    /** AS-IS getCategoryByCategoryCode. */
    @Transactional(readOnly = true)
    public Category byCode(String categoryCode) {
        List<?> rows = em.createNativeQuery(
                        "select category_id, category_code, category_group_id, category_url, category_name, category_type, "
                                + "category_class1, category_class2, category_class3, category_class4, category_level, ordering, "
                                + "category_flag, access_type, title, keywords, description, header_contents1, header_contents2, "
                                + "header_contents3, themaword_title, themaword_description "
                                + "from public.op_category where category_code = :c")
                .setParameter("c", categoryCode).getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = (Object[]) rows.get(0);
        return new Category(i(r[0]), s(r[1]), i(r[2]), s(r[3]), s(r[4]), s(r[5]), s(r[6]), s(r[7]), s(r[8]), s(r[9]),
                s(r[10]), i(r[11]), s(r[12]), s(r[13]),
                new Seo(s(r[14]), s(r[15]), s(r[16]), s(r[17]), s(r[18]), s(r[19]), s(r[20]), s(r[21])));
    }

    /**
     * AS-IS getCategoiesTreeListByLevel1 - 1차 카테고리(그룹 지정 시 그 그룹만)를 <b>이름순</b>으로.
     * 자식이 없으면(같은 1차 클래스가 자기 한 행뿐) 또는 타입이 "2"면 잎 노드.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> treeLevel1(String groupId) {
        boolean hasGroup = groupId != null && !groupId.isBlank();
        Query q = em.createNativeQuery(
                "select c1.category_group_id, c1.category_code, c1.category_name, c1.category_flag, c1.category_url, c1.ordering, "
                        + "c1.category_class1, c1.category_type, "
                        + "(select max(category_level) from public.op_category where category_class1 = c1.category_class1) as child_max_level, "
                        + "(select count(*) from public.op_category where category_class1 = c1.category_class1) as category_count "
                        + "from public.op_category c1 where c1.category_level = '1' "
                        + (hasGroup ? "and c1.category_group_id = :g " : "")
                        // CUBRID(utf8_bin)는 코드포인트 순 - PG의 en_US 콜레이션은 한글을 그렇게 정렬하지 않아 "C"로 맞춘다
                        + "order by c1.category_name collate \"C\" asc");
        if (hasGroup) {
            q.setParameter("g", Integer.parseInt(groupId.trim()));
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Object o : q.getResultList()) {
            Object[] r = (Object[]) o;
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", s(r[6]) + "-1-" + i(r[5]) + "-" + s(r[8]) + "-" + i(r[0]));
            node.put("text", s(r[2]) + " <span>[" + s(r[4]) + "]</span>");
            node.put("type", "root");
            node.put("icon", "Y".equals(s(r[3])) ? ICON_OPEN : ICON_CLOSED);
            node.put("children", !(l(r[9]) == 1 || "2".equals(s(r[7]))));
            nodes.add(node);
        }
        return nodes;
    }

    /** AS-IS getCategoiesTreeListByLevels - 노드 id("클래스들-레벨-…")의 한 단계 아래 자식들. 순서(ORDERING)순. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> treeChildren(String nodeId) {
        String[] parts = nodeId.split("-");
        String classes = parts[0];
        int childLevel = Integer.parseInt(parts[1]) + 1;
        if (childLevel < 2 || childLevel > 4 || classes.length() < (childLevel - 1) * 3) {
            return new ArrayList<>();
        }
        String[] parentClass = new String[3];
        for (int k = 0; k < childLevel - 1; k++) {
            parentClass[k] = classes.substring(k * 3, k * 3 + 3);
        }
        StringBuilder where = new StringBuilder("category_level = :lv");
        for (int k = 0; k < childLevel - 1; k++) {
            where.append(" and category_class").append(k + 1).append(" = :p").append(k + 1);
        }
        // 하위 행 수/최대 레벨은 "자기 클래스까지 같은 행들"로 센다(AS-IS C2/CHILD_MAX_LEVEL 서브쿼리)
        StringBuilder same = new StringBuilder();
        for (int k = 1; k <= childLevel; k++) {
            same.append(" and x.category_class").append(k).append(" = c1.category_class").append(k);
        }
        Query q = em.createNativeQuery(
                "select c1.category_group_id, c1.category_name, c1.category_flag, c1.category_url, c1.ordering, "
                        + "c1.category_class1, c1.category_class2, c1.category_class3, c1.category_class4, "
                        + "(select max(x.category_level) from public.op_category x where 1=1" + same + ") as child_max_level, "
                        + "(select count(*) from public.op_category x where 1=1" + same + ") as category_count "
                        + "from public.op_category c1 where " + where.toString().replace("category_", "c1.category_")
                        + " order by c1.ordering asc");
        q.setParameter("lv", String.valueOf(childLevel));
        for (int k = 0; k < childLevel - 1; k++) {
            q.setParameter("p" + (k + 1), parentClass[k]);
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Object o : q.getResultList()) {
            Object[] r = (Object[]) o;
            StringBuilder id = new StringBuilder();
            for (int k = 0; k < childLevel; k++) {
                id.append(s(r[5 + k]));
            }
            id.append('-').append(childLevel).append('-').append(i(r[4])).append('-').append(s(r[9])).append('-').append(i(r[0]));
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", id.toString());
            node.put("text", s(r[1]) + " <span>[" + s(r[3]) + "]</span>");
            node.put("icon", "Y".equals(s(r[2])) ? ICON_OPEN : ICON_CLOSED);
            node.put("children", l(r[10]) != 1);
            nodes.add(node);
        }
        return nodes;
    }

    /** AS-IS getCategoryCountByCode - URL(또는 코드)이 이미 쓰이는 수. 조건이 하나도 없으면 전체 행 수(AS-IS 그대로). */
    @Transactional(readOnly = true)
    public long countByUrlOrCode(String categoryUrl, String categoryCode) {
        StringBuilder sql = new StringBuilder("select count(*) from public.op_category where 1=1");
        boolean url = categoryUrl != null && !categoryUrl.isEmpty();
        boolean code = categoryCode != null && !categoryCode.isEmpty();
        if (url) {
            sql.append(" and category_url = :u");
        }
        if (code) {
            sql.append(" and category_code = :c");
        }
        Query q = em.createNativeQuery(sql.toString());
        if (url) {
            q.setParameter("u", categoryUrl);
        }
        if (code) {
            q.setParameter("c", categoryCode);
        }
        return l(q.getSingleResult());
    }

    // ---- 등록 ----

    public record Form(Integer categoryId, String categoryCode, Integer categoryGroupId, String categoryUrl,
                       String categoryName, String categoryLevel, String categoryFlag, String accessType,
                       String categoryClass1, String categoryClass2, String categoryClass3, String categoryClass4,
                       Seo categoriesSeo) {
    }

    /** AS-IS insertCategory(1차 카테고리 등록) - 코드는 (가장 큰 1차 클래스 + 1) + 000000000(비어 있으면 100000000000). */
    public Category create(Form f) {
        String level = "1";
        Object max = em.createNativeQuery("select max(cast(category_class1 as integer)) from public.op_category")
                .getSingleResult();
        int class1 = max == null ? 100 : ((Number) max).intValue() + 1;
        String code = String.format("%03d", class1) + "000000000";
        int ordering = maxOrdering(level, null, null, null) ;
        int id = nextCategoryId();
        insert(id, code, f, level, ordering);
        deleteFiltersOf(List.of(id));
        return byCode(code);
    }

    /** AS-IS insertCategorySub(하위 카테고리 등록) - 부모 코드 아래 비어 있는 다음 번호. 레벨은 폼의 값(부모 레벨+1). */
    public Category createSub(Form f) {
        Object[] parent = (Object[]) singleOrNull(em.createNativeQuery(
                        "select category_code, category_level from public.op_category where category_code = :c")
                .setParameter("c", f.categoryCode()).getResultList());
        if (parent == null) {
            throw new GiftException("상위 카테고리를 찾을 수 없습니다.");
        }
        int parentLevel = Integer.parseInt(s(parent[1]));
        String simple = s(parent[0]).substring(0, parentLevel * 3);
        int childLevel = parentLevel + 1;
        String classCol = "category_class" + childLevel;
        Object max = em.createNativeQuery("select max(cast(" + classCol + " as integer)) from public.op_category "
                        + "where category_code like :p and category_level = :lv")
                .setParameter("p", simple + "%").setParameter("lv", String.valueOf(childLevel)).getSingleResult();
        int next = (max == null ? 0 : ((Number) max).intValue()) + 1;
        String code = (simple + String.format("%03d", next) + "000000000").substring(0, 12);
        // 순서는 폼에 실려 온 "부모의" 클래스 기준으로 센다(AS-IS: 클래스 재계산 전에 getCategoryLevelMaxOrdring 호출)
        int ordering = maxOrdering(String.valueOf(childLevel), f.categoryClass1(), f.categoryClass2(), f.categoryClass3());
        int id = nextCategoryId();
        insert(id, code, f, String.valueOf(childLevel), ordering);
        return byCode(code);
    }

    private int maxOrdering(String level, String c1, String c2, String c3) {
        StringBuilder sql = new StringBuilder("select coalesce(max(ordering), 0) + 1 from public.op_category where category_level = :lv");
        if (usable(c1)) {
            sql.append(" and category_class1 = :c1");
        }
        if (usable(c2)) {
            sql.append(" and category_class2 = :c2");
        }
        if (usable(c3)) {
            sql.append(" and category_class3 = :c3");
        }
        Query q = em.createNativeQuery(sql.toString()).setParameter("lv", level);
        if (usable(c1)) {
            q.setParameter("c1", c1);
        }
        if (usable(c2)) {
            q.setParameter("c2", c2);
        }
        if (usable(c3)) {
            q.setParameter("c3", c3);
        }
        return ((Number) q.getSingleResult()).intValue();
    }

    private static boolean usable(String c) {
        return c != null && !c.isEmpty() && !"000".equals(c);
    }

    private int nextCategoryId() {
        Object max = em.createNativeQuery("select max(category_id) from public.op_category").getSingleResult();
        return (max == null ? 0 : ((Number) max).intValue()) + 1;
    }

    private void insert(int id, String code, Form f, String level, int ordering) {
        String[] cls = classesOf(code);
        Seo seo = f.categoriesSeo() == null ? new Seo(null, null, null, null, null, null, null, null) : f.categoriesSeo();
        em.createNativeQuery("insert into public.op_category (category_id, category_code, category_group_id, category_url, "
                        + "category_name, category_type, category_class1, category_class2, category_class3, category_class4, "
                        + "category_level, ordering, category_flag, access_type, title, keywords, description, header_contents1, "
                        + "header_contents2, header_contents3, themaword_title, themaword_description) values "
                        + "(:id, :code, :g, :url, :name, '1', :c1, :c2, :c3, :c4, :lv, :ord, :flag, :acc, cast(:t as varchar), cast(:k as varchar), cast(:d as varchar), cast(:h1 as varchar), cast(:h2 as varchar), cast(:h3 as varchar), cast(:tt as varchar), cast(:td as varchar))")
                .setParameter("id", id).setParameter("code", code).setParameter("g", f.categoryGroupId())
                .setParameter("url", f.categoryUrl()).setParameter("name", f.categoryName())
                .setParameter("c1", cls[0]).setParameter("c2", cls[1]).setParameter("c3", cls[2]).setParameter("c4", cls[3])
                .setParameter("lv", level).setParameter("ord", ordering)
                .setParameter("flag", f.categoryFlag() == null || f.categoryFlag().isEmpty() ? "Y" : f.categoryFlag())
                .setParameter("acc", f.accessType() == null || f.accessType().isEmpty() ? "1" : f.accessType())
                .setParameter("t", seo.title()).setParameter("k", seo.keywords()).setParameter("d", seo.description())
                .setParameter("h1", seo.headerContents1()).setParameter("h2", seo.headerContents2())
                .setParameter("h3", seo.headerContents3()).setParameter("tt", seo.themawordTitle())
                .setParameter("td", seo.themawordDescription())
                .executeUpdate();
    }

    // ---- 수정 ----

    /**
     * AS-IS updateCategory - 한 행을 고치고, 하위 카테고리의 그룹 ID를 같이 바꾼다. 카테고리 타입은 폼에 없어 AS-IS 기본값 "1"로
     * 덮이고, 접속권한은 폼 값이 안 닿으므로(AS-IS는 NOT NULL 컬럼이라 값이 있을 때만) 기존 값을 유지한다.
     */
    public void update(Form f) {
        Seo seo = f.categoriesSeo() == null ? new Seo(null, null, null, null, null, null, null, null) : f.categoriesSeo();
        em.createNativeQuery("update public.op_category set category_group_id = :g, category_url = :url, category_name = :name, "
                        + "category_type = '1', category_flag = :flag, access_type = coalesce(cast(:acc as varchar), access_type), title = cast(:t as varchar), "
                        + "keywords = cast(:k as varchar), description = cast(:d as varchar), header_contents1 = cast(:h1 as varchar), header_contents2 = cast(:h2 as varchar), header_contents3 = cast(:h3 as varchar), "
                        + "themaword_title = cast(:tt as varchar), themaword_description = cast(:td as varchar) where category_id = :id")
                .setParameter("g", f.categoryGroupId()).setParameter("url", f.categoryUrl())
                .setParameter("name", f.categoryName()).setParameter("flag", f.categoryFlag())
                .setParameter("acc", f.accessType() == null || f.accessType().isEmpty() ? null : f.accessType())
                .setParameter("t", seo.title()).setParameter("k", seo.keywords()).setParameter("d", seo.description())
                .setParameter("h1", seo.headerContents1()).setParameter("h2", seo.headerContents2())
                .setParameter("h3", seo.headerContents3()).setParameter("tt", seo.themawordTitle())
                .setParameter("td", seo.themawordDescription()).setParameter("id", f.categoryId())
                .executeUpdate();
        int level = Integer.parseInt(f.categoryLevel());
        String base = f.categoryCode().substring(0, level * 3);
        em.createNativeQuery("update public.op_category set category_group_id = :g where category_code like :p")
                .setParameter("g", f.categoryGroupId()).setParameter("p", base + "%").executeUpdate();
        resetFiltersKeepingAncestors(f.categoryId(), f.categoryCode(), level);
    }

    // ---- 삭제 ----

    /** AS-IS deleteCategoryByClass - 하위까지 전부 삭제(필터 초기화 + 상품-카테고리 매핑 삭제 포함). */
    public void delete(String categoryCode, String categoryLevel) {
        String base = categoryCode.substring(0, Integer.parseInt(categoryLevel) * 3);
        @SuppressWarnings("unchecked")
        List<Number> ids = em.createNativeQuery("select category_id from public.op_category where category_code like :p")
                .setParameter("p", base + "%").getResultList();
        List<Integer> idList = ids.stream().map(Number::intValue).toList();
        if (!idList.isEmpty()) {
            deleteFiltersOf(idList);
            em.createNativeQuery("delete from public.op_item_category where category_id in (:ids)")
                    .setParameter("ids", idList).executeUpdate();
        }
        em.createNativeQuery("delete from public.op_category where category_code like :p")
                .setParameter("p", base + "%").executeUpdate();
    }

    /**
     * AS-IS ItemFilterRepository.deleteByCategoryId + CategoriesFilterRepository.deleteByCategoryIds - 카테고리에 속한 상품들의
     * 상품 필터를 지우고(OP_ITEM_FILTER에는 CATEGORY_ID 칸이 없어 OP_ITEM_CATEGORY로 상품을 찾는다) 카테고리 필터 영역도 지운다.
     */
    private void deleteFiltersOf(List<Integer> categoryIds) {
        em.createNativeQuery("delete from public.op_item_filter where item_id in "
                        + "(select item_id from public.op_item_category where category_id in (:ids))")
                .setParameter("ids", categoryIds).executeUpdate();
        em.createNativeQuery("delete from public.op_category_filter where category_id in (:ids)")
                .setParameter("ids", categoryIds).executeUpdate();
    }

    /**
     * AS-IS CategoriesFilterServiceImpl.deleteCategoriesFilterByCategoryId(수정 시 saveCategoriesFilter가 부른다) - 폼에 필터 영역이
     * 없어 선택된 필터 그룹이 항상 비어 있으므로 이 카테고리의 필터를 모두 지우되, 상위(브레드크럼) 카테고리가 쓰는 필터 그룹의
     * 상품 필터는 남긴다.
     */
    private void resetFiltersKeepingAncestors(int categoryId, String categoryCode, int level) {
        List<Integer> ancestorIds = new java.util.ArrayList<>();
        for (int k = 1; k < level; k++) {
            String ancestorCode = (categoryCode.substring(0, k * 3) + "000000000").substring(0, 12);
            @SuppressWarnings("unchecked")
            List<Number> rows = em.createNativeQuery("select category_id from public.op_category where category_code = :c")
                    .setParameter("c", ancestorCode).getResultList();
            rows.forEach(r -> ancestorIds.add(r.intValue()));
        }
        List<Long> keep = new java.util.ArrayList<>();
        if (!ancestorIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Number> groups = em.createNativeQuery("select distinct filter_group_id from public.op_category_filter where category_id in (:ids)")
                    .setParameter("ids", ancestorIds).getResultList();
            groups.forEach(g -> keep.add(g.longValue()));
        }
        String sql = "delete from public.op_item_filter where item_id in "
                + "(select item_id from public.op_item_category where category_id in (:ids))"
                + (keep.isEmpty() ? "" : " and filter_group_id not in (:keep)");
        Query q = em.createNativeQuery(sql).setParameter("ids", List.of(categoryId));
        if (!keep.isEmpty()) {
            q.setParameter("keep", keep);
        }
        q.executeUpdate();
        em.createNativeQuery("delete from public.op_category_filter where category_id = :id")
                .setParameter("id", categoryId).executeUpdate();
    }

    // ---- 이동(jsTree 드래그앤드롭) ----

    public record Move(String categoryGroupId, String parentCode, String parentLevel, String parentGroupId,
                       String currentCode, String currentLevel, String previousCode, String previousLevel,
                       String previousOrdering) {
    }

    /** AS-IS updateCategoryPosition. 이동한 카테고리와 그 하위의 코드·레벨·클래스·그룹을 새 위치에 맞게 다시 매기고 필터를 초기화한다. */
    public void move(Move m) {
        String orgCode = (nz(m.currentCode()) + "000000000").substring(0, 12);
        Category org = byCode(orgCode);

        int newLevel = Integer.parseInt(m.parentLevel()) + 1;
        int previousOrdering = m.previousOrdering() == null || m.previousOrdering().isEmpty() ? 0 : Integer.parseInt(m.previousOrdering());
        String parentCode = nz(m.parentCode());
        String groupId = nz(m.categoryGroupId());

        // 1. 새 부모 아래 같은 레벨에서 직전 형제보다 뒤에 있는 것들의 ORDERING을 +1
        StringBuilder bump = new StringBuilder("update public.op_category set ordering = ordering + 1 where category_level = :lv");
        if (!parentCode.isEmpty()) {
            bump.append(" and category_code like :pp");
        }
        if (!groupId.isEmpty()) {
            bump.append(" and category_group_id = :g");
        }
        if (previousOrdering > 0) {
            bump.append(" and ordering > :po");
        }
        Query bq = em.createNativeQuery(bump.toString()).setParameter("lv", String.valueOf(newLevel));
        if (!parentCode.isEmpty()) {
            bq.setParameter("pp", parentCode + "%");
        }
        if (!groupId.isEmpty()) {
            bq.setParameter("g", Integer.parseInt(groupId));
        }
        if (previousOrdering > 0) {
            bq.setParameter("po", previousOrdering);
        }
        bq.executeUpdate();

        // 2-1. 새 부모 아래에서 비어 있는 클래스 번호(1차는 100부터, 그 외 001부터)
        String classCol = "category_class" + newLevel;
        StringBuilder usedSql = new StringBuilder("select " + classCol + " from public.op_category where category_level = :lv");
        if (!parentCode.isEmpty()) {
            usedSql.append(" and category_code like :pp");
        }
        usedSql.append(" group by ").append(classCol).append(" order by ").append(classCol).append(" asc");
        Query uq = em.createNativeQuery(usedSql.toString()).setParameter("lv", String.valueOf(newLevel));
        if (!parentCode.isEmpty()) {
            uq.setParameter("pp", parentCode + "%");
        }
        @SuppressWarnings("unchecked")
        List<String> used = uq.getResultList();
        String newClass = "";
        for (int i = (newLevel == 1 ? 100 : 1); i < 999; i++) {
            newClass = String.format("%03d", i);
            if (!used.contains(newClass)) {
                break;
            }
        }

        String newCodeBase = parentCode + newClass;
        String newCode = (newCodeBase + "000000000").substring(0, 12);
        String[] newClasses = classesOf(newCode);
        int newOrdering = previousOrdering + 1;
        String newGroup = newLevel == 1 ? groupId : nz(m.parentGroupId());

        // 이동하는 카테고리 자신(코드가 currentCode로 시작하는 같은 레벨 행) 갱신
        StringBuilder upd = new StringBuilder("update public.op_category set category_code = :nc, category_class1 = :c1, "
                + "category_class2 = :c2, category_class3 = :c3, category_class4 = :c4, category_level = :lv, ordering = :ord");
        if (!newGroup.isEmpty()) {
            upd.append(", category_group_id = :g");
        }
        upd.append(" where category_code like :cc and category_level = :cl");
        Query uu = em.createNativeQuery(upd.toString()).setParameter("nc", newCode)
                .setParameter("c1", newClasses[0]).setParameter("c2", newClasses[1])
                .setParameter("c3", newClasses[2]).setParameter("c4", newClasses[3])
                .setParameter("lv", String.valueOf(newLevel)).setParameter("ord", newOrdering)
                .setParameter("cc", m.currentCode() + "%").setParameter("cl", m.currentLevel());
        if (!newGroup.isEmpty()) {
            uu.setParameter("g", Integer.parseInt(newGroup));
        }
        // ★이 UPDATE가 자식의 코드까지 바꾸기 전에 자식 목록을 먼저 읽어 둔다(AS-IS는 UPDATE 뒤에 읽는데 자식은 레벨이
        //   currentLevel보다 커서 위 UPDATE의 조건(category_level = currentLevel)에 걸리지 않으므로 순서와 무관하다)
        uu.executeUpdate();

        // 3. 자식 카테고리들 - 코드 접미사를 유지한 채 새 코드/클래스/레벨(+그룹)로
        @SuppressWarnings("unchecked")
        List<Object[]> children = em.createNativeQuery("select category_id, category_code, category_level from public.op_category "
                        + "where category_code like :cc and category_level > :cl")
                .setParameter("cc", m.currentCode() + "%").setParameter("cl", m.currentLevel()).getResultList();
        int diff = newLevel - Integer.parseInt(m.currentLevel());
        for (Object[] c : children) {
            String oldSuffix = s(c[1]).substring(m.currentCode().length(), 12);
            String newChildCode = (newCodeBase + oldSuffix + "000000000").substring(0, 12);
            String[] cc = classesOf(newChildCode);
            int newChildLevel = Integer.parseInt(s(c[2])) + diff;
            StringBuilder cu = new StringBuilder("update public.op_category set category_code = :nc, category_class1 = :c1, "
                    + "category_class2 = :c2, category_class3 = :c3, category_class4 = :c4, category_level = :lv");
            if (!newGroup.isEmpty()) {
                cu.append(", category_group_id = :g");
            }
            cu.append(" where category_id = :id");
            Query cq = em.createNativeQuery(cu.toString()).setParameter("nc", newChildCode)
                    .setParameter("c1", cc[0]).setParameter("c2", cc[1]).setParameter("c3", cc[2]).setParameter("c4", cc[3])
                    .setParameter("lv", String.valueOf(newChildLevel)).setParameter("id", i(c[0]));
            if (!newGroup.isEmpty()) {
                cq.setParameter("g", Integer.parseInt(newGroup));
            }
            cq.executeUpdate();
        }

        // 필터 초기화(원래 카테고리 + 하위)
        List<Integer> ids = new ArrayList<>();
        if (org != null) {
            ids.add(org.categoryId());
        }
        for (Object[] c : children) {
            ids.add(i(c[0]));
        }
        if (!ids.isEmpty()) {
            deleteFiltersOf(ids);
        }
    }

    // ---- 도우미 ----

    private static String[] classesOf(String code) {
        String c = (code + "000000000").substring(0, 12);
        return new String[]{c.substring(0, 3), c.substring(3, 6), c.substring(6, 9), c.substring(9, 12)};
    }

    private static Object singleOrNull(List<?> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String s(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer i(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }

    private static long l(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}

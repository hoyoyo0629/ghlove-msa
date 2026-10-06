package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 관리자매뉴얼 (메뉴 5201) - AS-IS {@code manual-mapper.xml}의
 * {@code getAllMenuListDept2}/{@code getAllMenuListDept3}/{@code getOpMenu}/
 * {@code updateManagerManual}/{@code managerDeleteManualFile} 이식.
 *
 * <p><b>★ 이 화면은 별도 표가 없다</b>. 로그인한 운영자가 볼 수 있는 메뉴 트리를 2단(부모)으로
 * 묶어 그리고, 3단 메뉴 각각에 <b>{@code op_menu}의 첨부파일 컬럼</b>
 * ({@code file_nm}·{@code orginl_file_nm})을 직접 붙인다. 그래서 "관리자매뉴얼 등록"은
 * {@code op_menu} UPDATE 한 번이다.
 *
 * <p><b>PostgreSQL 적응 1건</b>: AS-IS 2단 SQL은 {@code group by menu_parent_id} 상태에서
 * {@code order by m.menu_seq, m.menu_id}를 쓴다(CUBRID는 허용, PostgreSQL은 오류).
 * 집계로 바꿨다 - 정렬 1순위가 {@code menu_parent_id}라 결과 순서는 같다.
 */
@Repository
@RequiredArgsConstructor
public class ManagerManualRepository {

    private final EntityManager entityManager;

    /** 2단(부모) 묶음 - 표 하나가 된다. */
    public record Dept2(Integer menuId, String menuName) {
    }

    /** 3단(리프) 한 줄 - 메뉴명과 붙어 있는 매뉴얼 파일. */
    public record Dept3(Integer menuId, String menuName, String fileNm, String orginlFileNm) {
    }

    @SuppressWarnings("unchecked")
    public List<Dept2> dept2(String authority) {
        Query query = entityManager.createNativeQuery("""
                select m.menu_parent_id as menu_id,
                       (select p.menu_name from admin.op_menu p where p.menu_id = m.menu_parent_id) as menu_name
                  from admin.op_menu m
                  join admin.op_menu_right mr on m.menu_id = mr.menu_id
                 where m.status_code = '1'
                   and m.display_flag = 'Y'
                   and mr.authority = :authority
                 group by m.menu_parent_id
                 order by m.menu_parent_id asc, min(m.menu_seq) asc, min(m.menu_id) asc
                """);
        query.setParameter("authority", authority);
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(r -> new Dept2(
                r[0] == null ? null : ((Number) r[0]).intValue(),
                (String) r[1])).toList();
    }

    @SuppressWarnings("unchecked")
    public List<Dept3> dept3(String authority, Integer parentMenuId) {
        Query query = entityManager.createNativeQuery("""
                select m.menu_id, m.menu_name, m.file_nm, m.orginl_file_nm
                  from admin.op_menu_right mr
                  join admin.op_menu m on mr.menu_id = m.menu_id
                 where m.menu_type = 3
                   and m.status_code = '1'
                   and m.display_flag = 'Y'
                   and mr.authority = :authority
                   and m.menu_parent_id = :parentMenuId
                 order by m.menu_parent_id asc, m.menu_seq asc, m.menu_id asc
                """);
        query.setParameter("authority", authority);
        query.setParameter("parentMenuId", parentMenuId);
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(r -> new Dept3(
                r[0] == null ? null : ((Number) r[0]).intValue(),
                (String) r[1], (String) r[2], (String) r[3])).toList();
    }

    /** AS-IS getOpMenu - 팝업이 보여줄 메뉴 한 건과 붙어 있는 파일. */
    @SuppressWarnings("unchecked")
    public Dept3 menu(Integer menuId) {
        Query query = entityManager.createNativeQuery("""
                select m.menu_id, m.menu_name, m.file_nm, m.orginl_file_nm
                  from admin.op_menu m
                 where m.menu_id = :menuId
                """);
        query.setParameter("menuId", menuId);
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        return new Dept3(r[0] == null ? null : ((Number) r[0]).intValue(),
                (String) r[1], (String) r[2], (String) r[3]);
    }
}

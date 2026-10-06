package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자매뉴얼 목록 (메뉴 5202) - AS-IS {@code manual-mapper.xml}의
 * {@code getManualCount}/{@code getManualList} 이식.
 *
 * <p><b>AS-IS SQL 그대로</b>:
 * <ul>
 *   <li>{@code g_mnl}과 공통코드 {@code MENU_URL}을 <b>내부조인</b>하고 {@code use_yn='Y'}를 건다 -
 *       그래서 <b>쓰지 않는 코드로 등록된 매뉴얼은 목록에서 사라진다</b>(AS-IS 그대로 둔다).</li>
 *   <li>'페이지 구분' 칸은 코드의 {@code detail}(화면명)이다.</li>
 *   <li>검색구분 PAGE(화면명) / SUBJECT(제목) / CONTENT(내용) - 전부 부분일치.</li>
 *   <li>등록일 범위는 {@code frst_regist_pnttm}을 {@code yyyyMMdd}로 바꿔 BETWEEN 한다
 *       (두 값이 <b>모두</b> 있어야 조건이 걸린다).</li>
 *   <li>작성자는 {@code op_manager.user_name}을 스칼라 서브쿼리로 뽑는다.</li>
 *   <li>정렬은 {@code frst_regist_pnttm DESC} 고정이다.</li>
 * </ul>
 *
 * <p>조건은 AS-IS 매퍼의 {@code <if>}처럼 <b>있을 때만 덧붙인다</b>(이 프로젝트의 기존 관례).
 */
@Repository
@RequiredArgsConstructor
public class MnlAdminRepository {

    private final EntityManager entityManager;

    /** 목록 한 행 - AS-IS getManualList가 내려주는 칸 그대로. */
    public record Row(Integer mnlSn, String pageGbn, String menuSj, Long frstRegisterId,
                      String userName, Integer inqireCo, String frstRegistPnttm) {
    }

    public int count(String where, String query, String startCreateDate, String endCreateDate) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*)");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> nativeQuery.setParameter((String) bind[0], bind[1]));
        return ((Number) nativeQuery.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Row> list(String where, String query, String startCreateDate, String endCreateDate,
                          int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select m.mnl_sn,
                       c.detail as page_gbn,
                       m.menu_sj,
                       m.frst_register_id,
                       (select mgr.user_name from admin.op_manager mgr where mgr.user_id = m.frst_register_id) as user_name,
                       m.inqire_co,
                       m.frst_regist_pnttm
                """);
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate);
        sql.append(" order by m.frst_regist_pnttm desc offset :offset limit :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> nativeQuery.setParameter((String) bind[0], bind[1]));
        nativeQuery.setParameter("offset", offset);
        nativeQuery.setParameter("limit", limit);

        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream().map(r -> new Row(
                r[0] == null ? null : ((Number) r[0]).intValue(),
                (String) r[1],
                (String) r[2],
                r[3] == null ? null : ((Number) r[3]).longValue(),
                (String) r[4],
                r[5] == null ? null : ((Number) r[5]).intValue(),
                // AS-IS는 SQL에서 yyyy-MM-dd로 포맷해 내려준다
                r[6] == null ? null : ((Timestamp) r[6]).toLocalDateTime().toLocalDate().toString()
        )).toList();
    }

    private static void appendFromWhere(StringBuilder sql, List<Object[]> binds, String where,
                                        String query, String startCreateDate, String endCreateDate) {
        sql.append("""
                  from admin.g_mnl m
                  join admin.op_common_code c
                    on m.menu_se_code = c.id and c.code_type = 'MENU_URL' and c.use_yn = 'Y'
                 where 1 = 1
                """);

        if (query != null && !query.isBlank()) {
            String column = switch (where == null ? "" : where) {
                case "PAGE" -> "c.detail";
                case "SUBJECT" -> "m.menu_sj";
                case "CONTENT" -> "m.menu_cn";
                default -> null;
            };
            if (column != null) {
                sql.append(" and ").append(column).append(" like :likeQuery");
                binds.add(new Object[]{"likeQuery", "%" + query.trim() + "%"});
            }
        }

        if (startCreateDate != null && !startCreateDate.isBlank()
                && endCreateDate != null && !endCreateDate.isBlank()) {
            sql.append(" and to_char(m.frst_regist_pnttm, 'YYYYMMDD') between :startCreateDate and :endCreateDate");
            binds.add(new Object[]{"startCreateDate", startCreateDate});
            binds.add(new Object[]{"endCreateDate", endCreateDate});
        }
    }
}

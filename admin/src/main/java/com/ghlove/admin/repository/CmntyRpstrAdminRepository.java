package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 자료실(메뉴 11402) 목록·상세 조회 - AS-IS {@code countRpstr}/{@code listRpstr}/
 * {@code getRpstrDetail}/{@code updateRpstrInqCnt} 이식(조회 전용).
 *
 * <p>검색조건(지자체·등록일·소속·제목)과 권한 치환은 소통방과 같다 -
 * {@link CmntyBbsAdminRepository} 주석의 "AS-IS 조건의 TO-BE 번역 2건" 참고.
 *
 * <p><b>다른 게시판과 달라지는 것</b>: 댓글도 비밀글도 없고(표에 컬럼 자체가 없다), 제목 옆
 * 아이콘은 <b>첨부가 있는지만</b> 본다.
 *
 * <p><b>AS-IS 결함 - 고쳤다</b>: AS-IS 목록은 첨부 유무를
 * {@code (select orgnl_atch_file_nm from g_cmnty_file where rpstr_id = ... and use_yn='Y')}
 * 스칼라 서브쿼리로 뽑아 <b>첨부가 2건 이상이면 SQL이 실패</b>한다(화면이 깨진다. SR 상세의
 * 파일 크기와 같은 유형이다). 여기서는 개수를 세어 아이콘 판정에 쓴다 - 화면 첨부는 1개 제한이라
 * 보이는 결과는 같다.
 */
@Repository
@RequiredArgsConstructor
public class CmntyRpstrAdminRepository {

    private static final List<String> ROLES_SYSTEM = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");
    private static final List<String> ROLES_MOIS = List.of("ROLE_ADMIN_3", "ROLE_ADMIN_4");
    private static final List<String> ROLES_LOCGOV = List.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10");

    private final EntityManager entityManager;

    public record Row(Long rpstrId, String rpstrTtl, String noticeYn, Long inqCnt, Long frstCrtId,
                      LocalDateTime frstCrtDt, String userName, String authority, String locgovCode,
                      Long attachedFileCnt) {
    }

    public record DetailRow(Long rpstrId, String rpstrTtl, String rpstrCn, String useYn,
                            String noticeYn, Long inqCnt, Long frstCrtId, LocalDateTime frstCrtDt,
                            LocalDateTime lastMdfcnDt, String userName, String authority,
                            String locgovCode) {
    }

    /** AS-IS countRpstr. */
    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                  from admin.g_cmnty_rpstr a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS listRpstr - 공지 우선, 등록일시 역순. */
    @SuppressWarnings("unchecked")
    public List<Row> list(String locgovCode, String startDt, String endDt, String searchRole,
                          String where, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.rpstr_id, a.rpstr_ttl, a.notice_yn, coalesce(a.inq_cnt, 0), a.frst_crt_id,
                       a.frst_crt_dt, b.user_name, b.authority, b.locgov_code,
                       (select count(*) from admin.g_cmnty_file f
                         where f.use_yn = 'Y' and f.rpstr_id = a.rpstr_id)
                  from admin.g_cmnty_rpstr a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query);
        sql.append(" order by coalesce(a.notice_yn, 'N') desc, a.frst_crt_dt desc offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        List<Row> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new Row(num(r[0]), str(r[1]), str(r[2]), num(r[3]), num(r[4]),
                    dt(r[5]), str(r[6]), str(r[7]), str(r[8]), num(r[9])));
        }
        return result;
    }

    /** AS-IS getRpstrDetail - AS-IS에는 {@code use_yn} 조건이 없어 여기서 걸었다(다른 게시판과 동일). */
    @SuppressWarnings("unchecked")
    public DetailRow detail(long rpstrId) {
        Query q = entityManager.createNativeQuery("""
                select a.rpstr_id, a.rpstr_ttl, a.rpstr_cn, a.use_yn, a.notice_yn,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code
                  from admin.g_cmnty_rpstr a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.rpstr_id = :rpstrId and a.use_yn = 'Y'
                """);
        q.setParameter("rpstrId", rpstrId);
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        return new DetailRow(num(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), num(r[5]),
                num(r[6]), dt(r[7]), dt(r[8]), str(r[9]), str(r[10]),
                str(r[11]));
    }

    /** AS-IS updateRpstrInqCnt. */
    @Transactional
    public void updateInqCnt(long rpstrId) {
        Query q = entityManager.createNativeQuery("""
                update admin.g_cmnty_rpstr set inq_cnt = coalesce(inq_cnt, 0) + 1
                 where rpstr_id = :rpstrId
                """);
        q.setParameter("rpstrId", rpstrId);
        q.executeUpdate();
    }

    private static void appendWhere(StringBuilder sql, List<Object[]> binds, String locgovCode,
                                    String startDt, String endDt, String searchRole,
                                    String where, String query) {
        sql.append(" where a.use_yn = 'Y'");
        if (notBlank(locgovCode)) {
            sql.append(" and b.locgov_code = :locgovCode");
            binds.add(new Object[]{"locgovCode", locgovCode});
        }
        if (notBlank(startDt) && notBlank(endDt)) {
            sql.append(" and to_char(a.frst_crt_dt, 'YYYYMMDD') between :startDt and :endDt");
            binds.add(new Object[]{"startDt", startDt});
            binds.add(new Object[]{"endDt", endDt});
        }
        List<String> roles = rolesOf(searchRole);
        if (roles != null) {
            sql.append(" and b.authority in (:roles)");
            binds.add(new Object[]{"roles", roles});
        }
        if (notBlank(query)) {
            if ("SUBJECT".equals(where)) {
                sql.append(" and a.rpstr_ttl like :likeQuery");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
            } else if ("USERNAME".equals(where)) {
                sql.append(" and b.user_name = :query");
                binds.add(new Object[]{"query", query});
            } else {
                sql.append(" and (a.rpstr_ttl like :likeQuery or b.user_name = :query)");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                binds.add(new Object[]{"query", query});
            }
        }
    }

    private static List<String> rolesOf(String searchRole) {
        if (searchRole == null) {
            return null;
        }
        return switch (searchRole) {
            case "ROLE_ADMIN_1" -> ROLES_SYSTEM;
            case "ROLE_ADMIN_3" -> ROLES_MOIS;
            case "ROLE_ADMIN_5" -> ROLES_LOCGOV;
            default -> null;
        };
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Long num(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    /**
     * 네이티브 쿼리의 timestamp 컬럼은 JDBC 드라이버가 {@link Timestamp}로 준다
     * (엔티티 매핑과 달리 변환이 끼지 않는다). {@code (LocalDateTime)}로 바로 캐스팅하면
     * <b>행이 있을 때만</b> ClassCastException이 나기 때문에, 표가 0건이던 이식 시점에는
     * 드러나지 않았다(2026-10-06 실데이터가 생기자 목록 화면 500). num/str과 같은 방어 변환.
     */
    private static LocalDateTime dt(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) value;
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }
}

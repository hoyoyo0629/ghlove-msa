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
 * 담당자용 FAQ(메뉴 11405) 목록·상세·댓글 조회 - AS-IS {@code countFaqBbs}/{@code listFaqBbs}/
 * {@code getFaqBbsDetail}/{@code selectFaqBbsCmntList}/{@code updateFaqBbsInqCnt} 이식(조회 전용).
 *
 * <p><b>SR 게시판(11404)과 달라지는 것</b>: 게시글에 <b>질문유형({@code FAQ_TYPE})</b>이 있고
 * 검색조건에 질문유형 탭({@code shFaqType}, 정확히 일치)이 하나 더 붙는다. 그 밖의 검색조건
 * (지자체·등록일·소속·제목)과 권한 치환은 SR과 같다 - {@link CmntyBbsAdminRepository} 주석 참고.
 */
@Repository
@RequiredArgsConstructor
public class CmntyFaqBbsAdminRepository {

    private static final List<String> ROLES_SYSTEM = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");
    private static final List<String> ROLES_MOIS = List.of("ROLE_ADMIN_3", "ROLE_ADMIN_4");
    private static final List<String> ROLES_LOCGOV = List.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10");

    private final EntityManager entityManager;

    public record Row(Long bbsId, String bbsTtl, String faqType, String noticeYn, String isSecret,
                      Long inqCnt, Long frstCrtId, LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt,
                      String userName, String authority, String locgovCode, Long cmntCnt,
                      Long attachedFileCnt) {
    }

    public record DetailRow(Long bbsId, String bbsTtl, String bbsCn, String faqType, String useYn,
                            String noticeYn, String isSecret, Long inqCnt, Long frstCrtId,
                            LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt, String userName,
                            String authority, String locgovCode) {
    }

    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String locgovCode, Long frstCrtId, LocalDateTime frstCrtDt) {
    }

    /** AS-IS countFaqBbs. */
    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query, String shFaqType) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                  from admin.g_cmnty_faq_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query, shFaqType);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS listFaqBbs - 공지 우선, 등록일시 역순. */
    @SuppressWarnings("unchecked")
    public List<Row> list(String locgovCode, String startDt, String endDt, String searchRole,
                          String where, String query, String shFaqType, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.bbs_id, a.bbs_ttl, a.faq_type, a.notice_yn, a.is_secret,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code,
                       (select count(*) from admin.g_cmnty_faq_bbs_cmnt c
                         where c.use_yn = 'Y' and c.bbs_id = a.bbs_id),
                       (select count(*) from admin.g_cmnty_faq_bbs_file f
                         where f.use_yn = 'Y' and f.bbs_id = a.bbs_id)
                  from admin.g_cmnty_faq_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query, shFaqType);
        sql.append(" order by coalesce(a.notice_yn, 'N') desc, a.frst_crt_dt desc offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        List<Row> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new Row(num(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), num(r[5]),
                    num(r[6]), dt(r[7]), dt(r[8]), str(r[9]), str(r[10]),
                    str(r[11]), num(r[12]), num(r[13])));
        }
        return result;
    }

    /** AS-IS getFaqBbsDetail - SR과 같이 AS-IS에는 {@code use_yn} 조건이 없어 여기서 걸었다. */
    @SuppressWarnings("unchecked")
    public DetailRow detail(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.bbs_id, a.bbs_ttl, a.bbs_cn, a.faq_type, a.use_yn, a.notice_yn, a.is_secret,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code
                  from admin.g_cmnty_faq_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.bbs_id = :bbsId and a.use_yn = 'Y'
                """);
        q.setParameter("bbsId", bbsId);
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        return new DetailRow(num(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]),
                str(r[6]), num(r[7]), num(r[8]), dt(r[9]), dt(r[10]),
                str(r[11]), str(r[12]), str(r[13]));
    }

    /** AS-IS selectFaqBbsCmntList - 등록일시 오름차순. */
    @SuppressWarnings("unchecked")
    public List<CmntRow> cmntList(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.cmnt_id, a.bbs_id, a.cmnt_cn, b.user_name, b.authority, b.locgov_code,
                       a.frst_crt_id, a.frst_crt_dt
                  from admin.g_cmnty_faq_bbs_cmnt a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.use_yn = 'Y' and a.bbs_id = :bbsId
                 order by a.frst_crt_dt asc
                """);
        q.setParameter("bbsId", bbsId);
        List<Object[]> rows = q.getResultList();
        List<CmntRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new CmntRow(num(r[0]), num(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]),
                    num(r[6]), dt(r[7])));
        }
        return result;
    }

    /** AS-IS updateFaqBbsInqCnt. */
    @Transactional
    public void updateInqCnt(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                update admin.g_cmnty_faq_bbs set inq_cnt = coalesce(inq_cnt, 0) + 1 where bbs_id = :bbsId
                """);
        q.setParameter("bbsId", bbsId);
        q.executeUpdate();
    }

    private static void appendWhere(StringBuilder sql, List<Object[]> binds, String locgovCode,
                                    String startDt, String endDt, String searchRole,
                                    String where, String query, String shFaqType) {
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
                sql.append(" and a.bbs_ttl like :likeQuery");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
            } else if ("USERNAME".equals(where)) {
                sql.append(" and b.user_name = :query");
                binds.add(new Object[]{"query", query});
            } else {
                sql.append(" and (a.bbs_ttl like :likeQuery or b.user_name = :query)");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                binds.add(new Object[]{"query", query});
            }
        }
        // AS-IS listFaqBbs - 질문유형 탭. 검색어 조건 뒤에 붙는다(순서까지 그대로).
        if (notBlank(shFaqType)) {
            sql.append(" and a.faq_type = :shFaqType");
            binds.add(new Object[]{"shFaqType", shFaqType});
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

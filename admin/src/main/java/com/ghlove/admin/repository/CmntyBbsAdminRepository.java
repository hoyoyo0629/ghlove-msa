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
 * 소통방(메뉴 11401) 목록·상세·댓글 조회 - AS-IS {@code cmnty-mapper.xml}의
 * {@code countBbs}/{@code listBbs}/{@code detailBbs}/{@code bbsCmntList}/{@code updateInqCnt}를
 * 옮긴 <b>조회 전용</b> 레포지토리다. 쓰기는 JPA({@link CmntyBbsRepository}·
 * {@link CmntyCmntRepository})로 한다.
 *
 * <p><b>AS-IS 조건의 TO-BE 번역 2건</b>:
 * <ul>
 *   <li>작성자 권한 - AS-IS는 별도 표({@code op_user_role})에 사용자별 권한이 여러 행이라
 *       {@code (select min(authority) from op_user_role where user_id = ...)}로 하나를 고른다.
 *       TO-BE는 {@code op_manager.authority} 한 컬럼이므로 그 값을 그대로 쓴다 - 담당자
 *       사용여부 9/2를 ACTIVE/LOCKED로 바꾼 것과 같은 <b>경계에서의 코드체계 번역</b>이다.
 *       소속 검색({@code searchRole})도 같은 치환이다.</li>
 *   <li>작성자 소속 지자체명 - AS-IS는 {@code g_locgov}를 조인해 상위지자체명·지자체명을
 *       SQL에서 붙이는데, TO-BE에서 {@code g_locgov}는 <b>donation 서비스 소유</b>다.
 *       그래서 여기서는 작성자의 {@code locgov_code}만 내려주고 이름은 서비스가
 *       {@code LocgovClient}로 채운다(4402·19101과 같은 방식, 보이는 결과는 같다).
 *       AS-IS는 {@code g_locgov}에 없는 지자체코드를 가진 작성자를 지자체 검색에서
 *       제외하지만, 실데이터에서 그런 행은 없다.</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class CmntyBbsAdminRepository {

    /** AS-IS 소속 검색값 → 실제로 걸러낼 권한들(mapper의 searchRole 분기 그대로). */
    private static final List<String> ROLES_SYSTEM = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");
    private static final List<String> ROLES_MOIS = List.of("ROLE_ADMIN_3", "ROLE_ADMIN_4");
    private static final List<String> ROLES_LOCGOV = List.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10");

    private final EntityManager entityManager;

    /**
     * 목록 한 행. {@code locgovCode}는 작성자의 지자체코드이고, 소속 표기(상위지자체명+지자체명)는
     * 서비스가 채운다. {@code cmntCnt}는 AS-IS와 같이 살아있는 댓글({@code use_yn='Y'}) 수다.
     */
    public record Row(Long bbsId, String bbsTtl, String bbsCn, String useYn, String noticeYn, Long inqCnt,
                      Long frstCrtId, LocalDateTime frstCrtDt, Long lastMdfcnId, LocalDateTime lastMdfcnDt,
                      String userName, String authority, String locgovCode, Long cmntCnt) {
    }

    /** 상세 한 행 - 목록 행에 비밀글 여부가 더 붙는다(AS-IS detailBbs가 is_secret을 같이 뽑는다). */
    public record DetailRow(Long bbsId, String bbsTtl, String bbsCn, String useYn, String noticeYn,
                            String isSecret, Long inqCnt, Long frstCrtId, LocalDateTime frstCrtDt,
                            Long lastMdfcnId, LocalDateTime lastMdfcnDt, String userName, String authority,
                            String locgovCode) {
    }

    /** 댓글 한 행 - AS-IS bbsCmntList. 소속 표기는 목록과 마찬가지로 서비스가 채운다. */
    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String useYn, String userName,
                          String authority, String locgovCode, Long frstCrtId, LocalDateTime frstCrtDt,
                          Long lastMdfcnId, LocalDateTime lastMdfcnDt) {
    }

    /** AS-IS countBbs. */
    public int countBbs(String locgovCode, String startDt, String endDt, String searchRole,
                        String where, String query) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                  from admin.g_cmnty_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS listBbs - 정렬은 공지 우선, 그다음 등록일시 역순이다. */
    @SuppressWarnings("unchecked")
    public List<Row> listBbs(String locgovCode, String startDt, String endDt, String searchRole,
                             String where, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.bbs_id, a.bbs_ttl, a.bbs_cn, a.use_yn, a.notice_yn, coalesce(a.inq_cnt, 0),
                       a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_id, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code,
                       (select count(*) from admin.g_cmnty_cmnt c
                         where c.use_yn = 'Y' and c.bbs_id = a.bbs_id)
                  from admin.g_cmnty_bbs a
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
            result.add(new Row(num(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), num(r[5]),
                    num(r[6]), dt(r[7]), num(r[8]), dt(r[9]),
                    str(r[10]), str(r[11]), str(r[12]), num(r[13])));
        }
        return result;
    }

    /** AS-IS detailBbs - {@code use_yn='Y'}인 글만 찾는다(삭제된 글은 없는 것으로 본다). */
    @SuppressWarnings("unchecked")
    public DetailRow detailBbs(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.bbs_id, a.bbs_ttl, a.bbs_cn, a.use_yn, a.notice_yn, a.is_secret,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_id,
                       a.last_mdfcn_dt, b.user_name, b.authority, b.locgov_code
                  from admin.g_cmnty_bbs a
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
                num(r[6]), num(r[7]), dt(r[8]), num(r[9]), dt(r[10]),
                str(r[11]), str(r[12]), str(r[13]));
    }

    /** AS-IS bbsCmntList - 등록일시 역순. */
    @SuppressWarnings("unchecked")
    public List<CmntRow> bbsCmntList(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.cmnt_id, a.bbs_id, a.cmnt_cn, a.use_yn, b.user_name, b.authority,
                       b.locgov_code, a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_id, a.last_mdfcn_dt
                  from admin.g_cmnty_cmnt a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.use_yn = 'Y' and a.bbs_id = :bbsId
                 order by a.frst_crt_dt desc
                """);
        q.setParameter("bbsId", bbsId);
        List<Object[]> rows = q.getResultList();
        List<CmntRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new CmntRow(num(r[0]), num(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]),
                    str(r[6]), num(r[7]), dt(r[8]), num(r[9]), dt(r[10])));
        }
        return result;
    }

    /** AS-IS updateInqCnt - 상세·수정 화면 진입마다 조회수를 1 올린다. */
    @Transactional
    public void updateInqCnt(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                update admin.g_cmnty_bbs set inq_cnt = coalesce(inq_cnt, 0) + 1 where bbs_id = :bbsId
                """);
        q.setParameter("bbsId", bbsId);
        q.executeUpdate();
    }

    /**
     * AS-IS countBbs/listBbs가 공유하는 WHERE 그대로 - 지자체·등록일범위·소속·검색어이고
     * 마지막에 항상 {@code use_yn='Y'}가 붙는다.
     */
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
    }

    /** AS-IS mapper의 searchRole 분기 - 값 하나가 권한 묶음 하나를 뜻한다. */
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

    /** PostgreSQL의 bpchar가 Character로 올 수 있어 문자열화는 toString으로 한다. */

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

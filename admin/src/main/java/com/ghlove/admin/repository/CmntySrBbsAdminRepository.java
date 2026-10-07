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
 * SR 게시판(메뉴 11404) 목록·상세·댓글 조회 - AS-IS {@code cmnty-mapper.xml}의
 * {@code countSrBbs}/{@code listSrBbs}/{@code getSrBbsDetail}/{@code selectSrBbsCmntList}/
 * {@code updateSrBbsInqCnt} 이식(조회 전용). 쓰기는 JPA 레포지토리로 한다.
 *
 * <p>검색조건·권한 치환은 소통방과 같다 - {@link CmntyBbsAdminRepository} 클래스 주석의
 * "AS-IS 조건의 TO-BE 번역 2건"(op_user_role → op_manager.authority / g_locgov는 donation 소유라
 * 코드만 내려주고 서비스가 이름을 채움)이 그대로 적용된다.
 *
 * <p>소통방과 달라지는 것: 목록에 <b>비밀글 여부</b>와 <b>첨부파일 개수</b>가 더 붙고(제목 옆
 * 자물쇠·클립 아이콘), 댓글 정렬이 <b>등록일시 오름차순</b>이다(소통방은 내림차순).
 */
@Repository
@RequiredArgsConstructor
public class CmntySrBbsAdminRepository {

    /** AS-IS 소속 검색값 → 걸러낼 권한들(mapper의 searchRole 분기 그대로). */
    private static final List<String> ROLES_SYSTEM = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");
    private static final List<String> ROLES_MOIS = List.of("ROLE_ADMIN_3", "ROLE_ADMIN_4");
    private static final List<String> ROLES_LOCGOV = List.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10");

    private final EntityManager entityManager;

    /** 목록 한 행. */
    public record Row(Long bbsId, String bbsTtl, String noticeYn, String isSecret, Long inqCnt,
                      Long frstCrtId, LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt,
                      String userName, String authority, String locgovCode, Long cmntCnt,
                      Long attachedFileCnt) {
    }

    /** 상세 한 행. */
    public record DetailRow(Long bbsId, String bbsTtl, String bbsCn, String useYn, String noticeYn,
                            String isSecret, Long inqCnt, Long frstCrtId, LocalDateTime frstCrtDt,
                            LocalDateTime lastMdfcnDt, String userName, String authority,
                            String locgovCode) {
    }

    /** 댓글 한 행 - 첨부파일은 서비스가 따로 묶어 붙인다. */
    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String locgovCode, Long frstCrtId, LocalDateTime frstCrtDt) {
    }

    /** AS-IS countSrBbs. */
    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                  from admin.g_cmnty_sr_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, locgovCode, startDt, endDt, searchRole, where, query);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS listSrBbs - 공지 우선, 등록일시 역순. */
    @SuppressWarnings("unchecked")
    public List<Row> list(String locgovCode, String startDt, String endDt, String searchRole,
                          String where, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.bbs_id, a.bbs_ttl, a.notice_yn, a.is_secret, coalesce(a.inq_cnt, 0),
                       a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code,
                       (select count(*) from admin.g_cmnty_sr_bbs_cmnt c
                         where c.use_yn = 'Y' and c.bbs_id = a.bbs_id),
                       (select count(*) from admin.g_cmnty_sr_bbs_file f
                         where f.use_yn = 'Y' and f.bbs_id = a.bbs_id)
                  from admin.g_cmnty_sr_bbs a
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
            result.add(new Row(num(r[0]), str(r[1]), str(r[2]), str(r[3]), num(r[4]), num(r[5]),
                    dt(r[6]), dt(r[7]), str(r[8]), str(r[9]), str(r[10]),
                    num(r[11]), num(r[12])));
        }
        return result;
    }

    /**
     * AS-IS getSrBbsDetail.
     *
     * <p><b>AS-IS는 여기에 {@code use_yn} 조건이 없다</b> - 삭제한 글도 URL로 직접 열면 보인다.
     * 그대로 두면 목록에서 사라진 글이 링크로 열리는 셈이라, 삭제된 글은 없는 것으로 본다
     * (소통방 detailBbs는 AS-IS도 {@code use_yn='Y'}를 걸고 있어 두 화면이 서로 달랐다).
     */
    @SuppressWarnings("unchecked")
    public DetailRow detail(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.bbs_id, a.bbs_ttl, a.bbs_cn, a.use_yn, a.notice_yn, a.is_secret,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code
                  from admin.g_cmnty_sr_bbs a
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
                num(r[6]), num(r[7]), dt(r[8]), dt(r[9]), str(r[10]),
                str(r[11]), str(r[12]));
    }

    /** AS-IS selectSrBbsCmntList - <b>등록일시 오름차순</b>. */
    @SuppressWarnings("unchecked")
    public List<CmntRow> cmntList(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.cmnt_id, a.bbs_id, a.cmnt_cn, b.user_name, b.authority, b.locgov_code,
                       a.frst_crt_id, a.frst_crt_dt
                  from admin.g_cmnty_sr_bbs_cmnt a
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

    /**
     * AS-IS updateSrBbsInqCnt. AS-IS는 {@code INQ_CNT = INQ_CNT + 1}로 <b>coalesce가 없어</b>
     * 조회수가 NULL인 행은 영원히 NULL이다 - 등록 시 0을 넣으므로 실제로는 생기지 않지만
     * 여기서는 소통방과 같이 coalesce를 둔다.
     */
    @Transactional
    public void updateInqCnt(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                update admin.g_cmnty_sr_bbs set inq_cnt = coalesce(inq_cnt, 0) + 1 where bbs_id = :bbsId
                """);
        q.setParameter("bbsId", bbsId);
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

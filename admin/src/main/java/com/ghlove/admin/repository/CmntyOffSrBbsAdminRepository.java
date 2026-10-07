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
 * 오프라인 담당자 SR 게시판(메뉴 11406) 목록·상세·댓글 조회 - AS-IS {@code countOffSrBbs}/
 * {@code listOffSrBbs}/{@code getOffSrBbsDetail}/{@code selectOffSrBbsCmntList}/
 * {@code updateOffSrBbsInqCnt} 이식(조회 전용).
 *
 * <p><b>SR 게시판(11404)과 달라지는 것</b> - 이 게시판은 오프라인(은행 지점) 담당자용이다:
 * <ul>
 *   <li>작성자 소속을 지자체 대신 <b>은행명 + 지점명</b>으로 보여준다. 은행명은 공통코드
 *       {@code OFF_BANK_LIST}의 label이고, AS-IS도 서브쿼리로 바로 가져온다(지점명은
 *       {@code op_manager.psitn_nm}). 지자체명도 같이 내려준다 - 댓글에는 지자체 담당자(5·6)가
 *       달 수 있어서 화면이 둘 다 쓴다.</li>
 *   <li>소속 검색값이 {@code ROLE_ADMIN_7}(= 7·8 오프라인 담당자)이고, 지자체 대신
 *       <b>은행코드({@code shBank})</b>로 좁힌다.</li>
 *   <li>검색구분이 제목 / <b>소속지점({@code PSITN}, LIKE)</b>이다(SR은 작성자명 {@code =} 비교).</li>
 * </ul>
 *
 * <p>권한 치환(op_user_role → op_manager.authority)과 지자체명을 서비스에서 채우는 방식은
 * {@link CmntyBbsAdminRepository} 주석과 같다.
 */
@Repository
@RequiredArgsConstructor
public class CmntyOffSrBbsAdminRepository {

    private static final List<String> ROLES_SYSTEM = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");
    private static final List<String> ROLES_MOIS = List.of("ROLE_ADMIN_3", "ROLE_ADMIN_4");
    /** AS-IS listOffSrBbs - ROLE_ADMIN_7을 고르면 7·8을 함께 본다. */
    private static final List<String> ROLES_OFFLINE = List.of("ROLE_ADMIN_7", "ROLE_ADMIN_8");

    /** 은행명을 가져오는 공통코드 - AS-IS가 서브쿼리로 바로 쓴다. */
    private static final String BANK_CODE_TYPE = "OFF_BANK_LIST";

    private final EntityManager entityManager;

    public record Row(Long bbsId, String bbsTtl, String noticeYn, String isSecret, Long inqCnt,
                      Long frstCrtId, LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt,
                      String userName, String authority, String locgovCode, String bankNm,
                      String psitnNm, Long cmntCnt, Long attachedFileCnt) {
    }

    public record DetailRow(Long bbsId, String bbsTtl, String bbsCn, String useYn, String noticeYn,
                            String isSecret, Long inqCnt, Long frstCrtId, LocalDateTime frstCrtDt,
                            LocalDateTime lastMdfcnDt, String userName, String authority,
                            String locgovCode, String bankNm, String psitnNm) {
    }

    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String locgovCode, String bankNm, String psitnNm, Long frstCrtId,
                          LocalDateTime frstCrtDt) {
    }

    /** AS-IS countOffSrBbs. */
    public int count(String startDt, String endDt, String searchRole, String shBank,
                     String where, String query) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                  from admin.g_cmnty_off_sr_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, startDt, endDt, searchRole, shBank, where, query);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS listOffSrBbs - 공지 우선, 등록일시 역순. */
    @SuppressWarnings("unchecked")
    public List<Row> list(String startDt, String endDt, String searchRole, String shBank,
                          String where, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.bbs_id, a.bbs_ttl, a.notice_yn, a.is_secret, coalesce(a.inq_cnt, 0),
                       a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code,
                       (select c.label from admin.op_common_code c
                         where c.code_type = :bankCodeType and c.use_yn = 'Y'
                           and c.language = 'ko' and c.id = b.bank_code),
                       b.psitn_nm,
                       (select count(*) from admin.g_cmnty_off_sr_bbs_cmnt t
                         where t.use_yn = 'Y' and t.bbs_id = a.bbs_id),
                       (select count(*) from admin.g_cmnty_off_sr_bbs_file f
                         where f.use_yn = 'Y' and f.bbs_id = a.bbs_id)
                  from admin.g_cmnty_off_sr_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                """);
        List<Object[]> binds = new ArrayList<>();
        appendWhere(sql, binds, startDt, endDt, searchRole, shBank, where, query);
        sql.append(" order by coalesce(a.notice_yn, 'N') desc, a.frst_crt_dt desc offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        q.setParameter("bankCodeType", BANK_CODE_TYPE);
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        List<Row> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new Row(num(r[0]), str(r[1]), str(r[2]), str(r[3]), num(r[4]), num(r[5]),
                    dt(r[6]), dt(r[7]), str(r[8]), str(r[9]), str(r[10]),
                    str(r[11]), str(r[12]), num(r[13]), num(r[14])));
        }
        return result;
    }

    /** AS-IS getOffSrBbsDetail - SR과 같이 AS-IS에는 {@code use_yn} 조건이 없어 여기서 걸었다. */
    @SuppressWarnings("unchecked")
    public DetailRow detail(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.bbs_id, a.bbs_ttl, a.bbs_cn, a.use_yn, a.notice_yn, a.is_secret,
                       coalesce(a.inq_cnt, 0), a.frst_crt_id, a.frst_crt_dt, a.last_mdfcn_dt,
                       b.user_name, b.authority, b.locgov_code,
                       (select c.label from admin.op_common_code c
                         where c.code_type = :bankCodeType and c.use_yn = 'Y'
                           and c.language = 'ko' and c.id = b.bank_code),
                       b.psitn_nm
                  from admin.g_cmnty_off_sr_bbs a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.bbs_id = :bbsId and a.use_yn = 'Y'
                """);
        q.setParameter("bankCodeType", BANK_CODE_TYPE);
        q.setParameter("bbsId", bbsId);
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        return new DetailRow(num(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]),
                num(r[6]), num(r[7]), dt(r[8]), dt(r[9]), str(r[10]),
                str(r[11]), str(r[12]), str(r[13]), str(r[14]));
    }

    /** AS-IS selectOffSrBbsCmntList - 등록일시 오름차순. */
    @SuppressWarnings("unchecked")
    public List<CmntRow> cmntList(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                select a.cmnt_id, a.bbs_id, a.cmnt_cn, b.user_name, b.authority, b.locgov_code,
                       (select c.label from admin.op_common_code c
                         where c.code_type = :bankCodeType and c.use_yn = 'Y'
                           and c.language = 'ko' and c.id = b.bank_code),
                       b.psitn_nm, a.frst_crt_id, a.frst_crt_dt
                  from admin.g_cmnty_off_sr_bbs_cmnt a
                  left join admin.op_manager b on b.user_id = a.frst_crt_id
                 where a.use_yn = 'Y' and a.bbs_id = :bbsId
                 order by a.frst_crt_dt asc
                """);
        q.setParameter("bankCodeType", BANK_CODE_TYPE);
        q.setParameter("bbsId", bbsId);
        List<Object[]> rows = q.getResultList();
        List<CmntRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new CmntRow(num(r[0]), num(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]),
                    str(r[6]), str(r[7]), num(r[8]), dt(r[9])));
        }
        return result;
    }

    /** AS-IS updateOffSrBbsInqCnt. */
    @Transactional
    public void updateInqCnt(long bbsId) {
        Query q = entityManager.createNativeQuery("""
                update admin.g_cmnty_off_sr_bbs set inq_cnt = coalesce(inq_cnt, 0) + 1
                 where bbs_id = :bbsId
                """);
        q.setParameter("bbsId", bbsId);
        q.executeUpdate();
    }

    private static void appendWhere(StringBuilder sql, List<Object[]> binds, String startDt,
                                    String endDt, String searchRole, String shBank,
                                    String where, String query) {
        sql.append(" where a.use_yn = 'Y'");
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
        if (notBlank(shBank)) {
            sql.append(" and b.bank_code = :shBank");
            binds.add(new Object[]{"shBank", shBank});
        }
        if (notBlank(query)) {
            if ("SUBJECT".equals(where)) {
                sql.append(" and a.bbs_ttl like :likeQuery");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
            } else if ("PSITN".equals(where)) {
                sql.append(" and b.psitn_nm like :likeQuery");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
            } else {
                sql.append(" and (a.bbs_ttl like :likeQuery or b.psitn_nm like :likeQuery)");
                binds.add(new Object[]{"likeQuery", "%" + query + "%"});
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
            case "ROLE_ADMIN_7" -> ROLES_OFFLINE;
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

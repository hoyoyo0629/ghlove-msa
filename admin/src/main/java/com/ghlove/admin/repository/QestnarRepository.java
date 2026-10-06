package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 설문관리 (AS-IS qustnr-mapper.xml) - 메서드 이름을 AS-IS 쿼리 id와 똑같이 맞췄다.
 * AS-IS 모델은 4개 표다:
 * <ul>
 *   <li>{@code G_QESTNAR} 설문(제목/목적/기간/대상 U:대민 M:관리자 S:답례품제공자)</li>
 *   <li>{@code G_QUSTNR_QESITM} 문항(QESTN_TY_CODE rtype=객관식 stype=주관식, PARENT_SN=연계질문의 부모)</li>
 *   <li>{@code G_QUSTNR_IEM} 선택지</li>
 *   <li>{@code G_QUSTNR_RSPNS_RESULT} 응답결과</li>
 * </ul>
 * 채번도 AS-IS 그대로다 - 설문은 {@code MAX(QUSTNR_SN)+1}, 문항의 QESTN_SN은 설문 안에서
 * {@code MAX+1}, 선택지의 QUSTNR_IEM_SN/IEM_SN은 (설문,문항) 안에서 {@code MAX+1}.
 * 문항번호(QUSTNR_QESITM_SN)는 화면이 1..n으로 다시 매겨 보내준다(form.jsp reClassQuestionSet).
 *
 * MyBatis 동적 SQL을 그대로 옮기려고 네이티브 쿼리를 쓴다 - 복합키 엔티티 4개를 만드는 것보다
 * 매퍼와 1:1로 대조하기 쉽고, 표가 전부 비어 있어 마이그레이션 위험도 없다.
 */
@Repository
@RequiredArgsConstructor
public class QestnarRepository {

    private final EntityManager entityManager;

    /** 설문 한 건 - AS-IS getQustnr. IS_SHOW는 기간이 오늘을 포함하는지로 계산한다. */
    public record QestnarRow(Long qustnrSn, String qustnrSj, String qustnrPurps, String qustnrBgnDe,
                             String qustnrEndDe, String frstRegistPnttm, String isShow,
                             Integer maxQesitm, String srvyTrgt) {
    }

    /** 문항+선택지 평면 행 - AS-IS getQustnrQesitmDetail. */
    public record QesitmDetailRow(Long qustnrSn, Integer qustnrQesitmSn, Integer qustnrIemSn,
                                  Integer qestnSn, String qestnTyCode, String qestnCn,
                                  Integer parentSn, Integer iemSn, String iemCn, long userCnt) {
    }

    /** AS-IS getQustnrListCnt. */
    public int getQustnrListCnt(String searchTxt) {
        String sql = "select count(qustnr_sn) from admin.g_qestnar where 1=1"
                + (searchTxt == null ? "" : " and qustnr_sj like concat('%', :searchTxt, '%')");
        var query = entityManager.createNativeQuery(sql);
        if (searchTxt != null) {
            query.setParameter("searchTxt", searchTxt);
        }
        return ((Number) query.getSingleResult()).intValue();
    }

    /** AS-IS getQustnrList - 등록일 역순(AS-IS는 QUSTNR_SN DESC). */
    public List<QestnarRow> getQustnrList(String searchTxt) {
        String sql = """
                select q.qustnr_sn, q.qustnr_sj, q.qustnr_purps,
                       q.qustnr_bgn_de, q.qustnr_end_de,
                       to_char(q.frst_regist_pnttm, 'YYYY-MM-DD') as frst_regist_pnttm,
                       case when q.qustnr_bgn_de <= to_char(current_date,'YYYYMMDD')
                             and q.qustnr_end_de >= to_char(current_date,'YYYYMMDD') then 'Y' else 'N' end as is_show,
                       0 as max_qesitm,
                       q.srvy_trgt
                  from admin.g_qestnar q
                 where 1=1
                """
                + (searchTxt == null ? "" : " and q.qustnr_sj like concat('%', :searchTxt, '%')")
                + " order by q.qustnr_sn desc";
        var query = entityManager.createNativeQuery(sql);
        if (searchTxt != null) {
            query.setParameter("searchTxt", searchTxt);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(QestnarRepository::toQestnarRow).toList();
    }

    /** AS-IS getQustnr - MAX_QESITM은 화면의 질문 카운터 초기값으로 쓰인다. */
    public QestnarRow getQustnr(long qustnrSn) {
        String sql = """
                select q.qustnr_sn, q.qustnr_sj, q.qustnr_purps,
                       q.qustnr_bgn_de, q.qustnr_end_de,
                       to_char(q.frst_regist_pnttm, 'YYYY-MM-DD') as frst_regist_pnttm,
                       case when q.qustnr_bgn_de <= to_char(current_date,'YYYYMMDD')
                             and q.qustnr_end_de >= to_char(current_date,'YYYYMMDD') then 'Y' else 'N' end as is_show,
                       (select coalesce(max(gqq.qustnr_qesitm_sn), 0) + 1
                          from admin.g_qustnr_qesitm gqq where gqq.qustnr_sn = q.qustnr_sn) as max_qesitm,
                       q.srvy_trgt
                  from admin.g_qestnar q
                 where q.qustnr_sn = :qustnrSn
                """;
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("qustnrSn", qustnrSn).getResultList();
        return rows.isEmpty() ? null : toQestnarRow(rows.get(0));
    }

    /**
     * AS-IS getQustnrQesitmDetail - 문항과 선택지를 조인해 평면으로 준다.
     * 결과화면은 선택지별 응답수(userCnt)가 필요하므로 G_QUSTNR_RSPNS_RESULT를 함께 센다
     * (AS-IS도 같은 쿼리에 userId 분기로 응답수를 붙인다).
     */
    public List<QesitmDetailRow> getQustnrQesitmDetail(long qustnrSn) {
        String sql = """
                select gqq.qustnr_sn, gqq.qustnr_qesitm_sn, gqi.qustnr_iem_sn, gqq.qestn_sn,
                       gqq.qestn_ty_code, gqq.qestn_cn, gqq.parent_sn, gqi.iem_sn, gqi.iem_cn,
                       (select count(*) from admin.g_qustnr_rspns_result r
                         where r.qustnr_sn = gqq.qustnr_sn
                           and r.qustnr_qesitm_sn = gqq.qustnr_qesitm_sn
                           and r.qustnr_iem_sn = gqi.qustnr_iem_sn) as user_cnt
                  from admin.g_qustnr_qesitm gqq
                  left join admin.g_qustnr_iem gqi
                         on gqi.qustnr_sn = gqq.qustnr_sn
                        and gqi.qustnr_qesitm_sn = gqq.qustnr_qesitm_sn
                 where gqq.qustnr_sn = :qustnrSn
                 order by gqq.qustnr_qesitm_sn, gqi.qustnr_iem_sn
                """;
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("qustnrSn", qustnrSn).getResultList();
        List<QesitmDetailRow> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new QesitmDetailRow(asLong(r[0]), asInt(r[1]), asInt(r[2]), asInt(r[3]),
                    asString(r[4]), asString(r[5]), asInt(r[6]), asInt(r[7]), asString(r[8]),
                    r[9] == null ? 0L : ((Number) r[9]).longValue()));
        }
        return result;
    }

    /** AS-IS getQustnrRspnsResultCnt - 설문 참여 인원(중복 제거). */
    public long getQustnrRspnsResultCnt(long qustnrSn) {
        Object result = entityManager.createNativeQuery(
                        "select count(distinct user_id) from admin.g_qustnr_rspns_result where qustnr_sn = :qustnrSn")
                .setParameter("qustnrSn", qustnrSn).getSingleResult();
        return result == null ? 0L : ((Number) result).longValue();
    }

    /** AS-IS insertQustnr - QUSTNR_SN = MAX+1. 새 설문번호를 돌려준다. */
    public long insertQustnr(String qustnrSj, String qustnrPurps, String qustnrBgnDe, String qustnrEndDe,
                             String srvyTrgt, Long registerId) {
        long qustnrSn = ((Number) entityManager.createNativeQuery(
                        "select coalesce(max(qustnr_sn) + 1, 1) from admin.g_qestnar").getSingleResult()).longValue();
        entityManager.createNativeQuery("""
                        insert into admin.g_qestnar
                          (qustnr_sn, qustnr_sj, qustnr_purps, qustnr_bgn_de, qustnr_end_de,
                           frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, srvy_trgt)
                        values (:qustnrSn, :qustnrSj, :qustnrPurps, :bgn, :end,
                           :registerId, now(), :registerId, now(), :srvyTrgt)
                        """)
                .setParameter("qustnrSn", qustnrSn)
                .setParameter("qustnrSj", qustnrSj)
                .setParameter("qustnrPurps", qustnrPurps)
                .setParameter("bgn", qustnrBgnDe)
                .setParameter("end", qustnrEndDe)
                .setParameter("registerId", registerId)
                .setParameter("srvyTrgt", srvyTrgt)
                .executeUpdate();
        return qustnrSn;
    }

    /** AS-IS updateQustnr - 제목/기간/대상만 갱신한다(목적은 AS-IS도 건드리지 않는다). */
    public void updateQustnr(long qustnrSn, String qustnrSj, String qustnrBgnDe, String qustnrEndDe,
                             String srvyTrgt, Long updusrId) {
        entityManager.createNativeQuery("""
                        update admin.g_qestnar
                           set qustnr_sj = :qustnrSj, qustnr_bgn_de = :bgn, qustnr_end_de = :end,
                               last_updusr_id = :updusrId, last_updt_pnttm = now(), srvy_trgt = :srvyTrgt
                         where qustnr_sn = :qustnrSn
                        """)
                .setParameter("qustnrSj", qustnrSj)
                .setParameter("bgn", qustnrBgnDe)
                .setParameter("end", qustnrEndDe)
                .setParameter("updusrId", updusrId)
                .setParameter("srvyTrgt", srvyTrgt)
                .setParameter("qustnrSn", qustnrSn)
                .executeUpdate();
    }

    /** AS-IS checkQusitm - 이미 있는 문항인지(insert/update 분기). */
    public boolean existsQesitm(long qustnrSn, int qustnrQesitmSn) {
        Object result = entityManager.createNativeQuery(
                        "select count(*) from admin.g_qustnr_qesitm where qustnr_sn = :s and qustnr_qesitm_sn = :q")
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn).getSingleResult();
        return ((Number) result).intValue() > 0;
    }

    /** AS-IS insertQustnrQesitm - QESTN_SN은 설문 안에서 MAX+1. */
    public void insertQustnrQesitm(long qustnrSn, int qustnrQesitmSn, String qestnTyCode, String qestnCn,
                                   Integer answerChoiseCo, Integer parentSn, Long registerId) {
        entityManager.createNativeQuery("""
                        insert into admin.g_qustnr_qesitm
                          (qustnr_sn, qustnr_qesitm_sn, qestn_sn, qestn_ty_code, qestn_cn, answer_choise_co,
                           frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, parent_sn)
                        values (:s, :q,
                           (select coalesce(max(qestn_sn) + 1, 1) from admin.g_qustnr_qesitm where qustnr_sn = :s),
                           :tyCode, :cn, :choiseCo, :registerId, now(), :registerId, now(), :parentSn)
                        """)
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn)
                .setParameter("tyCode", qestnTyCode).setParameter("cn", qestnCn)
                .setParameter("choiseCo", answerChoiseCo).setParameter("parentSn", parentSn)
                .setParameter("registerId", registerId)
                .executeUpdate();
    }

    /** AS-IS updateQustnrQesitm - 문항 내용만 갱신. */
    public void updateQustnrQesitm(long qustnrSn, int qustnrQesitmSn, String qestnCn, Long updusrId) {
        entityManager.createNativeQuery("""
                        update admin.g_qustnr_qesitm
                           set qestn_cn = :cn, last_updusr_id = :updusrId, last_updt_pnttm = now()
                         where qustnr_sn = :s and qustnr_qesitm_sn = :q
                        """)
                .setParameter("cn", qestnCn).setParameter("updusrId", updusrId)
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn)
                .executeUpdate();
    }

    /** AS-IS checkIEM. */
    public boolean existsIem(long qustnrSn, int qustnrQesitmSn, int qustnrIemSn) {
        Object result = entityManager.createNativeQuery(
                        "select count(*) from admin.g_qustnr_iem where qustnr_sn = :s and qustnr_qesitm_sn = :q and qustnr_iem_sn = :i")
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn).setParameter("i", qustnrIemSn)
                .getSingleResult();
        return ((Number) result).intValue() > 0;
    }

    /** AS-IS insertQustnrIem - QUSTNR_IEM_SN / IEM_SN 모두 (설문,문항) 안에서 MAX+1. */
    public int insertQustnrIem(long qustnrSn, int qustnrQesitmSn, String iemCn, Long registerId) {
        int qustnrIemSn = ((Number) entityManager.createNativeQuery(
                        "select coalesce(max(qustnr_iem_sn) + 1, 1) from admin.g_qustnr_iem where qustnr_sn = :s and qustnr_qesitm_sn = :q")
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn).getSingleResult()).intValue();
        entityManager.createNativeQuery("""
                        insert into admin.g_qustnr_iem
                          (qustnr_sn, qustnr_qesitm_sn, qustnr_iem_sn, iem_sn, iem_cn, etc_answer_at,
                           frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
                        values (:s, :q, :i,
                           (select coalesce(max(iem_sn) + 1, 1) from admin.g_qustnr_iem where qustnr_sn = :s and qustnr_qesitm_sn = :q),
                           :cn, 'N', :registerId, now(), :registerId, now())
                        """)
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn).setParameter("i", qustnrIemSn)
                .setParameter("cn", iemCn).setParameter("registerId", registerId)
                .executeUpdate();
        return qustnrIemSn;
    }

    /** AS-IS updateQustnrIem. */
    public void updateQustnrIem(long qustnrSn, int qustnrQesitmSn, int qustnrIemSn, String iemCn, Long updusrId) {
        entityManager.createNativeQuery("""
                        update admin.g_qustnr_iem
                           set iem_cn = :cn, last_updusr_id = :updusrId, last_updt_pnttm = now()
                         where qustnr_sn = :s and qustnr_qesitm_sn = :q and qustnr_iem_sn = :i
                        """)
                .setParameter("cn", iemCn).setParameter("updusrId", updusrId)
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn).setParameter("i", qustnrIemSn)
                .executeUpdate();
    }

    /**
     * AS-IS deleteQustnrQesitmAll - 이번에 제출되지 않은 문항을 응답결과→선택지→문항 순으로 지운다.
     * 제출 목록이 비면 그 설문의 문항을 전부 지운다(AS-IS foreach가 빈 IN을 만들면 터지므로 분기).
     */
    public void deleteQesitmNotIn(long qustnrSn, List<Integer> keepQesitmSns) {
        String notIn = keepQesitmSns.isEmpty() ? "" : " and qustnr_qesitm_sn not in (:keep)";
        for (String table : List.of("admin.g_qustnr_rspns_result", "admin.g_qustnr_iem", "admin.g_qustnr_qesitm")) {
            var query = entityManager.createNativeQuery(
                            "delete from " + table + " where qustnr_sn = :s" + notIn)
                    .setParameter("s", qustnrSn);
            if (!keepQesitmSns.isEmpty()) {
                query.setParameter("keep", keepQesitmSns);
            }
            query.executeUpdate();
        }
    }

    /** AS-IS deleteQustnrIemAll - 한 문항에서 제출되지 않은 선택지를 지운다. */
    public void deleteIemNotIn(long qustnrSn, int qustnrQesitmSn, List<Integer> keepIemSns) {
        String notIn = keepIemSns.isEmpty() ? "" : " and qustnr_iem_sn not in (:keep)";
        for (String table : List.of("admin.g_qustnr_rspns_result", "admin.g_qustnr_iem")) {
            var query = entityManager.createNativeQuery(
                            "delete from " + table + " where qustnr_sn = :s and qustnr_qesitm_sn = :q" + notIn)
                    .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn);
            if (!keepIemSns.isEmpty()) {
                query.setParameter("keep", keepIemSns);
            }
            query.executeUpdate();
        }
    }

    /** 이 사용자가 이 설문에 이미 응답했는지 - AS-IS getQustnrAndValidate의 regCnt 판정. */
    public boolean existsResponse(long qustnrSn, long userId) {
        Object result = entityManager.createNativeQuery(
                        "select count(*) from admin.g_qustnr_rspns_result where qustnr_sn = :s and user_id = :u")
                .setParameter("s", qustnrSn).setParameter("u", userId).getSingleResult();
        return ((Number) result).intValue() > 0;
    }

    /** AS-IS insertQustnrRspnsResult - 이용자 응답 1건(객관식은 선택지, 주관식은 텍스트). */
    public void insertQustnrRspnsResult(long qustnrSn, int qustnrQesitmSn, long userId,
                                        int qustnrIemSn, String respondAnswerCn) {
        entityManager.createNativeQuery("""
                        insert into admin.g_qustnr_rspns_result
                          (qustnr_sn, qustnr_qesitm_sn, user_id, qustnr_iem_sn, respond_answer_cn, etc_answer_cn,
                           frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
                        values (:s, :q, :u, :i, :cn, null, :u, now(), :u, now())
                        """)
                .setParameter("s", qustnrSn).setParameter("q", qustnrQesitmSn)
                .setParameter("u", userId).setParameter("i", qustnrIemSn)
                .setParameter("cn", respondAnswerCn)
                .executeUpdate();
    }

    /** AS-IS deleteQustnr - 설문 하나를 응답결과·선택지·문항까지 전부 지운다. */
    public void deleteQustnr(long qustnrSn) {
        for (String table : List.of("admin.g_qustnr_rspns_result", "admin.g_qustnr_iem",
                "admin.g_qustnr_qesitm", "admin.g_qestnar")) {
            entityManager.createNativeQuery("delete from " + table + " where qustnr_sn = :s")
                    .setParameter("s", qustnrSn).executeUpdate();
        }
    }

    /** 결과화면용 - 문항별 선택지와 응답수를 문항 순서대로 묶어 준다. */
    public Map<Integer, List<QesitmDetailRow>> resultGroupedByQesitm(long qustnrSn) {
        Map<Integer, List<QesitmDetailRow>> grouped = new LinkedHashMap<>();
        for (QesitmDetailRow row : getQustnrQesitmDetail(qustnrSn)) {
            grouped.computeIfAbsent(row.qustnrQesitmSn(), k -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private static QestnarRow toQestnarRow(Object[] r) {
        return new QestnarRow(asLong(r[0]), asString(r[1]), asString(r[2]), asString(r[3]), asString(r[4]),
                asString(r[5]), asString(r[6]), asInt(r[7]), asString(r[8]));
    }

    /**
     * 네이티브 쿼리의 문자 컬럼을 String으로 받는다.
     *
     * <p><b>{@code (String) r[n]}로 직접 캐스팅하면 안 된다</b> - {@code CASE WHEN ... THEN 'Y' ELSE 'N' END}
     * 처럼 SQL 리터럴에서 나온 값은 PostgreSQL이 타입을 {@code bpchar}로 정하고, Hibernate가
     * 길이 1인 그 값을 {@code Character}로 돌려줘서 {@code ClassCastException}이 난다
     * (2026-10-03 설문 조회 500 에러의 원인 - 표가 비어 있던 동안은 드러나지 않았다).
     */
    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInt(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private static Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}

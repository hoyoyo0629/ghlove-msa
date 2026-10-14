package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 답례품제공자 공지사항 목록 (메뉴 5114) - AS-IS {@code sys-notice-seller-mapper.xml}의
 * {@code getNoticeList}/{@code getNoticeCount} 이식.
 *
 * <p>AS-IS SQL 그대로: 항상 {@code USE_YN='Y'}(삭제가 소프트 삭제라서) + 검색조건이 없으면
 * {@code BOARD_CODE IS NULL}을 건다(기금사업 소개 글 제외) · 등록일 범위는 {@code FRST_CRT_DT}
 * 기준 · 검색구분은 SUBJECT/CONTENT · 첨부 개수는 서브쿼리({@code use_yn='Y'})로 센다 ·
 * 정렬은 상단공지 우선 + 최신순({@code notice_flag DESC, frst_crt_dt DESC})이다.
 */
@Repository
@RequiredArgsConstructor
public class SysNoticeSellerAdminRepository {

    private final EntityManager entityManager;

    /** 목록 한 행 - AS-IS list.jsp가 쓰는 칸 그대로. */
    public record Row(Long noticeId, String subject, Long hits, String noticeFlag, String displayFlag,
                      String frstCrtDt, String lastMdfcnDt, long attachedFileCnt) {
    }

    public int count(String where, String query, String startDt, String endDt) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*) from admin.op_sys_notice_seller n where 1 = 1");
        appendWhere(sql, binds, where, query, startDt, endDt);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> q.setParameter((String) b[0], b[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Row> list(String where, String query, String startDt, String endDt, int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select n.notice_id, n.subject, n.hits, n.notice_flag, n.display_flag,
                       n.frst_crt_dt, n.last_mdfcn_dt,
                       (select count(*) from admin.op_sys_notice_seller_file f
                         where f.use_yn = 'Y' and f.notice_id = n.notice_id) as attached_file_cnt
                  from admin.op_sys_notice_seller n
                 where 1 = 1
                """);
        appendWhere(sql, binds, where, query, startDt, endDt);
        // AS-IS: 상단공지 우선, 그다음 최신순
        sql.append(" order by n.notice_flag desc, n.frst_crt_dt desc offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> q.setParameter((String) b[0], b[1]));
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new Row(
                r[0] == null ? null : ((Number) r[0]).longValue(),
                str(r[1]),
                r[2] == null ? null : ((Number) r[2]).longValue(),
                str(r[3]), str(r[4]),
                str(r[5]), str(r[6]),
                r[7] == null ? 0L : ((Number) r[7]).longValue()
        )).toList();
    }

    /**
     * ★ {@code NOTICE_FLAG}·{@code DISPLAY_FLAG}는 {@code varchar(1)}인데 Hibernate 네이티브
     * 쿼리가 길이 1짜리를 <b>{@code Character}로</b> 돌려준다. {@code (String)} 캐스팅하면
     * {@code ClassCastException}으로 목록이 500이 난다(2026-10-12 실제 발생, 메뉴관리에서도 같은 함정).
     */
    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    /** AS-IS는 첨부 ID를 CUBRID serial에서 받는다 - 그 자리를 시퀀스로 대신한다. */
    public Long nextFileId() {
        Query q = entityManager.createNativeQuery("select nextval('admin.op_sys_notice_seller_file_file_id_seq')");
        return ((Number) q.getSingleResult()).longValue();
    }

    private static void appendWhere(StringBuilder sql, List<Object[]> binds, String where, String query,
                                     String startDt, String endDt) {
        // AS-IS: boardCode 검색조건이 없으면 BOARD_CODE IS NULL (기금사업 소개 글 제외)
        sql.append(" and n.board_code is null and n.use_yn = 'Y'");

        if (startDt != null && !startDt.isBlank() && endDt != null && !endDt.isBlank()) {
            sql.append(" and to_char(n.frst_crt_dt, 'YYYYMMDD') between :startDt and :endDt");
            binds.add(new Object[]{"startDt", startDt});
            binds.add(new Object[]{"endDt", endDt});
        }
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim() + "%";
            if ("SUBJECT".equals(where)) {
                sql.append(" and n.subject like :likeQuery");
                binds.add(new Object[]{"likeQuery", like});
            } else if ("CONTENT".equals(where)) {
                sql.append(" and n.content like :likeQuery");
                binds.add(new Object[]{"likeQuery", like});
            }
        }
    }
}

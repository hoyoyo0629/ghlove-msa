package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 운영관리 SR게시판 목록 (메뉴 5113) - AS-IS {@code maintenance-mapper.xml}의
 * {@code listMaintenance}/{@code countMaintenance} 이식.
 *
 * <p>AS-IS SQL 그대로: {@code op_manager}(작성자명)와 공통코드 4종을 LEFT JOIN 해 라벨을 뽑고,
 * 첨부 개수는 서브쿼리({@code use_yn='Y'})로 센다. 조건은 등록일 범위(FRST_CRT_DT 기준) ·
 * 검색구분(ALL이면 CSR번호·제목·내용 OR) · 완료구분 다중선택(IN) · 항상 {@code use_yn='Y'}.
 * 정렬은 <b>긴급 우선 + 최신순</b>({@code urgent_yn DESC, frst_crt_dt DESC}) 고정이다.
 */
@Repository
@RequiredArgsConstructor
public class MaintenanceAdminRepository {

    private final EntityManager entityManager;

    /** 목록 한 행 - AS-IS list.jsp가 쓰는 칸 그대로. */
    public record Row(Long bbsId, String srNo, String reqManagerNm, String reqTypeNm, String processTypeNm,
                      String bbsTtl, String bbsCn, String reqChannelNm, String processManagerNm,
                      String processCn, String processStateNm, String frstCrtDt, String lastMdfcnDt,
                      String processStartDate, String processEndDate, long attachedFileCnt) {
    }

    public int count(String where, String query, String startDt, String endDt, List<String> processStates) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*) from admin.g_sr_maintenance a where a.use_yn = 'Y'");
        appendWhere(sql, binds, where, query, startDt, endDt, processStates);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> q.setParameter((String) b[0], b[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Row> list(String where, String query, String startDt, String endDt,
                          List<String> processStates, int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select a.bbs_id, a.sr_no, m1.user_name as req_manager_nm,
                       c1.label as req_type_nm, c3.label as process_type_nm,
                       a.bbs_ttl, a.bbs_cn, c2.label as req_channel_nm,
                       a.process_manager_nm, a.process_cn, c4.label as process_state_nm,
                       a.frst_crt_dt, a.last_mdfcn_dt, a.process_start_date, a.process_end_date,
                       (select count(*) from admin.g_sr_maintenance_file f
                         where f.use_yn = 'Y' and f.bbs_id = a.bbs_id) as attached_file_cnt
                  from admin.g_sr_maintenance a
                  left join admin.op_manager m1 on a.frst_crt_id = m1.user_id
                  left join admin.op_common_code c1 on a.req_type = c1.id and c1.code_type = 'MAINTEN_CATE_CODE'
                  left join admin.op_common_code c2 on a.req_channel = c2.id and c2.code_type = 'MAINTEN_PATH_CODE'
                  left join admin.op_common_code c3 on a.process_type = c3.id and c3.code_type = 'MAINTEN_PROC_CODE'
                  left join admin.op_common_code c4 on a.process_state = c4.id and c4.code_type = 'MAINTEN_STATE_CODE'
                 where a.use_yn = 'Y'
                """);
        appendWhere(sql, binds, where, query, startDt, endDt, processStates);
        // AS-IS: 긴급(urgent_yn) 우선, 그다음 최신순
        sql.append(" order by a.urgent_yn desc, a.frst_crt_dt desc offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> q.setParameter((String) b[0], b[1]));
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new Row(
                r[0] == null ? null : ((Number) r[0]).longValue(),
                (String) r[1], (String) r[2], (String) r[3], (String) r[4],
                (String) r[5], (String) r[6], (String) r[7], (String) r[8], (String) r[9], (String) r[10],
                r[11] == null ? null : r[11].toString(),
                r[12] == null ? null : r[12].toString(),
                (String) r[13], (String) r[14],
                r[15] == null ? 0L : ((Number) r[15]).longValue()
        )).toList();
    }

    /** AS-IS는 첨부 ID를 CUBRID serial에서 받는다 - 그 자리를 시퀀스로 대신한다. */
    public Long nextFileId() {
        Query q = entityManager.createNativeQuery("select nextval('admin.g_sr_maintenance_file_file_id_seq')");
        return ((Number) q.getSingleResult()).longValue();
    }

    private static void appendWhere(StringBuilder sql, List<Object[]> binds, String where, String query,
                                     String startDt, String endDt, List<String> processStates) {
        if (startDt != null && !startDt.isBlank() && endDt != null && !endDt.isBlank()) {
            sql.append(" and to_char(a.frst_crt_dt, 'YYYYMMDD') between :startDt and :endDt");
            binds.add(new Object[]{"startDt", startDt});
            binds.add(new Object[]{"endDt", endDt});
        }
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim() + "%";
            switch (where == null ? "ALL" : where) {
                case "CSRNO" -> sql.append(" and a.sr_no like :likeQuery");
                case "SUBJECT" -> sql.append(" and a.bbs_ttl like :likeQuery");
                case "CN" -> sql.append(" and a.bbs_cn like :likeQuery");
                // AS-IS otherwise(= ALL): 세 칸을 OR로 본다
                default -> sql.append(" and (a.sr_no like :likeQuery or a.bbs_ttl like :likeQuery"
                        + " or a.bbs_cn like :likeQuery)");
            }
            binds.add(new Object[]{"likeQuery", like});
        }
        if (processStates != null && !processStates.isEmpty()) {
            sql.append(" and a.process_state in (:processStates)");
            binds.add(new Object[]{"processStates", processStates});
        }
    }
}

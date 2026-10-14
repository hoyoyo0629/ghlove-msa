package com.ghlove.admin.repository;

import com.ghlove.admin.domain.DataBoard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 자료실 목록 (메뉴 5107) - AS-IS {@code databoard-mapper.xml}의
 * {@code getDataboardList}/{@code getDataboardCount} 이식.
 *
 * <p>AS-IS SQL 그대로: 검색구분은 SUBJECT/CONTENT만(그 외 값이면 필터 없음 - 화면 select가
 * 둘뿐이고 FILE_TY 옵션은 주석처리), 등록일 범위는 시작·종료 <b>둘 다</b> 있을 때만 적용,
 * 정렬은 {@code NOTICE_FLAG DESC, DATA_ID DESC} 고정이다.
 * 공지사항(5111)과 달리 지자체코드·게시판코드 조건이 없다.
 */
@Repository
@RequiredArgsConstructor
public class DataBoardAdminRepository {

    private final EntityManager entityManager;

    public int count(String where, String query, String startCreateDate, String endCreateDate) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*)");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> nativeQuery.setParameter((String) b[0], b[1]));
        return ((Number) nativeQuery.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<DataBoard> list(String where, String query, String startCreateDate, String endCreateDate,
                                 int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select d.*");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate);
        // AS-IS: 2017.04.24 상단공지 우선 정렬 추가
        sql.append(" order by d.notice_flag desc, d.data_id desc offset :offset limit :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), DataBoard.class);
        binds.forEach(b -> nativeQuery.setParameter((String) b[0], b[1]));
        nativeQuery.setParameter("offset", offset);
        nativeQuery.setParameter("limit", limit);
        return nativeQuery.getResultList();
    }

    /** AS-IS는 첨부 ID를 CUBRID serial에서 받는다 - 그 자리를 시퀀스로 대신한다. */
    public Integer nextFileId() {
        Query q = entityManager.createNativeQuery("select nextval('admin.op_data_board_file_seq')");
        return ((Number) q.getSingleResult()).intValue();
    }

    private static void appendFromWhere(StringBuilder sql, List<Object[]> binds, String where, String query,
                                         String startCreateDate, String endCreateDate) {
        sql.append(" from admin.op_data_board d where 1 = 1");

        if (query != null && !query.isBlank()) {
            String likeQuery = "%" + query.trim() + "%";
            if ("SUBJECT".equals(where)) {
                sql.append(" and d.subject like :likeQuery");
                binds.add(new Object[]{"likeQuery", likeQuery});
            } else if ("CONTENT".equals(where)) {
                sql.append(" and d.content like :likeQuery");
                binds.add(new Object[]{"likeQuery", likeQuery});
            }
        }
        if (startCreateDate != null && !startCreateDate.isBlank()
                && endCreateDate != null && !endCreateDate.isBlank()) {
            sql.append(" and substring(d.created_date, 1, 8) between :startDate and :endDate");
            binds.add(new Object[]{"startDate", startCreateDate});
            binds.add(new Object[]{"endDate", endCreateDate});
        }
    }
}

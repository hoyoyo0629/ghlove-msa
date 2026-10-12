package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Notice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 공지사항(전체공지) 목록 (메뉴 5111) - AS-IS {@code notice-mapper.xml}의
 * {@code getNoticeList}/{@code getNoticeCount} 이식. AS-IS 컨트롤러가 검색조건의 지자체코드를
 * {@code "00000"}으로 <b>고정</b>해 넘기므로 전체공지만 조회된다.
 *
 * <p><b>경계 번역</b>: AS-IS는 전체공지를 {@code locgov_code='00000'}으로 쓰는데 TO-BE 시드는
 * 그 자리를 비워 두었다(실측 NULL 14행). 그래서 NULL·빈문자열·'00000'을 모두 전체공지로 본다 -
 * 이 화면을 통해 새로 등록되는 행은 AS-IS대로 '00000'이 들어간다.
 *
 * <p>AS-IS SQL 그대로: 검색구분은 SUBJECT/CONTENT만(그 외 값이면 필터 없음), 등록일 범위는
 * 시작·종료 <b>둘 다</b> 있을 때만 적용, 기금사업 소개(BOARD_CODE='DONATION') 제외,
 * 정렬은 상단공지 우선 + 최신순 고정이다.
 */
@Repository
@RequiredArgsConstructor
public class NoticeAdminRepository {

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
    public List<Notice> list(String where, String query, String startCreateDate, String endCreateDate,
                              int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select n.*");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate);
        // AS-IS: 2017.04.24 상단공지 우선 정렬 추가
        sql.append(" order by n.notice_flag desc, n.notice_id desc offset :offset limit :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), Notice.class);
        binds.forEach(b -> nativeQuery.setParameter((String) b[0], b[1]));
        nativeQuery.setParameter("offset", offset);
        nativeQuery.setParameter("limit", limit);
        return nativeQuery.getResultList();
    }

    private static void appendFromWhere(StringBuilder sql, List<Object[]> binds, String where, String query,
                                         String startCreateDate, String endCreateDate) {
        sql.append(" from admin.op_notice n")
           .append(" where (n.locgov_code is null or n.locgov_code = '' or n.locgov_code = '00000')")
           .append(" and (n.board_code <> 'DONATION' or n.board_code is null)");

        if (query != null && !query.isBlank()) {
            String likeQuery = "%" + query.trim() + "%";
            if ("SUBJECT".equals(where)) {
                sql.append(" and n.subject like :likeQuery");
                binds.add(new Object[]{"likeQuery", likeQuery});
            } else if ("CONTENT".equals(where)) {
                sql.append(" and n.content like :likeQuery");
                binds.add(new Object[]{"likeQuery", likeQuery});
            }
        }
        if (startCreateDate != null && !startCreateDate.isBlank()
                && endCreateDate != null && !endCreateDate.isBlank()) {
            sql.append(" and substring(n.created_date, 1, 8) between :startDate and :endDate");
            binds.add(new Object[]{"startDate", startCreateDate});
            binds.add(new Object[]{"endDate", endCreateDate});
        }
    }
}

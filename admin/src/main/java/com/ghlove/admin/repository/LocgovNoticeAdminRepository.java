package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Notice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 지자체공지사항 목록 (메뉴 5110) - AS-IS {@code notice-mapper.xml}의
 * {@code getLocgovNoticeList}/{@code getLocgovNoticeCount} 이식. 5111 공지사항(전체공지)과
 * 같은 {@code OP_NOTICE}를 보되 조회범위만 다르다 - 여기는 항상 {@code LOCGOV_CODE <> '00000'}을
 * 건다(전체공지 제외). {@code locgovCode}는 LOC 담당자일 때만 컨트롤러가 채워 넘긴다
 * (시스템·행안부는 null로 넘어와 전체 지자체를 본다 - AS-IS 그대로의 느슨한 동작).
 *
 * <p>AS-IS SQL 그대로 옮긴 것: 검색구분은 SUBJECT/CONTENT만(그 외 값이면 필터 없음 -
 * 화면 select가 둘뿐이라 실무상 항상 이 둘 중 하나다), 등록일 범위는 시작·종료 <b>둘 다</b>
 * 있을 때만 적용({@code substring(created_date,1,8)} 비교), 기금사업 소개(BOARD_CODE='DONATION')
 * 게시글은 항상 제외, 정렬은 상단공지 우선 + 최신순 고정이다.
 */
@Repository
@RequiredArgsConstructor
public class LocgovNoticeAdminRepository {

    private final EntityManager entityManager;

    public int count(String where, String query, String startCreateDate, String endCreateDate, String locgovCode) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*)");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate, locgovCode);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(b -> nativeQuery.setParameter((String) b[0], b[1]));
        return ((Number) nativeQuery.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Notice> list(String where, String query, String startCreateDate, String endCreateDate,
                              String locgovCode, int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select n.*");
        appendFromWhere(sql, binds, where, query, startCreateDate, endCreateDate, locgovCode);
        // AS-IS: 2017.04.24 상단공지 우선 정렬 추가
        sql.append(" order by n.notice_flag desc, n.notice_id desc offset :offset limit :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), Notice.class);
        binds.forEach(b -> nativeQuery.setParameter((String) b[0], b[1]));
        nativeQuery.setParameter("offset", offset);
        nativeQuery.setParameter("limit", limit);
        return nativeQuery.getResultList();
    }

    private static void appendFromWhere(StringBuilder sql, List<Object[]> binds, String where, String query,
                                         String startCreateDate, String endCreateDate, String locgovCode) {
        sql.append(" from admin.op_notice n where n.locgov_code <> '00000'");
        sql.append(" and (n.board_code <> 'DONATION' or n.board_code is null)");

        if (locgovCode != null && !locgovCode.isBlank()) {
            sql.append(" and n.locgov_code = :locgovCode");
            binds.add(new Object[]{"locgovCode", locgovCode});
        }
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

package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 1:1 문의 목록 (메뉴 5102) - AS-IS {@code qna-mapper.xml}의
 * {@code getQnaListByParam}/{@code getQnaListCountByParam} 이식.
 *
 * <p><b>5112 Q&A와 같은 표({@code OP_QNA})를 보지만 SQL이 다르다</b>:
 * Q&A는 답변 행을 펼치는 구조({@link QnaOpenAdminRepository})이고, 여기는 문의 한 건이 한 줄이다.
 * 구분은 {@code QNA_TYPE}이다 - AS-IS 상수로 <b>{@code '0'}=1:1문의</b>, {@code '1'}=상품문의(5103, 중지),
 * {@code '2'}=Q&A(5112).
 *
 * <p><b>AS-IS SQL 그대로</b>:
 * <ul>
 *   <li>항상 {@code DATA_STATUS_CODE='0' AND DISPLAY_FLAG='Y'}를 건다(소프트삭제·숨김 제외).</li>
 *   <li>검색구분 GROUP(문의유형 라벨 LIKE) / LOGIN_ID(회원 아이디) / SUBJECT(제목 LIKE) /
 *       QUESTION(내용 LIKE). 화면 select에는 이 넷만 있고 <b>'전체'와 '이름'은 주석처리</b>되어 있다.
 *       조건식에는 USER_NAME(<b>정확일치</b>)·EMAIL(<b>정확일치</b>) 분기도 있어 같이 옮겼다.</li>
 *   <li>등록일 범위는 {@code CREATED_DATE}(14자리 문자열)에 {@code 000000}/{@code 235959}를 붙여 비교한다.</li>
 *   <li>상태 라디오({@code qnaAnswerCode})는 {@code 0}=답변완료({@code ANSWER_COUNT>0}),
 *       {@code 1}=답변미완료({@code =0})다.</li>
 *   <li>정렬은 {@code CREATED_DATE DESC} 고정이다.</li>
 * </ul>
 *
 * <p><b>옮기지 않은 조건</b>: AS-IS 조건식에는 {@code answerCount} 1/2(답변 수 서브쿼리로 같은 판정을
 * 한 번 더), {@code itemId}·{@code sellerId}, 그리고 바깥 쿼리의
 * {@code ITEM_NAME}/{@code ITEM_CODE}/{@code COMPANY_NAME}/{@code sido}/{@code sigungu} 조건이 있다.
 * 전부 <b>상품문의(5103)</b>용이고 그 메뉴는 AS-IS에서 중지되어 있으며, 1:1문의 행에는
 * {@code item_id}/{@code seller_id}가 없어 화면에서 쓰이지 않는다. 답변상태 라디오도 주석처리 상태다.
 * {@code OP_ITEM}/{@code OP_SELLER}는 TO-BE에서 gift 서비스 소유라 조인 자체가 불가능하다.
 */
@Repository
@RequiredArgsConstructor
public class QnaIndividualAdminRepository {

    private final EntityManager entityManager;

    /** 목록 한 행 - 화면이 쓰는 칸 그대로(로그인ID는 member에서 따로 채운다). */
    public record Row(Integer qnaId, String qnaGroup, String qnaGroupNm, String subject,
                      Long userId, String userName, Integer answerCount, String createdDate) {
    }

    public int count(String qnaType, String qnaAnswerCode, String where, String query,
                     String searchStartDate, String searchEndDate, List<Long> userIdFilter) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select count(*)");
        appendFromWhere(sql, binds, qnaType, qnaAnswerCode, where, query,
                searchStartDate, searchEndDate, userIdFilter);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> nativeQuery.setParameter((String) bind[0], bind[1]));
        return ((Number) nativeQuery.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Row> list(String qnaType, String qnaAnswerCode, String where, String query,
                          String searchStartDate, String searchEndDate, List<Long> userIdFilter,
                          int offset, int limit) {
        List<Object[]> binds = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select q.qna_id, q.qna_group, c.label as qna_group_nm, q.subject,
                       q.user_id, q.user_name, q.answer_count, q.created_date
                """);
        appendFromWhere(sql, binds, qnaType, qnaAnswerCode, where, query,
                searchStartDate, searchEndDate, userIdFilter);
        sql.append(" order by q.created_date desc offset :offset limit :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> nativeQuery.setParameter((String) bind[0], bind[1]));
        nativeQuery.setParameter("offset", offset);
        nativeQuery.setParameter("limit", limit);

        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream().map(r -> new Row(
                r[0] == null ? null : ((Number) r[0]).intValue(),
                (String) r[1],
                (String) r[2],
                (String) r[3],
                r[4] == null ? null : ((Number) r[4]).longValue(),
                (String) r[5],
                r[6] == null ? null : ((Number) r[6]).intValue(),
                (String) r[7]
        )).toList();
    }

    private static void appendFromWhere(StringBuilder sql, List<Object[]> binds, String qnaType,
                                        String qnaAnswerCode, String where, String query,
                                        String searchStartDate, String searchEndDate,
                                        List<Long> userIdFilter) {
        // AS-IS: 문의유형 라벨로 검색하려고 공통코드를 LEFT JOIN 한다
        sql.append("""
                  from admin.op_qna q
                  left join admin.op_common_code c
                    on c.id = q.qna_group and c.code_type = 'QNA_GROUPS' and c.use_yn = 'Y'
                 where q.data_status_code = '0'
                   and q.display_flag = 'Y'
                   and q.qna_type = :qnaType
                """);
        binds.add(new Object[]{"qnaType", qnaType});

        if ("0".equals(qnaAnswerCode)) {
            sql.append(" and q.answer_count > 0");
        } else if ("1".equals(qnaAnswerCode)) {
            sql.append(" and q.answer_count = 0");
        }

        if (query != null && !query.isBlank()) {
            String keyword = query.trim();
            switch (where == null ? "" : where) {
                case "GROUP" -> {
                    sql.append(" and c.label like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + keyword + "%"});
                }
                case "SUBJECT" -> {
                    sql.append(" and q.subject like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + keyword + "%"});
                }
                case "QUESTION" -> {
                    sql.append(" and q.question like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + keyword + "%"});
                }
                // AS-IS는 이 둘을 LIKE가 아니라 정확일치로 건다(주석에 LIKE가 남아 있다)
                case "USER_NAME" -> {
                    sql.append(" and q.user_name = :exactQuery");
                    binds.add(new Object[]{"exactQuery", keyword});
                }
                case "EMAIL" -> {
                    sql.append(" and q.email = :exactQuery");
                    binds.add(new Object[]{"exactQuery", keyword});
                }
                case "LOGIN_ID" -> {
                    // AS-IS는 OP_USER를 조인해 LOGIN_ID LIKE를 건다. TO-BE는 회원이 member 소유라
                    // 먼저 회원ID를 받아 와 그걸로 거른다(5112와 같은 방식).
                    if (userIdFilter == null || userIdFilter.isEmpty()) {
                        sql.append(" and 1 = 0");
                    } else {
                        sql.append(" and q.user_id in (:userIds)");
                        binds.add(new Object[]{"userIds", userIdFilter});
                    }
                }
                default -> {
                    // 검색구분이 비면 AS-IS도 조건을 걸지 않는다
                }
            }
        }

        if (searchStartDate != null && !searchStartDate.isBlank()) {
            sql.append(" and q.created_date >= :startDate");
            binds.add(new Object[]{"startDate", searchStartDate + "000000"});
        }
        if (searchEndDate != null && !searchEndDate.isBlank()) {
            sql.append(" and q.created_date <= :endDate");
            binds.add(new Object[]{"endDate", searchEndDate + "235959"});
        }
    }
}

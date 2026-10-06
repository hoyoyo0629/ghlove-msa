package com.ghlove.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Q&A 관리(메뉴 5112) 목록 조회 - AS-IS {@code QnaMapper.getFrontQnaOpenManagerList}/
 * {@code ...Count} 이식(조회 전용).
 *
 * <p><b>AS-IS 고정 조건 그대로</b>: {@code QNA_TYPE = 2}(공개 Q&A) ·
 * {@code DATA_STATUS_CODE = 0} · {@code DISPLAY_FLAG = 'Y'} · {@code USE_YN = 'Y'}.
 * {@code qna_type}은 1:1문의 '0' / 상품문의 '1' / <b>Q&A '2'</b>로, 세 화면이 같은 표
 * {@code op_qna}를 공유하고 이 값으로 갈린다.
 *
 * <p><b>AS-IS 그대로 둔 것</b>:
 * <ul>
 *   <li>답변을 행으로 펼치는 {@code UNION ALL} 블록이 <b>주석 처리</b>돼 있어 질문만 나온다 -
 *       켜지 않는다({@link com.ghlove.admin.web.QnaAdminController} 주석 참고).</li>
 *   <li>바깥쪽에서 {@code OP_QNA_ANSWER}를 LEFT JOIN하므로 <b>답변이 2건 이상이면 행이 중복</b>된다.
 *       AS-IS 그대로다(답변은 보통 1건이고, 화면도 1건을 전제로 그린다).</li>
 *   <li>정렬이 {@code orderBy='CREATED_DATE'}인데 실제로는 <b>{@code QNA_ID} 기준</b>이다
 *       (AS-IS CASE가 CREATED_DATE를 QNA_ID로 매핑해 둔다). 등록일시와 순서가 어긋날 수 있지만 그대로 둔다.</li>
 * </ul>
 *
 * <p><b>TO-BE 경계 처리</b>: AS-IS는 {@code OP_USER}를 조인해 {@code LOGIN_ID}를 같이 뽑지만
 * TO-BE에서 회원은 <b>member 소유</b>라 조인하지 않는다 - {@code user_id}만 내려주고
 * 로그인ID는 서비스가 member에서 받아 채운다. 검색구분 '아이디'도 member에서 회원ID를 먼저 받아
 * {@code user_id IN (...)}으로 거른다.
 */
@Repository
@RequiredArgsConstructor
public class QnaOpenAdminRepository {

    /** AS-IS Qna.QNA_GROUP_TYPE_QNA. */
    private static final String QNA_TYPE_OPEN = "2";

    /** 문의유형 라벨을 가져오는 공통코드 - AS-IS가 조인으로 바로 쓴다. */
    private static final String QNA_GROUPS = "QNA_GROUPS";

    private final EntityManager entityManager;

    /** 목록 한 행. {@code loginId}는 서비스가 member에서 채운다. */
    public record Row(Integer qnaId, String createdDate, Long userId, String userName,
                      String subject, Integer hits, String secretFlag, String content,
                      String qnaGroup, String qnaGroupNm, Long answerCnt, Long answerUserId,
                      String answerDate) {
    }

    /** AS-IS getFrontQnaOpenManagerListCount. */
    public int count(String qnaOpenAnswerCode, String where, String query,
                     String searchStartDate, String searchEndDate, Collection<Long> userIdFilter) {
        StringBuilder sql = new StringBuilder("select count(*) from (");
        List<Object[]> binds = new ArrayList<>();
        appendInner(sql, binds, qnaOpenAnswerCode, where, query, userIdFilter);
        sql.append(") a left outer join admin.op_qna_answer b on b.qna_id = a.qna_id where 1=1");
        appendDateRange(sql, binds, searchStartDate, searchEndDate);

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS getFrontQnaOpenManagerList. */
    @SuppressWarnings("unchecked")
    public List<Row> list(String qnaOpenAnswerCode, String where, String query,
                          String searchStartDate, String searchEndDate, Collection<Long> userIdFilter,
                          int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                select a.qna_id, a.created_date, a.user_id, a.user_name, a.subject, a.hits,
                       a.secret_flag, a.content, a.qna_group, a.qna_group_nm,
                       (select count(*) from admin.op_qna_answer qa where qa.qna_id = a.qna_id),
                       coalesce(b.user_id, 0), b.answer_date
                  from (
                """);
        List<Object[]> binds = new ArrayList<>();
        appendInner(sql, binds, qnaOpenAnswerCode, where, query, userIdFilter);
        sql.append(") a left outer join admin.op_qna_answer b on b.qna_id = a.qna_id where 1=1");
        appendDateRange(sql, binds, searchStartDate, searchEndDate);
        // AS-IS: orderBy=CREATED_DATE 이면 QNA_ID 기준으로 정렬한다(매퍼 CASE 그대로)
        sql.append(" order by a.qna_id desc, coalesce(b.qna_answer_id, 0)")
                .append(" offset :offset limit :limit");

        Query q = entityManager.createNativeQuery(sql.toString());
        binds.forEach(bind -> q.setParameter((String) bind[0], bind[1]));
        q.setParameter("offset", offset);
        q.setParameter("limit", limit);

        List<Object[]> rows = q.getResultList();
        List<Row> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new Row(num(r[0]), str(r[1]), lng(r[2]), str(r[3]), str(r[4]), num(r[5]),
                    str(r[6]), str(r[7]), str(r[8]), str(r[9]), lng(r[10]), lng(r[11]),
                    str(r[12])));
        }
        return result;
    }

    /** AS-IS 안쪽 SELECT - 질문 행만 만든다(답변 UNION은 주석 상태 유지). */
    private static void appendInner(StringBuilder sql, List<Object[]> binds, String qnaOpenAnswerCode,
                                    String where, String query, Collection<Long> userIdFilter) {
        sql.append("""
                select q.qna_id, q.created_date, q.user_id, q.user_name, q.subject, q.hits,
                       q.secret_flag, q.question as content, q.qna_group, c.label as qna_group_nm
                  from admin.op_qna q
                  left join admin.op_common_code c
                    on c.id = q.qna_group and c.code_type = :qnaGroups and c.use_yn = 'Y'
                 where q.qna_type = :qnaType
                   and q.data_status_code = '0'
                   and q.display_flag = 'Y'
                   and q.use_yn = 'Y'
                """);
        binds.add(new Object[]{"qnaGroups", QNA_GROUPS});
        binds.add(new Object[]{"qnaType", QNA_TYPE_OPEN});

        if ("0".equals(qnaOpenAnswerCode)) {
            sql.append(" and q.answer_count > 0");
        } else if ("1".equals(qnaOpenAnswerCode)) {
            sql.append(" and q.answer_count = 0");
        }

        if (notBlank(query)) {
            switch (where == null ? "" : where) {
                case "GROUP" -> {
                    sql.append(" and c.label like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                }
                case "SUBJECT" -> {
                    sql.append(" and q.subject like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                }
                case "QUESTION" -> {
                    sql.append(" and q.question like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                }
                case "USER_NAME" -> {
                    sql.append(" and q.user_name like :likeQuery");
                    binds.add(new Object[]{"likeQuery", "%" + query + "%"});
                }
                case "LOGIN_ID" -> {
                    // AS-IS는 OP_USER.LOGIN_ID LIKE로 걸지만 회원은 member 소유다 -
                    // 서비스가 member에서 받아 온 회원ID로 거른다(없으면 0건).
                    if (userIdFilter == null || userIdFilter.isEmpty()) {
                        sql.append(" and 1 = 0");
                    } else {
                        sql.append(" and q.user_id in (:userIds)");
                        binds.add(new Object[]{"userIds", userIdFilter});
                    }
                }
                default -> {
                    // AS-IS는 검색구분이 비면 조건을 붙이지 않는다
                }
            }
        }
    }

    /** AS-IS: created_date가 varchar(14)라 뒤에 000000/235959를 붙여 문자열 비교한다. */
    private static void appendDateRange(StringBuilder sql, List<Object[]> binds,
                                        String searchStartDate, String searchEndDate) {
        if (notBlank(searchStartDate)) {
            sql.append(" and a.created_date >= :startDate");
            binds.add(new Object[]{"startDate", searchStartDate + "000000"});
        }
        if (notBlank(searchEndDate)) {
            sql.append(" and a.created_date <= :endDate");
            binds.add(new Object[]{"endDate", searchEndDate + "235959"});
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Integer num(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private static Long lng(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }
}

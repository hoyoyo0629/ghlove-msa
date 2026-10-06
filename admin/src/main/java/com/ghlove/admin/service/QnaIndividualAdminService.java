package com.ghlove.admin.service;

import com.ghlove.admin.domain.Qna;
import com.ghlove.admin.domain.QnaAnswer;
import com.ghlove.admin.repository.QnaAnswerRepository;
import com.ghlove.admin.repository.QnaIndividualAdminRepository;
import com.ghlove.admin.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1:1 문의 관리 (메뉴 5102) - AS-IS {@code QnaManagerController}
 * ({@code /opmanager/qna})의 목록·답변 저장을 이식한 것이다.
 *
 * <p><b>5112 Q&A와 같은 표를 쓰고 구분은 {@code QNA_TYPE}이다</b> -
 * AS-IS 상수 {@code Qna.QNA_GROUP_TYPE_INDIVIDUAL = "0"}(1:1문의) /
 * {@code QNA_GROUP_TYPE_ITEM = "1"}(상품문의, 5103 중지) /
 * {@code QNA_GROUP_TYPE_QNA = "2"}(Q&A, 5112).
 *
 * <p><b>AS-IS 그대로</b>:
 * <ul>
 *   <li>진입(GET)은 <b>조회하지 않고 빈 목록</b>이다 - 검색(POST)해야 조회된다.</li>
 *   <li>작성자명을 <b>마스킹하지 않는다</b>(5112 Q&A 목록은 마스킹한다 - 두 화면이 다르다).</li>
 *   <li>답변 저장 시 <b>답변제목을 화면에서 받는다</b>(기본값 "문의에 대한 답변입니다.").
 *       5112는 제목이 hidden 고정값("답변입니다.")이다.</li>
 *   <li><b>국민비서 문자를 보내지 않는다</b> - 5112는 답변 저장마다 {@code sendSmsQnaAnswer}를
 *       무조건 부르지만 이 화면은 그 호출이 없다. 메일·UMS 분기는 5112와 같이
 *       화면에 체크박스가 없어 닿지 않는 코드다({@link QnaOpenAdminService#answer} 주석 참고).</li>
 *   <li>{@code qnaTypes} 모델값은 항상 빈 목록이다(5112와 같은 이유 - {@code QNA_GROUPS}의
 *       {@code code_value}가 비어 있어 존재하지 않는 코드유형을 찾는다).</li>
 * </ul>
 *
 * <p><b>삭제·첨부는 {@link QnaOpenAdminService}의 것을 그대로 쓴다</b> - AS-IS도 두 화면이
 * 같은 {@code QnaService} 메서드({@code deleteQnaData}·{@code deleteQna}·{@code deleteQnaAnswer}·
 * {@code deleteItemImageByItemId})를 호출하기 때문이다. 같은 규칙·같은 결함 보정이 적용된다.
 */
@Service
@RequiredArgsConstructor
public class QnaIndividualAdminService {

    /** AS-IS Qna.QNA_GROUP_TYPE_INDIVIDUAL - 1:1 문의. */
    public static final String QNA_TYPE_INDIVIDUAL = "0";

    private static final DateTimeFormatter ANSWER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final QnaIndividualAdminRepository qnaIndividualAdminRepository;
    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final MemberAdminClient memberAdminClient;

    /** 목록 한 행 - 화면이 쓰는 모양 그대로. */
    public record QnaRow(Integer qnaId, String qnaGroup, String qnaGroupNm, String subject,
                         Long userId, String userName, String loginId, Integer answerCount,
                         String createdDate) {

        /** AS-IS 화면은 답변 건수로 상태를 표시한다(답변완료/답변대기). */
        public boolean answered() {
            return answerCount != null && answerCount > 0;
        }
    }

    public int count(String qnaAnswerCode, String where, String query,
                     String searchStartDate, String searchEndDate) {
        return qnaIndividualAdminRepository.count(QNA_TYPE_INDIVIDUAL, qnaAnswerCode, where, query,
                searchStartDate, searchEndDate, userIdFilter(where, query));
    }

    public List<QnaRow> list(String qnaAnswerCode, String where, String query,
                             String searchStartDate, String searchEndDate, int offset, int limit) {
        List<QnaIndividualAdminRepository.Row> rows = qnaIndividualAdminRepository.list(
                QNA_TYPE_INDIVIDUAL, qnaAnswerCode, where, query, searchStartDate, searchEndDate,
                userIdFilter(where, query), offset, limit);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = rows.stream().map(QnaIndividualAdminRepository.Row::userId)
                .filter(id -> id != null && id > 0).distinct().toList();
        Map<Long, String> loginIds = memberAdminClient.userLoginIds(userIds);

        List<QnaRow> result = new ArrayList<>(rows.size());
        for (QnaIndividualAdminRepository.Row r : rows) {
            result.add(new QnaRow(r.qnaId(), r.qnaGroup(), r.qnaGroupNm(), r.subject(), r.userId(),
                    // AS-IS 1:1문의 목록은 작성자명을 마스킹하지 않는다
                    r.userName(), r.userId() == null ? null : loginIds.get(r.userId()),
                    r.answerCount(), r.createdDate()));
        }
        return result;
    }

    /**
     * AS-IS qnaAnswerAction (POST answer/{qnaId}) - 답변 등록·수정.
     * 답변이 있으면 UPDATE, 없으면 INSERT → {@code ANSWER_COUNT}를 실제 답변 수로 다시 계산.
     * <b>문자·메일 발송은 없다</b>(클래스 주석 참고).
     *
     * <p>답변자({@code USER_ID})는 AS-IS가 화면 hidden으로 받지만 그 값이 로그인한 매니저라
     * 세션에서 직접 가져온다(5112와 같은 처리 - 결과는 같고 위조할 수 없다).
     */
    @Transactional
    public void answer(Integer qnaId, Long managerUserId, String title, String content) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new QnaException("문의를 찾을 수 없습니다."));

        QnaAnswer answer = qnaAnswerRepository.findFirstByQnaIdOrderByAnswerDateAsc(qnaId)
                .orElseGet(QnaAnswer::new);
        answer.setQnaId(qnaId);
        answer.setTitle(title);
        answer.setAnswer(content);
        answer.setUserId(managerUserId);
        answer.setAnswerDate(LocalDateTime.now().format(ANSWER_DATE));
        answer.setSendSmsFlag("N");
        answer.setSendMailFlag("N");
        answer.setDataStatusCode("0");
        qnaAnswerRepository.save(answer);

        qna.setAnswerCount(qnaAnswerRepository.findByQnaIdIn(List.of(qnaId)).size());
        qnaRepository.save(qna);
    }

    /**
     * AS-IS qnaUpdateAction (POST edit/{qnaId}) - 5112와 <b>똑같은 결함</b>이 있는 자리다:
     * {@code Qna}와 {@code QnaAnswer}를 둘 다 받고 {@code qna.setQnaId(qnaId)}까지 해 두고도
     * {@code updateQnaAnswer} 하나만 불러 <b>문의 본문은 저장되지 않는다</b>.
     * 이 URL로 전송하는 화면도 없다(답변화면 form의 action이 {@code answer/{qnaId}} 고정).
     * AS-IS 동작(답변만 저장) 그대로 옮긴다.
     */
    @Transactional
    public void updateAnswerOnly(Integer qnaAnswerId, Long managerUserId, String title, String content) {
        if (qnaAnswerId == null) {
            return;
        }
        qnaAnswerRepository.findById(qnaAnswerId).ifPresent(answer -> {
            answer.setTitle(title);
            answer.setAnswer(content);
            answer.setUserId(managerUserId);
            answer.setAnswerDate(LocalDateTime.now().format(ANSWER_DATE));
            answer.setSendSmsFlag("N");
            answer.setSendMailFlag("N");
            qnaAnswerRepository.save(answer);
        });
    }

    /** 검색구분이 '아이디'일 때만 member에서 회원ID를 받아 온다(5112와 같은 경계 처리). */
    private List<Long> userIdFilter(String where, String query) {
        if (!"LOGIN_ID".equals(where) || query == null || query.isBlank()) {
            return List.of();
        }
        return memberAdminClient.userIdsByLoginIdLike(query.trim());
    }
}

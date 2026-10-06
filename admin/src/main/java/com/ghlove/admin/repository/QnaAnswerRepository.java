package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Integer> {

    Optional<QnaAnswer> findByQnaId(Integer qnaId);

    List<QnaAnswer> findByQnaIdIn(List<Integer> qnaIds);

    /**
     * AS-IS {@code getQnaAnswerByQnaId}의 {@code ORDER BY ANSWER_DATE LIMIT 1} 그대로.
     * {@code OP_QNA_ANSWER}에는 {@code QNA_ID} 유일제약이 없어 한 문의에 답변이 두 행 이상
     * 생길 수 있고(그래서 AS-IS가 답변 건수를 COUNT로 다시 계산한다), 그때 {@link #findByQnaId}는
     * 예외로 터진다. Q&A 관리(5112)는 AS-IS와 같이 <b>가장 먼저 등록된 한 건</b>만 본다.
     */
    Optional<QnaAnswer> findFirstByQnaIdOrderByAnswerDateAsc(Integer qnaId);
}

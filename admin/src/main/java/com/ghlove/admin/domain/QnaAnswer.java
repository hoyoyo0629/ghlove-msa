package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 1:1 문의 답변. 운영자 답변 작성 화면(QnaAdminController) 추가 완료 - USER_ID는 답변한
 *  매니저의 USER_ID(AS-IS view_search_qna가 이 컬럼으로 OP_MANAGER/OP_USER를 조회해
 *  답변자명을 표시한다). */
@Entity
@Table(name = "OP_QNA_ANSWER")
@Getter
@Setter
@NoArgsConstructor
public class QnaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQnaAnswerIdSeq")
    @SequenceGenerator(name = "opQnaAnswerIdSeq", sequenceName = "op_qna_answer_qna_answer_id_seq", allocationSize = 1)
    @Column(name = "QNA_ANSWER_ID")
    private Integer qnaAnswerId;

    @Column(name = "QNA_ID")
    private Integer qnaId;

    @Column(name = "ANSWER")
    private String answer;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "ANSWER_DATE")
    private String answerDate;

    @Column(name = "USER_ID")
    private Long userId;
}

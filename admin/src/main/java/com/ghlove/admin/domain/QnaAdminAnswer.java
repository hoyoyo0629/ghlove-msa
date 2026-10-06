package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 내부문의 답변(본사 운영자). AS-IS G_QNA_ADMIN_ANSWER. 문의 1건에 답변 1건 관례. */
@Entity
@Table(name = "G_QNA_ADMIN_ANSWER")
@Getter
@Setter
@NoArgsConstructor
public class QnaAdminAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gQnaAdminAnswerIdSeq")
    @SequenceGenerator(name = "gQnaAdminAnswerIdSeq", sequenceName = "g_qna_admin_answer_id_seq", allocationSize = 1)
    @Column(name = "QNA_ADMIN_ANSWER_ID")
    private Long qnaAdminAnswerId;

    @Column(name = "QNA_ADMIN_ID")
    private Long qnaAdminId;

    @Column(name = "ANSWER")
    private String answer;

    /** 답변 작성 매니저 userId. */
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "SEND_SMS_FLAG")
    private String sendSmsFlag;

    @Column(name = "SEND_MAIL_FLAG")
    private String sendMailFlag;

    @Column(name = "ANSWER_DATE")
    private LocalDateTime answerDate;

    /** 데이터 상태코드 - '0': 정상, '1': 삭제. */
    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode;

    @Column(name = "HITS")
    private Long hits;

    @Column(name = "SECRET_FLAG")
    private String secretFlag;

    @Column(name = "FILLER1")
    private String filler1;
}

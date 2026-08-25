package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 1:1 문의 (마이페이지 "1:1 문의"). Maps a subset of the AS-IS OP_QNA columns.
 * ITEM_ID/SELLER_ID/ORDER_CODE stay null for a general inquiry - AS-IS reuses this same
 * table for per-item/per-order inquiries too, but this MSA only wires up the general case.
 */
@Entity
@Table(name = "OP_QNA")
@Getter
@Setter
@NoArgsConstructor
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQnaIdSeq")
    @SequenceGenerator(name = "opQnaIdSeq", sequenceName = "op_qna_qna_id_seq", allocationSize = 1)
    @Column(name = "QNA_ID")
    private Integer qnaId;

    @Column(name = "QNA_GROUP")
    private String qnaGroup;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "QUESTION")
    private String question;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    /** yyyyMMddHHmmss (AS-IS CommonMapper.datetime 컨벤션). */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "ANSWER_COUNT")
    private Integer answerCount;

    /** "Y"면 작성자 본인만 상세를 볼 수 있다(고객센터 Q&A 공개게시판에서만 의미가 있다). */
    @Column(name = "SECRET_FLAG")
    private String secretFlag;

    @Column(name = "HITS")
    private Integer hits;

    /** "N"이면 공개게시판 목록에서 숨긴다(운영자 비노출 처리). null/"Y"는 노출. */
    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;
}

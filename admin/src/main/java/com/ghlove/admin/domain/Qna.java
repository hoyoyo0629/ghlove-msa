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

    /**
     * 문의 구분 - AS-IS {@code Qna} 상수 그대로 <b>'0' 1:1문의 / '1' 상품문의 / '2' 공개 Q&A</b>다.
     * 세 화면(5102·5103·5112)이 이 표를 공유하고 이 값으로 갈린다 - Q&A 관리(5112)는
     * {@code qna_type='2'}만 조회한다({@link com.ghlove.admin.repository.QnaOpenAdminRepository}).
     */
    @Column(name = "QNA_TYPE")
    private String qnaType;

    /**
     * 데이터 상태 - AS-IS는 <b>'0' 정상 / '1' 삭제</b>다. 삭제는 행을 지우지 않고 이 값을 '1'로
     * 바꾸는 소프트 삭제이고, 목록 조회는 {@code data_status_code='0'}만 본다.
     */
    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode;

    /** AS-IS 공통 사용여부. 목록 조회는 'Y'만 본다. */
    @Column(name = "USE_YN")
    private String useYn;
}

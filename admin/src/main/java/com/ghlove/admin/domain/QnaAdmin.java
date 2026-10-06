package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 내부문의(지자체담당자 → 본사 운영자) 문의 본문. AS-IS G_QNA_ADMIN / QnaAdminManagerController.
 * 지자체 담당자(ROLE_ADMIN_5/6)가 자기 지자체 명의로 본사에 문의를 등록하고, 본사(ROLE_ADMIN_1~4)가
 * 답변한다. 일반 1:1문의(OP_QNA, {@code QnaAdminController})와는 별개 테이블·별개 도메인이다.
 */
@Entity
@Table(name = "G_QNA_ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class QnaAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gQnaAdminIdSeq")
    @SequenceGenerator(name = "gQnaAdminIdSeq", sequenceName = "g_qna_admin_id_seq", allocationSize = 1)
    @Column(name = "QNA_ADMIN_ID")
    private Long qnaAdminId;

    /** 문의유형 (공통코드 QNA_GROUPS). */
    @Column(name = "QNA_GROUP")
    private String qnaGroup;

    @Column(name = "QNA_TYPE")
    private String qnaType;

    @Column(name = "SUBJECT")
    private String subject;

    /** 질문 본문. */
    @Column(name = "QUESTION")
    private String question;

    /** 등록자(문의 작성 매니저) userId. */
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ANSWER_COUNT")
    private Integer answerCount;

    @Column(name = "SECRET_FLAG")
    private String secretFlag;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    /** 상태(Y: 사용, 삭제 시 사용안함). */
    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode;

    @Column(name = "QNA_IMAGE")
    private String qnaImage;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "HITS")
    private Long hits;

    /** 문의를 올린 지자체 코드. */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;
}

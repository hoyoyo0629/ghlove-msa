package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 관리자 문의(AS-IS G_QNA_ADMIN) - <b>답례품 제공자(판매자)가 자기 지자체에 남기는 문의</b>. 판매자 포털의 "지자체 문의"
 * (AS-IS /seller/qna-locgov)에서 쓰고, 운영관리 "관리자 문의"(메뉴 16651, AS-IS /opmanager/qna-admin)에서 그 지자체 담당자가 답한다.
 * USER_ID는 판매자 계정 번호(OP_SELLER_USER), LOCGOV_CODE는 문의 받은 지자체.
 * 상태: DATA_STATUS_CODE '0' 정상 / '1' 삭제, DISPLAY_FLAG 'Y'만 목록에 나온다(AS-IS insert 고정값).
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

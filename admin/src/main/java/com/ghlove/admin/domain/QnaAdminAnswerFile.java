package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 관리자 문의 답변 첨부파일(AS-IS G_QNA_ADMIN_ANSWER_FILE) - 지자체 담당자가 답변에 올린다. */
@Entity
@Table(name = "G_QNA_ADMIN_ANSWER_FILE")
@Getter
@Setter
@NoArgsConstructor
public class QnaAdminAnswerFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gQnaAdminAnswerFileIdSeq")
    @SequenceGenerator(name = "gQnaAdminAnswerFileIdSeq", sequenceName = "g_qna_admin_answer_file_id_seq", allocationSize = 1)
    @Column(name = "QNA_ADMIN_ANSWER_FILE_ID")
    private Long qnaAdminAnswerFileId;

    @Column(name = "QNA_ADMIN_ANSWER_ID")
    private Long qnaAdminAnswerId;

    /** 서버에 저장된 파일명. */
    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_TY")
    private String fileTy;

    @Column(name = "ORDERING")
    private Integer ordering;

    /** NOT NULL DEFAULT now() 컬럼 - 저장할 때 직접 채운다. */
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    /** 올릴 때의 원래 파일명. */
    @Column(name = "ORG_FILE_NAME")
    private String orgFileName;

    /** 이관 파일의 저장 폴더(업로드 루트 기준). 비어 있으면 qnaadminanswer/{문의번호}/ 아래에 있다. */
    @Column(name = "PATH_NAME")
    private String pathName;
}

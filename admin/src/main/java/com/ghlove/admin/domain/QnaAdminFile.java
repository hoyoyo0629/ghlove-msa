package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 관리자 문의 첨부파일(AS-IS G_QNA_ADMIN_FILE) - 판매자가 문의를 쓸 때 올린다. 운영관리 화면은 내려받기만 한다. */
@Entity
@Table(name = "G_QNA_ADMIN_FILE")
@Getter
@Setter
@NoArgsConstructor
public class QnaAdminFile {

    @Id
    @Column(name = "QNA_ADMIN_FILE_ID")
    private Long qnaAdminFileId;

    @Column(name = "QNA_ADMIN_ID")
    private Long qnaAdminId;

    /** 서버에 저장된 파일명. */
    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_TY")
    private String fileTy;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    /** 올릴 때의 원래 파일명. */
    @Column(name = "ORG_FILE_NAME")
    private String orgFileName;

    /** 이관 파일의 저장 폴더(업로드 루트 기준). 비어 있으면 qnaadmin/{문의번호}/ 아래에 있다. */
    @Column(name = "PATH_NAME")
    private String pathName;
}

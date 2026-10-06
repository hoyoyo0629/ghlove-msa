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

/**
 * 이메일 발송 첨부파일 (AS-IS opmanager/email - OP_EMAIL_FILE).
 * AS-IS 화면은 input[name=files] 하나만 두므로 실제로는 메일 한 건당 0~1개다.
 */
@Entity
@Table(name = "OP_EMAIL_FILE")
@Getter
@Setter
@NoArgsConstructor
public class OpEmailFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opEmailFileIdSeq")
    @SequenceGenerator(name = "opEmailFileIdSeq", sequenceName = "op_email_file_email_file_id_seq",
            allocationSize = 1)
    @Column(name = "EMAIL_FILE_ID")
    private Integer emailFileId;

    @Column(name = "EMAIL_ID")
    private Long emailId;

    /** 디스크에 저장한 파일명. */
    @Column(name = "FILE_NAME")
    private String fileName;

    /** 확장자. */
    @Column(name = "FILE_TY")
    private String fileTy;

    @Column(name = "ORDERING")
    private Integer ordering;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "ORG_FILE_NAME")
    private String orgFileName;
}

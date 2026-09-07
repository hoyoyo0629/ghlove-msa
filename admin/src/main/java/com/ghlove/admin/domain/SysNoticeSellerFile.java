package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** OP_SYS_NOTICE_SELLER_FILE - 판매자 시스템공지 첨부파일. */
@Entity
@Table(name = "OP_SYS_NOTICE_SELLER_FILE")
@Getter
@Setter
@NoArgsConstructor
public class SysNoticeSellerFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opSysNoticeSellerFileIdSeq")
    @SequenceGenerator(name = "opSysNoticeSellerFileIdSeq", sequenceName = "op_sys_notice_seller_file_file_id_seq", allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "NOTICE_ID")
    private Long noticeId;

    @Column(name = "ORGNL_ATCH_FILE_NM")
    private String orgnlAtchFileNm;

    @Column(name = "ATCH_FILE_NM")
    private String atchFileNm;

    @Column(name = "ATCH_FILE_EXTN_NM")
    private String atchFileExtnNm;

    @Column(name = "ATCH_FILE_SZ")
    private Long atchFileSz;

    @Column(name = "ATCH_FILE_SEQ")
    private Integer atchFileSeq;

    @Column(name = "ATCH_FILE_PATH_NM")
    private String atchFilePathNm;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;
}

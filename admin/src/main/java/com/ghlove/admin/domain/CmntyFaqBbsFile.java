package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 담당자용 FAQ 본문 첨부파일 (AS-IS {@code G_CMNTY_FAQ_BBS_FILE}).
 *  구조는 {@link CmntySrBbsFile}과 같다. */
@Entity
@Table(name = "G_CMNTY_FAQ_BBS_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CmntyFaqBbsFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyFaqBbsFileIdSeq")
    @SequenceGenerator(name = "cmntyFaqBbsFileIdSeq",
            sequenceName = "g_cmnty_faq_bbs_file_file_id_seq", allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "BBS_ID")
    private Long bbsId;

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

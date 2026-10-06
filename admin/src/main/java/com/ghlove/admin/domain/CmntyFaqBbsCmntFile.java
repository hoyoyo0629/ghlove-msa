package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 담당자용 FAQ <b>댓글</b> 첨부파일 (AS-IS {@code G_CMNTY_FAQ_BBS_CMNT_FILE}).
 *  AS-IS 화면이 이 기능을 노출하지 않는다 - {@link CmntyFaqBbsCmnt} 주석 참고. */
@Entity
@Table(name = "G_CMNTY_FAQ_BBS_CMNT_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CmntyFaqBbsCmntFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyFaqBbsCmntFileIdSeq")
    @SequenceGenerator(name = "cmntyFaqBbsCmntFileIdSeq",
            sequenceName = "g_cmnty_faq_bbs_cmnt_file_file_id_seq", allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "CMNT_ID")
    private Long cmntId;

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

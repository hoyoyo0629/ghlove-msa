package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SR게시판 <b>댓글</b> 첨부파일 (AS-IS {@code G_CMNTY_SR_BBS_CMNT_FILE}).
 * 본문 첨부({@link CmntySrBbsFile})와 구조가 같고 {@code BBS_ID} 대신 {@code CMNT_ID}를 갖는다.
 */
@Entity
@Table(name = "G_CMNTY_SR_BBS_CMNT_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CmntySrBbsCmntFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntySrBbsCmntFileIdSeq")
    @SequenceGenerator(name = "cmntySrBbsCmntFileIdSeq",
            sequenceName = "g_cmnty_sr_bbs_cmnt_file_file_id_seq", allocationSize = 1)
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

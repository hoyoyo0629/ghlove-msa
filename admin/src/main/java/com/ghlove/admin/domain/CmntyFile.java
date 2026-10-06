package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 자료실 첨부파일 (AS-IS {@code G_CMNTY_FILE}) - 게시글이 {@link CmntyRpstr}이므로
 * 다른 게시판 첨부표와 달리 {@code BBS_ID}가 아니라 {@code RPSTR_ID}를 갖는다.
 *
 * <p>삭제는 {@code USE_YN='N'} 소프트 삭제이고 디스크 파일은 지우지 않는다(AS-IS 동일).
 * 게시글을 삭제해도 AS-IS는 <b>첨부 행을 건드리지 않는다</b>(게시글만 USE_YN='N') - 그대로 따랐다.
 */
@Entity
@Table(name = "G_CMNTY_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CmntyFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyFileIdSeq")
    @SequenceGenerator(name = "cmntyFileIdSeq", sequenceName = "g_cmnty_file_file_id_seq",
            allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "RPSTR_ID")
    private Long rpstrId;

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

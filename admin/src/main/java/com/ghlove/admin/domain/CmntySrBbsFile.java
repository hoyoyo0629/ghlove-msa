package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SR게시판 본문 첨부파일 (AS-IS {@code G_CMNTY_SR_BBS_FILE}).
 *
 * <p>{@code ATCH_FILE_SEQ}는 게시글 안에서의 순번으로 AS-IS가 INSERT 시
 * {@code (select ifnull(max(atch_file_seq)+1, 1) ...)}로 채운다 - NOT NULL이라 반드시 넣어야 한다.
 * 삭제는 {@code USE_YN='N'} 소프트 삭제이고 디스크 파일은 지우지 않는다(AS-IS 동일).
 */
@Entity
@Table(name = "G_CMNTY_SR_BBS_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CmntySrBbsFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntySrBbsFileIdSeq")
    @SequenceGenerator(name = "cmntySrBbsFileIdSeq", sequenceName = "g_cmnty_sr_bbs_file_file_id_seq",
            allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "BBS_ID")
    private Long bbsId;

    /** 사용자가 올린 원래 파일명 - 다운로드할 때 이 이름으로 내려준다. */
    @Column(name = "ORGNL_ATCH_FILE_NM")
    private String orgnlAtchFileNm;

    /** 디스크에 저장된 파일명. */
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

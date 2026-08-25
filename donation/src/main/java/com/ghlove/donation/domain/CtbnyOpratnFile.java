package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기부금 지출내역 증빙서류 (AS-IS G_CTBNY_OPRATN_FILE). */
@Entity
@Table(name = "G_CTBNY_OPRATN_FILE")
@Getter
@Setter
@NoArgsConstructor
public class CtbnyOpratnFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ctbnyOpratnFileIdSeq")
    @SequenceGenerator(name = "ctbnyOpratnFileIdSeq", sequenceName = "g_ctbny_opratn_file_regist_file_id_seq", allocationSize = 1)
    @Column(name = "REGIST_FILE_ID")
    private Long registFileId;

    @Column(name = "REGIST_SN")
    private Long registSn;

    @Column(name = "FILE_NM")
    private String fileNm;

    @Column(name = "ORGINL_FILE_NM")
    private String orginlFileNm;

    @Column(name = "FILE_TY")
    private String fileTy;

    @Column(name = "SORT_ORDR")
    private Integer sortOrdr;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체 자료실 (AS-IS opmanager/community/databoard) - 지자체 담당자용 자료 공유 게시판.
 *  공개 화면의 자료실(OP_DATA_BOARD, 시민 대상)과는 별개 테이블 - 첨부파일은 이번
 *  라운드 범위 밖(gift의 FileStorageService 패턴 재사용은 별도 작업으로 남겨둠). */
@Entity
@Table(name = "G_CMNTY_RPSTR")
@Getter
@Setter
@NoArgsConstructor
public class CmntyRpstr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyRpstrIdSeq")
    @SequenceGenerator(name = "cmntyRpstrIdSeq", sequenceName = "g_cmnty_rpstr_rpstr_id_seq", allocationSize = 1)
    @Column(name = "RPSTR_ID")
    private Long rpstrId;

    @Column(name = "RPSTR_TTL")
    private String rpstrTtl;

    @Column(name = "RPSTR_CN")
    private String rpstrCn;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "NOTICE_YN")
    private String noticeYn;

    @Column(name = "INQ_CNT")
    private Long inqCnt;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;
}

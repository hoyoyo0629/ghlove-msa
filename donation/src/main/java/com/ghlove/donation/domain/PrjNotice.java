package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 특정사업 상세화면 "공지사항" 탭 (AS-IS getNoticeList). */
@Entity
@Table(name = "G_DSGN_PRJ_NOTICE")
@Getter
@Setter
@NoArgsConstructor
public class PrjNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRJ_NOTICE_ID")
    private Long prjNoticeId;

    @Column(name = "DSGN_DNTN_BIZ_ID")
    private Long dsgnDntnBizId;

    @Column(name = "PRJ_NOTICE_SUBJECT")
    private String prjNoticeSubject;

    @Column(name = "PRJ_NOTICE_CN")
    private String prjNoticeCn;

    @Column(name = "FRST_REGIST_PNTTM")
    private String frstRegistPnttm;
}

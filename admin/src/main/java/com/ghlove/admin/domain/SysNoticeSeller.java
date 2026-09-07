package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 판매자(답례품제공자) 전용 시스템공지 (AS-IS opmanager/sellerNotice - SysNoticeSellerController,
 *  OP_SYS_NOTICE_SELLER). AS-IS 실제 권한: ROLE_ADMIN_1/2(시스템 정·부담당자)만 등록/수정/삭제
 *  가능 - {@link com.ghlove.admin.web.SysNoticeSellerAdminController#requireWriteAccess}. */
@Entity
@Table(name = "OP_SYS_NOTICE_SELLER")
@Getter
@Setter
@NoArgsConstructor
public class SysNoticeSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opSysNoticeSellerNoticeIdSeq")
    @SequenceGenerator(name = "opSysNoticeSellerNoticeIdSeq", sequenceName = "op_sys_notice_seller_notice_id_seq", allocationSize = 1)
    @Column(name = "NOTICE_ID")
    private Long noticeId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "HITS")
    private Long hits;

    @Column(name = "BOARD_CODE")
    private String boardCode;

    @Column(name = "SUB_CATEGORY")
    private Long subCategory;

    @Column(name = "NOTICE_FLAG")
    private String noticeFlag;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

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

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;
}

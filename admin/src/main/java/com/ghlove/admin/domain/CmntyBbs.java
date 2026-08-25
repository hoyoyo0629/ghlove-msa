package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 자유게시판 (AS-IS opmanager/community/bbs) - 지자체 담당자용 행안부 소통방. */
@Entity
@Table(name = "G_CMNTY_BBS")
@Getter
@Setter
@NoArgsConstructor
public class CmntyBbs extends CmntyBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyBbsIdSeq")
    @SequenceGenerator(name = "cmntyBbsIdSeq", sequenceName = "g_cmnty_bbs_bbs_id_seq", allocationSize = 1)
    @Column(name = "BBS_ID")
    private Long bbsId;
}

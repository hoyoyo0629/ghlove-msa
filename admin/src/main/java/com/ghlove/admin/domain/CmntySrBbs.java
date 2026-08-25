package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** SR(시스템요청)게시판 (AS-IS opmanager/community/sr-bbs) - 기능개선 건의/시스템 오류 신고. */
@Entity
@Table(name = "G_CMNTY_SR_BBS")
@Getter
@Setter
@NoArgsConstructor
public class CmntySrBbs extends CmntyBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntySrBbsIdSeq")
    @SequenceGenerator(name = "cmntySrBbsIdSeq", sequenceName = "g_cmnty_sr_bbs_bbs_id_seq", allocationSize = 1)
    @Column(name = "BBS_ID")
    private Long bbsId;
}

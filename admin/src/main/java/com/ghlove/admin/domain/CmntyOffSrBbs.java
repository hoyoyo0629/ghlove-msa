package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 오프라인SR게시판 (AS-IS opmanager/community/off-sr-bbs) - 오프라인기부(offgive) 관련 건의/오류. */
@Entity
@Table(name = "G_CMNTY_OFF_SR_BBS")
@Getter
@Setter
@NoArgsConstructor
public class CmntyOffSrBbs extends CmntyBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyOffSrBbsIdSeq")
    @SequenceGenerator(name = "cmntyOffSrBbsIdSeq", sequenceName = "g_cmnty_off_sr_bbs_bbs_id_seq", allocationSize = 1)
    @Column(name = "BBS_ID")
    private Long bbsId;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** FAQ게시판 (AS-IS opmanager/community/faq-bbs) - 지자체 담당자용 시스템 운영 FAQ
 *  (고객센터 FAQ인 LocgovFaq/OP_COMMUNITY_LOCGOVFAQ와는 별개 테이블). */
@Entity
@Table(name = "G_CMNTY_FAQ_BBS")
@Getter
@Setter
@NoArgsConstructor
public class CmntyFaqBbs extends CmntyBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyFaqBbsIdSeq")
    @SequenceGenerator(name = "cmntyFaqBbsIdSeq", sequenceName = "g_cmnty_faq_bbs_bbs_id_seq", allocationSize = 1)
    @Column(name = "BBS_ID")
    private Long bbsId;

    @Column(name = "FAQ_TYPE")
    private String faqType;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 담당자용 FAQ 댓글 (AS-IS {@code G_CMNTY_FAQ_BBS_CMNT}).
 *
 * <p>댓글 첨부 표({@link CmntyFaqBbsCmntFile})와 서버 엔드포인트는 AS-IS에 있지만
 * <b>화면이 그 기능을 노출하지 않는다</b>(파일등록 버튼·첨부 목록이 FAQ 상세화면에만 빠져 있다) -
 * 자세한 내용은 {@link com.ghlove.admin.web.CmntyFaqBbsAdminController} 주석 참고.
 */
@Entity
@Table(name = "G_CMNTY_FAQ_BBS_CMNT")
@Getter
@Setter
@NoArgsConstructor
public class CmntyFaqBbsCmnt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyFaqBbsCmntIdSeq")
    @SequenceGenerator(name = "cmntyFaqBbsCmntIdSeq",
            sequenceName = "g_cmnty_faq_bbs_cmnt_cmnt_id_seq", allocationSize = 1)
    @Column(name = "CMNT_ID")
    private Long cmntId;

    @Column(name = "BBS_ID")
    private Long bbsId;

    @Column(name = "CMNT_CN")
    private String cmntCn;

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

    /** AS-IS가 댓글에는 쓰지 않는 컬럼 - 표에 있으니 매핑만 한다. */
    @Column(name = "IS_SECRET")
    private String isSecret;
}

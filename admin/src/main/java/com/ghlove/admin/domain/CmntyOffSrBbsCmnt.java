package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 오프라인 담당자 SR게시판 댓글 (AS-IS {@code G_CMNTY_OFF_SR_BBS_CMNT}).
 *  SR게시판 댓글과 같이 <b>댓글에도 첨부파일이 붙는다</b>({@link CmntyOffSrBbsCmntFile}). */
@Entity
@Table(name = "G_CMNTY_OFF_SR_BBS_CMNT")
@Getter
@Setter
@NoArgsConstructor
public class CmntyOffSrBbsCmnt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyOffSrBbsCmntIdSeq")
    @SequenceGenerator(name = "cmntyOffSrBbsCmntIdSeq",
            sequenceName = "g_cmnty_off_sr_bbs_cmnt_cmnt_id_seq", allocationSize = 1)
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

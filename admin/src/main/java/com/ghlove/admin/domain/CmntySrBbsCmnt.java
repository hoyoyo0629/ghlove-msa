package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SR게시판 댓글 (AS-IS {@code G_CMNTY_SR_BBS_CMNT}). 소통방 댓글({@link CmntyCmnt})과 달리
 * <b>댓글에도 첨부파일이 붙는다</b>({@link CmntySrBbsCmntFile}).
 *
 * <p>{@code IS_SECRET} 컬럼은 표에 있지만 AS-IS가 댓글에는 넣지도 읽지도 않는다.
 */
@Entity
@Table(name = "G_CMNTY_SR_BBS_CMNT")
@Getter
@Setter
@NoArgsConstructor
public class CmntySrBbsCmnt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntySrBbsCmntIdSeq")
    @SequenceGenerator(name = "cmntySrBbsCmntIdSeq", sequenceName = "g_cmnty_sr_bbs_cmnt_cmnt_id_seq",
            allocationSize = 1)
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

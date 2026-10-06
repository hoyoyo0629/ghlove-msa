package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 소통방 댓글 (AS-IS {@code G_CMNTY_CMNT} - {@code opmanager/community/cmnt/*}).
 *
 * <p>AS-IS는 게시판 4종이 <b>각자 자기 댓글 테이블</b>을 갖는다 - 소통방은 이 표,
 * SR·담당자FAQ·오프라인SR은 {@code g_cmnty_{sr,faq,off_sr}_bbs_cmnt}(+ {@code _file})다.
 * 소통방 댓글만 첨부파일이 없다.
 *
 * <p>삭제는 {@code USE_YN='N'} 소프트 삭제다(AS-IS deleteCmnt). {@code IS_SECRET} 컬럼은
 * 표에 있지만 AS-IS가 댓글에는 넣지도 읽지도 않으므로 매핑만 해 두고 쓰지 않는다.
 */
@Entity
@Table(name = "G_CMNTY_CMNT")
@Getter
@Setter
@NoArgsConstructor
public class CmntyCmnt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cmntyCmntIdSeq")
    @SequenceGenerator(name = "cmntyCmntIdSeq", sequenceName = "g_cmnty_cmnt_cmnt_id_seq", allocationSize = 1)
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

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 커뮤니티 게시판 댓글 (AS-IS opmanager/community/comment - CommentManagerController,
 *  OP_COMMUNITY_COMMENT). AS-IS는 게시판 종류마다 다른 댓글 서브테이블(cmnt/*)을 썼지만,
 *  CommentManagerController 자체는 boardId 하나로 게시판 종류를 가리지 않는 전역 댓글
 *  엔드포인트라 이 프로젝트도 boardType 판별컬럼을 둔 단일 테이블로 재현한다(신규 설계,
 *  AS-IS 원본 DB에 없던 테이블). CmntyBoardController(`/community/{boardType}`)의
 *  서브리소스로 CRUD한다. */
@Entity
@Table(name = "G_CMNTY_COMMENT")
@Getter
@Setter
@NoArgsConstructor
public class CmntyComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gCmntyCommentIdSeq")
    @SequenceGenerator(name = "gCmntyCommentIdSeq", sequenceName = "g_cmnty_comment_cmnt_id_seq", allocationSize = 1)
    @Column(name = "CMNT_ID")
    private Long cmntId;

    /** bbs/sr-bbs/off-sr-bbs/faq-bbs. */
    @Column(name = "BOARD_TYPE")
    private String boardType;

    @Column(name = "BBS_ID")
    private Long bbsId;

    @Column(name = "CMNT_CN")
    private String cmntCn;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "FRST_CRT_NM")
    private String frstCrtNm;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;
}

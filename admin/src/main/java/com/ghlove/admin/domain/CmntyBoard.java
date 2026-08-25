package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 커뮤니티 게시판 공통 필드 (AS-IS opmanager/community/{bbs,sr-bbs,off-sr-bbs,faq-bbs}) -
 *  지자체 담당자 ↔ 행안부 내부 소통용 게시판 4종(자유게시판/SR게시판/오프라인SR게시판/
 *  FAQ게시판)이 전부 동일한 컬럼 구조(TTL/CN/USE_YN/NOTICE_YN/INQ_CNT/작성자/수정자/
 *  비밀글여부)를 공유해서 매핑 슈퍼클래스로 공통화한다 - 4개 테이블이라 이름만 다를 뿐
 *  진짜 같은 구조라, 이 프로젝트 관례상 지나친 추상화가 아니라 실제 중복 제거다. */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class CmntyBoard {

    @Column(name = "BBS_TTL")
    private String bbsTtl;

    @Column(name = "BBS_CN")
    private String bbsCn;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "NOTICE_YN")
    private String noticeYn;

    @Column(name = "INQ_CNT")
    private Long inqCnt;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;

    @Column(name = "IS_SECRET")
    private String isSecret;
}

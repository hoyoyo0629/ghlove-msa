package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 답례품 판매 랭킹 (AS-IS opmanager/ranking - RankingManagerController, B9). OP_RANKING은
 * 관리자가 카테고리(그룹)별로 직접 순서를 정해 큐레이션하는 "수동 랭킹"이다(원본 스키마의
 * OP_RANKING_BATCH/OP_RANKING_CONFIG는 판매량/조회수 기준 자동 산정 배치용이나, 그 배치
 * 엔진 자체가 이 프로젝트엔 없어 이번 라운드에서는 수동 큐레이션만 구현한다).
 * CATEGORY_URL을 "카테고리그룹코드"로 재해석해 쓴다 - 특정 GIFT_CATEGORY 코드이거나,
 * 전체(사이트 공통) 랭킹을 뜻하는 "ALL"일 수 있다.
 */
@Entity
@Table(name = "OP_RANKING")
@Getter
@Setter
@NoArgsConstructor
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rankingIdSeq")
    @SequenceGenerator(name = "rankingIdSeq", sequenceName = "op_ranking_id_seq", allocationSize = 1)
    @Column(name = "RANKING_ID")
    private Integer rankingId;

    @Column(name = "CATEGORY_URL")
    private String categoryUrl;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "ORDERING")
    private Integer ordering;
}

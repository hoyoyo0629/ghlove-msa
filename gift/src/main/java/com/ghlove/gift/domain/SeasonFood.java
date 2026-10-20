package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 답례품몰 GNB "제철식품관" - AS-IS G_SEASON_FOOD. 월별 제철 키워드를 갖는다(월·등록순번 복합키).
 *  제철식품관 첫 화면의 월별 키워드 카드가 이 데이터를 쓴다. */
@Entity
@Table(name = "G_SEASON_FOOD")
@IdClass(SeasonFoodId.class)
@Getter
@Setter
@NoArgsConstructor
public class SeasonFood {

    @Id
    @Column(name = "SEASON_FOOD_MONTH")
    private Integer seasonFoodMonth;

    @Id
    @Column(name = "REG_SEQ")
    private Integer regSeq;

    @Column(name = "SEASON_FOOD_KEYWORD")
    private String seasonFoodKeyword;

    /** 최초 등록자(관리자 USER_ID) - AS-IS FRST_REGISTER_ID. */
    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    /** NOT NULL DEFAULT now() 컬럼이라 저장할 때 직접 채운다(매핑만 하면 NULL을 보내 insert가 실패한다). */
    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 답례품몰 GNB "제철식품관" - AS-IS G_SEASON_FOOD_ITEM(배치 스캔으로 이미 존재, 이번에
 *  처음 애플리케이션 레이어를 얹는다). 이달(월별)에 해당하는 답례품 itemId 목록을 갖는다. */
@Entity
@Table(name = "G_SEASON_FOOD_ITEM")
@IdClass(SeasonFoodItemId.class)
@Getter
@Setter
@NoArgsConstructor
public class SeasonFoodItem {

    @Id
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Id
    @Column(name = "SEASON_FOOD_MONTH")
    private Integer seasonFoodMonth;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 카테고리 "팀"에 배정된 답례품 (AS-IS OP_CATEGORY_TEAM_ITEM). */
@Entity
@Table(name = "OP_CATEGORY_TEAM_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class CategoryTeamItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_TEAM_ITEM_ID")
    private Integer categoryTeamItemId;

    @Column(name = "CATEGORY_TEAM_ID")
    private Integer categoryTeamId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메인 진열상품 (AS-IS opmanager/main-display - MainDisplayItemManagerController).
 * Maps OP_MAIN_DISPLAY_ITEM(TEMPLATE_ID+ITEM_ID 복합키, PK 미지정이지만 실제로는 이
 * 조합이 유일). TEMPLATE_ID = "{viewType}_{categoryTeamCode}" 조합 규칙(AS-IS 컨벤션). */
@Entity
@Table(name = "OP_MAIN_DISPLAY_ITEM")
@IdClass(MainDisplayItemId.class)
@Getter
@Setter
@NoArgsConstructor
public class MainDisplayItem {

    @Id
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Id
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

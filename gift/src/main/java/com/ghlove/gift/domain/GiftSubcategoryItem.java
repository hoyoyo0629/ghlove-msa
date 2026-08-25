package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 답례품몰 GNB "전체 카테고리" 메가메뉴 3단계의 마지막 단(개별품목) - GiftSubcategory에 딸린다.
 * gift 자체의 답례품 필터링에는 쓰이지 않고 메뉴 표시 용도로만 쓰인다(소분류와 동일). */
@Entity
@Table(name = "GIFT_SUBCATEGORY_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class GiftSubcategoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUBCATEGORY_ITEM_ID")
    private Long subcategoryItemId;

    @Column(name = "SUBCATEGORY_ID")
    private Long subcategoryId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ORDERING")
    private Integer ordering;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 답례품몰 GNB "전체 카테고리" 메가메뉴의 소분류 (GIFT_CATEGORY 대분류에 딸림).
 * gift 자체의 답례품 필터링에는 쓰이지 않고 메뉴 표시/링크 용도로만 쓰인다. */
@Entity
@Table(name = "GIFT_SUBCATEGORY")
@Getter
@Setter
@NoArgsConstructor
public class GiftSubcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUBCATEGORY_ID")
    private Long subcategoryId;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ORDERING")
    private Integer ordering;

    /** 답례품 카테고리 관리(admin) SEO 메타 편집 - AS-IS OP_CATEGORY TITLE/KEYWORDS/DESCRIPTION 서브셋. */
    @Column(name = "META_TITLE")
    private String metaTitle;

    @Column(name = "META_KEYWORDS")
    private String metaKeywords;

    @Column(name = "META_DESCRIPTION")
    private String metaDescription;
}

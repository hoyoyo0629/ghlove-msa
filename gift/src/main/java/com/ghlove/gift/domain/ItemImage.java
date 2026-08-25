package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 답례품 상품 이미지. ORDERING=0이 썸네일(대표 이미지)이다. */
@Entity
@Table(name = "OP_ITEM_IMAGE")
@Getter
@Setter
@NoArgsConstructor
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_IMAGE_ID")
    private Long itemImageId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    /** 비동기로 채워지는 리사이즈본 (SFR-005 "썸네일 자동생성 소/중/대 3종") - 생성 전에는 NULL. */
    @Column(name = "THUMBNAIL_SMALL")
    private String thumbnailSmall;

    @Column(name = "THUMBNAIL_MEDIUM")
    private String thumbnailMedium;

    @Column(name = "THUMBNAIL_LARGE")
    private String thumbnailLarge;
}

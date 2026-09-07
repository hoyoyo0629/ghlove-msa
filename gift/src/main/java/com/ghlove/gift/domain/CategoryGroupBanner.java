package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 카테고리 "그룹"에 딸린 프로모션 배너 (AS-IS OP_CATEGORY_GROUP_BANNER). */
@Entity
@Table(name = "OP_CATEGORY_GROUP_BANNER")
@Getter
@Setter
@NoArgsConstructor
public class CategoryGroupBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_GROUP_BANNER_ID")
    private Integer categoryGroupBannerId;

    @Column(name = "CATEGORY_GROUP_ID")
    private Integer categoryGroupId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "LINK_URL")
    private String linkUrl;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 브랜드관리 (AS-IS opmanager/brand) - 지자체별 답례품 브랜드(예: "OO시 특산물관"). Gift
 *  아이템의 BRAND_ID(NULL=자사제품, 값 있으면 특정 브랜드 소속)가 참조한다. */
@Entity
@Table(name = "OP_BRAND")
@Getter
@Setter
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opBrandIdSeq")
    @SequenceGenerator(name = "opBrandIdSeq", sequenceName = "op_brand_brand_id_seq", allocationSize = 1)
    @Column(name = "BRAND_ID")
    private Integer brandId;

    @Column(name = "BRAND_NAME")
    private String brandName;

    @Column(name = "BRAND_IMAGE")
    private String brandImage;

    @Column(name = "BRAND_CONTENT")
    private String brandContent;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "CREATED_USER_ID")
    private Long createdUserId;

    /** yyyyMMddHHmmss. */
    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    @Column(name = "UPDATED_USER_ID")
    private Long updatedUserId;
}

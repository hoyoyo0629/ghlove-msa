package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기획전/이벤트 관리 (AS-IS opmanager/featured, featured-mobile - FeaturedManagerController).
 *  OP_FEATURED는 원본 saleson 이커머스 전반(미용실/네일샵 등)의 정렬용 컬럼
 *  (ORDERING_ESTHETIC/NAIL/MATSUGE_EXTENSION/HAIR/SALE_OUTLETS)까지 갖고 있지만 이 프로젝트
 *  도메인과 무관해 매핑하지 않는다(DEFAULT 0이라 INSERT 시 생략해도 문제 없음, memory
 *  dormant-saleson-boilerplate-tables와 동일한 패턴). PC/모바일은 FEATURED_TYPE(1/2)
 *  하나로 구분하고(AS-IS처럼 URL을 이원화하지 않음), 기획전/이벤트는 FEATURED_CLASS(1/2). */
@Entity
@Table(name = "OP_FEATURED")
@Getter
@Setter
@NoArgsConstructor
public class Featured {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "featuredIdSeq")
    @SequenceGenerator(name = "featuredIdSeq", sequenceName = "op_featured_featured_id_seq", allocationSize = 1)
    @Column(name = "FEATURED_ID")
    private Integer featuredId;

    /** 1=기획전, 2=이벤트. */
    @Column(name = "FEATURED_CLASS")
    private Integer featuredClass = 1;

    /** 1=PC, 2=모바일. */
    @Column(name = "FEATURED_TYPE")
    private String featuredType = "1";

    @Column(name = "FEATURED_URL")
    private String featuredUrl;

    @Column(name = "FEATURED_CODE")
    private String featuredCode;

    @Column(name = "FEATURED_NAME")
    private String featuredName;

    @Column(name = "FEATURED_SIMPLE_CONTENT")
    private String featuredSimpleContent;

    @Column(name = "FEATURED_CONTENT")
    private String featuredContent;

    @Column(name = "FEATURED_IMAGE")
    private String featuredImage;

    @Column(name = "THUMBNAIL_IMAGE")
    private String thumbnailImage;

    /** 노출여부(Y/N). */
    @Column(name = "FEATURED_FLAG")
    private String featuredFlag = "Y";

    @Column(name = "LINK")
    private String link = "";

    @Column(name = "LINK_TARGET_FLAG")
    private String linkTargetFlag = "N";

    @Column(name = "LINK_REL_FLAG")
    private String linkRelFlag = "N";

    /** 목록 노출여부(Y/N). */
    @Column(name = "DISPLAY_LIST_FLAG")
    private String displayListFlag = "Y";

    @Column(name = "ORDERING")
    private Integer ordering = 0;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;
}

package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 페이지별 SEO 메타 관리 (AS-IS opmanager/seo - SeoManagerController). Maps OP_SEO. */
@Entity
@Table(name = "OP_SEO")
@Getter
@Setter
@NoArgsConstructor
public class Seo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seoIdSeq")
    @SequenceGenerator(name = "seoIdSeq", sequenceName = "op_seo_id_seq", allocationSize = 1)
    @Column(name = "SEO_ID")
    private Integer seoId;

    @Column(name = "SEO_URL")
    private String seoUrl;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "KEYWORDS")
    private String keywords;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "HEADER_CONTENTS1")
    private String headerContents1;

    @Column(name = "HEADER_CONTENTS2")
    private String headerContents2;

    @Column(name = "HEADER_CONTENTS3")
    private String headerContents3;

    @Column(name = "THEMAWORD_TITLE")
    private String themawordTitle;

    @Column(name = "THEMAWORD_DESCRIPTION")
    private String themawordDescription;

    /** "Y"/"N" - 검색엔진 색인 허용 여부. */
    @Column(name = "INDEX_FLAG")
    private String indexFlag;

    @Column(name = "CREATED_USER_ID")
    private Long createdUserId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

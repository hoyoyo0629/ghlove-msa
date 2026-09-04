package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 추천 검색어 관리 (AS-IS opmanager/search - SearchManagerController). Maps OP_SEARCH. */
@Entity
@Table(name = "OP_SEARCH")
@Getter
@Setter
@NoArgsConstructor
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "searchIdSeq")
    @SequenceGenerator(name = "searchIdSeq", sequenceName = "op_search_id_seq", allocationSize = 1)
    @Column(name = "SEARCH_ID")
    private Integer searchId;

    @Column(name = "SEARCH_CONTENTS")
    private String searchContents;

    @Column(name = "SEARCH_LINK")
    private String searchLink;

    @Column(name = "SEARCH_MOBILE_LINK")
    private String searchMobileLink;

    /** "Y"/"N" - 새창으로 열기 여부. */
    @Column(name = "SEARCH_LINK_TARGET_FLAG")
    private String searchLinkTargetFlag;

    @Column(name = "SEARCH_MOBILE_LINK_TARGET_FLAG")
    private String searchMobileLinkTargetFlag;

    @Column(name = "SEARCH_START_DATE")
    private String searchStartDate;

    @Column(name = "SEARCH_END_DATE")
    private String searchEndDate;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

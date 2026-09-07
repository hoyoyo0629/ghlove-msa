package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 "팀" - 대분류 상위그룹 (AS-IS OP_CATEGORY_TEAM, saleson.shop.categoriesteamgroup
 * 원본 - 답례품 상품관리 2단계 #5). 이 MVP는 콘텐츠/SEO 컬럼(TITLE/KEYWORDS/DESCRIPTION 등
 * 다수)은 매핑하지 않고 이름/코드/사용여부/순서만 관리한다 - 실제로 어느 화면도 그 콘텐츠
 * 컬럼을 참조하지 않기 때문(원본 saleson 쇼핑몰 템플릿용 필드).
 */
@Entity
@Table(name = "OP_CATEGORY_TEAM")
@Getter
@Setter
@NoArgsConstructor
public class CategoryTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_TEAM_ID")
    private Integer categoryTeamId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CODE")
    private String code;

    /** Y/N 사용여부. */
    @Column(name = "CATEGORY_TEAM_FLAG")
    private String categoryTeamFlag;

    @Column(name = "ORDERING")
    private Integer ordering;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;
}

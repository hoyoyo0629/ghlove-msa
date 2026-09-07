package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 "그룹" - 팀 하위, 프로모션 묶음 (AS-IS OP_CATEGORY_GROUP - 답례품 상품관리
 * 2단계 #5). CategoryTeam과 마찬가지로 콘텐츠/SEO 컬럼은 매핑하지 않는다.
 */
@Entity
@Table(name = "OP_CATEGORY_GROUP")
@Getter
@Setter
@NoArgsConstructor
public class CategoryGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_GROUP_ID")
    private Integer categoryGroupId;

    @Column(name = "CATEGORY_TEAM_ID")
    private Integer categoryTeamId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CODE")
    private String code;

    /** Y/N 사용여부. */
    @Column(name = "CATEGORY_GROUP_FLAG")
    private String categoryGroupFlag;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;
}

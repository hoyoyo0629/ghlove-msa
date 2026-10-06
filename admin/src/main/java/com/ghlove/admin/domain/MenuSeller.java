package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 답례품 관리자(판매자) 메뉴 트리 (AS-IS OP_MENU_SELLER). 메뉴관리 화면(1409)의 "카테고리 =
 * 답례품관리자" 선택 시 이 표를 다룬다 - AS-IS menu-manager-mapper.xml의 {@code sqlMenuWhere}가
 * menuGubun에 따라 OP_MENU / OP_MENU_SELLER로 테이블을 갈아끼우는 구조다.
 * 개발DB는 아직 0행이다(AS-IS 운영데이터를 받아야 채워진다).
 */
@Entity
@Table(name = "OP_MENU_SELLER")
@Getter
@Setter
@NoArgsConstructor
public class MenuSeller {

    @Id
    @Column(name = "MENU_ID")
    private Integer menuId;

    @Column(name = "MENU_PARENT_ID")
    private Integer menuParentId;

    @Column(name = "MENU_TYPE")
    private Integer menuType;

    @Column(name = "MENU_NAME")
    private String menuName;

    @Column(name = "MENU_CODE")
    private String menuCode;

    @Column(name = "MENU_URL")
    private String menuUrl;

    @Column(name = "MENU_SEQ")
    private Integer menuSeq;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "MENU_PAGE_CONTENT")
    private String menuPageContent;
}

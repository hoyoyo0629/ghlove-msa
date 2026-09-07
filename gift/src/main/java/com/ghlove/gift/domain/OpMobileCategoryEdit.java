package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 모바일 카테고리/메인 화면 레이아웃 편집 (AS-IS opmanager/mobile-category-edit -
 *  MobileCategoriesEditManagerController). 원본은 팀별 그룹 관리/HTML블록/프로모션배너/
 *  헤더·푸터 팝업까지 갖춘 복잡한 iframe 레이아웃 에디터지만, 이 프로젝트는 핵심인
 *  "코드(화면)별 위치(position)에 HTML블록 또는 배너이미지+링크를 배치"하는 CRUD로
 *  단순화한다(memory give-statistics-scope 등과 동일한 관행). OP_MOBILE_CATEGORY_EDIT. */
@Entity
@Table(name = "OP_MOBILE_CATEGORY_EDIT")
@Getter
@Setter
@NoArgsConstructor
public class OpMobileCategoryEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mobileCategoryEditIdSeq")
    @SequenceGenerator(name = "mobileCategoryEditIdSeq", sequenceName = "op_mobile_category_edit_id_seq", allocationSize = 1)
    @Column(name = "CATEGORY_EDIT_ID")
    private Integer categoryEditId;

    /** 편집 대상 화면 코드(예: "main"=모바일 메인, 그 외에는 카테고리 코드). */
    @Column(name = "CODE")
    private String code;

    /** 1=HTML 블록, 2=배너/프로모션 이미지. */
    @Column(name = "EDIT_KIND")
    private String editKind;

    /** 화면 내 노출 위치 식별자(자유 텍스트, 예: top/middle1/bottom). */
    @Column(name = "EDIT_POSITION")
    private String editPosition;

    @Column(name = "EDIT_CONTENT")
    private String editContent;

    @Column(name = "EDIT_IMAGE")
    private String editImage;

    @Column(name = "EDIT_URL")
    private String editUrl;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;
}

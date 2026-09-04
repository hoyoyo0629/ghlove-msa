package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운영관리 콘솔 메뉴 트리 (AS-IS OP_MENU) - AdminAuthInterceptor가 요청 URI를 여기 등록된
 *  menuUrl과 매칭해 어떤 메뉴에 대한 접근인지 판별하고, MenuRight로 권한을 확인한다.
 *  menu_id 0~4999는 이 프로젝트의 여러 구현 라운드가 서로 충돌을 피하려고 수동으로 배정해온
 *  범위라(각 라운드에 100단위로 예약) MenuAdminController(관리자가 화면에서 직접 등록하는
 *  신규 메뉴)는 5000부터 시작하는 별도 시퀀스(op_menu_id_seq)를 쓴다 - 두 채번 방식이 절대
 *  겹치지 않는다. */
@Entity
@Table(name = "OP_MENU")
@Getter
@Setter
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menuIdSeq")
    @SequenceGenerator(name = "menuIdSeq", sequenceName = "op_menu_id_seq", allocationSize = 1)
    @Column(name = "MENU_ID")
    private Integer menuId;

    @Column(name = "MENU_PARENT_ID")
    private Integer menuParentId;

    @Column(name = "MENU_NAME")
    private String menuName;

    /** 이 메뉴가 담당하는 URL prefix (예: "/stats") - 인터셉터가 최장 접두어 일치로 판별. */
    @Column(name = "MENU_URL")
    private String menuUrl;

    @Column(name = "MENU_SEQ")
    private Integer menuSeq;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    /** "1"=사용, "2"=사용안함 (AS-IS 컨벤션). */
    @Column(name = "STATUS_CODE")
    private String statusCode;
}

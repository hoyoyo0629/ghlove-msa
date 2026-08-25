package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운영관리 콘솔 메뉴 트리 (AS-IS OP_MENU) - AdminAuthInterceptor가 요청 URI를 여기 등록된
 *  menuUrl과 매칭해 어떤 메뉴에 대한 접근인지 판별하고, MenuRight로 권한을 확인한다. */
@Entity
@Table(name = "OP_MENU")
@Getter
@Setter
@NoArgsConstructor
public class Menu {

    @Id
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

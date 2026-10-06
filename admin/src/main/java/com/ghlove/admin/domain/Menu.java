package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운영관리 콘솔 메뉴 트리 (AS-IS OP_MENU) - AdminAuthInterceptor가 요청 URI를 여기 등록된
 *  menuUrl과 매칭해 어떤 메뉴에 대한 접근인지 판별하고, MenuRight로 권한을 확인한다.
 *
 *  menu_id는 **직접 배정**한다. AS-IS 메뉴관리 화면이 레벨별로 ID를 채번하기 때문이다
 *  (menu-manager-mapper.xml getMenuId: 1레벨 {@code MAX(MENU_ID)+1000}, 2레벨
 *  {@code NVL(MAX,부모ID)+100}, 3레벨 {@code NVL(MAX,부모ID)+1}) - 그래서 트리 위치가
 *  ID에 드러난다(예: 6000 통계 > 6700 기부금 모금현황 > 6706 전체). 예전에는 시퀀스
 *  (op_menu_id_seq, 5000부터)로 채번했는데 그러면 AS-IS 규칙이 깨져 쓰지 않는다. */
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

    /** AS-IS 1=상단(GNB) 2=LNB 섹션 3=링크(리프). 메뉴관리 화면이 이 값으로 ID를 채번하고
     *  목록 들여쓰기를 결정한다. 지금까지 TO-BE는 이 컬럼을 채우지 않아 전부 NULL이다. */
    @Column(name = "MENU_TYPE")
    private Integer menuType;

    /** AS-IS MENU_CODE - 3레벨(링크)에서는 필수 입력이고, 인터셉터가 화면 식별자로 쓴다. */
    @Column(name = "MENU_CODE")
    private String menuCode;

    @Column(name = "MENU_ICON")
    private String menuIcon;

    /**
     * 상단 1차(GNB) 메뉴인지 - AS-IS는 menu_id=0('root') 행을 두고 최상위 메뉴의
     * menu_parent_id를 0으로 둔다(계층 쿼리가 START WITH MENU_ID='0'으로 시작하기 때문).
     * 예전 TO-BE는 root 행 없이 최상위를 menu_parent_id NULL로 두었어서 둘 다 최상위로 본다.
     * root 행 자체는 메뉴가 아니므로 제외한다.
     */
    @Transient
    public boolean isTopLevel() {
        return menuId != null && menuId != 0 && (menuParentId == null || menuParentId == 0);
    }

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

    /**
     * 이 메뉴에 붙은 <b>관리자매뉴얼 파일</b>의 저장 파일명 - 메뉴 5201 화면이 쓴다.
     * AS-IS 관리자매뉴얼은 별도 표 없이 {@code op_menu}에 파일을 직접 붙인다
     * ({@code updateManagerManual}).
     */
    @Column(name = "FILE_NM")
    private String fileNm;

    /** 관리자매뉴얼 파일의 원본 파일명 - 다운로드할 때 쓴다. */
    @Column(name = "ORGINL_FILE_NM")
    private String orginlFileNm;
}

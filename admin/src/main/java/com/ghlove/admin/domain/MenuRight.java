package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메뉴×권한 매핑 (AS-IS OP_MENU_RIGHT) - 자연키는 (MENU_ID, AUTHORITY). ROLE_ADMIN(시스템관리자)은
 *  이 테이블과 무관하게 항상 전체 접근 가능(AdminAuthInterceptor에서 별도 우회) - AS-IS의
 *  ROLE_SUPERVISOR 개념과 동일하다. */
@Entity
@Table(name = "OP_MENU_RIGHT")
@IdClass(MenuRightId.class)
@Getter
@Setter
@NoArgsConstructor
public class MenuRight {

    @Id
    @Column(name = "MENU_ID")
    private Integer menuId;

    @Id
    @Column(name = "AUTHORITY")
    private String authority;
}

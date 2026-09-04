package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메뉴×권한 매핑 (AS-IS OP_MENU_RIGHT) - 조회는 (MENU_ID, AUTHORITY) 자연키 조합으로만
 *  쓴다(존재 여부 확인 전용, 이 엔티티로 직접 저장하지 않음 - 실제 테이블 PK는 별도
 *  MENU_RIGHT_ID 서로게이트키). 시스템/행안부(ROLE_ADMIN_1~4)는 이 테이블과 무관하게
 *  항상 전체 접근 가능(MenuService.UNRESTRICTED_ROLES) - AS-IS의 ROLE_SUPERVISOR
 *  개념과 동일하다. 실제로 이 테이블을 거쳐 권한을 확인하는 건 지자체담당자
 *  (ROLE_ADMIN_5/6, MenuService.LOCGOV_SCOPED_ROLES)뿐이다. */
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

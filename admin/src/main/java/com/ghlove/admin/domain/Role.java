package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 권한 라벨 (AS-IS OP_ROLE) - AS-IS는 ROLE_ADMIN_1~8(시스템·행안부·지자체·오프라인
 *  4그룹 × 정·부담당자)이고, 이 프로젝트는 그중 1~6만 구현했다(7·8 오프라인담당자 미구현). */
@Entity
@Table(name = "OP_ROLE")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @Column(name = "AUTHORITY")
    private String authority;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "ROLE_DESC")
    private String roleDesc;

    @Column(name = "ROLE_SEQ")
    private Integer roleSeq;
}

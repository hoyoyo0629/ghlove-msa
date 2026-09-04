package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 권한 라벨 (AS-IS OP_ROLE) - 실제 6단계: ROLE_ADMIN_1~6. */
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

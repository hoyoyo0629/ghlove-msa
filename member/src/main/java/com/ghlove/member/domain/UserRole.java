package com.ghlove.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RBAC role grant (SFR-002 역할기반 접근제어). AUTHORITY is a Spring-Security-style
 * authority string (e.g. ROLE_USER), not business code data, so it is not looked
 * up from OP_COMMON_CODE the way status/type codes are - only its display label is.
 */
@Entity
@Table(name = "OP_USER_ROLE")
@IdClass(UserRoleId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserRole {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "AUTHORITY")
    private String authority;

    public UserRole(Long userId, String authority) {
        this.userId = userId;
        this.authority = authority;
    }
}

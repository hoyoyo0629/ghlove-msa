package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Account-change audit trail (SFR-002 감사 로그: 비밀번호 변경, 탈퇴 등 주요 행위 기록).
 */
@Entity
@Table(name = "OP_USER_CHANGE_LOG")
@Getter
@Setter
@NoArgsConstructor
public class UserChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHANGE_LOG_ID")
    private Long changeLogId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "PARAMETER")
    private String parameter;

    @Column(name = "REMOTE_ADDR")
    private String remoteAddr;

    @Column(name = "MANAGER_ID")
    private Long managerId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

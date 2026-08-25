package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 관리자 로그인 이메일 2차인증 발송 이력 (AS-IS OP_MANAGER_LOGIN_EMAIL). */
@Entity
@Table(name = "OP_MANAGER_LOGIN_EMAIL")
@Getter
@Setter
@NoArgsConstructor
public class ManagerLoginEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMAIL_LOG_ID")
    private Long emailLogId;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "AUTH_NUM")
    private String authNum;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}

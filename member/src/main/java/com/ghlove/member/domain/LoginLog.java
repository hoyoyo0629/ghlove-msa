package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Login attempt audit trail (SFR-002 감사 로그: 로그인 성공/실패 기록).
 */
@Entity
@Table(name = "OP_USER_LOGIN_LOG")
@Getter
@Setter
@NoArgsConstructor
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loginLogIdSeq")
    @SequenceGenerator(name = "loginLogIdSeq", sequenceName = "op_user_login_log_id_seq", allocationSize = 1)
    @Column(name = "LOGIN_LOG_ID")
    private Integer loginLogId;

    @Column(name = "LOGIN_TYPE")
    private String loginType;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "SUCCESS_FLAG")
    private String successFlag;

    @Column(name = "REMOTE_ADDR")
    private String remoteAddr;

    @Column(name = "MEMO")
    private String memo;

    /** yyyyMMddHHmmss (AS-IS 레거시 컬럼이 VARCHAR(14)). */
    @Column(name = "LOGIN_DATE")
    private String loginDate;
}

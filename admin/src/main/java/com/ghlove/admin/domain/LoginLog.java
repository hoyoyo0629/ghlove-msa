package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 로그인 시도 이력 (AS-IS OP_LOGIN_LOG, opmanager/log/login-log). 성공/실패 모두 기록. */
@Entity
@Table(name = "OP_LOGIN_LOG")
@Getter
@Setter
@NoArgsConstructor
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opLoginLogIdSeq")
    @SequenceGenerator(name = "opLoginLogIdSeq", sequenceName = "op_login_log_login_log_id_seq", allocationSize = 1)
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

    /** yyyyMMddHHmmss. */
    @Column(name = "LOGIN_DATE")
    private String loginDate;
}

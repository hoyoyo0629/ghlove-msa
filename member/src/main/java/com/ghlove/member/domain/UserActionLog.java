package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 상태변경 액션 이력 (AS-IS OP_USER_ACTION_LOG, opmanager/log/user-action-log). 조회(GET)는
 *  제외하고 POST/PUT/DELETE/PATCH만 기록한다. */
@Entity
@Table(name = "OP_USER_ACTION_LOG")
@Getter
@Setter
@NoArgsConstructor
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opUserActionLogIdSeq")
    @SequenceGenerator(name = "opUserActionLogIdSeq", sequenceName = "op_user_action_log_action_log_id_seq", allocationSize = 1)
    @Column(name = "ACTION_LOG_ID")
    private Integer actionLogId;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "REMOTE_ADDR")
    private String remoteAddr;

    @Column(name = "REQUEST_URI")
    private String requestUri;

    @Column(name = "REQUEST_METHOD")
    private String requestMethod;

    @Column(name = "LOGIN_ID")
    private String loginId;
}

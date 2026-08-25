package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 상태변경 액션 이력 (AS-IS OP_MANAGER_ACTION_LOG, opmanager/log/action-log). 조회(GET)는
 *  제외하고 POST/PUT/DELETE/PATCH만 기록한다 - "무엇을 봤는가"는 OP_PRIVACY_ACCESS_LOG의
 *  영역이고, 이 로그의 목적은 "무엇을 바꿨는가"다. */
@Entity
@Table(name = "OP_MANAGER_ACTION_LOG")
@Getter
@Setter
@NoArgsConstructor
public class ManagerActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opManagerActionLogIdSeq")
    @SequenceGenerator(name = "opManagerActionLogIdSeq", sequenceName = "op_manager_action_log_action_log_id_seq", allocationSize = 1)
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

    @Column(name = "LOGIN_TYPE")
    private String loginType;

    @Column(name = "LOGIN_ID")
    private String loginId;
}

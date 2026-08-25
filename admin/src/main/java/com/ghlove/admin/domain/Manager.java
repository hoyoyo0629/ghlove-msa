package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자(운영자) 로그인 계정 - AS-IS OP_MANAGER 재현. LOGIN_DATE 등 날짜 컬럼이 OP_USER와
 * 달리 TIMESTAMP가 아니라 VARCHAR(14)(CommonMapper.datetime 컨벤션)인 것도 실제 DDL 그대로다.
 * AUTHORITY 컬럼은 라이브 테이블 배치 스캔에는 없었지만 OP_MANAGER_HIST(이력 테이블)에는
 * 있어(같은 행을 스냅샷 저장하는 구조상 라이브 테이블에도 있었을 것) 그대로 추가했다.
 */
@Entity
@Table(name = "OP_MANAGER")
@Getter
@Setter
@NoArgsConstructor
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opManagerUserIdSeq")
    @SequenceGenerator(name = "opManagerUserIdSeq", sequenceName = "op_manager_user_id_seq", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "LOGIN_COUNT")
    private Integer loginCount;

    /** yyyyMMddHHmmss. */
    @Column(name = "LOGIN_DATE")
    private String loginDate;

    @Column(name = "LOGIN_FAIL_COUNT")
    private Integer loginFailCount;

    @Column(name = "LOGIN_TRY_DATE")
    private String loginTryDate;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    /** OP_ROLE.AUTHORITY 참조 (예: ROLE_ADMIN, ROLE_OPERATOR). */
    @Column(name = "AUTHORITY")
    private String authority;

    /** 인증서(금융인증서/공동인증서) 로그인용 Subject DN - 사전 등록 필요, 미등록 시 null
     *  (AS-IS opmanager/user/login_main.jsp의 인증서 로그인 카드와 동일한 방식이지만,
     *  MagicLine4Web 브라우저 SDK 대신 이 프로젝트는 CertLoginService와 동일하게 인증서
     *  파일 업로드+로컬 X.509 파싱으로 DN을 얻는다 - SDK 없이도 실제로 동작하는 부분). */
    @Column(name = "CERT_SUBJECT_DN")
    private String certSubjectDn;
}

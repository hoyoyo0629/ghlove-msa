package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OP_USER")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opUserIdSeq")
    @SequenceGenerator(name = "opUserIdSeq", sequenceName = "op_user_user_id_seq", allocationSize = 1)
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

    /** yyyyMMddHHmmss (AS-IS 레거시 컬럼이 VARCHAR(14)). */
    @Column(name = "LOGIN_DATE")
    private String loginDate;

    @Column(name = "LOGIN_FAIL_COUNT")
    private Integer loginFailCount;

    /** yyyyMMddHHmmss. */
    @Column(name = "LOGIN_TRY_DATE")
    private String loginTryDate;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    /** yyyyMMddHHmmss. */
    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    @Column(name = "SBSCRB_SE_CODE")
    private String sbscrbSeCode;

    @Column(name = "LOGIN_PATH_CODE")
    private String loginPathCode;

    /** yyyyMMddHHmmss. */
    @Column(name = "LEAVE_DATE")
    private String leaveDate;

    /** 본인인증 연계정보(CI) - 디지털원패스 등 외부 신원확인 결과로 세팅되는 고유 식별값. */
    @Column(name = "MBER_CI")
    private String mberCi;

    /** 중복가입확인정보(DI). */
    @Column(name = "MBER_DI")
    private String mberDi;

    @Column(name = "MBER_DN")
    private String mberDn;
}

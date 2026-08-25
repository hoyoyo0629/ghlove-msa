package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 약관관리 (AS-IS opmanager/config/policy) - 0:약관, 1:개인정보취급방침, 2:특정상거래법,
 *  3:마케팅이용약관, 4:개인정보제3자동의, 5:저작권정책. 전시기간을 지정할 수 있어
 *  버전 관리처럼 여러 건을 등록해두고 특정 기간만 노출할 수 있다. */
@Entity
@Table(name = "OP_POLICY")
@Getter
@Setter
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opPolicyIdSeq")
    @SequenceGenerator(name = "opPolicyIdSeq", sequenceName = "op_policy_policy_id_seq", allocationSize = 1)
    @Column(name = "POLICY_ID")
    private Integer policyId;

    @Column(name = "POLICY_TYPE")
    private String policyType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String content;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "CREATED_USER_ID")
    private Integer createdUserId;

    /** yyyyMMddHHmmss. */
    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    @Column(name = "UPDATED_LOGIN_ID")
    private String updatedLoginId;

    /** Y:전시, N:비전시. */
    @Column(name = "EXHIBITION_STATUS")
    private String exhibitionStatus;

    /** yyyyMMddHHmmss. */
    @Column(name = "EXHIBITION_START_DATE")
    private String exhibitionStartDate;

    /** yyyyMMddHHmmss. */
    @Column(name = "EXHIBITION_END_DATE")
    private String exhibitionEndDate;
}

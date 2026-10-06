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

    /** AS-IS saleson.shop.policy.domain.Policy의 POLICY_TYPE 상수와 같은 값. */
    public static final String TYPE_AGREEMENT = "0";
    public static final String TYPE_PROTECT_POLICY = "1";
    public static final String TYPE_TRADER_RAW = "2";
    public static final String TYPE_MARKETING_AGREEMENT = "3";
    public static final String TYPE_COPYRIGHT = "5";
    public static final String EXHIBITION_ON = "Y";

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

    /**
     * AS-IS saleson.shop.policy.domain.Policy#getPolicyTypeLabel() 그대로 - 목록의 "정책구분"
     * 컬럼이 이 값을 쓴다. 주의: AS-IS 검색·등록 화면의 라디오에는 '6'(개인정보 수집·이용 동의)이
     * 있는데 이 메서드에는 '6' 분기가 없어 목록에서 빈칸으로 보인다. AS-IS 동작이라 그대로 둔다
     * ('1'의 라벨도 라디오는 "개인정보처리방침", 이 메서드는 "개인정보취급방침"으로 다르다).
     */
    @Transient
    public String getPolicyTypeLabel() {
        if (policyType == null || policyType.isEmpty()) {
            return "";
        }
        return switch (policyType) {
            case "0" -> "약관";
            case "1" -> "개인정보취급방침";
            case "2" -> "특정상거래법";
            case "3" -> "마케팅이용약관";
            case "4" -> "개인정보제3자동의";
            case "5" -> "저작권정책";
            default -> "";
        };
    }
}

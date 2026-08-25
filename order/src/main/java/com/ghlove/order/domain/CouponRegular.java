package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 정기발행쿠폰 (AS-IS OP_COUPON_REGULAR, opmanager/coupon-regular) - 발행기간 동안 매일 배치가
 *  훑어 대상 회원에게 반복 발급하는 쿠폰. {@link Coupon}과 컬럼이 거의 같지만 AS-IS에서부터
 *  이미 별도 테이블이라 그대로 별도 엔티티로 둔다(OFFLINE_FLAG/DIRECT_INPUT 계열 없음). */
@Entity
@Table(name = "OP_COUPON_REGULAR")
@Getter
@Setter
@NoArgsConstructor
public class CouponRegular {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opCouponRegularIdSeq")
    @SequenceGenerator(name = "opCouponRegularIdSeq", sequenceName = "op_coupon_regular_coupon_id_seq", allocationSize = 1)
    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "COUPON_TYPE")
    private String couponType = "WEB";

    @Column(name = "COUPON_NAME")
    private String couponName;

    @Column(name = "COUPON_COMMENT")
    private String couponComment;

    @Column(name = "COUPON_ISSUE_TYPE")
    private String issueType = "0";

    @Column(name = "COUPON_ISSUE_START_DATE")
    private String issueStartDate;

    @Column(name = "COUPON_ISSUE_END_DATE")
    private String issueEndDate;

    @Column(name = "COUPON_TARGET_TIME_TYPE")
    private String targetTimeType = "1";

    @Column(name = "COUPON_TARGET_USER_TYPE")
    private String targetUserType = "1";

    @Column(name = "COUPON_TARGET_USER_LEVEL")
    private String targetUserLevel;

    @Column(name = "COUPON_PAY_RESTRICTION")
    private Integer payRestriction = -1;

    @Column(name = "COUPON_CONCURRENTLY")
    private String concurrently = "1";

    @Column(name = "COUPON_PAY_TYPE")
    private String payType;

    @Column(name = "COUPON_PAY")
    private Integer pay;

    @Column(name = "COUPON_DISCOUNT_LIMIT_PRICE")
    private Integer discountLimitPrice = -1;

    @Column(name = "COUPON_TARGET_ITEM_TYPE")
    private String targetItemType = "1";

    @Column(name = "COUPON_FLAG")
    private String couponFlag = "Y";

    @Column(name = "COUPON_BIRTHDAY")
    private String birthday;

    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode = "1";

    @Column(name = "COUPON_DOWNLOAD_LIMIT")
    private Integer downloadLimit = -1;

    @Column(name = "COUPON_DOWNLOAD_USER_LIMIT")
    private Integer downloadUserLimit = -1;

    @Column(name = "COUPON_MULITPLE_DOWNLOAD_FLAG")
    private String multipleDownloadFlag = "N";

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATE_USER_NAME")
    private String updateUserName;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;
}

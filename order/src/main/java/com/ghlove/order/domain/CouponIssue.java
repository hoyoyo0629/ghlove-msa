package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원별 발급(다운로드)받은 쿠폰 1건 (AS-IS OP_COUPON_USER / saleson CouponUser). 발급 시점의
 *  쿠폰 설정을 스냅샷으로 복사해 담고, 사용 시 COUPON_USED_DATE+ORDER_CODE+DISCOUNT_AMOUNT가 채워진다. */
@Entity
@Table(name = "OP_COUPON_USER")
@Getter
@Setter
@NoArgsConstructor
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opCouponUserIdSeq")
    @SequenceGenerator(name = "opCouponUserIdSeq", sequenceName = "op_coupon_user_coupon_user_id_seq", allocationSize = 1)
    @Column(name = "COUPON_USER_ID")
    private Integer couponUserId;

    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "COUPON_TYPE")
    private String couponType;

    @Column(name = "COUPON_NAME")
    private String couponName;

    @Column(name = "COUPON_COMMENT")
    private String couponComment;

    @Column(name = "COUPON_APPLY_TYPE")
    private String applyType;

    @Column(name = "COUPON_APPLY_START_DATE")
    private String applyStartDate;

    @Column(name = "COUPON_APPLY_END_DATE")
    private String applyEndDate;

    @Column(name = "COUPON_PAY_RESTRICTION")
    private Integer payRestriction;

    @Column(name = "COUPON_CONCURRENTLY")
    private String concurrently;

    @Column(name = "COUPON_PAY_TYPE")
    private String payType;

    @Column(name = "COUPON_PAY")
    private Integer pay;

    @Column(name = "COUPON_DISCOUNT_LIMIT_PRICE")
    private Integer discountLimitPrice;

    @Column(name = "COUPON_TARGET_ITEM_TYPE")
    private String targetItemType;

    /** 0:다운받음(미사용), 1:사용완료. */
    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode = "0";

    @Column(name = "COUPON_DOWNLOAD_DATE")
    private String downloadDate;

    @Column(name = "COUPON_USED_DATE")
    private String usedDate;

    @Column(name = "ORDER_CODE")
    private String orderCode;

    @Column(name = "ORDER_SEQUENCE")
    private Integer orderSequence;

    @Column(name = "ITEM_SEQUENCE")
    private Integer itemSequence;

    @Column(name = "DISCOUNT_AMOUNT")
    private Integer discountAmount;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

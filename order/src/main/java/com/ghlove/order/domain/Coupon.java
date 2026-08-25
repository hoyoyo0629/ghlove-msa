package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 쿠폰 (AS-IS OP_COUPON, saleson.shop.coupon.domain.Coupon). AS-IS는 발급대상 회원
 * (COUPON_TARGET_USER)/특정상품(COUPON_TARGET_ITEM)을 JSON 문자열 컬럼에도 중복 저장하지만,
 * 여기서는 그 역할을 {@link CouponTargetUser}/{@link CouponTargetItem} 테이블만으로 대신한다
 * (원본 JSON 컬럼은 옮겨적을 데이터가 없어 컬럼만 존재, 실제로 읽지 않음).
 */
@Entity
@Table(name = "OP_COUPON")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opCouponIdSeq")
    @SequenceGenerator(name = "opCouponIdSeq", sequenceName = "op_coupon_coupon_id_seq", allocationSize = 1)
    @Column(name = "COUPON_ID")
    private Integer couponId;

    /** WEB/MOBILE/APP - 이 프로젝트는 웹 채널만 있어 항상 "WEB" 하나만 저장/표시된다. */
    @Column(name = "COUPON_TYPE")
    private String couponType = "WEB";

    @Column(name = "COUPON_NAME")
    private String couponName;

    @Column(name = "COUPON_COMMENT")
    private String couponComment;

    /** 0:제한없음, 1:기간지정 (다운로드 가능기간). */
    @Column(name = "COUPON_ISSUE_TYPE")
    private String issueType = "0";

    @Column(name = "COUPON_ISSUE_START_DATE")
    private String issueStartDate;

    @Column(name = "COUPON_ISSUE_END_DATE")
    private String issueEndDate;

    /** 0:제한없음, 1:기간지정, 2:다운로드일+N일 (쿠폰 사용가능기간). */
    @Column(name = "COUPON_APPLY_TYPE")
    private String applyType = "0";

    @Column(name = "COUPON_APPLY_DAY")
    private Integer applyDay;

    @Column(name = "COUPON_APPLY_START_DATE")
    private String applyStartDate;

    @Column(name = "COUPON_APPLY_END_DATE")
    private String applyEndDate;

    /** 발행시점 1:일반, 2:회원가입, 3:생일, 4:상품구매 후 발행, 5:첫구매. */
    @Column(name = "COUPON_TARGET_TIME_TYPE")
    private String targetTimeType = "1";

    /** 쿠폰대상 1:전체회원, 2:선택회원, 3:회원등급. 이 프로젝트는 회원등급 체계가 없어
     *  3은 전체회원과 동일하게 처리한다(발급대상 해석 로직 참고). */
    @Column(name = "COUPON_TARGET_USER_TYPE")
    private String targetUserType = "1";

    @Column(name = "COUPON_TARGET_USER_LEVEL")
    private String targetUserLevel;

    @Column(name = "COUPON_PAY_RESTRICTION")
    private Integer payRestriction = -1;

    /** 1:1개 수량만 할인, 2:구매 수량만큼 할인. */
    @Column(name = "COUPON_CONCURRENTLY")
    private String concurrently = "1";

    /** 1:원, 2:%. */
    @Column(name = "COUPON_PAY_TYPE")
    private String payType;

    @Column(name = "COUPON_PAY")
    private Integer pay;

    @Column(name = "COUPON_DISCOUNT_LIMIT_PRICE")
    private Integer discountLimitPrice = -1;

    /** 0:전체상품(AS-IS 원본은 1이지만 이 프로젝트 신규 쿠폰 기본값은 "전체"가 자연스러워 0 유지),
     *  1:전체상품, 2:특정상품 - AS-IS 실컬럼 정의(0)를 그대로 따른다. */
    @Column(name = "COUPON_TARGET_ITEM_TYPE")
    private String targetItemType = "1";

    @Column(name = "COUPON_FLAG")
    private String couponFlag = "Y";

    @Column(name = "COUPON_OFFLINE_FLAG")
    private String offlineFlag = "N";

    @Column(name = "COUPON_BIRTHDAY")
    private String birthday;

    /** 0:임시저장, 1:쿠폰발행완료, 9:삭제. */
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

    @Column(name = "DIRECT_INPUT_FLAG")
    private String directInputFlag = "N";

    @Column(name = "DIRECT_INPUT_VALUE")
    private String directInputValue;
}

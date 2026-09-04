package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사이트 설정 (AS-IS opmanager/config/site-config, shop-config, deny/edit, payment-config,
 *  delivery/delivery-hope, conversion-tag, order-temp-config 통합) - 원본 OP_CONFIG는 단일
 *  행에 PG/포인트/쿠폰/세금 등 saleson 범용 이커머스 설정까지 전부 담고 있지만, 이 프로젝트는
 *  실제 관련 있는 사이트 정보/SEO/가입차단/결제수단 활성화/배송정책/전환추적/주문임시저장
 *  설정만 관리한다. POINT_* 계열은 point 서비스가 지자체별로 자체 로직을 갖고 있어(memory
 *  domain-point-rate-policy) 이 전역 saleson 설정과 안 맞아 매핑하지 않는다. PG 연동 상세는
 *  {@link OpConfigPg}, GA 추적 설정은 {@link OpConfigGoogleAnalytics}로 별도 테이블에 매핑한다.
 *  나머지 NOT NULL 컬럼은 DDL 시드에서 합리적 기본값으로 채워둔다. */
@Entity
@Table(name = "OP_CONFIG")
@Getter
@Setter
@NoArgsConstructor
public class ShopConfig {

    public static final long SHOP_CONFIG_ID = 1L;

    @Id
    @Column(name = "SHOP_CONFIG_ID")
    private Long shopConfigId;

    @Column(name = "SHOP_NAME")
    private String shopName;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "BOSS_NAME")
    private String bossName;

    @Column(name = "COMPANY_NUMBER")
    private String companyNumber;

    @Column(name = "TEL_NUMBER")
    private String telNumber;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ADDRESS_DETAIL")
    private String addressDetail;

    @Column(name = "ADMIN_NAME")
    private String adminName;

    @Column(name = "ADMIN_EMAIL")
    private String adminEmail;

    @Column(name = "ADMIN_TEL_NUMBER")
    private String adminTelNumber;

    @Column(name = "SEO_TITLE")
    private String seoTitle;

    @Column(name = "SEO_KEYWORDS")
    private String seoKeywords;

    @Column(name = "SEO_DESCRIPTION")
    private String seoDescription;

    /** 회원가입 불가 아이디, 콤마(,) 구분. */
    @Column(name = "DENIED_ID")
    private String deniedId;

    /** 금지어(게시글/후기 등), 콤마(,) 구분. */
    @Column(name = "BAN_WORD")
    private String banWord;

    /** 회원가입 거부 IP, 콤마(,) 구분. */
    @Column(name = "DENIED_IP")
    private String deniedIp;

    /** 회원가입 거부 이메일 도메인, 콤마(,) 구분. */
    @Column(name = "DENIED_EMAIL_DOMAIN")
    private String deniedEmailDomain;

    // ---------------------------------------------------------------- 결제수단 활성화

    @Column(name = "PAYMENT_CARD")
    private String paymentCard;

    @Column(name = "PAYMENT_BANK")
    private String paymentBank;

    @Column(name = "PAYMENT_VBANK")
    private String paymentVbank;

    @Column(name = "PAYMENT_ESCROW")
    private String paymentEscrow;

    @Column(name = "PAYMENT_CONV")
    private String paymentConv;

    @Column(name = "PAYMENT_DLV")
    private String paymentDlv;

    @Column(name = "PAYMENT_REALTIME")
    private String paymentRealtime;

    @Column(name = "PAYMENT_HP")
    private String paymentHp;

    @Column(name = "MINIMUM_PAYMENT_AMOUNT")
    private Integer minimumPaymentAmount;

    @Column(name = "PAYMENT_CONV_LIMIT")
    private String paymentConvLimit;

    // ---------------------------------------------------------------- 배송 기본정책 + 희망배송일

    /** 배송 안내 문구(약관성 자유 텍스트). */
    @Column(name = "DELIVERY_INFO")
    private String deliveryInfo;

    /** 희망배송일 사용여부(Y/N). */
    @Column(name = "DELIVERY_HOPE_FLAG")
    private String deliveryHopeFlag;

    @Column(name = "DELIVERY_HOPE_START_DATE")
    private String deliveryHopeStartDate;

    @Column(name = "DELIVERY_HOPE_END_DATE")
    private String deliveryHopeEndDate;

    /** 무통장 입금 마감일(일). */
    @Column(name = "BANK_DEPOSIT_DUE_DAY")
    private Integer bankDepositDueDay;

    /** 배송완료 처리 기준일(일). */
    @Column(name = "SHIPPING_COMPLETE_DATE")
    private String shippingCompleteDate;

    /** SmartDelivery 연동 기준일. */
    @Column(name = "SMART_DELIVERY_CRITERIA_DATE")
    private String smartDeliveryCriteriaDate;

    // ---------------------------------------------------------------- 전환추적 스크립트(광고 픽셀)

    @Column(name = "TAG_OVERTURE")
    private String tagOverture;

    @Column(name = "TAG_ADWORDS")
    private String tagAdwords;

    // ---------------------------------------------------------------- 주문 임시저장 관련 설정

    /** 주문/장바구니 임시저장 데이터 보관일(일). */
    @Column(name = "RETENTION_PERIOD")
    private Integer retentionPeriod;

    /** 구매확정 요청 기준일. */
    @Column(name = "CONFIRM_PURCHASE_REQUEST_DATE")
    private String confirmPurchaseRequestDate;

    /** 구매 자동확정 처리 기준일. */
    @Column(name = "CONFIRM_PURCHASE_DATE")
    private String confirmPurchaseDate;

    /** 임시저장/대체 처리 방식(0/1 등 코드). */
    @Column(name = "ALTERNATE_SYSTEM")
    private Integer alternateSystem;
}

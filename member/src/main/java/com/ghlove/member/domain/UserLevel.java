package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원등급 (AS-IS OP_USER_LEVEL) - D11 UserLevelManagerController,
 * docs/as-is-admin-gap-deep-audit-part2.md 배치D D11 참고. GROUP_CODE는 라이브 데이터가
 * 전부 'default' 단일값이라(그룹관리 자체가 실질적으로 미사용) 이번 라운드는 그룹 CRUD는
 * 스코프아웃하고 등급(LEVEL) CRUD만 구현한다 - 화면/API에는 GROUP_CODE를 그대로 저장하되
 * 항상 "default"로 고정한다.
 */
@Entity
@Table(name = "OP_USER_LEVEL")
@Getter
@Setter
@NoArgsConstructor
public class UserLevel {

    public static final String DEFAULT_GROUP_CODE = "default";

    @Id
    @Column(name = "LEVEL_ID")
    private Integer levelId;

    @Column(name = "GROUP_CODE")
    private String groupCode;

    /** 등급 단계(낮을수록 상위등급으로 추정 - AS-IS 정렬 기준). */
    @Column(name = "DEPTH")
    private Integer depth;

    @Column(name = "LEVEL_NAME")
    private String levelName;

    /** 등급 아이콘 파일명(업로드된 이미지). */
    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "PRICE_START")
    private Integer priceStart;

    @Column(name = "PRICE_END")
    private Integer priceEnd;

    @Column(name = "DISCOUNT_RATE")
    private Double discountRate;

    @Column(name = "POINT_RATE")
    private Double pointRate;

    @Column(name = "SHIPPING_COUPON_COUNT")
    private Integer shippingCouponCount;

    @Column(name = "RETENTION_PERIOD")
    private Integer retentionPeriod;

    @Column(name = "REFERENCE_PERIOD")
    private Integer referencePeriod;

    @Column(name = "EXCEPT_REFERENCE_PERIOD")
    private Integer exceptReferencePeriod;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

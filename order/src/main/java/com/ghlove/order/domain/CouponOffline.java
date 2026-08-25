package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 오프라인(코드입력형) 쿠폰 발급분 (AS-IS OP_COUPON_OFFLINE). Coupon.offlineFlag='Y'인 쿠폰에
 *  대해 미리 코드 묶음을 생성해두고, 사용자가 코드를 직접 입력해 다운로드하면 USER_ID/사용여부가 채워진다. */
@Entity
@Table(name = "OP_COUPON_OFFLINE")
@Getter
@Setter
@NoArgsConstructor
public class CouponOffline {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opCouponOfflineIdSeq")
    @SequenceGenerator(name = "opCouponOfflineIdSeq", sequenceName = "op_coupon_offline_coupon_offline_id_seq", allocationSize = 1)
    @Column(name = "COUPON_OFFLINE_ID")
    private Integer couponOfflineId;

    @Column(name = "COUPON_ID")
    private Integer couponId;

    /** 코드가 발급(등록)된 시점엔 0, 회원이 코드를 입력해 다운로드하면 그 회원ID로 채워진다. */
    @Column(name = "USER_ID")
    private Long userId = 0L;

    @Column(name = "COUPON_OFFLINE_CODE")
    private String offlineCode;

    @Column(name = "COUPON_USED_FLAG")
    private String usedFlag = "N";

    @Column(name = "COUPON_USED_DATE")
    private String usedDate;

    @Column(name = "PUBLISHED_DATE")
    private String publishedDate;
}

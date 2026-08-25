package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사이트 설정 (AS-IS opmanager/config/site-config, shop-config, deny/edit 통합) - 원본
 *  OP_CONFIG는 단일 행에 PG/포인트/쿠폰/세금 등 saleson 범용 이커머스 설정까지 전부
 *  담고 있지만, 이 프로젝트는 실제 관련 있는 사이트 정보/SEO/가입차단ID/금지어만 관리한다
 *  (PAYMENT_, POINT_, TAX_ 계열 컬럼 등은 이 프로젝트의 point/order 서비스가 각자
 *  실제 로직을 갖고 있어 saleson 범용 설정과 안 맞음 - 매핑하지 않음). 나머지 NOT NULL
 *  컬럼은 DDL 시드에서 합리적 기본값으로 채워둔다. */
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
}

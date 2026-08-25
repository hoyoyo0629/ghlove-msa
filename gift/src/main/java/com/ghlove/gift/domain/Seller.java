package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 답례품 제공업체 (AS-IS opmanager/seller "입점업체관리"). 처음엔 답례품몰 GNB "마을기업관"
 *  필터용(COMMUNITY_BUSINESS_YN)과 admin 정산화면의 입금계좌 표시용 필드만 옮겼지만,
 *  이번 라운드에서 admin의 입점업체관리 CRUD 화면을 위해 전체 필드를 채운다. 판매자
 *  자기서비스 포털(로그인해서 자기 상품/주문 관리)은 openmarket과 동일하게 별도
 *  프론트엔드 접점이 필요해 이 라운드에서도 범위 밖 - admin 콘솔에서 하는 등록/승인/
 *  정보수정 관리만 구현한다. */
@Entity
@Table(name = "OP_SELLER")
@Getter
@Setter
@NoArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opSellerIdSeq")
    @SequenceGenerator(name = "opSellerIdSeq", sequenceName = "op_seller_seller_id_seq", allocationSize = 1)
    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "COMMUNITY_BUSINESS_YN")
    private String communityBusinessYn;

    @Column(name = "SELLER_NAME")
    private String sellerName;

    @Column(name = "BANK_NAME")
    private String bankName;

    @Column(name = "BANK_IN_NAME")
    private String bankInName;

    @Column(name = "BANK_ACCOUNT_NUMBER")
    private String bankAccountNumber;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ADDRESS_DETAIL")
    private String addressDetail;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "REPRESENTATIVE_NAME")
    private String representativeName;

    @Column(name = "BUSINESS_NUMBER")
    private String businessNumber;

    @Column(name = "BUSINESS_LOCATION")
    private String businessLocation;

    @Column(name = "COMMISSION_RATE")
    private Double commissionRate;

    /** 1:일정산, 2:주정산, 3:15일정산, 4:월정산. */
    @Column(name = "REMITTANCE_TYPE")
    private String remittanceType;

    /** 0:승인대기, 1:승인, 2:반려, 3:중지 (AS-IS 코드 관행 재사용). */
    @Column(name = "STATUS_CODE")
    private String statusCode;

    /** yyyyMMddHHmmss. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "CREATED_USER_ID")
    private Long createdUserId;

    /** yyyyMMddHHmmss. */
    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    @Column(name = "UPDATED_USER_ID")
    private Long updatedUserId;
}

package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 마이페이지 "배송지 관리" 주소록. AS-IS OP_USER_DELIVERY (배치 스캔으로 이미 존재하던
 *  테이블, PK 시퀀스만 이번에 추가) 그대로 재사용. */
@Entity
@Table(name = "OP_USER_DELIVERY")
@Getter
@Setter
@NoArgsConstructor
public class UserDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_DELIVERY_ID")
    private Long userDeliveryId;

    @Column(name = "USER_ID")
    private Long userId;

    /** Y/N. */
    @Column(name = "DEFAULT_FLAG")
    private String defaultFlag;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "NEW_ZIPCODE")
    private String newZipcode;

    @Column(name = "ZIPCODE")
    private String zipcode;

    @Column(name = "SIDO")
    private String sido;

    @Column(name = "SIGUNGU")
    private String sigungu;

    @Column(name = "EUPMYEONDONG")
    private String eupmyeondong;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ADDRESS_DETAIL")
    private String addressDetail;

    /** yyyyMMddHHmmss (AS-IS 레거시 컬럼이 VARCHAR(14)). */
    @Column(name = "CREATED_DATE")
    private String createdDate;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 답례품 옵션 (SFR-005 "카탈로그 관리: 카테고리, 옵션, 규격/구성..."). AS-IS
 * OP_ITEM_OPTION은 선택형(S)/조합형(S2·S3)/텍스트형(T)을 지원하며, MSA도 AS-IS와
 * 동일하게 전 옵션형태를 구현한다(docs/gift-option-redesign-plan.md). 판매자 등록화면에서
 * S2·T는 AS-IS처럼 숨김 처리하지만 데이터/서비스/구매흐름은 전부 동작한다.
 * 조합형은 optionName1/2/3 조합 한 줄이 한 옵션행(itemOptionId)이 된다.
 */
@Entity
@Table(name = "OP_ITEM_OPTION")
@Getter
@Setter
@NoArgsConstructor
public class GiftOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_OPTION_ID")
    private Long itemOptionId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    /** 옵션형태 S/S2/S3/T (아이템의 itemOptionType과 일치). */
    @Column(name = "OPTION_TYPE")
    private String optionType;

    /** 옵션 표시형태(드롭다운 등) - AS-IS optionDisplayType. */
    @Column(name = "OPTION_DISPLAY_TYPE")
    private String optionDisplayType;

    /** 옵션 숨김 여부 (Y/N) - AS-IS optionHideFlag. */
    @Column(name = "OPTION_HIDE_FLAG")
    private String optionHideFlag;

    /** S: 단일값 / S2: 1단계값 / S3: 1단계값. (조합형은 name1→name2[→name3] 종속) */
    @Column(name = "OPTION_NAME1")
    private String optionName1;

    /** S2/S3의 2단계값. */
    @Column(name = "OPTION_NAME2")
    private String optionName2;

    /** S3의 3단계값. */
    @Column(name = "OPTION_NAME3")
    private String optionName3;

    /** 추가금액 (기본가에 더해지는 금액, 음수 불가). */
    @Column(name = "OPTION_PRICE")
    private Integer optionPrice;

    /** 옵션 원가 - AS-IS optionCostPrice. */
    @Column(name = "OPTION_COST_PRICE")
    private Integer optionCostPrice;

    /** 비회원 추가금액 - AS-IS optionPriceNonmember. */
    @Column(name = "OPTION_PRICE_NONMEMBER")
    private Integer optionPriceNonmember;

    @Column(name = "OPTION_STOCK_FLAG")
    private String optionStockFlag;

    @Column(name = "OPTION_STOCK_QUANTITY")
    private Integer optionStockQuantity;

    /** 재고 연동 코드 - AS-IS optionStockCode. */
    @Column(name = "OPTION_STOCK_CODE")
    private String optionStockCode;

    /** 입고예정일 (yyyyMMdd, 재고연동 시) - AS-IS optionStockScheduleDate. */
    @Column(name = "OPTION_STOCK_SCHEDULE_DATE")
    private String optionStockScheduleDate;

    /** 입고예정 안내문 - AS-IS optionStockScheduleText. */
    @Column(name = "OPTION_STOCK_SCHEDULE_TEXT")
    private String optionStockScheduleText;

    @Column(name = "OPTION_SOLD_OUT_FLAG")
    private String optionSoldOutFlag;

    @Column(name = "OPTION_DISPLAY_FLAG")
    private String optionDisplayFlag;

    @Column(name = "CREATED_USER_ID")
    private Long createdUserId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

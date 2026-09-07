package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 답례품. Maps a subset of the AS-IS OP_ITEM columns (a ~150-column generic
 * legacy shopping-mall product table) - only the columns this service
 * actually populates/reads. DATA_STATUS_CODE doubles as the 지자체 승인
 * workflow status (PENDING/APPROVED/REJECTED/STOPPED, see OP_COMMON_CODE
 * GIFT_STATUS) since the AS-IS schema has no dedicated approval-status
 * column. CATEGORY_CODE is a new column (added in the MVP DDL migration)
 * since OP_ITEM's own categorization goes through tables this service
 * doesn't use.
 */
@Entity
@Table(name = "OP_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "itemIdSeq")
    @SequenceGenerator(name = "itemIdSeq", sequenceName = "op_item_item_id_seq", allocationSize = 1)
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "ITEM_SUMMARY")
    private String itemSummary;

    @Column(name = "DETAIL_CONTENT")
    private String detailContent;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "SALE_PRICE")
    private Integer salePrice;

    @Column(name = "STOCK_QUANTITY")
    private Integer stockQuantity;

    @Column(name = "SOLD_OUT")
    private String soldOut;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    /** ALWAYS(상시) / LIMITED(한시) - OP_COMMON_CODE GIFT_DISPLAY_TYPE. */
    @Column(name = "DISPLAY_TYPE")
    private String displayType;

    /** yyyyMMdd - DISPLAY_TYPE=LIMITED일 때만 의미 있음. */
    @Column(name = "DISPLAY_START_DATE")
    private String displayStartDate;

    @Column(name = "DISPLAY_END_DATE")
    private String displayEndDate;

    /** 이 답례품을 받으려면 필요한 최소 기부금액. */
    @Column(name = "MIN_DONATION_AMOUNT")
    private Integer minDonationAmount;

    /**
     * 배송비/택배사 설정 (SFR-005 "배송비·택배사 설정, 배송정책" - 재검토 라운드에서 신규
     * 매핑, AS-IS OP_ITEM엔 원래 있던 컬럼이었으나 지금까지 코드가 아예 매핑하지 않고
     * 있었다). AS-IS의 배송정책 전체(출고지/반송지/묶음배송/개당배송비 등)를 다 재현하지
     * 않고 order가 실제로 참조할 수 있는 핵심만 다룬다: 택배사명, 배송비구분(GIFT_SHIPPING_TYPE
     * 공통코드 1~6), 기본배송비, 조건부무료배송 기준금액, 제주/도서산간 추가배송비, 반품가능여부.
     */
    @Column(name = "DELIVERY_COMPANY_NAME")
    private String deliveryCompanyName;

    /** 1:무료배송 2:판매자조건부 3:출고지조건부 4:상품조건부 5:개당배송비 6:고정배송비 (GIFT_SHIPPING_TYPE). */
    @Column(name = "SHIPPING_TYPE")
    private String shippingType;

    @Column(name = "SHIPPING")
    private Integer shipping;

    @Column(name = "SHIPPING_FREE_AMOUNT")
    private Integer shippingFreeAmount;

    @Column(name = "SHIPPING_EXTRA_CHARGE1")
    private Integer shippingExtraCharge1;

    @Column(name = "SHIPPING_EXTRA_CHARGE2")
    private Integer shippingExtraCharge2;

    @Column(name = "ITEM_RETURN_FLAG")
    private String itemReturnFlag;

    /** 대표상품 여부 (AS-IS opmanager/item/representative-item - 실제 운영사이트에 살아있는
     *  기능, view_search_spcl_item과는 무관). 'Y'면 대표상품 목록에 노출된다. */
    @Column(name = "REPRESENTATIVE_ITEM_YN")
    private String representativeItemYn;

    /** 브랜드 ID (NULL: 자사제품, 그외: OP_BRAND 참고) - shop-statistics 브랜드별 통계용. */
    @Column(name = "BRAND_ID")
    private Integer brandId;

    /** 상품라벨 (1:없음, 2:NEW, 3:SALE, 4:사기) - AS-IS OP_ITEM.ITEM_LABEL, NOT NULL DEFAULT '1'.
     *  JPA가 매핑하는 컬럼은 항상 INSERT에 포함되므로 이 필드를 추가한 이상 모든 생성 경로
     *  (register/adminCreate/copy)에서 명시적으로 채워야 한다 - 안 그러면 NOT NULL 위반. */
    @Column(name = "ITEM_LABEL")
    private String itemLabel;

    /** 관리자 목록 노출순서 - AS-IS OP_ITEM에는 없는 이 MVP 전용 신규 컬럼(NULLABLE, DEFAULT 0).
     *  값이 작을수록 상단 노출, 초기값 0이라 명시적으로 순서를 옮기기 전까지는 itemId desc와
     *  동일하게 정렬된다(admin-console-item-mgmt-round #2 순서변경). */
    @Column(name = "ADMIN_ORDERING")
    private Integer adminOrdering;
}

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

    /** 1회 주문 최대 수량 (AS-IS item.orderMaxQuantity) - 답례품 상세 "최대 주문 수량" 표시·제한용. */
    @Column(name = "ORDER_MAX_QUANTITY")
    private Integer orderMaxQuantity;

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

    /**
     * 배송정책 전체 재현(G1~G7). 위 핵심 필드에 더해 AS-IS Shipping.getShippingGroups()가
     * 쓰는 나머지 컬럼을 매핑한다 - order가 묶음배송/개당배송비/본사·업체배송까지 계산한다.
     */
    /** 개당배송비(shippingType=5) BOX 기준수량. AS-IS boxCount = ceil(총수량/shippingItemCount). */
    @Column(name = "SHIPPING_ITEM_COUNT")
    private Integer shippingItemCount;

    /** 묶음배송 그룹코드. 같은 코드끼리 한 번의 배송비로 묶는다(type 1·6은 개별배송). */
    @Column(name = "SHIPPING_GROUP_CODE")
    private String shippingGroupCode;

    /** 물류통합(출고지조건부 type3) 그룹코드. 조건부무료 기준금액을 이 그룹 전체 합계로 본다. */
    @Column(name = "SHIPMENT_GROUP_CODE")
    private String shipmentGroupCode;

    /** 반품/교환 배송비(편도 기준). AS-IS 클레임에서 왕복 배송비 산정에 쓴다. */
    @Column(name = "SHIPPING_RETURN")
    private Integer shippingReturn;

    /** 배송구분 1:본사배송 2:업체배송. 본사배송이면 정산 대상 판매자를 운영자(본사)로 잡는다. */
    @Column(name = "DELIVERY_TYPE")
    private String deliveryType;

    /** 반품 가능여부 (Y/N, 기본 Y). AS-IS는 이 값이 'Y'일 때만 교환·반품 버튼을 보여준다. */
    @Column(name = "ITEM_RETURN_FLAG")
    private String itemReturnFlag;

    /**
     * 모바일상품(교환권) 여부 (Y/N, 기본 N). 실물 배송이 없는 상품이라 AS-IS는
     * 교환·반품 대상에서 제외한다(`mypage/orderList.html:318,321`).
     */
    @Column(name = "MOBILE_ITEM_YN")
    private String mobileItemYn;

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

    // ── 옵션 다형 체계 (AS-IS OP_ITEM 옵션/각인 컬럼, docs/gift-option-redesign-plan.md) ──

    /** 옵션 사용여부 (Y/N). AS-IS itemOptionFlag. */
    @Column(name = "ITEM_OPTION_FLAG")
    private String itemOptionFlag;

    /** 옵션형태 S(선택형)/S2(2조합형)/S3(3조합형)/T(텍스트형). AS-IS itemOptionType.
     *  판매자 화면에선 S2·T를 AS-IS처럼 숨기지만 데이터/서비스는 전부 지원. */
    @Column(name = "ITEM_OPTION_TYPE")
    private String itemOptionType;

    /** 필수 추가정보(각인) 사용여부 (Y/N). AS-IS itemTextOptionFlag - itemOptionType=T와 별개. */
    @Column(name = "ITEM_TEXT_OPTION_FLAG")
    private String itemTextOptionFlag;

    /** 필수 추가정보 제목1~3 (구매자에게 입력받을 텍스트 제목, 쉼표구분, `: | < >` 금지). */
    @Column(name = "ITEM_TEXT_OPTION_TITLE1")
    private String itemTextOptionTitle1;

    @Column(name = "ITEM_TEXT_OPTION_TITLE2")
    private String itemTextOptionTitle2;

    @Column(name = "ITEM_TEXT_OPTION_TITLE3")
    private String itemTextOptionTitle3;

    /** 상품데이터 형태 (1:일반상품, 2:추가구성상품) - AS-IS itemDataType. 추가구성은 이 값이 2인
     *  별도 답례품으로 만들어져 본품과 op_item_addition으로 연결된다. */
    @Column(name = "ITEM_DATA_TYPE")
    private String itemDataType;

    /** 추가구성 사용여부 (Y/N) - AS-IS itemAdditionFlag. */
    @Column(name = "ITEM_ADDITION_FLAG")
    private String itemAdditionFlag;
}

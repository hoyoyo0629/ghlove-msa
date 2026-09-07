package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 답례품 옵션 (SFR-005 "카탈로그 관리: 카테고리, 옵션, 규격/구성..."). AS-IS
 * OP_ITEM_OPTION은 선택형/조합형(최대 3단계)/텍스트형까지 지원하지만, 이 MVP는
 * 카탈로그 관리(등록/수정/삭제, 옵션별 추가금액·재고 표시)만 다룬다 - 실제 장바구니/
 * 주문(order 서비스)은 아직 옵션 개념이 없어 옵션 선택이 구매에 반영되지는 않는다
 * (SFR-005 재검토 라운드에서 의도적으로 이렇게 범위를 좁혔다 - 장바구니/주문/Kafka
 * 이벤트까지 다시 설계해야 하는 훨씬 큰 작업이라 별도 판단 필요).
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

    /** 이 프로젝트는 단일 선택형(S)만 다룬다 - 조합형(S2/S3)/텍스트형(T)은 스코프 밖. */
    @Column(name = "OPTION_TYPE")
    private String optionType;

    @Column(name = "OPTION_NAME1")
    private String optionName1;

    /** 추가금액 (기본가에 더해지는 금액, 음수 불가). */
    @Column(name = "OPTION_PRICE")
    private Integer optionPrice;

    @Column(name = "OPTION_STOCK_FLAG")
    private String optionStockFlag;

    @Column(name = "OPTION_STOCK_QUANTITY")
    private Integer optionStockQuantity;

    @Column(name = "OPTION_SOLD_OUT_FLAG")
    private String optionSoldOutFlag;

    @Column(name = "OPTION_DISPLAY_FLAG")
    private String optionDisplayFlag;

    @Column(name = "CREATED_USER_ID")
    private Long createdUserId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

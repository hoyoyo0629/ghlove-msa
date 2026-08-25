package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 답례품 통계용 ReadModel - gift.lifecycle 이벤트를 구독해 쌓는 로컬 사본 (itemId로 upsert, "현재 상태" 사본). */
@Entity
@Table(name = "STAT_GIFT_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class GiftLedgerStat {

    @Id
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "DATA_STATUS_CODE")
    private String dataStatusCode;

    @Column(name = "SALE_PRICE")
    private Integer salePrice;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    /** report/mctpv,lclgv "지자체별 답례품 제공현황" 탭용. */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;
}

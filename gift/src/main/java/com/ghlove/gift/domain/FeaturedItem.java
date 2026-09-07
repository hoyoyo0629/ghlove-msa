package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기획전에 소속된 답례품 목록 (AS-IS opmanager/featured - FeaturedItem). OP_FEATURED_ITEM. */
@Entity
@Table(name = "OP_FEATURED_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class FeaturedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "featuredItemRegSeq")
    @SequenceGenerator(name = "featuredItemRegSeq", sequenceName = "op_featured_item_reg_seq_seq", allocationSize = 1)
    @Column(name = "REG_SEQ")
    private Long regSeq;

    @Column(name = "FEATURED_ID")
    private Integer featuredId;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 스타일북에 소속된 답례품. Maps OP_STYLE_BOOK_ITEM. */
@Entity
@Table(name = "OP_STYLE_BOOK_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class StyleBookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "styleBookItemIdSeq")
    @SequenceGenerator(name = "styleBookItemIdSeq", sequenceName = "op_style_book_item_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STYLE_BOOK_ID")
    private Long styleBookId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}

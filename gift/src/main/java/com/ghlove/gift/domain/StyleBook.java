package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 스타일북(답례품 큐레이션 묶음) - AS-IS opmanager/style-book - StyleBookManagerController.
 * Maps OP_STYLE_BOOK. */
@Entity
@Table(name = "OP_STYLE_BOOK")
@Getter
@Setter
@NoArgsConstructor
public class StyleBook {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "styleBookIdSeq")
    @SequenceGenerator(name = "styleBookIdSeq", sequenceName = "op_style_book_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "IMAGE")
    private String image;

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

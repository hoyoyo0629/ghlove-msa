package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관심답례품 (마이페이지 "관심답례품"). Maps a subset of the AS-IS OP_WISHLIST columns. */
@Entity
@Table(name = "OP_WISHLIST")
@Getter
@Setter
@NoArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opWishlistIdSeq")
    @SequenceGenerator(name = "opWishlistIdSeq", sequenceName = "op_wishlist_wishlist_id_seq", allocationSize = 1)
    @Column(name = "WISHLIST_ID")
    private Integer wishlistId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}

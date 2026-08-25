package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 답례품 리뷰. AS-IS OP_ITEM_REVIEW (empty, batch-generated) subset - point
 * reward fields (POINT_PAYMENT/POINT/POINT_PAYMENT_DATE) exist in the legacy
 * table but reviews don't earn points in this MVP, so they're left unmapped.
 */
@Entity
@Table(name = "OP_ITEM_REVIEW")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_REVIEW_ID")
    private Long itemReviewId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "ORDER_CODE")
    private String orderCode;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "SCORE")
    private Integer score;

    @Column(name = "RECOMMEND_FLAG")
    private String recommendFlag;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}

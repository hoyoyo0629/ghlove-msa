package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "OP_ITEM_REVIEW_IMAGE")
@Getter
@Setter
@NoArgsConstructor
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_REVIEW_IMAGE_ID")
    private Long itemReviewImageId;

    @Column(name = "ITEM_REVIEW_ID")
    private Long itemReviewId;

    @Column(name = "REVIEW_IMAGE")
    private String reviewImage;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
